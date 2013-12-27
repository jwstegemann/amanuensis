package amanuensis.api

import amanuensis.core.{CoreActors, Core}

import akka.actor.Props

import spray.json.DefaultJsonProtocol


/**
 * The REST API layer. It exposes the REST services, but does not provide any
 * web server interface.<br/>
 * Notice that it requires to be mixed in with ``core.CoreActors``, which provides access
 * to the top-level actors that make up the system.
 */
trait Api { this: CoreActors with Core =>

  private implicit val _ = system.dispatcher

  val rootService = system.actorOf(Props[RootServiceActor], "root-service")

}