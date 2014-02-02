package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import scala.concurrent.Future

import spray.httpx.SprayJsonSupport
import spray.caching._

import amanuensis.core.neo4j._
import amanuensis.core.util.Converters
import amanuensis.domain.UserContext


object UserActor {
  case class CheckUser(username: String, password: String)
  case class GetUserContext(username: String)

  val retrieveUserString = """MATCH (u:User) WHERE u.login={login} and u.pwd={pwd} RETURN u.login as login, u.name as name, u.permissions as permissions LIMIT 1"""
  val retrieveUserWithoutPasswordString = """MATCH (u:User) WHERE u.login={login} RETURN u.login as login, u.name as name, u.permissions as permissions LIMIT 1"""
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

  val userCache : Cache[UserContext]  = LruCache() //TODO: set parameters

  override def preStart =  {
    log.info(s"UserActor started at: {}", self.path)
  }

  def receive = {
    case CheckUser(username: String, password: String) => checkUser(username, password) pipeTo sender
    case GetUserContext(username: String) => getUserContext(username) pipeTo sender
  }

  def checkUser(username: String, password: String) = {
    log.info("checking user {}...", username)
    
    userCache(username) {
      log.info("check neo4j for user {}...", username)

      server.one[UserContext](retrieveUserString, 
        ("login" -> username),
        ("pwd" -> Converters.sha(password))
      ).map (_.get)
    }
  }

  def getUserContext(username: String) = {
    userCache(username) {
      log.info("get user {} from neo4j...", username)

      server.one[UserContext](retrieveUserWithoutPasswordString, 
        ("login" -> username)
      ).map (_.get)
    }
  }

}