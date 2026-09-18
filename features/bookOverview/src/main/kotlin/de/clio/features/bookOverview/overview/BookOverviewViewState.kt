package de.clio.features.bookOverview.overview

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import de.clio.core.data.BookId
import de.clio.core.data.BookSortOrder

@Immutable
data class BookOverviewViewState(
  val books: Map<BookOverviewCategory, Map<BookId, State<BookOverviewItemViewState>>>,
  val layoutMode: BookOverviewLayoutMode,
  val playButtonState: PlayButtonState?,
  val showAddBookHint: Boolean,
  val showSearchIcon: Boolean,
  val isLoading: Boolean,
  val searchQuery: String,
  val showStoragePermissionBugCard: Boolean,
  val showFolderPickerIcon: Boolean,
  val dialog: Dialog?,
  val selectedBookIds: Set<BookId> = emptySet(),
  val gridColumnCount: Int = 2,
  val sortOrder: BookSortOrder = BookSortOrder.Default,
) {

  val inSelectionMode: Boolean = selectedBookIds.isNotEmpty()
  val allBookIds: Set<BookId> get() = books.values.flatMap { it.keys }.toSet()

  companion object {
    val Loading = BookOverviewViewState(
      books = mapOf(),
      layoutMode = BookOverviewLayoutMode.List,
      playButtonState = null,
      showAddBookHint = false,
      showSearchIcon = false,
      isLoading = true,
      searchQuery = "",
      showStoragePermissionBugCard = false,
      showFolderPickerIcon = true,
      dialog = null,
      gridColumnCount = 2,
    )
  }

  enum class PlayButtonState {
    Playing,
    Paused,
  }

  enum class Dialog {
    FolderPickerMovedToSettings,
  }
}
