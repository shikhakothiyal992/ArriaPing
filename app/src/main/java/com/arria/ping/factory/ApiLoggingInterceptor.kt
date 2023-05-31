package com.arria.ping.factory

import com.arria.ping.util.IpConstants
import okhttp3.Interceptor
import okhttp3.Response

class ApiLoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
                .addHeader("Content-Type", IpConstants.HEADER_CONTENT_TYPE)
                .build()
        return chain.proceed(request)
    }
}