package de.clio.core.data.repo

import de.clio.core.data.BookId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

public class FakeBookQueueRepository(
  initial: List<BookId> = emptyList(),
) : BookQueueRepository {

  private val _queueFlow = MutableStateFlow(initial)
  override val queueFlow: StateFlow<List<BookId>> = _queueFlow.asStateFlow()

  override suspend fun addToQueue(bookIds: List<BookId>) {
    _queueFlow.update { current ->
      val currentSet = current.toSet()
      current + bookIds.filter { it !in currentSet }
    }
  }

  override suspend fun removeFromQueue(bookIds: Set<BookId>) {
    _queueFlow.update { current ->
      current.filter { it !in bookIds }
    }
  }

  override suspend fun popNext(): BookId? {
    var next: BookId? = null
    _queueFlow.update { current ->
      if (current.isNotEmpty()) {
        next = current.first()
        current.drop(1)
      } else {
        current
      }
    }
    return next
  }

  override suspend fun reorder(bookIds: List<BookId>) {
    _queueFlow.value = bookIds
  }

  override suspend fun clearQueue() {
    _queueFlow.value = emptyList()
  }
}
