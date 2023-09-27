package controllers.auth

import javax.inject.Inject
import models.organization.{Attrs, Member}
import pdi.jwt._
import play.api.http.HeaderNames
import play.api.mvc._
import services.{OrganizationRepoService, PermissionService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

// A custom request type to hold our JWT claims, we can pass these on to the
// handling action
case class UserRequest[A](jwt: JwtClaim, memberLookup: (Permission) => Future[Option[Member]], request: Request[A])
    extends WrappedRequest[A](request)

class AuthAction @Inject()(
    bodyParser: BodyParsers.Default,
    authService: AuthService,
    members: OrganizationRepoService,
    permissionService: PermissionService
)(implicit ec: ExecutionContext)
    extends ActionBuilder[UserRequest, AnyContent] {

  // A regex for parsing the Authorization header value
  private val headerTokenRegex = """Bearer (.+?)""".r

  override def parser: BodyParser[AnyContent] = bodyParser

  // Called when a request is invoked. We should validate the bearer token here
  // and allow the request to proceed if it is valid.
  override def invokeBlock[A](request: Request[A], codeBlock: UserRequest[A] => Future[Result]): Future[Result] = {
    request.attrs.get(Attrs.FakeUser) match {
      // TEST REQUEST; No token is expected
      case Some(fakeUser) => {
        val fakeClaim = JwtClaim().about(fakeUser)
        codeBlock(UserRequest(fakeClaim, memberLookup(fakeClaim.subject.getOrElse("")), request))
      }
      // REAL REQUEST; Verify token
      case None => {
        extractBearerToken(request) map { token =>
          authService.validateJwt(token) match {
            case Success(claim) =>
              codeBlock(UserRequest(claim, memberLookup(claim.subject.getOrElse("")), request)) // token was valid - proceed!
            case Failure(t) => Future.successful(Results.Unauthorized(t.getMessage)) // token was invalid - return 401
          }
        } getOrElse Future.successful(Results.Unauthorized) // no token was sent - return 401
      }
    }
  }

  def memberLookup(subject: String)(permissions: Permission): Future[Option[Member]] = permissions match {
    case Permission(Some(orgID), None, Some(permList), Some(mapRights)) =>
      val permissions = permissionService.buildQuery(orgID, permList)
      members.getAuthorizedMember(subject, orgID, permissions, mapRights)

    case Permission(Some(orgID), Some(permission), None, Some(mapRights)) =>
      val permissions = permissionService.buildQuery(orgID, Seq(permission))
      members.getAuthorizedMember(subject, orgID, permissions, mapRights)

    case Permission(Some(orgID), Some(permission), None, None) =>
      val permissions = permissionService.buildQuery(orgID, Seq(permission))
      members.getAuthorizedMember(subject, orgID, permissions)

    case Permission(Some(orgID), None, Some(permList), None) =>
      val permissions = permissionService.buildQuery(orgID, permList)
      members.getAuthorizedMember(subject, orgID, permissions)

    case Permission(Some(orgID), None, None, None) =>
      members.getMemberBySubjectAndOrgID(subject, orgID)

    case _ =>
      members.getMemberBySubject(subject)
  }

  // Helper for extracting the token value
  private def extractBearerToken[A](request: Request[A]): Option[String] =
    request.headers.get(HeaderNames.AUTHORIZATION) collect {
      case headerTokenRegex(token) => {
        token
      }
    }

  override protected def executionContext: ExecutionContext = ec
}
