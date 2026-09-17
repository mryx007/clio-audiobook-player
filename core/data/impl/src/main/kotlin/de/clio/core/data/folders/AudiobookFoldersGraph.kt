package de.clio.core.data.folders

import android.net.Uri
import androidx.datastore.core.DataStore
import de.clio.core.common.serialization.UriSerializer
import de.clio.core.data.store.ClioDataStoreFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.builtins.SetSerializer

@ContributesTo(AppScope::class)
public interface AudiobookFoldersGraph {

  @Provides
  @SingleIn(AppScope::class)
  @RootAudiobookFoldersStore
  private fun audiobookFolders(factory: ClioDataStoreFactory): DataStore<Set<Uri>> {
    return factory.createUriSet("audiobookFolders")
  }

  @Provides
  @SingleIn(AppScope::class)
  @SingleFolderAudiobookFoldersStore
  private fun singleFolderAudiobookFolders(factory: ClioDataStoreFactory): DataStore<Set<Uri>> {
    return factory.createUriSet("SingleFolderAudiobookFolders")
  }

  @Provides
  @SingleIn(AppScope::class)
  @SingleFileAudiobookFoldersStore
  private fun singleFileAudiobookFolders(factory: ClioDataStoreFactory): DataStore<Set<Uri>> {
    return factory.createUriSet("SingleFileAudiobookFolders")
  }

  @Provides
  @SingleIn(AppScope::class)
  @AuthorAudiobookFoldersStore
  private fun authorAudiobookFolders(factory: ClioDataStoreFactory): DataStore<Set<Uri>> {
    return factory.createUriSet("AuthorAudiobookFolders")
  }
}

private fun ClioDataStoreFactory.createUriSet(name: String): DataStore<Set<Uri>> = create(
  serializer = SetSerializer(UriSerializer),
  fileName = name,
  defaultValue = emptySet(),
)
