package de.clio.core.logging.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import de.clio.core.logging.api.LogWriter
import de.clio.core.logging.api.Logger

internal class CrashlyticsLogWriter : LogWriter {

  private val crashlytics: FirebaseCrashlytics get() = FirebaseCrashlytics.getInstance()

  override fun log(
    severity: Logger.Severity,
    message: String,
    throwable: Throwable?,
  ) {
    if (severity <= Logger.Severity.Verbose) {
      return
    }

    crashlytics.log(message)
    if (severity >= Logger.Severity.Error) {
      crashlytics.recordException(throwable ?: AssertionError(message))
    }
  }
}
