package de.clio.features.folderPicker.folderPicker

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import de.clio.core.data.folders.AudiobookFolders
import de.clio.core.data.folders.FolderType
import de.clio.core.documentfile.nameWithoutExtension
import de.clio.core.featureflag.FeatureFlag
import de.clio.core.featureflag.KioskModeFeatureFlagQualifier
import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import de.clio.navigation.Origin
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Inject
class FolderPickerViewModel(
  private val audiobookFolders: AudiobookFolders,
  private val navigator: Navigator,
  @KioskModeFeatureFlagQualifier
  private val kioskModeFeatureFlag: FeatureFlag<Boolean>,
) {

  @Composable
  fun viewState(): FolderPickerViewState {
    val kioskMode = remember { kioskModeFeatureFlag.get() }
    if (kioskMode) {
      return FolderPickerViewState(
        items = kioskModeItems,
        showActions = false,
      )
    }

    val folders: List<FolderPickerViewState.Item> by remember {
      items()
    }.collectAsState(initial = emptyList())
    return FolderPickerViewState(folders)
  }

  private fun items(): Flow<List<FolderPickerViewState.Item>> {
    return audiobookFolders.all().map { folders ->
      withContext(Dispatchers.IO) {
        folders.flatMap { (folderType, folders) ->
          folders.map { (documentFile, uri) ->
            FolderPickerViewState.Item(
              name = documentFile.nameWithoutExtension(),
              id = uri,
              folderType = folderType,
            )
          }
        }.sortedDescending()
      }
    }
  }

  internal fun onCloseClick() {
    navigator.goBack()
  }

  internal fun add() {
    navigator.goTo(
      Destination.AddContent(
        Origin.Default,
      ),
    )
  }

  fun removeFolder(item: FolderPickerViewState.Item) {
    audiobookFolders.remove(item.id, item.folderType)
  }

  private companion object {
    val kioskModeItems = listOf(
      FolderPickerViewState.Item(
        name = "Audiobooks",
        id = Uri.EMPTY,
        folderType = FolderType.Root,
      ),
      FolderPickerViewState.Item(
        name = "Sci-Fi",
        id = Uri.EMPTY,
        folderType = FolderType.SingleFolder,
      ),
      FolderPickerViewState.Item(
        name = "Non-Fiction",
        id = Uri.EMPTY,
        folderType = FolderType.SingleFolder,
      ),
    )
  }
}
