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
import amanuensis.auth.CheckUserMsg
import amanuensis.auth.UserContext
import amanuensis.auth.UserContextActor
import amanuensis.services.SessionAware
import amanuensis.services.StaticHttpService
import amanuensis.services.UserHttpService
import amanuensis.story.StoryActor
import amanuensis.story.StoryHttpService
import spray.can.Http
import spray.routing.authentication.UserPass
import scala.language.postfixOps
import spray.routing.authentication.BasicAuth


class RootServiceActor extends Actor with ActorLogging with StaticHttpService with UserHttpService with StoryHttpService with SessionAware {

//  val userContextActor = actorRefFactory.actorSelection("user/userContext")

  private implicit val timeout = new Timeout(2 seconds)

  def myUserPassAuthenticator(userPassOption: Option[UserPass]): Future[Option[UserContext]] = {
      (userContextActor ? new CheckUserMsg(userPassOption)).mapTo[Option[UserContext]]
  }

  def actorRefFactory = context

  def receive = runRoute(
    staticRoute ~

    authenticate(BasicAuth(myUserPassAuthenticator _, realm = "Amanuensis")) { userContext =>
      storyRoute(userContext) ~
      userRoute(userContext)
    }
    

//    authenticate(SessionCookieAuth()(sessionServiceActor, context.dispatcher)) { userContext => //FIXME: make sessionServiceActor implicit again
//      storyRoute(userContext)
//    }
  )
}

object Boot extends App {
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("amanuensis")

  // create and start the userContext service
  val userContext = system.actorOf(Props[UserContextActor], "userContext")  

  /*
   * Domain Actors 
   */  
  val storyActor = system.actorOf(Props[StoryActor], "story")  

  /*
   * Web Actors
   */
  // create and start our routing service actors
  val rootService = system.actorOf(Props[RootServiceActor], "root-service")

  // create a new HttpServer using our handler and tell it where to bind to
   IO(Http) ! Http.Bind(rootService, interface = "localhost", port = 8080)
}
