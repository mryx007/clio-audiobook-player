package de.clio.features.playbackScreen

import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import de.clio.core.common.DispatcherProvider
import de.clio.core.data.BackButtonBehavior
import de.clio.core.data.Book
import de.clio.core.data.BookContent
import de.clio.core.data.BookId
import de.clio.core.data.Bookmark
import de.clio.core.data.Chapter
import de.clio.core.data.ChapterId
import de.clio.core.data.EndOfBookBehavior
import de.clio.core.data.KioskModeDemoData
import de.clio.core.data.MarkData
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.data.PlayerButtonVisibility
import de.clio.core.data.repo.BookQueueRepository
import de.clio.core.data.repo.FakeBookQueueRepository
import de.clio.core.data.sleeptimer.SleepTimerPreference
import de.clio.core.featureflag.MemoryFeatureFlag
import de.clio.core.playback.CurrentBookResolver
import de.clio.core.playback.LivePlaybackState
import de.clio.core.playback.PlayerController
import de.clio.core.playback.overlay
import de.clio.core.playback.playstate.PlayStateManager
import de.clio.core.sleeptimer.SleepTimer
import de.clio.core.sleeptimer.SleepTimerMode
import de.clio.core.sleeptimer.SleepTimerMode.TimedWithDuration
import de.clio.core.sleeptimer.SleepTimerState
import de.clio.features.sleepTimer.SleepTimerViewState
import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class BookPlayViewModelTest {

  private val scope = TestScope()
  private val sleepTimerDataStore = MemoryDataStore(SleepTimerPreference.Default.copy(duration = 5.minutes))
  private val book = book()
  private val sleepTimer = mockk<SleepTimer> {
    val stateFlow = MutableStateFlow<SleepTimerState>(SleepTimerState.Disabled)
    every {
      state
    } returns stateFlow
    every {
      enable(any())
    } answers {
      stateFlow.value = when (val mode = firstArg<SleepTimerMode>()) {
        is TimedWithDuration -> SleepTimerState.Enabled.WithDuration(mode.duration)
        SleepTimerMode.TimedWithDefault -> SleepTimerState.Enabled.WithDuration(runBlocking { sleepTimerDataStore.data.first() }.duration)
        SleepTimerMode.EndOfChapter -> SleepTimerState.Enabled.WithEndOfChapter
      }
    }
    every {
      disable()
    } answers {
      stateFlow.value = SleepTimerState.Disabled
    }
  }

  private val player = mockk<PlayerController>()
  private val playStateManager = mockk<PlayStateManager> {
    every { playStateFlow } returns MutableStateFlow(PlayStateManager.PlayState.Paused)
  }
  private val currentBookStoreId = MemoryDataStore<BookId?>(null)
  private val currentBookResolver = mockk<CurrentBookResolver> {
    coEvery { book(book.id) } returns book
  }
  private val playerLockedStore = MemoryDataStore(false)
  private val backButtonBehaviorStore = MemoryDataStore(BackButtonBehavior.BookOverview)
  private val endOfBookBehaviorStore = MemoryDataStore(EndOfBookBehavior.DoNothing)
  private val navigator = mockk<de.clio.navigation.Navigator>(relaxed = true)
  private val viewModel = BookPlayViewModel(
    bookRepository = mockk {
      coEvery { get(book.id) } returns book
      every { flow(book.id) } returns MutableStateFlow(book)
      every { getCached(any()) } returns null
    },
    currentBookResolver = currentBookResolver,
    player = player.apply {
      every { pauseIfCurrentBookDifferentFrom(book.id) } just Runs
      every { playbackEndedFlow() } returns emptyFlow()
    },
    sleepTimer = sleepTimer,
    playStateManager = playStateManager,
    currentBookStoreId = currentBookStoreId,
    navigator = navigator,
    bookmarkRepository = mockk {
      coEvery { addBookmarkAtBookPosition(book, any(), any()) } returns Bookmark(
        bookId = book.id,
        chapterId = book.currentChapter.id,
        addedAt = Instant.now(),
        setBySleepTimer = true,
        id = Bookmark.Id(Uuid.random()),
        time = 0L,
        title = null,
      )
    },
    volumeGainFormatter = mockk(),
    batteryOptimization = mockk(),
    rewindTimeStore = MemoryDataStore(20),
    fastForwardTimeStore = MemoryDataStore(30),
    playbackBackgroundStyleStore = MemoryDataStore(PlaybackBackgroundStyle.Solid),
    sleepTimerPreferenceStore = sleepTimerDataStore,
    playerButtonVisibilityStore = MemoryDataStore(PlayerButtonVisibility()),
    playerLockedStore = playerLockedStore,
    backButtonBehaviorStore = backButtonBehaviorStore,
    endOfBookBehaviorStore = endOfBookBehaviorStore,
    bookId = book.id,
    dispatcherProvider = DispatcherProvider(scope.coroutineContext, scope.coroutineContext, scope.coroutineContext),
    experimentalPlaybackPersistenceFeatureFlag = MemoryFeatureFlag(false),
    kioskModeFeatureFlag = MemoryFeatureFlag(false),
  )

  @Test
  fun sleepTimerValueChanging() = scope.runTest {
    fun assertDialogSleepTime(expected: Int) {
      assertEquals(expected = BookPlayDialogViewState.SleepTimer(SleepTimerViewState(expected)), actual = viewModel.dialogState.value)
    }

    viewModel.toggleSleepTimer()
    yield()
    assertDialogSleepTime(5)

    suspend fun incrementAndAssert(time: Int) {
      viewModel.incrementSleepTime()
      yield()
      assertDialogSleepTime(time)
    }

    suspend fun decrementAndAssert(time: Int) {
      viewModel.decrementSleepTime()
      yield()
      assertDialogSleepTime(time)
    }

    decrementAndAssert(4)
    decrementAndAssert(3)
    decrementAndAssert(2)
    decrementAndAssert(1)

    decrementAndAssert(1)

    incrementAndAssert(2)
    incrementAndAssert(3)
  }

  @Test
  fun sleepTimerSettingFixedValue() = scope.runTest {
    viewModel.toggleSleepTimer()
    viewModel.onAcceptSleepTime(10)
    assertEquals(expected = 5.minutes, actual = sleepTimerDataStore.data.first().duration)
    yield()
    verify(exactly = 1) {
      sleepTimer.enable(TimedWithDuration(10.minutes))
    }
  }

  @Test
  fun deactivateSleepTimer() = scope.runTest {
    viewModel.toggleSleepTimer()
    viewModel.onAcceptSleepTime(10)
    viewModel.toggleSleepTimer()
    yield()
    verifyOrder {
      sleepTimer.enable(TimedWithDuration(10.minutes))
      sleepTimer.disable()
    }
    assertIs<SleepTimerState.Disabled>(sleepTimer.state.value)
  }

  @Test
  fun onCurrentChapterClickShowsDialogWithCorrectState() = scope.runTest {
    viewModel.onCurrentChapterClick()
    yield()

    val dialogState = assertIs<BookPlayDialogViewState.SelectChapterDialog>(viewModel.dialogState.value)

    assertEquals(
      expected = listOf(
        BookPlayDialogViewState.SelectChapterDialog.ItemViewState(
          number = 1,
          name = "Chapter Start",
          active = false,
          time = "0:00",
        ),
        BookPlayDialogViewState.SelectChapterDialog.ItemViewState(
          number = 2,
          name = "Middle Section",
          active = false,
          time = "2:00",
        ),
        BookPlayDialogViewState.SelectChapterDialog.ItemViewState(
          number = 3,
          name = "Final Section",
          active = false,
          time = "4:00",
        ),
        BookPlayDialogViewState.SelectChapterDialog.ItemViewState(
          number = 4,
          name = "Chapter Start",
          active = false,
          time = "5:00",
        ),
        BookPlayDialogViewState.SelectChapterDialog.ItemViewState(
          number = 5,
          name = "Middle Section",
          active = true,
          time = "7:00",
        ),
        BookPlayDialogViewState.SelectChapterDialog.ItemViewState(
          number = 6,
          name = "Final Section",
          active = false,
          time = "9:00",
        ),
      ),
      actual = dialogState.items,
    )
  }

  @Test
  fun onChapterClickSetsPositionAndDismissesDialog() = scope.runTest {
    every { player.setPosition(any(), any()) } just Runs

    viewModel.onCurrentChapterClick()
    yield()

    assertIs<BookPlayDialogViewState.SelectChapterDialog>(viewModel.dialogState.value)

    viewModel.onChapterClick(number = 2)
    yield()

    // Verify player.setPosition was called with correct parameters
    // The second mark starts at 2 minutes position in the first chapter
    verify(exactly = 1) {
      player.setPosition(time = 2.minutes.inWholeMilliseconds, id = book.chapters.first().id)
    }

    assertEquals(expected = null, actual = viewModel.dialogState.value)
  }

  @Test
  fun `overlay prefers live controller position`() {
    val persistedBook = book()
    val overlaidBook = persistedBook.overlay(
      LivePlaybackState(
        bookId = persistedBook.id,
        chapterId = persistedBook.chapters.first().id,
        positionMs = 1.minutes.inWholeMilliseconds,
        isPlaying = true,
        playbackSpeed = 1F,
      ),
    )

    assertEquals(expected = persistedBook.chapters.first().id, actual = overlaidBook.currentChapter.id)
    assertEquals(expected = 1.minutes.inWholeMilliseconds, actual = overlaidBook.content.positionInChapter)
  }

  @Test
  fun `viewState prefers live playback state when feature flag is enabled`() = scope.runTest {
    val persistedBook = book()
    val livePlaybackFlow = MutableStateFlow<LivePlaybackState?>(null)
    val viewModel = viewModel(
      book = persistedBook,
      experimentalPlaybackPersistence = true,
      livePlaybackFlow = livePlaybackFlow,
    )

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = null, actual = awaitItem())
      assertEquals(expected = 30.seconds, actual = awaitItem()!!.playedTime)

      livePlaybackFlow.value = LivePlaybackState(
        bookId = persistedBook.id,
        chapterId = persistedBook.chapters.first().id,
        positionMs = 1.minutes.inWholeMilliseconds,
        isPlaying = true,
        playbackSpeed = 1F,
      )

      val state = awaitItem()!!
      assertEquals(expected = true, actual = state.playing)
      assertEquals(expected = "Chapter Start", actual = state.chapterName)
      assertEquals(expected = 1.minutes, actual = state.playedTime)
    }
  }

  @Test
  fun `viewState falls back to manager play state when live playback is unavailable`() = scope.runTest {
    val viewModel = viewModel(
      experimentalPlaybackPersistence = true,
      livePlaybackFlow = MutableStateFlow(null),
      playStateFlow = MutableStateFlow(PlayStateManager.PlayState.Playing),
    )

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = null, actual = awaitItem())
      val state = awaitItem()!!
      assertEquals(expected = true, actual = state.playing)
      assertEquals(expected = 30.seconds, actual = state.playedTime)
    }
  }

  @Test
  fun `viewState uses currently playing demo book in kiosk mode`() = scope.runTest {
    val viewModel = viewModel(kioskMode = true)

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      val state = awaitItem()!!
      assertEquals(expected = KioskModeDemoData.currentlyPlaying.title, actual = state.title)
      assertEquals(expected = KioskModeDemoData.currentlyPlaying.chapter, actual = state.chapterName)
      assertEquals(expected = KioskModeDemoData.currentlyPlaying.coverUrl, actual = state.cover)
    }
  }

  @Test
  fun toggleLockPersistsState() = scope.runTest {
    assertEquals(expected = false, actual = playerLockedStore.data.first())
    viewModel.toggleLock()
    yield()
    assertEquals(expected = true, actual = playerLockedStore.data.first())
    viewModel.toggleLock()
    yield()
    assertEquals(expected = false, actual = playerLockedStore.data.first())
  }

  @Test
  fun onCloseClickAlwaysNavigatesBack() = scope.runTest {
    backButtonBehaviorStore.updateData { BackButtonBehavior.MinimizeApp }
    yield()
    viewModel.onCloseClick()
    yield()
    verify(exactly = 1) { navigator.goBack() }
    verify(exactly = 0) { navigator.minimizeApp() }
  }

  @Test
  fun onSystemBackClickNavigatesBackWhenOverviewConfigured() = scope.runTest {
    backButtonBehaviorStore.updateData { BackButtonBehavior.BookOverview }
    yield()
    viewModel.onSystemBackClick()
    yield()
    verify(exactly = 1) { navigator.goBack() }
    verify(exactly = 0) { navigator.minimizeApp() }
  }

  @Test
  fun onSystemBackClickMinimizesAppWhenMinimizeAppConfigured() = scope.runTest {
    backButtonBehaviorStore.updateData { BackButtonBehavior.MinimizeApp }
    yield()
    viewModel.onSystemBackClick()
    yield()
    verify(exactly = 1) { navigator.minimizeApp() }
  }

  @Test
  fun whenPlaybackEndsAndEndOfBookBehaviorIsBookOverviewThenNavigatorGoesBack() = scope.runTest {
    val localPlayer = mockk<PlayerController> {
      every { pauseIfCurrentBookDifferentFrom(book.id) } just Runs
      every { playbackEndedFlow() } returns flowOf(book.id)
    }
    val localEndOfBookBehaviorStore = MemoryDataStore(EndOfBookBehavior.BookOverview)
    val localNavigator = mockk<de.clio.navigation.Navigator>(relaxed = true)

    val localViewModel = BookPlayViewModel(
      bookRepository = mockk {
        coEvery { get(book.id) } returns book
        every { flow(book.id) } returns MutableStateFlow(book)
      },
      currentBookResolver = currentBookResolver,
      player = localPlayer,
      sleepTimer = sleepTimer,
      playStateManager = playStateManager,
      currentBookStoreId = currentBookStoreId,
      navigator = localNavigator,
      bookmarkRepository = mockk(),
      volumeGainFormatter = mockk(),
      batteryOptimization = mockk(),
      rewindTimeStore = MemoryDataStore(20),
      fastForwardTimeStore = MemoryDataStore(30),
      playbackBackgroundStyleStore = MemoryDataStore(PlaybackBackgroundStyle.Solid),
      sleepTimerPreferenceStore = sleepTimerDataStore,
      playerButtonVisibilityStore = MemoryDataStore(PlayerButtonVisibility()),
      playerLockedStore = playerLockedStore,
      backButtonBehaviorStore = backButtonBehaviorStore,
      endOfBookBehaviorStore = localEndOfBookBehaviorStore,
      bookId = book.id,
      dispatcherProvider = DispatcherProvider(scope.coroutineContext, scope.coroutineContext, scope.coroutineContext),
      experimentalPlaybackPersistenceFeatureFlag = MemoryFeatureFlag(false),
      kioskModeFeatureFlag = MemoryFeatureFlag(false),
    )
    assertIs<BookPlayViewModel>(localViewModel)

    yield()
    verify(exactly = 1) { localNavigator.goBack() }
  }

  @Test
  fun whenPlaybackEndsAndEndOfBookBehaviorIsDoNothingThenNavigatorDoesNotGoBack() = scope.runTest {
    val localPlayer = mockk<PlayerController> {
      every { pauseIfCurrentBookDifferentFrom(book.id) } just Runs
      every { playbackEndedFlow() } returns flowOf(book.id)
    }
    val localEndOfBookBehaviorStore = MemoryDataStore(EndOfBookBehavior.DoNothing)
    val localNavigator = mockk<de.clio.navigation.Navigator>(relaxed = true)

    val localViewModel = BookPlayViewModel(
      bookRepository = mockk {
        coEvery { get(book.id) } returns book
        every { flow(book.id) } returns MutableStateFlow(book)
      },
      currentBookResolver = currentBookResolver,
      player = localPlayer,
      sleepTimer = sleepTimer,
      playStateManager = playStateManager,
      currentBookStoreId = currentBookStoreId,
      navigator = localNavigator,
      bookmarkRepository = mockk(),
      volumeGainFormatter = mockk(),
      batteryOptimization = mockk(),
      rewindTimeStore = MemoryDataStore(20),
      fastForwardTimeStore = MemoryDataStore(30),
      playbackBackgroundStyleStore = MemoryDataStore(PlaybackBackgroundStyle.Solid),
      sleepTimerPreferenceStore = sleepTimerDataStore,
      playerButtonVisibilityStore = MemoryDataStore(PlayerButtonVisibility()),
      playerLockedStore = playerLockedStore,
      backButtonBehaviorStore = backButtonBehaviorStore,
      endOfBookBehaviorStore = localEndOfBookBehaviorStore,
      bookId = book.id,
      dispatcherProvider = DispatcherProvider(scope.coroutineContext, scope.coroutineContext, scope.coroutineContext),
      experimentalPlaybackPersistenceFeatureFlag = MemoryFeatureFlag(false),
      kioskModeFeatureFlag = MemoryFeatureFlag(false),
    )
    assertIs<BookPlayViewModel>(localViewModel)

    yield()
    verify(exactly = 0) { localNavigator.goBack() }
  }

  @Test
  fun `when playback ends and continue queue is selected, plays next book and replaces screen`() = scope.runTest {
    val nextBookId = BookId("next-book-id")
    val queueRepository = FakeBookQueueRepository(listOf(nextBookId))
    val localNavigator = mockk<Navigator>(relaxed = true)
    val localPlayer = mockk<PlayerController>(relaxed = true) {
      every { playbackEndedFlow() } returns flowOf(book.id)
    }

    val localViewModel = viewModel(
      player = localPlayer,
      navigator = localNavigator,
      queueRepository = queueRepository,
      endOfBookBehavior = EndOfBookBehavior.ContinueQueue,
    )
    assertIs<BookPlayViewModel>(localViewModel)

    yield()

    verify {
      localPlayer.play()
      localNavigator.replace(Destination.Playback(nextBookId))
    }
  }

  @Test
  fun `when playback ends and do nothing is selected, leaves queued book untouched`() = scope.runTest {
    val nextBookId = BookId("next-book-id")
    val queueRepository = FakeBookQueueRepository(listOf(nextBookId))
    val localNavigator = mockk<Navigator>(relaxed = true)
    val localPlayer = mockk<PlayerController>(relaxed = true) {
      every { playbackEndedFlow() } returns flowOf(book.id)
    }

    val localViewModel = viewModel(
      player = localPlayer,
      navigator = localNavigator,
      queueRepository = queueRepository,
    )
    assertIs<BookPlayViewModel>(localViewModel)

    yield()

    verify(exactly = 0) {
      localPlayer.play()
      localNavigator.replace(any())
    }
    assertEquals(expected = nextBookId, actual = queueRepository.popNext())
  }

  @Test
  fun `when closed, ignores later playback ended events`() = scope.runTest {
    val playbackEnded = MutableSharedFlow<BookId>()
    val localNavigator = mockk<Navigator>(relaxed = true)
    val localPlayer = mockk<PlayerController>(relaxed = true) {
      every { playbackEndedFlow() } returns playbackEnded
    }
    val localViewModel = viewModel(
      player = localPlayer,
      navigator = localNavigator,
      endOfBookBehavior = EndOfBookBehavior.BookOverview,
      dispatcherProvider = DispatcherProvider(
        StandardTestDispatcher(testScheduler),
        StandardTestDispatcher(testScheduler),
        StandardTestDispatcher(testScheduler),
      ),
    )
    assertIs<BookPlayViewModel>(localViewModel)

    runCurrent()
    localViewModel.close()
    playbackEnded.emit(book.id)
    runCurrent()

    verify(exactly = 0) { localNavigator.goBack() }
  }

  private fun viewModel(
    book: Book = this.book,
    livePlaybackFlow: MutableStateFlow<LivePlaybackState?> = MutableStateFlow(null),
    player: PlayerController = mockk {
      every { pauseIfCurrentBookDifferentFrom(book.id) } just Runs
      every { livePlaybackStateFlow(book.id) } returns livePlaybackFlow
      every { playbackEndedFlow() } returns emptyFlow()
    },
    navigator: Navigator = mockk(relaxed = true),
    queueRepository: BookQueueRepository = FakeBookQueueRepository(),
    endOfBookBehavior: EndOfBookBehavior = EndOfBookBehavior.DoNothing,
    experimentalPlaybackPersistence: Boolean = false,
    kioskMode: Boolean = false,
    playStateFlow: MutableStateFlow<PlayStateManager.PlayState> = MutableStateFlow(PlayStateManager.PlayState.Paused),
    dispatcherProvider: DispatcherProvider = DispatcherProvider(scope.coroutineContext, scope.coroutineContext, scope.coroutineContext),
  ): BookPlayViewModel {
    return BookPlayViewModel(
      bookRepository = mockk {
        coEvery { get(book.id) } returns book
        every { flow(book.id) } returns MutableStateFlow(book)
        every { getCached(any()) } returns null
      },
      currentBookResolver = currentBookResolver,
      player = player,
      sleepTimer = sleepTimer,
      playStateManager = mockk {
        every { this@mockk.playStateFlow } returns playStateFlow
        every { playState } returns playStateFlow.value
      },
      currentBookStoreId = MemoryDataStore(null),
      navigator = navigator,
      bookmarkRepository = mockk(),
      volumeGainFormatter = mockk(),
      batteryOptimization = mockk(),
      rewindTimeStore = MemoryDataStore(20),
      fastForwardTimeStore = MemoryDataStore(30),
      playbackBackgroundStyleStore = MemoryDataStore(PlaybackBackgroundStyle.Solid),
      sleepTimerPreferenceStore = sleepTimerDataStore,
      playerButtonVisibilityStore = MemoryDataStore(PlayerButtonVisibility()),
      playerLockedStore = playerLockedStore,
      backButtonBehaviorStore = backButtonBehaviorStore,
      endOfBookBehaviorStore = MemoryDataStore(endOfBookBehavior),
      queueRepository = queueRepository,
      bookId = book.id,
      dispatcherProvider = dispatcherProvider,
      experimentalPlaybackPersistenceFeatureFlag = MemoryFeatureFlag(experimentalPlaybackPersistence),
      kioskModeFeatureFlag = MemoryFeatureFlag(kioskMode),
    )
  }
}

private fun book(
  name: String = "TestBook",
  lastPlayedAtMillis: Long = 0L,
  addedAtMillis: Long = 0L,
): Book {
  val chapters = listOf(
    chapter(),
    chapter(),
  )
  return Book(
    content = BookContent(
      author = Uuid.random().toString(),
      name = name,
      positionInChapter = 2.5.minutes.inWholeMilliseconds,
      playbackSpeed = 1F,
      addedAt = Instant.ofEpochMilli(addedAtMillis),
      chapters = chapters.map { it.id },
      cover = null,
      currentChapter = chapters[1].id,
      isActive = true,
      lastPlayedAt = Instant.ofEpochMilli(lastPlayedAtMillis),
      skipSilence = false,
      id = BookId(Uuid.random().toString()),
      gain = 0F,
      genre = null,
      narrator = null,
      series = null,
      part = null,
    ),
    chapters = chapters,
  )
}

private fun chapter(): Chapter {
  return Chapter(
    id = ChapterId("http://${Uuid.random()}"),
    duration = 5.minutes.inWholeMilliseconds,
    fileLastModified = Instant.EPOCH,
    markData = listOf(
      MarkData(startMs = 0L, name = "Chapter Start"),
      MarkData(startMs = 2.minutes.inWholeMilliseconds, name = "Middle Section"),
      MarkData(startMs = 4.minutes.inWholeMilliseconds, name = "Final Section"),
    ),
    name = "name",
    fileSize = 0,
  )
}
