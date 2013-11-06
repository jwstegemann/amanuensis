package amanuensis.mongo

import reactivemongo.bson._
import reactivemongo.core.commands.LastError
import akka.actor.ActorLogging
import scala.concurrent.{Future, ExecutionContext}

import amanuensis.system._
import amanuensis.system.Severities._
import amanuensis.system.Implicits._

trait LastErrorMapping { self: ActorLogging => 

	implicit class FutureOfLastError(lastError: Future[LastError]) {

		def mapToInserted(id: String)(implicit ex: ExecutionContext) = {
			lastError.map {
				case LastError(true, _, _, _, _, _, _) => Inserted(id)
			}
		}

		def mapToDeleted(id: String)(implicit ex: ExecutionContext) = {
			lastError.map {
				case LastError(true, _, _, _, _, _, _) => Deleted(id)
			}
		}

		def mapToUpdated(implicit ex: ExecutionContext) = {
			lastError.map {
				case LastError(true, _, _, _, _, n, _) => {
					if (n == 0) throw NotFoundException(Message("Sorry, no entity matching your criteria was found to be updated",`ERROR`,"no details available"))
					else Updated(n)
				}
			}
		}

		def recoverWithInternalServerError(implicit ex: ExecutionContext): Future[LastError] = {
			lastError.recover {
	      case LastError(_, err, code, msg, doc, _, _) => {
	        log.error("mongoDB-Error: {}{}{}{}",err.getOrElse(""),
	          code match {
	            case Some(s : Int) => s" with code '$s'"
	            case None => ""
	          },
	          msg match {
	            case Some(s) => s" with message '$s'"
	            case None => ""
	          },
	          doc match {
	            case Some(doc) => {
	              val pretty = BSONDocument.pretty(doc)
	              s" in document '$pretty'"
	            }
	            case None => ""
	          }
	        )
	        throw InternalServerErrorException(
	          Message("A database error occured. Please inform your system-administrator.", `ERROR`, err.getOrElse("no details available")) :: Nil
	        )
	      }
	    }
		}

	}

}
