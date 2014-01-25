package amanuensis.api

import scala.concurrent.duration._
import scala.util.{ Success, Failure }
import akka.pattern.ask
import akka.actor.ActorLogging
import spray.util._

import akka.actor.{ Props, Actor }
import spray.http._
import spray.http.StatusCodes._
import spray.http.MediaTypes._
import spray.routing._
import spray.http.BodyPart
import java.io.{ FileOutputStream }

import amanuensis.core.neo4j.Neo4JId

import com.roundeights.s3cala._

import java.io.File
import spray.routing.directives.ContentTypeResolver
import scala.concurrent.Future


// this trait defines our service behavior independently from the service actor
trait AttachmentHttpService extends HttpService { self : ActorLogging =>

  private implicit def executionContext = actorRefFactory.dispatcher
  
  val s3Key = scala.util.Properties.envOrElse("aws_s3_key", "none")
  val s3Secret = scala.util.Properties.envOrElse("aws_s3_secret", "none")

  val s3BucketName = scala.util.Properties.envOrElse("aws_s3_bucket", "none")

  val s3 = S3(s3Key,s3Secret)
  val bucket = s3.bucket(s3BucketName)


  val attachmentRoute = {

    import spray.httpx.encoding.{ NoEncoding, Gzip }
    
    pathPrefix("attachment") {
      //FixMe: check, if storyId is valid
      pathPrefix(Segment) { storyId: String =>
        pathEnd {
          post {
            entity(as[MultipartFormData]) { formData =>

              formData.get("file") match {

                case Some(bodyPart) => {

                  val filename = bodyPart.filename match {
                    case Some(name) => name
                    case None => Neo4JId.generateId()
                  }

                  val file = bodyPart.entity.data.toByteArray

                  val futureUpload = bucket.put(s"$storyId/$filename", file) map (nothing => 
                    s"""{"filename": "/attachment/$storyId/$filename"}"""
                  )                

                  complete(futureUpload) 

                }
                
                case None => complete(BadRequest, "invalid upload, missing file...")

              }

            }
          }
        } ~
        path(Segment) { filename: String =>
          detach() {
            val file = File.createTempFile("S3-",".tmp")            // use Metadata
            file.deleteOnExit()
            
            respondWithLastModifiedHeader(file.lastModified) {

              val result: Future[HttpEntity] = bucket.get(s"$storyId/$filename", file) map (metaData =>
                if (file.isFile && file.canRead) {
                    //autoChunk(settings.fileChunkingThresholdSize, settings.fileChunkingChunkSize) {
                      //ToDo: use MetaData for content-type
                  HttpEntity(ContentTypeResolver.Default(file.getName), HttpData(file))
                    //}
                
                } 
                else {
                  throw new Exception("Fehler!")
                }              
              )

              complete(result) 
            }
          }       
        }
      }
    }

  }

}