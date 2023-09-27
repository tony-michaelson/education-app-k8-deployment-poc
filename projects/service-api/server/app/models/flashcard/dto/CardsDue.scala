package models.flashcard.dto

import play.api.libs.json.{Json, OFormat}

case class CardsDue(
    cards: Seq[CardMetaData],
    dueDates: Seq[(String, Int)]
)

object CardsDue { implicit val CardDueJsonFormat: OFormat[CardsDue] = Json.format[CardsDue] }
