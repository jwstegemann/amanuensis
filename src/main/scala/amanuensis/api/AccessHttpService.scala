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
import amanuensis.domain.{Story, StoryInfo, StoryContext, StoryProtocol, UserContext, StoryAccess}
import amanuensis.domain.UserRights._

import StatusCode._


trait AccessHttpService extends HttpService with SprayJsonSupport { 

  import StoryProtocol._

  private val accessActor = actorRefFactory.actorSelection("/user/access")

  private implicit val timeout = new Timeout(5 seconds)
  private implicit def executionContext = actorRefFactory.dispatcher


  def accessRoute(userContext: UserContext) =
    pathPrefix("share") {
      pathPrefix(Segment) { storyId: String =>      
        pathEnd {
          get {
            dynamic {
              complete((accessActor ? RetrieveAccess(storyId, userContext.login)).mapTo[Seq[StoryAccess]])
            }
          }
        } ~
        path(Map("canRead" -> canRead, "canWrite" -> canWrite, "canGrant" -> canGrant) / Segment) { (rights: UserRight, userId: String) =>
          post {
            dynamic {
              if (userId == userContext.login) {
                reject(ValidationRejection("You cannot change your access to story by yourself."))
              }
              else {
                complete((accessActor ? Share(storyId, userId, rights, userContext.login)) map { value => StatusCodes.OK })
              }
            }
          }
        } ~ 
        path(Segment) { userId: String =>
          delete {
            dynamic {
              if (userId == userContext.login) {
                reject(ValidationRejection("You cannot unshare a story from yourself."))
              }
              else {
                complete((accessActor ? Unshare(storyId, userId, userContext.login)) map { value => StatusCodes.OK })
              }
            }
          } 
        }
      }
    }
}