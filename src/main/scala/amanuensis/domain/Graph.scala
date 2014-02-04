package amanuensis.domain

import spray.json.DefaultJsonProtocol

import amanuensis.api.exceptions.ValidationException


case class FindPathsRequest(sourceStoryId: String, tag: String, targetStoryId: String)

case class FindPathsResult(paths: Seq[Path])

case class Path(stories: Seq[StoryNode])

case class StoryNode(id: String, title: String)


object GraphProtocol extends DefaultJsonProtocol {
  // JSON-Serialization
  implicit val findPathsRequestJsonFormat = jsonFormat3(FindPathsRequest.apply)
  implicit val storyNodeJsonFormat = jsonFormat2(StoryNode.apply)
  implicit val pathJsonFormat = jsonFormat1(Path.apply)
  implicit val findPathsResultJsonFormat = jsonFormat1(FindPathsResult.apply)
}
