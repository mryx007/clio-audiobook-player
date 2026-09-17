package de.clio.core.scanner.matroska

import de.clio.core.data.MarkData

internal data class MatroskaMediaInfo(
  val album: String? = null,
  val artist: String? = null,
  val title: String? = null,
  val chapters: List<MarkData> = emptyList(),
)
