package amanuensis.domain

import spray.json.DefaultJsonProtocol


case class UserContext(login: String, name: String, permissions: Seq[String])

case class LoginRequest(username: String, password: String)

object UserContextProtocol extends DefaultJsonProtocol {
  implicit val userContextJsonFormat = jsonFormat3(UserContext.apply)
  implicit val LoginRequestJsonFormat = jsonFormat2(LoginRequest.apply)
}