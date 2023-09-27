package utils.controller

import models.organization.Member
import play.api.libs.json.{JsError, JsObject, JsResult, Json}
import play.api.mvc.{BaseControllerHelpers, Result}

import scala.concurrent.{ExecutionContext, Future}

trait ControllerHelperFunctions extends BaseControllerHelpers {
  protected def authorizeMember(memberLookup: Future[Option[Member]], responseHandler: (Member) => Future[Result])(
      implicit ec: ExecutionContext): Future[Result] = {
    memberLookup.flatMap {
      case Some(user) => responseHandler(user)
      case None       => Future.successful(unAuthorizedMessage("ACCESS DENIED"))
    }
  }

  private def unAuthorizedMessage(message: String): Result = {
    Unauthorized(Json.obj("message" -> message))
  }

  protected def validateJSON[JsonModel](jsModel: JsResult[JsonModel],
                                        responseHandler: JsonModel => Future[Result]): Future[Result] = {
    jsModel.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      modelObj => responseHandler(modelObj)
    )
  }

  protected def reportUpdateStatus(numberOfUpdates: Future[Int], returnJSON: Future[Option[JsObject]])(
      implicit ec: ExecutionContext): Future[Result] =
    returnJSON.flatMap {
      case Some(json) => reportUpdateStatus(numberOfUpdates, Some(json))
      case None       => reportUpdateStatus(numberOfUpdates, None)
    }

  protected def reportUpdateStatus(numberOfUpdates: Future[Int], returnJSON: Option[JsObject])(
      implicit ec: ExecutionContext): Future[Result] =
    numberOfUpdates.map {
      case rowsUpdated if rowsUpdated > 0 =>
        returnJSON match {
          case Some(jsonKeys) => Ok(jsonKeys)
          case _              => Ok("")
        }
      case _ =>
        // Most often this will be from the client supplying bad input data
        badRequestMessage("Nothing Updated")
    }

  protected def badRequestMessage(message: String): Result = {
    BadRequest(Json.obj("message" -> message))
  }

  protected def okMessage(message: String): Result = {
    Ok(Json.obj("message" -> message))
  }

  protected def severErrorMessage(message: String): Result = {
    InternalServerError(Json.obj("message" -> message))
  }

  protected def definedOrElse[T](value: Option[T], setNull: Boolean, altValue: Option[T]) = value match {
    case Some(x) => Some(x)
    case None    => if (setNull) { None } else { altValue }
  }
}
