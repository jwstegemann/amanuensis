package amanuensis.story

import amanuensis.mongo.MongoJsonProtocol
import amanuensis.entity._

import reactivemongo.bson._


case class Story(_id : Option[BSONObjectID], title : String, content : String) extends Entity


object Story extends MongoJsonProtocol {
  // JSON-Serialization
  implicit val storyJsonFormat = jsonFormat3(Story.apply)

  // BSON-Serialization
  implicit val storyBsonHandler = Macros.handler[Story]

  /* Validation
  override def checks() = checkName _ :: Nil

  def checkName(schueler: Schueler): Result[Schueler] = {
    if (schueler.name.startsWith("Ca")) Right(schueler)
    else Left(Message("Namde muss mit Ca beginnen!",`ERROR`, field=Some("name")))
  }
  */
}