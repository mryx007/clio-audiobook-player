package de.clio.features.onboarding.batteryOptimization

import androidx.datastore.core.DataStore
import de.clio.core.common.IsIgnoringBatteryOptimizations
import de.clio.core.data.store.OnlineCoverEnabledStore
import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Inject
class OnboardingBatteryOptimizationViewModel(
  private val isIgnoringBatteryOptimizations: IsIgnoringBatteryOptimizations,
  private val navigator: Navigator,
  @OnlineCoverEnabledStore
  private val onlineCoverEnabledStore: DataStore<Boolean>,
) {

  private val scope = MainScope()

  private val _isIgnoringBatteryOptimizationsState =
    MutableStateFlow(isIgnoringBatteryOptimizations())
  val isIgnoringBatteryOptimizationsState: StateFlow<Boolean> =
    _isIgnoringBatteryOptimizationsState.asStateFlow()

  private val _onlineCoverEnabledState = MutableStateFlow(false)
  val onlineCoverEnabledState: StateFlow<Boolean> = _onlineCoverEnabledState.asStateFlow()

  fun refreshStatus() {
    _isIgnoringBatteryOptimizationsState.value = isIgnoringBatteryOptimizations()
  }

  fun requestIgnoreBatteryOptimization() {
    navigator.goTo(Destination.BatteryOptimization)
  }

  fun setOnlineCoverEnabled(enabled: Boolean) {
    _onlineCoverEnabledState.value = enabled
  }

  fun next() {
    scope.launch {
      onlineCoverEnabledStore.updateData { _onlineCoverEnabledState.value }
      navigator.goTo(Destination.OnboardingCompletion)
    }
  }

  fun back() {
    navigator.goBack()
  }
}
