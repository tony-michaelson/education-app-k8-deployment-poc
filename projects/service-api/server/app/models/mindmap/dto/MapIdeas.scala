package models.mindmap.dto

import play.api.libs.json.{Json, OFormat}

case class MapIdeas(
    id: String,
    title: String,
    attr: NodeAttr,
    ideas: Option[Map[String, MapIdea]],
    formatVersion: String,
    permissions: MapRightsBrief,
)
object MapIdeas { implicit val mindMapFormat: OFormat[MapIdeas] = Json.format[MapIdeas] }
