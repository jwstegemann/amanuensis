package amanuensis.story

import spray.routing._

import spray.json._
import spray.httpx.marshalling._
import spray.httpx.SprayJsonSupport

import akka.actor.ActorLogging


// this trait defines our service behavior independently from the service actor
trait StoryHttpService { self : ActorLogging =>


}
