package de.clio.features.folderPicker.addcontent

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import de.clio.core.data.folders.AudiobookFolders
import de.clio.core.data.folders.FolderType
import de.clio.features.folderPicker.folderPicker.FileTypeSelection
import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import de.clio.navigation.Origin

@RunWith(AndroidJUnit4::class)
class AddContentViewModelTest {

  private val audiobookFolders = mockk<AudiobookFolders>(relaxed = true)
  private val navigator = mockk<Navigator>(relaxed = true)

  @Test
  fun `add folder directly uses FolderType Root and completes onboarding`() {
    val viewModel = AddContentViewModel(audiobookFolders, navigator, Origin.Onboarding)
    val uri = mockk<Uri>()
    viewModel.add(uri, FileTypeSelection.Folder)

    verify { audiobookFolders.add(uri, FolderType.Root) }
    verify { navigator.goTo(Destination.OnboardingCompletion) }
  }

  @Test
  fun `add folder directly uses FolderType Root and sets book overview on default origin`() {
    val viewModel = AddContentViewModel(audiobookFolders, navigator, Origin.Default)
    val uri = mockk<Uri>()
    viewModel.add(uri, FileTypeSelection.Folder)

    verify { audiobookFolders.add(uri, FolderType.Root) }
    verify { navigator.setRoot(Destination.BookOverview) }
  }

  @Test
  fun `add file uses FolderType SingleFile`() {
    val viewModel = AddContentViewModel(audiobookFolders, navigator, Origin.Onboarding)
    val uri = mockk<Uri>()
    viewModel.add(uri, FileTypeSelection.File)

    verify { audiobookFolders.add(uri, FolderType.SingleFile) }
    verify { navigator.goTo(Destination.OnboardingCompletion) }
  }
}
