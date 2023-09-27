package models.flashcard.dto

import io.masterypath.slick.FlashcardTypeID
import play.api.libs.json.{Json, OFormat}

case class FlashcardTypeBrief(
    id: FlashcardTypeID,
    cardType: String,
    name: String,
    commonName: String,
    description: Option[String],
)

object FlashcardTypeBrief { implicit val cardJsonFormat: OFormat[FlashcardTypeBrief] = Json.format[FlashcardTypeBrief] }
