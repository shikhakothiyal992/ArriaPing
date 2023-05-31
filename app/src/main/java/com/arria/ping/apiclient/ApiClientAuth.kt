package com.arria.ping.apiclient

import com.arria.ping.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClientAuth {

    private var retrofit: Retrofit? = null

    private val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private var httpClientProvider = OkHttpClient.Builder().addInterceptor(interceptor)
        .addNetworkInterceptor { chain ->

            val newRequest = chain.request().newBuilder()
                .addHeader("Content-Type", "text/plain")
                .build()
            chain.proceed(newRequest)
        }.retryOnConnectionFailure(true).connectTimeout(10, TimeUnit.MINUTES)
        .readTimeout(10, TimeUnit.MINUTES).writeTimeout(10, TimeUnit.MINUTES).build()


    fun getClient(): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .client(httpClientProvider)
                .baseUrl(BuildConfig.BASE_URL_AUTH)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }

}