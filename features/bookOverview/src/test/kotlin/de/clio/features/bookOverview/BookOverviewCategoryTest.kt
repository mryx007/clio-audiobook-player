package de.clio.features.bookOverview

import de.clio.features.bookOverview.overview.BookOverviewCategory
import de.clio.features.bookOverview.overview.categorizeByStatus
import de.clio.features.bookOverview.overview.category
import kotlin.test.Test
import kotlin.test.assertEquals

class BookOverviewCategoryTest {

  @Test
  fun overview() {
    val book = book()
    assertEquals(expected = BookOverviewCategory.OVERVIEW, actual = book.category)
  }

  @Test
  fun `categorizeByStatus returns UNBEGUN when position is 0`() {
    val book = book(time = 0L)
    assertEquals(expected = BookOverviewCategory.UNBEGUN, actual = book.categorizeByStatus())
  }

  @Test
  fun `categorizeByStatus returns STARTED when position is greater than 0 but not near end`() {
    val book = book(time = 1_000L)
    assertEquals(expected = BookOverviewCategory.STARTED, actual = book.categorizeByStatus())
  }

  @Test
  fun `categorizeByStatus returns COMPLETED when within 5 seconds of duration`() {
    val chapters = listOf(chapter(duration = 10_000L))
    val book = book(chapters = chapters, time = 9_000L)
    assertEquals(expected = BookOverviewCategory.COMPLETED, actual = book.categorizeByStatus())
  }

  @Test
  fun `categorizeByStatus respects custom threshold`() {
    val chapters = listOf(chapter(duration = 100_000L))
    val book = book(chapters = chapters, time = 75_000L)
    assertEquals(expected = BookOverviewCategory.STARTED, actual = book.categorizeByStatus(thresholdInSeconds = 20))
    assertEquals(expected = BookOverviewCategory.COMPLETED, actual = book.categorizeByStatus(thresholdInSeconds = 30))
  }

  @Test
  fun `categorizeByStatus does not mark short book completed too early`() {
    val chapters = listOf(chapter(duration = 10_000L))
    val book = book(chapters = chapters, time = 1_000L)
    assertEquals(expected = BookOverviewCategory.STARTED, actual = book.categorizeByStatus(thresholdInSeconds = 30))
  }
}
