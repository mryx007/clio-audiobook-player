package voice.core.data.repo

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import voice.core.data.BookId
import voice.core.data.BookStatistic
import voice.core.data.ImportResult
import voice.core.data.ListeningStatistic
import voice.core.data.MonthlyStatistic
import voice.core.data.StatisticsSummary
import voice.core.data.folders.AudiobookFolders
import voice.core.data.folders.FolderType
import voice.core.data.repo.internals.SmartAudioBookPlayerXmlParser
import voice.core.data.repo.internals.SmartAudioBookPlayerXmlSerializer
import voice.core.data.repo.internals.dao.ListeningStatisticDao
import voice.core.logging.api.Logger
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
public class ListeningStatisticRepositoryImpl(
  private val dao: ListeningStatisticDao,
  private val bookRepository: BookRepository,
  private val audiobookFolders: AudiobookFolders,
  private val context: Context,
) : ListeningStatisticRepository {

  private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
  private val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  override fun getMonthlyStatistics(): Flow<List<MonthlyStatistic>> {
    return dao.getMonthlyStatisticsFlow().flowOn(Dispatchers.IO)
  }

  override fun getBookStatistics(): Flow<List<BookStatistic>> {
    return dao.getBookStatisticsFlow().flowOn(Dispatchers.IO)
  }

  override fun getTotalListeningTimeSeconds(): Flow<Long> {
    return dao.getTotalSecondsFlow().flowOn(Dispatchers.IO)
  }

  override fun getStatisticsSummary(): Flow<StatisticsSummary> {
    val currentYearMonth = LocalDate.now().format(yearMonthFormatter)
    return combine(
      dao.getTotalSecondsFlow(),
      dao.getMonthSecondsFlow(currentYearMonth),
      dao.getDistinctBooksCountFlow(),
      dao.getMonthlyStatisticsFlow(),
      dao.getBookStatisticsFlow(),
    ) { totalSeconds, thisMonthSeconds, booksCount, monthlyStats, bookStats ->
      val resolvedBookStats = bookStats.map { book ->
        if (book.coverUrl.isNullOrEmpty()) {
          val matchingBook = book.bookId?.let { bookRepository.get(it) }
            ?: bookRepository.all().firstOrNull { it.content.name == book.bookTitle }
          val coverUrl = getOrPersistCover(book.bookTitle, matchingBook?.content?.cover)
          if (coverUrl != null) {
            dao.updateCoverForBook(book.bookTitle, coverUrl)
            book.copy(coverUrl = coverUrl)
          } else {
            book
          }
        } else {
          book
        }
      }
      StatisticsSummary(
        totalSeconds = totalSeconds,
        thisMonthSeconds = thisMonthSeconds,
        booksCount = booksCount,
        monthlyStats = monthlyStats,
        bookStats = resolvedBookStats,
      )
    }.flowOn(Dispatchers.IO)
  }

  override suspend fun recordListeningTime(
    bookId: BookId?,
    bookTitle: String,
    seconds: Long,
  ) {
    if (seconds <= 0L || bookTitle.isBlank()) return
    withContext(Dispatchers.IO) {
      val now = LocalDate.now()
      val yearMonth = now.format(yearMonthFormatter)
      val day = now.format(dayFormatter)

      val book = bookId?.let { bookRepository.get(it) }
        ?: bookRepository.all().firstOrNull { it.content.name == bookTitle }
      val coverUrl = getOrPersistCover(bookTitle, book?.content?.cover)

      val existing = dao.findByBookAndMonth(bookTitle, yearMonth)
      if (existing != null) {
        dao.addDuration(existing.id, seconds)
        if (coverUrl != null && existing.coverUrl.isNullOrEmpty()) {
          dao.updateCoverForBook(bookTitle, coverUrl)
        }
      } else {
        dao.insert(
          ListeningStatistic(
            bookId = bookId,
            bookTitle = bookTitle,
            yearMonth = yearMonth,
            day = day,
            durationSeconds = seconds,
            coverUrl = coverUrl,
          ),
        )
      }
      Logger.d("Recorded $seconds seconds for book '$bookTitle' ($yearMonth)")
      syncToAudiobookFolders()
    }
  }

  override suspend fun importSmartAudioBookPlayerXml(
    xmlContent: String,
    coverProvider: (suspend (rawPath: String, bookTitle: String) -> java.io.InputStream?)?,
  ): ImportResult {
    return withContext(Dispatchers.IO) {
      val parsedBooks = SmartAudioBookPlayerXmlParser.parseDetailed(xmlContent)
      if (parsedBooks.isEmpty()) {
        return@withContext ImportResult(booksImported = 0, totalSecondsImported = 0L)
      }

      val allBooks = bookRepository.all()
      val distinctBookTitles = mutableSetOf<String>()
      var totalSeconds = 0L

      for (parsedBook in parsedBooks) {
        distinctBookTitles.add(parsedBook.bookTitle)

        var coverUrl: String? = null
        if (coverProvider != null) {
          try {
            val stream = coverProvider(parsedBook.rawPath, parsedBook.bookTitle)
            if (stream != null) {
              coverUrl = persistCoverStream(parsedBook.bookTitle, stream)
            }
          } catch (e: Exception) {
            Logger.w(e, "Error loading cover from provider for ${parsedBook.bookTitle}")
          }
        }

        if (coverUrl == null) {
          val matchedBook = allBooks.firstOrNull { it.content.name == parsedBook.bookTitle }
          coverUrl = getOrPersistCover(parsedBook.bookTitle, matchedBook?.content?.cover)
        }

        val matchedBook = allBooks.firstOrNull { it.content.name == parsedBook.bookTitle }

        for (stat in parsedBook.statistics) {
          totalSeconds += stat.durationSeconds
          val existing = dao.findByBookAndMonth(stat.bookTitle, stat.yearMonth)
          if (existing != null) {
            val newDuration = maxOf(existing.durationSeconds, stat.durationSeconds)
            dao.updateDuration(existing.id, newDuration)
            if (coverUrl != null) {
              dao.updateCoverForBook(stat.bookTitle, coverUrl)
            }
          } else {
            dao.insert(
              stat.copy(
                bookId = matchedBook?.id,
                coverUrl = coverUrl,
              ),
            )
          }
        }
      }

      Logger.i("Imported ${distinctBookTitles.size} books (${totalSeconds / 3600} hours) from statistics")
      syncToAudiobookFolders()

      ImportResult(
        booksImported = distinctBookTitles.size,
        totalSecondsImported = totalSeconds,
      )
    }
  }

  private fun persistCoverStream(bookTitle: String, stream: java.io.InputStream): String? {
    return try {
      val coversDir = File(context.filesDir, "statisticCovers").also { it.mkdirs() }
      val safeFileName = "stat_cover_" + (bookTitle.hashCode().toString().replace('-', 'n')) + ".png"
      val destFile = File(coversDir, safeFileName)
      stream.use { input ->
        destFile.outputStream().use { output ->
          input.copyTo(output)
        }
      }
      destFile.toURI().toString()
    } catch (e: Exception) {
      Logger.w(e, "Error saving cover stream for $bookTitle")
      null
    }
  }

  override suspend fun clearStatistics() {
    withContext(Dispatchers.IO) {
      dao.clearAll()
      try {
        File(context.filesDir, "statisticCovers").deleteRecursively()
      } catch (e: Exception) {
        Logger.w(e, "Error clearing statistic covers")
      }
      try {
        val foldersMap = audiobookFolders.all().first()
        val primaryFolder = foldersMap[FolderType.Root]?.firstOrNull()
          ?: foldersMap[FolderType.SingleFolder]?.firstOrNull()
          ?: foldersMap[FolderType.Author]?.firstOrNull()
        if (primaryFolder != null) {
          val rootDoc = DocumentFile.fromTreeUri(context, primaryFolder.uri)
          val statsDir = rootDoc?.findFile("!Clio Audiobook Player Statistics")
          statsDir?.findFile("statistics.xml")?.delete()
        }
      } catch (e: Exception) {
        Logger.w(e, "Error clearing synced statistics in audiobook folder")
      }
    }
  }

  private suspend fun syncToAudiobookFolders() {
    try {
      val foldersMap = audiobookFolders.all().first()
      val primaryFolder = foldersMap[FolderType.Root]?.firstOrNull()
        ?: foldersMap[FolderType.SingleFolder]?.firstOrNull()
        ?: foldersMap[FolderType.Author]?.firstOrNull()
        ?: return

      val rootDoc = DocumentFile.fromTreeUri(context, primaryFolder.uri) ?: return
      if (!rootDoc.isDirectory || !rootDoc.canWrite()) return

      var statsDir = rootDoc.findFile("!Clio Audiobook Player Statistics")
      if (statsDir == null) {
        statsDir = rootDoc.createDirectory("!Clio Audiobook Player Statistics")
      }
      if (statsDir == null || !statsDir.isDirectory) return

      val allStats = dao.getAll()
      if (allStats.isNotEmpty()) {
        val xmlContent = SmartAudioBookPlayerXmlSerializer.serialize(allStats)
        var xmlFile = statsDir.findFile("statistics.xml")
        if (xmlFile == null) {
          xmlFile = statsDir.createFile("text/xml", "statistics.xml")
        }
        xmlFile?.let { file ->
          context.contentResolver.openOutputStream(file.uri, "wt")?.use { out ->
            out.write(xmlContent.toByteArray(Charsets.UTF_8))
          }
        }

        val coversDir = File(context.filesDir, "statisticCovers")
        val distinctBooks = allStats.map { it.bookTitle }.distinct()
        for (bookTitle in distinctBooks) {
          val safeHashName = "stat_cover_" + (bookTitle.hashCode().toString().replace('-', 'n')) + ".png"
          val localCover = File(coversDir, safeHashName)
          if (localCover.exists() && localCover.length() > 0) {
            val coverFileName = sanitizeFileName(bookTitle) + ".jpg"
            var destDoc = statsDir.findFile(coverFileName)
            if (destDoc == null) {
              destDoc = statsDir.createFile("image/jpeg", coverFileName)
            }
            if (destDoc != null && destDoc.length() == 0L) {
              localCover.inputStream().use { input ->
                context.contentResolver.openOutputStream(destDoc.uri, "wt")?.use { output ->
                  input.copyTo(output)
                }
              }
            }
          }
        }
      }
    } catch (e: Exception) {
      Logger.w(e, "Error syncing statistics to audiobook folder")
    }
  }

  private fun sanitizeFileName(name: String): String {
    val reservedChars = setOf('/', '\\', '?', '%', '*', ':', '|', '"', '<', '>', '.')
    val sb = StringBuilder(name.length)
    for (c in name) {
      if (c in reservedChars) {
        sb.append('_')
      } else {
        sb.append(c)
      }
    }
    return sb.toString().trim()
  }

  private fun getOrPersistCover(bookTitle: String, sourceCoverFile: File?): String? {
    try {
      val coversDir = File(context.filesDir, "statisticCovers").also { it.mkdirs() }
      val safeFileName = "stat_cover_" + (bookTitle.hashCode().toString().replace('-', 'n')) + ".png"
      val destFile = File(coversDir, safeFileName)

      if (destFile.exists() && destFile.length() > 0) {
        return destFile.toURI().toString()
      }

      if (sourceCoverFile != null && sourceCoverFile.exists() && sourceCoverFile.length() > 0) {
        sourceCoverFile.copyTo(destFile, overwrite = true)
        return destFile.toURI().toString()
      }
    } catch (e: Exception) {
      Logger.w(e, "Could not persist cover for statistic of $bookTitle")
    }
    return null
  }
}
