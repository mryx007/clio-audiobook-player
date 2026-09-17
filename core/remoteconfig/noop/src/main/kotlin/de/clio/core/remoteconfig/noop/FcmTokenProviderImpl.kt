package de.clio.core.remoteconfig.noop

import de.clio.core.remoteconfig.api.FmcTokenProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class NoopFmcTokenProvider : FmcTokenProvider {

  override suspend fun token(): String? {
    return null
  }
}
