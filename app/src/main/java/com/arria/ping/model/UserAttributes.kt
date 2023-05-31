package  com.arria.ping.model

import com.google.gson.annotations.SerializedName

data class UserAttributes(

        @SerializedName("Name") val name: String,
        @SerializedName("Value") val value: String
)