package services

import java.util.concurrent.atomic.AtomicReference
import javax.inject.{Inject, Singleton}

import akka.actor.{ActorSystem, Cancellable}
import play.api.Logger
import play.api.http.Status.OK
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{JsArray, JsValue}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Entity to load and hold the NY times sections.
  */
trait SectionsLoader {

  var sections: AtomicReference[Seq[String]]

}

/**
  * An implementation that gathers data by querying the API.
  *
  * @param settingsRepo   - A dependency on the settings repository.
  * @param ws             - Play's web service client to use to request data over http.
  * @param system         - The actor system to use to load from the API.
  * @param defaultContext - The default execution context.
  * @param lifecycle      - Dependency on lifecycle to shutdown scheduled task
  */
@Singleton
class QueryingSectionsLoader @Inject()(private val settingsRepo: SettingsLoader,
                                       private val ws: WSClient,
                                       private val system: ActorSystem,
                                       private val lifecycle: ApplicationLifecycle)
                                      (implicit private val defaultContext: ExecutionContext) extends SectionsLoader {

  override var sections: AtomicReference[Seq[String]] = new AtomicReference[Seq[String]]()

  private val queryContext = system.dispatchers.lookup(Constants.NAME_QUERY_CONTEXT)
  private val logger = Logger(getClass)

  /**
    * Makes an Http request to the NY times api for the available list of sections.
    *
    * @return Eventually returns a response
    */
  private def querySections: Future[WSResponse] = {
    ws.url(Constants.URL_SECTIONS)
      .withQueryStringParameters("api-key" -> settingsRepo.settings.apiKey)
      .get()
  }

  /**
    * Takes a json response that is assumed to be from the sections endpoint of the NY times API, extracts the list
    * of sections and sets the local variable value.
    *
    * @param json - The successful response json received.
    */
  private def setSectionsFromJson(json: JsValue): Unit = {
    val names =
      (json \ "results")
        .as[JsArray].value.map(sectionObj => (sectionObj \ "name").as[String])
    sections.set(names)
  }

  //Get the list of sections at the start of this Singleton which is when the app starts
  // The initial get blocks as it doesn't make sense to start the application without it
  //If we fail to get the list, exit the application
  Await.ready(querySections, 45 seconds).onComplete {
    case Success(response) =>
      response.status match {
        case OK => setSectionsFromJson(response.json)
        case _ =>
          logger.error(s"Received response code ${response.status} from query.")
          logger.error(s"Body of response was ${response.body}")
          logger.error("System is exiting")
          System.exit(1)
      }
    case Failure(t) =>
      logger.error("Unable to retrieve initial list of sections. Shuting down", t)
      System.exit(1)
  }

  val sectionUpdates: Cancellable = system.scheduler.schedule(24 hours, 24 hours) {
    Future {
      querySections.map(response => {
        response.status match {
          case OK => setSectionsFromJson(response.json)
          case _ =>
            logger.warn("Did not update list of sections")
        }
      })(queryContext)
    }
  }

  lifecycle.addStopHook(() => {
    sectionUpdates.cancel()
    Await.ready(system.terminate(), 45 seconds)
  })

}

