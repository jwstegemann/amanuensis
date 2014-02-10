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

import StatusCode._


trait GraphHttpService extends HttpService with SprayJsonSupport { 

  import GraphProtocol._

  private val graphActor = actorRefFactory.actorSelection("/user/graph")

  private implicit val timeout = new Timeout(10.seconds)
  private implicit def executionContext = actorRefFactory.dispatcher


  val graphRoute =
    pathPrefix("graph") {
      path("findpaths" / Segment / Segment / Segment) { (sourceStoryId: String, tagName: String, targetStoryId: String ) =>
        get {
          dynamic {
            complete((graphActor ? FindPaths(sourceStoryId, targetStoryId, tagName)).mapTo[Seq[StoryNode]])
          }
        }
      }
    }
}