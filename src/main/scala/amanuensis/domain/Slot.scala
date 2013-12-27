package amanuensis.domain

import spray.json.DefaultJsonProtocol


case class Slot(name : String)


object SlotProtocol extends DefaultJsonProtocol {
  // JSON-Serialization
  implicit val slotJsonFormat = jsonFormat1(Slot.apply)
}