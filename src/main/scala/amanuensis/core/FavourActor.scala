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


object FavourActor {

  /*
   * messages to be used with this actor
   */
  case class Star(storyId: String, login: String)
  case class Unstar(storyId: String, login: String)
  case class Due(storyId: String, date: String, login: String)
  case class Undue(storyId: String, login: String)  

  /*
   * query-string for neo4j
   */
  val starQueryString = """
    MATCH (s:Story {id: {id}}), (u:User {login: {login}})
    WHERE (s)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    MERGE (s)<-[:stars]-(u)
    RETURN s.id
  """
  
  val unstarQueryString = """
    MATCH (s:Story {id: {id}}), (u:User {login: {login}})
    WHERE (s)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    MATCH (s)<-[l:stars]-(u)
    DELETE l
    RETURN s.id
  """

  val dueQueryString = """
    MATCH (s:Story {id: {id}}), (u:User {login: {login}})
    WHERE (s)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    MERGE (s)<-[d:due]-(u)
    SET d.date = {date}
    RETURN s.id
  """
  
  val undueQueryString = """
    MATCH (s:Story {id: {id}}), (u:User {login: {login}})
    WHERE (s)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    MATCH (s)<-[l:due]-(u)
    DELETE l
    RETURN s.id
  """
}

/**
 * Registers the users. Replies with
 */
class FavourActor extends Actor with ActorLogging with Failable with UsingParams with Neo4JJsonProtocol {

  import FavourActor._
  import StoryNeoProtocol._

  implicit def executionContext = context.dispatcher
  //ToDo: is this necessary?
  implicit val system = context.system
  def actorRefFactory = system

  final val server = CypherServer.default

	override def preStart =  {
    log.info(s"FavourActor started at: {}", self.path)
  }

  def receive = {
    case Star(storyId, login) => star(storyId, login) pipeTo sender
    case Unstar(storyId, login) => unstar(storyId, login) pipeTo sender
    case Due(storyId, date, login) => due(storyId, date, login) pipeTo sender
    case Undue(storyId, login) => undue(storyId, login) pipeTo sender
  }

  def star(storyId: String, login: String) = {

  	server.one[StoryId](starQueryString, 
      ("id" -> storyId),
      ("login" -> login)
    ) map {
        case Some(s) => s
        case None => throw NotFoundException(Message(s"story with id '$storyId' could not be starred",`ERROR`) :: Nil)
      }    
  }

  def unstar(storyId: String, login: String) = {

    server.one[StoryId](unstarQueryString, 
      ("id" -> storyId),
      ("login" -> login)
    ) map {
        case Some(s) => s
        case None => throw NotFoundException(Message(s"story with id '$storyId' could not be unstarred",`ERROR`) :: Nil)
      }    
  }

  def due(storyId: String, date: String, login: String) = {

    server.one[StoryId](dueQueryString, 
      ("id" -> storyId),
      ("login" -> login),
      ("date" -> date)
    ) map {
        case Some(s) => s
        case None => throw NotFoundException(Message(s"story with id '$storyId' could not be dued",`ERROR`) :: Nil)
      }    
  }

  def undue(storyId: String, login: String) = {

    server.one[StoryId](undueQueryString, 
      ("id" -> storyId),
      ("login" -> login)
    ) map {
        case Some(s) => s
        case None => throw NotFoundException(Message(s"story with id '$storyId' could not be undued",`ERROR`) :: Nil)
      }    
  }  

}