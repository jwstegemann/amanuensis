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


object QueryActor {

  //FIXME: reverse attributes fitting to url
  case class FindAll()

  val findAllQueryString = """MATCH (s:Story) RETURN s.id as id, s.title as title"""
}

/**
 * Registers the users. Replies with
 */
class QueryActor extends Actor with ActorLogging with Failable with Neo4JJsonProtocol {

  import QueryActor._
  import StoryNeoProtocol._

  implicit def executionContext = context.dispatcher
  implicit val system = context.system

  final val server = CypherServer("http://localhost:7474/db/data/cypher")

	override def preStart =  {
    log.info(s"QueryActor started at: {}", self.path)
  }

  def receive = {
    case FindAll() => findAll() pipeTo sender
  }

  def findAll() = {
  	server.list[StoryInfo](findAllQueryString)
  }

}