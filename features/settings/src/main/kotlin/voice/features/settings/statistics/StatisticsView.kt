package voice.features.settings.statistics

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavEntry
import coil.compose.AsyncImage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.core.ui.icons.VoiceIcons
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.core.strings.R as StringsR
import voice.core.ui.R as UiR

@Composable
internal fun StatisticsView(
  viewState: StatisticsViewState,
  viewModel: StatisticsViewModel,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val openFolderLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocumentTree(),
    onResult = { uri ->
      if (uri != null) {
        viewModel.importFromUri(uri)
      }
    },
  )

  LaunchedEffect(viewModel) {
    viewModel.viewEffects.collect { effect ->
      when (effect) {
        is StatisticsViewEffect.ShowMessage -> {
          snackbarHostState.showSnackbar(effect.message)
        }
      }
    }
  }

  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
          Text(stringResource(StringsR.string.statistics_title))
        },
        navigationIcon = {
          IconButton(onClick = viewModel::close) {
            Icon(
              imageVector = VoiceIcons.ArrowBack,
              contentDescription = stringResource(StringsR.string.common_action_close),
            )
          }
        },
        actions = {
          IconButton(
            onClick = { openFolderLauncher.launch(null) },
            enabled = !viewState.isImporting,
          ) {
            if (viewState.isImporting) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
              Icon(
                imageVector = VoiceIcons.Add,
                contentDescription = stringResource(StringsR.string.statistics_import_button),
              )
            }
          }
          if (viewState.monthlyStats.isNotEmpty()) {
            IconButton(onClick = viewModel::onClearClick) {
              Icon(
                imageVector = VoiceIcons.Delete,
                contentDescription = stringResource(StringsR.string.statistics_clear_all),
              )
            }
          }
        },
      )
    },
  ) { paddingValues ->
    if (viewState.monthlyStats.isEmpty() && viewState.bookStats.isEmpty()) {
      EmptyStatisticsView(
        paddingValues = paddingValues,
        isImporting = viewState.isImporting,
        onImportClick = { openFolderLauncher.launch(null) },
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Summary Cards
        item {
          SummarySection(viewState)
        }

        // Import Banner / Button
        item {
          OutlinedButton(
            onClick = { openFolderLauncher.launch(null) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewState.isImporting,
          ) {
            Icon(
              imageVector = VoiceIcons.Add,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(StringsR.string.statistics_import_button))
          }
        }

        // Monthly Breakdown
        if (viewState.monthlyStats.isNotEmpty()) {
          item {
            Text(
              text = stringResource(StringsR.string.statistics_monthly_history),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
          }

          items(viewState.monthlyStats, key = { it.yearMonth }) { month ->
            MonthlyStatCard(month)
          }
        }

        // Books Breakdown
        if (viewState.bookStats.isNotEmpty()) {
          item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = stringResource(StringsR.string.statistics_book_history),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
          }

          items(viewState.bookStats, key = { it.bookTitle }) { book ->
            BookStatCard(book)
          }
        }
      }
    }
  }

  if (viewState.showClearDialog) {
    AlertDialog(
      onDismissRequest = viewModel::onDismissClearDialog,
      title = { Text(stringResource(StringsR.string.statistics_clear_all)) },
      text = { Text(stringResource(StringsR.string.statistics_clear_confirm)) },
      confirmButton = {
        TextButton(onClick = viewModel::onConfirmClear) {
          Text(stringResource(StringsR.string.common_action_delete))
        }
      },
      dismissButton = {
        TextButton(onClick = viewModel::onDismissClearDialog) {
          Text(stringResource(StringsR.string.common_dialog_cancel))
        }
      },
    )
  }
}

@Composable
private fun SummarySection(viewState: StatisticsViewState) {
  ElevatedCard(
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
    ),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column {
          Text(
            text = stringResource(StringsR.string.statistics_total_time),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
          )
          Text(
            text = viewState.totalFormattedTime,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
        Icon(
          imageVector = VoiceIcons.Analytics,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(36.dp),
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column {
          Text(
            text = stringResource(StringsR.string.statistics_this_month),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
          )
          Text(
            text = viewState.thisMonthFormattedTime,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = stringResource(StringsR.string.statistics_books_listened),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
          )
          Text(
            text = viewState.booksListenedCount.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
      }
    }
  }
}

@Composable
private fun MonthlyStatCard(item: StatisticsViewState.MonthlyStatItem) {
  Card(
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    ),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = item.displayMonth,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = item.formattedDuration,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.primary,
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      LinearProgressIndicator(
        progress = { item.progressFraction },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
      )
    }
  }
}

@Composable
private fun BookStatCard(item: StatisticsViewState.BookStatItem) {
  Card(
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      AsyncImage(
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(8.dp)),
        model = item.coverUrl,
        placeholder = painterResource(id = UiR.drawable.album_art),
        error = painterResource(id = UiR.drawable.album_art),
        contentScale = ContentScale.Crop,
        contentDescription = null,
      )

      Spacer(modifier = Modifier.width(12.dp))

      Column(
        modifier = Modifier.weight(1f),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = item.bookTitle,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = item.formattedDuration,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
          progress = { item.progressFraction },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
        )
      }
    }
  }
}

@Composable
private fun EmptyStatisticsView(
  paddingValues: PaddingValues,
  isImporting: Boolean,
  onImportClick: () -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = VoiceIcons.Analytics,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = stringResource(StringsR.string.statistics_empty),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(24.dp))
      Button(
        onClick = onImportClick,
        enabled = !isImporting,
      ) {
        if (isImporting) {
          CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
          Icon(imageVector = VoiceIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(stringResource(StringsR.string.statistics_import_button))
        }
      }
    }
  }
}

@ContributesTo(AppScope::class)
interface StatisticsGraph {
  val statisticsViewModel: StatisticsViewModel
}

@ContributesTo(AppScope::class)
interface StatisticsProvider {

  @Provides
  @IntoSet
  fun statisticsNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Statistics> { key ->
    NavEntry(key) {
      StatisticsScreen()
    }
  }
}

@Composable
fun StatisticsScreen() {
  val viewModel = retain<StatisticsViewModel> { rootGraphAs<StatisticsGraph>().statisticsViewModel }
  val viewState = viewModel.viewState()
  StatisticsView(viewState, viewModel)
}
