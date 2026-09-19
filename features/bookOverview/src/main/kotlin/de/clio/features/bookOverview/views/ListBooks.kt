package de.clio.features.bookOverview.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
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
import de.clio.core.ui.R as UiR

@Composable
internal fun ListBooks(
  books: Map<BookOverviewCategory, Map<BookId, State<BookOverviewItemViewState>>>,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  selectedBookIds: Set<BookId>,
  inSelectionMode: Boolean,
  onBookMoreClick: (BookId) -> Unit,
  selectedBookId: BookId?,
  menuItems: List<BottomSheetItem>,
  onMenuItemClick: (BookId, BottomSheetItem) -> Unit,
  showPermissionBugCard: Boolean,
  onPermissionBugCardClick: () -> Unit,
  allBookIds: Set<BookId> = emptySet(),
  onSelectAllClick: () -> Unit = {},
  onDeleteSelectedClick: () -> Unit = {},
  sortOrder: BookSortOrder = BookSortOrder.Default,
  onSortOrderChange: (BookSortOrder) -> Unit = {},
  selectedTab: de.clio.features.bookOverview.overview.OverviewTab = de.clio.features.bookOverview.overview.OverviewTab.Books,
  onTabSelected: (de.clio.features.bookOverview.overview.OverviewTab) -> Unit = {},
  queueCount: Int = 0,
  contentPadding: PaddingValues = PaddingValues(top = 4.dp, start = 12.dp, end = 12.dp, bottom = 16.dp),
  currentBook: BookOverviewItemViewState? = null,
) {
  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = contentPadding,
  ) {
    if (showPermissionBugCard) {
      item {
        PermissionBugCard(onPermissionBugCardClick)
      }
    }
    books.forEach { (category, books) ->
      if (books.isEmpty()) return@forEach
      stickyHeader(
        key = category,
        contentType = "header",
      ) {
        val allSelected = selectedBookIds.size == allBookIds.size && allBookIds.isNotEmpty()
        Header(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
          category = category,
          selectedTab = selectedTab,
          onTabSelected = onTabSelected,
          queueCount = queueCount,
          inSelectionMode = inSelectionMode,
          selectedCount = selectedBookIds.size,
          allSelected = allSelected,
          onSelectAllClick = onSelectAllClick,
          onDeleteSelectedClick = onDeleteSelectedClick,
          sortOrder = sortOrder,
          onSortOrderChange = onSortOrderChange,
        )
      }
      if (category == BookOverviewCategory.OVERVIEW && currentBook != null && !inSelectionMode) {
        item(
          key = "overview_now_playing",
          contentType = "now_playing",
        ) {
          NowPlayingSection(
            book = currentBook,
            onClick = { onBookClick(currentBook.id) },
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp),
          )
        }
      }
      items(
        items = books.toList(),
        key = { (bookId, _) -> bookId.value },
        contentType = { "item" },
      ) { (_, bookState) ->
        ListBookRow(
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
      item {
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
      }
    }
  }
}

@Composable
internal fun ListBookRow(
  book: BookOverviewItemViewState,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  isSelected: Boolean,
  inSelectionMode: Boolean,
  onBookMoreClick: (BookId) -> Unit,
  selectedBookId: BookId?,
  menuItems: List<BottomSheetItem>,
  onMenuItemClick: (BookId, BottomSheetItem) -> Unit,
  modifier: Modifier = Modifier,
  showCheckboxInSelectionMode: Boolean = true,
  clickable: Boolean = true,
  trailingContent: (@Composable () -> Unit)? = null,
  enableSharedTransition: Boolean = true,
) {
  var menuExpanded by remember { mutableStateOf(false) }

  BookCard(
    bookId = book.id,
    onBookClick = onBookClick,
    onBookLongClick = onBookLongClick,
    isSelected = isSelected,
    clickable = clickable,
    modifier = modifier,
  ) {
    Column(Modifier.padding()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(end = 4.dp),
      ) {
        CoverImage(book.id, book.cover, enableSharedTransition)

        Column(
          modifier = Modifier
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
            .weight(1f)
            .heightIn(min = 76.dp),
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
          ) {
            Column(
              modifier = Modifier.weight(1f),
            ) {
              if (book.author != null) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Text(
                    text = book.author.toUpperCase(LocaleList.current),
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontSize = 12.5.sp,
                      fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                  )
                  if (book.progress == 0f) {
                    NewBadge(modifier = Modifier.padding(start = 6.dp))
                  }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                  text = book.name,
                  style = MaterialTheme.typography.titleSmall,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
              } else {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Text(
                    text = book.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                  )
                  if (book.progress == 0f) {
                    NewBadge(modifier = Modifier.padding(start = 6.dp))
                  }
                }
              }
            }

            if (trailingContent != null) {
              trailingContent()
            } else if (inSelectionMode) {
              if (showCheckboxInSelectionMode) {
                Checkbox(
                  checked = isSelected,
                  onCheckedChange = { onBookClick(book.id) },
                  modifier = Modifier.padding(start = 4.dp),
                )
              }
            } else {
              Box(
                modifier = Modifier.padding(start = 4.dp),
              ) {
                IconButton(
                  onClick = {
                    onBookMoreClick(book.id)
                    menuExpanded = true
                  },
                  modifier = Modifier.size(36.dp),
                ) {
                  Icon(
                    imageVector = ClioIcons.MoreVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
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

          BookRemainingProgressRow(
            remainingTime = book.remainingTime,
            progress = book.progress,
            remainingTimeMaxLines = 1,
            progressMaxLines = 1,
            textStyle = MaterialTheme.typography.labelLarge.copy(
              fontSize = 13.5.sp,
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(end = 8.dp),
          )
        }
      }

      if (book.progress > 0.05f) {
        BookProgressIndicator(
          progress = book.progress,
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            .height(4.dp),
          color = MaterialTheme.colorScheme.primary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun CoverImage(
  bookId: BookId,
  cover: String?,
  enableSharedTransition: Boolean = true,
) {
  val cornerRadius = 4.dp
  AsyncImage(
    modifier = Modifier
      .padding(top = 8.dp, start = 8.dp, bottom = 8.dp)
      .size(76.dp)
      .then(
        if (enableSharedTransition) Modifier.sharedCoverElementModifier(bookId) else Modifier,
      )
      .clip(RoundedCornerShape(cornerRadius)),
    model = cover,
    placeholder = painterResource(id = UiR.drawable.album_art),
    error = painterResource(id = UiR.drawable.album_art),
    contentScale = ContentScale.Crop,
    contentDescription = null,
  )
}

@Composable
@Preview
private fun ListBookRowPreviewWithProgress() {
  ListBookRow(BookOverviewPreviewParameterProvider().book().copy(progress = 0.6f), {}, {}, false, false, {}, null, emptyList(), { _, _ -> })
}

@Composable
@Preview
private fun ListBookRowPreviewWithoutProgress() {
  ListBookRow(BookOverviewPreviewParameterProvider().book().copy(progress = 0f), {}, {}, false, false, {}, null, emptyList(), { _, _ -> })
}
