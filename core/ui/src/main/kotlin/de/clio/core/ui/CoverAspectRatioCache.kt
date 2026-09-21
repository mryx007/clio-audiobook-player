package de.clio.core.ui

import java.util.concurrent.ConcurrentHashMap

public object CoverAspectRatioCache {
  private val cache = ConcurrentHashMap<String, Float>()

  public fun get(cover: String?): Float? = cover?.let { cache[it] }

  public fun put(cover: String?, ratio: Float) {
    if (cover != null && ratio > 0f) {
      cache[cover] = ratio
    }
  }
}
