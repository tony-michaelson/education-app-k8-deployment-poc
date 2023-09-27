package models.mindmap.dto

import io.masterypath.slick.NodeID
import play.api.libs.json.{Json, OFormat}

case class NodePost(
    nodeNumber: Short,
    parentID: NodeID,
    order: Double,
    name: String,
    nodeType: String
)

object NodePost { implicit val nodeFormat: OFormat[NodePost] = Json.format[NodePost] }
