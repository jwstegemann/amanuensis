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

  case class List(storyId: String, slotName: String)
  case class Add(toStory: String, slotName: String, storyId: String)
  case class Remove(fromStory: String, slotName: String, storyId: String)
  case class CreateAndAdd(toStory: String, slotName: String, story: Story)


  val retrieveQueryString = """MATCH (s:Story {id: {story}})
    MATCH (s)-[r:Slot {name: {slot}}]-(:Story)
    WITH s, count(*) as weight
    MATCH (s)-[r:Slot {name: {slot}}]-(m:Story)
    WITH m, weight,
      (CASE
        WHEN weight < 5 THEN m.content
        ELSE null 
      END) as content
    RETURN m.id, m.title, m.created, content"""
  val addQueryString = """MATCH (n:Story {id: {toStory}}),(m:Story {id: {story}}) MERGE (n)-[r:Slot]->(m) SET r.name={slot}"""
  val removeQueryString = """MATCH (n:Story {id: {fromStory}})-[r:Slot {name: {slot}}]-(m:Story {id: {story}}) DELETE r"""
  val createAndAddQueryString = """MATCH (n:Story {id: {toStory}}) MERGE (n)-[r:Slot {name: {slot}}]->(m:Story {id: {id}, title: {title}, content: {content}, created: {created}, createdBy: {createdBy} })"""
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
    case List(storyId, slotName) => list(storyId, slotName) pipeTo sender
    case Add(toStory, slotName, storyId) => add(toStory, slotName, storyId) pipeTo sender
    case Remove(fromStory, slotName, storyId) => remove(fromStory, slotName, storyId) pipeTo sender
    case CreateAndAdd(toStory, slotName, story) => createAndAdd(toStory, slotName, story) pipeTo sender
  }

  def list(storyId: String, slotName: String) = {
  	server.list[StoryInfo](retrieveQueryString,
      ("story" -> storyId),
      ("slot" -> slotName)
    )
  }

  def add(toStory: String, slotName: String, storyId: String) = {
    server.execute(addQueryString, 
      ("toStory" -> toStory),
      ("slot" -> slotName), 
      ("story" -> storyId)
    )
  }

  def remove(fromStory: String, slotName: String, storyId: String) = {
    server.execute(removeQueryString, 
      ("fromStory" -> fromStory),
      ("slot" -> slotName), 
      ("story" -> storyId)
    )
  }

  def createAndAdd(toStory: String, slotName: String, story: Story) = {

    import QueryActor.Index

    if (story.id.nonEmpty) throw ValidationException(Message("A new story must not have an id.",`ERROR`) :: Nil)
    story.check

    val id = Neo4JId.generateId

    indexActor ! Index(story.copy(id = Some(id)))

    server.execute(createAndAddQueryString, 
      ("toStory" -> toStory),
      ("slot" -> slotName), 
      ("id" -> id),
      ("title" -> story.title),
      ("content" -> story.content),
      ("created" -> story.created),
      ("createdBy" -> story.createdBy)      
    ) map { nothing => StoryInfo(id, story.title, story.created, None) }
  }

}