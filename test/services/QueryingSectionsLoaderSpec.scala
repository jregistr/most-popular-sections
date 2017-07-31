package services

import mockws.MockWS
import org.scalatestplus.play._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{ApplicationLifecycle, bind}
import play.api.libs.ws.WSClient
import testinghelpers.FakeEndpoints

class QueryingSectionsLoaderSpec extends PlaySpec with FakeEndpoints {

  "When properly configured, loader" should {
    val ws = MockWS(sectionsEndPointGood)
    val application = new GuiceApplicationBuilder()
      .configure("ny-times.api-key" -> expectedApiKey)
      .overrides(bind[WSClient].toInstance(ws))
      .build()

    "Get the sections" in {
      val sectionLoader = application.injector.instanceOf(classOf[SectionsLoader])
      val loadedSections = sectionLoader.sections.get()

      val check = loadedSections.forall(s => sectionsNames.contains(s))

      check must be(true)
    }
  }

  "when app is well configured and outside api returns empty, loader" should {
    val ws = MockWS(sectionsEndPointEmpty)
    val app = new GuiceApplicationBuilder()
      .configure("ny-times.api-key" -> expectedApiKey)
      .overrides(bind[WSClient].toInstance(ws))
      .build()

    "Should accept the empty list" in {
      val sectionLoader = app.injector.instanceOf(classOf[SectionsLoader])
      val lifeCycle = app.injector.instanceOf[ApplicationLifecycle]

      val loadedSections = sectionLoader.sections.get()
      loadedSections must be (empty)
    }
  }

  "When an unexpected API key is sent, loader" should {
    val ws = MockWS(sectionsEndPointGood)
    val app = new GuiceApplicationBuilder()
      .configure("ny-times.api-key" -> "Some key that is not expected")
      .overrides(bind[WSClient].toInstance(ws))
      .build()

    "not have any sections" in {
      val sectionLoader = app.injector.instanceOf(classOf[SectionsLoader])
      sectionLoader.sections.get() must be(null)
    }
  }

}
