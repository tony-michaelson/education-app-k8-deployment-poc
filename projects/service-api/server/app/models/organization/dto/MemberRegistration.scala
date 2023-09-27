package models.organization.dto

import play.api.libs.json.{Json, OFormat}

case class MemberRegistration(
    firstName: String,
    lastName: String,
    idToken: String
)
object MemberRegistration {
  implicit val memberRegistrationJsonFormat: OFormat[MemberRegistration] = Json.format[MemberRegistration]
}
