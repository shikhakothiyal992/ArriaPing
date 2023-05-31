package com.arria.ping.log.data

import kotlinx.serialization.Serializable

@Serializable
data class Payloads(val userInfo: UserInfo, val queryParams: String?)