package com.arria.ping.ui.generalview

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.arria.ping.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.util.StorePrefData
import kotlinx.coroutines.launch


class SplashActivity : AppCompatActivity() {
    lateinit var dbHelper: DatabaseHelperImpl


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        dbHelper = DatabaseHelperImpl(DatabaseBuilder.getInstance(this))
        launchActivity()


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            val token = task.result
            Log.i("FCM", "token $token")
        })


    }

    private fun launchActivity() {

        Handler(Looper.getMainLooper()).postDelayed({

            when {
                StorePrefData.token.isEmpty() -> {

                    if(StorePrefData.isDeviceHasBiometricFeatures && StorePrefData.isUserBioMetricLoggedIn){
                        startActivity(Intent(this, WelcomeActivity::class.java))
                    }else{
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    this.finish()
                }
                else -> {
                    deleteAllDB()
                    StorePrefData.role = ""
                    StorePrefData.token = ""
                    StorePrefData.iDToken = ""
                    StorePrefData.refreshToken = ""
                    StorePrefData.filterType = ""
                    StorePrefData.isSelectedPeriod = getString(R.string.yesterday_text)
                    StorePrefData.isPeriodSelected=getString(R.string.period_text)
                    StorePrefData.dayOfLastServiceDate = ""
                    StorePrefData.filterDate = ""

                    if(StorePrefData.isDeviceHasBiometricFeatures && StorePrefData.isUserBioMetricLoggedIn){
                        startActivity(Intent(this, WelcomeActivity::class.java))
                    }else{
                        startActivity(Intent(this, LoginActivity::class.java))
                    }


                    this.finish()
                }
            }
        }, 2000)
    }

    private fun deleteAllDB() {
        lifecycleScope.launch {
            dbHelper.deleteArea()
            dbHelper.deleteAllState()
            dbHelper.deleteAllStore()
            dbHelper.deleteAllSupervisor()

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

        }
    }
}