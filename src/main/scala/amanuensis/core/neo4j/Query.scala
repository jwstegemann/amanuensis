package amanuensis.core


import scala.concurrent.Future

import spray.httpx.SprayJsonSupport
import spray.json._
import spray.http._

import spray.client.pipelining._

import spray.can.Http
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import spray.util._

import akka.actor.ActorSystem

import amanuensis.core.neo4j.Neo4JRestFormats


case class Neo4JServer(url: String)
  

case class Query(implicit val actorSystem: ActorSystem, val server: Neo4JServer) extends DefaultJsonProtocol with SprayJsonSupport {

  type Param = (String, JsValue)

  import actorSystem.dispatcher

  final val pipeline: HttpRequest => Future[JsObject] = (
    addHeader("Accept","application/json; charset=UTF-8")
    ~> sendReceive
    ~> unmarshal[JsObject]
  )

  final val pipelineRaw: HttpRequest => Future[HttpResponse] = (
    sendReceive
  )

  def buildQueryObject(query: String, params: Param*): JsObject = JsObject(("query", JsString(query)), ("params", JsObject(params: _*)))

  def list[R](query: String, params: Param*)(implicit rowJsonFormat: JsonFormat[R]): Future[Seq[R]] = {
    pipeline(Post(server.url, buildQueryObject(query, params: _*))) map {
      case result: JsObject => result.fields("data").convertTo[Seq[R]]
    }
  }

  def one[R](query: String, params: Param*)(implicit rowJsonFormat: JsonFormat[R]): Future[R] = {
    //FIXME: do not convert all entries to case class
    list[R](query, params: _*) map ( list => list.head )
  }

  def execute(query: String, params: Param*): Future[HttpResponse] = {
    pipelineRaw(Post(server.url, buildQueryObject(query, params: _*)))
  }

}

/*       println("QUERY: " + this.toJson(qf).toString)

      val result = """{
          "columns" : [ "type(r)", "n.name?", "n.age?" ],
          "data" : [ [ "know", 25 ], [ "know", 12 ] ]
        }""".asJson.convertTo[Result[R]](rf)
      
      result 
*/

