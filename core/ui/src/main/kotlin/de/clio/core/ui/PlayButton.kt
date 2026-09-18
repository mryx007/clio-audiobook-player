package de.clio.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import de.clio.core.strings.R as StringsR
import de.clio.core.ui.icons.ClioIcons

@Composable
fun PlayButton(
  playing: Boolean,
  fabSize: Dp,
  iconSize: Dp,
  onPlayClick: () -> Unit,
  modifier: Modifier = Modifier,
  sharedElementModifier: Modifier = Modifier,
  containerColor: Color = FloatingActionButtonDefaults.containerColor,
  contentColor: Color = contentColorFor(containerColor),
) {
  FloatingActionButton(
    modifier = modifier
      .size(fabSize)
      .then(sharedElementModifier),
    onClick = onPlayClick,
    shape = CircleShape,
    containerColor = containerColor,
    contentColor = contentColor,
  ) {
    AnimatedContent(
      targetState = playing,
      transitionSpec = {
        fadeIn(animationSpec = tween(durationMillis = 150)) togetherWith
          fadeOut(animationSpec = tween(durationMillis = 150))
      },
      label = "playButtonIconTransition",
    ) { isPlaying ->
      Icon(
        modifier = Modifier.size(iconSize),
        imageVector = if (isPlaying) ClioIcons.Pause else ClioIcons.PlayArrow,
        contentDescription = stringResource(
          id = if (isPlaying) {
            StringsR.string.playback_action_pause
          } else {
            StringsR.string.playback_action_play
          },
        ),
      )
    }
  }
}
