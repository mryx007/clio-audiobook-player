package de.clio.features.playbackScreen.view

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.clio.core.data.BookId
import de.clio.core.ui.sharedCoverElementModifier
import de.clio.core.strings.R as StringsR
import de.clio.core.ui.R as UiR

@Composable
internal fun Cover(
  bookId: BookId,
  onDoubleClick: () -> Unit,
  cover: String?,
  onCoverLoad: ((Float) -> Unit)? = null,
) {
  AsyncImage(
    modifier = Modifier
      .fillMaxSize()
      .clip(RoundedCornerShape(6.dp))
      .sharedCoverElementModifier(bookId)
      .pointerInput(Unit) {
        detectTapGestures(
          onDoubleTap = {
            onDoubleClick()
          },
        )
      },
    contentScale = ContentScale.Crop,
    model = cover,
    onSuccess = { state ->
      val drawable = state.result.drawable
      if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
        onCoverLoad?.invoke(drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat())
      }
    },
    placeholder = null,
    fallback = painterResource(id = UiR.drawable.album_art),
    error = painterResource(id = UiR.drawable.album_art),
    contentDescription = stringResource(id = StringsR.string.cover_title),
  )
}
