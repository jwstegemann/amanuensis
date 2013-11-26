package amanuensis.domain

import spray.json.DefaultJsonProtocol


case class Story(_id : Option[Long], title : String, content : String)


object Story extends DefaultJsonProtocol {
  // JSON-Serialization
  implicit val storyJsonFormat = jsonFormat3(Story.apply)

}