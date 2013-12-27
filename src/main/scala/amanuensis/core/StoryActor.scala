package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.domain.Story
import amanuensis.core.util.Failable

import amanuensis.core.neo4j._

import amanuensis.domain.{Story, StoryInfo, StoryContext, StoryProtocol, Slot, SlotProtocol}

import spray.httpx.SprayJsonSupport

import scala.concurrent.Future



object StoryActor {

  case class Create(story: Story)  
  case class Retrieve(storyId: String)
  case class Update(storyId: String, story: Story)
  case class Delete(storyId: String)

  val createQueryString = """CREATE (s:Story { id: {id},title: {title},content: {content} }) RETURN s.id"""
  val retrieveStoryQueryString = """MATCH (s:Story) WHERE s.id={id} return s.id as id, s.title as title, s.content as content"""

  val retrieveOutSlotQueryString = """MATCH (s:Story)-[r]->(m:Story) WHERE s.id={id} return type(r) as name"""
  val retrieveInSlotQueryString = """MATCH (s:Story)<-[r]-(m:Story) WHERE s.id={id} return type(r) as name"""

  val removeStoryQueryString = """ """
}


object StoryNeoProtocol extends Neo4JJsonProtocol {
  implicit val storyNeo4JFormat = jsonCaseClassArrayFormat(Story)
  implicit val slotNeo4JFormat = jsonCaseClassArrayFormat(Slot)
}

/**
 * Registers the users. Replies with
 */
class StoryActor extends Actor with ActorLogging with Failable with UsingParams with Neo4JJsonProtocol {

  import StoryActor._
  import StoryNeoProtocol._

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
    println("creating " + story)

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

    val paramList: Param = ("id" -> storyId)

    for {
      story <- server.one[Story](retrieveStoryQueryString, paramList) map {
        case Some(s) => s
        case None => throw NotFoundException(Message(s"story with id '$storyId' could not be found",`ERROR`) :: Nil)
      }
      //FIXME: allow simple strings here as a result!
      inSlots <- server.list[Slot](retrieveInSlotQueryString, paramList) 
      outSlots <- server.list[Slot](retrieveOutSlotQueryString, paramList) 
    } yield StoryContext(story,inSlots,outSlots)

  }

  def update(storyId: String, story: Story) = {
  	Story(Some(storyId),"Testtitel","Testcontent")
  }

  def delete(storyId: String) = {
  	
  }
}