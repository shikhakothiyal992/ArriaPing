package com.arria.ping.factory

import com.arria.ping.BuildConfig
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.apimanager.APIFactory
import com.arria.ping.apimanager.APIFactoryImpl
import com.arria.ping.util.IpConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun provideBaseUrl() = BuildConfig.BASE_URL_AUTH


    @Provides
    @Singleton
    fun provideOkHttpClient() = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(IpConstants.RETROFIT_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(IpConstants.RETROFIT_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(IpConstants.RETROFIT_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(ApiLoggingInterceptor())
                .build()
    }else{
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(IpConstants.RETROFIT_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(IpConstants.RETROFIT_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(IpConstants.RETROFIT_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(ApiLoggingInterceptor())
                .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
            okHttpClient: OkHttpClient,
            baseUrl: String
    ): Retrofit =
            Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .build()

    @Provides
    @Singleton
    fun provideApiServices(retrofit: Retrofit): ApiInterface = retrofit.create(ApiInterface::class.java)

    @Provides
    @Singleton
    fun provideApiFactory(apiHelper: APIFactoryImpl): APIFactory = apiHelper

}