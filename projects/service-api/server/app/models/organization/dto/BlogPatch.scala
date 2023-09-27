package models.organization.dto

import play.api.libs.json._

case class BlogPatch(
    theme: Option[String],
    name: Option[String],
    description: Option[String],
)
object BlogPatch {
  implicit val f: OFormat[BlogPatch] = Json.format[BlogPatch]
}
