package amanuensis.services

import spray.routing.HttpService

trait SessionAware { self: HttpService =>

	protected implicit val sessionServiceActor = actorRefFactory.actorSelection("/user/sessionService")

}