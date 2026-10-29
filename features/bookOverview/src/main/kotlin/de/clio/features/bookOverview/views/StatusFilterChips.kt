package de.clio.features.bookOverview.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.clio.core.data.BookId
import de.clio.features.bookOverview.overview.BookOverviewCategory
import de.clio.features.bookOverview.overview.BookOverviewItemViewState
import de.clio.core.strings.R as StringsR

@Composable
internal fun StatusFilterChips(
  selectedCategory: BookOverviewCategory?,
  onCategorySelect: (BookOverviewCategory?) -> Unit,
  booksByCategory: Map<BookOverviewCategory, Map<BookId, State<BookOverviewItemViewState>>>,
  totalCount: Int,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()

  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalFadingEdge(scrollState, length = 28.dp)
      .horizontalScroll(scrollState),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    StatusFilterPill(
      label = stringResource(StringsR.string.library_category_all_title),
      count = totalCount,
      isSelected = selectedCategory == null,
      onClick = { onCategorySelect(null) },
    )
    val categories = listOf(
      BookOverviewCategory.STARTED,
      BookOverviewCategory.UNBEGUN,
      BookOverviewCategory.COMPLETED,
    )
    categories.forEach { category ->
      val count = booksByCategory[category]?.size ?: 0
      if (count > 0 || selectedCategory == category) {
        StatusFilterPill(
          label = stringResource(category.nameRes),
          count = count,
          isSelected = selectedCategory == category,
          onClick = {
            onCategorySelect(if (selectedCategory == category) null else category)
          },
        )
      }
    }
    Spacer(modifier = Modifier.width(8.dp))
  }
}

private fun Modifier.horizontalFadingEdge(
  scrollState: ScrollState,
  length: Dp = 24.dp,
): Modifier = this
  .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
  .drawWithContent {
    drawContent()
    val lengthPx = length.toPx()
    val canScrollBackward = scrollState.value > 0
    val canScrollForward = scrollState.value < scrollState.maxValue

    if (canScrollBackward) {
      drawRect(
        brush = Brush.horizontalGradient(
          colors = listOf(Color.Transparent, Color.Black),
          startX = 0f,
          endX = lengthPx,
        ),
        topLeft = Offset.Zero,
        size = Size(lengthPx, size.height),
        blendMode = BlendMode.DstIn,
      )
    }
    if (canScrollForward) {
      drawRect(
        brush = Brush.horizontalGradient(
          colors = listOf(Color.Black, Color.Transparent),
          startX = size.width - lengthPx,
          endX = size.width,
        ),
        topLeft = Offset(size.width - lengthPx, 0f),
        size = Size(lengthPx, size.height),
        blendMode = BlendMode.DstIn,
      )
    }
  }

@Composable
private fun StatusFilterPill(
  label: String,
  count: Int,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onClick,
    shape = CircleShape,
    color = if (isSelected) {
      MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    } else {
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    },
    border = BorderStroke(
      width = 1.dp,
      color = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.60f)
      } else {
        Color.Transparent
      },
    ),
    modifier = modifier.height(32.dp),
  ) {
    Row(
      modifier = Modifier.padding(start = 12.dp, end = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        },
      )
      Surface(
        shape = CircleShape,
        color = if (isSelected) {
          MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        } else {
          MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        },
      ) {
        Text(
          text = count.toString(),
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
          fontWeight = FontWeight.SemiBold,
          color = if (isSelected) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          },
        )
      }
    }
  }
}
