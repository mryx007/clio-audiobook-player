package de.clio.core.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
public enum class BackButtonBehavior {
  BookOverview,
  @OptIn(ExperimentalSerializationApi::class)
  @JsonNames("CloseApp")
  MinimizeApp,
}
