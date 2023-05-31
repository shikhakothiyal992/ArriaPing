package com.arria.ping.factory

import android.content.Context
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.apimanager.APIFactory
import com.arria.ping.repository.LoginService
import com.arria.ping.repository.SettingsService
import com.arria.ping.ui.refreshtoken.repository.RefreshTokenRepository
import com.arria.ping.util.NetworkHelper

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryFactory {

    @Singleton
    @Provides
    fun provideRefreshTokenRepository(
            @ApplicationContext context: Context,
            networkHelper: NetworkHelper,
            apiInterface: ApiInterface

    ): RefreshTokenRepository {
        return RefreshTokenRepository(
                context,
                networkHelper,
                apiInterface

        )
    }

    @Singleton
    @Provides
    fun provideLoginRepository(
            @ApplicationContext context: Context,
            networkHelper: NetworkHelper,
            apiFactory: APIFactory

    ): LoginService {
        return LoginService(
                context,
                networkHelper,
                apiFactory
        )
    }

    @Singleton
    @Provides
    fun provideSettingsRepository(
            @ApplicationContext context: Context,
            networkHelper: NetworkHelper,
            apiFactory: APIFactory

    ): SettingsService {
        return SettingsService(
                context,
                networkHelper,
                apiFactory
        )
    }

}