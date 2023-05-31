package com.arria.ping.util

import com.chibatching.kotpref.KotprefModel
import java.util.*

object StorePrefData : KotprefModel() {
    var token by stringPref()
    var refreshToken by stringPref()
    var iDToken by stringPref()

    var isFirstTimeLogin by booleanPref()
    var isTempPassword by intPref(default = -1)
    var isTermsAgree by intPref(default = -1)
    var isConsent by intPref(default = -1)
    var storeTempPassword by stringPref()
    // profile data
    var firstName by stringPref()
    var lastName by stringPref()
    var email by stringPref()
    var role by stringPref()

    // challenge parameter -
    var Session by stringPref()
    var USER_ID_FOR_SRP by stringPref()
    var isStoreSelected by booleanPref()
    var isAreaSelected by booleanPref()
    var isStateSelected by booleanPref()
    var isSupervisorSelected by booleanPref()

    var isStoreSelectedDone by booleanPref()
    var isAreaSelectedDone by booleanPref()
    var isStateSelectedDone by booleanPref()
    var isSupervisorSelectedDone by booleanPref()

    var isPeriodSelected by stringPref()
    var isSelectedPeriod by stringPref()
    var isSelectedDate by stringPref()

    val areaId by stringSetPref {
        return@stringSetPref TreeSet<String>()
    }
    val stateId by stringSetPref {
        return@stringSetPref TreeSet<String>()
    }
    val storeId by stringSetPref {
        return@stringSetPref TreeSet<String>()
    }
    var isAreaChanged by booleanPref()
    var isStateChanged by booleanPref()
    var isSupervisorChanged by booleanPref()
    var isStoreChanged by booleanPref()
    var startDateValue by stringPref()
    var endDateValue by stringPref()
    var isCalendarSelected by booleanPref()

    var isFromAction by booleanPref()

    var isFromCEOPastActionStore by booleanPref()
    var isFromCEOPastActionList by booleanPref()

    var isFromDOPastActionStore by booleanPref()
    var isFromDOPastActionList by booleanPref()

    var isFromSupervisorPastActionStore by booleanPref()
    var isFromSupervisorPastActionList by booleanPref()

    var whichBottomNavigationClicked  by stringPref()
    var isFromBioMetricLoginORPassword by booleanPref()
    var isForGMCheckInTimeFromLogin by booleanPref()

    var isScreenSelect by stringPref()
    var StoreIdFromLogin by stringPref()
    var firebaseDeviceToken by stringPref()
    var filterType by stringPref()
    var isNotificationShown by booleanPref(default = false)
    var isUserAllowedBiometric by booleanPref(default = false)
    var isTouchIDEnabled by booleanPref(default = false)
    var isDeviceHasBiometricFeatures by booleanPref(default = false)
    var isUserBioMetricLoggedIn by booleanPref(default = false)
    var encryptedPassword by stringPref()
    var encryptedIV by stringPref()
    var encryptedSalt by stringPref()
    var isFromWelcomeScreen by booleanPref(default = false)
    var dayOfLastServiceDate by stringPref()
    var filterDate by stringPref()
}

