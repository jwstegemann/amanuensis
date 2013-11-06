package amanuensis.system

import spray.json.DefaultJsonProtocol

case class Message(text: String, severity : String, details: String = "no details available", field: Option[String] = None)

case class Inserted(id: String)
case class Updated(number: Int)
case class Deleted(id: String)

case class Severity(severity: String) {
	override def toString() = severity
}

object Severities {
	val `DEBUG` = "DEBUG"
	val `INFO` = "INFO"
	val `WARN` = "WARN"
	val `ERROR` = "ERROR"
	val `FATAL` = "FATAL"
}

trait MessageFormats { self: DefaultJsonProtocol => 
  implicit val severityFormat = jsonFormat1(Severity)
  implicit val messageFormat = jsonFormat4(Message.apply)	

  implicit val insertedFormat = jsonFormat1(Inserted)	
  implicit val updatedFormat = jsonFormat1(Updated)	
  implicit val deletedFormat = jsonFormat1(Deleted)	
}

object MessageJsonProtocol extends DefaultJsonProtocol with MessageFormats {
}
