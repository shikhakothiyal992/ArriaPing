package  com.arria.ping.model

import com.google.gson.annotations.SerializedName

data class Data(

        @SerializedName("Username") val username: String,
        @SerializedName("UserAttributes") val userAttributes: List<UserAttributes>
)