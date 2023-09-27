package models.mindmap.dto

import play.api.libs.json.{Json, OFormat}

case class BlogPage(
    html: String
)
object BlogPage { implicit val blogPageFormat: OFormat[BlogPage] = Json.format[BlogPage] }
