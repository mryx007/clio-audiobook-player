package de.clio.features.bookOverview.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.clio.core.strings.R as StringsR
import de.clio.core.ui.icons.ClioIcons

@Composable
internal fun GridColumnsIcon(
  gridColumnCount: Int,
  onGridColumnCountChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier) {
    IconButton(onClick = { expanded = true }) {
      Icon(
        imageVector = ClioIcons.GridView,
        contentDescription = stringResource(StringsR.string.grid_columns_title),
      )
    }

    SmoothDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      listOf(
        1 to StringsR.string.grid_columns_one,
        2 to StringsR.string.grid_columns_two,
        3 to StringsR.string.grid_columns_three,
      ).forEach { (count, stringRes) ->
        DropdownMenuItem(
          text = { Text(stringResource(stringRes)) },
          trailingIcon = if (count == gridColumnCount) {
            {
              Icon(
                imageVector = ClioIcons.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
              )
            }
          } else null,
          onClick = {
            expanded = false
            onGridColumnCountChange(count)
          },
        )
      }
    }
  }
}
