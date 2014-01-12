import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.routing.HttpService
import spray.http.StatusCodes._
import spray.routing.authentication.UserPass
import spray.routing.authentication.BasicAuth
import akka.actor.{ActorLogging, Actor}

import amanuensis.api.UserHttpService

import amanuensis.core._

import amanuensis.api.exceptions._
import amanuensis.domain.Message
import amanuensis.domain.Severities._
import amanuensis.domain.MessageJsonProtocol._
import amanuensis.core.neo4j.Neo4JException
import spray.http._
import spray.routing._


class AmanuensisUserApiSpec extends Specification with Specs2RouteTest with UserHttpService with Core with CoreActors {
  
  def actorRefFactory = system // connect the DSL to the test ActorSystem

  //import UserContextProtocol._

  //FIXME: externalize this (use just one for Service and Test)!
  implicit val amanuensisExceptionHandler = ExceptionHandler {
    case InternalServerErrorException(messages) => complete(InternalServerError, messages)
    case NotFoundException(message) => complete(NotFound, message)
    case ValidationException(messages) => complete(PreconditionFailed, messages)
    case Neo4JException(message) => {
      //log.error(s"Neo4J-error: $message")
      complete(InternalServerError, Message("An unexpected Error occured. Please inform your system administrator.", `ERROR`))     
    }
    case t: Throwable => {
      //log.error(t, "Unexpected error:")
      complete(InternalServerError, Message("An unexpected Error occured. Please inform your system administrator.", `ERROR`))
    }
  }

/*
  //FixMe: This should be imported somehow
  def myUserPassAuthenticator(userPassOption: Option[UserPass]): Future[Option[UserContext]] = {
    (userActorInstance ? new CheckUser(userPassOption)).mapTo[Option[UserContext]]
  }

  val authUserRoute = authenticate(BasicAuth(myUserPassAuthenticator _, realm = "Amanuensis")) { userContext =>
    userRoute
  }


  "The UserService" should {

  sequential

  "log in with existing user" in {
      Get("/user/login") ~> addCredentials(BasicHttpCredentials("wrong", "user")) ~> authUserRoute ~> check {
        responseAs[amanuensis.domain.UserContext] must beLike {
          case UserContext(login, name, permissions) => {
            login === "hallo"
            name === "Hallo Welt"
          }
        }
      }
    }

/*
    "fail to log in with non-existing user" in {
      Get("/user/login") ~> addCredentials(BasicHttpCredentials("wrong", "user")) ~> userRoute ~> check {
         status != OK
      }
      
    }
*/

  }
  */
}
