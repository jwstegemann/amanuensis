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
  case class Remove(storyId: String, slotName: String, fromStory: String)

  val retrieveQueryString = """MATCH (s:Story)-[{slot}]-(m:Story) WHERE s.id={id} RETURN m.id as id, m.title as title"""
}

/**
 * Registers the users. Replies with
 */
class SlotActor extends Actor with ActorLogging with Failable with Neo4JJsonProtocol {

  import SlotActor._
  import StoryNeoProtocol._

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
  	server.list[StoryInfo](retrieveQueryString,
      ("id" -> storyId),
      ("slot" -> slotName)
    )
  }

}