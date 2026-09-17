package de.clio.features.playbackScreen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
  Box(
    modifier = modifier,
    contentAlignment = contentAlignment,
  ) {
    Box(modifier = Modifier.aspectRatio(1f)) {
      Cover(bookId = bookId, onDoubleClick = onPlayClick, cover = cover)
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
