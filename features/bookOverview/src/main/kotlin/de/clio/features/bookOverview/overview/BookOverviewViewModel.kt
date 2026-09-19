package de.clio.features.bookOverview.overview

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import de.clio.core.common.AppInfoProvider
import de.clio.core.common.DispatcherProvider
import de.clio.core.common.MainScope
import de.clio.core.data.Book
import de.clio.core.data.BookId
import de.clio.core.data.BookSortOrder
import de.clio.core.data.GridMode
import de.clio.core.data.KioskModeDemoData
import de.clio.core.data.repo.BookQueueRepository
import de.clio.core.data.repo.BookRepository
import de.clio.core.data.repo.FakeBookQueueRepository
import de.clio.core.data.store.BookSortOrderStore
import de.clio.core.data.store.CurrentBookStore
import de.clio.core.data.store.FolderPickerMovedDialogShownStore
import de.clio.core.data.store.GridColumnCountStore
import de.clio.core.data.store.GridModeStore
import de.clio.core.featureflag.ExperimentalPlaybackPersistenceQualifier
import de.clio.core.featureflag.FeatureFlag
import de.clio.core.featureflag.FolderPickerInSettingsFeatureFlagQualifier
import de.clio.core.featureflag.KioskModeFeatureFlagQualifier
import de.clio.core.playback.LivePlaybackState
import de.clio.core.playback.PlayerController
import de.clio.core.playback.overlay
import de.clio.core.playback.playstate.PlayStateManager
import de.clio.core.scanner.DeviceHasStoragePermissionBug
import de.clio.core.scanner.MediaScanTrigger
import de.clio.core.ui.GridCount
import de.clio.features.bookOverview.di.BookOverviewScope
import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.launch
import kotlin.time.Instant

@SingleIn(BookOverviewScope::class)
@Inject
class BookOverviewViewModel(
  private val repo: BookRepository,
  private val queueRepo: BookQueueRepository = FakeBookQueueRepository(),
  private val mediaScanner: MediaScanTrigger,
  private val playStateManager: PlayStateManager,
  private val playerController: PlayerController,
  @CurrentBookStore
  private val currentBookStoreDataStore: DataStore<BookId?>,
  @FolderPickerMovedDialogShownStore
  private val folderPickerMovedDialogShownStore: DataStore<Boolean>,
  @GridModeStore
  private val gridModeStore: DataStore<GridMode>,
  @GridColumnCountStore
  private val gridColumnCountStore: DataStore<Int>,
  @BookSortOrderStore
  private val bookSortOrderStore: DataStore<BookSortOrder>,
  private val gridCount: GridCount,
  private val navigator: Navigator,
  private val appInfoProvider: AppInfoProvider,
  private val deviceHasStoragePermissionBug: DeviceHasStoragePermissionBug,
  @FolderPickerInSettingsFeatureFlagQualifier
  private val folderPickerInSettingsFeatureFlag: FeatureFlag<Boolean>,
  @ExperimentalPlaybackPersistenceQualifier
  private val experimentalPlaybackPersistenceFeatureFlag: FeatureFlag<Boolean>,
  @KioskModeFeatureFlagQualifier
  private val kioskModeFeatureFlag: FeatureFlag<Boolean>,
  dispatcherProvider: DispatcherProvider,
) {

  private val scope = MainScope(dispatcherProvider)
  private var query by mutableStateOf("")
  private var dialog by mutableStateOf<BookOverviewViewState.Dialog?>(null)
  private var selectedBookIds by mutableStateOf<Set<BookId>>(emptySet())
  private var selectedTab by mutableStateOf(OverviewTab.Books)

  private var lastGridMode: GridMode? = null
  private var lastGridColumnCount: Int = 2
  private var lastBooks: List<Book> = emptyList()
  private var lastCurrentBookId: BookId? = null
  private var lastPlayState = PlayStateManager.PlayState.Paused
  private var lastScannerActive = false
  private var lastFolderPickerMovedDialogShown = false
  private var lastQueueIds: List<BookId> = emptyList()

  init {
    scope.launch {
      gridModeStore.data.collect { lastGridMode = it }
    }
    scope.launch {
      gridColumnCountStore.data.collect { lastGridColumnCount = it }
    }
    scope.launch {
      repo.flow().collect { lastBooks = it }
    }
    scope.launch {
      currentBookStoreDataStore.data.collect { lastCurrentBookId = it }
    }
    scope.launch {
      playStateManager.playStateFlow.collect { lastPlayState = it }
    }
    scope.launch {
      mediaScanner.scannerActive.collect { lastScannerActive = it }
    }
    scope.launch {
      folderPickerMovedDialogShownStore.data.collect { lastFolderPickerMovedDialogShown = it }
    }
    scope.launch {
      queueRepo.queueFlow.collect { lastQueueIds = it }
    }
  }

  fun attach() {
    mediaScanner.scan()
  }

  @Composable
  internal fun state(): BookOverviewViewState {
    val kioskMode = remember { kioskModeFeatureFlag.get() }
    if (kioskMode) return kioskModeState()

    val playState = remember { playStateManager.playStateFlow }
      .collectAsState(initial = lastPlayState).value
    val hasStoragePermissionBug = remember { deviceHasStoragePermissionBug.hasBug }
      .collectAsState().value
    val books = remember { repo.flow() }
      .collectAsState(initial = lastBooks).value
    val currentBookId = remember { currentBookStoreDataStore.data }
      .collectAsState(initial = lastCurrentBookId).value
    val scannerActive = remember { mediaScanner.scannerActive }
      .collectAsState(initial = lastScannerActive).value
    val folderPickerMovedDialogShown = remember { folderPickerMovedDialogShownStore.data }
      .collectAsState(initial = lastFolderPickerMovedDialogShown).value
    val gridMode = remember { gridModeStore.data }
      .collectAsState(initial = lastGridMode).value
      ?: return BookOverviewViewState.Loading
    val gridColumnCount = remember { gridColumnCountStore.data }
      .collectAsState(initial = lastGridColumnCount).value

    val noBooks = !scannerActive && books.isEmpty()

    val layoutMode = when (gridMode) {
      GridMode.LIST -> BookOverviewLayoutMode.List
      GridMode.GRID -> BookOverviewLayoutMode.Grid
      GridMode.FOLLOW_DEVICE -> if (gridCount.useGridAsDefault()) {
        BookOverviewLayoutMode.Grid
      } else {
        BookOverviewLayoutMode.List
      }
    }

    val filteredBooks = if (query.isNotBlank()) {
      val q = query.trim().lowercase()
      books.filter { book ->
        book.content.name.lowercase().contains(q) ||
          (book.content.author?.lowercase()?.contains(q) == true) ||
          (book.content.series?.lowercase()?.contains(q) == true) ||
          (book.content.narrator?.lowercase()?.contains(q) == true)
      }
    } else {
      books
    }

    val experimentalPlaybackPersistence = experimentalPlaybackPersistenceFeatureFlag.get()
    val livePlaybackState: State<LivePlaybackState?> = if (experimentalPlaybackPersistence && currentBookId != null) {
      remember(currentBookId) {
        playerController.livePlaybackStateFlow(currentBookId)
      }.collectAsState(null)
    } else {
      remember { mutableStateOf(null) }
    }

    val sortOrder = remember { bookSortOrderStore.data }
      .collectAsState(initial = BookSortOrder.Default).value

    val queueIds = remember { queueRepo.queueFlow }
      .collectAsState(initial = lastQueueIds).value

    val filteredBookMap = remember(filteredBooks) { filteredBooks.associateBy { it.id } }
    val queueBooks = queueIds.mapNotNull { bookId ->
      filteredBookMap[bookId]?.let { book ->
        val livePlayback = if (book.id == currentBookId) livePlaybackState.value else null
        if (livePlayback != null) {
          book.overlay(livePlayback).toItemViewState()
        } else {
          book.toItemViewState()
        }
      }
    }

    val currentBook = currentBookId?.let { id ->
      filteredBookMap[id] ?: books.firstOrNull { it.id == id }
    }?.let { book ->
      val livePlayback = livePlaybackState.value
      if (livePlayback != null && livePlayback.bookId == book.id) {
        book.overlay(livePlayback).toItemViewState()
      } else {
        book.toItemViewState()
      }
    }

    return BookOverviewViewState(
      layoutMode = layoutMode,
      gridColumnCount = gridColumnCount,
      sortOrder = sortOrder,
      selectedTab = selectedTab,
      queueBooks = queueBooks,
      queueCount = queueIds.size,
      currentBook = currentBook,
      books = filteredBooks
        .groupBy {
          it.category
        }
        .mapValues { (category, books) ->
          books
            .sortedWith(sortOrder)
            .associate { book ->
              book.id to book.itemViewState(
                currentBookId = currentBookId,
                livePlaybackState = { livePlaybackState.value },
              )
            }
        }
        .toSortedMap(),
      playButtonState = if (playState == PlayStateManager.PlayState.Playing) {
        BookOverviewViewState.PlayButtonState.Playing
      } else {
        BookOverviewViewState.PlayButtonState.Paused
      }.takeIf { currentBookId != null },
      showAddBookHint = if (hasStoragePermissionBug) {
        false
      } else {
        noBooks
      },
      showSearchIcon = books.isNotEmpty(),
      isLoading = scannerActive,
      searchQuery = query,
      showStoragePermissionBugCard = hasStoragePermissionBug,
      showFolderPickerIcon = !folderPickerInSettingsFeatureFlag.get() &&
        !folderPickerMovedDialogShown &&
        appInfoProvider.installTime < FolderPickerMigrationInstallTimeCutoff,
      dialog = dialog,
      selectedBookIds = selectedBookIds,
    )
  }

  fun onTabSelected(tab: OverviewTab) {
    selectedTab = tab
    selectedBookIds = emptySet()
  }

  fun onDeleteSelectedFromQueue() {
    val selected = selectedBookIds
    if (selected.isNotEmpty()) {
      scope.launch {
        queueRepo.removeFromQueue(selected)
      }
      onClearSelection()
    }
  }

  fun onReorderQueue(bookIds: List<BookId>) {
    scope.launch {
      queueRepo.reorder(bookIds)
    }
  }

  fun onSortOrderChange(order: BookSortOrder) {
    scope.launch {
      bookSortOrderStore.updateData { order }
    }
  }

  fun onGridColumnCountChange(count: Int) {
    scope.launch {
      gridColumnCountStore.updateData { count.coerceIn(1, 3) }
    }
  }

  private fun kioskModeState(): BookOverviewViewState {
    val demoBooks = KioskModeDemoData.demoAudiobooks
    val filteredBooks = if (query.isNotBlank()) {
      val q = query.trim().lowercase()
      demoBooks.filter {
        it.title.lowercase().contains(q) || it.author.lowercase().contains(q)
      }
    } else {
      demoBooks
    }
    return BookOverviewViewState(
      layoutMode = BookOverviewLayoutMode.List,
      books = mapOf(
        BookOverviewCategory.OVERVIEW to filteredBooks.associate { book ->
          book.id to mutableStateOf(
            BookOverviewItemViewState(
              name = book.title,
              author = book.author,
              cover = book.coverUrl,
              progress = book.progress / 100F,
              id = book.id,
              remainingTime = book.remaining,
            ),
          )
        },
      ),
      playButtonState = BookOverviewViewState.PlayButtonState.Paused,
      showAddBookHint = false,
      showSearchIcon = true,
      isLoading = false,
      searchQuery = query,
      showStoragePermissionBugCard = false,
      showFolderPickerIcon = false,
      dialog = null,
      selectedBookIds = emptySet(),
    )
  }

  fun onSettingsClick() {
    navigator.goTo(Destination.Settings)
  }

  fun onBookClick(id: BookId) {
    if (selectedBookIds.isNotEmpty()) {
      toggleSelection(id)
    } else {
      navigator.goTo(Destination.Playback(id))
    }
  }

  fun onBookLongClick(id: BookId) {
    toggleSelection(id)
  }

  private fun toggleSelection(id: BookId) {
    selectedBookIds = if (id in selectedBookIds) {
      selectedBookIds - id
    } else {
      selectedBookIds + id
    }
  }

  fun onSelectAllClick(allIds: Set<BookId>) {
    selectedBookIds = if (selectedBookIds.size == allIds.size && allIds.isNotEmpty()) {
      emptySet()
    } else {
      allIds
    }
  }

  fun onClearSelection() {
    selectedBookIds = emptySet()
  }

  fun onAddSelectedToQueue() {
    val selected = selectedBookIds.toList()
    if (selected.isNotEmpty()) {
      scope.launch {
        queueRepo.addToQueue(selected)
      }
      onClearSelection()
    }
  }

  fun onBookFolderClick() {
    dialog = BookOverviewViewState.Dialog.FolderPickerMovedToSettings
  }

  fun onFolderPickerMovedDialogDismiss() {
    dialog = null
    scope.launch {
      folderPickerMovedDialogShownStore.updateData { true }
    }
  }

  fun onSearchActiveChange(active: Boolean) {
    if (!active) {
      query = ""
    }
  }

  fun onSearchQueryChange(query: String) {
    this.query = query
  }

  fun playPause() {
    playerController.playPause()
  }

  fun onPermissionBugCardClick() {
    if (Build.VERSION.SDK_INT >= 30) {
      navigator.goTo(
        Destination.Activity(
          Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            .setData("package:com.android.externalstorage".toUri()),
        ),
      )
    }
  }
}

private val FolderPickerMigrationInstallTimeCutoff = Instant.parse("2026-06-17T00:00:00Z")

@Composable
private fun Book.itemViewState(
  currentBookId: BookId?,
  livePlaybackState: () -> LivePlaybackState?,
): State<BookOverviewItemViewState> {
  if (id != currentBookId) {
    return rememberUpdatedState(toItemViewState())
  }
  val currentPlaybackState by rememberUpdatedState(livePlaybackState)
  return remember(this, currentBookId) {
    derivedStateOf {
      val livePlayback = currentPlaybackState()
      if (livePlayback != null) {
        overlay(livePlayback)
      } else {
        this
      }.toItemViewState()
    }
  }
}
