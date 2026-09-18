package de.clio.core.data

import kotlin.test.Test
import kotlin.test.assertEquals

class BookSortOrderTest {

  private val b1 = book(
    name = "A",
    author = "Stephen King",
    lastPlayedAtMillis = 100,
    addedAtMillis = 10,
    chapters = listOf(chapter(duration = 1000)),
  )
  private val b2 = book(
    name = "B",
    author = "Candice Fox",
    lastPlayedAtMillis = 200,
    addedAtMillis = 30,
    chapters = listOf(chapter(duration = 5000)),
  )
  private val b3 = book(
    name = "C",
    author = "Agatha Christie",
    lastPlayedAtMillis = 50,
    addedAtMillis = 20,
    chapters = listOf(chapter(duration = 2000)),
  )
  private val books = listOf(b1, b2, b3)

  @Test
  fun byLastPlayed() {
    val sorted = books.sortedWith(BookSortOrder.BY_LAST_PLAYED)
    assertEquals(listOf(b2, b1, b3), sorted)
  }

  @Test
  fun byAddedAt() {
    val sorted = books.sortedWith(BookSortOrder.BY_ADDED_AT)
    assertEquals(listOf(b2, b3, b1), sorted)
  }

  @Test
  fun byNameAsc() {
    val sorted = books.sortedWith(BookSortOrder.BY_NAME_ASC)
    assertEquals(listOf(b1, b2, b3), sorted)
  }

  @Test
  fun byNameDesc() {
    val sorted = books.sortedWith(BookSortOrder.BY_NAME_DESC)
    assertEquals(listOf(b3, b2, b1), sorted)
  }

  @Test
  fun byAuthor() {
    val sorted = books.sortedWith(BookSortOrder.BY_AUTHOR)
    assertEquals(listOf(b3, b2, b1), sorted)
  }

  @Test
  fun byDurationAsc() {
    val sorted = books.sortedWith(BookSortOrder.BY_DURATION_ASC)
    assertEquals(listOf(b1, b3, b2), sorted)
  }

  @Test
  fun byDurationDesc() {
    val sorted = books.sortedWith(BookSortOrder.BY_DURATION_DESC)
    assertEquals(listOf(b2, b3, b1), sorted)
  }
}
