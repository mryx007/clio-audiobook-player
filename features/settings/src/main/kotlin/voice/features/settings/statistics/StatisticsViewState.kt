package voice.features.settings.statistics

public data class StatisticsViewState(
  val totalFormattedTime: String,
  val thisMonthFormattedTime: String,
  val booksListenedCount: Int,
  val monthlyStats: List<MonthlyStatItem>,
  val bookStats: List<BookStatItem>,
  val isImporting: Boolean = false,
  val showClearDialog: Boolean = false,
) {
  public data class MonthlyStatItem(
    val yearMonth: String,
    val displayMonth: String,
    val formattedDuration: String,
    val totalSeconds: Long,
    val progressFraction: Float,
  )

  public data class BookStatItem(
    val bookTitle: String,
    val coverUrl: String? = null,
    val formattedDuration: String,
    val totalSeconds: Long,
    val progressFraction: Float,
  )
}
