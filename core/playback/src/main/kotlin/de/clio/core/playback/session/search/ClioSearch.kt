package de.clio.core.playback.session.search

data class ClioSearch(
  val query: String? = null,
  val mediaFocus: String? = null,
  val album: String? = null,
  val artist: String? = null,
)
