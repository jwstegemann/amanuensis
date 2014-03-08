package amanuensis.domain

import spray.json.DefaultJsonProtocol


object UserRights extends Enumeration {
    type UserRight = Value
    val canRead, canWrite, canGrant = Value
  }

case class UserContext(login: String, name: String, permissions: Seq[String])

case class LoginRequest(username: String, password: String)

object UserContextProtocol extends DefaultJsonProtocol {
  implicit val userContextJsonFormat = jsonFormat3(UserContext.apply)
  implicit val LoginRequestJsonFormat = jsonFormat2(LoginRequest.apply)
}