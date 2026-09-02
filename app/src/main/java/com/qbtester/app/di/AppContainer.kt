package com.qbtester.app.di

import android.content.Context
import com.qbtester.app.BuildConfig
import com.qbtester.app.data.local.QbCacheDataSource
import com.qbtester.app.data.remote.SleeperApi
import com.qbtester.app.data.remote.SleeperRemoteDataSource
import com.qbtester.app.data.repository.QuarterbackRepository
import com.qbtester.app.data.repository.QuarterbackRepositoryImpl
import com.qbtester.app.data.repository.RefreshPolicy
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit

/**
 * Small hand-written dependency container. The app is intentionally simple enough that a full
 * DI framework (Hilt/Dagger) would add build complexity (annotation processing, generated code)
 * without a real benefit - see CLAUDE.md for the reasoning.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SLEEPER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(
            json.asConverterFactory("application/json".toMediaType())
        )
        .build()

    private val sleeperApi: SleeperApi = retrofit.create()

    private val remoteDataSource = SleeperRemoteDataSource(
        api = sleeperApi,
        cdnBaseUrl = BuildConfig.SLEEPER_CDN_BASE_URL,
    )

    private val cacheDataSource = QbCacheDataSource(appContext, json)

    val quarterbackRepository: QuarterbackRepository = QuarterbackRepositoryImpl(
        remote = remoteDataSource,
        cache = cacheDataSource,
        refreshPolicy = RefreshPolicy(),
    )
}
