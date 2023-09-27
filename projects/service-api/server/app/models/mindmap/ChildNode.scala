package models.mindmap

import io.masterypath.slick.{CardDue, Node, NodeAttributes}

case class ChildNode(
    parent_node_id: Short,
    node: Node,
    cardDue: Option[CardDue],
    postExists: Boolean,
    memberAttributes: Option[NodeAttributes]
) extends NodeColor
