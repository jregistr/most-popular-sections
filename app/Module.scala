import com.google.inject.AbstractModule
import services._

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.

 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[SectionsLoader]).to(classOf[QueryingSectionsLoader]).asEagerSingleton()
    bind(classOf[SettingsLoader]).to(classOf[ConfigSettingsLoader]).asEagerSingleton()

    bind(classOf[CategoryQuery]).to(classOf[CategoryQueryOverRest])
    bind(classOf[SectionsRanker]).to(classOf[MostPopularSectionsRanker])
  }

}
