package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.domain.Story
import amanuensis.core.util.Failable

import amanuensis.core.neo4j._
import amanuensis.core.util.StringUtils

import amanuensis.domain._

import spray.httpx.SprayJsonSupport

import scala.concurrent.Future
import amanuensis.core.elasticsearch._
import amanuensis.domain.UserRights._

object AccessActor {

  /*
   * messages to be used with this actor
   */

  case class RetrieveAccess(storyId: String, login: String)
  
  case class Share(storyId: String, userId: String, rights: UserRight, login: String)
  case class Unshare(storyId: String, userId: String, login: String)


  /*
   * query-string for neo4j
   */

  val retrieveAccessQueryString = """
    MATCH (s:Story {id: {storyId}}), (l:User {login: {login}})
    WHERE (s)<-[:canRead|:canWrite|:canGrant*1..5]-(l)
    MATCH (s)<-[r:canRead|:canWrite|:canGrant]-(u:User)
    RETURN u.login,u.name,type(r),
       (CASE
        WHEN u.login = 'public' THEN 'public'
        WHEN u:Group THEN 'group'
        ELSE 'user' 
      END) as content
  """
  
  val shareReadOnlyQueryString = """
    MATCH (s:Story {id: {storyId}}), (l:User {login: {login}})
    WHERE (s)<-[:canGrant*1..5]-(l) 
    MATCH (u:User {login: {userId}})
    OPTIONAL MATCH (s)<-[r:canWrite | :canGrant]-(u)
    DELETE (r)
    CREATE UNIQUE (s)<-[:canRead]-(u)
    RETURN s.id
  """

  val shareReadWriteQueryString = """
    MATCH (s:Story {id: {storyId}}), (l:User {login: {login}})
    WHERE (s)<-[:canGrant*1..5]-(l) 
    MATCH (u:User {login: {userId}})
    OPTIONAL MATCH (s)<-[r:canRead | :canGrant]-(u)
    DELETE (r)
    CREATE UNIQUE (s)<-[:canWrite]-(u)
    RETURN s.id
  """

  val shareReadWriteGrantQueryString = """
    MATCH (s:Story {id: {storyId}}), (l:User {login: {login}})
    WHERE (s)<-[:canGrant*1..5]-(l) 
    MATCH (u:User {login: {userId}})
    OPTIONAL MATCH (s)<-[r:canRead | :canWrite]-(u)
    DELETE (r)
    CREATE UNIQUE (s)<-[:canGrant]-(u)
    RETURN s.id
  """


  val unshareQueryString = """
    MATCH (s:Story {id: {storyId}}), (l:User {login: {login}})
    WHERE (s)<-[:canGrant*1..5]-(l) 
    MATCH (u:User {login: {userId}})
    MATCH (s)<-[r:canRead | :canWrite | :canGrant]-(u)
    DELETE r
    RETURN s.id
  """
}


/**
 * Registers the users. Replies with
 */
class AccessActor extends Actor with ActorLogging with Failable with UsingParams with Neo4JJsonProtocol {

  import AccessActor._
  import StoryNeoProtocol._

  implicit def executionContext = context.dispatcher
  //ToDo: is this necessary?
  implicit val system = context.system
  def actorRefFactory = system

  final val elastic_server = ElasticSearchServer.default
  final val server = CypherServer.default


  override def preStart =  {
    log.info(s"AccessActor started at: {}", self.path)
  }

  def receive = {

    case RetrieveAccess(storyId, login) => retrieveAccess(storyId, login) pipeTo sender
    
    case Share(storyId, userId, UserRights.canRead, login) => {
      chmod(shareReadOnlyQueryString, storyId, userId, login) pipeTo sender
      elastic_server.changeReadAccess(storyId, userId, true)
    }

    case Share(storyId, userId, UserRights.canWrite, login) => {
      chmod(shareReadWriteQueryString, storyId, userId, login) pipeTo sender
      elastic_server.changeReadAccess(storyId, userId, true)
    }

    case Share(storyId, userId, UserRights.canGrant, login) => {
      chmod(shareReadWriteGrantQueryString, storyId, userId, login) pipeTo sender
      elastic_server.changeReadAccess(storyId, userId, true)
    }

    case Unshare(storyId, userId, login) => {
      chmod(unshareQueryString, storyId, userId, login) pipeTo sender
      elastic_server.changeReadAccess(storyId, userId, false)
    }
  }


  def retrieveAccess(storyId: String, login: String) = {
    server.list[StoryAccess](retrieveAccessQueryString, 
      ("storyId" -> storyId), 
      ("login" -> login))    
  }


  def chmod(queryString: String, storyId: String, userId: String, login: String) = {

    server.one[StoryId](queryString, 
      ("storyId" -> storyId),
      ("userId" -> userId), 
      ("login" -> login)
    ) map {
        case Some(s) => s
        case None => throw NotFoundException(Message(s"access-rights for story with id '$storyId' could not be changed.",`ERROR`) :: Nil)
      } 
  }

}