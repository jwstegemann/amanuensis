package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.domain.Story
import amanuensis.core.util.Failable

object StoryActor {

  case class Create(story: Story)
  case class Retrieve(storyId: Long)
  case class Update(storyId: Long, story: Story)
  case class Delete(storyId: Long)
}

/**
 * Registers the users. Replies with
 */
class StoryActor extends Actor with ActorLogging with Failable {

  import StoryActor._

  implicit def executionContext = context.dispatcher

	override def preStart =  {
    log.info(s"StoryActor started at: {}", self.path)
  }

  def receive = {
    case Create(story) => create(story)
    case Retrieve(storyId) => sender ! retrieve(storyId)
    case Update(storyId, story) => sender ! update(storyId, story)
    case Delete(storyId) => sender ! delete(storyId)
  }

  def create(story: Story) = {
  	failWith(NotFoundException(Message("Testmessage",`ERROR`) :: Nil))
  }

  def retrieve(storyId: Long) = {
  	Story(Some(storyId),"Testtitel","Testcontent")
  }

  def update(storyId: Long, story: Story) = {
  	Story(Some(storyId),"Testtitel","Testcontent")
  }

  def delete(storyId: Long) = {
  	Story(Some(storyId),"Testtitel","Testcontent")
  }
}