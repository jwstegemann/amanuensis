package amanuensis.services

import spray.http._
import StatusCodes._
import MediaTypes._
import spray.json._
import spray.httpx.SprayJsonSupport
import spray.routing._
import spray.routing.Directives._

import akka.actor.ActorLogging

import amanuensis.system.MessageJsonProtocol._
import amanuensis.system._
import amanuensis.system.Severities._


trait MessageHandling { self: SprayJsonSupport with ActorLogging =>
  /*
  implicit val amanuensisRejectionHandler = RejectionHandler {
	case AuthenticationRequiredRejection(scheme, realm, params) :: _ =>
		complete(Unauthorized, "Please login")
  }
  */

  implicit val amanuensisExceptionHandler = ExceptionHandler {
    case InternalServerErrorException(messages) => complete(InternalServerError, messages)
    case NotFoundException(message) => complete(NotFound, message)
    case ValidationException(messages) => complete(PreconditionFailed, messages)
    case t: Throwable => {
      log.error(t, s"Unexpected error:")
      complete(InternalServerError, Message("An unexpected Error occured. Please inform your system administrator.", `ERROR`))
    }
  }
}