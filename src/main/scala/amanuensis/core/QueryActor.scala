package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.domain.Story
import amanuensis.core.util.Failable

import amanuensis.core.elasticsearch._


object QueryActor {

  case class Fulltext(query: String)
  case class Index(story: Story)
  case class Delete(storyId: String)

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
    case Fulltext(query: String) => server.query(query) pipeTo sender
    case Index(story: Story) => server.index(story) pipeTo sender
    case Delete(storyId: String) => server.delete(storyId) pipeTo sender
  }

}