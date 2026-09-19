package de.clio.features.settings

import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import de.clio.core.common.AppInfoProvider
import de.clio.core.common.DispatcherProvider
import de.clio.core.data.BackButtonBehavior
import de.clio.core.data.EndOfBookBehavior
import de.clio.core.data.GridMode
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.data.ThemeColor
import de.clio.core.data.ThemeMode
import de.clio.core.data.repo.UserSettingsRepository
import de.clio.core.data.sleeptimer.SleepTimerPreference
import de.clio.core.featureflag.MemoryFeatureFlag
import de.clio.core.ui.GridCount
import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class SettingsViewModelTest {

  private val scope = TestScope()
  private val userSettingsRepository = MemoryUserSettingsRepository()
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
    userSettingsRepository = userSettingsRepository,
    navigator = navigator,
    appInfoProvider = appInfoProvider,
    gridCount = gridCount,
    kioskModeFeatureFlag = kioskModeFeatureFlag,
    dispatcherProvider = DispatcherProvider(scope.coroutineContext, scope.coroutineContext, scope.coroutineContext),
  )

  private fun TestScope.viewStateFlow(): Flow<SettingsViewState> = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
    viewModel.viewState()
  }.filterNotNull()

  @Test
  fun `view state defaults to follow system and voice blue`() = scope.runTest {
    viewStateFlow().test {
      awaitItem().let {
        assertEquals(expected = ThemeMode.FollowSystem, actual = it.themeMode)
      }
    }
  }

  @Test
  fun `theme mode changes update view state`() = scope.runTest {
    viewStateFlow().test {
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

    assertEquals(expected = ThemeMode.CatppuccinMocha, actual = userSettingsRepository.themeMode.first())
  }

  @Test
  fun `setCustomThemeHex updates themeColorStore and sets theme mode to custom`() = scope.runTest {
    viewModel.setCustomThemeHex("#123456")
    testScheduler.advanceUntilIdle()

    assertEquals(expected = "#123456", actual = userSettingsRepository.themeColor.first().hex)
    assertEquals(expected = ThemeMode.Custom, actual = userSettingsRepository.themeMode.first())
  }

  @Test
  fun `developer menu is hidden until app version tapped 13 times`() = scope.runTest {
    viewStateFlow().test {
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

    viewStateFlow().test {
      assertEquals(expected = true, actual = awaitItem().showSupportDevelopment)
    }
  }

  @Test
  fun `view state hides support development when not included`() = scope.runTest {
    every { appInfoProvider.supportDevelopmentIncluded } returns false

    viewStateFlow().test {
      assertEquals(expected = false, actual = awaitItem().showSupportDevelopment)
    }
  }

  @Test
  fun `view state exposes kiosk mode`() = scope.runTest {
    kioskModeFeatureFlag.value = true

    viewStateFlow().test {
      awaitItem().let {
        assertEquals(expected = true, actual = it.kioskMode)
      }
    }
  }

  @Test
  fun `rewind amount changes and row click`() = scope.runTest {
    viewStateFlow().test {
      assertEquals(expected = 20, actual = awaitItem().rewindTimeInSeconds)

      viewModel.rewindAmountChanged(15)
      assertEquals(expected = 15, actual = awaitItem().rewindTimeInSeconds)

      viewModel.onRewindRowClick()
      assertEquals(expected = SettingsViewState.Dialog.RewindTime, actual = awaitItem().dialog)
    }
  }

  @Test
  fun `fast forward amount changes and row click`() = scope.runTest {
    viewStateFlow().test {
      assertEquals(expected = 30, actual = awaitItem().fastForwardTimeInSeconds)

      viewModel.fastForwardAmountChanged(45)
      assertEquals(expected = 45, actual = awaitItem().fastForwardTimeInSeconds)

      viewModel.onFastForwardRowClick()
      assertEquals(expected = SettingsViewState.Dialog.FastForwardTime, actual = awaitItem().dialog)
    }
  }

  @Test
  fun `back button behavior updates and row click opens dialog`() = scope.runTest {
    viewStateFlow().test {
      assertEquals(expected = BackButtonBehavior.BookOverview, actual = awaitItem().backButtonBehavior)

      viewModel.setBackButtonBehavior(BackButtonBehavior.MinimizeApp)
      assertEquals(expected = BackButtonBehavior.MinimizeApp, actual = awaitItem().backButtonBehavior)

      viewModel.onBackButtonBehaviorRowClick()
      assertEquals(expected = SettingsViewState.Dialog.BackButtonBehavior, actual = awaitItem().dialog)
    }
  }

  @Test
  fun `end of book behavior updates and row click opens dialog`() = scope.runTest {
    viewStateFlow().test {
      assertEquals(expected = EndOfBookBehavior.DoNothing, actual = awaitItem().endOfBookBehavior)

      viewModel.setEndOfBookBehavior(EndOfBookBehavior.ContinueQueue)
      assertEquals(expected = EndOfBookBehavior.ContinueQueue, actual = awaitItem().endOfBookBehavior)

      viewModel.onEndOfBookBehaviorRowClick()
      assertEquals(expected = SettingsViewState.Dialog.EndOfBookBehavior, actual = awaitItem().dialog)
    }
  }
}

private class MemoryUserSettingsRepository : UserSettingsRepository {
  override val themeMode = MutableStateFlow(ThemeMode.FollowSystem)
  override val themeColor = MutableStateFlow(ThemeColor())
  override val autoRewindAmount = MutableStateFlow(10)
  override val rewindTime = MutableStateFlow(20)
  override val fastForwardTime = MutableStateFlow(30)
  override val gridMode = MutableStateFlow(GridMode.GRID)
  override val sleepTimerPreference = MutableStateFlow(SleepTimerPreference.Default)
  override val analyticsConsent = MutableStateFlow(false)
  override val openLastBookOnStartup = MutableStateFlow(false)
  override val playbackBackgroundStyle = MutableStateFlow(PlaybackBackgroundStyle.Solid)
  override val backButtonBehavior = MutableStateFlow(BackButtonBehavior.BookOverview)
  override val endOfBookBehavior = MutableStateFlow(EndOfBookBehavior.DoNothing)
  override val developerMenuUnlocked = MutableStateFlow(false)

  override suspend fun setThemeMode(themeMode: ThemeMode) {
    this.themeMode.value = themeMode
  }

  override suspend fun setThemeColor(themeColor: ThemeColor) {
    this.themeColor.value = themeColor
  }

  override suspend fun setAutoRewindAmount(seconds: Int) {
    this.autoRewindAmount.value = seconds
  }

  override suspend fun setRewindTime(seconds: Int) {
    this.rewindTime.value = seconds
  }

  override suspend fun setFastForwardTime(seconds: Int) {
    this.fastForwardTime.value = seconds
  }

  override suspend fun setGridMode(gridMode: GridMode) {
    this.gridMode.value = gridMode
  }

  override suspend fun setSleepTimerPreference(preference: SleepTimerPreference) {
    this.sleepTimerPreference.value = preference
  }

  override suspend fun setAnalyticsConsent(enabled: Boolean) {
    this.analyticsConsent.value = enabled
  }

  override suspend fun setOpenLastBookOnStartup(enabled: Boolean) {
    this.openLastBookOnStartup.value = enabled
  }

  override suspend fun setPlaybackBackgroundStyle(style: PlaybackBackgroundStyle) {
    this.playbackBackgroundStyle.value = style
  }

  override suspend fun setBackButtonBehavior(behavior: BackButtonBehavior) {
    this.backButtonBehavior.value = behavior
  }

  override suspend fun setEndOfBookBehavior(behavior: EndOfBookBehavior) {
    this.endOfBookBehavior.value = behavior
  }

  override suspend fun setDeveloperMenuUnlocked(unlocked: Boolean) {
    this.developerMenuUnlocked.value = unlocked
  }
}
