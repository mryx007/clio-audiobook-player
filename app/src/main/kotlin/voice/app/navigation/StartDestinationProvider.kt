package voice.app.navigation

import android.content.Intent
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import voice.app.MainActivity
import voice.core.data.BookId
import voice.core.data.folders.AudiobookFolders
import voice.core.data.store.CurrentBookStore
import voice.core.data.store.OnboardingCompletedStore
import voice.core.data.store.OpenLastBookOnStartupStore
import voice.core.playback.PlayerController
import voice.navigation.Destination

@Inject
class StartDestinationProvider(
  @OnboardingCompletedStore
  private val onboardingCompletedStore: DataStore<Boolean>,
  private val audiobookFolders: AudiobookFolders,
  @CurrentBookStore
  private val currentBookStore: DataStore<BookId?>,
  @OpenLastBookOnStartupStore
  private val openLastBookOnStartupStore: DataStore<Boolean>,
  private val playerController: PlayerController,
) {

  operator fun invoke(intent: Intent): List<Destination.Compose> {
    val (showOnboarding, openLastBookOnStartup, currentBookId) = runBlocking(Dispatchers.IO) {
      val onboardingDeferred = async { showOnboarding() }
      val openLastBookDeferred = async { openLastBookOnStartupStore.data.first() }
      val currentBookDeferred = async { currentBookStore.data.first() }
      Triple(
        onboardingDeferred.await(),
        openLastBookDeferred.await(),
        currentBookDeferred.await(),
      )
    }

    if (showOnboarding) {
      return listOf(Destination.OnboardingWelcome)
    }

    val goToBook = intent.getBooleanExtra(MainActivity.Companion.NI_GO_TO_BOOK, false)
    if (goToBook || (openLastBookOnStartup && intent.action == Intent.ACTION_MAIN)) {
      if (currentBookId != null) {
        return listOf(Destination.BookOverview, Destination.Playback(currentBookId))
      }
    }

    if (intent.action == "playCurrent") {
      if (currentBookId != null) {
        playerController.play()
        return listOf(Destination.BookOverview, Destination.Playback(currentBookId))
      }
    }
    return listOf(Destination.BookOverview)
  }

  private suspend fun showOnboarding(): Boolean {
    return when {
      onboardingCompletedStore.data.first() -> false
      audiobookFolders.hasAnyFolders() -> false
      else -> true
    }
  }
}
