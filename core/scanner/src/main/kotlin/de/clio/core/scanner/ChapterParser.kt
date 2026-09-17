package de.clio.core.scanner

import de.clio.core.data.Chapter
import de.clio.core.data.ChapterId
import de.clio.core.data.isAudioFile
import de.clio.core.data.repo.ChapterRepo
import de.clio.core.data.repo.getOrPut
import de.clio.core.documentfile.CachedDocumentFile
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Instant

internal data class ChapterParseResult(
  val chapters: List<Chapter>,
  val firstChapterMetadata: Metadata?,
)

@Inject
internal class ChapterParser(
  private val chapterRepo: ChapterRepo,
  private val mediaAnalyzer: MediaAnalyzer,
) {

  suspend fun parse(documentFile: CachedDocumentFile): ChapterParseResult = coroutineScope {
    fun collectAudioFiles(file: CachedDocumentFile): List<CachedDocumentFile> {
      if (file.isAudioFile()) return listOf(file)
      if (file.isDirectory) return file.children.flatMap { collectAudioFiles(it) }
      return emptyList()
    }

    val audioFiles = collectAudioFiles(documentFile)
    if (audioFiles.isEmpty()) {
      return@coroutineScope ChapterParseResult(emptyList(), null)
    }

    val parsedList = audioFiles.map { file ->
      async(Dispatchers.IO) {
        val id = ChapterId(file.uri)
        var parsedMeta: Metadata? = null
        val chapter = chapterRepo.getOrPut(
          id = id,
          lastModified = Instant.ofEpochMilli(file.lastModified),
          fileSize = file.length,
        ) {
          val metaData = mediaAnalyzer.analyze(file) ?: return@getOrPut null
          parsedMeta = metaData
          Chapter(
            id = id,
            duration = metaData.duration,
            fileLastModified = Instant.ofEpochMilli(file.lastModified),
            name = metaData.title ?: metaData.fileName,
            markData = metaData.chapters,
            fileSize = file.length,
          )
        }
        chapter?.let { it to parsedMeta }
      }
    }.awaitAll().filterNotNull()

    val chapters = parsedList.map { it.first }.sorted()
    val firstChapterId = chapters.firstOrNull()?.id
    val firstChapterMetadata = parsedList.firstOrNull { it.first.id == firstChapterId }?.second

    ChapterParseResult(
      chapters = chapters,
      firstChapterMetadata = firstChapterMetadata,
    )
  }
}
