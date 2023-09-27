package models.flashcard.exercise.dto

import play.api.libs.json.{Json, OFormat}

case class UserCode(
    code: String
)
object UserCode { implicit val userCodeJsonFormat: OFormat[UserCode] = Json.format[UserCode] }
