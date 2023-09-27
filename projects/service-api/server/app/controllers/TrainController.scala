package controllers

import java.time.{Instant, LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.UUID

import controllers.auth.{AuthAction, Permission}
import io.masterypath.slick.{
  CodeExercise,
  FlashcardType,
  MapID,
  Node,
  NodeID,
  OrgID,
  Post,
  PostRead,
  ProfileID,
  SegmentID
}
import javax.inject._
import models.flashcard.dto.{CardGradeAnswer, CardMetaData, CardsDue, FlashcardTypeBrief, Quality, TestAnswer}
import models.flashcard.exercise.dto.ExerciseAnswer
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc._
import utils.controller.ControllerHelperFunctions
import models.flashcard.{AnswerChoices, ChildCard, Flashcard, FlashcardMode}
import models.mindmap.PostInfo
import models.mindmap.dto.PostTimeRead
import models.organization.Member
import services.{DockerService, FlashcardRepoService, MindMapRepoService, PermissionService}

import scala.concurrent.{ExecutionContext, Future}

class TrainController @Inject()(
    permission: PermissionService,
    mindMapRepo: MindMapRepoService,
    flashcardRepo: FlashcardRepoService,
    cc: MessagesControllerComponents,
    authAction: AuthAction,
    dockerService: DockerService,
    mapController: MapController,
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc)
    with ControllerHelperFunctions
    with I18nSupport {

  def getCardAsJSON(orgID: OrgID, mapID: MapID, nodeID: NodeID): Action[AnyContent] =
    authAction.async { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.training,
            permission.MapsLocal.training(orgID, mapID)
          )),
        (_: Member) => {
          getFlashcardAndDo(mapID, nodeID) { flashcard =>
            mapController.getFlashcardAsJSON(flashcard, mapID, FlashcardMode.TEST)
          }
        }
      )
    }

  def getCardTypeAsJSON(orgID: OrgID, mapID: MapID, nodeID: NodeID): Action[AnyContent] =
    authAction.async { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.training,
            permission.MapsLocal.training(orgID, mapID)
          )),
        (_: Member) => {
          getFlashcardAndDo(mapID, nodeID) { flashcard =>
            Future.successful(
              Ok(Json.toJsObject(FlashcardTypeBrief(
                id = flashcard.cardType.id,
                cardType = flashcard.cardType.cardType,
                name = flashcard.cardType.name,
                commonName = flashcard.cardType.commonName,
                description = flashcard.cardType.description,
              ))))
          }
        }
      )
    }

  def cardsToJSON(orgID: OrgID, mapID: MapID, segmentID: SegmentID): Action[AnyContent] = authAction.async {
    implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.training,
            permission.MapsLocal.training(orgID, mapID)
          )),
        (member: Member) => {
          getCardsAsJSON(member, mapID, segmentID)
        }
      )
  }

  def getCardsAsJSON(member: Member, mapID: MapID, segmentID: SegmentID): Future[Result] = {
    def nodePrecedence(nodeType: String): Int = {
      val node_precedence = Map("root" -> 99, "flashcard" -> 0, "category" -> 1, "mindmap" -> 2)
      node_precedence(nodeType)
    }

    def getDueDates(cards: Seq[CardMetaData]): Seq[(String, Int)] = {
      val accumulator: Map[String, Int] = Map()
      val formatter                     = DateTimeFormatter.ofPattern(("YYYYMMdd:EEEE, MMMM dd"))
      val dates = cards.foldLeft(accumulator)((acc, card) => {
        val timestamp: Instant =
          if (card.due < Instant.now.getEpochSecond) Instant.now else Instant.ofEpochSecond(card.due)
        val date = formatter.format(LocalDateTime.ofInstant(timestamp, ZoneId.of("UTC")))
        acc + (date -> (acc.getOrElse(date, 0) + 1))
      })
      dates.toSeq.sortWith(_._1 < _._1)
    }

    def cardsDueDTO(root: Node, children: Seq[ChildCard]): CardsDue = {
      val cards = flashcardRepo
        .getCardsDue(root.id, children.sortBy(x => (nodePrecedence(x.node.`type`), x.node.order)))
      CardsDue(cards = cards.filter(x => x.due <= Instant.now.getEpochSecond || x.due == 0),
               dueDates = getDueDates(cards))
    }

    mindMapRepo.getMapRootNode(mapID, segmentID).flatMap {
      case Some(root) =>
        for {
          children <- flashcardRepo.getChildCardsDeep(member, root.path)
        } yield Ok(Json.toJson(cardsDueDTO(root, children)))
      case None => Future.successful(BadRequest(Json.obj("message" -> "NOT FOUND")))
    }
  }

  def getPostsReadTimes(orgID: OrgID, mapID: MapID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.MapsGlobal.training,
          permission.MapsLocal.training(orgID, mapID)
        )),
      (_: Member) => {
        mindMapRepo.getPostsByMapID(mapID).map { result =>
          Ok(postsInfoToJson(result))
        }
      }
    )
  }

  private def postsInfoToJson(posts: Seq[PostInfo]) = {
    val postsInfoJson = posts.map(
      x =>
        PostTimeRead(
          postID = x.post.id.toString,
          timeRead = x.postRead.map(_.timeRead).getOrElse(0)
      ))
    Json.toJson(postsInfoJson)
  }

  def submitCardQuality(orgID: OrgID, mapID: MapID, nodeID: NodeID): Action[JsValue] =
    authAction.async(parse.json) { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.training,
            permission.MapsLocal.training(orgID, mapID)
          )),
        (member: Member) => {
          validateJSON[Quality](
            request.body.validate[Quality], { json =>
              getFlashcardAndDo(mapID, nodeID) { _ =>
                reportUpdateStatus(
                  numberOfUpdates = flashcardRepo.log_quality(member, nodeID, mapID, json.quality),
                  returnJSON = None
                )
              }
            }
          )
        }
      )
    }

  def markPostRead(orgID: OrgID, mapID: MapID, nodeID: NodeID): Action[AnyContent] =
    authAction.async { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.training,
            permission.MapsLocal.training(orgID, mapID)
          )),
        (member: Member) => {
          val profileID = member.profile.id
          mindMapRepo.getPost(nodeID, mapID).flatMap {
            case Some(post) => markPostRead(post, profileID)
            case None       => Future.successful(NotFound("Error, Not Found"))
          }
        }
      )
    }

  def getPostAsJSON(orgID: OrgID, mapID: MapID, nodeID: NodeID): Action[AnyContent] =
    authAction.async { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.training,
            permission.MapsLocal.training(orgID, mapID)
          )),
        (_: Member) => {
          mindMapRepo.getPost(nodeID, mapID).map {
            case Some(post) => Ok(Json.toJson(post))
            case None       => NotFound("")
          }
        }
      )
    }

  def gradeCard(orgID: OrgID, mapID: MapID, nodeID: NodeID, cardType: String): Action[JsValue] =
    authAction.async(parse.json) { implicit request =>
      def gradeBasic(member: Member) =
        validateJSON[TestAnswer](
          request.body.validate[TestAnswer],
          json => gradeTestAnswers(member, nodeID, mapID, json.choices, json.seconds)
        )

      def gradeCodeExercise(member: Member) =
        validateJSON[ExerciseAnswer](
          request.body.validate[ExerciseAnswer],
          json =>
            getCodeExerciseAndDo(nodeID) { (exercise, cardType) =>
              codeRunner(member, cardType, nodeID, code = json.code, test = exercise.test, json.seconds)
          }
        )

      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.training,
            permission.MapsLocal.training(orgID, mapID)
          )),
        (member: Member) => {
          getFlashcardAndDo(mapID, nodeID) { _ =>
            cardType match {
              case "basic"         => gradeBasic(member)
              case "code_exercise" => gradeCodeExercise(member)
              case _ =>
                Future.successful(BadRequest(Json.obj("message" -> s"""Flashcard type: '$cardType' is not valid""")))
            }
          }
        }
      )
    }

  private def markPostRead[A](post: Post, profileID: ProfileID) = {
    val timestamp = Instant.now.getEpochSecond
    val postRead  = PostRead(id = NodeID(post.id), profileID = profileID, timeRead = timestamp)
    mindMapRepo.insertOrUpdatePostRead(postRead).map { _ =>
      Ok("")
    }
  }

  private def codeRunner(member: Member,
                         cardType: FlashcardType,
                         nodeID: NodeID,
                         code: String,
                         test: String,
                         seconds: Int): Future[Result] = {
    dockerService.dockerRunCode(cardType, code, test).flatMap { result =>
      if (result.pass) {
        flashcardRepo.logAnswer(member, nodeID, correct = true, seconds).map { _ =>
          Ok(
            Json.toJson(CardGradeAnswer(correct = true, message = Some(result.output)))
          )
        }
      } else {
        flashcardRepo.logAnswer(member, nodeID, correct = false, seconds).map { _ =>
          Ok(Json.toJson(CardGradeAnswer(correct = false, message = Some(result.output))))
        }
      }
    }
  }

  private def gradeTestAnswers(member: Member,
                               nodeID: NodeID,
                               mapID: MapID,
                               userTestAnswers: Seq[UUID],
                               seconds: Int): Future[Result] = {
    def gradeAndReport(answers: AnswerChoices): Future[Result] = {
      if (answers.userSelectedAllCorrectChoices(userTestAnswers)) {
        flashcardRepo.logAnswer(member, nodeID, correct = true, seconds).map { _ =>
          Ok(Json.toJson(CardGradeAnswer(correct = true)))
        }
      } else {
        flashcardRepo.logAnswer(member, nodeID, correct = false, seconds).map { _ =>
          Ok(Json.toJson(CardGradeAnswer(correct = false, answers = Some(answers.correctChoices))))
        }
      }
    }

    flashcardRepo.getAnswerChoices(nodeID, mapID).flatMap { answers =>
      gradeAndReport(answers)
    }
  }

  private def getFlashcardAndDo(mapID: MapID, nodeID: NodeID)(callback: Flashcard => Future[Result]): Future[Result] =
    flashcardRepo.getFlashcard(nodeID, mapID).flatMap {
      case Some(card) => callback(card)
      case None       => Future.successful(NotFound(Json.obj("message" -> "CARD NOT FOUND")))
    }

  private def getCodeExerciseAndDo(nodeID: NodeID)(
      callback: (CodeExercise, FlashcardType) => Future[Result]): Future[Result] =
    flashcardRepo.getCodeExerciseAndCardType(nodeID).flatMap {
      case Some((exercise, cardType)) => callback(exercise, cardType)
      case None                       => Future.successful(BadRequest(Json.obj("message" -> "EXERCISE NOT FOUND")))
    }

}
