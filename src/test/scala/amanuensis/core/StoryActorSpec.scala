package amanuensis.core

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.ActorSystem
import org.specs2.mutable.SpecificationLike

//import scala.concurrent.duration._

import amanuensis.domain.Story


class StoryActorSpec extends TestKit(ActorSystem()) with SpecificationLike with CoreActors with Core with ImplicitSender {
  
  import StoryActor._


  sequential

  "StoryActor should" >> {

    "return story with given id" in {
      storyActor ! Retrieve(29)
    
      expectMsgPF() {
        case Story(Some(id: Long), _, _) => id mustEqual 29 //if id == 24 => success
      }

    }

/*    "accept valid user to be registered" in {
      registration ! Register(mkUser("jan@eigengo.com"))
      expectMsg(Right(Registered))
      success
    }
*/    
  }

}