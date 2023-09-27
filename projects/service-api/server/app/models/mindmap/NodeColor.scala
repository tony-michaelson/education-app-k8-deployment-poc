package models.mindmap

import io.masterypath.slick.{CardDue, Node}

abstract class NodeColor {
  val cardDue: Option[CardDue]
  val node: Node

  def nodeColor: String = {
    def color_map = node.`type` match {
      case "mindmap"   => "#4286f4"
      case "category"  => "#e0e0e0"
      case "flashcard" => "#00b8ff"
      case "custom"    => "#b168ff"
    }
    cardDue match {
      case Some(due) if node.`type` == "flashcard" =>
        due.interval match {
          case x if x <= 3           => "#ff0000" // red
          case x if x >= 3 && x <= 6 => "#e6e600" // yellow
          case x if x >= 7           => "#00db74" // green
        }
      case _ => color_map
    }
  }
}
