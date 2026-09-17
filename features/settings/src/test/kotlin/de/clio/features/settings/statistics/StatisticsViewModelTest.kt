package de.clio.features.settings.statistics

import android.content.Context
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import de.clio.core.common.DispatcherProvider
import de.clio.core.data.BookStatistic
import de.clio.core.data.ImportResult
import de.clio.core.data.MonthlyStatistic
import de.clio.core.data.StatisticsSummary
import de.clio.core.data.repo.ListeningStatisticRepository
import de.clio.navigation.Navigator
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StatisticsViewModelTest {

  private val scope = TestScope()
  private val navigator = mockk<Navigator> {
    every { goBack() } just Runs
  }
  private val context = mockk<Context>(relaxed = true)

  private val summaryFlow = MutableStateFlow(
    StatisticsSummary(
      totalSeconds = 7200L,
      thisMonthSeconds = 3600L,
      booksCount = 2,
      monthlyStats = listOf(
        MonthlyStatistic("2024-10", 7200L),
      ),
      bookStats = listOf(
        BookStatistic("Der Schwarm", null, 5000L, coverUrl = "file:///cover.png"),
        BookStatistic("Meteor", null, 2200L),
      ),
    ),
  )

  private val statisticRepo = mockk<ListeningStatisticRepository> {
    every { getStatisticsSummary() } returns summaryFlow
    coEvery { clearStatistics() } just Runs
    coEvery { importSmartAudioBookPlayerXml(any()) } returns ImportResult(2, 7200L)
  }

  private val viewModel = StatisticsViewModel(
    navigator = navigator,
    statisticRepo = statisticRepo,
    context = context,
    dispatcherProvider = DispatcherProvider(scope.coroutineContext, scope.coroutineContext, scope.coroutineContext),
  )

  @Test
  fun `viewState formats duration and calculates monthly and book fractions`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      val state = awaitItem()
      assertEquals("2 Std.", state.totalFormattedTime)
      assertEquals("1 Std.", state.thisMonthFormattedTime)
      assertEquals(2, state.booksListenedCount)
      assertEquals(1, state.monthlyStats.size)
      assertEquals(2, state.bookStats.size)
      assertEquals("file:///cover.png", state.bookStats[0].coverUrl)
      assertEquals(null, state.bookStats[1].coverUrl)
      assertEquals(1.0f, state.monthlyStats[0].progressFraction)
      assertEquals(1.0f, state.bookStats[0].progressFraction)
      assertFalse(state.showClearDialog)
    }
  }

  @Test
  fun `close navigates back`() {
    viewModel.close()
    verify(exactly = 1) { navigator.goBack() }
  }

  @Test
  fun `clear dialog open and confirm calls repo`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(false, awaitItem().showClearDialog)

      viewModel.onClearClick()
      assertEquals(true, awaitItem().showClearDialog)

      viewModel.onConfirmClear()
      assertEquals(false, awaitItem().showClearDialog)

      coVerify(exactly = 1) { statisticRepo.clearStatistics() }
    }
  }
}
