package de.clio.features.settings.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import de.clio.core.common.rootGraphAs
import de.clio.core.ui.ClioTheme
import de.clio.core.ui.icons.ClioIcons
import de.clio.features.settings.SettingsListener
import de.clio.features.settings.SettingsViewEffect
import de.clio.features.settings.SettingsViewModel
import de.clio.features.settings.SettingsViewState
import de.clio.features.settings.views.sleeptimer.AutoSleepTimerCard
import de.clio.navigation.Destination
import de.clio.navigation.NavEntryProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import de.clio.core.strings.R as StringsR

@Composable
@Preview
private fun SettingsPreview() {
  ClioTheme {
    Settings(
      SettingsViewState.preview(),
      SettingsListener.noop(),
    )
  }
}

@Composable
private fun Settings(
  viewState: SettingsViewState,
  listener: SettingsListener,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    },
    topBar = {
      TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
          Text(stringResource(StringsR.string.settings_action_open))
        },
        navigationIcon = {
          IconButton(
            onClick = {
              listener.close()
            },
          ) {
            Icon(
              imageVector = ClioIcons.ArrowBack,
              contentDescription = stringResource(StringsR.string.common_action_close),
            )
          }
        },
      )
    },
    bottomBar = {
      Spacer(
        Modifier
          .fillMaxWidth()
          .windowInsetsBottomHeight(WindowInsets.navigationBars)
          .background(MaterialTheme.colorScheme.surface),
      )
    },
  ) { contentPadding ->
    LazyColumn(contentPadding = contentPadding) {
      if (viewState.showDeveloperMenu && !viewState.kioskMode) {
        item {
          DeveloperMenuItem(
            onClick = listener::openDeveloperMenu,
          )
        }
      }
      item {
        ListItem(
          modifier = Modifier.clickable { listener.openFolderPicker() },
          leadingContent = {
            Icon(
              imageVector = ClioIcons.Book,
              contentDescription = stringResource(StringsR.string.library_folders_title),
            )
          },
          supportingContent = {
            Text(stringResource(StringsR.string.settings_library_folders_summary))
          },
        ) {
          Text(stringResource(StringsR.string.library_folders_title))
        }
      }
      item {
        ThemeModeRow(viewState.themeMode, listener::onThemeModeRowClick)
      }
      item {
        PlaybackBackgroundStyleRow(viewState.playbackBackgroundStyle, listener::onPlaybackBackgroundStyleRowClick)
      }
      item {
        BackButtonBehaviorRow(viewState.backButtonBehavior, listener::onBackButtonBehaviorRowClick)
      }
      item {
        ListItem(
          modifier = Modifier.clickable { listener.openPlayerControlsSettings() },
          leadingContent = {
            Icon(
              imageVector = ClioIcons.Tune,
              contentDescription = stringResource(StringsR.string.settings_player_controls_title),
            )
          },
          supportingContent = {
            Text(stringResource(StringsR.string.settings_player_controls_summary))
          },
        ) {
          Text(stringResource(StringsR.string.settings_player_controls_title))
        }
      }
      if (viewState.showAnalyticSetting && !viewState.kioskMode) {
        item {
          AnalyticsRow(analyticsEnabled = viewState.analyticsEnabled, toggle = listener::toggleAnalytics)
        }
      }
      item {
        ListItem(
          modifier = Modifier.clickable { listener.toggleGrid() },
          leadingContent = {
            val icon = if (viewState.useGrid) {
              ClioIcons.GridView
            } else {
              ClioIcons.ViewList
            }
            Icon(
              imageVector = icon,
              contentDescription = stringResource(StringsR.string.settings_library_use_grid_title),
            )
          },
          trailingContent = {
            Switch(
              checked = viewState.useGrid,
              onCheckedChange = {
                listener.toggleGrid()
              },
            )
          },
        ) {
          Text(stringResource(StringsR.string.settings_library_use_grid_title))
        }
      }

      item {
        OpenLastBookOnStartupRow(
          enabled = viewState.openLastBookOnStartup,
          toggle = listener::toggleOpenLastBookOnStartup,
        )
      }

      item {
        AutoSleepTimerCard(viewState.autoSleepTimer, listener)
      }

      if (viewState.showSupportDevelopment) {
        item {
          ListItem(
            modifier = Modifier.clickable { listener.openSupportVoice() },
            leadingContent = {
              Icon(
                imageVector = ClioIcons.Favorite,
                contentDescription = stringResource(StringsR.string.settings_support_support_voice_title),
                tint = MaterialTheme.colorScheme.primary,
              )
            },
            supportingContent = {
              Text(stringResource(StringsR.string.settings_support_support_voice_summary))
            },
          ) {
            Text(stringResource(StringsR.string.settings_support_support_voice_title))
          }
        }
      }

      item {
        ListItem(
          modifier = Modifier.clickable { listener.openStatistics() },
          leadingContent = {
            Icon(
              imageVector = ClioIcons.Analytics,
              contentDescription = stringResource(StringsR.string.settings_statistics_title),
              tint = MaterialTheme.colorScheme.primary,
            )
          },
          supportingContent = {
            Text(stringResource(StringsR.string.settings_statistics_summary))
          },
        ) {
          Text(stringResource(StringsR.string.settings_statistics_title))
        }
      }

      item {
        ListItem(
          modifier = Modifier.clickable { listener.openBugReport() },
          leadingContent = {
            Icon(
              imageVector = ClioIcons.BugReport,
              contentDescription = stringResource(StringsR.string.settings_support_report_issue_title),
            )
          },
        ) {
          Text(stringResource(StringsR.string.settings_support_report_issue_title))
        }
      }
      item {
        AppVersion(
          appVersion = viewState.appVersion,
          onClick = listener::onAppVersionClick,
        )
      }
      if (viewState.kioskMode) {
        if (viewState.showAnalyticSetting) {
          item {
            AnalyticsRow(analyticsEnabled = viewState.analyticsEnabled, toggle = listener::toggleAnalytics)
          }
        }
        if (viewState.showDeveloperMenu) {
          item {
            DeveloperMenuItem(
              onClick = listener::openDeveloperMenu,
            )
          }
        }
      }
    }
    Dialog(viewState, listener)
  }
}

@Composable
private fun OpenLastBookOnStartupRow(
  enabled: Boolean,
  toggle: () -> Unit,
) {
  ListItem(
    modifier = Modifier.clickable { toggle() },
    leadingContent = {
      Icon(
        imageVector = ClioIcons.Undo, // Or find a better icon, history/undo seems okay for "last"
        contentDescription = null,
      )
    },
    supportingContent = {
      Text(text = stringResource(StringsR.string.settings_playback_open_last_book_on_startup_summary))
    },
    trailingContent = {
      Switch(
        checked = enabled,
        onCheckedChange = { toggle() },
      )
    },
  ) {
    Text(text = stringResource(StringsR.string.settings_playback_open_last_book_on_startup_title))
  }
}

@Composable
private fun AnalyticsRow(
  analyticsEnabled: Boolean,
  toggle: () -> Unit,
) {
  ListItem(
    modifier = Modifier.clickable { toggle() },
    leadingContent = {
      Icon(
        imageVector = ClioIcons.Analytics,
        contentDescription = null,
      )
    },
    supportingContent = {
      Text(text = stringResource(StringsR.string.settings_analytics_consent_description))
    },
    trailingContent = {
      Switch(
        checked = analyticsEnabled,
        onCheckedChange = { toggle() },
      )
    },
  ) {
    Text(text = stringResource(StringsR.string.settings_analytics_consent_title))
  }
}

@ContributesTo(AppScope::class)
interface SettingsGraph {
  val settingsViewModel: SettingsViewModel
}

@ContributesTo(AppScope::class)
interface SettingsProvider {

  @Provides
  @IntoSet
  fun settingsNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Settings> { key ->
    NavEntry(key) {
      Settings()
    }
  }
}

@Composable
fun Settings() {
  val viewModel = retain<SettingsViewModel> { rootGraphAs<SettingsGraph>().settingsViewModel }
  val snackbarHostState = remember { SnackbarHostState() }
  val viewState = viewModel.viewState()
  val currentDeveloperMenuUnlockedMessage = rememberUpdatedState("Developer Menu unlocked")
  LaunchedEffect(viewModel) {
    viewModel.viewEffects.collect { viewEffect ->
      when (viewEffect) {
        SettingsViewEffect.DeveloperMenuUnlocked -> {
          snackbarHostState.showSnackbar(currentDeveloperMenuUnlockedMessage.value)
        }
      }
    }
  }
  Settings(viewState, viewModel, snackbarHostState)
}

@Composable
private fun Dialog(
  viewState: SettingsViewState,
  listener: SettingsListener,
) {
  val dialog = viewState.dialog ?: return
  when (dialog) {
    SettingsViewState.Dialog.AutoRewindAmount -> {
      AutoRewindAmountDialog(
        currentSeconds = viewState.autoRewindInSeconds,
        onSecondsConfirm = listener::autoRewindAmountChang,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.RewindTime -> {
      SeekAmountDialog(
        title = stringResource(StringsR.string.playback_action_rewind),
        currentSeconds = viewState.rewindTimeInSeconds,
        onSecondsConfirm = listener::rewindAmountChanged,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.FastForwardTime -> {
      SeekAmountDialog(
        title = stringResource(StringsR.string.playback_action_fast_forward),
        currentSeconds = viewState.fastForwardTimeInSeconds,
        onSecondsConfirm = listener::fastForwardAmountChanged,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.Theme -> {
      ThemeModeDialog(
        selectedThemeMode = viewState.themeMode,
        customThemeColor = viewState.customThemeColor,
        onThemeModeSelect = listener::setThemeMode,
        onCustomThemeSelect = listener::setCustomTheme,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.BackgroundStyle -> {
      PlaybackBackgroundStyleDialog(
        selectedStyle = viewState.playbackBackgroundStyle,
        onStyleSelect = listener::setPlaybackBackgroundStyle,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.BackButtonBehavior -> {
      BackButtonBehaviorDialog(
        selectedBehavior = viewState.backButtonBehavior,
        onBehaviorSelect = listener::setBackButtonBehavior,
        onDismiss = listener::dismissDialog,
      )
    }
  }
}
