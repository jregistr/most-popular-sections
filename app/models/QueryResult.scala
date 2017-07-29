package models

import play.api.libs.json.{Json, OFormat}

/**
  * Encapsulates view count per category data.
  *
  * @param mostShared  - The view count for the most shared category.
  * @param mostViewed  - @see[[mostShared]]
  * @param mostEmailed @see [[mostShared]]
  */
case class AppearanceCount(mostShared: Int, mostViewed: Int, mostEmailed: Int)

/**
  * @see [[AppearanceCount]]
  */
case class Section(section: String, appearedIn: AppearanceCount) extends Ordered[Section] {

  override def compare(that: Section): Int = {
    def score(section: Section): Int = {
      val ap = section.appearedIn
      ap.mostViewed + ap.mostShared + ap.mostEmailed
    }

    score(this).compareTo(score(that))
  }
}

/**
  * Companion Object to define json conversion.
  */
object Section {
  implicit val format: OFormat[Section] = Json.format[Section]
}

/**
  * Companion Object to define json conversion.
  */
object AppearanceCount {
  implicit val format: OFormat[AppearanceCount] = Json.format[AppearanceCount]
}