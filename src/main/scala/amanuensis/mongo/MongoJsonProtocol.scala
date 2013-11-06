package amanuensis.mongo

import spray.json._

import reactivemongo.bson.{BSONObjectID}

/*
 * Allows to serialize objects using special mongo-types to JSON and back
 */
trait MongoJsonProtocol extends DefaultJsonProtocol {

  /*
   * provides conversion of BSONObjectID used by mongoDB into a valid JSON-String and back
   */
  implicit object BSONObjectIDFormat extends RootJsonFormat[BSONObjectID] {
    def write(id: BSONObjectID) =
      JsString(id.stringify)

    def read(value: JsValue) = value match {
      case JsString(id) =>
        new BSONObjectID(id)
      case _ => deserializationError("BSONObjectID expected")
    }
  }

}