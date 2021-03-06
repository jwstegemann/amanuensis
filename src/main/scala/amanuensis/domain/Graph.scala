package amanuensis.domain

import spray.json.DefaultJsonProtocol

import amanuensis.api.exceptions.ValidationException


case class StoryNode(id: String, title: String, content: String, created: String, createdBy: String, icon: String)

object GraphProtocol extends DefaultJsonProtocol {
  // JSON-Serialization
  implicit val storyNodeJsonFormat = jsonFormat6(StoryNode.apply)
}
