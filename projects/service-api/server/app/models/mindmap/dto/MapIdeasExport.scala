package models.mindmap.dto

import play.api.libs.json.{Json, OFormat}

case class MapIdeasExport(
    item: NodeCardPostInfo,
    children: Seq[MapIdeasExport],
)
object MapIdeasExport { implicit val mapContentFormat: OFormat[MapIdeasExport] = Json.format[MapIdeasExport] }
