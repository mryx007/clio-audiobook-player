package voice.features.bookOverview.overview

import androidx.annotation.StringRes
import voice.core.data.Book
import voice.core.data.BookComparator
import java.util.concurrent.TimeUnit.SECONDS
import voice.core.strings.R as StringsR

enum class BookOverviewCategory(
  @StringRes val nameRes: Int,
  val comparator: Comparator<Book>,
) {
  OVERVIEW(
    nameRes = StringsR.string.library_category_overview_title,
    comparator = BookComparator.ByLastPlayed,
  ),
}

val Book.category: BookOverviewCategory
  get() = BookOverviewCategory.OVERVIEW
