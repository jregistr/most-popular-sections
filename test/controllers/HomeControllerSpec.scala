package controllers

import mockws.MockWS
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.MimeTypes.HTML
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testinghelpers.FakeEndpoints

import scala.concurrent.Future

class HomeControllerSpec extends PlaySpec with GuiceOneServerPerSuite with FakeEndpoints {

  "when app is launched and configured, visiting home" should {
    "render an html view" in {
      val controller = app.injector.instanceOf[HomeController]
      val result: Future[Result] = controller.index().apply(FakeRequest())

      status(result) must be(OK)
      contentType(result) must contain(HTML)
    }
  }

  override def fakeApplication(): Application = {
    val ws = MockWS(sectionsEndPointGood)
    new GuiceApplicationBuilder()
      .configure("ny-times.api-key" -> expectedApiKey)
      .overrides(bind[WSClient].toInstance(ws))
      .build()
  }
}
