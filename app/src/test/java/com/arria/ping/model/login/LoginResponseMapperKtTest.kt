package com.arria.ping.model.login

import com.arria.ping.model.responsehandlers.Response
import com.arria.ping.model.responsehandlers.Status
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class LoginResponseMapperKtTest {
    private lateinit var response: Response<*>
    private lateinit var loginResponse: LoginResponse
    private lateinit var authenticationResult: LoginResponse.AuthenticationResult
    private lateinit var challengeParameters: LoginResponse.ChallengeParameters

    private lateinit var login: Login
    private lateinit var challengeParametersResuliData: Login.ChallengeParametersData
    private lateinit var authenticationResultData: Login.AuthenticationResultData

    @Before
    fun init() {
        authenticationResult =
                LoginResponse.AuthenticationResult(
                        "weZ12ac",
                        300,
                        "Bearer",
                        "eyJjdH",
                        "eyJraWQiOi"
                )
        challengeParameters = LoginResponse.ChallengeParameters(null, null, null)
        loginResponse = LoginResponse(null, null, challengeParameters, authenticationResult)
    }

    @Test
    fun map_LoginModel_When_ChallengeName_Session_And_ChallengeParametersValues_Null_Expected_AuthenticationResultData() {

        val result = map(loginResponse)
        challengeParametersResuliData = Login.ChallengeParametersData(null, null, null)
        authenticationResultData = Login.AuthenticationResultData(
                "weZ12ac",
                300,
                "Bearer",
                "eyJjdH",
                "eyJraWQiOi"
        )
        login = Login(null, null, challengeParametersResuliData, authenticationResultData)
        assertThat(result).isEqualTo(login)
    }

    @Test
    fun map_LoginModel_When_AuthenticationResult_Null_Expected_AuthenticationResultData() {
        val challengeParameters = LoginResponse.ChallengeParameters(
                "31cbf97e", "[]", "{\"email_verified\":\"True\"," +
                "\"given_name\":\"Test1\",\"family_name\":\"Test\",\"email\":\"test1@test.com\"}"
        )
        val loginResponse = LoginResponse("NEW_PASSWORD_REQUIRED", "AYABeBG1XRwVIes", challengeParameters, null)

        val result = map(loginResponse)

        val challengeParametersResuliData = Login.ChallengeParametersData(
                "31cbf97e", "[]", "{\"email_verified\":\"True\"," +
                "\"given_name\":\"Test1\",\"family_name\":\"Test\",\"email\":\"test1@test.com\"}"
        )
       val authenticationResultData = Login.AuthenticationResultData(
                null,
                null,
                null,
                null,
                null
        )
        val login = Login("NEW_PASSWORD_REQUIRED", "AYABeBG1XRwVIes", challengeParametersResuliData, authenticationResultData)
        assertThat(result).isEqualTo(login)
    }


    @Test
    fun map_when_Login_NotNull() {
        challengeParametersResuliData = Login.ChallengeParametersData(null, null, null)
        authenticationResultData = Login.AuthenticationResultData(
                "weZ12ac",
                300,
                "Bearer",
                "eyJjdH",
                "eyJraWQiOi"
        )
        login = Login(null, null, challengeParametersResuliData, authenticationResultData)
        assertThat(login).isNotNull()
    }

    @Test
    fun map_when_Login_IsNull() {
        val login: Login? = null
        assertThat(login).isNull()
    }

    @Test
    fun response_When_Response_Is_Loading(){
        val result = Response.loading(null)
        val actual = Response(Status.LOADING, null, null, -1, null)
        assertThat(result).isEqualTo(actual)
    }

    @Test
    fun response_When_Response_Is_Success_Expected_DATA(){
        val result = Response.success(loginResponse)
        val actual = Response(Status.SUCCESS, loginResponse, null, -1, null)
        assertThat(result).isEqualTo(actual)
    }

    @Test
    fun response_When_Response_Is_ERROR_Expected_DATA_NUll(){
        val result = Response.error("Offline",null,400,null)
        print(result)
        val actual =  Response(Status.ERROR, null, "Offline", 400, null)
        assertThat(result).isEqualTo(actual)
    }

}