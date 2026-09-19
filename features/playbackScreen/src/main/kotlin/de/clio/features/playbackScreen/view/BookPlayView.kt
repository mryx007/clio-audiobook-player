package de.clio.features.playbackScreen.view

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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
import androidx.core.view.WindowCompat
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
  onQueueClick: () -> Unit,
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
  val backgroundColor = MaterialTheme.colorScheme.background
  val isAppDark = remember(backgroundColor) {
    (0.299f * backgroundColor.red + 0.587f * backgroundColor.green + 0.114f * backgroundColor.blue) < 0.5f
  }
  val isCustomBackground = viewState.backgroundStyle != PlaybackBackgroundStyle.Solid
  val isScreenDark = isCustomBackground || isAppDark

  DisposableEffect(isCustomBackground, isAppDark) {
    val activity = context as? ComponentActivity
    if (activity != null) {
      val insetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
      insetsController.isAppearanceLightStatusBars = !isScreenDark
      insetsController.isAppearanceLightNavigationBars = !isScreenDark

      activity.enableEdgeToEdge(
        statusBarStyle = if (isScreenDark) {
          SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        } else {
          SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        },
        navigationBarStyle = if (isScreenDark) {
          SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        } else {
          SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        },
      )
      if (android.os.Build.VERSION.SDK_INT >= 29) {
        activity.window.isNavigationBarContrastEnforced = false
      }
    }
    onDispose {
      val activity = context as? ComponentActivity
      if (activity != null) {
        val insetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        insetsController.isAppearanceLightStatusBars = !isAppDark
        insetsController.isAppearanceLightNavigationBars = !isAppDark

        activity.enableEdgeToEdge(
          statusBarStyle = if (isAppDark) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
          } else {
            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
          },
          navigationBarStyle = if (isAppDark) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
          } else {
            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
          },
        )
        if (android.os.Build.VERSION.SDK_INT >= 29) {
          activity.window.isNavigationBarContrastEnforced = false
        }
      }
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
          onQueueClick = onQueueClick,
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
      onQueueClick = {},
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
