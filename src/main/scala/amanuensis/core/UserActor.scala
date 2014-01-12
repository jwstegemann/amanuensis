package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import scala.concurrent.Future

import spray.httpx.SprayJsonSupport
import spray.routing.authentication.UserPass
import spray.caching._

import amanuensis.core.neo4j._
import amanuensis.core.util.Converters
import amanuensis.domain.UserContext


object UserActor {
  case class CheckUser(userPassOption: Option[UserPass])

  val retrieveUserString = """MATCH (u:User) WHERE u.login={login} and u.pwd={pwd} RETURN u.login as login, u.name as name, u.permissions as permissions LIMIT 1"""
}

object UserNeoProtocol extends Neo4JJsonProtocol {
  implicit val userNeo4JFormat = jsonCaseClassArrayFormat(UserContext)
}

class UserActor extends Actor with ActorLogging with UsingParams with Neo4JJsonProtocol {

  implicit def executionContext = context.dispatcher
  implicit val system = context.system

  import UserActor._
  import UserNeoProtocol._

  final val server = CypherServer.default

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
        log.info("checking user {}...", userPass.user)
        
        userCache(userPass) {
          log.info("check neo4j for user {}...", userPass.user)

          server.one[UserContext](retrieveUserString, 
            ("login" -> userPass.user),
            ("pwd" -> Converters.sha(userPass.pass))
          )

        } pipeTo sender
      }
      case None => sender ! None
    }
  }

}