package amanuensis.core.neo4j

import spray.httpx.SprayJsonSupport
import spray.json._
import spray.util._


//import scala.concurrent.duration._

trait Neo4JJsonProtocol extends DefaultJsonProtocol with Neo4JRestFormats {
  implicit def string2JsonObject(v: String) = JsString(v) 

  implicit def int2JsonObject(v: Int) = JsNumber(v) 

  implicit def long2JsonObject(v: Long) = JsNumber(v) 

  implicit def double2JsonObject(v: Double) = JsNumber(v)
  
  implicit def boolean2JsonObject(v: Boolean) = JsBoolean(v)
}


trait Neo4JRestFormats { this: StandardFormats => 

  type JFo[T] = JsonFormat[T] // simple alias for reduced verbosity

  def jsonCaseClassArrayFormat[A :JFo, B :JFo, T <: Product](construct: (A, B) => T) = {
    new RootJsonFormat[T] {
      def write(t: T) = serializationError("row is not to be written")
      def read(value: JsValue) = value match {
        case JsArray(a :: b  :: Nil) => construct(a.convertTo[A], b.convertTo[B])
        case x => deserializationError("Expected case class as JsArray")
      }
    }
  }

}