package de.clio.features.bookOverview.internetCover

import androidx.datastore.core.DataStore
import de.clio.core.data.BookId
import de.clio.core.data.store.OnlineCoverEnabledStore
import de.clio.features.bookOverview.bottomSheet.BottomSheetItem
import de.clio.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import de.clio.features.bookOverview.di.BookOverviewScope
import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first

@SingleIn(BookOverviewScope::class)
@ContributesIntoSet(BookOverviewScope::class)
class InternetCoverViewModel(
  private val navigator: Navigator,
  @OnlineCoverEnabledStore
  private val onlineCoverEnabledStore: DataStore<Boolean>,
) : BottomSheetItemViewModel {

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    val isEnabled = onlineCoverEnabledStore.data.first()
    return if (isEnabled) {
      listOf(BottomSheetItem.InternetCover)
    } else {
      emptyList()
    }
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
