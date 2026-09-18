package de.clio.core.playback.session

import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import de.clio.core.common.rootGraphAs
import de.clio.core.playback.di.PlaybackGraph
import de.clio.core.playback.player.ClioPlayer
import de.clio.core.playback.playstate.PositionUpdater
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

class PlaybackService : MediaLibraryService() {

  @Inject
  lateinit var session: MediaLibrarySession

  @Inject
  lateinit var scope: CoroutineScope

  @Inject
  lateinit var player: ClioPlayer

  @Inject
  lateinit var positionUpdater: PositionUpdater

  @Inject
  lateinit var voiceNotificationProvider: ClioMediaNotificationProvider

  override fun onCreate() {
    super.onCreate()
    rootGraphAs<PlaybackGraph.Provider>()
      .playbackGraphFactory
      .create(this)
      .inject(this)
    setMediaNotificationProvider(voiceNotificationProvider)
  }

  private fun release() {
    runBlocking {
      positionUpdater.flushPositionNow()
    }
    positionUpdater.release()
    player.release()
    session.release()
    scope.cancel()
  }

  override fun onDestroy() {
    super.onDestroy()
    release()
  }

  override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
    return session
  }
}
