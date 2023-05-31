package com.arria.ping.repository

import android.content.Context
import com.arria.ping.R
import com.arria.ping.apimanager.APIFactory
import com.arria.ping.util.NetworkHelper
import com.arria.ping.model.LoginFail
import com.arria.ping.model.login.LoginRequest
import com.arria.ping.model.login.Login
import com.arria.ping.model.login.LoginResponse
import com.arria.ping.model.login.map
import com.arria.ping.model.responsehandlers.Response
import com.arria.ping.util.IpConstants
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class LoginService @Inject constructor(
        private val context: Context,
        private val networkHelper: NetworkHelper,
        private val loginApiFactory: APIFactory,

        ) {

    suspend fun getLogin(loginRequest: LoginRequest): Response<Login?> {
        return withContext(IO) {
            if (!networkHelper.isNetworkConnected()) {
                return@withContext Response.error(
                        context.resources.getString(R.string.no_internet_error_title), null,
                        IpConstants.OFFLINE_ERROR_CODE,
                        null
                )
            }

            try {
                val response = loginApiFactory.getLogin(loginRequest)
                return@withContext when {
                    response.code() == 200 -> {
                        val responseLogin: LoginResponse = Gson().fromJson(
                                response.body()
                                        ?.charStream(), LoginResponse::class.java
                        )
                        Response.success(map(responseLogin))
                    }
                    else -> {
                        val errorResponse: LoginFail = Gson().fromJson(
                                response.errorBody()
                                        ?.charStream(), LoginFail::class.java
                        )
                        Response.error(errorResponse.message, null, errorResponse.statusCode, null)
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