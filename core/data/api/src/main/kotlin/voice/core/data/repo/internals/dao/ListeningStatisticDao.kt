package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import voice.core.data.BookStatistic
import voice.core.data.ListeningStatistic
import voice.core.data.MonthlyStatistic

@Dao
public interface ListeningStatisticDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(statistic: ListeningStatistic)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insertAll(statistics: List<ListeningStatistic>)

  @Query("SELECT * FROM listening_statistics WHERE bookTitle = :bookTitle AND yearMonth = :yearMonth LIMIT 1")
  public suspend fun findByBookAndMonth(bookTitle: String, yearMonth: String): ListeningStatistic?

  @Query("UPDATE listening_statistics SET durationSeconds = durationSeconds + :additionalSeconds WHERE id = :id")
  public suspend fun addDuration(id: Long, additionalSeconds: Long)

  @Query("SELECT yearMonth, SUM(durationSeconds) as totalSeconds FROM listening_statistics GROUP BY yearMonth ORDER BY yearMonth DESC")
  public fun getMonthlyStatisticsFlow(): Flow<List<MonthlyStatistic>>

  @Query("SELECT bookTitle, bookId, SUM(durationSeconds) as totalSeconds FROM listening_statistics GROUP BY bookTitle ORDER BY totalSeconds DESC")
  public fun getBookStatisticsFlow(): Flow<List<BookStatistic>>

  @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM listening_statistics")
  public fun getTotalSecondsFlow(): Flow<Long>

  @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM listening_statistics WHERE yearMonth = :yearMonth")
  public fun getMonthSecondsFlow(yearMonth: String): Flow<Long>

  @Query("SELECT COUNT(DISTINCT bookTitle) FROM listening_statistics")
  public fun getDistinctBooksCountFlow(): Flow<Int>

  @Query("DELETE FROM listening_statistics")
  public suspend fun clearAll()
}
