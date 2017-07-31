import helpers.FakeEndpoints
import mockws.MockWS
import mockws.MockWS.Routes
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import services.{CategoryQuery, Constants}

class CategoryQuerySpec extends PlaySpec with GuiceOneAppPerSuite with FakeEndpoints with ScalaFutures {

  "When app is configured and all sections have values in all categories, querying for categories" should {
    val viewedPairs = makeSectionCountPairings(1, sectionsNames).toMap
    val mailedPairs = makeSectionCountPairings(2, sectionsNames).toMap
    val sharedPairs = makeSectionCountPairings(3, sectionsNames).toMap

    val endpoints: Routes =
      sectionsEndPointGood orElse
        createCategoryEndPoints(Constants.URL_MOST_VIEWED, 7, viewedPairs.toList) orElse
        createCategoryEndPoints(Constants.URL_MOST_MAILED, 7, mailedPairs.toList) orElse
        createCategoryEndPoints(Constants.URL_MOST_SHARED, 7, sharedPairs.toList)

    val ws = MockWS(endpoints)
    val app = new GuiceApplicationBuilder()
      .configure("ny-times.api-key" -> expectedApiKey)
      .overrides(bind[WSClient].toInstance(ws))
      .build()

    "yield expected counts for all sections for the most viewed category" in {
      val query = app.injector.instanceOf[CategoryQuery]
      val futureOutput = query.getCountsInCategory(sectionsNames, 7)(Constants.URL_MOST_VIEWED)

      val sectionCountsOutput = futureOutput.futureValue
      val check = checkCounts(viewedPairs, sectionCountsOutput)

      check must be(true)
    }

    "also should yield expected values for most mailed" in {
      val query = app.injector.instanceOf[CategoryQuery]
      val futureOutput = query.getCountsInCategory(sectionsNames, 7)(Constants.URL_MOST_MAILED)

      val countsOutput = futureOutput.futureValue
      val check = checkCounts(mailedPairs, countsOutput)

      check must be(true)
    }

    "also should yield expected values for most shared" in {
      val query = app.injector.instanceOf[CategoryQuery]
      val futureOutput = query.getCountsInCategory(sectionsNames, 7)(Constants.URL_MOST_SHARED)

      val countsOutput = futureOutput.futureValue
      val check = checkCounts(sharedPairs, countsOutput)

      check must be(true)
    }

  }

  "when app is well configured, and endpoint has error, querying" should {
    val viewedPairs = makeSectionCountPairings(1, sectionsNames).toMap
    val mailedPairs = makeSectionCountPairings(2, sectionsNames).toMap
    val sharedPairs = makeSectionCountPairings(3, sectionsNames).toMap

    val endpoints: Routes =
      sectionsEndPointGood orElse
        createCategoryEndPoints(Constants.URL_MOST_VIEWED, 7, viewedPairs.toList, pos = false) orElse
        createCategoryEndPoints(Constants.URL_MOST_MAILED, 7, mailedPairs.toList, pos = false) orElse
        createCategoryEndPoints(Constants.URL_MOST_SHARED, 7, sharedPairs.toList, pos = false)

    val ws = MockWS(endpoints)
    val app = new GuiceApplicationBuilder()
      .configure("ny-times.api-key" -> expectedApiKey)
      .overrides(bind[WSClient].toInstance(ws))
      .build()

    "yield an empty for for any endpoint that has error" in {
      val query = app.injector.instanceOf[CategoryQuery]
      val mkQuery = query.getCountsInCategory(sectionsNames, 7) _

      val viewed = mkQuery(Constants.URL_MOST_VIEWED).futureValue
      val shared = mkQuery(Constants.URL_MOST_SHARED).futureValue
      val emailed = mkQuery(Constants.URL_MOST_MAILED).futureValue

      val allZeroes = sectionsNames.map(_ -> 0).toMap

      checkCounts(allZeroes, viewed) must be(true)
      checkCounts(allZeroes, shared) must be(true)
      checkCounts(allZeroes, emailed) must be(true)
    }
  }

  private def checkCounts(expected: Map[String, Int], actual: Map[String, Int]): Boolean =
    expected.forall(pair => {
      actual.isDefinedAt(pair._1) && actual(pair._1) == pair._2
    })

}
