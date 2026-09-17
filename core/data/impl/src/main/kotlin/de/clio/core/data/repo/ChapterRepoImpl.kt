package de.clio.core.data.repo

import de.clio.core.data.Chapter
import de.clio.core.data.ChapterId
import de.clio.core.data.repo.internals.dao.ChapterDao
import de.clio.core.data.runForMaxSqlVariableNumber
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@ContributesBinding(AppScope::class)
public class ChapterRepoImpl(private val dao: ChapterDao) : ChapterRepo {

  private val mutex = Mutex()
  private val cache = mutableMapOf<ChapterId, Chapter?>()

  override suspend fun get(id: ChapterId): Chapter? = mutex.withLock {
    // this does not use getOrPut because a `null` value should also be cached
    if (!cache.containsKey(id)) {
      cache[id] = dao.chapter(id)
    }
    cache[id]
  }

  internal suspend fun warmup(ids: List<ChapterId>): Unit = mutex.withLock {
    val missing = ids.filter { it !in cache }
    missing
      .runForMaxSqlVariableNumber {
        dao.chapters(it)
      }
      .forEach { cache[it.id] = it }
  }

  override suspend fun put(chapter: Chapter): Unit = mutex.withLock {
    dao.insert(chapter)
    cache[chapter.id] = chapter
  }
}
