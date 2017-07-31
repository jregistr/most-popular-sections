package services

import javax.inject.Inject

import models.{AppearanceCount, Section}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Defines an object that will rank sections across categories
  */
trait SectionsRanker {

  /**
    * Finds and sorts sections according to their appearances in the available categories.
    *
    * @param limit      - The max number of results to return. If a number higher than available sections
    *                   is provided, the number of sections of results is returned.
    * @param timePeriod - The time period of content to query against.
    * @return - The sorted formatted data.
    */
  def getMostPopularSections(limit: Int, timePeriod: Int): Future[Seq[Section]]

}

/**
  * An implementation of the SectionsRanker which makes use of the category query service to get
  * data from NY times api.
  *
  * @param categoryQuery  - A dependency on the category query object used to query the api.
  * @param sectionsLoader - The sections loader object to get the available sections.
  * @param context        - A dependency on play's default execution context.
  */
class MostPopularSectionsRanker @Inject()(categoryQuery: CategoryQuery,
                                          sectionsLoader: SectionsLoader)
                                         (implicit val context: ExecutionContext) extends SectionsRanker {

  /**
    *
    * @param limit      - The max number of results to return. If a number higher than available sections
    *                   is provided, the number of sections of results is returned.
    * @param timePeriod - The time period of content to query against.
    * @return - The sorted formatted data.
    */
  override def getMostPopularSections(limit: Int, timePeriod: Int): Future[Seq[Section]] = {
    val sections = sectionsLoader.sections.get()
    val countsQuery = categoryQuery.getCountsInCategory(sections, timePeriod) _

    Future.sequence(List(
      countsQuery(Constants.URL_MOST_VIEWED),
      countsQuery(Constants.URL_MOST_SHARED),
      countsQuery(Constants.URL_MOST_MAILED)
    )).map(futures => {
      val viewed = futures.head
      val shared = futures(1)
      val mailed = futures.last

      sections.foldLeft(List[Section]())((list, sectionName) => {
        val vCount = viewed.getOrElse(sectionName, 0)
        val sCount = shared.getOrElse(sectionName, 0)
        val mCount = mailed.getOrElse(sectionName, 0)
        val appearanceCount = AppearanceCount(sCount, vCount, mCount)
        list :+ Section(sectionName, appearanceCount)
      }).sorted.reverse.take(limit)
    })

  }
}
