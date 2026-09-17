package de.clio.core.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import coil.Coil
import coil.ImageLoader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import de.clio.core.data.ThemeMode
import de.clio.core.data.store.ThemeModeStore
import de.clio.core.initializer.AppInitializer

@ContributesIntoSet(AppScope::class)
class UIAppStartInitializer(
  @ThemeModeStore
  private val themeModeStore: DataStore<ThemeMode>,
  private val scope: CoroutineScope,
) : AppInitializer {

  override fun onAppStart(application: Application) {
    Coil.setImageLoader(
      ImageLoader.Builder(application)
        .addLastModifiedToFileCacheKey(false)
        .build(),
    )
    themeModeStore.data
      .distinctUntilChanged()
      .onEach { themeMode ->
        val nightMode = when (themeMode) {
          ThemeMode.FollowSystem, ThemeMode.Dynamic -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
          ThemeMode.Light -> AppCompatDelegate.MODE_NIGHT_NO
          else -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
      }
      .launchIn(scope)
  }
}
