package de.clio.features.onboarding.batteryOptimization

import de.clio.core.common.IsIgnoringBatteryOptimizations
import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
class OnboardingBatteryOptimizationViewModel(
  private val isIgnoringBatteryOptimizations: IsIgnoringBatteryOptimizations,
  private val navigator: Navigator,
) {

  private val _isIgnoringBatteryOptimizationsState =
    MutableStateFlow(isIgnoringBatteryOptimizations())
  val isIgnoringBatteryOptimizationsState: StateFlow<Boolean> =
    _isIgnoringBatteryOptimizationsState.asStateFlow()

  fun refreshStatus() {
    _isIgnoringBatteryOptimizationsState.value = isIgnoringBatteryOptimizations()
  }

  fun requestIgnoreBatteryOptimization() {
    navigator.goTo(Destination.BatteryOptimization)
  }

  fun next() {
    navigator.goTo(Destination.OnboardingCompletion)
  }

  fun back() {
    navigator.goBack()
  }
}
