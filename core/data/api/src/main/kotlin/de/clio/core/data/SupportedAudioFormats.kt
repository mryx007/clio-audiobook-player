package de.clio.core.data

import de.clio.core.documentfile.CachedDocumentFile
import de.clio.core.documentfile.walk

private val supportedAudioFormats = setOf(
  "3gp",
  "aac",
  "awb",
  "flac",
  "imy",
  "m4a",
  "m4b",
  "mid",
  "mka",
  "mkv",
  "mp3",
  "mp3package",
  "mp4",
  "mpga",
  "mxmf",
  "oga",
  "ogg",
  "ogx",
  "opus",
  "ota",
  "rtttl",
  "rtx",
  "wav",
  "webm",
  "xmf",
)

private val supportedImageFormats = setOf(
  "jpg",
  "jpeg",
  "png",
  "webp",
  "bmp",
)

public fun CachedDocumentFile.isAudioFile(): Boolean {
  if (!isFile) return false
  val name = name ?: return false
  val extension = name.substringAfterLast(".").lowercase()
  return extension in supportedAudioFormats
}

public fun CachedDocumentFile.isImageFile(): Boolean {
  if (!isFile) return false
  val name = name ?: return false
  val extension = name.substringAfterLast(".").lowercase()
  return extension in supportedImageFormats
}

public fun CachedDocumentFile.audioFileCount(): Int {
  return if (isAudioFile()) {
    1
  } else {
    walk().count { it.isAudioFile() }
  }
}

