package com.arria.ping.apimanager

import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.model.login.LoginRequest
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class APIFactoryImpl @Inject constructor(private val apiInterface: ApiInterface) : APIFactory {

    override suspend fun getLogin(loginRequest: LoginRequest): Response<ResponseBody> =  apiInterface.login(loginRequest)
    override suspend fun doLogout(): Response<ResponseBody> =  apiInterface.logout()

}