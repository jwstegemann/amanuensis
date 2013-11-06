package amanuensis.services

import spray.http._
import spray.routing._
import spray.routing.PathMatchers.PathEnd
import spray.util._
import MediaTypes._
import StatusCodes._
import Directives._

//import spray.httpx.unmarshalling.pimpHttpEntity
import spray.json._
import spray.httpx.marshalling._
import spray.httpx.SprayJsonSupport

import spray.httpx.unmarshalling.Unmarshaller
import spray.httpx.marshalling.Marshaller

import scala.concurrent.duration._
import scala.concurrent._
import akka.util.Timeout

import language.postfixOps

import akka.pattern.ask
import akka.actor.{ActorLogging, ActorSelection}

import amanuensis.auth.{SessionCookieAuth, UserContext}
import amanuensis.system._

import amanuensis.mongo.EntityActor

import amanuensis.entity._
import scala.reflect._

import amanuensis.entity.EntityJsonProtocol._


trait EntityHttpService extends HttpService with SprayJsonSupport with MessageHandling { self: ActorLogging =>

  private implicit val timeout = new Timeout(5 seconds)

  protected implicit def executionContext = actorRefFactory.dispatcher

  val logger = log


  def route[T <: Entity: ClassTag](prefix: String, entityActor: ActorSelection, userContext: UserContext)
  	(implicit marshaller: spray.httpx.marshalling.Marshaller[Future[T]],
  		listMarshaller: Marshaller[Future[List[T]]],
  		insMarsh: Marshaller[Future[Inserted]],
  		delMarsh: Marshaller[Future[Deleted]],
  		updMarsh: Marshaller[Future[Updated]],
  		unmarshaller: Unmarshaller[T]) = {
	
    pathPrefix(prefix) {
			path(PathEnd) {
			  post {
			    entity(as[T]) { item =>
			      complete((entityActor ? Create(item)).mapTo[Inserted])
			    }
			  } ~
			  get {
			    //TODO: is this necessary or is it enough to be called just once per change
			    dynamic {
			    	logger.info("entityervice: {}, {}, {} ", prefix, entityActor, userContext )
			      complete((entityActor ? FindAll()).mapTo[List[T]])
			    }
			  }
			} ~ 
			path(Rest) { id: String =>
			  get {
			    dynamic {
				  complete((entityActor ? Load(id)).mapTo[T])
				}
				  } ~
			  delete {
			    dynamic {
			      complete((entityActor ? Delete(id)).mapTo[Deleted])
			    }
			  } ~
			  put {
			    entity(as[T]) { item =>
			      complete((entityActor ? Update(item)).mapTo[Updated])
			    }
			  }
			}
    }
  }
}