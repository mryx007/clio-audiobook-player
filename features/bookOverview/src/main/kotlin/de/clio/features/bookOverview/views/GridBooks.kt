package de.clio.features.bookOverview.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import de.clio.core.data.BookId
import de.clio.core.data.BookSortOrder
import de.clio.core.ui.icons.ClioIcons
import de.clio.core.ui.sharedCoverElementModifier
import de.clio.features.bookOverview.bottomSheet.BottomSheetItem
import de.clio.features.bookOverview.overview.BookOverviewCategory
import de.clio.features.bookOverview.overview.BookOverviewItemViewState
import kotlin.math.roundToInt
import de.clio.core.strings.R as StringsR
import de.clio.core.ui.R as UiR

@Composable
internal fun GridBooks(
  books: Map<BookOverviewCategory, Map<BookId, State<BookOverviewItemViewState>>>,
  onBookClick: (BookId) -> Unit,
  modifier: Modifier = Modifier,
  gridColumnCount: Int = 2,
  onBookLongClick: (BookId) -> Unit = {},
  selectedBookIds: Set<BookId> = emptySet(),
  allBookIds: Set<BookId> = emptySet(),
  inSelectionMode: Boolean = false,
  onSelectAllClick: () -> Unit = {},
  onDeleteSelectedClick: () -> Unit = {},
  onBookMoreClick: (BookId) -> Unit = {},
  selectedBookId: BookId? = null,
  menuItems: List<BottomSheetItem> = emptyList(),
  onMenuItemClick: (BookId, BottomSheetItem) -> Unit = { _, _ -> },
  showPermissionBugCard: Boolean = false,
  onPermissionBugCardClick: () -> Unit = {},
  sortOrder: BookSortOrder = BookSortOrder.Default,
  onSortOrderChange: (BookSortOrder) -> Unit = {},
  contentPadding: PaddingValues = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 12.dp),
) {
  LazyVerticalGrid(
    columns = GridCells.Fixed(gridColumnCount.coerceIn(1, 3)),
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = contentPadding,
  ) {
    if (showPermissionBugCard) {
      item(
        span = { GridItemSpan(maxLineSpan) },
      ) {
        PermissionBugCard(onPermissionBugCardClick)
      }
    }
    books.forEach { (category, books) ->
      if (books.isEmpty()) return@forEach
      item(
        span = { GridItemSpan(maxLineSpan) },
        key = category,
        contentType = "header",
      ) {
        val allSelected = selectedBookIds.size == allBookIds.size && allBookIds.isNotEmpty()
        Header(
          modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
          category = category,
          inSelectionMode = inSelectionMode,
          selectedCount = selectedBookIds.size,
          allSelected = allSelected,
          onSelectAllClick = onSelectAllClick,
          onDeleteSelectedClick = onDeleteSelectedClick,
          sortOrder = sortOrder,
          onSortOrderChange = onSortOrderChange,
        )
      }
      items(
        items = books.toList(),
        key = { (bookId, _) -> bookId.value },
        contentType = { "item" },
      ) { (_, bookState) ->
        GridBook(
          book = bookState.value,
          onBookClick = onBookClick,
          onBookLongClick = onBookLongClick,
          isSelected = bookState.value.id in selectedBookIds,
          inSelectionMode = inSelectionMode,
          onBookMoreClick = onBookMoreClick,
          selectedBookId = selectedBookId,
          menuItems = menuItems,
          onMenuItemClick = onMenuItemClick,
        )
      }
      item(
        span = { GridItemSpan(maxLineSpan) },
      ) {
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun GridBook(
  book: BookOverviewItemViewState,
  onBookClick: (BookId) -> Unit,
  modifier: Modifier = Modifier,
  onBookLongClick: (BookId) -> Unit = {},
  isSelected: Boolean = false,
  inSelectionMode: Boolean = false,
  onBookMoreClick: (BookId) -> Unit = {},
  selectedBookId: BookId? = null,
  menuItems: List<BottomSheetItem> = emptyList(),
  onMenuItemClick: (BookId, BottomSheetItem) -> Unit = { _, _ -> },
) {
  var menuExpanded by remember { mutableStateOf(false) }
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.medium)
      .combinedClickable(
        onClick = { onBookClick(book.id) },
        onLongClick = { onBookLongClick(book.id) },
      )
      .padding(4.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = Modifier.weight(1f),
      ) {
        Text(
          text = book.author ?: "",
          style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
          ),
          color = MaterialTheme.colorScheme.onSurface,
          minLines = 1,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(1.dp))

        Text(
          text = book.name,
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 12.5.sp,
            lineHeight = 16.sp,
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          minLines = 1,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      if (inSelectionMode) {
        CompositionLocalProvider(
          LocalMinimumInteractiveComponentSize provides 0.dp,
        ) {
          Checkbox(
            checked = isSelected,
            onCheckedChange = { onBookClick(book.id) },
            modifier = Modifier
              .size(20.dp)
              .padding(start = 2.dp),
          )
        }
      }
    }

    Spacer(Modifier.height(4.dp))

    Surface(
      shape = RoundedCornerShape(6.dp),
      shadowElevation = 3.dp,
      border = if (isSelected) BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary) else null,
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .sharedCoverElementModifier(book.id),
      color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
          model = book.cover,
          placeholder = painterResource(id = UiR.drawable.album_art),
          error = painterResource(id = UiR.drawable.album_art),
          contentDescription = null,
        )

        if (book.progress == 0f) {
          NewBadge(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(4.5.dp),
          )
        }

        if (isSelected) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
          )
        }

        if (book.progress > 0.05f) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .height(3.5.dp)
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          ) {
            Box(
              modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(book.progress)
                .background(MaterialTheme.colorScheme.primary),
            )
          }
        }
      }
    }

    Spacer(Modifier.height(4.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = book.remainingTime,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f, fill = false),
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "${(book.progress * 100).toInt()}%",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
        )

        if (!inSelectionMode) {
          Box {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .padding(start = 6.dp)
                .clip(RoundedCornerShape(3.dp))
                .clickable {
                  onBookMoreClick(book.id)
                  menuExpanded = true
                }
                .padding(vertical = 2.dp),
            ) {
              Box(
                modifier = Modifier
                  .width(7.dp)
                  .height(18.dp),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = ClioIcons.MoreVert,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.requiredSize(20.dp),
                )
              }
            }

            SmoothDropdownMenu(
              expanded = menuExpanded && selectedBookId == book.id && menuItems.isNotEmpty(),
              onDismissRequest = { menuExpanded = false },
            ) {
              menuItems.forEach { item ->
                DropdownMenuItem(
                  text = { Text(stringResource(item.titleRes)) },
                  leadingIcon = {
                    Icon(
                      imageVector = item.icon,
                      contentDescription = null,
                    )
                  },
                  onClick = {
                    menuExpanded = false
                    onMenuItemClick(book.id, item)
                  },
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
internal fun gridColumnCount(): Int {
  val displayMetrics = LocalResources.current.displayMetrics
  val widthPx = displayMetrics.widthPixels.toFloat()
  val desiredPx = with(LocalDensity.current) {
    130.dp.toPx()
  }
  val columns = (widthPx / desiredPx).roundToInt()
  return columns.coerceAtLeast(2)
}

@Composable
@Preview(widthDp = 200)
private fun GridBookPreviewWithProgress() {
  GridBook(
    book = BookOverviewPreviewParameterProvider().book().copy(progress = 0.66f),
    onBookClick = {},
  )
}

@Composable
@Preview(widthDp = 200)
private fun GridBookPreviewWithoutProgress() {
  GridBook(
    book = BookOverviewPreviewParameterProvider().book().copy(progress = 0f),
    onBookClick = {},
  )
}
