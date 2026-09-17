package de.clio.features.playbackScreen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.clio.core.ui.formatTime
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SliderRow(
  duration: Duration,
  playedTime: Duration,
  isCustomBackground: Boolean,
  enabled: Boolean,
  onSeek: (Duration) -> Unit,
) {
  val surfaceColor = MaterialTheme.colorScheme.surface
  val isDark = isCustomBackground || (0.299f * surfaceColor.red + 0.587f * surfaceColor.green + 0.114f * surfaceColor.blue) < 0.5f

  val labelColor = (if (isDark) Color.White else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.85f)
  val trackBackgroundColor = if (isDark) {
    Color.White.copy(alpha = 0.22f)
  } else {
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
  }
  val activeProgressColor = if (isDark) {
    Color.White
  } else {
    MaterialTheme.colorScheme.primary
  }

  var showTotalDuration by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    var localValue by remember { mutableFloatStateOf(0F) }
    val interactionSource = remember { MutableInteractionSource() }
    val dragging by interactionSource.collectIsDraggedAsState()

    val currentDisplayTime = if (dragging) {
      duration * localValue.toDouble()
    } else {
      playedTime
    }

    val sliderValue = if (dragging) {
      localValue
    } else {
      (playedTime / duration).toFloat()
        .coerceIn(0F, 1F)
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(22.dp),
      contentAlignment = Alignment.Center,
    ) {
      // Custom full-width track background
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(10.dp)
          .background(
            color = trackBackgroundColor,
            shape = RoundedCornerShape(5.dp),
          ),
      )

      // Custom progress track
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(10.dp),
        contentAlignment = Alignment.CenterStart,
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(sliderValue)
            .fillMaxHeight()
            .background(
              color = activeProgressColor,
              shape = RoundedCornerShape(
                topStart = 5.dp,
                bottomStart = 5.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp,
              ),
            ),
        )
      }

      // The actual slider on top, with transparent tracks and NO thumb
      Slider(
        modifier = Modifier.fillMaxWidth(),
        interactionSource = interactionSource,
        enabled = enabled,
        value = sliderValue,
        onValueChange = {
          localValue = it
        },
        onValueChangeFinished = {
          onSeek(duration * localValue.toDouble())
        },
        colors = SliderDefaults.colors(
          activeTrackColor = Color.Transparent,
          inactiveTrackColor = Color.Transparent,
          disabledActiveTrackColor = Color.Transparent,
          disabledInactiveTrackColor = Color.Transparent,
          thumbColor = Color.Transparent, // Make default thumb transparent
        ),
        thumb = {}, // Remove the thumb entirely
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      // Left: Total elapsed
      Text(
        text = formatTime(currentDisplayTime.inWholeMilliseconds, duration.inWholeMilliseconds),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = labelColor,
      )

      // Right: Remaining OR Total duration (Toggleable)
      Text(
        modifier = Modifier.clickable { showTotalDuration = !showTotalDuration },
        text = if (showTotalDuration) {
          formatTime(duration.inWholeMilliseconds, duration.inWholeMilliseconds)
        } else {
          "-${formatTime((duration - currentDisplayTime).inWholeMilliseconds, duration.inWholeMilliseconds)}"
        },
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = labelColor,
      )
    }
  }
}

internal fun Color.toVibrant(): Color {
  val hsv = FloatArray(3)
  android.graphics.Color.colorToHSV(this.toArgb(), hsv)
  if (hsv[1] > 0.08f) {
    hsv[1] = hsv[1].coerceAtLeast(0.85f)
    hsv[2] = hsv[2].coerceIn(0.85f, 1.0f)
    return Color(android.graphics.Color.HSVToColor(hsv))
  }
  return Color.White
}
