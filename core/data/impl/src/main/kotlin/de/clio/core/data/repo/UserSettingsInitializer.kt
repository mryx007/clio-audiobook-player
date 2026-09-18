package de.clio.core.data.repo

import android.app.Application
import de.clio.core.initializer.AppInitializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
public class UserSettingsInitializer(
  @Suppress("unused")
  private val userSettingsRepository: UserSettingsRepository,
) : AppInitializer {

  override fun onAppStart(application: Application) {
    // Eagerly pre-warms userSettingsRepository on app startup so all DataStore StateFlows are ready on Frame 0
  }
}
