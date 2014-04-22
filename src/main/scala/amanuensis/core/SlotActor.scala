package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.domain.Story
import amanuensis.core.util.Failable

import amanuensis.core.neo4j._

import amanuensis.domain.{StoryInfo, StoryId, StoryRights}


object SlotActor {

  case class List(storyId: String, slotName: String, login: String)
  case class Add(toStory: String, slotName: String, storyId: String, login: String)
  case class Remove(fromStory: String, slotName: String, storyId: String, login: String)
  case class CreateAndAdd(toStory: String, slotName: String, story: Story, login: String)


  val retrieveQueryString = """
    MATCH (u:User {login: {login}})
    MATCH (s:Story {id: {story}})
    WHERE (s)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    MATCH (s)-[r:Slot {name: {slot}}]-(:Story)
    WITH s, u, count(*) as weight
    MATCH (s)-[r:Slot {name: {slot}}]-(m:Story)
    WHERE (m)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    WITH m, weight,
      (CASE
        WHEN weight < 5 THEN m.content
        ELSE null 
      END) as content
    RETURN m.id, m.title, m.created, content LIMIT 250
  """

  val addQueryString = """
    MATCH (u:User {login: {login}})
    MATCH (n:Story {id: {toStory}})
    WHERE (n)<-[:canWrite|:canGrant*1..5]-(u)
    MATCH (m:Story {id: {story}})
    WHERE (m)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    CREATE (n)-[r:Slot {name: {slot}}]->(m)
    RETURN n.id
  """

  val removeQueryString = """
    MATCH (u:User {login: {login}})
    MATCH (n:Story {id: {fromStory}})
    WHERE (n)<-[:canWrite|:canGrant*1..5]-(u)
    MATCH (m:Story {id: {story}})
    WHERE (m)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    MATCH (n)-[r:Slot {name: {slot}}]-(m)
    DELETE r
    RETURN n.id
  """

  val createAndAddQueryString = """
    MATCH (u:User {login: {login}})
    MATCH (n:Story {id: {toStory}})
      WHERE (n)<-[:canWrite|:canGrant*1..5]-(u)
    OPTIONAL MATCH (n)<-[:canRead]-(x:User)
    OPTIONAL MATCH (n)<-[:canWrite]-(y:User)
    OPTIONAL MATCH (n)<-[:canGrant]-(z:User)
    WITH n,u,collect(x) as readers, collect(y) as writers, collect(z) as granters    
    CREATE (n)-[r:Slot {name: {slot}}]->(m:Story {id: {id}, title: {title}, content: {content}, created: {created}, createdBy: {createdBy}})
    WITH m,u,readers,writers,granters
    FOREACH (reader IN readers |
      MERGE (m)<-[:canRead]-(reader))
    FOREACH (writer IN writers |
      MERGE (m)<-[:canWrite]-(writer))
    FOREACH (granter IN granters |
      MERGE (m)<-[:canGrant]-(granter))
    MERGE (m)<-[:canGrant]-(u)
    FOREACH (tagname IN {tags} |
      MERGE (t:Tag {name: tagname})
      MERGE (m)-[:is]->(t:Tag))
    RETURN extract(x in (readers + writers + granters) | x.login)
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

    server.one[StoryId](addQueryString, 
      ("toStory" -> toStory),
      ("slot" -> slotName), 
      ("story" -> storyId),
      ("login" -> login)
    ) map {
        case Some(s) => indexActor ! IndexSlotName(slotName, toStory, storyId)
        case None => throw NotFoundException(Message(s"could not add story $storyId to slot $slotName",`ERROR`) :: Nil)
      }
  }

  def remove(fromStory: String, slotName: String, storyId: String, login: String) = {
    import QueryActor.DeleteSlotName

    server.one[StoryId](removeQueryString, 
      ("fromStory" -> fromStory),
      ("slot" -> slotName), 
      ("story" -> storyId),
      ("login" -> login)
    ) map {
        case Some(s) => indexActor ! DeleteSlotName(slotName, fromStory, storyId)
        case None => throw NotFoundException(Message(s"could not remove story $storyId from slot $slotName",`ERROR`) :: Nil)
      }
  }

  def createAndAdd(toStory: String, slotName: String, story: Story, login: String) = {

    import QueryActor.{Index, IndexSlotName}

    if (story.id.nonEmpty) throw ValidationException(Message("A new story must not have an id.",`ERROR`) :: Nil)
    story.check

    val id = Neo4JId.generateId

    server.one[StoryRights](createAndAddQueryString, 
      ("toStory" -> toStory),
      ("slot" -> slotName), 
      ("id" -> id),
      ("title" -> story.title),
      ("content" -> story.content),
      ("created" -> story.created),
      ("createdBy" -> story.createdBy),
      ("tags" -> story.tags),
      ("login" -> login)   
    ) map {
        case Some(rights: StoryRights) => {
          indexActor ! Index(story.copy(id = Some(id)), rights.canRead)
          indexActor ! IndexSlotName(slotName, toStory, id)
          StoryInfo(id, story.title, story.created, None)
        }
        case _ => throw NotFoundException(Message(s"could not create new story in slot $slotName",`ERROR`) :: Nil)
      }
  }

}