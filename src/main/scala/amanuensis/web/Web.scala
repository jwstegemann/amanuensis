package amanuensis.web

import amanuensis.core.{CoreActors, Core}
import amanuensis.api.Api
import akka.io.IO
import spray.can.Http
import java.security.{SecureRandom, KeyStore}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

/**
 * Provides the web server (spray-can) for the REST api in ``Api``, using the actor system
 * defined in ``Core``.
 *
 * You may sometimes wish to construct separate ``ActorSystem`` for the web server machinery.
 * However, for this simple application, we shall use the same ``ActorSystem`` for the
 * entire application.
 *
 * Benefits of separate ``ActorSystem`` include the ability to use completely different
 * configuration, especially when it comes to the threading model.
 */

import spray.io.ServerSSLEngineProvider



trait Web {
  this: Api with CoreActors with Core =>

  IO(Http)(system) ! Http.Bind(rootService, "0.0.0.0", port = Integer.parseInt(scala.util.Properties.envOrElse("PORT", "8080")))

}