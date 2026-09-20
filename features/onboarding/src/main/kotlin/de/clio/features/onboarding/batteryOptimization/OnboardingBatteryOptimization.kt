package de.clio.features.onboarding.batteryOptimization

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.clio.core.common.rootGraphAs
import de.clio.core.ui.ClioTheme
import de.clio.core.ui.icons.ClioIcons
import de.clio.features.onboarding.R
import de.clio.core.strings.R as StringsR

@Composable
fun OnboardingBatteryOptimization(modifier: Modifier = Modifier) {
  val viewModel = retain<OnboardingBatteryOptimizationViewModel> {
    rootGraphAs<OnboardingBatteryOptimizationProvider>()
      .onboardingBatteryOptimizationViewModel
  }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner, viewModel) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        viewModel.refreshStatus()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  val isIgnoringBatteryOptimizations by viewModel.isIgnoringBatteryOptimizationsState.collectAsState()

  OnboardingBatteryOptimization(
    isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
    onRequestPermission = viewModel::requestIgnoreBatteryOptimization,
    onNext = viewModel::next,
    onBack = viewModel::back,
    modifier = modifier,
  )
}

@Composable
internal fun OnboardingBatteryOptimization(
  isIgnoringBatteryOptimizations: Boolean,
  onRequestPermission: () -> Unit,
  onNext: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = ClioIcons.ArrowBack,
              contentDescription = stringResource(id = StringsR.string.common_action_close),
            )
          }
        },
      )
    },
    floatingActionButtonPosition = if (isIgnoringBatteryOptimizations) FabPosition.End else FabPosition.Center,
    floatingActionButton = {
      if (isIgnoringBatteryOptimizations) {
        ExtendedFloatingActionButton(onClick = onNext) {
          Text(text = stringResource(StringsR.string.onboarding_action_next))
        }
      } else {
        Column(
          modifier = Modifier
            .sizeIn(maxWidth = 360.dp)
            .padding(horizontal = 24.dp, vertical = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          ExtendedFloatingActionButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onRequestPermission,
          ) {
            Text(text = stringResource(StringsR.string.onboarding_battery_optimization_action_grant))
          }

          OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNext,
          ) {
            Text(text = stringResource(StringsR.string.onboarding_battery_optimization_action_skip))
          }
        }
      }
    },
    content = { contentPadding ->
      Column(
        modifier = Modifier
          .padding(contentPadding)
          .fillMaxSize(),
      ) {
        if (shouldShowImage()) {
          Image(
            modifier = Modifier
              .weight(1F)
              .padding(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
              .heightIn(max = 400.dp)
              .aspectRatio(1F)
              .fillMaxSize()
              .align(Alignment.CenterHorizontally)
              .clip(CircleShape),
            painter = painterResource(id = R.drawable.battery_optimization_artwork),
            contentDescription = null,
          )
        }

        Column(Modifier.weight(2F)) {
          Spacer(modifier = Modifier.size(16.dp))
          Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = stringResource(StringsR.string.onboarding_battery_optimization_title),
            style = MaterialTheme.typography.displayMedium,
          )
          Spacer(modifier = Modifier.size(8.dp))
          Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = stringResource(StringsR.string.onboarding_battery_optimization_subtitle),
            style = MaterialTheme.typography.bodyLarge,
          )
          if (isIgnoringBatteryOptimizations) {
            Spacer(modifier = Modifier.size(16.dp))
            Row(
              modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = ClioIcons.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
              Text(
                text = stringResource(StringsR.string.onboarding_battery_optimization_status_already_unrestricted),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }
    },
  )
}

@Composable
private fun shouldShowImage(): Boolean {
  val localWindowInfo = LocalWindowInfo.current
  val thresholdPx = with(LocalDensity.current) { 440.dp.toPx() }
  return localWindowInfo.containerSize.height > thresholdPx
}

@Composable
@Preview
private fun OnboardingBatteryOptimizationPreview() {
  ClioTheme {
    OnboardingBatteryOptimization(
      isIgnoringBatteryOptimizations = false,
      onRequestPermission = {},
      onNext = {},
      onBack = {},
    )
  }
}
