package de.clio.core.data

import kotlinx.serialization.Serializable

@Serializable
public data class PlayerButtonVisibility(
  val showLock: Boolean = true,
  val showEqualizer: Boolean = true,
  val showSleepTimer: Boolean = true,
  val showBookmark: Boolean = true,
  val showSpeed: Boolean = true,
)
