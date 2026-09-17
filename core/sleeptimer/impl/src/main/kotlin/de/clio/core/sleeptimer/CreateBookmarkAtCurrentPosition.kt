package de.clio.core.sleeptimer

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import de.clio.core.data.BookId
import de.clio.core.data.repo.BookmarkRepo
import de.clio.core.data.store.CurrentBookStore
import de.clio.core.playback.CurrentBookResolver

@Inject
class CreateBookmarkAtCurrentPosition(
  private val bookmarkRepo: BookmarkRepo,
  private val currentBookResolver: CurrentBookResolver,
  @CurrentBookStore
  private val currentBookStore: DataStore<BookId?>,
) {

  suspend fun create() {
    val currentBookId = currentBookStore.data.first() ?: return
    val currentBook = currentBookResolver.book(currentBookId) ?: return
    bookmarkRepo.addBookmarkAtBookPosition(
      book = currentBook,
      title = null,
      setBySleepTimer = true,
    )
  }
}
