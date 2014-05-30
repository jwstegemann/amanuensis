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
import amanuensis.core.elasticsearch.{QueryResult, Hits, Facets, Hit, Tags, Dates}


object GraphActor {

  case class FindPaths(sourceStoryId: String, targetStoryId: String, tagName: String, page: Int, login: String)
  case class FindFavourites(page: Int, login: String)
  case class FindToDos(page: Int, login: String)

  val pathQueryString = """
    MATCH (s:Story {id: {source}})
    MATCH (t:Story {id: {target}})
    MATCH (s)-[:Slot*1..5]-(m:Story)-[:Slot*1..5]-(t)
    WITH distinct m
    MATCH (m)-[:is]->(:Tag {name: {tagName}})
      WHERE (m)<-[:canRead|:canWrite|:canGrant*1..5]-(:User {login: {login}})
    RETURN m.id, m.title, m.content, m.created, m.createdBy, m.icon SKIP {skip} LIMIT 25
  """

  val favouritesQueryString = """
    MATCH (u:User {login: {login}})-[:stars]->(s:Story)
      WHERE (s)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    RETURN s.id, s.title, s.content, s.created, s.createdBy, s.modified, s.modifiedBy, null, [], s.icon
    ORDER BY s.modified DESC SKIP {skip} LIMIT 25
  """

  val toDosQueryString = """
    MATCH (u:User {login: {login}})-[d:due]->(s:Story)
    WHERE (s)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    RETURN s.id, s.title, s.content, s.created, s.createdBy, s.modified, s.modifiedBy, d.date, [], s.icon
    ORDER BY d.date SKIP {skip} LIMIT 25
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
  import StoryNeoProtocol._

  implicit def executionContext = context.dispatcher
  implicit val system = context.system
  def actorRefFactory = system

  final val server = CypherServer.default

  override def preStart =  {
    log.info(s"GraphActor started at: {}", self.path)
  }

  def receive = {
    case FindPaths(sourceStoryId, targetStoryId, tagName, page, login) => findPaths(sourceStoryId, targetStoryId, tagName, page, login) pipeTo sender
    case FindFavourites(page, login) => findFavourites(page, login) pipeTo sender
    case FindToDos(page, login) => findToDos(page, login) pipeTo sender
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

  def findFavourites(page: Int, login: String) = {
    server.list[Story](favouritesQueryString,
      ("skip" -> (page*25)),
      ("login" -> login)
    ) map { list =>
      QueryResult(
        0,
        Hits(-1, 0.0, list map { story =>
            Hit(story.id.getOrElse("unknown"), 0.0, story)
          }),
        Facets(Tags(0, Nil), Dates(Nil))
      )      
    }
  }  

  def findToDos(page: Int, login: String) = {
    server.list[Story](toDosQueryString,
      ("skip" -> (page*25)),
      ("login" -> login)
    ) map { list =>
      QueryResult(
        0,
        Hits(-1, 0.0, list map { story =>
            Hit(story.id.getOrElse("unknown"), 0.0, story)
          }),
        Facets(Tags(0, Nil), Dates(Nil))
      )      
    }
  } 

}