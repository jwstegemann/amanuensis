package amanuensis.auth

import akka.actor._
import scala.concurrent.duration._
import scala.collection.concurrent.TrieMap

import scala.concurrent.duration._

import language.postfixOps

/*
 * Messages for SessionServiceActor
 */
case class IsSessionValidMsg(sessionId : String, host : String)

case class CreateSessionMsg(sid : String, userContext : UserContext, hostName : String)

case class ExpireSessionsMsg()


/*
 * SessionServicActor
 */
class SessionServiceActor extends Actor with ActorLogging {

  val config = context.system.settings.config

  val sessionTimeout = config.getInt("amanuensis.session.timeout")
  val expireInterval = config.getInt("amanuensis.session.expire")

  override def preStart =  {
    log.info("SessionServiceActor started at: {}", self.path)

    // expire sessions regularly
    context.system.scheduler.schedule(expireInterval seconds,
      expireInterval seconds,
      self,
      ExpireSessionsMsg())(context.system.dispatcher)

    log.info("expiring sessions every {} seconds if older than {} seconds", expireInterval, sessionTimeout)
  }

  case class Session(id: String, userContext: UserContext, host: String, lastAccess: Long) {
    def withAccessTime(accessTime: Long) = this.copy(lastAccess = accessTime)
  }

  private var sessions = TrieMap[String, Session]()

  def receive = {
    case IsSessionValidMsg(sessionId, host) => isSessionValid(sessionId, host)
    case CreateSessionMsg(sid, userContext, hostName) => createSession(sid, userContext, hostName)
    case ExpireSessionsMsg() => expireSessions()
  }

  def isSessionValid(sessionId: String, host: String) = {
      //FIXME: Make this configurable
      val dummyUserContext = UserContext("login","info","vorname","nachname",Nil)
      sender ! Some(dummyUserContext)

      //TODO: use map instead of match?
/*      sessions.get(sessionId) match {
        case Some(session) => {
          sender ! Some(session.userContext)
          sessions += (session.id -> (session withAccessTime currentTime))
        }
        case None => sender ! None
      }
    */
  }

  def createSession(sid : String, userContext : UserContext, hostName : String) = {
    sessions += (sid -> Session(sid, userContext, hostName, currentTime))
    log.info("created session '{}' for user '{}'", sid, userContext.username)
  }  

  def expireSessions() = {
    log.debug("expring sessions starting with {} open sessions...", sessions.size)
    sessions = sessions.filterNot { case (key, value) => (currentTime - value.lastAccess) > sessionTimeout }
    log.debug("done expring sessions leaving {} open...", sessions.size)
  }

  def currentTime = System.currentTimeMillis / 1000

}