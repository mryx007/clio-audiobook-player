package voice.features.review

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import voice.core.strings.R as StringsR
import voice.core.ui.icons.VoiceIcons

@Composable
internal fun AskForReviewDialog(
  onReview: (Int) -> Unit,
  onReviewDeny: () -> Unit,
  onDismiss: () -> Unit,
) {
  var selectedStars by remember { mutableIntStateOf(5) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(stringResource(StringsR.string.review_request_title))
    },
    text = {
      Column {
        Text(stringResource(StringsR.string.review_request_message))
        Spacer(Modifier.size(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
        ) {
          val activeColor = MaterialTheme.colorScheme.primary
          val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)

          repeat(5) { index ->
            val star = index + 1
            val selected = star <= selectedStars
            val animatedColor by animateColorAsState(
              targetValue = if (selected) activeColor else inactiveColor,
              label = "star_color",
            )
            val animatedScale by animateFloatAsState(
              targetValue = if (selected) 1f else 0.85f,
              animationSpec = spring(dampingRatio = 0.6f),
              label = "star_scale",
            )
            Icon(
              imageVector = VoiceIcons.Star,
              contentDescription = "$star",
              tint = animatedColor,
              modifier = Modifier
                .padding(4.dp)
                .scale(animatedScale)
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = ripple(bounded = false, radius = 24.dp),
                ) {
                  selectedStars = star
                }
                .size(40.dp),
            )
          }
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onReviewDeny) {
        Text(stringResource(StringsR.string.review_request_action_dismiss))
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onReview(selectedStars)
        },
      ) {
        Text(stringResource(StringsR.string.review_request_action_rate))
      }
    },
  )
}

@Composable
@Preview
private fun AskForReviewDialogPreview() {
  AskForReviewDialog(
    onReview = {},
    onReviewDeny = {},
    onDismiss = {},
  )
}
