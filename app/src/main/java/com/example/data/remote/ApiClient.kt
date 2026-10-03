package com.example.data.remote

import android.content.Context
import com.example.data.repository.TokenManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // 10.0.2.2 connects to host computer from Android Emulator
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/api/v1/"

    private val configuredBaseUrl: String by lazy {
        try {
            val url = com.example.BuildConfig.API_BASE_URL
            if (!url.isNullOrBlank() && url.startsWith("http")) url else DEFAULT_BASE_URL
        } catch (e: Throwable) {
            DEFAULT_BASE_URL
        }
    }

    @Volatile
    private var apiService: JobSaarthiApiService? = null

    fun getApiService(context: Context, baseUrl: String = configuredBaseUrl): JobSaarthiApiService {
        return apiService ?: synchronized(this) {
            val tokenManager = TokenManager(context)
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(tokenManager))
                .addInterceptor(loggingInterceptor)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val service = retrofit.create(JobSaarthiApiService::class.java)
            apiService = service
            service
        }
    }
}
