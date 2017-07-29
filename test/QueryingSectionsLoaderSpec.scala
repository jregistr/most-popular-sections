import org.scalatest.BeforeAndAfter
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsArray, JsNumber, JsObject, JsString}
import services.ConfigSettingsLoader

class QueryingSectionsLoaderSpec extends PlaySpec with BeforeAndAfter with GuiceOneAppPerSuite {

  val fakeSections = List(
    "A",
    "B",
    "C",
    "D",
    "EFG",
    "HIJ",
    "KLM",
    "NOP",
    "QR",
    "ST"
  )

  val fakeSectionsResponse = JsObject(Seq(
    "status" -> JsString("OK"),
    "num_results" -> JsNumber(fakeSections.length),
    "results" -> JsArray(fakeSections.map(name => JsObject(Seq("name" -> JsString(name)))))
  ))

  "Querying Sections Loader" should {
    "set a value for sections" in {
      val settingsRepo = new ConfigSettingsLoader
      //      val fakeWs = Mocki
    }
  }

}
