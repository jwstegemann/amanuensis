package amanuensis.core.neo4j

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


trait UsingParams {
  type Param = (String, JsValue)
}

case class CypherServer(url: String)(implicit val actorSystem: ActorSystem) extends DefaultJsonProtocol with SprayJsonSupport with UsingParams {

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
    println(buildQueryObject(query, params: _*))
    pipeline(Post(url, buildQueryObject(query, params: _*))) map {
      //TODO: is there a way to hande objects in return s for example
      case result: JsObject => {
        println(result)
        result.fields("data").convertTo[Seq[R]]
      }
    }
    //FIXME: map xception to Neo4JException and handle it!
  }

  def one[R](query: String, params: Param*)(implicit rowJsonFormat: JsonFormat[R]): Future[Option[R]] = {
    //FIXME: do not convert all entries to case class
    list[R](query, params : _*) map ( list => list.headOption )
  }

  def execute(query: String, params: Param*): Future[HttpResponse] = {
    //FIXME: handle error! (NoException)
    println(query + ", " + params)
    pipelineRaw(Post(url, buildQueryObject(query, params: _*))) // map (httpResponse => httpResponse.status.isSuccess)
  }

}

/*       println("QUERY: " + this.toJson(qf).toString)

      val result = """{
          "columns" : [ "type(r)", "n.name?", "n.age?" ],
          "data" : [ [ "know", 25 ], [ "know", 12 ] ]
        }""".asJson.convertTo[Result[R]](rf)
      
      result 
*/

