package amanuensis.story

import amanuensis.mongo.EntityActor
import amanuensis.entity._

import amanuensis.story.Story._

import amanuensis.system._
import amanuensis.system.Severities._


//case class Test(msg: String)


class StoryActor extends EntityActor[Story]("story") {

  /* override def receive = super.receive orElse {
    case Test(msg) => log.debug(msg)
  }
  */

}