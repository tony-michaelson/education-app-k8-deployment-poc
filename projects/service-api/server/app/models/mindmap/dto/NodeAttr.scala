package models.mindmap.dto

import play.api.libs.json.{Json, OFormat}

// TODO - ID classes needed here
case class NodeAttr(
    collapsed: Boolean,
    postExists: Boolean,
    id: String,
    parentID: Option[String],
    mapID: String,
    path: String,
    nodeType: String,
    style: NodeAttrStyle
)
object NodeAttr { implicit val nodeAttrFormat: OFormat[NodeAttr] = Json.format[NodeAttr] }
