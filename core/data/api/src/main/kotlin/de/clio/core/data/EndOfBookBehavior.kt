package de.clio.core.data

import kotlinx.serialization.Serializable

@Serializable
public enum class EndOfBookBehavior {
  DoNothing,
  BookOverview,
  ContinueQueue,
}
