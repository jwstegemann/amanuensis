package amanuensis.auth

import spray.http._
import spray.util._
import HttpHeaders._
import spray.routing.authentication._
import spray.routing.{RequestContext,RoutingSettings,AuthenticationFailedRejection}
import spray.routing.AuthenticationFailedRejection.{CredentialsRejected, CredentialsMissing}
import spray.json._

import scala.concurrent._
import scala.concurrent.duration._

import amanuensis.auth._

import akka.pattern.ask
import akka.actor._
import akka.util.Timeout

import language.postfixOps




/**
 * A SessionCookieAuthenticator is a ContextAuthenticator that uses credentials passed to the server via the
 * HTTP `Authorization` header to authenticate the user and extract a user object.
 */
class SessionCookieAuthenticator(sessionServiceActor : ActorSelection)(implicit val executionContext: ExecutionContext) extends ContextAuthenticator[UserContext] {

  implicit val timeout = new Timeout(2 seconds)

  val challenges = `WWW-Authenticate`(HttpChallenge(scheme = "Basic", realm = "Amanuensis", params = Map.empty)) :: Nil

  def apply(ctx: RequestContext) = {

    val cookieOption: Option[HttpCookie] = ctx.request.cookies.find(_.name == SESSION_COOKIE_NAME)

    cookieOption match {
      case Some(sessionCookie) => {
        ctx.request.header[Host] match {
          case Some(host) => sessionServiceActor ? IsSessionValidMsg(sessionCookie.content, host.host) map {
              case Some(userContext : UserContext) => Right(userContext)
              case None => Left(AuthenticationFailedRejection(CredentialsRejected, challenges))
            }
          case None => future { Left(AuthenticationFailedRejection(CredentialsMissing, challenges)) }
        }
        
      }
      case None => future { Left(AuthenticationFailedRejection(CredentialsMissing, challenges)) }
    }
  }
}

object SessionCookieAuth {
  def apply()(implicit sessionServiceActor : ActorSelection, ec : ExecutionContext): SessionCookieAuthenticator =
    new SessionCookieAuthenticator(sessionServiceActor)
}