package de.clio.features.bookOverview.editBookCategory

import de.clio.core.data.BookId
import de.clio.core.data.repo.BookRepository
import de.clio.features.bookOverview.bottomSheet.BottomSheetItem
import de.clio.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import de.clio.features.bookOverview.di.BookOverviewScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import java.util.concurrent.TimeUnit.SECONDS

@SingleIn(BookOverviewScope::class)
@ContributesIntoSet(BookOverviewScope::class)
class EditBookCategoryViewModel(private val repo: BookRepository) : BottomSheetItemViewModel {

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    val book = repo.get(bookId) ?: return emptyList()
    val isFinished = book.position >= book.duration - SECONDS.toMillis(5)
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
