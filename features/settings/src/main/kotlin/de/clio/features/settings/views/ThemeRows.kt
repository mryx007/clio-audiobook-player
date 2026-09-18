package de.clio.features.settings.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.clio.core.data.PlaybackBackgroundStyle
import de.clio.core.data.ThemeColor
import de.clio.core.data.ThemeMode
import de.clio.core.ui.icons.ClioIcons
import de.clio.core.strings.R as StringsR

@Composable
internal fun ThemeModeRow(
  themeMode: ThemeMode,
  onClick: () -> Unit,
) {
  SelectionRow(
    title = stringResource(StringsR.string.settings_appearance_theme_title),
    value = themeMode.label(),
    leadingIcon = ClioIcons.Palette,
    onClick = onClick,
  )
}

@Composable
internal fun PlaybackBackgroundStyleRow(
  style: PlaybackBackgroundStyle,
  onClick: () -> Unit,
) {
  SelectionRow(
    title = stringResource(StringsR.string.settings_playback_background_style_title),
    value = style.label(),
    leadingIcon = ClioIcons.Image,
    onClick = onClick,
  )
}

@Composable
internal fun ThemeModeDialog(
  selectedThemeMode: ThemeMode,
  customThemeColor: ThemeColor,
  onThemeModeSelect: (ThemeMode) -> Unit,
  onCustomThemeSelect: (String, Int) -> Unit,
  onDismiss: () -> Unit,
) {
  var temporarySelection by remember(selectedThemeMode) {
    mutableStateOf(selectedThemeMode)
  }
  var showCustomHexDialog by remember { mutableStateOf(false) }
  var currentCustomHex by remember(customThemeColor) { mutableStateOf(customThemeColor.hex) }
  var currentCustomHue by remember(customThemeColor) { mutableFloatStateOf(customThemeColor.hue.toFloat()) }

  if (showCustomHexDialog) {
    CustomHexColorDialog(
      initialHex = currentCustomHex,
      initialHue = currentCustomHue,
      onConfirm = { hex, hue ->
        currentCustomHex = hex
        currentCustomHue = hue
        showCustomHexDialog = false
        temporarySelection = ThemeMode.Custom
        onCustomThemeSelect(hex, hue.toInt())
      },
      onDismiss = { showCustomHexDialog = false },
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
      ) {
        selectableThemeModes.forEach { themeMode ->
          val previewColor = if (themeMode == ThemeMode.Custom) {
            val baseColor = Color(ThemeColor(hex = currentCustomHex).parseColor())
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(baseColor.toArgb(), hsv)
            if (hsv[1] >= 0.12f) baseColor else Color(android.graphics.Color.HSVToColor(floatArrayOf(currentCustomHue, 0.70f, 0.90f)))
          } else {
            themeMode.previewColor()
          }
          ThemeModeDialogItem(
            themeMode = themeMode,
            previewColor = previewColor,
            selected = themeMode == temporarySelection,
            onClick = {
              temporarySelection = themeMode
              if (themeMode == ThemeMode.Custom) {
                showCustomHexDialog = true
              }
            },
          )
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (temporarySelection == ThemeMode.Custom) {
            onCustomThemeSelect(currentCustomHex, currentCustomHue.toInt())
          } else {
            onThemeModeSelect(temporarySelection)
          }
        },
        content = {
          Text(stringResource(StringsR.string.common_dialog_confirm))
        },
      )
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        content = {
          Text(stringResource(StringsR.string.common_dialog_cancel))
        },
      )
    },
  )
}

@Composable
private fun CustomHexColorDialog(
  initialHex: String,
  initialHue: Float,
  onConfirm: (String, Float) -> Unit,
  onDismiss: () -> Unit,
) {
  val initialHsv = remember(initialHex) { hexToHsv(initialHex) }
  var hue by remember(initialHex, initialHue) {
    mutableFloatStateOf(if (initialHsv[1] >= 0.12f) initialHsv[0] else initialHue)
  }
  var saturation by remember(initialHex) { mutableFloatStateOf(initialHsv[1]) }
  var value by remember(initialHex) { mutableFloatStateOf(initialHsv[2]) }
  var hexText by remember(initialHex) { mutableStateOf(initialHex) }

  val hueColor = remember(hue) {
    Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
  }
  val currentColor = remember(hue, saturation, value) {
    Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
  }

  val rainbowBrush = remember {
    Brush.horizontalGradient(
      colors = listOf(
        Color.Red,
        Color.Yellow,
        Color.Green,
        Color.Cyan,
        Color.Blue,
        Color.Magenta,
        Color.Red,
      ),
    )
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color(0xFF1E1E1E),
      contentColor = Color.White,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp),
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = stringResource(StringsR.string.settings_appearance_theme_custom_hex_title),
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        // 2D Saturation / Value panel
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(4.dp))
            .pointerInput(Unit) {
              detectTapGestures { offset ->
                val w = size.width.toFloat().coerceAtLeast(1f)
                val h = size.height.toFloat().coerceAtLeast(1f)
                saturation = (offset.x / w).coerceIn(0f, 1f)
                value = (1f - (offset.y / h)).coerceIn(0f, 1f)
                hexText = hsvToHex(hue, saturation, value)
              }
            }
            .pointerInput(Unit) {
              detectDragGestures { change, _ ->
                change.consume()
                val w = size.width.toFloat().coerceAtLeast(1f)
                val h = size.height.toFloat().coerceAtLeast(1f)
                saturation = (change.position.x / w).coerceIn(0f, 1f)
                value = (1f - (change.position.y / h)).coerceIn(0f, 1f)
                hexText = hsvToHex(hue, saturation, value)
              }
            },
        ) {
          Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
              brush = Brush.horizontalGradient(
                colors = listOf(Color.White, hueColor),
              ),
            )
            drawRect(
              brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black),
              ),
            )

            val selectorX = saturation * size.width
            val selectorY = (1f - value) * size.height
            val ringRadius = 10.dp.toPx()

            drawCircle(
              color = Color.Black.copy(alpha = 0.5f),
              radius = ringRadius + 1.5.dp.toPx(),
              center = Offset(selectorX, selectorY),
              style = Stroke(width = 3.dp.toPx()),
            )
            drawCircle(
              color = Color.White,
              radius = ringRadius,
              center = Offset(selectorX, selectorY),
              style = Stroke(width = 2.5.dp.toPx()),
            )
          }
        }

        Spacer(Modifier.height(16.dp))

        // Hue Slider
        BoxWithConstraints(
          modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(Unit) {
              detectTapGestures { offset ->
                val w = size.width.toFloat().coerceAtLeast(1f)
                hue = (offset.x / w).coerceIn(0f, 1f) * 360f
                hexText = hsvToHex(hue, saturation, value)
              }
            }
            .pointerInput(Unit) {
              detectDragGestures { change, _ ->
                change.consume()
                val w = size.width.toFloat().coerceAtLeast(1f)
                hue = (change.position.x / w).coerceIn(0f, 1f) * 360f
                hexText = hsvToHex(hue, saturation, value)
              }
            },
          contentAlignment = Alignment.CenterStart,
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(14.dp)
              .clip(CircleShape)
              .background(rainbowBrush),
          )

          val maxThumbOffset = (maxWidth - 24.dp).coerceAtLeast(0.dp)
          val thumbOffset = maxThumbOffset * (hue / 360f).coerceIn(0f, 1f)
          Box(
            modifier = Modifier
              .offset(x = thumbOffset)
              .size(24.dp)
              .clip(CircleShape)
              .background(hueColor)
              .border(2.5.dp, Color.White, CircleShape),
          )
        }

        Spacer(Modifier.height(16.dp))

        // Color preview circle and Hex text input
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        ) {
          Box(
            modifier = Modifier
              .size(26.dp)
              .clip(CircleShape)
              .background(currentColor)
              .border(1.dp, Color(0xFF444444), CircleShape),
          )
          Spacer(Modifier.width(16.dp))
          BasicTextField(
            value = hexText,
            onValueChange = { input ->
              val filtered = input.filter { it == '#' || it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
              if (filtered.length <= 7) {
                hexText = filtered
                val clean = filtered.removePrefix("#")
                if (clean.length == 6) {
                  val hsv = hexToHsv(filtered)
                  if (hsv[1] >= 0.12f) {
                    hue = hsv[0]
                  }
                  saturation = hsv[1]
                  value = hsv[2]
                }
              }
            },
            textStyle = MaterialTheme.typography.titleMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.Medium,
            ),
            singleLine = true,
            cursorBrush = SolidColor(Color.White),
            decorationBox = { innerTextField ->
              Column {
                innerTextField()
                Spacer(Modifier.height(2.dp))
                Box(
                  modifier = Modifier
                    .width(90.dp)
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.7f)),
                )
              }
            },
          )
        }

        Spacer(Modifier.height(24.dp))

        // Action buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Button(
            onClick = {
              val defaultHsv = hexToHsv("#111111")
              hue = 212f
              saturation = defaultHsv[1]
              value = defaultHsv[2]
              hexText = "#111111"
            },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF2C2C2E),
              contentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          ) {
            Text(
              text = stringResource(StringsR.string.settings_appearance_theme_custom_color_reset),
              style = MaterialTheme.typography.labelMedium,
              maxLines = 1,
              softWrap = false,
            )
          }
          Spacer(Modifier.weight(1f))
          Button(
            onClick = onDismiss,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF2C2C2E),
              contentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
          ) {
            Text(
              text = stringResource(StringsR.string.common_dialog_cancel),
              style = MaterialTheme.typography.labelMedium,
              maxLines = 1,
              softWrap = false,
            )
          }
          Spacer(Modifier.width(8.dp))
          Button(
            onClick = {
              val formatted = if (hexText.startsWith("#")) hexText else "#$hexText"
              onConfirm(formatted, hue)
            },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.White,
              contentColor = Color.Black,
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
            modifier = Modifier.defaultMinSize(minWidth = 56.dp),
          ) {
            Text(
              text = stringResource(StringsR.string.common_dialog_ok),
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
              maxLines = 1,
              softWrap = false,
            )
          }
        }
      }
    }
  }
}

private fun hexToHsv(hex: String): FloatArray {
  val clean = hex.removePrefix("#").trim()
  val colorInt = try {
    when (clean.length) {
      6 -> (0xFF000000L or clean.toLong(16)).toInt()
      8 -> clean.toLong(16).toInt()
      else -> 0xFF111111.toInt()
    }
  } catch (_: Exception) {
    0xFF111111.toInt()
  }
  val hsv = FloatArray(3)
  android.graphics.Color.colorToHSV(colorInt, hsv)
  return hsv
}

private fun hsvToHex(
  h: Float,
  s: Float,
  v: Float,
): String {
  val colorInt = android.graphics.Color.HSVToColor(
    floatArrayOf(
      h.coerceIn(0f, 360f),
      s.coerceIn(0f, 1f),
      v.coerceIn(0f, 1f),
    ),
  )
  return String.format(java.util.Locale.ROOT, "#%06X", 0xFFFFFF and colorInt)
}

private val selectableThemeModes = listOf(
  ThemeMode.FollowSystem,
  ThemeMode.Light,
  ThemeMode.Dark,
  ThemeMode.Amoled,
  ThemeMode.CatppuccinMocha,
  ThemeMode.DarkGray,
  ThemeMode.DarkPink,
  ThemeMode.DarkBlue,
  ThemeMode.DarkGreen,
  ThemeMode.DarkYellow,
  ThemeMode.DarkOrange,
  ThemeMode.DarkRed,
  ThemeMode.Custom,
)

@Composable
private fun ThemeModeDialogItem(
  themeMode: ThemeMode,
  previewColor: Color,
  selected: Boolean,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .selectable(
        selected = selected,
        onClick = onClick,
        role = Role.RadioButton,
      )
      .padding(vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(
      selected = selected,
      onClick = null,
    )
    Spacer(Modifier.width(12.dp))
    if (themeMode == ThemeMode.FollowSystem) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = CircleShape,
          ),
      ) {
        Canvas(modifier = Modifier.matchParentSize()) {
          drawRect(
            color = Color(0xFFF5F5F5),
            size = Size(size.width / 2f, size.height),
          )
          drawRect(
            color = Color(0xFF181C24),
            topLeft = Offset(size.width / 2f, 0f),
            size = Size(size.width / 2f, size.height),
          )
        }
      }
    } else {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(previewColor)
          .then(
            if (themeMode == ThemeMode.Amoled || themeMode == ThemeMode.Light) {
              Modifier.border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape,
              )
            } else {
              Modifier
            },
          ),
      )
    }
    Spacer(Modifier.width(16.dp))
    Text(
      text = themeMode.label(),
      style = MaterialTheme.typography.bodyLarge,
    )
  }
}

private fun ThemeMode.previewColor(): Color {
  return when (this) {
    ThemeMode.FollowSystem -> Color(0xFF4A4D54)
    ThemeMode.Light -> Color(0xFFF5F5F5)
    ThemeMode.Dark -> Color(0xFF181C24)
    ThemeMode.Amoled -> Color(0xFF000000)
    ThemeMode.Dynamic -> Color(0xFF3B5998)
    ThemeMode.CatppuccinMocha -> Color(0xFF1E1E2E)
    ThemeMode.DarkGray, ThemeMode.ClassicYouTube -> Color(0xFF212121)
    ThemeMode.DarkPink -> Color(0xFF381537)
    ThemeMode.DarkBlue -> Color(0xFF0A224A)
    ThemeMode.DarkGreen -> Color(0xFF0B3B14)
    ThemeMode.DarkYellow -> Color(0xFF453C05)
    ThemeMode.DarkOrange -> Color(0xFF4A2305)
    ThemeMode.DarkRed -> Color(0xFF4D0707)
    ThemeMode.Custom -> Color(0xFF003B7F)
  }
}

@Composable
internal fun PlaybackBackgroundStyleDialog(
  selectedStyle: PlaybackBackgroundStyle,
  onStyleSelect: (PlaybackBackgroundStyle) -> Unit,
  onDismiss: () -> Unit,
) {
  var temporarySelection by remember(selectedStyle) {
    mutableStateOf(selectedStyle)
  }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(stringResource(StringsR.string.settings_playback_background_style_title))
    },
    text = {
      Column {
        PlaybackBackgroundStyle.entries.forEach { style ->
          SelectionDialogItem(
            text = style.label(),
            selected = style == temporarySelection,
            onClick = {
              temporarySelection = style
            },
          )
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onStyleSelect(temporarySelection)
        },
        content = {
          Text(stringResource(StringsR.string.common_dialog_confirm))
        },
      )
    },
  )
}

@Composable
internal fun SelectionRow(
  title: String,
  value: String,
  leadingIcon: ImageVector? = null,
  onClick: () -> Unit,
) {
  ListItem(
    modifier = Modifier
      .clickable {
        onClick()
      }
      .fillMaxWidth(),
    leadingContent = if (leadingIcon != null) {
      {
        Icon(
          imageVector = leadingIcon,
          contentDescription = title,
        )
      }
    } else {
      null
    },
    supportingContent = {
      Text(text = value)
    },
  ) {
    Text(text = title)
  }
}

@Composable
internal fun SelectionDialogItem(
  text: String,
  selected: Boolean,
  supportingText: String? = null,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .selectable(
        selected = selected,
        onClick = onClick,
        role = Role.RadioButton,
      )
      .padding(vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(
      selected = selected,
      onClick = null,
    )
    Spacer(Modifier.width(16.dp))
    Column {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
      )
      if (supportingText != null) {
        Text(
          text = supportingText,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun ThemeMode.label(): String {
  return when (this) {
    ThemeMode.FollowSystem -> stringResource(StringsR.string.settings_appearance_theme_follow_system)
    ThemeMode.Light -> stringResource(StringsR.string.settings_appearance_theme_light)
    ThemeMode.Dark -> stringResource(StringsR.string.settings_appearance_theme_dark)
    ThemeMode.Amoled -> stringResource(StringsR.string.settings_appearance_theme_amoled)
    ThemeMode.Dynamic -> stringResource(StringsR.string.settings_appearance_theme_dynamic)
    ThemeMode.CatppuccinMocha -> stringResource(StringsR.string.settings_appearance_theme_catppuccin_mocha)
    ThemeMode.DarkGray, ThemeMode.ClassicYouTube -> stringResource(StringsR.string.settings_appearance_theme_dark_gray)
    ThemeMode.DarkPink -> stringResource(StringsR.string.settings_appearance_theme_dark_pink)
    ThemeMode.DarkBlue -> stringResource(StringsR.string.settings_appearance_theme_dark_blue)
    ThemeMode.DarkGreen -> stringResource(StringsR.string.settings_appearance_theme_dark_green)
    ThemeMode.DarkYellow -> stringResource(StringsR.string.settings_appearance_theme_dark_yellow)
    ThemeMode.DarkOrange -> stringResource(StringsR.string.settings_appearance_theme_dark_orange)
    ThemeMode.DarkRed -> stringResource(StringsR.string.settings_appearance_theme_dark_red)
    ThemeMode.Custom -> stringResource(StringsR.string.settings_appearance_theme_custom)
  }
}

@Composable
private fun PlaybackBackgroundStyle.label(): String {
  return when (this) {
    PlaybackBackgroundStyle.Solid -> stringResource(StringsR.string.settings_playback_background_style_solid)
    PlaybackBackgroundStyle.BlurredCover -> stringResource(StringsR.string.settings_playback_background_style_blurred_cover)
    PlaybackBackgroundStyle.DimmedCover -> stringResource(StringsR.string.settings_playback_background_style_dimmed_cover)
    PlaybackBackgroundStyle.DynamicGradient -> stringResource(StringsR.string.settings_playback_background_style_dynamic_gradient)
    PlaybackBackgroundStyle.AmbientColors -> stringResource(StringsR.string.settings_playback_background_style_ambient_colors)
    PlaybackBackgroundStyle.AmoledBlack -> stringResource(StringsR.string.settings_playback_background_style_amoled_black)
    PlaybackBackgroundStyle.Glassmorphism -> stringResource(StringsR.string.settings_playback_background_style_glassmorphism)
  }
}
