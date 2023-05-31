package com.arria.ping.ui.filter

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.arria.ping.ui.generalview.MainActivity
import com.arria.ping.R
import com.arria.ping.log.Logger
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.kpi.type.FilterType
import com.arria.ping.ui.actions.ceo.CEOPastActionListActivity
import com.arria.ping.ui.actions.ceo.CEOPastActionStoreListActivity
import com.arria.ping.ui.actions.do_.DOPastActionListActivity
import com.arria.ping.ui.actions.do_.DOPastActionStoreListActivity
import com.arria.ping.ui.actions.supervisor.SupervisorPastActionListActivity
import com.arria.ping.ui.actions.supervisor.SupervisorPastActionStoreListActivity
import com.arria.ping.util.DateFormatterUtil
import com.arria.ping.util.StorePrefData
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.common_header_filter.*
import kotlinx.android.synthetic.main.common_header_filter.view.*
import kotlinx.android.synthetic.main.common_header_filter_initial_screen.view.*
import kotlinx.android.synthetic.main.gm_yesterday_fragment_kpi.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class FilterActivity : AppCompatActivity(), View.OnClickListener {
    var selectedDate = 0
    var apiArgument = ""
    var startDateValueDate = ""
    var endDateValueDate = ""
    var action = ""
    var filterData = ""
    var isAnySelectionChanged = ""

    lateinit var dbHelper: DatabaseHelperImpl
    var hasSomeFilterData :Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        Logger.info("Filter Selection Screen","Filter")
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        initialise()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.period_select -> {
                callPeriodFilterActivity()
                Logger.info("Period Filter Clicked","Filter")
            }
            R.id.btn_apply -> {
                applyButtonHit()
                Logger.info("Apply Button Clicked","Filter")
            }
            R.id.cross_filter_img_layout -> {
                Logger.info("Cancelled Button Clicked","Filter")
                finish()
            }
            R.id.reset_text_layout -> {
                reset_text.setTextColor(getColor(R.color.reset_text_color))
                reset_text.isFocusableInTouchMode = false
                period_selection_parent.visibility = View.GONE
                calendar_custom_range.visibility = View.GONE
                area__select.text = getString(R.string.select_text)
                store_select.text = getString(R.string.select_text)
                supervisor_select.text = getString(R.string.select_text)
                state_select.text = getString(R.string.select_text)
                period_select.text = getString(R.string.yesterday_text)
                StorePrefData.isPeriodSelected = period_select.text.toString()
                StorePrefData.filterType =  FilterType.LAST_SERVICE.rawValue
                Logger.info("Reset Button Clicked","Filter")
                deleteAllDB()

            }
            R.id.store_parent -> {
                Logger.info("Store Filters Clicked","Filter")
                val intent = Intent(this@FilterActivity, StoreFilterActivity::class.java)
                intent.putExtra("action", getString(R.string.store_text))
                startActivity(intent)
                finish()
            }
            R.id.area_parent -> {
                Logger.info("Area Filters Clicked","Filter")
                val intent = Intent(this@FilterActivity, StoreFilterActivity::class.java)
                intent.putExtra("action", getString(R.string.area_text))
                startActivity(intent)
                finish()
            }
            R.id.state_parent -> {
                Logger.info("State Filters Clicked","Filter")
                val intent = Intent(this@FilterActivity, StoreFilterActivity::class.java)
                intent.putExtra("action", getString(R.string.state_text))
                startActivity(intent)
                finish()
            }
            R.id.supervisor_parent -> {
                Logger.info("Supervisor Filters Clicked","Filter")
                val intent = Intent(this@FilterActivity, StoreFilterActivity::class.java)
                intent.putExtra("action", getString(R.string.supervisor_text))
                startActivity(intent)
                finish()
            }
        }
    }

    private fun deleteAllDB() {
        lifecycleScope.launch {
            dbHelper.deleteArea()
            dbHelper.deleteAllState()
            dbHelper.deleteAllStore()
            dbHelper.deleteAllSupervisor()
            StorePrefData.isSupervisorSelected = true
            StorePrefData.isStateSelected = true
            StorePrefData.isAreaSelected = true
            StorePrefData.isStoreSelected = true

            StorePrefData.isSupervisorSelectedDone = false
            StorePrefData.isStateSelectedDone = false
            StorePrefData.isAreaSelectedDone = false
            StorePrefData.isStoreSelectedDone = false

            StorePrefData.isAreaChanged = false
            StorePrefData.isStateChanged = false
            StorePrefData.isStoreChanged = false
            StorePrefData.isSupervisorChanged = false
            StorePrefData.isSelectedDate= DateFormatterUtil.previousDate()
            StorePrefData.isSelectedPeriod=getString(R.string.yesterday_text)
        }
    }

    private fun getDaysAgo() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -selectedDate)
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val formattedDate: String = df.format(calendar.time)
        callMainActivity(formattedDate)
    }

    private fun callMainActivity(formattedDate: String) {
        val intent = Intent(this@FilterActivity, MainActivity::class.java)
        intent.putExtra("filter_range_date", formattedDate)
        intent.putExtra("period_range", StorePrefData.isPeriodSelected)
        intent.putExtra("apiArgument", apiArgument)
        intent.putExtra("startDateValue", startDateValueDate)
        intent.putExtra("endDateValue", endDateValueDate)
        startActivity(intent)
    }

    private fun callActionActivity() {
        StorePrefData.isFromAction = false
        val intent = Intent(this@FilterActivity, MainActivity::class.java)
        startActivity(intent)
    }

    private fun callCEOPastActionStoreListActivity() {
        val intent = Intent(this@FilterActivity, CEOPastActionStoreListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun callCEOPastActionListActivity() {
        val intent = Intent(this@FilterActivity, CEOPastActionListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun callDOPastActionStoreListActivity() {
        val intent = Intent(this@FilterActivity, DOPastActionStoreListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun callDOPastActionListActivity() {
        val intent = Intent(this@FilterActivity, DOPastActionListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun callSupervisorPastActionStoreListActivity() {
        val intent = Intent(this@FilterActivity, SupervisorPastActionStoreListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun callSupervisorPastActionListActivity() {
        val intent = Intent(this@FilterActivity, SupervisorPastActionListActivity::class.java)
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialise() {
        cross_filter_img.setImageResource(R.drawable.ic_icon_close)
        val typeface = resources.getFont(R.font.sf_ui_text_regular)
        calendar_custom_range.setFonts(typeface)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(4, 5, 5, 5)
        cross_filter_img.layoutParams = params

        dbHelper = DatabaseHelperImpl(DatabaseBuilder.getInstance(applicationContext))

        if (intent.hasExtra("action")) {
            action = intent.getStringExtra("action")!!
        }
        if (intent.hasExtra("filter_data")) {
            filterData = intent.getStringExtra("filter_data")!!
        }
        if (intent.hasExtra("isAnySelectionChanged")) {
            isAnySelectionChanged = intent.getStringExtra("isAnySelectionChanged")!!
        }
       
        period_select.setOnClickListener(this)
        common_header_filter.reset_text_layout.setOnClickListener(this)
        btn_apply.setOnClickListener(this)
        common_header_filter.cross_filter_img_layout.setOnClickListener(this)
        store_parent.setOnClickListener(this)
        area_parent.setOnClickListener(this)
        state_parent.setOnClickListener(this)
        supervisor_parent.setOnClickListener(this)

        if (StorePrefData.role == getString(R.string.ceo_text) || StorePrefData.role == getString(R.string.do_text)) {
            if ( StorePrefData.isFromAction) {
                other_filter_parent.visibility = View.VISIBLE
                period_parent.visibility = View.GONE
            } else {
                other_filter_parent.visibility = View.VISIBLE
                period_parent.visibility = View.VISIBLE
            }
        } else if (StorePrefData.role == getString(R.string.supervisor_text)) {
            if ( StorePrefData.isFromAction) {
                area_parent.visibility = View.GONE
                area_view.visibility = View.GONE
                state_parent.visibility = View.GONE
                state_view.visibility = View.GONE
                supervisor_parent.visibility = View.GONE
                supervisor_view.visibility = View.GONE
                other_filter_parent.visibility = View.VISIBLE
                period_parent.visibility = View.GONE
            } else {
                area_parent.visibility = View.GONE
                area_view.visibility = View.GONE
                state_parent.visibility = View.GONE
                state_view.visibility = View.GONE
                supervisor_parent.visibility = View.GONE
                supervisor_view.visibility = View.GONE
                other_filter_parent.visibility = View.VISIBLE
                period_parent.visibility = View.VISIBLE
            }
        } else if (StorePrefData.role == getString(R.string.gm_text)) {
            other_filter_parent.visibility = View.GONE
            period_parent.visibility = View.VISIBLE
        }

        if (StorePrefData.isPeriodSelected.isEmpty() ) {
                period_select.text = getString(R.string.yesterday_text)
                StorePrefData.isPeriodSelected = getString(R.string.yesterday_text)
            } else {
                if(StorePrefData.isSelectedPeriod.isEmpty()){
                    period_select.text = getString(R.string.yesterday_text)
                }else{
                    if(StorePrefData.isSelectedPeriod == "Custom"){
                        period_select.text = StorePrefData.isPeriodSelected
                    }else{
                        period_select.text = StorePrefData.isSelectedPeriod
                    }
                }
                period_select.setTextColor(getColor(R.color.header_color))
            }

        lifecycleScope.launch {
            // area
            if (isAnySelectionChanged == getString(R.string.area_text)) {
                dbHelper.deleteAllState()
                dbHelper.deleteAllStore()
                dbHelper.deleteAllSupervisor()
            }
            if (dbHelper.getAllSelectedAreaList(true).isNotEmpty()) {
                if (StorePrefData.isAreaSelectedDone) {
                    area__select.setTextColor(getColor(R.color.header_color))
                    area__select.text =
                        dbHelper.getAllSelectedAreaName(true).toString().replace("[", "")
                            .replace("]", "")
                    hasSomeFilterData = true
                } else {
                    area__select.text = getString(R.string.select_text)
                    StorePrefData.isAreaSelected=true
                }
            }
            // state
            if (isAnySelectionChanged == getString(R.string.state_text)) {
                dbHelper.deleteAllStore()
                dbHelper.deleteAllSupervisor()
            }
            if (dbHelper.getAllSelectedStoreListState(true).isNotEmpty()) {
                if (StorePrefData.isStateSelectedDone) {
                    state_select.setTextColor(getColor(R.color.header_color))
                    state_select.text =
                        dbHelper.getAllSelectedStateName(true).toString().replace("[", "")
                            .replace("]", "")
                    hasSomeFilterData = true
                } else {
                    state_select.text = getString(R.string.select_text)
                    StorePrefData.isStateSelected=true
                }
            }

            // supervisor
            if (isAnySelectionChanged == getString(R.string.supervisor_text)) {
                dbHelper.deleteAllStore()
            }
            if (dbHelper.getAllSelectedStoreListSupervisor(true).isNotEmpty()) {
                if (StorePrefData.isSupervisorSelectedDone) {
                    supervisor_select.setTextColor(getColor(R.color.header_color))
                    supervisor_select.text =
                        dbHelper.getAllSelectedSuperVisorName(true).toString().replace("[", "")
                            .replace("]", "")
                    hasSomeFilterData = true
                } else {
                    supervisor_select.text = getString(R.string.select_text)
                    StorePrefData.isSupervisorSelected=true
                }
            }
            // store
            if (dbHelper.getAllSelectedStoreList(true).isNotEmpty()) {
                if (StorePrefData.isStoreSelectedDone) {
                    store_select.setTextColor(getColor(R.color.header_color))
                    store_select.text =
                        dbHelper.getAllSelectedStoreList(true).toString().replace("[", "")
                            .replace("]", "")
                    hasSomeFilterData = true
                } else {
                    store_select.text = getString(R.string.select_text)
                    StorePrefData.isStoreSelected=true
                }
            }
            if(hasSomeFilterData || StorePrefData.isPeriodSelected != "Last service"){
                reset_text.setTextColor(getColor(R.color.header_color))
                reset_text.isFocusableInTouchMode = true
            }else{
                reset_text.setTextColor(getColor(R.color.reset_text_color))
                reset_text.isFocusableInTouchMode = false
            }


        }

    }

    private fun applyButtonHit() {
        if (StorePrefData.role == getString(R.string.ceo_text)) {
            if (StorePrefData.isFromCEOPastActionStore) {
                callCEOPastActionStoreListActivity()
            } else if (StorePrefData.isFromCEOPastActionList) {
                callCEOPastActionListActivity()
            } else if ( StorePrefData.isFromAction) {
                callActionActivity()
            } else {
                getDaysAgo()
            }

        } else if (StorePrefData.role == getString(R.string.do_text)) {
            if (StorePrefData.isFromDOPastActionStore) {
                callDOPastActionStoreListActivity()
            } else if (StorePrefData.isFromDOPastActionList) {
                callDOPastActionListActivity()
            } else if ( StorePrefData.isFromAction) {
                callActionActivity()
            } else {
                getDaysAgo()
            }

        } else if (StorePrefData.role == getString(R.string.supervisor_text)) {
            if (StorePrefData.isFromSupervisorPastActionStore) {
                callSupervisorPastActionStoreListActivity()
            } else if (StorePrefData.isFromSupervisorPastActionList) {
                callSupervisorPastActionListActivity()
            } else if ( StorePrefData.isFromAction) {
                callActionActivity()
            } else {
                getDaysAgo()
            }
        } else {
            getDaysAgo()
        }
    }
    private fun callPeriodFilterActivity() {
        val intent = Intent(this, PeriodFilterActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

}