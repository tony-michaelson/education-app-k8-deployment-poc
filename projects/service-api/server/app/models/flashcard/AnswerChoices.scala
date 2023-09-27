package models.flashcard

import java.util.UUID

import io.masterypath.slick.{AnswerChoice, AnswerChoiceID}
import play.api.libs.json.{Json, OFormat}

case class AnswerChoiceBriefEdit(id: AnswerChoiceID, answer: String, correct: Boolean)
object AnswerChoiceBriefEdit { implicit val f: OFormat[AnswerChoiceBriefEdit] = Json.format[AnswerChoiceBriefEdit] }

case class AnswerChoiceBriefTest(id: AnswerChoiceID, answer: String)
object AnswerChoiceBriefTest { implicit val f: OFormat[AnswerChoiceBriefTest] = Json.format[AnswerChoiceBriefTest] }

case class AnswerChoices(choices: Seq[AnswerChoice]) {
  lazy val correctChoices: Seq[AnswerChoice] = choices.filter(_.correct == true)
  lazy val allChoices: Seq[AnswerChoice]     = choices
  lazy val choicesBriefEdit: Seq[AnswerChoiceBriefEdit] =
    choices.map(x => AnswerChoiceBriefEdit(id = x.id, answer = x.answer, correct = x.correct))
  lazy val choicesBriefTest: Seq[AnswerChoiceBriefTest] =
    choices.map(x => AnswerChoiceBriefTest(id = x.id, answer = x.answer))
  lazy val correctNum: Int  = choices.foldLeft(0)((a, b) => if (b.correct) a + 1 else a)
  lazy val isRadio: Boolean = if (correctNum == 1) true else false

  def userSelectedAllCorrectChoices(userAnswers: Seq[UUID]): Boolean = {
    val correctAnswersTotal = userAnswers.foldLeft(0) { (correctNum, userChoiceID) =>
      if (userSelectedACorrectChoice(userChoiceID)) markCorrect(correctNum) else markIncorrect(correctNum)
    }
    correctAnswersTotal == correctChoices.length
  }

  private def userSelectedACorrectChoice(userChoiceID: UUID): Boolean = {
    correctChoices.exists(choice => choice.id.uuid == userChoiceID)
  }

  private def markCorrect(correctNum: Int): Int = { correctNum + 1 }

  private def markIncorrect(correctNum: Int): Int = { correctNum - 1 }
}
