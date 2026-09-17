package de.clio.core.playback.session

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import de.clio.core.data.BookId
import de.clio.core.data.ChapterId

@Serializable
sealed interface MediaId {
  @Serializable
  @SerialName("root")
  data object Root : MediaId

  @Serializable
  @SerialName("book")
  data class Book(val id: BookId) : MediaId

  @Serializable
  @SerialName("chapter")
  data class Chapter(
    val bookId: BookId,
    val chapterId: ChapterId,
  ) : MediaId

  @Serializable
  @SerialName("chapterMark")
  data class ChapterMark(
    val bookId: BookId,
    val chapterId: ChapterId,
    val markIndex: Int,
    val startMs: Long,
    val endMs: Long,
  ) : MediaId

  @Serializable
  @SerialName("recent")
  data object Recent : MediaId
}
