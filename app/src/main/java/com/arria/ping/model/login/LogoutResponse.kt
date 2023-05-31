package  com.arria.ping.model.login

import com.google.gson.annotations.SerializedName

data class LogoutResponse(
        @SerializedName("message") val message: String,
)
