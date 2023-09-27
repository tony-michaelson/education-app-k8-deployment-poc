package services

import io.masterypath.slick.Site
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.libs.json._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JekyllService @Inject()(config: Configuration, ws: WSClient)(implicit ec: ExecutionContext) {
  private val url: String = config.get[String]("jekyllService.url")

  def buildSite(site: Site): Future[Boolean] = {
    val data = Json.obj(
      "domain" -> ("staging." + site.domain),
      "theme"  -> site.theme,
    )

    ws.url(url + """/build""").post(data).map { response =>
      if (response.status == 200) {
        true
      } else {
        false
      }
    }
  }

  def publishSite(site: Site): Future[Boolean] = {
    val data = Json.obj(
      "staging_domain" -> ("staging." + site.domain),
      "domain"         -> site.domain,
    )

    ws.url(url + """/publish""").post(data).map { response =>
      if (response.status == 200) {
        true
      } else {
        false
      }
    }
  }
}
