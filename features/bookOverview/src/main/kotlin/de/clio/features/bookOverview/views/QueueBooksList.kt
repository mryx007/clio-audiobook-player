package de.clio.features.bookOverview.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import de.clio.core.data.BookId
import de.clio.core.ui.icons.ClioIcons
import de.clio.features.bookOverview.overview.BookOverviewCategory
import de.clio.features.bookOverview.overview.BookOverviewItemViewState
import de.clio.features.bookOverview.overview.OverviewTab
import kotlinx.coroutines.launch
import java.util.Collections
import de.clio.core.strings.R as StringsR

@Composable
internal fun QueueBooksList(
  books: List<BookOverviewItemViewState>,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  selectedBookIds: Set<BookId>,
  inSelectionMode: Boolean,
  selectedTab: OverviewTab,
  onTabSelect: (OverviewTab) -> Unit,
  queueCount: Int,
  allBookIds: Set<BookId>,
  onSelectAllClick: () -> Unit,
  onDeleteSelectedClick: () -> Unit,
  onReorderQueue: (List<BookId>) -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
  isSearching: Boolean = false,
  currentBook: BookOverviewItemViewState? = null,
) {
  var currentBooks by remember { mutableStateOf(books) }
  var draggingBookId by remember { mutableStateOf<BookId?>(null) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(books, draggingBookId) {
    if (draggingBookId == null) {
      currentBooks = books
    }
  }
  val density = LocalDensity.current
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  LazyColumn(
    state = listState,
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = contentPadding,
  ) {
    item(
      key = "queue_header",
      contentType = "header",
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        val allSelected = selectedBookIds.size == allBookIds.size && allBookIds.isNotEmpty()
        Header(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
          category = BookOverviewCategory.OVERVIEW,
          selectedTab = selectedTab,
          onTabSelect = onTabSelect,
          queueCount = queueCount,
          inSelectionMode = inSelectionMode,
          selectedCount = selectedBookIds.size,
          allSelected = allSelected,
          onSelectAllClick = onSelectAllClick,
          onDeleteSelectedClick = onDeleteSelectedClick,
        )

        if (currentBook != null && !inSelectionMode) {
          NowPlayingSection(
            book = currentBook,
            onClick = { onBookClick(currentBook.id) },
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp),
          )
          if (currentBooks.isNotEmpty()) {
            Text(
              text = stringResource(StringsR.string.queue_next).uppercase(),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 2.dp),
            )
          }
        }
      }
    }

    if (currentBooks.isEmpty()) {
      item(key = "empty_queue") {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 64.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
          ) {
            Icon(
              imageVector = ClioIcons.QueueMusic,
              contentDescription = null,
              modifier = Modifier.size(64.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = stringResource(StringsR.string.queue_empty),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = stringResource(StringsR.string.queue_empty_hint),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.outline,
              textAlign = TextAlign.Center,
            )
          }
        }
      }
    } else {
      itemsIndexed(
        items = currentBooks,
        key = { _, book -> book.id.value },
        contentType = { _, _ -> "queue_item" },
      ) { index, book ->
        val isDragging = draggingBookId == book.id

        val itemModifier = Modifier
          .fillMaxWidth()
          .zIndex(if (isDragging) 2f else 1f)
          .graphicsLayer {
            translationY = if (isDragging) dragOffsetY else 0f
          }
          .then(
            if (isDragging) Modifier else Modifier.animateItem(),
          )

        Row(
          modifier = itemModifier,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "${index + 1}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center,
          )
          ListBookRow(
            book = book,
            onBookClick = onBookClick,
            onBookLongClick = onBookLongClick,
            isSelected = book.id in selectedBookIds,
            inSelectionMode = inSelectionMode,
            showCheckboxInSelectionMode = false,
            clickable = true,
            enableSharedTransition = false,
            onBookMoreClick = {},
            selectedBookId = null,
            menuItems = emptyList(),
            onMenuItemClick = { _, _ -> },
            trailingContent = if (isSearching) {
              null
            } else {
              {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .pointerInput(book.id) {
                      detectDragGestures(
                        onDragStart = {
                          draggingBookId = book.id
                          dragOffsetY = 0f
                        },
                        onDrag = { change, dragAmount ->
                          change.consume()
                          dragOffsetY += dragAmount.y

                          var currentIdx = currentBooks.indexOfFirst { it.id == book.id }
                          if (currentIdx != -1) {
                            val visibleItems = listState.layoutInfo.visibleItemsInfo
                            val currentItemInfo = visibleItems.firstOrNull { it.key == book.id.value }
                            val itemHeight = currentItemInfo?.size?.toFloat()
                              ?: with(density) { 96.dp.toPx() }
                            val spacing = with(density) { 12.dp.toPx() }
                            val slotDistance = itemHeight + spacing
                            val threshold = slotDistance * 0.65f

                            val next = currentBooks.toMutableList()
                            var changed = false
                            while (dragOffsetY > threshold && currentIdx < next.lastIndex) {
                              Collections.swap(next, currentIdx, currentIdx + 1)
                              currentIdx++
                              dragOffsetY -= slotDistance
                              changed = true
                            }
                            while (dragOffsetY < -threshold && currentIdx > 0) {
                              Collections.swap(next, currentIdx, currentIdx - 1)
                              currentIdx--
                              dragOffsetY += slotDistance
                              changed = true
                            }
                            if (changed) {
                              currentBooks = next
                            }
                          }
                        },
                        onDragEnd = {
                          val finalOrder = currentBooks.map { it.id }
                          onReorderQueue(finalOrder)
                          scope.launch {
                            if (dragOffsetY != 0f) {
                              Animatable(dragOffsetY).animateTo(
                                targetValue = 0f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                              ) {
                                dragOffsetY = value
                              }
                            }
                            draggingBookId = null
                            dragOffsetY = 0f
                          }
                        },
                        onDragCancel = {
                          draggingBookId = null
                          dragOffsetY = 0f
                          currentBooks = books
                        },
                      )
                    },
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = ClioIcons.DragHandle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp),
                  )
                }
              }
            },
            modifier = Modifier.weight(1f),
          )
        }
      }
      item {
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
      }
    }
  }
}
