package models.flashcard.dto

import play.api.libs.json.{Json, OFormat}

case class CardAnswer(
    correct: Boolean,
    answer: String
)
object CardAnswer { implicit val answerJsonFormat: OFormat[CardAnswer] = Json.format[CardAnswer] }
