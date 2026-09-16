package voice.features.playbackScreen.view

import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import voice.core.ui.formatTime
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChapterSliderRow(
  chapterName: String?,
  duration: Duration,
  playedTime: Duration,
  isCustomBackground: Boolean,
  enabled: Boolean,
  onSeek: (Duration) -> Unit,
) {
  val labelColor = if (isCustomBackground) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
  val trackBackgroundColor = if (isCustomBackground) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
  val activeTrackColor = MaterialTheme.colorScheme.primary.toVibrant().copy(alpha = 0.85f)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(0.dp)
  ) {
    if (chapterName != null) {
      Text(
        text = chapterName,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = if (isCustomBackground) Color.White else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 4.dp)
      )
    }

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
        .height(18.dp),
      contentAlignment = Alignment.Center
    ) {
      // Custom thinner track background
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp) // Thinner than main bar
          .background(
            color = trackBackgroundColor,
            shape = RoundedCornerShape(3.dp)
          )
      )

      // Custom progress track
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(sliderValue)
            .fillMaxHeight()
            .background(
              color = activeTrackColor,
              shape = RoundedCornerShape(
                topStart = 3.dp,
                bottomStart = 3.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp
              )
            )
        )
      }

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
          thumbColor = Color.Transparent,
        ),
        thumb = {}
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = formatTime(currentDisplayTime.inWholeMilliseconds, duration.inWholeMilliseconds),
        style = MaterialTheme.typography.labelMedium,
        color = labelColor,
      )
      Text(
        text = "-${formatTime((duration - currentDisplayTime).inWholeMilliseconds, duration.inWholeMilliseconds)}",
        style = MaterialTheme.typography.labelMedium,
        color = labelColor,
      )
    }
  }
}
