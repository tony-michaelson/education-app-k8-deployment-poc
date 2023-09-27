package controllers

import java.time.Instant
import controllers.auth.{AuthAction, Permission}
import io.masterypath.slick.{MapRightsID, OrgID, ProfileID, Role, RoleID, RoleInviteID, Site}
import play.api.libs.Files.TemporaryFile
import services.SpaceKind.IMAGE
import services.SpacesService

import java.io.{File, PrintWriter}
import java.nio.file.Paths
import models.blog.{JekyllConfig, JekyllSite}

import javax.inject._
import models.organization.Member
import models.organization.dto.{
  BlogCreate,
  BlogPatch,
  ConfigPatch,
  MemberOrgPermissions,
  PostOrg,
  RoleInviteRequest,
  RoleInviteResponse,
  RolePatch,
  RolePost
}
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc.{Action, _}
import services.{CertbotService, JekyllService, OrganizationRepoService, PermissionService, SendgridService}
import utils.controller.ControllerHelperFunctions

import scala.concurrent.{ExecutionContext, Future}

class OrganizationController @Inject()(
    orgRepo: OrganizationRepoService,
    permission: PermissionService,
    certbotService: CertbotService,
    jekyllService: JekyllService,
    cc: MessagesControllerComponents,
    spacesService: SpacesService,
    authAction: AuthAction,
    email: SendgridService,
    config: Configuration
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc)
    with ControllerHelperFunctions {
  private val sitesDomain: String = config.get[String]("certbotService.domain")

  def createOrg(): Action[JsValue] = authAction.async(parse.json) { implicit request =>
    authorizeMember(
      request.memberLookup(Permission()),
      (member: Member) =>
        validateJSON[PostOrg](
          request.body.validate[PostOrg], { json =>
            orgRepo.orgNameDomainCheck(json.name, json.domain).flatMap {
              case (true, _) =>
                Future.successful(badRequestMessage("Organization name already exists."))
              case (_, true) =>
                Future.successful(badRequestMessage("Organization domain already exists."))
              case (_, _) =>
                val (newOrgID, updates) = orgRepo.createOrganization(member, json)
                reportUpdateStatus(
                  numberOfUpdates = updates,
                  returnJSON = Some(Json.obj("id" -> newOrgID))
                )
            }
          }
      )
    )
  }

  def getBlog(orgID: OrgID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(Permission(orgID, permission.Manage.blog)),
      (_: Member) => getBlogDetails(orgID)
    )
  }

  private def getBlogDetails(orgID: OrgID): Future[Result] = {
    orgRepo
      .getSiteByOrgID(orgID)
      .map {
        case Some(site) => Ok(Json.toJson(site))
        case None       => NotFound(Json.obj())
      }
  }

  def buildBlog(orgID: OrgID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(Permission(orgID, permission.Blog.publish)),
      (_: Member) => {
        orgRepo.getSiteByOrgID(orgID).flatMap {
          case Some(site) => writeBlogBuildFiles(site)
          case None       => Future.successful(badRequestMessage("No site for this organization"))
        }
      }
    )
  }

  private def writeBlogBuildFiles(site: Site): Future[Result] = {
    val jekyllConfig = JekyllConfig(
      site = JekyllSite(
        name = site.name,
        description = site.description,
        logo = site.logo,
        favicon = ""
      )
    )
    val config   = views.html.blog.jekyll_config(jekyllConfig)
    val filePath = Paths.get(s"/sites/.build_files/" + site.domain + "/_config.yml")
    val pw       = new PrintWriter(new File(filePath.toString))
    pw.write(config.toString())
    pw.close()
    buildBlog(site)
  }

  private def buildBlog(site: Site): Future[Result] = {
    jekyllService.buildSite(site).map {
      case true  => okMessage("Successfully built site")
      case false => severErrorMessage("Error building site")
    }
  }

  def publishBlog(orgID: OrgID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(Permission(orgID, permission.Blog.publish)),
      (_: Member) => {
        orgRepo.getSiteByOrgID(orgID).flatMap {
          case Some(site) => publishBlog(site)
          case None       => Future.successful(badRequestMessage("No site for this organization"))
        }
      }
    )
  }

  private def publishBlog(site: Site): Future[Result] = {
    jekyllService.publishSite(site).map {
      case true  => okMessage("Successfully built site")
      case false => severErrorMessage("Error building site")
    }
  }

  def uploadSiteLogo(orgID: OrgID): Action[MultipartFormData[TemporaryFile]] =
    authAction.async(parse.multipartFormData) { implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(
            orgID,
            permission.Manage.blog
          )),
        (_: Member) => {
          request.body
            .file("logo")
            .map {
              picture =>
                // only get the last part of the filename
                // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
                val uuid       = java.util.UUID.randomUUID()
                val objectName = uuid.toString
                val filePath   = Paths.get(s"/tmp/$objectName")
                picture.ref.copyTo(filePath, replace = true)
                val bucketFile =
                  spacesService.uploadFileToBucket(objectName, new File(filePath.toString), IMAGE)
                if (bucketFile.s3Object.getKey == objectName) {
                  val publicUrl = bucketFile.url.toString
                  new File(filePath.toString).delete()
                  orgRepo
                    .getSiteByOrgIDAndUpdate(
                      orgID,
                      site =>
                        site.copy(
                          logo = publicUrl
                      )
                    )
                    .map {
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

  def patchBlog(orgID: OrgID): Action[JsValue] = authAction.async(parse.json) { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.Manage.blog
        )),
      (_: Member) => {
        validateJSON[BlogPatch](
          request.body.validate[BlogPatch], { json =>
            val updateConfig = orgRepo.getSiteByOrgIDAndUpdate(
              orgID,
              site =>
                site.copy(
                  theme = json.theme.getOrElse(site.theme),
                  name = json.name.getOrElse(site.name),
                  description = json.description.getOrElse(site.description)
              )
            )
            reportUpdateStatus(
              numberOfUpdates = updateConfig,
              returnJSON = None
            )
          }
        )
      }
    )
  }

  def createBlog(orgID: OrgID): Action[JsValue] = authAction.async(parse.json) { implicit request =>
    authorizeMember(
      request.memberLookup(Permission(orgID, Seq(permission.Manage.blog, permission.Blog.create))),
      (_: Member) => {
        validateJSON[BlogCreate](
          request.body.validate[BlogCreate], { json =>
            val validString = "^[a-z0-9_]+$".r
            json.subDomain match {
              case validString(_*) => createBlogUnlessExists(orgID, json.subDomain + '.' + sitesDomain)
              case _ =>
                Future.successful(badRequestMessage("Only lowercase alpha-numeric characters and underscores allowed."))
            }
          }
        )
      }
    )
  }

  private def createBlogUnlessExists(orgID: OrgID, subDomain: String): Future[Result] = {
    orgRepo.getSiteByOrgID(orgID).flatMap {
      case Some(_) => Future.successful(badRequestMessage("Site already exists for this organization"))
      case None    => createBlogUnlessDomainIsUsed(orgID, subDomain)
    }
  }

  private def createBlogUnlessDomainIsUsed(orgID: OrgID, subDomain: String): Future[Result] = {
    orgRepo.getSiteByDomain(subDomain).flatMap {
      case Some(_) => Future.successful(badRequestMessage("Domain Not Available"))
      case None =>
        for {
          req1                 <- certbotService.createSite(subDomain)
          req2                 <- certbotService.createStagingSite(subDomain)
          createRecordResponse <- createBlogSiteRecord(orgID, subDomain, req1, req2)
        } yield createRecordResponse
    }
  }

  private def createBlogSiteRecord(orgID: OrgID,
                                   subDomain: String,
                                   siteCreated: Boolean,
                                   stagingSiteCreated: Boolean): Future[Result] = {
    (siteCreated, stagingSiteCreated) match {
      case (true, true) =>
        val updates = orgRepo.createBlog(orgID = orgID, domain = subDomain, theme = "memoirs", ssl = false)
        reportUpdateStatus(
          numberOfUpdates = updates,
          returnJSON = Some(Json.obj("message" -> "Successfully Created Site"))
        )
      case (_, _) =>
        Future.successful(severErrorMessage("Failed to create site"))
    }
  }

  def deleteBlog(orgID: OrgID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(Permission(orgID, permission.Blog.delete)),
      (_: Member) => {
        orgRepo.getSiteByOrgID(orgID).flatMap {
          case Some(site) => deleteBlog(site)
          case None       => Future.successful(badRequestMessage("No Site for this Organization"))
        }
      }
    )
  }

  private def deleteBlogSiteRecord(site: Site, siteDeleted: Boolean, stagingSiteDeleted: Boolean): Future[Result] = {
    (siteDeleted, stagingSiteDeleted) match {
      case (true, true) =>
        val updates = orgRepo.deleteBlog(site)
        reportUpdateStatus(
          numberOfUpdates = updates,
          returnJSON = Some(Json.obj("message" -> "Successfully Delete Site"))
        )
      case (_, _) =>
        Future.successful(severErrorMessage("Failed to delete site"))
    }
  }

  private def deleteBlog(site: Site): Future[Result] =
    for {
      req1                 <- certbotService.deleteSite(site.domain)
      req2                 <- certbotService.deleteStagingSite(site.domain)
      deleteRecordResponse <- deleteBlogSiteRecord(site, req1, req2)
    } yield deleteRecordResponse

  def getConfig(orgID: OrgID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(Permission(orgID, permission.Manage.organizationConfig)),
      (_: Member) => getConfigAsJSON(orgID)
    )
  }

  private def getConfigAsJSON(orgID: OrgID): Future[Result] = {
    orgRepo
      .getConfigByID(orgID)
      .map {
        case Some(config) => Ok(Json.toJson(config))
        case None         => NotFound(Json.obj())
      }
  }

  def patchConfig(orgID: OrgID): Action[JsValue] = authAction.async(parse.json) { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.Manage.organizationConfig
        )),
      (_: Member) => {
        validateJSON[ConfigPatch](
          request.body.validate[ConfigPatch], {
            json =>
              val updateConfig = orgRepo.getConfigByIDAndUpdate(
                orgID,
                config =>
                  config.copy(
                    blog = json.blog.getOrElse(config.blog),
                    contests = json.contests.getOrElse(config.contests),
                    defaultLanguage = json.defaultLanguage.getOrElse(config.defaultLanguage),
                    mapDocumentationGeneration =
                      json.mapDocumentationGeneration.getOrElse(config.mapDocumentationGeneration),
                    marketingCampaigns = json.marketingCampaigns.getOrElse(config.marketingCampaigns),
                    marketingEngagementCampaigns =
                      json.marketingEngagementCampaigns.getOrElse(config.marketingEngagementCampaigns),
                    problemBoard = json.problemBoard.getOrElse(config.problemBoard),
                    salesAds = json.salesAds.getOrElse(config.salesAds),
                    salesCertificates = json.salesCertificates.getOrElse(config.salesCertificates),
                    salesCourses = json.salesCourses.getOrElse(config.salesCourses),
                    salesFreeTrials = json.salesFreeTrials.getOrElse(config.salesFreeTrials),
                    salesMemberFeesActive = json.salesMemberFeesActive.getOrElse(config.salesMemberFeesActive),
                    salesMemberFeesStatic = json.salesMemberFeesStatic.getOrElse(config.salesMemberFeesStatic),
                    salesMemberships = json.salesMemberships.getOrElse(config.salesMemberships),
                    salesOrganizations = json.salesOrganizations.getOrElse(config.salesOrganizations),
                    supportTier = json.supportTier.getOrElse(config.supportTier),
                    trainingAnswerTimeTracking =
                      json.trainingAnswerTimeTracking.getOrElse(config.trainingAnswerTimeTracking),
                    trainingBreakTime = json.trainingBreakTime.getOrElse(config.trainingBreakTime),
                    trainingComplianceEnforcement =
                      json.trainingComplianceEnforcement.getOrElse(config.trainingComplianceEnforcement),
                    trainingComments = json.trainingComments.getOrElse(config.trainingComments),
                    trainingContentPageStudentSubmission =
                      json.trainingContentPageStudentSubmission.getOrElse(config.trainingContentPageStudentSubmission),
                    trainingContentPageTimeTracking =
                      json.trainingContentPageTimeTracking.getOrElse(config.trainingContentPageTimeTracking),
                    trainingContentPageUpvote =
                      json.trainingContentPageUpvote.getOrElse(config.trainingContentPageUpvote),
                    trainingCorrectAnswerAnimation =
                      json.trainingCorrectAnswerAnimation.getOrElse(config.trainingCorrectAnswerAnimation),
                    trainingCorrectAnswerSound =
                      json.trainingCorrectAnswerSound.getOrElse(config.trainingCorrectAnswerSound),
                    trainingFeedback = json.trainingFeedback.getOrElse(config.trainingFeedback),
                    trainingLearningPaths = json.trainingLearningPaths.getOrElse(config.trainingLearningPaths),
                    trainingMnemonics = json.trainingMnemonics.getOrElse(config.trainingMnemonics),
                    trainingQuotes = json.trainingQuotes.getOrElse(config.trainingQuotes),
                    trainingRankings = json.trainingRankings.getOrElse(config.trainingRankings),
                    trainingReporting = json.trainingReporting.getOrElse(config.trainingReporting),
                    trainingRewardsProgram = json.trainingRewardsProgram.getOrElse(config.trainingRewardsProgram),
                    trainingSessionEndCelebration =
                      json.trainingSessionEndCelebration.getOrElse(config.trainingSessionEndCelebration),
                    trainingSessionEndFeedback =
                      json.trainingSessionEndFeedback.getOrElse(config.trainingSessionEndFeedback),
                    trainingStraightThruMode = json.trainingStraightThruMode.getOrElse(config.trainingStraightThruMode),
                    trainingStudyGoals = json.trainingStudyGoals.getOrElse(config.trainingStudyGoals),
                    trainingVirtualLabs = json.trainingVirtualLabs.getOrElse(config.trainingVirtualLabs),
                    whiteLabeled = json.whiteLabeled.getOrElse(config.whiteLabeled),
                    memberMonthlyCost = json.memberMonthlyCost.getOrElse(config.memberMonthlyCost),
                    memberAnnualCost = json.memberAnnualCost.getOrElse(config.memberAnnualCost),
                    memberPaymentMethodRequired =
                      json.memberPaymentMethodRequired.getOrElse(config.memberPaymentMethodRequired),
                )
              )
              reportUpdateStatus(
                numberOfUpdates = updateConfig,
                returnJSON = None
              )
          }
        )
      }
    )
  }

  def join(inviteID: RoleInviteID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(Permission()),
      (member: Member) => {
        reportUpdateStatus(
          numberOfUpdates = joinOrgRole(inviteID, member),
          returnJSON = None
        )
      }
    )
  }

  private def joinOrgRole(inviteID: RoleInviteID, member: Member): Future[Int] =
    orgRepo.getInviteRole(inviteID).flatMap {
      case (Some(role), Some(mapRightsID)) =>
        orgRepo.getOrgMember(role.orgID, member.profile.id).flatMap {
          case Some(_) => orgRepo.joinRoleAndMapRights(role.orgID, role.id, mapRightsID, inviteID, member)
          case None    => orgRepo.joinOrgRoleAndMapRights(role.orgID, role.id, mapRightsID, inviteID, member)
        }
      case (Some(role), None) =>
        orgRepo.getOrgMember(role.orgID, member.profile.id).flatMap {
          case Some(_) => orgRepo.joinRole(role.orgID, role.id, inviteID, member)
          case None    => orgRepo.joinOrgRole(role.orgID, role.id, inviteID, member)
        }
      case (None, _) => Future.successful(0)
    }

  def removeOrgMember(orgID: OrgID, profileID: ProfileID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(Permission(orgID, permission.Manage.organizationMembers)),
      (_: Member) =>
        getMemberAndDo(orgID, profileID, (orgMember: Member) => {
          reportUpdateStatus(
            numberOfUpdates = orgRepo.removeOrgMember(orgID, orgMember),
            returnJSON = None
          )
        })
    )
  }

  def removeRoleMember(orgID: OrgID, roleID: RoleID, profileID: ProfileID): Action[AnyContent] = authAction.async {
    implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(orgID, Seq(permission.Manage.organizationPermissions, permission.Manage.organizationMembers))),
        (_: Member) =>
          getRoleAndDo(
            orgID,
            roleID,
            (role: Role) =>
              getMemberAndDo(
                orgID,
                profileID,
                (roleMember: Member) => {
                  reportUpdateStatus(
                    numberOfUpdates = orgRepo.removeRoleMember(role.id, roleMember.profile.id),
                    returnJSON = None
                  )
                }
            )
        )
      )
  }

  def addRoleMember(orgID: OrgID, roleID: RoleID, profileID: ProfileID): Action[AnyContent] = authAction.async {
    implicit request =>
      authorizeMember(
        request.memberLookup(
          Permission(orgID, Seq(permission.Manage.organizationPermissions, permission.Manage.organizationMembers))),
        (_: Member) =>
          getRoleAndDo(
            orgID,
            roleID,
            (role: Role) =>
              getMemberAndDo(
                orgID,
                profileID,
                (roleMember: Member) => {
                  reportUpdateStatus(
                    numberOfUpdates = orgRepo.addRoleMember(orgID, role.id, roleMember.profile.id),
                    returnJSON = None
                  )
                }
            )
        )
      )
  }

  private def getRoleAndDo(orgID: OrgID, roleID: RoleID, callback: (Role) => Future[Result]): Future[Result] =
    orgRepo.getRoleByID(orgID, roleID).flatMap {
      case Some(role) => callback(role)
      case None       => Future.successful(BadRequest(Json.obj("message" -> "ROLE NOT FOUND")))
    }

  private def getMemberAndDo(orgID: OrgID,
                             profileID: ProfileID,
                             callback: (Member) => Future[Result]): Future[Result] = {
    orgRepo.getOrgMember(orgID, profileID).flatMap {
      case Some(member) => callback(member)
      case None         => Future.successful(BadRequest(Json.obj("message" -> "USER NOT FOUND")))
    }
  }

  def roleInvite(orgID: OrgID, roleID: RoleID): Action[JsValue] = authAction.async(parse.json) { implicit request =>
    authorizeMember(
      request.memberLookup(Permission(orgID, permission.Organization.invite)),
      (_: Member) =>
        validateJSON[RoleInviteRequest](
          request.body.validate[RoleInviteRequest], { json =>
            orgRepo.getRoleByID(orgID, roleID).flatMap {
              case Some(role) => createRoleInvite(role.id, json.emailAddress, json.mapRightsID)
              case None       => Future.successful(BadRequest(Json.obj("message" -> "NOT FOUND")))
            }
          }
      )
    )
  }

  private def createRoleInvite(roleID: RoleID, emailAddress: String, mapRightsID: Option[MapRightsID]): Future[Result] =
    orgRepo
      .createRoleInvite(roleID, mapRightsID, Instant.now.getEpochSecond)
      .map { inviteID =>
        email.sendOrgInvite(inviteID, emailAddress) match {
          case code if code >= 200 && code <= 202 =>
            Ok(Json.toJson(RoleInviteResponse(status = code, inviteID = Some(inviteID))))
          case code => BadRequest(Json.toJson(RoleInviteResponse(status = code, inviteID = None)))
        }
      }

  def getRole(orgID: OrgID, roleID: RoleID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(Permission(orgID, permission.Manage.organizationPermissions)),
      (_: Member) => getRoleAsJSON(orgID, roleID)
    )
  }

  private def getRoleAsJSON(orgID: OrgID, roleID: RoleID): Future[Result] = {
    orgRepo
      .getRoleByID(orgID, roleID)
      .map {
        case Some(role) => Ok(Json.toJson(role))
        case None       => NotFound(Json.obj())
      }
  }

  def getRoles(orgID: OrgID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(Permission(orgID, permission.Manage.organizationPermissions)),
      (_: Member) => getRolesAsJSON(orgID)
    )
  }

  private def getRolesAsJSON(orgID: OrgID): Future[Result] = {
    orgRepo
      .getRolesByOrgID(orgID)
      .map(
        roles => Ok(Json.toJson(roles))
      )
  }

  def getRoleMembers(orgID: OrgID, roleID: RoleID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(orgID, Seq(permission.Manage.organizationPermissions, permission.Manage.organizationMembers))),
      (_: Member) => getRoleMembersAsJSON(orgID, roleID)
    )
  }

  private def getRoleMembersAsJSON(orgID: OrgID, roleID: RoleID): Future[Result] = {
    orgRepo
      .getRoleMembers(orgID, roleID)
      .map(
        members => Ok(Json.toJson(members))
      )
  }

  def getOrgMembers(orgID: OrgID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(Permission(orgID, permission.Manage.organizationMembers)),
      (_: Member) => getOrgMembersAsJSON(orgID)
    )
  }

  private def getOrgMembersAsJSON(orgID: OrgID): Future[Result] = {
    orgRepo
      .getOrgMembers(orgID)
      .map(
        members => Ok(Json.toJson(members))
      )
  }

  def getMyPermissions(orgID: OrgID): Action[AnyContent] = authAction.async { implicit request =>
    authorizeMember(
      request.memberLookup(Permission(orgID, permission.Manage.organizationMembers)),
      (member: Member) => getMyPermissionsAsJSON(orgID, member.profile.id)
    )
  }

  def getMyPermissionsAsJSON(orgID: OrgID, profileID: ProfileID): Future[Result] = {
    orgRepo
      .getRolesByMember(orgID, profileID)
      .map(
        roles =>
          Ok(Json.toJson(
            roles.foldLeft(MemberOrgPermissions())((perms, role) => {
              perms.copy(
                blogApprove = if (role.blogApprove) true else perms.blogApprove,
                blogCreate = if (role.blogCreate) true else perms.blogCreate,
                blogDelete = if (role.blogDelete) true else perms.blogDelete,
                blogPublish = if (role.blogPublish) true else perms.blogPublish,
                manageBlog = if (role.manageBlog) true else perms.manageBlog,
                manageContests = if (role.manageContests) true else perms.manageContests,
                manageMarketingCampaigns = if (role.manageMarketingCampaigns) true else perms.manageMarketingCampaigns,
                manageOrganizationBilling =
                  if (role.manageOrganizationBilling) true else perms.manageOrganizationBilling,
                manageOrganizationConfig = if (role.manageOrganizationConfig) true else perms.manageOrganizationConfig,
                manageOrganizationMembers =
                  if (role.manageOrganizationMembers) true else perms.manageOrganizationMembers,
                manageOrganizationPermissions =
                  if (role.manageOrganizationPermissions) true else perms.manageOrganizationPermissions,
                manageOrganizationWhitelabel =
                  if (role.manageOrganizationWhitelabel) true else perms.manageOrganizationWhitelabel,
                manageRewardsProgram = if (role.manageRewardsProgram) true else perms.manageRewardsProgram,
                manageSalesAds = if (role.manageSalesAds) true else perms.manageSalesAds,
                manageSalesCertificates = if (role.manageSalesCertificates) true else perms.manageSalesCertificates,
                manageSalesCourses = if (role.manageSalesCourses) true else perms.manageSalesCourses,
                manageSalesMemberships = if (role.manageSalesMemberships) true else perms.manageSalesMemberships,
                manageSalesOrganizations = if (role.manageSalesOrganizations) true else perms.manageSalesOrganizations,
                manageTrainingBreakTime = if (role.manageTrainingBreakTime) true else perms.manageTrainingBreakTime,
                manageTrainingComplianceEnforcement =
                  if (role.manageTrainingComplianceEnforcement) true else perms.manageTrainingComplianceEnforcement,
                manageTrainingQuotes = if (role.manageTrainingQuotes) true else perms.manageTrainingQuotes,
                manageTrainingSessionSettings =
                  if (role.manageTrainingSessionSettings) true else perms.manageTrainingSessionSettings,
                manageProblemBoard = if (role.manageProblemBoard) true else perms.manageProblemBoard,
                mapApprove = if (role.mapApprove) true else perms.mapApprove,
                mapCreate = if (role.mapCreate) true else perms.mapCreate,
                mapCreateDocuments = if (role.mapCreateDocuments) true else perms.mapCreateDocuments,
                mapDirectory = if (role.mapDirectory) true else perms.mapDirectory,
                mapFeedback = if (role.mapFeedback) true else perms.mapFeedback,
                mapFork = if (role.mapFork) true else perms.mapFork,
                mapMnemonics = if (role.mapMnemonics) true else perms.mapMnemonics,
                mapModify = if (role.mapModify) true else perms.mapModify,
                mapPermissions = if (role.mapPermissions) true else perms.mapPermissions,
                mapPublish = if (role.mapPublish) true else perms.mapPublish,
                mapShare = if (role.mapShare) true else perms.mapShare,
                mapStats = if (role.mapStats) true else perms.mapStats,
                mapTraining = if (role.mapTraining) true else perms.mapTraining,
                mapTransfer = if (role.mapTransfer) true else perms.mapTransfer,
                mapView = if (role.mapView) true else perms.mapView,
                organizationInvite = if (role.organizationInvite) true else perms.organizationInvite,
                organizationPublish = if (role.organizationPublish) true else perms.organizationPublish,
                trainingRankings = if (role.trainingRankings) true else perms.trainingRankings,
                trainingReporting = if (role.trainingReporting) true else perms.trainingReporting,
              )
            })
          ))
      )
  }

  def createRole(orgID: OrgID): Action[JsValue] = authAction.async(parse.json) { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.Manage.organizationPermissions
        )),
      (_: Member) => {
        validateJSON[RolePost](
          request.body.validate[RolePost], {
            json =>
              val roleID = RoleID.random
              val role = Role(
                id = roleID,
                orgID = orgID,
                roleName = json.name,
                autoJoin = json.autoJoin.getOrElse(false),
                blogApprove = json.blogApprove.getOrElse(false),
                blogCreate = json.blogCreate.getOrElse(false),
                blogDelete = json.blogDelete.getOrElse(false),
                blogPublish = json.blogPublish.getOrElse(false),
                manageBlog = json.manageBlog.getOrElse(false),
                manageContests = json.manageContests.getOrElse(false),
                manageMarketingCampaigns = json.manageMarketingCampaigns.getOrElse(false),
                manageOrganizationBilling = json.manageOrganizationBilling.getOrElse(false),
                manageOrganizationConfig = json.manageOrganizationConfig.getOrElse(false),
                manageOrganizationMembers = json.manageOrganizationMembers.getOrElse(false),
                manageOrganizationPermissions = json.manageOrganizationPermissions.getOrElse(false),
                manageOrganizationWhitelabel = json.manageOrganizationWhitelabel.getOrElse(false),
                manageRewardsProgram = json.manageRewardsProgram.getOrElse(false),
                manageSalesAds = json.manageSalesAds.getOrElse(false),
                manageSalesCertificates = json.manageSalesCertificates.getOrElse(false),
                manageSalesCourses = json.manageSalesCourses.getOrElse(false),
                manageSalesMemberships = json.manageSalesMemberships.getOrElse(false),
                manageSalesOrganizations = json.manageSalesOrganizations.getOrElse(false),
                manageTrainingBreakTime = json.manageTrainingBreakTime.getOrElse(false),
                manageTrainingComplianceEnforcement = json.manageTrainingComplianceEnforcement.getOrElse(false),
                manageTrainingQuotes = json.manageTrainingQuotes.getOrElse(false),
                manageTrainingSessionSettings = json.manageTrainingSessionSettings.getOrElse(false),
                manageProblemBoard = json.manageProblemBoard.getOrElse(false),
                mapApprove = json.mapApprove.getOrElse(false),
                mapCreate = json.mapCreate.getOrElse(false),
                mapCreateDocuments = json.mapCreateDocuments.getOrElse(false),
                mapDirectory = json.mapDirectory.getOrElse(false),
                mapFeedback = json.mapFeedback.getOrElse(false),
                mapFork = json.mapFork.getOrElse(false),
                mapMnemonics = json.mapMnemonics.getOrElse(false),
                mapModify = json.mapModify.getOrElse(false),
                mapPermissions = json.mapPermissions.getOrElse(false),
                mapPublish = json.mapPublish.getOrElse(false),
                mapShare = json.mapShare.getOrElse(false),
                mapStats = json.mapStats.getOrElse(false),
                mapTraining = json.mapTraining.getOrElse(false),
                mapTransfer = json.mapTransfer.getOrElse(false),
                mapView = json.mapView.getOrElse(false),
                organizationInvite = json.organizationInvite.getOrElse(false),
                organizationPublish = json.organizationPublish.getOrElse(false),
                trainingRankings = json.trainingRankings.getOrElse(false),
                trainingReporting = json.trainingReporting.getOrElse(false)
              )
              reportUpdateStatus(
                numberOfUpdates = orgRepo.saveRole(role),
                returnJSON = Some(Json.obj("id" -> roleID.toString))
              )
          }
        )
      }
    )
  }

  def patchRole(orgID: OrgID, roleID: RoleID): Action[JsValue] = authAction.async(parse.json) { implicit request =>
    authorizeMember(
      request.memberLookup(
        Permission(
          orgID,
          permission.Manage.organizationPermissions
        )),
      (_: Member) => {
        validateJSON[RolePatch](
          request.body.validate[RolePatch], {
            json =>
              val updateRole = orgRepo.getRoleByIDAndUpdate(
                roleID,
                orgID,
                role =>
                  role.copy(
                    roleName = json.name.getOrElse(role.roleName),
                    autoJoin = json.autoJoin.getOrElse((role.autoJoin)),
                    blogApprove = json.blogApprove.getOrElse(role.blogApprove),
                    blogCreate = json.blogCreate.getOrElse(role.blogCreate),
                    blogDelete = json.blogDelete.getOrElse(role.blogDelete),
                    blogPublish = json.blogPublish.getOrElse(role.blogPublish),
                    manageBlog = json.manageBlog.getOrElse(role.manageBlog),
                    manageContests = json.manageContests.getOrElse(role.manageContests),
                    manageMarketingCampaigns = json.manageMarketingCampaigns.getOrElse(role.manageMarketingCampaigns),
                    manageOrganizationBilling = json.manageOrganizationBilling.getOrElse(role.manageOrganizationBilling),
                    manageOrganizationConfig = json.manageOrganizationConfig.getOrElse(role.manageOrganizationConfig),
                    manageOrganizationMembers = json.manageOrganizationMembers.getOrElse(role.manageOrganizationMembers),
                    manageOrganizationPermissions =
                      json.manageOrganizationPermissions.getOrElse(role.manageOrganizationPermissions),
                    manageOrganizationWhitelabel =
                      json.manageOrganizationWhitelabel.getOrElse(role.manageOrganizationWhitelabel),
                    manageRewardsProgram = json.manageRewardsProgram.getOrElse(role.manageRewardsProgram),
                    manageSalesAds = json.manageSalesAds.getOrElse(role.manageSalesAds),
                    manageSalesCertificates = json.manageSalesCertificates.getOrElse(role.manageSalesCertificates),
                    manageSalesCourses = json.manageSalesCourses.getOrElse(role.manageSalesCourses),
                    manageSalesMemberships = json.manageSalesMemberships.getOrElse(role.manageSalesMemberships),
                    manageSalesOrganizations = json.manageSalesOrganizations.getOrElse(role.manageSalesOrganizations),
                    manageTrainingBreakTime = json.manageTrainingBreakTime.getOrElse(role.manageTrainingBreakTime),
                    manageTrainingComplianceEnforcement =
                      json.manageTrainingComplianceEnforcement.getOrElse(role.manageTrainingComplianceEnforcement),
                    manageTrainingQuotes = json.manageTrainingQuotes.getOrElse(role.manageTrainingQuotes),
                    manageTrainingSessionSettings =
                      json.manageTrainingSessionSettings.getOrElse(role.manageTrainingSessionSettings),
                    manageProblemBoard = json.manageProblemBoard.getOrElse(role.manageProblemBoard),
                    mapApprove = json.mapApprove.getOrElse(role.mapApprove),
                    mapCreate = json.mapCreate.getOrElse(role.mapCreate),
                    mapCreateDocuments = json.mapCreateDocuments.getOrElse(role.mapCreateDocuments),
                    mapDirectory = json.mapDirectory.getOrElse(role.mapDirectory),
                    mapFeedback = json.mapFeedback.getOrElse(role.mapFeedback),
                    mapFork = json.mapFork.getOrElse(role.mapFork),
                    mapMnemonics = json.mapMnemonics.getOrElse(role.mapMnemonics),
                    mapModify = json.mapModify.getOrElse(role.mapModify),
                    mapPermissions = json.mapPermissions.getOrElse(role.mapPermissions),
                    mapPublish = json.mapPublish.getOrElse(role.mapPublish),
                    mapShare = json.mapShare.getOrElse(role.mapShare),
                    mapStats = json.mapStats.getOrElse(role.mapStats),
                    mapTraining = json.mapTraining.getOrElse(role.mapTraining),
                    mapTransfer = json.mapTransfer.getOrElse(role.mapTransfer),
                    mapView = json.mapView.getOrElse(role.mapView),
                    organizationInvite = json.organizationInvite.getOrElse(role.organizationInvite),
                    organizationPublish = json.organizationPublish.getOrElse(role.organizationPublish),
                    trainingRankings = json.trainingRankings.getOrElse(role.trainingRankings),
                    trainingReporting = json.trainingReporting.getOrElse(role.trainingReporting)
                )
              )
              reportUpdateStatus(
                numberOfUpdates = updateRole,
                returnJSON = None
              )
          }
        )
      }
    )
  }
}
