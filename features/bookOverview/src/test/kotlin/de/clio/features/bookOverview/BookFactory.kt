package de.clio.features.bookOverview

import de.clio.core.data.Book
import de.clio.core.data.BookContent
import de.clio.core.data.BookId
import de.clio.core.data.Chapter
import de.clio.core.data.ChapterId
import java.time.Instant
import kotlin.uuid.Uuid

fun book(
  chapters: List<Chapter> = listOf(chapter(), chapter()),
  time: Long = 42,
  currentChapter: ChapterId = chapters.first().id,
  name: String = Uuid.random().toString(),
  author: String? = Uuid.random().toString(),
  lastPlayedAt: Instant = Instant.EPOCH,
  addedAt: Instant = Instant.EPOCH,
): Book {
  return Book(
    content = BookContent(
      author = author,
      name = name,
      positionInChapter = time,
      playbackSpeed = 1F,
      addedAt = addedAt,
      chapters = chapters.map { it.id },
      cover = null,
      currentChapter = currentChapter,
      isActive = true,
      lastPlayedAt = lastPlayedAt,
      skipSilence = false,
      id = BookId(Uuid.random().toString()),
      gain = 0F,
      genre = null,
      narrator = null,
      series = null,
      part = null,
    ),
    chapters = chapters,
  )
}

fun chapter(
  duration: Long = 10000,
  id: ChapterId = ChapterId(Uuid.random().toString()),
): Chapter {
  return Chapter(
    id = id,
    name = Uuid.random().toString(),
    duration = duration,
    fileLastModified = Instant.EPOCH,
    markData = emptyList(),
    fileSize = 0,
  )
}
