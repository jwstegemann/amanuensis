package amanuensis.story

import spray.routing._

import spray.json._
import spray.httpx.marshalling._
import spray.httpx.SprayJsonSupport

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

  def storyRoute(userContext: UserContext) = route[Story]("story", actorRefFactory.actorSelection("/user/story"), userContext)

}
