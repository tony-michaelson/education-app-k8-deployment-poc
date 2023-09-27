package models.flashcard

import io.masterypath.slick.{Card, CardDue, FlashcardType, Node}
import models.mindmap.NodeColor

case class ChildCard(
    node: Node,
    card: Option[Card],
    cardDue: Option[CardDue],
    flashcardType: Option[FlashcardType],
) extends NodeColor
