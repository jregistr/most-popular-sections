package services

import javax.inject.{Inject, Singleton}

import com.typesafe.config.ConfigFactory
import play.api.Play
import play.api.libs.ws.WSClient
import play.core.ApplicationProvider

trait SettingsLoader {

  case class Settings(apiKey: String)

  val settings: Settings

}

@Singleton
class ConfigSettingsLoader @Inject()(ws: WSClient) extends SettingsLoader {

  override lazy val settings: Settings = {
    val configFactory = ConfigFactory.load()
    val nyTimesConfig = configFactory.getConfig("ny-times")
    val apiKey = nyTimesConfig.getString("api-key")

    Settings(apiKey)
  }

}
