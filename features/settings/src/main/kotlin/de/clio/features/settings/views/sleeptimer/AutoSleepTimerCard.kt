package de.clio.features.settings.views.sleeptimer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.clio.core.ui.ClioTheme
import de.clio.core.ui.icons.ClioIcons
import de.clio.features.settings.SettingsListener
import de.clio.features.settings.SettingsViewState
import de.clio.core.strings.R as StringsR

@Composable
internal fun AutoSleepTimerCard(
  viewState: SettingsViewState.AutoSleepTimerViewState,
  listener: SettingsListener,
) {
  val localTimeFormatter = rememberLocalTimeFormatter()
  ListItem(
    modifier = Modifier.clickable { listener.setAutoSleepTimer(!viewState.enabled) },
    leadingContent = {
      Icon(
        imageVector = ClioIcons.Bedtime,
        contentDescription = null,
      )
    },
    trailingContent = {
      Switch(
        checked = viewState.enabled,
        onCheckedChange = listener::setAutoSleepTimer,
      )
    },
    supportingContent = {
      Column {
        Text(
          text = stringResource(
            id = StringsR.string.settings_auto_sleep_timer_summary,
            localTimeFormatter.format(viewState.startTime),
            localTimeFormatter.format(viewState.endTime),
          ),
        )
        Row(
          modifier = Modifier.padding(top = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          AutoSleepTimerSetting(
            time = viewState.startTime,
            label = stringResource(StringsR.string.settings_auto_sleep_timer_start_label),
            enabled = viewState.enabled,
            setAutoSleepTime = listener::setAutoSleepTimerStart,
          )
          AutoSleepTimerSetting(
            time = viewState.endTime,
            label = stringResource(StringsR.string.settings_auto_sleep_timer_end_label),
            enabled = viewState.enabled,
            setAutoSleepTime = listener::setAutoSleepTimerEnd,
          )
        }
      }
    },
  ) {
    Text(stringResource(StringsR.string.settings_auto_sleep_timer_title))
  }
}

@Composable
@Preview
private fun AutoSleepTimerCardPreview() {
  ClioTheme {
    AutoSleepTimerCard(SettingsViewState.AutoSleepTimerViewState.preview(), SettingsListener.noop())
  }
}
