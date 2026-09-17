plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(libs.androidPluginForGradle)
  implementation(libs.kotlin.pluginForGradle)
  implementation(libs.kotlin.powerAssert)
  implementation(libs.compose.compiler.gradle.plugin)
  implementation(libs.ktlint.gradlePlugin)
}

gradlePlugin {
  plugins {
    create("library") {
      id = "de.clio.library"
      implementationClass = "LibraryPlugin"
    }
    create("app") {
      id = "de.clio.app"
      implementationClass = "AppPlugin"
    }
    create("compose") {
      id = "de.clio.compose"
      implementationClass = "ComposePlugin"
    }
    create("ktlint") {
      id = "de.clio.ktlint"
      implementationClass = "KtlintPlugin"
    }
  }
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.jvm.toolchain.get().toInt()))
  }
}
