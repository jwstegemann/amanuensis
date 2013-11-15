package amanuensis.auth

import akka.actor._
import akka.pattern._
import reactivemongo.api._
import reactivemongo.bson._
import language.postfixOps
import amanuensis.mongo.MongoUsingActor
import spray.routing.authentication.UserPass
import spray.caching._


case class CheckUserMsg(userPassOption: Option[UserPass])


class UserContextActor extends MongoUsingActor {

  val userCache : Cache[Option[UserContext]]  = LruCache() //TODO: set parameters

  override def preStart =  {
    log.info("UserContextActor started at: {}", self.path)  
  }

  val collection = db("user")

  def receive = {
    case CheckUserMsg(userPassOption) => checkUser(userPassOption)
  }

  private val digest = java.security.MessageDigest.getInstance("SHA-256")

  private def sha(s: String): String = {
    val m = digest.digest(s.getBytes("UTF-8"));
    m map { c => (c & 0xff) toHexString } mkString
  }

  def checkUser(userPassOption: Option[UserPass]) = {
    userPassOption match {
      case Some(userPass) => {
        log.info("checking user {} with {}...", userPass.user, userPass.pass)

        userCache(userPass) {
          log.info("had to check db for userContext")
          val query = BSONDocument("username" -> userPass.user, "password" -> sha(userPass.pass))
          collection.find(query).one[UserContext]
        } pipeTo sender
      }
      case None => sender ! None
    }
  }

}