package de.clio.features.settings

import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.data.ThemeMode
import java.time.LocalTime

interface SettingsListener {
  fun close()
  fun onThemeModeRowClick()
  fun onPlaybackBackgroundStyleRowClick()
  fun setThemeMode(themeMode: ThemeMode)
  fun setCustomThemeHex(hex: String)
  fun setCustomTheme(
    hex: String,
    hue: Int,
  ) = setCustomThemeHex(hex)
  fun setPlaybackBackgroundStyle(style: PlaybackBackgroundStyle)
  fun toggleGrid()
  fun rewindAmountChanged(seconds: Int)
  fun onRewindRowClick()
  fun fastForwardAmountChanged(seconds: Int)
  fun onFastForwardRowClick()
  fun autoRewindAmountChang(seconds: Int)
  fun onAutoRewindRowClick()
  fun dismissDialog()
  fun getSupport()
  fun suggestIdea()
  fun openBugReport()
  fun openTranslations()
  fun openFaq()
  fun openSupportVoice()
  fun setAutoSleepTimer(checked: Boolean)
  fun setAutoSleepTimerStart(time: LocalTime)
  fun setAutoSleepTimerEnd(time: LocalTime)
  fun toggleAnalytics()
  fun toggleOpenLastBookOnStartup()
  fun openFolderPicker()
  fun onAppVersionClick()

  fun openDeveloperMenu()
  fun openStatistics()
  fun openPlayerControlsSettings()

  companion object {
    fun noop() = object : SettingsListener {
      override fun close() {}
      override fun onThemeModeRowClick() {}
      override fun onPlaybackBackgroundStyleRowClick() {}
      override fun setThemeMode(themeMode: ThemeMode) {}
      override fun setCustomThemeHex(hex: String) {}
      override fun setPlaybackBackgroundStyle(style: PlaybackBackgroundStyle) {}
      override fun toggleGrid() {}
      override fun rewindAmountChanged(seconds: Int) {}
      override fun onRewindRowClick() {}
      override fun fastForwardAmountChanged(seconds: Int) {}
      override fun onFastForwardRowClick() {}
      override fun autoRewindAmountChang(seconds: Int) {}
      override fun onAutoRewindRowClick() {}
      override fun dismissDialog() {}
      override fun getSupport() {}
      override fun suggestIdea() {}
      override fun openBugReport() {}
      override fun openTranslations() {}
      override fun openFaq() {}
      override fun openSupportVoice() {}
      override fun setAutoSleepTimer(checked: Boolean) {}
      override fun setAutoSleepTimerStart(time: LocalTime) {}
      override fun setAutoSleepTimerEnd(time: LocalTime) {}
      override fun toggleAnalytics() {}
      override fun toggleOpenLastBookOnStartup() {}
      override fun openFolderPicker() {}
      override fun onAppVersionClick() {}
      override fun openDeveloperMenu() {}
      override fun openStatistics() {}
      override fun openPlayerControlsSettings() {}
    }
  }
}
