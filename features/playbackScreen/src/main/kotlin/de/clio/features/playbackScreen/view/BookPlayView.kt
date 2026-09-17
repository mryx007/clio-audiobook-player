package de.clio.features.playbackScreen.view

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.clio.core.data.BookId
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.ui.ClioTheme
import de.clio.features.playbackScreen.BookPlayViewState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Composable
internal fun BookPlayView(
  viewState: BookPlayViewState,
  bookId: BookId,
  useLandscapeLayout: Boolean,
  onPlayClick: () -> Unit,
  onRewindClick: () -> Unit,
  onFastForwardClick: () -> Unit,
  onSeek: (Duration) -> Unit,
  onChapterSeek: (Duration) -> Unit,
  onSkipToNext: () -> Unit,
  onSkipToPrevious: () -> Unit,
  onSleepTimerClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onBookmarkLongClick: () -> Unit,
  onSpeedChangeClick: () -> Unit,
  onSkipSilenceClick: () -> Unit,
  onVolumeBoostClick: () -> Unit,
  onEqualizerClick: () -> Unit,
  onLockClick: () -> Unit,
  onCloseClick: () -> Unit,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val context = LocalContext.current
  val isDarkTheme = isSystemInDarkTheme()
  val isCustomBackground = viewState.backgroundStyle != PlaybackBackgroundStyle.Solid

  DisposableEffect(isCustomBackground, isDarkTheme) {
    val activity = context as? ComponentActivity
    if (activity != null) {
      if (isCustomBackground) {
        // Force light icons for custom (typically dark) backgrounds
        activity.enableEdgeToEdge(
          statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
          navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
      } else {
        // Restore default auto behavior
        activity.enableEdgeToEdge(
          statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
          navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
      }
    }
    onDispose {
      // Always restore default on dispose
      activity?.enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
      )
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    PlaybackBackground(
      cover = viewState.cover,
      style = viewState.backgroundStyle,
    )
    Scaffold(
      containerColor = Color.Transparent,
      snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
      },
      topBar = {
        BookPlayAppBar(
          viewState = viewState,
          onSleepTimerClick = onSleepTimerClick,
          onBookmarkClick = onBookmarkClick,
          onBookmarkLongClick = onBookmarkLongClick,
          onSpeedChangeClick = onSpeedChangeClick,
          onSkipSilenceClick = onSkipSilenceClick,
          onVolumeBoostClick = onVolumeBoostClick,
          onEqualizerClick = onEqualizerClick,
          onLockClick = onLockClick,
          onCloseClick = onCloseClick,
          useLandscapeLayout = useLandscapeLayout,
        )
      },
      content = { padding ->
        BookPlayContent(
          contentPadding = padding,
          viewState = viewState,
          bookId = bookId,
          onPlayClick = onPlayClick,
          onRewindClick = onRewindClick,
          onFastForwardClick = onFastForwardClick,
          onSkipToNext = onSkipToNext,
          onSkipToPrevious = onSkipToPrevious,
          onSeek = onSeek,
          onChapterSeek = onChapterSeek,
          useLandscapeLayout = useLandscapeLayout,
        )
      },
    )
  }
}

@Composable
@Preview
private fun BookPlayPreview(
  @PreviewParameter(BookPlayViewStatePreviewProvider::class)
  viewState: BookPlayViewState,
) {
  ClioTheme {
    BookPlayView(
      viewState = viewState,
      bookId = BookId("preview"),
      onPlayClick = {},
      onRewindClick = {},
      onFastForwardClick = {},
      onSeek = {},
      onChapterSeek = {},
      onSkipToNext = {},
      onSkipToPrevious = {},
      onSleepTimerClick = {},
      onBookmarkClick = {},
      onBookmarkLongClick = {},
      onSpeedChangeClick = {},
      onSkipSilenceClick = {},
      onVolumeBoostClick = {},
      onEqualizerClick = {},
      onLockClick = {},
      onCloseClick = {},
      useLandscapeLayout = false,
    )
  }
}

private class BookPlayViewStatePreviewProvider : PreviewParameterProvider<BookPlayViewState> {
  override val values = sequence {
    val initial = BookPlayViewState(
      chapterName = "My Chapter",
      showPreviousNextButtons = false,
      cover = null,
      duration = 10.minutes,
      playedTime = 3.minutes,
      totalDuration = 60.minutes,
      totalPlayedTime = 23.minutes,
      playing = true,
      skipSilence = true,
      sleepTimerState = BookPlayViewState.SleepTimerViewState.Disabled,
      isLocked = false,
      backgroundStyle = PlaybackBackgroundStyle.Solid,
      title = "Das Ende der Welt",
    )
    yield(initial)
    yield(
      initial.copy(
        showPreviousNextButtons = !initial.showPreviousNextButtons,
        playing = !initial.playing,
        skipSilence = !initial.skipSilence,
      ),
    )
    yield(initial.copy(chapterName = null))
  }
}
