package de.clio.core.remoteconfig.noop

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import de.clio.core.remoteconfig.api.FmcTokenProvider

@ContributesBinding(AppScope::class)
class NoopFmcTokenProvider : FmcTokenProvider {

  override suspend fun token(): String? {
    return null
  }
}
