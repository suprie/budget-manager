package com.budgetmanager.data.api

import com.budgetmanager.data.settings.AppSettings
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Default base URL - can be changed via settings
    // Use 10.0.2.2 for Android emulator to access host machine's localhost
    // "localhost" on emulator refers to the emulator itself, NOT your computer!
    private var baseUrl: String = "http://10.0.2.2:8080/"

    private var authToken: String? = null
    private var retrofit: Retrofit? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun setBaseUrl(url: String) {
        if (baseUrl != url) {
            baseUrl = url
            retrofit = null // Force recreation
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        authToken?.let { token ->
            requestBuilder.header("Authorization", "Bearer $token")
        }

        requestBuilder.header("Content-Type", "application/json")
        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()

    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }

    val authApi: AuthApi
        get() = getRetrofit().create(AuthApi::class.java)

    val pocketApi: PocketApi
        get() = getRetrofit().create(PocketApi::class.java)

    val budgetApi: BudgetApi
        get() = getRetrofit().create(BudgetApi::class.java)

    val expenseApi: ExpenseApi
        get() = getRetrofit().create(ExpenseApi::class.java)
}
