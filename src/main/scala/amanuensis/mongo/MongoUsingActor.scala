package amanuensis.mongo

import scala.concurrent.Future
import akka.actor._
import akka.pattern.pipe

import reactivemongo.api._
import reactivemongo.core.commands.LastError
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.GetLastError

import amanuensis.system._
import amanuensis.system.Severities._
import amanuensis.system.Implicits._


trait MongoUsingActor extends Actor with ActorLogging with Failable {

  // get DB-settings from config-file
  val config = context.system.settings.config

  private val mongodbUrl = config.getString("amanuensis.mongodb.url")
  private val mongodbDb = config.getString("amanuensis.mongodb.db")

  // establish connecttion to mongoDB
  protected val driver = new MongoDriver
  protected val connection = driver.connection(List(mongodbUrl))
  protected val db = connection(mongodbDb)

  log.info("creating connection to {}@{}", mongodbDb, mongodbUrl)

  //TODO: is this correct and efficient?
  val defaultWriteConcern = GetLastError(true)

  /*
   * fails future with a NotFound-exception when option is empty
   */
  def failIfEmpty[T](item: Future[Option[T]], id: String) = {
    (item map {
      case Some(t) => t
      case None => throw NotFoundException(Message(s"id '$id' could not be found",`ERROR`))
    }) pipeTo sender
  }

}