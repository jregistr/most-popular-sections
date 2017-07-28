package controllers

import javax.inject.{Inject, Singleton}

import play.api.libs.json._
import play.api.mvc._
import services.CategoryQuery

import scala.concurrent.ExecutionContext

@Singleton
class ApiController @Inject()(cc: ControllerComponents, categoryQuery: CategoryQuery)
                             (implicit defaultContext: ExecutionContext) extends AbstractController(cc) {

  def mostPopular: Action[AnyContent] = Action.async {
    categoryQuery.getCountsInCategory(7)("https://api.nytimes.com/svc/mostpopular/v2/mostviewed")
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
