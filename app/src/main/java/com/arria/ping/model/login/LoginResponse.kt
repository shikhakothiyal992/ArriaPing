package  com.arria.ping.model.login

import com.google.gson.annotations.SerializedName

data class LoginResponse(
        @SerializedName("ChallengeName") val challengeName: String?,
        @SerializedName("Session") val session: String?,
        @SerializedName("ChallengeParameters") val challengeParameters: ChallengeParameters,
        @SerializedName("AuthenticationResult") val authenticationResult: AuthenticationResult?
){
    data class ChallengeParameters(
            @SerializedName("USER_ID_FOR_SRP") val userIdForSRP: String?,
            @SerializedName("requiredAttributes") val requiredAttributes: String?,
            @SerializedName("userAttributes") val userAttributes: String?
    )

    data class AuthenticationResult(

            @SerializedName("AccessToken") val accessToken: String?,
            @SerializedName("ExpiresIn") val expiresIn: Int?,
            @SerializedName("TokenType") val tokenType: String?,
            @SerializedName("RefreshToken") val refreshToken: String?,
            @SerializedName("IdToken") val idToken: String?
    )
}