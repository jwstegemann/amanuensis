package amanuensis.api

import scala.language.postfixOps

import spray.http.StatusCodes._
import spray.http._
import spray.routing._
import spray.util.{SprayActorLogging, LoggingContext}
import util.control.NonFatal
import spray.httpx.marshalling.Marshaller
import spray.http.HttpHeaders.RawHeader
import akka.actor.{ActorLogging, Actor}
import akka.util.Timeout
import scala.concurrent.duration.DurationInt
import spray.httpx.SprayJsonSupport

import amanuensis.api.exceptions._
import amanuensis.domain.Message
import amanuensis.domain.Severities._
import amanuensis.domain.MessageJsonProtocol._


class RootServiceActor extends Actor with ActorLogging with HttpService with SprayJsonSupport with StoryHttpService {

//  val userContextActor = actorRefFactory.actorSelection("user/userContext")

  private implicit val timeout = new Timeout(2 seconds)

  def actorRefFactory = context

  implicit val amanuensisExceptionHandler = ExceptionHandler {
    case InternalServerErrorException(messages) => complete(InternalServerError, messages)
    case NotFoundException(message) => complete(NotFound, message)
    case ValidationException(messages) => complete(PreconditionFailed, messages)
    case t: Throwable => {
      log.error(t, s"Unexpected error:")
      complete(InternalServerError, Message("An unexpected Error occured. Please inform your system administrator.", `ERROR`))
    }
  }

  def receive = runRoute(
    storyRoute
  )
}
