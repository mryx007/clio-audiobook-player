package de.clio.core.remoteconfig.api

interface FmcTokenProvider {

  suspend fun token(): String?
}
