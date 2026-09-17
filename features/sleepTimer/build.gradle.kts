plugins {
  id("de.clio.library")
  id("de.clio.compose")
}

dependencies {
  implementation(projects.core.strings)
  implementation(projects.core.ui)

  testImplementation(libs.junit)
  testImplementation(libs.androidX.test.core)
  testImplementation(libs.androidX.test.junit)
  testImplementation(libs.androidX.test.runner)
  testImplementation(libs.robolectric)
}
