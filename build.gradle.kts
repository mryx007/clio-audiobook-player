plugins {
  alias(libs.plugins.compose.compiler) apply false
  id("de.clio.ktlint")
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
}
