package amanuensis.api.exceptions

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

import spray.http.StatusCodes._
import spray.http._
import spray.routing._
import spray.util.{SprayActorLogging, LoggingContext}
import spray.httpx.SprayJsonSupport
import spray.http.HttpHeaders._
import spray.httpx.marshalling.Marshaller

import akka.actor.{ActorLogging, Actor}
import akka.pattern._
import akka.util.Timeout

import amanuensis.api.exceptions._
import amanuensis.domain.Message
import amanuensis.domain.Severities._
import amanuensis.domain.MessageJsonProtocol._
import amanuensis.domain.{UserContext, UserContextProtocol}
import amanuensis.core.neo4j.Neo4JException
import amanuensis.core.elasticsearch.ElasticSearchException


object Implicits {
	implicit def makeListFrom(message: Message) = message :: Nil
}

case class InternalServerErrorException(val messages: List[Message]) extends Exception

case class NotFoundException(val messages: List[Message]) extends Exception

case class ValidationException(val messages: List[Message]) extends Exception

case class OptimisticLockException(val messages: List[Message]) extends Exception


trait AmanuensisExceptionHandler { this: Actor with ActorLogging with HttpService with SprayJsonSupport =>

  implicit val amanuensisExceptionHandler = ExceptionHandler {
      case InternalServerErrorException(messages) => {
        log.error(s"Internal-Server-Error: $messages")
        complete(InternalServerError, messages)
      }
      case NotFoundException(message) => complete(NotFound, message)
      case OptimisticLockException(message) => complete(Locked, message)
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
  
}
