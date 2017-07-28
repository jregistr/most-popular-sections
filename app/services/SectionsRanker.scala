package services

import javax.inject.Inject

import models.Section

import scala.concurrent.{ExecutionContext, Future}

trait SectionsRanker {

  def getMostPopularSections(limit: Int, timePeriod: Int): Future[Seq[Section]]

}

class MostPopularSectionsRanker @Inject() (categoryQuery: CategoryQuery)
                                          (implicit val context: ExecutionContext) extends SectionsRanker {
  override def getMostPopularSections(limit: Int, timePeriod: Int): Future[Seq[Section]] = ???
}
