package services

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.Logger
import play.api.http.Status.OK
import play.api.libs.ws.WSClient

import scala.concurrent.Future


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

}