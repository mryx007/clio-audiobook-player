package de.clio.core.data.repo

import de.clio.core.data.BackButtonBehavior
import de.clio.core.data.EndOfBookBehavior
import de.clio.core.data.GridMode
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.data.ThemeColor
import de.clio.core.data.ThemeMode
import de.clio.core.data.sleeptimer.SleepTimerPreference
import kotlinx.coroutines.flow.StateFlow

public interface UserSettingsRepository {
  public val themeMode: StateFlow<ThemeMode>
  public val themeColor: StateFlow<ThemeColor>
  public val autoRewindAmount: StateFlow<Int>
  public val rewindTime: StateFlow<Int>
  public val fastForwardTime: StateFlow<Int>
  public val gridMode: StateFlow<GridMode>
  public val sleepTimerPreference: StateFlow<SleepTimerPreference>
  public val analyticsConsent: StateFlow<Boolean>
  public val openLastBookOnStartup: StateFlow<Boolean>
  public val playbackBackgroundStyle: StateFlow<PlaybackBackgroundStyle>
  public val backButtonBehavior: StateFlow<BackButtonBehavior>
  public val endOfBookBehavior: StateFlow<EndOfBookBehavior>
  public val developerMenuUnlocked: StateFlow<Boolean>

  public suspend fun setThemeMode(themeMode: ThemeMode)
  public suspend fun setThemeColor(themeColor: ThemeColor)
  public suspend fun setAutoRewindAmount(seconds: Int)
  public suspend fun setRewindTime(seconds: Int)
  public suspend fun setFastForwardTime(seconds: Int)
  public suspend fun setGridMode(gridMode: GridMode)
  public suspend fun setSleepTimerPreference(preference: SleepTimerPreference)
  public suspend fun setAnalyticsConsent(enabled: Boolean)
  public suspend fun setOpenLastBookOnStartup(enabled: Boolean)
  public suspend fun setPlaybackBackgroundStyle(style: PlaybackBackgroundStyle)
  public suspend fun setBackButtonBehavior(behavior: BackButtonBehavior)
  public suspend fun setEndOfBookBehavior(behavior: EndOfBookBehavior)
  public suspend fun setDeveloperMenuUnlocked(unlocked: Boolean)
}
