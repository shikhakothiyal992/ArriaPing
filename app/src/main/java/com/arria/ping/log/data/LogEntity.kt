package com.arria.ping.log.data

import kotlinx.serialization.Serializable

@Serializable
data class LogEntity(val level:String, val message: String, val context: String, val payloads: Payloads)
