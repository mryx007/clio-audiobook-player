package de.clio.core.playback.player

import androidx.media3.common.Player
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import de.clio.core.data.repo.ChapterRepo
import de.clio.core.logging.api.Logger
import de.clio.core.playback.session.MediaId
import de.clio.core.playback.session.toMediaIdOrNull

@Inject
class DurationInconsistenciesUpdater(private val chapterRepo: ChapterRepo) : Player.Listener {

  private lateinit var player: Player

  private val scope = MainScope()

  fun attachTo(player: Player) {
    this.player = player
    player.addListener(this)
  }

  override fun onPlaybackStateChanged(playbackState: Int) {
    if (playbackState != Player.STATE_READY) return
    val mediaId = player.currentMediaItem?.mediaId?.toMediaIdOrNull()
      ?: return
    if (mediaId is MediaId.Chapter) {
      val playerDuration = player.duration
      scope.launch {
        val chapter = chapterRepo.get(mediaId.chapterId)
        if (chapter != null && chapter.duration != playerDuration) {
          Logger.d(
            """For chapter=${chapter.id}, we had ${chapter.duration},
            |but the player reported $playerDuration. Updating the chapter now
            """.trimMargin(),
          )
          chapterRepo.put(chapter.copy(duration = playerDuration))
        }
      }
    }
  }
}
