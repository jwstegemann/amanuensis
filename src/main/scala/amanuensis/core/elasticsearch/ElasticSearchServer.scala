package amanuensis.core.elasticsearch

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

import amanuensis.domain.{Story, StoryProtocol}


case class ElasticSearchException(val message: String) extends Exception

trait UsingParams {
  type Param = (String, JsValue)
}

object ElasticSearchServer {
  //ToDo: externalize this for both server types
  val WithCredentials = """(http|https)://([\w\.-]+):([\w\.-]+)@([\w\.-]+):(\d+)""".r
  val WithoutCredentials = """(http|https)://([\w\.-]+):(\d+)""".r

  def default(implicit actorSystem: ActorSystem) : ElasticSearchServer = {
    val db_server = scala.util.Properties.envOrElse("BONSAI_URL", "http://localhost:9200")

    db_server match {
      case WithCredentials(protocol,username,password,host,port) => 
        ElasticSearchServer(s"$protocol://$host:$port", Some(BasicHttpCredentials(username, password)))
      case WithoutCredentials(protocol,host,port) => 
        ElasticSearchServer(s"$protocol://$host:$port", None)
      case _ => throw ElasticSearchException(s"invalid url for ElasticSearchServer: $db_server")
    }

  }

}

case class ElasticSearchServer(url: String, credentialsOption: Option[BasicHttpCredentials])(implicit val actorSystem: ActorSystem) extends DefaultJsonProtocol with SprayJsonSupport with UsingParams {

  import actorSystem.dispatcher

  import ElasticSearchProtocol._
  import StoryProtocol._

  val log = Logging(actorSystem, classOf[ElasticSearchServer])

  log.info(s"created ElasticSearchServer @ $url with credentials: $credentialsOption")


  val searchUrl = s"$url/stories/story/_search"
  val indexUrl = s"$url/stories/story"

  // interpret the HttpResponse and throw a Neo4JException if necessary
  val mapToElasticSeachException: HttpResponse => HttpResponse = { response =>
    log.debug("Elastic-Search-Response: {}", response)
    if (!response.status.isSuccess) throw ElasticSearchException(response.entity.asString)
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
  final def pipeline: HttpRequest => Future[QueryResult] = (
    addHeader("Accept","application/json; charset=UTF-8")
    ~> send
    ~> mapToElasticSeachException
    ~> unmarshal[QueryResult]
  )

  // pipeline for queries just to be executed
  final val pipelineRaw: HttpRequest => Future[Unit] = (
    send
    ~> mapToElasticSeachException
    ~> forgetResponse
  )

  def query(queryString: String): Future[QueryResult] = {
    //ToDo: make constants to improve performance
    val queryObject = JsObject(
      ("query", JsObject(
        ("multi_match", JsObject(
          ("query", JsString(queryString)),
          ("fields", JsArray(JsString("title"),JsString("content"))),
          ("type", JsString("phrase_prefix")),
          ("max_expansions", JsString("10"))
        ))
      ))
    )
    log.debug("ElasticSearch-Query-Request: {}", queryObject)
    pipeline(Get(searchUrl, queryObject)) recover {
      case x => throw ElasticSearchException(s"Error retrieving response from ElasticSearch-server: $x")
    }
  }

  def index(story: Story): Future[Unit] = {
    //ToDo: check, if id is valid
    val id = story.id.get
    val myUrl =  s"$indexUrl/$id"
    log.debug("ElasticSearch-Index-Request: {} @ {}", story, myUrl)
    pipelineRaw(Post(myUrl, story))
  }

  def delete(id: String): Future[Unit] = {
    //ToDo: check, if id is valid
    val myUrl =  s"$indexUrl/$id"
    log.debug("ElasticSearch-Delete-Request: {}", myUrl)
    pipelineRaw(Delete(myUrl))
  }

}


