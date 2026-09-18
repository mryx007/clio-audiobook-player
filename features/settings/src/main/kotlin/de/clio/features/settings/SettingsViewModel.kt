package de.clio.features.settings

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import de.clio.core.common.AppInfoProvider
import de.clio.core.common.DispatcherProvider
import de.clio.core.common.MainScope
import de.clio.core.data.BackButtonBehavior
import de.clio.core.data.GridMode
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.data.ThemeColor
import de.clio.core.data.ThemeMode
import de.clio.core.data.repo.UserSettingsRepository
import de.clio.core.data.sleeptimer.SleepTimerPreference
import de.clio.core.featureflag.FeatureFlag
import de.clio.core.featureflag.KioskModeFeatureFlagQualifier
import de.clio.core.ui.GridCount
import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.time.LocalTime

@Inject
class SettingsViewModel(
  private val userSettingsRepository: UserSettingsRepository,
  private val navigator: Navigator,
  private val appInfoProvider: AppInfoProvider,
  private val gridCount: GridCount,
  @KioskModeFeatureFlagQualifier
  private val kioskModeFeatureFlag: FeatureFlag<Boolean>,
  dispatcherProvider: DispatcherProvider,
) : SettingsListener {

  private val mainScope = MainScope(dispatcherProvider)
  internal val viewEffects: SharedFlow<SettingsViewEffect>
    field = MutableSharedFlow<SettingsViewEffect>(extraBufferCapacity = 1)
  private val dialog = mutableStateOf<SettingsViewState.Dialog?>(null)
  private var appVersionTapCount = 0

  @Composable
  fun viewState(): SettingsViewState {
    val themeMode by userSettingsRepository.themeMode.collectAsState()
    val customThemeColor by userSettingsRepository.themeColor.collectAsState()
    val autoRewindAmount by userSettingsRepository.autoRewindAmount.collectAsState()
    val rewindTime by userSettingsRepository.rewindTime.collectAsState()
    val fastForwardTime by userSettingsRepository.fastForwardTime.collectAsState()
    val gridMode by userSettingsRepository.gridMode.collectAsState()
    val autoSleepTimer by userSettingsRepository.sleepTimerPreference.collectAsState()
    val analyticsEnabled by userSettingsRepository.analyticsConsent.collectAsState()
    val openLastBookOnStartup by userSettingsRepository.openLastBookOnStartup.collectAsState()
    val playbackBackgroundStyle by userSettingsRepository.playbackBackgroundStyle.collectAsState()
    val backButtonBehavior by userSettingsRepository.backButtonBehavior.collectAsState()
    val showDeveloperMenu by userSettingsRepository.developerMenuUnlocked.collectAsState()
    val kioskMode = remember {
      kioskModeFeatureFlag.get()
    }
    return SettingsViewState(
      themeMode = themeMode,
      customThemeColor = customThemeColor,
      playbackBackgroundStyle = playbackBackgroundStyle,
      backButtonBehavior = backButtonBehavior,
      rewindTimeInSeconds = rewindTime,
      fastForwardTimeInSeconds = fastForwardTime,
      autoRewindInSeconds = autoRewindAmount,
      dialog = dialog.value,
      appVersion = appInfoProvider.versionName,
      useGrid = when (gridMode) {
        GridMode.LIST -> false
        GridMode.GRID -> true
        GridMode.FOLLOW_DEVICE -> gridCount.useGridAsDefault()
      },
      autoSleepTimer = SettingsViewState.AutoSleepTimerViewState(
        enabled = autoSleepTimer.autoSleepTimerEnabled,
        startTime = autoSleepTimer.autoSleepStartTime,
        endTime = autoSleepTimer.autoSleepEndTime,
      ),
      analyticsEnabled = analyticsEnabled,
      openLastBookOnStartup = openLastBookOnStartup,
      showAnalyticSetting = appInfoProvider.analyticsIncluded,
      showDeveloperMenu = showDeveloperMenu,
      showSupportDevelopment = appInfoProvider.supportDevelopmentIncluded,
      kioskMode = kioskMode,
    )
  }

  override fun close() {
    navigator.goBack()
  }

  override fun onThemeModeRowClick() {
    dialog.value = SettingsViewState.Dialog.Theme
  }

  override fun onPlaybackBackgroundStyleRowClick() {
    dialog.value = SettingsViewState.Dialog.BackgroundStyle
  }

  override fun onBackButtonBehaviorRowClick() {
    dialog.value = SettingsViewState.Dialog.BackButtonBehavior
  }

  override fun setThemeMode(themeMode: ThemeMode) {
    mainScope.launch {
      userSettingsRepository.setThemeMode(themeMode)
    }
    dialog.value = null
  }

  override fun setBackButtonBehavior(behavior: BackButtonBehavior) {
    mainScope.launch {
      userSettingsRepository.setBackButtonBehavior(behavior)
    }
    dialog.value = null
  }

  override fun setCustomTheme(
    hex: String,
    hue: Int,
  ) {
    val formatted = if (hex.startsWith("#")) hex else "#$hex"
    mainScope.launch {
      userSettingsRepository.setThemeColor(userSettingsRepository.themeColor.value.copy(hex = formatted, hue = hue))
      userSettingsRepository.setThemeMode(ThemeMode.Custom)
    }
    dialog.value = null
  }

  override fun setCustomThemeHex(hex: String) {
    val clean = hex.removePrefix("#").trim()
    val colorInt = try {
      (0xFF000000L or clean.toLong(16)).toInt()
    } catch (_: Exception) {
      0
    }
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(colorInt, hsv)
    val hue = if (hsv[1] >= 0.12f) hsv[0].toInt() else 212
    setCustomTheme(hex, hue)
  }

  override fun setPlaybackBackgroundStyle(style: PlaybackBackgroundStyle) {
    mainScope.launch {
      userSettingsRepository.setPlaybackBackgroundStyle(style)
    }
    dialog.value = null
  }

  override fun toggleGrid() {
    mainScope.launch {
      val currentMode = userSettingsRepository.gridMode.value
      val nextMode = when (currentMode) {
        GridMode.LIST -> GridMode.GRID
        GridMode.GRID -> GridMode.LIST
        GridMode.FOLLOW_DEVICE -> if (gridCount.useGridAsDefault()) {
          GridMode.LIST
        } else {
          GridMode.GRID
        }
      }
      userSettingsRepository.setGridMode(nextMode)
    }
  }

  override fun rewindAmountChanged(seconds: Int) {
    mainScope.launch {
      userSettingsRepository.setRewindTime(seconds)
    }
  }

  override fun onRewindRowClick() {
    dialog.value = SettingsViewState.Dialog.RewindTime
  }

  override fun fastForwardAmountChanged(seconds: Int) {
    mainScope.launch {
      userSettingsRepository.setFastForwardTime(seconds)
    }
  }

  override fun onFastForwardRowClick() {
    dialog.value = SettingsViewState.Dialog.FastForwardTime
  }

  override fun autoRewindAmountChang(seconds: Int) {
    mainScope.launch {
      userSettingsRepository.setAutoRewindAmount(seconds)
    }
  }

  override fun onAutoRewindRowClick() {
    dialog.value = SettingsViewState.Dialog.AutoRewindAmount
  }

  override fun dismissDialog() {
    dialog.value = null
  }

  override fun getSupport() {
    navigator.goTo(Destination.Website("https://github.com/mryx007/clio-audiobook-player/discussions"))
  }

  override fun suggestIdea() {
    navigator.goTo(Destination.Website("https://github.com/mryx007/clio-audiobook-player/discussions"))
  }

  override fun openBugReport() {
    val url = "https://github.com/mryx007/clio-audiobook-player/issues/new".toUri()
      .buildUpon()
      .appendQueryParameter("template", "bug.yml")
      .appendQueryParameter("version", appInfoProvider.versionName)
      .appendQueryParameter("androidversion", Build.VERSION.SDK_INT.toString())
      .appendQueryParameter("device", Build.MODEL)
      .toString()
    navigator.goTo(Destination.Website(url))
  }

  override fun openTranslations() {
    dismissDialog()
    navigator.goTo(Destination.Website("https://hosted.weblate.org/engage/voice/"))
  }

  override fun openFaq() {
    navigator.goTo(Destination.Website("https://voice.woitaschek.de/faq/"))
  }

  override fun openSupportVoice() {
    navigator.goTo(Destination.SupportVoice)
  }

  override fun openStatistics() {
    navigator.goTo(Destination.Statistics)
  }

  override fun openFolderPicker() {
    navigator.goTo(Destination.FolderPicker)
  }

  override fun setAutoSleepTimer(checked: Boolean) {
    mainScope.launch {
      val currentPrefs = userSettingsRepository.sleepTimerPreference.value
      userSettingsRepository.setSleepTimerPreference(currentPrefs.copy(autoSleepTimerEnabled = checked))
    }
  }

  override fun setAutoSleepTimerStart(time: LocalTime) {
    mainScope.launch {
      val currentPrefs = userSettingsRepository.sleepTimerPreference.value
      userSettingsRepository.setSleepTimerPreference(currentPrefs.copy(autoSleepStartTime = time))
    }
  }

  override fun setAutoSleepTimerEnd(time: LocalTime) {
    mainScope.launch {
      val currentPrefs = userSettingsRepository.sleepTimerPreference.value
      userSettingsRepository.setSleepTimerPreference(currentPrefs.copy(autoSleepEndTime = time))
    }
  }

  override fun toggleAnalytics() {
    mainScope.launch {
      userSettingsRepository.setAnalyticsConsent(!userSettingsRepository.analyticsConsent.value)
    }
  }

  override fun toggleOpenLastBookOnStartup() {
    mainScope.launch {
      userSettingsRepository.setOpenLastBookOnStartup(!userSettingsRepository.openLastBookOnStartup.value)
    }
  }

  override fun onAppVersionClick() {
    mainScope.launch {
      if (userSettingsRepository.developerMenuUnlocked.value) {
        return@launch
      }
      if (++appVersionTapCount >= 13) {
        userSettingsRepository.setDeveloperMenuUnlocked(true)
        viewEffects.emit(SettingsViewEffect.DeveloperMenuUnlocked)
      }
    }
  }

  override fun openDeveloperMenu() {
    navigator.goTo(Destination.DeveloperSettings)
  }

  override fun openPlayerControlsSettings() {
    navigator.goTo(Destination.PlayerControlsSettings)
  }
}
