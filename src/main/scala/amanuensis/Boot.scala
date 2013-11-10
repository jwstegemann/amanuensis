package amanuensis

import akka.actor.{Props, Actor, ActorSystem, ActorLogging}
import akka.io.IO
import spray.can.Http

import amanuensis.services._

import amanuensis.auth.{SessionCookieAuth, UserContext}

import amanuensis.auth.SessionServiceActor
import amanuensis.auth.UserContextActor

import amanuensis.story.{StoryActor, StoryHttpService}

//BasicAuth

import scala.concurrent._
import spray.routing.authentication._
import amanuensis.auth._




class RootServiceActor extends Actor with ActorLogging with StaticHttpService with UserHttpService with StoryHttpService with SessionAware {

  def myUserPassAuthenticator(userPass: Option[UserPass]): Future[Option[UserContext]] = {
    Future {
      if (userPass.exists(up => up.user == "hallo" && up.pass == "welt")) Some(UserContext("TestUser", "","","",Nil))
      else None
    }
  }

  def actorRefFactory = context

  def receive = runRoute(
    userRoute ~ 
    staticRoute ~

    authenticate(BasicAuth(myUserPassAuthenticator _, realm = "Amanuensis")) { userName =>
      storyRoute(null)
    }
    

//    authenticate(SessionCookieAuth()(sessionServiceActor, context.dispatcher)) { userContext => //FIXME: make sessionServiceActor implicit again
//      storyRoute(userContext)
//    }
  )
}

object Boot extends App {
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("amanuensis")

  // create and start the session service
  val sessionService = system.actorOf(Props[SessionServiceActor], "sessionService")	
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
