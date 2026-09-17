package de.clio.features.onboarding.explanation

import androidx.datastore.core.DataStore
import de.clio.core.common.AppInfoProvider
import de.clio.core.data.store.AnalyticsConsentStore
import de.clio.navigation.Destination
import de.clio.navigation.Navigator
import de.clio.navigation.Origin
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@Inject
class OnboardingExplanationViewModel(
  private val navigator: Navigator,
  @AnalyticsConsentStore
  private val analyticsConsentStore: DataStore<Boolean>,
  private val appInfoProvider: AppInfoProvider,
) {

  private val scope = MainScope()

  fun viewState(): OnboardingExplanationViewState {
    return OnboardingExplanationViewState(
      askForAnalytics = appInfoProvider.analyticsIncluded,
    )
  }

  fun onContinueWithAnalytics() {
    scope.launch {
      analyticsConsentStore.updateData { true }
    }
    navigator.goTo(Destination.AddContent(origin = Origin.Onboarding))
  }

  fun onContinueWithoutAnalytics() {
    scope.launch {
      analyticsConsentStore.updateData { false }
    }
    navigator.goTo(Destination.AddContent(origin = Origin.Onboarding))
  }

  fun onPrivacyPolicyClick() {
    navigator.goTo(Destination.Website("https://voice.woitaschek.de/privacy-policy"))
  }

  fun onClose() {
    navigator.goBack()
  }
}
