package de.clio.core.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "listening_statistics",
  indices = [
    Index(value = ["yearMonth"]),
    Index(value = ["bookTitle"]),
  ],
)
public data class ListeningStatistic(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val bookId: BookId? = null,
  val bookTitle: String,
  val yearMonth: String,
  val day: String? = null,
  val durationSeconds: Long,
  val coverUrl: String? = null,
)

public data class MonthlyStatistic(
  val yearMonth: String,
  val totalSeconds: Long,
)

public data class BookStatistic(
  val bookTitle: String,
  val bookId: BookId?,
  val totalSeconds: Long,
  val coverUrl: String? = null,
)

public data class StatisticsSummary(
  val totalSeconds: Long,
  val thisMonthSeconds: Long,
  val booksCount: Int,
  val monthlyStats: List<MonthlyStatistic>,
  val bookStats: List<BookStatistic>,
)

public data class ImportResult(
  val booksImported: Int,
  val totalSecondsImported: Long,
)
