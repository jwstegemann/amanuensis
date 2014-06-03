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

  case class List(storyId: String, slotName: String, inbound: Boolean, login: String)
  case class Add(toStory: String, slotName: String, storyId: String, inbound: Boolean, login: String)
  case class Remove(fromStory: String, slotName: String, storyId: String, inbound: Boolean, login: String)
  case class CreateAndAdd(toStory: String, slotName: String, story: Story, inbound: Boolean, login: String)


  val retrieveLeftQueryString = """
    MATCH (u:User {login: {login}})
    MATCH (s:Story {id: {story}})
    WHERE (s)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    MATCH (s)-[r:Slot {name: {slot}}]-(:Story)
    WITH s, u, count(*) as weight
    MATCH (s)<-[r:Slot {name: {slot}}]-(m:Story)
    WHERE (m)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    WITH m, weight,
      (CASE
        WHEN weight < 5 THEN m.content
        ELSE null 
      END) as content
    RETURN m.id, m.title, m.created, m.modified, content, m.icon LIMIT 250
  """

  val retrieveRightQueryString = """
    MATCH (u:User {login: {login}})
    MATCH (s:Story {id: {story}})
    WHERE (s)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    MATCH (s)-[r:Slot {name: {slot}}]-(:Story)
    WITH s, u, count(*) as weight
    MATCH (s)-[r:Slot {name: {slot}}]->(m:Story)
    WHERE (m)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    WITH m, weight,
      (CASE
        WHEN weight < 5 THEN m.content
        ELSE null 
      END) as content
    RETURN m.id, m.title, m.created, m.modified, content, m.icon LIMIT 250
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
      WHERE x.login <> {login}
    OPTIONAL MATCH (n)<-[:canWrite]-(y:User)
      WHERE y.login <> {login}
    OPTIONAL MATCH (n)<-[:canGrant]-(z:User)
      WHERE z.login <> {login}
    WITH n,u,collect(x) as readers, collect(y) as writers, collect(z) as granters    
    CREATE (n)-[r:Slot {name: {slot}}]->(m:Story {id: {id}, title: {title}, content: {content}, created: {created}, createdBy: {createdBy}, modified: {modified}, modifiedBy: {modifiedBy}, icon: {icon}})
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
    case List(storyId, slotName, inbound, login) => list(storyId, slotName, inbound, login) pipeTo sender
    case Add(toStory, slotName, storyId, inbound, login) => add(toStory, slotName, storyId, inbound, login) pipeTo sender
    case Remove(fromStory, slotName, storyId, inbound, login) => remove(fromStory, slotName, storyId, inbound, login) pipeTo sender
    case CreateAndAdd(toStory, slotName, story, inbound, login) => createAndAdd(toStory, slotName, story, inbound, login) pipeTo sender
  }

  def list(storyId: String, slotName: String, inbound: Boolean, login: String) = {
    val queryString = if (inbound) retrieveLeftQueryString else retrieveRightQueryString
  	server.list[StoryInfo](queryString,
      ("story" -> storyId),
      ("slot" -> slotName),
      ("login" -> login)
    )
  }

  def add(toStory: String, slotName: String, storyId: String, inbound: Boolean, login: String) = {
    import QueryActor.IndexSlotName

    if (inbound) throw NotFoundException(Message(s"cannot add a story to an inbound slot",`ERROR`) :: Nil)

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

  def remove(fromStory: String, slotName: String, storyId: String, inbound: Boolean, login: String) = {
    import QueryActor.DeleteSlotName

    if (inbound) throw NotFoundException(Message(s"cannot remove a story from an inbound slot",`ERROR`) :: Nil)

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

  def createAndAdd(toStory: String, slotName: String, story: Story, inbound: Boolean, login: String) = {
    import QueryActor.{Index, IndexSlotName}

    if (inbound) throw NotFoundException(Message(s"cannot create a story into an inbound slot",`ERROR`) :: Nil)

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
      ("modified" -> story.modified),
      ("modifiedBy" -> story.modifiedBy),      
      ("tags" -> story.tags),
      ("icon" -> story.icon),
      ("login" -> login)   
    ) map {
        case Some(rights: StoryRights) => {
          indexActor ! Index(story.copy(id = Some(id)), rights.canRead :+ login)
          indexActor ! IndexSlotName(slotName, toStory, id)
          StoryInfo(id, story.title, story.created, story.modified, None, story.icon)
        }
        case _ => throw NotFoundException(Message(s"could not create new story in slot $slotName",`ERROR`) :: Nil)
      }
  }

}