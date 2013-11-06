package amanuensis.entity

/*
 * available message-types for entity-actors
 */
case class FindAll()
case class Create(item: Entity)
case class Load(id: String)
case class Update(item: Entity)
case class Delete(id: String)