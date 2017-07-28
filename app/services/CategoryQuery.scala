package services

import javax.inject.Inject

import play.api.Logger
import play.api.http.Status.OK
import play.api.libs.ws.WSClient

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait CategoryQuery {

  protected val settingsRepo: ConfigSettingsLoader

  def getCountsInCategory(sections: Seq[String], timePeriod: Int)(apiBase: String): Future[Map[String, Int]]

  protected def getCountForCategory(section: String, apiBase: String, timePeriod: Int): Future[(String, Int)]

  protected def mkUrl(apiBase: String, section: String, timePeriod: Int): String =
    s"$apiBase/$section/$timePeriod.json?api-key=${settingsRepo.settings.apiKey}"
}

class CategoryQueryOverRest @Inject()(override protected val settingsRepo: ConfigSettingsLoader,
                                      private val ws: WSClient)
                                     (private implicit val executionContext: ExecutionContext)
  extends CategoryQuery {

  private val logger = Logger(getClass)

  override def getCountsInCategory(sections: Seq[String], timePeriod: Int)(apiBase: String): Future[Map[String, Int]] = {
    val allCounts = Future.sequence(sections.map(section => {
      getCountForCategory(section, apiBase, timePeriod)
    }))
    allCounts.map(_.toMap)
  }

  override protected def getCountForCategory(section: String, apiBase: String,
                                             timePeriod: Int): Future[(String, Int)] = {
    val queryUrl = mkUrl(apiBase, section, timePeriod)
    ws.url(queryUrl)
      .withRequestTimeout(15 seconds)
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

}
