package services

import javax.inject.{Inject, Singleton}

import play.api.Configuration

/**
  * An object that's responsible for loading settings for the application
  */
trait SettingsLoader {

  case class UpdateSectionsDelay(initial: Int, repeating: Int)

  case class Settings(apiKey: String, updateDelays: UpdateSectionsDelay)

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
    val delayConfig = config.get[Configuration]("sections-update-delay")

    val apiKey = nyTimesConfig.get[String]("api-key")

    val initial = delayConfig.get[Option[Int]]("initial")
    val repeat = delayConfig.get[Option[Int]]("repeating")

    Settings(apiKey, UpdateSectionsDelay(initial.getOrElse(24), repeat.getOrElse(24)))
  }

}
