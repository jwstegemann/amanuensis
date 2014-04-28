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
  case class Like(storyId: String, login: String)
  case class Unlike(storyId: String, login: String)

  /*
   * query-string for neo4j
   */
  val likeQueryString = """
    MATCH (s:Story {id: {id}}), (u:User {login: {login}})
    WHERE (s)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    MERGE (s)<-[:likes]-(u)
    RETURN s.id
  """
  
  val unlikeQueryString = """
    MATCH (s:Story {id: {id}}), (u:User {login: {login}})
    WHERE (s)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    MATCH (s)<-[l:likes]-(u)
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
    case Like(storyId, login) => like(storyId, login) pipeTo sender
    case Unlike(storyId, login) => unlike(storyId, login) pipeTo sender
  }

  def like(storyId: String, login: String) = {

  	server.one[StoryId](likeQueryString, 
      ("id" -> storyId),
      ("login" -> login)
    ) map {
        case Some(s) => s
        case None => throw NotFoundException(Message(s"story with id '$storyId' could not be liked",`ERROR`) :: Nil)
      }    
  }

  def unlike(storyId: String, login: String) = {

    server.one[StoryId](unlikeQueryString, 
      ("id" -> storyId),
      ("login" -> login)
    ) map {
        case Some(s) => s
        case None => throw NotFoundException(Message(s"story with id '$storyId' could not be unliked",`ERROR`) :: Nil)
      }    
  }

}