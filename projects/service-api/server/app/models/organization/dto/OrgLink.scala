package models.organization.dto

import io.masterypath.slick.OrgID
import play.api.libs.json._

case class OrgLink(
    name: String,
    orgID: OrgID,
    internal: Boolean,
    payPerCourse: Boolean,
)
object OrgLink {
  implicit val f: OFormat[OrgLink] = Json.format[OrgLink]
}
