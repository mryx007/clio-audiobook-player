package de.clio.core.data.repo

import de.clio.core.data.BookId
import kotlinx.coroutines.flow.StateFlow

public interface BookQueueRepository {

  public val queueFlow: StateFlow<List<BookId>>

  public suspend fun addToQueue(bookIds: List<BookId>)

  public suspend fun removeFromQueue(bookIds: Set<BookId>)

  public suspend fun popNext(): BookId?

  public suspend fun reorder(bookIds: List<BookId>)

  public suspend fun clearQueue()
}
