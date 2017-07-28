package controllers

import play.api.libs.json.{JsBoolean, JsNumber, JsObject, JsValue}

trait FormattedJsonOutput {

  def success(data: JsValue, count: Int): JsObject = JsObject(Seq(
    "success" -> JsBoolean(true),
    "status" -> JsNumber(200),
    "results" -> JsNumber(count),
    "data" -> data
  ))

  def failed(error: JsValue, code: Int): JsObject = JsObject(Seq(
    "success" -> JsBoolean(false),
    "status" -> JsNumber(code),
    "error" -> error
  ))

}
