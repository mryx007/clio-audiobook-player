package de.clio.features.bookOverview.views.topbar

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import de.clio.features.bookOverview.views.BookFolderIcon
import de.clio.features.bookOverview.views.SettingsIcon

@Composable
internal fun TopBarTrailingIcon(
  showAddBookHint: Boolean,
  showFolderPickerIcon: Boolean,
  onBookFolderClick: () -> Unit,
  onSettingsClick: () -> Unit,
) {
  Row {
    if (showFolderPickerIcon) {
      BookFolderIcon(withHint = showAddBookHint, onClick = onBookFolderClick)
    }
    SettingsIcon(onSettingsClick)
  }
}
