package voice.core.data.repo

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import voice.core.data.BookId
import voice.core.data.BookStatistic
import voice.core.data.ImportResult
import voice.core.data.ListeningStatistic
import voice.core.data.MonthlyStatistic
import voice.core.data.StatisticsSummary
import voice.core.data.repo.internals.SmartAudioBookPlayerXmlParser
import voice.core.data.repo.internals.dao.ListeningStatisticDao
import voice.core.logging.api.Logger
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
public class ListeningStatisticRepositoryImpl(
  private val dao: ListeningStatisticDao,
  private val bookRepository: BookRepository,
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
      StatisticsSummary(
        totalSeconds = totalSeconds,
        thisMonthSeconds = thisMonthSeconds,
        booksCount = booksCount,
        monthlyStats = monthlyStats,
        bookStats = bookStats,
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

      val existing = dao.findByBookAndMonth(bookTitle, yearMonth)
      if (existing != null) {
        dao.addDuration(existing.id, seconds)
      } else {
        dao.insert(
          ListeningStatistic(
            bookId = bookId,
            bookTitle = bookTitle,
            yearMonth = yearMonth,
            day = day,
            durationSeconds = seconds,
          ),
        )
      }
      Logger.d("Recorded $seconds seconds for book '$bookTitle' ($yearMonth)")
    }
  }

  override suspend fun importSmartAudioBookPlayerXml(xmlContent: String): ImportResult {
    return withContext(Dispatchers.IO) {
      val parsedStats = SmartAudioBookPlayerXmlParser.parse(xmlContent)
      if (parsedStats.isEmpty()) {
        return@withContext ImportResult(booksImported = 0, totalSecondsImported = 0L)
      }

      val allBooks = bookRepository.all()
      val titleToBookId = mutableMapOf<String, BookId>()
      for (book in allBooks) {
        titleToBookId[book.content.name] = book.id
      }

      val distinctBookTitles = mutableSetOf<String>()
      var totalSeconds = 0L

      for (stat in parsedStats) {
        distinctBookTitles.add(stat.bookTitle)
        totalSeconds += stat.durationSeconds

        val matchedBookId = titleToBookId[stat.bookTitle]
        val existing = dao.findByBookAndMonth(stat.bookTitle, stat.yearMonth)
        if (existing != null) {
          dao.addDuration(existing.id, stat.durationSeconds)
        } else {
          dao.insert(
            stat.copy(bookId = matchedBookId),
          )
        }
      }

      Logger.i("Imported ${distinctBookTitles.size} books (${totalSeconds / 3600} hours) from Smart AudioBook Player statistics.xml")
      ImportResult(
        booksImported = distinctBookTitles.size,
        totalSecondsImported = totalSeconds,
      )
    }
  }

  override suspend fun clearStatistics() {
    withContext(Dispatchers.IO) {
      dao.clearAll()
    }
  }
}
