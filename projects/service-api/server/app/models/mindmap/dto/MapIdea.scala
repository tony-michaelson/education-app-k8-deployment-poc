package models.mindmap.dto

import play.api.libs.json.{Json, OFormat}

case class MapIdea(
    id: String,
    title: String,
    attr: NodeAttr,
    ideas: Option[Map[String, MapIdea]]
)
object MapIdea { implicit val mapIdeaFormat: OFormat[MapIdea] = Json.format[MapIdea] }
