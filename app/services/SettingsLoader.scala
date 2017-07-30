package services

import javax.inject.{Inject, Singleton}

import com.typesafe.config.ConfigFactory
import play.api.Configuration

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
class ConfigSettingsLoader @Inject()(config: Configuration) extends SettingsLoader {

  // loads settings from application.conf into Settings object.
  override val settings: Settings = {
    val nyTimesConfig: Configuration = config.get[Configuration]("ny-times")
    val apiKey = nyTimesConfig.get[String]("api-key")

    Settings(apiKey)
  }

}
