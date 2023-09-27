package controllers

import java.util.UUID

import io.masterypath.slick.OrgID
import org.specs2.specification.Scope
import play.api.inject.guice.GuiceApplicationBuilder
import services.OrganizationRepoService

import scala.concurrent.ExecutionContext

trait OrganizationControllerSpecContext extends Scope {

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

  implicit val ec  = ExecutionContext.global
  lazy val orgRepo = application.injector.instanceOf[OrganizationRepoService]
}
