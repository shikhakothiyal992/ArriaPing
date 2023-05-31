package  com.arria.ping.model.login

data class Login(
        val challengeName: String?,
        val session: String?,
        val challengeParameters: ChallengeParametersData,
        val authenticationResult: AuthenticationResultData?
) {
    data class ChallengeParametersData(
            val userIdForSRP: String?,
            val requiredAttributes: String?,
            val userAttributes: String?
    )

    data class AuthenticationResultData(
            val accessToken: String?,
            val expiresIn: Int?,
            val tokenType: String?,
            val refreshToken: String?,
            val idToken: String?
    )
}