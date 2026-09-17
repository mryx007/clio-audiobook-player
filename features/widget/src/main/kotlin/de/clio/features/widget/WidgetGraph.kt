package de.clio.features.widget

import de.clio.app.features.widget.BaseWidgetProvider

interface WidgetGraph {
  fun inject(target: BaseWidgetProvider)
}
