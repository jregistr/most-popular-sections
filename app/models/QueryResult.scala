package models

case class AppearanceCount(mostShared: Int, mostViewed: Int, mostEmailed: Int)

case class Section(section: String, appearedIn: AppearanceCount)