plugins {
  id("de.clio.library")
  id("de.clio.compose")
  alias(libs.plugins.metro)
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.navigation)
  implementation(projects.core.strings)
  implementation(projects.core.featureflag)
  implementation(projects.core.playback)
  implementation(projects.core.ui)
  implementation(projects.core.remoteconfig.api)
  implementation(projects.core.data.api)

  implementation(libs.androidxCore)
  implementation(libs.coil)
  implementation(libs.documentFile)

  testImplementation(libs.molecule)
  testImplementation(libs.turbine)
}
