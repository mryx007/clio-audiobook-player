package de.clio.features.onboarding.welcome

import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import de.clio.navigation.Origin
import dev.zacsweers.metro.Inject

@Inject
class OnboardingWelcomeViewModel(private val navigator: Navigator) {

  fun next() {
    navigator.goTo(Destination.AddContent(origin = Origin.Onboarding))
  }
}
