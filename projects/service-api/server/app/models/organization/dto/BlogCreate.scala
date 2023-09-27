package models.organization.dto

import play.api.libs.json._

case class BlogCreate(
    subDomain: String
)
object BlogCreate {
  implicit val f: OFormat[BlogCreate] = Json.format[BlogCreate]
}
