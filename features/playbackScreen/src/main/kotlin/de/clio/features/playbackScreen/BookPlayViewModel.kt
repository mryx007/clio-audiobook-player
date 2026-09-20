package de.clio.features.playbackScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import de.clio.core.common.DispatcherProvider
import de.clio.core.common.MainScope
import de.clio.core.data.BackButtonBehavior
import de.clio.core.data.Book
import de.clio.core.data.BookId
import de.clio.core.data.EndOfBookBehavior
import de.clio.core.data.EqualizerSetting
import de.clio.core.data.KioskModeDemoData
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.data.PlayerButtonVisibility
import de.clio.core.data.durationMs
import de.clio.core.data.formatDisplayChapterName
import de.clio.core.data.markForPosition
import de.clio.core.data.repo.BookQueueRepository
import de.clio.core.data.repo.BookRepository
import de.clio.core.data.repo.BookmarkRepo
import de.clio.core.data.repo.FakeBookQueueRepository
import de.clio.core.data.sleeptimer.SleepTimerPreference
import de.clio.core.data.store.BackButtonBehaviorStore
import de.clio.core.data.store.CurrentBookStore
import de.clio.core.data.store.EndOfBookBehaviorStore
import de.clio.core.data.store.FastForwardTimeStore
import de.clio.core.data.store.PlaybackBackgroundStyleStore
import de.clio.core.data.store.PlayerButtonVisibilityStore
import de.clio.core.data.store.PlayerLockedStore
import de.clio.core.data.store.RewindTimeStore
import de.clio.core.data.store.SleepTimerPreferenceStore
import de.clio.core.featureflag.ExperimentalPlaybackPersistenceQualifier
import de.clio.core.featureflag.FeatureFlag
import de.clio.core.featureflag.KioskModeFeatureFlagQualifier
import de.clio.core.logging.api.Logger
import de.clio.core.playback.CurrentBookResolver
import de.clio.core.playback.PlayerController
import de.clio.core.playback.misc.Decibel
import de.clio.core.playback.misc.VolumeGain
import de.clio.core.playback.overlay
import de.clio.core.playback.playstate.PlayStateManager
import de.clio.core.sleeptimer.SleepTimer
import de.clio.core.sleeptimer.SleepTimerMode
import de.clio.core.sleeptimer.SleepTimerMode.TimedWithDuration
import de.clio.core.sleeptimer.SleepTimerState
import de.clio.core.ui.formatTime
import de.clio.features.playbackScreen.batteryOptimization.BatteryOptimization
import de.clio.features.sleepTimer.SleepTimerViewState
import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@AssistedInject
class BookPlayViewModel(
  private val bookRepository: BookRepository,
  private val currentBookResolver: CurrentBookResolver,
  private val player: PlayerController,
  private val sleepTimer: SleepTimer,
  private val playStateManager: PlayStateManager,
  @CurrentBookStore
  private val currentBookStoreId: DataStore<BookId?>,
  @RewindTimeStore
  private val rewindTimeStore: DataStore<Int>,
  @FastForwardTimeStore
  private val fastForwardTimeStore: DataStore<Int>,
  @PlaybackBackgroundStyleStore
  private val playbackBackgroundStyleStore: DataStore<PlaybackBackgroundStyle>,
  private val navigator: Navigator,
  private val bookmarkRepository: BookmarkRepo,
  private val volumeGainFormatter: VolumeGainFormatter,
  private val batteryOptimization: BatteryOptimization,
  dispatcherProvider: DispatcherProvider,
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  @PlayerButtonVisibilityStore
  private val playerButtonVisibilityStore: DataStore<PlayerButtonVisibility>,
  @PlayerLockedStore
  private val playerLockedStore: DataStore<Boolean>,
  @BackButtonBehaviorStore
  private val backButtonBehaviorStore: DataStore<BackButtonBehavior>,
  @EndOfBookBehaviorStore
  private val endOfBookBehaviorStore: DataStore<EndOfBookBehavior>,
  private val queueRepository: BookQueueRepository = FakeBookQueueRepository(),
  @ExperimentalPlaybackPersistenceQualifier
  private val experimentalPlaybackPersistenceFeatureFlag: FeatureFlag<Boolean>,
  @KioskModeFeatureFlagQualifier
  private val kioskModeFeatureFlag: FeatureFlag<Boolean>,
  @Assisted
  private val bookId: BookId,
) {

  private val scope = MainScope(dispatcherProvider)

  internal val viewEffects: Flow<BookPlayViewEffect>
    field = MutableSharedFlow<BookPlayViewEffect>(extraBufferCapacity = 1)

  internal val dialogState: State<BookPlayDialogViewState?>
    field = mutableStateOf<BookPlayDialogViewState?>(null)

  init {
    scope.launch {
      player.pauseIfCurrentBookDifferentFrom(bookId)
      currentBookStoreId.updateData { bookId }
    }
    scope.launch {
      player.playbackEndedFlow()
        .filter { it == bookId }
        .collect {
          when (endOfBookBehaviorStore.data.first()) {
            EndOfBookBehavior.ContinueQueue -> {
              val nextBookId = queueRepository.popNext()
              if (nextBookId != null) {
                currentBookStoreId.updateData { nextBookId }
                player.play()
                navigator.replace(Destination.Playback(nextBookId))
              }
            }
            EndOfBookBehavior.BookOverview -> navigator.goBack()
            EndOfBookBehavior.DoNothing -> Unit
          }
        }
    }
  }

  @Composable
  fun viewState(): BookPlayViewState? {
    val kioskMode = remember { kioskModeFeatureFlag.get() }
    if (kioskMode) return kioskModeViewState()

    val persistedBook = remember(bookId) {
      bookRepository.flow(bookId).filterNotNull()
    }.collectAsState(initial = null).value ?: return null

    val livePlaybackState = remember(bookId) {
      player.livePlaybackStateFlow(bookId)
    }.collectAsState(null).value
    val managerPlayState by remember {
      playStateManager.playStateFlow
    }.collectAsState()

    val book = if (livePlaybackState != null) {
      persistedBook.overlay(livePlaybackState)
    } else {
      persistedBook
    }
    val isPlaying = livePlaybackState?.isPlaying ?: (managerPlayState == PlayStateManager.PlayState.Playing)

    val currentMark = book.currentChapter.markForPosition(book.content.positionInChapter)
    val positionInCurrentMark = if (isPlaying && currentMark.durationMs > 0) {
      val relativePosition = book.content.positionInChapter - currentMark.startMs
      relativePosition.coerceIn(0L, currentMark.durationMs)
    } else {
      book.content.positionInChapter - currentMark.startMs
    }

    val sleepTime = remember { sleepTimer.state }.collectAsState().value
    val backgroundStyle = remember { playbackBackgroundStyleStore.data }
      .collectAsState(initial = PlaybackBackgroundStyle.Solid).value
    val rewindTime = remember { rewindTimeStore.data }.collectAsState(initial = 20).value
    val fastForwardTime = remember { fastForwardTimeStore.data }.collectAsState(initial = 30).value
    val playerButtonVisibility = remember { playerButtonVisibilityStore.data }
      .collectAsState(initial = PlayerButtonVisibility()).value
    val isLocked = remember { playerLockedStore.data }
      .collectAsState(initial = false).value
    val queue = remember { queueRepository.queueFlow }.collectAsState().value
    val hasMoreThanOneChapter = remember(persistedBook.chapters) {
      persistedBook.chapters.sumOf { it.chapterMarks.count() } > 1
    }
    val chapterName = remember(currentMark.name, book.content.name, book.currentChapter.id, book.content.author, hasMoreThanOneChapter) {
      if (hasMoreThanOneChapter) {
        formatDisplayChapterName(
          chapterName = currentMark.name,
          bookName = book.content.name,
          chapterUri = book.currentChapter.id.value,
          author = book.content.author,
        )
      } else {
        null
      }
    }
    val chapters =
      remember(persistedBook.chapters, currentMark, book.currentChapter, hasMoreThanOneChapter, book.content.name, book.content.author) {
        if (!hasMoreThanOneChapter) {
          emptyList()
        } else {
          persistedBook.chapters.flatMapIndexed { chapterIndex, chapter ->
            chapter.chapterMarks.mapIndexed { markIndex, chapterMark ->
              val previousChapters = persistedBook.chapters.take(chapterIndex)
              val displayName = formatDisplayChapterName(
                chapterName = chapterMark.name,
                bookName = book.content.name,
                chapterUri = chapter.id.value,
                author = book.content.author,
              ) ?: ""
              BookPlayViewState.BookPlayChapter(
                number = previousChapters.sumOf { it.chapterMarks.count() } + markIndex + 1,
                name = displayName,
                active = chapterMark == currentMark && chapter == book.currentChapter,
                time = formatTime(previousChapters.sumOf { it.duration } + chapterMark.startMs),
              )
            }
          }
        }
      }
    return BookPlayViewState(
      sleepTimerState = sleepTime.toViewState(),
      playing = isPlaying,
      title = book.content.name,
      showPreviousNextButtons = hasMoreThanOneChapter,
      chapterName = chapterName,
      duration = currentMark.durationMs.milliseconds,
      playedTime = positionInCurrentMark.milliseconds,
      totalDuration = book.duration.milliseconds,
      totalPlayedTime = book.position.milliseconds,
      isLocked = isLocked,
      backgroundStyle = backgroundStyle,
      cover = book.content.coverUrl,
      skipSilence = book.content.skipSilence,
      rewindTimeInSeconds = rewindTime,
      fastForwardTimeInSeconds = fastForwardTime,
      playerButtonVisibility = playerButtonVisibility,
      queueCount = queue.size,
      chapters = chapters,
    )
  }

  private fun kioskModeViewState(): BookPlayViewState {
    val currentlyPlaying = KioskModeDemoData.currentlyPlaying
    val book = KioskModeDemoData.currentlyPlayingBook
    return BookPlayViewState(
      sleepTimerState = BookPlayViewState.SleepTimerViewState.Disabled,
      playing = true,
      title = currentlyPlaying.title,
      showPreviousNextButtons = true,
      chapterName = currentlyPlaying.chapter,
      duration = 14.hours + 27.minutes,
      playedTime = 10.hours + 24.minutes,
      totalDuration = 14.hours + 27.minutes,
      totalPlayedTime = 10.hours + 24.minutes,
      isLocked = false,
      backgroundStyle = PlaybackBackgroundStyle.Solid,
      cover = book.coverUrl,
      skipSilence = false,
      rewindTimeInSeconds = 20,
      fastForwardTimeInSeconds = 30,
    )
  }

  fun dismissDialog() {
    Logger.d("dismissDialog")
    dialogState.value = null
  }

  fun onQueueClick() {
    scope.launch {
      val currentBook = bookRepository.get(bookId)
      val queueIds = queueRepository.queueFlow.value
      val queuedBooks = queueIds.mapNotNull { bookRepository.get(it) }
      dialogState.value = BookPlayDialogViewState.QueueSheet(
        currentBook = currentBook,
        queueItems = queuedBooks,
      )
    }
  }

  fun onQueueBookClick(id: BookId) {
    dismissDialog()
    scope.launch {
      queueRepository.removeFromQueue(setOf(id))
      currentBookStoreId.updateData { id }
      player.play()
      navigator.replace(Destination.Playback(id))
    }
  }

  fun onRemoveFromQueue(bookIds: Set<BookId>) {
    scope.launch {
      queueRepository.removeFromQueue(bookIds)
      val currentBook = bookRepository.get(bookId)
      val queueIds = queueRepository.queueFlow.value.filter { it !in bookIds }
      val queuedBooks = queueIds.mapNotNull { bookRepository.get(it) }
      dialogState.value = BookPlayDialogViewState.QueueSheet(
        currentBook = currentBook,
        queueItems = queuedBooks,
      )
    }
  }

  fun onClearQueue() {
    scope.launch {
      queueRepository.clearQueue()
      val currentBook = bookRepository.get(bookId)
      dialogState.value = BookPlayDialogViewState.QueueSheet(
        currentBook = currentBook,
        queueItems = emptyList(),
      )
    }
  }

  fun onReorderQueue(bookIds: List<BookId>) {
    scope.launch {
      queueRepository.reorder(bookIds)
      val currentBook = bookRepository.get(bookId)
      val queuedBooks = bookIds.mapNotNull { bookRepository.get(it) }
      dialogState.value = BookPlayDialogViewState.QueueSheet(
        currentBook = currentBook,
        queueItems = queuedBooks,
      )
    }
  }

  fun incrementSleepTime() {
    updateSleepTimeViewState {
      val customTime = it.customSleepTime
      val newTime = customTime + 1
      sleepTimerPreferenceStore.updateData { preference -> preference.copy(duration = newTime.minutes) }
      SleepTimerViewState(newTime)
    }
  }

  fun decrementSleepTime() {
    updateSleepTimeViewState {
      val customTime = it.customSleepTime
      val newTime = (customTime - 1).coerceAtLeast(1)
      sleepTimerPreferenceStore.updateData { preference ->
        preference.copy(duration = newTime.minutes)
      }
      SleepTimerViewState(newTime)
    }
  }

  fun onAcceptSleepTime(time: Int) {
    updateSleepTimeViewState {
      val book = currentBook() ?: return@updateSleepTimeViewState null
      scope.launch {
        bookmarkRepository.addBookmarkAtBookPosition(
          book = book,
          setBySleepTimer = true,
          title = null,
        )
      }
      sleepTimer.enable(TimedWithDuration(time.minutes))
      null
    }
  }

  fun onAcceptSleepAtEndOfChapter() {
    updateSleepTimeViewState {
      sleepTimer.enable(SleepTimerMode.EndOfChapter)
      null
    }
  }

  private fun updateSleepTimeViewState(update: suspend (SleepTimerViewState) -> SleepTimerViewState?) {
    scope.launch {
      val current = dialogState.value
      val updated: SleepTimerViewState? = if (current is BookPlayDialogViewState.SleepTimer) {
        update(current.viewState)
      } else {
        update(SleepTimerViewState(sleepTimerPreferenceStore.data.first().duration.inWholeMinutes.toInt()))
      }
      dialogState.value = updated?.let(BookPlayDialogViewState::SleepTimer)
    }
  }

  fun onPlaybackSpeedChanged(speed: Float) {
    dialogState.value = BookPlayDialogViewState.SpeedDialog(speed)
    player.setSpeed(speed)
  }

  fun onVolumeGainChanged(gain: Decibel) {
    dialogState.value = volumeGainDialogViewState(gain)
    player.setGain(gain)
  }

  fun next() {
    player.next()
  }

  fun previous() {
    player.previous()
  }

  fun playPause() {
    if (playStateManager.playState != PlayStateManager.PlayState.Playing) {
      scope.launch {
        if (batteryOptimization.shouldRequest()) {
          viewEffects.tryEmit(BookPlayViewEffect.RequestIgnoreBatteryOptimization)
          batteryOptimization.onBatteryOptimizationsRequested()
        }
      }
    }
    player.playPause()
  }

  fun rewind() {
    player.rewind()
  }

  fun fastForward() {
    player.fastForward()
  }

  fun toggleLock() {
    scope.launch {
      playerLockedStore.updateData { !it }
    }
  }

  fun onCloseClick() {
    navigator.goBack()
  }

  fun onSystemBackClick() {
    scope.launch {
      if (backButtonBehaviorStore.data.first() == BackButtonBehavior.MinimizeApp) {
        navigator.minimizeApp()
      } else {
        navigator.goBack()
      }
    }
  }

  fun onCurrentChapterClick() {
    scope.launch {
      val book = currentBook() ?: return@launch
      dialogState.value = BookPlayDialogViewState.SelectChapterDialog(
        items = book.chapters.flatMapIndexed { chapterIndex, chapter ->
          chapter.chapterMarks.mapIndexed { markIndex, chapterMark ->
            val previousChapters = book.chapters.take(chapterIndex)
            val displayName = formatDisplayChapterName(
              chapterName = chapterMark.name,
              bookName = book.content.name,
              chapterUri = chapter.id.value,
              author = book.content.author,
            ) ?: ""
            BookPlayDialogViewState.SelectChapterDialog.ItemViewState(
              number = previousChapters.sumOf { it.chapterMarks.count() } + markIndex + 1,
              name = displayName,
              active = chapterMark == book.currentMark && chapter == book.currentChapter,
              time = formatTime(previousChapters.sumOf { it.duration } + chapterMark.startMs),
            )
          }
        },
      )
    }
  }

  fun onChapterClick(number: Int) {
    scope.launch {
      val book = currentBook() ?: return@launch
      var currentIndex = -1
      book.chapters.forEach { chapter ->
        chapter.chapterMarks.forEach { mark ->
          currentIndex++
          if (currentIndex == number - 1) {
            player.setPosition(mark.startMs, chapter.id)
            dialogState.value = null
            return@launch
          }
        }
      }
    }
  }

  fun onPlaybackSpeedIconClick() {
    scope.launch {
      val playbackSpeed = currentBook()?.content?.playbackSpeed ?: return@launch
      dialogState.value = BookPlayDialogViewState.SpeedDialog(playbackSpeed)
    }
  }

  fun onVolumeGainIconClick() {
    scope.launch {
      val content = currentBook()?.content ?: return@launch
      dialogState.value = volumeGainDialogViewState(Decibel(content.gain))
    }
  }

  private fun volumeGainDialogViewState(gain: Decibel): BookPlayDialogViewState.VolumeGainDialog {
    return BookPlayDialogViewState.VolumeGainDialog(
      gain = gain,
      maxGain = VolumeGain.MAX_GAIN,
      valueFormatted = volumeGainFormatter.format(gain),
    )
  }

  fun onEqualizerIconClick() {
    scope.launch {
      val content = currentBook()?.content ?: return@launch
      dialogState.value = BookPlayDialogViewState.EqualizerDialog(content.equalizerSetting.bands)
    }
  }

  fun onEqualizerBandChanged(
    index: Int,
    gainDb: Int,
  ) {
    val currentDialog = dialogState.value as? BookPlayDialogViewState.EqualizerDialog ?: return
    val newBands = currentDialog.bands.toMutableList()
    if (index in newBands.indices) {
      newBands[index] = gainDb.coerceIn(-12, 12)
      dialogState.value = BookPlayDialogViewState.EqualizerDialog(newBands)
      player.setEqualizer(newBands)
    }
  }

  fun onEqualizerPresetSelected(setting: EqualizerSetting) {
    dialogState.value = BookPlayDialogViewState.EqualizerDialog(setting.bands)
    player.setEqualizer(setting.bands)
  }

  fun onEqualizerReset() {
    val flatBands = EqualizerSetting.Flat.bands
    dialogState.value = BookPlayDialogViewState.EqualizerDialog(flatBands)
    player.setEqualizer(flatBands)
  }

  fun onBookmarkClick() {
    navigator.goTo(Destination.Bookmarks(bookId))
  }

  fun onBookmarkLongClick() {
    scope.launch {
      val book = currentBook() ?: return@launch
      bookmarkRepository.addBookmarkAtBookPosition(
        book = book,
        title = null,
        setBySleepTimer = false,
      )
      viewEffects.tryEmit(BookPlayViewEffect.BookmarkAdded)
    }
  }

  fun seekTo(position: Duration) {
    scope.launch {
      val book = currentBook() ?: return@launch
      var remaining = position.inWholeMilliseconds
      for (chapter in book.chapters) {
        if (remaining < chapter.duration) {
          player.setPosition(remaining, chapter.id)
          return@launch
        }
        remaining -= chapter.duration
      }
      val lastChapter = book.chapters.last()
      player.setPosition(lastChapter.duration, lastChapter.id)
    }
  }

  fun seekToChapter(position: Duration) {
    scope.launch {
      val book = currentBook() ?: return@launch
      val currentChapter = book.currentChapter
      val currentMark = currentChapter.markForPosition(book.content.positionInChapter)
      player.setPosition(currentMark.startMs + position.inWholeMilliseconds, currentChapter.id)
    }
  }

  fun toggleSleepTimer() {
    scope.launch {
      Logger.d("toggleSleepTimer while active=${sleepTimer.state.value}")
      if (sleepTimer.state.value.enabled) {
        sleepTimer.disable()
        dialogState.value = null
      } else {
        dialogState.value = BookPlayDialogViewState.SleepTimer(
          viewState = SleepTimerViewState(
            customSleepTime = sleepTimerPreferenceStore.data.first().duration.inWholeMinutes.toInt(),
          ),
        )
      }
    }
  }

  fun onBatteryOptimizationRequested() {
    navigator.goTo(Destination.BatteryOptimization)
  }

  fun toggleSkipSilence() {
    scope.launch {
      val skipSilence = currentBook()?.content?.skipSilence ?: return@launch
      player.skipSilence(!skipSilence)
    }
  }

  private suspend fun currentBook(): Book? {
    return currentBookResolver.book(bookId)
  }

  fun close() {
    scope.cancel()
  }

  @AssistedFactory
  interface Factory {
    fun create(bookId: BookId): BookPlayViewModel
  }
}

private fun SleepTimerState.toViewState(): BookPlayViewState.SleepTimerViewState = when (this) {
  SleepTimerState.Disabled -> BookPlayViewState.SleepTimerViewState.Disabled
  is SleepTimerState.Enabled.WithDuration -> BookPlayViewState.SleepTimerViewState.Enabled.WithDuration(this.leftDuration)
  SleepTimerState.Enabled.WithEndOfChapter -> BookPlayViewState.SleepTimerViewState.Enabled.WithEndOfChapter
}
