package voice.features.settings.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import voice.core.strings.R as StringsR

@Composable
internal fun SeekTimeRow(
  title: String,
  icon: ImageVector,
  seconds: Int,
  onClick: () -> Unit,
) {
  ListItem(
    modifier = Modifier
      .clickable {
        onClick()
      }
      .fillMaxWidth(),
    leadingContent = {
      Icon(
        imageVector = icon,
        contentDescription = title,
      )
    },
    supportingContent = {
      Text(
        text = LocalResources.current.getQuantityString(
          StringsR.plurals.duration_seconds,
          seconds,
          seconds,
        ),
      )
    },
  ) {
    Text(text = title)
  }
}

@Composable
internal fun SeekAmountDialog(
  title: String,
  currentSeconds: Int,
  onSecondsConfirm: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  TimeSettingDialog(
    title = title,
    currentSeconds = currentSeconds,
    minSeconds = 3,
    maxSeconds = 60,
    textPluralRes = StringsR.plurals.duration_seconds,
    onSecondsConfirm = onSecondsConfirm,
    onDismiss = onDismiss,
  )
}
