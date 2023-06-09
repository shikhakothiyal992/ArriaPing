package com.arria.ping.apollo

import android.content.Context
import android.os.Looper
import com.apollographql.apollo.ApolloClient
import com.arria.ping.BuildConfig
import com.arria.ping.util.StorePrefData
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

private var instance: ApolloClient? = null

fun apolloClient(context: Context): ApolloClient {
    check(Looper.myLooper() == Looper.getMainLooper()) {
        "Only the main thread can get the apolloClient instance"
    }

    if (instance != null) {
        return instance!!
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthorizationInterceptor(context))
        .build()

    instance = ApolloClient.builder()
        .serverUrl(BuildConfig.BASE_URL_KPI)
        .okHttpClient(okHttpClient)
        .build()

    return instance!!
}

private class AuthorizationInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization",  StorePrefData.token)
            .build()
        return chain.proceed(request)
    }
}
