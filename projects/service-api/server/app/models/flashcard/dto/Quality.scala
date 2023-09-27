package models.flashcard.dto

import play.api.libs.json.{Json, OFormat}

case class Quality(quality: Int)
object Quality { implicit val QualityJsonFormat: OFormat[Quality] = Json.format[Quality] }
