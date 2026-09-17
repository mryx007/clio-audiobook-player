package voice.features.bookOverview.views.topbar

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import voice.features.bookOverview.views.BookFolderIcon
import voice.features.bookOverview.views.SettingsIcon

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

