package de.clio.features.bookOverview.bottomSheet

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import de.clio.core.ui.icons.ClioIcons
import de.clio.core.strings.R as StringsR

internal data class EditBookBottomSheetState(val items: List<BottomSheetItem>)

enum class BottomSheetItem(
  @StringRes val titleRes: Int,
  val icon: ImageVector,
) {
  Title(StringsR.string.book_edit_name_label, ClioIcons.Title),
  InternetCover(StringsR.string.book_edit_cover_internet, ClioIcons.Download),
  FileCover(StringsR.string.book_edit_cover_file, ClioIcons.Image),
  DeleteBook(StringsR.string.book_delete_bottom_sheet_title, ClioIcons.Delete),
  BookCategoryMarkAsNotStarted(StringsR.string.book_category_action_mark_not_started, ClioIcons.HourglassEmpty),
  BookCategoryMarkAsCurrent(StringsR.string.book_category_action_mark_current, ClioIcons.NotStarted),
  BookCategoryMarkAsCompleted(StringsR.string.book_category_action_mark_completed, ClioIcons.Done),
}
