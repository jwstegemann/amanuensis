package amanuensis.domain

import spray.json.DefaultJsonProtocol


object UserRights extends Enumeration {
    type UserRight = Value
    val canRead, canWrite, canGrant = Value
  }

case class UserContext(login: String, name: String, permissions: Seq[String], lang: String)

case class ChangePasswordRequest(oldPwd: String, newPwd: String)

case class UserLogin(login: String)
case class LoginRequest(username: String, password: String)

object UserContextProtocol extends DefaultJsonProtocol {
  implicit val userContextJsonFormat = jsonFormat4(UserContext.apply)
  implicit val LoginRequestJsonFormat = jsonFormat2(LoginRequest.apply)
  implicit val userLoginFormat = jsonFormat1(UserLogin.apply)
  implicit val changePasswordRequestFormat = jsonFormat2(ChangePasswordRequest.apply)
}