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

import amanuensis.domain._

import spray.httpx.SprayJsonSupport

import scala.concurrent.Future


object StoryActor {

  /*
   * messages to be used with this actor
   */
  case class Create(story: Story, login: String)  
  case class Retrieve(storyId: String, login: String)
  case class Update(storyId: String, story: Story, login: String)
  case class Delete(storyId: String, login: String)

  /*
   * query-string for neo4j
   */
  //TODO: use one param of story-object
  //TODO: use merge when creating stories
  val createQueryString = """
    MATCH (u:User {login: {login}})
    CREATE (s:Story { id: {id},title: {title},content: {content}, created: {created}, createdBy: {createdBy} })<-[:canGrant]-(u)
    WITH s
    FOREACH (tagname IN {tags} |
      MERGE (t:Tag {name: tagname})
      MERGE (s)-[:is]->(t:Tag))
    RETURN s.id
  """
  
  val retrieveStoryQueryString = """
    MATCH (s:Story {id: {id}})<-[:canRead|:canWrite|:canGrant]-(:User {login: {login}})
    OPTIONAL MATCH (s)-[:is]->(t:Tag)
    RETURN s.id as id, s.title as title, s.content as content, s.created as created, s.createdBy as createdBy, collect(t.name) as tags
  """

  val retrieveOutSlotQueryString = """
    MATCH (s:Story)-[r:Slot]->()<-[:canRead|:canWrite|:canGrant]-(:User {login: {login}})
    WHERE s.id={id} RETURN DISTINCT r.name as name LIMIT 250
  """

  val retrieveInSlotQueryString = """
    MATCH (s:Story)<-[r:Slot]-()<-[:canRead|:canWrite|:canGrant]-(:User {login: {login}}) 
    WHERE s.id={id} RETURN DISTINCT r.name as name LIMIT 250
  """

  val removeStoryQueryString = """
    MATCH (s:Story {id: {id}})<-[:canWrite|:canGrant]-(:User {login: {login}})
    WITH s.id as id, s
    OPTIONAL MATCH s-[r]-() 
    DELETE r,s
    RETURN id
  """

  val updateStoryQueryString = """
    MATCH (s:Story {id: {id}})<-[:canWrite|:canGrant]-(:User {login: {login}})
    OPTIONAL MATCH (s)-[r:is]->(:Tag) DELETE r
    SET s.title={title}, s.content={content}
    FOREACH (tagname IN {tags} |
      MERGE (t:Tag {name: tagname})
      MERGE (s)-[:is]->(t:Tag))
    RETURN s.id
  """
}


object StoryNeoProtocol extends Neo4JJsonProtocol {
  implicit val storyNeo4JFormat = jsonCaseClassArrayFormat(Story)
  implicit val slotNeo4JFormat = jsonCaseClassArrayFormat(Slot)
  implicit val storyInfoNeo4JFormat = jsonCaseClassArrayFormat(StoryInfo)
  implicit val storyIdNeo4JFormat = jsonCaseClassArrayFormat(StoryId)
  implicit val storyAccessNeo4JFormat = jsonCaseClassArrayFormat(StoryAccess)
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
    case Create(story, login) => create(story, login) pipeTo sender
    case Retrieve(storyId, login) => retrieve(storyId, login) pipeTo sender
    case Update(storyId, story, login) => update(storyId, story, login) pipeTo sender
    case Delete(storyId, login) => delete(storyId, login) pipeTo sender

  }

  def create(story: Story, login: String) = {

    import QueryActor.Index

    if (story.id.nonEmpty) failWith(ValidationException(Message("A new story must not have an id.",`ERROR`) :: Nil))
    story.check

    val id = Neo4JId.generateId

    server.execute(createQueryString, 
      ("id" -> id), 
      ("title" -> story.title), 
      ("content" -> story.content),
      ("created" -> story.created),
      ("createdBy" -> story.createdBy),
      ("tags" -> story.tags),
      ("login" -> login)
    ) map { nothing =>
        indexActor ! Index(story.copy(id = Some(id)), login :: Nil)
        StoryInfo(id, story.title, story.created, None) 
      }
  }

  def retrieve(storyId: String, login: String) = {

    for {
      story <- server.one[Story](retrieveStoryQueryString, 
        ("id" -> storyId), 
        ("login" -> login)) map {
          case Some(s) => s
          case None => throw NotFoundException(Message(s"story with id '$storyId' could not be found",`ERROR`) :: Nil)
        }
      //FIXME: allow simple strings here as a result!
      inSlots <- server.list[Slot](retrieveInSlotQueryString, 
        ("id" -> storyId),
        ("login" -> login)) 
      outSlots <- server.list[Slot](retrieveOutSlotQueryString, 
        ("id" -> storyId),
        ("login" -> login)) 
    } yield StoryContext(story,inSlots,outSlots)

  }

  def update(storyId: String, story: Story, login: String) = {

    import QueryActor.UpdateIndex

    if (storyId != story.id.getOrElse("")) failWith(ValidationException(Message("You cannot save story with id $story.id at id $storyId.",`ERROR`) :: Nil))
    story.check()

  	server.one[StoryId](updateStoryQueryString, 
      ("id" -> storyId),
      ("title" -> story.title), 
      ("content" -> story.content),
      ("tags" -> story.tags),
      ("login" -> login)
    ) map {
        case Some(s) => indexActor ! UpdateIndex(story)
        case None => throw NotFoundException(Message(s"story with id '$storyId' could not be updated",`ERROR`) :: Nil)
      }    
  }

  def delete(storyId: String, login: String) = {

    import QueryActor.DeleteFromIndex

    server.one[StoryId](removeStoryQueryString, 
      ("id" -> storyId),
      ("login" -> login)
    ) map {
        case Some(s) => indexActor ! DeleteFromIndex(storyId)
        case None => throw NotFoundException(Message(s"story with id '$storyId' could not be deleted",`ERROR`) :: Nil)
      }    
  }

}