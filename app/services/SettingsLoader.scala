package services

import javax.inject.Singleton

import com.typesafe.config.ConfigFactory

/**
  * An object that's responsible for loading settings for the application
  */
trait SettingsLoader {

  case class Settings(apiKey: String)

  val settings: Settings
}

/**
  * An implementation of [[services.SettingsLoader]] which loads data from the application.conf.
  */
@Singleton
class ConfigSettingsLoader extends SettingsLoader {

  // loads settings from application.conf into Settings object.
  override val settings: Settings = {
    val configFactory = ConfigFactory.load()
    val nyTimesConfig = configFactory.getConfig("ny-times")
    val apiKey = nyTimesConfig.getString("api-key")

    Settings(apiKey)
  }

}
