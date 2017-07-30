package services

import javax.inject.Inject

import akka.util.Timeout
import models.{AppearanceCount, Section}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait SectionsRanker {

  def getMostPopularSections(limit: Int, timePeriod: Int): Future[Seq[Section]]

}

class MostPopularSectionsRanker @Inject()(categoryQuery: CategoryQuery,
                                          sectionsLoader: SectionsLoader)
                                         (implicit val context: ExecutionContext) extends SectionsRanker {
  implicit val timeout: Timeout = 5.seconds

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
