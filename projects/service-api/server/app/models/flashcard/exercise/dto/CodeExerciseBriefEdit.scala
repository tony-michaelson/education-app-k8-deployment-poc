package models.flashcard.exercise.dto

import play.api.libs.json.{Json, OFormat}

case class CodeExerciseBriefEdit(
    cardType: String,
    name: String,
    explanation: String,
    template: String,
    test: String,
    solution: String,
)
object CodeExerciseBriefEdit { implicit val f: OFormat[CodeExerciseBriefEdit] = Json.format[CodeExerciseBriefEdit] }
