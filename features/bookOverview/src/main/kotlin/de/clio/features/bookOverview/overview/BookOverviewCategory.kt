package de.clio.features.bookOverview.overview

import androidx.annotation.StringRes
import de.clio.core.data.Book
import de.clio.core.data.BookComparator
import java.util.concurrent.TimeUnit.SECONDS
import de.clio.core.strings.R as StringsR

enum class BookOverviewCategory(
  @StringRes val nameRes: Int,
  val comparator: Comparator<Book>,
) {
  OVERVIEW(
    nameRes = StringsR.string.library_category_overview_title,
    comparator = BookComparator.ByLastPlayed,
  ),
  STARTED(
    nameRes = StringsR.string.library_category_current_title,
    comparator = BookComparator.ByLastPlayed,
  ),
  UNBEGUN(
    nameRes = StringsR.string.library_category_not_started_title,
    comparator = BookComparator.ByLastPlayed,
  ),
  COMPLETED(
    nameRes = StringsR.string.library_category_completed_title,
    comparator = BookComparator.ByLastPlayed,
  ),
}

val Book.category: BookOverviewCategory
  get() = BookOverviewCategory.OVERVIEW

fun Book.categorizeByStatus(): BookOverviewCategory {
  val isFinished = position >= duration - SECONDS.toMillis(5)
  return when {
    isFinished -> BookOverviewCategory.COMPLETED
    position > 0L -> BookOverviewCategory.STARTED
    else -> BookOverviewCategory.UNBEGUN
  }
}
