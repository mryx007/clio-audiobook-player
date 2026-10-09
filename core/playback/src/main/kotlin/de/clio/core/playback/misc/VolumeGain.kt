package de.clio.core.playback.misc

import de.clio.core.logging.api.Logger
import de.clio.core.playback.audio.GainAudioProcessor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.properties.Delegates

@SingleIn(AppScope::class)
@Inject
class VolumeGain(private val gainAudioProcessor: GainAudioProcessor) {

  var gain: Decibel by Delegates.observable(Decibel(0F)) { _, _, newGain ->
    Logger.v("VolumeGain updated gain=$newGain")
    gainAudioProcessor.setGain(newGain)
  }

  var audioSessionId: Int? = null

  companion object {
    val MAX_GAIN = Decibel(18F)
  }
}
