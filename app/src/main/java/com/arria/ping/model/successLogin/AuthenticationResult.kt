package  com.arria.ping.model.successLogin

import com.google.gson.annotations.SerializedName

data class AuthenticationResult(

        @SerializedName("AccessToken") val accessToken: String,
        @SerializedName("ExpiresIn") val expiresIn: Int,
        @SerializedName("TokenType") val tokenType: String,
        @SerializedName("RefreshToken") val refreshToken: String,
        @SerializedName("IdToken") val idToken: String
)