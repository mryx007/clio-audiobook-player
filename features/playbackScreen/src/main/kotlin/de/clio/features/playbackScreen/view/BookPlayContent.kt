package de.clio.features.playbackScreen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.clio.core.data.BookId
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.features.playbackScreen.BookPlayViewState
import kotlin.time.Duration

@Composable
internal fun BookPlayContent(
  contentPadding: PaddingValues,
  viewState: BookPlayViewState,
  bookId: BookId,
  onPlayClick: () -> Unit,
  onRewindClick: () -> Unit,
  onFastForwardClick: () -> Unit,
  onSkipToNext: () -> Unit,
  onSkipToPrevious: () -> Unit,
  onSeek: (Duration) -> Unit,
  onChapterSeek: (Duration) -> Unit,
  useLandscapeLayout: Boolean,
) {
  val isCustomBackground = viewState.backgroundStyle != PlaybackBackgroundStyle.Solid
  val isGlass = viewState.backgroundStyle == PlaybackBackgroundStyle.Glassmorphism
  val contentModifier = if (isGlass) {
    Modifier
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .clip(RoundedCornerShape(24.dp))
      .background(Color.White.copy(alpha = 0.1f))
      .padding(16.dp)
  } else {
    Modifier
  }

  if (useLandscapeLayout) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(contentPadding)
        .then(contentModifier)
    ) {
      CoverRow(
        bookId = bookId,
        cover = viewState.cover,
        onPlayClick = onPlayClick,
        sleepTimerState = viewState.sleepTimerState,
        modifier = Modifier
          .fillMaxHeight()
          .weight(1F)
          .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
      )
      Column(
        modifier = Modifier
          .fillMaxHeight()
          .weight(1F),
        verticalArrangement = Arrangement.Center,
      ) {
        ChapterSliderRow(
          chapterName = viewState.chapterName,
          duration = viewState.duration,
          playedTime = viewState.playedTime,
          isCustomBackground = isCustomBackground,
          enabled = !viewState.isLocked,
          onSeek = onChapterSeek,
        )
        Spacer(modifier = Modifier.size(12.dp))
        SliderRow(
          duration = viewState.totalDuration,
          playedTime = viewState.totalPlayedTime,
          isCustomBackground = isCustomBackground,
          enabled = !viewState.isLocked,
          onSeek = onSeek,
        )
        Spacer(modifier = Modifier.size(16.dp))
        PlaybackRow(
          playing = viewState.playing,
          isLocked = viewState.isLocked,
          isCustomBackground = isCustomBackground,
          rewindTimeInSeconds = viewState.rewindTimeInSeconds,
          fastForwardTimeInSeconds = viewState.fastForwardTimeInSeconds,
          onPlayClick = onPlayClick,
          onRewindClick = onRewindClick,
          onFastForwardClick = onFastForwardClick,
          onSkipToNext = onSkipToNext,
          onSkipToPrevious = onSkipToPrevious,
        )
      }
    }
  } else {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(contentPadding)
        .then(contentModifier)
    ) {
      CoverRow(
        bookId = bookId,
        onPlayClick = onPlayClick,
        cover = viewState.cover,
        sleepTimerState = viewState.sleepTimerState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1F)
          .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp)
          .offset(y = (-14).dp),
      )

      ChapterSliderRow(
        chapterName = viewState.chapterName,
        duration = viewState.duration,
        playedTime = viewState.playedTime,
        isCustomBackground = isCustomBackground,
        enabled = !viewState.isLocked,
        onSeek = onChapterSeek,
      )

      Spacer(modifier = Modifier.size(8.dp))

      SliderRow(
        duration = viewState.totalDuration,
        playedTime = viewState.totalPlayedTime,
        isCustomBackground = isCustomBackground,
        enabled = !viewState.isLocked,
        onSeek = onSeek,
      )

      Spacer(modifier = Modifier.size(16.dp))
      PlaybackRow(
        playing = viewState.playing,
        isLocked = viewState.isLocked,
        isCustomBackground = isCustomBackground,
        rewindTimeInSeconds = viewState.rewindTimeInSeconds,
        fastForwardTimeInSeconds = viewState.fastForwardTimeInSeconds,
        onPlayClick = onPlayClick,
        onRewindClick = onRewindClick,
        onFastForwardClick = onFastForwardClick,
        onSkipToNext = onSkipToNext,
        onSkipToPrevious = onSkipToPrevious,
      )
      Spacer(modifier = Modifier.size(24.dp))
    }
  }
}
