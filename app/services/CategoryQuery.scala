package services

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.Logger
import play.api.http.Status.OK
import play.api.libs.ws.WSClient

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Defines the shape of an object that calculates section view counts for a given category.
  */
trait CategoryQuery {

  protected val settingsRepo: ConfigSettingsLoader

  /**
    * Finds the appearance count for each given section for the provided category determined by the apiBase.
    *
    * @param sections   - The sections to query for.
    * @param timePeriod - The time period.
    * @param apiBase    - The url for the category.
    * @return - A mapping of each section to its appearance count.
    */
  def getCountsInCategory(sections: Seq[String], timePeriod: Int)(apiBase: String): Future[Map[String, Int]]

  /**
    * For a given sections, finds the appearance count.
    *
    * @param section    - The section to find appearance count data for.
    * @param apiBase    - The url for the category.
    * @param timePeriod - The time period.
    * @return - A tuple pairing the given section with its appearance count.
    */
  protected def getCountForCategory(section: String, apiBase: String, timePeriod: Int): Future[(String, Int)]

}

/**
  * An implementation of the [[CategoryQuery]] that fetches its data from the NY times api.
  *
  * @param settingsRepo - The settings repo for this project. Needed to fetch the API key.
  * @param ws           - A dependency on play's webservice for performing requests.
  * @param system       - A dependency on the actor system for loading a dispatcher.
  */
class CategoryQueryOverRest @Inject()(override protected val settingsRepo: ConfigSettingsLoader,
                                      private val ws: WSClient,
                                      system: ActorSystem)

  extends CategoryQuery {

  private implicit val queryContext = system.dispatchers.lookup(Constants.NAME_QUERY_CONTEXT)
  private val logger = Logger(getClass)

  /**
    * Collects the appearance count for each given section.
    *
    * @see [[CategoryQuery.getCountsInCategory()]]
    */
  override def getCountsInCategory(sections: Seq[String], timePeriod: Int)
                                  (apiBase: String): Future[Map[String, Int]] = {
    val allCounts = Future.sequence(sections.map(section => {
      getCountForCategory(section, apiBase, timePeriod)
    }))
    allCounts.map(_.toMap)
  }

  /**
    * Queries for the appearance count of a given section. Defaults to 0 for the appearance count if no output was
    * returned or an error occurred.
    *
    * @see [[CategoryQuery.getCountForCategory()]]
    */
  override protected def getCountForCategory(section: String, apiBase: String,
                                             timePeriod: Int): Future[(String, Int)] = {
    val queryUrl = mkUrl(apiBase, section, timePeriod)
    ws.url(queryUrl)
      .withRequestTimeout(15.seconds) //15 second timeout
      .get().map(response => {
      response.status match {
        case OK =>
          val json = response.json
          if ((json \ "status").as[String] == "OK") {
            val count = (json \ "num_results").as[Int]
            section -> count
          } else {
            section -> 0
          }
        case _ => section -> 0
      }
    }).recover {
      case t =>
        logger.error(s"Failed to get count for category: $section with API: $apiBase. Returning default", t)
        section -> 0
    }
  }

  /**
    * Formats the query url using the base api uri and the provided url parameters along with the API key.
    *
    * @param apiBase    - The base api url.
    * @param section    - The section.
    * @param timePeriod - The time period.
    * @return - A formatted query url to perform the request on.
    */
  private def mkUrl(apiBase: String, section: String, timePeriod: Int): String =
    s"$apiBase/$section/$timePeriod.json?api-key=${settingsRepo.settings.apiKey}"

}
