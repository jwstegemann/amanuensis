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
      pathEnd {
        post {
          entity(as[MultipartFormData]) { formData =>

            formData.get("file") match {

              case Some(bodyPart) => {

                val filename = bodyPart.filename match {
                  case Some(name) => s"testfolder/$name"
                  case None => Neo4JId.generateId()
                }

                val file = bodyPart.entity.data.toByteArray

                val futureUpload = bucket.put(filename, file) map (nothing => 
                  s"""{"filename": "/attachment/$filename"}"""
                )                

                complete(futureUpload) 

              }
              
              case None => complete(BadRequest, "invalid upload, missing file...")

            }

          }
        }
      } ~
      getFromResourceDirectory("uploads")
    }

  }

}