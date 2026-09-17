package de.clio.core.data

import android.net.Uri
import androidx.core.net.toUri
import de.clio.core.common.comparator.NaturalOrderComparator
import kotlinx.serialization.Serializable

@Serializable(with = ChapterIdSerializer::class)
public data class ChapterId(val value: String) : Comparable<ChapterId> {
  public constructor(uri: Uri) : this(uri.toString())

  override fun compareTo(other: ChapterId): Int {
    return NaturalOrderComparator.uriComparator.compare(value.toUri(), other.value.toUri())
  }
}
