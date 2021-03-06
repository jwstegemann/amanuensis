package amanuensis.core.elasticsearch

import spray.json.DefaultJsonProtocol
import amanuensis.domain.{Story, StoryProtocol}

case class QueryRequest(query: String, tags: Seq[String], fromDate: Option[String], page: Int)

case class QueryResult(took: Int, hits: Hits, facets: Facets)

case class Hits(total: Int, max_score: Double, hits: Seq[Hit])

case class Hit(_id: String, _score: Double, _source: Story)

case class Facets(tags: Tags, dates: Dates)

case class Tags(total: Int, terms: Seq[Term])

case class Term(term: String, count: Int)

case class Dates(ranges: Seq[Range])

case class Range(from_str: String, count: Int)

case class SuggestResult(suggest: Seq[Suggestion])

case class Suggestion(options: Seq[SuggestOption])

case class SuggestOption(text: String, score: Double)


object ElasticSearchProtocol extends DefaultJsonProtocol {
  import StoryProtocol._

  // JSON-Serialization
  implicit val hitJsonFormat = jsonFormat3(Hit)
  implicit val hitsJsonFormat = jsonFormat3(Hits)
  implicit val queryRequestJsonFormat = jsonFormat4(QueryRequest)
  
  implicit val termJsonFormat = jsonFormat2(Term)
  implicit val tagsJsonFormat = jsonFormat2(Tags)

  implicit val rangeJsonFormat = jsonFormat2(Range)
  implicit val datesJsonFormat = jsonFormat1(Dates)

  implicit val facetsJsonFormat = jsonFormat2(Facets)
  implicit val queryResultJsonFormat = jsonFormat3(QueryResult)

  implicit val suggestOptionJsonFormat = jsonFormat2(SuggestOption)
  implicit val suggestionJsonFormat = jsonFormat1(Suggestion)
  implicit val SuggestResultJsonFormat = jsonFormat1(SuggestResult)
}