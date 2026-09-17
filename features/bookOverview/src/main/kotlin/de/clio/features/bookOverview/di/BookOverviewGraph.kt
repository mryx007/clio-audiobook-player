package de.clio.features.bookOverview.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import de.clio.features.bookOverview.bottomSheet.BottomSheetViewModel
import de.clio.features.bookOverview.deleteBook.DeleteBookViewModel
import de.clio.features.bookOverview.editTitle.EditBookTitleViewModel
import de.clio.features.bookOverview.fileCover.FileCoverViewModel
import de.clio.features.bookOverview.overview.BookOverviewViewModel

abstract class BookOverviewScope private constructor()

@GraphExtension(scope = BookOverviewScope::class)
interface BookOverviewGraph {
  val bookOverviewViewModel: BookOverviewViewModel
  val editBookTitleViewModel: EditBookTitleViewModel
  val bottomSheetViewModel: BottomSheetViewModel
  val deleteBookViewModel: DeleteBookViewModel
  val fileCoverViewModel: FileCoverViewModel

  @GraphExtension.Factory
  @ContributesTo(AppScope::class)
  interface Factory {
    fun create(): BookOverviewGraph

    @ContributesTo(AppScope::class)
    interface Provider {
      val bookOverviewGraphProviderFactory: Factory
    }
  }
}
