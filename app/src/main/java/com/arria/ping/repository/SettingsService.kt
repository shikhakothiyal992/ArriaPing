package com.arria.ping.repository

import android.content.Context
import com.arria.ping.R
import com.arria.ping.apimanager.APIFactory
import com.arria.ping.util.NetworkHelper
import com.arria.ping.model.login.*
import com.arria.ping.model.responsehandlers.Response
import com.arria.ping.util.IpConstants
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class SettingsService @Inject constructor(
        private val context: Context,
        private val networkHelper: NetworkHelper,
        private val apiFactory: APIFactory,

        ) {

    suspend fun doLogout(): Response<Logout?> {
        return withContext(IO) {
            if (!networkHelper.isNetworkConnected()) {
                return@withContext Response.error(
                        context.resources.getString(R.string.no_internet_error_title), null,
                        IpConstants.OFFLINE_ERROR_CODE,
                        null
                )
            }

            try {
                val response = apiFactory.doLogout()
                return@withContext when {
                    response.code() == 200 -> {
                        val responseLogin: LogoutResponse = Gson().fromJson(
                                response.body()
                                        ?.charStream(), LogoutResponse::class.java
                        )

                        Response.success(Logout(responseLogin.message))
                    }
                    else -> {
                        val errorResponse: Logout = Gson().fromJson(
                                response.errorBody()
                                        ?.charStream(), Logout::class.java
                        )
                        Response.error(errorResponse.message, null, response.code(), null)
                    }
                }
            } catch (e: HttpException) {
                if (!networkHelper.isNetworkConnected()) {
                    return@withContext Response.error(
                            context.resources.getString(R.string.no_internet_error_title), null,
                            IpConstants.OFFLINE_ERROR_CODE,
                            null
                    )
                } else {
                    Response.error(
                            e.message, null,
                            e.code(),
                            null
                    )
                }
            } catch (e: Exception) {
                Response.error(
                        e.message, null,
                        0,
                        null
                )
            }
        }
    }
}