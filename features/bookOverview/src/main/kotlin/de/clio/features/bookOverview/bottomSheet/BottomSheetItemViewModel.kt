package de.clio.features.bookOverview.bottomSheet

import de.clio.core.data.BookId

interface BottomSheetItemViewModel {

  suspend fun items(bookId: BookId): List<BottomSheetItem>
  suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  )
}
