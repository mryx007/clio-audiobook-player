package voice.features.settings.statistics

public enum class StatisticsTab {
  YEARS,
  BOOKS,
}

public data class StatisticsViewState(
  val isLoading: Boolean = false,
  val totalFormattedTime: String,
  val thisMonthFormattedTime: String,
  val booksListenedCount: Int,
  val selectedTab: StatisticsTab = StatisticsTab.YEARS,
  val selectedYear: String? = null,
  val selectedYearData: SelectedYearData? = null,
  val selectedMonthIndex: Int? = null,
  val yearlyStats: List<YearlyStatItem> = emptyList(),
  val monthlyStats: List<MonthlyStatItem>,
  val bookStats: List<BookStatItem>,
  val isImporting: Boolean = false,
  val showClearDialog: Boolean = false,
) {
  public data class SelectedYearData(
    val year: String,
    val formattedDuration: String,
    val yAxisLabels: List<String> = emptyList(),
    val bars: List<MonthBarData>,
  )

  public data class MonthBarData(
    val month: Int,
    val label: String,
    val fullLabel: String,
    val totalSeconds: Long,
    val formattedDuration: String,
    val heightFraction: Float,
  )

  public data class YearlyStatItem(
    val year: String,
    val formattedDuration: String,
    val totalSeconds: Long,
    val isSelected: Boolean = false,
  )

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
