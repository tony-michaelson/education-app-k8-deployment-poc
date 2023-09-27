package services

import java.util.UUID

import javax.inject.{Inject, Singleton}
import com.sendgrid._
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.{Content, Email}
import play.api.Configuration
import play.twirl.api.Html

@Singleton
class SendgridService @Inject()(config: Configuration) {
  private val apiKey    = config.get[String]("sendgrid.apiKey")
  private val clientUrl = config.get[String]("clientUrl")

  def sendOrgInvite(inviteID: UUID, emailAddress: String): Int = {
    val inviteLink = clientUrl + "/join/" + inviteID.toString
    val body       = views.html.email.orgInvite(inviteLink)
    sendEmail("MasteryPath Invite Link", emailAddress, body)
  }

  private def sendEmail(subject: String, emailAddress: String, html: Html): Int = {
    val from    = new Email("customersupport@masterypath.io")
    val to      = new Email(emailAddress)
    val content = new Content("text/html", html.toString)
    val mail    = new Mail(from, subject, to, content)
    val sg      = new SendGrid(apiKey)
    val request = new Request
    request.setMethod(Method.POST)
    request.setEndpoint("mail/send")
    request.setBody(mail.build)
    val response = sg.api(request)
    response.getStatusCode
  }

}
