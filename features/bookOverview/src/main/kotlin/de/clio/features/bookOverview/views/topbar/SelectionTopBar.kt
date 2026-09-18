package de.clio.features.bookOverview.views.topbar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.clio.core.ui.icons.ClioIcons
import de.clio.core.strings.R as StringsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectionTopBar(
  selectedCount: Int,
  allSelected: Boolean,
  onClearSelection: () -> Unit,
  onSelectAllClick: () -> Unit,
  onDeleteSelectedClick: () -> Unit,
  horizontalPadding: Dp = 16.dp,
  modifier: Modifier = Modifier,
) {
  TopAppBar(
    title = {
      Text(
        text = stringResource(StringsR.string.selection_title, selectedCount),
        style = MaterialTheme.typography.titleMedium,
      )
    },
    navigationIcon = {
      IconButton(onClick = onClearSelection) {
        Icon(
          imageVector = ClioIcons.Close,
          contentDescription = stringResource(StringsR.string.common_action_close),
        )
      }
    },
    actions = {
      IconButton(onClick = onSelectAllClick) {
        Icon(
          imageVector = ClioIcons.Check,
          contentDescription = stringResource(
            if (allSelected) StringsR.string.selection_action_deselect_all else StringsR.string.selection_action_select_all,
          ),
          tint = if (allSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
      }
      IconButton(onClick = onDeleteSelectedClick) {
        Icon(
          imageVector = ClioIcons.Delete,
          contentDescription = stringResource(StringsR.string.common_action_delete),
          tint = MaterialTheme.colorScheme.error,
        )
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = horizontalPadding)
      .clip(CircleShape),
  )
}
