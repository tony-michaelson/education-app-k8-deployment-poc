package models

import play.api.mvc.Result

import scala.concurrent.Future

abstract class baseDTO {
  def result: Result
  def futureResult: Future[Result]
}
