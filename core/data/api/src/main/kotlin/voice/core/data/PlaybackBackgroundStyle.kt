package voice.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class PlaybackBackgroundStyle {
  @SerialName("Solid")
  Solid,

  @SerialName("BlurredCover")
  BlurredCover,

  @SerialName("DimmedCover")
  DimmedCover,

  @SerialName("DynamicGradient")
  DynamicGradient,

  @SerialName("AmbientColors")
  AmbientColors,

  @SerialName("AmoledBlack")
  AmoledBlack,

  @SerialName("Glassmorphism")
  Glassmorphism,
}
