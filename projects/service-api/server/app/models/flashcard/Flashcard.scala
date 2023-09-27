package models.flashcard

import io.masterypath.slick.{Card, FlashcardType, Node}
import play.api.libs.json.{Json, OFormat}

case class Flashcard(
    card: Card,
    node: Node,
    cardType: FlashcardType
)

object Flashcard { implicit val flashcardFormat: OFormat[Flashcard] = Json.format[Flashcard] }
