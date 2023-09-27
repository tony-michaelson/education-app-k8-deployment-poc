package models.flashcard.exercise.dto

import io.masterypath.slick.FlashcardTypeID
import play.api.libs.json.{Json, OFormat}

case class Exercise(
    flashcardTypeID: FlashcardTypeID,
    solution: String,
    test: String,
    explanation: String,
    template: String
)
object Exercise { implicit val exerciseJsonFormat: OFormat[Exercise] = Json.format[Exercise] }
