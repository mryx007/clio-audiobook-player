plugins {
  id("de.clio.library")
}

dependencies {
  implementation(libs.serialization.json)
  implementation(libs.androidxCore)

  testImplementation(libs.bundles.testing.jvm)
}
