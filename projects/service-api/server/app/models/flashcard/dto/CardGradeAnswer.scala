package models.flashcard.dto

import io.masterypath.slick.AnswerChoice
import play.api.libs.json.{Json, OFormat}

case class CardGradeAnswer(
    correct: Boolean,
    answers: Option[Seq[AnswerChoice]] = None,
    message: Option[String] = None,
)
object CardGradeAnswer { implicit val answerJsonFormat: OFormat[CardGradeAnswer] = Json.format[CardGradeAnswer] }
