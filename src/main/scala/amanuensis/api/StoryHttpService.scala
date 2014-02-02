package amanuensis.api

import language.postfixOps

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

import org.joda.time.DateTime

import amanuensis.core.StoryActor._
import amanuensis.core.SlotActor._
import amanuensis.domain.{Story, StoryInfo, StoryContext, StoryProtocol, UserContext}

import StatusCode._


trait StoryHttpService extends HttpService with SprayJsonSupport { 

  import StoryProtocol._

  private val storyActor = actorRefFactory.actorSelection("/user/story")
  private val slotActor = actorRefFactory.actorSelection("/user/slot")

  private implicit val timeout = new Timeout(5 seconds)
  private implicit def executionContext = actorRefFactory.dispatcher


  def storyRoute(userContext: UserContext) =
    pathPrefix("story") {
      pathPrefix(Segment) { storyId: String =>
        pathEnd {
          // FIXME: rehect if not a valid id!
          get {
            dynamic {
//              log.debug(s"request: get details for story $storyId")
              complete((storyActor ? Retrieve(storyId)).mapTo[StoryContext])
            }
          } ~
          put {
            entity(as[Story]) { story =>
              dynamic {
 //               log.debug(s"request: update story $storyId with $story")
                complete((storyActor ? Update(storyId, story)) map { value => StatusCodes.OK })
              }
            }
          } ~
          delete {
            dynamic {
  //            log.debug(s"request: remove story $storyId")
              complete((storyActor ? Delete(storyId)) map { value => StatusCodes.OK })
            }
          }
        } ~
        pathPrefix(Segment) { slotName: String =>
          pathEnd {
            get {
              dynamic {
    //            log.debug(s"request: get stories in slot $slotName for story $storyId")
                complete((slotActor ? List(storyId, slotName)).mapTo[Seq[StoryInfo]])
              }
            } ~
            post {
              entity(as[Story]) { story =>
                dynamic {
  //                log.debug(s"request: creating new story $story in slot $slotName at story $storyId")
                  val storyWithMeta = story.copy(created = DateTime.now.toString, createdBy = userContext.name)
                  complete((slotActor ? CreateAndAdd(storyId, slotName, storyWithMeta)).mapTo[StoryInfo])
                }
              }              
            }
          } ~
          path(Segment) { targetStoryId: String =>
            put {
              dynamic {
    //            log.debug(s"request: add story $targetStoryId to slot $slotName at story $storyId")
                complete((slotActor ? Add(storyId, slotName, targetStoryId)) map { value => StatusCodes.OK })
              }
            } ~
            delete {
              dynamic {
      //          log.debug(s"request: remove story $targetStoryId from slot $slotName at story $storyId")
                complete((slotActor ? Remove(storyId, slotName, targetStoryId)) map { value => StatusCodes.OK })
              }
            }
          }
        }
      } ~
      post {
        pathEnd {
          entity(as[Story]) { story =>
            dynamic {
   //           log.debug(s"request: creating new story with $story")
              val storyWithMeta = story.copy(created = DateTime.now.toString, createdBy = userContext.name)
              complete((storyActor ? Create(storyWithMeta)).mapTo[StoryInfo])
  //            complete(s"creating new story with $story")
            }
          }
        }
      }

    }
}