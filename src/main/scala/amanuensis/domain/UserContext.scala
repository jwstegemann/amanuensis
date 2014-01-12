package amanuensis.domain

import spray.json.DefaultJsonProtocol


case class UserContext(login: String, name: String, permissions: Seq[String])

object UserContextProtocol extends DefaultJsonProtocol {
  implicit val userContextJsonFormat = jsonFormat3(UserContext.apply)
}