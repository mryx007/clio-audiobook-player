package de.clio.core.analytics.firebase

import android.app.Application
import androidx.datastore.core.DataStore
import de.clio.core.data.store.AnalyticsConsentStore
import de.clio.core.initializer.AppInitializer
import de.clio.core.logging.api.Logger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.google.firebase.analytics.FirebaseAnalytics as GmsFirebaseAnalytics

@ContributesIntoSet(AppScope::class)
class EnableAnalyticsOnConsent(
  @AnalyticsConsentStore
  private val analyticsConsentStore: DataStore<Boolean>,
  private val analytics: GmsFirebaseAnalytics,
) : AppInitializer {

  private val scope = MainScope()

  override fun onAppStart(application: Application) {
    scope.launch {
      analyticsConsentStore.data.collect {
        Logger.d("Enabling analytics collection: $it")
        analytics.setAnalyticsCollectionEnabled(it)
      }
    }
  }
}
