package controllers

import akka.stream.Materializer
import mockws.MockWS
import mockws.MockWS.Routes
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsArray
import play.api.libs.ws.WSClient
import play.api.test.FakeRequest
import services.Constants
import testinghelpers.FakeEndpoints
import play.api.test.Helpers._

class ApiControllerSpec extends PlaySpec with GuiceOneServerPerSuite with FakeEndpoints {

  val top = List("Obituaries", "Automobiles", "Climate")

  "querying the api" should {
    implicit lazy val materializer: Materializer = app.materializer

    "return an error for values other than 1, 7 and 30 for time period" in {
      val controller = app.injector.instanceOf[ApiController]
      val request = FakeRequest(GET, "/api/mostpopular?timePeriod=5")
      val result = call(controller.mostPopular, request)

      status(result) must be(BAD_REQUEST)
    }

    "return with json response with 10 values when no limit are passed." in {
      val controller = app.injector.instanceOf[ApiController]
      val request = FakeRequest(GET, "/api/mostpopular?timePeriod=7")
      val result = call(controller.mostPopular, request)

      status(result) must be(OK)
      contentType(result) must contain(JSON)

      val json = contentAsJson(result)
      (json \ "success").as[Boolean] must be(true)
      (json \ "results").as[Int] must be(10)
      (json \ "data").as[JsArray].value must have size 10
    }

  }

  override def fakeApplication(): Application = {
    val mostViewed: List[(String, Int)] = makeSectionCountPairings(25, top)
    val mostShared: List[(String, Int)] = makeSectionCountPairings(15, top)
    val mostEmailed: List[(String, Int)] = makeSectionCountPairings(5, top)

    val rest: List[(String, Int)] = makeSectionCountPairings(1, sectionsNames.splitAt(3)._2)

    val endPoints: Routes = {
      val viewPoints = createCategoryEndPoints(Constants.URL_MOST_VIEWED, 7, mostViewed ++ rest)
      val sharedPoints = createCategoryEndPoints(Constants.URL_MOST_SHARED, 7, mostShared ++ rest)
      val emailedPoints = createCategoryEndPoints(Constants.URL_MOST_MAILED, 7, mostEmailed ++ rest)

      sectionsEndPointGood orElse viewPoints orElse sharedPoints orElse emailedPoints
    }

    val ws = MockWS(endPoints)
    new GuiceApplicationBuilder()
      .configure("ny-times.api-key" -> expectedApiKey)
      .overrides(bind[WSClient].toInstance(ws))
      .build()
  }
}
