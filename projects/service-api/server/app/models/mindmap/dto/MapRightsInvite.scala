package models.mindmap.dto

import play.api.libs.json._

case class MapRightsInvite(
    emailAddress: String
)
object MapRightsInvite {
  implicit val f: OFormat[MapRightsInvite] = Json.format[MapRightsInvite]
}
