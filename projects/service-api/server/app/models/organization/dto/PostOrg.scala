package models.organization.dto

import play.api.libs.json._

case class PostOrg(
    name: String,
    domain: String,
    familyPlan: Boolean,
)
object PostOrg {
  implicit val f: OFormat[PostOrg] = Json.format[PostOrg]
}
