package models.flashcard.exercise.dto

import play.api.libs.json.{Json, OFormat}

case class ExerciseAnswer(
    code: String,
    seconds: Int,
)
object ExerciseAnswer {
  implicit val testAnswerJsonFormat: OFormat[ExerciseAnswer] = Json.format[ExerciseAnswer]
}
