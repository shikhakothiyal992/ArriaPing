package com.arria.ping.log.data

data class QueryData(
    var areaCodes: List<String>,
    var stateCodes: List<String>,
    var supervisors: List<String>,
    var storeNumbers: List<String>,
    var rangeTo: String,
    var rangeFrom: String,
    var filterType: String,
    var queryName: String
)
