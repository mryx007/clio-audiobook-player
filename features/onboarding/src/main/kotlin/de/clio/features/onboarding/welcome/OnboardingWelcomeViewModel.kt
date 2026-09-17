package de.clio.features.onboarding.welcome

import dev.zacsweers.metro.Inject
import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import de.clio.navigation.Origin

@Inject
class OnboardingWelcomeViewModel(private val navigator: Navigator) {

  fun next() {
    navigator.goTo(Destination.AddContent(origin = Origin.Onboarding))
  }
}

