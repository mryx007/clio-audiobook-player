package de.clio.core.data

public data class EqualizerSetting(public val bands: List<Int> = List(10) { 0 }) {
  init {
    require(bands.size == 10) { "Equalizer must have exactly 10 bands" }
  }

  public val isFlat: Boolean get() = bands.all { it == 0 }

  public fun serialize(): String = bands.joinToString(",")

  public fun withBand(
    index: Int,
    valueDb: Int,
  ): EqualizerSetting {
    val newBands = bands.toMutableList()
    newBands[index] = valueDb.coerceIn(-12, 12)
    return EqualizerSetting(newBands)
  }

  public companion object {
    public val Flat: EqualizerSetting = EqualizerSetting(listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0))

    // Sprachklarheit: Rumpeln & Dröhnen dämpfen, Sprach-Präsenz (1-4 kHz) deutlich hervorheben
    public val VocalClarity: EqualizerSetting = EqualizerSetting(listOf(-8, -6, 0, -3, 0, 4, 7, 6, 2, 0))

    // Dumpfe Aufnahmen aufhellen: Höhen ab 2 kHz breit anheben
    public val TrebleBoost: EqualizerSetting = EqualizerSetting(listOf(-6, -4, 0, 0, 1, 2, 5, 6, 6, 4))

    // Bass-Dröhnen & Rumpeln radikal kappen (High-Pass)
    public val BassCut: EqualizerSetting = EqualizerSetting(listOf(-12, -12, -8, -4, 0, 0, 0, 0, 0, 0))

    // Zischlaute dämpfen (De-Esser bei 4-8 kHz)
    public val DeEsser: EqualizerSetting = EqualizerSetting(listOf(0, 0, 0, 0, 0, 0, -2, -6, -8, -5))

    public val Presets: List<EqualizerPreset> = EqualizerPreset.entries

    public val Frequencies: List<String> = listOf(
      "31 Hz", "63 Hz", "125 Hz", "250 Hz", "500 Hz",
      "1 kHz", "2 kHz", "4 kHz", "8 kHz", "16 kHz",
    )

    public fun fromString(serialized: String?): EqualizerSetting {
      if (serialized.isNullOrBlank()) return Flat
      val parsed = serialized.split(",").mapNotNull { it.trim().toIntOrNull()?.coerceIn(-12, 12) }
      if (parsed.size != 10) return Flat
      return EqualizerSetting(parsed)
    }
  }
}

public enum class EqualizerPreset(public val setting: EqualizerSetting) {
  Flat(EqualizerSetting.Flat),
  VocalClarity(EqualizerSetting.VocalClarity),
  TrebleBoost(EqualizerSetting.TrebleBoost),
  BassCut(EqualizerSetting.BassCut),
  DeEsser(EqualizerSetting.DeEsser),
}
