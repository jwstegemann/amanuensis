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

import amanuensis.domain.{Story, StoryProtocol, Slot, SlotProtocol, StoryIndex}

import amanuensis.core.util.Converters
import amanuensis.core.UsingParams

import org.joda.time.{DateTime, Years, Months, Weeks, Days}


case class ElasticSearchException(val message: String) extends Exception


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
  val userSuggestUrl = s"$url/users/_suggest"
  val groupSuggestUrl = s"$url/groups/_suggest"

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

  def query(queryRequest: QueryRequest, groups: Seq[String]): Future[QueryResult] = {
    val today = DateTime.now.withTimeAtStartOfDay
    val aYearAgo = today minus Years.ONE
    val aMonthAgo = today minus Months.ONE
    val twoWeeksAgo = today minus Weeks.TWO
    val aWeekAgo = today minus Weeks.ONE
    val yesterday = today minus Days.ONE

    //ToDo: make constants for query-json-objects to improve performance

    //FIXME: terms or term-query-filter
    val userFilter: JsObject = JsObject(
      ("terms", JsObject(
        ("canRead", groups.toJson)
      )))

    val tagFilter: JsObject = queryRequest.tags match {
        case (first :: rest) => JsObject(
          ("terms", JsObject(
            ("tags", (first :: rest).toJson)
          )))
        case _ => JsObject()
      }

    val dateFilter: JsObject = queryRequest.fromDate match {
        case Some(fromDate) => JsObject(
          ("range", JsObject(
            ("modified", JsObject (
              ("gte", JsString(fromDate))
            ))
          )))
        case None => JsObject()
      }

    val queryObject = JsObject(
      ("query", JsObject(
        ("filtered", JsObject(
          ("query", JsObject(
            ("multi_match", JsObject(
              ("query", JsString(queryRequest.query)),
              ("fields", JsArray(JsString("title"),JsString("content"),JsString("tags"))),
              ("type", JsString("phrase_prefix")),
              ("max_expansions", JsString("10"))
            ))
          )),
          ("filter", userFilter)
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
            ("field", JsString("modified")),
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
      ("filter", JsObject(
        ("and", JsArray(tagFilter :: dateFilter :: Nil))
      ))          
    )

    log.debug("ElasticSearch-Query-Request: {}", queryObject)
    pipeline(Get(searchUrl, queryObject)) recover {
      case x => throw ElasticSearchException(s"Error retrieving response from ElasticSearch-server: $x")
    }
  }

  def index(story: StoryIndex): Future[Unit] = {
    //ToDo: check, if id is valid
    val id = story.id.get
    val myUrl =  s"$indexUrl/$id"
    log.debug("ElasticSearch-Index-Request: {} @ {}", story, myUrl)
    pipelineRaw(Post(myUrl, story))
  }

  def update(story: Story): Future[Unit] = {
    //ToDo: check, if id is valid
    val id = story.id.get
    val myUrl =  s"$indexUrl/$id/_update"

    val updateObject = JsObject(
      ("doc", story.toJson)
    ) 

    log.debug("ElasticSearch-Update-Request: {} @ {}", updateObject, myUrl)
    pipelineRaw(Post(myUrl, updateObject))
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


  def suggest(url: String, field: String, text: String): Future[SuggestResult] = {
    val queryObject = JsObject(
      ("suggest", JsObject(
        ("text", JsString(text)),
        ("completion", JsObject(
          ("field", JsString(field)),
          ("size", JsNumber(20))
        ))
    )))

    log.debug("ElasticSearch-Suggestion-Request: {}", queryObject)
    pipelineSuggest(Get(url, queryObject)) recover {
      case x => throw ElasticSearchException(s"Error retrieving response from ElasticSearch-server: $x")
    }    
  }  

  def suggestTags(text: String) = suggest(tagSuggestUrl, "tags.suggest", text)
  def suggestSlots(text: String) = suggest(slotSuggestUrl, "name", text)
  def suggestUsers(text: String) = suggest(userSuggestUrl, "login", text)
  def suggestGroups(text: String) = suggest(groupSuggestUrl, "login", text)


  private val addUserScript = "if (!ctx._source.canRead.contains(user)) {ctx._source.canRead += user}"
  private val removeUserScript = "ctx._source.canRead.remove(user)"

  def changeReadAccess(storyId: String, userId: String, allow: Boolean) = {
    val myUrl =  s"$indexUrl/$storyId/_update"
    val script = if(allow) addUserScript else removeUserScript
    val queryObject = JsObject(
      ("script", JsString(script)),
      ("params", JsObject(
        ("user", JsString(userId))
      ))
    )

    log.debug("ElasticSearch-AddReadAccess-Request: {} @ {}", queryObject, storyId)
    pipelineRaw(Post(myUrl, queryObject))
  }


def handleQuery(queryField: JsField, sortField: JsField, queryRequest: QueryRequest, groups: Seq[String], login: String): Future[QueryResult] = {
    val today = DateTime.now.withTimeAtStartOfDay
    val aYearAgo = today minus Years.ONE
    val aMonthAgo = today minus Months.ONE
    val twoWeeksAgo = today minus Weeks.TWO
    val aWeekAgo = today minus Weeks.ONE
    val yesterday = today minus Days.ONE

    //ToDo: make constants for query-json-objects to improve performance

    //FIXME: terms or term-query-filter
    val userFilter: JsObject = JsObject(
      ("terms", JsObject(
        ("canRead", groups.toJson)
      )))

    val tagFilter: JsObject = queryRequest.tags match {
        case (first :: rest) => JsObject(
          ("terms", JsObject(
            ("tags", (first :: rest).toJson)
          )))
        case _ => JsObject()
      }

    val dateFilter: JsObject = queryRequest.fromDate match {
        case Some(fromDate) => JsObject(
          ("range", JsObject(
            ("modified", JsObject (
              ("gte", JsString(fromDate))
            ))
          )))
        case None => JsObject()
      }

    val queryObject = JsObject(
      ("query", JsObject(
        ("filtered", JsObject(
          queryField,
          ("filter", userFilter)
        ))
      )),
      sortField,
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
            ("field", JsString("modified")),
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
      ("filter", JsObject(
        ("and", JsArray(tagFilter :: dateFilter :: Nil))
      ))          
    )

    log.debug("ElasticSearch-Query-Request: {}", queryObject)
    pipeline(Get(searchUrl, queryObject)) recover {
      case x => throw ElasticSearchException(s"Error retrieving response from ElasticSearch-server: $x")
    }
  }


  def mylatest(queryRequest: QueryRequest, groups: Seq[String], login: String): Future[QueryResult] = {
    val lookBackTo = DateTime.now.withTimeAtStartOfDay minus Months.ONE

    val queryField = 
      ("query", JsObject(
        ("bool", JsObject(
          ("must", JsArray(
            JsObject(("range", JsObject(
              ("modified", JsObject (
                ("gte", JsString(lookBackTo.toString()))
              ))
            ))),
            JsObject(("term", JsObject(
              ("modifiedBy", JsString(login))
            )))
                    
          ))
        ))
      ))

    val sortField = 
      ("sort", JsArray(
        JsObject(("modified", JsString("desc")))
      ))

    handleQuery(queryField, sortField, queryRequest, groups, login)
  }

  def otherslatest(queryRequest: QueryRequest, groups: Seq[String], login: String): Future[QueryResult] = {
    val lookBackTo = DateTime.now.withTimeAtStartOfDay minus Weeks.ONE

    val queryField = 
      ("query", JsObject(
        ("bool", JsObject(
          ("must", JsObject(
            ("range", JsObject(
              ("modified", JsObject (
                ("gte", JsString(lookBackTo.toString()))
              ))
            ))            
          )),
          ("must_not", JsObject(
            ("term", JsObject(
              ("modifiedBy", JsString(login))
            ))            
          ))
        ))
      ))

    val sortField = 
      ("sort", JsArray(
        JsObject(("modified", JsString("desc")))
      ))

    handleQuery(queryField, sortField, queryRequest, groups, login)
  }

}


