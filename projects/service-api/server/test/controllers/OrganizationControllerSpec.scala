package controllers

import java.util.UUID
import java.util.regex.Pattern

import io.masterypath.slick.{MapID, MapRightsID, OrgID, ProfileID, RoleID, RoleInviteID}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import play.api.test.Helpers._
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

import scala.util.matching.Regex

/**
  * Test case for the [[OrganizationController]] class.
  */
class OrganizationControllerSpec extends PlaySpecification {
  sequential

  // placeholder UUID variables
  var codeSchoolOrgID: OrgID              = OrgID.random
  val codeSchoolDomain: String            = "deleteme.dev.masterypath.net"
  var profile1UUID: ProfileID             = ProfileID.random
  var profile2UUID: ProfileID             = ProfileID.random
  var profile3UUID: ProfileID             = ProfileID.random
  var role1UUID: RoleID                   = RoleID.random
  var role2UUID: RoleID                   = RoleID.random
  var role3UUID: RoleID                   = RoleID.random
  var chemistryMapUUID: MapID             = MapID.random
  var chemistryMapRightsUUID: MapRightsID = MapRightsID.random
  var invite1UUID: RoleInviteID           = RoleInviteID.random

  // POST `/org`

  "The POST `/org` action" should {
    "Create \"Code School\" Organization" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name"       -> "Code School",
          "domain"     -> "codeschool.com",
          "familyPlan" -> false,
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createOrg)
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)

        val pattern: Regex =
          """(?s)\{.*?"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*?\}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        codeSchoolOrgID = OrgID(UUID.fromString(newUUID))
      }
    }

    "Reject duplicate name" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name"       -> "Code School",
          "domain"     -> "dummy.com",
          "familyPlan" -> false,
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createOrg)
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)

        resultString must beMatching(Pattern.compile("""(?s).*message.*"""))
      }
    }

    "Reject duplicate domain" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name"       -> "Dummy",
          "domain"     -> "codeschool.com",
          "familyPlan" -> false,
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createOrg)
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)

        resultString must beMatching(Pattern.compile("""(?s).*message.*"""))
      }
    }

    "Provide the organization config settings" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getConfig(codeSchoolOrgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile(""".*?"blog":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"contests":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"defaultLanguage":"\w+".*"""))
        resultString must beMatching(Pattern.compile(""".*?"mapDocumentationGeneration":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"marketingCampaigns":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"marketingEngagementCampaigns":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"problemBoard":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesAds":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesCertificates":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesCourses":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesFreeTrials":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesMemberFeesActive":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesMemberFeesStatic":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesMemberships":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesOrganizations":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"supportTier":\d+.*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingAnswerTimeTracking":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingBreakTime":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingComplianceEnforcement":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingComments":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingContentPageStudentSubmission":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingContentPageTimeTracking":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingContentPageUpvote":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingCorrectAnswerAnimation":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingCorrectAnswerSound":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingFeedback":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingLearningPaths":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingMnemonics":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingQuotes":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingRankings":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingReporting":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingRewardsProgram":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingSessionEndCelebration":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingSessionEndFeedback":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingStraightThruMode":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingStudyGoals":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingVirtualLabs":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"whiteLabeled":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"memberMonthlyCost":(\d+\.\d+|\d+).*"""))
        resultString must beMatching(Pattern.compile(""".*?"memberAnnualCost":(\d+\.\d+|\d+).*"""))
        resultString must beMatching(Pattern.compile(""".*?"memberPaymentMethodRequired":(true|false).*"""))

      }
    }

    "Reject bad JSON format with \"error.path.missing\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createOrg)
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.path\.missing.*"""))
      }
    }

    "Reject bad JSON format with \"error.expected.jsboolean\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name"       -> "Dummy",
          "domain"     -> "dummy.com",
          "familyPlan" -> "a string instead of a boolean",
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createOrg)
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.expected\.jsboolean.*"""))
      }
    }

    "Reject bad JSON format with \"error.expected.jsstring\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name"       -> "Dummy",
          "domain"     -> false, // boolean instead of string
          "familyPlan" -> false,
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createOrg)
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.expected\.jsstring.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name"       -> "Dummy",
          "domain"     -> "dummy.com",
          "familyPlan" -> false,
        )

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.createOrg)
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST `/org/:orgID/blog`

  "The POST `/org/:orgID/blog` action" should {
    "Create \"Code School\" Blog Site" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "subDomain" -> codeSchoolDomain
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createBlog(codeSchoolOrgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result = route(app, request).get
        status(result) must beEqualTo(OK)
      }
    }

    "Reject duplicate" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "subDomain" -> codeSchoolDomain
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createOrg)
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)

        resultString must beMatching(Pattern.compile("""(?s).*message.*"""))
      }
    }

    "Reject bad JSON format with \"error.path.missing\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createOrg)
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result = route(app, request).get
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.path\.missing.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "subDomain" -> "Dummy"
        )

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.createOrg)
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST `/org/:orgID/blog/build`

  "The POST `/org/:orgID/blog/build` action" should {
    "Build \"Code School\" Blog Site" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.buildBlog(codeSchoolOrgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result = route(app, request).get
        status(result) must beEqualTo(OK)
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.publishBlog(codeSchoolOrgID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST `/org/:orgID/blog/publish`

  "The POST `/org/:orgID/blog/publish` action" should {
    "Publish \"Code School\" Blog Site" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.publishBlog(codeSchoolOrgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result = route(app, request).get
        status(result) must beEqualTo(OK)
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.publishBlog(codeSchoolOrgID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // PATCH `/org/:orgID/blog`

  "The PATCH `/org/:orgID/blog` action" should {
    "Patch blog site with different theme" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "theme" -> "memoirs2",
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchBlog(codeSchoolOrgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result = route(app, request).get
        status(result) must beEqualTo(OK)
      }
    }

    "Reject bad JSON format with \"error.expected.jsstring\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "theme" -> true // instead of string
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchBlog(codeSchoolOrgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result = route(app, request).get
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.expected\.jsstring.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "theme" -> "memoirs",
        )

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.patchBlog(codeSchoolOrgID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("theme" -> "memoirs")

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.patchBlog(codeSchoolOrgID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // DELETE
  "The DELETE `/org/:orgID/blog` action" should {
    "Delete the \"Code School\" Blog Site" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.deleteBlog(codeSchoolOrgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
      }
    }

    "Reject duplicate" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.deleteBlog(codeSchoolOrgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(BAD_REQUEST)

        contentAsString(result) must beMatching(Pattern.compile("""(?s).*message.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "subDomain" -> "Dummy"
        )

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.deleteBlog(codeSchoolOrgID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // GET `/member/profile`
  // verify members exist or test failures might be very confusing

  "The GET `/member/profile` action" should {
    "Return a profile for token: fakeUserSubjectID" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.MemberController.getProfile())
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)

        val pattern: Regex =
          """\{.*?"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*\}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        profile1UUID = ProfileID(UUID.fromString(newUUID))
      }
    }

    "Return a profile for token: fakeUser2SubjectID" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.MemberController.getProfile())
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUser2SubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)

        val pattern: Regex =
          """\{.*?"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*\}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        profile2UUID = ProfileID(UUID.fromString(newUUID))
      }
    }

    "Return a profile for token: fakeUser3SubjectID" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.MemberController.getProfile())
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUser3SubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)

        val pattern: Regex =
          """\{.*?"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*\}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        profile3UUID = ProfileID(UUID.fromString(newUUID))
      }
    }
  }

  // GET `/org/:orgID/members`

  "The GET `/org/:orgID/members` action" should {
    "Return members list with profile1UUID and profile2UUID" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getOrgMembers(orgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(profile1UUID)))
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(profile2UUID)))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.getOrgMembers(invalidOrgID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getOrgMembers(orgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUser2SubjectID)).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.getOrgMembers(orgID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST `/org/:orgID/role`

  "The POST `/org/:orgID/role` action" should {
    "Create \"Role Test #1\" with all permissions set to true and return UUID" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name"                                -> "Role Test #1",
          "autoJoin"                            -> true,
          "blogApprove"                         -> true,
          "blogCreate"                          -> true,
          "blogDelete"                          -> true,
          "blogPublish"                         -> true,
          "manageBlog"                          -> true,
          "manageContests"                      -> true,
          "manageMarketingCampaigns"            -> true,
          "manageOrganizationBilling"           -> true,
          "manageOrganizationConfig"            -> true,
          "manageOrganizationMembers"           -> true,
          "manageOrganizationPermissions"       -> true,
          "manageOrganizationWhitelabel"        -> true,
          "manageRewardsProgram"                -> true,
          "manageSalesAds"                      -> true,
          "manageSalesCertificates"             -> true,
          "manageSalesCourses"                  -> true,
          "manageSalesMemberships"              -> true,
          "manageSalesOrganizations"            -> true,
          "manageTrainingBreakTime"             -> true,
          "manageTrainingComplianceEnforcement" -> true,
          "manageTrainingQuotes"                -> true,
          "manageTrainingSessionSettings"       -> true,
          "manageProblemBoard"                  -> true,
          "mapApprove"                          -> true,
          "mapCreate"                           -> true,
          "mapCreateDocuments"                  -> true,
          "mapDirectory"                        -> true,
          "mapFeedback"                         -> true,
          "mapFork"                             -> true,
          "mapMnemonics"                        -> true,
          "mapModify"                           -> true,
          "mapPermissions"                      -> true,
          "mapPublish"                          -> true,
          "mapShare"                            -> true,
          "mapStats"                            -> true,
          "mapTraining"                         -> true,
          "mapTransfer"                         -> true,
          "mapView"                             -> true,
          "organizationInvite"                  -> true,
          "organizationPublish"                 -> true,
          "trainingRankings"                    -> true,
          "trainingReporting"                   -> true
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createRole(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)

        val pattern: Regex =
          """\{"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"\}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        role1UUID = RoleID(UUID.fromString(newUUID))
      }
    }

    "Create \"Role Test #2\" with all permissions set to false and return UUID" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name"                                -> "Role Test #2",
          "autoJoin"                            -> true,
          "blogApprove"                         -> false,
          "blogCreate"                          -> false,
          "blogDelete"                          -> false,
          "blogPublish"                         -> false,
          "manageBlog"                          -> false,
          "manageContests"                      -> false,
          "manageMarketingCampaigns"            -> false,
          "manageOrganizationBilling"           -> false,
          "manageOrganizationConfig"            -> false,
          "manageOrganizationMembers"           -> false,
          "manageOrganizationPermissions"       -> false,
          "manageOrganizationWhitelabel"        -> false,
          "manageRewardsProgram"                -> false,
          "manageSalesAds"                      -> false,
          "manageSalesCertificates"             -> false,
          "manageSalesCourses"                  -> false,
          "manageSalesMemberships"              -> false,
          "manageSalesOrganizations"            -> false,
          "manageTrainingBreakTime"             -> false,
          "manageTrainingComplianceEnforcement" -> false,
          "manageTrainingQuotes"                -> false,
          "manageTrainingSessionSettings"       -> false,
          "manageProblemBoard"                  -> false,
          "mapApprove"                          -> false,
          "mapCreate"                           -> false,
          "mapCreateDocuments"                  -> false,
          "mapDirectory"                        -> false,
          "mapFeedback"                         -> false,
          "mapFork"                             -> false,
          "mapMnemonics"                        -> false,
          "mapModify"                           -> false,
          "mapPermissions"                      -> false,
          "mapPublish"                          -> false,
          "mapShare"                            -> false,
          "mapStats"                            -> false,
          "mapTraining"                         -> false,
          "mapTransfer"                         -> false,
          "mapView"                             -> false,
          "organizationInvite"                  -> false,
          "organizationPublish"                 -> false,
          "trainingRankings"                    -> false,
          "trainingReporting"                   -> false
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createRole(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)

        val pattern: Regex =
          """\{"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"\}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        role2UUID = RoleID(UUID.fromString(newUUID))
      }
    }

    "Create \"Role Test #3\" with all permissions set to default and return UUID" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name" -> "Role Test #3"
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createRole(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)

        val pattern: Regex =
          """\{"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"\}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        role3UUID = RoleID(UUID.fromString(newUUID))
      }
    }

    "Reject bad JSON format with \"error.path.missing\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createRole(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.path\.missing.*"""))
      }
    }

    "Reject bad JSON format with \"error.expected.jsboolean\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name"        -> "A Name",
          "blogApprove" -> "a string instead of boolean"
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createRole(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.expected\.jsboolean.*"""))
      }
    }

    "Reject bad JSON format with \"error.expected.jsstring\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name" -> true // instead of string
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.createRole(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.expected\.jsstring.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("name" -> "role name")

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.createRole(invalidOrgID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("name" -> "role name")

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.createRole(orgID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("name" -> "role name")

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.createRole(orgID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // GET `/org/:orgID/role/:roleID`

  "The GET `/org/:orgID/role/:roleID` action" should {
    "Return the role for role1UUID and show all permissions set to true" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getRole(orgID, role1UUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(role1UUID)))
        resultString must_!= beMatching(Pattern.compile(""".*?false.*"""))
      }
    }

    "Return the role for role2UUID and show all permissions set to false" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getRole(orgID, role2UUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(role2UUID)))
        resultString must_!= beMatching(Pattern.compile(""".*?true.*"""))
      }
    }

    "Return the role for role3UUID and show all permissions set to false" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getRole(orgID, role3UUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(role3UUID)))
        resultString must_!= beMatching(Pattern.compile(""".*?true.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.getRole(invalidOrgID, role1UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getRole(orgID, role1UUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUser2SubjectID)).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.getRole(orgID, role1UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // GET `/org/:orgID/roles`

  "The GET `/org/:orgID/roles` action" should {
    "Return the roles we created" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getRoles(orgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(role1UUID)))
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(role2UUID)))
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(role3UUID)))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.getRoles(invalidOrgID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getRoles(orgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUser2SubjectID)).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.getRoles(orgID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST `/org/:orgID/role/:roleID/invite`

  "The POST `/org/:orgID/role/:roleID/invite` action" should {
    "Send invite to \"Role Test #1\" for a valid emailAddress" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("emailAddress" -> testEmailAddress)

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.roleInvite(orgID, role1UUID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)
        val pattern: Regex =
          """\{"status":20[0-2],"inviteID":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"\}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        invite1UUID = RoleInviteID(UUID.fromString(newUUID))
      }
    }

    "Allow us to join fakeUser3SubjectID (Bob) to \"Role Test #1\"" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.join(invite1UUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUser3SubjectID)).get // bob's token
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "and cause fakeUser3SubjectID (Bob) / profile3UUID to show in the organization members list" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getOrgMembers(orgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(profile3UUID)))
      }
    }

    "after which we'll remove fakeUser3SubjectID (Bob) from the organization" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.removeOrgMember(orgID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Need a new mind map to include in the invite request" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue =
          Json.obj("name" -> "Chemistry", "mode" -> "MAP", "description" -> "Map for testing org invites")

        val request: Request[JsValue] = FakeRequest(routes.MapController.createMap(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)

        val pattern: Regex =
          """\{"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        chemistryMapUUID = MapID(UUID.fromString(newUUID))
      }
    }

    "Need a mapRightsID from the new mind map" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.MapController.getMapRights(orgID, chemistryMapUUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString: String = contentAsString(result)

        val pattern: Regex =
          """.*?"rights":.*?"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        chemistryMapRightsUUID = MapRightsID(UUID.fromString(newUUID))
      }
    }

    "Send invite to \"Role Test #1\" AND Chemistry mapRightsID for a valid emailAddress" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("emailAddress" -> testEmailAddress, "mapRightsID" -> chemistryMapRightsUUID)

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.roleInvite(orgID, role1UUID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)
        val pattern: Regex =
          """\{"status":20[0-2],"inviteID":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"\}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        invite1UUID = RoleInviteID(UUID.fromString(newUUID))
      }
    }

    "Reject request for invite to \"Role Test #1\" for an invalid emailAddress" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "emailAddress" -> "invalid"
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.roleInvite(orgID, role1UUID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)

        resultString must beMatching("""\{"status":400\}""".r)
      }
    }

    "Reject bad JSON format with \"error.path.missing\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.roleInvite(orgID, role1UUID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.path\.missing.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("emailAddress" -> testEmailAddress)

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.roleInvite(invalidOrgID, role1UUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("emailAddress" -> testEmailAddress)

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.roleInvite(orgID, role1UUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("emailAddress" -> testEmailAddress)

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.roleInvite(orgID, role1UUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST `/invite/:inviteID/join`

  "The POST `/invite/:inviteID/join` action" should {
    "Join fakeUser3SubjectID (Bob) \"Role Test #1\" and Chemistry mapRightsID" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.join(invite1UUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUser3SubjectID)).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Cause mapRights members to list profile3UUID (Bob) as a member" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.MapController.getMapRightsByID(orgID, chemistryMapUUID, chemistryMapRightsUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)
        val resultString: String = contentAsString(result)

        val pattern: Regex =
          """.*?"members":.*?"%s".*""".format(profile3UUID).r
        resultString must beMatching(pattern)
      }
    }

    "Cause fakeUser3SubjectID (Bob) / profile3UUID to show in the organization members list" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getOrgMembers(orgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(profile3UUID)))
      }
    }

    "Reject duplicate join request for fakeUser3SubjectID (Bob) \"Role Test #1\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.join(invite1UUID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUser3SubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
      }
    }

    "Reject invalid inviteID as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.join(RoleInviteID.random))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser3SubjectID)
        ).get
        status(result) must beEqualTo(BAD_REQUEST)
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.join(invite1UUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST `/org/:orgID/role/:roleID/member/:profileID`

  "The POST `/org/:orgID/role/:roleID/member/:profileID` action" should {
    "Join fakeUser3SubjectID (Bob) \"Role Test #2\"" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.addRoleMember(orgID, role2UUID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)

        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Pass validation that profile3UUID (Bob) was added to \"Role Test #2\"" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getRoleMembers(orgID, role2UUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get // not bob
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(profile3UUID)))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.addRoleMember(invalidOrgID, role2UUID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.addRoleMember(orgID, role2UUID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.addRoleMember(orgID, role2UUID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // DELETE `/org/:orgID/role/:roleID/member/:profileID`

  "The DELETE `/org/:orgID/role/:roleID/member/:profileID` action" should {
    "Delete fakeUser3SubjectID (Bob) \"Role Test #2\"" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.removeRoleMember(orgID, role2UUID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Pass validation that profile3UUID (Bob) was removed from \"Role Test #2\"" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getRoleMembers(orgID, role2UUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get // not bob
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must_!= beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(profile3UUID)))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.removeRoleMember(invalidOrgID, role2UUID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.removeRoleMember(orgID, role2UUID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.removeRoleMember(orgID, role2UUID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // GET `/org/:orgID/role/:roleID/members`

  "The GET `/org/:orgID/role/:roleID/members` action" should {
    "Return the member list with profile3UUID (Bob)" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getRoleMembers(orgID, role1UUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get // not bob
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(profile3UUID)))
      }
    }

    "Return the member list with profile3UUID (Bob) with Bob's own TokenID" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getRoleMembers(orgID, role1UUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUser3SubjectID)).get // bob
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(profile3UUID)))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.getRoleMembers(invalidOrgID, role1UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getRoleMembers(orgID, role1UUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUser2SubjectID)).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.getRoleMembers(orgID, role1UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // PATCH `/org/:orgID/role/:roleID`

  "The PATCH `/org/:orgID/role/:roleID` action" should {
    "Patch \"Role Test #1\" with all permissions set to false and name to \"Role Patch Test #1\"" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name"                                -> "Role Patch Test #1",
          "autoJoin"                            -> false,
          "blogApprove"                         -> false,
          "blogCreate"                          -> false,
          "blogDelete"                          -> false,
          "blogPublish"                         -> false,
          "manageBlog"                          -> false,
          "manageContests"                      -> false,
          "manageMarketingCampaigns"            -> false,
          "manageOrganizationBilling"           -> false,
          "manageOrganizationConfig"            -> false,
          "manageOrganizationMembers"           -> false,
          "manageOrganizationPermissions"       -> false,
          "manageOrganizationWhitelabel"        -> false,
          "manageRewardsProgram"                -> false,
          "manageSalesAds"                      -> false,
          "manageSalesCertificates"             -> false,
          "manageSalesCourses"                  -> false,
          "manageSalesMemberships"              -> false,
          "manageSalesOrganizations"            -> false,
          "manageTrainingBreakTime"             -> false,
          "manageTrainingComplianceEnforcement" -> false,
          "manageTrainingQuotes"                -> false,
          "manageTrainingSessionSettings"       -> false,
          "manageProblemBoard"                  -> false,
          "mapApprove"                          -> false,
          "mapCreate"                           -> false,
          "mapCreateDocuments"                  -> false,
          "mapDirectory"                        -> false,
          "mapFeedback"                         -> false,
          "mapFork"                             -> false,
          "mapMnemonics"                        -> false,
          "mapModify"                           -> false,
          "mapPermissions"                      -> false,
          "mapPublish"                          -> false,
          "mapShare"                            -> false,
          "mapStats"                            -> false,
          "mapTraining"                         -> false,
          "mapTransfer"                         -> false,
          "mapView"                             -> false,
          "organizationInvite"                  -> false,
          "organizationPublish"                 -> false,
          "trainingRankings"                    -> false,
          "trainingReporting"                   -> false
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchRole(orgID, role1UUID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)

      }
    }

    "Pass validation for all permissions patched to false on role1UUID" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getRole(orgID, role1UUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(role1UUID)))
        resultString must_!= beMatching(Pattern.compile(""".*?true.*"""))
      }
    }

    "Patch \"Role Test #2\" with blogApprove -> true" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("blogApprove" -> true)

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchRole(orgID, role2UUID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)

      }
    }

    "Pass validation for blogApproved patched to true on role2UUID" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getRole(orgID, role2UUID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(role2UUID)))
        resultString must_!= beMatching(Pattern.compile(""".*?"blogApprove":true.*"""))
      }
    }

    "Reject bad roleID as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("blogApprove" -> true)

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchRole(orgID, RoleID.random))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)

      }
    }

    "Reject bad JSON format with \"error.expected.jsboolean\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name"        -> "A Name",
          "blogApprove" -> "a string instead of boolean"
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchRole(orgID, role1UUID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.expected\.jsboolean.*"""))
      }
    }

    "Reject bad JSON format with \"error.expected.jsstring\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name" -> true // instead of string
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchRole(orgID, role1UUID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.expected\.jsstring.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("name" -> "role name")

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.patchRole(invalidOrgID, role1UUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("name" -> "role name")

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.patchRole(orgID, role1UUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("name" -> "role name")

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.patchRole(orgID, role1UUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // DELETE `/org/:orgID/member/:profileID`

  "The DELETE `/org/:orgID/member/:profileID` action" should {
    "Remove fakeUser3SubjectID (Bob) from organization" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.removeOrgMember(orgID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Cause fakeUser3SubjectID (Bob) / profile3UUID to NOT show in the organization members list" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getOrgMembers(orgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must_!= beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(profile3UUID)))
      }
    }

    "Reject duplicate remove request for fakeUser3SubjectID (Bob) as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.removeOrgMember(orgID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
      }
    }

    "Reject invalid profileID as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.removeOrgMember(orgID, ProfileID.random))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(BAD_REQUEST)
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.removeOrgMember(invalidOrgID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.removeOrgMember(orgID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.removeOrgMember(orgID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // GET `/org/:orgID/config`

  "The GET `/org/:orgID/config` action" should {
    "Provide the organization config settings" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getConfig(orgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must beMatching(Pattern.compile(""".*?"blog":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"contests":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"defaultLanguage":"\w+".*"""))
        resultString must beMatching(Pattern.compile(""".*?"mapDocumentationGeneration":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"marketingCampaigns":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"marketingEngagementCampaigns":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"problemBoard":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesAds":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesCertificates":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesCourses":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesFreeTrials":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesMemberFeesActive":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesMemberFeesStatic":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesMemberships":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"salesOrganizations":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"supportTier":\d+.*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingAnswerTimeTracking":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingBreakTime":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingComplianceEnforcement":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingComments":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingContentPageStudentSubmission":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingContentPageTimeTracking":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingContentPageUpvote":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingCorrectAnswerAnimation":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingCorrectAnswerSound":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingFeedback":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingLearningPaths":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingMnemonics":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingQuotes":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingRankings":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingReporting":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingRewardsProgram":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingSessionEndCelebration":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingSessionEndFeedback":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingStraightThruMode":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingStudyGoals":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"trainingVirtualLabs":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"whiteLabeled":(true|false).*"""))
        resultString must beMatching(Pattern.compile(""".*?"memberMonthlyCost":(\d+\.\d+|\d+).*"""))
        resultString must beMatching(Pattern.compile(""".*?"memberAnnualCost":(\d+\.\d+|\d+).*"""))
        resultString must beMatching(Pattern.compile(""".*?"memberPaymentMethodRequired":(true|false).*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.getConfig(invalidOrgID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getConfig(orgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUser2SubjectID)).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.getConfig(orgID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // PATCH `/org/:orgID/config`

  "The PATCH `/org/:orgID/config` action" should {
    "Patch org config with all configurations set to false" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "blog"                                 -> false,
          "contests"                             -> false,
          "defaultLanguage"                      -> "es",
          "mapDocumentationGeneration"           -> false,
          "marketingCampaigns"                   -> false,
          "marketingEngagementCampaigns"         -> false,
          "problemBoard"                         -> false,
          "salesAds"                             -> false,
          "salesCertificates"                    -> false,
          "salesCourses"                         -> false,
          "salesFreeTrials"                      -> false,
          "salesMemberFeesActive"                -> false,
          "salesMemberFeesStatic"                -> false,
          "salesMemberships"                     -> false,
          "salesOrganizations"                   -> false,
          "supportTier"                          -> 2,
          "trainingAnswerTimeTracking"           -> false,
          "trainingBreakTime"                    -> false,
          "trainingComplianceEnforcement"        -> false,
          "trainingComments"                     -> false,
          "trainingContentPageStudentSubmission" -> false,
          "trainingContentPageTimeTracking"      -> false,
          "trainingContentPageUpvote"            -> false,
          "trainingCorrectAnswerAnimation"       -> false,
          "trainingCorrectAnswerSound"           -> false,
          "trainingFeedback"                     -> false,
          "trainingLearningPaths"                -> false,
          "trainingMnemonics"                    -> false,
          "trainingQuotes"                       -> false,
          "trainingRankings"                     -> false,
          "trainingReporting"                    -> false,
          "trainingRewardsProgram"               -> false,
          "trainingSessionEndCelebration"        -> false,
          "trainingSessionEndFeedback"           -> false,
          "trainingStraightThruMode"             -> false,
          "trainingStudyGoals"                   -> false,
          "trainingVirtualLabs"                  -> false,
          "whiteLabeled"                         -> false,
          "memberMonthlyCost"                    -> 30.99,
          "memberAnnualCost"                     -> 200.99,
          "memberPaymentMethodRequired"          -> false,
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchConfig(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)

      }
    }

    "Pass validation for all config options patched to false" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getConfig(orgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must_!= beMatching(Pattern.compile(""".*?true.*"""))
        resultString must beMatching(Pattern.compile(""".*?"defaultLanguage":"es".*"""))
        resultString must beMatching(Pattern.compile(""".*?"supportTier":2.*"""))
        resultString must beMatching(Pattern.compile(""".*?"memberMonthlyCost":30.99.*"""))
        resultString must beMatching(Pattern.compile(""".*?"memberAnnualCost":200.99.*"""))
      }
    }

    "Patch org config with all configurations set to true" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "blog"                                 -> true,
          "contests"                             -> true,
          "defaultLanguage"                      -> "en",
          "mapDocumentationGeneration"           -> true,
          "marketingCampaigns"                   -> true,
          "marketingEngagementCampaigns"         -> true,
          "problemBoard"                         -> true,
          "salesAds"                             -> true,
          "salesCertificates"                    -> true,
          "salesCourses"                         -> true,
          "salesFreeTrials"                      -> true,
          "salesMemberFeesActive"                -> true,
          "salesMemberFeesStatic"                -> true,
          "salesMemberships"                     -> true,
          "salesOrganizations"                   -> true,
          "supportTier"                          -> 1,
          "trainingAnswerTimeTracking"           -> true,
          "trainingBreakTime"                    -> true,
          "trainingComplianceEnforcement"        -> true,
          "trainingComments"                     -> true,
          "trainingContentPageStudentSubmission" -> true,
          "trainingContentPageTimeTracking"      -> true,
          "trainingContentPageUpvote"            -> true,
          "trainingCorrectAnswerAnimation"       -> true,
          "trainingCorrectAnswerSound"           -> true,
          "trainingFeedback"                     -> true,
          "trainingLearningPaths"                -> true,
          "trainingMnemonics"                    -> true,
          "trainingQuotes"                       -> true,
          "trainingRankings"                     -> true,
          "trainingReporting"                    -> true,
          "trainingRewardsProgram"               -> true,
          "trainingSessionEndCelebration"        -> true,
          "trainingSessionEndFeedback"           -> true,
          "trainingStraightThruMode"             -> true,
          "trainingStudyGoals"                   -> true,
          "trainingVirtualLabs"                  -> true,
          "whiteLabeled"                         -> true,
          "memberMonthlyCost"                    -> 29.99,
          "memberAnnualCost"                     -> 199.99,
          "memberPaymentMethodRequired"          -> true,
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchConfig(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)

      }
    }

    "Pass validation for all config options patched to true" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getConfig(orgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must_!= beMatching(Pattern.compile(""".*?false.*"""))
        resultString must beMatching(Pattern.compile(""".*?"defaultLanguage":"en".*"""))
        resultString must beMatching(Pattern.compile(""".*?"supportTier":1.*"""))
        resultString must beMatching(Pattern.compile(""".*?"memberMonthlyCost":29.99.*"""))
        resultString must beMatching(Pattern.compile(""".*?"memberAnnualCost":199.99.*"""))
      }
    }

    "Patch org config with \"whiteLabeled\" configuration set to false" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "whiteLabeled" -> false
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchConfig(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)

      }
    }

    "Pass validation for \"whiteLabeled\" config option patched to false" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getConfig(orgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must_!= beMatching(Pattern.compile(""".*?"whiteLabeled":false.*"""))
      }
    }

    "Patch org config with \"whiteLabeled\" configuration set to true" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "whiteLabeled" -> true
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchConfig(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)

      }
    }

    "Pass validation for \"whiteLabeled\" config option patched to true" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val result = route(app,
                           FakeRequest(routes.OrganizationController.getConfig(orgID))
                             .withHeaders(CONTENT_TYPE -> JSON)
                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString = contentAsString(result)
        resultString must_!= beMatching(Pattern.compile(""".*?"whiteLabeled":true.*"""))
      }
    }

    "Reject bad JSON format with \"error.expected.jsboolean\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "blog" -> "a string instead of boolean"
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchConfig(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.expected\.jsboolean.*"""))
      }
    }

    "Reject bad JSON format with \"error.expected.jsstring\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "defaultLanguage" -> true // instead of string
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchConfig(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.expected\.jsstring.*"""))
      }
    }

    "Reject bad JSON format with \"error.expected.jsnumber\" as BAD_REQUEST" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "supportTier" -> "string instead of number" // instead of number
        )

        val request: Request[JsValue] = FakeRequest(routes.OrganizationController.patchConfig(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result               = route(app, request).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.expected\.jsnumber.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.patchConfig(invalidOrgID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("blog" -> false)

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.patchConfig(orgID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new OrganizationControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("blog" -> false)

        val result = route(
          app,
          FakeRequest(routes.OrganizationController.patchConfig(orgID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

}
