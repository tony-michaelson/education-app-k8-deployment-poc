package models.flashcard.dto

import models.flashcard.AnswerChoiceBriefEdit
import play.api.libs.json.{Json, OFormat}

case class CardBriefEdit(
    name: String,
    question: String,
    markdown: String,
    cardType: String,
    choices: Seq[AnswerChoiceBriefEdit],
    audio: Option[String],
)
object CardBriefEdit { implicit val flashcardFormat: OFormat[CardBriefEdit] = Json.format[CardBriefEdit] }
