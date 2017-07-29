package controllers

import javax.inject._
import play.api.mvc._

/**
  * Simple controller, exists to display user data for the api.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
    * Action to return the API documentation page to users.
    *
    * @return - An html response to be rendered.
    */
  def index = Action {
    Ok(views.html.index("Most Popular Sections"))
  }
}
