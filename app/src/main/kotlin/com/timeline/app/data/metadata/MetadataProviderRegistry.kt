package com.timeline.app.data.metadata

import com.timeline.app.data.metadata.tmdb.TmdbMetadataProvider
import com.timeline.app.data.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Registry of available metadata providers. Only TMDB is functional today; a second provider
 * would just be appended to [providers] with `isAvailable = false` until it's actually wired up.
 */
@Singleton
class MetadataProviderRegistry @Inject constructor(
    tmdbMetadataProvider: TmdbMetadataProvider,
    private val settingsRepository: SettingsRepository,
) {
    val providers: List<MetadataProvider> = listOf(tmdbMetadataProvider)

    val activeProvider: Flow<MetadataProvider> = settingsRepository.selectedProviderId.map { selectedId ->
        providers.firstOrNull { it.id == selectedId && it.isAvailable }
            ?: providers.first { it.isAvailable }
    }
}
