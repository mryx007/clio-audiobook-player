package de.clio.app.di

import de.clio.app.features.widget.BaseWidgetProvider
import de.clio.features.widget.WidgetGraph

interface AppGraph : WidgetGraph {

  fun inject(target: App)
  override fun inject(target: BaseWidgetProvider)
}
