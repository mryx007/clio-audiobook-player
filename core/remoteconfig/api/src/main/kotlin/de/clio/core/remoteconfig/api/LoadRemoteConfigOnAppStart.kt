package de.clio.core.remoteconfig.api

import android.app.Application
import de.clio.core.common.DispatcherProvider
import de.clio.core.common.MainScope
import de.clio.core.initializer.AppInitializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.launch

@ContributesIntoSet(AppScope::class)
class LoadRemoteConfigOnAppStart(
  private val remoteConfig: RemoteConfig,
  dispatcherProvider: DispatcherProvider,
) : AppInitializer {

  private val mainScope = MainScope(dispatcherProvider)

  override fun onAppStart(application: Application) {
    mainScope.launch {
      remoteConfig.refresh()
    }
  }
}
