package com.arria.ping.ui.refreshtoken.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.log.Logger
import com.arria.ping.util.NetworkHelper
import com.arria.ping.ui.refreshtoken.model.RefreshTokenResponse
import com.arria.ping.ui.refreshtoken.model.Status
import com.arria.ping.model.LoginFail
import com.arria.ping.model.SendRefreshRequest
import com.arria.ping.model.successLogin.LoginSuccess
import com.arria.ping.util.StorePrefData
import com.arria.ping.util.Validation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback
import javax.inject.Inject

class RefreshTokenRepository @Inject constructor(
        private val context: Context,
        private val networkHelper: NetworkHelper,
        private val apiInterface: ApiInterface

) {

    fun getRefreshToken(): MutableLiveData<RefreshTokenResponse> {

        val refreshTokenResponse = MutableLiveData<RefreshTokenResponse>()

        val call = apiInterface.refreshToken(SendRefreshRequest(StorePrefData.refreshToken))

        refreshTokenResponse.value = RefreshTokenResponse(Status.LOADING)
        call.enqueue(object : Callback<LoginSuccess> {
            @RequiresApi(Build.VERSION_CODES.O)

            override fun onResponse(
                call: Call<LoginSuccess>,
                response: Response<LoginSuccess>,
            ) {

                if (response.isSuccessful) {
                    StorePrefData.token = response.body()!!.authenticationResult.accessToken
                    refreshTokenResponse.value = RefreshTokenResponse(Status.SUCCESS)
                    Logger.info("Token Refreshed successfully","Refresh Token Query")
                } else {
                    val gsonRefreshCEOPeriodKpi = Gson()
                    val typeRefreshCEOPeriodKpi = object : TypeToken<LoginFail>() {
                    }.type
                    val errorResponse = gsonRefreshCEOPeriodKpi.fromJson<LoginFail>(
                        response.errorBody()!!.charStream(), typeRefreshCEOPeriodKpi
                    )
                    refreshTokenResponse.value = RefreshTokenResponse(Status.UNSUCCESSFUL)
                    Logger.error("Failed  to RefreshToken ${errorResponse.message}","Refresh Token Query")
                }
            }

            override fun onFailure(call: Call<LoginSuccess>, t: Throwable) {
                if (networkHelper.isNetworkConnected()) {
                    Logger.error("Failed  to RefreshToken ${t.message}","Refresh Token Query")
                    refreshTokenResponse.value = RefreshTokenResponse(Status.ERROR)
                } else {
                    refreshTokenResponse.value = RefreshTokenResponse(Status.OFFLINE)
                }

            }
        })
        return refreshTokenResponse
    }

}




