package de.clio.features.playbackScreen

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import de.clio.core.ui.LocalAppReady
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import de.clio.core.common.rootGraphAs
import de.clio.core.data.BookId
import de.clio.features.playbackScreen.view.BookPlayView
import de.clio.features.sleepTimer.SleepTimerDialog
import de.clio.navigation.Destination
import de.clio.navigation.NavEntryProvider
import de.clio.core.strings.R as StringsR

@Composable
fun BookPlayScreen(bookId: BookId) {
  val viewModel = retain(bookId.value) {
    rootGraphAs<BookPlayGraph>()
      .bookPlayViewModelFactory
      .create(bookId)
  }
  val snackbarHostState = remember { SnackbarHostState() }
  val dialogState = viewModel.dialogState.value
  val viewState = viewModel.viewState()
    ?: return

  val onAppReady = LocalAppReady.current
  SideEffect {
    onAppReady()
  }

  val bookmarkAddedMessage = stringResource(StringsR.string.bookmark_added_snackbar)
  val batteryOptimizationMessage = stringResource(StringsR.string.playback_battery_optimization_rationale)
  val batteryOptimizationAction = stringResource(StringsR.string.playback_battery_optimization_action)
  LaunchedEffect(viewModel) {
    viewModel.viewEffects.collect { viewEffect ->
      when (viewEffect) {
        BookPlayViewEffect.BookmarkAdded -> {
          snackbarHostState.showSnackbar(message = bookmarkAddedMessage)
        }
        BookPlayViewEffect.RequestIgnoreBatteryOptimization -> {
          val result = snackbarHostState.showSnackbar(
            message = batteryOptimizationMessage,
            duration = SnackbarDuration.Long,
            actionLabel = batteryOptimizationAction,
          )
          if (result == SnackbarResult.ActionPerformed) {
            viewModel.onBatteryOptimizationRequested()
          }
        }
      }
    }
  }
  BookPlayView(
    viewState,
    bookId = bookId,
    onPlayClick = viewModel::playPause,
    onFastForwardClick = viewModel::fastForward,
    onRewindClick = viewModel::rewind,
    onSeek = viewModel::seekTo,
    onChapterSeek = viewModel::seekToChapter,
    onSkipToNext = viewModel::next,
    onSkipToPrevious = viewModel::previous,
    onBookmarkClick = viewModel::onBookmarkClick,
    onBookmarkLongClick = viewModel::onBookmarkLongClick,
    onSkipSilenceClick = viewModel::toggleSkipSilence,
    onSleepTimerClick = viewModel::toggleSleepTimer,
    onVolumeBoostClick = viewModel::onVolumeGainIconClick,
    onSpeedChangeClick = viewModel::onPlaybackSpeedIconClick,
    onCloseClick = viewModel::onCloseClick,
    onEqualizerClick = viewModel::onEqualizerIconClick,
    onLockClick = viewModel::toggleLock,
    useLandscapeLayout = LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE,
    snackbarHostState = snackbarHostState,
  )
  if (dialogState != null) {
    when (dialogState) {
      is BookPlayDialogViewState.SpeedDialog -> {
        SpeedDialog(dialogState, viewModel)
      }
      is BookPlayDialogViewState.VolumeGainDialog -> {
        VolumeGainDialog(dialogState, viewModel)
      }
      is BookPlayDialogViewState.SelectChapterDialog -> {
        SelectChapterDialog(dialogState, viewModel)
      }
      is BookPlayDialogViewState.SleepTimer -> {
        SleepTimerDialog(
          viewState = dialogState.viewState,
          onDismiss = viewModel::dismissDialog,
          onIncrementSleepTime = viewModel::incrementSleepTime,
          onDecrementSleepTime = viewModel::decrementSleepTime,
          onAcceptSleepTime = viewModel::onAcceptSleepTime,
          onAcceptSleepAtEndOfChapter = viewModel::onAcceptSleepAtEndOfChapter,
        )
      }
      is BookPlayDialogViewState.EqualizerDialog -> {
        EqualizerDialog(dialogState, viewModel)
      }
    }
  }
}

@ContributesTo(AppScope::class)
interface BookPlayGraph {
  val bookPlayViewModelFactory: BookPlayViewModel.Factory
}

@ContributesTo(AppScope::class)
interface BookPlayProvider {

  @Provides
  @IntoSet
  fun bookPlayNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Playback> { key ->
    NavEntry(key) {
      BookPlayScreen(bookId = key.bookId)
    }
  }
}
