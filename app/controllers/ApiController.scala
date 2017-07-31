package controllers

import javax.inject.{Inject, Singleton}

import helpers.FormattedJsonOutput
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc._
import services.SectionsRanker

import scala.concurrent.{ExecutionContext, Future}

/**
  * Controller for exposing endpoints for the API's endpoints.
  *
  * @param cc             - Controller components
  * @param sectionsRanker - Dependency on the sections ranker service.
  * @param defaultContext - Play's default execution context
  */
@Singleton
class ApiController @Inject()(cc: ControllerComponents, sectionsRanker: SectionsRanker)
                             (implicit defaultContext: ExecutionContext)
  extends AbstractController(cc) with I18nSupport with FormattedJsonOutput {

  /**
    * Small case class to represent the expected input for the MostPopularApi endpoint.
    *
    * @param limit      - The amount of results to limit output to.
    * @param timePeriod - The time period of content to query most popular sections for.
    */
  private case class MostPopularQuery(limit: Int, timePeriod: Int)

  //custom constraint for the validation. Defines the period constraint of 1, 7 or 30.
  private val periodConstraint = Constraint[Int]("constraints.timeperiod") {
    case 1 | 7 | 30 => Valid
    case _ => Invalid(Seq(ValidationError("timePeriod must be 1, 7 or 30")))
  }

  //form object for input validation. Defaults limit to 10 and timePeriod to 7.
  private val mostPopularForm = Form(
    mapping(
      "limit" -> default[Int](number(min = 1), 10),
      "timePeriod" -> default(number, 1).verifying(periodConstraint)
    )(MostPopularQuery.apply)(MostPopularQuery.unapply)
  )

  /**
    * Action for the url /api/mostpopular. This endpoint has two parameters: limit, and timePeriod.
    *
    * @return A response to render to the user.
    */
  def mostPopular: Action[AnyContent] = Action.async { implicit request =>
    mostPopularForm.bindFromRequest().fold(badInput => Future {
      BadRequest(failed(badInput.errorsAsJson, BAD_REQUEST))
    }, query => {
      sectionsRanker.getMostPopularSections(query.limit, query.timePeriod)
        .map(set => {
          Ok(success(Json.toJson(set), set.length))
        }).recover {
        case _ => InternalServerError(failed("An error on our end occured", INTERNAL_SERVER_ERROR))
      }
    })
  }

}
