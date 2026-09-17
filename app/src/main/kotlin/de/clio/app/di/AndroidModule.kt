package de.clio.app.di

import android.app.Application
import android.content.Context
import android.os.PowerManager
import de.clio.app.misc.AppInfoProviderImpl
import de.clio.app.misc.MainActivityIntentProviderImpl
import de.clio.core.common.AppInfoProvider
import de.clio.core.common.DispatcherProvider
import de.clio.core.common.MainScope
import de.clio.core.playback.notification.MainActivityIntentProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import java.time.Clock

@ContributesTo(AppScope::class)
interface AndroidModule {

  @Provides
  fun provideContext(app: Application): Context = app

  @Provides
  fun coroutineScope(dispatcherProvider: DispatcherProvider): CoroutineScope = MainScope(dispatcherProvider)

  @Provides
  @SingleIn(AppScope::class)
  fun providePowerManager(context: Context): PowerManager {
    return context.getSystemService(Context.POWER_SERVICE) as PowerManager
  }

  @Provides
  fun toToBookIntentProvider(impl: MainActivityIntentProviderImpl): MainActivityIntentProvider = impl

  @Provides
  fun applicationIdProvider(impl: AppInfoProviderImpl): AppInfoProvider = impl

  @Provides
  @SingleIn(AppScope::class)
  fun json(): Json {
    return Json.Default
  }

  @Provides
  @SingleIn(AppScope::class)
  fun dispatcherProvider(): DispatcherProvider {
    return DispatcherProvider()
  }

  @Provides
  fun clock(): Clock = Clock.systemDefaultZone()
}
