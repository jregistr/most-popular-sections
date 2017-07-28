package controllers

import javax.inject.{Inject, Singleton}

import play.api.libs.json._
import play.api.mvc._
import services.{CategoryQuery, Constants}

import scala.concurrent.ExecutionContext

@Singleton
class ApiController @Inject()(cc: ControllerComponents, categoryQuery: CategoryQuery)
                             (implicit defaultContext: ExecutionContext) extends AbstractController(cc) {

  def mostPopular: Action[AnyContent] = Action.async {
    categoryQuery.getCountsInCategory(7)(Constants.URL_MOST_VIEWED)
      .map((mapped: Map[String, Int]) => {
        val t = mapped.map {case (section, count) =>
          JsObject(Seq(
            "name" -> JsString(section),
            "count" -> JsNumber(count)
          ))
        }.toList

        Ok(JsArray(t))
      })
  }

}
