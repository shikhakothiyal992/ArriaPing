package com.arria.ping.apimanager

import com.arria.ping.model.login.LoginRequest
import okhttp3.ResponseBody
import retrofit2.Response

interface APIFactory {

    suspend fun getLogin(loginRequest: LoginRequest): Response<ResponseBody>
    suspend fun doLogout(): Response<ResponseBody>

}