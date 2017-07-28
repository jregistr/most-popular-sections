package services

import javax.inject.Inject

import play.api.Logger
import play.api.libs.ws.WSClient
import play.api.http.Status.OK
import play.api.libs.json.JsArray

import scala.concurrent.{ExecutionContext, Future}

trait CategoryQuery {

  protected implicit val executionContext: ExecutionContext
  protected val settingsRepo: ConfigSettingsLoader

  /// move this to the most popular sections
  protected val getSections: Future[Seq[String]]

  def getCountsInCategory(timePeriod: Int)(apiBase: String): Future[Map[String, Int]]

  protected def getCountForCategory(section: String, apiBase: String, timePeriod: Int): Future[(String, Int)]

  protected def mkUrl(apiBase: String, section: String, timePeriod: Int): String =
    s"$apiBase/$section/$timePeriod.json?api-key=${settingsRepo.settings.apiKey}"
}

class CategoryQueryOverRest @Inject()(override protected val settingsRepo: ConfigSettingsLoader,
                                      private val ws: WSClient)
                                     (override protected implicit val executionContext: ExecutionContext)
  extends CategoryQuery {

  protected override lazy val getSections: Future[Seq[String]] = {
    val request = ws.url("https://api.nytimes.com/svc/mostpopular/v2/viewed/sections.json")
      .withQueryStringParameters("api-key" -> settingsRepo.settings.apiKey)
    request.get().map(response => {
      (response.status, response.json) match {
        case (OK, json) if (json \ "status").as[String] == "OK" =>
          (json \ "results").as[JsArray].value.map(sectionObj => (sectionObj \ "name").as[String])
        case _ => throw new RuntimeException("Unable to get sections")
      }
    })
  }

  override def getCountsInCategory(timePeriod: Int)(apiBase: String): Future[Map[String, Int]] = {
    getSections.flatMap(sections => {
      val allCounts = Future.sequence(sections.map(section => {
        getCountForCategory(section, apiBase, timePeriod)
      }))
      allCounts.map(_.toMap)
    })
  }

  override protected def getCountForCategory(section: String, apiBase: String, timePeriod: Int): Future[(String, Int)] = {
    val queryUrl = mkUrl(apiBase, section, timePeriod)
    ws.url(queryUrl).get().map(response => {
      response.status -> response.json match {
        case (OK, json) if (json \ "status").as[String] == "OK" =>
          val json = response.json
          val count = (json \ "num_results").as[Int]
          section -> count
        case _ => section -> 0
      }
    })
  }

}
