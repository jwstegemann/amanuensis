package amanuensis

import amanuensis.api.Api
import amanuensis.core.{BootedCore, CoreActors}
import amanuensis.web.Web


object Rest extends App with BootedCore with CoreActors with Api with Web {
  
}
