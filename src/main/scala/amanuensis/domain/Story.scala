package amanuensis.domain

import spray.json.DefaultJsonProtocol

import amanuensis.api.exceptions.ValidationException


case class Story(id : Option[String], title : String, content : String, created: String, createdBy: String)  {
	def check() = {
		//FIXME: Implement checks here
	}
}

case class StoryInfo(id : String, title : String, created: String, content: Option[String])

case class StoryContext(story: Story, inSlots: Seq[Slot], outSlots: Seq[Slot])


object StoryProtocol extends DefaultJsonProtocol {
	import SlotProtocol._

  // JSON-Serialization
  implicit val storyJsonFormat = jsonFormat5(Story.apply)
  implicit val storyInfoJsonFormat = jsonFormat4(StoryInfo.apply)
  implicit val storyContextJsonFormat = jsonFormat3(StoryContext.apply)
}
