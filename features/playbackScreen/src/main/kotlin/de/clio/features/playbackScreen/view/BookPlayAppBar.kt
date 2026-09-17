package de.clio.features.playbackScreen.view

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.strings.R
import de.clio.core.ui.icons.ClioIcons
import de.clio.features.playbackScreen.BookPlayViewState

@Composable
internal fun BookPlayAppBar(
  viewState: BookPlayViewState,
  onSleepTimerClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onBookmarkLongClick: () -> Unit,
  onSpeedChangeClick: () -> Unit,
  onSkipSilenceClick: () -> Unit,
  onVolumeBoostClick: () -> Unit,
  onEqualizerClick: () -> Unit,
  onLockClick: () -> Unit,
  onCloseClick: () -> Unit,
  useLandscapeLayout: Boolean,
) {
  val isCustomBackground = viewState.backgroundStyle != PlaybackBackgroundStyle.Solid
  val contentColor = if (isCustomBackground) Color.White else MaterialTheme.colorScheme.onSurface

  val appBarActions: @Composable RowScope.() -> Unit = {
    if (viewState.playerButtonVisibility.showLock) {
      val lockTint = if (viewState.isLocked) Color(0xFFE57373) else contentColor
      IconButton(onClick = onLockClick) {
        Icon(
          imageVector = if (viewState.isLocked) ClioIcons.Lock else ClioIcons.LockOpen,
          tint = lockTint,
          contentDescription = stringResource(
            id = if (viewState.isLocked) R.string.playback_action_unlock else R.string.playback_action_lock,
          ),
        )
      }
    }
    if (viewState.playerButtonVisibility.showEqualizer) {
      IconButton(onClick = onEqualizerClick) {
        Icon(
          imageVector = ClioIcons.Tune,
          tint = contentColor,
          contentDescription = stringResource(id = R.string.playback_equalizer_title),
        )
      }
    }
    if (viewState.playerButtonVisibility.showSleepTimer) {
      IconButton(onClick = onSleepTimerClick) {
        val sleepTimerIcon = if (viewState.sleepTimerState is BookPlayViewState.SleepTimerViewState.Disabled) {
          ClioIcons.Bedtime
        } else {
          ClioIcons.BedtimeOff
        }
        Icon(
          imageVector = sleepTimerIcon,
          tint = contentColor,
          contentDescription = stringResource(id = R.string.sleep_timer_action_open),
        )
      }
    }
    if (viewState.playerButtonVisibility.showBookmark) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .combinedClickable(
            onClick = onBookmarkClick,
            onLongClick = onBookmarkLongClick,
            indication = ripple(bounded = false, radius = 20.dp),
            interactionSource = remember { MutableInteractionSource() },
          ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = ClioIcons.CollectionsBookmark,
          tint = contentColor,
          contentDescription = stringResource(id = R.string.bookmark_title),
        )
      }
    }
    if (viewState.playerButtonVisibility.showSpeed) {
      IconButton(onClick = onSpeedChangeClick) {
        Icon(
          imageVector = ClioIcons.Speed,
          tint = contentColor,
          contentDescription = stringResource(id = R.string.playback_speed_title),
        )
      }
    }
    OverflowMenu(
      skipSilence = viewState.skipSilence,
      onSkipSilenceClick = onSkipSilenceClick,
      onVolumeBoostClick = onVolumeBoostClick,
      tint = contentColor,
    )
  }

  val appBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = Color.Transparent,
    scrolledContainerColor = Color.Transparent,
    navigationIconContentColor = contentColor,
    titleContentColor = contentColor,
    actionIconContentColor = contentColor,
  )

  if (useLandscapeLayout) {
    TopAppBar(
      colors = appBarColors,
      navigationIcon = {
        CloseIcon(onCloseClick, tint = contentColor)
      },
      actions = appBarActions,
      title = {
        AppBarTitle(viewState.title, maxLines = 1)
      },
    )
  } else {
    LargeTopAppBar(
      colors = appBarColors,
      navigationIcon = {
        CloseIcon(onCloseClick, tint = contentColor)
      },
      actions = appBarActions,
      title = {
        AppBarTitle(viewState.title)
      },
    )
  }
}
