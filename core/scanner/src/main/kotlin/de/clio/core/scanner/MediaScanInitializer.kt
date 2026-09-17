package de.clio.core.scanner

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import de.clio.core.initializer.AppInitializer

@ContributesIntoSet(AppScope::class)
public class MediaScanInitializer(private val mediaScanTrigger: MediaScanTrigger) : AppInitializer {

  override fun onAppStart(application: android.app.Application) {
    mediaScanTrigger.scan()
  }
}
