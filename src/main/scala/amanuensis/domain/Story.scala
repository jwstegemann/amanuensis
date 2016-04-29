package amanuensis.domain

import spray.json.DefaultJsonProtocol

import amanuensis.api.exceptions.ValidationException


case class Story(
  id : Option[String],
  title : String, 
  content : String,
  created: String,
  createdBy: String, 
  modified: String,
  modifiedBy: String,
  due: Option[String],
  tags: Seq[String],
  icon: String)  {

	def check() = {
		//FIXME: Implement checks here
	}
}

case class StoryInfo(id : String, title : String, created: String, modified: String, content: Option[String], icon: String)

case class StoryContext(story: Story, inSlots: Seq[Slot], outSlots: Seq[Slot], flags: StoryFlags)

case class StoryId(id: String, version: String)

case class StoryUpdateResult(id: String, updated: Int)

case class StoryFlags(canWrite: Int, stars: Int)

case class StoryIndex(id : Option[String], title : String, content : String, created: String, createdBy: String, 
  modified: String, modifiedBy: String,
  tags: Seq[String], icon: String, canRead: Seq[String])

case class StoryAccess(login: String, name: String, access: String, level: String)

case class StoryRights(canRead: Seq[String])


object StoryProtocol extends DefaultJsonProtocol {
	import SlotProtocol._

  // JSON-Serialization
  implicit val storyJsonFormat = jsonFormat10(Story.apply)
  implicit val storyInfoJsonFormat = jsonFormat6(StoryInfo.apply)
  implicit val storyIdJsonFormat = jsonFormat2(StoryId.apply)
  implicit val storyIndexJsonFormat = jsonFormat10(StoryIndex.apply)
  implicit val storyAccessJsonFormat = jsonFormat4(StoryAccess.apply)
  implicit val storyRightsJsonFormat = jsonFormat1(StoryRights.apply)  
  implicit val storyFlagsJsonFormat = jsonFormat2(StoryFlags.apply)    
  implicit val storyContextJsonFormat = jsonFormat4(StoryContext.apply)
  implicit val storyUpdateResultJsonFormat = jsonFormat2(StoryUpdateResult.apply)
}
