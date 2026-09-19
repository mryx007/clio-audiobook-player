package de.clio.features.playbackScreen.view

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import de.clio.core.data.Book
import de.clio.core.data.BookId
import de.clio.core.strings.R as StringsR
import de.clio.core.ui.R as UiR
import de.clio.core.ui.formatTime
import de.clio.core.ui.icons.ClioIcons
import de.clio.features.playbackScreen.BookPlayDialogViewState
import java.util.Collections
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Suppress("DEPRECATION")
@Composable
internal fun QueueBottomSheet(
  dialogState: BookPlayDialogViewState.QueueSheet,
  onDismiss: () -> Unit,
  onBookClick: (BookId) -> Unit,
  onRemoveFromQueue: (Set<BookId>) -> Unit,
  onClearQueue: () -> Unit,
  onReorderQueue: (List<BookId>) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
  var selectedBookIds by remember { mutableStateOf(emptySet<BookId>()) }

  var currentBooks by remember { mutableStateOf(dialogState.queueItems) }
  var draggingBookId by remember { mutableStateOf<BookId?>(null) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(dialogState.queueItems, draggingBookId) {
    if (draggingBookId == null) {
      currentBooks = dialogState.queueItems
    }
  }
  val density = LocalDensity.current
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
  ) {
    BackHandler(enabled = selectedBookIds.isNotEmpty()) {
      selectedBookIds = emptySet()
    }

    BackHandler(
      enabled = selectedBookIds.isEmpty() &&
        (sheetState.targetValue == SheetValue.Expanded || sheetState.currentValue == SheetValue.Expanded),
    ) {
      scope.launch {
        sheetState.partialExpand()
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
    ) {
      // Header
      if (selectedBookIds.isEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = if (dialogState.queueItems.isNotEmpty()) {
              "${stringResource(StringsR.string.queue_title)} (${dialogState.queueItems.size})"
            } else {
              stringResource(StringsR.string.queue_title)
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.weight(1f))
          if (dialogState.queueItems.isNotEmpty()) {
            IconButton(onClick = onClearQueue) {
              Icon(
                imageVector = ClioIcons.Delete,
                contentDescription = stringResource(StringsR.string.queue_action_clear),
              )
            }
          }
        }
      } else {
        // Selection Mode Header
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(onClick = { selectedBookIds = emptySet() }) {
              Icon(
                imageVector = ClioIcons.Close,
                contentDescription = stringResource(StringsR.string.common_action_close),
              )
            }
            Text(
              text = stringResource(StringsR.string.selection_title, selectedBookIds.size),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
              onClick = {
                onRemoveFromQueue(selectedBookIds)
                selectedBookIds = emptySet()
              },
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

      // Now Playing (if available and not in selection mode)
      if (selectedBookIds.isEmpty() && dialogState.currentBook != null) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
          Text(
            text = stringResource(StringsR.string.queue_now_playing).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(6.dp))
          QueueBookItemRow(
            book = dialogState.currentBook,
            index = null,
            isSelected = false,
            onClick = onDismiss,
            onLongClick = {},
            isCurrentPlaying = true,
          )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
      }

      // Up Next section
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 16.dp),
      ) {
        Text(
          text = stringResource(StringsR.string.queue_next).uppercase(),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(top = 8.dp, bottom = 6.dp),
        )

        if (dialogState.queueItems.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = stringResource(StringsR.string.queue_empty),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        } else {
          LazyColumn(
            state = listState,
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            itemsIndexed(currentBooks, key = { _, book -> book.id.value }) { index, book ->
              val isDragging = draggingBookId == book.id
              val isSelected = book.id in selectedBookIds

              val itemModifier = Modifier
                .fillMaxWidth()
                .zIndex(if (isDragging) 2f else 1f)
                .graphicsLayer {
                  translationY = if (isDragging) dragOffsetY else 0f
                }
                .then(
                  if (isDragging) Modifier else Modifier.animateItem(),
                )

              QueueBookItemRow(
                book = book,
                index = index + 1,
                isSelected = isSelected,
                onClick = {
                  if (selectedBookIds.isNotEmpty()) {
                    selectedBookIds = if (isSelected) selectedBookIds - book.id else selectedBookIds + book.id
                  } else {
                    onBookClick(book.id)
                  }
                },
                onLongClick = {
                  selectedBookIds = if (isSelected) selectedBookIds - book.id else selectedBookIds + book.id
                },
                isCurrentPlaying = false,
                modifier = itemModifier,
                trailingContent = if (selectedBookIds.isEmpty()) {
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
                                  ?: with(density) { 64.dp.toPx() }
                                val spacing = with(density) { 4.dp.toPx() }
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
                              currentBooks = dialogState.queueItems
                            },
                          )
                        },
                      contentAlignment = Alignment.Center,
                    ) {
                      Icon(
                        imageVector = ClioIcons.DragHandle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                } else null,
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QueueBookItemRow(
  book: Book,
  index: Int?,
  isSelected: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  isCurrentPlaying: Boolean,
  modifier: Modifier = Modifier,
  trailingContent: (@Composable () -> Unit)? = null,
) {
  val shape = if (isCurrentPlaying) CircleShape else RoundedCornerShape(12.dp)
  val backgroundColor = when {
    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    isCurrentPlaying -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    else -> Color.Transparent
  }
  val borderModifier = if (isCurrentPlaying) {
    Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f), shape)
  } else {
    Modifier
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(backgroundColor)
      .then(borderModifier)
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick,
      )
      .padding(horizontal = 8.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (index != null) {
      Text(
        text = index.toString(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.width(28.dp),
      )
    }

    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(RoundedCornerShape(6.dp)),
      contentAlignment = Alignment.Center,
    ) {
      AsyncImage(
        model = book.content.coverUrl,
        contentDescription = book.content.name,
        modifier = Modifier.matchParentSize(),
        contentScale = ContentScale.Crop,
        fallback = painterResource(id = UiR.drawable.album_art),
        error = painterResource(id = UiR.drawable.album_art),
      )
      if (isSelected) {
        Box(
          modifier = Modifier
            .matchParentSize()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = ClioIcons.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp),
          )
        }
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = book.content.name,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (isCurrentPlaying) FontWeight.Bold else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      val subtitle = buildString {
        book.content.author?.let { append(it) }
        if (book.duration > 0L) {
          if (isNotEmpty()) append(" • ")
          append(formatTime(book.duration))
        }
      }
      if (subtitle.isNotEmpty()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    if (trailingContent != null) {
      trailingContent()
    }
  }
}
