package amanuensis.auth

import reactivemongo.bson.BSONArray
import reactivemongo.bson.Macros
import spray.json.DefaultJsonProtocol


case class LoginRequest(username: String, password: String)

case class UserContext(username: String, info: String, firstName: String, lastName: String, permissions: List[String])

object UserContext {
  implicit val userContextBsonFormat = Macros.handler[UserContext]
}

object AmanuensisAuthJsonProtocol extends DefaultJsonProtocol {
  implicit val loginRequestFormat = jsonFormat2(LoginRequest)
  implicit val UserContextFormat = jsonFormat5(UserContext.apply)
}


