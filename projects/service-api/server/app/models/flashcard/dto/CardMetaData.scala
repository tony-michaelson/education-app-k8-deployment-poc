package models.flashcard.dto

import java.util.UUID

import play.api.libs.json.{Json, OFormat}

case class CardMetaData(
    nodeID: UUID,
    parentID: UUID,
    segmentID: UUID,
    ef: Double,
    lastAnswer: Long,
    due: Long,
    color: String,
    flashCardType: FlashcardTypeBrief,
)
object CardMetaData { implicit val CardDueJsonFormat: OFormat[CardMetaData] = Json.format[CardMetaData] }
