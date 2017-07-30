package services

import javax.inject.{Inject, Provider}

import akka.actor.{Actor, ActorSystem}
import play.api.{Application, Logger}
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

object SectionsLoaderActor {

  case object GetSections

  case object UpdateSections

}

class SectionsLoaderActor @Inject()(settingsRepo: SettingsLoader,
                                    ws: WSClient,
                                    system: ActorSystem,
                                    lifecycle: ApplicationLifecycle,
                                    app: Provider[Application])
                                   (implicit val defaultContext: ExecutionContext) extends Actor {

  private val apiKey: String = settingsRepo.settings.apiKey
  private val queryContext = system.dispatchers.lookup(Constants.NAME_QUERY_CONTEXT)
  private val logger = Logger(getClass)

  override def receive: Receive = ???
}
