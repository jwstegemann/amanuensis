package amanuensis.core.elasticsearch

import spray.json.DefaultJsonProtocol
import amanuensis.domain.{Story, StoryProtocol}


case class QueryResult(took: Int, hits: Hits)

case class Hits(total: Int, max_score: Double, hits: Seq[Hit])

case class Hit(_id: String, _score: Double, _source: Story)


object ElasticSearchProtocol extends DefaultJsonProtocol {
  import StoryProtocol._

  // JSON-Serialization
  implicit val hitJsonFormat = jsonFormat3(Hit)
  implicit val hitsJsonFormat = jsonFormat3(Hits)
  implicit val queryResultJsonFormat = jsonFormat2(QueryResult)
}