package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import spray.httpx.SprayJsonSupport
import spray.json._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.core.util.Failable


case class QueryX[T](query: String, params: T)(implicit val paramsJsonFormat: JsonFormat[T]) extends DefaultJsonProtocol {
  val jf = jsonFormat(QueryX[T],"query","params")
}

case class ResultX[T](columns: IndexedSeq[String], data: IndexedSeq[T])



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

  type JF[T] = JsonFormat[T] // simple alias for reduced verbosity

  def jsonArrayFormat2[A :JF, B :JF, T <: Product](construct: (A, B) => T) = {
    new RootJsonFormat[T] {
      def write(t: T) = serializationError("row is not to be written")
      def read(value: JsValue) = value match {
        case JsArray(a :: b  :: Nil) => construct(a.convertTo[A], b.convertTo[B])
        case x => deserializationError("Expected case class as JsArray")
      }
    }
  }

  def handleQuery(q: Query) = {

    case class TestParams(p1: String, p2: Int)
    implicit val testParamsJsonFormat = jsonFormat2(TestParams)

    val q = new QueryX[TestParams]("testQuery",TestParams("A",1))

//    implicit val format = q.ColorJsonFormat
    println(q.toJson(q.jf).toString)


    case class TestRow(a: String, c: Int)
    implicit val testRowJsonFormat = jsonArrayFormat2(TestRow)

    println(
    """{
      "columns" : [ "type(r)", "n.name?", "n.age?" ],
      "data" : [ [ "know", 25 ], [ "know", 12 ] ]
    }""".asJson.convertTo[ResultX[TestRow]](jsonFormat(ResultX[TestRow], "columns", "data")).toString
    )

    "test"

/*    """{
  "columns" : [ "type(r)", "n.name?", "n.age?" ],
  "data" : [ [ "know", "him", 25 ], [ "know", "you", null ] ]
}""".asJson.convertTo[Result]//.toString
*/
  }

}