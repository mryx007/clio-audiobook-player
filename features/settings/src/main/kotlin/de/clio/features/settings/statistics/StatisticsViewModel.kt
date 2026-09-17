package de.clio.features.settings.statistics

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import de.clio.core.common.DispatcherProvider
import de.clio.core.common.MainScope
import de.clio.core.data.StatisticsSummary
import de.clio.core.data.repo.ListeningStatisticRepository
import de.clio.core.logging.api.Logger
import de.clio.navigation.Navigator
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Inject
public class StatisticsViewModel(
  private val navigator: Navigator,
  private val statisticRepo: ListeningStatisticRepository,
  private val context: Context,
  dispatcherProvider: DispatcherProvider,
) {

  private val scope = MainScope(dispatcherProvider)
  private val _viewEffects = Channel<StatisticsViewEffect>(Channel.BUFFERED)
  public val viewEffects = _viewEffects.receiveAsFlow()

  private var showClearDialogState by mutableStateOf(false)
  private var isImportingState by mutableStateOf(false)
  private var selectedTabState by mutableStateOf(StatisticsTab.YEARS)
  private var selectedYearState by mutableStateOf<String?>(null)
  private var selectedMonthIndexState by mutableStateOf<Int?>(null)

  public fun selectTab(tab: StatisticsTab) {
    selectedTabState = tab
  }

  public fun onYearClick(year: String) {
    selectedYearState = if (selectedYearState == year) null else year
    selectedMonthIndexState = null
  }

  public fun onMonthClick(monthIndex: Int) {
    selectedMonthIndexState = if (selectedMonthIndexState == monthIndex) null else monthIndex
  }

  public fun clearSelectedYear() {
    selectedYearState = null
    selectedMonthIndexState = null
  }

  @Composable
  @SuppressLint("NonObservableLocale")
  public fun viewState(): StatisticsViewState {
    val summary: StatisticsSummary? by remember {
      statisticRepo.getStatisticsSummary()
    }.collectAsState()

    val currentSummary = summary ?: StatisticsSummary(
      totalSeconds = 0L,
      thisMonthSeconds = 0L,
      booksCount = 0,
      monthlyStats = emptyList(),
      bookStats = emptyList(),
    )

    val maxMonthlySeconds = currentSummary.monthlyStats.maxOfOrNull { it.totalSeconds }?.coerceAtLeast(1L) ?: 1L
    val maxBookSeconds = currentSummary.bookStats.maxOfOrNull { it.totalSeconds }?.coerceAtLeast(1L) ?: 1L

    val selectedYear = selectedYearState
    val selectedYearData = if (selectedYear != null) {
      val yearMonths = currentSummary.monthlyStats.filter { it.yearMonth.startsWith("$selectedYear-") }
      val totalYearSecs = yearMonths.sumOf { it.totalSeconds }
      val maxMonthSecs = yearMonths.maxOfOrNull { it.totalSeconds }?.coerceAtLeast(1L) ?: 1L
      val monthMap = yearMonths.associateBy { it.yearMonth.substringAfter('-').toIntOrNull() ?: 0 }

      val maxHoursDouble = maxMonthSecs / 3600.0
      val (chartCeilingHours, yLevels) = calculateChartScale(maxHoursDouble)
      val chartCeilingSecs = (chartCeilingHours * 3600.0).coerceAtLeast(1.0)

      val hourUnit = if (Locale.getDefault().language == "de") "Std." else "h"
      val minUnit = if (Locale.getDefault().language == "de") "Min." else "min"

      val yAxisLabels = yLevels.map { level ->
        if (level % 1.0 == 0.0) {
          "${level.toInt()} $hourUnit"
        } else {
          String.format(Locale.getDefault(), "%.1f %s", level, hourUnit)
        }
      }

      val bars = (1..12).map { m ->
        val secs = monthMap[m]?.totalSeconds ?: 0L
        val heightFraction = if (secs > 0L) (secs.toFloat() / chartCeilingSecs.toFloat()).coerceIn(0.08f, 1f) else 0f
        val hours = secs / 3600.0
        val formatted = if (secs >= 3600L) {
          String.format(Locale.getDefault(), "%.1f %s", hours, hourUnit)
        } else if (secs > 0L) {
          "${(secs / 60).coerceAtLeast(1)} $minUnit"
        } else {
          "0 $hourUnit"
        }
        val label = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val fullLabel = Month.of(m).getDisplayName(TextStyle.FULL, Locale.getDefault())
        StatisticsViewState.MonthBarData(
          month = m,
          label = label,
          fullLabel = fullLabel,
          totalSeconds = secs,
          formattedDuration = formatted,
          heightFraction = heightFraction,
        )
      }

      val hours = totalYearSecs / 3600.0
      val suffix = if (Locale.getDefault().language == "de") "Stunden" else "hours"
      StatisticsViewState.SelectedYearData(
        year = selectedYear,
        formattedDuration = String.format(Locale.getDefault(), "%.1f %s", hours, suffix),
        yAxisLabels = yAxisLabels,
        bars = bars,
      )
    } else {
      null
    }

    val yearlyItems = currentSummary.monthlyStats
      .groupBy { it.yearMonth.substringBefore('-', missingDelimiterValue = "") }
      .filterKeys { it.length == 4 }
      .map { (year, stats) ->
        val totalSecs = stats.sumOf { it.totalSeconds }
        val hours = totalSecs / 3600.0
        val suffix = if (Locale.getDefault().language == "de") "Stunden" else "hours"
        val formatted = String.format(Locale.getDefault(), "%.1f %s", hours, suffix)
        StatisticsViewState.YearlyStatItem(
          year = year,
          formattedDuration = formatted,
          totalSeconds = totalSecs,
          isSelected = (year == selectedYear),
        )
      }
      .sortedByDescending { it.year }

    val monthlyItems = currentSummary.monthlyStats.map { month ->
      val displayMonth = formatMonthLabel(month.yearMonth)
      StatisticsViewState.MonthlyStatItem(
        yearMonth = month.yearMonth,
        displayMonth = displayMonth,
        formattedDuration = formatDuration(month.totalSeconds),
        totalSeconds = month.totalSeconds,
        progressFraction = (month.totalSeconds.toFloat() / maxMonthlySeconds).coerceIn(0f, 1f),
      )
    }

    val bookItems = currentSummary.bookStats.map { book ->
      StatisticsViewState.BookStatItem(
        bookTitle = book.bookTitle,
        coverUrl = book.coverUrl,
        formattedDuration = formatDuration(book.totalSeconds),
        totalSeconds = book.totalSeconds,
        progressFraction = (book.totalSeconds.toFloat() / maxBookSeconds).coerceIn(0f, 1f),
      )
    }

    return StatisticsViewState(
      isLoading = summary == null,
      totalFormattedTime = formatDuration(currentSummary.totalSeconds),
      thisMonthFormattedTime = formatDuration(currentSummary.thisMonthSeconds),
      booksListenedCount = currentSummary.booksCount,
      selectedTab = selectedTabState,
      selectedYear = selectedYear,
      selectedYearData = selectedYearData,
      selectedMonthIndex = selectedMonthIndexState,
      yearlyStats = yearlyItems,
      monthlyStats = monthlyItems,
      bookStats = bookItems,
      isImporting = isImportingState,
      showClearDialog = showClearDialogState,
    )
  }

  public fun close() {
    navigator.goBack()
  }

  public fun onClearClick() {
    showClearDialogState = true
  }

  public fun onDismissClearDialog() {
    showClearDialogState = false
  }

  public fun onConfirmClear() {
    showClearDialogState = false
    scope.launch {
      statisticRepo.clearStatistics()
    }
  }

  public fun importXml(uri: Uri) {
    importFromUri(uri)
  }

  @SuppressLint("Recycle")
  public fun importFromUri(uri: Uri) {
    if (isImportingState) return
    isImportingState = true
    scope.launch {
      try {
        var xmlContent: String? = null
        var coverProvider: (suspend (rawPath: String, bookTitle: String) -> InputStream?)? = null

        val treeDoc = try {
          DocumentFile.fromTreeUri(context, uri)
        } catch (_: Exception) {
          null
        }

        if (treeDoc != null && treeDoc.isDirectory) {
          var xmlDoc: DocumentFile? = null
          var imageFolder: DocumentFile = treeDoc

          val directXml = treeDoc.listFiles().firstOrNull { it.isFile && it.name?.endsWith(".xml", ignoreCase = true) == true }
          if (directXml != null) {
            xmlDoc = directXml
            imageFolder = treeDoc
          } else {
            for (sub in treeDoc.listFiles()) {
              if (sub.isDirectory) {
                val subXml = sub.listFiles().firstOrNull { it.isFile && it.name?.endsWith(".xml", ignoreCase = true) == true }
                if (subXml != null) {
                  xmlDoc = subXml
                  imageFolder = sub
                  break
                }
              }
            }
          }

          if (xmlDoc != null) {
            xmlContent = context.contentResolver.openInputStream(xmlDoc.uri)?.use { stream ->
              stream.bufferedReader(Charsets.UTF_8).readText()
            }

            val imageFiles = mutableListOf<DocumentFile>()
            imageFolder.listFiles().forEach { file ->
              if (file.isFile && isImageFile(file.name)) {
                imageFiles.add(file)
              }
            }
            if (imageFolder != treeDoc) {
              treeDoc.listFiles().forEach { file ->
                if (file.isFile && isImageFile(file.name)) {
                  imageFiles.add(file)
                }
              }
            }

            val imagesByExactBase = mutableMapOf<String, DocumentFile>()
            val imagesByNormBase = mutableMapOf<String, DocumentFile>()

            for (img in imageFiles) {
              val name = img.name ?: continue
              val base = name.substringBeforeLast('.')
              imagesByExactBase[base] = img
              imagesByNormBase[normalizeForMatching(base)] = img
            }

            coverProvider = { rawPath, bookTitle ->
              val cleanRaw = rawPath.replace('\\', '/').trimEnd('/').substringAfterLast('/')
              val candidate = imagesByExactBase[rawPath]
                ?: imagesByExactBase[bookTitle]
                ?: imagesByExactBase[cleanRaw]
                ?: imagesByNormBase[normalizeForMatching(rawPath)]
                ?: imagesByNormBase[normalizeForMatching(bookTitle)]
                ?: imagesByNormBase[normalizeForMatching(cleanRaw)]

              candidate?.let { context.contentResolver.openInputStream(it.uri) }
            }
          }
        } else {
          xmlContent = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
          }
        }

        if (!xmlContent.isNullOrBlank()) {
          val result = statisticRepo.importSmartAudioBookPlayerXml(xmlContent, coverProvider)
          val hours = result.totalSecondsImported / 3600
          _viewEffects.send(
            StatisticsViewEffect.ShowMessage(
              "${result.booksImported} Hörbücher ($hours Std.) importiert",
            ),
          )
        } else {
          _viewEffects.send(StatisticsViewEffect.ShowMessage("Keine gültige XML-Datei gefunden"))
        }
      } catch (e: Exception) {
        Logger.e(e, "Error importing statistics")
        _viewEffects.send(StatisticsViewEffect.ShowMessage("Fehler beim Importieren: ${e.message}"))
      } finally {
        isImportingState = false
      }
    }
  }

  private fun isImageFile(name: String?): Boolean {
    if (name == null) return false
    val lower = name.lowercase()
    return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")
  }

  private fun normalizeForMatching(s: String): String {
    val sb = StringBuilder(s.length)
    for (c in s.lowercase()) {
      when (c) {
        ' ', '_', '.', '-', '(', ')', '[', ']', ',', '\'', '"', ':', '!', '?' -> {}
        'ä' -> sb.append("ae")
        'ö' -> sb.append("oe")
        'ü' -> sb.append("ue")
        'ß' -> sb.append("ss")
        else -> sb.append(c)
      }
    }
    return sb.toString()
  }

  private fun formatMonthLabel(yearMonthStr: String): String {
    return try {
      val ym = YearMonth.parse(yearMonthStr)
      val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
      ym.format(formatter)
    } catch (_: Exception) {
      yearMonthStr
    }
  }

  private fun formatDuration(totalSeconds: Long): String {
    if (totalSeconds <= 0L) return "0 Min."
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return when {
      hours > 0 && minutes > 0 -> "$hours Std. $minutes Min."
      hours > 0 -> "$hours Std."
      else -> "${minutes.coerceAtLeast(1)} Min."
    }
  }

  private fun calculateChartScale(maxHours: Double): Pair<Double, List<Double>> {
    val rawMax = maxHours.coerceAtLeast(0.1)
    val rawStep = rawMax / 4.0
    val step = when {
      rawStep <= 0.25 -> 0.25
      rawStep <= 0.5 -> 0.5
      rawStep <= 1.0 -> 1.0
      rawStep <= 2.0 -> 2.0
      rawStep <= 2.5 -> 2.5
      rawStep <= 5.0 -> 5.0
      rawStep <= 10.0 -> 10.0
      rawStep <= 15.0 -> 15.0
      rawStep <= 20.0 -> 20.0
      rawStep <= 25.0 -> 25.0
      rawStep <= 50.0 -> 50.0
      else -> kotlin.math.ceil(rawStep / 25.0) * 25.0
    }
    val count = kotlin.math.ceil(rawMax / step).toInt().coerceIn(3, 5)
    val ceiling = step * count
    val levels = (count downTo 0).map { it * step }
    return ceiling to levels
  }
}
