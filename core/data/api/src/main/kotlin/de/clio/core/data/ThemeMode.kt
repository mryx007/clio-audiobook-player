package de.clio.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ThemeMode {
  @SerialName("FollowSystem")
  FollowSystem,

  @SerialName("light")
  Light,

  @SerialName("dark")
  Dark,

  @SerialName("Amoled")
  Amoled,

  @SerialName("Dynamic")
  Dynamic,

  @SerialName("CatppuccinMocha")
  CatppuccinMocha,

  @SerialName("DarkGray")
  DarkGray,

  @Deprecated("Replaced by DarkGray")
  @SerialName("ClassicYouTube")
  ClassicYouTube,

  @SerialName("DarkPink")
  DarkPink,

  @SerialName("DarkBlue")
  DarkBlue,

  @SerialName("DarkGreen")
  DarkGreen,

  @SerialName("DarkYellow")
  DarkYellow,

  @SerialName("DarkOrange")
  DarkOrange,

  @SerialName("DarkRed")
  DarkRed,

  @Deprecated("Replaced by preset themes")
  @SerialName("Custom")
  Custom,
}
