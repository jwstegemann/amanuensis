package amanuensis.api.security

import spray.http._
import spray.util._
import HttpHeaders._
import spray.routing.authentication._
import spray.routing.{RequestContext, RoutingSettings, AuthenticationFailedRejection}
import spray.routing.AuthenticationFailedRejection.{CredentialsRejected, CredentialsMissing}

import scala.concurrent._
import scala.concurrent.duration._

import akka.pattern._
import akka.actor._
import akka.util.Timeout

import language.postfixOps

import javax.crypto._
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import org.apache.commons.codec.binary.Hex

import amanuensis.domain.UserContext
import amanuensis.core.util.Converters
import spray.http.HttpHeaders._

import amanuensis.core.UserActor


class StatelessCookieAuthenticator(userActor: ActorSelection)(implicit val ec: ExecutionContext, val timeout: Timeout) extends ContextAuthenticator[UserContext] {

  import UserActor._

  val invalid = future { Left(AuthenticationFailedRejection(CredentialsMissing, Nil)) }

  def apply(ctx: RequestContext) = {
    val cookieOption: Option[HttpCookie] = ctx.request.cookies.find(_.name == StatelessCookieAuth.AUTH_COOKIE_NAME)

    ctx.request.header[Host] match {
      case Some(host) => {
        cookieOption match {
          case Some(token) => {
            val username = token.content.slice(41,token.content.length)

            println("username:" + username)
            println("host:" + host.host)            

            println("token from cookie:" + token.content)
            println("generated token:" + StatelessCookieAuth.getSignedToken(username, host.host))

            if (Converters.constantTimeEquals(token.content, StatelessCookieAuth.getSignedToken(username, host.host))) {

              (userActor ? GetUserContext(username)).mapTo[Option[UserContext]] map {
                case Some(userContext) => Right(userContext)
                case None => Left(AuthenticationFailedRejection(CredentialsMissing, Nil)) // not logged in (no cache entry)
              }
            }
            else {
               invalid // invalid token
            } 
          }    
          case None => invalid // no cookie
        }
      }
      case None => invalid // no host
    }
  }
}


object StatelessCookieAuth {

  val mac = Mac.getInstance("HmacSHA1")
  val transformation = "AES"
  val random = new SecureRandom()

  def sign(msg: String): String = {
    mac.init(new SecretKeySpec(secret, "HmacSHA1"))
    Converters.hex2Str(mac.doFinal(msg.getBytes("utf-8")))
  }

  def getSignedToken(username: String, host: String): String = {
    //ToDo: Add actual Date
    val token = username + host
    sign(token) + "-" + username
  }


  val AUTH_COOKIE_NAME = "authtoken"

  val secret = scala.util.Properties.envOrElse("AUTH_SECRET", "skjdkj3uandka!279r4348o7rsoidfbsbd$iu2z212423bu&bshj%sdf0&9093").getBytes("utf-8")


  def apply(userActor: ActorSelection)(implicit ec: ExecutionContext, timeout: Timeout): StatelessCookieAuthenticator =
    new StatelessCookieAuthenticator(userActor)
}