plugins {
  id("de.clio.library")
  alias(libs.plugins.metro)
}

dependencies {
  implementation(projects.core.data.api)
  testImplementation(projects.core.data.impl)
}
