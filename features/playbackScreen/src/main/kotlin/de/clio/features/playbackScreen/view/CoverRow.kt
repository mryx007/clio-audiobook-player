package de.clio.features.playbackScreen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import de.clio.core.data.BookId
import de.clio.core.strings.R
import de.clio.core.ui.formatTime
import de.clio.features.playbackScreen.BookPlayViewState

@Composable
internal fun CoverRow(
  bookId: BookId,
  cover: String?,
  sleepTimerState: BookPlayViewState.SleepTimerViewState,
  onPlayClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentAlignment: Alignment = Alignment.Center,
) {
  var coverAspectRatio by remember(cover) { mutableFloatStateOf(1f) }

  BoxWithConstraints(
    modifier = modifier,
    contentAlignment = contentAlignment,
  ) {
    val targetModifier = if (maxHeight.isSpecified && maxWidth.isSpecified && maxHeight.value > 0 && maxWidth.value > 0) {
      val containerRatio = maxWidth.value / maxHeight.value
      val (targetWidth, targetHeight) = if (coverAspectRatio > containerRatio) {
        maxWidth to (maxWidth / coverAspectRatio)
      } else {
        (maxHeight * coverAspectRatio) to maxHeight
      }
      Modifier.size(width = targetWidth, height = targetHeight)
    } else {
      Modifier
    }

    Box(modifier = targetModifier) {
      Cover(
        bookId = bookId,
        onDoubleClick = onPlayClick,
        cover = cover,
        onCoverLoad = { ratio ->
          coverAspectRatio = ratio
        },
      )
      when (sleepTimerState) {
        BookPlayViewState.SleepTimerViewState.Disabled -> {
        }
        is BookPlayViewState.SleepTimerViewState.Enabled -> {
          Text(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(top = 8.dp, end = 8.dp)
              .background(
                color = Color(0x7E000000),
                shape = RoundedCornerShape(20.dp),
              )
              .padding(horizontal = 20.dp, vertical = 16.dp),
            text = when (sleepTimerState) {
              is BookPlayViewState.SleepTimerViewState.Enabled.WithDuration -> formatTime(
                timeMs = sleepTimerState.leftDuration.inWholeMilliseconds,
              )
              BookPlayViewState.SleepTimerViewState.Enabled.WithEndOfChapter -> stringResource(R.string.sleep_timer_end_of_chapter)
            },
            color = Color.White,
          )
        }
      }
    }
  }
}
