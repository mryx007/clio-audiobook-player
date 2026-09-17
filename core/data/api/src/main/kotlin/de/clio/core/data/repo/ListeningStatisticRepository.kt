package de.clio.core.data.repo

import de.clio.core.data.BookId
import de.clio.core.data.BookStatistic
import de.clio.core.data.ImportResult
import de.clio.core.data.MonthlyStatistic
import de.clio.core.data.StatisticsSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

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
