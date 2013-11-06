package amanuensis.system

import scala.language.implicitConversions

object Implicits {
	implicit def makeListFrom(message: Message) = message :: Nil
}

case class InternalServerErrorException(val messages: List[Message]) extends Exception

case class NotFoundException(val messages: List[Message]) extends Exception

case class ValidationException(val messages: List[Message]) extends Exception

case class BSONDeserializationError(msg: String) extends Exception