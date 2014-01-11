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
import spray.routing.authentication.UserPass
import spray.routing.authentication.BasicAuth
import spray.httpx.marshalling.Marshaller
import spray.http.HttpHeaders.RawHeader

import amanuensis.api.exceptions._
import amanuensis.domain.Message
import amanuensis.domain.Severities._
import amanuensis.domain.MessageJsonProtocol._
import amanuensis.core.neo4j.Neo4JException
import amanuensis.core.UserContext




class RootServiceActor extends Actor with ActorLogging with HttpService with SprayJsonSupport 
  with StoryHttpService 
  with QueryHttpService 
  with StaticHttpService {

  import amanuensis.core.UserActor._

  def actorRefFactory = context
  implicit def executionContext = context.dispatcher

  private implicit val timeout = new Timeout(2 seconds)

  val userActor = actorRefFactory.actorSelection("/user/user")


  def myUserPassAuthenticator(userPassOption: Option[UserPass]): Future[Option[UserContext]] = {
    (userActor ? new CheckUser(userPassOption)).mapTo[Option[UserContext]]
  }


  implicit val amanuensisExceptionHandler = ExceptionHandler {
    case InternalServerErrorException(messages) => complete(InternalServerError, messages)
    case NotFoundException(message) => complete(NotFound, message)
    case ValidationException(messages) => complete(PreconditionFailed, messages)
    case Neo4JException(message) => {
      log.error(s"Neo4J-error: $message")
      complete(InternalServerError, Message("An unexpected Error occured. Please inform your system administrator.", `ERROR`))     
    }
    case t: Throwable => {
      log.error(t, "Unexpected error:")
      complete(InternalServerError, Message("An unexpected Error occured. Please inform your system administrator.", `ERROR`))
    }
  }

  def receive = runRoute(
    staticRoute ~
    authenticate(BasicAuth(myUserPassAuthenticator _, realm = "Amanuensis")) { userContext =>
      storyRoute ~
      queryRoute
    } 
  )
}
