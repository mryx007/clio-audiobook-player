package de.clio.features.bookOverview.views

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import kotlin.math.roundToInt
import de.clio.core.common.rootGraphAs
import de.clio.core.data.BookId
import de.clio.core.ui.ClioTheme
import de.clio.core.ui.icons.ClioIcons
import de.clio.features.bookOverview.bottomSheet.BottomSheetItem
import de.clio.features.bookOverview.deleteBook.DeleteBookDialog
import de.clio.features.bookOverview.di.BookOverviewGraph
import de.clio.features.bookOverview.editTitle.EditBookTitleDialog
import de.clio.features.bookOverview.overview.BookOverviewCategory
import de.clio.features.bookOverview.overview.BookOverviewItemViewState
import de.clio.features.bookOverview.overview.BookOverviewLayoutMode
import de.clio.features.bookOverview.overview.BookOverviewViewState
import de.clio.features.bookOverview.views.topbar.BookOverviewTopBar
import de.clio.navigation.Destination
import de.clio.navigation.NavEntryProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlin.uuid.Uuid
import de.clio.core.strings.R as StringsR

@ContributesTo(AppScope::class)
interface BookOverviewProvider {

  @Provides
  @IntoSet
  fun bookOverviewNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.BookOverview> { key ->
    NavEntry(key) {
      BookOverviewScreen()
    }
  }
}

@Composable
fun BookOverviewScreen(modifier: Modifier = Modifier) {
  val bookGraph = retain<BookOverviewGraph> {
    rootGraphAs<BookOverviewGraph.Factory.Provider>()
      .bookOverviewGraphProviderFactory.create()
  }
  val bookOverviewViewModel = bookGraph.bookOverviewViewModel
  val editBookTitleViewModel = bookGraph.editBookTitleViewModel
  val bottomSheetViewModel = bookGraph.bottomSheetViewModel
  val deleteBookViewModel = bookGraph.deleteBookViewModel
  val fileCoverViewModel = bookGraph.fileCoverViewModel

  LaunchedEffect(Unit) {
    bookOverviewViewModel.attach()
  }
  val viewState = bookOverviewViewModel.state()

  BackHandler(enabled = viewState.inSelectionMode) {
    bookOverviewViewModel.onClearSelection()
  }

  BackHandler(enabled = !viewState.inSelectionMode && viewState.searchQuery.isNotEmpty()) {
    bookOverviewViewModel.onSearchQueryChange("")
  }

  val getContentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
    onResult = { uri ->
      if (uri != null) {
        fileCoverViewModel.onImagePicked(uri)
      }
    },
  )

  BookOverview(
    viewState = viewState,
    onSettingsClick = bookOverviewViewModel::onSettingsClick,
    onBookClick = bookOverviewViewModel::onBookClick,
    onBookLongClick = bookOverviewViewModel::onBookLongClick,
    onClearSelection = bookOverviewViewModel::onClearSelection,
    onSelectAllClick = { bookOverviewViewModel.onSelectAllClick(viewState.allBookIds) },
    onDeleteSelectedClick = {
      deleteBookViewModel.onDeleteMultiple(
        bookIds = viewState.selectedBookIds,
        onDeleted = bookOverviewViewModel::onClearSelection,
      )
    },
    onBookMoreClick = bottomSheetViewModel::bookSelected,
    selectedBookId = bottomSheetViewModel.selectedBookId,
    menuItems = bottomSheetViewModel.state.value.items,
    onMenuItemClick = { bookId, item ->
      if (item == BottomSheetItem.FileCover) {
        getContentLauncher.launch("image/*")
      }
      bottomSheetViewModel.onItemClick(item)
    },
    onBookFolderClick = bookOverviewViewModel::onBookFolderClick,
    onFolderPickerMovedDialogDismiss = bookOverviewViewModel::onFolderPickerMovedDialogDismiss,
    onSearchQueryChange = bookOverviewViewModel::onSearchQueryChange,
    onPermissionBugCardClick = bookOverviewViewModel::onPermissionBugCardClick,
    onGridColumnCountChange = bookOverviewViewModel::onGridColumnCountChange,
    modifier = modifier,
  )
  val deleteBookViewState = deleteBookViewModel.state.value
  if (deleteBookViewState != null) {
    DeleteBookDialog(
      viewState = deleteBookViewState,
      onDismiss = deleteBookViewModel::onDismiss,
      onConfirmDeletion = deleteBookViewModel::onConfirmDeletion,
    )
  }

  val editBookTitleState = editBookTitleViewModel.state.value
  if (editBookTitleState != null) {
    EditBookTitleDialog(
      onDismissEditTitleClick = editBookTitleViewModel::onDismissEditTitle,
      onConfirmEditTitle = editBookTitleViewModel::onConfirmEditTitle,
      viewState = editBookTitleState,
      onUpdateEditTitle = editBookTitleViewModel::onUpdateEditTitle,
    )
  }
}

@Composable
internal fun BookOverview(
  viewState: BookOverviewViewState,
  onSettingsClick: () -> Unit,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  onClearSelection: () -> Unit,
  onSelectAllClick: () -> Unit,
  onDeleteSelectedClick: () -> Unit,
  onBookMoreClick: (BookId) -> Unit,
  selectedBookId: BookId?,
  menuItems: List<BottomSheetItem>,
  onMenuItemClick: (BookId, BottomSheetItem) -> Unit,
  onBookFolderClick: () -> Unit,
  onFolderPickerMovedDialogDismiss: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onPermissionBugCardClick: () -> Unit,
  modifier: Modifier = Modifier,
  onGridColumnCountChange: (Int) -> Unit = {},
) {
  val density = LocalDensity.current
  val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
  val initialTopBarHeightDp = statusBarTop + 72.dp

  var topBarHeightPx by remember { mutableFloatStateOf(0f) }
  var topBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }

  val topBarHeightDp = remember(topBarHeightPx, density) {
    if (topBarHeightPx > 0f) with(density) { topBarHeightPx.toDp() } else initialTopBarHeightDp
  }

  val nestedScrollConnection = remember {
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y
        val maxOffset = if (topBarHeightPx > 0f) topBarHeightPx else with(density) { initialTopBarHeightDp.toPx() }
        if (maxOffset > 0f) {
          topBarOffsetHeightPx = (topBarOffsetHeightPx + delta).coerceIn(-maxOffset, 0f)
        }
        return Offset.Zero
      }
    }
  }

  LaunchedEffect(viewState.searchQuery.isNotEmpty(), viewState.inSelectionMode) {
    if (viewState.searchQuery.isNotEmpty() || viewState.inSelectionMode) {
      topBarOffsetHeightPx = 0f
    }
  }

  Scaffold(
    modifier = modifier,
    bottomBar = {
      Spacer(
        Modifier
          .fillMaxWidth()
          .windowInsetsBottomHeight(WindowInsets.navigationBars)
          .background(MaterialTheme.colorScheme.surface),
      )
    },
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
  ) { contentPadding ->
    Box(
      Modifier
        .fillMaxSize()
        .padding(contentPadding)
        .consumeWindowInsets(contentPadding),
    ) {
      @Composable
      fun TopBarContent() {
        BookOverviewTopBar(
          viewState = viewState,
          onBookFolderClick = onBookFolderClick,
          onSettingsClick = onSettingsClick,
          onQueryChange = onSearchQueryChange,
          onGridColumnCountChange = onGridColumnCountChange,
        )
      }

      val hasBooks = viewState.books.values.any { it.isNotEmpty() }
      if (!hasBooks && viewState.searchQuery.isNotBlank()) {
        Column(modifier = Modifier.fillMaxSize()) {
          TopBarContent()
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 32.dp, vertical = 64.dp),
            contentAlignment = Alignment.TopCenter,
          ) {
            Text(
              text = stringResource(StringsR.string.search_no_results, viewState.searchQuery),
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        ) {
          val listContentPadding = PaddingValues(
            top = topBarHeightDp + 4.dp,
            start = 12.dp,
            end = 12.dp,
            bottom = 16.dp,
          )
          when (viewState.layoutMode) {
            BookOverviewLayoutMode.List -> {
              ListBooks(
                books = viewState.books,
                onBookClick = onBookClick,
                onBookLongClick = onBookLongClick,
                selectedBookIds = viewState.selectedBookIds,
                allBookIds = viewState.allBookIds,
                inSelectionMode = viewState.inSelectionMode,
                onSelectAllClick = onSelectAllClick,
                onDeleteSelectedClick = onDeleteSelectedClick,
                onBookMoreClick = onBookMoreClick,
                selectedBookId = selectedBookId,
                menuItems = menuItems,
                onMenuItemClick = onMenuItemClick,
                showPermissionBugCard = viewState.showStoragePermissionBugCard,
                onPermissionBugCardClick = onPermissionBugCardClick,
                contentPadding = listContentPadding,
              )
            }
            BookOverviewLayoutMode.Grid -> {
              GridBooks(
                books = viewState.books,
                gridColumnCount = viewState.gridColumnCount,
                onBookClick = onBookClick,
                onBookLongClick = onBookLongClick,
                selectedBookIds = viewState.selectedBookIds,
                allBookIds = viewState.allBookIds,
                inSelectionMode = viewState.inSelectionMode,
                onSelectAllClick = onSelectAllClick,
                onDeleteSelectedClick = onDeleteSelectedClick,
                onBookMoreClick = onBookMoreClick,
                selectedBookId = selectedBookId,
                menuItems = menuItems,
                onMenuItemClick = onMenuItemClick,
                showPermissionBugCard = viewState.showStoragePermissionBugCard,
                onPermissionBugCardClick = onPermissionBugCardClick,
                contentPadding = listContentPadding,
              )
            }
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.TopCenter)
              .offset { IntOffset(0, topBarOffsetHeightPx.roundToInt()) }
              .onSizeChanged { topBarHeightPx = it.height.toFloat() },
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              TopBarContent()
              Spacer(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(16.dp)
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color.Transparent,
                      ),
                    ),
                  ),
              )
            }
          }
        }
      }
    }
  }
  Dialog(
    dialog = viewState.dialog,
    onFolderPickerMovedDialogDismiss = onFolderPickerMovedDialogDismiss,
  )
}

@Composable
private fun Dialog(
  dialog: BookOverviewViewState.Dialog?,
  onFolderPickerMovedDialogDismiss: () -> Unit,
) {
  when (dialog) {
    BookOverviewViewState.Dialog.FolderPickerMovedToSettings -> {
      AlertDialog(
        onDismissRequest = onFolderPickerMovedDialogDismiss,
        icon = {
          Row {
            Icon(imageVector = ClioIcons.ArrowForward, contentDescription = null)
            Icon(imageVector = ClioIcons.Settings, contentDescription = null)
            Icon(imageVector = ClioIcons.ArrowBack, contentDescription = null)
          }
        },
        title = {
          Text(stringResource(StringsR.string.library_folders_moved_dialog_title))
        },
        text = {
          Text(stringResource(StringsR.string.library_folders_moved_dialog_message))
        },
        confirmButton = {
          TextButton(onClick = onFolderPickerMovedDialogDismiss) {
            Text(stringResource(StringsR.string.common_dialog_ok))
          }
        },
      )
    }
    null -> Unit
  }
}

@Suppress("ktlint:compose:preview-public-check")
@Preview
@Composable
fun BookOverviewPreview(
  @PreviewParameter(BookOverviewPreviewParameterProvider::class)
  viewState: BookOverviewViewState,
) {
  ClioTheme {
    BookOverview(
      viewState = viewState,
      onSettingsClick = {},
      onBookClick = {},
      onBookLongClick = {},
      onClearSelection = {},
      onSelectAllClick = {},
      onDeleteSelectedClick = {},
      onBookMoreClick = {},
      selectedBookId = null,
      menuItems = emptyList(),
      onMenuItemClick = { _, _ -> },
      onBookFolderClick = {},
      onFolderPickerMovedDialogDismiss = {},
      onSearchQueryChange = {},
      onPermissionBugCardClick = {},
    )
  }
}

internal class BookOverviewPreviewParameterProvider : PreviewParameterProvider<BookOverviewViewState> {

  fun book(): BookOverviewItemViewState {
    return BookOverviewItemViewState(
      name = "Book",
      author = "Author",
      cover = null,
      progress = 0.8F,
      id = BookId(Uuid.random().toString()),
      remainingTime = "01:04",
    )
  }

  override val values = sequenceOf(
    BookOverviewViewState(
      books = mapOf(
        BookOverviewCategory.OVERVIEW to buildMap {
          repeat(10) {
            put(
              BookId(Uuid.random().toString()),
              mutableStateOf(book()),
            )
          }
        },
      ),
      layoutMode = BookOverviewLayoutMode.List,
      playButtonState = BookOverviewViewState.PlayButtonState.Paused,
      showAddBookHint = false,
      showSearchIcon = true,
      isLoading = true,
      searchQuery = "",
      showStoragePermissionBugCard = false,
      showFolderPickerIcon = true,
      dialog = null,
    ),
  )
}
