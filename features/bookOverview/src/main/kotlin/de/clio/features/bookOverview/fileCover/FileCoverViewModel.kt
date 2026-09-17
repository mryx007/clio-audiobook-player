package de.clio.features.bookOverview.fileCover

import android.net.Uri
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import de.clio.core.data.BookId
import de.clio.features.bookOverview.bottomSheet.BottomSheetItem
import de.clio.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import de.clio.features.bookOverview.di.BookOverviewScope
import de.clio.navigation.Destination
import de.clio.navigation.Navigator

@SingleIn(BookOverviewScope::class)
@ContributesIntoSet(BookOverviewScope::class)
class FileCoverViewModel(private val navigator: Navigator) : BottomSheetItemViewModel {

  private var bookId: BookId? = null

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    return listOf(BottomSheetItem.FileCover)
  }

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    if (item == BottomSheetItem.FileCover) {
      this.bookId = bookId
    }
  }

  fun onImagePicked(uri: Uri) {
    val bookId = bookId ?: return
    navigator.goTo(Destination.EditCover(bookId, uri))
  }
}
