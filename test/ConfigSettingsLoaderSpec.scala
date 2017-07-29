import com.typesafe.config.ConfigFactory
import org.scalatestplus.play._
import services.ConfigSettingsLoader

class ConfigSettingsLoaderSpec extends PlaySpec {

  "Config settings loader " should {
    "Load the correct values for NY times config" in {
      val nyTimesConfig = ConfigFactory.load().getConfig("ny-times")
      val apiKey = nyTimesConfig.getString("api-key")

      val settingsLoader = new ConfigSettingsLoader()
      settingsLoader.settings.apiKey must be (apiKey)
    }
  }

}
