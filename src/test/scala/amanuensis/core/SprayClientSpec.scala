package amanuensis.core

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.ActorSystem
import org.specs2.mutable.SpecificationLike

import scala.concurrent.Future

import spray.httpx.SprayJsonSupport
import spray.json._
import spray.http._

import spray.client.pipelining._

import spray.can.Http
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import spray.util._

import amanuensis.core.neo4j._


//import scala.concurrent.duration._


class SprayClientSpec extends TestKit(ActorSystem()) with SpecificationLike with CoreActors with Core 
  with ImplicitSender with SprayJsonSupport with DefaultJsonProtocol with Neo4JRestFormats {

  import system.dispatcher

  sequential

  "spray-client should" >> {

    "bla bla return story with given id" in {
     
        case class TestParams(p1: String, p2: Int)
        implicit val testParamsJsonFormat = jsonFormat2(TestParams)

        case class TestRow(a: String, c: Int)
        implicit val testRowJsonFormat = jsonArrayFormat2(TestRow)

        implicit val server = Neo4JServer("http://localhost:7474/db/data/cypher")

        val q = new Query[TestParams, TestRow]("start n=node(*) return n.title,id(n)",TestParams("A",1))

        q.execute onSuccess { 
          case r => println(r)
        }

        success
        
    }





/*    "accept valid user to be registered" in {
      registration ! Register(mkUser("jan@eigengo.com"))
      expectMsg(Right(Registered))
      success
    }
*/    
  }

}