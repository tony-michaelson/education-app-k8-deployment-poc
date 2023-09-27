package controllers

import java.util.UUID

import io.masterypath.slick.OrgID
import models.flashcard.dto.CardAnswer
import org.specs2.specification.Scope
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import services.MindMapRepoService

import scala.concurrent.ExecutionContext

trait MapControllerSpecContext extends Scope {

  import com.google.inject.AbstractModule
  import models.organization.Attrs
  import net.codingwell.scalaguice.ScalaModule

  val fakeUser           = Attrs.FakeUser
  val fakeUserSubjectID  = "auth0|5e9d101f68010d0c5ab9668f"
  val fakeUser2SubjectID = "auth0|5ec8b0ee6784c00caaa90891"
  val fakeUser3SubjectID = "bob"
  val testEmailAddress   = "tonymichaelson@protonmail.com"

  val orgID        = OrgID(UUID.fromString("57d00d57-28de-410c-b1cf-d97c4ccba08d"))
  val invalidOrgID = OrgID(UUID.fromString("11111111-28de-410c-b1cf-d97c4ccba08d"))

  def newNodeJsonData(rootID: UUID, nodeType: String, name: String, nodeID: Int): JsValue = Json.obj(
    "name"       -> name,
    "nodeNumber" -> nodeID,
    "parentID"   -> rootID,
    "nodeType"   -> nodeType,
    "order"      -> nodeID
  )

  def newBasicCardJsonData(question: String = "What is your favorite color?") = Json.obj(
    "flashcardTypeID" -> "8e5918f8-89bf-4371-9999-3856d63700ac",
    "question"        -> question,
    "markdown"        -> "",
    "answers" -> List(new CardAnswer(
                        correct = true,
                        answer = "Answer 1"
                      ),
                      new CardAnswer(
                        correct = true,
                        answer = "Answer 2"
                      ),
                      new CardAnswer(
                        correct = false,
                        answer = "Answer 3"
                      ))
  )

  def newCodeExerciseCardJsonData(explanation: String = "explanation text") = Json.obj(
    "flashcardTypeID" -> "8e5918f8-89bf-4371-9999-3856d63700ab",
    "solution"        -> "solution text",
    "test"            -> "import org.scalatest.flatspec.AnyFlatSpec\n\nclass Tests extends AnyFlatSpec {\n  it should \"return the string 'Hi'\" in {\n    assert(Exercise.hello() == \"Hi\")\n  }\n}",
    "explanation"     -> explanation,
    "explanationHtml" -> "explanation html",
    "template"        -> "template"
  )

  /**
    * A fake Guice module.
    */
  class FakeModule extends AbstractModule with ScalaModule

  /**
    * The application.
    */
  lazy val application = new GuiceApplicationBuilder()
    .overrides(new FakeModule)
    .build()

  implicit val ec             = ExecutionContext.global
  lazy val mindMapRepoService = application.injector.instanceOf[MindMapRepoService]
}
