package de.clio.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import de.clio.app.navigation.BottomSheetSceneStrategy
import de.clio.app.navigation.NavEntryResolver
import de.clio.app.navigation.StartDestinationProvider
import de.clio.core.analytics.api.Analytics
import de.clio.core.common.rootGraphAs
import de.clio.core.data.ThemeColor
import de.clio.core.data.ThemeMode
import de.clio.core.data.store.ThemeColorStore
import de.clio.core.data.store.ThemeModeStore
import de.clio.core.logging.api.Logger
import de.clio.core.ui.ClioTheme
import de.clio.core.ui.LocalAppReady
import de.clio.core.ui.LocalSharedTransitionScope
import de.clio.features.review.ReviewFeature
import de.clio.navigation.Destination
import de.clio.navigation.NavigationCommand
import de.clio.navigation.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@ContributesTo(AppScope::class)
interface MainActivityGraph {
  fun inject(activity: MainActivity)
}

class MainActivity : AppCompatActivity() {

  @Inject
  private lateinit var navigator: Navigator

  @Inject
  lateinit var navEntryResolver: NavEntryResolver

  @Inject
  private lateinit var startDestinationProvider: StartDestinationProvider

  @Inject
  private lateinit var analytics: Analytics

  @Inject
  @ThemeModeStore
  private lateinit var themeModeStore: DataStore<ThemeMode>

  @Inject
  @ThemeColorStore
  private lateinit var themeColorStore: DataStore<ThemeColor>

  private var isAppReady = false

  @OptIn(ExperimentalSharedTransitionApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    rootGraphAs<MainActivityGraph>().inject(this)
    super.onCreate(savedInstanceState)

    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
    )
    if (android.os.Build.VERSION.SDK_INT >= 29) {
      window.isNavigationBarContrastEnforced = false
    }

    val content: android.view.View = findViewById(android.R.id.content)
    val splashStartTime = android.os.SystemClock.uptimeMillis()
    val minSplashDuration = 100L
    val maxSplashDuration = 1500L
    content.viewTreeObserver.addOnPreDrawListener(
      object : android.view.ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
          val elapsed = android.os.SystemClock.uptimeMillis() - splashStartTime
          return if ((isAppReady && elapsed >= minSplashDuration) || elapsed >= maxSplashDuration) {
            content.viewTreeObserver.removeOnPreDrawListener(this)
            true
          } else {
            false
          }
        }
      },
    )

    val (initialThemeMode, initialThemeColor) = runBlocking {
      val modeDeferred = async(Dispatchers.IO) { themeModeStore.data.first() }
      val colorDeferred = async(Dispatchers.IO) { themeColorStore.data.first() }
      modeDeferred.await() to colorDeferred.await()
    }

    setContent {
      @Suppress("UNCHECKED_CAST")
      val backStack = rememberNavBackStack(*startDestinationProvider(intent).toTypedArray()) as MutableList<Destination.Compose>
      LaunchedEffect(backStack.last()) {
        analytics.screenView(backStack.last().trackingName)
      }
      val themeMode = themeModeStore.data.collectAsState(initial = initialThemeMode).value
      val themeColor = themeColorStore.data.collectAsState(initial = initialThemeColor).value
      val isCustomDark = remember(themeColor) {
        val baseColor = Color(themeColor.parseColor())
        (0.299f * baseColor.red + 0.587f * baseColor.green + 0.114f * baseColor.blue) < 0.45f
      }
      val isDarkTheme = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.FollowSystem, ThemeMode.Dynamic -> isSystemInDarkTheme()
        ThemeMode.Custom -> isCustomDark
        else -> true
      }
      val currentNavColor = when (themeMode) {
        ThemeMode.Light -> android.graphics.Color.WHITE
        ThemeMode.Dark -> 0xFF181C24.toInt()
        ThemeMode.Amoled -> android.graphics.Color.BLACK
        ThemeMode.CatppuccinMocha -> 0xFF11111B.toInt()
        ThemeMode.DarkGray, ThemeMode.ClassicYouTube -> 0xFF212121.toInt()
        ThemeMode.DarkPink -> 0xFF381537.toInt()
        ThemeMode.DarkBlue -> 0xFF0A224A.toInt()
        ThemeMode.DarkGreen -> 0xFF0B3B14.toInt()
        ThemeMode.DarkYellow -> 0xFF453C05.toInt()
        ThemeMode.DarkOrange -> 0xFF4A2305.toInt()
        ThemeMode.DarkRed -> 0xFF4D0707.toInt()
        ThemeMode.Custom -> themeColor.parseColor().toInt()
        ThemeMode.Dynamic, ThemeMode.FollowSystem -> android.graphics.Color.TRANSPARENT
      }

      DisposableEffect(themeMode, themeColor, isDarkTheme) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme
        enableEdgeToEdge(
          statusBarStyle = if (isDarkTheme) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
          } else {
            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
          },
          navigationBarStyle = if (isDarkTheme) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
          } else {
            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
          },
        )
        if (android.os.Build.VERSION.SDK_INT >= 29) {
          window.isNavigationBarContrastEnforced = false
        }
        if (android.os.Build.VERSION.SDK_INT < 35 && currentNavColor != android.graphics.Color.TRANSPARENT) {
          @Suppress("DEPRECATION")
          window.navigationBarColor = currentNavColor
        }
        onDispose {}
      }

      ClioTheme(
        themeMode = themeMode,
        themeColor = themeColor,
      ) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background,
        ) {
          val bottomSheetStrategy = remember { BottomSheetSceneStrategy<Destination.Compose>() }
          val dialogStrategy = remember { DialogSceneStrategy<Destination.Compose>() }
          val density = LocalDensity.current

          SharedTransitionLayout {
            CompositionLocalProvider(
              LocalSharedTransitionScope provides this,
              LocalAppReady provides { isAppReady = true },
            ) {
              NavDisplay(
                backStack = backStack,
                sceneStrategies = listOf(bottomSheetStrategy, dialogStrategy),
                sharedTransitionScope = this,
                transitionSpec = {
                  if (isBookOverviewPlaybackTransition(initialState.destination(), targetState.destination())) {
                    SharedZAxisEnterTransition togetherWith SharedZAxisExitTransition
                  } else {
                    CrossfadeEnterTransition togetherWith CrossfadeExitTransition
                  }
                },
                popTransitionSpec = {
                  if (isBookOverviewPlaybackTransition(initialState.destination(), targetState.destination())) {
                    SharedZAxisEnterTransition togetherWith SharedZAxisExitTransition
                  } else {
                    CrossfadeEnterTransition togetherWith CrossfadeExitTransition
                  }
                },
                predictivePopTransitionSpec = {
                  if (isBookOverviewPlaybackTransition(initialState.destination(), targetState.destination())) {
                    SharedZAxisEnterTransition togetherWith SharedZAxisExitTransition
                  } else {
                    CrossfadeEnterTransition togetherWith CrossfadeExitTransition
                  }
                },
                onBack = {
                  if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                  }
                },
                entryProvider = { key ->
                  navEntryResolver.create(key)
                },
              )
            }
          }

          LaunchedEffect(navigator) {
            navigator.navigationCommands.collect { command ->
              when (command) {
                is NavigationCommand.GoTo -> {
                  when (val destination = command.destination) {
                    is Destination.Compose -> {
                      backStack += destination
                    }
                    is Destination.Activity -> {
                      startActivity(destination.intent)
                    }
                    Destination.BatteryOptimization -> {
                      toBatteryOptimizations()
                    }
                    is Destination.Website -> {
                      try {
                        startActivity(Intent(Intent.ACTION_VIEW, destination.url.toUri()))
                      } catch (exception: ActivityNotFoundException) {
                        Logger.w(exception)
                      }
                    }
                  }
                }
                NavigationCommand.GoBack -> {
                  if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                  }
                }
                is NavigationCommand.SetRoot -> {
                  backStack.clear()
                  backStack.add(command.root)
                }
              }
            }
          }

          ReviewFeature()
        }
      }
    }
  }

  private fun toBatteryOptimizations() {
    val intent = Intent()
      .apply {
        @Suppress("BatteryLife")
        action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        data = "package:$packageName".toUri()
      }
    try {
      startActivity(intent)
    } catch (e: ActivityNotFoundException) {
      Logger.w(e, "Can't request ignoring battery optimizations")
    }
  }

  companion object {

    const val NI_GO_TO_BOOK = "niGotoBook"

    fun goToBookIntent(context: Context) = Intent(context, MainActivity::class.java).apply {
      putExtra(NI_GO_TO_BOOK, true)
      flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
    }
  }
}
