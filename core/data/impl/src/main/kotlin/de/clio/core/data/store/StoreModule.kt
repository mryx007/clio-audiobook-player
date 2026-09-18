package de.clio.core.data.store

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import de.clio.core.data.BackButtonBehavior
import de.clio.core.data.BookId
import de.clio.core.data.BookSortOrder
import de.clio.core.data.GridMode
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.data.PlayerButtonVisibility
import de.clio.core.data.ThemeColor
import de.clio.core.data.ThemeMode
import de.clio.core.data.sleeptimer.SleepTimerPreference
import de.clio.core.featureflag.FeatureFlagOverride
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@ContributesTo(AppScope::class)
public interface StoreModule {

  @Provides
  @SingleIn(AppScope::class)
  private fun sharedPreferences(context: Application): SharedPreferences {
    return context.getSharedPreferences(
      "${context.packageName}_preferences",
      Context.MODE_PRIVATE,
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @ThemeModeStore
  private fun themeMode(
    factory: ClioDataStoreFactory,
    application: Application,
    sharedPreferences: SharedPreferences,
  ): DataStore<ThemeMode> {
    return factory.create(
      serializer = ThemeMode.serializer(),
      fileName = "themeMode",
      defaultValue = ThemeMode.FollowSystem,
      migrations = listOf(
        LegacyDarkThemeMigration(application, sharedPreferences),
      ),
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @ThemeColorStore
  private fun themeColor(factory: ClioDataStoreFactory): DataStore<ThemeColor> {
    return factory.create(
      serializer = ThemeColor.serializer(),
      fileName = "themeColor",
      defaultValue = ThemeColor(),
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @AutoRewindAmountStore
  private fun autoRewindAmount(
    factory: ClioDataStoreFactory,
    sharedPreferences: SharedPreferences,
  ): DataStore<Int> {
    return factory.int(
      fileName = "autoRewind",
      defaultValue = 2,
      migrations = listOf(intPrefsDataMigration(sharedPreferences, "AUTO_REWIND")),
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @FadeOutStore
  private fun fadeOut(factory: ClioDataStoreFactory): DataStore<Duration> {
    return factory.create(
      fileName = "fadeOut",
      defaultValue = 10.seconds,
      serializer = Duration.serializer(),
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @RewindTimeStore
  private fun rewindTime(
    factory: ClioDataStoreFactory,
    sharedPreferences: SharedPreferences,
  ): DataStore<Int> {
    return factory.int(
      fileName = "seekTime",
      defaultValue = 20,
      migrations = listOf(intPrefsDataMigration(sharedPreferences, "SEEK_TIME")),
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @SeekTimeStore
  private fun seekTime(@RewindTimeStore rewindTimeStore: DataStore<Int>): DataStore<Int> = rewindTimeStore

  @Provides
  @SingleIn(AppScope::class)
  @FastForwardTimeStore
  private fun fastForwardTime(factory: ClioDataStoreFactory): DataStore<Int> {
    return factory.int(
      fileName = "fastForwardTime",
      defaultValue = 30,
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @SleepTimerPreferenceStore
  private fun sleepTimerPreference(factory: ClioDataStoreFactory): DataStore<SleepTimerPreference> {
    return factory.create(
      serializer = SleepTimerPreference.Companion.serializer(),
      fileName = "sleepTime3",
      defaultValue = SleepTimerPreference.Default,
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @GridModeStore
  private fun gridMode(
    factory: ClioDataStoreFactory,
    sharedPreferences: SharedPreferences,
  ): DataStore<GridMode> {
    return factory.create(
      GridMode.serializer(),
      GridMode.FOLLOW_DEVICE,
      "gridMode",
      migrations = listOf(
        PrefsDataMigration(
          sharedPreferences,
          key = "gridView",
          getFromSharedPreferences = {
            when (sharedPreferences.getString("gridView", null)) {
              "LIST" -> GridMode.LIST
              "GRID" -> GridMode.GRID
              else -> GridMode.FOLLOW_DEVICE
            }
          },
        ),
      ),
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @GridColumnCountStore
  private fun gridColumnCount(factory: ClioDataStoreFactory): DataStore<Int> {
    return factory.int(
      fileName = "gridColumnCount",
      defaultValue = 2,
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @BookSortOrderStore
  private fun bookSortOrder(factory: ClioDataStoreFactory): DataStore<BookSortOrder> {
    return factory.create(
      serializer = BookSortOrder.serializer(),
      defaultValue = BookSortOrder.Default,
      fileName = "bookSortOrder",
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @OnboardingCompletedStore
  private fun onboardingCompleted(factory: ClioDataStoreFactory): DataStore<Boolean> {
    return factory.boolean("onboardingCompleted", defaultValue = false)
  }

  @Provides
  @SingleIn(AppScope::class)
  @CurrentBookStore
  private fun currentBook(factory: ClioDataStoreFactory): DataStore<BookId?> {
    return factory.create(
      serializer = BookId.serializer().nullable,
      fileName = "currentBook",
      defaultValue = null,
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @AmountOfBatteryOptimizationRequestedStore
  private fun amountOfBatteryOptimizationsRequestedStore(factory: ClioDataStoreFactory): DataStore<Int> {
    return factory.int("amountOfBatteryOptimizationsRequestedStore", 0)
  }

  @Provides
  @SingleIn(AppScope::class)
  @ReviewDialogShownStore
  private fun reviewDialogShown(factory: ClioDataStoreFactory): DataStore<Boolean> {
    return factory.create(Boolean.serializer(), false, "reviewDialogShown")
  }

  @Provides
  @SingleIn(AppScope::class)
  @FolderPickerMovedDialogShownStore
  private fun folderPickerMovedDialogShown(factory: ClioDataStoreFactory): DataStore<Boolean> {
    return factory.boolean("folderPickerMovedDialogShow2n", defaultValue = false)
  }

  @Provides
  @SingleIn(AppScope::class)
  @AnalyticsConsentStore
  private fun analyticsConsent(factory: ClioDataStoreFactory): DataStore<Boolean> {
    return factory.boolean("analyticsConsent", defaultValue = false)
  }

  @Provides
  @SingleIn(AppScope::class)
  @DeveloperMenuUnlockedStore
  private fun developerMenuUnlocked(factory: ClioDataStoreFactory): DataStore<Boolean> {
    return factory.boolean("developerMenuUnlocked", defaultValue = false)
  }

  @Provides
  @SingleIn(AppScope::class)
  @FeatureFlagOverridesStore
  private fun featureFlagOverrides(factory: ClioDataStoreFactory): DataStore<Map<String, FeatureFlagOverride>> {
    return factory.create(
      serializer = MapSerializer(String.serializer(), FeatureFlagOverride.serializer()),
      defaultValue = emptyMap(),
      fileName = "featureFlagOverrides",
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @OpenLastBookOnStartupStore
  private fun openLastBookOnStartup(factory: ClioDataStoreFactory): DataStore<Boolean> {
    return factory.boolean("openLastBookOnStartup", defaultValue = false)
  }

  @Provides
  @SingleIn(AppScope::class)
  @PlaybackBackgroundStyleStore
  private fun playbackBackgroundStyle(factory: ClioDataStoreFactory): DataStore<PlaybackBackgroundStyle> {
    return factory.create(
      serializer = PlaybackBackgroundStyle.serializer(),
      fileName = "playbackBackgroundStyle",
      defaultValue = PlaybackBackgroundStyle.Solid,
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @PlayerButtonVisibilityStore
  private fun playerButtonVisibility(factory: ClioDataStoreFactory): DataStore<PlayerButtonVisibility> {
    return factory.create(
      serializer = PlayerButtonVisibility.serializer(),
      fileName = "playerButtonVisibility",
      defaultValue = PlayerButtonVisibility(),
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @PlayerLockedStore
  private fun playerLocked(factory: ClioDataStoreFactory): DataStore<Boolean> {
    return factory.boolean("playerLocked", defaultValue = false)
  }

  @Provides
  @SingleIn(AppScope::class)
  @BackButtonBehaviorStore
  private fun backButtonBehavior(factory: ClioDataStoreFactory): DataStore<BackButtonBehavior> {
    return factory.create(
      serializer = BackButtonBehavior.serializer(),
      fileName = "backButtonBehavior",
      defaultValue = BackButtonBehavior.BookOverview,
    )
  }
}

private class LegacyDarkThemeMigration(
  application: Application,
  private val sharedPreferences: SharedPreferences,
) : DataMigration<ThemeMode> {

  private val oldDataStoreFile = File(application.applicationContext.filesDir, "datastore/darkTheme")

  override suspend fun cleanUp() {
    oldDataStoreFile.delete()
    sharedPreferences.edit {
      remove("darkTheme")
    }
  }

  override suspend fun migrate(currentData: ThemeMode): ThemeMode {
    val legacyValue = when {
      oldDataStoreFile.exists() -> oldDataStoreFile.readText().trim().toBooleanStrictOrNull()
      sharedPreferences.contains("darkTheme") -> sharedPreferences.getBoolean("darkTheme", false)
      else -> null
    }
    return when (legacyValue) {
      true -> ThemeMode.Dark
      false -> ThemeMode.Light
      null -> currentData
    }
  }

  override suspend fun shouldMigrate(currentData: ThemeMode): Boolean {
    return oldDataStoreFile.exists() || sharedPreferences.contains("darkTheme")
  }
}
