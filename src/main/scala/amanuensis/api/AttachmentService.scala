package amanuensis.api

import scala.concurrent.duration._
import scala.util.{ Success, Failure }
import akka.pattern.ask
import akka.actor.ActorLogging
import spray.util._

import akka.actor.{ Props, Actor }
import spray.http._
import spray.http.MediaTypes._
import spray.routing._
import spray.http.BodyPart
import java.io.{ FileOutputStream }

import amanuensis.core.neo4j.Neo4JId


// this trait defines our service behavior independently from the service actor
trait AttachmentHttpService extends HttpService { self : ActorLogging =>

  val attachmentRoute = {

    import spray.httpx.encoding.{ NoEncoding, Gzip }
    
    pathPrefix("attachment") {
      pathEnd {
        post {
          println("Bin daaaaaaaaaaaaaa")

          formField('file.as[Array[Byte]]) { file =>

            val filename = Neo4JId.generateId()

            // import spray.httpx.SprayJsonSupport._
            val fos: FileOutputStream = new FileOutputStream(s"target/scala-2.10/classes/uploads/$filename");
            try {
              fos.write(file);
            } finally {
              fos.close();
            }

            complete(s"""{"filename": "/attachment/$filename"}""")
          }
        }
      } ~
      getFromResourceDirectory("uploads")
    }

  }

}