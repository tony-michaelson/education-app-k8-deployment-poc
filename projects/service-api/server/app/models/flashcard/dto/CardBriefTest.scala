package models.flashcard.dto

import models.flashcard.AnswerChoiceBriefTest
import play.api.libs.json.{Json, OFormat}

case class CardBriefTest(
    name: String,
    question: String,
    markdown_html: String,
    cardType: String,
    choices: Seq[AnswerChoiceBriefTest],
    radio: Boolean,
    audio: Option[String],
)
object CardBriefTest { implicit val flashcardFormat: OFormat[CardBriefTest] = Json.format[CardBriefTest] }
