package de.clio.features.bookOverview.views.topbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.clio.core.ui.ClioTheme
import de.clio.features.bookOverview.overview.BookOverviewLayoutMode
import de.clio.features.bookOverview.overview.BookOverviewViewState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun BookOverviewTopBar(
  viewState: BookOverviewViewState,
  onBookFolderClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onQueryChange: (String) -> Unit,
) {
  Column {
    BookOverviewSearchBar(
      query = viewState.searchQuery,
      onQueryChange = onQueryChange,
      onBookFolderClick = onBookFolderClick,
      onSettingsClick = onSettingsClick,
      showAddBookHint = viewState.showAddBookHint,
      showFolderPickerIcon = viewState.showFolderPickerIcon,
    )
    var showLoading by remember { mutableStateOf(false) }
    LaunchedEffect(viewState.isLoading) {
      if (viewState.isLoading) {
        delay(3.seconds)
      }
      showLoading = viewState.isLoading
    }
    if (showLoading) {
      LinearProgressIndicator(
        Modifier
          .padding(top = 12.dp)
          .fillMaxWidth(),
      )
    }
  }
}

@Composable
@Preview
private fun BookOverviewTopBarPreview() {
  ClioTheme {
    BookOverviewTopBar(
      viewState = BookOverviewViewState(
        books = emptyMap(),
        layoutMode = BookOverviewLayoutMode.List,
        playButtonState = BookOverviewViewState.PlayButtonState.Paused,
        showAddBookHint = true,
        showSearchIcon = true,
        isLoading = true,
        searchQuery = "",
        showStoragePermissionBugCard = false,
        showFolderPickerIcon = true,
        dialog = null,
      ),
      onBookFolderClick = {},
      onSettingsClick = {},
      onQueryChange = {},
    )
  }
}
