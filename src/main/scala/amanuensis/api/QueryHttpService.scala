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

import amanuensis.core.QueryActor._
import amanuensis.domain.{Story, StoryInfo, StoryContext, StoryProtocol}

import StatusCode._


trait QueryHttpService extends HttpService with SprayJsonSupport { 

  import StoryProtocol._

  private val queryActor = actorRefFactory.actorSelection("/user/query")

  private implicit val timeout = new Timeout(2.seconds)
  private implicit def executionContext = actorRefFactory.dispatcher


  val queryRoute =
    pathPrefix("query") {
      pathEnd {
        get {
          dynamic {
            complete((queryActor ? FindAll()).mapTo[Seq[StoryInfo]])
          }
        }
      }
    }
}