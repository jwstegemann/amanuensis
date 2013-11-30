package amanuensis.api

import amanuensis.core.{CoreActors, Core}
import amanuensis.core.{Created}

import akka.actor.Props

import spray.json.DefaultJsonProtocol


object CoreJsonProtocol extends DefaultJsonProtocol {
  implicit val createdJsonFormat = jsonFormat1(Created.apply)
}

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