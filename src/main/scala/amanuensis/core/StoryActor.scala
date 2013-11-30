package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.domain.Story
import amanuensis.core.util.Failable

import amanuensis.core.neo4j._


object StoryActor {

  case class Create(story: Story)
  case class Retrieve(storyId: String)
  case class Update(storyId: String, story: Story)
  case class Delete(storyId: String)

  val createQueryString = """CREATE (s:Story { id: {id}, title: {title}, content: {content} }) RETURN s.id"""
}

/**
 * Registers the users. Replies with
 */
class StoryActor extends Actor with ActorLogging with Failable with Neo4JJsonProtocol {

  import StoryActor._

  implicit def executionContext = context.dispatcher
  implicit val system = context.system

  final val server = CypherServer("http://localhost:7474/db/data/cypher")

	override def preStart =  {
    log.info(s"StoryActor started at: {}", self.path)
  }

  def receive = {
    case Create(story) => create(story) pipeTo sender
    case Retrieve(storyId) => sender ! retrieve(storyId)
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
    ) map {
      response => println(response)
        Created(id)
    }
//  	failWith(NotFoundException(Message("Testmessage",`ERROR`) :: Nil))
  }

  def retrieve(storyId: String) = {
  	Story(Some(storyId),"Testtitel","Testcontent")
  }

  def update(storyId: String, story: Story) = {
  	Story(Some(storyId),"Testtitel","Testcontent")
  }

  def delete(storyId: String) = {
  	Story(Some(storyId),"Testtitel","Testcontent")
  }
}