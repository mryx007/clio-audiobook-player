package de.clio.features.playbackScreen.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.ui.icons.ClioIcons
import de.clio.features.playbackScreen.BookPlayViewState.BookPlayChapter

@Composable
internal fun ChaptersOverlay(
  chapters: List<BookPlayChapter>,
  backgroundStyle: PlaybackBackgroundStyle,
  onChapterClick: (Int) -> Unit,
  modifier: Modifier = Modifier,
  chapterName: String? = null,
  isFullyExpanded: Boolean = false,
  onExpand: () -> Unit = {},
  onCollapse: () -> Unit = {},
  onClose: () -> Unit = {},
) {
  val isDarkSurface = when (backgroundStyle) {
    PlaybackBackgroundStyle.AmoledBlack -> true
    else -> {
      val surface = MaterialTheme.colorScheme.surfaceContainerHigh
      (0.299f * surface.red + 0.587f * surface.green + 0.114f * surface.blue) < 0.5f
    }
  }

  val containerColor = when (backgroundStyle) {
    PlaybackBackgroundStyle.AmoledBlack -> Color(0xFF121212)
    else -> MaterialTheme.colorScheme.surfaceContainerHigh
  }

  val onSurfaceColor = if (isDarkSurface) Color.White else MaterialTheme.colorScheme.onSurface
  val secondaryTextColor = if (isDarkSurface) Color.White.copy(alpha = 0.72f) else MaterialTheme.colorScheme.onSurfaceVariant
  val handleColor = if (isDarkSurface) Color.White.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
  val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDarkSurface) 0.25f else 0.45f)

  val selectedIndex = chapters.indexOfFirst { it.active }
  val initialFirstVisibleItemIndex = (selectedIndex - 1).coerceAtLeast(0)
  val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialFirstVisibleItemIndex)
  val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

  val density = LocalDensity.current
  val swipeThresholdPx = with(density) { 20.dp.toPx() }
  var dragDistance by remember { mutableFloatStateOf(0f) }
  val draggableState = rememberDraggableState { delta ->
    dragDistance += delta
    if (delta < 0f && dragDistance < -swipeThresholdPx) {
      if (!isFullyExpanded) {
        onExpand()
        dragDistance = 0f
      }
    } else if (delta > 0f && dragDistance > swipeThresholdPx) {
      if (isFullyExpanded) {
        onCollapse()
        dragDistance = 0f
      } else {
        onClose()
      }
    }
  }

  val nestedScrollConnection = remember(isFullyExpanded) {
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (!isFullyExpanded && available.y < -15f && source == NestedScrollSource.UserInput) {
          onExpand()
          return Offset.Zero
        }
        return Offset.Zero
      }

      override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
      ): Offset {
        if (available.y > 15f && source == NestedScrollSource.UserInput) {
          if (isFullyExpanded) {
            onCollapse()
          } else {
            onClose()
          }
          return Offset(0f, available.y)
        }
        return Offset.Zero
      }
    }
  }

  Surface(
    modifier = modifier.fillMaxSize(),
    color = containerColor,
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    border = BorderStroke(
      width = 1.dp,
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDarkSurface) 0.35f else 0.5f),
    ),
    tonalElevation = 8.dp,
    shadowElevation = 16.dp,
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { if (isFullyExpanded) onCollapse() else onClose() }
          .draggable(
            state = draggableState,
            orientation = Orientation.Vertical,
            onDragStarted = { dragDistance = 0f },
            onDragStopped = { velocity ->
              if (velocity < -120f || dragDistance < -swipeThresholdPx) {
                if (!isFullyExpanded) onExpand()
              } else if (velocity > 120f || dragDistance > swipeThresholdPx) {
                if (isFullyExpanded) onCollapse() else onClose()
              }
              dragDistance = 0f
            },
          ),
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
          contentAlignment = Alignment.Center,
        ) {
          Box(
            modifier = Modifier
              .size(width = 36.dp, height = 4.dp)
              .background(
                color = handleColor,
                shape = RoundedCornerShape(2.dp),
              ),
          )
        }

        if (chapterName != null) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 10.dp),
          ) {
            Text(
              text = chapterName,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = onSurfaceColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(modifier = Modifier.size(6.dp))
            Icon(
              imageVector = ClioIcons.ExpandMore,
              contentDescription = null,
              tint = onSurfaceColor,
              modifier = Modifier
                .size(20.dp)
                .rotate(if (isFullyExpanded) 0f else 180f),
            )
          }
        }
        HorizontalDivider(
          color = dividerColor,
          thickness = 1.dp,
        )
      }

      LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
          start = 8.dp,
          end = 8.dp,
          top = 6.dp,
          bottom = 12.dp + navBarPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
          .fillMaxSize()
          .nestedScroll(nestedScrollConnection),
      ) {
        items(chapters, key = { it.number }) { chapter ->
          val isCurrent = chapter.active
          val itemBackgroundColor = if (isCurrent) {
            MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkSurface) 0.22f else 0.14f)
          } else {
            Color.Transparent
          }
          val textColor = if (isCurrent) {
            MaterialTheme.colorScheme.primary
          } else {
            onSurfaceColor
          }
          val subtitleColor = if (isCurrent) {
            MaterialTheme.colorScheme.primary
          } else {
            secondaryTextColor
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(itemBackgroundColor)
              .clickable { onChapterClick(chapter.number) }
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (isCurrent) {
              Icon(
                imageVector = ClioIcons.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                  .size(16.dp)
                  .padding(end = 4.dp),
              )
            }

            Text(
              text = "${chapter.number}.",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
              color = subtitleColor,
              maxLines = 1,
              modifier = Modifier.padding(end = 8.dp),
            )
            Text(
              text = chapter.name,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
              color = textColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
              text = chapter.time,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
              color = subtitleColor,
            )
          }
        }
      }
    }
  }
}
