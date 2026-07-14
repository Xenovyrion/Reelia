package com.reelia.app.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.reelia.app.R

data class CastRowItem(val personId: Int, val name: String, val character: String, val photoUrl: String?)

/** Wider than a typical avatar row on purpose: with ~4-5 narrow columns visible at once, most
 * names got truncated to the point of being useless — fitting ~3 at a time (with a peek of the
 * next) leaves enough width for a full name to actually read. */
@Composable
fun CastRow(cast: List<CastRowItem>, onPersonClick: (Int) -> Unit = {}) {
    LazyRow {
        items(cast) { member ->
            Column(
                modifier = Modifier
                    .width(104.dp)
                    .padding(end = 12.dp)
                    .clickable { onPersonClick(member.personId) },
            ) {
                AsyncImage(
                    model = member.photoUrl,
                    contentDescription = member.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Text(
                    member.name,
                    style = MaterialTheme.typography.labelMedium,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    member.character,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** TMDB crew job titles are a fixed English vocabulary, not translated by the language query
 * param the way overviews/titles are — this maps the handful this app actually shows (see
 * TmdbMetadataProvider's RELEVANT_CREW_JOBS) to a localized label, falling back to the raw
 * TMDB string for anything unrecognized. */
@Composable
fun localizedCrewJobLabel(job: String): String = when (job) {
    "Director" -> stringResource(R.string.crew_job_director)
    "Creator" -> stringResource(R.string.crew_job_creator)
    "Original Music Composer" -> stringResource(R.string.crew_job_composer)
    "Writer" -> stringResource(R.string.crew_job_writer)
    else -> job
}
