package amanuensis.auth

import akka.actor._
import akka.pattern.pipe

import reactivemongo.api._
import reactivemongo.bson._

import language.postfixOps

import amanuensis.mongo.MongoUsingActor


case class CheckUserMsg(username : String, password : String)


class UserContextActor extends MongoUsingActor {

  override def preStart =  {
    log.info("UserContextActor started at: {}", self.path)
  }

  val collection = db("user")

  def receive = {
    case CheckUserMsg(username,password) => checkUser(username,password)
  }

  private val digest = java.security.MessageDigest.getInstance("SHA-256")

  private def sha(s: String): String = {
    val m = digest.digest(s.getBytes("UTF-8"));
    m map { c => (c & 0xff) toHexString } mkString
  }

  def checkUser(username: String, password: String) = {
    log.info("checking user {} with {}...", username, password)

    val query = BSONDocument("username" -> username, "password" -> sha(password))

    collection.find(query).one[UserContext] pipeTo sender
  }

}