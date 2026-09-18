package de.clio.features.bookOverview.views.topbar

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import de.clio.features.bookOverview.views.BookFolderIcon
import de.clio.features.bookOverview.views.GridColumnsIcon
import de.clio.features.bookOverview.views.SettingsIcon

@Composable
internal fun TopBarTrailingIcon(
  showAddBookHint: Boolean,
  showFolderPickerIcon: Boolean,
  showGridColumnsIcon: Boolean,
  gridColumnCount: Int,
  onGridColumnCountChange: (Int) -> Unit,
  onBookFolderClick: () -> Unit,
  onSettingsClick: () -> Unit,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    if (showGridColumnsIcon) {
      GridColumnsIcon(
        gridColumnCount = gridColumnCount,
        onGridColumnCountChange = onGridColumnCountChange,
      )
    }
    if (showFolderPickerIcon) {
      BookFolderIcon(withHint = showAddBookHint, onClick = onBookFolderClick)
    }
    SettingsIcon(onSettingsClick)
  }
}
