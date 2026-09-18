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
import de.clio.core.data.BookSortOrder
import de.clio.core.ui.icons.ClioIcons
import de.clio.core.strings.R as StringsR

@Composable
internal fun SortOrderIcon(
  sortOrder: BookSortOrder,
  onSortOrderChange: (BookSortOrder) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier) {
    IconButton(
      onClick = { expanded = true },
      modifier = Modifier.size(40.dp),
    ) {
      Icon(
        imageVector = ClioIcons.Sort,
        contentDescription = stringResource(StringsR.string.book_sort_title),
      )
    }

    SmoothDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      BookSortOrder.entries.forEach { order ->
        DropdownMenuItem(
          text = { Text(stringResource(order.nameRes)) },
          trailingIcon = if (order == sortOrder) {
            {
              Icon(
                imageVector = ClioIcons.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
              )
            }
          } else {
            null
          },
          onClick = {
            expanded = false
            onSortOrderChange(order)
          },
        )
      }
    }
  }
}

private val BookSortOrder.nameRes: Int
  get() = when (this) {
    BookSortOrder.BY_LAST_PLAYED -> StringsR.string.book_sort_by_last_played
    BookSortOrder.BY_ADDED_AT -> StringsR.string.book_sort_by_added_at
    BookSortOrder.BY_NAME_ASC -> StringsR.string.book_sort_by_name_asc
    BookSortOrder.BY_NAME_DESC -> StringsR.string.book_sort_by_name_desc
    BookSortOrder.BY_AUTHOR -> StringsR.string.book_sort_by_author
    BookSortOrder.BY_DURATION_ASC -> StringsR.string.book_sort_by_duration_asc
    BookSortOrder.BY_DURATION_DESC -> StringsR.string.book_sort_by_duration_desc
  }

