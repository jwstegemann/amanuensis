package amanuensis.core

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.ActorSystem
import org.specs2.mutable.SpecificationLike

import spray.json._

//import scala.concurrent.duration._


class CypherActorSpec extends TestKit(ActorSystem()) with SpecificationLike with CoreActors with Core with ImplicitSender {
  
  import CypherActor._


  sequential

  "CypherActor should" >> {

    "bla bla return story with given id" in {
      cypherActor ! Query("test", Map("param1" -> JsNumber(17)))
    
/*      expectMsgPF() {
        case Story(Some(id: Long), _, _) => id mustEqual 29 //if id == 24 => success
      }
*/

      expectMsgPF() {
        //case msg: String => msg mustEqual ""
        case msg: Result => msg.data(0)(2) mustEqual JsNumber(25)
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