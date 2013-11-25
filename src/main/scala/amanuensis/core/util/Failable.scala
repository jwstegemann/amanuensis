package amanuensis.core.util

import scala.concurrent.{Future, ExecutionContext}
import akka.actor.Actor
import akka.pattern.pipe


trait Failable { self: Actor =>

   /*
   * replies with excetion as answer to sender
   */
  def failWith(ex: Exception)(implicit ec: ExecutionContext) = { 
    Future.failed(ex) pipeTo sender
  }
}