package de.clio.core.data

private val CHAPTER_KEYWORD_REGEX = Regex("""\b(?:kapitel|chapter|teil|part|track|abschnitt)\b""", RegexOption.IGNORE_CASE)
private val LEADING_NUMBER_REGEX = Regex("""^(\d+)\s*(?:[-–—._]\s*|\s+)(.+)$""")
private val TRAILING_NUMBER_REGEX = Regex("""[-–—._\s]\d+$""")

public fun formatChapterName(name: String, author: String? = null): String {
  var working = name.trim()
  if (working.isEmpty()) return working

  // Replace underscores with spaces early
  working = working.replace('_', ' ')

  // If author is at the start (e.g. "Candice Fox - Crimson Lake 082")
  if (!author.isNullOrBlank()) {
    working = removeAuthorPrefix(working, author)
  }

  val hasTrailingNumber = TRAILING_NUMBER_REGEX.containsMatchIn(working)
  val match = LEADING_NUMBER_REGEX.matchEntire(working)
  var result = if (match != null && !hasTrailingNumber) {
    val number = match.groupValues[1]
    val rawRest = match.groupValues[2].trim()
    val rest = rawRest.replaceFirst(Regex("""^[-–—._\s]+"""), "").trim()
    if (CHAPTER_KEYWORD_REGEX.containsMatchIn(rest)) {
      rest
    } else if (rest.isNotEmpty()) {
      "$rest $number"
    } else {
      number
    }
  } else if (match != null && hasTrailingNumber) {
    val rawRest = match.groupValues[2].trim()
    val rest = rawRest.replaceFirst(Regex("""^[-–—._\s]+"""), "").trim()
    if (CHAPTER_KEYWORD_REGEX.containsMatchIn(rest)) {
      rest
    } else {
      working
    }
  } else {
    working
  }

  // Remove hyphen/dash before trailing number: e.g. "Goldwäsche - 05" -> "Goldwäsche 05"
  result = result.replace(Regex("""\s*[-–—]\s*(\d+)$"""), " $1")

  // Collapse multiple spaces
  result = result.replace(Regex("""\s+"""), " ").trim()

  // In case author was preceded by a leading number (e.g. "01 - Candice Fox - Crimson Lake" -> "Candice Fox - Crimson Lake 01")
  if (!author.isNullOrBlank()) {
    result = removeAuthorPrefix(result, author)
  }

  return result
}

public fun removeAuthorPrefix(name: String, author: String?): String {
  if (author.isNullOrBlank()) return name
  val cleanAuthor = author.trim()
  if (cleanAuthor.isEmpty()) return name

  val candidates = mutableListOf<String>()
  val rawAuthors = cleanAuthor.split(Regex("""[;&]"""))
    .map { it.trim() }
    .filter { it.length > 1 } + listOf(cleanAuthor)

  for (auth in rawAuthors) {
    candidates.add(auth)
    val commaParts = auth.split(",")
    if (commaParts.size == 2) {
      val first = commaParts[1].trim()
      val last = commaParts[0].trim()
      if (first.isNotEmpty() && last.isNotEmpty()) {
        candidates.add("$first $last")
        candidates.add("$last $first")
      }
    } else {
      val spaceWords = auth.trim().split(Regex("""\s+"""))
      if (spaceWords.size == 2) {
        val first = spaceWords[0]
        val last = spaceWords[1]
        candidates.add("$last, $first")
        candidates.add("$last $first")
      }
    }
  }

  var result = name
  for (auth in candidates.distinct().sortedByDescending { it.length }) {
    val words = auth.trim().split(Regex("""\s+""")).map { Regex.escape(it) }
    val escaped = words.joinToString("""[ _]+""")
    val pattern = Regex("""^$escaped\s*(?:[-–—:_.]\s*|\s+)""", RegexOption.IGNORE_CASE)
    val match = pattern.find(result)
    if (match != null) {
      val afterAuthor = result.substring(match.range.last + 1).trim()
        .replaceFirst(Regex("""^[-–—:_.\s]+"""), "")
        .trim()
      if (afterAuthor.isNotEmpty()) {
        result = afterAuthor
        break
      }
    }
  }
  return result
}

public fun isAlbumOrBookTitle(
  title: String,
  album: String?,
  fileName: String? = null,
  author: String? = null,
): Boolean {
  if (CHAPTER_KEYWORD_REGEX.containsMatchIn(title)) {
    return false
  }

  val cleanTitle = if (!author.isNullOrBlank()) removeAuthorPrefix(title, author) else title

  if (!album.isNullOrBlank()) {
    val t = cleanTitle.lowercase()
    val a = album.lowercase()
    if (t == a || a.contains(t) || t.contains(a)) {
      return true
    }
  }

  if (album == null && !fileName.isNullOrBlank()) {
    val cleanBase = fileName.replace(Regex("""\d+"""), "").trim().trim('-', '_', '.')
    val t = cleanTitle.trim().trim('-', '_', '.')
    if (cleanBase.isNotEmpty() && (cleanBase.equals(t, ignoreCase = true) || t.contains(cleanBase, ignoreCase = true) || cleanBase.contains(t, ignoreCase = true))) {
      return true
    }
  }

  return false
}

public fun resolveChapterName(
  title: String?,
  album: String?,
  fileName: String,
  author: String? = null,
): String {
  val cleanTitle = title?.trim()
  val cleanAlbum = album?.trim()

  val isExplicit = !cleanTitle.isNullOrBlank() &&
    cleanTitle != fileName &&
    !isAlbumOrBookTitle(cleanTitle, cleanAlbum, fileName, author = author)

  val rawName = if (isExplicit) cleanTitle else fileName
  return formatChapterName(rawName, author)
}

public fun formatDisplayChapterName(
  chapterName: String?,
  bookName: String?,
  chapterUri: String?,
  author: String? = null,
): String? {
  val cleanChapter = chapterName?.trim()
  val isExplicit = !cleanChapter.isNullOrBlank() && !isAlbumOrBookTitle(cleanChapter, bookName, author = author)
  val raw = if (isExplicit) {
    cleanChapter
  } else if (!chapterUri.isNullOrBlank()) {
    val decoded = android.net.Uri.decode(chapterUri)
    val lastSegment = decoded.substringAfterLast('/').substringBeforeLast('.')
    lastSegment.ifBlank { cleanChapter }
  } else {
    cleanChapter
  } ?: return null
  return formatChapterName(raw, author)
}
