package models.mindmap.dto

import io.masterypath.slick.NodeID
import play.api.libs.json.{Json, OFormat}

case class NodePatch(
    nodeNumber: Option[Short],
    parentID: Option[NodeID],
    order: Option[Double],
    name: Option[String],
    nodeType: Option[String],
    disabled: Option[Boolean],
    returnNode: Option[Boolean]
)
object NodePatch { implicit val nodeFormat: OFormat[NodePatch] = Json.format[NodePatch] }
