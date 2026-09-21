package de.clio.features.playbackScreen.view

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
  onQueueClick: () -> Unit,
  useLandscapeLayout: Boolean,
  onChapterClick: (Int) -> Unit = {},
) {
  var isChaptersOpen by rememberSaveable { mutableStateOf(false) }
  var isChaptersFullyExpanded by rememberSaveable { mutableStateOf(false) }

  BackHandler(enabled = isChaptersOpen) {
    if (isChaptersFullyExpanded) {
      isChaptersFullyExpanded = false
    } else {
      isChaptersOpen = false
    }
  }

  val chaptersTransition = updateTransition(targetState = isChaptersOpen, label = "chapters_transition")
  val chaptersProgress by chaptersTransition.animateFloat(
    transitionSpec = {
      tween(durationMillis = 300, easing = FastOutSlowInEasing)
    },
    label = "chapters_progress",
  ) { open ->
    if (open) 1f else 0f
  }

  val chaptersSheetHeight by animateFloatAsState(
    targetValue = if (isChaptersFullyExpanded) 1f else 0.55f,
    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
    label = "chapters_sheet_height",
  )

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

  val swipeUpModifier = if (!viewState.isLocked && viewState.playerButtonVisibility.showQueue) {
    Modifier.pointerInput(onQueueClick) {
      var totalDrag = 0f
      var triggered = false
      detectVerticalDragGestures(
        onDragStart = {
          totalDrag = 0f
          triggered = false
        },
        onDragEnd = {
          if (!triggered && totalDrag < -40.dp.toPx()) {
            onQueueClick()
          }
          totalDrag = 0f
          triggered = false
        },
        onDragCancel = {
          totalDrag = 0f
          triggered = false
        },
        onVerticalDrag = { change, dragAmount ->
          change.consume()
          totalDrag += dragAmount
          if (!triggered && totalDrag < -50.dp.toPx()) {
            triggered = true
            onQueueClick()
          }
        },
      )
    }
  } else {
    Modifier
  }

  if (useLandscapeLayout) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(contentPadding)
        .then(contentModifier)
        .then(swipeUpModifier),
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
        ChapterTitleHeader(
          chapterName = viewState.chapterName,
          hasChapters = viewState.hasChapters,
          isChaptersOpen = isChaptersOpen,
          isCustomBackground = isCustomBackground,
          enabled = !viewState.isLocked,
          onToggleChapters = { isChaptersOpen = !isChaptersOpen },
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .graphicsLayer {
                alpha = (1f - chaptersProgress * 1.5f).coerceIn(0f, 1f)
              },
          ) {
            ChapterSliderBar(
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

          if (chaptersProgress > 0f) {
            Box(
              modifier = Modifier
                .matchParentSize()
                .clipToBounds()
                .graphicsLayer {
                  alpha = chaptersProgress.coerceIn(0f, 1f)
                  translationY = -size.height * (1f - chaptersProgress)
                },
            ) {
              ChaptersOverlay(
                chapters = viewState.chapters,
                backgroundStyle = viewState.backgroundStyle,
                onChapterClick = { number ->
                  onChapterClick(number)
                  isChaptersOpen = false
                },
              )
            }
          }
        }
      }
    }
  } else {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .then(if (!isChaptersOpen) swipeUpModifier else Modifier),
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = contentPadding.calculateTopPadding())
          .navigationBarsPadding()
          .then(contentModifier),
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
          hasChapters = viewState.hasChapters,
          isChaptersOpen = isChaptersOpen,
          onToggleChapters = { isChaptersOpen = !isChaptersOpen },
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

      if (chaptersProgress > 0f) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
            ) {
              if (isChaptersFullyExpanded) {
                isChaptersFullyExpanded = false
              } else {
                isChaptersOpen = false
              }
            },
        )

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = contentPadding.calculateTopPadding())
            .fillMaxHeight(chaptersSheetHeight)
            .align(Alignment.BottomCenter)
            .graphicsLayer {
              translationY = size.height * (1f - chaptersProgress)
            },
        ) {
          ChaptersOverlay(
            chapterName = viewState.chapterName,
            chapters = viewState.chapters,
            backgroundStyle = viewState.backgroundStyle,
            isFullyExpanded = isChaptersFullyExpanded,
            onExpand = { isChaptersFullyExpanded = true },
            onCollapse = { isChaptersFullyExpanded = false },
            onClose = {
              isChaptersOpen = false
              isChaptersFullyExpanded = false
            },
            onChapterClick = { number ->
              onChapterClick(number)
              isChaptersOpen = false
              isChaptersFullyExpanded = false
            },
          )
        }
      }
    }
  }
}
