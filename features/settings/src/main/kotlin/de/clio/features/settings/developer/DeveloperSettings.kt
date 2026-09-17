package de.clio.features.settings.developer

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import de.clio.core.common.rootGraphAs
import de.clio.core.ui.icons.ClioIcons
import de.clio.navigation.Destination
import de.clio.navigation.NavEntryProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.launch
import de.clio.core.strings.R as StringsR

@Composable
private fun DeveloperSettings(
  viewState: DeveloperSettingsViewState,
  viewModel: DeveloperSettingsViewModel,
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
          Text("Developer Menu")
        },
        navigationIcon = {
          IconButton(onClick = viewModel::close) {
            Icon(
              imageVector = ClioIcons.Close,
              contentDescription = stringResource(StringsR.string.common_action_close),
            )
          }
        },
      )
    },
    bottomBar = {
      Spacer(
        Modifier
          .fillMaxWidth()
          .windowInsetsBottomHeight(WindowInsets.navigationBars)
          .background(MaterialTheme.colorScheme.surface),
      )
    },
  ) { contentPadding ->
    LazyColumn(contentPadding = contentPadding) {
      item {
        ListItem(
          trailingContent = {
            TextButton(
              onClick = {
                viewModel.refreshRemoteConfig()
              },
              content = {
                Text("Refresh")
              },
            )
          },
        ) {
          Text("Refresh FCM")
        }
      }

      val fcmToken = viewState.fcmToken
      if (fcmToken != null) {
        item {
          val clipboard = LocalClipboard.current
          val scope = rememberCoroutineScope()
          ListItem(
            trailingContent = {
              TextButton(
                onClick = {
                  scope.launch {
                    clipboard.setClipEntry(
                      ClipEntry(ClipData.newPlainText("FCM Token", fcmToken)),
                    )
                  }
                },
                content = {
                  Text("Copy to Clipboard")
                },
              )
            },
          ) {
            Text("FCM Token")
          }
        }
      }

      items(viewState.featureFlags, key = { it.key }) { viewState ->
        when (viewState) {
          is DeveloperSettingsViewState.FeatureFlagViewState.BooleanFlag -> {
            BooleanFeatureFlagRow(
              viewState = viewState,
              clearOverride = {
                viewModel.clearOverride(viewState.key)
              },
              setOverride = { viewModel.setBooleanOverride(viewState.key, it) },
            )
          }
          is DeveloperSettingsViewState.FeatureFlagViewState.StringFlag -> {
            StringFeatureFlagRow(
              viewState = viewState,
              clearOverride = {
                viewModel.clearOverride(viewState.key)
              },
              setOverride = { viewModel.setStringOverride(viewState.key, it) },
            )
          }
        }
      }
    }
  }
}

@ContributesTo(AppScope::class)
interface DeveloperSettingsGraph {
  val developerSettingsViewModel: DeveloperSettingsViewModel
}

@ContributesTo(AppScope::class)
interface DeveloperSettingsProvider {

  @Provides
  @IntoSet
  fun developerSettingsNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.DeveloperSettings> { key ->
    NavEntry(key) {
      DeveloperSettings()
    }
  }
}

@Composable
fun DeveloperSettings() {
  val viewModel = retain<DeveloperSettingsViewModel> { rootGraphAs<DeveloperSettingsGraph>().developerSettingsViewModel }
  val viewState = viewModel.viewState()
  DeveloperSettings(viewState, viewModel)
}
