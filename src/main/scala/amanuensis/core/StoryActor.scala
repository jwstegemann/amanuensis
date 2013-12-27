package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.domain.Story
import amanuensis.core.util.Failable

import amanuensis.core.neo4j._

import amanuensis.domain.{Story, StoryInfo, StoryContext, StoryProtocol}

import spray.httpx.SprayJsonSupport



object StoryActor extends Neo4JJsonProtocol {

  case class Create(story: Story)  
  case class Retrieve(storyId: String)
  case class Update(storyId: String, story: Story)
  case class Delete(storyId: String)

  val createQueryString = """CREATE (s:Story { id: {id},title: {title},content: {content} }) RETURN s.id"""
  val retrieveStoryQueryString = """MATCH (s:Story) WHERE s.id={id} return s.id as id, s.title as title, s.content as content"""

  implicit val storyNeo4JFormat = jsonCaseClassArrayFormat(Story)

}

/**
 * Registers the users. Replies with
 */
class StoryActor extends Actor with ActorLogging with Failable {

  import StoryActor._
  import StoryProtocol._

  implicit def executionContext = context.dispatcher
  implicit val system = context.system

  final val server = CypherServer("http://localhost:7474/db/data/cypher")

	override def preStart =  {
    log.info(s"StoryActor started at: {}", self.path)
  }

  def receive = {
    case Create(story) => create(story) pipeTo sender
    case Retrieve(storyId) => retrieve(storyId) pipeTo sender

    case Update(storyId, story) => sender ! update(storyId, story)
    case Delete(storyId) => sender ! delete(storyId)
  }

  def create(story: Story) = {
    // Todo: check for id not be present
    val id = Neo4JId.generateId

    server.execute(createQueryString, 
      ("id" -> id), 
      ("title" -> story.title), 
      ("content" -> story.content)
    ) map { response => 
      println(response) //FIXME: logging
      StoryInfo(id, story.title)
    }
//  	failWith(NotFoundException(Message("Testmessage",`ERROR`) :: Nil))
  }

  def retrieve(storyId: String) = {
    val story = server.one[Story](retrieveStoryQueryString, ("id" -> storyId))

  	story.map {
      case Some(s) => Some(StoryContext(s, Nil, Nil))
      case None => None
    }
  }

  def update(storyId: String, story: Story) = {
  	Story(Some(storyId),"Testtitel","Testcontent")
  }

  def delete(storyId: String) = {
  	Story(Some(storyId),"Testtitel","Testcontent")
  }
}