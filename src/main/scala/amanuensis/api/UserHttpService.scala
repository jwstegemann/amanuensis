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

import amanuensis.domain.{UserContext, UserContextProtocol}

import StatusCode._


trait UserHttpService extends HttpService with SprayJsonSupport  { self: ActorLogging =>

  import UserContextProtocol._


  def userRoute(userContext: UserContext) = {
    /*
       * return UserContext when successfully logged in
       */
      path("user" / "login") {
        get {
          log.info(userContext.login + " logged in (or tried at least)")
          complete(userContext);
        }
      }
  }

}