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

import amanuensis.core.elasticsearch._
import amanuensis.core.QueryActor._
import amanuensis.domain.{Story, StoryInfo, StoryContext, StoryProtocol}

import StatusCode._


trait QueryHttpService extends HttpService with SprayJsonSupport { 

  import ElasticSearchProtocol._

  private val queryActor = actorRefFactory.actorSelection("/user/query")

  private implicit val timeout = new Timeout(2.seconds)
  private implicit def executionContext = actorRefFactory.dispatcher


  val queryRoute =
    path("query") {
      entity(as[QueryRequest]) { queryRequest: QueryRequest =>
        post {
          dynamic {
            complete((queryActor ? Fulltext(queryRequest)).mapTo[QueryResult])
          }
        }
      }
    }
}