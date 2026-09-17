package de.clio.app

import de.clio.app.di.App
import de.clio.app.di.AppGraph
import dev.zacsweers.metro.createGraphFactory

class TestApp : App() {

  override fun createGraph(): AppGraph {
    return createGraphFactory<TestGraph.Factory>()
      .create(this)
  }
}
