package amanuensis.services

import scala.concurrent.duration._
import scala.util.{ Success, Failure }
import akka.pattern.ask
import akka.actor.ActorLogging
import spray.routing.{ HttpService, RequestContext }
import spray.routing.directives.CachingDirectives
import spray.util._
import spray.http._
import MediaTypes._
import CachingDirectives._


// this trait defines our service behavior independently from the service actor
trait StaticHttpService extends HttpService { self : ActorLogging =>

  //TODO: configure cache
  lazy val staticCache = routeCache(maxCapacity = 200, timeToIdle = Duration("12 h"))

  val staticRoute = {
    pathPrefix("static") {
      cache(staticCache) {
        getFromResourceDirectory("static")
      }
    }
  }
}
