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
import amanuensis.core.neo4j.Neo4JException
import amanuensis.core.elasticsearch.ElasticSearchException
import spray.http.HttpHeaders._

import amanuensis.api.security._


class RootServiceActor extends Actor with ActorLogging with HttpService with SprayJsonSupport 
  with StoryHttpService 
  with QueryHttpService 
  with UserHttpService
  with StaticHttpService 
  with AttachmentHttpService {

  import amanuensis.core.UserActor._

  import UserContextProtocol._

  def actorRefFactory = context
  implicit def executionContext = context.dispatcher

  //FixMe: reduce it again!
  private implicit val timeout = new Timeout(60 seconds)

  //val userActor = actorRefFactory.actorSelection("/user/user")

  private val doAuth = scala.util.Properties.envOrElse("AMANUENSIS_AUTH", "true").toBoolean

  if (!doAuth) log.info("************** DISABLING AUTHENTICATION ********************")

  implicit val amanuensisExceptionHandler = ExceptionHandler {
    case InternalServerErrorException(messages) => complete(InternalServerError, messages)
    case NotFoundException(message) => complete(NotFound, message)
    case ValidationException(messages) => complete(PreconditionFailed, messages)
    case Neo4JException(message) => {
      log.error(s"Neo4J-error: $message")
      complete(InternalServerError, Message("An unexpected Error occured. Please inform your system administrator.", `ERROR`))     
    }
    case ElasticSearchException(message) => {
      log.error(s"ElasticSearch-error: $message")
      complete(InternalServerError, Message("An unexpected Error occured. Please inform your system administrator.", `ERROR`))     
    }
    case t: Throwable => {
      log.error(t, "Unexpected error:")
      complete(InternalServerError, Message("An unexpected Error occured. Please inform your system administrator.", `ERROR`))
    }
  }


  def innerRoute(userContext: UserContext) = {
      storyRoute(userContext) ~
      queryRoute ~
      attachmentRoute
  } 

  def receive = runRoute(
    userRoute() ~
    staticRoute ~
    (doAuth match {
      case true => {
        authenticate(StatelessCookieAuth(userActor)) { userContext =>
          innerRoute(userContext)
        }
      }
      case false => {
        innerRoute(UserContext("dummy", "Dummy", Nil))
      }
    })
  )
}
