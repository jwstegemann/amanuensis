package amanuensis.mongo

import akka.actor._
import akka.pattern.pipe

import reactivemongo.api._
import reactivemongo.bson._

import scala.concurrent.Future
import scala.reflect.ClassTag

import language.postfixOps

import amanuensis.system._
import amanuensis.entity._

import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter}


abstract class EntityActor[T <: Entity: ClassTag](val collectionName: String)
  (implicit val bsonReader: BSONDocumentReader[T], val bsonWriter: BSONDocumentWriter[T]) extends MongoUsingActor with LastErrorMapping {

  val collection = db(collectionName)

  def receive = {
    case FindAll() => findAll()
    case Create(item: T) => create(item)
    case Load(id) => load(id)
    case Update(item: T) => update(item)
    case Delete(id) => delete(id)
  }

  override def preStart =  {
    log.info(s"EntityActor for collection $collectionName started at: {}", self.path)
  }

  /*
   * find all items and send back the list to the sender
   */ 
  def findAll() = {
    log.debug(s"finding all entites in $collectionName")
    collection.find(BSONDocument()).cursor[T].collect[List]() pipeTo sender
  }

  /*
   * create a new item
   */
  def create(item: T) = {
    log.debug(s"creating new entity in $collectionName '{}'", item)

//    if (validate(item, hasNoId _ :: validator.checks())) {
//      item.generateId
//      item.version.update
      (collection.insert(item).recoverWithInternalServerError.mapToInserted(item._id.toString)) pipeTo sender
//    }
  }

  /*
   * load an item
   */
  def load(id: String) = {
    log.debug(s"loading item from $collectionName with id '{}'", id)
    val query = BSONDocument("_id" -> BSONObjectID(id))
    failIfEmpty(collection.find(query).one[T], id)
  }

  /*
   * update an item
   */
  def update(item: T) = {
    log.debug(s"updating entity in $collectionName with id '{}'", item)
    
//    if (validate(item, hasId _ :: validator.checks())) { // hasVersion _ ::
      val query = BSONDocument("_id" -> item._id) //, "version" -> item.version.asBSON)
      //item.version.update
      (collection.update(query, item, defaultWriteConcern,false,false).recoverWithInternalServerError.mapToUpdated) pipeTo sender
//    }
  }

  /*
   * delete an item
   */
  def delete(id: String) = {
    log.debug(s"deleting entity from $collectionName with id '{}'", id)
    val query = BSONDocument("_id" -> BSONObjectID(id))    
    (collection.remove(query,defaultWriteConcern,true).recoverWithInternalServerError.mapToDeleted(id)) pipeTo sender
  }

	
}