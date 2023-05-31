package com.arria.ping.util

object IpConstants {
    const val MEDIA_TYPE = "application/json"
    const val SUCCESS_STATUS = "success"
    const val NEW_PASSWORD_REQUIRED = "NEW_PASSWORD_REQUIRED"
    const val RESPONSE_CODE_201 = 201
    const val RESPONSE_CODE_200 = 200
    const val CodeMismatchException = "CodeMismatchException"
    const val ExpiredCodeException = "ExpiredCodeException"
    const val Today = "Today"
    const val Yesterday = "Last service"
    const val ThisWeek = "This week"
    const val LastWeek = "Last week"
    const val ThisMonth = "This month"
    const val LastMonth = "Last month"
    const val ThisYear = "This year"
    const val LastYear = "Last year"
    const val Last7Days = "Last 7 days"
    const val Last28Days = "Last 28 days"

    const val date = "date"
    const val rangeFrom = "rangeFrom"
    const val rangeTo = "rangeFrom"
    const val rollingThreeDays = "rollingThreeDays"
    const val rollingTwentyEightDays = "rollingTwentyEightDays"
    const val rollingSevenDays = "rollingSevenDays"
    const val CustomRange = "Custom"

    // period filter params
    const val THIS_WEEK = "THIS_WEEK"
    const val LAST_WEEK = "LAST_WEEK"
    const val THIS_MONTH = "THIS_MONTH"
    const val LAST_MONTH = "LAST_MONTH"
    const val THIS_YEAR = "THIS_YEAR"
    const val LAST_YEAR = "LAST_YEAR"
    const val LAST_3_DAYS = "LAST_3_DAYS"
    const val LAST_7_DAYS = "LAST_7_DAYS"
    const val LAST_28_DAYS = "LAST_28_DAYS"
    const val LAST_SERVICE = "LAST_SERVICE"
    const val CUSTOM_RANGE = "CUSTOM_RANGE"

    const val RETROFIT_CONNECTION_TIMEOUT_SECONDS: Long = 10
    const val HEADER_CONTENT_TYPE = "text/plain"

    const val OFFLINE_ERROR_CODE = 651
    const val ERROR_CODE_400 = 400
    const val ANDROID_KEYSTORE = "AndroidKeyStore"
    const val KEY_NAME = "Ping Analyst"
    const val CODE_MISMATCH = "CodeMismatchException"
    const val CODE_EXPIRED = "ExpiredCodeException"
    const val SESSION_EXPIRED = "NotAuthorizedException"
    const val CODE_LIMIT_EXCEED = "LimitExceededException"
}