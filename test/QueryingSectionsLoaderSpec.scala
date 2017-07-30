import java.util.concurrent.ForkJoinPool

import akka.actor.ActorSystem
import mockws.MockWS
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, JsNumber, JsObject, JsString}
import play.api.libs.ws.WSClient
import play.api.mvc.Action
import play.api.mvc.Results._
import play.api.test.Helpers._
import services.{ConfigSettingsLoader, QueryingSectionsLoader, SettingsLoader}

import scala.concurrent.ExecutionContext

class QueryingSectionsLoaderSpec extends PlaySpec with BeforeAndAfter with MockitoSugar {

  val fakeSections = List("A", "B", "C", "D", "EFG", "HIJ", "KLM", "NOP", "QR", "ST")

  val fakeSectionsResponse = JsObject(Seq(
    "status" -> JsString("OK"),
    "num_results" -> JsNumber(fakeSections.length),
    "results" -> JsArray(fakeSections.map(name => JsObject(Seq("name" -> JsString(name)))))
  ))


}
