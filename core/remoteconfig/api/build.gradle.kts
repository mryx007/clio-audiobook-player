plugins {
  id("de.clio.library")
  alias(libs.plugins.metro)
}

dependencies {
  implementation(projects.core.initializer)
  implementation(projects.core.common)
}
