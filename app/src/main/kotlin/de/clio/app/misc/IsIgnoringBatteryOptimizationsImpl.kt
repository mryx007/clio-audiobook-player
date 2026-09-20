package de.clio.app.misc

import android.app.Application
import android.os.PowerManager
import androidx.core.content.getSystemService
import de.clio.core.common.IsIgnoringBatteryOptimizations
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class IsIgnoringBatteryOptimizationsImpl(private val context: Application) : IsIgnoringBatteryOptimizations {
  override fun invoke(): Boolean {
    val powerManager = context.getSystemService<PowerManager>() ?: return true
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
  }
}
