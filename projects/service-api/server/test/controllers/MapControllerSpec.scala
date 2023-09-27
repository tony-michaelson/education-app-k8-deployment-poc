package controllers

import java.util.UUID
import java.util.regex.Pattern

import io.masterypath.slick.{MapID, MapRightsID, NodeID, ProfileID, RoleInviteID, SegmentID}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import play.api.mvc.{Request, Result}
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.matching.Regex

/**
  * Test case for the [[MapController]] class.
  */
class MapControllerSpec extends PlaySpecification {
  sequential

  // placeholder UUID variables
  var profile1UUID: ProfileID               = ProfileID.random
  var profile2UUID: ProfileID               = ProfileID.random
  var profile3UUID: ProfileID               = ProfileID.random
  var programmingMapUUID: MapID             = MapID.random
  var programmingMapRightsUUID: MapRightsID = MapRightsID.random
  var programmingSubMapUUID: MapID          = MapID.random
  var rootUUID: NodeID                      = NodeID.random
  var programmingCategoryUUID: NodeID       = NodeID.random
  var scalaCategoryUUID: NodeID             = NodeID.random
  var pythonCategoryUUID: NodeID            = NodeID.random
  var scalatestFlashcardUUID: NodeID        = NodeID.random
  var fibonacciFlashcardUUID: NodeID        = NodeID.random
  var fibonacciFlashcardAnswerUUID1: UUID   = UUID.randomUUID()
  var fibonacciFlashcardAnswerUUID2: UUID   = UUID.randomUUID()
  var fibonacciFlashcardAnswerUUID3: UUID   = UUID.randomUUID()
  var invite1UUID: RoleInviteID             = RoleInviteID.random

  // nodeNumbers
  val programmingCategoryNodeNumber: Int = 2
  val scalaCategoryNodeNumber: Int       = 3
  val scalaTestFlashcardNodeNumber: Int  = 4
  val pythonCategoryNodeNumber: Int      = 5
  val fibonacciFlashcardNodeNumber: Int  = 6

  // GET `/member/profile`
  // verify members exist or test failures might be very confusing

  "The GET `/member/profile` action" should {
    "Return a profile for token: fakeUserSubjectID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.MemberController.getProfile())
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString: String = contentAsString(result)

        val pattern: Regex =
          """\{.*?"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        profile1UUID = ProfileID(UUID.fromString(newUUID))
      }
    }

    "Return a profile for token: fakeUser2SubjectID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.MemberController.getProfile())
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, fakeUser2SubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString: String = contentAsString(result)

        val pattern: Regex =
          """\{.*?"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        profile2UUID = ProfileID(UUID.fromString(newUUID))
      }
    }

    "Return a profile for token: fakeUser3SubjectID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.MemberController.getProfile())
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, fakeUser3SubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString: String = contentAsString(result)

        val pattern: Regex =
          """\{.*?"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        profile3UUID = ProfileID(UUID.fromString(newUUID))
      }
    }
  }

  // POST `/org/:orgID/mymaps`

  "The POST `/org/:orgID/mymaps` action" should {
    "Return OK for a valid request with an optional description" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue =
          Json.obj("name" -> "My First Map", "mode" -> "MAP", "description" -> "My first mind map")

        val request: Request[JsValue] = FakeRequest(routes.MapController.createMap(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        val pattern: Regex =
          """\{"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        programmingMapUUID = MapID(UUID.fromString(newUUID))
      }
    }

    "Return OK for a valid request with no description" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("name" -> "My Second Map", "mode" -> "MAP")

        val request: Request[JsValue] = FakeRequest(routes.MapController.createMap(orgID))
          .withBody(jsonData)
          .withHeaders(CONTENT_TYPE -> JSON)
          .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)

        val pattern: Regex =
          """\{"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"}""".r
        resultString must beMatching(pattern)
      }
    }

    "Reject bad JSON format with \"error.path.missing\" as BAD_REQUEST" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.MapController.createMap(orgID))
                                             .withBody(jsonData)
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.path\.missing.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue =
          Json.obj("name" -> "My First Map", "mode" -> "MAP", "description" -> "My first mind map")

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.MapController.createMap(invalidOrgID))
                                             .withBody(jsonData)
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue =
          Json.obj("name" -> "My First Map", "mode" -> "MAP", "description" -> "My first mind map")

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.MapController.createMap(orgID))
                                             .withBody(jsonData)
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, fakeUser2SubjectID)).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue =
          Json.obj("name" -> "My First Map", "mode" -> "MAP", "description" -> "My first mind map")

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.createMap(orgID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // GET `/org/:orgID/map/:mapID/rights`

  "The GET `/org/:orgID/map/:mapID/rights` action" should {
    "Return the mapRights and members" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.MapController.getMapRights(orgID, programmingMapUUID))
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString: String = contentAsString(result)

        val pattern: Regex =
          """.*?"rights":.*?"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        programmingMapRightsUUID = MapRightsID(UUID.fromString(newUUID))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.getMapRights(invalidOrgID, programmingMapUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.MapController.getMapRights(orgID, programmingMapUUID))
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, fakeUser2SubjectID)).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.getMapRights(orgID, programmingMapUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST /org/:orgID/map/:mapID/rights/:rightsID/member/:profileID

  "The POST `/org/:orgID/map/:mapID/rights/:rightsID/member/:profileID` action" should {
    "Add \"User1\" (profile3UUID) to programmingMapRights" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.addMapRightsMember(orgID, programmingMapUUID, programmingMapRightsUUID, profile2UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        val resultString: String = contentAsString(result)

        status(result) must beEqualTo(OK)
      }
    }

    "Cause \"User1\" to show up in the members list for the programmingMapRights UUID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.getMapRightsByID(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)
        val resultString: String = contentAsString(result)

        val pattern: Regex =
          """.*?"members":.*?"%s".*""".format(profile2UUID).r
        resultString must beMatching(pattern)
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.addMapRightsMember(invalidOrgID,
                                                    programmingMapUUID,
                                                    programmingMapRightsUUID,
                                                    profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.addMapRightsMember(orgID, programmingMapUUID, programmingMapRightsUUID, profile2UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.addMapRightsMember(orgID, programmingMapUUID, programmingMapRightsUUID, profile2UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // DELETE /org/:orgID/map/:mapID/rights/:rightsID/member/:profileID

  "The DELETE `/org/:orgID/map/:mapID/rights/:rightsID/member/:profileID` action" should {
    "Remove \"User1\" (profile3UUID) from programmingMapRights" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.removeMapRightsMember(orgID,
                                                       programmingMapUUID,
                                                       programmingMapRightsUUID,
                                                       profile2UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Cause \"User1\" to NOT show up in the members list for the programmingMapRights UUID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.getMapRightsByID(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)
        val resultString: String = contentAsString(result)

        val pattern: Regex =
          """.*?"members":.*?"%s".*""".format(profile2UUID).r
        resultString must_!= beMatching(pattern)
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.removeMapRightsMember(invalidOrgID,
                                                       programmingMapUUID,
                                                       programmingMapRightsUUID,
                                                       profile2UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.removeMapRightsMember(orgID,
                                                       programmingMapUUID,
                                                       programmingMapRightsUUID,
                                                       profile2UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.removeMapRightsMember(orgID,
                                                       programmingMapUUID,
                                                       programmingMapRightsUUID,
                                                       profile2UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST /org/:orgID/map/:mapID/rights

  "The POST `/org/:orgID/map/:mapID/rights` action" should {
    "Return OK post request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name"        -> "programming map rights #2",
          "admin"       -> true,
          "feedback"    -> false,
          "mnemonics"   -> false,
          "modify"      -> false,
          "publish"     -> false,
          "permissions" -> false,
          "share"       -> false,
          "stats"       -> false,
          "training"    -> false,
          "transfer"    -> false,
          "view"        -> false
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.createMapRights(orgID, programmingMapUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get

        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Respond with UNAUTHORIZED for bad mapID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "name"        -> "programming map rights #2",
          "admin"       -> true,
          "feedback"    -> false,
          "mnemonics"   -> false,
          "modify"      -> false,
          "publish"     -> false,
          "permissions" -> false,
          "share"       -> false,
          "stats"       -> false,
          "training"    -> false,
          "transfer"    -> false,
          "view"        -> false
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.createMapRights(orgID, MapID.random))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(UNAUTHORIZED)
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.createMapRights(invalidOrgID, programmingMapUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.createMapRights(orgID, programmingMapUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.createMapRights(orgID, programmingMapUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // PATCH /org/:orgID/map/:mapID/rights
  // *note; we start testing the mapRights as soon as the map exists.
  // This way we'll obtain the row ID for the mapRights table entry
  // and update different columns directly to support permission testing.

  "The PATCH `/org/:orgID/map/:mapID/rights` action" should {
    "Return OK and return mapRights for a patch request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "admin"        -> true,
          "feedback"     -> false,
          "mnemonics"    -> false,
          "modify"       -> false,
          "publish"      -> false,
          "permissions"  -> false,
          "share"        -> false,
          "stats"        -> false,
          "training"     -> false,
          "transfer"     -> false,
          "view"         -> false,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get

        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
        resultString must beMatching(""".*?"admin":true.*""".r)
        resultString must beMatching(""".*?"feedback":false.*""".r)
        resultString must beMatching(""".*?"mnemonics":false.*""".r)
        resultString must beMatching(""".*?"modify":false.*""".r)
        resultString must beMatching(""".*?"permissions":false.*""".r)
        resultString must beMatching(""".*?"publish":false.*""".r)
        resultString must beMatching(""".*?"share":false.*""".r)
        resultString must beMatching(""".*?"stats":false.*""".r)
        resultString must beMatching(""".*?"training":false.*""".r)
        resultString must beMatching(""".*?"transfer":false.*""".r)
        resultString must beMatching(""".*?"view":false.*""".r)
      }
    }

    "Respond with {\"ret\": \"OK\", \"permissions\": true, \"admin\": false} for an update that relies on \"admin\" rights." in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "permissions"  -> true,
          "admin"        -> false,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(""".*?"id":"%s".*""".format(programmingMapRightsUUID).r)

        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
        resultString must beMatching(""".*?"permissions":true.*""".r)
        resultString must beMatching(""".*?"admin":false.*""".r)
      }
    }

    "Respond with {\"ret\": \"OK\", \"admin\": true} for an update that relies on \"permission\" rights." in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "admin"        -> true,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(""".*?"id":"%s".*""".format(programmingMapRightsUUID).r)

        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
        resultString must beMatching(""".*?"admin":true.*""".r)
      }
    }

    "Respond with {\"ret\": \"OK\", \"feedback\": true} and return mapRights for a patch request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "feedback"     -> true,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(""".*?"id":"%s".*""".format(programmingMapRightsUUID).r)

        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
        resultString must beMatching(""".*?"feedback":true.*""".r)
      }
    }

    "Respond with {\"ret\": \"OK\", \"mnemonics\": true} and return mapRights for a patch request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "mnemonics"    -> true,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(""".*?"id":"%s".*""".format(programmingMapRightsUUID).r)

        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
        resultString must beMatching(""".*?"mnemonics":true.*""".r)
      }
    }

    "Respond with {\"ret\": \"OK\", \"modify\": true} and return mapRights for a patch request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "modify"       -> true,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(""".*?"id":"%s".*""".format(programmingMapRightsUUID).r)

        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
        resultString must beMatching(""".*?"modify":true.*""".r)
      }
    }

    "Respond with {\"ret\": \"OK\", \"permissions\": true} and return mapRights for a patch request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "permissions"  -> true,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(""".*?"id":"%s".*""".format(programmingMapRightsUUID).r)

        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
        resultString must beMatching(""".*?"permissions":true.*""".r)
      }
    }

    "Respond with {\"ret\": \"OK\", \"publish\": true} and return mapRights for a patch request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "publish"      -> true,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(""".*?"id":"%s".*""".format(programmingMapRightsUUID).r)

        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
        resultString must beMatching(""".*?"publish":true.*""".r)
      }
    }

    "Respond with {\"ret\": \"OK\", \"share\": true} and return mapRights for a patch request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "share"        -> true,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(""".*?"id":"%s".*""".format(programmingMapRightsUUID).r)

        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
        resultString must beMatching(""".*?"share":true.*""".r)
      }
    }

    "Respond with {\"ret\": \"OK\", \"stats\": true} and return mapRights for a patch request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "stats"        -> true,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(""".*?"id":"%s".*""".format(programmingMapRightsUUID).r)

        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
        resultString must beMatching(""".*?"stats":true.*""".r)
      }
    }

    "Respond with {\"ret\": \"OK\", \"training\": true} and return mapRights for a patch request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "training"     -> true,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(""".*?"id":"%s".*""".format(programmingMapRightsUUID).r)

        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
        resultString must beMatching(""".*?"training":true.*""".r)
      }
    }

    "Respond with {\"ret\": \"OK\", \"transfer\": true} and return mapRights for a patch request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "transfer"     -> true,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(""".*?"id":"%s".*""".format(programmingMapRightsUUID).r)

        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
        resultString must beMatching(""".*?"transfer":true.*""".r)
      }
    }

    "Respond with {\"ret\": \"OK\", \"view\": true} and return mapRights for a patch request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "view"         -> true,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(""".*?"id":"%s".*""".format(programmingMapRightsUUID).r)

        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
        resultString must beMatching(""".*?"view":true.*""".r)
      }
    }

    // TODO pending ability to create more mapRights and modify by ID
    //    "Respond with {\"ret\": \"OK\", \"permissions\": false} and return mapRights for a patch request on another profileID" in new MapControllerSpecContext {
    //      new WithApplication(application) {
    //
    //        val jsonData: JsValue = Json.obj(
    //          "id" -> mapRightsUUID,
    //          "permissions" -> false,
    //          "returnRights" -> true
    //        )
    //
    //        val request: Request[JsValue] = FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID))
    //          .withBody(jsonData)
    //          .withHeaders(CONTENT_TYPE -> JSON)
    //          .addAttr(fakeUser, fakeUserSubjectID)
    //        val result: Future[Result] = route(app, request).get
    //        val resultString: String = contentAsString(result)
    //        status(result) must beEqualTo(OK)
    //
    //        resultString must beMatching(""".*?"id":"%s".*""".format(programmingMapRightsUUID).r)
    //        resultString must beMatching(""".*?"profileID":"%s".*""".format(profile2UUID).r)
    //        resultString must beMatching(""".*?"mapID":"%s".*""".format(programmingMapUUID).r)
    //        resultString must beMatching(""".*?"permissions":false.*""".r)
    //      }
    //    }

    "Respond with UNAUTHORIZED for bad mapID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "admin"        -> true,
          "returnRights" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.patchMapRights(orgID, MapID.random, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(UNAUTHORIZED)
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.patchMapRights(invalidOrgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Local) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        // revoke right
        Await.result(
          mindMapRepoService.getMapRightsAndUpdate(programmingMapUUID,
                                                   programmingMapRightsUUID,
                                                   mapRights => mapRights.copy(permissions = false, admin = false)),
          5 seconds
        )

        val jsonData: JsValue = Json.obj(
          "view"         -> true,
          "returnRights" -> true
        )
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))

        // grant right
        Await.result(
          mindMapRepoService.getMapRightsAndUpdate(programmingMapUUID,
                                                   programmingMapRightsUUID,
                                                   mapRights => mapRights.copy(permissions = true)),
          5 seconds
        )
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.patchMapRights(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST `/org/:orgID/map/:mapID/rights/:rightsID/invite`

  "The POST `/org/:orgID/map/:mapID/rights/:rightsID/invite` action" should {
    "Send invite to programmingMap for a valid emailAddress" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("emailAddress" -> testEmailAddress)

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.mapRightsInvite(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)
        val pattern: Regex =
          """\{"status":20[0-2],"inviteID":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        invite1UUID = RoleInviteID(UUID.fromString(newUUID))
      }
    }

    "Allow us to join fakeUser3SubjectID (Bob) to programmingMap" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.OrganizationController.join(invite1UUID))
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, fakeUser3SubjectID)).get // bob's token
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "and cause fakeUser3SubjectID (Bob) / profile3UUID to show in the programmingMapRights members list" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.getMapRightsByID(orgID, programmingMapUUID, programmingMapRightsUUID))
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

    "and cause fakeUser3SubjectID (Bob) / profile3UUID to show in the organization members list" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.OrganizationController.getOrgMembers(orgID))
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        val resultString: String = contentAsString(result)
        resultString must beMatching(Pattern.compile("""(?s).*"id":"%s.*""".format(profile3UUID)))
      }
    }

    "after which we'll remove fakeUser3SubjectID (Bob) from the organization" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.OrganizationController.removeOrgMember(orgID, profile3UUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        val resultString: String = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Reject request for invite to programmingMap for an invalid emailAddress" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "emailAddress" -> "invalid"
        )

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.mapRightsInvite(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)

        resultString must beMatching("""\{"status":400}""".r)
      }
    }

    "Reject bad JSON format with \"error.path.missing\" as BAD_REQUEST" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val request: Request[JsValue] =
          FakeRequest(routes.MapController.mapRightsInvite(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.path\.missing.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("emailAddress" -> testEmailAddress)

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.mapRightsInvite(invalidOrgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("emailAddress" -> testEmailAddress)

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.mapRightsInvite(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj("emailAddress" -> testEmailAddress)

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.mapRightsInvite(orgID, programmingMapUUID, programmingMapRightsUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // GET `/org/:orgID/mymaps`

  "The GET `/org/:orgID/mymaps` action" should {
    "Return the map we created and match the returned ID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.MapController.getMyMaps(orgID))
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(OK)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*"id":"%s".*""".format(programmingMapUUID)))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.MapController.getMyMaps(invalidOrgID))
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, fakeUserSubjectID)).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.MapController.getMyMaps(orgID))
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, fakeUser2SubjectID)).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(app,
                                           FakeRequest(routes.MapController.getMyMaps(orgID))
                                             .withHeaders(CONTENT_TYPE -> JSON)
                                             .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // GET /org/:orgID/map/:mapID

  "The GET `/org/:orgID/map/:mapID` action" should {
    "Return the map we created and match the returned ID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.mapToJson(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)

        val resultString: String = contentAsString(result)

        val pattern: Regex =
          """.*?"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        rootUUID = NodeID(UUID.fromString(newUUID))

        contentAsString(result) must beMatching(Pattern.compile("""(?s).*"path":"%s".*""".format(programmingMapUUID)))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.mapToJson(invalidOrgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.mapToJson(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Local) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        // revoke right
        Await.result(mindMapRepoService.getMapRightsAndUpdate(programmingMapUUID,
                                                              programmingMapRightsUUID,
                                                              mapRights => mapRights.copy(view = false)),
                     5 seconds)

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.mapToJson(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))

        // grant right
        Await.result(mindMapRepoService.getMapRightsAndUpdate(programmingMapUUID,
                                                              programmingMapRightsUUID,
                                                              mapRights => mapRights.copy(view = true)),
                     5 seconds)
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.mapToJson(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST /org/:orgID/map/:mapID/node

  "The POST `/org/:orgID/map/:mapID/node` action" should {
    "Return OK for creating category \"Programming\"" in new MapControllerSpecContext {
      new WithApplication(application) {
        val request: Request[JsValue] =
          FakeRequest(routes.MapController.createMapNode(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withBody(newNodeJsonData(rootUUID, "category", "Programming", programmingCategoryNodeNumber))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        val pattern: Regex =
          """\{"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        programmingCategoryUUID = NodeID(UUID.fromString(newUUID))
      }
    }

    "Return OK for creating category \"Programming -> Scala\"" in new MapControllerSpecContext {
      new WithApplication(application) {
        val request: Request[JsValue] =
          FakeRequest(routes.MapController.createMapNode(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withBody(newNodeJsonData(programmingCategoryUUID, "category", "Scala", scalaCategoryNodeNumber))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)

        val pattern: Regex =
          """\{"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        scalaCategoryUUID = NodeID(UUID.fromString(newUUID))
      }
    }

    "Return OK for creating flashcard \"Programming -> Scala -> Scalatest\"" in new MapControllerSpecContext {
      new WithApplication(application) {
        val request: Request[JsValue] =
          FakeRequest(routes.MapController.createMapNode(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withBody(newNodeJsonData(scalaCategoryUUID, "flashcard", "Scalatest", scalaTestFlashcardNodeNumber))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)

        val pattern: Regex =
          """\{"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        scalatestFlashcardUUID = NodeID(UUID.fromString(newUUID))
      }
    }

    "Return OK for creating category \"Programming -> Python\"" in new MapControllerSpecContext {
      new WithApplication(application) {
        val request: Request[JsValue] =
          FakeRequest(routes.MapController.createMapNode(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withBody(newNodeJsonData(programmingCategoryUUID, "category", "Python", pythonCategoryNodeNumber))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)

        val pattern: Regex =
          """\{"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        pythonCategoryUUID = NodeID(UUID.fromString(newUUID))
      }
    }

    "Return OK for creating flashcard \"Programming -> Python -> Fibonacci\"" in new MapControllerSpecContext {
      new WithApplication(application) {
        val request: Request[JsValue] =
          FakeRequest(routes.MapController.createMapNode(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withBody(newNodeJsonData(pythonCategoryUUID, "flashcard", "Fibonacci", fibonacciFlashcardNodeNumber))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)

        val pattern: Regex =
          """\{"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        fibonacciFlashcardUUID = NodeID(UUID.fromString(newUUID))
      }
    }

    "Reject bad JSON format with \"error.path.missing\" as BAD_REQUEST" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj( // missing "name"
                                         "nodeNumber" -> 22,
                                         "parentID"   -> rootUUID,
                                         "nodeType"   -> "flashcard",
                                         "order"      -> 1)

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.createMapNode(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.path\.missing.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.createMapNode(invalidOrgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withBody(newNodeJsonData(rootUUID, "flashcard", "Scalatest", scalaTestFlashcardNodeNumber))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.createMapNode(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withBody(newNodeJsonData(rootUUID, "flashcard", "Scalatest", scalaTestFlashcardNodeNumber))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Local) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        // revoke right
        Await.result(mindMapRepoService.getMapRightsAndUpdate(programmingMapUUID,
                                                              programmingMapRightsUUID,
                                                              mapRights => mapRights.copy(modify = false)),
                     5 seconds)

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.createMapNode(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withBody(newNodeJsonData(rootUUID, "flashcard", "Scalatest", scalaTestFlashcardNodeNumber))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))

        // grant right
        Await.result(mindMapRepoService.getMapRightsAndUpdate(programmingMapUUID,
                                                              programmingMapRightsUUID,
                                                              mapRights => mapRights.copy(modify = true)),
                     5 seconds)
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.createMapNode(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withBody(newNodeJsonData(rootUUID, "flashcard", "Scalatest", scalaTestFlashcardNodeNumber))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // PATCH /org/:orgID/map/:mapID/node/attributes

  "The PATCH `/org/:orgID/map/:mapID/node/attributes` action" should {
    "Return OK for assign collapsed to true" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "collapsed" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(
            routes.MapController.patchMapNodeAttributes(orgID,
                                                        programmingMapUUID,
                                                        SegmentID(programmingMapUUID),
                                                        programmingCategoryUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Show \"collapsed\": true in node attributes from map json data" in new MapControllerSpecContext {
      new WithApplication(application) {
        // check setting in map json data
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.mapToJson(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)

        val resultString: String = contentAsString(result)

        resultString must beMatching(""".*?"attr":\{"collapsed":true,.*""".format(programmingCategoryUUID).r)
      }
    }

    "Return OK for assign collapsed to false" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "collapsed" -> false
        )

        val request: Request[JsValue] =
          FakeRequest(
            routes.MapController.patchMapNodeAttributes(orgID,
                                                        programmingMapUUID,
                                                        SegmentID(programmingMapUUID),
                                                        programmingCategoryUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Show \"collapsed\": false in node attributes from map json data" in new MapControllerSpecContext {
      new WithApplication(application) {
        // check setting in map json data
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.mapToJson(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)

        val resultString: String = contentAsString(result)

        resultString must beMatching(""".*?"attr":\{"collapsed":false,.*""".format(programmingCategoryUUID).r)
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.patchMapNodeAttributes(invalidOrgID,
                                                        programmingMapUUID,
                                                        SegmentID(programmingMapUUID),
                                                        programmingCategoryUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.patchMapNodeAttributes(orgID,
                                                        programmingMapUUID,
                                                        SegmentID(programmingMapUUID),
                                                        programmingCategoryUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.patchMapNodeAttributes(orgID,
                                                        programmingMapUUID,
                                                        SegmentID(programmingMapUUID),
                                                        programmingCategoryUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // PATCH /org/:orgID/map/:mapID/node

  "The PATCH `/org/:orgID/map/:mapID/node` action" should {
    "Return OK and return Node for a valid request #1" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "nodeNumber" -> 33,
          "parentID"   -> pythonCategoryUUID,
          "order"      -> 11,
          "name"       -> "FlashCard NewName",
          "nodeType"   -> "category",
          "disabled"   -> true,
          "returnNode" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(
            routes.MapController.patchMapNode(orgID,
                                              programmingMapUUID,
                                              SegmentID(programmingMapUUID),
                                              scalatestFlashcardUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        resultString must beMatching(
          """.*?"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*""".r)
        resultString must beMatching(""".*?"parentID":"%s".*""".format(pythonCategoryUUID).r)
        resultString must beMatching(""".*?"nodeNumber":33.*""".r)
        resultString must beMatching(""".*?"order":11.*""".r)
        resultString must beMatching(""".*?"name":"FlashCard NewName".*""".r)
        resultString must beMatching(""".*?"type":"category".*""".r)
        resultString must beMatching(""".*?"disabled":true.*""".r)
      }
    }

    "Return OK and return Node for a valid request #2" in new MapControllerSpecContext {
      new WithApplication(application) {

        val randomUUID: UUID = UUID.randomUUID()
        val jsonData: JsValue = Json.obj(
          "nodeNumber" -> scalaTestFlashcardNodeNumber,
          "parentID"   -> scalaCategoryUUID,
          "order"      -> 9,
          "name"       -> "Scala test",
          "nodeType"   -> "flashcard",
          "disabled"   -> false,
          "returnNode" -> true
        )

        val request: Request[JsValue] =
          FakeRequest(
            routes.MapController.patchMapNode(orgID,
                                              programmingMapUUID,
                                              SegmentID(programmingMapUUID),
                                              scalatestFlashcardUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)

        resultString must beMatching(
          """.*?"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*""".r)
        resultString must beMatching(""".*?"parentID":"%s".*""".format(scalaCategoryUUID).r)
        resultString must beMatching(""".*?"nodeNumber":%s.*""".format(scalaTestFlashcardNodeNumber).r)
        resultString must beMatching(""".*?"order":9.*""".r)
        resultString must beMatching(""".*?"name":"Scala test".*""".r)
        resultString must beMatching(""".*?"type":"flashcard".*""".r)
        resultString must_!= beMatching(""".*?"disabled":false""".r)
      }
    }

    "Return OK and NO return Node for a valid request #3" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj()

        val request: Request[JsValue] =
          FakeRequest(
            routes.MapController.patchMapNode(orgID,
                                              programmingMapUUID,
                                              SegmentID(programmingMapUUID),
                                              scalatestFlashcardUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        status(result) must beEqualTo(OK)
      }
    }

    "Reject bad UUID format with \"error.expected.uuid\" as BAD_REQUEST" in new MapControllerSpecContext {
      new WithApplication(application) {

        val jsonData: JsValue = Json.obj(
          "parentID" -> "BAD UUID"
        )

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.patchMapNode(orgID,
                                              programmingMapUUID,
                                              SegmentID(programmingMapUUID),
                                              scalatestFlashcardUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.expected\.uuid.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.patchMapNode(invalidOrgID,
                                              programmingMapUUID,
                                              SegmentID(programmingMapUUID),
                                              scalatestFlashcardUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.patchMapNode(orgID,
                                              programmingMapUUID,
                                              SegmentID(programmingMapUUID),
                                              scalatestFlashcardUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.patchMapNode(orgID,
                                              programmingMapUUID,
                                              SegmentID(programmingMapUUID),
                                              scalatestFlashcardUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST /org/:orgID/map/:mapID/segment/:segmentID/node/:nodeID/card

  "The POST `/org/:orgID/map/:mapID/segment/:segmentID/node/:nodeID/card` action" should {
    "Create a new basic flashcard" in new MapControllerSpecContext {
      new WithApplication(application) {
        val request: Request[JsValue] =
          FakeRequest(
            routes.MapController
              .createCard(orgID, programmingMapUUID, SegmentID(programmingMapUUID), fibonacciFlashcardUUID, "basic"))
            .withBody(newBasicCardJsonData())
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Create a new code_exercise flashcard" in new MapControllerSpecContext {
      new WithApplication(application) {
        val request: Request[JsValue] =
          FakeRequest(
            routes.MapController
              .createCard(orgID,
                          programmingMapUUID,
                          SegmentID(programmingMapUUID),
                          scalatestFlashcardUUID,
                          "code_exercise"))
            .withBody(newCodeExerciseCardJsonData())
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Reject bad JSON format with \"error.path.missing\" as BAD_REQUEST" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController
            .createCard(orgID, programmingMapUUID, SegmentID(programmingMapUUID), fibonacciFlashcardUUID, "basic"))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.path\.missing.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.createCard(invalidOrgID,
                                            programmingMapUUID,
                                            SegmentID(programmingMapUUID),
                                            fibonacciFlashcardUUID,
                                            "basic"))
            .withBody(newBasicCardJsonData())
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController
            .createCard(orgID, programmingMapUUID, SegmentID(programmingMapUUID), fibonacciFlashcardUUID, "basic"))
            .withBody(newBasicCardJsonData())
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController
            .createCard(orgID, programmingMapUUID, SegmentID(programmingMapUUID), fibonacciFlashcardUUID, "basic"))
            .withBody(newBasicCardJsonData())
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // PUT /org/:orgID/map/:mapID/segment/:segmentID/node/:nodeID/card

  "The PUT `/org/:orgID/map/:mapID/segment/:segmentID/node/:nodeID/card` action" should {
    "Update a basic flashcard" in new MapControllerSpecContext {
      new WithApplication(application) {
        val request: Request[JsValue] =
          FakeRequest(
            routes.MapController
              .updateCard(orgID, programmingMapUUID, SegmentID(programmingMapUUID), fibonacciFlashcardUUID, "basic"))
            .withBody(newBasicCardJsonData("What is your favorite motorcycle?"))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Have resulted in a the question being updated" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.getCardAsJSON(orgID,
                                               programmingMapUUID,
                                               SegmentID(programmingMapUUID),
                                               fibonacciFlashcardUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)

        val resultString: String = contentAsString(result)
        resultString must beMatching(""".*?"What is your favorite motorcycle\?".*""".r)
      }
    }

    "Update a code_exercise flashcard" in new MapControllerSpecContext {
      new WithApplication(application) {
        val request: Request[JsValue] =
          FakeRequest(
            routes.MapController.updateCard(orgID,
                                            programmingMapUUID,
                                            SegmentID(programmingMapUUID),
                                            scalatestFlashcardUUID,
                                            "code_exercise"))
            .withBody(newCodeExerciseCardJsonData("explanation text updated"))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Have resulted in a the solution being updated" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.getCardAsJSON(orgID,
                                               programmingMapUUID,
                                               SegmentID(programmingMapUUID),
                                               scalatestFlashcardUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)

        val resultString: String = contentAsString(result)
        resultString must beMatching(""".*?"explanation text updated".*""".r)
      }
    }

    "Reject bad JSON format with \"error.path.missing\" as BAD_REQUEST" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController
            .updateCard(orgID, programmingMapUUID, SegmentID(programmingMapUUID), fibonacciFlashcardUUID, "basic"))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.path\.missing.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.updateCard(invalidOrgID,
                                            programmingMapUUID,
                                            SegmentID(programmingMapUUID),
                                            fibonacciFlashcardUUID,
                                            "basic"))
            .withBody(newNodeJsonData(rootUUID, "flashcard", "Scalatest", scalaTestFlashcardNodeNumber))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController
            .updateCard(orgID, programmingMapUUID, SegmentID(programmingMapUUID), fibonacciFlashcardUUID, "basic"))
            .withBody(newBasicCardJsonData())
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController
            .updateCard(orgID, programmingMapUUID, SegmentID(programmingMapUUID), fibonacciFlashcardUUID, "basic"))
            .withBody(newNodeJsonData(rootUUID, "flashcard", "Scalatest", scalaTestFlashcardNodeNumber))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // GET /org/:orgID/map/:mapID/segment/:segmentID/node/:nodeID/card/:cardID

  "The GET `/org/:orgID/map/:mapID/segment/:segmentID/node/:nodeID/card/:cardID` action" should {
    "Return the flashcard we created and match the returned ID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.getCardAsJSON(orgID,
                                               programmingMapUUID,
                                               SegmentID(programmingMapUUID),
                                               fibonacciFlashcardUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)

        val resultString: String = contentAsString(result)

        resultString must beMatching(""".*?"cardType":.*"basic".*""".r)
        resultString must beMatching(""".*?"question":.*""".r)
        resultString must beMatching(""".*?"choices":\[.*""".r)

        val pattern1: Regex =
          """.*"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*?"Answer 1".*""".r
        resultString must beMatching(pattern1)
        val pattern1(newUUID1) = resultString
        fibonacciFlashcardAnswerUUID1 = UUID.fromString(newUUID1)

        val pattern2: Regex =
          """.*"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*?"Answer 2".*""".r
        resultString must beMatching(pattern2)
        val pattern2(newUUID2) = resultString
        fibonacciFlashcardAnswerUUID2 = UUID.fromString(newUUID2)

        val pattern3: Regex =
          """.*"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*?"Answer 3".*""".r
        resultString must beMatching(pattern3)
        val pattern3(newUUID3) = resultString
        fibonacciFlashcardAnswerUUID3 = UUID.fromString(newUUID3)
      }
    }

    "Return \"node not found\" for an invalid UUID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController
              .getCardAsJSON(orgID, programmingMapUUID, SegmentID(programmingMapUUID), NodeID.random)) // wrong ID
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(NOT_FOUND)
        contentAsString(result) must beMatching(""".*?"message":"NODE NOT FOUND".*""".r)
      }
    }

    "Return NOT_FOUND for an node with no flashcard" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController
              .getCardAsJSON(orgID, programmingMapUUID, SegmentID(programmingMapUUID), pythonCategoryUUID)) // wrong ID
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(NOT_FOUND)
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.getCardAsJSON(invalidOrgID,
                                               programmingMapUUID,
                                               SegmentID(programmingMapUUID),
                                               fibonacciFlashcardUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.getCardAsJSON(orgID,
                                               programmingMapUUID,
                                               SegmentID(programmingMapUUID),
                                               fibonacciFlashcardUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.getCardAsJSON(orgID,
                                               programmingMapUUID,
                                               SegmentID(programmingMapUUID),
                                               fibonacciFlashcardUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST & PUT /org/:orgID/map/:mapID/post

  "The POST & PUT `/org/:orgID/map/:mapID/post` action" should {
    "Return OK for a valid create request" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj(
          "markdown" -> "# Hi There!"
        )
        val request: Request[JsValue] =
          FakeRequest(
            routes.MapController.createOrUpdatePost(orgID,
                                                    programmingMapUUID,
                                                    SegmentID(programmingMapUUID),
                                                    programmingCategoryUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Return OK for a valid update request" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj(
          "markdown" -> "# Hi Bob!"
        )
        val request: Request[JsValue] =
          FakeRequest(
            routes.MapController.createOrUpdatePost(orgID,
                                                    programmingMapUUID,
                                                    SegmentID(programmingMapUUID),
                                                    programmingCategoryUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        status(result) must beEqualTo(OK)
      }
    }

    "Reject bad JSON format with \"error.path.missing\" as BAD_REQUEST" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj(
          )
        val request: Request[JsValue] =
          FakeRequest(
            routes.MapController.createOrUpdatePost(orgID,
                                                    programmingMapUUID,
                                                    SegmentID(programmingMapUUID),
                                                    programmingCategoryUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)

        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.path\.missing.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.createOrUpdatePost(invalidOrgID,
                                                    programmingMapUUID,
                                                    SegmentID(programmingMapUUID),
                                                    programmingCategoryUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.createOrUpdatePost(orgID,
                                                    programmingMapUUID,
                                                    SegmentID(programmingMapUUID),
                                                    programmingCategoryUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.createOrUpdatePost(orgID,
                                                    programmingMapUUID,
                                                    SegmentID(programmingMapUUID),
                                                    programmingCategoryUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // GET /org/:orgID/map/:mapID/post/:postID

  "The GET `/org/:orgID/map/:mapID/post/:postID` action" should {
    "Return OK for valid request" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.getPostAsJSON(orgID,
                                               programmingMapUUID,
                                               SegmentID(programmingMapUUID),
                                               NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)

        val resultString: String = contentAsString(result)

        val pattern: Regex = """.*?"markdown":"# Hi Bob!".*""".r
        resultString must beMatching(pattern)
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.getPostAsJSON(invalidOrgID,
                                               programmingMapUUID,
                                               SegmentID(programmingMapUUID),
                                               NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.getPostAsJSON(orgID,
                                               programmingMapUUID,
                                               SegmentID(programmingMapUUID),
                                               NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.getPostAsJSON(orgID,
                                               programmingMapUUID,
                                               SegmentID(programmingMapUUID),
                                               NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  //  *********** ********** ***********
  //  Training Controller Tests
  //  *********** ********** ***********

  // GET /org/:orgID/train/:mapID/segment/:segmentID/cards

  "The GET `/org/:orgID/train/:mapID/segment/:segmentID/cards` action" should {
    "Return the flashcard we created and match the returned ID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController.cardsToJSON(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)

        val resultString: String = contentAsString(result)

        resultString must beMatching(
          """.*?"parentID":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*""".r)
        resultString must beMatching(""".*?"ef":2\.5.*""".r)
        resultString must beMatching(""".*?"lastAnswer":0.*""".r)
        resultString must beMatching(""".*?"due":0.*""".r)
        resultString must beMatching(Pattern.compile("""(?s).*"nodeID":"%s".*""".format(fibonacciFlashcardUUID)))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.TrainController.cardsToJSON(invalidOrgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController.cardsToJSON(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController.cardsToJSON(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST /org/:orgID/train/:mapID/card/:nodeID/grade

  "The POST `/org/:orgID/train/:mapID/card/:nodeID/grade` action" should {
    "Grade our answers as correct = true for a basic cardType correct answer submission" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj(
          "choices" -> List(fibonacciFlashcardAnswerUUID1, fibonacciFlashcardAnswerUUID2),
          "seconds" -> 33
        )
        val request: Request[JsValue] =
          FakeRequest(
            routes.TrainController
              .gradeCard(orgID, programmingMapUUID, fibonacciFlashcardUUID, "basic"))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        val pattern: Regex = """.*?"correct":true.*""".r
        resultString must beMatching(pattern)
      }
    }

    "Grade our answer as correct = true for a code_exercise cardType correct answer submission" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj(
          "code"    -> "object Exercise {\n  def hello() = {\n    \"Hi\"\n  }\n}",
          "seconds" -> 33
        )
        val request: Request[JsValue] =
          FakeRequest(
            routes.TrainController
              .gradeCard(orgID, programmingMapUUID, scalatestFlashcardUUID, "code_exercise"))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        val pattern: Regex = """.*?"correct":true.*""".r
        resultString must beMatching(pattern)
      }
    }

    "Grade our answers as correct = false and show the correct answers for a basic cardType incorrect answer submission with a random UUID" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj(
          "choices" -> List(fibonacciFlashcardAnswerUUID1, UUID.randomUUID()),
          "seconds" -> 33
        )
        val request: Request[JsValue] =
          FakeRequest(
            routes.TrainController
              .gradeCard(orgID, programmingMapUUID, fibonacciFlashcardUUID, "basic"))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)

        val pattern: Regex = """.*?"correct":false.*""".r
        resultString must beMatching(pattern)
        resultString must beMatching(""".*?"answer":"Answer 1".*""".r)
        resultString must beMatching(""".*?"answer":"Answer 2".*""".r)
        resultString must_!= beMatching(""".*?"answer":"Answer 3".*""".r)
      }
    }

    "Grade our answers as correct = false and show the correct answers for a basic cardType incorrect answer submission" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj(
          "choices" -> List(fibonacciFlashcardAnswerUUID1,
                            fibonacciFlashcardAnswerUUID2,
                            fibonacciFlashcardAnswerUUID3),
          "seconds" -> 33
        )
        val request: Request[JsValue] =
          FakeRequest(
            routes.TrainController
              .gradeCard(orgID, programmingMapUUID, fibonacciFlashcardUUID, "basic"))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)

        val pattern: Regex = """.*?"correct":false.*""".r
        resultString must beMatching(pattern)
        resultString must beMatching(""".*?"answer":"Answer 1".*""".r)
        resultString must beMatching(""".*?"answer":"Answer 2".*""".r)
        resultString must_!= beMatching(""".*?"answer":"Answer 3".*""".r)
      }
    }

    "Grade our answer as correct = false for a code_exercise cardType correct answer submission" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj(
          "code"    -> "object Hello {\r\n  def run() = {\r\n    \"Goodbye\"\r\n  }\r\n}\r\n",
          "seconds" -> 33
        )
        val request: Request[JsValue] =
          FakeRequest(
            routes.TrainController
              .gradeCard(orgID, programmingMapUUID, scalatestFlashcardUUID, "code_exercise"))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)

        val pattern: Regex = """.*?"correct":false.*""".r
        resultString must beMatching(pattern)
      }
    }

    "Reject bad JSON format with \"error.path.missing\" as BAD_REQUEST" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController
            .gradeCard(orgID, programmingMapUUID, fibonacciFlashcardUUID, "basic"))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.path\.missing.*"""))
      }
    }

    "Reject bad JSON format with \"error.expected.uuid\" as BAD_REQUEST" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj(
          "choices" -> List("INVALID UUID")
        )
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController
            .gradeCard(orgID, programmingMapUUID, fibonacciFlashcardUUID, "basic"))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.expected\.uuid.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj(
          "choices" -> List(fibonacciFlashcardAnswerUUID1, fibonacciFlashcardAnswerUUID2),
          "seconds" -> 33
        )
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.TrainController.gradeCard(invalidOrgID, programmingMapUUID, fibonacciFlashcardUUID, "basic"))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj(
          "choices" -> List(fibonacciFlashcardAnswerUUID1, fibonacciFlashcardAnswerUUID2),
          "seconds" -> 33
        )
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController
            .gradeCard(orgID, programmingMapUUID, fibonacciFlashcardUUID, "basic"))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj(
          "choices" -> List(fibonacciFlashcardAnswerUUID1, fibonacciFlashcardAnswerUUID2),
          "seconds" -> 33
        )
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController
            .gradeCard(orgID, programmingMapUUID, fibonacciFlashcardUUID, "basic"))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST /org/:orgID/train/:mapID/card/:nodeID/quality

  "The POST `/org/:orgID/train/:mapID/card/:nodeID/quality` action" should {
    "Return OK for a valid request" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj("quality" -> 1)
        val request: Request[JsValue] =
          FakeRequest(routes.TrainController.submitCardQuality(orgID, programmingMapUUID, fibonacciFlashcardUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        val result: Future[Result] = route(app, request).get
        val resultString: String   = contentAsString(result)
        status(result) must beEqualTo(OK)
      }
    }

    "Have updated the easiness factor, lastAnswer, and due fields" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController.cardsToJSON(orgID, programmingMapUUID, SegmentID(programmingMapUUID)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)

        val resultString: String = contentAsString(result)

        resultString must beMatching(
          """.*?"parentID":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*""".r)
        resultString must beMatching(""".*?"ef":1\.\d+.*""".r)
        resultString must beMatching(""".*?"lastAnswer":\d\d+.*""".r)
        resultString must beMatching(""".*?"due":\d\d+.*""".r)
        resultString must beMatching(Pattern.compile("""(?s).*"nodeID":"%s".*""".format(fibonacciFlashcardUUID)))
      }
    }

    "Reject bad JSON format with \"error.path.missing\" as BAD_REQUEST" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj()
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController.submitCardQuality(orgID, programmingMapUUID, fibonacciFlashcardUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.path\.missing.*"""))
      }
    }

    "Reject bad JSON format with \"error.expected.jsnumber\" as BAD_REQUEST" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj("quality" -> "NOT AN INT")
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController.submitCardQuality(orgID, programmingMapUUID, fibonacciFlashcardUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*error\.expected\.jsnumber.*"""))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj("quality" -> 1)
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.TrainController.submitCardQuality(invalidOrgID, programmingMapUUID, fibonacciFlashcardUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj("quality" -> 1)
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController.submitCardQuality(orgID, programmingMapUUID, fibonacciFlashcardUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val jsonData: JsValue = Json.obj("quality" -> 1)
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController.submitCardQuality(orgID, programmingMapUUID, fibonacciFlashcardUUID))
            .withBody(jsonData)
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST /org/:orgID/train/:mapID/post/:nodeID/markRead

  "The POST `/org/:orgID/train/:mapID/post/:nodeID/markRead` action" should {
    "Return OK for a valid create request" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.TrainController.markPostRead(orgID, programmingMapUUID, NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        val resultString: String = contentAsString(result)

        status(result) must beEqualTo(OK)
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.TrainController.markPostRead(invalidOrgID, programmingMapUUID, NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.TrainController.markPostRead(orgID, programmingMapUUID, NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.TrainController.markPostRead(orgID, programmingMapUUID, NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // GET /org/:orgID/train/:mapID/postsReadTimes

  "The GET `/org/:orgID/train/:mapID/postsReadTimes` action" should {
    "Return OK for valid request" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] =
          route(
            app,
            FakeRequest(routes.TrainController.getPostsReadTimes(orgID, programmingMapUUID))
              .withHeaders(CONTENT_TYPE -> JSON)
              .addAttr(fakeUser, fakeUserSubjectID)
          ).get
        status(result) must beEqualTo(OK)

        val resultString: String = contentAsString(result)

        val pattern: Regex = """.*?"timeRead":\d+.*""".r
        resultString must beMatching(pattern)
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController.getPostsReadTimes(invalidOrgID, programmingMapUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController.getPostsReadTimes(orgID, programmingMapUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        val result: Future[Result] = route(
          app,
          FakeRequest(routes.TrainController.getPostsReadTimes(orgID, programmingMapUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  //  *********** ********** ***********
  // State destructive tests now that all dependant tests are completed
  // *********** *********** ***********
  // DELETE /org/:orgID/map/:mapID/post/:postID

  "The DELETE `/org/:orgID/map/:mapID/post/:postID` action" should {
    "Return OK for a valid request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.deletePost(orgID,
                                            programmingMapUUID,
                                            SegmentID(programmingMapUUID),
                                            NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)
      }
    }

    "Respond with BAD_REQUEST for request to delete non-existent postID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.deletePost(orgID,
                                            programmingMapUUID,
                                            SegmentID(programmingMapUUID),
                                            NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(BAD_REQUEST)
        contentAsString(result) must beMatching(""".*?Nothing Updated.*""".r)
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.deletePost(invalidOrgID,
                                            programmingMapUUID,
                                            SegmentID(programmingMapUUID),
                                            NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.deletePost(orgID,
                                            programmingMapUUID,
                                            SegmentID(programmingMapUUID),
                                            NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Local) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {
        // revoke right
        Await.result(mindMapRepoService.getMapRightsAndUpdate(programmingMapUUID,
                                                              programmingMapRightsUUID,
                                                              mapRights => mapRights.copy(modify = false)),
                     5 seconds)

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.deletePost(orgID,
                                            programmingMapUUID,
                                            SegmentID(programmingMapUUID),
                                            NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))

        // grant right
        Await.result(mindMapRepoService.getMapRightsAndUpdate(programmingMapUUID,
                                                              programmingMapRightsUUID,
                                                              mapRights => mapRights.copy(modify = true)),
                     5 seconds)
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.deletePost(orgID,
                                            programmingMapUUID,
                                            SegmentID(programmingMapUUID),
                                            NodeID(programmingCategoryUUID.uuid)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // DELETE /org/:orgID/map/:mapID/node/:nodeID

  "The DELETE `/org/:orgID/map/:mapID/node/:nodeID` action" should {
    "Return OK for a valid request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.deleteNode(orgID,
                                            programmingMapUUID,
                                            SegmentID(programmingMapUUID),
                                            scalaCategoryUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)

      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.deleteNode(invalidOrgID,
                                            programmingMapUUID,
                                            SegmentID(programmingMapUUID),
                                            scalaCategoryUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.deleteNode(orgID,
                                            programmingMapUUID,
                                            SegmentID(programmingMapUUID),
                                            scalaCategoryUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.deleteNode(orgID,
                                            programmingMapUUID,
                                            SegmentID(programmingMapUUID),
                                            scalaCategoryUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }

  // POST /org/:orgID/map/:mapID/node/:nodeID/makeSubMap
  // *note; this test is last because the mapIDs will change after convertNodeToSubMap

  "The POST `/org/:orgID/map/:mapID/node/:nodeID/makeSubMap` action" should {
    "Return OK for a valid request" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.convertNodeToSubMap(orgID,
                                                     programmingMapUUID,
                                                     SegmentID(programmingMapUUID),
                                                     programmingCategoryUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)
        val resultString: String = contentAsString(result)

        val pattern: Regex =
          """\{"subMapID":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"}""".r
        resultString must beMatching(pattern)
        val pattern(newUUID) = resultString
        programmingSubMapUUID = MapID(UUID.fromString(newUUID))
      }
    }

    "Return the new sub map we created and match the returned ID" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(routes.MapController.mapToJson(orgID, programmingMapUUID, SegmentID(programmingSubMapUUID)))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(OK)

        val resultString: String = contentAsString(result)

        val pattern: Regex =
          """.*?"id":"([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})".*""".r
        resultString must beMatching(pattern)

        contentAsString(result) must beMatching(
          Pattern.compile(
            """(?s).*"path":"%s".*""".format(programmingMapUUID.toString + ":" + programmingSubMapUUID.toString)))
      }
    }

    "Reject invalid orgID with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.convertNodeToSubMap(invalidOrgID,
                                                     programmingMapUUID,
                                                     SegmentID(programmingMapUUID),
                                                     programmingCategoryUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUserSubjectID)
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject (Global) permission check with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.convertNodeToSubMap(orgID,
                                                     programmingMapUUID,
                                                     SegmentID(programmingMapUUID),
                                                     programmingCategoryUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, fakeUser2SubjectID)
        ).get // User With No Permissions
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }

    "Reject invalid token with \"ACCESS DENIED\" as UNAUTHORIZED" in new MapControllerSpecContext {
      new WithApplication(application) {

        val result: Future[Result] = route(
          app,
          FakeRequest(
            routes.MapController.convertNodeToSubMap(orgID,
                                                     programmingMapUUID,
                                                     SegmentID(programmingMapUUID),
                                                     programmingCategoryUUID))
            .withHeaders(CONTENT_TYPE -> JSON)
            .addAttr(fakeUser, "--INVALID TOKEN SUBJECT ID--")
        ).get
        status(result) must beEqualTo(UNAUTHORIZED)
        contentAsString(result) must beMatching(Pattern.compile("""(?s).*ACCESS DENIED.*"""))
      }
    }
  }
}
