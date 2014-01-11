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
import akka.event.Logging


case class Neo4JException(val message: String) extends Exception

trait UsingParams {
  type Param = (String, JsValue)
}


object CypherServer {
  val WithCredentials = """(\w+)://(\w+):(\w+)@(\w+):(\w+)""".r
  val WithoutCredentials = """(\w+)://(\w+):(\w+)""".r

  def default(implicit actorSystem: ActorSystem) : CypherServer = {
    val db_server = scala.util.Properties.envOrElse("GRAPHENEDB_URL", "http://localhost:7474")

    db_server match {
      case WithCredentials(protocol,username,password,host,port) => 
        CypherServer(s"$protocol://$host:$port/db/data/cypher", Some(BasicHttpCredentials(username, password)))
      case WithoutCredentials(protocol,host,port) => 
        CypherServer(s"$protocol://$host:$port/db/data/cypher", None)
      case _ => throw Neo4JException(s"invalid url for CypherServer: $db_server")
    }

  }
}

case class CypherServer(url: String, credentialsOption: Option[BasicHttpCredentials])(implicit val actorSystem: ActorSystem) extends DefaultJsonProtocol with SprayJsonSupport with UsingParams {

  import actorSystem.dispatcher

  val log = Logging(actorSystem, classOf[CypherServer])

  log.info(s"created CypherServer @ $url with credentials: $credentialsOption")

  // interpret the HttpResponse and throw a Neo4JException if necessary
  val mapToNeo4JException: HttpResponse => HttpResponse = { response =>
    log.debug("Neo4J-Response: {}", response)
    if (!response.status.isSuccess) throw Neo4JException(response.entity.asString)
    response
  }

  // just forget the Response
  val forgetResponse: HttpResponse => Unit = {
    response => Unit
  }

  val send = credentialsOption match {
    case Some(credentials) => (addCredentials(credentials) ~> sendReceive)
    case None => sendReceive
  }

  // pipeline for queries that should return something
  final val pipeline: HttpRequest => Future[JsObject] = (
    addHeader("Accept","application/json; charset=UTF-8")
    ~> send
    ~> mapToNeo4JException
    ~> unmarshal[JsObject]
  )

  // pipeline for queries just to be executed
  final val pipelineRaw: HttpRequest => Future[Unit] = (
    send
    ~> mapToNeo4JException
    ~> forgetResponse
  )

  def buildQueryObject(query: String, params: Param*): JsObject = JsObject(("query", JsString(query)), ("params", JsObject(params: _*)))

  def list[R](query: String, params: Param*)(implicit rowJsonFormat: JsonFormat[R]): Future[Seq[R]] = {
    val queryObject = buildQueryObject(query, params: _*)
    log.debug("Neo4J-Request: {}", queryObject)
    pipeline(Post(url, queryObject)) map {
      //TODO: is there a way to hande objects in return s for example
      case result: JsObject => result.fields("data").convertTo[Seq[R]]
      case x => throw Neo4JException(s"no JSON-Object in response from Neo4J-server, but $x")
    }
  }

  def one[R](query: String, params: Param*)(implicit rowJsonFormat: JsonFormat[R]): Future[Option[R]] = {
    //FIXME: do not convert all entries to case class
    list[R](query, params : _*) map ( list => list.headOption )
  }

  def execute(query: String, params: Param*): Future[Unit] = {
    val queryObject = buildQueryObject(query, params: _*)
    log.debug("Neo4J-Request: {}", queryObject)
    pipelineRaw(Post(url, queryObject))
  }

}


