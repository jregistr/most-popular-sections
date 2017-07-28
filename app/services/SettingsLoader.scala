package services

import java.util.concurrent.atomic.AtomicReference
import javax.inject.{Inject, Singleton}

import scala.concurrent.duration._
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import play.api.Logger
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.Status.OK
import play.api.libs.json.{JsArray, JsValue}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * An object that's responsible for loading settings for the application
  */
trait SettingsLoader {

  case class Settings(apiKey: String)

  val settings: Settings

  var sections: AtomicReference[Seq[String]]

}

/**
  * An implementation of [[services.SettingsLoader]] which loads data from the application.conf file and over
  * the web.
  *
  * @param ws             - Depends on the Web service client to query NY times.
  * @param system         - Dependency on the actor system.
  * @param defaultContext - The default application execution context.
  */
@Singleton
class ConfigSettingsLoader @Inject()(private val ws: WSClient, private val system: ActorSystem,
                                     private val defaultContext: ExecutionContext) extends SettingsLoader {

  private val queryContext = system.dispatchers.lookup(Constants.NAME_QUERY_CONTEXT)
  private val logger = Logger(getClass)

  // loads settings from application.conf into Settings class
  override val settings: Settings = {
    val configFactory = ConfigFactory.load()
    val nyTimesConfig = configFactory.getConfig("ny-times")
    val apiKey = nyTimesConfig.getString("api-key")

    Settings(apiKey)
  }

  override var sections: AtomicReference[Seq[String]] = new AtomicReference[Seq[String]]()

  /**
    * Makes an Http request to the NY times api for the available list of sections.
    *
    * @return Eventually returns a response
    */
  private def querySections: Future[WSResponse] = {
    ws.url(Constants.URL_SECTIONS)
      .withQueryStringParameters("api-key" -> settings.apiKey)
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
      (response.status, response.json) match {
        case (OK, json) if (json \ "status").as[String] == "OK" => setSectionsFromJson(json)
        case _ =>
          logger.error(s"Received response code ${response.status} from query.")
          logger.error(s"Body of response was ${response.body}")
          logger.error("System is exiting")
          System.exit(1)
      }
    case Failure(t) =>
      logger.error("Unable to retrieve initial list of sections. Shuting down", t)
      System.exit(1)
  }(defaultContext)

  //Schedule updating the section list once every 24 hours
  system.scheduler.schedule(24 hours, 24 hours, () => {
    querySections.map(response => {
      (response.status, response.json) match {
        case (OK, json) if (json \ "status").as[String] == "OK" => setSectionsFromJson(json)
        case _ =>
          logger.warn("Did not update list of sections")
      }
    })(queryContext)
  })(defaultContext)

}
