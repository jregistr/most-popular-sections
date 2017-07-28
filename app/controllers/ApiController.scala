package controllers

import javax.inject.{Inject, Singleton}

import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc._
import services.SectionsRanker

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiController @Inject()(cc: ControllerComponents, sectionsRanker: SectionsRanker)
                             (implicit defaultContext: ExecutionContext)
  extends AbstractController(cc) with I18nSupport with FormattedJsonOutput {

  private case class MostPopularQuery(limit: Int, timePeriod: Int)

  private val periodConstraint = Constraint[Int]("constraints.timeperiod") {
    case 1 | 7 | 30 => Valid
    case _ => Invalid(Seq(ValidationError("timePeriod must be 1, 7 or 30")))
  }

  private val mostPopularForm = Form(
    mapping(
      "limit" -> default[Int](number(min = 1), 10),
      "timePeriod" -> default(number, 1).verifying(periodConstraint)
    )(MostPopularQuery.apply)(MostPopularQuery.unapply)
  )

  def mostPopular: Action[AnyContent] = Action.async { implicit request =>
    mostPopularForm.bindFromRequest().fold(badInput => Future {
      BadRequest(failed(badInput.errorsAsJson, BAD_REQUEST))
    }, query => {
      sectionsRanker.getMostPopularSections(query.limit, query.timePeriod)
        .map(set => {
          Ok(success(Json.toJson(set), set.length))
        })
    })
  }

}
