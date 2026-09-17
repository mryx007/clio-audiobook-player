package de.clio.core.initializer

import android.app.Application

interface AppInitializer {

  fun onAppStart(application: Application)
}
