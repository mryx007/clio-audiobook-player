package de.clio.features.onboarding.batteryOptimization

import androidx.navigation3.runtime.NavEntry
import de.clio.navigation.Destination
import de.clio.navigation.NavEntryProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
interface OnboardingBatteryOptimizationProvider {

  val onboardingBatteryOptimizationViewModel: OnboardingBatteryOptimizationViewModel

  @Provides
  @IntoSet
  fun onboardingBatteryOptimizationNavEntryProvider(): NavEntryProvider<*> =
    NavEntryProvider<Destination.OnboardingBatteryOptimization> { key ->
      NavEntry(key) {
        OnboardingBatteryOptimization()
      }
    }
}
