package de.clio.features.folderPicker

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import de.clio.core.data.folders.FolderType
import de.clio.core.ui.icons.ClioIcons
import de.clio.core.strings.R as StringsR

@Composable
internal fun FolderTypeIcon(folderType: FolderType) {
  Icon(
    imageVector = folderType.icon(),
    contentDescription = folderType.contentDescription(),
  )
}

private fun FolderType.icon(): ImageVector = when (this) {
  FolderType.SingleFile -> ClioIcons.AudioFile
  FolderType.SingleFolder -> ClioIcons.Folder
  FolderType.Root -> ClioIcons.LibraryBooks
  FolderType.Author -> ClioIcons.Person
}

@Composable
private fun FolderType.contentDescription(): String {
  val res = when (this) {
    FolderType.SingleFile,
    FolderType.SingleFolder,
    -> StringsR.string.folder_mode_single_title
    FolderType.Root -> StringsR.string.folder_mode_root_title
    FolderType.Author -> StringsR.string.folder_mode_author_title
  }
  return stringResource(res)
}
