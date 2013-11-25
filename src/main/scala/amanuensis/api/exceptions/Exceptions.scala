package amanuensis.api.exceptions

import scala.language.implicitConversions
import amanuensis.domain.Message

object Implicits {
	implicit def makeListFrom(message: Message) = message :: Nil
}

case class InternalServerErrorException(val messages: List[Message]) extends Exception

case class NotFoundException(val messages: List[Message]) extends Exception

case class ValidationException(val messages: List[Message]) extends Exception
