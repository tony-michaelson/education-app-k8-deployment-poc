package models.mindmap.dto

import play.api.libs.json.{Json, OFormat}

case class MapRightsPatch(
    admin: Option[Boolean],
    feedback: Option[Boolean],
    mnemonics: Option[Boolean],
    modify: Option[Boolean],
    permissions: Option[Boolean],
    publish: Option[Boolean],
    share: Option[Boolean],
    stats: Option[Boolean],
    training: Option[Boolean],
    transfer: Option[Boolean],
    view: Option[Boolean],
    returnRights: Option[Boolean]
)
object MapRightsPatch {
  implicit val mapRightsPatchFormat: OFormat[MapRightsPatch] = Json.format[MapRightsPatch]
}
