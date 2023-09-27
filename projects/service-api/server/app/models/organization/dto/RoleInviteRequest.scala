package models.organization.dto

import io.masterypath.slick.MapRightsID
import play.api.libs.json._

case class RoleInviteRequest(
    emailAddress: String,
    mapRightsID: Option[MapRightsID]
)
object RoleInviteRequest {
  implicit val f: OFormat[RoleInviteRequest] = Json.format[RoleInviteRequest]
}
