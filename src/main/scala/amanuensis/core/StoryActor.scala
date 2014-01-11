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

  /*
   * messages to be used with this actor
   */
  case class Create(story: Story)  
  case class Retrieve(storyId: String)
  case class Update(storyId: String, story: Story)
  case class Delete(storyId: String)

  /*
   * query-string for neo4j
   */
  //TODO: use one param of story-object
  //TODO: use merge when creating stories
  val createQueryString = """CREATE (s:Story { id: {id},title: {title},content: {content} }) RETURN s.id"""
  
  val retrieveStoryQueryString = """MATCH (s:Story) WHERE s.id={id} return s.id as id, s.title as title, s.content as content"""

  val retrieveOutSlotQueryString = """MATCH (s:Story)-[r:Slot]->() WHERE s.id={id} RETURN DISTINCT r.name as name"""
  val retrieveInSlotQueryString = """MATCH (s:Story)<-[r:Slot]-() WHERE s.id={id} RETURN DISTINCT r.name as name"""

  val removeStoryQueryString = """MATCH (s:Story) WHERE s.id={id} WITH s OPTIONAL MATCH s-[r]-() DELETE r,s"""

  val updateStoryQueryString = """MATCH (s:Story) WHERE s.id={id} SET s.title={title}, s.content={content}"""
}


object StoryNeoProtocol extends Neo4JJsonProtocol {
  implicit val storyNeo4JFormat = jsonCaseClassArrayFormat(Story)
  implicit val slotNeo4JFormat = jsonCaseClassArrayFormat(Slot)
  implicit val storyInfoNeo4JFormat = jsonCaseClassArrayFormat(StoryInfo)
}

/**
 * Registers the users. Replies with
 */
class StoryActor extends Actor with ActorLogging with Failable with UsingParams with Neo4JJsonProtocol {

  import StoryActor._
  import StoryNeoProtocol._

  implicit def executionContext = context.dispatcher
  implicit val system = context.system

  final val server = CypherServer.default

	override def preStart =  {
    log.info(s"StoryActor started at: {}", self.path)
  }

  def receive = {
    case Create(story) => create(story) pipeTo sender
    case Retrieve(storyId) => retrieve(storyId) pipeTo sender
    case Update(storyId, story) => sender ! update(storyId, story)
    case Delete(storyId) => delete(storyId) pipeTo sender
  }

  def create(story: Story) = {

    if (story.id.nonEmpty) throw ValidationException(Message("A new story must not have an id.",`ERROR`) :: Nil)
    story.check

    val id = Neo4JId.generateId

    server.execute(createQueryString, 
      ("id" -> id), 
      ("title" -> story.title), 
      ("content" -> story.content)
    ) map { nothing => StoryInfo(id, story.title) }
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

    if (storyId != story.id.getOrElse("")) throw ValidationException(Message("You cannot save story with id $story.id at id $storyId.",`ERROR`) :: Nil)

    story.check()

  	server.execute(updateStoryQueryString, 
      ("id" -> storyId),
      ("title" -> story.title), 
      ("content" -> story.content)
    )
  }

  def delete(storyId: String) = {
    server.execute(removeStoryQueryString, 
      ("id" -> storyId)
    )
  }

}