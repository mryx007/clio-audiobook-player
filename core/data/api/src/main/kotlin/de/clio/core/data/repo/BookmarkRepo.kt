package de.clio.core.data.repo

import de.clio.core.data.Book
import de.clio.core.data.BookContent
import de.clio.core.data.Bookmark

public interface BookmarkRepo {
  public suspend fun deleteBookmark(id: Bookmark.Id)

  public suspend fun addBookmark(bookmark: Bookmark)

  @IgnorableReturnValue
  public suspend fun addBookmarkAtBookPosition(
    book: Book,
    title: String?,
    setBySleepTimer: Boolean,
  ): Bookmark

  public suspend fun bookmarks(book: BookContent): List<Bookmark>
}
