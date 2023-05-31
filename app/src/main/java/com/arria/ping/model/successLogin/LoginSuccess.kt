package  com.arria.ping.model.successLogin

import com.google.gson.annotations.SerializedName

data class LoginSuccess(
        @SerializedName("ChallengeName") val challengeName: String,
        @SerializedName("Session") val session: String,
        @SerializedName("ChallengeParameters") val challengeParameters: ChallengeParameters,
        @SerializedName("AuthenticationResult") val authenticationResult: AuthenticationResult
)