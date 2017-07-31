package helpers

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.JsString

class FormattedJsonOutputSpec extends PlaySpec with FormattedJsonOutput {

  "when data is passed, formatter" should {
    "simply add our data object in the standard json" in {
      val testData = JsString("hello world")
      val result = success(testData, 1)

      (result \ "data").as[String] must be("hello world")
      (result \ "results").as[Int] must be(1)
      (result \ "success").as[Boolean] must be(true)
    }

    "format a json with a js value with error json rather" in {
      val testData = JsString("ERROR OCCURED")
      val result = failed(testData, 404)

      (result \ "status").as[Int] must be(404)
      (result \ "error").as[String] must be(testData.value)
    }

    "format a json with a string message as the error" in {
      val stringVal = "Error message!"
      val result = failed(stringVal, 404)
      (result \ "error").as[String] must be(stringVal)
      (result \ "status").as[Int] must be(404)
    }
  }

}
