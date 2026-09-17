import org.gradle.api.Plugin
import org.gradle.api.Project

class AppPlugin : Plugin<Project> {

  override fun apply(target: Project) {
    target.pluginManager.run {
      apply("de.clio.ktlint")
      apply("com.android.application")
      withPlugin("com.android.application") {
        target.baseSetup()
        target.tasks.register("clioUnitTest") {
          dependsOn("testFreeDebugUnitTest")
        }
        target.tasks.register("voiceUnitTest") {
          dependsOn("testFreeDebugUnitTest")
        }
      }
    }
  }
}
