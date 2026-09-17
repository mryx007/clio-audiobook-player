package voice.features.bookOverview.views.topbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import voice.core.strings.R
import voice.core.ui.icons.VoiceIcons

@Suppress("DEPRECATION")
@Composable
internal fun ColumnScope.BookOverviewSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  onBookFolderClick: () -> Unit,
  onSettingsClick: () -> Unit,
  showAddBookHint: Boolean,
  showFolderPickerIcon: Boolean,
  horizontalPadding: Dp = 16.dp,
) {
  SearchBar(
    inputField = {
      SearchBarDefaults.InputField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = {},
        expanded = false,
        onExpandedChange = {},
        placeholder = {
          Text(stringResource(R.string.library_search_hint))
        },
        leadingIcon = {
          Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(
              imageVector = VoiceIcons.Search,
              contentDescription = stringResource(id = R.string.library_search_hint),
            )
          }
        },
        trailingIcon = {
          if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
              Icon(
                imageVector = VoiceIcons.Close,
                contentDescription = stringResource(id = R.string.common_action_close),
              )
            }
          } else {
            TopBarTrailingIcon(
              showAddBookHint = showAddBookHint,
              showFolderPickerIcon = showFolderPickerIcon,
              onBookFolderClick = onBookFolderClick,
              onSettingsClick = onSettingsClick,
            )
          }
        },
      )
    },
    expanded = false,
    onExpandedChange = {},
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = horizontalPadding),
    content = {},
  )
}

