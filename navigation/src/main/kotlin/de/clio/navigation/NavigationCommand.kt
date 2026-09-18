package de.clio.navigation

sealed interface NavigationCommand {
  data object GoBack : NavigationCommand
  data class GoTo(val destination: Destination) : NavigationCommand
  data class SetRoot(val root: Destination.Compose) : NavigationCommand
  data object MinimizeApp : NavigationCommand
}
