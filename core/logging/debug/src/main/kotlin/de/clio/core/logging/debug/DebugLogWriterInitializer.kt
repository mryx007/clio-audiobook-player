package de.clio.core.logging.debug

import android.app.Application
import de.clio.core.initializer.AppInitializer
import de.clio.core.logging.api.Logger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class DebugLogWriterInitializer : AppInitializer {

  override fun onAppStart(application: Application) {
    Logger.install(DebugLogWriter())
  }
}
