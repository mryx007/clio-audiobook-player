package de.clio.core.data.repo.internals

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.clio.core.data.BookContent
import de.clio.core.data.Bookmark
import de.clio.core.data.Chapter
import de.clio.core.data.ListeningStatistic
import de.clio.core.data.RecentBookSearch
import de.clio.core.data.repo.internals.dao.BookContentDao
import de.clio.core.data.repo.internals.dao.BookSearchFts
import de.clio.core.data.repo.internals.dao.BookmarkDao
import de.clio.core.data.repo.internals.dao.ChapterDao
import de.clio.core.data.repo.internals.dao.ListeningStatisticDao
import de.clio.core.data.repo.internals.dao.RecentBookSearchDao
import de.clio.core.data.repo.internals.migrations.Migration56

@Database(
  entities = [
    Chapter::class,
    BookContent::class,
    Bookmark::class,
    BookSearchFts::class,
    RecentBookSearch::class,
    ListeningStatistic::class,
  ],
  version = AppDb.VERSION,
  autoMigrations = [
    AutoMigration(from = 51, to = 52),
    AutoMigration(from = 52, to = 53),
    AutoMigration(from = 54, to = 55),
    AutoMigration(from = 55, to = 56),
    AutoMigration(from = 56, to = 57, spec = Migration56::class),
    AutoMigration(from = 57, to = 58),
    AutoMigration(from = 58, to = 59),
    AutoMigration(from = 59, to = 60),
    AutoMigration(from = 60, to = 61),
    AutoMigration(from = 61, to = 62),
  ],
)
@TypeConverters(Converters::class)
public abstract class AppDb : RoomDatabase() {

  public abstract fun chapterDao(): ChapterDao
  public abstract fun bookContentDao(): BookContentDao
  public abstract fun bookmarkDao(): BookmarkDao
  public abstract fun listeningStatisticDao(): ListeningStatisticDao
  public abstract fun recentBookSearchDao(): RecentBookSearchDao

  internal companion object {
    const val VERSION = 62
    const val DATABASE_NAME = "autoBookDB"
  }
}
