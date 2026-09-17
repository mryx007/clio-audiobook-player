package de.clio.core.playback.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import de.clio.core.data.EqualizerSetting
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EqualizerAudioProcessorTest {

  @Test
  fun `default setting is flat`() {
    val processor = EqualizerAudioProcessor()
    assertEquals(expected = EqualizerSetting.Flat, actual = processor.getSetting())
    assertTrue(processor.getSetting().isFlat)
  }

  @Test
  fun `setting serialization and deserialization`() {
    val setting = EqualizerSetting(listOf(-12, -6, 0, 3, 6, 9, 12, -3, 0, 1))
    val serialized = setting.serialize()
    val deserialized = EqualizerSetting.fromString(serialized)
    assertEquals(expected = setting, actual = deserialized)
    assertFalse(deserialized.isFlat)
  }

  @Test
  @Suppress("DEPRECATION")
  fun `bypass when flat`() {
    val processor = EqualizerAudioProcessor()
    val format = AudioProcessor.AudioFormat(44100, 2, C.ENCODING_PCM_16BIT)
    processor.configure(format)
    processor.flush()

    val inputBytes = ByteArray(16) { it.toByte() }
    val buffer = ByteBuffer.allocateDirect(inputBytes.size).order(ByteOrder.LITTLE_ENDIAN)
    buffer.put(inputBytes)
    buffer.flip()

    processor.queueInput(buffer)
    val output = processor.output
    val outputBytes = ByteArray(output.remaining())
    output.get(outputBytes)

    assertEquals(expected = inputBytes.toList(), actual = outputBytes.toList())
  }

  @Test
  @Suppress("DEPRECATION")
  fun `processes audio with non-flat setting without error`() {
    val processor = EqualizerAudioProcessor()
    processor.setSetting(EqualizerSetting.VocalClarity)
    val format = AudioProcessor.AudioFormat(44100, 2, C.ENCODING_PCM_16BIT)
    processor.configure(format)
    processor.flush()

    val sampleCount = 64
    val buffer = ByteBuffer.allocateDirect(sampleCount * 2 * 2).order(ByteOrder.LITTLE_ENDIAN)
    for (i in 0 until sampleCount * 2) {
      buffer.putShort(1000.toShort())
    }
    buffer.flip()

    processor.queueInput(buffer)
    val output = processor.output
    assertTrue(output.remaining() > 0)
  }
}
