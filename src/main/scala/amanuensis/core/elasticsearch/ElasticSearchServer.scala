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

import amanuensis.domain.{Story, StoryProtocol, Slot, SlotProtocol}

import amanuensis.core.util.Converters

import org.joda.time.{DateTime, Years, Months, Weeks, Days}


case class ElasticSearchException(val message: String) extends Exception


trait UsingParams {
  type Param = (String, JsValue)
}

object ElasticSearchServer {
  //ToDo: externalize this for both server types
  val WithCredentials = """(http|https)://([\w\.-]+):([\w\.-]+)@([\w\.-]+):(\d+)""".r
  val WithoutCredentials = """(http|https)://([\w\.-]+):(\d+)""".r

  def default(implicit actorSystem: ActorSystem) : ElasticSearchServer = {
    val db_server = scala.util.Properties.envOrElse("ELASTICSEARCH_URL", "http://localhost:9200")

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
  import SlotProtocol._

  val log = Logging(actorSystem, classOf[ElasticSearchServer])

  log.info(s"created ElasticSearchServer @ $url with credentials: $credentialsOption")


  val searchUrl = s"$url/stories/story/_search"
  val tagSuggestUrl = s"$url/stories/_suggest"
  val slotSuggestUrl = s"$url/slots/_suggest"
  val indexUrl = s"$url/stories/story"
  val slotIndexUrl = s"$url/slots/slot"  

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

  // pipeline for queries that should return something
  final def pipelineSuggest: HttpRequest => Future[SuggestResult] = (
    addHeader("Accept","application/json; charset=UTF-8")
    ~> send
    ~> mapToElasticSeachException
    ~> unmarshal[SuggestResult]
  )

  // pipeline for queries just to be executed
  final val pipelineRaw: HttpRequest => Future[Unit] = (
    send
    ~> mapToElasticSeachException
    ~> forgetResponse
  )

  def query(queryRequest: QueryRequest): Future[QueryResult] = {
    val today = DateTime.now.withTimeAtStartOfDay
    val aYearAgo = today minus Years.ONE
    val aMonthAgo = today minus Months.ONE
    val twoWeeksAgo = today minus Weeks.TWO
    val aWeekAgo = today minus Weeks.ONE
    val yesterday = today minus Days.ONE

    //ToDo: make constants to improve performance
    val queryObject = JsObject(
      ("query", JsObject(
        ("multi_match", JsObject(
          ("query", JsString(queryRequest.query)),
          ("fields", JsArray(JsString("title"),JsString("content"),JsString("tags"))),
          ("type", JsString("phrase_prefix")),
          ("max_expansions", JsString("10"))
        ))
      )),
      ("from", JsNumber(queryRequest.page * 25)),
      ("size", JsNumber(25)),
      ("facets", JsObject(
        ("tags", JsObject(
          ("terms", JsObject(
            ("field", JsString("tags"))
          ))
        )),
        ("dates", JsObject(
          ("range", JsObject(
            ("field", JsString("created")),
            ("ranges", JsArray(
              JsObject(("from", JsString(today.toString()))),
              JsObject(("from", JsString(yesterday.toString()))),
              JsObject(("from", JsString(aWeekAgo.toString()))),
              JsObject(("from", JsString(twoWeeksAgo.toString()))),
              JsObject(("from", JsString(aMonthAgo.toString()))),
              JsObject(("from", JsString(aYearAgo.toString())))
            ))
          ))
        ))
      )),
      queryRequest.tags match {
        case (first :: rest) => ("filter", JsObject(
            ("terms", JsObject(
              ("tags", (first :: rest).toJson)
            ))
          ))
        case _ => ("filter", JsObject())
      },
      queryRequest.fromDate match {
        case Some(fromDate) => ("filter", JsObject(
            ("range", JsObject(
              ("created", JsObject (
                ("gte", JsString(fromDate))
              ))
            ))
          ))
        case None => ("filter", JsObject())
      }      
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

  def calcSlotId(slotName: String, sourceStoryId: String, targetStoryId: String): String = Converters.md5Hex(s"$sourceStoryId$slotName$targetStoryId")

  def indexSlotName(slotName: String, sourceStoryId: String, targetStoryId: String): Future[Unit] = {
    val id = calcSlotId(slotName, sourceStoryId, targetStoryId)
    val myUrl =  s"$slotIndexUrl/$id"
    log.debug("ElasticSearch-Index-Slot-Request: {}", myUrl)
    pipelineRaw(Post(myUrl, Slot(slotName)))
  }

  def deleteSlotName(slotName: String, sourceStoryId: String, targetStoryId: String): Future[Unit] = {
    val id = calcSlotId(slotName, sourceStoryId, targetStoryId)
    val myUrl =  s"$slotIndexUrl/$id"
    log.debug("ElasticSearch-Delete-Slot-Request: {}", myUrl)
    pipelineRaw(Delete(myUrl))
  }

  def suggestTags(text: String): Future[SuggestResult] = {
    val queryObject = JsObject(
      ("suggest", JsObject(
        ("text", JsString(text)),
        ("completion", JsObject(
          ("field", JsString("tags.suggest")),
          ("size", JsNumber(20))
        ))
    )))

    log.debug("ElasticSearch-Suggestion-Request: {}", queryObject)
    pipelineSuggest(Get(tagSuggestUrl, queryObject)) recover {
      case x => throw ElasticSearchException(s"Error retrieving response from ElasticSearch-server: $x")
    }    
  }

  def suggestSlots(text: String): Future[SuggestResult] = {
    val queryObject = JsObject(
      ("suggest", JsObject(
        ("text", JsString(text)),
        ("completion", JsObject(
          ("field", JsString("name")),
          ("size", JsNumber(20))
        ))
    )))

    log.debug("ElasticSearch-Suggestion-Request: {}", queryObject)
    pipelineSuggest(Get(slotSuggestUrl, queryObject)) recover {
      case x => throw ElasticSearchException(s"Error retrieving response from ElasticSearch-server: $x")
    }    
  }  

}


