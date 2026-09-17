package voice.features.settings.playercontrols

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import voice.core.common.DispatcherProvider
import voice.core.common.MainScope
import voice.core.data.PlayerButtonVisibility
import voice.core.data.store.AutoRewindAmountStore
import voice.core.data.store.FastForwardTimeStore
import voice.core.data.store.PlayerButtonVisibilityStore
import voice.core.data.store.RewindTimeStore
import voice.navigation.Navigator

data class PlayerControlsSettingsViewState(
  val buttonVisibility: PlayerButtonVisibility,
  val rewindTimeInSeconds: Int,
  val fastForwardTimeInSeconds: Int,
  val autoRewindInSeconds: Int,
  val dialog: Dialog? = null,
) {
  sealed interface Dialog {
    data object RewindTime : Dialog
    data object FastForwardTime : Dialog
    data object AutoRewindAmount : Dialog
  }
}

@Inject
class PlayerControlsSettingsViewModel(
  private val navigator: Navigator,
  @PlayerButtonVisibilityStore
  private val playerButtonVisibilityStore: DataStore<PlayerButtonVisibility>,
  @RewindTimeStore
  private val rewindTimeStore: DataStore<Int>,
  @FastForwardTimeStore
  private val fastForwardTimeStore: DataStore<Int>,
  @AutoRewindAmountStore
  private val autoRewindAmountStore: DataStore<Int>,
  dispatcherProvider: DispatcherProvider,
) {

  private val scope = MainScope(dispatcherProvider)
  private val dialog = mutableStateOf<PlayerControlsSettingsViewState.Dialog?>(null)

  @Composable
  fun viewState(): PlayerControlsSettingsViewState? {
    val buttonVisibility = remember { playerButtonVisibilityStore.data }
      .collectAsState(initial = null).value ?: return null
    val rewindTime by remember { rewindTimeStore.data }
      .collectAsState(initial = 20)
    val fastForwardTime by remember { fastForwardTimeStore.data }
      .collectAsState(initial = 30)
    val autoRewindAmount by remember { autoRewindAmountStore.data }
      .collectAsState(initial = 2)
    return PlayerControlsSettingsViewState(
      buttonVisibility = buttonVisibility,
      rewindTimeInSeconds = rewindTime,
      fastForwardTimeInSeconds = fastForwardTime,
      autoRewindInSeconds = autoRewindAmount,
      dialog = dialog.value,
    )
  }

  fun toggleLock() {
    scope.launch {
      playerButtonVisibilityStore.updateData { it.copy(showLock = !it.showLock) }
    }
  }

  fun toggleEqualizer() {
    scope.launch {
      playerButtonVisibilityStore.updateData { it.copy(showEqualizer = !it.showEqualizer) }
    }
  }

  fun toggleSleepTimer() {
    scope.launch {
      playerButtonVisibilityStore.updateData { it.copy(showSleepTimer = !it.showSleepTimer) }
    }
  }

  fun toggleBookmark() {
    scope.launch {
      playerButtonVisibilityStore.updateData { it.copy(showBookmark = !it.showBookmark) }
    }
  }

  fun toggleSpeed() {
    scope.launch {
      playerButtonVisibilityStore.updateData { it.copy(showSpeed = !it.showSpeed) }
    }
  }

  fun onRewindRowClick() {
    dialog.value = PlayerControlsSettingsViewState.Dialog.RewindTime
  }

  fun onFastForwardRowClick() {
    dialog.value = PlayerControlsSettingsViewState.Dialog.FastForwardTime
  }

  fun onAutoRewindRowClick() {
    dialog.value = PlayerControlsSettingsViewState.Dialog.AutoRewindAmount
  }

  fun rewindAmountChanged(seconds: Int) {
    scope.launch {
      rewindTimeStore.updateData { seconds }
    }
  }

  fun fastForwardAmountChanged(seconds: Int) {
    scope.launch {
      fastForwardTimeStore.updateData { seconds }
    }
  }

  fun autoRewindAmountChanged(seconds: Int) {
    scope.launch {
      autoRewindAmountStore.updateData { seconds }
    }
  }

  fun dismissDialog() {
    dialog.value = null
  }

  fun close() {
    navigator.goBack()
  }
}
