package com.reelia.app.di

import javax.inject.Qualifier

/** Distinguishes the Wikidata [retrofit2.Retrofit]/[okhttp3.OkHttpClient] bindings from the
 * default TMDB ones in [NetworkModule] — both provide the same types, so Hilt needs a
 * qualifier to tell them apart. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WikidataRetrofit
