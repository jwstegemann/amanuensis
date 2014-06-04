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

import spray.http.HttpHeaders._
import spray.http.CacheDirectives._


import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.AmazonS3Client


// this trait defines our service behavior independently from the service actor
trait AttachmentHttpService extends HttpService { self : ActorLogging =>

  private implicit def executionContext = actorRefFactory.dispatcher
  
  val s3Key = scala.util.Properties.envOrElse("AWS_S3_KEY", "none")
  val s3Secret = scala.util.Properties.envOrElse("AWS_S3_SECRET", "none")

  val s3BucketName = scala.util.Properties.envOrElse("AWS_S3_BUCKET", "none")

  val s3 = S3(s3Key,s3Secret)
  val bucket = s3.bucket(s3BucketName)

  val credentials = new BasicAWSCredentials(s3Key, s3Secret)
  val s3client = new AmazonS3Client(credentials)


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
          respondWithHeader(`Cache-Control`(`max-age`(3600))) {
            detach() {

              val s3object = s3client.getObject(s3BucketName, s"$storyId/$filename")

//              complete(s3object.getObjectContent()) 
              complete("OK") 
            }       
          }
        }
      }
    }

  }

}