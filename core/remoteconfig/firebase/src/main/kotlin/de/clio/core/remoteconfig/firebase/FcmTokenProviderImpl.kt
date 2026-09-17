package de.clio.core.remoteconfig.firebase

import com.google.firebase.installations.FirebaseInstallations
import de.clio.core.logging.api.Logger
import de.clio.core.remoteconfig.api.FmcTokenProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.tasks.await

@ContributesBinding(AppScope::class)
class FcmTokenProviderImpl : FmcTokenProvider {

  override suspend fun token(): String? {
    val tokenResult = try {
      FirebaseInstallations.getInstance().getToken(true)
        .await()
    } catch (e: Exception) {
      Logger.w(e)
      return null
    }
    return tokenResult.token
  }
}
