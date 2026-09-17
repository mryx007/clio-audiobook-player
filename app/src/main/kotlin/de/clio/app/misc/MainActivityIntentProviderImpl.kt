package de.clio.app.misc

import android.app.PendingIntent
import android.content.Context
import de.clio.app.MainActivity
import de.clio.core.playback.notification.MainActivityIntentProvider
import dev.zacsweers.metro.Inject

@Inject
class MainActivityIntentProviderImpl(private val context: Context) : MainActivityIntentProvider {

  override fun toCurrentBook(): PendingIntent {
    val intent = MainActivity.goToBookIntent(context)
    return PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }
}
