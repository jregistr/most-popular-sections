import helpers.FakeEndpoints
import mockws.MockWS
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import play.api.libs.ws.WSClient
import services.SectionsLoader

class QueryingSectionsLoaderSpec extends PlaySpec with GuiceOneAppPerSuite with FakeEndpoints {

  "When properly configured" should {
    "Get the sections" in {
      val ws = MockWS(sectionsEndPointGood)
      val application = new GuiceApplicationBuilder()
        .configure("ny-times.api-key" -> expectedApiKey)
        .overrides(bind[WSClient].toInstance(ws))
        .build()

      val sectionLoader = application.injector.instanceOf(classOf[SectionsLoader])
      val loadedSections = sectionLoader.sections.get()

      val check = loadedSections.forall(s => sectionsNames.contains(s))

      check must be(true)
    }
  }
}
