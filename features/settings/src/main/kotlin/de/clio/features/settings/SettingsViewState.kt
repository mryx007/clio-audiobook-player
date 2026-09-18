package de.clio.features.settings

import de.clio.core.data.BackButtonBehavior
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.data.ThemeColor
import de.clio.core.data.ThemeMode
import java.time.LocalTime

data class SettingsViewState(
  val themeMode: ThemeMode,
  val customThemeColor: ThemeColor,
  val playbackBackgroundStyle: PlaybackBackgroundStyle,
  val backButtonBehavior: BackButtonBehavior,
  val rewindTimeInSeconds: Int,
  val fastForwardTimeInSeconds: Int,
  val autoRewindInSeconds: Int,
  val appVersion: String,
  val dialog: Dialog?,
  val useGrid: Boolean,
  val autoSleepTimer: AutoSleepTimerViewState,
  val showAnalyticSetting: Boolean,
  val analyticsEnabled: Boolean,
  val openLastBookOnStartup: Boolean,
  val showDeveloperMenu: Boolean,
  val showSupportDevelopment: Boolean,
  val kioskMode: Boolean,
) {

  enum class Dialog {
    AutoRewindAmount,
    RewindTime,
    FastForwardTime,
    Theme,
    BackgroundStyle,
    BackButtonBehavior,
  }

  companion object {
    fun preview(): SettingsViewState {
      return SettingsViewState(
        themeMode = ThemeMode.FollowSystem,
        customThemeColor = ThemeColor(),
        playbackBackgroundStyle = PlaybackBackgroundStyle.Solid,
        backButtonBehavior = BackButtonBehavior.BookOverview,
        rewindTimeInSeconds = 20,
        fastForwardTimeInSeconds = 30,
        autoRewindInSeconds = 12,
        dialog = null,
        appVersion = "1.2.3",
        useGrid = true,
        autoSleepTimer = AutoSleepTimerViewState.preview(),
        analyticsEnabled = false,
        openLastBookOnStartup = false,
        showAnalyticSetting = true,
        showDeveloperMenu = true,
        showSupportDevelopment = true,
        kioskMode = false,
      )
    }
  }

  data class AutoSleepTimerViewState(
    val enabled: Boolean,
    val startTime: LocalTime,
    val endTime: LocalTime,
  ) {
    companion object {
      fun preview(): AutoSleepTimerViewState {
        return AutoSleepTimerViewState(
          enabled = false,
          startTime = LocalTime.of(22, 0),
          endTime = LocalTime.of(6, 0),
        )
      }
    }
  }
}
