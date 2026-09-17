package de.clio.features.folderPicker.addcontent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import de.clio.core.common.rootGraphAs
import de.clio.core.ui.ClioTheme
import de.clio.navigation.Destination
import de.clio.navigation.NavEntryProvider
import de.clio.navigation.Origin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
interface AddContentGraph {
  val viewModelFactory: AddContentViewModel.Factory
}

@ContributesTo(AppScope::class)
interface AddContentProvider {

  @Provides
  @IntoSet
  fun addContentNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.AddContent> { key ->
    NavEntry(key) {
      AddContent(origin = key.origin)
    }
  }
}

@Composable
fun AddContent(origin: Origin) {
  val viewModel = retain(origin.name) {
    rootGraphAs<AddContentGraph>().viewModelFactory.create(origin)
  }
  SelectFolder(
    onBack = {
      viewModel.back()
    },
    origin = origin,
    onAdd = { folderType, uri ->
      viewModel.add(uri, folderType)
    },
  )
}

@Composable
@Preview
private fun AddContentPreview() {
  ClioTheme {
    AddContent(Origin.Default)
  }
}
