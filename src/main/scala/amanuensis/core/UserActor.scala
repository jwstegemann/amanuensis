package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import scala.concurrent.Future

import spray.httpx.SprayJsonSupport
import spray.routing.authentication.UserPass
import spray.caching._

import amanuensis.core.neo4j._


case class UserContext(name: String)

object UserActor {
  case class CheckUser(userPassOption: Option[UserPass])
}

class UserActor extends Actor with ActorLogging with UsingParams with Neo4JJsonProtocol {

  implicit def executionContext = context.dispatcher
  implicit val system = context.system

  import UserActor._

  val userCache : Cache[Option[UserContext]]  = LruCache() //TODO: set parameters

  override def preStart =  {
    log.info(s"UserActor started at: {}", self.path)
  }

  def receive = {
    case CheckUser(userPassOption) => checkUser(userPassOption)
  }

  def checkUser(userPassOption: Option[UserPass]) = {
    userPassOption match {
      case Some(userPass) => {
        log.info("checking user {} with {}...", userPass.user, userPass.pass)
        
        userCache(userPass) {
          log.info("check db for user {}...", userPass.user, userPass.pass)

          if (userPass.user == "hallo" && userPass.pass == "welt07541") {
            Some(UserContext("Alfred Tetzlaw"))
          }
          else {
            None
          }
        } pipeTo sender
      }
      case None => sender ! None
    }
  }

}