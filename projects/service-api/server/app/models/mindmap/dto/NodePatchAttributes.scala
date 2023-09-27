package models.mindmap.dto

import play.api.libs.json.{Json, OFormat}

case class NodePatchAttributes(
    collapsed: Option[Boolean]
)
object NodePatchAttributes {
  implicit val nodeAttributesFormat: OFormat[NodePatchAttributes] = Json.format[NodePatchAttributes]
}
