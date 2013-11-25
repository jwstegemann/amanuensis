package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import amanuensis.domain.Story


object StoryActor {

  case class Get(storyId: Long)

}

/**
 * Registers the users. Replies with
 */
class StoryActor extends Actor with ActorLogging {

  import StoryActor._

	override def preStart =  {
    log.info(s"StoryActor started at: {}", self.path)
  }

  def receive = {
    case Get(storyId) => sender ! getStory(storyId)
  }

  def getStory(storyId: Long) = {
  	Story(Some(storyId),"Testtitel","Testcontent")
  }

}