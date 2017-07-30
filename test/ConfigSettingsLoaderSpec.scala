import helpers.FakeEndpoints
import mockws.MockWS
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import services.SettingsLoader

class ConfigSettingsLoaderSpec extends PlaySpec with GuiceOneAppPerSuite with FakeEndpoints {

  "Config settings loader " should {
    "Load the correct values for NY times config" in {

      val key = expectedApiKey

      val fakeClient = MockWS(sectionsEndPointGood)

      val application = new GuiceApplicationBuilder()
        .overrides(bind[WSClient].toInstance(fakeClient))
        .configure("ny-times.api-key" -> key)
        .build()

      val settingsLoader = application.injector.instanceOf(classOf[SettingsLoader])
      settingsLoader.settings.apiKey must be(key)
    }
  }

}
