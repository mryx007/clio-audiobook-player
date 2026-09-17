package de.clio.core.data.folders

import android.net.Uri
import de.clio.core.documentfile.CachedDocumentFile
import kotlinx.coroutines.flow.Flow

public interface AudiobookFolders {
  public fun all(): Flow<Map<FolderType, List<DocumentFileWithUri>>>

  public fun add(
    uri: Uri,
    type: FolderType,
  )

  public fun remove(
    uri: Uri,
    folderType: FolderType,
  )

  public suspend fun hasAnyFolders(): Boolean
}

public data class DocumentFileWithUri(
  val documentFile: CachedDocumentFile,
  val uri: Uri,
)
