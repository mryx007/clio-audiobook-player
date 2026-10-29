package de.clio.features.settings.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import de.clio.core.ui.icons.ClioIcons
import de.clio.core.strings.R as StringsR

@Composable
internal fun CompletionThresholdRow(
  completionThresholdInSeconds: Int,
  onClick: () -> Unit,
) {
  ListItem(
    modifier = Modifier
      .clickable { onClick() }
      .fillMaxWidth(),
    leadingContent = {
      Icon(
        imageVector = ClioIcons.Check,
        contentDescription = stringResource(StringsR.string.settings_playback_completion_threshold_title),
      )
    },
    supportingContent = {
      Text(
        text = LocalResources.current.getQuantityString(
          StringsR.plurals.duration_seconds,
          completionThresholdInSeconds,
          completionThresholdInSeconds,
        ),
      )
    },
  ) {
    Text(text = stringResource(StringsR.string.settings_playback_completion_threshold_title))
  }
}

@Composable
internal fun CompletionThresholdDialog(
  currentSeconds: Int,
  onSecondsConfirm: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  TimeSettingDialog(
    title = stringResource(StringsR.string.settings_playback_completion_threshold_title),
    currentSeconds = currentSeconds,
    minSeconds = 0,
    maxSeconds = 120,
    textPluralRes = StringsR.plurals.duration_seconds,
    onSecondsConfirm = onSecondsConfirm,
    onDismiss = onDismiss,
  )
}
