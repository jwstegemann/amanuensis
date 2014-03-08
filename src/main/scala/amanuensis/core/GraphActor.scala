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

  case class FindPaths(sourceStoryId: String, targetStoryId: String, tagName: String, page: Int, login: String)

  val pathQueryString = """
    MATCH (s:Story {id: {source}})
    MATCH (t:Story {id: {target}})
    MATCH (s)-[:Slot*1..5]-(m:Story)-[:Slot*1..5]-(t)
    WITH distinct m
    MATCH (m)-[:is]->(:Tag {name: {tagName}})
    WITH m
    MATCH (m)<-[:canRead|:canWrite|:canGrant]-(u)
    RETURN m.id, m.title, m.content, m.created, m.createdBy SKIP {skip} LIMIT 25
  """
}


object GraphNeoProtocol extends Neo4JJsonProtocol {
  implicit val storyNodeNeo4JFormat = jsonCaseClassArrayFormat(StoryNode)
}

/**
 * Performs graph queries
 */
class GraphActor extends Actor with ActorLogging with Failable with Neo4JJsonProtocol {

  import GraphActor._
  import GraphNeoProtocol._

  implicit def executionContext = context.dispatcher
  implicit val system = context.system
  def actorRefFactory = system

  final val server = CypherServer.default

  override def preStart =  {
    log.info(s"GraphActor started at: {}", self.path)
  }

  def receive = {
    case FindPaths(sourceStoryId, targetStoryId, tagName, page, login) => findPaths(sourceStoryId, targetStoryId, tagName, page, login) pipeTo sender
  }

  def findPaths(sourceStoryId: String, targetStoryId: String, tagName: String, page: Int, login: String) = {
    server.list[StoryNode](pathQueryString,
      ("source" -> sourceStoryId),
      ("target" -> targetStoryId),
      ("tagName" -> tagName),
      ("skip" -> (page*25)),
      ("login" -> login)
    )
  }

}