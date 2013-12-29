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

import amanuensis.core.StoryActor._
import amanuensis.domain.{Story, StoryInfo, StoryContext, StoryProtocol}

import StatusCode._


trait StoryHttpService extends Directives { self : Actor with HttpService with ActorLogging with SprayJsonSupport =>

  import StoryProtocol._

  val storyActor = actorRefFactory.actorSelection("/user/story")

  private implicit val timeout = new Timeout(2.seconds)
  implicit def executionContext = actorRefFactory.dispatcher


  val storyRoute =
    pathPrefix("story") {
      path(Segment) { storyId : String =>
        // FIXME: rehect if not a valid id!
        get {
          dynamic {
            log.debug(s"request: get details for story $storyId")
            //FIXME: fail if Option is None
            complete((storyActor ? Retrieve(storyId)).mapTo[StoryContext])
//              complete(future { Story(Some(17),"A","B") } )
          }
        } ~
        put {
          entity(as[Story]) { story =>
            dynamic {
              log.debug(s"request: update story $storyId with $story")
              complete((storyActor ? Update(storyId, story)) map { value => StatusCodes.OK })
            }
          }
        } ~
        delete {
          dynamic {
            log.debug(s"request: remove story $storyId")
            complete((storyActor ? Delete(storyId)) map { value => StatusCodes.OK })
          }
        }
      } ~
      post {
        entity(as[Story]) { story =>
          dynamic {
            log.debug(s"request: creating new story with $story")
            complete((storyActor ? Create(story)).mapTo[StoryInfo])
//            complete(s"creating new story with $story")
          }
        }
      }

    }
}