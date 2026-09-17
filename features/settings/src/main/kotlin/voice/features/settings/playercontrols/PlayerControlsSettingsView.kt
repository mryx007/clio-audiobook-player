package voice.features.settings.playercontrols

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.core.ui.icons.VoiceIcons
import voice.features.settings.views.AutoRewindAmountDialog
import voice.features.settings.views.AutoRewindRow
import voice.features.settings.views.SeekAmountDialog
import voice.features.settings.views.SeekTimeRow
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.core.strings.R as StringsR

@Composable
internal fun PlayerControlsSettings(
  viewState: PlayerControlsSettingsViewState,
  viewModel: PlayerControlsSettingsViewModel,
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
          Text(stringResource(StringsR.string.settings_player_controls_title))
        },
        navigationIcon = {
          IconButton(onClick = viewModel::close) {
            Icon(
              imageVector = VoiceIcons.ArrowBack,
              contentDescription = stringResource(StringsR.string.common_action_close),
            )
          }
        },
      )
    },
  ) { contentPadding ->
    LazyColumn(contentPadding = contentPadding) {
      item {
        SectionHeader(stringResource(StringsR.string.settings_player_controls_category_buttons))
      }

      item {
        ListItem(
          modifier = Modifier.clickable { viewModel.toggleLock() },
          leadingContent = {
            Icon(
              imageVector = VoiceIcons.Lock,
              contentDescription = null,
            )
          },
          supportingContent = {
            Text(stringResource(StringsR.string.settings_player_controls_lock_summary))
          },
          trailingContent = {
            Switch(
              checked = viewState.buttonVisibility.showLock,
              onCheckedChange = { viewModel.toggleLock() },
            )
          },
        ) {
          Text(stringResource(StringsR.string.settings_player_controls_lock))
        }
      }

      item {
        ListItem(
          modifier = Modifier.clickable { viewModel.toggleEqualizer() },
          leadingContent = {
            Icon(
              imageVector = VoiceIcons.Tune,
              contentDescription = null,
            )
          },
          supportingContent = {
            Text(stringResource(StringsR.string.settings_player_controls_equalizer_summary))
          },
          trailingContent = {
            Switch(
              checked = viewState.buttonVisibility.showEqualizer,
              onCheckedChange = { viewModel.toggleEqualizer() },
            )
          },
        ) {
          Text(stringResource(StringsR.string.settings_player_controls_equalizer))
        }
      }

      item {
        ListItem(
          modifier = Modifier.clickable { viewModel.toggleSleepTimer() },
          leadingContent = {
            Icon(
              imageVector = VoiceIcons.Bedtime,
              contentDescription = null,
            )
          },
          supportingContent = {
            Text(stringResource(StringsR.string.settings_player_controls_sleep_timer_summary))
          },
          trailingContent = {
            Switch(
              checked = viewState.buttonVisibility.showSleepTimer,
              onCheckedChange = { viewModel.toggleSleepTimer() },
            )
          },
        ) {
          Text(stringResource(StringsR.string.settings_player_controls_sleep_timer))
        }
      }

      item {
        ListItem(
          modifier = Modifier.clickable { viewModel.toggleBookmark() },
          leadingContent = {
            Icon(
              imageVector = VoiceIcons.CollectionsBookmark,
              contentDescription = null,
            )
          },
          supportingContent = {
            Text(stringResource(StringsR.string.settings_player_controls_bookmark_summary))
          },
          trailingContent = {
            Switch(
              checked = viewState.buttonVisibility.showBookmark,
              onCheckedChange = { viewModel.toggleBookmark() },
            )
          },
        ) {
          Text(stringResource(StringsR.string.settings_player_controls_bookmark))
        }
      }

      item {
        ListItem(
          modifier = Modifier.clickable { viewModel.toggleSpeed() },
          leadingContent = {
            Icon(
              imageVector = VoiceIcons.Speed,
              contentDescription = null,
            )
          },
          supportingContent = {
            Text(stringResource(StringsR.string.settings_player_controls_speed_summary))
          },
          trailingContent = {
            Switch(
              checked = viewState.buttonVisibility.showSpeed,
              onCheckedChange = { viewModel.toggleSpeed() },
            )
          },
        ) {
          Text(stringResource(StringsR.string.settings_player_controls_speed))
        }
      }

      item {
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
      }

      item {
        SectionHeader(stringResource(StringsR.string.settings_player_controls_category_seeking))
      }

      item {
        SeekTimeRow(
          title = stringResource(StringsR.string.playback_action_rewind),
          icon = VoiceIcons.FastRewind,
          seconds = viewState.rewindTimeInSeconds,
        ) {
          viewModel.onRewindRowClick()
        }
      }

      item {
        SeekTimeRow(
          title = stringResource(StringsR.string.playback_action_fast_forward),
          icon = VoiceIcons.FastForward,
          seconds = viewState.fastForwardTimeInSeconds,
        ) {
          viewModel.onFastForwardRowClick()
        }
      }

      item {
        AutoRewindRow(viewState.autoRewindInSeconds) {
          viewModel.onAutoRewindRowClick()
        }
      }

      item {
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
      }
    }
  }

  when (viewState.dialog) {
    PlayerControlsSettingsViewState.Dialog.RewindTime -> {
      SeekAmountDialog(
        title = stringResource(StringsR.string.playback_action_rewind),
        currentSeconds = viewState.rewindTimeInSeconds,
        onSecondsConfirm = {
          viewModel.rewindAmountChanged(it)
          viewModel.dismissDialog()
        },
        onDismiss = viewModel::dismissDialog,
      )
    }
    PlayerControlsSettingsViewState.Dialog.FastForwardTime -> {
      SeekAmountDialog(
        title = stringResource(StringsR.string.playback_action_fast_forward),
        currentSeconds = viewState.fastForwardTimeInSeconds,
        onSecondsConfirm = {
          viewModel.fastForwardAmountChanged(it)
          viewModel.dismissDialog()
        },
        onDismiss = viewModel::dismissDialog,
      )
    }
    PlayerControlsSettingsViewState.Dialog.AutoRewindAmount -> {
      AutoRewindAmountDialog(
        currentSeconds = viewState.autoRewindInSeconds,
        onSecondsConfirm = {
          viewModel.autoRewindAmountChanged(it)
          viewModel.dismissDialog()
        },
        onDismiss = viewModel::dismissDialog,
      )
    }
    null -> Unit
  }
}

@Composable
private fun SectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelLarge,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
  )
}

@ContributesTo(AppScope::class)
interface PlayerControlsSettingsGraph {
  val playerControlsSettingsViewModel: PlayerControlsSettingsViewModel
}

@ContributesTo(AppScope::class)
interface PlayerControlsSettingsProvider {

  @Provides
  @IntoSet
  fun playerControlsSettingsNavEntryProvider(): NavEntryProvider<*> =
    NavEntryProvider<Destination.PlayerControlsSettings> { key ->
      NavEntry(key) {
        val viewModel = retain<PlayerControlsSettingsViewModel> {
          rootGraphAs<PlayerControlsSettingsGraph>().playerControlsSettingsViewModel
        }
        val viewState = viewModel.viewState() ?: return@NavEntry
        PlayerControlsSettings(viewState, viewModel)
      }
    }
}
