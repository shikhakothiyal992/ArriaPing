package  com.arria.ping.model

import com.google.gson.annotations.SerializedName

data class Profile(

        @SerializedName("data") val data: Data
)