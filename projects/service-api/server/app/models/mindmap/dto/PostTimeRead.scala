package models.mindmap.dto

import play.api.libs.json.{Json, OFormat}

case class PostTimeRead(postID: String, timeRead: Long)

object PostTimeRead { implicit val PostFormat: OFormat[PostTimeRead] = Json.format[PostTimeRead] }
