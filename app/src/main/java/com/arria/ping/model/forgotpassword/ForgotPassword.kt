package  com.arria.ping.model.forgotpassword

import com.google.gson.annotations.SerializedName

data class ForgotPassword(
        @SerializedName("CodeDeliveryDetails") val codeDeliveryDetails: CodeDeliveryDetails
)