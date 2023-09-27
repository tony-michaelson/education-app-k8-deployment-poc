package models.organization.dto

import io.masterypath.slick.MemberProfile
import models.baseDTO
import models.organization.Member
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.Result
import play.api.mvc.Results._

import scala.concurrent.Future

case class MemberProfileEmail(
    profile: MemberProfile,
    email: String,
) extends baseDTO {
  def result: Result               = Ok(Json.toJson(this))
  def futureResult: Future[Result] = Future.successful(result)
}
object MemberProfileEmail {
  implicit val MemberJsonFormat: OFormat[MemberProfileEmail] = Json.format[MemberProfileEmail]
  def apply(member: Member): MemberProfileEmail = {
    MemberProfileEmail(profile = member.profile, email = member.account.email)
  }
}
