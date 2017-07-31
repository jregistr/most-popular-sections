package services

import testinghelpers.FakeEndpoints
import mockws.MockWS
import mockws.MockWS.Routes
import models.Section
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient

class SectionsRankerSpec extends PlaySpec with GuiceOneAppPerSuite with FakeEndpoints with ScalaFutures {

  val top = List("Obituaries", "Automobiles", "Climate")

  val mostViewed: List[(String, Int)] = makeSectionCountPairings(25, top)
  val mostShared: List[(String, Int)] = makeSectionCountPairings(15, top)
  val mostEmailed: List[(String, Int)] = makeSectionCountPairings(7, top)

  val rest: List[(String, Int)] = makeSectionCountPairings(1, sectionsNames.splitAt(3)._2)

  val endPoints: Routes = {
    val viewPoints = createCategoryEndPoints(Constants.URL_MOST_VIEWED, 7, mostViewed ++ rest)
    val sharedPoints = createCategoryEndPoints(Constants.URL_MOST_SHARED, 7, mostShared ++ rest)
    val emailedPoints = createCategoryEndPoints(Constants.URL_MOST_MAILED, 7, mostEmailed ++ rest)

    sectionsEndPointGood orElse viewPoints orElse sharedPoints orElse emailedPoints
  }

  "when app is well configured, asking for most popular" should {
    val ws = MockWS(endPoints)
    val app = new GuiceApplicationBuilder()
      .configure("ny-times.api-key" -> expectedApiKey)
      .overrides(bind[WSClient].toInstance(ws))
      .build()

    "yield 8 values with Climate as the top" in {
      val ranker = app.injector.instanceOf[SectionsRanker]
      val mostPopulars: Seq[Section] = ranker.getMostPopularSections(8, 7).futureValue

      mostPopulars must have size 8
      mostPopulars.head.section must be("Climate")

      mostPopulars.head.appearedIn.mostViewed must be(27)
      mostPopulars.head.appearedIn.mostShared must be(17)
      mostPopulars.head.appearedIn.mostEmailed must be(9)
    }

    "yield 11 when limit above maximum available is passed" in {
      val ranker = app.injector.instanceOf[SectionsRanker]
      val mostPopulars: Seq[Section] = ranker.getMostPopularSections(50, 7).futureValue

      mostPopulars must have size 11
    }

  }

}
