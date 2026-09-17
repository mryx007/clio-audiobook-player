package de.clio.core.playback.playstate

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import de.clio.core.data.repo.BookRepository
import de.clio.core.data.repo.ListeningStatisticRepository
import de.clio.core.featureflag.ExperimentalPlaybackPersistenceQualifier
import de.clio.core.featureflag.FeatureFlag
import de.clio.core.logging.api.Logger
import de.clio.core.playback.di.PlaybackScope
import de.clio.core.playback.session.bookId
import de.clio.core.playback.session.positionInChapter
import de.clio.core.playback.session.realChapterId
import de.clio.core.playback.session.toMediaIdOrNull
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Inject
@SingleIn(PlaybackScope::class)
class PositionUpdater(
  private val bookRepo: BookRepository,
  private val listeningStatisticRepo: ListeningStatisticRepository,
  private val scope: CoroutineScope,
  private val playStateManager: PlayStateManager,
  @ExperimentalPlaybackPersistenceQualifier
  private val experimentalPlaybackPersistenceFeatureFlag: FeatureFlag<Boolean>,
) : Player.Listener {

  private var player: Player? = null
  private var updateJob: Job? = null
  private var lastTrackedWallTimeMs: Long = 0L

  fun attachTo(player: Player) {
    this.player?.removeListener(this)
    this.player = player
    player.addListener(this)

    updateJob = scope.launch {
      playStateManager.playStateFlow
        .map { it == PlayStateManager.PlayState.Playing }
        .distinctUntilChanged()
        .collectLatest { playing ->
          if (playing) {
            lastTrackedWallTimeMs = System.currentTimeMillis()
            while (true) {
              delay(
                if (experimentalPlaybackPersistenceFeatureFlag.get()) {
                  5.minutes
                } else {
                  400.milliseconds
                },
              )
              flushPositionNow()
              trackListeningTimeNow()
            }
          } else {
            trackListeningTimeNow()
            lastTrackedWallTimeMs = 0L
          }
        }
    }
  }

  override fun onPositionDiscontinuity(
    oldPosition: Player.PositionInfo,
    newPosition: Player.PositionInfo,
    reason: Int,
  ) {
    flushPosition()
  }

  override fun onPlayWhenReadyChanged(
    playWhenReady: Boolean,
    reason: Int,
  ) {
    flushPosition()
  }

  override fun onPlaybackStateChanged(playbackState: Int) {
    if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
      flushPosition()
    }
  }

  override fun onMediaItemTransition(
    mediaItem: MediaItem?,
    reason: Int,
  ) {
    flushPosition()
  }

  private fun flushPosition() {
    scope.launch {
      flushPositionNow()
    }
  }

  suspend fun flushPositionNow() {
    val player = player ?: return
    val mediaItem = player.currentMediaItem ?: return
    val currentPosition = player.currentPosition
      .takeIf { it >= 0 } ?: return
    val mediaId = mediaItem.mediaId.toMediaIdOrNull() ?: return
    val bookId = mediaId.bookId ?: return
    val chapterId = mediaId.realChapterId ?: return
    val positionInChapter = mediaId.positionInChapter(currentPosition) ?: return
    bookRepo.updateBook(bookId) { content ->
      if (chapterId in content.chapters) {
        Logger.d("$positionInChapter is the new position!")
        content.copy(
          currentChapter = chapterId,
          positionInChapter = positionInChapter,
          lastPlayedAt = Instant.now(),
        )
      } else {
        Logger.w("$mediaId not in $content")
        content
      }
    }
  }

  private suspend fun trackListeningTimeNow() {
    if (lastTrackedWallTimeMs <= 0L) return
    val now = System.currentTimeMillis()
    val elapsedMs = now - lastTrackedWallTimeMs
    val elapsedSeconds = elapsedMs / 1000L
    if (elapsedSeconds >= 1L) {
      lastTrackedWallTimeMs = now - (elapsedMs % 1000L)
      val player = player ?: return
      val mediaItem = player.currentMediaItem ?: return
      val mediaId = mediaItem.mediaId.toMediaIdOrNull() ?: return
      val bookId = mediaId.bookId ?: return
      val book = bookRepo.get(bookId)
      val title = book?.content?.name ?: return
      listeningStatisticRepo.recordListeningTime(bookId, title, elapsedSeconds)
    }
  }

  fun release() {
    player?.removeListener(this)
    updateJob?.cancel()
  }
}
