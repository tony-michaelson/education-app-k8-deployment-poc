package services

import play.api.Configuration
import play.api.libs.ws.WSClient
import io.masterypath.slick.FlashcardType
import models.services.docker.dto.DockerResult

import javax.inject.Inject
import models.flashcard.exercise.{CodeExerciseConfig, CodeExerciseResult}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

class DockerService @Inject()(config: Configuration, ws: WSClient)(implicit ec: ExecutionContext) {
  private val url: String = config.get[String]("dockerService.url")

  def dockerRunCode(cardType: FlashcardType, code: String, test: String): Future[CodeExerciseResult] = {
    val config      = Json.parse(cardType.config.getOrElse("{}")).as[CodeExerciseConfig]
    val dockerImage = config.docker_image
    val jsonData = Json.obj(
      "code"  -> code,
      "test"  -> test,
      "image" -> dockerImage
    )

    println(Json.stringify(jsonData))
    ws.url(url + """/run""").post(jsonData).map { response =>
      if (response.status == 200) {
        val result = Json.parse(response.body).as[DockerResult]
        CodeExerciseResult(pass = result.output.matches("(?s)^pass\n.*"),
                           output = result.output.replaceFirst("^pass\n", ""))
      } else {
        CodeExerciseResult(false, output = "Internal Error")
      }
    }
  }

}
