package de.clio.core.scanner

import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import de.clio.core.data.BookId
import de.clio.core.data.Chapter
import de.clio.core.data.ChapterId
import de.clio.core.documentfile.FileBasedDocumentFile
import java.io.File
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class BookParserTest {

  @get:Rule
  val testFolder = TemporaryFolder()

  private val parser = BookParser(
    contentRepo = mockk(),
    mediaAnalyzer = mockk(),
    fileFactory = mockk(),
  )

  @Test
  fun folderBookUsesFolderNameWhenAlbumMissing() {
    val bookFolder = testFolder.newFolder("My Audiobook")
    val chapters = listOf(
      chapter(File(bookFolder, "1.mp3").apply { createNewFile() }),
      chapter(File(bookFolder, "2.mp3").apply { createNewFile() }),
    )

    val content = parser.parse(
      chapters = chapters,
      id = BookId(bookFolder.toUri()),
      analyzed = metadata(album = null, title = "First Chapter Title"),
      file = FileBasedDocumentFile(bookFolder),
    )

    assertEquals(expected = "My Audiobook", actual = content.name)
  }

  @Test
  fun folderBookWithSingleChapterStillUsesFolderName() {
    val bookFolder = testFolder.newFolder("Harry Potter 3")
    val chapters = listOf(chapter(File(bookFolder, "track01.mp3").apply { createNewFile() }))

    val content = parser.parse(
      chapters = chapters,
      id = BookId(bookFolder.toUri()),
      analyzed = metadata(album = null, title = "Track Title"),
      file = FileBasedDocumentFile(bookFolder),
    )

    assertEquals(expected = "Harry Potter 3", actual = content.name)
  }

  @Test
  fun singleFileBookUsesTitleWhenAlbumMissing() {
    val bookFile = testFolder.newFile("book.mp3")
    val chapters = listOf(chapter(bookFile))

    val content = parser.parse(
      chapters = chapters,
      id = BookId(bookFile.toUri()),
      analyzed = metadata(album = null, title = "The Title"),
      file = FileBasedDocumentFile(bookFile),
    )

    assertEquals(expected = "The Title", actual = content.name)
  }

  @Test
  fun albumAlwaysWinsOverTitleAndFolderName() {
    val bookFolder = testFolder.newFolder("Folder Name")
    val chapters = listOf(
      chapter(File(bookFolder, "1.mp3").apply { createNewFile() }),
      chapter(File(bookFolder, "2.mp3").apply { createNewFile() }),
    )

    val content = parser.parse(
      chapters = chapters,
      id = BookId(bookFolder.toUri()),
      analyzed = metadata(album = "Album Name", title = "First Chapter Title"),
      file = FileBasedDocumentFile(bookFolder),
    )

    assertEquals(expected = "Album Name", actual = content.name)
  }

  @Test
  fun missingMetadataFallsBackToFolderName() {
    val bookFolder = testFolder.newFolder("Fallback Folder")
    val chapters = listOf(
      chapter(File(bookFolder, "1.mp3").apply { createNewFile() }),
      chapter(File(bookFolder, "2.mp3").apply { createNewFile() }),
    )

    val content = parser.parse(
      chapters = chapters,
      id = BookId(bookFolder.toUri()),
      analyzed = null,
      file = FileBasedDocumentFile(bookFolder),
    )

    assertEquals(expected = "Fallback Folder", actual = content.name)
  }

  @Test
  fun folderBookWithAuthorAndTitleInFolderNameParsesBoth() {
    val bookFolder = testFolder.newFolder("Franz Kafka - Die Verwandlung")
    val chapters = listOf(chapter(File(bookFolder, "1.mp3").apply { createNewFile() }))

    val content = parser.parse(
      chapters = chapters,
      id = BookId(bookFolder.toUri()),
      analyzed = null,
      file = FileBasedDocumentFile(bookFolder),
    )

    assertEquals(expected = "Franz Kafka", actual = content.author)
    assertEquals(expected = "Die Verwandlung", actual = content.name)
  }

  @Test
  fun folderBookWithTrackPrefixDoesNotParseTrackAsAuthor() {
    val bookFolder = testFolder.newFolder("01 - Chapter One")
    val chapters = listOf(chapter(File(bookFolder, "1.mp3").apply { createNewFile() }))

    val content = parser.parse(
      chapters = chapters,
      id = BookId(bookFolder.toUri()),
      analyzed = null,
      file = FileBasedDocumentFile(bookFolder),
    )

    assertEquals(expected = null, actual = content.author)
    assertEquals(expected = "01 - Chapter One", actual = content.name)
  }

  @Test
  fun folderBookWithUnderscoreDelimiterParsesBoth() {
    val bookFolder = testFolder.newFolder("Stephen_King_-_Die_Arena")
    val chapters = listOf(chapter(File(bookFolder, "1.mp3").apply { createNewFile() }))

    val content = parser.parse(
      chapters = chapters,
      id = BookId(bookFolder.toUri()),
      analyzed = null,
      file = FileBasedDocumentFile(bookFolder),
    )

    assertEquals(expected = "Stephen King", actual = content.author)
    assertEquals(expected = "Die Arena", actual = content.name)
  }

  @Test
  fun titleWithNarratorInParenthesesIsCleanedAndNarratorExtracted() {
    val (title, narrator) = cleanTitleAndExtractNarrator("Die Arena (gelesen von David Nathan)")
    assertEquals(expected = "Die Arena", actual = title)
    assertEquals(expected = "David Nathan", actual = narrator)
  }

  @Test
  fun titleWithUnabridgedNarratorIsCleaned() {
    val (title, narrator) = cleanTitleAndExtractNarrator("Die Arena (Ungekürzt, gelesen von David Nathan)")
    assertEquals(expected = "Die Arena", actual = title)
    assertEquals(expected = "David Nathan", actual = narrator)
  }

  @Test
  fun titleWithReadByIsCleaned() {
    val (title, narrator) = cleanTitleAndExtractNarrator("Harry Potter (read by Stephen Fry)")
    assertEquals(expected = "Harry Potter", actual = title)
    assertEquals(expected = "Stephen Fry", actual = narrator)
  }

  @Test
  fun parseFiltersNarratorFromAlbum() {
    val bookFolder = testFolder.newFolder("Stephen King - Die Arena")
    val chapters = listOf(chapter(File(bookFolder, "1.mp3").apply { createNewFile() }))

    val content = parser.parse(
      chapters = chapters,
      id = BookId(bookFolder.toUri()),
      analyzed = metadata(album = "Die Arena (gelesen von David Nathan)", title = "Track 1"),
      file = FileBasedDocumentFile(bookFolder),
    )

    assertEquals(expected = "Die Arena", actual = content.name)
    assertEquals(expected = "David Nathan", actual = content.narrator)
  }

  private fun chapter(file: File): Chapter = Chapter(
    id = ChapterId(file.toUri()),
    name = "Chapter",
    duration = 1000L,
    fileLastModified = Instant.EPOCH,
    markData = emptyList(),
    fileSize = 0,
  )

  private fun metadata(
    album: String?,
    title: String?,
  ): Metadata = Metadata(
    duration = 1000L,
    artist = null,
    album = album,
    title = title,
    fileName = "file",
    chapters = emptyList(),
    genre = null,
    narrator = null,
    series = null,
    part = null,
  )
}
