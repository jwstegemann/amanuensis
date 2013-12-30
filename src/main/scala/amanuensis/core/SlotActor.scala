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

  case class List(storyId: String, slotName: String)
  case class Add(toStory: String, storyId: String, slotName: String)
  case class Remove(fromStory: String, storyId: String, slotName: String)

  val retrieveQueryString = """MATCH (s:Story)-[r:Slot]-(m:Story) WHERE s.id={id} and r.name={slot} RETURN m.id as id, m.title as title"""
  val addQueryString = """MATCH (n:Story),(m:Story) WHERE n.id={toStory} and m.id={story} CREATE (n)-[r:Slot]->(m) set r.name={slot}"""
  val removeQueryString = """MATCH (n:Story)-[r:Slot]->(m:Story) WHERE n.id={fromStory} and m.id={story} and r.name={slot} DELETE r"""
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
    case List(storyId, slotName) => list(storyId, slotName) pipeTo sender
    case Add(toStory, storyId, slotName) => add(toStory, storyId, slotName) pipeTo sender
    case Remove(toStory, storyId, slotName) => remove(toStory, storyId, slotName) pipeTo sender
  }

  def list(storyId: String, slotName: String) = {
  	server.list[StoryInfo](retrieveQueryString,
      ("id" -> storyId),
      ("slot" -> slotName)
    )
  }

  def add(toStory: String, slotName: String, storyId: String) = {
    server.execute(addQueryString, 
      ("toStory" -> toStory),
      ("slot" -> slotName), 
      ("story" -> storyId)
    )
  }

  def remove(fromStory: String, slotName: String, storyId: String) = {
    server.execute(removeQueryString, 
      ("fromStory" -> fromStory),
      ("slot" -> slotName), 
      ("story" -> storyId)
    )
  }

}