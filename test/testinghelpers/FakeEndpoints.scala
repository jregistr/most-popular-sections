package testinghelpers

import mockws.MockWS.Routes
import play.api.libs.json.JsObject
import play.api.mvc.{Action, AnyContent, Result}
import play.api.mvc.Results._
import play.api.test.Helpers._
import services.Constants

trait FakeEndpoints extends FakeDataProvider {

  val expectedApiKey = "Thisistheexpectedapikeythatshouldbesetforallrequests"
  private val missingKey = BadRequest(erroredResponse(Seq("Missing api key")))
  private val errors = Seq("Bad Request")

  val sectionsEndPointGood: Routes = {
    case (GET, Constants.URL_SECTIONS) => mkAction(Ok(sectionsResponse))
  }

  val sectionsEndPointEmpty: Routes = {
    case (GET, Constants.URL_SECTIONS) => mkAction(Ok(emptySectionsResponse))
  }

  val sectionsEndPointError: Routes = {
    case (GET, Constants.URL_SECTIONS) => mkAction(BadRequest(erroredResponse(errors)))
  }

  def allSectionsEmptyForCategory(categoryBaseUrl: String): Routes = {
    case (GET, `categoryBaseUrl`) => mkAction(Ok(emptySectionsResponse))
  }

  def createCategoryEndPoints(base: String, period: Int,
                              sectionAndCounts: List[(String, Int)], pos: Boolean = true): Routes = {
    //folding left. If we don't find it in the sections map, we do nothing
    //if we do find it, make a route. If the count is zero, return empty, otherwise return `count` articles.
    val routes = sectionAndCounts.foldLeft(List[Routes]())((accumulated, pair) => {
      if (sectionNamesAsMap.isDefinedAt(pair._1)) {
        val responseJson = if (pair._2 > 0) articlesForSection(pair._1, pair._2) else emptySectionsResponse
        val url = mkUrl(base, pair._1, period)
        accumulated :+ (if(pos) mOkCategoryEndPoint(url, responseJson) else mkBadCategoryEndPoint(url))
      } else {
        accumulated
      }
    })
    routes.reduceLeft(_ orElse _)
  }

  def mergeWithSectionsRoute(routes: Routes): Routes = sectionsEndPointGood orElse routes

  private def mkUrl(base: String, section: String, period: Int): String = s"$base/$section/$period.json"

  private def mOkCategoryEndPoint(url: String, response: JsObject): Routes = {
    case (GET, `url`) => mkAction(Ok(response))
  }

  private def mkBadCategoryEndPoint(url: String): Routes = {
    case (GET, `url`) => mkAction(BadRequest("Something went wrong."))
  }

  private def mkAction(positive: Result, negative: Result = missingKey): Action[AnyContent] = Action { request =>
    request.getQueryString("api-key").map(_ == expectedApiKey) match {
      case Some(true) => positive
      case _ => negative
    }
  }

}
