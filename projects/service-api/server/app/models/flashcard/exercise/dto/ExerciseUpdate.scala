package models.flashcard.exercise.dto

import play.api.libs.json.{Json, OFormat}

case class ExerciseUpdate(
    solution: String,
    test: String,
    explanation: String,
    template: String
)
object ExerciseUpdate {
  implicit val exerciseUpdateJsonFormat: OFormat[ExerciseUpdate] = Json.format[ExerciseUpdate]
}
