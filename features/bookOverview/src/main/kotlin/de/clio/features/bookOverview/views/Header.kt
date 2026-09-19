package de.clio.features.bookOverview.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.clio.core.data.BookSortOrder
import de.clio.core.ui.icons.ClioIcons
import de.clio.features.bookOverview.overview.BookOverviewCategory
import de.clio.features.bookOverview.overview.OverviewTab
import de.clio.core.strings.R as StringsR

@Composable
internal fun Header(
  category: BookOverviewCategory,
  modifier: Modifier = Modifier,
  selectedTab: OverviewTab = OverviewTab.Books,
  onTabSelect: (OverviewTab) -> Unit = {},
  queueCount: Int = 0,
  inSelectionMode: Boolean = false,
  selectedCount: Int = 0,
  allSelected: Boolean = false,
  onSelectAllClick: () -> Unit = {},
  onDeleteSelectedClick: () -> Unit = {},
  sortOrder: BookSortOrder = BookSortOrder.Default,
  onSortOrderChange: (BookSortOrder) -> Unit = {},
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    if (inSelectionMode) {
      Text(
        text = if (allSelected) {
          stringResource(StringsR.string.selection_title_all)
        } else {
          stringResource(StringsR.string.selection_title, selectedCount)
        },
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )

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
    } else {
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (selectedTab == OverviewTab.Queue) {
          IconButton(
            onClick = { onTabSelect(OverviewTab.Books) },
            modifier = Modifier.size(40.dp),
          ) {
            Icon(
              imageVector = ClioIcons.ArrowBack,
              contentDescription = null,
            )
          }
        }
        Text(
          text = if (selectedTab == OverviewTab.Queue) {
            stringResource(StringsR.string.queue_title)
          } else {
            stringResource(id = category.nameRes)
          },
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        if (queueCount > 0) {
          Surface(
            onClick = {
              onTabSelect(if (selectedTab == OverviewTab.Queue) OverviewTab.Books else OverviewTab.Queue)
            },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            border = BorderStroke(
              width = 1.dp,
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
            ),
            modifier = Modifier.height(32.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = ClioIcons.QueueMusic,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = stringResource(id = StringsR.string.queue_title),
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = queueCount.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        } else {
          IconButton(
            onClick = {
              onTabSelect(if (selectedTab == OverviewTab.Queue) OverviewTab.Books else OverviewTab.Queue)
            },
            modifier = Modifier.size(40.dp),
          ) {
            Icon(
              imageVector = ClioIcons.QueueMusic,
              tint = MaterialTheme.colorScheme.onSurface,
              contentDescription = stringResource(id = StringsR.string.queue_title),
            )
          }
        }

        if (selectedTab == OverviewTab.Books) {
          SortOrderIcon(
            sortOrder = sortOrder,
            onSortOrderChange = onSortOrderChange,
          )
        }
      }
    }
  }
}
