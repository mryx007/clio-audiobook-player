plugins {
  id("de.clio.library")
  alias(libs.plugins.metro)
}

dependencies {
  api(projects.core.analytics.api)
}
