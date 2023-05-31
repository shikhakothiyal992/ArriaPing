package  com.arria.ping.apiclient

import com.arria.ping.model.SendRefreshRequest
import com.arria.ping.model.forgotpassword.ForgotPassword
import com.arria.ping.model.login.LoginRequest
import com.arria.ping.model.successLogin.LoginSuccess
import com.arria.ping.util.StorePrefData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    @POST("/auth")
    suspend fun login(@Body loginRequest: LoginRequest): Response<ResponseBody>

    @POST("/auth/challenge/response")
    fun changePassword(@Body body: String): Call<LoginSuccess>

    @POST("/idm/user/password/forgot")
    fun forgotPassword(@Body body: String): Call<ForgotPassword>

    @POST("/auth/token/refresh")
    fun refreshToken( @Body refreshToken: SendRefreshRequest): Call<LoginSuccess>

    @PATCH("/idm/user/password/forgot/confim")
    fun forgotConfirmPassword(@Body body: String): Call<ForgotPassword>

    @DELETE("/auth")
    suspend fun logout(@Header("X-Access-Token") token: String = StorePrefData.token): Response<ResponseBody>

}