package de.clio.core.remoteconfig.noop

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import de.clio.core.remoteconfig.api.RemoteConfig

@ContributesBinding(AppScope::class)
class NoopRemoteConfig : RemoteConfig {

  override fun boolean(
    key: String,
    defaultValue: Boolean,
  ): Boolean = defaultValue

  override fun string(
    key: String,
    defaultValue: String,
  ): String = defaultValue

  override suspend fun refresh() {}
}
