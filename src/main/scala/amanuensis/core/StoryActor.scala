package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.domain.Story
import amanuensis.core.util.Failable

import amanuensis.core.neo4j._
import amanuensis.core.util.StringUtils

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
  val createQueryString = """CREATE (s:Story { id: {id},title: {title},content: {content}, created: {created}, createdBy: {createdBy} }) RETURN s.id"""
  
  val retrieveStoryQueryString = """MATCH (s:Story) WHERE s.id={id} return s.id as id, s.title as title, s.content as content, s.created as created, s.createdBy as createdBy"""

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
  //ToDo: is this necessary?
  implicit val system = context.system
  def actorRefFactory = system

  final val server = CypherServer.default

  private val indexActor = actorRefFactory.actorSelection("/user/query")

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

    import QueryActor.Index

    if (story.id.nonEmpty) throw ValidationException(Message("A new story must not have an id.",`ERROR`) :: Nil)
    story.check

    val id = Neo4JId.generateId

    indexActor ! Index(story.copy(id = Some(id), content = StringUtils.truncate(story.content,250)))

    server.execute(createQueryString, 
      ("id" -> id), 
      ("title" -> story.title), 
      ("content" -> story.content),
      ("created" -> story.created),
      ("createdBy" -> story.createdBy)
    ) map { nothing => StoryInfo(id, story.title, story.created, None) }
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

    import QueryActor.Index

    if (storyId != story.id.getOrElse("")) throw ValidationException(Message("You cannot save story with id $story.id at id $storyId.",`ERROR`) :: Nil)

    story.check()

    indexActor ! Index(story.copy(content = StringUtils.truncate(story.content,250)))

  	server.execute(updateStoryQueryString, 
      ("id" -> storyId),
      ("title" -> story.title), 
      ("content" -> story.content)    
    )
  }

  def delete(storyId: String) = {

    import QueryActor.DeleteFromIndex

    indexActor ! Delete(storyId)

    server.execute(removeStoryQueryString, 
      ("id" -> storyId)
    )
  }

}