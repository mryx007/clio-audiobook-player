package voice.features.onboarding.welcome

import dev.zacsweers.metro.Inject
import voice.navigation.Destination
import voice.navigation.Navigator
import voice.navigation.Origin

@Inject
class OnboardingWelcomeViewModel(private val navigator: Navigator) {

  fun next() {
    navigator.goTo(Destination.AddContent(origin = Origin.Onboarding))
  }
}

