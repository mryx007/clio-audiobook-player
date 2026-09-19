package de.clio.core.data.repo

import androidx.datastore.core.DataStore
import de.clio.core.common.DispatcherProvider
import de.clio.core.common.MainScope
import de.clio.core.data.BookId
import de.clio.core.data.store.BookQueueStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
public class BookQueueRepositoryImpl(
  @BookQueueStore
  private val store: DataStore<List<BookId>>,
  private val dispatchers: DispatcherProvider = DispatcherProvider(),
) : BookQueueRepository {

  private val scope: CoroutineScope = MainScope(dispatchers)

  override val queueFlow: StateFlow<List<BookId>> = store.data
    .map { it.distinct() }
    .stateIn(
      scope = scope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList(),
    )

  override suspend fun addToQueue(bookIds: List<BookId>) {
    withContext(dispatchers.io) {
      store.updateData { current ->
        val currentSet = current.toSet()
        val toAdd = bookIds.filter { it !in currentSet }
        (current + toAdd).distinct()
      }
    }
  }

  override suspend fun removeFromQueue(bookIds: Set<BookId>) {
    withContext(dispatchers.io) {
      store.updateData { current ->
        current.filter { it !in bookIds }.distinct()
      }
    }
  }

  override suspend fun popNext(): BookId? {
    return withContext(dispatchers.io) {
      var popped: BookId? = null
      store.updateData { current ->
        val distinct = current.distinct()
        if (distinct.isEmpty()) {
          distinct
        } else {
          popped = distinct.first()
          distinct.drop(1)
        }
      }
      popped
    }
  }

  override suspend fun reorder(bookIds: List<BookId>) {
    withContext(dispatchers.io) {
      store.updateData { bookIds.distinct() }
    }
  }

  override suspend fun clearQueue() {
    withContext(dispatchers.io) {
      store.updateData { emptyList() }
    }
  }
}
