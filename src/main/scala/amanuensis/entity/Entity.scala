package amanuensis.entity

import spray.json.DefaultJsonProtocol
import spray.json._

import reactivemongo.bson._

abstract class Entity {
	def _id: Option[BSONObjectID]
//	def version: long
}

object Entity {

}

object EntityJsonProtocol extends DefaultJsonProtocol {
  implicit object amanuensisEntityFormat extends RootJsonFormat[Entity] {
   	def write(e: Entity) = JsString("abstract entity with id " + e.toString)
   	def read(value: JsValue) = deserializationError("pure Entity cannot be deserialized")
  }
}