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


object RebuildIndexActor {

  /*
   * query-string for neo4j
   */
   //FIXME: always the same amount of tags and canReads...
  val listStoriesQueryString = """
    MATCH (s:Story)
    OPTIONAL MATCH (s)-[:is]->(t:Tag)
    OPTIONAL MATCH (s)<-[:canRead|:canWrite|:canGrant*1..5]-(u)
    RETURN s.id as id, s.title as title, s.content as content, s.created as created, s.createdBy as createdBy,
      s.modified as modified, s.modifiedBy as modifiedBy, collect(t.name) as tags, s.icon as icon, collect(u.login) as canRead
  """

  val listSlotsQueryString = """
    MATCH (s:Story)-[r]->(x:Story)
    RETURN r.name, s.id, x.id
  """  

  val listUsersQueryString = """
    MATCH (u:User)
    RETURN u.login, u.pwd
  """  
}


case class SlotInfo(name: String, source: String, target: String)
case class UserInfo(login: String, pwd: String)

/**
 * Registers the users. Replies with
 */
class RebuildIndexActor extends Actor with ActorLogging with Failable with UsingParams with Neo4JJsonProtocol {

  import RebuildIndexActor._

  implicit val storyNeo4JFormat = jsonCaseClassArrayFormat(StoryIndex)
  implicit val slotNeo4JFormat = jsonCaseClassArrayFormat(SlotInfo)
  implicit val userNeo4JFormat = jsonCaseClassArrayFormat(UserInfo)

  import StoryNeoProtocol._


  implicit def executionContext = context.dispatcher
  implicit val system = context.system
  def actorRefFactory = system

  final val server = CypherServer.default

  private val indexActor = actorRefFactory.actorSelection("/user/query")

  override def preStart =  {
    log.info("Started to rebuild index...")

    reindexStories()
    reindexSlots()
    reindexUsers()

    log.info("... finished rebuilding index!")    
  }


  def receive = {
    case _ => log.error("RebuildIndexActor received message, but should not!")
  }

  def reindexStories() = {

    import QueryActor.Index

    server.list[StoryIndex](listStoriesQueryString) onSuccess { case stories: Seq[StoryIndex] =>
        stories.foreach { i =>
          log.info("indexing story {}", i.id.get)
          val story = Story(i.id, i.title, i.content, i.created, i.createdBy, i.modified, i.modifiedBy, None, i.tags, i.icon)
          indexActor ! Index(story, i.canRead)
        }
      }
  }


  def reindexSlots() = {

    import QueryActor.IndexSlotName

    server.list[SlotInfo](listSlotsQueryString) onSuccess { case slots: Seq[SlotInfo] =>
        slots.foreach { i =>
          log.info("indexing slot {}", i.name)
          indexActor ! IndexSlotName(i.name, i.source, i.target)
        }
      }
  }
 

  def reindexUsers() = {

    import QueryActor.IndexUser

    server.list[UserInfo](listUsersQueryString) onSuccess { case users: Seq[UserInfo] =>
        users.foreach { i =>
          log.info("indexing user {}", i.login)
          val isGroup = (i.login == i.pwd)
          if (i.login != "public") {
            indexActor ! IndexUser(i.login, isGroup)
          }
        }
      }
  }
}