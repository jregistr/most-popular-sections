package models

import play.api.libs.json.{Json, OFormat}


case class AppearanceCount(mostShared: Int, mostViewed: Int, mostEmailed: Int)

case class Section(section: String, appearedIn: AppearanceCount) extends Ordered[Section] {

  private def score(section: Section): Int = {
    val ap = section.appearedIn
    ap.mostViewed + ap.mostShared + ap.mostEmailed
  }

  override def compare(that: Section): Int = score(this).compareTo(score(that))
}

object Section {
  implicit val format: OFormat[Section] = Json.format[Section]
}

object AppearanceCount {
  implicit val format: OFormat[AppearanceCount] = Json.format[AppearanceCount]
}