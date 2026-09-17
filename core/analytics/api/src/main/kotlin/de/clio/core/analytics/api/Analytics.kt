package de.clio.core.analytics.api

interface Analytics {
  fun screenView(screenName: String)

  fun event(
    name: String,
    params: Map<String, String> = emptyMap(),
  )
}
