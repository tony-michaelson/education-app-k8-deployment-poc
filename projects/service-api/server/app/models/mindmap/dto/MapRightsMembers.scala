package models.mindmap.dto

import io.masterypath.slick.{MapRights, MemberProfile}
import play.api.libs.json.{Json, OFormat}

case class MapRightsMembers(
    rights: MapRights,
    members: Seq[MemberProfile]
)
object MapRightsMembers {
  implicit val mapRightsMembersFormat: OFormat[MapRightsMembers] = Json.format[MapRightsMembers]
}
