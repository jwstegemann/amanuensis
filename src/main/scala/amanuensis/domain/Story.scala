package amanuensis.domain

import spray.json.DefaultJsonProtocol


case class Story(id : Option[String], title : String, content : String)

case class StoryInfo(id : String, title : String)

case class StoryContext(story: Story, inSlots: Seq[String], outSlots: Seq[String])

object StoryProtocol extends DefaultJsonProtocol {
  // JSON-Serialization
  implicit val storyJsonFormat = jsonFormat3(Story.apply)
  implicit val storyInfoJsonFormat = jsonFormat2(StoryInfo.apply)
  implicit val storyContextJsonFormat = jsonFormat3(StoryContext.apply)
}
