package models.mindmap.dto

import io.masterypath.slick.{Card, Node, Post}
import play.api.libs.json.{Json, OFormat}

case class NodeCardPostInfo(
    node: Node,
    card: Option[Card],
    post: Option[Post],
)

object NodeCardPostInfo {
  implicit val nodeCardPostInfoFormat: OFormat[NodeCardPostInfo] = Json.format[NodeCardPostInfo]
}
