package amanuensis.api

import spray.http._
import spray.routing._
import scala.concurrent.ExecutionContext
import akka.actor.{Actor, ActorRef, ActorLogging}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration.DurationInt
import amanuensis.domain.Story
import spray.json._
import spray.httpx.marshalling._
import spray.httpx.SprayJsonSupport

import scala.concurrent.future

import amanuensis.domain.{UserContext, LoginRequest, UserContextProtocol, ChangePasswordRequest, UserLogin}

import StatusCodes._

import spray.routing.AuthenticationFailedRejection

import language.postfixOps
import amanuensis.core.UserActor
import amanuensis.api.security.StatelessCookieAuth

import spray.http.HttpHeaders._


trait UserHttpService extends HttpService with SprayJsonSupport  { self: ActorLogging =>

  import UserContextProtocol._
  import UserActor._

  private implicit val timeout = new Timeout(5 seconds)
  private implicit def executionContext = actorRefFactory.dispatcher

  val userActor = actorRefFactory.actorSelection("/user/user")

  val secureCookie = scala.util.Properties.envOrElse("AMANUENSIS_SECURE_COOKIE", "true").toBoolean
  if (!secureCookie) log.info("************** DISABLING SECURE COOKIES ********************")

  def userRoute() = {
    /*
       * return UserContext when successfully logged in
       */
    pathPrefix("user") {
      path("login") {
        post {
          clientIP { ip =>
            entity(as[LoginRequest]) { loginRequest =>
              log.info(loginRequest.username + " logged in (or tried at least)")

              val future = (userActor ? CheckUser(loginRequest.username, loginRequest.password)).mapTo[UserContext]
                
              val result = future.map { userContext => 
                val token = StatelessCookieAuth.getSignedToken(loginRequest.username, ip.toString)
                //ToDo: read cookie ttl from config
                val cookie = HttpCookie(StatelessCookieAuth.AUTH_COOKIE_NAME, token, path = Some("/"), maxAge = Some(86400L), httpOnly = true, secure = secureCookie)
                HttpResponse(status=OK,headers=`Set-Cookie`(cookie) :: Nil, entity=HttpEntity(userContext.toJson.compactPrint))
              }.recover {
                case _ => HttpResponse(Unauthorized)
              }
              complete(result)
            }
          }
        }
      } ~
      path("logout") {
        get {
          deleteCookie(name=StatelessCookieAuth.AUTH_COOKIE_NAME, path = "/"  ) {
            complete(OK)
          }
        }
      }
    }
  }

  def innerUserRoute(userContext: UserContext) = {
    pathPrefix("user") {
      path("changePwd") {
        post {
          entity(as[ChangePasswordRequest]) { request =>
            dynamic {
              complete((userActor ? ChangePassword(userContext.login, request.oldPwd, request.newPwd)).mapTo[UserLogin])
            }
          }              
        }
      } ~
      path("info") {
        get {
          dynamic {
            complete(userContext);
          }
        }
      } 
    }
  }
}