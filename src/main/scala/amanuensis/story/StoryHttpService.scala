package amanuensis.story

import scala.concurrent.Future

import spray.routing._

import spray.json._
import spray.httpx.marshalling._
import spray.httpx.SprayJsonSupport

import spray.httpx.unmarshalling.Unmarshaller
import spray.httpx.marshalling.Marshaller

import akka.actor.ActorLogging

//import amanuensis.story.Story._
import amanuensis.auth.UserContext
import amanuensis.system._
import amanuensis.services.MessageHandling

import amanuensis.system.MessageJsonProtocol._

import amanuensis.services.EntityHttpService
import amanuensis.entity._


// this trait defines our service behavior independently from the service actor
trait StoryHttpService extends EntityHttpService { self : ActorLogging =>

  def storyRoute = route[Story]("story", actorRefFactory.actorSelection("/user/story"), null)

  // (userContext: UserContext)

}
