package com.arria.ping.ui.generalview

import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arria.ping.R
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.log.Logger
import com.arria.ping.log.awsCognitoAccessProvider
import com.arria.ping.ui.actions.BaseActionsActivity
import com.arria.ping.ui.bonus.GMBonusKpiFragment
import com.arria.ping.ui.bonus.SupervisorBonusKpiFragment
import com.arria.ping.ui.dashboard.DashboardQuickSiteWebView
import com.arria.ping.ui.kpi.*
import com.arria.ping.ui.kpi.ceo.view.CEOPeriodKpiRangeFragment
import com.arria.ping.ui.kpi.ceo.view.CEOYesterdayKpiFragment
import com.arria.ping.ui.kpi.do_.view.DOPeriodKpiRangeFragment
import com.arria.ping.ui.kpi.do_.view.DOYesterdayKpiFragment
import com.arria.ping.ui.kpi.gm.view.GMPeriodKpiFragment
import com.arria.ping.ui.kpi.gm.view.GMTodayKpiFragment
import com.arria.ping.ui.kpi.gm.view.GMYesterdayKpiFragment
import com.arria.ping.ui.kpi.supervisor.view.SupervisorPeriodRangeKpiFragment
import com.arria.ping.ui.kpi.supervisor.view.SupervisorYesterdayKpiFragment
import com.arria.ping.ui.phones.PhoneFragment
import com.arria.ping.ui.settings.SettingsFragment
import com.arria.ping.util.IpConstants
import com.arria.ping.util.StorePrefData
import com.arria.ping.util.globalexceptionhandler.GlobalExceptionHandler
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_actions.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_period_filter.*
import kotlinx.android.synthetic.main.common_header_for_action.view.*
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    var filterRange = ""
    val gson = Gson()
    lateinit var dbHelper: DatabaseHelperImpl
    var storeNumber = listOf<String>()
    var areaCode = listOf<String>()
    var stateCode = listOf<String>()
    var supervisorNumber = listOf<String>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalExceptionHandler(this)


        setContentView(R.layout.activity_main)

        Logger.info("Dashboard Screen", "Dashboard")
        dbHelper = DatabaseHelperImpl(DatabaseBuilder.getInstance(this))
        if (StorePrefData.isNotificationShown) {
            getFilterData()
        } else {
            showNotificationDialog()
        }

        StorePrefData.isUserBioMetricLoggedIn = StorePrefData.isTouchIDEnabled

        if (StorePrefData.whichBottomNavigationClicked == getString(R.string.title_kpis)) {
            if (StorePrefData.role == getString(R.string.gm_text) || StorePrefData.role == getString(
                        R.string.supervisor_text
                ) || StorePrefData.role == getString(R.string.ceo_text) || StorePrefData.role == getString(
                        R.string.do_text
                )
            ) {
                bottom_navigation_view.menu.getItem(1).isChecked = true
            } else {
                bottom_navigation_view.menu.getItem(0).isChecked = true
            }

        } else if (StorePrefData.whichBottomNavigationClicked == getString(R.string.title_actions)) {
            if (StorePrefData.role == getString(R.string.gm_text) || StorePrefData.role == getString(
                        R.string.supervisor_text
                )
            ) {
                bottom_navigation_view.menu.getItem(2).isChecked = true
            } else {
                bottom_navigation_view.menu.getItem(1).isChecked = true
            }
        } else if (StorePrefData.whichBottomNavigationClicked == getString(R.string.title_phone)) {
            if (StorePrefData.role == getString(R.string.gm_text) || StorePrefData.role == getString(
                        R.string.supervisor_text
                )
            ) {
                bottom_navigation_view.menu.getItem(3).isChecked = true
            } else {
                bottom_navigation_view.menu.getItem(2).isChecked = true
            }
        }
        bottom_navigation_view.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_bonus -> {
                    when (StorePrefData.role) {
                        getString(R.string.gm_text) -> {
                            Logger.info("Bonus Screen Navigation clicked", "Dashboard")
                            loadFragment(GMBonusKpiFragment())
                        }
                        getString(R.string.supervisor_text) -> {
                            Logger.info("Bonus Screen Navigation clicked", "Dashboard")
                            loadFragment(SupervisorBonusKpiFragment())
                        }
                        getString(R.string.do_text) -> {
                            bottom_navigation_view.menu.getItem(0).isVisible = false
                            bottom_navigation_view.menu.getItem(1).isChecked = true
                        }
                        else -> {
                            bottom_navigation_view.menu.getItem(0).isEnabled = false
                            bottom_navigation_view.menu.getItem(0)
                                    .setIcon(R.drawable.bonus_unselected)
                            bottom_navigation_view.menu.getItem(0).isChecked = false
                        }
                    }
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_kpis -> {
                    Logger.info("KPIS Screen Navigation clicked", "Dashboard")
                    StorePrefData.whichBottomNavigationClicked = getString(R.string.title_kpis)
                    setKpiFragment()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_action -> {
                    Logger.info("Actions Screen Navigation clicked", "Dashboard")
                    StorePrefData.whichBottomNavigationClicked = getString(R.string.title_actions)
                    val intent = Intent(this, BaseActionsActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_phone -> {
                    //                    Logger.info("Phones Screen Navigation clicked", "Dashboard")
                    //                    StorePrefData.whichBottomNavigationClicked = getString(R.string.title_phone)
                    //                    setPhoneFragment()
                    Logger.info("QuickSite dashboard Navigation clicked", "Dashboard")
                    StorePrefData.whichBottomNavigationClicked = getString(R.string.title_dashboard)
                    loadFragment(DashboardQuickSiteWebView())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_more -> {
                    Logger.info("Settings Screen Navigation clicked", "Dashboard")
                    StorePrefData.whichBottomNavigationClicked = getString(R.string.title_settings)
                    loadFragment(SettingsFragment())
                    return@setOnNavigationItemSelectedListener true
                }

            }
            false
        }
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(
                        mMessageReceiver,
                        IntentFilter("NotificationData")
                )
        notification_button.setOnClickListener {
            notification_button.visibility = View.GONE
        }
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
                context: Context?,
                intent: Intent
        ) {
            try {
                val message = intent.getStringExtra("Data")
                notification_button.visibility = View.VISIBLE
                notification_button.text = message
                Log.d("receiver", "Got message: $message")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setKpiFragment() {
        lifecycleScope.launch {
            storeNumber = dbHelper.getAllSelectedStoreList(true)
            areaCode = dbHelper.getAllSelectedAreaList(true)
            stateCode = dbHelper.getAllSelectedStoreListState(true)
            supervisorNumber = dbHelper.getAllSelectedStoreListSupervisor(true)

            if (StorePrefData.role == getString(R.string.ceo_text)) {
                if (intent.extras != null && intent.extras!!.getString("filter_range_date") != null && intent.extras!!.getString(
                            "apiArgument"
                    ) != null
                ) {
                    val periodRange = intent.extras!!.getString("period_range")
                    if (periodRange != IpConstants.Yesterday && (areaCode.isNotEmpty() || stateCode.isNotEmpty() || supervisorNumber.isNotEmpty() || storeNumber.isNotEmpty() || StorePrefData.startDateValue.isNotEmpty() || StorePrefData.endDateValue.isNotEmpty() || StorePrefData.filterType.isNotEmpty())) {
                        loadFragment(CEOPeriodKpiRangeFragment())
                    } else if (periodRange == IpConstants.Yesterday) {
                        loadFragment(CEOYesterdayKpiFragment())
                    }
                } else {
                    loadFragment(CEOYesterdayKpiFragment())
                }
            } else if (StorePrefData.role == getString(R.string.gm_text)) {

                callGMAfterLogin()

            } else if (StorePrefData.role == getString(R.string.do_text)) {
                if (intent.extras != null && intent.extras!!.getString("filter_range_date") != null && intent.extras!!.getString(
                            "apiArgument"
                    ) != null
                ) {
                    val periodRange = intent.extras!!.getString("period_range")
                    if (periodRange != IpConstants.Yesterday && (areaCode.isNotEmpty() || stateCode.isNotEmpty() || supervisorNumber.isNotEmpty() || storeNumber.isNotEmpty() || StorePrefData.startDateValue.isNotEmpty() || StorePrefData.endDateValue.isNotEmpty() || StorePrefData.filterType.isNotEmpty())) {
                        loadFragment(DOPeriodKpiRangeFragment())
                    } else if (periodRange == IpConstants.Yesterday) {
                        loadFragment(DOYesterdayKpiFragment())
                    }
                } else {
                    loadFragment(DOYesterdayKpiFragment())
                }

            } else if (StorePrefData.role == getString(R.string.supervisor_text_small)) {
                if (intent.extras != null && intent.extras!!.getString("filter_range_date") != null && intent.extras!!.getString(
                            "apiArgument"
                    ) != null
                ) {
                    val periodRange = intent.extras!!.getString("period_range")
                    if (periodRange != IpConstants.Yesterday && (storeNumber.isNotEmpty() || StorePrefData.startDateValue.isNotEmpty() || StorePrefData.endDateValue.isNotEmpty() || StorePrefData.filterType.isNotEmpty())) {
                        loadFragment(
                                SupervisorPeriodRangeKpiFragment()
                        )
                    } else if (periodRange == IpConstants.Yesterday) {
                        loadFragment(SupervisorYesterdayKpiFragment(false))
                    }

                } else {
                    loadFragment(SupervisorYesterdayKpiFragment(false))
                }
            }
        }
    }


    private fun setPhoneFragment() {
        if (StorePrefData.role == getString(R.string.ceo_text)) {
            if (intent.extras != null && intent.extras!!.getString("filter_range_date") != null && intent.extras!!.getString(
                        "apiArgument"
                ) != null
            ) {
                val periodRange = intent.extras!!.getString("period_range")
                if (periodRange == IpConstants.Yesterday) {
                    loadFragment(PhoneFragment(periodRange))
                } else if (periodRange == IpConstants.Today) {
                    loadFragment(PhoneFragment(periodRange))
                } else if (areaCode.isNotEmpty() || stateCode.isNotEmpty() || supervisorNumber.isNotEmpty() || storeNumber.isNotEmpty() || StorePrefData.startDateValue.isNotEmpty() || StorePrefData.endDateValue.isNotEmpty() || StorePrefData.filterType.isNotEmpty()) {
                    loadFragment(PhoneFragment(IpConstants.rangeFrom))
                }
            } else {
                loadFragment(PhoneFragment(IpConstants.Yesterday))
            }
        } else if (StorePrefData.role == getString(R.string.gm_text)) {
            if (intent.extras != null && intent.extras!!.getString("filter_range_date") != null && intent.extras!!.getString(
                        "apiArgument"
                ) != null
            ) {
                val periodRange1 = intent.extras!!.getString("period_range")
                if (periodRange1 == IpConstants.Yesterday) {
                    loadFragment(PhoneFragment(periodRange1))
                } else if (periodRange1 == IpConstants.Today) {
                    loadFragment(PhoneFragment(periodRange1))
                } else if (areaCode.isNotEmpty() || stateCode.isNotEmpty() || supervisorNumber.isNotEmpty() || storeNumber.isNotEmpty() || StorePrefData.startDateValue.isNotEmpty() || StorePrefData.endDateValue.isNotEmpty() || StorePrefData.filterType.isNotEmpty()) {
                    loadFragment(PhoneFragment(IpConstants.rangeFrom))
                }
            } else {
                loadFragment(PhoneFragment(IpConstants.Yesterday))
            }
        } else if (StorePrefData.role == getString(R.string.do_text)) {
            if (intent.extras != null && intent.extras!!.getString("filter_range_date") != null && intent.extras!!.getString(
                        "apiArgument"
                ) != null
            ) {
                val periodRange2 = intent.extras!!.getString("period_range")
                if (periodRange2 == IpConstants.Yesterday) {
                    loadFragment(PhoneFragment(periodRange2))
                } else if (periodRange2 == IpConstants.Today) {
                    loadFragment(PhoneFragment(periodRange2))
                } else if (areaCode.isNotEmpty() || stateCode.isNotEmpty() || supervisorNumber.isNotEmpty() || storeNumber.isNotEmpty() || StorePrefData.startDateValue.isNotEmpty() || StorePrefData.endDateValue.isNotEmpty() || StorePrefData.filterType.isNotEmpty()) {
                    loadFragment(PhoneFragment(IpConstants.rangeFrom))
                } else {
                    loadFragment(PhoneFragment(IpConstants.rangeFrom))
                }
            } else {
                loadFragment(PhoneFragment(IpConstants.Yesterday))
            }

        } else if (StorePrefData.role == getString(R.string.supervisor_text_small)) {
            if (intent.extras != null && intent.extras!!.getString("filter_range_date") != null && intent.extras!!.getString(
                        "apiArgument"
                ) != null
            ) {
                bottom_navigation_view.menu.getItem(3).isChecked = true
                val periodRange3 = intent.extras!!.getString("period_range")
                if (periodRange3 == IpConstants.Yesterday) {
                    loadFragment(PhoneFragment(periodRange3))
                } else if (periodRange3 == IpConstants.Today) {
                    loadFragment(PhoneFragment(periodRange3))
                } else if (areaCode.isNotEmpty() || stateCode.isNotEmpty() || supervisorNumber.isNotEmpty() || storeNumber.isNotEmpty() || StorePrefData.startDateValue.isNotEmpty() || StorePrefData.endDateValue.isNotEmpty() || StorePrefData.filterType.isNotEmpty()) {
                    loadFragment(PhoneFragment(IpConstants.rangeFrom))
                } else {

                    loadFragment(PhoneFragment(IpConstants.rangeFrom))
                }
            } else {
                bottom_navigation_view.menu.getItem(3).isChecked = true
                loadFragment(PhoneFragment(IpConstants.Yesterday))
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callGMAfterLogin() {
        if (intent.extras != null && intent.extras!!.getString("filter_range_date") != null && intent.extras!!.getString(
                    "apiArgument"
            ) != null
        ) {
            val filterRangeGM = intent.extras!!.getString("filter_range_date")
            val periodRangeGM = intent.extras!!.getString("period_range")
            val apiArgumentGM = intent.extras!!.getString("apiArgument")
            val startDateValueGM = intent.extras!!.getString("startDateValue")
            val endDateValueGM = intent.extras!!.getString("endDateValue")

            when (periodRangeGM) {
                IpConstants.Yesterday -> {
                    loadFragment(GMYesterdayKpiFragment())
                }
                IpConstants.Today -> {
                    loadFragment(GMTodayKpiFragment())
                }
                else -> {
                    loadFragment(
                            GMPeriodKpiFragment(
                                    filterRangeGM!!,
                                    periodRangeGM,
                                    apiArgumentGM!!,
                                    startDateValueGM!!,
                                    endDateValueGM!!
                            )
                    )
                }
            }
        } else {
            StorePrefData.isPeriodSelected = getString(R.string.yesterday_text)
            loadFragment(GMYesterdayKpiFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFilterData() {
        if (StorePrefData.isFromBioMetricLoginORPassword) {
            StorePrefData.isFromBioMetricLoginORPassword = false
            if (StorePrefData.role == getString(R.string.gm_text)) {
                StorePrefData.isPeriodSelected = getString(R.string.today_text)
            } else {
                StorePrefData.isPeriodSelected = getString(R.string.yesterday_text)
            }
        }
        if ((StorePrefData.role == getString(R.string.ceo_text) || StorePrefData.role == getString(R.string.do_text) ||
                    StorePrefData.role == getString(R.string.supervisor_text) || StorePrefData.role.isEmpty() )
        ) {
            bottom_navigation_view.menu.getItem(0).isVisible = true
            bottom_navigation_view.menu.getItem(0).isChecked = false
            bottom_navigation_view.menu.getItem(0).isEnabled = false
            when (StorePrefData.whichBottomNavigationClicked) {
                getString(R.string.title_kpis) -> {
                    bottom_navigation_view.menu.getItem(1).isChecked = true
                    setKpiFragment()
                }
                getString(R.string.title_actions) -> {
                    bottom_navigation_view.menu.getItem(2).isChecked = true
                    val intent = Intent(this, BaseActionsActivity::class.java)
                    startActivity(intent)
                }
                getString(R.string.title_dashboard) -> {
                    bottom_navigation_view.menu.getItem(3).isChecked = true
                    // setPhoneFragment()
                    loadFragment(DashboardQuickSiteWebView())
                }
                getString(R.string.title_settings) -> {
                    bottom_navigation_view.menu.getItem(4).isChecked = true

                    loadFragment(SettingsFragment())
                }
                else -> {
                    bottom_navigation_view.menu.getItem(1).isChecked = true
                    setKpiFragment()
                }
            }
        } else {
            bottom_navigation_view.menu.getItem(0).isVisible = true
            bottom_navigation_view.menu.getItem(0).isChecked = false
            bottom_navigation_view.menu.getItem(0).isEnabled = true
            bottom_navigation_view.menu.getItem(0)
                    .setIcon(R.drawable.ic_bottom_nav_bonus_unselected)
            if (StorePrefData.whichBottomNavigationClicked == getString(R.string.title_kpis)) {
                bottom_navigation_view.menu.getItem(1).isChecked = true
                setKpiFragment()
            } else if (StorePrefData.whichBottomNavigationClicked == getString(R.string.title_actions)) {
                bottom_navigation_view.menu.getItem(2).isChecked = true
                val intent = Intent(this, BaseActionsActivity::class.java)
                startActivity(intent)
            } else if (StorePrefData.whichBottomNavigationClicked == getString(R.string.title_dashboard)) {
                bottom_navigation_view.menu.getItem(3).isChecked = true
                // setPhoneFragment()
                loadFragment(DashboardQuickSiteWebView())
            } else if (StorePrefData.whichBottomNavigationClicked == getString(R.string.title_settings)) {
                bottom_navigation_view.menu.getItem(4).isChecked = true
                loadFragment(SettingsFragment())
            } else if (StorePrefData.whichBottomNavigationClicked == getString(R.string.title_bonus)) {
                bottom_navigation_view.menu.getItem(0).isChecked = true
                bottom_navigation_view.menu.getItem(0).isEnabled = true
                bottom_navigation_view.menu.getItem(0)
                        .setIcon(R.drawable.ic_bottom_nav_bonus_unselected)
                if (StorePrefData.role == getString(R.string.gm_text)) {
                    loadFragment(GMBonusKpiFragment())
                } else if (StorePrefData.role == getString(R.string.supervisor_text)) {
                    loadFragment(SupervisorBonusKpiFragment())
                }
            } else {
                setKpiFragment()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBackPressed() {
        if (StorePrefData.whichBottomNavigationClicked == getString(R.string.title_actions)) {
            StorePrefData.whichBottomNavigationClicked = ""
            setKpiFragment()
        }
    }


    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotificationDialog() {
        StorePrefData.isNotificationShown = true
        val notification = NotificationManagerCompat.from(this)
        val isEnabled = notification.areNotificationsEnabled()
        if (isEnabled) {
            val dialog = Dialog(this)

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.notification_dialog_layout)

            val notificationTitleText = dialog.findViewById(R.id.notification_title_text) as TextView
            val notificationDescriptionText = dialog.findViewById(R.id.notification_description_text) as TextView
            val notificationAllowBtn = dialog.findViewById(R.id.notification_btn_ok_text) as TextView
            val notificationDoNotAllowBtn = dialog.findViewById(R.id.notification_btn_cancel_text) as TextView
            notificationTitleText.text = getString(R.string.notification_title_text)
            notificationDescriptionText.text = getString(R.string.notification_description_text, getString(R.string.app_name))

            notificationDoNotAllowBtn.setOnClickListener {
                val intent = Intent()
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        intent.putExtra(
                                "android.provider.extra.APP_PACKAGE",
                                this@MainActivity.packageName
                        )
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {  //5.0
                        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        intent.putExtra("app_package", this@MainActivity.packageName)
                        intent.putExtra("app_uid", this@MainActivity.applicationInfo.uid)
                        startActivity(intent)
                    }
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT -> {  //4.4
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        intent.data = Uri.parse("package:" + this@MainActivity.packageName)
                    }
                    Build.VERSION.SDK_INT >= 15 -> {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                        intent.data =
                                Uri.fromParts("package", this@MainActivity.packageName, null)
                    }
                }
                startActivity(intent)
                if (StorePrefData.isDeviceHasBiometricFeatures) {
                    showBiometricDialog()
                } else {
                    getFilterData()
                }
                dialog.dismiss()

            }
            notificationAllowBtn.setOnClickListener {
                if (StorePrefData.isDeviceHasBiometricFeatures) {
                    showBiometricDialog()
                } else {
                    getFilterData()
                }

                dialog.dismiss()
            }

            dialog.show()

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showBiometricDialog() {
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.touch_id_dialog_layout)

        val titleText = dialog.findViewById(R.id.touch_title_text) as TextView
        val descriptionText = dialog.findViewById(R.id.touch_description_text) as TextView
        val allowButton = dialog.findViewById(R.id.touch_btn_ok_text) as TextView
        val skipButton = dialog.findViewById(R.id.touch_btn_skip_text) as TextView

        titleText.text = getString(R.string.touch_title_text)
        descriptionText.text = getString(R.string.touch_description_text, getString(R.string.app_name))

        skipButton.setOnClickListener {
            StorePrefData.isUserAllowedBiometric = false
            StorePrefData.isTouchIDEnabled = false
            StorePrefData.isUserBioMetricLoggedIn = false
            getFilterData()
            dialog.dismiss()
        }

        allowButton.setOnClickListener {
            StorePrefData.isUserAllowedBiometric = true
            StorePrefData.isTouchIDEnabled = true
            StorePrefData.isUserBioMetricLoggedIn = true
            getFilterData()
            dialog.dismiss()
        }
        dialog.show()
    }
}