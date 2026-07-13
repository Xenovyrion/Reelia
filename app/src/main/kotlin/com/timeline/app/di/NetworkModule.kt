package com.timeline.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.timeline.app.data.remote.tmdb.TmdbApi
import com.timeline.app.data.remote.tmdb.TmdbApiKeyInterceptor
import com.timeline.app.data.remote.tmdb.TmdbLanguageInterceptor
import com.timeline.app.data.remote.wikidata.WikidataApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        apiKeyInterceptor: TmdbApiKeyInterceptor,
        languageInterceptor: TmdbLanguageInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(languageInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(TmdbApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideTmdbApi(retrofit: Retrofit): TmdbApi = retrofit.create(TmdbApi::class.java)

    /** Separate client: no TMDB interceptors, and Wikidata's usage policy asks for a
     * descriptive User-Agent identifying the app rather than a generic/default one. */
    @Provides
    @Singleton
    @WikidataRetrofit
    fun provideWikidataOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Reelia/1.0 (personal Android app; github.com/Xenovyrion/TimeLine)")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()

    @Provides
    @Singleton
    @WikidataRetrofit
    fun provideWikidataRetrofit(@WikidataRetrofit okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(WikidataApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideWikidataApi(@WikidataRetrofit retrofit: Retrofit): WikidataApi = retrofit.create(WikidataApi::class.java)
}
