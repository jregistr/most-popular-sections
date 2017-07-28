package controllers

import javax.inject.{Inject, Singleton}

import play.api.libs.json._
import play.api.mvc._
import services.{CategoryQuery, Constants, SectionsRanker}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiController @Inject()(cc: ControllerComponents, sectionsRanker: SectionsRanker)
                             (implicit defaultContext: ExecutionContext) extends AbstractController(cc) {

  def mostPopular: Action[AnyContent] = Action.async {
    val rankedSections = sectionsRanker.getMostPopularSections(10, 7)
    rankedSections.map(set => {
      Ok(Json.toJson(set))
    })
//    categoryQuery.getCountsInCategory(7)(Constants.URL_MOST_VIEWED)
//      .map((mapped: Map[String, Int]) => {
//        val t = mapped.map {case (section, count) =>
//          JsObject(Seq(
//            "name" -> JsString(section),
//            "count" -> JsNumber(count)
//          ))
//        }.toList
//
//        Ok(JsArray(t))
//      })
  }

}
