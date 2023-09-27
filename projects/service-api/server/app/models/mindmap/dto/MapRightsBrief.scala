package models.mindmap.dto

import play.api.libs.json.{Json, OFormat}

case class MapRightsBrief(
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
    view: Boolean,
)

object MapRightsBrief {
  implicit val MapRightsBriefFormat: OFormat[MapRightsBrief] = Json.format[MapRightsBrief]
}
