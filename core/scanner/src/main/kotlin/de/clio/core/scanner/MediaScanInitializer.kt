package de.clio.core.scanner

import de.clio.core.initializer.AppInitializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
public class MediaScanInitializer(private val mediaScanTrigger: MediaScanTrigger) : AppInitializer {

  override fun onAppStart(application: android.app.Application) {
    mediaScanTrigger.scan()
  }
}
