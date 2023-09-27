package controllers

import java.time.Instant

import controllers.auth.{AuthAction, AuthService, Permission}
import io.masterypath.slick.{Account, AccountID, MemberProfile, ProfileID}
import javax.inject._
import models.organization
import models.organization.Member
import models.organization.dto.{MemberProfileEmail, MemberRegistration}
import play.api.libs.json._
import play.api.mvc._
import services.OrganizationRepoService
import utils.controller.ControllerHelperFunctions

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class MemberController @Inject()(
    orgRepo: OrganizationRepoService,
    cc: MessagesControllerComponents,
    authAction: AuthAction,
    authService: AuthService
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc)
    with ControllerHelperFunctions {

  def getJWT(walletID: String): Action[AnyContent] = Action.async {
//    println(request.remoteAddress)
    println(walletID)
    Future.successful(Ok("Hello"))
  }

  def getProfile: Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(Permission()),
      (member: Member) => {
        MemberProfileEmail(member).futureResult
      }
    )
  }

  def getOrganizations: Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(Permission()),
      (member: Member) => getOrganizationsAsJSON(ProfileID(member.profile.id))
    )
  }

  private def getOrganizationsAsJSON(profileID: ProfileID): Future[Result] = {
    orgRepo
      .getOrganizationsByProfileID(profileID)
      .map(
        orgs => Ok(Json.toJson(orgs))
      )
  }

  def logSignIn: Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(request.memberLookup(Permission()), (member: Member) => logUserSignIn(member))
  }

  private def logUserSignIn(member: Member): Future[Result] = {
    orgRepo.logUserSignIn(member, Instant.now.getEpochSecond).flatMap {
      case n if n > 0 => Future.successful(Ok(""))
      case _          => Future.successful(BadRequest(""))
    }
  }

  def register: Action[JsValue] = authAction.async(parse.json) { implicit request =>
    val subject = request.jwt.subject.getOrElse("")
    validateJSON[MemberRegistration](
      request.body.validate[MemberRegistration], { registration =>
        authService.getIdTokenClaims(registration.idToken) match {
          case Success(idToken) =>
            verifyUserNotExists(
              request.memberLookup(Permission()),
              () => registerNewMember(Json.parse(idToken.toJson), subject, registration)
            )
          case Failure(_) =>
            // token was invalid - return 401
            Future.successful(
              BadRequest(Json.obj("requestId" -> request.id, "message" -> "Unable to Parse JWT ID Token")))
        }
      }
    )
  }

  protected def verifyUserNotExists(memberLookup: Future[Option[Member]], callback: () => Future[Result])(
      implicit ec: ExecutionContext): Future[Result] = {
    memberLookup.flatMap {
      case Some(_) => Future.successful(Ok(Json.obj("message" -> "USER ALREADY EXISTS")))
      case None    => callback()
    }
  }

  private def registerNewMember(idToken: JsValue, subject: String, registration: MemberRegistration) = {
    val email     = (idToken \ "email").get.as[String]
    val profileID = ProfileID.random
    val newMember = organization.Member(
      MemberProfile(
        id = profileID,
        firstName = registration.firstName,
        lastName = registration.lastName,
        avatarURL = "",
        enabled = true
      ),
      Account(
        id = AccountID.random,
        profileID = profileID,
        tokenSubject = subject,
        email = email,
        enabled = true,
        created = Instant.now.getEpochSecond
      )
    )
    reportUpdateStatus(
      numberOfUpdates = orgRepo.createNewMember(newMember, domain = "masterypath.io"),
      returnJSON = None
    )
  }

  def linkAccount: Action[JsValue] = authAction.async(parse.json) { implicit request =>
    validateJSON[MemberRegistration](
      request.body.validate[MemberRegistration], { registration =>
        authService.getIdTokenClaims(registration.idToken) match {
          case Success(idToken) =>
            authorizeMember(
              request.memberLookup(Permission()),
              (member: Member) => linkAccountToProfile(Json.parse(idToken.toJson), member)
            )
          case Failure(_) =>
            // token was invalid - return 401
            Future.successful(
              BadRequest(Json.obj("requestId" -> request.id, "message" -> "Unable to Parse JWT ID Token")))
        }
      }
    )
  }

  private def linkAccountToProfile(idToken: JsValue, member: Member) = {
    val email     = (idToken \ "email").get.as[String]
    val subject   = (idToken \ "sub").get.as[String]
    val profileID = member.profile.id
    val newAccount = Account(
      id = AccountID.random,
      profileID = profileID,
      tokenSubject = subject,
      email = email,
      enabled = true,
      created = Instant.now.getEpochSecond
    )
    reportUpdateStatus(
      numberOfUpdates = orgRepo.saveAccount(newAccount),
      returnJSON = None
    )
  }
}
