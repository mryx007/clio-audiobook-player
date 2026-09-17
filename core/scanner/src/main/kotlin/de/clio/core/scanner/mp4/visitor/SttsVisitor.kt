package de.clio.core.scanner.mp4.visitor

import androidx.media3.common.util.ParsableByteArray
import de.clio.core.logging.api.Logger
import de.clio.core.scanner.mp4.Mp4ChpaterExtractorOutput
import de.clio.core.scanner.mp4.SttsEntry
import dev.zacsweers.metro.Inject

// https://developer.apple.com/documentation/quicktime-file-format/time-to-sample_atom
@Inject
internal class SttsVisitor : AtomVisitor {

  override val path: List<String> = listOf("moov", "trak", "mdia", "minf", "stbl", "stts")

  override fun visit(
    buffer: ParsableByteArray,
    parseOutput: Mp4ChpaterExtractorOutput,
  ) {
    val version = buffer.readUnsignedByte()
    if (version != 0) {
      Logger.w("Unexpected version $version in stts atom, expected 0")
    } else {
      buffer.skipBytes(3) // flags
      val numberOfEntriesInSttsTable = buffer.readUnsignedIntToInt()
      Logger.v("Number of entries in stts: $numberOfEntriesInSttsTable")
      val durations = (0 until numberOfEntriesInSttsTable).map {
        SttsEntry(
          sampleCount = buffer.readUnsignedInt(),
          sampleDuration = buffer.readUnsignedInt(),
        )
      }
      parseOutput.durations.add(durations)
    }
  }
}
