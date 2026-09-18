package de.clio.core.scanner

import de.clio.core.data.folders.AudiobookFolders
import de.clio.core.data.folders.FolderType
import de.clio.core.data.repo.BookRepository
import de.clio.core.documentfile.CachedDocumentFile
import de.clio.core.documentfile.CachedDocumentFileFactory
import de.clio.core.logging.api.Logger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.measureTime

@SingleIn(AppScope::class)
@Inject
public class MediaScanTrigger
internal constructor(
  private val audiobookFolders: AudiobookFolders,
  private val scanner: MediaScanner,
  private val coverScanner: CoverScanner,
  private val bookRepo: BookRepository,
  private val documentFileFactory: CachedDocumentFileFactory,
) {

  public val scannerActive: Flow<Boolean>
    field = MutableStateFlow(false)

  private val scope = CoroutineScope(Dispatchers.IO)
  private var scanningJob: Job? = null

  public fun scan(restartIfScanning: Boolean = false) {
    Logger.i("scanForFiles with restartIfScanning=$restartIfScanning")
    if (scanningJob?.isActive == true && !restartIfScanning) {
      return
    }
    val oldJob = scanningJob
    scanningJob = scope.launch {
      try {
        oldJob?.cancelAndJoin()
        scannerActive.value = true

        measureTime {
          val folders: Map<FolderType, List<CachedDocumentFile>> = audiobookFolders.all()
            .first()
            .mapValues { (_, documentFilesWithUri) ->
              documentFilesWithUri.map {
                documentFileFactory.create(it.documentFile.uri)
              }
            }
          scanner.scan(folders)
        }.also {
          Logger.i("scan took $it")
        }

        scannerActive.value = false

        val books = bookRepo.all()
        coverScanner.scan(books)
      } finally {
        scannerActive.value = false
      }
    }
  }
}
