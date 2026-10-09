package de.clio.core.playback.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.UnhandledAudioFormatException
import androidx.media3.common.audio.BaseAudioProcessor
import de.clio.core.playback.misc.Decibel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.pow
import kotlin.math.tanh

@SingleIn(AppScope::class)
@Inject
class GainAudioProcessor : BaseAudioProcessor() {

  @Volatile
  private var gain: Decibel = Decibel.Zero

  @Volatile
  private var factor: Float = 1.0f

  fun setGain(newGain: Decibel) {
    if (gain == newGain) return
    gain = newGain
    factor = if (newGain.value <= 0f) {
      1.0f
    } else {
      10f.pow(newGain.value / 20f)
    }
  }

  fun getGain(): Decibel = gain

  override fun isActive(): Boolean = gain.value > 0f && super.isActive()

  override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
    if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
      throw UnhandledAudioFormatException(inputAudioFormat)
    }
    return inputAudioFormat
  }

  override fun queueInput(inputBuffer: ByteBuffer) {
    val remaining = inputBuffer.remaining()
    if (remaining == 0) return

    val buffer = replaceOutputBuffer(remaining)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    inputBuffer.order(ByteOrder.LITTLE_ENDIAN)

    val currentFactor = factor
    if (currentFactor <= 1.0f || gain.value <= 0f) {
      buffer.put(inputBuffer)
      buffer.flip()
      return
    }

    val sampleCount = remaining / 2
    for (i in 0 until sampleCount) {
      val raw = inputBuffer.short * currentFactor
      val sample = if (raw > 30000f) {
        30000f + 2767f * tanh((raw - 30000f) / 2767f)
      } else if (raw < -30000f) {
        -30000f - 2768f * tanh((-raw - 30000f) / 2768f)
      } else {
        raw
      }
      buffer.putShort(sample.toInt().coerceIn(-32768, 32767).toShort())
    }
    buffer.flip()
  }
}
