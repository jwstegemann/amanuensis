package amanuensis.core

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.ActorSystem
import org.specs2.mutable.SpecificationLike

import spray.httpx.SprayJsonSupport
import spray.json._

//import scala.concurrent.duration._


class SprayClientSpec extends TestKit(ActorSystem()) with SpecificationLike with CoreActors with Core with ImplicitSender with SprayJsonSupport with DefaultJsonProtocol {
  

  case class ResultX[T](columns: IndexedSeq[String], data: IndexedSeq[T])

  case class QueryX[T,R](query: String, params: T)
    (implicit val paramsJsonFormat: JsonFormat[T], val rowJsonFormat: JsonFormat[R]) extends DefaultJsonProtocol {

    val qf = jsonFormat(QueryX[T,R],"query","params")
    val rf = jsonFormat(ResultX[R], "columns", "data")

    def execute(): ResultX[R] = {

      println("QUERY: " + this.toJson(qf).toString)

      val result = """{
          "columns" : [ "type(r)", "n.name?", "n.age?" ],
          "data" : [ [ "know", 25 ], [ "know", 12 ] ]
        }""".asJson.convertTo[ResultX[R]](rf)
      
      result
    }

  } 

  


  type JFo[T] = JsonFormat[T] // simple alias for reduced verbosity

  def jsonArrayFormat2[A :JFo, B :JFo, T <: Product](construct: (A, B) => T) = {
    new RootJsonFormat[T] {
      def write(t: T) = serializationError("row is not to be written")
      def read(value: JsValue) = value match {
        case JsArray(a :: b  :: Nil) => construct(a.convertTo[A], b.convertTo[B])
        case x => deserializationError("Expected case class as JsArray")
      }
    }
  }


  sequential

  "spray-client should" >> {

    "bla bla return story with given id" in {
     
        case class TestParams(p1: String, p2: Int)
        implicit val testParamsJsonFormat = jsonFormat2(TestParams)

        case class TestRow(a: String, c: Int)
        implicit val testRowJsonFormat = jsonArrayFormat2(TestRow)

        val q = new QueryX[TestParams, TestRow]("testQuery",TestParams("A",1))

        println(q.execute)

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