package de.clio.features.support

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import de.clio.navigation.Destination
import de.clio.navigation.Navigator

@ContributesBinding(AppScope::class)
class KoFiSupportBackend(private val navigator: Navigator) : SupportBackend {

  override val state: StateFlow<SupportBackendState> = MutableStateFlow(SupportBackendState.Free)

  override fun openSupport() {
    navigator.goTo(Destination.Website(KO_FI_URL))
  }

  private companion object {
    const val KO_FI_URL = "https://ko-fi.com/paul_voice"
  }
}
