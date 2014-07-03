package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import scala.concurrent.Future

import spray.httpx.SprayJsonSupport
import spray.caching._

import amanuensis.core.neo4j._
import amanuensis.core.util.Converters
import amanuensis.domain.UserContext

import amanuensis.api.exceptions._

import amanuensis.domain.UserLogin
import amanuensis.domain.Message
import amanuensis.domain.Severities._



object UserActor {
  case class CheckUser(username: String, password: String)
  case class GetUserContext(username: String)
  case class ChangePassword(username: String, oldPwd: String, newPwd: String)

  //FIXME: change g:User to g:Group

  val retrieveUserString = """
    MATCH (u:User {login: {login}, pwd: {pwd}})-[:canRead|:canWrite|:canGrant*1..5]->(g:User)
    RETURN u.login as login, u.name as name, collect(g.login)+u.login as permissions, u.lang as lang LIMIT 1
  """

  val retrieveUserWithoutPasswordString = """
    MATCH (u:User {login: {login}})-[:canRead|:canWrite|:canGrant*1..5]->(g:User)
    RETURN u.login as login, u.name as name, collect(g.login)+u.login as permissions, u.lang as lang LIMIT 1
  """

  val changePasswordString = """
    MATCH (u:User {login: {login}, pwd: {oldPwd}})
    SET u.pwd = {newPwd}
    RETURN u.login LIMIT 1
  """

}

object UserNeoProtocol extends Neo4JJsonProtocol {
  implicit val userNeo4JFormat = jsonCaseClassArrayFormat(UserContext)
  implicit val loginNeo4JFormat = jsonCaseClassArrayFormat(UserLogin)
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
    case ChangePassword(username: String, oldPwd: String, newPwd: String) => changePassword(username, oldPwd, newPwd) pipeTo sender
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

  def changePassword(username: String, oldPwd: String, newPwd: String) = {
    log.info("changing password for {}...", username)
    
    server.one[UserLogin](changePasswordString, 
      ("login" -> username),
      ("oldPwd" -> Converters.sha(oldPwd)),
      ("newPwd" -> Converters.sha(newPwd))      
    ) map {
        case Some(s) => s
        case None => throw NotFoundException(Message(s"You cannot change this password.",`ERROR`) :: Nil)
      }   
  }


}