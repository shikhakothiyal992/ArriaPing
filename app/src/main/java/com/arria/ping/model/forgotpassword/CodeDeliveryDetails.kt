package  com.arria.ping.model.forgotpassword

import com.google.gson.annotations.SerializedName

data class CodeDeliveryDetails(

        @SerializedName("Destination") val destination: String,
        @SerializedName("DeliveryMedium") val deliveryMedium: String,
        @SerializedName("AttributeName") val attributeName: String
)