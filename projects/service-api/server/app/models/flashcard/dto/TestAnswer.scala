package models.flashcard.dto

import java.util.UUID

import play.api.libs.json.{Json, OFormat}

case class TestAnswer(choices: Seq[UUID], seconds: Int)
object TestAnswer { implicit val TestAnswerJsonFormat: OFormat[TestAnswer] = Json.format[TestAnswer] }
