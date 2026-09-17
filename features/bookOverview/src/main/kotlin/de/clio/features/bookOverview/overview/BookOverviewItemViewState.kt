package de.clio.features.bookOverview.overview

import androidx.compose.runtime.Immutable
import de.clio.core.data.Book
import de.clio.core.data.BookId
import de.clio.core.logging.api.Logger
import de.clio.core.ui.formatTime

@Immutable
data class BookOverviewItemViewState(
  val name: String,
  val author: String?,
  val cover: String?,
  val progress: Float,
  val id: BookId,
  val remainingTime: String,
)

internal fun Book.toItemViewState() = BookOverviewItemViewState(
  name = content.name,
  author = content.author,
  cover = content.coverUrl,
  id = id,
  progress = progress(),
  remainingTime = formatTime(duration - position),
)

private fun Book.progress(): Float {
  val globalPosition = position
  val totalDuration = duration
  val progress = globalPosition.toFloat() / totalDuration.toFloat()
  if (progress < 0F) {
    Logger.w("Couldn't determine progress for book=$this")
  }
  return progress.coerceIn(0F, 1F)
}
