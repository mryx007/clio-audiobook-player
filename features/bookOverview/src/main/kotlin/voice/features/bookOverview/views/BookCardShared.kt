package voice.features.bookOverview.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voice.core.data.BookId
import voice.core.strings.R as StringsR

@Composable
internal fun BookCard(
  bookId: BookId,
  onBookClick: (BookId) -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ),
    modifier = modifier
      .fillMaxWidth()
      .clickable(
        onClick = { onBookClick(bookId) },
      ),
  ) {
    content()
  }
}

@Composable
internal fun NewBadge(
  modifier: Modifier = Modifier,
) {
  val surfaceColor = MaterialTheme.colorScheme.surface
  val isDark = (0.299f * surfaceColor.red + 0.587f * surfaceColor.green + 0.114f * surfaceColor.blue) < 0.5f
  val badgeBgColor = if (isDark) Color(0xFF1E3A24) else Color(0xFFE8F5E9)
  val badgeTextColor = if (isDark) Color(0xFF81C784) else Color(0xFF1B5E20)
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .background(badgeBgColor)
      .padding(horizontal = 6.dp, vertical = 2.dp),
  ) {
    Text(
      text = stringResource(StringsR.string.book_badge_new),
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
      ),
      color = badgeTextColor,
    )
  }
}

@Composable
internal fun BookRemainingProgressRow(
  remainingTime: String,
  progress: Float,
  modifier: Modifier = Modifier,
  remainingTimeMaxLines: Int = Int.MAX_VALUE,
  progressMaxLines: Int = Int.MAX_VALUE,
  textStyle: TextStyle = MaterialTheme.typography.labelMedium,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = remainingTime,
      style = textStyle,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = remainingTimeMaxLines,
    )
    Text(
      text = "${(progress * 100).toInt()}%",
      style = textStyle,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = progressMaxLines,
    )
  }
}

@Composable
internal fun BookProgressIndicator(
  progress: Float,
  modifier: Modifier = Modifier,
  color: Color? = null,
  trackColor: Color? = null,
) {
  if (progress > 0.05f) {
    val actualColor = color ?: MaterialTheme.colorScheme.primary
    val actualTrackColor = trackColor ?: MaterialTheme.colorScheme.surfaceVariant
    Box(
      modifier = modifier
        .background(actualTrackColor, MaterialTheme.shapes.small)
        .clip(MaterialTheme.shapes.small)
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(progress)
          .background(actualColor)
      )
    }
  }
}
