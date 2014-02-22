package amanuensis.api

import scala.language.postfixOps

import akka.actor.{ActorLogging, Actor}
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.concurrent.Future

import spray.http.StatusCodes._
import spray.http._
import spray.routing._
import spray.util.{SprayActorLogging, LoggingContext}
import spray.httpx.SprayJsonSupport
import spray.routing.authentication._
import scala.concurrent.ExecutionContext
import spray.httpx.marshalling.Marshaller
import spray.http.HttpHeaders.RawHeader

import amanuensis.api.exceptions._
import amanuensis.domain.Message
import amanuensis.domain.Severities._
import amanuensis.domain.MessageJsonProtocol._
import amanuensis.domain.{UserContext, UserContextProtocol}

import spray.http.HttpHeaders._

import amanuensis.api.security._

import amanuensis.core.neo4j.Neo4JException
import amanuensis.core.elasticsearch.ElasticSearchException


class RootServiceActor extends Actor with ActorLogging with HttpService with AmanuensisExceptionHandler with SprayJsonSupport 
  with StoryHttpService 
  with QueryHttpService 
  with UserHttpService
  with StaticHttpService 
  with AttachmentHttpService 
  with GraphHttpService {

  import UserContextProtocol._

  def actorRefFactory = context
  implicit def executionContext = context.dispatcher

  //FixMe: reduce it again!
  private implicit val timeout = new Timeout(60 seconds)

  // check if auth is disabled for development
  private val doAuth = scala.util.Properties.envOrElse("AMANUENSIS_AUTH", "true").toBoolean
  if (!doAuth) log.info("************** DISABLING AUTHENTICATION ********************")

  def innerRoute(userContext: UserContext) = {
      storyRoute(userContext) ~
      queryRoute(userContext) ~
      attachmentRoute ~
      graphRoute(userContext)
  } 

  val dummyUser = UserContext("dummy", "Dummy", Nil)

  def receive = runRoute(
    staticRoute ~
    (doAuth match {
      case true => {
        userRoute() ~
        authenticate(StatelessCookieAuth(userActor)) { userContext =>
          innerRoute(userContext)
        }
      }
      case false => {
        path("user" / "login") {
          post {
            complete(dummyUser)
          }
        } ~
        innerRoute(dummyUser)
      }
    }) ~
    pathSingleSlash {
      redirect("/app/index.html", MovedPermanently)
    }
  )
}
