package utils

import javax.inject.Singleton
import org.postgresql.util.PSQLException
import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent._

@Singleton
class JsonHttpErrorHandler extends HttpErrorHandler {
  private final def error(content: JsObject): JsObject = Json.obj("error" -> content)

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    if (play.api.http.Status.isClientError(statusCode)) {
      Future.successful(Results.Status(statusCode)(error(Json.obj("requestId" -> request.id, "message" -> message))))
    } else {
      throw new IllegalArgumentException(
        s"onClientError invoked with non client error status code $statusCode: $message"
      )
    }
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = Future.successful {
    exception match {
      case e: PSQLException =>
        InternalServerError(error(Json
          .obj("requestId" -> request.id, "type" -> "PSQLException", "message" -> e.getServerErrorMessage.getMessage)))
      case e: Exception =>
        InternalServerError(
          error(Json.obj("requestId" -> request.id, "type" -> "Exception", "message" -> e.getMessage)))
    }
  }
}
