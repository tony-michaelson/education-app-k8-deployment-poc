package models.flashcard.dto

import java.util.UUID

import io.masterypath.slick.{AnswerChoice, AnswerChoiceID, Card, FlashcardTypeID, NodeID}
import play.api.libs.json.{Json, OFormat}

case class CardPost(
    flashcardTypeID: FlashcardTypeID,
    question: String,
    markdown: String,
    audioURL: Option[String],
    answers: Seq[CardAnswer]
) {
  def toCardAnswerChoiceTuple(nodeID: NodeID, mapID: UUID, markdownHTML: String): (Card, Seq[AnswerChoice]) = {
    val newCard = Card(
      id = nodeID,
      mapID = mapID,
      flashcardTypeID = flashcardTypeID,
      question = question,
      markdown = markdown,
      markdown_html = markdownHTML,
      audio = audioURL,
    )
    val newAnswerChoices = answers.map(
      x => AnswerChoice(AnswerChoiceID.random, cardID = nodeID, answer = x.answer, correct = x.correct)
    )
    (newCard, newAnswerChoices)
  }
}
object CardPost { implicit val cardJsonFormat: OFormat[CardPost] = Json.format[CardPost] }
