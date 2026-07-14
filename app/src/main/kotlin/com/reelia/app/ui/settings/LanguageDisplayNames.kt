package com.reelia.app.ui.settings

import com.reelia.app.R

/** Maps a TMDB content-language code to the @StringRes name shown in the language picker,
 * so the list reads in whichever language the UI is currently displayed in. */
val LANGUAGE_DISPLAY_NAME_RES: Map<String, Int> = mapOf(
    "fr-FR" to R.string.lang_name_fr,
    "en-US" to R.string.lang_name_en_us,
    "en-GB" to R.string.lang_name_en_gb,
    "es-ES" to R.string.lang_name_es,
    "de-DE" to R.string.lang_name_de,
    "it-IT" to R.string.lang_name_it,
    "ja-JP" to R.string.lang_name_ja,
)
