package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.domain.{Story, StoryIndex}
import amanuensis.core.util.Failable

import amanuensis.core.elasticsearch._


object QueryActor {

  case class Fulltext(queryRequest: QueryRequest, groups: Seq[String])
  case class MyLatest(queryRequest: QueryRequest, groups: Seq[String], login: String)
  case class OthersLatest(queryRequest: QueryRequest, groups: Seq[String], login: String)

  case class Index(story: Story, canRead: Seq[String])
  case class UpdateIndex(story: Story)
  case class DeleteFromIndex(storyId: String)
  case class IndexSlotName(slotName: String, sourceStoryId: String, targetStoryId: String)
  case class DeleteSlotName(slotName: String, sourceStoryId: String, targetStoryId: String)
  case class SuggestTags(text: String)
  case class SuggestSlots(text: String)
  case class SuggestUsers(text: String)
  case class SuggestGroups(text: String)  
  case class IndexUser(login: String, isGroup: Boolean)
}

/**
 * Registers the users. Replies with
 */
class QueryActor extends Actor with ActorLogging with Failable {

  import QueryActor._

  implicit def executionContext = context.dispatcher
  implicit val system = context.system

  final val server = ElasticSearchServer.default

	override def preStart =  {
    log.info(s"QueryActor started at: {}", self.path)
  }

  def receive = {
    case Fulltext(queryRequest: QueryRequest, groups: Seq[String]) => server.query(queryRequest, groups) pipeTo sender
    
    case MyLatest(queryRequest: QueryRequest, groups: Seq[String], login: String) => server.mylatest(queryRequest, groups, login) pipeTo sender
    case OthersLatest(queryRequest: QueryRequest, groups: Seq[String], login: String) => server.otherslatest(queryRequest, groups, login) pipeTo sender

    //FIXME: get StoryIndex in msg!
    case Index(story: Story, canRead: Seq[String]) => server.index(StoryIndex(story.id, story.title, story.content, story.created, story.createdBy, 
      story.modified, story.modifiedBy, story.tags, story.icon, canRead))
    case UpdateIndex(story: Story) => server.update(story) // pipeTo sender    
    case DeleteFromIndex(storyId: String) => server.delete(storyId) // pipeTo sender
    case IndexSlotName(slotName: String, sourceStoryId: String, targetStoryId: String) => server.indexSlotName(slotName, sourceStoryId, targetStoryId)
    case DeleteSlotName(slotName: String, sourceStoryId: String, targetStoryId: String) => server.deleteSlotName(slotName, sourceStoryId, targetStoryId)

    case SuggestTags(text: String) => server.suggestTags(text) pipeTo sender
    case SuggestSlots(text: String) => server.suggestSlots(text) pipeTo sender
    case SuggestUsers(text: String) => server.suggestUsers(text) pipeTo sender
    case SuggestGroups(text: String) => server.suggestGroups(text) pipeTo sender

    case IndexUser(login: String, isGroup: Boolean) => server.indexUser(login, isGroup)
  }

}