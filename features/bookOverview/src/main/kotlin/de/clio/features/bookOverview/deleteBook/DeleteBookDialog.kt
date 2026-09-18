package de.clio.features.bookOverview.deleteBook

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.clio.core.strings.R as StringsR

@Composable
internal fun DeleteBookDialog(
  viewState: DeleteBookViewState,
  onDismiss: () -> Unit,
  onConfirmDeletion: () -> Unit,
) {
  val isMultiple = viewState.count > 1
  val title = if (isMultiple) {
    stringResource(StringsR.string.book_bulk_delete_dialog_title)
  } else {
    stringResource(StringsR.string.book_delete_dialog_title)
  }
  val message = if (isMultiple) {
    stringResource(StringsR.string.book_bulk_delete_dialog_message, viewState.count)
  } else {
    stringResource(StringsR.string.book_delete_dialog_message)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(title)
    },
    confirmButton = {
      Button(
        onClick = onConfirmDeletion,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.errorContainer,
          contentColor = MaterialTheme.colorScheme.error,
        ),
      ) {
        Text(stringResource(id = StringsR.string.common_action_delete))
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
      ) {
        Text(stringResource(id = StringsR.string.common_dialog_cancel))
      }
    },
    text = {
      Column {
        Text(message)

        Spacer(modifier = Modifier.heightIn(8.dp))
        Text(viewState.fileToDelete, style = MaterialTheme.typography.bodyLarge)
      }
    },
  )
}
