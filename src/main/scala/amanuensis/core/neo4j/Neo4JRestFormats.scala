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

  implicit def seq2JsonArray[T](s: Seq[T])(implicit format: JsonFormat[T]) = s.toJson 
}


trait Neo4JRestFormats { this: StandardFormats with CollectionFormats => 

  type JFo[T] = JsonFormat[T] // simple alias for reduced verbosity

  def jsonCaseClassArrayFormat[A :JFo, T <: Product](construct: (A) => T) = {
    new RootJsonFormat[T] {
      def write(t: T) = serializationError("row is not to be written")
      def read(value: JsValue) = value match {
        case JsArray(a :: Nil) => construct(a.convertTo[A])
        case x => deserializationError("Expected case class as JsArray")
      }
    }
  }

  def jsonCaseClassArrayFormat[A :JFo, B :JFo, T <: Product](construct: (A, B) => T) = {
    new RootJsonFormat[T] {
      def write(t: T) = serializationError("row is not to be written")
      def read(value: JsValue) = value match {
        case JsArray(a :: b  :: Nil) => construct(a.convertTo[A], b.convertTo[B])
        case x => deserializationError("Expected case class as JsArray")
      }
    }
  }

  def jsonCaseClassArrayFormat[A: JFo, B: JFo, C: JFo, T <: Product](construct: (A, B, C) => T) = {
    new RootJsonFormat[T] {
      def write(t: T) = serializationError("row is not to be written")
      def read(value: JsValue) = value match {
        case JsArray(a :: b :: c :: Nil) => construct(a.convertTo[A], b.convertTo[B], c.convertTo[C])
        case x => deserializationError("Expected case class as JsArray")
      }
    }
  }

  def jsonCaseClassArrayFormat[A: JFo, B: JFo, C: JFo, D: JFo, T <: Product](construct: (A, B, C, D) => T) = {
    new RootJsonFormat[T] {
      def write(t: T) = serializationError("row is not to be written")
      def read(value: JsValue) = value match {
        case JsArray(a :: b :: c :: d :: Nil) => construct(a.convertTo[A], b.convertTo[B], c.convertTo[C], d.convertTo[D])
        case x => deserializationError("Expected case class as JsArray")
      }
    }
  }  

  def jsonCaseClassArrayFormat[A: JFo, B: JFo, C: JFo, D: JFo, E: JFo, T <: Product](construct: (A, B, C, D, E) => T) = {
    new RootJsonFormat[T] {
      def write(t: T) = serializationError("row is not to be written")
      def read(value: JsValue) = value match {
        case JsArray(a :: b :: c :: d :: e :: Nil) => construct(a.convertTo[A], b.convertTo[B], c.convertTo[C], d.convertTo[D], e.convertTo[E])
        case x => deserializationError("Expected case class as JsArray")
      }
    }
  }  

  def jsonCaseClassArrayFormat[A: JFo, B: JFo, C: JFo, D: JFo, E: JFo, F: JFo, T <: Product](construct: (A, B, C, D, E, F) => T) = {
    new RootJsonFormat[T] {
      def write(t: T) = serializationError("row is not to be written")
      def read(value: JsValue) = value match {
        case JsArray(a :: b :: c :: d :: e :: f :: Nil) => construct(a.convertTo[A], b.convertTo[B], c.convertTo[C], d.convertTo[D], 
          e.convertTo[E], f.convertTo[F])
        case x => deserializationError("Expected case class as JsArray")
      }
    }
  }

  def jsonCaseClassArrayFormat[A: JFo, B: JFo, C: JFo, D: JFo, E: JFo, F: JFo, G: JFo, H: JFo, I: JFo, J: JFo, T <: Product](construct: (A, B, C, D, E, F, G, H, I, J) => T) = {
    new RootJsonFormat[T] {
      def write(t: T) = serializationError("row is not to be written")
      def read(value: JsValue) = value match {
        case JsArray(a :: b :: c :: d :: e :: f :: g :: h :: i :: j :: Nil) => construct(a.convertTo[A], b.convertTo[B], c.convertTo[C], d.convertTo[D], 
          e.convertTo[E], f.convertTo[F], g.convertTo[G], h.convertTo[H], i.convertTo[I], j.convertTo[J])
        case x => deserializationError("Expected case class as JsArray")
      }
    }
  } 

  def jsonCaseClassArrayFormat[A: JFo, B: JFo, C: JFo, D: JFo, E: JFo, F: JFo, G: JFo, H: JFo, I: JFo, J: JFo, K: JFo, T <: Product](construct: (A, B, C, D, E, F, G, H, I, J, K) => T) = {
    new RootJsonFormat[T] {
      def write(t: T) = serializationError("row is not to be written")
      def read(value: JsValue) = value match {
        case JsArray(a :: b :: c :: d :: e :: f :: g :: h :: i :: j :: k :: Nil) => construct(a.convertTo[A], b.convertTo[B], c.convertTo[C], d.convertTo[D], 
          e.convertTo[E], f.convertTo[F], g.convertTo[G], h.convertTo[H], i.convertTo[I], j.convertTo[J], k.convertTo[K])
        case x => deserializationError("Expected case class as JsArray")
      }
    }
  }   

}