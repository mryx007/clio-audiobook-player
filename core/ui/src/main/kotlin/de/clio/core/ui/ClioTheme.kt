package de.clio.core.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.DynamicMaterialExpressiveTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import de.clio.core.data.ThemeColor
import de.clio.core.data.ThemeMode

val VoiceBlue = Color(0xFF003b7f)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ClioTheme(
  themeMode: ThemeMode = ThemeMode.FollowSystem,
  themeColor: ThemeColor = ThemeColor(),
  content: @Composable () -> Unit,
) {
  val isCustomDark = remember(themeColor) {
    val baseColor = Color(themeColor.parseColor())
    (0.299f * baseColor.red + 0.587f * baseColor.green + 0.114f * baseColor.blue) < 0.45f
  }
  val darkTheme = when (themeMode) {
    ThemeMode.Light -> false
    ThemeMode.FollowSystem, ThemeMode.Dynamic -> isSystemInDarkTheme()
    ThemeMode.Custom -> isCustomDark
    else -> true
  }
  val themedContent = remember(content) {
    movableContentOf {
      content()
    }
  }

  when (themeMode) {
    ThemeMode.Dynamic -> {
      if (Build.VERSION.SDK_INT >= 31) {
        MaterialExpressiveTheme(
          colorScheme = systemDynamicColorScheme(darkTheme),
        ) {
          themedContent()
        }
      } else {
        DynamicMaterialExpressiveTheme(
          primary = VoiceBlue,
          secondary = Color(0xFF5E6F95),
          isDark = darkTheme,
          style = PaletteStyle.Expressive,
          specVersion = ColorSpec.SpecVersion.SPEC_2025,
        ) {
          themedContent()
        }
      }
    }
    ThemeMode.Amoled -> {
      val scheme = rememberDynamicColorScheme(
        primary = Color(0xFFE5E5E5),
        secondary = Color(0xFFCCCCCC),
        neutral = Color.Black,
        neutralVariant = Color(0xFF1C1C1E),
        isDark = true,
        style = PaletteStyle.TonalSpot,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        modifyColorScheme = {
          it.copy(
            primary = Color(0xFFE5E5E5),
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF2C2C2E),
            onPrimaryContainer = Color.White,
            secondary = Color(0xFFCCCCCC),
            onSecondary = Color.Black,
            background = Color.Black,
            surface = Color.Black,
            surfaceDim = Color.Black,
            surfaceBright = Color(0xFF1C1C1E),
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color(0xFF0A0A0A),
            surfaceContainer = Color(0xFF121212),
            surfaceContainerHigh = Color(0xFF1C1C1E),
            surfaceContainerHighest = Color(0xFF242426),
            onBackground = Color(0xFFF1F1F1),
            onSurface = Color(0xFFF1F1F1),
            onSurfaceVariant = Color(0xFFB0B0B0),
          )
        },
      )
      MaterialExpressiveTheme(colorScheme = scheme) {
        themedContent()
      }
    }
    ThemeMode.CatppuccinMocha -> {
      val scheme = rememberDynamicColorScheme(
        primary = Color(0xFFCBA6F7),
        secondary = Color(0xFF89B4FA),
        neutral = Color(0xFF1E1E2E),
        neutralVariant = Color(0xFF313244),
        isDark = true,
        style = PaletteStyle.Expressive,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        modifyColorScheme = {
          it.copy(
            primary = Color(0xFFCBA6F7),
            onPrimary = Color(0xFF11111B),
            secondary = Color(0xFF89B4FA),
            onSecondary = Color(0xFF11111B),
            background = Color(0xFF11111B),
            surface = Color(0xFF181825),
            surfaceDim = Color(0xFF11111B),
            surfaceBright = Color(0xFF313244),
            surfaceContainerLowest = Color(0xFF11111B),
            surfaceContainerLow = Color(0xFF181825),
            surfaceContainer = Color(0xFF1E1E2E),
            surfaceContainerHigh = Color(0xFF313244),
            surfaceContainerHighest = Color(0xFF45475A),
            onBackground = Color(0xFFCDD6F4),
            onSurface = Color(0xFFCDD6F4),
            onSurfaceVariant = Color(0xFFA6ADC8),
          )
        },
      )
      MaterialExpressiveTheme(colorScheme = scheme) {
        themedContent()
      }
    }
    ThemeMode.DarkGray, ThemeMode.ClassicYouTube -> {
      val scheme = rememberDynamicColorScheme(
        primary = Color(0xFFE2E2E2),
        secondary = Color(0xFFB0B0B0),
        neutral = Color(0xFF0F0F0F),
        neutralVariant = Color(0xFF282828),
        isDark = true,
        style = PaletteStyle.TonalSpot,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        modifyColorScheme = {
          it.copy(
            primary = Color(0xFFE2E2E2),
            onPrimary = Color(0xFF1A1A1A),
            primaryContainer = Color(0xFF383838),
            onPrimaryContainer = Color(0xFFF1F1F1),
            secondary = Color(0xFFB0B0B0),
            onSecondary = Color(0xFF1E1E1E),
            background = Color(0xFF212121),
            surface = Color(0xFF212121),
            surfaceDim = Color(0xFF191919),
            surfaceBright = Color(0xFF383838),
            surfaceContainerLowest = Color(0xFF191919),
            surfaceContainerLow = Color(0xFF1C1C1C),
            surfaceContainer = Color(0xFF262626),
            surfaceContainerHigh = Color(0xFF303030),
            surfaceContainerHighest = Color(0xFF3B3B3B),
            onBackground = Color(0xFFF1F1F1),
            onSurface = Color(0xFFF1F1F1),
            onSurfaceVariant = Color(0xFFB0B0B0),
          )
        },
      )
      MaterialExpressiveTheme(colorScheme = scheme) {
        themedContent()
      }
    }
    ThemeMode.DarkPink -> {
      val scheme = rememberPresetColorScheme(
        primary = Color(0xFFF48FB1),
        onPrimary = Color(0xFF4A0027),
        bgColor = Color(0xFF381537),
        surfaceColor = Color(0xFF381537),
        containerColor = Color(0xFF4A1E49),
        containerHighColor = Color(0xFF5A2559),
        onBgColor = Color(0xFFFDF0F5),
        onSurfaceVarColor = Color(0xFFE2BFD2),
      )
      MaterialExpressiveTheme(colorScheme = scheme) {
        themedContent()
      }
    }
    ThemeMode.DarkBlue -> {
      val scheme = rememberPresetColorScheme(
        primary = Color(0xFF82B1FF),
        onPrimary = Color(0xFF002766),
        bgColor = Color(0xFF0A224A),
        surfaceColor = Color(0xFF0A224A),
        containerColor = Color(0xFF113061),
        containerHighColor = Color(0xFF193E7A),
        onBgColor = Color(0xFFF0F4FF),
        onSurfaceVarColor = Color(0xFFB4C8E8),
      )
      MaterialExpressiveTheme(colorScheme = scheme) {
        themedContent()
      }
    }
    ThemeMode.DarkGreen -> {
      val scheme = rememberPresetColorScheme(
        primary = Color(0xFF81C784),
        onPrimary = Color(0xFF00390F),
        bgColor = Color(0xFF0B3B14),
        surfaceColor = Color(0xFF0B3B14),
        containerColor = Color(0xFF134E1D),
        containerHighColor = Color(0xFF1B6127),
        onBgColor = Color(0xFFF0F9F1),
        onSurfaceVarColor = Color(0xFFB5D8B8),
      )
      MaterialExpressiveTheme(colorScheme = scheme) {
        themedContent()
      }
    }
    ThemeMode.DarkYellow -> {
      val scheme = rememberPresetColorScheme(
        primary = Color(0xFFFFD54F),
        onPrimary = Color(0xFF3B2F00),
        bgColor = Color(0xFF453C05),
        surfaceColor = Color(0xFF453C05),
        containerColor = Color(0xFF574C0A),
        containerHighColor = Color(0xFF6B5E10),
        onBgColor = Color(0xFFFFFDF0),
        onSurfaceVarColor = Color(0xFFDDD5A8),
      )
      MaterialExpressiveTheme(colorScheme = scheme) {
        themedContent()
      }
    }
    ThemeMode.DarkOrange -> {
      val scheme = rememberPresetColorScheme(
        primary = Color(0xFFFFAB40),
        onPrimary = Color(0xFF421900),
        bgColor = Color(0xFF4A2305),
        surfaceColor = Color(0xFF4A2305),
        containerColor = Color(0xFF5E2E0A),
        containerHighColor = Color(0xFF73390F),
        onBgColor = Color(0xFFFFF6F0),
        onSurfaceVarColor = Color(0xFFE5C4AC),
      )
      MaterialExpressiveTheme(colorScheme = scheme) {
        themedContent()
      }
    }
    ThemeMode.DarkRed -> {
      val scheme = rememberPresetColorScheme(
        primary = Color(0xFFFF5252),
        onPrimary = Color(0xFF4A0000),
        bgColor = Color(0xFF4D0707),
        surfaceColor = Color(0xFF4D0707),
        containerColor = Color(0xFF610E0E),
        containerHighColor = Color(0xFF751515),
        onBgColor = Color(0xFFFFF0F0),
        onSurfaceVarColor = Color(0xFFE2B7B7),
      )
      MaterialExpressiveTheme(colorScheme = scheme) {
        themedContent()
      }
    }
    ThemeMode.Custom -> {
      val baseColor = Color(themeColor.parseColor())
      val lum = 0.299f * baseColor.red + 0.587f * baseColor.green + 0.114f * baseColor.blue
      val isCustomDark = lum < 0.45f

      val hsv = FloatArray(3)
      android.graphics.Color.colorToHSV(baseColor.toArgb(), hsv)
      val effectiveHue = if (hsv[1] >= 0.12f) hsv[0] else themeColor.hue.toFloat()

      val primary = if (isCustomDark) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(effectiveHue, 0.65f, 0.95f)))
      } else {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(effectiveHue, 0.80f, 0.45f)))
      }

      val containerLow: Color
      val container: Color
      val containerHigh: Color
      val containerHighest: Color

      if (isCustomDark) {
        containerLow = Color.White.copy(alpha = 0.06f).compositeOver(baseColor)
        container = Color.White.copy(alpha = 0.10f).compositeOver(baseColor)
        containerHigh = Color.White.copy(alpha = 0.14f).compositeOver(baseColor)
        containerHighest = Color.White.copy(alpha = 0.18f).compositeOver(baseColor)
      } else {
        containerLow = Color.White.copy(alpha = 0.40f).compositeOver(baseColor)
        container = Color.White.copy(alpha = 0.65f).compositeOver(baseColor)
        containerHigh = Color.Black.copy(alpha = 0.06f).compositeOver(baseColor)
        containerHighest = Color.Black.copy(alpha = 0.10f).compositeOver(baseColor)
      }

      val onBg = if (isCustomDark) Color(0xFFF1F1F1) else Color(0xFF1C1B1F)
      val onSurface = if (isCustomDark) Color(0xFFF1F1F1) else Color(0xFF1C1B1F)
      val onSurfaceVar = if (isCustomDark) Color(0xFFB0B0B0) else Color(0xFF49454F)
      val onPri = if (isCustomDark) Color(0xFF1A1A1A) else Color.White

      val scheme = rememberDynamicColorScheme(
        primary = primary,
        neutral = baseColor,
        neutralVariant = container,
        isDark = isCustomDark,
        style = PaletteStyle.TonalSpot,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        modifyColorScheme = {
          it.copy(
            primary = primary,
            onPrimary = onPri,
            primaryContainer = container,
            onPrimaryContainer = onBg,
            background = baseColor,
            surface = baseColor,
            surfaceDim = baseColor,
            surfaceBright = containerHigh,
            surfaceContainerLowest = baseColor,
            surfaceContainerLow = containerLow,
            surfaceContainer = container,
            surfaceContainerHigh = containerHigh,
            surfaceContainerHighest = containerHighest,
            onBackground = onBg,
            onSurface = onSurface,
            onSurfaceVariant = onSurfaceVar,
          )
        },
      )
      MaterialExpressiveTheme(colorScheme = scheme) {
        themedContent()
      }
    }
    ThemeMode.FollowSystem, ThemeMode.Light, ThemeMode.Dark -> {
      DynamicMaterialExpressiveTheme(
        primary = VoiceBlue,
        secondary = Color(0xFF5E6F95),
        isDark = darkTheme,
        style = PaletteStyle.Expressive,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
      ) {
        themedContent()
      }
    }
  }
}

@Composable
private fun rememberPresetColorScheme(
  primary: Color,
  onPrimary: Color,
  bgColor: Color,
  surfaceColor: Color,
  containerColor: Color,
  containerHighColor: Color,
  onBgColor: Color = Color(0xFFF8F8F8),
  onSurfaceVarColor: Color,
): ColorScheme = rememberDynamicColorScheme(
  primary = primary,
  neutral = bgColor,
  neutralVariant = containerColor,
  isDark = true,
  style = PaletteStyle.TonalSpot,
  specVersion = ColorSpec.SpecVersion.SPEC_2025,
  modifyColorScheme = {
    it.copy(
      primary = primary,
      onPrimary = onPrimary,
      primaryContainer = containerColor,
      onPrimaryContainer = onBgColor,
      background = bgColor,
      surface = surfaceColor,
      surfaceDim = bgColor,
      surfaceBright = containerHighColor,
      surfaceContainerLowest = bgColor,
      surfaceContainerLow = surfaceColor,
      surfaceContainer = containerColor,
      surfaceContainerHigh = containerHighColor,
      surfaceContainerHighest = containerHighColor,
      onBackground = onBgColor,
      onSurface = onBgColor,
      onSurfaceVariant = onSurfaceVarColor,
    )
  },
)

@RequiresApi(31)
@Composable
private fun systemDynamicColorScheme(darkTheme: Boolean): ColorScheme {
  return if (darkTheme) {
    dynamicDarkColorScheme(LocalContext.current)
  } else {
    dynamicLightColorScheme(LocalContext.current)
  }
}

