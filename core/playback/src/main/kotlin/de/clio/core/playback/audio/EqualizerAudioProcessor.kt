package de.clio.core.playback.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.UnhandledAudioFormatException
import androidx.media3.common.audio.BaseAudioProcessor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import de.clio.core.data.EqualizerSetting
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin

@SingleIn(AppScope::class)
@Inject
class EqualizerAudioProcessor : BaseAudioProcessor() {

  private var setting: EqualizerSetting = EqualizerSetting.Flat
  @Volatile
  private var isFlat: Boolean = true

  private val filters = Array(10) { BiquadFilter() }

  fun setSetting(newSetting: EqualizerSetting) {
    setting = newSetting
    isFlat = newSetting.isFlat
    updateCoefficients()
  }

  fun getSetting(): EqualizerSetting = setting

  override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
    if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
      throw UnhandledAudioFormatException(inputAudioFormat)
    }
    updateCoefficients()
    return inputAudioFormat
  }

  override fun queueInput(inputBuffer: ByteBuffer) {
    val remaining = inputBuffer.remaining()
    if (remaining == 0) return

    val buffer = replaceOutputBuffer(remaining)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    inputBuffer.order(ByteOrder.LITTLE_ENDIAN)

    if (isFlat) {
      buffer.put(inputBuffer)
      buffer.flip()
      return
    }

    val channelCount = inputAudioFormat.channelCount
    val sampleRate = inputAudioFormat.sampleRate
    if (channelCount <= 0 || sampleRate <= 0) {
      buffer.put(inputBuffer)
      buffer.flip()
      return
    }

    val activeFilters = filters.filter { it.gainDb != 0 }
    if (activeFilters.isEmpty()) {
      buffer.put(inputBuffer)
      buffer.flip()
      return
    }

    for (filter in activeFilters) {
      filter.ensureChannels(channelCount)
    }

    val sampleCount = remaining / (2 * channelCount)
    for (i in 0 until sampleCount) {
      for (c in 0 until channelCount) {
        var sample = inputBuffer.short.toFloat()
        for (filter in activeFilters) {
          sample = filter.processSample(sample, c)
        }
        val clamped = sample.toInt().coerceIn(-32768, 32767).toShort()
        buffer.putShort(clamped)
      }
    }
    buffer.flip()
  }

  @Deprecated("Deprecated in Java")
  @Suppress("DEPRECATION")
  override fun onFlush() {
    for (filter in filters) {
      filter.reset()
    }
  }

  override fun onReset() {
    for (filter in filters) {
      filter.reset()
    }
  }

  private fun updateCoefficients() {
    val sampleRate = inputAudioFormat.sampleRate
    if (sampleRate <= 0) return

    val bands = setting.bands
    val q = 1.2f
    val freqValues = intArrayOf(31, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)

    for (i in 0 until 10) {
      val centerFreq = freqValues[i].toFloat().coerceAtMost((sampleRate / 2 - 1).toFloat())
      val gainDb = bands.getOrElse(i) { 0 }
      filters[i].setParameters(gainDb, centerFreq, sampleRate.toFloat(), q)
    }
  }

  private class BiquadFilter {
    var gainDb: Int = 0
      private set

    private var b0 = 1f
    private var b1 = 0f
    private var b2 = 0f
    private var a1 = 0f
    private var a2 = 0f

    private var x1 = FloatArray(2)
    private var x2 = FloatArray(2)
    private var y1 = FloatArray(2)
    private var y2 = FloatArray(2)

    fun ensureChannels(channelCount: Int) {
      if (x1.size < channelCount) {
        x1 = FloatArray(channelCount)
        x2 = FloatArray(channelCount)
        y1 = FloatArray(channelCount)
        y2 = FloatArray(channelCount)
      }
    }

    fun setParameters(gainDb: Int, f0: Float, fs: Float, q: Float) {
      this.gainDb = gainDb
      if (gainDb == 0) return

      val a = Math.pow(10.0, (gainDb / 40.0)).toFloat()
      val w0 = (2.0 * Math.PI * f0 / fs).toFloat()
      val alpha = (sin(w0) / (2.0 * q)).toFloat()
      val cosW0 = cos(w0)

      val b0Temp = 1f + alpha * a
      val b1Temp = -2f * cosW0
      val b2Temp = 1f - alpha * a
      val a0Temp = 1f + alpha / a
      val a1Temp = -2f * cosW0
      val a2Temp = 1f - alpha / a

      b0 = b0Temp / a0Temp
      b1 = b1Temp / a0Temp
      b2 = b2Temp / a0Temp
      a1 = a1Temp / a0Temp
      a2 = a2Temp / a0Temp
    }

    fun processSample(x: Float, channel: Int): Float {
      val y = b0 * x + b1 * x1[channel] + b2 * x2[channel] - a1 * y1[channel] - a2 * y2[channel]
      x2[channel] = x1[channel]
      x1[channel] = x
      y2[channel] = y1[channel]
      y1[channel] = y
      return y
    }

    fun reset() {
      x1.fill(0f)
      x2.fill(0f)
      y1.fill(0f)
      y2.fill(0f)
    }
  }
}
