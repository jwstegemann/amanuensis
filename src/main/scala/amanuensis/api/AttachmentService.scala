package amanuensis.api

import scala.concurrent.duration._
import scala.util.{ Success, Failure }
import akka.pattern.ask
import akka.actor.ActorLogging
import spray.util._

import akka.actor._
import spray.http._
import spray.can.Http
import spray.http.StatusCodes._
import spray.http.MediaTypes._
import spray.routing._
import spray.http.BodyPart
import java.io.{ FileOutputStream }

import amanuensis.core.neo4j.Neo4JId

import spray.routing.directives.ContentTypeResolver
import scala.concurrent.Future

import spray.http.HttpHeaders._
import spray.http.CacheDirectives._

import amanuensis.core.AttachmentActor._

import akka.util.Timeout
import scala.concurrent.duration.DurationInt
import language.postfixOps

import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata

import java.io.{File, ByteArrayInputStream, IOException, InputStream}

import amanuensis.api.exceptions._

import amanuensis.domain.Message
import amanuensis.domain.Severities._

import akka.actor.ActorRefFactory
import scala.concurrent.duration._

import java.util.Arrays


// this trait defines our service behavior independently from the service actor
trait AttachmentHttpService extends HttpService { self : ActorLogging =>

  private implicit val timeout = new Timeout(5 seconds)
  private implicit def executionContext = actorRefFactory.dispatcher

  /*
   * config
   */

  val s3Key = scala.util.Properties.envOrElse("AWS_S3_KEY", "none")
  val s3Secret = scala.util.Properties.envOrElse("AWS_S3_SECRET", "none")

  val s3BucketName = scala.util.Properties.envOrElse("AWS_S3_BUCKET", "none")

  val local = (s3Key == "local")

  val s3ChunkSize = 5000
  val s3ChunkThreshold = 2500

  val credentials = if (!local) new BasicAWSCredentials(s3Key, s3Secret) else null
  val s3Client = if (!local) new AmazonS3Client(credentials) else null


  val attachmentRoute = {

    import spray.httpx.encoding.{ NoEncoding, Gzip }
    
    pathPrefix("attachment") {
      //FixMe: check, if storyId is valid
      pathPrefix(Segment) { storyId: String =>
        pathEnd {
          post {
            entity(as[MultipartFormData]) { formData =>
              detach() {
                formData.get("file") match {

                  case Some(bodyPart) => {

                    val filename = bodyPart.filename match {
                      case Some(name) => name
                      case None => Neo4JId.generateId()
                    }

                    val content = bodyPart.entity.data.toByteArray

                    /*
                     * store local
                     */
                    validate(local, "") {
                      //FIXME: filename absichern (kein . oder .. am Anfang!)
                      val targetFile = new File(s"$s3BucketName/$storyId/$filename")

                      targetFile.getParentFile().mkdirs()

                      if (!targetFile.exists()) {
                        targetFile.createNewFile()
                      }

                      val outputStream = new FileOutputStream(targetFile)
                      outputStream.write(content)
                      outputStream.close(); 

                      complete(s"""{"filename": "/attachment/$storyId/$filename"}""")                     
                    } ~
                    /*
                     * store S3
                     */                    
                    validate(!local, "") {
                      val metaData = new ObjectMetadata()
                      metaData.setContentLength(content.length)
                      s3Client.putObject(s3BucketName, s"$storyId/$filename", new ByteArrayInputStream(content), metaData)

                      complete(s"""{"filename": "/attachment/$storyId/$filename"}""") 
                    }

                  }
                  
                  case None => complete(BadRequest, "invalid upload, missing file...")

                }
              }
            }
          }
        } ~
        path(Segment) { filename: String =>
          /*
           * get local
           */
          get {
            if (!local) reject  
            else getFromFile(s"$s3BucketName/$storyId/$filename")
          } ~
          /*
           * get S3
           */          
          get {
            if (local) reject
            else streamFromS3(storyId, filename, _)
          }           
        }
      }
    }
  }

  case class Ok()

  def streamFromS3(storyId: String, filename: String, ctx: RequestContext): Unit = {

    actorRefFactory.actorOf {
      //FIXME: create own pool for s3-actors
      Props {
        new Actor with ActorLogging {

          //FIXME: check for not found
          val s3Object = s3Client.getObject(s3BucketName, s"$storyId/$filename")          
          val inputStream = s3Object.getObjectContent()
          val metaData = s3Object.getObjectMetadata()

          val buffer = new Array[Byte](s3ChunkSize)

          // we use the successful sending of a chunk as trigger for scheduling the next chunk

          val responseStart = HttpResponse(
            entity = HttpEntity(ContentTypeResolver.Default(filename),HttpData(fillBuffer())),
            headers = `Last-Modified`(DateTime(metaData.getLastModified().getTime())) :: Nil
          )
          ctx.responder ! ChunkedResponseStart(responseStart).withAck(Ok())

          def receive = {
            case Ok() =>
              val msg = fillBuffer()

              //log.debug("********* BYTES READ: " + msg.length)

              if (msg.length > 0) {
                val nextChunk = MessageChunk(msg)
                ctx.responder ! nextChunk.withAck(Ok())
              }
              else {
                ctx.responder ! ChunkedMessageEnd
                context.stop(self)
              }

            case ev: Http.ConnectionClosed =>
              log.error("Stopping S3- treaming due to {}", ev)
              inputStream.abort()
          }


          def fillBuffer() : Array[Byte] = {
            var start = 0
            var bytesRead = 0

            do {
              bytesRead = inputStream.read(buffer,start,buffer.length - start)
              start += bytesRead
            } while (bytesRead != -1 && start < s3ChunkThreshold)

            if (start == -1) {
              Array[Byte]()
            } 
            else if (start == buffer.length) {
              buffer
            } else {
              Arrays.copyOfRange(buffer, 0, start)
            }
          }

        }
      }
    }
  }

}