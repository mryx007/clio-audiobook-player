package voice.core.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import voice.core.data.BookId
import voice.core.data.BookStatistic
import voice.core.data.ImportResult
import voice.core.data.MonthlyStatistic
import voice.core.data.StatisticsSummary

public interface ListeningStatisticRepository {

  public fun getMonthlyStatistics(): Flow<List<MonthlyStatistic>>

  public fun getBookStatistics(): Flow<List<BookStatistic>>

  public fun getTotalListeningTimeSeconds(): Flow<Long>

  public fun getStatisticsSummary(): StateFlow<StatisticsSummary?>

  public suspend fun recordListeningTime(
    bookId: BookId?,
    bookTitle: String,
    seconds: Long,
  )

  public suspend fun importSmartAudioBookPlayerXml(
    xmlContent: String,
    coverProvider: (suspend (rawPath: String, bookTitle: String) -> java.io.InputStream?)? = null,
  ): ImportResult

  public suspend fun clearStatistics()
}
