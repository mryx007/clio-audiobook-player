package de.clio.features.folderPicker.addcontent

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.clio.core.strings.R
import de.clio.core.ui.icons.ClioIcons

@Composable
internal fun SelectFolderAppBar(onBack: () -> Unit) {
  TopAppBar(
    title = { },
    navigationIcon = {
      IconButton(onClick = onBack) {
        Icon(
          imageVector = ClioIcons.ArrowBack,
          contentDescription = stringResource(id = R.string.common_action_close),
        )
      }
    },
  )
}
