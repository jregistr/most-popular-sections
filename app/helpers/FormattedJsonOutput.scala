package helpers

import play.api.libs.json._

/**
  * Defines the standard json output for this API.
  */
trait FormattedJsonOutput {

  /**
    * Takes an output json data and formats the standard json output with it.
    *
    * @param data  - The payload data for the response.
    * @param count - The number of results.
    * @return - A Json Object formatted with the provided data.
    */
  def success(data: JsValue, count: Int): JsObject = JsObject(Seq(
    "success" -> JsBoolean(true),
    "status" -> JsNumber(200),
    "results" -> JsNumber(count),
    "data" -> data
  ))

  /**
    * Formats a standard error output json.
    * @param error - The error to provide the user.
    * @param code - The status code to be placed in the json.
    * @return - A Json Object formatted to the standard error format.
    */
  def failed(error: JsValue, code: Int): JsObject = JsObject(Seq(
    "success" -> JsBoolean(false),
    "status" -> JsNumber(code),
    "error" -> error
  ))

  /**
    * Formats the standard error output with a string error instead.
    * @see [[FormattedJsonOutput.failed()]]
    */
  def failed(error: String, code: Int): JsObject = failed(JsString(error), code)
}
