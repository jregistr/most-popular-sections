import akka.actor.ActorSelection
import helpers.FakeEndpoints
import mockws.MockWS
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import play.api.libs.ws.WSClient
import services.SectionsLoaderActor
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

class SectionsLoaderActorSpec extends PlaySpec  with FakeEndpoints {

//  "When configured correctly, actor" should {
//    val ws = MockWS(sectionsEndPointGood)
//    val app = new GuiceApplicationBuilder()
//      .overrides(bind[WSClient].toInstance(ws))
//      .configure("ny-times.api-key" -> expectedApiKey)
//      .build()
//
//    "Get the sections from web service request" in {
//      val loader: ActorSelection = app.actorSystem.actorSelection("Sections-Loader")
//      println(loader.toString())
//
////      (loader ? SectionsLoaderActor.GetSections)
//    }
//  }

}
