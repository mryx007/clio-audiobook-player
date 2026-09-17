package de.clio.features.bookOverview

import de.clio.features.bookOverview.overview.BookOverviewCategory
import de.clio.features.bookOverview.overview.category
import kotlin.test.Test
import kotlin.test.assertEquals

class BookOverviewCategoryTest {

  @Test
  fun overview() {
    val book = book()
    assertEquals(expected = BookOverviewCategory.OVERVIEW, actual = book.category)
  }
}
