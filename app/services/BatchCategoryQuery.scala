package services

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.Logger
import play.api.libs.ws.WSClient

import scala.concurrent.duration._
import play.api.http.Status.OK
import play.api.libs.json.{JsArray, JsValue}

import scala.collection.parallel.ParMap
import scala.concurrent.Future

class BatchCategoryQuery @Inject()(override protected val settingsRepo: ConfigSettingsLoader,
                                   private val ws: WSClient,
                                   system: ActorSystem) extends CategoryQuery {

  private implicit val queryContext = system.dispatchers.lookup(Constants.NAME_QUERY_CONTEXT)
  private val logger = Logger(getClass)

  /**
    * Finds the appearance count for each given section for the provided category determined by the apiBase.
    *
    * @param sections   - The sections to query for.
    * @param timePeriod - The time period.
    * @param apiBase    - The url for the category.
    * @return - A mapping of each section to its appearance count.
    */
  override def getCountsInCategory(sections: Seq[String], timePeriod: Int)
                                  (apiBase: String): Future[Map[String, Int]] = {
    val sectionsAsMap = sections.map(_ -> 0).toMap
    val url = s"$apiBase/all-sections/$timePeriod.json"
    val futureResponse = ws.url(url)
      .withQueryStringParameters("api-key" -> settingsRepo.settings.apiKey)
      .withRequestTimeout(15.seconds)
      .get()

    futureResponse.map(response => {
      response.status match {
        case OK =>
          val json = response.json
          val results = (json \ "results").as[JsArray].value

          results.foldLeft(Map[String, Int]())((map: Map[String, Int], article: JsValue) => {
            val articleSection = (article \ "section").as[String]
            println(articleSection)
            if (sectionsAsMap.isDefinedAt(articleSection)) {
              val nValue: Int = map.getOrElse(articleSection, 0) + 1
              map + (articleSection -> nValue)
            } else {
              println("NOT FOUND")
              map
            }
          })
        case _ => Map[String, Int]()
      }
    }).recover {
      case t =>
        logger.error("Failed to get counts for category:", t)
        Map[String, Int]()
    }
  }
}
