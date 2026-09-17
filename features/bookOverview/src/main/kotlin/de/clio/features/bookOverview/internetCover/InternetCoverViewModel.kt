package de.clio.features.bookOverview.internetCover

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
class InternetCoverViewModel(private val navigator: Navigator) : BottomSheetItemViewModel {

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    return listOf(BottomSheetItem.InternetCover)
  }

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    if (item == BottomSheetItem.InternetCover) {
      navigator.goTo(Destination.CoverFromInternet(bookId))
    }
  }
}
