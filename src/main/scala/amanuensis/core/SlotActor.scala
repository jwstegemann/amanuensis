package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.domain.Story
import amanuensis.core.util.Failable

import amanuensis.core.neo4j._

import amanuensis.domain.{StoryInfo}


object SlotActor {

  case class Retrieve(storyId: String, slotName: String)
  case class Add(storyId: String, slotName: String, toStory: String)
  case class Removed(storyId: String, slotName: String, fromStory: String)

  val createQueryString = """CREATE (s:Story { id: {id}, title: {title}, content: {content} }) RETURN s.id"""
}

/**
 * Registers the users. Replies with
 */
class SlotActor extends Actor with ActorLogging with Failable with Neo4JJsonProtocol {

  import SlotActor._

  implicit def executionContext = context.dispatcher
  implicit val system = context.system

  final val server = CypherServer("http://localhost:7474/db/data/cypher")

	override def preStart =  {
    log.info(s"SlotActor started at: {}", self.path)
  }

  def receive = {
    case Retrieve(storyId, slotName) => sender ! retrieve(storyId, slotName)
  }


  def retrieve(storyId: String, slotName: String) = {
  	StoryInfo("abc","Testtitel") :: Nil
  }

}