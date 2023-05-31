package com.arria.ping.util

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.arria.ping.R
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.ui.generalview.LoginActivity
import com.arria.ping.ui.generalview.MainActivity
import kotlinx.coroutines.launch

object CommonUtil {
    lateinit var dbHelper: DatabaseHelperImpl
    fun navigateToLogin(requireActivity: MainActivity) {
        deleteAllDB(requireActivity)
        resetStorePreferencesData(requireActivity)

        val intent = Intent(requireActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        requireActivity.startActivity(intent)
        requireActivity.finish()
    }

    fun deleteAllDB(requireActivity: MainActivity) {
        dbHelper = DatabaseHelperImpl(DatabaseBuilder.getInstance(requireActivity))
        requireActivity.lifecycleScope.launch {
            dbHelper.deleteArea()
            dbHelper.deleteAllState()
            dbHelper.deleteAllStore()
            dbHelper.deleteAllSupervisor()
        }
    }

    fun resetStorePreferencesData(requireActivity: MainActivity) {
        StorePrefData.isSupervisorSelected = false
        StorePrefData.isStateSelected = false
        StorePrefData.isAreaSelected = false
        StorePrefData.isStoreSelected = false

        StorePrefData.isSupervisorSelectedDone = false
        StorePrefData.isStateSelectedDone = false
        StorePrefData.isAreaSelectedDone = false
        StorePrefData.isStoreSelectedDone = false

        StorePrefData.isAreaChanged = false
        StorePrefData.isStateChanged = false
        StorePrefData.isStoreChanged = false
        StorePrefData.isSupervisorChanged = false
        StorePrefData.isPeriodSelected = ""
        StorePrefData.isCalendarSelected = false
        StorePrefData.startDateValue = ""
        StorePrefData.endDateValue = ""
        StorePrefData.isFromBioMetricLoginORPassword = false
        StorePrefData.isForGMCheckInTimeFromLogin = false

        StorePrefData.token = ""
        StorePrefData.filterType = ""
        StorePrefData.isSelectedDate = DateFormatterUtil.previousDate()
        StorePrefData.isSelectedPeriod = requireActivity.getString(R.string.yesterday_text)
        StorePrefData.isPeriodSelected = requireActivity.getString(R.string.period_text)
        StorePrefData.isUserBioMetricLoggedIn = false
        StorePrefData.isFromWelcomeScreen = false
        StorePrefData.role = ""
        StorePrefData.refreshToken = ""
        StorePrefData.iDToken = ""
        StorePrefData.dayOfLastServiceDate = ""
        StorePrefData.filterDate = ""

    }
}