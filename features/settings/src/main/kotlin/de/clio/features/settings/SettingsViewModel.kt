package de.clio.features.settings

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import de.clio.core.common.AppInfoProvider
import de.clio.core.common.DispatcherProvider
import de.clio.core.common.MainScope
import de.clio.core.data.GridMode
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.data.ThemeColor
import de.clio.core.data.ThemeMode
import de.clio.core.data.sleeptimer.SleepTimerPreference
import de.clio.core.data.store.AnalyticsConsentStore
import de.clio.core.data.store.AutoRewindAmountStore
import de.clio.core.data.store.DeveloperMenuUnlockedStore
import de.clio.core.data.store.FastForwardTimeStore
import de.clio.core.data.store.GridModeStore
import de.clio.core.data.store.OpenLastBookOnStartupStore
import de.clio.core.data.store.PlaybackBackgroundStyleStore
import de.clio.core.data.store.RewindTimeStore
import de.clio.core.data.store.SleepTimerPreferenceStore
import de.clio.core.data.store.ThemeColorStore
import de.clio.core.data.store.ThemeModeStore
import de.clio.core.featureflag.FeatureFlag
import de.clio.core.featureflag.KioskModeFeatureFlagQualifier
import de.clio.core.ui.GridCount
import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime

@Inject
class SettingsViewModel(
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
  private val navigator: Navigator,
  private val appInfoProvider: AppInfoProvider,
  @GridModeStore
  private val gridModeStore: DataStore<GridMode>,
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  @AnalyticsConsentStore
  private val analyticsConsentStore: DataStore<Boolean>,
  private val gridCount: GridCount,
  @KioskModeFeatureFlagQualifier
  private val kioskModeFeatureFlag: FeatureFlag<Boolean>,
  @DeveloperMenuUnlockedStore
  private val developerMenuUnlockedStore: DataStore<Boolean>,
  @OpenLastBookOnStartupStore
  private val openLastBookOnStartupStore: DataStore<Boolean>,
  @PlaybackBackgroundStyleStore
  private val playbackBackgroundStyleStore: DataStore<PlaybackBackgroundStyle>,
  dispatcherProvider: DispatcherProvider,
) : SettingsListener {

  private val mainScope = MainScope(dispatcherProvider)
  internal val viewEffects: SharedFlow<SettingsViewEffect>
    field = MutableSharedFlow<SettingsViewEffect>(extraBufferCapacity = 1)
  private val dialog = mutableStateOf<SettingsViewState.Dialog?>(null)
  private var appVersionTapCount = 0

  @Composable
  fun viewState(): SettingsViewState? {
    val themeMode by remember { themeModeStore.data }.collectAsState(initial = ThemeMode.FollowSystem)
    val customThemeColor by remember { themeColorStore.data }.collectAsState(initial = ThemeColor())
    val autoRewindAmount by remember { autoRewindAmountStore.data }.collectAsState(initial = 2)
    val rewindTime by remember { rewindTimeStore.data }.collectAsState(initial = 20)
    val fastForwardTime by remember { fastForwardTimeStore.data }.collectAsState(initial = 30)
    val gridMode = remember { gridModeStore.data }.collectAsState(initial = null).value ?: return null
    val autoSleepTimer by remember { sleepTimerPreferenceStore.data }.collectAsState(
      initial = SleepTimerPreference.Default,
    )
    val analyticsEnabled by remember { analyticsConsentStore.data }.collectAsState(initial = false)
    val openLastBookOnStartup by remember { openLastBookOnStartupStore.data }.collectAsState(initial = false)
    val playbackBackgroundStyle by remember { playbackBackgroundStyleStore.data }.collectAsState(initial = PlaybackBackgroundStyle.Solid)
    val kioskMode = remember {
      kioskModeFeatureFlag.get()
    }
    val showDeveloperMenu by remember { developerMenuUnlockedStore.data }.collectAsState(initial = false)
    return SettingsViewState(
      themeMode = themeMode,
      customThemeColor = customThemeColor,
      playbackBackgroundStyle = playbackBackgroundStyle,
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

  override fun setThemeMode(themeMode: ThemeMode) {
    mainScope.launch {
      themeModeStore.updateData { themeMode }
    }
    dialog.value = null
  }

  override fun setCustomTheme(
    hex: String,
    hue: Int,
  ) {
    val formatted = if (hex.startsWith("#")) hex else "#$hex"
    mainScope.launch {
      themeColorStore.updateData { it.copy(hex = formatted, hue = hue) }
      themeModeStore.updateData { ThemeMode.Custom }
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
      playbackBackgroundStyleStore.updateData { style }
    }
    dialog.value = null
  }

  override fun toggleGrid() {
    mainScope.launch {
      gridModeStore.updateData { currentMode ->
        when (currentMode) {
          GridMode.LIST -> GridMode.GRID
          GridMode.GRID -> GridMode.LIST
          GridMode.FOLLOW_DEVICE -> if (gridCount.useGridAsDefault()) {
            GridMode.LIST
          } else {
            GridMode.GRID
          }
        }
      }
    }
  }

  override fun rewindAmountChanged(seconds: Int) {
    mainScope.launch {
      rewindTimeStore.updateData { seconds }
    }
  }

  override fun onRewindRowClick() {
    dialog.value = SettingsViewState.Dialog.RewindTime
  }

  override fun fastForwardAmountChanged(seconds: Int) {
    mainScope.launch {
      fastForwardTimeStore.updateData { seconds }
    }
  }

  override fun onFastForwardRowClick() {
    dialog.value = SettingsViewState.Dialog.FastForwardTime
  }

  override fun autoRewindAmountChang(seconds: Int) {
    mainScope.launch {
      autoRewindAmountStore.updateData { seconds }
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
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(autoSleepTimerEnabled = checked)
      }
    }
  }

  override fun setAutoSleepTimerStart(time: LocalTime) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(autoSleepStartTime = time)
      }
    }
  }

  override fun setAutoSleepTimerEnd(time: LocalTime) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(autoSleepEndTime = time)
      }
    }
  }

  override fun toggleAnalytics() {
    mainScope.launch {
      analyticsConsentStore.updateData { !it }
    }
  }

  override fun toggleOpenLastBookOnStartup() {
    mainScope.launch {
      openLastBookOnStartupStore.updateData { !it }
    }
  }

  override fun onAppVersionClick() {
    mainScope.launch {
      if (developerMenuUnlockedStore.data.first()) {
        return@launch
      }
      if (++appVersionTapCount >= 13) {
        developerMenuUnlockedStore.updateData { true }
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
