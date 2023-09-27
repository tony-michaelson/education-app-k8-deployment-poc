package controllers

import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.time.Instant
import java.util.UUID
import controllers.auth.{AuthAction, Permission}
import io.masterypath.slick.{
  Card,
  CodeExercise,
  FlashcardType,
  FlashcardTypeID,
  MapID,
  MapRights,
  MapRightsID,
  MindMap,
  Node,
  NodeID,
  OrgID,
  Post,
  ProfileID,
  RoleID,
  SegmentID
}

import javax.inject._
import models.flashcard.dto.{CardBriefEdit, CardBriefTest, CardPost, FlashcardTypeBrief}
import models.flashcard.exercise.dto.{CodeExerciseBriefEdit, CodeExerciseBriefTest, Exercise, ExerciseUpdate}
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc._
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import utils.controller.ControllerHelperFunctions
import models.flashcard.{Flashcard, FlashcardMode}
import models.mindmap.dto.{
  MapProperties,
  MapPropertiesPatch,
  MapRightsBrief,
  MapRightsInvite,
  MapRightsPatch,
  MapRightsPost,
  NodePatch,
  NodePatchAttributes,
  NodePost,
  PostMarkdown
}
import models.organization.Member
import models.organization.dto.RoleInviteResponse
import services.{
  DockerService,
  FlashcardRepoService,
  MindMapRepoService,
  OrganizationRepoService,
  PermissionService,
  SendgridService,
  SpacesService
}

import scala.concurrent.{ExecutionContext, Future}
import scala.math.Ordering.Float
import com.sksamuel.scrimage._
import com.sksamuel.scrimage.nio.PngWriter
import play.api.Configuration
import play.api.libs.Files.TemporaryFile
import services.SpaceKind.{FLASHCARD_AUDIO, IMAGE}

class MapController @Inject()(
    permission: PermissionService,
    mindMapRepo: MindMapRepoService,
    flashcardRepo: FlashcardRepoService,
    orgRepo: OrganizationRepoService,
    cc: MessagesControllerComponents,
    email: SendgridService,
    authAction: AuthAction,
    dockerService: DockerService,
    spacesService: SpacesService,
    config: Configuration
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc)
    with ControllerHelperFunctions
    with I18nSupport {
  implicit val orderMethod: Float.TotalOrdering.type = Ordering.Float.TotalOrdering

  def mapContentToJson(orgID: OrgID, mapID: MapID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.MapsGlobal.view,
          permission.MapsLocal.view(orgID, mapID)
        )),
      (_: Member) => exportMapContent(mapID).map(Ok(_))
    )
  }

  private def exportMapContent(mapID: MapID): Future[JsValue] = {
    mindMapRepo.getMapRootNode(mapID, SegmentID(mapID)).flatMap {
      case Some(root) => getMapContentAsJson(root)
      case None       => Future.successful(Json.obj("message" -> "NOT FOUND"))
    }
  }

  private def getMapContentAsJson(root: Node): Future[JsValue] = {
    for {
      posts <- mindMapRepo.getMapPosts(root.mapID)
    } yield Json.toJson(mindMapRepo.getMapContentAsJson(root, posts))
  }

  def forkMap(orgID: OrgID, mapID: MapID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.MapsGlobal.view,
          permission.MapsLocal.view(orgID, mapID)
        )),
      (member: Member) =>
        mindMapRepo.forkMap(member, mapID).map {
          case Some(newMapID) => Ok(Json.obj("newMapID" -> newMapID.toString))
          case None           => severErrorMessage("Unable to fork map.")
      }
    )
  }

  def getMapAsBlogPage(orgID: OrgID, mapID: MapID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.MapsGlobal.publish,
          permission.MapsLocal.publish(orgID, mapID)
        )),
      (_: Member) => getMapAsBlogPage(mapID).map(Ok(_))
    )
  }

  private def getMapAsBlogPage(mapID: MapID): Future[JsValue] = {
    mindMapRepo.getMapRootNode(mapID, SegmentID(mapID)).flatMap {
      case Some(root) => generateBlogPage(root)
      case None       => Future.successful(Json.obj("message" -> "NOT FOUND"))
    }
  }

  private def generateBlogPage(root: Node): Future[JsValue] = {
    for {
      posts <- mindMapRepo.getMapPosts(root.mapID)
    } yield Json.toJson(mindMapRepo.generateBlogPage(root.id, posts))
  }

  def publishMap(orgID: OrgID, mapID: MapID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.MapsGlobal.publish,
          permission.MapsLocal.publish(orgID, mapID)
        )),
      (member: Member) =>
        mindMapRepo.publishMap(member, mapID).map {
          case Some(newMapID) => Ok(Json.obj("newMapID" -> newMapID.toString))
          case None           => severErrorMessage("Unable to publish map.")
      }
    )
  }

  def getMyMaps(orgID: OrgID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.MapsGlobal.create
        )),
      (member: Member) => {
        val profileID = member.profile.id
        mindMapRepo
          .getAllMapsByUserID(orgID, profileID)
          .map(
            mapList => Ok(Json.toJson(mapList))
          )
      }
    )
  }

  def getFlashcardTypes(orgID: OrgID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.MapsGlobal.modify
        )),
      (_: Member) => {
        flashcardRepo.getFlashcardTypes
          .map(
            seq =>
              Ok(
                Json.toJson(
                  seq.map(
                    row =>
                      FlashcardTypeBrief(
                        id = row.id,
                        cardType = row.cardType,
                        name = row.name,
                        commonName = row.commonName,
                        description = row.description,
                    ))
                ))
          )
      }
    )
  }

  def createMap(orgID: OrgID): Action[JsValue] = authAction.async(parse.json) { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.MapsGlobal.create
        )),
      (member: Member) => {
        validateJSON[MapProperties](
          request.body.validate[MapProperties], { json =>
            val profileID = member.profile.id
            val newMapID  = MapID.random
            val mindMap = MindMap(
              id = newMapID,
              orgID = orgID,
              icon = None,
              name = json.name,
              mode = json.mode.toString,
              description = json.description,
              cost = json.cost
            )
            reportUpdateStatus(
              numberOfUpdates = mindMapRepo.createUserMindMap(orgID, mindMap, profileID),
              returnJSON = Some(Json.obj("id" -> newMapID))
            )
          }
        )
      }
    )
  }

  def patchMapSettings(orgID: OrgID, mapID: MapID): Action[JsValue] =
    authAction.async(parse.json) { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.modify(orgID, mapID)
          )),
        (_: Member) => {
          validateJSON[MapPropertiesPatch](
            request.body.validate[MapPropertiesPatch], {
              json =>
                val mode = json.mode.flatMap { x =>
                  Option(x.toString)
                }
                val nodeUpdated = mindMapRepo.getMapSettingsByIDAndUpdate(mapID) { settings =>
                  settings.copy(
                    name = json.name getOrElse settings.name,
                    mode = mode getOrElse settings.mode,
                    icon = json.icon orElse settings.icon,
                    description = json.description orElse settings.description,
                    cost = json.cost orElse settings.cost,
                  )
                }
                nodeUpdated.flatMap { updateNumber =>
                  reportUpdateStatus(
                    numberOfUpdates = Future.successful(updateNumber),
                    returnJSON = None
                  )
                }
            }
          )
        }
      )
    }

  def getMapSettings(orgID: OrgID, mapID: MapID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.MapsGlobal.modify,
          permission.MapsLocal.modify(orgID, mapID)
        )),
      (_: Member) => {
        mindMapRepo
          .getMapSettingsByID(mapID)
          .map(
            settings => Ok(Json.toJson(settings))
          )
      }
    )
  }

  def uploadMapIcon(orgID: OrgID, mapID: MapID): Action[MultipartFormData[TemporaryFile]] =
    authAction.async(parse.multipartFormData) { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.view,
            permission.MapsLocal.view(orgID, mapID)
          )),
        (_: Member) => {
          request.body
            .file("icon")
            .map {
              picture =>
                // only get the last part of the filename
                // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
                val objectName = mapID.toString + ':' + MapID.random
                val filename   = mapID.toString
                val filePath   = Paths.get(s"/tmp/$filename")
                picture.ref.copyTo(filePath, replace = true)
                val bucketFile =
                  spacesService.uploadFileToBucket(objectName, new File(filePath.toString), IMAGE)
                if (bucketFile.s3Object.getKey == objectName) {
                  val publicUrl = bucketFile.url.toString
                  new File(filePath.toString).delete()
                  mindMapRepo.updateMapIcon(mapID, publicUrl).map {
                    case n if n > 0 => Ok("File uploaded")
                    case _          => InternalServerError("Unable to upload file.")
                  }
                } else {
                  Future.successful(InternalServerError("Unable to upload file."))
                }
            }
            .getOrElse {
              Future.successful(BadRequest(""))
            }
        }
      )
    }

  def storeImageFile(orgID: OrgID, mapID: MapID): Action[TemporaryFile] =
    authAction.async(parse.temporaryFile) { request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.modify(orgID, mapID)
          )),
        (_: Member) => {
          val maxHeight: Int = config.get[Int]("googleCloud.maxImageHeight")
          val maxWidth: Int  = config.get[Int]("googleCloud.maxImageWidth")

          val filename = mapID.uuid.toString + '-' + UUID.randomUUID() + ".png"
          val filePath = Paths.get("/tmp/" + filename)
          request.body.moveTo(filePath, replace = true)

          val png   = convertToPNG(filePath)
          val image = enforceImageMaxWidth(enforceImageMaxHeight(png, maxHeight), maxWidth)
          Files.write(filePath, image)
          val bucketFile = spacesService.uploadFileToBucket(filename, new File(filePath.toString), IMAGE)

          if (bucketFile.s3Object.getKey == filename) {
            val publicUrl = bucketFile.url.toString
            new File(filePath.getFileName.toString).delete()
            Future.successful(Ok(Json.obj("url" -> publicUrl)))
          } else {
            Future.successful(BadRequest(Json.obj("message" -> "Unable to upload file")))
          }
        }
      )
    }

  private def convertToPNG(path: Path): Array[Byte] = {
    ImmutableImage.loader().fromPath(path).bytes(PngWriter.NoCompression)
  }

  private def enforceImageMaxHeight(image: Array[Byte], maxHeight: Int): Array[Byte] = {
    val immutableImage = ImmutableImage.loader().fromBytes(image)
    if (immutableImage.height > maxHeight) {
      immutableImage.scaleToHeight(maxHeight).bytes(PngWriter.NoCompression)
    } else {
      image
    }
  }

  private def enforceImageMaxWidth(image: Array[Byte], maxWidth: Int): Array[Byte] = {
    val immutableImage = ImmutableImage.loader().fromBytes(image)
    if (immutableImage.width > maxWidth) {
      immutableImage.scaleToWidth(maxWidth).bytes(PngWriter.NoCompression)
    } else {
      image
    }
  }

  def mapToJson(orgID: OrgID, mapID: MapID, segmentID: SegmentID): Action[AnyContent] = authAction.async {
    implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.view,
            permission.MapsLocal.view(orgID, mapID)
          )),
        (member: Member) => {
          getMapAsJSON(mapID, segmentID, member.profile.id).map { Ok(_) }
        }
      )
  }

  private def getMapAsJSON(mapID: MapID, segmentID: SegmentID, profileID: ProfileID): Future[JsValue] = {
    mindMapRepo.getMapRootNode(mapID, segmentID).flatMap {
      case Some(root) => getMapAsJSONUsingRoot(root, profileID)
      case None       => Future.successful(Json.obj("message" -> "NOT FOUND"))
    }
  }

  private def consolidateMapRights(mapRights: Seq[MapRights]): MapRightsBrief =
    MapRightsBrief(
      admin = mapRights.foldLeft(false)((z: Boolean, a: MapRights) => if (a.admin || z) true else false),
      feedback = mapRights.foldLeft(false)((z: Boolean, a: MapRights) => if (a.feedback || z) true else false),
      mnemonics = mapRights.foldLeft(false)((z: Boolean, a: MapRights) => if (a.mnemonics || z) true else false),
      modify = mapRights.foldLeft(false)((z: Boolean, a: MapRights) => if (a.modify || z) true else false),
      permissions = mapRights.foldLeft(false)((z: Boolean, a: MapRights) => if (a.permissions || z) true else false),
      publish = mapRights.foldLeft(false)((z: Boolean, a: MapRights) => if (a.publish || z) true else false),
      share = mapRights.foldLeft(false)((z: Boolean, a: MapRights) => if (a.share || z) true else false),
      stats = mapRights.foldLeft(false)((z: Boolean, a: MapRights) => if (a.stats || z) true else false),
      training = mapRights.foldLeft(false)((z: Boolean, a: MapRights) => if (a.training || z) true else false),
      transfer = mapRights.foldLeft(false)((z: Boolean, a: MapRights) => if (a.transfer || z) true else false),
      view = mapRights.foldLeft(false)((z: Boolean, a: MapRights) => if (a.view || z) true else false),
    )

  private def getMapAsJSONUsingRoot(root: Node, profileID: ProfileID): Future[JsValue] = {
    for {
      children       <- mindMapRepo.getChildNodes(root.segmentID, profileID)
      mapRightsBrief <- mindMapRepo.getMapRightsByProfileID(root.mapID, profileID)
    } yield Json.toJson(mindMapRepo.getMindMap(root, children, consolidateMapRights(mapRightsBrief)))
  }

  def getBreadcrumb(orgID: OrgID, mapID: MapID, segmentID: SegmentID): Action[AnyContent] = authAction.async {
    implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.view,
            permission.MapsLocal.view(orgID, mapID)
          )),
        (_: Member) => {
          mindMapRepo
            .getMapRootNodes(mapID)
            .map(
              roots => generateBreadcrumbTrail(roots, segmentID)
            )
        }
      )
  }

  private def generateBreadcrumbTrail(roots: Seq[Node], segmentID: SegmentID) = {
    @scala.annotation.tailrec
    def generateTrail(acc: Seq[Node], nodeIDs: Seq[String]): Seq[Node] = {
      nodeIDs match {
        case Nil => acc
        case item =>
          roots.find(_.segmentID.uuid.toString == item.head) match {
            case Some(node) => generateTrail(acc :+ node, item.tail)
            case None       => generateTrail(acc, item.tail)
          }
      }
    }

    roots.find(_.segmentID == segmentID) match {
      case Some(node) => Ok(Json.toJson(generateTrail(Seq(), node.path.split(':').toSeq)))
      case _          => NotFound(Json.obj("message" -> "Not Found"))
    }
  }

  def createMapNode(orgID: OrgID, mapID: MapID, segmentID: SegmentID): Action[JsValue] = authAction.async(parse.json) {
    implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.modify(orgID, mapID)
          )),
        (_: Member) => {
          validateJSON[NodePost](
            request.body.validate[NodePost], {
              json =>
                getNodeAndDo(mapID, segmentID, json.parentID) {
                  parentNode =>
                    {
                      val newNodeID = NodeID.random
                      val newNode = Node(
                        id = newNodeID,
                        name = json.name,
                        nodeNumber = json.nodeNumber,
                        parentID = Some(parentNode.id),
                        mapID = parentNode.mapID,
                        segmentID = parentNode.segmentID,
                        path = parentNode.path,
                        order = json.order,
                        `type` = json.nodeType,
                        root = None
                      )
                      reportUpdateStatus(
                        numberOfUpdates = mindMapRepo.createNode(newNode),
                        Some(Json.obj("id" -> newNodeID.toString))
                      )
                    }
                }
            }
          )
        }
      )
  }

  private def getNodeAndDo(mapID: MapID, segmentID: SegmentID, nodeID: NodeID)(
      callback: Node => Future[Result]): Future[Result] =
    mindMapRepo.getNodeByID(mapID, segmentID, nodeID).flatMap {
      case Some(node) => callback(node)
      case None       => Future.successful(NotFound(Json.obj("message" -> "NODE NOT FOUND")))
    }

  def patchMapNode(orgID: OrgID, mapID: MapID, segmentID: SegmentID, nodeID: NodeID): Action[JsValue] =
    authAction.async(parse.json) { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.modify(orgID, mapID)
          )),
        (_: Member) => {
          validateJSON[NodePatch](
            request.body.validate[NodePatch], {
              json =>
                val nodeUpdated = mindMapRepo.getNodeByIDAndUpdate(mapID, segmentID, nodeID) { node =>
                  node.copy(
                    nodeNumber = json.nodeNumber getOrElse node.nodeNumber,
                    parentID = json.parentID orElse node.parentID,
                    order = json.order getOrElse node.order,
                    name = json.name getOrElse node.name,
                    `type` = json.nodeType getOrElse node.`type`,
                    disabled = json.disabled getOrElse node.disabled
                  )
                }
                lazy val returnJSON = if (json.returnNode.getOrElse(false)) {
                  mindMapRepo.getNodeAsJSON(nodeID)
                } else {
                  Future.successful(None)
                }
                nodeUpdated.flatMap { updateNumber =>
                  reportUpdateStatus(
                    numberOfUpdates = Future.successful(updateNumber),
                    returnJSON = returnJSON
                  )
                }
            }
          )
        }
      )
    }

  def patchMapNodeAttributes(orgID: OrgID, mapID: MapID, segmentID: SegmentID, nodeID: NodeID): Action[JsValue] =
    authAction.async(parse.json) { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.modify(orgID, mapID)
          )),
        (member: Member) => {
          validateJSON[NodePatchAttributes](
            request.body.validate[NodePatchAttributes], { json =>
              getNodeAndDo(mapID, segmentID, nodeID) { node =>
                val update = mindMapRepo.getNodeAttrAndSave(node, member.profile.id) { nodeAttributes =>
                  nodeAttributes.copy(collapsed = json.collapsed.getOrElse(false))
                }
                reportUpdateStatus(
                  numberOfUpdates = update,
                  returnJSON = None
                )
              }
            }
          )
        }
      )
    }

  def deleteNode(orgID: OrgID, mapID: MapID, segmentID: SegmentID, nodeID: NodeID): Action[AnyContent] =
    authAction.async { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.modify(orgID, mapID)
          )),
        (_: Member) => {
          reportUpdateStatus(
            numberOfUpdates = mindMapRepo.deleteNode(mapID, segmentID, nodeID),
            returnJSON = None
          )
        }
      )
    }

  def convertNodeToSubMap(orgID: OrgID, mapID: MapID, segmentID: SegmentID, nodeID: NodeID): Action[AnyContent] =
    authAction.async { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.modify(orgID, mapID)
          )),
        (member: Member) => {
          verifyNodeAndConvertToSubMap(nodeID, mapID, segmentID, member.profile.id)
        }
      )
    }

  private def verifyNodeAndConvertToSubMap(nodeID: NodeID, mapID: MapID, segmentID: SegmentID, profileID: ProfileID) = {
    mindMapRepo.getNodeByID(mapID, segmentID, nodeID).flatMap {
      case Some(node) =>
        mindMapRepo.convertNodeToMindMap(node, profileID).map { newSegmentID =>
          Ok(Json.obj("subMapID" -> newSegmentID.toString))
        }
      case None => Future.successful(BadRequest(Json.obj("message" -> "Node not found")))
    }
  }

  def storeAudioFile(orgID: OrgID, mapID: MapID, nodeID: NodeID): Action[TemporaryFile] =
    authAction.async(parse.temporaryFile) { request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.modify(orgID, mapID)
          )),
        (_: Member) => {
          val filename = nodeID.uuid.toString + ".wav"
          val filePath = Paths.get("/tmp/" + filename)
          request.body.moveTo(filePath, replace = true)

          val bucketFile = spacesService.uploadFileToBucket(filename, new File(filePath.toString), FLASHCARD_AUDIO)

          if (bucketFile.s3Object.getKey == filename) {
            val r         = scala.util.Random
            val publicUrl = bucketFile.url.toString + "?" + r.nextInt(999999999)
            new File(filePath.getFileName.toString).delete()
            flashcardRepo.updateFlashcardAudio(mapID, nodeID, publicUrl).map {
              case n if n > 0 => Ok("File uploaded")
              case _          => InternalServerError("Unable to upload file.")
            }
          } else {
            Future.successful(InternalServerError("Unable to upload file."))
          }
        }
      )
    }

  def createCard(orgID: OrgID, mapID: MapID, segmentID: SegmentID, nodeID: NodeID, cardType: String): Action[JsValue] =
    authAction.async(parse.json) { implicit request =>
      def createCardBasic() = {
        validateJSON[CardPost](
          request.body.validate[CardPost], { json =>
            val (card, answerChoices) =
              json.toCardAnswerChoiceTuple(nodeID, mapID, parseMarkdown(json.markdown))
            reportUpdateStatus(
              numberOfUpdates = flashcardRepo.createCard(card, answerChoices),
              returnJSON = None
            )
          }
        )
      }

      def createCardCodeExercise() = {
        validateJSON[Exercise](
          request.body.validate[Exercise], { json =>
            val newExercise = CodeExercise(
              id = nodeID,
              explanation = json.explanation,
              explanationHTML = parseMarkdown(json.explanation),
              template = json.template,
              test = json.test,
              solution = json.solution
            )
            val newCard = Card(
              id = nodeID,
              mapID = mapID,
              flashcardTypeID = json.flashcardTypeID,
              question = "",
              audio = None,
            )
            reportUpdateStatus(
              numberOfUpdates = flashcardRepo.createCodeExerciseCard(newExercise, newCard),
              returnJSON = None
            )
          }
        )
      }

      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.modify(orgID, mapID)
          )),
        (_: Member) => {
          getNodeAndDo(mapID, segmentID, nodeID) { node =>
            if (node.`type` == "flashcard") {
              cardType match {
                case "basic"         => createCardBasic()
                case "code_exercise" => createCardCodeExercise()
                case _ =>
                  Future.successful(BadRequest(Json.obj("message" -> s"""Flashcard type: '$cardType' is not valid""")))
              }
            } else {
              Future.successful(BadRequest(Json.obj("message" -> "Node type is not flashcard")))
            }
          }
        }
      )
    }

  def getCardTypeAsJSON(orgID: OrgID, mapID: MapID, segmentID: SegmentID, nodeID: NodeID): Action[AnyContent] =
    authAction.async { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.view,
            permission.MapsLocal.view(orgID, mapID)
          )),
        (_: Member) => {
          getNodeAndDo(mapID, segmentID, nodeID) { _ =>
            flashcardRepo.getFlashcard(nodeID, mapID).map {
              case Some(flashcard) =>
                Ok(Json.toJsObject(FlashcardTypeBrief(
                  id = flashcard.cardType.id,
                  cardType = flashcard.cardType.cardType,
                  name = flashcard.cardType.name,
                  commonName = flashcard.cardType.commonName,
                  description = flashcard.cardType.description,
                )))
              case None => NotFound("")
            }
          }
        }
      )
    }

  def getCardAsJSON(orgID: OrgID, mapID: MapID, segmentID: SegmentID, nodeID: NodeID): Action[AnyContent] =
    authAction.async { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.view,
            permission.MapsLocal.view(orgID, mapID)
          )),
        (_: Member) => {
          getNodeAndDo(mapID, segmentID, nodeID) { _ =>
            flashcardRepo.getFlashcard(nodeID, mapID).flatMap {
              case Some(flashcard) => getFlashcardAsJSON(flashcard, mapID, FlashcardMode.EDIT)
              case None            => Future.successful(NotFound(""))
            }
          }
        }
      )
    }

  def getFlashcardAsJSON[A](flashcard: Flashcard, mapID: MapID, mode: FlashcardMode) = {
    flashcard.cardType.cardType match {
      case "code_exercise" =>
        flashcardRepo.getCodeExerciseAndCardType(flashcard.card.id).map {
          case Some((exercise, cardType)) =>
            Ok(
              if (mode == FlashcardMode.EDIT) {
                Json.toJsObject(
                  CodeExerciseBriefEdit(
                    cardType = cardType.cardType,
                    name = cardType.name,
                    explanation = exercise.explanation,
                    template = exercise.template,
                    test = exercise.test,
                    solution = exercise.solution,
                  ))
              } else {
                Json.toJsObject(
                  CodeExerciseBriefTest(
                    cardType = cardType.cardType,
                    name = cardType.name,
                    explanation = exercise.explanationHTML,
                    template = exercise.template,
                    test = exercise.test,
                  ))
              }
            )
          case _ => NotFound("Not Found")
        }
      case _ =>
        flashcardRepo.getAnswerChoices(NodeID(flashcard.card.id), mapID).map { choices =>
          Ok(
            if (mode == FlashcardMode.EDIT) {
              Json.toJsObject(
                CardBriefEdit(
                  name = flashcard.node.name,
                  question = flashcard.card.question,
                  markdown = flashcard.card.markdown,
                  cardType = flashcard.cardType.cardType,
                  choices = choices.choicesBriefEdit,
                  audio = flashcard.card.audio,
                )
              )
            } else {
              Json.toJsObject(
                CardBriefTest(
                  name = flashcard.node.name,
                  question = flashcard.card.question,
                  markdown_html = flashcard.card.markdown_html,
                  cardType = flashcard.cardType.cardType,
                  choices = choices.choicesBriefTest,
                  radio = choices.isRadio,
                  audio = flashcard.card.audio,
                )
              )
            }
          )
        }
    }
  }

  def updateCard(orgID: OrgID, mapID: MapID, segmentID: SegmentID, nodeID: NodeID, cardType: String): Action[JsValue] =
    authAction.async(parse.json) { implicit request =>
      def updateCardBasic(): Future[Result] = {
        validateJSON[CardPost](
          request.body.validate[CardPost], { json =>
            val (card, answerChoices) = json.toCardAnswerChoiceTuple(nodeID, mapID, parseMarkdown(json.markdown))
            reportUpdateStatus(
              numberOfUpdates = flashcardRepo.updateCard(card, answerChoices),
              returnJSON = None
            )
          }
        )
      }

      def updateCardCodeExercise(): Future[Result] = {
        validateJSON[ExerciseUpdate](
          request.body.validate[ExerciseUpdate], { json =>
            val codeExercise = CodeExercise(
              id = nodeID,
              explanation = json.explanation,
              explanationHTML = parseMarkdown(json.explanation),
              template = json.template,
              test = json.test,
              solution = json.solution
            )
            reportUpdateStatus(
              numberOfUpdates = flashcardRepo.insertOrUpdateCodeExercise(codeExercise),
              returnJSON = None
            )
          }
        )
      }

      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.modify(orgID, mapID)
          )),
        (_: Member) => {
          getNodeAndDo(mapID, segmentID, nodeID) { _ =>
            cardType match {
              case "basic"         => updateCardBasic()
              case "code_exercise" => updateCardCodeExercise()
              case _ =>
                Future.successful(BadRequest(Json.obj("message" -> s"""Flashcard type: '$cardType' is not valid""")))
            }
          }
        }
      )
    }

  private def parseMarkdown(markdown: String): String = {
    val options  = new MutableDataSet()
    val parser   = Parser.builder(options).build
    val renderer = HtmlRenderer.builder(options).build()
    val document = parser.parse(markdown)
    val html     = renderer.render(document)
    html
  }

  def validateCard(orgID: OrgID, mapID: MapID, cardType: String): Action[JsValue] =
    authAction.async(parse.json) { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.modify(orgID, mapID)
          )),
        (_: Member) => {
          cardType match {
            case "code_exercise" =>
              validateJSON[Exercise](
                request.body.validate[Exercise], { json =>
                  validateCodeExercise(json)
                }
              )
            case _ => Future.successful(NotFound(Json.obj("message" -> "Card Type Not Found")))
          }
        }
      )
    }

  private def validateCodeExercise(json: Exercise) = {
    getCardTypeAndDo(json.flashcardTypeID) { cardType =>
      dockerService.dockerRunCode(cardType, json.solution, json.test).map { result =>
        if (result.pass) {
          Ok(Json.obj("message" -> "Code Exercise Passed", "output" -> result.output))
        } else {
          BadRequest(Json.obj("message" -> "Code Exercise Test Not Passing", "output" -> result.output))
        }
      }
    }
  }

  private def getCardTypeAndDo(cardTypeID: FlashcardTypeID)(
      callback: (FlashcardType) => Future[Result]): Future[Result] =
    flashcardRepo.getCardTypeByID(cardTypeID).flatMap {
      case Some(cardType) => callback(cardType)
      case None           => Future.successful(BadRequest(Json.obj("message" -> "CARD TYPE NOT FOUND")))
    }

  def createOrUpdatePost(orgID: OrgID, mapID: MapID, segmentID: SegmentID, nodeID: NodeID): Action[JsValue] =
    authAction.async(parse.json) { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.modify(orgID, mapID)
          )),
        (_: Member) => {
          validateJSON[PostMarkdown](
            request.body.validate[PostMarkdown], { json =>
              getNodeAndDo(mapID, segmentID, nodeID) { node =>
                if (node.`type` == "category") {
                  reportUpdateStatus(
                    numberOfUpdates = mindMapRepo.insertOrUpdatePost(
                      Post(
                        id = nodeID,
                        mapID = mapID,
                        markdown = json.markdown,
                        html = parseMarkdown(json.markdown)
                      )),
                    returnJSON = None
                  )
                } else {
                  Future.successful(BadRequest(Json.obj("message" -> "Node type is not category")))
                }
              }
            }
          )
        }
      )
    }

  def deletePost(orgID: OrgID, mapID: MapID, segmentID: SegmentID, nodeID: NodeID): Action[AnyContent] =
    authAction.async { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.modify(orgID, mapID)
          )),
        (_: Member) => {
          getNodeAndDo(mapID, segmentID, nodeID) { _ =>
            reportUpdateStatus(
              numberOfUpdates = mindMapRepo.deletePost(nodeID, mapID),
              returnJSON = None
            )
          }
        }
      )
    }

  def getPostAsJSON(orgID: OrgID, mapID: MapID, segmentID: SegmentID, nodeID: NodeID): Action[AnyContent] =
    authAction.async { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.view,
            permission.MapsLocal.view(orgID, mapID)
          )),
        (_: Member) => {
          getNodeAndDo(mapID, segmentID, nodeID) { _ =>
            mindMapRepo.getPost(nodeID, mapID).map {
              case Some(post) => Ok(Json.toJson(post))
              case None       => NotFound("")
            }
          }
        }
      )
    }

  def getMapRights(orgID: OrgID, mapID: MapID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.MapsGlobal.modify,
          permission.MapsLocal.permissions(orgID, mapID)
        )),
      (_: Member) => {
        mindMapRepo.getMapRightsMembers(mapID).map { mapRights =>
          Ok(Json.toJson(mapRights))
        }
      }
    )
  }

  def getMapRightsByID(orgID: OrgID, mapID: MapID, mapRightsID: MapRightsID): Action[AnyContent] = authAction.async {
    implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.permissions,
            permission.MapsLocal.permissions(orgID, mapID)
          )),
        (_: Member) => {
          mindMapRepo.getMapRightsMembers(mapID, mapRightsID).map { mapRights =>
            Ok(Json.toJson(mapRights.headOption))
          }
        }
      )
  }

  def deleteMapRightsByID(orgID: OrgID, mapID: MapID, mapRightsID: MapRightsID): Action[AnyContent] = authAction.async {
    implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.permissions,
            permission.MapsLocal.permissions(orgID, mapID)
          )),
        (_: Member) => {
          reportUpdateStatus(
            numberOfUpdates = mindMapRepo.deleteMapRights(mapID, mapRightsID),
            returnJSON = None
          )
        }
      )
  }

  def mapRightsInvite(orgID: OrgID, mapID: MapID, mapRightsID: MapRightsID): Action[JsValue] =
    authAction.async(parse.json) { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            Seq(permission.Organization.invite, permission.MapsGlobal.share, permission.MapsGlobal.permissions),
            permission.MapsLocal.permissions(orgID, mapID))),
        (_: Member) =>
          validateJSON[MapRightsInvite](
            request.body.validate[MapRightsInvite], { json =>
              mindMapRepo.getMapRightsByID(mapID, mapRightsID).flatMap {
                case Some(mapRights) => createRoleInvite(orgID, mapRights.id, json.emailAddress)
                case None            => Future.successful(BadRequest(Json.obj("message" -> "NOT FOUND")))
              }
            }
        )
      )
    }

  private def createRoleInvite(orgID: OrgID, mapRightsID: MapRightsID, emailAddress: String): Future[Result] = {
    def createRole(roleID: RoleID) = {
      orgRepo
        .createRoleInvite(roleID, Some(mapRightsID), Instant.now.getEpochSecond)
        .map { inviteID =>
          email.sendOrgInvite(inviteID, emailAddress) match {
            case code if code >= 200 && code <= 202 =>
              Ok(Json.toJson(RoleInviteResponse(status = code, inviteID = Some(inviteID))))
            case code => BadRequest(Json.toJson(RoleInviteResponse(status = code, inviteID = None)))
          }
        }
    }
    orgRepo.getRolesByOrgID(orgID).map(seq => seq.filter(_.autoJoin == true)).flatMap {
      case Nil   => Future.successful(BadRequest(Json.obj("message" -> "NO DEFAULT ROLE")))
      case roles => createRole(roles.head.id)
    }
  }

  def addMapRightsMember(orgID: OrgID,
                         mapID: MapID,
                         mapRightsID: MapRightsID,
                         profileID: ProfileID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.MapsGlobal.permissions,
          permission.MapsLocal.permissions(orgID, mapID)
        )),
      (_: Member) => {
        getMapRightsAndDo(mapID, mapRightsID) { _ =>
          getMemberAndDo(orgID, profileID) { member =>
            reportUpdateStatus(
              numberOfUpdates = mindMapRepo.addMapRightsMember(orgID, mapRightsID, member.profile.id),
              returnJSON = None
            )
          }
        }
      }
    )
  }

  def removeMapRightsMember(orgID: OrgID,
                            mapID: MapID,
                            mapRightsID: MapRightsID,
                            profileID: ProfileID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.MapsGlobal.permissions,
          permission.MapsLocal.permissions(orgID, mapID)
        )),
      (_: Member) => {
        getMapRightsAndDo(mapID, mapRightsID) { _ =>
          getMemberAndDo(orgID, profileID) { member =>
            reportUpdateStatus(
              numberOfUpdates = mindMapRepo.removeMapRightsMember(mapRightsID, member.profile.id),
              returnJSON = None
            )
          }
        }
      }
    )
  }

  def createMapRights(orgID: OrgID, mapID: MapID): Action[JsValue] =
    authAction.async(parse.json) { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.permissions,
            permission.MapsLocal.permissions(orgID, mapID)
          )),
        (_: Member) => {
          validateJSON[MapRightsPost](
            request.body.validate[MapRightsPost], {
              json =>
                val mapRightsID = MapRightsID.random
                val mapRights = MapRights(
                  id = mapRightsID,
                  mapID = mapID,
                  name = json.name,
                  admin = json.admin,
                  feedback = json.feedback,
                  mnemonics = json.mnemonics,
                  modify = json.modify,
                  permissions = json.permissions,
                  publish = json.publish,
                  share = json.share,
                  stats = json.stats,
                  training = json.training,
                  transfer = json.transfer,
                  view = json.view
                )
                reportUpdateStatus(
                  numberOfUpdates = mindMapRepo.createMapRights(mapRights),
                  returnJSON = Some(Json.obj("mapRightsID" -> mapRightsID))
                )
            }
          )
        }
      )
    }

  def patchMapRights(orgID: OrgID, mapID: MapID, mapRightsID: MapRightsID): Action[JsValue] =
    authAction.async(parse.json) { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.MapsGlobal.modify,
            permission.MapsLocal.permissions(orgID, mapID)
          )),
        (_: Member) => {
          validateJSON[MapRightsPatch](
            request.body.validate[MapRightsPatch], {
              json =>
                val update = mindMapRepo.getMapRightsAndUpdate(
                  mapID,
                  mapRightsID,
                  mapRights =>
                    mapRights.copy(
                      admin = json.admin.getOrElse(mapRights.admin),
                      feedback = json.feedback.getOrElse(mapRights.feedback),
                      mnemonics = json.mnemonics.getOrElse(mapRights.mnemonics),
                      modify = json.modify.getOrElse(mapRights.modify),
                      permissions = json.permissions.getOrElse(mapRights.permissions),
                      publish = json.publish.getOrElse(mapRights.publish),
                      share = json.share.getOrElse(mapRights.share),
                      stats = json.stats.getOrElse(mapRights.stats),
                      training = json.training.getOrElse(mapRights.training),
                      transfer = json.transfer.getOrElse(mapRights.transfer),
                      view = json.view.getOrElse(mapRights.view)
                  )
                )
                lazy val returnJSON = json.returnRights.getOrElse(false) match {
                  case true  => getMapRightsAsJSON(mapRightsID, mapID)
                  case false => Future.successful(None)
                }
                update.flatMap { updateNumber =>
                  reportUpdateStatus(
                    numberOfUpdates = Future.successful(updateNumber),
                    returnJSON = returnJSON
                  )
                }
            }
          )
        }
      )
    }

  private def getMemberAndDo(orgID: OrgID, profileID: ProfileID)(
      callback: (Member) => Future[Result]): Future[Result] = {
    orgRepo.getOrgMember(orgID, profileID).flatMap {
      case Some(member) => callback(member)
      case None         => Future.successful(BadRequest(Json.obj("message" -> "USER NOT FOUND")))
    }
  }

  private def getMapRightsAndDo(mapID: MapID, mapRightsID: MapRightsID)(
      callback: (MapRights) => Future[Result]): Future[Result] = {
    mindMapRepo.getMapRightsByID(mapID, mapRightsID).flatMap {
      case Some(mapRights) => callback(mapRights)
      case None            => Future.successful(BadRequest(Json.obj("message" -> "MAP RIGHTS NOT FOUND")))
    }
  }

  private def getMapRightsAsJSON(mapRightsID: MapRightsID, mapID: MapID): Future[Option[JsObject]] = {
    mindMapRepo.getMapRightsByID(mapID, mapRightsID).map {
      case Some(mapRights) => Some(Json.toJsObject(mapRights))
      case None            => None
    }
  }

}
