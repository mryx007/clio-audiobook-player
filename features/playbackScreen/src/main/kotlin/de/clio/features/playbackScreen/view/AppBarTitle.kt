package de.clio.features.playbackScreen.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal fun AppBarTitle(
  title: String,
  modifier: Modifier = Modifier,
  maxLines: Int = 2,
  color: Color = Color.Unspecified,
  textAlign: TextAlign = TextAlign.Start,
) {
  Text(
    text = title,
    modifier = modifier,
    maxLines = maxLines,
    overflow = TextOverflow.Ellipsis,
    color = color,
    textAlign = textAlign,
    style = MaterialTheme.typography.titleLarge.copy(
      fontWeight = FontWeight.Bold,
    ),
  )
}

