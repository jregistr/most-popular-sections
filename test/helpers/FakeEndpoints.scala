package helpers

import mockws.MockWS.Routes
import play.api.libs.json.JsObject
import play.api.mvc.Action
import play.api.mvc.Results._
import play.api.test.Helpers._
import services.Constants

trait FakeEndpoints extends FakeDataProvider {

  private val errors = Seq("Bad Request")

  val sectionsEndPointGood: Routes = {
    case (GET, Constants.URL_SECTIONS) => Action {
      Ok(sectionsResponse)
    }
  }

  val sectionsEndPointError: Routes = {
    case (GET, Constants.URL_SECTIONS) => Action {
      BadRequest(erroredResponse(errors))
    }
  }

  def allSectionsEmptyForCategory(categoryBaseUrl: String): Routes = {
    case (GET, `categoryBaseUrl`) => Action {
      Ok(emptySectionsResponse)
    }
  }

  def createCategoryEndPoints(base: String, period: Int, sectionAndCounts: List[(String, Int)]): Routes = {
    //folding left. If we don't find it in the sections map, we do nothing
    //if we do find it, make a route. If the count is zero, return empty, otherwise return `count` articles.
    val routes = sectionAndCounts.foldLeft(List[Routes]())((accumulated, pair) => {
      if (sectionNamesAsMap.isDefinedAt(pair._1)) {
        val responseJson = if (pair._2 > 0) articlesForSection(pair._1, pair._2) else emptySectionsResponse
        val url = mkUrl(base, pair._1, period)
        accumulated :+ mOkCategoryEndPoint(url, responseJson)
      } else {
        accumulated
      }
    })
    routes.reduceLeft(_ orElse _)
  }

  private def mOkCategoryEndPoint(url: String, response: JsObject): Routes = {
    case (GET, `url`) => Action {
      Ok(response)
    }
  }

  def mergeWithSectionsRoute(routes: Routes): Routes = sectionsEndPointGood orElse routes

  def mkUrl(base: String, section: String, period: Int): String = s"$base/$section/$period.json"

}
