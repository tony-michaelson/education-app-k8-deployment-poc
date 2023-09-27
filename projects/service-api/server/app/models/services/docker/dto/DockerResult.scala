package models.services.docker.dto

import play.api.libs.json._

case class DockerResult(
    output: String
)

// https://www.playframework.com/documentation/2.8.x/ScalaJsonAutomated
object DockerResult {
  implicit val dockerResultReads = Json.reads[DockerResult]
}
