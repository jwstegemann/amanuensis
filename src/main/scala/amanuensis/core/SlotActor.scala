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

  case class List(storyId: String, slotName: String, login: String)
  case class Add(toStory: String, slotName: String, storyId: String, login: String)
  case class Remove(fromStory: String, slotName: String, storyId: String, login: String)
  case class CreateAndAdd(toStory: String, slotName: String, story: Story, login: String)


  val retrieveQueryString = """
    MATCH (u:User {login: {login}})
    MATCH (s:Story {id: {story}})<-[:canRead]-(u)
    MATCH (s)-[r:Slot {name: {slot}}]-(:Story)
    WITH s, count(*) as weight
    MATCH (s)-[r:Slot {name: {slot}}]-(m:Story)<-[:canRead]-(u)
    WITH m, weight,
      (CASE
        WHEN weight < 5 THEN m.content
        ELSE null 
      END) as content
    RETURN m.id, m.title, m.created, content LIMIT 250
  """

  val addQueryString = """
    MATCH (u:User {login: {login}})
    MATCH (n:Story {id: {toStory}})<-[:canWrite]-(u)
    MATCH (m:Story {id: {story}})<-[:canRead]-(u)
    MERGE (n)-[r:Slot]->(m) 
    SET r.name={slot}
  """

  val removeQueryString = """
    MATCH (u:User {login: {login}})
    MATCH (n:Story {id: {fromStory}})<-[:canWrite]-(u)
    MATCH (m:Story {id: {story}})<-[:canRead]-(u)
    MATCH (n)-[r:Slot {name: {slot}}]-(m)
    DELETE r
  """

  val createAndAddQueryString = """
    MATCH (u:User {login: {login}})  
    MATCH (n:Story {id: {toStory}})<-[:canWrite]-(u)
    MERGE (n)-[r:Slot {name: {slot}}]->(m:Story {id: {id}, title: {title}, content: {content}, created: {created}, createdBy: {createdBy}})
    WITH m,u
    CREATE (u)-[:canRead]->(m)<-[:canWrite]-(u)
    FOREACH (tagname IN {tags} |
      MERGE (t:Tag {name: tagname})
      MERGE (m)-[:is]->(t:Tag))
  """
}

/**
 * Registers the users. Replies with
 */
class SlotActor extends Actor with ActorLogging with Failable with Neo4JJsonProtocol {

  import SlotActor._
  import StoryNeoProtocol._

  implicit def executionContext = context.dispatcher
  implicit val system = context.system
  def actorRefFactory = system

  final val server = CypherServer.default

  private val indexActor = actorRefFactory.actorSelection("/user/query")


	override def preStart =  {
    log.info(s"SlotActor started at: {}", self.path)
  }

  def receive = {
    case List(storyId, slotName, login) => list(storyId, slotName, login) pipeTo sender
    case Add(toStory, slotName, storyId, login) => add(toStory, slotName, storyId, login) pipeTo sender
    case Remove(fromStory, slotName, storyId, login) => remove(fromStory, slotName, storyId, login) pipeTo sender
    case CreateAndAdd(toStory, slotName, story, login) => createAndAdd(toStory, slotName, story, login) pipeTo sender
  }

  def list(storyId: String, slotName: String, login: String) = {
  	server.list[StoryInfo](retrieveQueryString,
      ("story" -> storyId),
      ("slot" -> slotName),
      ("login" -> login)
    )
  }

  def add(toStory: String, slotName: String, storyId: String, login: String) = {
    import QueryActor.IndexSlotName

    indexActor ! IndexSlotName(slotName, toStory, storyId)

    server.execute(addQueryString, 
      ("toStory" -> toStory),
      ("slot" -> slotName), 
      ("story" -> storyId),
      ("login" -> login)
    )
  }

  def remove(fromStory: String, slotName: String, storyId: String, login: String) = {
    import QueryActor.DeleteSlotName

    indexActor ! DeleteSlotName(slotName, fromStory, storyId)

    server.execute(removeQueryString, 
      ("fromStory" -> fromStory),
      ("slot" -> slotName), 
      ("story" -> storyId),
      ("login" -> login)
    )
  }

  def createAndAdd(toStory: String, slotName: String, story: Story, login: String) = {

    import QueryActor.{Index, IndexSlotName}

    if (story.id.nonEmpty) throw ValidationException(Message("A new story must not have an id.",`ERROR`) :: Nil)
    story.check

    val id = Neo4JId.generateId

    indexActor ! Index(story.copy(id = Some(id)))
    indexActor ! IndexSlotName(slotName, toStory, id)

    server.execute(createAndAddQueryString, 
      ("toStory" -> toStory),
      ("slot" -> slotName), 
      ("id" -> id),
      ("title" -> story.title),
      ("content" -> story.content),
      ("created" -> story.created),
      ("createdBy" -> story.createdBy),
      ("tags" -> story.tags),
      ("login" -> login)   
    ) map { nothing => StoryInfo(id, story.title, story.created, None) }
  }

}