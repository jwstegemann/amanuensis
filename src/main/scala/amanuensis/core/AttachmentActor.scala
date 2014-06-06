package amanuensis.core

import akka.actor.{Actor, ActorLogging}
import akka.pattern._

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import amanuensis.core.util.Failable

import amanuensis.core.util.StringUtils

import scala.concurrent.Future
import scala.util.{Try, Success, Failure}

import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.AmazonS3Client

import spray.http._
import java.io.{ FileOutputStream }

import java.io.File


object AttachmentActor {

  /*
   * messages to be used with this actor
   */
  
  case class Store(filename: String, storyId: String, content: Array[Byte])
  case class Retrieve(filename: String, storyId: String)
}


/**
 * Registers the users. Replies with
 */
class AttachmentActor extends Actor with ActorLogging with Failable {

  import AttachmentActor._

  //FIXME: use dedicated pool
  implicit def executionContext = context.dispatcher
  implicit val system = context.system
  def actorRefFactory = system

  /*
   * config
   */

  val s3Key = scala.util.Properties.envOrElse("AWS_S3_KEY", "none")
  val s3Secret = scala.util.Properties.envOrElse("AWS_S3_SECRET", "none")

  val s3BucketName = scala.util.Properties.envOrElse("AWS_S3_BUCKET", "none")

  val local = s3Key == "local"

  private val store = if (local) storeLocal _ else storeS3 _
  private val retrieve = if (local) retrieveLocal _ else retrieveS3 _

  val credentials = if (!local) new BasicAWSCredentials(s3Key, s3Secret) else null
  val s3client = if (!local) new AmazonS3Client(credentials) else null


  override def preStart =  {
    log.info(s"AttachmentActor started at: {} {} @ {}", self.path, s3BucketName, if (local) "local" else "S3")
  }

  def receive = {
    case Store(filename, storyId, content) => store(filename, storyId, content)
    case Retrieve(filename, storyId) => retrieve(filename, storyId) pipeTo sender
  }


  
  def retrieveLocal(filename: String, storyId: String) : Future[HttpData] = {
    val sourceFile = new File(s"$s3BucketName/$storyId/$filename")       
    if (!sourceFile.isFile || !sourceFile.canRead) {
      throw NotFoundException(Message(s"no such attachment available",`ERROR`) :: Nil)
    }
    Future {
      HttpData(sourceFile)
    }
  }

  def storeLocal(filename: String, storyId: String, content: Array[Byte]) : Unit = {
    //FIXME: filename absichern (kein . oder .. am Anfang!)
    val targetFile = new File(s"$s3BucketName/$storyId/$filename")

    targetFile.getParentFile().mkdirs()

    if (!targetFile.exists()) {
      targetFile.createNewFile()
    }

    val outputStream = new FileOutputStream(targetFile)
    outputStream.write(content)
    outputStream.close();    
  }

  def retrieveS3(filename: String, storyId: String) : Future[HttpData] = {
    Future {
      HttpData("shdfjsdfsfd")
    }
  }

  def storeS3(filename: String, storyId: String, content: Array[Byte]) : Unit = {

  }

}