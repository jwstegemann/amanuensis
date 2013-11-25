package amanuensis.domain

import spray.json.DefaultJsonProtocol

case class Message(text: String, severity : String, details: String = "no details available", field: Option[String] = None)

object Severities {
	val `DEBUG` = "DEBUG"
	val `INFO` = "INFO"
	val `WARN` = "WARN"
	val `ERROR` = "ERROR"
	val `FATAL` = "FATAL"
}


object MessageJsonProtocol extends DefaultJsonProtocol {
  implicit val messageFormat = jsonFormat4(Message.apply)	
}