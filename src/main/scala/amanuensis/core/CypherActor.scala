package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import spray.httpx.SprayJsonSupport
import spray.json._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.core.util.Failable


object CypherActor extends DefaultJsonProtocol {

  case class Query(query: String, params: Map[String, JsValue])

  case class Result(columns: Seq[String], data: Seq[Seq[JsValue]])

  implicit val queryJsonFormat = jsonFormat2(Query.apply)

  implicit val resultJsonFormat = jsonFormat2(Result.apply)

}


class CypherActor extends Actor with ActorLogging with Failable with SprayJsonSupport {

  import CypherActor._

  implicit def executionContext = context.dispatcher

	override def preStart =  {
    log.info(s"CypherActor started at: {}", self.path)
  }

  def receive = {
    case q: Query => sender ! handleQuery(q) // pipeTo sender
  }

  def handleQuery(q: Query) = {
  	//q.toJson

    """{
  "columns" : [ "type(r)", "n.name?", "n.age?" ],
  "data" : [ [ "know", "him", 25 ], [ "know", "you", null ] ]
}""".asJson.convertTo[Result]//.toString
  }

}