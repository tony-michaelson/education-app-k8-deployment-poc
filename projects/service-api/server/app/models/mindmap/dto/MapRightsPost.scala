package models.mindmap.dto

import play.api.libs.json.{Json, OFormat}

case class MapRightsPost(
    name: String,
    admin: Boolean,
    feedback: Boolean,
    mnemonics: Boolean,
    modify: Boolean,
    permissions: Boolean,
    publish: Boolean,
    share: Boolean,
    stats: Boolean,
    training: Boolean,
    transfer: Boolean,
    view: Boolean
)
object MapRightsPost {
  implicit val mapRightsPatchFormat: OFormat[MapRightsPost] = Json.format[MapRightsPost]
}
