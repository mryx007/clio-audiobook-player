package de.clio.features.onboarding.explanation

import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import de.clio.navigation.Destination
import de.clio.navigation.NavEntryProvider

@ContributesTo(AppScope::class)
interface OnboardingExplanationProvider {

  val onboardingExplanationViewModel: OnboardingExplanationViewModel

  @Provides
  @IntoSet
  fun onboardingExplanationNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.OnboardingExplanation> { key ->
    NavEntry(key) {
      OnboardingExplanation()
    }
  }
}
