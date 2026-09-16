package voice.features.playbackScreen.view

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import voice.core.data.PlaybackBackgroundStyle

@Composable
internal fun PlaybackBackground(
  cover: String?,
  style: PlaybackBackgroundStyle,
) {
  val context = LocalContext.current
  var dominantColors by remember { mutableStateOf<List<Color>>(emptyList()) }

  // Extract colors if needed for DynamicGradient or AmbientColors
  if (style == PlaybackBackgroundStyle.DynamicGradient || style == PlaybackBackgroundStyle.AmbientColors) {
    LaunchedEffect(cover) {
      if (cover == null) {
        dominantColors = emptyList()
        return@LaunchedEffect
      }
      val loader = coil.ImageLoader(context)
      val request = ImageRequest.Builder(context)
        .data(cover)
        .allowHardware(false) // Required for Palette
        .build()
      val result = loader.execute(request)
      val bitmap = result.drawable?.toBitmap()
      if (bitmap != null) {
        val palette = Palette.from(bitmap).generate()
        val swatches = listOfNotNull(
          palette.vibrantSwatch,
          palette.darkVibrantSwatch,
          palette.mutedSwatch,
          palette.darkMutedSwatch
        ).sortedByDescending { it.population }

        dominantColors = swatches.map { Color(it.rgb) }
      }
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    when (style) {
      PlaybackBackgroundStyle.Solid -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
        )
      }
      PlaybackBackgroundStyle.AmoledBlack -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
        )
      }
      PlaybackBackgroundStyle.BlurredCover, PlaybackBackgroundStyle.DimmedCover, PlaybackBackgroundStyle.Glassmorphism -> {
        val blurRadius = when (style) {
          PlaybackBackgroundStyle.BlurredCover -> 60.dp
          PlaybackBackgroundStyle.Glassmorphism -> 40.dp
          PlaybackBackgroundStyle.DimmedCover -> 0.dp
        }
        val dimAlpha = when (style) {
          PlaybackBackgroundStyle.DimmedCover -> 0.7f
          PlaybackBackgroundStyle.BlurredCover -> 0.5f
          PlaybackBackgroundStyle.Glassmorphism -> 0.3f
        }

        AsyncImage(
          model = cover,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .fillMaxSize()
            .blur(blurRadius)
            .drawWithContent {
              drawContent()
              drawRect(Color.Black.copy(alpha = dimAlpha))
            }
        )
      }
      PlaybackBackgroundStyle.DynamicGradient -> {
        val defaultDarkSurface = Color(0xFF1E1E1E)
        val defaultDarkVariant = Color(0xFF121212)
        val color1 by animateColorAsState(
          targetValue = dominantColors.getOrElse(0) { defaultDarkSurface },
          animationSpec = tween(500)
        )
        val color2 by animateColorAsState(
          targetValue = dominantColors.getOrElse(1) { defaultDarkVariant },
          animationSpec = tween(500)
        )

        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(color1, color2)
              )
            )
            .drawWithContent {
               drawContent()
               drawRect(Color.Black.copy(alpha = 0.3f))
            }
        )
      }
      PlaybackBackgroundStyle.AmbientColors -> {
        val defaultDark1 = Color(0xFF1E1E1E)
        val defaultDark2 = Color(0xFF161616)
        val defaultDark3 = Color(0xFF101010)
        val color1 by animateColorAsState(dominantColors.getOrElse(0) { defaultDark1 }, tween(500))
        val color2 by animateColorAsState(dominantColors.getOrElse(1) { defaultDark2 }, tween(500))
        val color3 by animateColorAsState(dominantColors.getOrElse(2) { defaultDark3 }, tween(500))

        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.linearGradient(
                0.0f to color1,
                0.5f to color2,
                1.0f to color3
              )
            )
            .blur(100.dp)
            .drawWithContent {
               drawContent()
               drawRect(Color.Black.copy(alpha = 0.4f))
            }
        )
      }
    }
  }
}
