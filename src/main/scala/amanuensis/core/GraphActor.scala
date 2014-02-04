package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.domain.Story
import amanuensis.core.util.Failable

import amanuensis.core.neo4j._

import amanuensis.domain._


object GraphActor {

  case class FindPaths(request: FindPathsRequest)

  val pathQueryString = """MATCH (s:Story {id: {story}})
    MATCH (s)-[r:Slot {name: {slot}}]-(:Story)
    WITH s, count(*) as weight
    MATCH (s)-[r:Slot {name: {slot}}]-(m:Story)
    WITH m, weight,
      (CASE
        WHEN weight < 5 THEN m.content
        ELSE null 
      END) as content
    RETURN m.id, m.title, m.created, content"""
}

/**
 * Registers the users. Replies with
 */
class GraphActor extends Actor with ActorLogging with Failable with Neo4JJsonProtocol {

  import GraphActor._
  import StoryNeoProtocol._

  implicit def executionContext = context.dispatcher
  implicit val system = context.system
  def actorRefFactory = system

  final val server = CypherServer.default

  override def preStart =  {
    log.info(s"GraphActor started at: {}", self.path)
  }

  def receive = {
    case FindPaths(request) => findPaths(request) pipeTo sender
  }

  def findPaths(request: FindPathsRequest) = {
    server.list[StoryInfo](pathQueryString
//      ("story" -> storyId),
//      ("slot" -> slotName)
    )
  }

}