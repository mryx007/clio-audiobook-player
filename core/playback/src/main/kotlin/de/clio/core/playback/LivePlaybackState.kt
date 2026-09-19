package de.clio.core.playback

import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import de.clio.core.data.Book
import de.clio.core.data.BookId
import de.clio.core.data.ChapterId
import de.clio.core.playback.session.bookId
import de.clio.core.playback.session.positionInChapter
import de.clio.core.playback.session.realChapterId
import de.clio.core.playback.session.toMediaIdOrNull

data class LivePlaybackState(
  val bookId: BookId,
  val chapterId: ChapterId,
  val positionMs: Long,
  val isPlaying: Boolean,
  val playbackSpeed: Float,
)

internal fun MediaController.livePlaybackStateSnapshot(bookId: BookId? = null): LivePlaybackState? {
  val mediaId = currentMediaItem?.mediaId?.toMediaIdOrNull() ?: return null
  val mediaItemBookId = mediaId.bookId ?: return null
  if (bookId != null && mediaItemBookId != bookId) {
    return null
  }
  val positionMs = currentPosition.takeUnless { it == C.TIME_UNSET || it < 0 } ?: return null
  val chapterId = mediaId.realChapterId ?: return null
  val positionInChapter = mediaId.positionInChapter(positionMs) ?: return null
  val isPlaying = when {
    playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE -> false
    else -> playWhenReady
  }
  return LivePlaybackState(
    bookId = mediaItemBookId,
    chapterId = chapterId,
    positionMs = positionInChapter,
    isPlaying = isPlaying,
    playbackSpeed = playbackParameters.speed,
  )
}

fun Book.overlay(livePlaybackState: LivePlaybackState): Book {
  return if (livePlaybackState.bookId == id) {
    update {
      it.copy(
        currentChapter = livePlaybackState.chapterId,
        positionInChapter = livePlaybackState.positionMs,
        playbackSpeed = livePlaybackState.playbackSpeed,
      )
    }
  } else {
    this
  }
}
