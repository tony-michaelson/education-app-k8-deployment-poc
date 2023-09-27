package models.flashcard.exercise.dto

import play.api.libs.json.{Json, OFormat}

case class CodeExerciseBriefTest(
    cardType: String,
    name: String,
    explanation: String,
    template: String,
    test: String,
)
object CodeExerciseBriefTest { implicit val f: OFormat[CodeExerciseBriefTest] = Json.format[CodeExerciseBriefTest] }
