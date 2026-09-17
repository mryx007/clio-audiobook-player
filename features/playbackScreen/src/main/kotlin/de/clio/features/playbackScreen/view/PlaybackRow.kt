package de.clio.features.playbackScreen.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.clio.core.strings.R
import de.clio.core.ui.PlayButton
import de.clio.core.ui.icons.ClioIcons

@Composable
internal fun PlaybackRow(
  playing: Boolean,
  isLocked: Boolean,
  isCustomBackground: Boolean,
  rewindTimeInSeconds: Int,
  fastForwardTimeInSeconds: Int,
  onPlayClick: () -> Unit,
  onRewindClick: () -> Unit,
  onFastForwardClick: () -> Unit,
  onSkipToNext: () -> Unit,
  onSkipToPrevious: () -> Unit,
) {
  val iconColor = if (isCustomBackground) Color.White else MaterialTheme.colorScheme.onSurface
  val isDarkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f
  val playButtonContainerColor = if (isCustomBackground || isDarkSurface) Color.White else Color(0xFF1C1B1F)
  val playButtonContentColor = if (isCustomBackground || isDarkSurface) Color(0xFF1C1B1F) else Color.White

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {
    // Previous Chapter
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      PlaybackIconButton(
        imageVector = ClioIcons.SkipPrevious,
        contentDescription = stringResource(id = R.string.playback_chapter_previous),
        enabled = !isLocked,
        tint = iconColor,
        onClick = onSkipToPrevious,
        size = 40.dp,
      )
      Spacer(modifier = Modifier.height(14.dp))
    }

    // Rewind
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      PlaybackIconButton(
        imageVector = ClioIcons.FastRewind,
        contentDescription = stringResource(id = R.string.playback_action_rewind),
        enabled = !isLocked,
        tint = iconColor,
        onClick = onRewindClick,
        size = 48.dp,
      )
      Text(
        text = "${rewindTimeInSeconds}s",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = iconColor.copy(alpha = 0.85f),
      )
    }

    // Play/Pause
    PlayButton(
      playing = playing,
      fabSize = 80.dp,
      iconSize = 36.dp,
      containerColor = playButtonContainerColor,
      contentColor = playButtonContentColor,
      onPlayClick = onPlayClick,
    )

    // Fast Forward
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      PlaybackIconButton(
        imageVector = ClioIcons.FastForward,
        contentDescription = stringResource(id = R.string.playback_action_fast_forward),
        enabled = !isLocked,
        tint = iconColor,
        onClick = onFastForwardClick,
        size = 48.dp,
      )
      Text(
        text = "${fastForwardTimeInSeconds}s",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = iconColor.copy(alpha = 0.85f),
      )
    }

    // Next Chapter
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      PlaybackIconButton(
        imageVector = ClioIcons.SkipNext,
        contentDescription = stringResource(id = R.string.playback_chapter_next),
        enabled = !isLocked,
        tint = iconColor,
        onClick = onSkipToNext,
        size = 40.dp,
      )
      Spacer(modifier = Modifier.height(14.dp))
    }
  }
}
