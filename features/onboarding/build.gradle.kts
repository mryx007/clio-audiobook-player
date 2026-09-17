plugins {
  id("de.clio.library")
  id("de.clio.compose")
  alias(libs.plugins.metro)
}

android {
  androidResources {
    enable = true
  }
}

dependencies {
  implementation(projects.navigation)
  implementation(projects.core.common)
  implementation(projects.core.strings)
  implementation(projects.core.ui)

  implementation(libs.coil)
  implementation(libs.androidxCore)
}
