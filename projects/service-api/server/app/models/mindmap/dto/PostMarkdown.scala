package models.mindmap.dto

import play.api.libs.json.{Json, OFormat}

case class PostMarkdown(
    markdown: String
)
object PostMarkdown { implicit val postJsonFormat: OFormat[PostMarkdown] = Json.format[PostMarkdown] }
