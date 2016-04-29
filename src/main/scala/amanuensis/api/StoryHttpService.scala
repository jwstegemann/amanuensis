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
import amanuensis.core.AccessActor._
import amanuensis.domain.{Story, StoryInfo, StoryContext, StoryProtocol, UserContext, StoryAccess, StoryId}

import StatusCode._


trait StoryHttpService extends HttpService with SprayJsonSupport { 

  import StoryProtocol._

  private val storyActor = actorRefFactory.actorSelection("/user/story")
  private val slotActor = actorRefFactory.actorSelection("/user/slot")
  private val accessActor = actorRefFactory.actorSelection("/user/access")

  private implicit val timeout = new Timeout(5 seconds)
  private implicit def executionContext = actorRefFactory.dispatcher


  def storyRoute(userContext: UserContext) =
    pathPrefix("story") {
      pathPrefix(Segment) { storyId: String =>
        pathEnd {
          // FIXME: rehect if not a valid id!
          get {
            complete((storyActor ? Retrieve(storyId, userContext.login)).mapTo[StoryContext])
          } ~
          put {
            entity(as[Story]) { story =>
              val storyWithMeta = story.copy(
                modified = DateTime.now.toString,
                modifiedBy = userContext.login                
              )
              complete((storyActor ? Update(storyId, storyWithMeta, story.modified, userContext.login)).mapTo[StoryId])
            }
          } ~
          delete {
            complete((storyActor ? Delete(storyId, userContext.login)) map { value => StatusCodes.OK })
          }
        } ~
        pathPrefix(Map("in" -> true, "out" -> false) / Segment) { (inbound: Boolean, slotName: String) =>
          pathEnd {
            get {
              dynamic {                
    //            log.debug(s"request: get stories in slot $slotName for story $storyId")
                complete((slotActor ? List(storyId, slotName, inbound, userContext.login)).mapTo[Seq[StoryInfo]])
              }
            } ~
            post {
              entity(as[Story]) { story =>
                dynamic {
  //                log.debug(s"request: creating new story $story in slot $slotName at story $storyId")
                  val storyWithMeta = story.copy(
                    created = DateTime.now.toString, 
                    createdBy = userContext.login, 
                    modified = DateTime.now.toString,
                    modifiedBy = userContext.login
                  )
                  complete((slotActor ? CreateAndAdd(storyId, slotName, storyWithMeta, inbound, userContext.login)).mapTo[StoryInfo])
                }
              }              
            }
          } ~
          path(Segment) { targetStoryId: String =>
            put {
              dynamic {
    //            log.debug(s"request: add story $targetStoryId to slot $slotName at story $storyId")
                if (targetStoryId == storyId) {
                  reject(ValidationRejection("A story cannot be added to a slot at itself."))
                }
                else {
                  complete((slotActor ? Add(storyId, slotName, targetStoryId, inbound, userContext.login)) map { value => StatusCodes.OK })
                }
              }
            } ~
            delete {
              dynamic {
      //          log.debug(s"request: remove story $targetStoryId from slot $slotName at story $storyId")
                complete((slotActor ? Remove(storyId, slotName, targetStoryId, inbound, userContext.login)) map { value => StatusCodes.OK })
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
              val storyWithMeta = story.copy(
                created = DateTime.now.toString, 
                createdBy = userContext.login,
                modified = DateTime.now.toString,
                modifiedBy = userContext.login                
              )
              complete((storyActor ? Create(storyWithMeta, userContext.login)).mapTo[StoryInfo])
  //            complete(s"creating new story with $story")
            }
          }
        }
      }

    }
}