package de.clio.features.settings

internal sealed interface SettingsViewEffect {
  data object DeveloperMenuUnlocked : SettingsViewEffect
}
