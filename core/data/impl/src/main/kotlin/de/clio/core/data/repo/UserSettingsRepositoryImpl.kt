package de.clio.core.data.repo

import androidx.datastore.core.DataStore
import de.clio.core.common.DispatcherProvider
import de.clio.core.common.MainScope
import de.clio.core.data.BackButtonBehavior
import de.clio.core.data.EndOfBookBehavior
import de.clio.core.data.GridMode
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.data.ThemeColor
import de.clio.core.data.ThemeMode
import de.clio.core.data.sleeptimer.SleepTimerPreference
import de.clio.core.data.store.AnalyticsConsentStore
import de.clio.core.data.store.AutoRewindAmountStore
import de.clio.core.data.store.BackButtonBehaviorStore
import de.clio.core.data.store.DeveloperMenuUnlockedStore
import de.clio.core.data.store.EndOfBookBehaviorStore
import de.clio.core.data.store.FastForwardTimeStore
import de.clio.core.data.store.GridModeStore
import de.clio.core.data.store.OpenLastBookOnStartupStore
import de.clio.core.data.store.PlaybackBackgroundStyleStore
import de.clio.core.data.store.RewindTimeStore
import de.clio.core.data.store.SleepTimerPreferenceStore
import de.clio.core.data.store.ThemeColorStore
import de.clio.core.data.store.ThemeModeStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
public class UserSettingsRepositoryImpl(
  @ThemeModeStore
  private val themeModeStore: DataStore<ThemeMode>,
  @ThemeColorStore
  private val themeColorStore: DataStore<ThemeColor>,
  @AutoRewindAmountStore
  private val autoRewindAmountStore: DataStore<Int>,
  @RewindTimeStore
  private val rewindTimeStore: DataStore<Int>,
  @FastForwardTimeStore
  private val fastForwardTimeStore: DataStore<Int>,
  @GridModeStore
  private val gridModeStore: DataStore<GridMode>,
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  @AnalyticsConsentStore
  private val analyticsConsentStore: DataStore<Boolean>,
  @OpenLastBookOnStartupStore
  private val openLastBookOnStartupStore: DataStore<Boolean>,
  @PlaybackBackgroundStyleStore
  private val playbackBackgroundStyleStore: DataStore<PlaybackBackgroundStyle>,
  @BackButtonBehaviorStore
  private val backButtonBehaviorStore: DataStore<BackButtonBehavior>,
  @EndOfBookBehaviorStore
  private val endOfBookBehaviorStore: DataStore<EndOfBookBehavior>,
  @DeveloperMenuUnlockedStore
  private val developerMenuUnlockedStore: DataStore<Boolean>,
  dispatcherProvider: DispatcherProvider = DispatcherProvider(),
) : UserSettingsRepository {

  private val scope: CoroutineScope = MainScope(dispatcherProvider)

  override val themeMode: StateFlow<ThemeMode> = themeModeStore.data
    .stateIn(scope, SharingStarted.Eagerly, ThemeMode.FollowSystem)

  override val themeColor: StateFlow<ThemeColor> = themeColorStore.data
    .stateIn(scope, SharingStarted.Eagerly, ThemeColor())

  override val autoRewindAmount: StateFlow<Int> = autoRewindAmountStore.data
    .stateIn(scope, SharingStarted.Eagerly, 2)

  override val rewindTime: StateFlow<Int> = rewindTimeStore.data
    .stateIn(scope, SharingStarted.Eagerly, 20)

  override val fastForwardTime: StateFlow<Int> = fastForwardTimeStore.data
    .stateIn(scope, SharingStarted.Eagerly, 30)

  override val gridMode: StateFlow<GridMode> = gridModeStore.data
    .stateIn(scope, SharingStarted.Eagerly, GridMode.FOLLOW_DEVICE)

  override val sleepTimerPreference: StateFlow<SleepTimerPreference> = sleepTimerPreferenceStore.data
    .stateIn(scope, SharingStarted.Eagerly, SleepTimerPreference.Default)

  override val analyticsConsent: StateFlow<Boolean> = analyticsConsentStore.data
    .stateIn(scope, SharingStarted.Eagerly, false)

  override val openLastBookOnStartup: StateFlow<Boolean> = openLastBookOnStartupStore.data
    .stateIn(scope, SharingStarted.Eagerly, false)

  override val playbackBackgroundStyle: StateFlow<PlaybackBackgroundStyle> = playbackBackgroundStyleStore.data
    .stateIn(scope, SharingStarted.Eagerly, PlaybackBackgroundStyle.Solid)

  override val backButtonBehavior: StateFlow<BackButtonBehavior> = backButtonBehaviorStore.data
    .stateIn(scope, SharingStarted.Eagerly, BackButtonBehavior.BookOverview)

  override val endOfBookBehavior: StateFlow<EndOfBookBehavior> = endOfBookBehaviorStore.data
    .stateIn(scope, SharingStarted.Eagerly, EndOfBookBehavior.DoNothing)

  override val developerMenuUnlocked: StateFlow<Boolean> = developerMenuUnlockedStore.data
    .stateIn(scope, SharingStarted.Eagerly, false)

  override suspend fun setThemeMode(themeMode: ThemeMode) {
    themeModeStore.updateData { themeMode }
  }

  override suspend fun setThemeColor(themeColor: ThemeColor) {
    themeColorStore.updateData { themeColor }
  }

  override suspend fun setAutoRewindAmount(seconds: Int) {
    autoRewindAmountStore.updateData { seconds }
  }

  override suspend fun setRewindTime(seconds: Int) {
    rewindTimeStore.updateData { seconds }
  }

  override suspend fun setFastForwardTime(seconds: Int) {
    fastForwardTimeStore.updateData { seconds }
  }

  override suspend fun setGridMode(gridMode: GridMode) {
    gridModeStore.updateData { gridMode }
  }

  override suspend fun setSleepTimerPreference(preference: SleepTimerPreference) {
    sleepTimerPreferenceStore.updateData { preference }
  }

  override suspend fun setAnalyticsConsent(enabled: Boolean) {
    analyticsConsentStore.updateData { enabled }
  }

  override suspend fun setOpenLastBookOnStartup(enabled: Boolean) {
    openLastBookOnStartupStore.updateData { enabled }
  }

  override suspend fun setPlaybackBackgroundStyle(style: PlaybackBackgroundStyle) {
    playbackBackgroundStyleStore.updateData { style }
  }

  override suspend fun setBackButtonBehavior(behavior: BackButtonBehavior) {
    backButtonBehaviorStore.updateData { behavior }
  }

  override suspend fun setEndOfBookBehavior(behavior: EndOfBookBehavior) {
    endOfBookBehaviorStore.updateData { behavior }
  }

  override suspend fun setDeveloperMenuUnlocked(unlocked: Boolean) {
    developerMenuUnlockedStore.updateData { unlocked }
  }
}
