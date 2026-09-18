package de.clio.features.bookOverview.deleteBook

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.documentfile.provider.DocumentFile
import de.clio.core.data.BookId
import de.clio.core.logging.api.Logger
import de.clio.core.scanner.MediaScanTrigger
import de.clio.features.bookOverview.bottomSheet.BottomSheetItem
import de.clio.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import de.clio.features.bookOverview.di.BookOverviewScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@SingleIn(BookOverviewScope::class)
@ContributesIntoSet(BookOverviewScope::class)
class DeleteBookViewModel(
  private val application: Application,
  private val mediaScanTrigger: MediaScanTrigger,
) : BottomSheetItemViewModel {

  private val scope = MainScope()

  private val _state = mutableStateOf<DeleteBookViewState?>(null)
  internal val state: State<DeleteBookViewState?> get() = _state

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    return listOf(BottomSheetItem.DeleteBook)
  }

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    if (item != BottomSheetItem.DeleteBook) return

    _state.value = DeleteBookViewState(
      ids = setOf(bookId),
      fileToDelete = bookId.toUri().pathSegments
        .let { segments ->
          val result = segments.lastOrNull()?.removePrefix("primary:")
          if (result.isNullOrEmpty()) {
            Logger.w("Could not determine path for $segments")
            segments.joinToString(separator = "\"")
          } else {
            result
          }
        },
    )
  }

  fun onDeleteMultiple(bookIds: Set<BookId>, onDeleted: () -> Unit = {}) {
    if (bookIds.isEmpty()) return
    val description = if (bookIds.size == 1) {
      bookIds.first().toUri().pathSegments
        .let { segments ->
          val result = segments.lastOrNull()?.removePrefix("primary:")
          if (result.isNullOrEmpty()) {
            Logger.w("Could not determine path for $segments")
            segments.joinToString(separator = "\"")
          } else {
            result
          }
        }
    } else {
      "${bookIds.size} Bücher"
    }

    _state.value = DeleteBookViewState(
      ids = bookIds,
      fileToDelete = description,
    )
    this.onDeletedCallback = onDeleted
  }

  private var onDeletedCallback: (() -> Unit)? = null

  internal fun onDismiss() {
    _state.value = null
    onDeletedCallback = null
  }

  internal fun onConfirmDeletion() {
    val state = _state.value
    if (state != null) {
      val callback = onDeletedCallback
      scope.launch {
        state.ids.forEach { id ->
          val uri = id.toUri()
          val documentFile = DocumentFile.fromSingleUri(application, uri)
          documentFile?.delete()
        }
        mediaScanTrigger.scan(restartIfScanning = true)
        callback?.invoke()
      }
    }
    _state.value = null
    onDeletedCallback = null
  }
}

data class DeleteBookViewState(
  val ids: Set<BookId>,
  val fileToDelete: String,
) {
  val id: BookId get() = ids.first()
  val count: Int get() = ids.size
}


