package de.clio.features.folderPicker.addcontent

import android.net.Uri
import de.clio.core.data.folders.AudiobookFolders
import de.clio.core.data.folders.FolderType
import de.clio.features.folderPicker.folderPicker.FileTypeSelection
import de.clio.navigation.Destination
import de.clio.navigation.Destination.OnboardingCompletion
import de.clio.navigation.Navigator
import de.clio.navigation.Origin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class AddContentViewModel(
  private val audiobookFolders: AudiobookFolders,
  private val navigator: Navigator,
  @Assisted
  private val origin: Origin,
) {

  internal fun add(
    uri: Uri,
    type: FileTypeSelection,
  ) {
    val folderType = when (type) {
      FileTypeSelection.File -> FolderType.SingleFile
      FileTypeSelection.Folder -> FolderType.Root
    }
    audiobookFolders.add(uri, folderType)
    when (origin) {
      Origin.Default -> {
        navigator.setRoot(Destination.BookOverview)
      }
      Origin.Onboarding -> {
        navigator.goTo(OnboardingCompletion)
      }
    }
  }

  internal fun back() {
    navigator.goBack()
  }

  @AssistedFactory
  interface Factory {
    fun create(origin: Origin): AddContentViewModel
  }
}
