package voice.features.settings

import androidx.datastore.core.DataStore
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import voice.core.common.AppInfoProvider
import voice.core.common.DispatcherProvider
import voice.core.data.GridMode
import voice.core.data.PlaybackBackgroundStyle
import voice.core.data.ThemeColor
import voice.core.data.ThemeMode
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.featureflag.MemoryFeatureFlag
import voice.core.ui.GridCount
import voice.navigation.Destination
import voice.navigation.Navigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class SettingsViewModelTest {

  private val scope = TestScope()
  private val themeModeStore = MemoryDataStore(ThemeMode.FollowSystem)
  private val themeColorStore = MemoryDataStore(ThemeColor())
  private val autoRewindAmountStore = MemoryDataStore(10)
  private val rewindTimeStore = MemoryDataStore(20)
  private val fastForwardTimeStore = MemoryDataStore(30)
  private val gridModeStore = MemoryDataStore(GridMode.GRID)
  private val sleepTimerPreferenceStore = MemoryDataStore(SleepTimerPreference.Default)
  private val analyticsConsentStore = MemoryDataStore(false)
  private val developerMenuUnlockedStore = MemoryDataStore(false)
  private val openLastBookOnStartupStore = MemoryDataStore(false)
  private val playbackBackgroundStyleStore = MemoryDataStore(PlaybackBackgroundStyle.Solid)
  private val navigator = mockk<Navigator> {
    every { goTo(any()) } just Runs
  }
  private val appInfoProvider = mockk<AppInfoProvider> {
    every { versionName } returns "1.2.3"
    every { analyticsIncluded } returns true
    every { supportDevelopmentIncluded } returns true
    every { installTime } returns Instant.parse("2026-06-01T00:00:00Z")
  }
  private val gridCount = mockk<GridCount> {
    every { useGridAsDefault() } returns true
  }
  private val kioskModeFeatureFlag = MemoryFeatureFlag(false)

  private val viewModel = SettingsViewModel(
    themeModeStore = themeModeStore,
    themeColorStore = themeColorStore,
    autoRewindAmountStore = autoRewindAmountStore,
    rewindTimeStore = rewindTimeStore,
    fastForwardTimeStore = fastForwardTimeStore,
    navigator = navigator,
    appInfoProvider = appInfoProvider,
    gridModeStore = gridModeStore,
    sleepTimerPreferenceStore = sleepTimerPreferenceStore,
    analyticsConsentStore = analyticsConsentStore,
    gridCount = gridCount,
    kioskModeFeatureFlag = kioskModeFeatureFlag,
    developerMenuUnlockedStore = developerMenuUnlockedStore,
    openLastBookOnStartupStore = openLastBookOnStartupStore,
    playbackBackgroundStyleStore = playbackBackgroundStyleStore,
    dispatcherProvider = DispatcherProvider(scope.coroutineContext, scope.coroutineContext, scope.coroutineContext),
  )

  @Test
  fun `view state defaults to follow system and voice blue`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      awaitItem().let {
        assertEquals(expected = ThemeMode.FollowSystem, actual = it.themeMode)
      }
    }
  }

  @Test
  fun `theme mode changes update view state`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = ThemeMode.FollowSystem, actual = awaitItem().themeMode)

      viewModel.setThemeMode(ThemeMode.Dark)
      assertEquals(expected = ThemeMode.Dark, actual = awaitItem().themeMode)

      viewModel.setThemeMode(ThemeMode.Light)
      assertEquals(expected = ThemeMode.Light, actual = awaitItem().themeMode)

      viewModel.setThemeMode(ThemeMode.Amoled)
      assertEquals(expected = ThemeMode.Amoled, actual = awaitItem().themeMode)

      viewModel.setThemeMode(ThemeMode.CatppuccinMocha)
      assertEquals(expected = ThemeMode.CatppuccinMocha, actual = awaitItem().themeMode)

      viewModel.setThemeMode(ThemeMode.FollowSystem)
      assertEquals(expected = ThemeMode.FollowSystem, actual = awaitItem().themeMode)
    }
  }

  @Test
  fun `theme presets store correctly`() = scope.runTest {
    viewModel.setThemeMode(ThemeMode.CatppuccinMocha)
    testScheduler.advanceUntilIdle()

    assertEquals(expected = ThemeMode.CatppuccinMocha, actual = themeModeStore.data.first())
  }

  @Test
  fun `setCustomThemeHex updates themeColorStore and sets theme mode to custom`() = scope.runTest {
    viewModel.setCustomThemeHex("#123456")
    testScheduler.advanceUntilIdle()

    assertEquals(expected = "#123456", actual = themeColorStore.data.first().hex)
    assertEquals(expected = ThemeMode.Custom, actual = themeModeStore.data.first())
  }

  @Test
  fun `developer menu is hidden until app version tapped 13 times`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = false, actual = awaitItem().showDeveloperMenu)

      repeat(13) {
        viewModel.onAppVersionClick()
      }

      assertEquals(expected = true, actual = awaitItem().showDeveloperMenu)
    }
  }

  @Test
  fun `developer menu unlock emits snackbar effect`() = scope.runTest {
    viewModel.viewEffects.test {
      repeat(13) {
        viewModel.onAppVersionClick()
      }

      assertIs<SettingsViewEffect.DeveloperMenuUnlocked>(awaitItem())
    }
  }

  @Test
  fun `openDeveloperMenu navigates to developer settings`() {
    viewModel.openDeveloperMenu()

    verify(exactly = 1) {
      navigator.goTo(Destination.DeveloperSettings)
    }
  }

  @Test
  fun `openSupportVoice navigates to support screen`() {
    viewModel.openSupportVoice()

    verify(exactly = 1) {
      navigator.goTo(Destination.SupportVoice)
    }
  }

  @Test
  fun `openFolderPicker navigates to folder picker`() {
    viewModel.openFolderPicker()

    verify(exactly = 1) {
      navigator.goTo(Destination.FolderPicker)
    }
  }

  @Test
  fun `view state shows support development when included`() = scope.runTest {
    every { appInfoProvider.supportDevelopmentIncluded } returns true

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = true, actual = awaitItem().showSupportDevelopment)
    }
  }

  @Test
  fun `view state hides support development when not included`() = scope.runTest {
    every { appInfoProvider.supportDevelopmentIncluded } returns false

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = false, actual = awaitItem().showSupportDevelopment)
    }
  }

  @Test
  fun `view state exposes kiosk mode`() = scope.runTest {
    kioskModeFeatureFlag.value = true

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      awaitItem().let {
        assertEquals(expected = true, actual = it.kioskMode)
      }
    }
  }

  @Test
  fun `rewind amount changes and row click`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = 20, actual = awaitItem().rewindTimeInSeconds)

      viewModel.rewindAmountChanged(15)
      assertEquals(expected = 15, actual = awaitItem().rewindTimeInSeconds)

      viewModel.onRewindRowClick()
      assertEquals(expected = SettingsViewState.Dialog.RewindTime, actual = awaitItem().dialog)
    }
  }

  @Test
  fun `fast forward amount changes and row click`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = 30, actual = awaitItem().fastForwardTimeInSeconds)

      viewModel.fastForwardAmountChanged(45)
      assertEquals(expected = 45, actual = awaitItem().fastForwardTimeInSeconds)

      viewModel.onFastForwardRowClick()
      assertEquals(expected = SettingsViewState.Dialog.FastForwardTime, actual = awaitItem().dialog)
    }
  }
}

private class MemoryDataStore<T>(initial: T) : DataStore<T> {

  private val value = MutableStateFlow(initial)

  override val data: Flow<T> get() = value

  override suspend fun updateData(transform: suspend (t: T) -> T): T {
    return value.updateAndGet { transform(it) }
  }
}
