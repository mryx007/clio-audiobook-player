package de.clio.core.data

import kotlinx.serialization.Serializable

@Serializable
public data class ThemeColor(
  val hex: String = "#0F141C",
  val hue: Int = 212,
  val saturation: Int = 42,
  val lightness: Int = 44,
) {
  public fun parseColor(): Long {
    val clean = hex.removePrefix("#").trim()
    return try {
      when (clean.length) {
        6 -> 0xFF000000L or clean.toLong(16)
        8 -> clean.toLong(16)
        else -> 0xFF0F141CL
      }
    } catch (e: Exception) {
      0xFF0F141CL
    }
  }

  public fun normalized(): ThemeColor {
    return copy(
      hue = hue.coerceIn(0, 360),
      saturation = saturation.coerceIn(20, 55),
      lightness = lightness.coerceIn(35, 60),
    )
  }
}
