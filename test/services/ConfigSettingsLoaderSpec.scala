package services

import testinghelpers.FakeEndpoints
import mockws.MockWS
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient

class ConfigSettingsLoaderSpec extends PlaySpec with GuiceOneAppPerSuite with FakeEndpoints {

  "Config settings loader " should {
    "Load the correct values for NY times config" in {

      val settingsLoader = app.injector.instanceOf(classOf[SettingsLoader])
      settingsLoader.settings.apiKey must be(expectedApiKey)
    }
  }

  override def fakeApplication(): Application = {
    val key = expectedApiKey
    val fakeClient = MockWS(sectionsEndPointGood)

    new GuiceApplicationBuilder()
      .overrides(bind[WSClient].toInstance(fakeClient))
      .configure("ny-times.api-key" -> key)
      .build()
  }
}
