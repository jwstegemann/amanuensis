package amanuensis.api

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions

import spray.testkit.Specs2RouteTest
import spray.routing.HttpService
import spray.http.StatusCodes._
import akka.actor.{ActorLogging, Actor}

import amanuensis.domain._
import amanuensis.core._

import amanuensis.core.elasticsearch._

import akka.pattern._

import amanuensis.api.exceptions._
import amanuensis.domain.Message
import amanuensis.domain.Severities._
import amanuensis.domain.MessageJsonProtocol._
import spray.http._
import spray.routing._

import org.joda.time.DateTime


class QueryApiSpec extends Specification with Specs2RouteTest with QueryHttpService with Core with CoreActors with NoTimeConversions {
  
  def actorRefFactory = system // connect the DSL to the test ActorSystem

  import ElasticSearchProtocol._
  import QueryActor._

  //FIXME: externalize this (use just one for Service and Test)!
  implicit val amanuensisExceptionHandler = ExceptionHandler {
    case InternalServerErrorException(messages) => complete(InternalServerError, messages)
    case NotFoundException(message) => complete(NotFound, message)
    case ValidationException(messages) => complete(PreconditionFailed, messages)
    case ElasticSearchException(message) => {
      //log.error(s"Neo4J-error: $message")
      complete(InternalServerError, Message("An unexpected Error occured. Please inform your system administrator.", `ERROR`))     
    }
    case t: Throwable => {
      //log.error(t, "Unexpected error:")
      complete(InternalServerError, Message("An unexpected Error occured. Please inform your system administrator.", `ERROR`))
    }
  }

  private val indexActor = actorRefFactory.actorSelection("/user/query")    

  val testStory = Story(Some("nurEinTestId"),"Testtitel","Dies ist ein kleines Testbeispiel", DateTime.now.toString, "Testuser")

  "The QueryService" should {

    sequential

    "index a story in" in {
      val future = indexActor ! Index(testStory)
      Thread.sleep(2000)
      success
    }

    "find a story in" in {
      Get("/query/Testbeispiel") ~> queryRoute ~> check {
        responseAs[QueryResult] must beLike {
          case QueryResult(took, hits) => {
            println(hits)
            hits.total === 1
          }
        }
      }
    }

    "delete a story in" in {
      val future = indexActor ! DeleteFromIndex(testStory.id.get)
      Thread.sleep(2000)
      success
    }

    "find no story in" in {
      Get("/query/Testbeisp") ~> queryRoute ~> check {
        responseAs[QueryResult] must beLike {
          case QueryResult(took, hits) => {
            println(hits)
            hits.total === 0
          }
        }
      }
    }
    

  }
}