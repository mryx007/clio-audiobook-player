package de.clio.core.playback.session

import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import de.clio.core.common.rootGraphAs
import de.clio.core.logging.api.Logger
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
    return session.takeUnless { session ->
      session.invokeIsReleased
    }.also {
      if (it == null) {
        Logger.w("onGetSession returns null because the session is already released")
      }
    }
  }
}

private val MediaSession.invokeIsReleased: Boolean
  get() = try {
    // temporarily checked to debug
    // https://github.com/androidx/media/issues/422
    MediaSession::class.java.getDeclaredMethod("isReleased")
      .apply { isAccessible = true }
      .invoke(this) as Boolean
  } catch (e: Exception) {
    Logger.w(e, "Couldn't check if it's released")
    false
  }
