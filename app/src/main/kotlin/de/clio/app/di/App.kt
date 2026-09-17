package de.clio.app.di

import android.app.Application
import dev.zacsweers.metro.HasMemberInjections
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import de.clio.core.common.rootGraph
import de.clio.core.initializer.AppInitializer

@HasMemberInjections
open class App : Application() {

  @Inject
  lateinit var appInitializers: Set<AppInitializer>

  override fun onCreate() {
    super.onCreate()

    rootGraph = createGraph()
      .also { graph ->
        graph.inject(this)
      }

    CoroutineScope(Dispatchers.Default).launch {
      appInitializers.forEach {
        it.onAppStart(this@App)
      }
    }
  }

  open fun createGraph(): AppGraph {
    return createGraphFactory<ProductionAppGraph.Factory>().create(this)
  }
}
