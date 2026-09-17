package de.clio.core.logging.crashlytics

import android.app.Application
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import de.clio.core.initializer.AppInitializer
import de.clio.core.logging.api.Logger

@ContributesIntoSet(AppScope::class)
class CrashlyticsLogWriterInitializer : AppInitializer {

  override fun onAppStart(application: Application) {
    Logger.install(CrashlyticsLogWriter())
  }
}
