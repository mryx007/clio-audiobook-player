package de.clio.core.analytics.noop

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import de.clio.core.analytics.api.Analytics
import de.clio.core.logging.api.Logger

@ContributesBinding(AppScope::class)
class NoOpAnalytics : Analytics {

  override fun screenView(screenName: String) {
    Logger.v("screenView($screenName)")
  }

  override fun event(
    name: String,
    params: Map<String, String>,
  ) {
    Logger.v("event(name=$name, params=$params)")
  }
}
