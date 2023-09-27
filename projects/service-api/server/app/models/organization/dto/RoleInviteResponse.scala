package models.organization.dto

import io.masterypath.slick.RoleInviteID
import play.api.libs.json._

case class RoleInviteResponse(
    status: Int,
    inviteID: Option[RoleInviteID]
)
object RoleInviteResponse {
  implicit val f: OFormat[RoleInviteResponse] = Json.format[RoleInviteResponse]
}
