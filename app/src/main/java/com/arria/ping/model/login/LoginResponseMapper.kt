package com.arria.ping.model.login

fun map(response: LoginResponse): Login {
    val challengeParameters = map(response.challengeParameters)
    val statesData = map(response.authenticationResult)

    return Login(
            challengeName = response.challengeName, session = response.session, challengeParameters,
            statesData
    )
}

fun map(challengeResponse: LoginResponse.ChallengeParameters): Login.ChallengeParametersData {
    return Login.ChallengeParametersData(
            userIdForSRP = challengeResponse.userIdForSRP,
            requiredAttributes = challengeResponse.requiredAttributes,
            userAttributes = challengeResponse.userAttributes
    )
}

fun map(authResponse: LoginResponse.AuthenticationResult?): Login.AuthenticationResultData {
    return Login.AuthenticationResultData(
            accessToken = authResponse?.accessToken,
            expiresIn = authResponse?.expiresIn,
            tokenType = authResponse?.tokenType,
            refreshToken = authResponse?.refreshToken,
            idToken = authResponse?.idToken
    )
}
