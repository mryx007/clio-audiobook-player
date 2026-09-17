package de.clio.core.analytics.firebase

import com.google.firebase.analytics.logEvent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import de.clio.core.analytics.api.Analytics
import de.clio.core.logging.api.Logger
import com.google.firebase.analytics.FirebaseAnalytics as GmsFirebaseAnalytics

@ContributesBinding(AppScope::class)
class FirebaseAnalytics
private constructor(private val analytics: GmsFirebaseAnalytics) : Analytics {

  override fun screenView(screenName: String) {
    Logger.v("screenView($screenName)")
    analytics.logEvent(GmsFirebaseAnalytics.Event.SCREEN_VIEW) {
      param(GmsFirebaseAnalytics.Param.SCREEN_NAME, screenName)
    }
  }

  override fun event(
    name: String,
    params: Map<String, String>,
  ) {
    Logger.v("event(name=$name, params=$params)")
    analytics.logEvent(name) {
      params.forEach { (key, value) ->
        param(key, value)
      }
    }
  }
}
