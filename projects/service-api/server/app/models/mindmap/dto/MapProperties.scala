package models.mindmap.dto

import enumeratum.{PlayJsonEnum, Enum, EnumEntry}
import play.api.libs.json.{Json, OFormat}

sealed trait MapMode extends EnumEntry

object MapMode extends Enum[MapMode] with PlayJsonEnum[MapMode] {

  val values = findValues

  case object MAP      extends MapMode
  case object LIST     extends MapMode
  case object DOCUMENT extends MapMode

}

case class MapProperties(name: String,
                         mode: MapMode,
                         description: Option[String],
                         icon: Option[String],
                         cost: Option[Double])
object MapProperties {
  implicit val mindMapFormat: OFormat[MapProperties] = Json.format[MapProperties]
}

case class MapPropertiesPatch(name: Option[String],
                              mode: Option[MapMode],
                              icon: Option[String],
                              description: Option[String],
                              cost: Option[Double])
object MapPropertiesPatch {
  implicit val patchMindMapFormat: OFormat[MapPropertiesPatch] = Json.format[MapPropertiesPatch]
}
