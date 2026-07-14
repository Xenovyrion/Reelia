package com.timeline.app.ui.guide

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.app.R
import com.timeline.app.data.guide.GuideBlock
import com.timeline.app.data.guide.GuideSection
import com.timeline.app.ui.common.components.BackButton
import com.timeline.app.ui.common.components.SectionHeader
import com.timeline.app.ui.common.format.parseInlineMarkdown
import com.timeline.app.ui.theme.StatusFavorite
import com.timeline.app.ui.theme.StatusPlanned
import com.timeline.app.ui.theme.StatusWantToWatch
import com.timeline.app.ui.theme.StatusWatchingCompleted
import com.timeline.app.ui.theme.timeLineTopAppBarColors

/** Cycled per section so each card reads as visually distinct at a glance, same functional
 * palette used throughout the app (stats, status dots) rather than a one-off guide palette. */
private val GuideAccentPalette = listOf(StatusWatchingCompleted, StatusWantToWatch, StatusPlanned, StatusFavorite)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(onBack: () -> Unit, viewModel: GuideViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.guide_title)) },
                navigationIcon = { BackButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) },
                colors = timeLineTopAppBarColors(),
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is GuideUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is GuideUiState.Error -> {
                Text(
                    stringResource(R.string.guide_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(padding).padding(16.dp),
                )
            }
            is GuideUiState.Loaded -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.content.intro.isNotBlank()) {
                        Text(state.content.intro.parseInlineMarkdown(), style = MaterialTheme.typography.bodyMedium)
                    }
                    state.content.sections.forEachIndexed { index, section ->
                        GuideSectionCard(section, accentColor = GuideAccentPalette[index % GuideAccentPalette.size])
                    }
                    Spacer(Modifier.padding(bottom = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun GuideSectionCard(section: GuideSection, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(accentColor, CircleShape))
                Spacer(Modifier.padding(start = 8.dp))
                SectionHeader(section.title)
            }
            Spacer(Modifier.padding(top = 12.dp))
            section.blocks.forEachIndexed { index, block ->
                if (index > 0) Spacer(Modifier.padding(top = 8.dp))
                when (block) {
                    is GuideBlock.Paragraph -> Text(
                        block.text.parseInlineMarkdown(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    is GuideBlock.BulletList -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        block.items.forEach { item ->
                            Text(
                                buildAnnotatedString {
                                    withStyle(SpanStyle(color = accentColor)) { append("•  ") }
                                    append(item.parseInlineMarkdown())
                                },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
