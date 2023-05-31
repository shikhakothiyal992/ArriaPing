package  com.arria.ping.model

import com.google.gson.annotations.SerializedName

data class StoreDetailPojo(

        @SerializedName("storeNumber") val storeNumber: String?,
        @SerializedName("storeGoal") val storeGoal: String?,
        @SerializedName("storeVariance") val storeVariance: String?,
        @SerializedName("storeActual") val storeActual: String?,
        @SerializedName("status") val status: String?,

        )