package de.clio.core.playback.session.search

import android.provider.MediaStore
import androidx.datastore.core.DataStore
import de.clio.core.data.Book
import de.clio.core.data.BookContent
import de.clio.core.data.BookId
import de.clio.core.data.Chapter
import de.clio.core.data.ChapterId
import de.clio.core.data.repo.BookRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class BookSearchHandlerTest {

  private val searchHandler: BookSearchHandler

  private val repo = mockk<BookRepository>()
  private val currentBookId = MemoryDataStore<BookId?>(null)

  private val anotherBook = book(listOf(chapter(), chapter()))
  private val bookToFind = book(listOf(chapter(), chapter()))

  init {
    coEvery { repo.all() } coAnswers { listOf(anotherBook, bookToFind) }

    searchHandler = BookSearchHandler(repo, currentBookId)
  }

  @Test
  fun unstructuredSearchByBook() = runTest {
    val bookSearch = ClioSearch(query = bookToFind.content.name)
    assertEquals(expected = bookToFind, actual = searchHandler.handle(bookSearch))
  }

  @Test
  fun unstructuredSearchByArtist() = runTest {
    val bookSearch = ClioSearch(query = bookToFind.content.author)
    assertEquals(expected = bookToFind, actual = searchHandler.handle(bookSearch))
  }

  @Test
  fun unstructuredSearchByChapter() = runTest {
    val bookSearch = ClioSearch(query = bookToFind.chapters.first().name)
    assertEquals(expected = bookToFind, actual = searchHandler.handle(bookSearch))
  }

  @Test
  fun mediaFocusAnyNoneFoundButPlayed() = runTest {
    val bookSearch = ClioSearch(mediaFocus = "vnd.android.cursor.item/*")
    assertNull(searchHandler.handle(bookSearch))
  }

  @Test
  fun mediaFocusArtist() = runTest {
    val bookSearch = ClioSearch(
      mediaFocus = MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE,
      artist = bookToFind.content.author,
    )
    assertEquals(expected = bookToFind, actual = searchHandler.handle(bookSearch))
  }

  @Test
  fun mediaFocusArtistInTitleNoArtistInBook() = runTest {
    val bookToFind = bookToFind.copy(
      content = bookToFind.content.copy(
        author = null,
        name = "The book of Tim",
      ),
    )
    coEvery { repo.all() } coAnswers { listOf(bookToFind) }

    val bookSearch = ClioSearch(
      mediaFocus = MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE,
      query = "Tim",
      artist = "Tim",
    )
    assertEquals(expected = bookToFind, actual = searchHandler.handle(bookSearch))
  }

  @Test
  fun mediaFocusAlbum() = runTest {
    val bookSearch = ClioSearch(
      mediaFocus = MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE,
      artist = bookToFind.content.author,
      album = bookToFind.content.name,
      query = null,
    )
    assertEquals(expected = bookToFind, actual = searchHandler.handle(bookSearch))
  }
}

fun book(
  chapters: List<Chapter>,
  id: BookId = BookId(Uuid.random().toString()),
  positionInChapter: Long = 0,
): Book {
  return Book(
    content = BookContent(
      author = Uuid.random().toString(),
      name = Uuid.random().toString(),
      positionInChapter = positionInChapter,
      playbackSpeed = 1F,
      addedAt = Instant.EPOCH,
      chapters = chapters.map { it.id },
      cover = null,
      currentChapter = chapters.first().id,
      isActive = true,
      lastPlayedAt = Instant.EPOCH,
      skipSilence = false,
      id = id,
      gain = 0F,
      genre = null,
      narrator = null,
      series = null,
      part = null,
    ),
    chapters = chapters,
  )
}

private fun chapter(): Chapter {
  return Chapter(
    id = ChapterId(Uuid.random().toString()),
    name = Uuid.random().toString(),
    duration = 10000,
    fileLastModified = Instant.EPOCH,
    markData = emptyList(),
    fileSize = 0,
  )
}

private class MemoryDataStore<T>(initial: T) : DataStore<T> {

  private val value = MutableStateFlow(initial)

  override val data: Flow<T> get() = value

  override suspend fun updateData(transform: suspend (t: T) -> T): T {
    return value.updateAndGet { transform(it) }
  }
}
