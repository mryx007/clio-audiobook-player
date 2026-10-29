package de.clio.features.bookOverview.editBookCategory

import androidx.datastore.core.DataStore
import de.clio.core.data.BookId
import de.clio.core.data.repo.BookRepository
import de.clio.core.data.store.CompletionThresholdStore
import de.clio.features.bookOverview.bottomSheet.BottomSheetItem
import de.clio.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import de.clio.features.bookOverview.di.BookOverviewScope
import de.clio.features.bookOverview.overview.BookOverviewCategory
import de.clio.features.bookOverview.overview.categorizeByStatus
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first

@SingleIn(BookOverviewScope::class)
@ContributesIntoSet(BookOverviewScope::class)
class EditBookCategoryViewModel(
  private val repo: BookRepository,
  @CompletionThresholdStore
  private val completionThresholdStore: DataStore<Int>,
) : BottomSheetItemViewModel {

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    val book = repo.get(bookId) ?: return emptyList()
    val thresholdSeconds = completionThresholdStore.data.first()
    val isFinished = book.categorizeByStatus(thresholdSeconds) == BookOverviewCategory.COMPLETED
    return when {
      book.position == 0L -> listOf(BottomSheetItem.BookCategoryMarkAsCompleted)
      isFinished -> listOf(BottomSheetItem.BookCategoryMarkAsNotStarted)
      else -> listOf(
        BottomSheetItem.BookCategoryMarkAsNotStarted,
        BottomSheetItem.BookCategoryMarkAsCompleted,
      )
    }
  }

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    val book = repo.get(bookId) ?: return

    val (currentChapter, positionInChapter) = when (item) {
      BottomSheetItem.BookCategoryMarkAsCurrent -> {
        book.chapters.first().id to 1L
      }
      BottomSheetItem.BookCategoryMarkAsNotStarted -> {
        book.chapters.first().id to 0L
      }
      BottomSheetItem.BookCategoryMarkAsCompleted -> {
        val lastChapter = book.chapters.last()
        lastChapter.id to lastChapter.duration
      }
      else -> return
    }

    repo.updateBook(book.id) {
      it.copy(
        currentChapter = currentChapter,
        positionInChapter = positionInChapter,
      )
    }
  }
}
