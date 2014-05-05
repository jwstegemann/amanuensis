package amanuensis.api

import spray.http._
import spray.routing._
import scala.concurrent.ExecutionContext
import akka.actor.{Actor, ActorRef, ActorLogging}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration.DurationInt
import spray.json._
import spray.httpx.marshalling._
import spray.httpx.SprayJsonSupport

import scala.concurrent.future

import amanuensis.core.GraphActor._
import amanuensis.domain._
import amanuensis.core.elasticsearch._

import StatusCode._


trait GraphHttpService extends HttpService with SprayJsonSupport { 

  import GraphProtocol._

  private val graphActor = actorRefFactory.actorSelection("/user/graph")

  private implicit val timeout = new Timeout(10.seconds)
  private implicit def executionContext = actorRefFactory.dispatcher


  def graphRoute(userContext: UserContext)=
    pathPrefix("graph") {
      path("findpaths" / Segment / Segment / Segment) { (sourceStoryId: String, tagName: String, targetStoryId: String) =>
        parameter("page".as[Int] ? 0) { page =>
          get {
            dynamic {
              complete((graphActor ? FindPaths(sourceStoryId, targetStoryId, tagName, page, userContext.login)).mapTo[Seq[StoryNode]])
            }
          }
        }
      } ~
      path("favourites") {
        parameter("page".as[Int] ? 0) { page =>
          get {
            dynamic {
              import ElasticSearchProtocol._
              complete((graphActor ? FindFavourites(page, userContext.login)).mapTo[QueryResult])
            }
          }
        }
      } ~
      path("todos") {
        parameter("page".as[Int] ? 0) { page =>
          get {
            dynamic {
              import ElasticSearchProtocol._
              complete((graphActor ? FindToDos(page, userContext.login)).mapTo[QueryResult])
            }
          }
        }
      }     
    }
}