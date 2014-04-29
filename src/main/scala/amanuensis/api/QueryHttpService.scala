package amanuensis.api

import spray.http._
import spray.routing._
import scala.concurrent.ExecutionContext
import akka.actor.{Actor, ActorRef, ActorLogging}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration.DurationInt
import amanuensis.domain.Story
import spray.json._
import spray.httpx.marshalling._
import spray.httpx.SprayJsonSupport

import scala.concurrent.future

import amanuensis.core.elasticsearch._
import amanuensis.core.QueryActor._
import amanuensis.domain.{Story, StoryInfo, StoryContext, StoryProtocol, UserContext}

import StatusCode._


trait QueryHttpService extends HttpService with SprayJsonSupport { 

  import ElasticSearchProtocol._

  private val queryActor = actorRefFactory.actorSelection("/user/query")

  private implicit val timeout = new Timeout(5.seconds)
  private implicit def executionContext = actorRefFactory.dispatcher


  def queryRoute(userContext: UserContext) =
    pathPrefix("query") {
      path("fulltext") {
        entity(as[QueryRequest]) { queryRequest: QueryRequest =>
          post {
            dynamic {
              //FixMe: we do not need a Message-Type Fulltext here...
              complete((queryActor ? Fulltext(queryRequest, userContext.permissions)).mapTo[QueryResult])
            }
          }
        }
      } ~
      path("todos") {
        entity(as[QueryRequest]) { queryRequest: QueryRequest =>
          post {
            dynamic {
              //FixMe: we do not need a Message-Type Fulltext here...
              complete((queryActor ? ToDos(queryRequest, userContext.permissions, userContext.login)).mapTo[QueryResult])
            }
          }
        }
      }~
      path("mylatest") {
        entity(as[QueryRequest]) { queryRequest: QueryRequest =>
          post {
            dynamic {
              //FixMe: we do not need a Message-Type Fulltext here...
              complete((queryActor ? MyLatest(queryRequest, userContext.permissions, userContext.login)).mapTo[QueryResult])
            }
          }
        }
      } ~
      path("otherslatest") {
        entity(as[QueryRequest]) { queryRequest: QueryRequest =>
          post {
            dynamic {
              //FixMe: we do not need a Message-Type Fulltext here...
              complete((queryActor ? OthersLatest(queryRequest, userContext.permissions, userContext.login)).mapTo[QueryResult])
            }
          }
        }
      } ~    
      pathPrefix("suggest") {
        get {
          dynamic {
            path("tags" / Segment) { text: String =>
              complete((queryActor ? SuggestTags(text)).mapTo[SuggestResult])
            } ~
            path("slots" / Segment) { text: String =>
              complete((queryActor ? SuggestSlots(text)).mapTo[SuggestResult])
            } ~
            path("users" / Segment) { text: String =>
              complete((queryActor ? SuggestUsers(text)).mapTo[SuggestResult])
            } ~
            path("groups" / Segment) { text: String =>
              complete((queryActor ? SuggestGroups(text)).mapTo[SuggestResult])
            }                        
          }
        }
      }
    }
}