package models.flashcard.exercise

import play.api.libs.json.{Json, OFormat}

case class CodeExerciseConfig(docker_image: String, language: String)
object CodeExerciseConfig {
  implicit val testAnswerJsonFormat: OFormat[CodeExerciseConfig] = Json.format[CodeExerciseConfig]
}
