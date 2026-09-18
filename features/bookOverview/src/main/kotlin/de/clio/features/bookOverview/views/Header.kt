package de.clio.features.bookOverview.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.clio.core.strings.R as StringsR
import de.clio.core.ui.icons.ClioIcons
import de.clio.features.bookOverview.overview.BookOverviewCategory

@Composable
internal fun Header(
  category: BookOverviewCategory,
  modifier: Modifier = Modifier,
  inSelectionMode: Boolean = false,
  selectedCount: Int = 0,
  allSelected: Boolean = false,
  onSelectAllClick: () -> Unit = {},
  onDeleteSelectedClick: () -> Unit = {},
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = if (inSelectionMode) {
        if (allSelected) {
          stringResource(StringsR.string.selection_title_all)
        } else {
          stringResource(StringsR.string.selection_title, selectedCount)
        }
      } else {
        stringResource(id = category.nameRes)
      },
      style = MaterialTheme.typography.headlineSmall,
    )

    if (inSelectionMode) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = onSelectAllClick,
          modifier = Modifier.size(40.dp),
        ) {
          Icon(
            imageVector = ClioIcons.Check,
            contentDescription = stringResource(
              if (allSelected) StringsR.string.selection_action_deselect_all else StringsR.string.selection_action_select_all,
            ),
            tint = if (allSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
          )
        }
        IconButton(
          onClick = onDeleteSelectedClick,
          modifier = Modifier.size(40.dp),
        ) {
          Icon(
            imageVector = ClioIcons.Delete,
            contentDescription = stringResource(StringsR.string.common_action_delete),
            tint = MaterialTheme.colorScheme.error,
          )
        }
      }
    }
  }
}

