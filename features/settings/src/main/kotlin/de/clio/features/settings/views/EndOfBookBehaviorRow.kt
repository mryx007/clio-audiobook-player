package de.clio.features.settings.views

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import de.clio.core.data.EndOfBookBehavior
import de.clio.core.ui.icons.ClioIcons
import de.clio.core.strings.R as StringsR

@Composable
internal fun EndOfBookBehaviorRow(
  behavior: EndOfBookBehavior,
  onClick: () -> Unit,
) {
  SelectionRow(
    title = stringResource(StringsR.string.settings_playback_end_of_book_behavior_title),
    value = behavior.label(),
    leadingIcon = ClioIcons.Done,
    onClick = onClick,
  )
}

@Composable
internal fun EndOfBookBehaviorDialog(
  selectedBehavior: EndOfBookBehavior,
  onBehaviorSelect: (EndOfBookBehavior) -> Unit,
  onDismiss: () -> Unit,
) {
  var temporarySelection by remember(selectedBehavior) {
    mutableStateOf(selectedBehavior)
  }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(stringResource(StringsR.string.settings_playback_end_of_book_behavior_title))
    },
    text = {
      Column {
        EndOfBookBehavior.entries.forEach { behavior ->
          SelectionDialogItem(
            text = behavior.label(),
            selected = behavior == temporarySelection,
            onClick = {
              temporarySelection = behavior
            },
          )
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onBehaviorSelect(temporarySelection)
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
internal fun EndOfBookBehavior.label(): String {
  return when (this) {
    EndOfBookBehavior.DoNothing -> stringResource(StringsR.string.settings_playback_end_of_book_behavior_do_nothing)
    EndOfBookBehavior.BookOverview -> stringResource(StringsR.string.settings_playback_end_of_book_behavior_book_overview)
  }
}
