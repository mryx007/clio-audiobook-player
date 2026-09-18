package de.clio.features.bookOverview.bottomSheet

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.clio.core.data.BookId
import de.clio.features.bookOverview.di.BookOverviewScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@SingleIn(BookOverviewScope::class)
@Inject
class BottomSheetViewModel(private val viewModels: Set<@JvmSuppressWildcards BottomSheetItemViewModel>) {

  private val scope = MainScope()

  internal val state: State<EditBookBottomSheetState>
    field = mutableStateOf(EditBookBottomSheetState(emptyList()))

  var bookId: BookId? = null
    private set

  var selectedBookId: BookId? by mutableStateOf(null)
    private set

  internal fun bookSelected(bookId: BookId) {
    this.bookId = bookId
    scope.launch {
      val items = viewModels.flatMap { it.items(bookId) }
        .toSet()
        .sorted()
      state.value = EditBookBottomSheetState(items)
      selectedBookId = bookId
    }
  }

  internal fun onItemClick(item: BottomSheetItem) {
    val bookId = bookId ?: return
    scope.launch {
      viewModels.forEach {
        it.onItemClick(bookId, item)
      }
    }
  }
}
