package de.clio.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.clio.core.data.BookStatistic
import de.clio.core.data.ListeningStatistic
import de.clio.core.data.MonthlyStatistic
import kotlinx.coroutines.flow.Flow

@Dao
public interface ListeningStatisticDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(statistic: ListeningStatistic)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insertAll(statistics: List<ListeningStatistic>)

  @Query("SELECT * FROM listening_statistics WHERE bookTitle = :bookTitle AND yearMonth = :yearMonth LIMIT 1")
  public suspend fun findByBookAndMonth(
    bookTitle: String,
    yearMonth: String,
  ): ListeningStatistic?

  @Query("UPDATE listening_statistics SET durationSeconds = durationSeconds + :additionalSeconds WHERE id = :id")
  public suspend fun addDuration(
    id: Long,
    additionalSeconds: Long,
  )

  @Query("UPDATE listening_statistics SET durationSeconds = :durationSeconds WHERE id = :id")
  public suspend fun updateDuration(
    id: Long,
    durationSeconds: Long,
  )

  @Query("SELECT yearMonth, SUM(durationSeconds) as totalSeconds FROM listening_statistics GROUP BY yearMonth ORDER BY yearMonth DESC")
  public fun getMonthlyStatisticsFlow(): Flow<List<MonthlyStatistic>>

  @Query(
    "SELECT bookTitle, bookId, SUM(durationSeconds) as totalSeconds, MAX(coverUrl) as coverUrl FROM listening_statistics GROUP BY bookTitle ORDER BY totalSeconds DESC",
  )
  public fun getBookStatisticsFlow(): Flow<List<BookStatistic>>

  @Query("UPDATE listening_statistics SET coverUrl = :coverUrl WHERE bookTitle = :bookTitle")
  public suspend fun updateCoverForBook(
    bookTitle: String,
    coverUrl: String,
  )

  @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM listening_statistics")
  public fun getTotalSecondsFlow(): Flow<Long>

  @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM listening_statistics WHERE yearMonth = :yearMonth")
  public fun getMonthSecondsFlow(yearMonth: String): Flow<Long>

  @Query("SELECT COUNT(DISTINCT bookTitle) FROM listening_statistics")
  public fun getDistinctBooksCountFlow(): Flow<Int>

  @Query("SELECT * FROM listening_statistics")
  public suspend fun getAll(): List<ListeningStatistic>

  @Query("DELETE FROM listening_statistics")
  public suspend fun clearAll()
}
