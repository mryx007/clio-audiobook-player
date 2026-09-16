plugins {
  id("voice.library")
  id("voice.compose")
  alias(libs.plugins.metro)
}

android {
  androidResources {
    enable = true
  }
}

dependencies {
  api(libs.review)

  implementation(projects.core.common)
  implementation(projects.core.data.api)
  implementation(projects.core.playback)
  implementation(projects.core.featureflag)
  implementation(projects.core.strings)
  implementation(projects.core.ui)
}
