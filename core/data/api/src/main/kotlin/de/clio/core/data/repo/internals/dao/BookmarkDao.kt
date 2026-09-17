package de.clio.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.clio.core.data.Bookmark
import de.clio.core.data.ChapterId

@Dao
public interface BookmarkDao {

  @Query("DELETE FROM bookmark2 WHERE id = :id")
  public suspend fun deleteBookmark(id: Bookmark.Id)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun addBookmark(bookmark: Bookmark)

  @Query("SELECT * FROM bookmark2 WHERE chapterId IN(:chapters)")
  public suspend fun allForChapters(chapters: List<@JvmSuppressWildcards ChapterId>): List<Bookmark>
}
