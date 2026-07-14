package com.reelia.app.ui.persondetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelia.app.R
import com.reelia.app.data.remote.tmdb.TmdbApi
import com.reelia.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.reelia.app.data.remote.wikidata.WikidataApi
import com.reelia.app.domain.model.MediaType
import com.reelia.app.ui.common.format.toYearOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tmdbApi: TmdbApi,
    private val wikidataApi: WikidataApi,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : ViewModel() {

    private val personId: Int = checkNotNull(savedStateHandle["personId"])

    private val _uiState = MutableStateFlow(PersonDetailUiState())
    val uiState: StateFlow<PersonDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val details = tmdbApi.getPersonDetails(personId)
                // TMDB biographies are rarely translated outside English — fall back to the
                // English one rather than show an empty biography for most non-English users.
                val biography = details.biography.ifBlank {
                    runCatching { tmdbApi.getPersonDetails(personId, language = "en-US") }
                        .getOrNull()
                        ?.biography
                        .orEmpty()
                }

                val (filmography, awards) = coroutineScope {
                    val creditsDeferred = async { runCatching { tmdbApi.getPersonCombinedCredits(personId) }.getOrNull() }
                    val awardsDeferred = async { runCatching { fetchAwards(personId) }.getOrDefault(emptyList()) }

                    val filmography = creditsDeferred.await()?.cast
                        .orEmpty()
                        .filter { it.mediaType == "movie" || it.mediaType == "tv" }
                        .distinctBy { it.id to it.mediaType }
                        .sortedByDescending { (it.releaseDate ?: it.firstAirDate).orEmpty() }
                        .mapNotNull { credit ->
                            val title = credit.title ?: credit.name ?: return@mapNotNull null
                            PersonFilmographyItem(
                                id = credit.id,
                                mediaType = if (credit.mediaType == "movie") MediaType.MOVIE else MediaType.TV,
                                title = title,
                                posterUrl = imageUrlBuilder.posterUrl(credit.posterPath, size = "w185"),
                                character = credit.character?.takeIf { it.isNotBlank() },
                                year = (credit.releaseDate ?: credit.firstAirDate).toYearOrNull(),
                            )
                        }

                    filmography to awardsDeferred.await()
                }

                _uiState.value = PersonDetailUiState(
                    isLoading = false,
                    name = details.name,
                    photoUrl = imageUrlBuilder.posterUrl(details.profilePath, size = "w185"),
                    biography = biography,
                    birthday = details.birthday,
                    deathday = details.deathday,
                    placeOfBirth = details.placeOfBirth,
                    filmography = filmography,
                    awards = awards,
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessageRes = R.string.person_detail_error_load)
                }
            }
        }
    }

    /** TMDB has no awards data at all, so this queries Wikidata (free, keyless) instead,
     * matching the person via the "TMDb person ID" property (P4985) and reading both
     * "award received" (P166, wins) and "nominated for" (P1411, nominations), each with
     * optional "point in time" (P585) and "for work" (P1686) qualifiers. */
    private suspend fun fetchAwards(tmdbPersonId: Int): List<PersonAward> {
        val sparql = """
            SELECT ?awardLabel ?pointInTime ?forWorkLabel ?type WHERE {
              {
                ?person wdt:P4985 "$tmdbPersonId" ;
                        p:P166 ?stmt .
                ?stmt ps:P166 ?award .
                OPTIONAL { ?stmt pq:P585 ?pointInTime . }
                OPTIONAL { ?stmt pq:P1686 ?forWork . }
                BIND("won" AS ?type)
              }
              UNION
              {
                ?person wdt:P4985 "$tmdbPersonId" ;
                        p:P1411 ?stmt .
                ?stmt ps:P1411 ?award .
                OPTIONAL { ?stmt pq:P585 ?pointInTime . }
                OPTIONAL { ?stmt pq:P1686 ?forWork . }
                BIND("nominated" AS ?type)
              }
              SERVICE wikibase:label { bd:serviceParam wikibase:language "fr,en". }
            }
            ORDER BY DESC(?pointInTime)
            LIMIT 50
        """.trimIndent()

        return wikidataApi.query(sparql).results.bindings.mapNotNull { binding ->
            val name = binding["awardLabel"]?.value ?: return@mapNotNull null
            PersonAward(
                name = name,
                won = binding["type"]?.value == "won",
                year = binding["pointInTime"]?.value?.take(4),
                forWork = binding["forWorkLabel"]?.value,
            )
        }
    }
}
