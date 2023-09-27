package models.mindmap.dto

import play.api.libs.json.{Json, OFormat}

case class NodeAttrStyle(background: String)
object NodeAttrStyle {
  implicit val NodeAttrStyleFormat: OFormat[NodeAttrStyle] = Json.format[NodeAttrStyle]
}
