package voice.features.playbackScreen.view

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import voice.core.strings.R
import voice.core.ui.icons.VoiceIcons

@Composable
internal fun CloseIcon(
  onCloseClick: () -> Unit,
  tint: Color = MaterialTheme.colorScheme.onSurface,
) {
  IconButton(onClick = onCloseClick) {
    Icon(
      imageVector = VoiceIcons.ArrowBack,
      tint = tint,
      contentDescription = stringResource(id = R.string.common_action_close),
    )
  }
}
