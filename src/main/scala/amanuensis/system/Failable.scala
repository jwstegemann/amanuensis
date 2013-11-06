package amanuensis.system

import scala.concurrent.Future
import akka.actor._
import akka.pattern.pipe


trait Failable { self: Actor =>
	// provide ExecutionContext
  implicit val ec = context.dispatcher

   /*
   * replies with excetion as answer to sender
   */
  def failWith(ex: Exception) = { 
    Future.failed(ex) pipeTo sender
  }
}