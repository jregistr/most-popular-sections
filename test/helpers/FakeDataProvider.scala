package helpers

import com.github.javafaker.Faker
import play.api.libs.json._

import scala.util.Random

trait FakeDataProvider {
  val sectionsNames: Seq[String] = List(
    "Obituaries",
    "Automobiles",
    "Climate",
    "Education",
    "Science",
    "Food",
    "Opinion",
    "Books",
    "Magazine",
    "Movies",
    "Sports"
  )

  val sectionNamesAsMap: Map[String, Int] = sectionsNames.map(_ -> 0).toMap

  val sectionsResponse: JsObject = JsArray(sectionsNames.map(name => JsObject(Seq("name" -> JsString(name)))))

  val emptySectionsResponse: JsObject = JsArray()

  def articlesForSection(section: String, count: Int): JsObject = makeRandomArticles(section, count)

  def makeRandomArticles(section: String, count: Int): JsArray = {
    val faker = new Faker
    val articles = for (_ <- 0 until count) yield JsObject(Seq(
      "url" -> JsString(faker.internet().url()),
      "title" -> JsString(faker.book().title()),
      "byline" -> JsString(faker.name().fullName()),
      "section" -> JsString(section)
    ))
    JsArray(articles)
  }

  def randBetween(max: Int, min: Int = 0): Int = min + new Random().nextInt((max - min) + 1)

  def erroredResponse(errors: Seq[String]): JsObject = JsObject(Seq(
    "status" -> JsString("ERROR"),
    "errors" -> JsArray(errors.map(e => JsString(e))),
    "results" -> JsArray()
  ))

  def makeSectionCountPairings(start: Int, names: Seq[String]): List[(String, Int)] =
    names.foldLeft(List[(String, Int)]())((list, name) => {
      val next = if (list.isEmpty) start else list.last._2 + 1
      list :+ name -> next
    })

  private implicit def formatArrayResponse(array: JsArray): JsObject = formatArrayResponse(array, "OK")

  private def formatArrayResponse(array: JsArray, status: String): JsObject = JsObject(Seq(
    "status" -> JsString(status),
    "num_results" -> JsNumber(array.value.length),
    "results" -> array
  ))
}



