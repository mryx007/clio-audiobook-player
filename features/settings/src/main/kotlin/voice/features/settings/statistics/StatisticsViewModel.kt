package voice.features.settings.statistics

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import voice.core.common.DispatcherProvider
import voice.core.common.MainScope
import voice.core.data.StatisticsSummary
import voice.core.data.repo.ListeningStatisticRepository
import voice.core.logging.api.Logger
import voice.navigation.Navigator
import androidx.documentfile.provider.DocumentFile
import java.io.InputStream
import java.time.YearMonth
import java.time.format.DateTimeFormatter
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

  @Composable
  public fun viewState(): StatisticsViewState {
    val summary: StatisticsSummary? by remember {
      statisticRepo.getStatisticsSummary()
    }.collectAsState(null)

    val currentSummary = summary ?: StatisticsSummary(
      totalSeconds = 0L,
      thisMonthSeconds = 0L,
      booksCount = 0,
      monthlyStats = emptyList(),
      bookStats = emptyList(),
    )

    val maxMonthlySeconds = currentSummary.monthlyStats.maxOfOrNull { it.totalSeconds }?.coerceAtLeast(1L) ?: 1L
    val maxBookSeconds = currentSummary.bookStats.maxOfOrNull { it.totalSeconds }?.coerceAtLeast(1L) ?: 1L

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
      totalFormattedTime = formatDuration(currentSummary.totalSeconds),
      thisMonthFormattedTime = formatDuration(currentSummary.thisMonthSeconds),
      booksListenedCount = currentSummary.booksCount,
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
      hours > 0 && minutes > 0 -> "${hours} Std. ${minutes} Min."
      hours > 0 -> "${hours} Std."
      else -> "${minutes.coerceAtLeast(1)} Min."
    }
  }
}
