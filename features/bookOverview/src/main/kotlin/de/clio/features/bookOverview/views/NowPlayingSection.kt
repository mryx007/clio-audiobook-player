package de.clio.features.bookOverview.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.clio.features.bookOverview.overview.BookOverviewItemViewState
import de.clio.core.strings.R as StringsR
import de.clio.core.ui.R as UiR

@Composable
internal fun NowPlayingSection(
  book: BookOverviewItemViewState,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(
      text = stringResource(StringsR.string.queue_now_playing).uppercase(),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.primary,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 2.dp),
    )

    Surface(
      onClick = onClick,
      shape = RoundedCornerShape(6.dp),
      color = Color.Transparent,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 8.dp, end = 16.dp, top = 2.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(6.dp)),
          contentAlignment = Alignment.Center,
        ) {
          AsyncImage(
            model = book.cover,
            contentDescription = book.name,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            fallback = painterResource(id = UiR.drawable.album_art),
            error = painterResource(id = UiR.drawable.album_art),
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = book.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          val subtitle = buildString {
            book.author?.let { append(it) }
            if (book.remainingTime.isNotEmpty()) {
              if (isNotEmpty()) append(" • ")
              append(book.remainingTime)
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
      }
    }

    HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 2.dp))
  }
}
