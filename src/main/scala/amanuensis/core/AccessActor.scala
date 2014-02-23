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


object AccessActor {

  /*
   * messages to be used with this actor
   */

  case class RetrieveAccess(storyId: String, login: String)
  
  case class AddReadAccess(storyId: String, userId: String, login: String)
  case class RemoveReadAccess(storyId: String, userId: String, login: String)

  case class AddWriteAccess(storyId: String, userId: String, login: String)
  case class RemoveWriteAccess(storyId: String, userId: String, login: String)

  /*
   * query-string for neo4j
   */

  val retrieveAccessQueryString = """
    MATCH (l:User {login: {login}})-[:canWrite]->(s:Story {id: {storyId}})
    MATCH (s)<-[r:canRead | :canWrite]-(u:User)
    RETURN u.login,u.name,collect(type(r))
  """
  
  val addReadAccessQueryString = """
    MATCH (l:User {login: {login}})-[:canWrite]->(s:Story {id: {storyId}}), (u:User {login: {userId}})
    CREATE UNIQUE (s)<-[:canRead]-(u)
    RETURN s.id
  """

  val removeReadAccessQueryString = """
    MATCH (l:User {login: {login}})-[:canWrite]->(s:Story {id: {storyId}})
    MATCH (s)<-[r:canRead]-(u:User {login: {userId}})
    WITH r, s.id as id
    DELETE r
    RETURN id
  """

  val addWriteAccessQueryString = """
    MATCH (l:User {login: {login}})-[:canWrite]->(s:Story {id: {storyId}}), (u:User {login: {userId}})
    CREATE UNIQUE (s)<-[:canWrite]-(u)
    RETURN s.id
  """

  val removeWriteAccessQueryString = """
    MATCH (l:User {login: {login}})-[:canWrite]->(s:Story {id: {storyId}})
    MATCH (s)<-[r:canWrite]-(u:User {login: {userId}})
    WITH r, s.id as id
    DELETE r
    RETURN id
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
    
    case AddReadAccess(storyId, userId, login) => {
      chmod(addReadAccessQueryString, storyId, userId, login) pipeTo sender
      elastic_server.changeReadAccess(storyId, userId, true)
    }

    case RemoveReadAccess(storyId, userId, login) => {
      chmod(removeReadAccessQueryString, storyId, userId, login) pipeTo sender
      elastic_server.changeReadAccess(storyId, userId, false)
    }

    case AddWriteAccess(storyId, userId, login) => {
      chmod(addWriteAccessQueryString, storyId, userId, login) pipeTo sender
    }

    case RemoveWriteAccess(storyId, userId, login) => {
      chmod(removeWriteAccessQueryString, storyId, userId, login) pipeTo sender
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