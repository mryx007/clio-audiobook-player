package de.clio.features.playbackScreen.batteryOptimization

import android.os.Build
import androidx.datastore.core.DataStore
import de.clio.core.common.IsIgnoringBatteryOptimizations
import de.clio.core.data.store.AmountOfBatteryOptimizationRequestedStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

@Inject
class BatteryOptimization(
  private val isIgnoringBatteryOptimizations: IsIgnoringBatteryOptimizations,
  @AmountOfBatteryOptimizationRequestedStore
  private val amountOfBatteryOptimizationsRequested: DataStore<Int>,
) {

  suspend fun shouldRequest(): Boolean = when {
    isIgnoringBatteryOptimizations() -> false
    Build.MANUFACTURER.equals("Google", ignoreCase = true) -> false
    else -> amountOfBatteryOptimizationsRequested.data.first() <= 3
  }

  suspend fun onBatteryOptimizationsRequested() {
    amountOfBatteryOptimizationsRequested.updateData { it + 1 }
  }
}
