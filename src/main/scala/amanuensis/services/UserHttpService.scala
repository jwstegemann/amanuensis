package amanuensis.services

import spray.routing.{ HttpService, RequestContext }
import spray.routing.directives.CachingDirectives
import spray.util._
import spray.http._
import MediaTypes._
import CachingDirectives._
import spray.routing._
import spray.http._
import StatusCodes._
import Directives._
import spray.json._
import spray.httpx.marshalling._
import spray.httpx.SprayJsonSupport
import amanuensis.auth._
import amanuensis.auth.AmanuensisAuthJsonProtocol._
import scala.concurrent.duration._
import scala.concurrent._
import akka.util.Timeout
import language.postfixOps
import reactivemongo.bson.utils.Converters
import scala.util.Random
import akka.pattern.ask
import akka.actor.ActorLogging
import spray.http.HttpHeaders._
import spray.routing.authentication.UserPass

// this trait defines our service behavior independently from the service actor
trait UserHttpService extends HttpService with SprayJsonSupport with SessionAware { self: ActorLogging =>

  val userContextActor = actorRefFactory.actorSelection("/user/userContext")

  private implicit val timeout = new Timeout(2 seconds)
  private implicit def executionContext = actorRefFactory.dispatcher

  def userRoute(userContext : UserContext) = {
    pathPrefix("user") {
      get {
        path("info") {
          log.info("Userinfo for " + userContext.username + " requested")
          complete(userContext);
        }
      }
    }
  }

  val random = new Random(System.currentTimeMillis)
  //  def createSessionId(hostName: String) = new UUID(Platform.currentTime, hostName.hashCode).toString
  def createSessionId(hostName: String) = Converters.md5Hex(hostName + System.currentTimeMillis + random.nextString(4))

}
