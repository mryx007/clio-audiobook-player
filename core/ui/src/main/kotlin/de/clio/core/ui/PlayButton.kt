package de.clio.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.clio.core.strings.R as StringsR

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
  val animatedContainerColor by animateColorAsState(
    targetValue = containerColor,
    animationSpec = tween(durationMillis = 300),
    label = "playButtonContainerColor",
  )
  val animatedContentColor by animateColorAsState(
    targetValue = contentColor,
    animationSpec = tween(durationMillis = 300),
    label = "playButtonContentColor",
  )

  FloatingActionButton(
    modifier = modifier
      .size(fabSize)
      .then(sharedElementModifier),
    onClick = onPlayClick,
    shape = CircleShape,
    containerColor = animatedContainerColor,
    contentColor = animatedContentColor,
  ) {
    Icon(
      modifier = Modifier.size(iconSize),
      painter = rememberPlayIconPainter(playing = playing),
      contentDescription = stringResource(
        id = if (playing) {
          StringsR.string.playback_action_pause
        } else {
          StringsR.string.playback_action_play
        },
      ),
    )
  }
}

@Composable
private fun rememberPlayIconPainter(playing: Boolean): Painter {
  return rememberAnimatedVectorPainter(
    animatedImageVector = AnimatedImageVector.animatedVectorResource(
      id = R.drawable.avd_pause_to_play,
    ),
    atEnd = !playing,
  )
}
