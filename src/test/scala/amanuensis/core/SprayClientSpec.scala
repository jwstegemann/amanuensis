package amanuensis.core

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.ActorSystem

import org.specs2.mutable.SpecificationLike
import org.specs2.time.NoTimeConversions

import scala.concurrent.Future

import spray.httpx.SprayJsonSupport
import spray.json._
import spray.http._

import spray.client.pipelining._

import spray.can.Http
import spray.httpx.SprayJsonSupport
import spray.http.StatusCodes.Success
import spray.client.pipelining._
import spray.util._

import scala.concurrent._
import scala.concurrent.duration._

import amanuensis.core.neo4j._


//import scala.concurrent.duration._


class SprayClientSpec extends TestKit(ActorSystem()) with NoTimeConversions with SpecificationLike with CoreActors with Core 
  with ImplicitSender with SprayJsonSupport with Neo4JJsonProtocol  {

  import system.dispatcher

  final val serverUrl = "http://localhost:7474/db/data/cypher"
  final val queryString = "start n=node(*) return n.title,id(n)"
  final val createString = "CREATE (n:Story { title: {title}, content: {content} }) RETURN n.title"

  sequential

  "neo4j spray-client should" >> {

    "return list of case class for semi-typed query" in {
      case class TestRow(a: String, c: Int)
      implicit val testRowJsonFormat = jsonCaseClassArrayFormat(TestRow)

      implicit val server = Neo4JServer(serverUrl)

      val q = new Query()

      val res = q.list[TestRow](queryString)

      res onSuccess { 
        case r => println("C +++++ " + r)
      }

      res onFailure {
        case e => println("C ----- " + e)
      }

      Await.result(res, 2 seconds) must not be empty
    }
    

    "create a new node correctly" in {

      implicit val server = Neo4JServer(serverUrl)

      val q = new Query()

      val res = q.execute(createString, ("title" -> "Story21"), ("content" -> "Test-Content 2"))

      Await.result(res, 2 seconds).status must beAnInstanceOf[Success]
    }
  }



}