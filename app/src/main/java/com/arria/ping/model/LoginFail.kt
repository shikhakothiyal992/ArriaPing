package  com.arria.ping.model

import com.google.gson.annotations.SerializedName

data class LoginFail(
        @SerializedName("message") val message: String,
        @SerializedName("code") val code: String,
        @SerializedName("time") val time: String,
        @SerializedName("requestId") val requestId: String,
        @SerializedName("statusCode") val statusCode: Int,
        @SerializedName("retryable") val retryable: Boolean,
        @SerializedName("retryDelay") val retryDelay: Double
)