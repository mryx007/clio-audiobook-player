package de.clio.features.support

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.clio.navigation.Navigator
import dev.zacsweers.metro.Inject

@Inject
class SupportViewModel(
  private val backend: SupportBackend,
  private val navigator: Navigator,
) : SupportListener {

  @Composable
  fun viewState(): SupportViewState {
    val backendState by backend.state.collectAsState()
    return SupportViewState(backendState)
  }

  override fun close() {
    navigator.goBack()
  }

  override fun openSupport() {
    backend.openSupport()
  }
}
