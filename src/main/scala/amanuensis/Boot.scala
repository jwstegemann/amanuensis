package amanuensis

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import amanuensis.services.StaticHttpService
import spray.can.Http
import spray.routing.authentication.UserPass
import scala.language.postfixOps
import spray.routing.authentication.BasicAuth


import amanuensis.story._
import amanuensis.story.Story._
import spray.httpx.SprayJsonSupport

import spray.httpx.unmarshalling.Unmarshaller
import spray.httpx.marshalling.Marshaller


class RootServiceActor extends Actor with ActorLogging with SprayJsonSupport with StaticHttpService {

  def actorRefFactory = context

  def receive = runRoute(
    staticRoute ~


    pathPrefix("story") {
      path(LongNumber) { storyId : Long =>
        get {
          dynamic {
            log.debug(s"request: get details for story $storyId")
            complete(s"getting details for story $storyId")
          }
        } ~
        put {
          entity(as[Story]) { story =>
            dynamic {
              log.debug(s"request: update story $storyId with $story")
              complete(s"updating story $storyId with: $story")
            }
          }
        } ~
        delete {
          dynamic {
            log.debug(s"request: remove story $storyId")
            complete(s"removing story $storyId")
          }
        }
      } ~
      post {
        entity(as[Story]) { story =>
          dynamic {
            log.debug(s"request: creating new story with $story")
            complete(s"creating new story with $story")
          }
        }
      }

    }~

    path("slot" / LongNumber / Segment / LongNumber) { (sourceStoryId : Long, slotName : String, targetStoryId : Long) =>
      post {
        dynamic {
          log.debug(s"request: add slot for source: $sourceStoryId, slot: $slotName, target: $targetStoryId")
          complete(s"adding slot for source: $sourceStoryId, slot: $slotName, target: $targetStoryId")
        }
      } ~
      delete {
        dynamic {
          log.debug(s"request: remove slot for source: $sourceStoryId, slot: $slotName, target: $targetStoryId")
          complete(s"removing slot for source: $sourceStoryId, slot: $slotName, target: $targetStoryId")
        }
      }
    } ~

    path("tag" / LongNumber / Rest ) { (storyId : Long, tagName : String) =>
      post {
        dynamic {
          log.debug(s"request: add tag: $tagName to story: $storyId")
          complete(s"adding tag: $tagName to story: $storyId")
        }
      } ~
      delete {
        dynamic {
          log.debug(s"request: remove tag: $tagName from story: $storyId")
          complete(s"removing tag: $tagName from story: $storyId")
        }
      }
    }    

//    authenticate(BasicAuth(myUserPassAuthenticator _, realm = "Amanuensis")) { userContext =>
//      storyRoute(userContext) // ~
//      userRoute(userContext)
//    }
  )
}

object Boot extends App {
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("amanuensis")

  /*
   * Domain Actors 
   */  
  //val storyActor = system.actorOf(Props[StoryActor], "story")  

  /*
   * Web Actors
   */
  // create and start our routing service actors
  val rootService = system.actorOf(Props[RootServiceActor], "root-service")

  // create a new HttpServer using our handler and tell it where to bind to
   IO(Http) ! Http.Bind(rootService, interface = "localhost", port = 8080)
}
