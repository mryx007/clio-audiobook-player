package de.clio.core.playback.di

import de.clio.core.playback.session.PlaybackService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(
  scope = PlaybackScope::class,
)
interface PlaybackGraph {

  fun inject(target: PlaybackService)

  @ContributesTo(AppScope::class)
  @GraphExtension.Factory
  interface Factory {
    fun create(@Provides playbackService: PlaybackService): PlaybackGraph
  }

  @ContributesTo(AppScope::class)
  interface Provider {
    val playbackGraphFactory: Factory
  }
}
