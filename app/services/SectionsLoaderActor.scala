package services

import javax.inject.{Inject, Provider}

import akka.actor.{Actor, ActorSystem}
import play.api.http.Status.OK
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{JsArray, JsValue}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Application, Logger}
import services.SectionsLoaderActor.{GetSections, UpdateSections, UpdatedSections}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object SectionsLoaderActor {

  case object GetSections

  case object UpdateSections

  case class UpdatedSections(sections: Seq[String])

}

class SectionsLoaderActor @Inject()(settingsRepo: SettingsLoader,
                                    ws: WSClient,
                                    system: ActorSystem,
                                    lifecycle: ApplicationLifecycle,
                                    app: Provider[Application])
                                   (implicit val defaultContext: ExecutionContext) extends Actor {

  private val apiKey: String = settingsRepo.settings.apiKey
  private val delays: settingsRepo.UpdateSectionsDelay = settingsRepo.settings.updateDelays
  private val queryContext = system.dispatchers.lookup(Constants.NAME_QUERY_CONTEXT)
  private val logger = Logger(getClass)

  private var sections: Seq[String] = Seq()

//  system.scheduler.schedule(delays.initial hours, delays.repeating hours, self, UpdateSections)

  override def receive: Receive = {
    case GetSections => sender() ! sections
    case UpdatedSections(nSections) => if (nSections.nonEmpty) sections = nSections
    case UpdateSections => querySections.map(response => {
      response.status match {
        case OK => self ! UpdatedSections(response.json)
        case _ => logger.warn("Failed to update sections list")
      }
    })(queryContext)
  }

  /**
    * Makes an Http request to the NY times api for the available list of sections.
    *
    * @return Eventually returns a response
    */
  private def querySections: Future[WSResponse] = {
    ws.url(Constants.URL_SECTIONS)
      .withQueryStringParameters("api-key" -> apiKey)
      .get()
  }

  private implicit def jsonToSections(json: JsValue): Seq[String] =
    (json \ "results")
      .as[JsArray].value.map(sectionObj => (sectionObj \ "name").as[String])

  //Get the list of sections at the start of this Singleton which is when the app starts
  // The initial get blocks as it doesn't make sense to start the application without it
  //If we fail to get the list, exit the application
  Await.ready(querySections, 45 seconds).onComplete {
    case Success(response) =>
      response.status match {
        case OK =>
          println(response.json.toString())
          sections = response.json
        case _ =>
          logger.error(s"Received response code ${response.status} from query.")
          logger.error(s"Body of response was ${response.body}")
          logger.error("System is exiting")
          app.get().stop()
      }
    case Failure(t) =>
      logger.error("Unable to retrieve initial list of sections. Shutting down", t)
      app.get().stop()
  }



}
