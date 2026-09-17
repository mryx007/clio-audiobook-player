package de.clio.features.bookOverview.views

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import de.clio.core.ui.icons.ClioIcons
import de.clio.core.ui.sharedCoverElementModifier
import de.clio.features.bookOverview.bottomSheet.BottomSheetItem
import de.clio.features.bookOverview.overview.BookOverviewCategory
import de.clio.features.bookOverview.overview.BookOverviewItemViewState
import kotlin.math.roundToInt
import de.clio.core.ui.R as UiR

@Composable
internal fun GridBooks(
  books: Map<BookOverviewCategory, Map<BookId, State<BookOverviewItemViewState>>>,
  onBookClick: (BookId) -> Unit,
  onBookMoreClick: (BookId) -> Unit = {},
  selectedBookId: BookId? = null,
  menuItems: List<BottomSheetItem> = emptyList(),
  onMenuItemClick: (BookId, BottomSheetItem) -> Unit = { _, _ -> },
  showPermissionBugCard: Boolean = false,
  onPermissionBugCardClick: () -> Unit = {},
) {
  val cellCount = gridColumnCount()
  LazyVerticalGrid(
    columns = GridCells.Fixed(cellCount),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 20.dp, bottom = 12.dp),
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
        Header(
          modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
          category = category,
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
          onBookMoreClick = onBookMoreClick,
          selectedBookId = selectedBookId,
          menuItems = menuItems,
          onMenuItemClick = onMenuItemClick,
          modifier = Modifier.animateItem(),
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

@Composable
internal fun GridBook(
  book: BookOverviewItemViewState,
  onBookClick: (BookId) -> Unit,
  onBookMoreClick: (BookId) -> Unit = {},
  selectedBookId: BookId? = null,
  menuItems: List<BottomSheetItem> = emptyList(),
  onMenuItemClick: (BookId, BottomSheetItem) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier,
) {
  var menuExpanded by remember { mutableStateOf(false) }
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.medium)
      .clickable { onBookClick(book.id) }
      .padding(4.dp),
  ) {
    Text(
      text = book.author ?: "",
      style = MaterialTheme.typography.labelMedium.copy(
        fontSize = 14.sp,
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
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
      ),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      minLines = 1,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )

    Spacer(Modifier.height(4.dp))

    Surface(
      shape = RoundedCornerShape(6.dp),
      shadowElevation = 3.dp,
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

        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(4.dp),
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.35f))
              .clickable {
                onBookMoreClick(book.id)
                menuExpanded = true
              },
          ) {
            Icon(
              imageVector = ClioIcons.MoreVert,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp),
            )
          }

          DropdownMenu(
            expanded = menuExpanded && selectedBookId == book.id,
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

    BookRemainingProgressRow(
      remainingTime = book.remainingTime,
      progress = book.progress,
      textStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 11.sp,
      ),
    )
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
  GridBook(BookOverviewPreviewParameterProvider().book().copy(progress = 0.66f), {}, {}, null, emptyList(), { _, _ -> })
}

@Composable
@Preview(widthDp = 200)
private fun GridBookPreviewWithoutProgress() {
  GridBook(BookOverviewPreviewParameterProvider().book().copy(progress = 0f), {}, {}, null, emptyList(), { _, _ -> })
}
