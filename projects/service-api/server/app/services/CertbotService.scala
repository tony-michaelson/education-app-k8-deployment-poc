package services

import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.libs.json._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CertbotService @Inject()(config: Configuration, ws: WSClient)(implicit ec: ExecutionContext) {
  private val url: String = config.get[String]("certbotService.url")

  def createSite(domain: String): Future[Boolean] = {
    val data = Json.obj(
      "domain" -> (domain)
    )

    ws.url(url + """/create_site""").post(data).map { response =>
      if (response.status == 200) {
        true
      } else {
        false
      }
    }
  }

  def deleteSite(domain: String): Future[Boolean] = {
    val data = Json.obj(
      "domain" -> (domain)
    )

    ws.url(url + """/delete_site""").post(data).map { response =>
      if (response.status == 200) {
        true
      } else {
        false
      }
    }
  }

  def createStagingSite(domain: String): Future[Boolean] = {
    createSite("staging." + domain)
  }

  def deleteStagingSite(domain: String): Future[Boolean] = {
    deleteSite("staging." + domain)
  }
}
