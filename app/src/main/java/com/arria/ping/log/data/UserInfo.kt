package com.arria.ping.log.data

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(val email: String, val role: String)
