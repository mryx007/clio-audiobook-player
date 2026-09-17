package de.clio.app

import dev.zacsweers.metro.createGraphFactory
import de.clio.app.di.App
import de.clio.app.di.AppGraph

class TestApp : App() {

  override fun createGraph(): AppGraph {
    return createGraphFactory<TestGraph.Factory>()
      .create(this)
  }
}
