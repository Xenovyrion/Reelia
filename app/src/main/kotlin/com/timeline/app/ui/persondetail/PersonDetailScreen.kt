package com.timeline.app.ui.persondetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.timeline.app.R
import com.timeline.app.domain.model.MediaType
import com.timeline.app.ui.common.components.BackButton
import com.timeline.app.ui.common.components.SectionHeader
import com.timeline.app.ui.common.format.toFormattedDateOrNull
import com.timeline.app.ui.theme.StatusFavorite
import com.timeline.app.ui.theme.timeLineTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    onBack: () -> Unit,
    onShowClick: (Int) -> Unit = {},
    onMovieClick: (Int) -> Unit = {},
    viewModel: PersonDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = timeLineTopAppBarColors(),
                navigationIcon = { BackButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = uiState.photoUrl,
                    contentDescription = uiState.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Text(
                    uiState.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                )

                val metadata = listOfNotNull(
                    uiState.birthday.toFormattedDateOrNull()?.let { stringResource(R.string.person_detail_born_format, it) },
                    uiState.deathday.toFormattedDateOrNull()?.let { stringResource(R.string.person_detail_died_format, it) },
                    uiState.placeOfBirth,
                ).joinToString(" • ")
                if (metadata.isNotBlank()) {
                    Text(
                        metadata,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                uiState.errorMessageRes?.let {
                    Text(
                        stringResource(it),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(stringResource(R.string.person_detail_biography_section_title))
                    Spacer(Modifier.padding(top = 12.dp))
                    Text(
                        uiState.biography.takeIf { it.isNotBlank() } ?: stringResource(R.string.person_detail_no_biography),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(stringResource(R.string.person_detail_filmography_section_title))
                    Spacer(Modifier.padding(top = 12.dp))
                    if (uiState.filmography.isEmpty()) {
                        Text(
                            stringResource(R.string.person_detail_no_filmography),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LazyRow {
                            items(uiState.filmography, key = { "${it.mediaType}_${it.id}" }) { item ->
                                FilmographyPoster(
                                    item = item,
                                    onClick = {
                                        when (item.mediaType) {
                                            MediaType.TV -> onShowClick(item.id)
                                            MediaType.MOVIE -> onMovieClick(item.id)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.awards.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(stringResource(R.string.person_detail_awards_section_title))
                        Spacer(Modifier.padding(top = 12.dp))
                        uiState.awards.forEachIndexed { index, award ->
                            AwardRow(award)
                            if (index != uiState.awards.lastIndex) {
                                Spacer(Modifier.padding(top = 12.dp))
                            }
                        }
                    }
                }
            }

            Box(Modifier.padding(bottom = 16.dp))
        }
    }
}

@Composable
private fun AwardRow(award: PersonAward) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Icon(
            if (award.won) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents,
            contentDescription = null,
            tint = if (award.won) StatusFavorite else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(award.name, style = MaterialTheme.typography.bodyMedium)
            val subtitle = listOfNotNull(
                stringResource(if (award.won) R.string.person_detail_award_won else R.string.person_detail_award_nominated),
                award.year,
                award.forWork,
            ).joinToString(" • ")
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FilmographyPoster(item: PersonFilmographyItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .padding(end = 12.dp)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = item.posterUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Text(
            item.title,
            style = MaterialTheme.typography.labelMedium,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            item.character.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            item.year.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
