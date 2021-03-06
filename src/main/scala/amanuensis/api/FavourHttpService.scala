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

import amanuensis.core.FavourActor._
import amanuensis.domain.{Story, StoryInfo, StoryContext, StoryProtocol, UserContext, StoryAccess}

import StatusCode._


trait FavourHttpService extends HttpService with SprayJsonSupport { 

  import StoryProtocol._

  private val favourActor = actorRefFactory.actorSelection("/user/favour")

  private implicit val timeout = new Timeout(5 seconds)
  private implicit def executionContext = actorRefFactory.dispatcher


  def favourRoute(userContext: UserContext) =
    path("star" / Segment) { storyId: String =>
      post {
        dynamic {
          complete((favourActor ? Star(storyId, userContext.login)) map { value => StatusCodes.OK })
        }
      } ~
      delete {
        dynamic {
          complete((favourActor ? Unstar(storyId, userContext.login)) map { value => StatusCodes.OK })
        }
      }
    } ~ 
    path("due" / Segment) { storyId: String =>
      post {
        parameter("date".as[String] ? "") { date =>
          dynamic {
            complete((favourActor ? Due(storyId, date, userContext.login)) map { value => StatusCodes.OK })
          }
        }
      } ~
      delete {
        dynamic {
          complete((favourActor ? Undue(storyId, userContext.login)) map { value => StatusCodes.OK })
        }
      }
    }    
}