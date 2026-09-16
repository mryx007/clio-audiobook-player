package voice.features.playbackScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun PlaybackIconButton(
  imageVector: ImageVector,
  contentDescription: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  tint: Color = MaterialTheme.colorScheme.onSurface,
  size: Dp = 48.dp,
) {
  Icon(
    imageVector = imageVector,
    contentDescription = contentDescription,
    tint = tint,
    modifier = modifier
      .size(size)
      .graphicsLayer {
        alpha = if (enabled) 1f else 0.38f
      }
      .clickable(
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false),
        onClick = onClick,
      )
  )
}
