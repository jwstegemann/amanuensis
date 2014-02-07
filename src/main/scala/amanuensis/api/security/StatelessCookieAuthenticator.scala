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

  val useForward = scala.util.Properties.envOrElse("AMANUENSIS_USE_FORWARDED_FOR", "true").toBoolean
  //if (useForward) log.info("************** USING FORWARD HEADER ********************")  

  def apply(ctx: RequestContext) = {
    val cookieOption: Option[HttpCookie] = ctx.request.cookies.find(_.name == StatelessCookieAuth.AUTH_COOKIE_NAME)
  
    val host = useForward match {
      case true => {
        ctx.request.header[`X-Forwarded-For`] map { _.addresses.head.toString() }
      }
      case false => {
        ctx.request.header[`Remote-Address`] map { _.address.toString() }
      }
    }

    host match {
      case Some(host) => {
        println("HOST: " + host)
        cookieOption match {
          case Some(token) => {
            val username = token.content.slice(41,token.content.length)

            if (Converters.constantTimeEquals(token.content, StatelessCookieAuth.getSignedToken(username, host))) {

              ((userActor ? GetUserContext(username)).mapTo[UserContext]).map { anything: UserContext =>
                Right(anything)
              }.recover {
                case _ => Left(AuthenticationFailedRejection(CredentialsMissing, Nil)) // not logged in (no cache entry)                
              }
            }
            else {
               invalid // invalid token
            } 
          }    
          case None => invalid // no cookie
        }
      }
      case None => {
        //println("------------------- NO HOST!")
        invalid // no host
      }
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
    //ToDo: Use Calculation of daynumber from spray.http.DateTime here for performance-Reasons
    val date = DateTime.now.toIsoDateString
    val token = s"$username$host$date"
    val signature = sign(token)
    s"$signature-$username"
  }


  val AUTH_COOKIE_NAME = "authtoken"

  val secret = scala.util.Properties.envOrElse("AUTH_SECRET", "skjdkj3uandka!279r4348o7rsoidfbsbd$iu2z212423bu&bshj%sdf0&9093").getBytes("utf-8")


  def apply(userActor: ActorSelection)(implicit ec: ExecutionContext, timeout: Timeout): StatelessCookieAuthenticator =
    new StatelessCookieAuthenticator(userActor)
}