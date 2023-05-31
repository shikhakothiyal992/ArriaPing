package  com.arria.ping.model.successLogin

import com.google.gson.annotations.SerializedName

data class ChallengeParameters(
        @SerializedName("USER_ID_FOR_SRP") val userIdForSRP: String,
        @SerializedName("requiredAttributes") val requiredAttributes: String,
        @SerializedName("userAttributes") val userAttributes: String
)