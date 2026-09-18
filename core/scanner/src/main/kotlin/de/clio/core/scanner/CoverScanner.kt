package de.clio.core.scanner

import android.content.Context
import de.clio.core.data.Book
import de.clio.core.data.isImageFile
import de.clio.core.data.toUri
import de.clio.core.documentfile.CachedDocumentFileFactory
import de.clio.core.logging.api.Logger
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.IOException

@Inject
internal class CoverScanner(
  private val context: Context,
  private val coverSaver: CoverSaver,
  private val coverExtractor: CoverExtractor,
  private val documentFileFactory: CachedDocumentFileFactory,
) {

  suspend fun scan(books: List<Book>): Unit = coroutineScope {
    val concurrency = (Runtime.getRuntime().availableProcessors() * 2).coerceIn(4, 16)
    val semaphore = Semaphore(concurrency)
    books.map { book ->
      async(Dispatchers.IO) {
        semaphore.withPermit {
          findCoverForBook(book)
        }
      }
    }.awaitAll()
  }

  private suspend fun findCoverForBook(book: Book) {
    val coverFile = book.content.cover
    if (coverFile != null && coverFile.exists()) {
      return
    }

    val foundOnDisc = findAndSaveCoverFromDisc(book)
    if (foundOnDisc) {
      return
    }

    scanForEmbeddedCover(book)
  }

  private suspend fun findAndSaveCoverFromDisc(book: Book): Boolean = withContext(Dispatchers.IO) {
    val documentFile = try {
      documentFileFactory.create(book.id.toUri())
    } catch (_: IllegalArgumentException) {
      null
    } ?: return@withContext false

    if (!documentFile.isDirectory) {
      return@withContext false
    }

    val imageFile = documentFile.children.firstOrNull { it.isImageFile() } ?: return@withContext false
    val coverFile = coverSaver.newBookCoverFile()
    val worked = try {
      context.contentResolver.openInputStream(imageFile.uri)?.use { input ->
        coverFile.outputStream().use { output ->
          input.copyTo(output)
        }
      }
      true
    } catch (e: IOException) {
      Logger.w(e, "Error while copying the cover from ${imageFile.uri}")
      false
    } catch (e: IllegalStateException) {
      // On some Samsung Devices, openInputStream throws this exception, though it should not.
      Logger.w(e, "Error while copying the cover from ${imageFile.uri}")
      false
    }
    if (worked) {
      coverSaver.setBookCover(coverFile, book.id)
      return@withContext true
    }

    false
  }

  private suspend fun scanForEmbeddedCover(book: Book) {
    val coverFile = coverSaver.newBookCoverFile()
    book.chapters
      .take(5).forEach { chapter ->
        val success = coverExtractor.extractCover(
          input = chapter.id.toUri(),
          outputFile = coverFile,
        )
        if (success && coverFile.exists() && coverFile.length() > 0) {
          coverSaver.setBookCover(coverFile, bookId = book.id)
          return
        }
      }
  }
}
