package de.clio.core.scanner

import dev.zacsweers.metro.Inject
import de.clio.core.data.Book
import de.clio.core.data.BookContent
import de.clio.core.data.BookId
import de.clio.core.data.Chapter
import de.clio.core.data.repo.BookContentRepo
import de.clio.core.data.repo.getOrPut
import de.clio.core.data.toUri
import de.clio.core.documentfile.CachedDocumentFile
import de.clio.core.documentfile.CachedDocumentFileFactory
import de.clio.core.logging.api.Logger
import java.time.Instant

@Inject
internal class BookParser(
  private val contentRepo: BookContentRepo,
  private val mediaAnalyzer: MediaAnalyzer,
  private val fileFactory: CachedDocumentFileFactory,
) {

  suspend fun parseAndStore(
    chapters: List<Chapter>,
    file: CachedDocumentFile,
    firstChapterMetadata: Metadata?,
  ): BookContent {
    val id = BookId(file.uri)
    val existing = contentRepo.get(id)
    if (existing != null) {
      val (cleanedName, extractedNarrator) = cleanTitleAndExtractNarrator(existing.name)
      val needsNameUpdate = cleanedName != existing.name
      val needsAuthorUpdate = existing.author.isNullOrBlank()
      val needsNarratorUpdate = existing.narrator.isNullOrBlank() && extractedNarrator != null

      if (needsNameUpdate || needsAuthorUpdate || needsNarratorUpdate) {
        val analyzed = if (needsAuthorUpdate) {
          firstChapterMetadata
            ?: mediaAnalyzer.analyze(fileFactory.create(chapters.first().id.toUri()))
        } else null

        val (newAuthor, _) = if (needsAuthorUpdate) {
          resolveAuthorAndName(
            explicitAuthor = analyzed?.artist,
            explicitAlbum = analyzed?.album,
            explicitTitle = analyzed?.title,
            isFile = file.isFile,
            rawFallbackName = file.bookName(),
          )
        } else Pair(existing.author, existing.name)

        val updated = existing.copy(
          author = newAuthor?.takeIf { it.isNotBlank() } ?: existing.author,
          name = cleanedName,
          narrator = existing.narrator ?: extractedNarrator,
        )
        contentRepo.put(updated)
        return updated
      }
      return existing
    }
    val analyzed = firstChapterMetadata
      ?: mediaAnalyzer.analyze(fileFactory.create(chapters.first().id.toUri()))
    val parsed = parse(chapters, id, analyzed, file)
    contentRepo.put(parsed)
    return parsed
  }

  fun parse(
    chapters: List<Chapter>,
    id: BookId,
    analyzed: Metadata?,
    file: CachedDocumentFile,
  ): BookContent {
    val (author, rawName) = resolveAuthorAndName(
      explicitAuthor = analyzed?.artist,
      explicitAlbum = analyzed?.album,
      explicitTitle = analyzed?.title,
      isFile = file.isFile,
      rawFallbackName = file.bookName(),
    )
    val (cleanedName, extractedNarrator) = cleanTitleAndExtractNarrator(rawName)
    val narrator = analyzed?.narrator ?: extractedNarrator
    return BookContent(
      id = id,
      isActive = true,
      addedAt = Instant.now(),
      author = author,
      lastPlayedAt = Instant.EPOCH,
      name = cleanedName,
      playbackSpeed = 1F,
      skipSilence = false,
      chapters = chapters.map { it.id },
      positionInChapter = 0L,
      currentChapter = chapters.first().id,
      cover = null,
      gain = 0F,
      genre = analyzed?.genre,
      narrator = narrator,
      series = analyzed?.series,
      part = analyzed?.part,
    ).also {
      validateIntegrity(it, chapters)
    }
  }

  internal fun resolveAuthorAndName(
    explicitAuthor: String?,
    explicitAlbum: String?,
    explicitTitle: String?,
    isFile: Boolean,
    rawFallbackName: String,
  ): Pair<String?, String> {
    var author = explicitAuthor?.takeIf { it.isNotBlank() }
    var name = explicitAlbum?.takeIf { it.isNotBlank() }
      ?: explicitTitle?.takeIf { isFile && it.isNotBlank() }

    if (author == null || name == null) {
      val splitMatch = DELIMITER_REGEX.find(rawFallbackName)
      if (splitMatch != null) {
        val leftRaw = rawFallbackName.substring(0, splitMatch.range.first).trim()
        val rightRaw = rawFallbackName.substring(splitMatch.range.last + 1).trim()
        val left = if (leftRaw.contains('_') && !leftRaw.contains(' ')) leftRaw.replace('_', ' ') else leftRaw
        val right = if (rightRaw.contains('_') && !rightRaw.contains(' ')) rightRaw.replace('_', ' ') else rightRaw
        if (left.isNotBlank() && right.isNotBlank() && !NON_AUTHOR_PREFIXES.containsMatchIn(left)) {
          if (author == null) {
            author = left
          }
          if (name == null) {
            name = right
          }
        }
      }
    }

    if (name == null && author != null && rawFallbackName.startsWith(author, ignoreCase = true)) {
      val candidate = rawFallbackName.substring(author.length).trim().removePrefix("-").removePrefix("–").trim()
      if (candidate.isNotBlank()) {
        name = candidate
      }
    }

    return Pair(author, name ?: rawFallbackName)
  }

  private fun CachedDocumentFile.bookName(): String {
    val fileName = name
    return if (fileName == null) {
      uri.toString()
        .removePrefix("/storage/emulated/0/")
        .removePrefix("/storage/emulated/")
        .removePrefix("/storage/")
        .also {
          Logger.w("Could not parse fileName from $this. Fallback to $it")
        }
    } else {
      if (isFile) {
        fileName.substringBeforeLast(".")
      } else {
        fileName
      }
    }
  }
}

internal fun validateIntegrity(
  content: BookContent,
  chapters: List<Chapter>,
) {
  // the init block performs integrity validation
  @Suppress("RETURN_VALUE_NOT_USED")
  Book(content, chapters)
}

internal fun cleanTitleAndExtractNarrator(rawTitle: String): Pair<String, String?> {
  var title = rawTitle.trim()
  var narrator: String? = null

  val parenMatch = NARRATOR_IN_PARENS_REGEX.find(title)
  if (parenMatch != null) {
    narrator = parenMatch.groupValues[1].trim()
    title = title.removeRange(parenMatch.range).trim()
  } else {
    val trailingMatch = NARRATOR_TRAILING_REGEX.find(title)
    if (trailingMatch != null) {
      narrator = trailingMatch.groupValues[1].trim()
      title = title.substring(0, trailingMatch.range.first).trim()
    }
  }

  return Pair(if (title.isNotEmpty()) title else rawTitle, narrator)
}

private val DELIMITER_REGEX = Regex("""(?:\s+[-–—]\s+)|(?:_-_)""")
private val NON_AUTHOR_PREFIXES = Regex("""^(?:cd|track|disc|part|kapitel|chapter|\d+)\b""", RegexOption.IGNORE_CASE)
private val NARRATOR_IN_PARENS_REGEX = Regex(
  """\s*[\(\[]\s*(?:ungekürzt(?:e\s+lesung)?|gekürzt(?:e\s+lesung)?|unabridged|abridged)?\s*,?\s*(?:gelesen\s+von|read\s+by|narrated\s+by)\s+([^()\[\]]+)[\)\]]""",
  RegexOption.IGNORE_CASE
)
private val NARRATOR_TRAILING_REGEX = Regex(
  """\s*[,–-]\s*(?:gelesen\s+von|read\s+by|narrated\s+by)\s+(.+)$""",
  RegexOption.IGNORE_CASE
)

