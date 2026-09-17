package de.clio.features.bookOverview.editTitle

import de.clio.core.data.BookId

internal data class EditBookTitleState(
  val title: String,
  val bookId: BookId,
) {

  val confirmButtonEnabled: Boolean = title.trim().isNotEmpty()
}
