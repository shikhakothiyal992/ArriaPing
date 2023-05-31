package com.arria.ping.ui.kpi.overview

import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.google.gson.Gson
import com.arria.ping.R
import com.arria.ping.apollo.apolloClient
import com.arria.ping.log.Logger
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.kpi.*
import com.arria.ping.util.NetworkHelper
import com.arria.ping.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_foodkpi.*
import kotlinx.android.synthetic.main.activity_labour_kpi.*
import kotlinx.android.synthetic.main.activity_labour_kpi.level_two_scroll_data_action
import kotlinx.android.synthetic.main.activity_labour_kpi.level_two_scroll_data_action_value
import kotlinx.android.synthetic.main.activity_labour_kpi.parent_data_on_scroll_linear
import kotlinx.android.synthetic.main.activity_labour_kpi.parent_data_on_scroll_view
import kotlinx.android.synthetic.main.data_error_layout.view.*
import kotlinx.android.synthetic.main.expandable_list_store.view.*
import javax.inject.Inject
import com.arria.ping.kpi.gm.StorePeriodRangeKPIQuery
import com.arria.ping.kpi.gm.StoreYesterdayKPIQuery

@AndroidEntryPoint
class LabourKpiActivity : AppCompatActivity() {
    var apiLabourArgumentFromFilter = ""
    private val gsonLabour = Gson()
    private lateinit var dbHelperLabourOverview: DatabaseHelperImpl

    @Inject
    lateinit var networkHelper: NetworkHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_labour_kpi)
        this.setFinishOnTouchOutside(false)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dbHelperLabourOverview =
            DatabaseHelperImpl(DatabaseBuilder.getInstance(this@LabourKpiActivity))
        setLabourData()
        cross_button_labor.setOnClickListener {
            Logger.info("Cancel Button clicked","Labor Overview KPI Screen")
            finish()
        }
    }

    private fun setLabourData() {

        when (StorePrefData.role) {
            getString(R.string.ceo_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiLabourArgumentFromFilter =
                        intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiLabourArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val labourYesterdayDetailCEO = gsonLabour.fromJson(
                            intent.getStringExtra("labour_data"),
                            CEOOverviewRangeQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callLabourOverviewNullApi()
                            rangeOverViewCEO(labourYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val labourYesterdayDetail = gsonLabour.fromJson(
                            intent.getStringExtra("labour_data"),
                            CEOOverviewYesterdayQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callLabourOverviewNullApi()
                            yesterdayViewCEO(labourYesterdayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val labourTodayDetail = gsonLabour.fromJson(
                            intent.getStringExtra("labour_data"),
                            CEOOverviewTodayQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callLabourOverviewNullApi()
                            todayViewCEOLabour(labourTodayDetail)
                        }
                    }
                }
            }
            getString(R.string.do_text) -> {

                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiLabourArgumentFromFilter =
                        intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiLabourArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val labourYesterdayDetailCEO = gsonLabour.fromJson(
                            intent.getStringExtra("labour_data"),
                            DOOverviewRangeQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callLabourOverviewNullApi()
                            rangeOverViewDO(labourYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val labourYesterdayDetail = gsonLabour.fromJson(
                            intent.getStringExtra("labour_data"),
                            DOOverviewYesterdayQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callLabourOverviewNullApi()
                            yesterdayViewDO(labourYesterdayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val labourTodayDetail = gsonLabour.fromJson(
                            intent.getStringExtra("labour_data"),
                            DOOverviewTodayQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callLabourOverviewNullApi()
                            todayViewDOLabour(labourTodayDetail)
                        }
                    }
                }
            }
            getString(R.string.gm_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiLabourArgumentFromFilter =
                        intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiLabourArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val laborData = gsonLabour.fromJson(
                            intent.getStringExtra("labour_data"),
                            StorePeriodRangeKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callLabourOverviewNullApi()
                            rangeView(laborData)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val labourYesterdayDetail = gsonLabour.fromJson(
                            intent.getStringExtra("labour_data"),
                            StoreYesterdayKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callLabourOverviewNullApi()
                            yesterdayView(labourYesterdayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val labourTodayDetail = gsonLabour.fromJson(
                            intent.getStringExtra("labour_data"),
                            StoreTodayKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callLabourOverviewNullApi()
                            todayViewLabour(labourTodayDetail)
                        }
                    }
                }
            }
            getString(R.string.supervisor_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiLabourArgumentFromFilter =
                        intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiLabourArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val labourYesterdayDetailCEO = gsonLabour.fromJson(
                            intent.getStringExtra("labour_data"),
                            SupervisorOverviewRangeQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callLabourOverviewNullApi()
                            rangeViewSupervisor(labourYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val labourYesterdayDetail = gsonLabour.fromJson(
                            intent.getStringExtra("labour_data"),
                            SupervisorOverviewYesterdayQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callLabourOverviewNullApi()
                            yesterdayViewSupervisor(labourYesterdayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val labourTodayDetail = gsonLabour.fromJson(
                            intent.getStringExtra("labour_data"),
                            SupervisorOverviewTodayQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callLabourOverviewNullApi()
                            todayViewSupervisorLabour(labourTodayDetail)
                        }
                    }
                }
            }
        }

    }

    // GM
    private fun todayViewLabour(laborTodayDetail: StoreTodayKPIQuery.GeneralManager) {
        val laborDataTodayView = laborTodayDetail.kpis?.store?.today?.labor

        Logger.info("Labor Today", "Labor Overview KPI")

        labour_display.text = getString(R.string.labour_text)
        breaks_qty_text.text =
            laborDataTodayView?.breaks?.breakQuantity?.displayName
                ?: getString(R.string.breaks_qty_text)
        labor_breaks_dollar_text.text = laborDataTodayView?.breaks?.laborWithBreaks?.displayName
        labor_breaks_percentage_text.text =
            laborDataTodayView?.breaks?.laborWithBreaks?.displayName?.replace(
                "\$",
                getString(R.string.percentage_text)
            )
        labor_breaks_ot_percentage_text.text =
            laborDataTodayView?.breaks?.laborWithoutBreaksAndOT?.displayName?.replace(
                "%",
                getString(R.string.dollar_text)
            )
        labor_breaks_ot_dollar_text.text =
            laborDataTodayView?.breaks?.laborWithoutBreaksAndOT?.displayName
        drive_ot_display.text = laborDataTodayView?.driverOTFull?.displayName
        driver_ot_premium_display.text = laborDataTodayView?.driverOTPremium?.displayName
        // display name below
        if (laborDataTodayView?.laborvsGoal == null || laborDataTodayView.laborvsGoal.displayName.isNullOrEmpty()) {
            ll_labor_display1.visibility = View.GONE
        } else {
            ll_labor_display1.visibility = View.VISIBLE
            labor_display1.text = laborDataTodayView.laborvsGoal.displayName
        }
        if (laborDataTodayView?.salesPerLaborHour == null || laborDataTodayView.salesPerLaborHour.displayName.isNullOrEmpty()) {
            ll_labor_display2.visibility = View.GONE
        } else {
            ll_labor_display2.visibility = View.VISIBLE
            labor_display2.text = laborDataTodayView.salesPerLaborHour.displayName
        }
        if (laborDataTodayView?.laborHours == null || laborDataTodayView.laborHours.displayName.isNullOrEmpty()) {
            ll_labor_display3.visibility = View.GONE
        } else {
            ll_labor_display3.visibility = View.VISIBLE
            labor_display3.text = laborDataTodayView.laborHours.displayName
        }
        if (laborDataTodayView?.laborvsManagerBudget == null || laborDataTodayView.laborvsManagerBudget.displayName.isNullOrEmpty()) {
            ll_labor_display4.visibility = View.GONE
        } else {
            ll_labor_display4.visibility = View.VISIBLE
            labor_display4.text = laborDataTodayView.laborvsManagerBudget.displayName
        }

        if (laborDataTodayView?.staffing?.total == null || laborDataTodayView.staffing.total.displayName.isNullOrEmpty()) {
            ll_staffing.visibility = View.GONE
        } else {
            ll_staffing.visibility = View.VISIBLE
            staffing_display.text = laborDataTodayView.staffing.total.displayName
        }


        if (laborDataTodayView?.staffing?.totalTMCount == null || laborDataTodayView.staffing.totalTMCount.displayName.isNullOrEmpty()) {
            ll_total_tm_count_text.visibility = View.GONE
        } else {
            ll_total_tm_count_text.visibility = View.VISIBLE
            total_tm_count_text.text = laborDataTodayView.staffing.totalTMCount.displayName
        }
        if (laborDataTodayView?.staffing?.insiders == null || laborDataTodayView.staffing.insiders.displayName.isNullOrEmpty()) {
            ll_insider_text.visibility = View.GONE
        } else {
            ll_insider_text.visibility = View.VISIBLE
            insider_text.text = laborDataTodayView.staffing.insiders.displayName
        }
        if (laborDataTodayView?.staffing?.drivers == null || laborDataTodayView.staffing.drivers.displayName.isNullOrEmpty()) {
            ll_drivers_text.visibility = View.GONE
        } else {
            ll_drivers_text.visibility = View.VISIBLE
            drivers_text.text = laborDataTodayView.staffing.drivers.displayName
        }
        if (laborDataTodayView?.staffing?.tmCountLessThan30Hours == null || laborDataTodayView.staffing.tmCountLessThan30Hours.displayName.isNullOrEmpty()) {
            tm_count_less_than_thirty_parent.visibility = View.GONE
        } else {
            tm_count_less_than_thirty_parent.visibility = View.VISIBLE
            tm_count_less_than_thirty_text.text =
                laborDataTodayView.staffing.tmCountLessThan30Hours.displayName
        }
        if (laborDataTodayView?.staffing?.tmCountMoreThan30Hours == null || laborDataTodayView.staffing.tmCountMoreThan30Hours.displayName.isNullOrEmpty()) {
            tm_count_grater_than_thirty_parent.visibility = View.GONE
        } else {
            tm_count_grater_than_thirty_parent.visibility = View.VISIBLE
            tm_count_grater_than_thirty_text.text =
                laborDataTodayView.staffing.tmCountMoreThan30Hours.displayName
        }

        if (laborDataTodayView?.actual?.percentage?.isNaN() == false && laborDataTodayView.status != null) {
            labour_sales.text =
                Validation().dollarFormatting(laborDataTodayView.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                laborDataTodayView.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_sales.setTextColor(getColor(R.color.red))
                }
                laborDataTodayView.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_sales.setTextColor(getColor(R.color.green))
                }
                else -> {
                    labour_sales.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        // scroll detect

        labor_scroll.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 0f
                override fun onScrollChanged() {
                    if (labor_scroll.scrollY > y) {
                        parent_data_on_scroll_linear.visibility = View.VISIBLE
                        parent_data_on_scroll_view.visibility = View.VISIBLE
                        level_two_scroll_data_action.text =
                            laborDataTodayView?.displayName ?: getString(R.string.labour_text)

                        if (laborDataTodayView?.actual?.percentage?.isNaN() == false && laborDataTodayView.status != null) {
                            level_two_scroll_data_action_value.text =
                                Validation().ignoreZeroAfterDecimal(laborDataTodayView.actual.percentage)
                                    .plus(getString(R.string.percentage_text))
                            when {
                                laborDataTodayView.status.toString() == resources.getString(R.string.out_of_range) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.red_circle,
                                        0
                                    )
                                }
                                laborDataTodayView.status.toString() == resources.getString(R.string.under_limit) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.green_circle,
                                        0
                                    )
                                }
                                else -> {
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.black_circle,
                                        0
                                    )
                                }
                            }
                        }
                    }
                    y = labor_scroll.scrollY.toFloat()
                    if (y <= 0) {
                        parent_data_on_scroll_linear.visibility = View.INVISIBLE
                        parent_data_on_scroll_view.visibility = View.GONE
                    }
                }
            })



        labour_goal_value.text =
            if (laborDataTodayView?.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                laborDataTodayView.goal.percentage
            )
                .plus(getString(R.string.percentage_text)) else ""
        labour_variance_value.text =
            if (laborDataTodayView?.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                laborDataTodayView.variance.percentage
            )
                .plus(getString(R.string.percentage_text)) else ""

        showLaborNarrativeData(laborDataTodayView?.narrative.toString())


        // Labor vs goal
        labor_vs_goal_goal.text =
            if (laborDataTodayView?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(laborDataTodayView.laborvsGoal.goal.amount)
            ) else ""

        labor_vs_goal_goal.text =
            if (laborDataTodayView?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(laborDataTodayView.laborvsGoal.goal.amount)
            ) else ""

        labor_vs_goal_variance.text =
            if (laborDataTodayView?.laborvsGoal?.variance?.amount?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().ignoreZeroAfterDecimal(laborDataTodayView.laborvsGoal.variance.amount)
            ) else ""

        if (laborDataTodayView?.laborvsGoal?.actual?.amount?.isNaN() == false && laborDataTodayView.laborvsGoal.status != null) {
            labor_vs_goal_actual.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        laborDataTodayView.laborvsGoal.actual.amount
                    )
                )
            when {
                laborDataTodayView.laborvsGoal.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                }
                laborDataTodayView.laborvsGoal.status.toString() == resources.getString(R.string.under_limit) -> {
                    labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                }
                else -> {
                    labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        // sales per labour

        if (laborDataTodayView?.salesPerLaborHour?.actual?.value?.isNaN() == false && laborDataTodayView.salesPerLaborHour.status != null) {
            sales_labor_vs_goal_actual.text =
                Validation().ignoreZeroAfterDecimal(laborDataTodayView.salesPerLaborHour.actual.value)
            when {
                laborDataTodayView.salesPerLaborHour.status.toString() == resources.getString(R.string.out_of_range) -> {
                    sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    sales_labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                }
                laborDataTodayView.salesPerLaborHour.status.toString() == resources.getString(R.string.under_limit) -> {
                    sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    sales_labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                }
                else -> {
                    sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    sales_labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                }
            }

        }


        // Labor vs management

        labor_vs_mgmt_goal_goal.text =
            if (laborDataTodayView?.laborvsManagerBudget?.goal?.amount?.isNaN() == false) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(laborDataTodayView.laborvsManagerBudget.goal.amount)
            ) else ""
        labor_vs_mgmt_goal_variance.text =
            if (laborDataTodayView?.laborvsManagerBudget?.variance?.amount?.isNaN() == false) getString(
                R.string.dollar_text
            ).plus(
                Validation().ignoreZeroAfterDecimal(laborDataTodayView.laborvsManagerBudget.variance.amount)
            ) else ""

        if (laborDataTodayView?.laborvsManagerBudget?.actual?.amount?.isNaN() == false && laborDataTodayView.laborvsManagerBudget.status != null) {
            labor_vs_mgmt_goal_actual.text = getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(laborDataTodayView.laborvsManagerBudget.actual.amount)
            )
            when {
                laborDataTodayView.laborvsManagerBudget.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.red))
                }
                laborDataTodayView.laborvsManagerBudget.status.toString() == resources.getString(R.string.under_limit) -> {
                    labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.green))

                }
                else -> {
                    labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        // labor hour
        labor_hrs_vs_goal_goal.text =
            Validation().ignoreZeroAfterDecimal(laborDataTodayView?.laborHours?.goal?.value)
        labor_hrs_vs_goal_variance.text =
            Validation().ignoreZeroAfterDecimal(laborDataTodayView?.laborHours?.variance?.value)

        if (laborDataTodayView?.laborHours?.actual?.value?.isNaN() == false && laborDataTodayView.laborHours.status != null) {
            labor_hrs_vs_goal_actual.text =
                Validation().ignoreZeroAfterDecimal(laborDataTodayView.laborHours.actual.value)
            when {
                laborDataTodayView.laborHours.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.red))
                }
                laborDataTodayView.laborHours.status.toString() == resources.getString(R.string.under_limit) -> {
                    labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.green))

                }
                else -> {
                    labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        // breaks sales

        if (laborDataTodayView?.breaks?.total?.actual?.amount?.isNaN() == false && laborDataTodayView.breaks.total.status != null) {
            breaks_percentage.text = getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(laborDataTodayView.breaks.total.actual.amount)
            )
            when {
                laborDataTodayView.breaks.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    breaks_percentage.setTextColor(getColor(R.color.red))
                }
                laborDataTodayView.breaks.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    breaks_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    breaks_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        breaks_qty_percentage.text =
            if (!laborDataTodayView?.breaks?.total!!.actual!!.amount!!.isNaN()) Validation().ignoreZeroAfterDecimal(
                laborDataTodayView.breaks.total.actual!!.amount
            ) else ""

        breaks_break_dollar_percentage.text =
            if (!laborDataTodayView.breaks.laborWithBreaks?.actual!!.amount!!.isNaN()) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(laborDataTodayView.breaks.laborWithBreaks.actual.amount)
            ) else ""

        labor_breaks_percentage.text =
            if (!laborDataTodayView.breaks.laborWithBreaks.actual.percentage!!.isNaN()) Validation().ignoreZeroAfterDecimal(
                laborDataTodayView.breaks.laborWithBreaks.actual.percentage
            )
                .plus(getString(R.string.percentage_text)) else ""


        labor_breaks_ot_dollar.text =
            if (!laborDataTodayView.breaks.laborWithoutBreaksAndOT?.actual!!.percentage!!.isNaN()) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(laborDataTodayView.breaks.laborWithoutBreaksAndOT.actual.percentage)
            ) else ""


        labor_breaks_ot_percentage.text =
            if (!laborDataTodayView.breaks.laborWithoutBreaksAndOT.actual.percentage!!.isNaN()) Validation().ignoreZeroAfterDecimal(
                laborDataTodayView.breaks.laborWithoutBreaksAndOT.actual.percentage
            )
                .plus(getString(R.string.percentage_text)) else ""

        // driver


        if (!laborDataTodayView.driverOTFull!!.actual!!.amount!!.isNaN() && laborDataTodayView.driverOTFull.status != null) {
            driver_ot_percentage.text = getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(laborDataTodayView.driverOTFull.actual!!.amount)
            )
            when {
                laborDataTodayView.driverOTFull.status.toString() == resources.getString(R.string.out_of_range) -> {
                    driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    driver_ot_percentage.setTextColor(getColor(R.color.red))
                }
                laborDataTodayView.driverOTFull.status.toString() == resources.getString(R.string.under_limit) -> {
                    driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    driver_ot_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    driver_ot_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        // driver ot premium
        if (!laborDataTodayView.driverOTPremium!!.actual!!.amount!!.isNaN() && laborDataTodayView.driverOTPremium.status != null) {
            driver_ot_premium_percentage.text = getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(laborDataTodayView.driverOTPremium.actual!!.amount)
            )
            when {
                laborDataTodayView.driverOTPremium.status.toString() == resources.getString(R.string.out_of_range) -> {
                    driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    driver_ot_premium_percentage.setTextColor(getColor(R.color.red))
                }
                laborDataTodayView.driverOTPremium.status.toString() == resources.getString(R.string.under_limit) -> {
                    driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    driver_ot_premium_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    driver_ot_premium_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }


        // staffing
        if (!laborDataTodayView.staffing?.total!!.actual!!.value!!.isNaN() && laborDataTodayView.staffing.total.status != null) {
            staffing_percentage.text =
                Validation().ignoreZeroAfterDecimal(laborDataTodayView.staffing.total.actual!!.value)
            when {
                laborDataTodayView.staffing.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    staffing_percentage.setTextColor(getColor(R.color.red))
                }
                laborDataTodayView.staffing.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    staffing_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    staffing_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        total_tm_count_percentage.text =
            if (!laborDataTodayView.staffing.totalTMCount!!.actual!!.value!!.isNaN()) Validation().ignoreZeroAfterDecimal(
                laborDataTodayView.staffing.totalTMCount.actual!!.value
            ) else ""
        insider_percentage.text =
            if (!laborDataTodayView.staffing.insiders?.actual!!.value!!.isNaN()) Validation().ignoreZeroAfterDecimal(
                laborDataTodayView.staffing.insiders.actual.value
            ) else ""
        drivers_percentage.text =
            if (!laborDataTodayView.staffing.drivers?.actual!!.value!!.isNaN()) Validation().ignoreZeroAfterDecimal(
                laborDataTodayView.staffing.drivers.actual.value
            ) else ""
        tm_count_less_than_thirty_percentage.text =
            if (!laborDataTodayView.staffing.tmCountLessThan30Hours?.actual!!.value!!.isNaN()) Validation().ignoreZeroAfterDecimal(
                laborDataTodayView.staffing.tmCountLessThan30Hours.actual.value
            ) else ""
        tm_count_grater_than_percentage.text =
            if (!laborDataTodayView.staffing.tmCountMoreThan30Hours?.actual!!.value!!.isNaN()) Validation().ignoreZeroAfterDecimal(
                laborDataTodayView.staffing.tmCountMoreThan30Hours.actual.value
            ) else ""

    }

    private fun yesterdayView(laborTodayDetail: StoreYesterdayKPIQuery.GeneralManager) {
        try {
            val laborDataYesterdayView = laborTodayDetail.kpis?.store?.yesterday?.labor

            Logger.info("Labor Yesterday", "Labor Overview KPI")

            // display name
            Validation().checkNullValueToShowView(
                this,
                laborDataYesterdayView?.staffing?.tmCountLessThan30Hours?.displayName,
                tm_count_less_than_thirty_parent
            )
            Validation().checkNullValueToShowView(
                this,
                laborDataYesterdayView?.staffing?.tmCountMoreThan30Hours?.displayName,
                tm_count_grater_than_thirty_parent
            )

            labour_display.text = getString(R.string.labour_text)
            drive_ot_display.text = laborDataYesterdayView?.driverOTFull?.displayName
            driver_ot_premium_display.text = laborDataYesterdayView?.driverOTPremium?.displayName
            // display name below
            if (laborDataYesterdayView?.laborvsGoal == null || laborDataYesterdayView.laborvsGoal.displayName.isNullOrEmpty()) {
                ll_labor_display1.visibility = View.GONE
            } else {
                ll_labor_display1.visibility = View.VISIBLE
                labor_display1.text = laborDataYesterdayView.laborvsGoal.displayName
            }
            if (laborDataYesterdayView?.salesPerLaborHour == null || laborDataYesterdayView.salesPerLaborHour.displayName.isNullOrEmpty()) {
                ll_labor_display2.visibility = View.GONE
            } else {
                ll_labor_display2.visibility = View.VISIBLE
                labor_display2.text = laborDataYesterdayView.salesPerLaborHour.displayName
            }
            if (laborDataYesterdayView?.laborHours == null || laborDataYesterdayView.laborHours.displayName.isNullOrEmpty()) {
                ll_labor_display3.visibility = View.GONE
            } else {
                ll_labor_display3.visibility = View.VISIBLE
                labor_display3.text = laborDataYesterdayView.laborHours.displayName
            }
            if (laborDataYesterdayView?.laborvsManagerBudget == null || laborDataYesterdayView.laborvsManagerBudget.displayName.isNullOrEmpty()) {
                ll_labor_display4.visibility = View.GONE
            } else {
                ll_labor_display4.visibility = View.VISIBLE
                labor_display4.text = laborDataYesterdayView.laborvsManagerBudget.displayName
            }

            if (laborDataYesterdayView?.staffing?.total == null || laborDataYesterdayView.staffing.total.displayName.isNullOrEmpty()) {
                ll_staffing.visibility = View.GONE
            } else {
                ll_staffing.visibility = View.VISIBLE
                staffing_display.text = laborDataYesterdayView.staffing.total.displayName
            }

            if (laborDataYesterdayView?.staffing?.totalTMCount == null || laborDataYesterdayView.staffing.totalTMCount.displayName.isNullOrEmpty()) {
                ll_total_tm_count_text.visibility = View.GONE
            } else {
                ll_total_tm_count_text.visibility = View.VISIBLE
                total_tm_count_text.text = laborDataYesterdayView.staffing.totalTMCount.displayName
            }
            if (laborDataYesterdayView?.staffing?.insiders == null || laborDataYesterdayView.staffing.insiders.displayName.isNullOrEmpty()) {
                ll_insider_text.visibility = View.GONE
            } else {
                ll_insider_text.visibility = View.VISIBLE
                insider_text.text = laborDataYesterdayView.staffing.insiders.displayName
            }
            if (laborDataYesterdayView?.staffing?.drivers == null || laborDataYesterdayView.staffing.drivers.displayName.isNullOrEmpty()) {
                ll_drivers_text.visibility = View.GONE
            } else {
                ll_drivers_text.visibility = View.VISIBLE
                drivers_text.text = laborDataYesterdayView.staffing.drivers.displayName
            }
            if (laborDataYesterdayView?.staffing?.tmCountLessThan30Hours == null || laborDataYesterdayView.staffing.tmCountLessThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_less_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_less_than_thirty_parent.visibility = View.VISIBLE
                tm_count_less_than_thirty_text.text =
                    laborDataYesterdayView.staffing.tmCountLessThan30Hours.displayName
            }
            if (laborDataYesterdayView?.staffing?.tmCountMoreThan30Hours == null || laborDataYesterdayView.staffing.tmCountMoreThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_grater_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_grater_than_thirty_parent.visibility = View.VISIBLE
                tm_count_grater_than_thirty_text.text =
                    laborDataYesterdayView.staffing.tmCountMoreThan30Hours.displayName
            }

            // break display
            break_display.text = laborDataYesterdayView?.breaks?.total?.displayName
            breaks_qty_text.text = laborDataYesterdayView?.breaks?.breakQuantity?.displayName
            labor_breaks_dollar_text.text = laborDataYesterdayView?.breaks?.laborWithBreaks?.displayName
            labor_breaks_percentage_text.text = laborDataYesterdayView?.breaks?.laborWithBreaks?.displayName
            labor_breaks_ot_percentage_text.text = laborDataYesterdayView?.breaks?.laborWithoutBreaksAndOT?.displayName
            labor_breaks_ot_dollar_text.text = laborDataYesterdayView?.breaks?.laborWithoutBreaksAndOT?.displayName
            // end break

            labour_sales.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.actual?.amount,
                laborDataYesterdayView?.actual?.percentage,
                laborDataYesterdayView?.actual?.value
            )

            if (laborDataYesterdayView?.status != null) {
                when {
                    laborDataYesterdayView.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labour_sales.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayView.status.toString() == resources.getString(R.string.under_limit) -> {
                        labour_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        labour_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // scroll detect

            labor_scroll.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (labor_scroll.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE

                            level_two_scroll_data_action.text = getString(R.string.labour_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@LabourKpiActivity,
                                    laborDataYesterdayView?.actual?.amount,
                                    laborDataYesterdayView?.actual?.percentage,
                                    laborDataYesterdayView?.actual?.value
                                )

                            if (laborDataYesterdayView?.status != null) {
                                when {
                                    laborDataYesterdayView.status.toString() == resources.getString(
                                        R.string.out_of_range
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    laborDataYesterdayView.status.toString() == resources.getString(
                                        R.string.under_limit
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    }
                                    else -> {
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.black_circle,
                                            0
                                        )
                                    }
                                }
                            }
                        }
                        y = labor_scroll.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            labour_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.goal?.amount,
                laborDataYesterdayView?.goal?.percentage,
                laborDataYesterdayView?.goal?.value
            )
            labour_variance_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.variance?.amount,
                laborDataYesterdayView?.variance?.percentage,
                laborDataYesterdayView?.variance?.value
            )

            showLaborNarrativeData(laborDataYesterdayView?.narrative.toString())

            // Labor vs goal
            val laborGoalGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.laborvsGoal?.goal?.amount,
                laborDataYesterdayView?.laborvsGoal?.goal?.percentage,
                laborDataYesterdayView?.laborvsGoal?.goal?.value
            )
            val laborVarianceGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.laborvsGoal?.variance?.amount,
                laborDataYesterdayView?.laborvsGoal?.variance?.percentage,
                laborDataYesterdayView?.laborvsGoal?.variance?.value
            )
            val laborActualGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.laborvsGoal?.actual?.amount,
                laborDataYesterdayView?.laborvsGoal?.actual?.percentage,
                laborDataYesterdayView?.laborvsGoal?.actual?.value
            )
            labourGoal(
                laborGoalGMYesterday,
                laborVarianceGMYesterday,
                laborActualGMYesterday
            )


            if (laborDataYesterdayView?.laborvsGoal?.status != null) {
                when {
                    laborDataYesterdayView.laborvsGoal.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayView.laborvsGoal.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // sales per labour
            val laborSalesHoursActualGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.salesPerLaborHour?.actual?.amount,
                laborDataYesterdayView?.salesPerLaborHour?.actual?.percentage,
                laborDataYesterdayView?.salesPerLaborHour?.actual?.value
            )
            labourSalesHrs(laborSalesHoursActualGMYesterday)

//            sales_labor_vs_goal_actual.text =Validation().checkAmountPercentageValue(this,laborDataYesterdayView?.salesPerLaborHour?.actual?.amount,laborDataYesterdayView?.salesPerLaborHour?.actual?.percentage,laborDataYesterdayView?.salesPerLaborHour?.actual?.value)

            if (laborDataYesterdayView?.salesPerLaborHour?.status != null) {
                when {
                    laborDataYesterdayView.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayView.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }

            }


            // Labor vs management
            val laborManagementsGoalGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.laborvsManagerBudget?.goal?.amount,
                laborDataYesterdayView?.laborvsManagerBudget?.goal?.percentage,
                laborDataYesterdayView?.laborvsManagerBudget?.goal?.value
            )
            val laborManagementsVarianceGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.laborvsManagerBudget?.variance?.amount,
                laborDataYesterdayView?.laborvsManagerBudget?.variance?.percentage,
                laborDataYesterdayView?.laborvsManagerBudget?.variance?.value
            )
            val laborManagementsActualGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.laborvsManagerBudget?.actual?.amount,
                laborDataYesterdayView?.laborvsManagerBudget?.actual?.percentage,
                laborDataYesterdayView?.laborvsManagerBudget?.actual?.value
            )
            labourManagement(
                laborManagementsGoalGMYesterday,
                laborManagementsVarianceGMYesterday,
                laborManagementsActualGMYesterday
            )

            if (laborDataYesterdayView?.laborvsManagerBudget?.status != null) {
                when {
                    laborDataYesterdayView.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayView.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // labor hour
            val laborHoursGoalGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.laborHours?.goal?.amount,
                laborDataYesterdayView?.laborHours?.goal?.percentage,
                laborDataYesterdayView?.laborHours?.goal?.value
            )
            val laborHoursVarianceGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.laborHours?.variance?.amount,
                laborDataYesterdayView?.laborHours?.variance?.percentage,
                laborDataYesterdayView?.laborHours?.variance?.value
            )
            val laborHoursActualGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.laborHours?.actual?.amount,
                laborDataYesterdayView?.laborHours?.actual?.percentage,
                laborDataYesterdayView?.laborHours?.actual?.value
            )
            labourHours(
                laborHoursGoalGMYesterday,
                laborHoursVarianceGMYesterday,
                laborHoursActualGMYesterday
            )

            if (laborDataYesterdayView?.laborHours?.status != null) {
                when {
                    laborDataYesterdayView.laborHours.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayView.laborHours.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // breaks sales
            val strBreakPercentageGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.breaks?.total?.actual?.amount,
                laborDataYesterdayView?.breaks?.total?.actual?.percentage,
                laborDataYesterdayView?.breaks?.total?.actual?.value
            )
            if (strBreakPercentageGMYesterday.isEmpty()) {
                breaks_error.visibility = View.VISIBLE
                breaks_percentage.visibility = View.GONE
            } else {
                breaks_percentage.text = strBreakPercentageGMYesterday
            }

//            breaks_percentage.text =Validation().checkAmountPercentageValue(this,laborDataYesterdayView?.breaks?.total?.actual?.amount,laborDataYesterdayView?.breaks?.total?.actual?.percentage,laborDataYesterdayView?.breaks?.total?.actual?.value)

            if (laborDataYesterdayView?.breaks?.total?.status != null) {
                when {
                    laborDataYesterdayView.breaks.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayView.breaks.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            //Missed Break

            val laborMissedBreakQuantityGMYesterday :String = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.breaks?.breakQuantity?.actual?.amount,
                laborDataYesterdayView?.breaks?.breakQuantity?.actual?.percentage,
                laborDataYesterdayView?.breaks?.breakQuantity?.actual?.value
            )

            val laborMissedBreakDollarGMYesterday :String = if(laborDataYesterdayView?.breaks?.laborWithBreaks?.actual?.amount == null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataYesterdayView.breaks.laborWithBreaks.actual.amount)
                )
            }

            val laborMissedBreakPercentageGMYesterday :String = if(laborDataYesterdayView?.breaks?.laborWithBreaks?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataYesterdayView.breaks.laborWithBreaks.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }

            val laborMissedBreakOTDollarGMYesterday :String = if(laborDataYesterdayView?.breaks?.laborWithoutBreaksAndOT?.actual?.amount ==  null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataYesterdayView.breaks.laborWithoutBreaksAndOT.actual.amount)
                )
            }

            val laborMissedBreakOTPercentageGMYesterday :String = if(laborDataYesterdayView?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataYesterdayView.breaks.laborWithoutBreaksAndOT.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }
            laborMissedBreaksData(
                laborMissedBreakQuantityGMYesterday,
                laborMissedBreakDollarGMYesterday,
                laborMissedBreakPercentageGMYesterday,
                laborMissedBreakOTDollarGMYesterday,
                laborMissedBreakOTPercentageGMYesterday
            )

            // driver OT full
            val laborOtGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.driverOTFull?.actual?.amount,
                laborDataYesterdayView?.driverOTFull?.actual?.percentage,
                laborDataYesterdayView?.driverOTFull?.actual?.value
            )
            labourDriverOT(laborOtGMYesterday)

            if (laborDataYesterdayView?.driverOTFull?.status != null) {
                when {
                    laborDataYesterdayView.driverOTFull.status.toString() == resources.getString(R.string.out_of_range) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayView.driverOTFull.status.toString() == resources.getString(R.string.under_limit) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // driver ot premium
            val laborOtPremiumGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.driverOTPremium?.actual?.amount,
                laborDataYesterdayView?.driverOTPremium?.actual?.percentage,
                laborDataYesterdayView?.driverOTPremium?.actual?.value
            )
            labourDriverOTPremium(laborOtPremiumGMYesterday)

            if (laborDataYesterdayView?.driverOTPremium?.status != null) {
                when {
                    laborDataYesterdayView.driverOTPremium.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayView.driverOTPremium.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // staffing

            if (laborDataYesterdayView?.staffing?.total?.status != null) {
                staffing_percentage.text = Validation().ignoreZeroAfterDecimal(laborDataYesterdayView.staffing.total.actual?.value)
                when {
                    laborDataYesterdayView.staffing.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayView.staffing.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val totalTmCountGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.staffing?.totalTMCount?.actual?.amount,
                laborDataYesterdayView?.staffing?.totalTMCount?.actual?.percentage,
                laborDataYesterdayView?.staffing?.totalTMCount?.actual?.value
            )
            val totalInsiderGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.staffing?.insiders?.actual?.amount,
                laborDataYesterdayView?.staffing?.insiders?.actual?.percentage,
                laborDataYesterdayView?.staffing?.insiders?.actual?.value
            )
            val totalDriversGMYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.staffing?.drivers?.actual?.amount,
                laborDataYesterdayView?.staffing?.drivers?.actual?.percentage,
                laborDataYesterdayView?.staffing?.drivers?.actual?.value
            )
            labourStaffing(
                totalTmCountGMYesterday,
                totalInsiderGMYesterday,
                totalDriversGMYesterday
            )

            tm_count_less_than_thirty_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.staffing?.tmCountLessThan30Hours?.actual?.amount,
                laborDataYesterdayView?.staffing?.tmCountLessThan30Hours?.actual?.percentage,
                laborDataYesterdayView?.staffing?.tmCountLessThan30Hours?.actual?.value
            )
            tm_count_grater_than_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayView?.staffing?.tmCountMoreThan30Hours?.actual?.amount,
                laborDataYesterdayView?.staffing?.tmCountMoreThan30Hours?.actual?.percentage,
                laborDataYesterdayView?.staffing?.tmCountMoreThan30Hours?.actual?.value
            )

        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Labor Overview Yesterday KPI")
        }
    }

    private fun rangeView(laborTodayDetail: StorePeriodRangeKPIQuery.GeneralManager) {
        try {
            val laborDataRangeView = laborTodayDetail.kpis?.store?.period?.labor

            Logger.info("Labor Period Range", "Labor Overview KPI")
            // display name
            Validation().checkNullValueToShowView(
                this,
                laborDataRangeView?.staffing?.tmCountLessThan30Hours?.displayName,
                tm_count_less_than_thirty_parent
            )
            Validation().checkNullValueToShowView(
                this,
                laborDataRangeView?.staffing?.tmCountMoreThan30Hours?.displayName,
                tm_count_grater_than_thirty_parent
            )

            labour_display.text = getString(R.string.labour_text)
            drive_ot_display.text = laborDataRangeView?.driverOTFull?.displayName
            driver_ot_premium_display.text = laborDataRangeView?.driverOTPremium?.displayName
            // display name below
            if (laborDataRangeView?.laborvsGoal == null || laborDataRangeView.laborvsGoal.displayName.isNullOrEmpty()) {
                ll_labor_display1.visibility = View.GONE
            } else {
                ll_labor_display1.visibility = View.VISIBLE
                labor_display1.text = laborDataRangeView.laborvsGoal.displayName
            }
            if (laborDataRangeView?.salesPerLaborHour == null || laborDataRangeView.salesPerLaborHour.displayName.isNullOrEmpty()) {
                ll_labor_display2.visibility = View.GONE
            } else {
                ll_labor_display2.visibility = View.VISIBLE
                labor_display2.text = laborDataRangeView.salesPerLaborHour.displayName
            }
            if (laborDataRangeView?.laborHours == null || laborDataRangeView.laborHours.displayName.isNullOrEmpty()) {
                ll_labor_display3.visibility = View.GONE
            } else {
                ll_labor_display3.visibility = View.VISIBLE
                labor_display3.text = laborDataRangeView.laborHours.displayName
            }
            if (laborDataRangeView?.laborvsManagerBudget == null || laborDataRangeView.laborvsManagerBudget.displayName.isNullOrEmpty()) {
                ll_labor_display4.visibility = View.GONE
            } else {
                ll_labor_display4.visibility = View.VISIBLE
                labor_display4.text = laborDataRangeView.laborvsManagerBudget.displayName
            }

            if (laborDataRangeView?.staffing?.total == null || laborDataRangeView.staffing.total.displayName.isNullOrEmpty()) {
                ll_staffing.visibility = View.GONE
            } else {
                ll_staffing.visibility = View.VISIBLE
                staffing_display.text = laborDataRangeView.staffing.total.displayName
            }

            if (laborDataRangeView?.staffing?.totalTMCount == null || laborDataRangeView.staffing.totalTMCount.displayName.isNullOrEmpty()) {
                ll_total_tm_count_text.visibility = View.GONE
            } else {
                ll_total_tm_count_text.visibility = View.VISIBLE
                total_tm_count_text.text = laborDataRangeView.staffing.totalTMCount.displayName
            }
            if (laborDataRangeView?.staffing?.insiders == null || laborDataRangeView.staffing.insiders.displayName.isNullOrEmpty()) {
                ll_insider_text.visibility = View.GONE
            } else {
                ll_insider_text.visibility = View.VISIBLE
                insider_text.text = laborDataRangeView.staffing.insiders.displayName
            }
            if (laborDataRangeView?.staffing?.drivers == null || laborDataRangeView.staffing.drivers.displayName.isNullOrEmpty()) {
                ll_drivers_text.visibility = View.GONE
            } else {
                ll_drivers_text.visibility = View.VISIBLE
                drivers_text.text = laborDataRangeView.staffing.drivers.displayName
            }
            if (laborDataRangeView?.staffing?.tmCountLessThan30Hours == null || laborDataRangeView.staffing.tmCountLessThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_less_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_less_than_thirty_parent.visibility = View.VISIBLE
                tm_count_less_than_thirty_text.text =
                    laborDataRangeView.staffing.tmCountLessThan30Hours.displayName
            }
            if (laborDataRangeView?.staffing?.tmCountMoreThan30Hours == null || laborDataRangeView.staffing.tmCountMoreThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_grater_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_grater_than_thirty_parent.visibility = View.VISIBLE
                tm_count_grater_than_thirty_text.text =
                    laborDataRangeView.staffing.tmCountMoreThan30Hours.displayName
            }

            // break display
            break_display.text = laborDataRangeView?.breaks?.total?.displayName
            breaks_qty_text.text = laborDataRangeView?.breaks?.breakQuantity?.displayName
            labor_breaks_dollar_text.text = laborDataRangeView?.breaks?.laborWithBreaks?.displayName
            labor_breaks_percentage_text.text = laborDataRangeView?.breaks?.laborWithBreaks?.displayName
            labor_breaks_ot_percentage_text.text = laborDataRangeView?.breaks?.laborWithoutBreaksAndOT?.displayName
            labor_breaks_ot_dollar_text.text = laborDataRangeView?.breaks?.laborWithoutBreaksAndOT?.displayName
            // end break

            labour_sales.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.actual?.amount,
                laborDataRangeView?.actual?.percentage,
                laborDataRangeView?.actual?.value
            )

            if (laborDataRangeView?.status != null) {
                when {
                    laborDataRangeView.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labour_sales.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeView.status.toString() == resources.getString(R.string.under_limit) -> {
                        labour_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        labour_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // scroll detect

            labor_scroll.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (labor_scroll.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            /* level_two_scroll_data_action.text =
                                 laborDataRangeView?.displayName ?: getString(R.string.labour_text)*/
                            level_two_scroll_data_action.text = getString(R.string.labour_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@LabourKpiActivity,
                                    laborDataRangeView?.actual?.amount,
                                    laborDataRangeView?.actual?.percentage,
                                    laborDataRangeView?.actual?.value
                                )

                            if (laborDataRangeView?.status != null) {
                                when {
                                    laborDataRangeView.status.toString() == resources.getString(R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    laborDataRangeView.status.toString() == resources.getString(R.string.under_limit) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    }
                                    else -> {
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.black_circle,
                                            0
                                        )
                                    }
                                }
                            }
                        }
                        y = labor_scroll.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            labour_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.goal?.amount,
                laborDataRangeView?.goal?.percentage,
                laborDataRangeView?.goal?.value
            )
            labour_variance_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.variance?.amount,
                laborDataRangeView?.variance?.percentage,
                laborDataRangeView?.variance?.value
            )
            showLaborNarrativeData(laborDataRangeView?.narrative.toString())


            // Labor vs goal
            val laborGoalGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.laborvsGoal?.goal?.amount,
                laborDataRangeView?.laborvsGoal?.goal?.percentage,
                laborDataRangeView?.laborvsGoal?.goal?.value
            )
            val laborVarianceGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.laborvsGoal?.variance?.amount,
                laborDataRangeView?.laborvsGoal?.variance?.percentage,
                laborDataRangeView?.laborvsGoal?.variance?.value
            )
            val laborActualGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.laborvsGoal?.actual?.amount,
                laborDataRangeView?.laborvsGoal?.actual?.percentage,
                laborDataRangeView?.laborvsGoal?.actual?.value
            )
            labourGoal(laborGoalGMRange, laborVarianceGMRange, laborActualGMRange)

            if (laborDataRangeView?.laborvsGoal?.status != null) {
                when {
                    laborDataRangeView.laborvsGoal.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeView.laborvsGoal.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // sales per labour
            val laborSalesHoursActualGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.salesPerLaborHour?.actual?.amount,
                laborDataRangeView?.salesPerLaborHour?.actual?.percentage,
                laborDataRangeView?.salesPerLaborHour?.actual?.value
            )
            labourSalesHrs(laborSalesHoursActualGMRange)

//            sales_labor_vs_goal_actual.text =Validation().checkAmountPercentageValue(this,laborDataRangeView?.salesPerLaborHour?.actual?.amount,laborDataRangeView?.salesPerLaborHour?.actual?.percentage,laborDataRangeView?.salesPerLaborHour?.actual?.value)

            if (laborDataRangeView?.salesPerLaborHour?.status != null) {
                when {
                    laborDataRangeView.salesPerLaborHour.status.toString() == resources.getString(R.string.out_of_range) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeView.salesPerLaborHour.status.toString() == resources.getString(R.string.under_limit) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }

            }


            // Labor vs management
            val laborManagementsGoalGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.laborvsManagerBudget?.goal?.amount,
                laborDataRangeView?.laborvsManagerBudget?.goal?.percentage,
                laborDataRangeView?.laborvsManagerBudget?.goal?.value
            )
            val laborManagementsVarianceGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.laborvsManagerBudget?.variance?.amount,
                laborDataRangeView?.laborvsManagerBudget?.variance?.percentage,
                laborDataRangeView?.laborvsManagerBudget?.variance?.value
            )
            val laborManagementsActualGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.laborvsManagerBudget?.actual?.amount,
                laborDataRangeView?.laborvsManagerBudget?.actual?.percentage,
                laborDataRangeView?.laborvsManagerBudget?.actual?.value
            )
            labourManagement(
                laborManagementsGoalGMRange,
                laborManagementsVarianceGMRange,
                laborManagementsActualGMRange
            )

            if (laborDataRangeView?.laborvsManagerBudget?.status != null) {
                when {
                    laborDataRangeView.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeView.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // labor hour
            val laborHoursGoalGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.laborHours?.goal?.amount,
                laborDataRangeView?.laborHours?.goal?.percentage,
                laborDataRangeView?.laborHours?.goal?.value
            )
            val laborHoursVarianceGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.laborHours?.variance?.amount,
                laborDataRangeView?.laborHours?.variance?.percentage,
                laborDataRangeView?.laborHours?.variance?.value
            )
            val laborHoursActualGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.laborHours?.actual?.amount,
                laborDataRangeView?.laborHours?.actual?.percentage,
                laborDataRangeView?.laborHours?.actual?.value
            )
            labourHours(
                laborHoursGoalGMRange,
                laborHoursVarianceGMRange,
                laborHoursActualGMRange
            )

            if (laborDataRangeView?.laborHours?.status != null) {
                when {
                    laborDataRangeView.laborHours.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeView.laborHours.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // breaks sales
            val breakPercentageGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.breaks?.total?.actual?.amount,
                laborDataRangeView?.breaks?.total?.actual?.percentage,
                laborDataRangeView?.breaks?.total?.actual?.value
            )
            if (breakPercentageGMRange.isEmpty()) {
                breaks_error.visibility = View.VISIBLE
                breaks_percentage.visibility = View.GONE
            } else {
                breaks_percentage.text = breakPercentageGMRange
            }

            if (laborDataRangeView?.breaks?.total?.status != null) {
                when {
                    laborDataRangeView.breaks.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeView.breaks.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            //Missed Break
            val laborMissedBreakQuantityGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.breaks?.breakQuantity?.actual?.amount,
                laborDataRangeView?.breaks?.breakQuantity?.actual?.percentage,
                laborDataRangeView?.breaks?.breakQuantity?.actual?.value
            )

            val laborMissedBreakDollarGMRange :String = if(laborDataRangeView?.breaks?.laborWithBreaks?.actual?.amount == null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataRangeView.breaks.laborWithBreaks.actual.amount)
                )
            }

            val laborMissedBreakPercentageGMRange :String = if(laborDataRangeView?.breaks?.laborWithBreaks?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataRangeView.breaks.laborWithBreaks.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }

            val laborMissedBreakOTDollarGMRange :String = if(laborDataRangeView?.breaks?.laborWithoutBreaksAndOT?.actual?.amount ==  null){
                 ""
            }else{
               getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(laborDataRangeView.breaks.laborWithoutBreaksAndOT.actual.amount)
            )
            }

            val laborMissedBreakOTPercentageGMRange :String = if(laborDataRangeView?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataRangeView.breaks.laborWithoutBreaksAndOT.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }

            laborMissedBreaksData(
                laborMissedBreakQuantityGMRange,
                laborMissedBreakDollarGMRange,
                laborMissedBreakPercentageGMRange,
                laborMissedBreakOTDollarGMRange,
                laborMissedBreakOTPercentageGMRange
            )

            // driver OT full
            val laborOtGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.driverOTFull?.actual?.amount,
                laborDataRangeView?.driverOTFull?.actual?.percentage,
                laborDataRangeView?.driverOTFull?.actual?.value
            )
            labourDriverOT(laborOtGMRange)

            if (laborDataRangeView?.driverOTFull?.status != null) {
                when {
                    laborDataRangeView.driverOTFull.status.toString() == resources.getString(R.string.out_of_range) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeView.driverOTFull.status.toString() == resources.getString(R.string.under_limit) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // driver ot premium
            val laborOtPremiumGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.driverOTPremium?.actual?.amount,
                laborDataRangeView?.driverOTPremium?.actual?.percentage,
                laborDataRangeView?.driverOTPremium?.actual?.value
            )
            labourDriverOTPremium(laborOtPremiumGMRange)

            if (laborDataRangeView?.driverOTPremium?.status != null) {
                when {
                    laborDataRangeView.driverOTPremium.status.toString() == resources.getString(R.string.out_of_range) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeView.driverOTPremium.status.toString() == resources.getString(R.string.under_limit) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // staffing
            staffing_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.staffing?.total?.actual?.amount,
                laborDataRangeView?.staffing?.total?.actual?.percentage,
                laborDataRangeView?.staffing?.total?.actual?.value
            )
            if (laborDataRangeView?.staffing?.total?.status != null && !laborDataRangeView.staffing.total.actual!!.value!!.isNaN()) {
                staffing_percentage.text =
                        Validation().ignoreZeroAfterDecimal(laborDataRangeView.staffing.total.actual.value)
                when {
                    laborDataRangeView.staffing.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeView.staffing.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val totalTmCountGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.staffing?.totalTMCount?.actual?.amount,
                laborDataRangeView?.staffing?.totalTMCount?.actual?.percentage,
                laborDataRangeView?.staffing?.totalTMCount?.actual?.value
            )
            val totalInsiderGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.staffing?.insiders?.actual?.amount,
                laborDataRangeView?.staffing?.insiders?.actual?.percentage,
                laborDataRangeView?.staffing?.insiders?.actual?.value
            )
            val totalDriversGMRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.staffing?.drivers?.actual?.amount,
                laborDataRangeView?.staffing?.drivers?.actual?.percentage,
                laborDataRangeView?.staffing?.drivers?.actual?.value
            )
            labourStaffing(totalTmCountGMRange, totalInsiderGMRange, totalDriversGMRange)
            tm_count_less_than_thirty_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.staffing?.tmCountLessThan30Hours?.actual?.amount,
                laborDataRangeView?.staffing?.tmCountLessThan30Hours?.actual?.percentage,
                laborDataRangeView?.staffing?.tmCountLessThan30Hours?.actual?.value
            )
            tm_count_grater_than_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeView?.staffing?.tmCountMoreThan30Hours?.actual?.amount,
                laborDataRangeView?.staffing?.tmCountMoreThan30Hours?.actual?.percentage,
                laborDataRangeView?.staffing?.tmCountMoreThan30Hours?.actual?.value
            )

        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Labor Overview Period Range KPI")
        }
    }

    // ceo
    private fun todayViewCEOLabour(laborTodayDetail: CEOOverviewTodayQuery.Ceo) {
        try {
            val laborDataTodayViewCEO = laborTodayDetail.kpis?.supervisors?.stores?.today?.labor

            Logger.info("Labor Today", "Labor Overview KPI")

            // display name
            labour_display.text = getString(R.string.labour_text)
            breaks_qty_text.text =
                laborDataTodayViewCEO?.breaks?.breakQuantity?.displayName
                    ?: getString(R.string.breaks_qty_text)
            labor_breaks_dollar_text.text =
                laborDataTodayViewCEO?.breaks?.laborWithBreaks?.displayName
            labor_breaks_percentage_text.text =
                laborDataTodayViewCEO?.breaks?.laborWithBreaks?.displayName
            labor_breaks_ot_percentage_text.text = laborDataTodayViewCEO?.breaks?.laborWithoutBreaksAndOT?.displayName
            labor_breaks_ot_dollar_text.text = laborDataTodayViewCEO?.breaks?.laborWithoutBreaksAndOT?.displayName
            drive_ot_display.text = laborDataTodayViewCEO?.driverOTFull?.displayName
            driver_ot_premium_display.text = laborDataTodayViewCEO?.driverOTPremium?.displayName
            // display name below
              if (laborDataTodayViewCEO?.laborvsGoal == null || laborDataTodayViewCEO.laborvsGoal.displayName.isNullOrEmpty()) {
                ll_labor_display1.visibility = View.GONE
            } else {
                ll_labor_display1.visibility = View.VISIBLE
                labor_display1.text = laborDataTodayViewCEO.laborvsGoal.displayName
            }
            if (laborDataTodayViewCEO?.salesPerLaborHour == null || laborDataTodayViewCEO.salesPerLaborHour.displayName.isNullOrEmpty()) {
                ll_labor_display2.visibility = View.GONE
            } else {
                ll_labor_display2.visibility = View.VISIBLE
                labor_display2.text = laborDataTodayViewCEO.salesPerLaborHour.displayName
            }
            if (laborDataTodayViewCEO?.laborHours == null || laborDataTodayViewCEO.laborHours.displayName.isNullOrEmpty()) {
                ll_labor_display3.visibility = View.GONE
            } else {
                ll_labor_display3.visibility = View.VISIBLE
                labor_display3.text = laborDataTodayViewCEO.laborHours.displayName
            }
            if (laborDataTodayViewCEO?.laborvsManagerBudget == null || laborDataTodayViewCEO.laborvsManagerBudget.displayName.isNullOrEmpty()) {
                ll_labor_display4.visibility = View.GONE
            } else {
                ll_labor_display4.visibility = View.VISIBLE
                labor_display4.text = laborDataTodayViewCEO.laborvsManagerBudget.displayName
            }

            if (laborDataTodayViewCEO?.staffing?.total == null || laborDataTodayViewCEO.staffing.total.displayName.isNullOrEmpty()) {
                ll_staffing.visibility = View.GONE
            } else {
                ll_staffing.visibility = View.VISIBLE
                staffing_display.text = laborDataTodayViewCEO.staffing.total.displayName
            }

            if (laborDataTodayViewCEO?.staffing?.totalTMCount == null || laborDataTodayViewCEO.staffing.totalTMCount.displayName.isNullOrEmpty()) {
                ll_total_tm_count_text.visibility = View.GONE
            } else {
                ll_total_tm_count_text.visibility = View.VISIBLE
                total_tm_count_text.text = laborDataTodayViewCEO.staffing.totalTMCount.displayName
            }
            if (laborDataTodayViewCEO?.staffing?.insiders == null || laborDataTodayViewCEO.staffing.insiders.displayName.isNullOrEmpty()) {
                ll_insider_text.visibility = View.GONE
            } else {
                ll_insider_text.visibility = View.VISIBLE
                insider_text.text = laborDataTodayViewCEO.staffing.insiders.displayName
            }
            if (laborDataTodayViewCEO?.staffing?.drivers == null || laborDataTodayViewCEO.staffing.drivers.displayName.isNullOrEmpty()) {
                ll_drivers_text.visibility = View.GONE
            } else {
                ll_drivers_text.visibility = View.VISIBLE
                drivers_text.text = laborDataTodayViewCEO.staffing.drivers.displayName
            }
            if (laborDataTodayViewCEO?.staffing?.tmCountLessThan30Hours == null || laborDataTodayViewCEO.staffing.tmCountLessThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_less_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_less_than_thirty_parent.visibility = View.VISIBLE
                tm_count_less_than_thirty_text.text =
                    laborDataTodayViewCEO.staffing.tmCountLessThan30Hours.displayName
            }
            if (laborDataTodayViewCEO?.staffing?.tmCountMoreThan30Hours == null || laborDataTodayViewCEO.staffing.tmCountMoreThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_grater_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_grater_than_thirty_parent.visibility = View.VISIBLE
                tm_count_grater_than_thirty_text.text =
                    laborDataTodayViewCEO.staffing.tmCountMoreThan30Hours.displayName
            }

            if (laborDataTodayViewCEO?.actual?.percentage?.isNaN() == false && laborDataTodayViewCEO.status != null) {
                labour_sales.text =
                    getString(R.string.dollar_text).plus(
                        Validation().dollarFormatting(
                            laborDataTodayViewCEO.laborvsGoal?.actual?.amount
                        )
                    )

                when {
                    laborDataTodayViewCEO.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labour_sales.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewCEO.status.toString() == resources.getString(R.string.under_limit) -> {
                        labour_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        labour_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // scroll detect

            labor_scroll.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (labor_scroll.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text =
                                laborDataTodayViewCEO?.displayName
                                    ?: getString(R.string.labour_text)

                            if (laborDataTodayViewCEO?.actual?.percentage?.isNaN() == false && laborDataTodayViewCEO.status != null) {
                                level_two_scroll_data_action_value.text =
                                    Validation().ignoreZeroAfterDecimal(laborDataTodayViewCEO.actual.percentage)
                                        .plus(getString(R.string.percentage_text))
                                when {
                                    laborDataTodayViewCEO.status.toString() == resources.getString(R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    laborDataTodayViewCEO.status.toString() == resources.getString(R.string.under_limit) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    }
                                    else -> {
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.black_circle,
                                            0
                                        )
                                    }
                                }
                            }
                        }
                        y = labor_scroll.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })



            labour_goal_value.text =
                if (laborDataTodayViewCEO?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.laborvsGoal.goal.amount)
                )
                else ""
            labour_variance_value.text =
                if (laborDataTodayViewCEO?.laborvsGoal?.variance?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.laborvsGoal.variance.amount)
                )
                else ""
            showLaborNarrativeData(laborDataTodayViewCEO?.narrative.toString())

            // Labor vs goal
            labor_vs_goal_goal.text =
                if (laborDataTodayViewCEO?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.laborvsGoal.goal.amount)
                ) else ""

            labour_goal_value.text =
                if (laborDataTodayViewCEO?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.laborvsGoal.goal.amount)
                ) else ""
            labor_vs_goal_goal.text =
                if (laborDataTodayViewCEO?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.laborvsGoal.goal.amount)
                ) else ""

            labor_vs_goal_variance.text =
                if (laborDataTodayViewCEO?.laborvsGoal?.variance?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.laborvsGoal.variance.amount)
                ) else ""

            if (laborDataTodayViewCEO?.laborvsGoal?.actual?.amount?.isNaN() == false && laborDataTodayViewCEO.laborvsGoal.status != null) {
                labor_vs_goal_actual.text =
                    getString(R.string.dollar_text).plus(
                        Validation().dollarFormatting(
                            laborDataTodayViewCEO.laborvsGoal.actual.amount
                        )
                    )
                when {
                    laborDataTodayViewCEO.laborvsGoal.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewCEO.laborvsGoal.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // sales per labour

            if (laborDataTodayViewCEO?.salesPerLaborHour?.actual?.value?.isNaN() == false && laborDataTodayViewCEO.salesPerLaborHour.status != null) {
                sales_labor_vs_goal_actual.text =
                    Validation().ignoreZeroAfterDecimal(laborDataTodayViewCEO.salesPerLaborHour.actual.value)
                when {
                    laborDataTodayViewCEO.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewCEO.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }

            }


            // Labor vs management

            labor_vs_mgmt_goal_goal.text =
                if (laborDataTodayViewCEO?.laborvsManagerBudget?.goal?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.laborvsManagerBudget.goal.amount)
                ) else ""
            labor_vs_mgmt_goal_variance.text =
                if (laborDataTodayViewCEO?.laborvsManagerBudget?.variance?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.laborvsManagerBudget.variance.amount)
                ) else ""

            if (laborDataTodayViewCEO?.laborvsManagerBudget?.actual?.amount?.isNaN() == false && laborDataTodayViewCEO.laborvsManagerBudget.status != null) {
                labor_vs_mgmt_goal_actual.text = getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.laborvsManagerBudget.actual.amount)
                )
                when {
                    laborDataTodayViewCEO.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewCEO.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // labor hour
            labor_hrs_vs_goal_goal.text =
                Validation().ignoreZeroAfterDecimal(laborDataTodayViewCEO?.laborHours?.goal?.value)
            labor_hrs_vs_goal_variance.text =
                Validation().ignoreZeroAfterDecimal(laborDataTodayViewCEO?.laborHours?.variance?.value)

            if (laborDataTodayViewCEO?.laborHours?.actual?.value?.isNaN() == false && laborDataTodayViewCEO.laborHours.status != null) {
                labor_hrs_vs_goal_actual.text =
                    Validation().ignoreZeroAfterDecimal(laborDataTodayViewCEO.laborHours.actual.value)
                when {
                    laborDataTodayViewCEO.laborHours.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewCEO.laborHours.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // breaks sales

            if (laborDataTodayViewCEO?.breaks?.total?.actual?.amount?.isNaN() == false && laborDataTodayViewCEO.breaks.total.status != null) {
                breaks_percentage.text = getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.breaks.total.actual.amount)
                )
                when {
                    laborDataTodayViewCEO.breaks.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewCEO.breaks.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            breaks_qty_percentage.text = Validation().ignoreZeroAfterDecimal(
                laborDataTodayViewCEO?.breaks?.total?.actual?.amount
            )

            breaks_break_dollar_percentage.text =
                if (laborDataTodayViewCEO?.breaks?.laborWithBreaks?.actual?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.breaks.laborWithBreaks.actual.amount)
                ) else ""

            labor_breaks_percentage.text =
                if (laborDataTodayViewCEO?.breaks?.laborWithBreaks?.actual?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewCEO.breaks.laborWithBreaks.actual.percentage
                )
                    .plus(getString(R.string.percentage_text)) else ""


            labor_breaks_ot_dollar.text =
                if (laborDataTodayViewCEO?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.breaks.laborWithoutBreaksAndOT.actual.percentage)
                ) else ""


            labor_breaks_ot_percentage.text =
                if (laborDataTodayViewCEO?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewCEO.breaks.laborWithoutBreaksAndOT.actual.percentage
                )
                    .plus(getString(R.string.percentage_text)) else ""

            // driver


            if (laborDataTodayViewCEO?.driverOTFull?.actual?.amount?.isNaN() == false && laborDataTodayViewCEO.driverOTFull.status != null) {
                driver_ot_percentage.text = getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.driverOTFull.actual.amount)
                )
                when {
                    laborDataTodayViewCEO.driverOTFull.status.toString() == resources.getString(R.string.out_of_range) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewCEO.driverOTFull.status.toString() == resources.getString(R.string.under_limit) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // driver ot premium
            if (laborDataTodayViewCEO?.driverOTPremium?.actual?.amount?.isNaN() == false && laborDataTodayViewCEO.driverOTPremium.status != null) {
                driver_ot_premium_percentage.text = getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewCEO.driverOTPremium.actual.amount)
                )
                when {
                    laborDataTodayViewCEO.driverOTPremium.status.toString() == resources.getString(R.string.out_of_range) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewCEO.driverOTPremium.status.toString() == resources.getString(R.string.under_limit) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // staffing
            if (laborDataTodayViewCEO?.staffing?.total != null) {
                if (laborDataTodayViewCEO.staffing.total.actual?.value?.isNaN() == false && laborDataTodayViewCEO.staffing.total.status != null) {
                    staffing_percentage.text =
                        Validation().ignoreZeroAfterDecimal(laborDataTodayViewCEO.staffing.total.actual.value)
                    when {
                        laborDataTodayViewCEO.staffing.total.status.toString() == resources.getString(
                            R.string.out_of_range
                        ) -> {
                            staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.red_circle,
                                0
                            )
                            staffing_percentage.setTextColor(getColor(R.color.red))
                        }
                        laborDataTodayViewCEO.staffing.total.status.toString() == resources.getString(
                            R.string.under_limit
                        ) -> {
                            staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.green_circle,
                                0
                            )
                            staffing_percentage.setTextColor(getColor(R.color.green))

                        }
                        else -> {
                            staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.black_circle,
                                0
                            )
                            staffing_percentage.setTextColor(getColor(R.color.text_color))

                        }
                    }
                }
            }

            total_tm_count_percentage.text =
                if (laborDataTodayViewCEO?.staffing?.totalTMCount?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewCEO.staffing.totalTMCount.actual.value
                ) else ""
            insider_percentage.text =
                if (laborDataTodayViewCEO?.staffing?.insiders?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewCEO.staffing.insiders.actual.value
                ) else ""
            drivers_percentage.text =
                if (laborDataTodayViewCEO?.staffing?.drivers?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewCEO.staffing.drivers.actual.value
                ) else ""
            tm_count_less_than_thirty_percentage.text =
                if (laborDataTodayViewCEO?.staffing?.tmCountLessThan30Hours?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewCEO.staffing.tmCountLessThan30Hours.actual.value
                ) else ""
            tm_count_grater_than_percentage.text =
                if (laborDataTodayViewCEO?.staffing?.tmCountMoreThan30Hours?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewCEO.staffing.tmCountMoreThan30Hours.actual.value
                ) else ""
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun yesterdayViewCEO(laborTodayDetail: CEOOverviewYesterdayQuery.Ceo) {
        try {
            val laborDataYesterdayViewCEO =
                laborTodayDetail.kpis?.supervisors?.stores?.yesterday?.labor

            Logger.info("Labor Yesterday", "Labor Overview KPI")

            // display name
            // display name
            Validation().checkNullValueToShowView(
                this,
                laborDataYesterdayViewCEO?.staffing?.tmCountLessThan30Hours?.displayName,
                tm_count_less_than_thirty_parent
            )
            Validation().checkNullValueToShowView(
                this,
                laborDataYesterdayViewCEO?.staffing?.tmCountMoreThan30Hours?.displayName,
                tm_count_grater_than_thirty_parent
            )

            labour_display.text = getString(R.string.labour_text)
            drive_ot_display.text = laborDataYesterdayViewCEO?.driverOTFull?.displayName
            driver_ot_premium_display.text = laborDataYesterdayViewCEO?.driverOTPremium?.displayName
            // display name below
            if (laborDataYesterdayViewCEO?.laborvsGoal == null || laborDataYesterdayViewCEO.laborvsGoal.displayName.isNullOrEmpty()) {
                ll_labor_display1.visibility = View.GONE
            } else {
                ll_labor_display1.visibility = View.VISIBLE
                labor_display1.text = laborDataYesterdayViewCEO.laborvsGoal.displayName
            }
            if (laborDataYesterdayViewCEO?.salesPerLaborHour == null || laborDataYesterdayViewCEO.salesPerLaborHour.displayName.isNullOrEmpty()) {
                ll_labor_display2.visibility = View.GONE
            } else {
                ll_labor_display2.visibility = View.VISIBLE
                labor_display2.text = laborDataYesterdayViewCEO.salesPerLaborHour.displayName
            }
            if (laborDataYesterdayViewCEO?.laborHours == null || laborDataYesterdayViewCEO.laborHours.displayName.isNullOrEmpty()) {
                ll_labor_display3.visibility = View.GONE
            } else {
                ll_labor_display3.visibility = View.VISIBLE
                labor_display3.text = laborDataYesterdayViewCEO.laborHours.displayName
            }
            if (laborDataYesterdayViewCEO?.laborvsManagerBudget == null || laborDataYesterdayViewCEO.laborvsManagerBudget.displayName.isNullOrEmpty()) {
                ll_labor_display4.visibility = View.GONE
            } else {
                ll_labor_display4.visibility = View.VISIBLE
                labor_display4.text = laborDataYesterdayViewCEO.laborvsManagerBudget.displayName
            }

            if (laborDataYesterdayViewCEO?.staffing?.total == null || laborDataYesterdayViewCEO.staffing.total.displayName.isNullOrEmpty()) {
                ll_staffing.visibility = View.GONE
            } else {
                ll_staffing.visibility = View.VISIBLE
                staffing_display.text = laborDataYesterdayViewCEO.staffing.total.displayName
            }

            if (laborDataYesterdayViewCEO?.staffing?.totalTMCount == null || laborDataYesterdayViewCEO.staffing.totalTMCount.displayName.isNullOrEmpty()) {
                ll_total_tm_count_text.visibility = View.GONE
            } else {
                ll_total_tm_count_text.visibility = View.VISIBLE
                total_tm_count_text.text = laborDataYesterdayViewCEO.staffing.totalTMCount.displayName
            }
            if (laborDataYesterdayViewCEO?.staffing?.insiders == null || laborDataYesterdayViewCEO.staffing.insiders.displayName.isNullOrEmpty()) {
                ll_insider_text.visibility = View.GONE
            } else {
                ll_insider_text.visibility = View.VISIBLE
                insider_text.text = laborDataYesterdayViewCEO.staffing.insiders.displayName
            }
            if (laborDataYesterdayViewCEO?.staffing?.drivers == null || laborDataYesterdayViewCEO.staffing.drivers.displayName.isNullOrEmpty()) {
                ll_drivers_text.visibility = View.GONE
            } else {
                ll_drivers_text.visibility = View.VISIBLE
                drivers_text.text = laborDataYesterdayViewCEO.staffing.drivers.displayName
            }
            if (laborDataYesterdayViewCEO?.staffing?.tmCountLessThan30Hours == null || laborDataYesterdayViewCEO.staffing.tmCountLessThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_less_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_less_than_thirty_parent.visibility = View.VISIBLE
                tm_count_less_than_thirty_text.text =
                    laborDataYesterdayViewCEO.staffing.tmCountLessThan30Hours.displayName
            }
            if (laborDataYesterdayViewCEO?.staffing?.tmCountMoreThan30Hours == null || laborDataYesterdayViewCEO.staffing.tmCountMoreThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_grater_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_grater_than_thirty_parent.visibility = View.VISIBLE
                tm_count_grater_than_thirty_text.text =
                    laborDataYesterdayViewCEO.staffing.tmCountMoreThan30Hours.displayName
            }

            // break display
            break_display.text = laborDataYesterdayViewCEO?.breaks?.total?.displayName
            breaks_qty_text.text = laborDataYesterdayViewCEO?.breaks?.breakQuantity?.displayName
            labor_breaks_dollar_text.text = laborDataYesterdayViewCEO?.breaks?.laborWithBreaks?.displayName
            labor_breaks_percentage_text.text = laborDataYesterdayViewCEO?.breaks?.laborWithBreaks?.displayName
            labor_breaks_ot_percentage_text.text = laborDataYesterdayViewCEO?.breaks?.laborWithoutBreaksAndOT?.displayName
            labor_breaks_ot_dollar_text.text = laborDataYesterdayViewCEO?.breaks?.laborWithoutBreaksAndOT?.displayName
            // end break

            labour_sales.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.actual?.amount,
                laborDataYesterdayViewCEO?.actual?.percentage,
                laborDataYesterdayViewCEO?.actual?.value
            )

            if (laborDataYesterdayViewCEO?.status != null) {
                when {
                    laborDataYesterdayViewCEO.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labour_sales.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewCEO.status.toString() == resources.getString(R.string.under_limit) -> {
                        labour_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        labour_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // scroll detect

            labor_scroll.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (labor_scroll.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            /* level_two_scroll_data_action.text =
                                 laborDataYesterdayViewCEO?.displayName ?: getString(R.string.labour_text)*/
                            level_two_scroll_data_action.text = getString(R.string.labour_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@LabourKpiActivity,
                                    laborDataYesterdayViewCEO?.actual?.amount,
                                    laborDataYesterdayViewCEO?.actual?.percentage,
                                    laborDataYesterdayViewCEO?.actual?.value
                                )

                            if (laborDataYesterdayViewCEO?.status != null) {
                                when {
                                    laborDataYesterdayViewCEO.status.toString() == resources.getString(
                                        R.string.out_of_range
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    laborDataYesterdayViewCEO.status.toString() == resources.getString(
                                        R.string.under_limit
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    }
                                    else -> {
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.black_circle,
                                            0
                                        )
                                    }
                                }
                            }
                        }
                        y = labor_scroll.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            labour_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.goal?.amount,
                laborDataYesterdayViewCEO?.goal?.percentage,
                laborDataYesterdayViewCEO?.goal?.value
            )
            labour_variance_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.variance?.amount,
                laborDataYesterdayViewCEO?.variance?.percentage,
                laborDataYesterdayViewCEO?.variance?.value
            )
            showLaborNarrativeData(laborDataYesterdayViewCEO?.narrative.toString())


            // Labor vs goal
            val laborGoalCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.laborvsGoal?.goal?.amount,
                laborDataYesterdayViewCEO?.laborvsGoal?.goal?.percentage,
                laborDataYesterdayViewCEO?.laborvsGoal?.goal?.value
            )
            val laborVarianceCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.laborvsGoal?.variance?.amount,
                laborDataYesterdayViewCEO?.laborvsGoal?.variance?.percentage,
                laborDataYesterdayViewCEO?.laborvsGoal?.variance?.value
            )
            val laborActualCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.laborvsGoal?.actual?.amount,
                laborDataYesterdayViewCEO?.laborvsGoal?.actual?.percentage,
                laborDataYesterdayViewCEO?.laborvsGoal?.actual?.value
            )
            labourGoal(
                laborGoalCEOYesterday,
                laborVarianceCEOYesterday,
                laborActualCEOYesterday
            )

            if (laborDataYesterdayViewCEO?.laborvsGoal?.status != null) {
                when {
                    laborDataYesterdayViewCEO.laborvsGoal.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewCEO.laborvsGoal.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // sales per labour
            val laborSalesHoursActualCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.salesPerLaborHour?.actual?.amount,
                laborDataYesterdayViewCEO?.salesPerLaborHour?.actual?.percentage,
                laborDataYesterdayViewCEO?.salesPerLaborHour?.actual?.value
            )
            labourSalesHrs(laborSalesHoursActualCEOYesterday)

            if (laborDataYesterdayViewCEO?.salesPerLaborHour?.status != null) {
                when {
                    laborDataYesterdayViewCEO.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewCEO.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }

            }


            // Labor vs management
            val laborManagementsGoalCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.laborvsManagerBudget?.goal?.amount,
                laborDataYesterdayViewCEO?.laborvsManagerBudget?.goal?.percentage,
                laborDataYesterdayViewCEO?.laborvsManagerBudget?.goal?.value
            )
            val laborManagementsVarianceCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.laborvsManagerBudget?.variance?.amount,
                laborDataYesterdayViewCEO?.laborvsManagerBudget?.variance?.percentage,
                laborDataYesterdayViewCEO?.laborvsManagerBudget?.variance?.value
            )
            val laborManagementsActualCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.laborvsManagerBudget?.actual?.amount,
                laborDataYesterdayViewCEO?.laborvsManagerBudget?.actual?.percentage,
                laborDataYesterdayViewCEO?.laborvsManagerBudget?.actual?.value
            )
            labourManagement(
                laborManagementsGoalCEOYesterday,
                laborManagementsVarianceCEOYesterday,
                laborManagementsActualCEOYesterday
            )

            if (laborDataYesterdayViewCEO?.laborvsManagerBudget?.status != null) {
                when {
                    laborDataYesterdayViewCEO.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewCEO.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // labor hour
            val laborHoursGoalCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.laborHours?.goal?.amount,
                laborDataYesterdayViewCEO?.laborHours?.goal?.percentage,
                laborDataYesterdayViewCEO?.laborHours?.goal?.value
            )
            val laborHoursVarianceCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.laborHours?.variance?.amount,
                laborDataYesterdayViewCEO?.laborHours?.variance?.percentage,
                laborDataYesterdayViewCEO?.laborHours?.variance?.value
            )
            val laborHoursActualCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.laborHours?.actual?.amount,
                laborDataYesterdayViewCEO?.laborHours?.actual?.percentage,
                laborDataYesterdayViewCEO?.laborHours?.actual?.value
            )
            labourHours(
                laborHoursGoalCEOYesterday,
                laborHoursVarianceCEOYesterday,
                laborHoursActualCEOYesterday
            )

            if (laborDataYesterdayViewCEO?.laborHours?.status != null) {
                when {
                    laborDataYesterdayViewCEO.laborHours.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewCEO.laborHours.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // breaks sales
            val breakPercentageCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.breaks?.total?.actual?.amount,
                laborDataYesterdayViewCEO?.breaks?.total?.actual?.percentage,
                laborDataYesterdayViewCEO?.breaks?.total?.actual?.value
            )
            if (breakPercentageCEOYesterday.isEmpty()) {
                breaks_error.visibility = View.VISIBLE
                breaks_percentage.visibility = View.GONE
            } else {
                breaks_percentage.text = breakPercentageCEOYesterday
            }

            if (laborDataYesterdayViewCEO?.breaks?.total?.status != null) {
                when {
                    laborDataYesterdayViewCEO.breaks.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewCEO.breaks.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            //Missed Break
            val laborMissedBreakQuantityCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.breaks?.breakQuantity?.actual?.amount,
                laborDataYesterdayViewCEO?.breaks?.breakQuantity?.actual?.percentage,
                laborDataYesterdayViewCEO?.breaks?.breakQuantity?.actual?.value
            )


            val laborMissedBreakDollarCEOYesterday :String = if(laborDataYesterdayViewCEO?.breaks?.laborWithBreaks?.actual?.amount == null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataYesterdayViewCEO.breaks.laborWithBreaks.actual.amount)
                )
            }

            val laborMissedBreakPercentageCEOYesterday :String= if(laborDataYesterdayViewCEO?.breaks?.laborWithBreaks?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataYesterdayViewCEO.breaks.laborWithBreaks.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }

            val laborMissedBreakOTDollarCEOYesterday :String = if(laborDataYesterdayViewCEO?.breaks?.laborWithoutBreaksAndOT?.actual?.amount ==  null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataYesterdayViewCEO.breaks.laborWithoutBreaksAndOT.actual.amount)
                )
            }

            val laborMissedBreakOTPercentageCEOYesterday :String = if(laborDataYesterdayViewCEO?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataYesterdayViewCEO.breaks.laborWithoutBreaksAndOT.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }

            laborMissedBreaksData(
                laborMissedBreakQuantityCEOYesterday,
                laborMissedBreakDollarCEOYesterday,
                laborMissedBreakPercentageCEOYesterday,
                laborMissedBreakOTDollarCEOYesterday,
                laborMissedBreakOTPercentageCEOYesterday
            )

            // driver OT full
            val laborOtCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.driverOTFull?.actual?.amount,
                laborDataYesterdayViewCEO?.driverOTFull?.actual?.percentage,
                laborDataYesterdayViewCEO?.driverOTFull?.actual?.value
            )
            labourDriverOT(laborOtCEOYesterday)


            if (laborDataYesterdayViewCEO?.driverOTFull?.status != null) {
                when {
                    laborDataYesterdayViewCEO.driverOTFull.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewCEO.driverOTFull.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // driver ot premium
            val laborOtPremiumCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.driverOTPremium?.actual?.amount,
                laborDataYesterdayViewCEO?.driverOTPremium?.actual?.percentage,
                laborDataYesterdayViewCEO?.driverOTPremium?.actual?.value
            )
            labourDriverOTPremium(laborOtPremiumCEOYesterday)

            if (laborDataYesterdayViewCEO?.driverOTPremium?.status != null) {
                when {
                    laborDataYesterdayViewCEO.driverOTPremium.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewCEO.driverOTPremium.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // staffing
            staffing_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.staffing?.total?.actual?.amount,
                laborDataYesterdayViewCEO?.staffing?.total?.actual?.percentage,
                laborDataYesterdayViewCEO?.staffing?.total?.actual?.value
            )
            if (laborDataYesterdayViewCEO?.staffing?.total?.status != null) {
                staffing_percentage.text =
                        Validation().ignoreZeroAfterDecimal(laborDataYesterdayViewCEO.staffing.total.actual?.value)
                when {
                    laborDataYesterdayViewCEO.staffing.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewCEO.staffing.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val totalTmCountCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.staffing?.totalTMCount?.actual?.amount,
                laborDataYesterdayViewCEO?.staffing?.totalTMCount?.actual?.percentage,
                laborDataYesterdayViewCEO?.staffing?.totalTMCount?.actual?.value
            )
            val totalInsiderCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.staffing?.insiders?.actual?.amount,
                laborDataYesterdayViewCEO?.staffing?.insiders?.actual?.percentage,
                laborDataYesterdayViewCEO?.staffing?.insiders?.actual?.value
            )
            val totalDriversCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.staffing?.drivers?.actual?.amount,
                laborDataYesterdayViewCEO?.staffing?.drivers?.actual?.percentage,
                laborDataYesterdayViewCEO?.staffing?.drivers?.actual?.value
            )
            labourStaffing(
                totalTmCountCEOYesterday,
                totalInsiderCEOYesterday,
                totalDriversCEOYesterday
            )

            tm_count_less_than_thirty_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.staffing?.tmCountLessThan30Hours?.actual?.amount,
                laborDataYesterdayViewCEO?.staffing?.tmCountLessThan30Hours?.actual?.percentage,
                laborDataYesterdayViewCEO?.staffing?.tmCountLessThan30Hours?.actual?.value
            )
            tm_count_grater_than_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewCEO?.staffing?.tmCountMoreThan30Hours?.actual?.amount,
                laborDataYesterdayViewCEO?.staffing?.tmCountMoreThan30Hours?.actual?.percentage,
                laborDataYesterdayViewCEO?.staffing?.tmCountMoreThan30Hours?.actual?.value
            )

        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Labor Overview Yesterday KPI")
        }
    }

    private fun rangeOverViewCEO(laborTodayDetail: CEOOverviewRangeQuery.Ceo) {
        try {
            val laborDataRangeOverViewCEO =
                laborTodayDetail.kpis?.supervisors?.stores?.period?.labor

            Logger.info("Labor Period Range", "Labor Overview KPI")

            // display name
            Validation().checkNullValueToShowView(
                this,
                laborDataRangeOverViewCEO?.staffing?.tmCountLessThan30Hours?.displayName,
                tm_count_less_than_thirty_parent
            )
            Validation().checkNullValueToShowView(
                this,
                laborDataRangeOverViewCEO?.staffing?.tmCountMoreThan30Hours?.displayName,
                tm_count_grater_than_thirty_parent
            )

            labour_display.text = getString(R.string.labour_text)
            drive_ot_display.text = laborDataRangeOverViewCEO?.driverOTFull?.displayName
            driver_ot_premium_display.text = laborDataRangeOverViewCEO?.driverOTPremium?.displayName
            // display name below
            if (laborDataRangeOverViewCEO?.laborvsGoal == null || laborDataRangeOverViewCEO.laborvsGoal.displayName.isNullOrEmpty()) {
                ll_labor_display1.visibility = View.GONE
            } else {
                ll_labor_display1.visibility = View.VISIBLE
                labor_display1.text = laborDataRangeOverViewCEO.laborvsGoal.displayName
            }
            if (laborDataRangeOverViewCEO?.salesPerLaborHour == null || laborDataRangeOverViewCEO.salesPerLaborHour.displayName.isNullOrEmpty()) {
                ll_labor_display2.visibility = View.GONE
            } else {
                ll_labor_display2.visibility = View.VISIBLE
                labor_display2.text = laborDataRangeOverViewCEO.salesPerLaborHour.displayName
            }
            if (laborDataRangeOverViewCEO?.laborHours == null || laborDataRangeOverViewCEO.laborHours.displayName.isNullOrEmpty()) {
                ll_labor_display3.visibility = View.GONE
            } else {
                ll_labor_display3.visibility = View.VISIBLE
                labor_display3.text = laborDataRangeOverViewCEO.laborHours.displayName
            }
            if (laborDataRangeOverViewCEO?.laborvsManagerBudget == null || laborDataRangeOverViewCEO.laborvsManagerBudget.displayName.isNullOrEmpty()) {
                ll_labor_display4.visibility = View.GONE
            } else {
                ll_labor_display4.visibility = View.VISIBLE
                labor_display4.text = laborDataRangeOverViewCEO.laborvsManagerBudget.displayName
            }

            if (laborDataRangeOverViewCEO?.staffing?.total == null || laborDataRangeOverViewCEO.staffing.total.displayName.isNullOrEmpty()) {
                ll_staffing.visibility = View.GONE
            } else {
                ll_staffing.visibility = View.VISIBLE
                staffing_display.text = laborDataRangeOverViewCEO.staffing.total.displayName
            }

            if (laborDataRangeOverViewCEO?.staffing?.totalTMCount == null || laborDataRangeOverViewCEO.staffing.totalTMCount.displayName.isNullOrEmpty()) {
                ll_total_tm_count_text.visibility = View.GONE
            } else {
                ll_total_tm_count_text.visibility = View.VISIBLE
                total_tm_count_text.text = laborDataRangeOverViewCEO.staffing.totalTMCount.displayName
            }
            if (laborDataRangeOverViewCEO?.staffing?.insiders == null || laborDataRangeOverViewCEO.staffing.insiders.displayName.isNullOrEmpty()) {
                ll_insider_text.visibility = View.GONE
            } else {
                ll_insider_text.visibility = View.VISIBLE
                insider_text.text = laborDataRangeOverViewCEO.staffing.insiders.displayName
            }
            if (laborDataRangeOverViewCEO?.staffing?.drivers == null || laborDataRangeOverViewCEO.staffing.drivers.displayName.isNullOrEmpty()) {
                ll_drivers_text.visibility = View.GONE
            } else {
                ll_drivers_text.visibility = View.VISIBLE
                drivers_text.text = laborDataRangeOverViewCEO.staffing.drivers.displayName
            }
            if (laborDataRangeOverViewCEO?.staffing?.tmCountLessThan30Hours == null || laborDataRangeOverViewCEO.staffing.tmCountLessThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_less_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_less_than_thirty_parent.visibility = View.VISIBLE
                tm_count_less_than_thirty_text.text =
                    laborDataRangeOverViewCEO.staffing.tmCountLessThan30Hours.displayName
            }
            if (laborDataRangeOverViewCEO?.staffing?.tmCountMoreThan30Hours == null || laborDataRangeOverViewCEO.staffing.tmCountMoreThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_grater_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_grater_than_thirty_parent.visibility = View.VISIBLE
                tm_count_grater_than_thirty_text.text =
                    laborDataRangeOverViewCEO.staffing.tmCountMoreThan30Hours.displayName
            }

            // break display
            break_display.text = laborDataRangeOverViewCEO?.breaks?.total?.displayName
            breaks_qty_text.text = laborDataRangeOverViewCEO?.breaks?.breakQuantity?.displayName
            labor_breaks_dollar_text.text = laborDataRangeOverViewCEO?.breaks?.laborWithBreaks?.displayName
            labor_breaks_percentage_text.text = laborDataRangeOverViewCEO?.breaks?.laborWithBreaks?.displayName
            labor_breaks_ot_percentage_text.text = laborDataRangeOverViewCEO?.breaks?.laborWithoutBreaksAndOT?.displayName
            labor_breaks_ot_dollar_text.text = laborDataRangeOverViewCEO?.breaks?.laborWithoutBreaksAndOT?.displayName
            // end break

            labour_sales.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.actual?.amount,
                laborDataRangeOverViewCEO?.actual?.percentage,
                laborDataRangeOverViewCEO?.actual?.value
            )

            if (laborDataRangeOverViewCEO?.status != null) {
                when {
                    laborDataRangeOverViewCEO.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labour_sales.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewCEO.status.toString() == resources.getString(R.string.under_limit) -> {
                        labour_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        labour_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // scroll detect

            labor_scroll.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (labor_scroll.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            /* level_two_scroll_data_action.text =
                                 laborDataRangeOverViewCEO?.displayName ?: getString(R.string.labour_text)*/
                            level_two_scroll_data_action.text = getString(R.string.labour_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@LabourKpiActivity,
                                    laborDataRangeOverViewCEO?.actual?.amount,
                                    laborDataRangeOverViewCEO?.actual?.percentage,
                                    laborDataRangeOverViewCEO?.actual?.value
                                )

                            if (laborDataRangeOverViewCEO?.status != null) {
                                when {
                                    laborDataRangeOverViewCEO.status.toString() == resources.getString(
                                        R.string.out_of_range
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    laborDataRangeOverViewCEO.status.toString() == resources.getString(
                                        R.string.under_limit
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    }
                                    else -> {
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.black_circle,
                                            0
                                        )
                                    }
                                }
                            }
                        }
                        y = labor_scroll.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            labour_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.goal?.amount,
                laborDataRangeOverViewCEO?.goal?.percentage,
                laborDataRangeOverViewCEO?.goal?.value
            )
            labour_variance_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.variance?.amount,
                laborDataRangeOverViewCEO?.variance?.percentage,
                laborDataRangeOverViewCEO?.variance?.value
            )
            showLaborNarrativeData(laborDataRangeOverViewCEO?.narrative.toString())


            // Labor vs goal
            val laborGoalCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.laborvsGoal?.goal?.amount,
                laborDataRangeOverViewCEO?.laborvsGoal?.goal?.percentage,
                laborDataRangeOverViewCEO?.laborvsGoal?.goal?.value
            )
            val laborVarianceCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.laborvsGoal?.variance?.amount,
                laborDataRangeOverViewCEO?.laborvsGoal?.variance?.percentage,
                laborDataRangeOverViewCEO?.laborvsGoal?.variance?.value
            )
            val laborActualCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.laborvsGoal?.actual?.amount,
                laborDataRangeOverViewCEO?.laborvsGoal?.actual?.percentage,
                laborDataRangeOverViewCEO?.laborvsGoal?.actual?.value
            )
            labourGoal(
                laborGoalCEOYesterday,
                laborVarianceCEOYesterday,
                laborActualCEOYesterday
            )

            if (laborDataRangeOverViewCEO?.laborvsGoal?.status != null) {
                when {
                    laborDataRangeOverViewCEO.laborvsGoal.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewCEO.laborvsGoal.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // sales per labour
            val laborSalesHoursActualCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.salesPerLaborHour?.actual?.amount,
                laborDataRangeOverViewCEO?.salesPerLaborHour?.actual?.percentage,
                laborDataRangeOverViewCEO?.salesPerLaborHour?.actual?.value
            )
            labourSalesHrs(laborSalesHoursActualCEOYesterday)

            if (laborDataRangeOverViewCEO?.salesPerLaborHour?.status != null) {
                when {
                    laborDataRangeOverViewCEO.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewCEO.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }

            }


            // Labor vs management
            val laborManagementsGoalCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.laborvsManagerBudget?.goal?.amount,
                laborDataRangeOverViewCEO?.laborvsManagerBudget?.goal?.percentage,
                laborDataRangeOverViewCEO?.laborvsManagerBudget?.goal?.value
            )
            val laborManagementsVarianceCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.laborvsManagerBudget?.variance?.amount,
                laborDataRangeOverViewCEO?.laborvsManagerBudget?.variance?.percentage,
                laborDataRangeOverViewCEO?.laborvsManagerBudget?.variance?.value
            )
            val laborManagementsActualCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.laborvsManagerBudget?.actual?.amount,
                laborDataRangeOverViewCEO?.laborvsManagerBudget?.actual?.percentage,
                laborDataRangeOverViewCEO?.laborvsManagerBudget?.actual?.value
            )
            labourManagement(
                laborManagementsGoalCEOYesterday,
                laborManagementsVarianceCEOYesterday,
                laborManagementsActualCEOYesterday
            )

            if (laborDataRangeOverViewCEO?.laborvsManagerBudget?.status != null) {
                when {
                    laborDataRangeOverViewCEO.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewCEO.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // labor hour
            val laborHoursGoalCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.laborHours?.goal?.amount,
                laborDataRangeOverViewCEO?.laborHours?.goal?.percentage,
                laborDataRangeOverViewCEO?.laborHours?.goal?.value
            )
            val laborHoursVarianceCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.laborHours?.variance?.amount,
                laborDataRangeOverViewCEO?.laborHours?.variance?.percentage,
                laborDataRangeOverViewCEO?.laborHours?.variance?.value
            )
            val laborHoursActualCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.laborHours?.actual?.amount,
                laborDataRangeOverViewCEO?.laborHours?.actual?.percentage,
                laborDataRangeOverViewCEO?.laborHours?.actual?.value
            )
            labourHours(
                laborHoursGoalCEOYesterday,
                laborHoursVarianceCEOYesterday,
                laborHoursActualCEOYesterday
            )

            if (laborDataRangeOverViewCEO?.laborHours?.status != null) {
                when {
                    laborDataRangeOverViewCEO.laborHours.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewCEO.laborHours.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // breaks sales
            val breakPercentageCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.breaks?.total?.actual?.amount,
                laborDataRangeOverViewCEO?.breaks?.total?.actual?.percentage,
                laborDataRangeOverViewCEO?.breaks?.total?.actual?.value
            )
            if (breakPercentageCEOYesterday.isEmpty()) {

                breaks_error.visibility = View.VISIBLE
                breaks_percentage.visibility = View.GONE
            } else {
                breaks_percentage.text = breakPercentageCEOYesterday
            }

            if (laborDataRangeOverViewCEO?.breaks?.total?.status != null) {
                when {
                    laborDataRangeOverViewCEO.breaks.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewCEO.breaks.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            //Missed Break
            val laborMissedBreakQuantityCEORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.breaks?.breakQuantity?.actual?.amount,
                laborDataRangeOverViewCEO?.breaks?.breakQuantity?.actual?.percentage,
                laborDataRangeOverViewCEO?.breaks?.breakQuantity?.actual?.value
            )


            val laborMissedBreakDollarCEORange: String = if(laborDataRangeOverViewCEO?.breaks?.laborWithBreaks?.actual?.amount == null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataRangeOverViewCEO.breaks.laborWithBreaks.actual.amount)
                )
            }

            val laborMissedBreakPercentageCEORange: String = if(laborDataRangeOverViewCEO?.breaks?.laborWithBreaks?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataRangeOverViewCEO.breaks.laborWithBreaks.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }

            val laborMissedBreakOTDollarCEORange: String = if(laborDataRangeOverViewCEO?.breaks?.laborWithoutBreaksAndOT?.actual?.amount ==  null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataRangeOverViewCEO.breaks.laborWithoutBreaksAndOT.actual.amount)
                )
            }

            val laborMissedBreakOTPercentageCEORange : String = if(laborDataRangeOverViewCEO?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataRangeOverViewCEO.breaks.laborWithoutBreaksAndOT.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }


            laborMissedBreaksData(
                laborMissedBreakQuantityCEORange,
                laborMissedBreakDollarCEORange,
                laborMissedBreakPercentageCEORange,
                laborMissedBreakOTDollarCEORange,
                laborMissedBreakOTPercentageCEORange
            )

            // driver OT full
            val laborOtCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.driverOTFull?.actual?.amount,
                laborDataRangeOverViewCEO?.driverOTFull?.actual?.percentage,
                laborDataRangeOverViewCEO?.driverOTFull?.actual?.value
            )
            labourDriverOT(laborOtCEOYesterday)

            if (laborDataRangeOverViewCEO?.driverOTFull?.status != null) {
                when {
                    laborDataRangeOverViewCEO.driverOTFull.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewCEO.driverOTFull.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // driver ot premium
            val laborOtPremiumCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.driverOTPremium?.actual?.amount,
                laborDataRangeOverViewCEO?.driverOTPremium?.actual?.percentage,
                laborDataRangeOverViewCEO?.driverOTPremium?.actual?.value
            )
            labourDriverOTPremium(laborOtPremiumCEOYesterday)

            if (laborDataRangeOverViewCEO?.driverOTPremium?.status != null) {
                when {
                    laborDataRangeOverViewCEO.driverOTPremium.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewCEO.driverOTPremium.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // staffing
            staffing_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.staffing?.total?.actual?.amount,
                laborDataRangeOverViewCEO?.staffing?.total?.actual?.percentage,
                laborDataRangeOverViewCEO?.staffing?.total?.actual?.value
            )
            if (laborDataRangeOverViewCEO?.staffing?.total?.status != null) {
                staffing_percentage.text =
                        Validation().ignoreZeroAfterDecimal(laborDataRangeOverViewCEO.staffing.total.actual?.value)
                when {
                    laborDataRangeOverViewCEO.staffing.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewCEO.staffing.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val totalTmCountCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.staffing?.totalTMCount?.actual?.amount,
                laborDataRangeOverViewCEO?.staffing?.totalTMCount?.actual?.percentage,
                laborDataRangeOverViewCEO?.staffing?.totalTMCount?.actual?.value
            )
            val totalInsiderCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.staffing?.insiders?.actual?.amount,
                laborDataRangeOverViewCEO?.staffing?.insiders?.actual?.percentage,
                laborDataRangeOverViewCEO?.staffing?.insiders?.actual?.value
            )
            val totalDriversCEOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.staffing?.drivers?.actual?.amount,
                laborDataRangeOverViewCEO?.staffing?.drivers?.actual?.percentage,
                laborDataRangeOverViewCEO?.staffing?.drivers?.actual?.value
            )
            labourStaffing(
                totalTmCountCEOYesterday,
                totalInsiderCEOYesterday,
                totalDriversCEOYesterday
            )

            tm_count_less_than_thirty_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.staffing?.tmCountLessThan30Hours?.actual?.amount,
                laborDataRangeOverViewCEO?.staffing?.tmCountLessThan30Hours?.actual?.percentage,
                laborDataRangeOverViewCEO?.staffing?.tmCountLessThan30Hours?.actual?.value
            )
            tm_count_grater_than_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewCEO?.staffing?.tmCountMoreThan30Hours?.actual?.amount,
                laborDataRangeOverViewCEO?.staffing?.tmCountMoreThan30Hours?.actual?.percentage,
                laborDataRangeOverViewCEO?.staffing?.tmCountMoreThan30Hours?.actual?.value
            )

        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Labor Overview Period Range KPI")
        }
    }

    // do
    private fun todayViewDOLabour(laborTodayDetail: DOOverviewTodayQuery.Do_) {
        try {
            val laborDataTodayViewDOLabour =
                laborTodayDetail.kpis?.supervisors?.stores?.today?.labor

            Logger.info("Labor Today", "Labor Overview KPI")

            // display name
            labour_display.text = getString(R.string.labour_text)
            breaks_qty_text.text =
                laborDataTodayViewDOLabour?.breaks?.breakQuantity?.displayName
                    ?: getString(R.string.breaks_qty_text)
            labor_breaks_dollar_text.text =
                laborDataTodayViewDOLabour?.breaks?.laborWithBreaks?.displayName
            labor_breaks_percentage_text.text = laborDataTodayViewDOLabour?.breaks?.laborWithBreaks?.displayName
            labor_breaks_ot_percentage_text.text = laborDataTodayViewDOLabour?.breaks?.laborWithoutBreaksAndOT?.displayName
            labor_breaks_ot_dollar_text.text =
                laborDataTodayViewDOLabour?.breaks?.laborWithoutBreaksAndOT?.displayName
            drive_ot_display.text = laborDataTodayViewDOLabour?.driverOTFull?.displayName
            driver_ot_premium_display.text =
                laborDataTodayViewDOLabour?.driverOTPremium?.displayName
            // display name below
              if (laborDataTodayViewDOLabour?.laborvsGoal == null || laborDataTodayViewDOLabour.laborvsGoal.displayName.isNullOrEmpty()) {
                ll_labor_display1.visibility = View.GONE
            } else {
                ll_labor_display1.visibility = View.VISIBLE
                labor_display1.text = laborDataTodayViewDOLabour.laborvsGoal.displayName
            }
            if (laborDataTodayViewDOLabour?.salesPerLaborHour == null || laborDataTodayViewDOLabour.salesPerLaborHour.displayName.isNullOrEmpty()) {
                ll_labor_display2.visibility = View.GONE
            } else {
                ll_labor_display2.visibility = View.VISIBLE
                labor_display2.text = laborDataTodayViewDOLabour.salesPerLaborHour.displayName
            }
            if (laborDataTodayViewDOLabour?.laborHours == null || laborDataTodayViewDOLabour.laborHours.displayName.isNullOrEmpty()) {
                ll_labor_display3.visibility = View.GONE
            } else {
                ll_labor_display3.visibility = View.VISIBLE
                labor_display3.text = laborDataTodayViewDOLabour.laborHours.displayName
            }
            if (laborDataTodayViewDOLabour?.laborvsManagerBudget == null || laborDataTodayViewDOLabour.laborvsManagerBudget.displayName.isNullOrEmpty()) {
                ll_labor_display4.visibility = View.GONE
            } else {
                ll_labor_display4.visibility = View.VISIBLE
                labor_display4.text = laborDataTodayViewDOLabour.laborvsManagerBudget.displayName
            }

            if (laborDataTodayViewDOLabour?.staffing?.total == null || laborDataTodayViewDOLabour.staffing.total.displayName.isNullOrEmpty()) {
                ll_staffing.visibility = View.GONE
            } else {
                ll_staffing.visibility = View.VISIBLE
                staffing_display.text = laborDataTodayViewDOLabour.staffing.total.displayName
            }

            if (laborDataTodayViewDOLabour?.staffing?.totalTMCount == null || laborDataTodayViewDOLabour.staffing.totalTMCount.displayName.isNullOrEmpty()) {
                ll_total_tm_count_text.visibility = View.GONE
            } else {
                ll_total_tm_count_text.visibility = View.VISIBLE
                total_tm_count_text.text = laborDataTodayViewDOLabour.staffing.totalTMCount.displayName
            }
            if (laborDataTodayViewDOLabour?.staffing?.insiders == null || laborDataTodayViewDOLabour.staffing.insiders.displayName.isNullOrEmpty()) {
                ll_insider_text.visibility = View.GONE
            } else {
                ll_insider_text.visibility = View.VISIBLE
                insider_text.text = laborDataTodayViewDOLabour.staffing.insiders.displayName
            }
            if (laborDataTodayViewDOLabour?.staffing?.drivers == null || laborDataTodayViewDOLabour.staffing.drivers.displayName.isNullOrEmpty()) {
                ll_drivers_text.visibility = View.GONE
            } else {
                ll_drivers_text.visibility = View.VISIBLE
                drivers_text.text = laborDataTodayViewDOLabour.staffing.drivers.displayName
            }
            if (laborDataTodayViewDOLabour?.staffing?.tmCountLessThan30Hours == null || laborDataTodayViewDOLabour.staffing.tmCountLessThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_less_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_less_than_thirty_parent.visibility = View.VISIBLE
                tm_count_less_than_thirty_text.text =
                    laborDataTodayViewDOLabour.staffing.tmCountLessThan30Hours.displayName
            }
            if (laborDataTodayViewDOLabour?.staffing?.tmCountMoreThan30Hours == null || laborDataTodayViewDOLabour.staffing.tmCountMoreThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_grater_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_grater_than_thirty_parent.visibility = View.VISIBLE
                tm_count_grater_than_thirty_text.text =
                    laborDataTodayViewDOLabour.staffing.tmCountMoreThan30Hours.displayName
            }

            if (laborDataTodayViewDOLabour?.actual?.percentage?.isNaN() == false && laborDataTodayViewDOLabour.status != null) {
                labour_sales.text =
                    getString(R.string.dollar_text).plus(
                        Validation().dollarFormatting(
                            laborDataTodayViewDOLabour.laborvsGoal?.actual?.amount
                        )
                    )

                when {
                    laborDataTodayViewDOLabour.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labour_sales.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewDOLabour.status.toString() == resources.getString(R.string.under_limit) -> {
                        labour_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        labour_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // scroll detect

            labor_scroll.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (labor_scroll.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text =
                                laborDataTodayViewDOLabour?.displayName
                                    ?: getString(R.string.labour_text)

                            if (laborDataTodayViewDOLabour?.actual?.percentage?.isNaN() == false && laborDataTodayViewDOLabour.status != null) {
                                level_two_scroll_data_action_value.text =
                                    Validation().ignoreZeroAfterDecimal(laborDataTodayViewDOLabour.actual.percentage)
                                        .plus(getString(R.string.percentage_text))
                                when {
                                    laborDataTodayViewDOLabour.status.toString() == resources.getString(
                                        R.string.out_of_range
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    laborDataTodayViewDOLabour.status.toString() == resources.getString(
                                        R.string.under_limit
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    }
                                    else -> {
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.black_circle,
                                            0
                                        )
                                    }
                                }
                            }
                        }
                        y = labor_scroll.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })



            labour_goal_value.text =
                if (laborDataTodayViewDOLabour?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.laborvsGoal.goal.amount)
                )
                else ""
            labour_variance_value.text =
                if (laborDataTodayViewDOLabour?.laborvsGoal?.variance?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.laborvsGoal.variance.amount)
                )
                else ""

            showLaborNarrativeData(laborDataTodayViewDOLabour?.narrative.toString())


            // Labor vs goal
            labor_vs_goal_goal.text =
                if (laborDataTodayViewDOLabour?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.laborvsGoal.goal.amount)
                ) else ""

            labour_goal_value.text =
                if (laborDataTodayViewDOLabour?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.laborvsGoal.goal.amount)
                ) else ""
            labor_vs_goal_goal.text =
                if (laborDataTodayViewDOLabour?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.laborvsGoal.goal.amount)
                ) else ""

            labor_vs_goal_variance.text =
                if (laborDataTodayViewDOLabour?.laborvsGoal?.variance?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.laborvsGoal.variance.amount)
                ) else ""

            if (laborDataTodayViewDOLabour?.laborvsGoal?.actual?.amount?.isNaN() == false && laborDataTodayViewDOLabour.laborvsGoal.status != null) {
                labor_vs_goal_actual.text =
                    getString(R.string.dollar_text).plus(
                        Validation().dollarFormatting(
                            laborDataTodayViewDOLabour.laborvsGoal.actual.amount
                        )
                    )
                when {
                    laborDataTodayViewDOLabour.laborvsGoal.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewDOLabour.laborvsGoal.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // sales per labour

            if (laborDataTodayViewDOLabour?.salesPerLaborHour?.actual?.value?.isNaN() == false && laborDataTodayViewDOLabour.salesPerLaborHour.status != null) {
                sales_labor_vs_goal_actual.text =
                    Validation().ignoreZeroAfterDecimal(laborDataTodayViewDOLabour.salesPerLaborHour.actual.value)
                when {
                    laborDataTodayViewDOLabour.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewDOLabour.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }

            }


            // Labor vs management

            labor_vs_mgmt_goal_goal.text =
                if (laborDataTodayViewDOLabour?.laborvsManagerBudget?.goal?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.laborvsManagerBudget.goal.amount)
                ) else ""
            labor_vs_mgmt_goal_variance.text =
                if (laborDataTodayViewDOLabour?.laborvsManagerBudget?.variance?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.laborvsManagerBudget.variance.amount)
                ) else ""

            if (laborDataTodayViewDOLabour?.laborvsManagerBudget?.actual?.amount?.isNaN() == false && laborDataTodayViewDOLabour.laborvsManagerBudget.status != null) {
                labor_vs_mgmt_goal_actual.text = getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.laborvsManagerBudget.actual.amount)
                )
                when {
                    laborDataTodayViewDOLabour.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewDOLabour.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // labor hour
            labor_hrs_vs_goal_goal.text =
                Validation().ignoreZeroAfterDecimal(laborDataTodayViewDOLabour?.laborHours?.goal?.value)
            labor_hrs_vs_goal_variance.text =
                Validation().ignoreZeroAfterDecimal(laborDataTodayViewDOLabour?.laborHours?.variance?.value)

            if (laborDataTodayViewDOLabour?.laborHours?.actual?.value?.isNaN() == false && laborDataTodayViewDOLabour.laborHours.status != null) {
                labor_hrs_vs_goal_actual.text =
                    Validation().ignoreZeroAfterDecimal(laborDataTodayViewDOLabour.laborHours.actual.value)
                when {
                    laborDataTodayViewDOLabour.laborHours.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewDOLabour.laborHours.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // breaks sales

            if (laborDataTodayViewDOLabour?.breaks?.total?.actual?.amount?.isNaN() == false && laborDataTodayViewDOLabour.breaks.total.status != null) {
                breaks_percentage.text = getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.breaks.total.actual.amount)
                )
                when {
                    laborDataTodayViewDOLabour.breaks.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewDOLabour.breaks.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            breaks_qty_percentage.text = Validation().ignoreZeroAfterDecimal(
                laborDataTodayViewDOLabour?.breaks?.total?.actual?.amount
            )

            breaks_break_dollar_percentage.text =
                if (laborDataTodayViewDOLabour?.breaks?.laborWithBreaks?.actual?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.breaks.laborWithBreaks.actual.amount)
                ) else ""

            labor_breaks_percentage.text =
                if (laborDataTodayViewDOLabour?.breaks?.laborWithBreaks?.actual?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewDOLabour.breaks.laborWithBreaks.actual.percentage
                )
                    .plus(getString(R.string.percentage_text)) else ""


            labor_breaks_ot_dollar.text =
                if (laborDataTodayViewDOLabour?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.breaks.laborWithoutBreaksAndOT.actual.percentage)
                ) else ""


            labor_breaks_ot_percentage.text =
                if (laborDataTodayViewDOLabour?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewDOLabour.breaks.laborWithoutBreaksAndOT.actual.percentage
                )
                    .plus(getString(R.string.percentage_text)) else ""

            // driver


            if (laborDataTodayViewDOLabour?.driverOTFull?.actual?.amount?.isNaN() == false && laborDataTodayViewDOLabour.driverOTFull.status != null) {
                driver_ot_percentage.text = getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.driverOTFull.actual.amount)
                )
                when {
                    laborDataTodayViewDOLabour.driverOTFull.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewDOLabour.driverOTFull.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // driver ot premium
            if (laborDataTodayViewDOLabour?.driverOTPremium?.actual?.amount?.isNaN() == false && laborDataTodayViewDOLabour.driverOTPremium.status != null) {
                driver_ot_premium_percentage.text = getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewDOLabour.driverOTPremium.actual.amount)
                )
                when {
                    laborDataTodayViewDOLabour.driverOTPremium.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewDOLabour.driverOTPremium.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // staffing
            if (laborDataTodayViewDOLabour?.staffing?.total != null) {
                if (laborDataTodayViewDOLabour.staffing.total.actual?.value?.isNaN() == false && laborDataTodayViewDOLabour.staffing.total.status != null) {
                    staffing_percentage.text =
                        Validation().ignoreZeroAfterDecimal(laborDataTodayViewDOLabour.staffing.total.actual.value)
                    when {
                        laborDataTodayViewDOLabour.staffing.total.status.toString() == resources.getString(
                            R.string.out_of_range
                        ) -> {
                            staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.red_circle,
                                0
                            )
                            staffing_percentage.setTextColor(getColor(R.color.red))
                        }
                        laborDataTodayViewDOLabour.staffing.total.status.toString() == resources.getString(
                            R.string.under_limit
                        ) -> {
                            staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.green_circle,
                                0
                            )
                            staffing_percentage.setTextColor(getColor(R.color.green))

                        }
                        else -> {
                            staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.black_circle,
                                0
                            )
                            staffing_percentage.setTextColor(getColor(R.color.text_color))

                        }
                    }
                }
            }

            total_tm_count_percentage.text =
                if (laborDataTodayViewDOLabour?.staffing?.totalTMCount?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewDOLabour.staffing.totalTMCount.actual.value
                ) else ""
            insider_percentage.text =
                if (laborDataTodayViewDOLabour?.staffing?.insiders?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewDOLabour.staffing.insiders.actual.value
                ) else ""
            drivers_percentage.text =
                if (laborDataTodayViewDOLabour?.staffing?.drivers?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewDOLabour.staffing.drivers.actual.value
                ) else ""
            tm_count_less_than_thirty_percentage.text =
                if (laborDataTodayViewDOLabour?.staffing?.tmCountLessThan30Hours?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewDOLabour.staffing.tmCountLessThan30Hours.actual.value
                ) else ""
            tm_count_grater_than_percentage.text =
                if (laborDataTodayViewDOLabour?.staffing?.tmCountMoreThan30Hours?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewDOLabour.staffing.tmCountMoreThan30Hours.actual.value
                ) else ""
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun yesterdayViewDO(laborTodayDetail: DOOverviewYesterdayQuery.Do_) {
        try {
            val laborDataYesterdayViewDO = laborTodayDetail.kpis?.supervisors?.stores?.yesterday?.labor

            Logger.info("Labor Yesterday", "Labor Overview KPI")

            // display name
            Validation().checkNullValueToShowView(
                this,
                laborDataYesterdayViewDO?.staffing?.tmCountLessThan30Hours?.displayName,
                tm_count_less_than_thirty_parent
            )
            Validation().checkNullValueToShowView(
                this,
                laborDataYesterdayViewDO?.staffing?.tmCountMoreThan30Hours?.displayName,
                tm_count_grater_than_thirty_parent
            )

            labour_display.text = getString(R.string.labour_text)
            drive_ot_display.text = laborDataYesterdayViewDO?.driverOTFull?.displayName
            driver_ot_premium_display.text = laborDataYesterdayViewDO?.driverOTPremium?.displayName
            // display name below
            if (laborDataYesterdayViewDO?.laborvsGoal == null || laborDataYesterdayViewDO.laborvsGoal.displayName.isNullOrEmpty()) {
                ll_labor_display1.visibility = View.GONE
            } else {
                ll_labor_display1.visibility = View.VISIBLE
                labor_display1.text = laborDataYesterdayViewDO.laborvsGoal.displayName
            }
            if (laborDataYesterdayViewDO?.salesPerLaborHour == null || laborDataYesterdayViewDO.salesPerLaborHour.displayName.isNullOrEmpty()) {
                ll_labor_display2.visibility = View.GONE
            } else {
                ll_labor_display2.visibility = View.VISIBLE
                labor_display2.text = laborDataYesterdayViewDO.salesPerLaborHour.displayName
            }
            if (laborDataYesterdayViewDO?.laborHours == null || laborDataYesterdayViewDO.laborHours.displayName.isNullOrEmpty()) {
                ll_labor_display3.visibility = View.GONE
            } else {
                ll_labor_display3.visibility = View.VISIBLE
                labor_display3.text = laborDataYesterdayViewDO.laborHours.displayName
            }
            if (laborDataYesterdayViewDO?.laborvsManagerBudget == null || laborDataYesterdayViewDO.laborvsManagerBudget.displayName.isNullOrEmpty()) {
                ll_labor_display4.visibility = View.GONE
            } else {
                ll_labor_display4.visibility = View.VISIBLE
                labor_display4.text = laborDataYesterdayViewDO.laborvsManagerBudget.displayName
            }

            if (laborDataYesterdayViewDO?.staffing?.total == null || laborDataYesterdayViewDO.staffing.total.displayName.isNullOrEmpty()) {
                ll_staffing.visibility = View.GONE
            } else {
                ll_staffing.visibility = View.VISIBLE
                staffing_display.text = laborDataYesterdayViewDO.staffing.total.displayName
            }

            if (laborDataYesterdayViewDO?.staffing?.totalTMCount == null || laborDataYesterdayViewDO.staffing.totalTMCount.displayName.isNullOrEmpty()) {
                ll_total_tm_count_text.visibility = View.GONE
            } else {
                ll_total_tm_count_text.visibility = View.VISIBLE
                total_tm_count_text.text = laborDataYesterdayViewDO.staffing.totalTMCount.displayName
            }
            if (laborDataYesterdayViewDO?.staffing?.insiders == null || laborDataYesterdayViewDO.staffing.insiders.displayName.isNullOrEmpty()) {
                ll_insider_text.visibility = View.GONE
            } else {
                ll_insider_text.visibility = View.VISIBLE
                insider_text.text = laborDataYesterdayViewDO.staffing.insiders.displayName
            }
            if (laborDataYesterdayViewDO?.staffing?.drivers == null || laborDataYesterdayViewDO.staffing.drivers.displayName.isNullOrEmpty()) {
                ll_drivers_text.visibility = View.GONE
            } else {
                ll_drivers_text.visibility = View.VISIBLE
                drivers_text.text = laborDataYesterdayViewDO.staffing.drivers.displayName
            }
            if (laborDataYesterdayViewDO?.staffing?.tmCountLessThan30Hours == null || laborDataYesterdayViewDO.staffing.tmCountLessThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_less_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_less_than_thirty_parent.visibility = View.VISIBLE
                tm_count_less_than_thirty_text.text =
                    laborDataYesterdayViewDO.staffing.tmCountLessThan30Hours.displayName
            }
            if (laborDataYesterdayViewDO?.staffing?.tmCountMoreThan30Hours == null || laborDataYesterdayViewDO.staffing.tmCountMoreThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_grater_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_grater_than_thirty_parent.visibility = View.VISIBLE
                tm_count_grater_than_thirty_text.text =
                    laborDataYesterdayViewDO.staffing.tmCountMoreThan30Hours.displayName
            }

            // break display
            break_display.text = laborDataYesterdayViewDO?.breaks?.total?.displayName
            breaks_qty_text.text = laborDataYesterdayViewDO?.breaks?.breakQuantity?.displayName
            labor_breaks_dollar_text.text = laborDataYesterdayViewDO?.breaks?.laborWithBreaks?.displayName
            labor_breaks_percentage_text.text = laborDataYesterdayViewDO?.breaks?.laborWithBreaks?.displayName
            labor_breaks_ot_percentage_text.text = laborDataYesterdayViewDO?.breaks?.laborWithoutBreaksAndOT?.displayName
            labor_breaks_ot_dollar_text.text = laborDataYesterdayViewDO?.breaks?.laborWithoutBreaksAndOT?.displayName
            // end break

            labour_sales.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.actual?.amount,
                laborDataYesterdayViewDO?.actual?.percentage,
                laborDataYesterdayViewDO?.actual?.value
            )

            if (laborDataYesterdayViewDO?.status != null) {
                when {
                    laborDataYesterdayViewDO.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labour_sales.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewDO.status.toString() == resources.getString(R.string.under_limit) -> {
                        labour_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        labour_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // scroll detect

            labor_scroll.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (labor_scroll.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            /* level_two_scroll_data_action.text =
                                 laborDataYesterdayViewDO?.displayName ?: getString(R.string.labour_text)*/
                            level_two_scroll_data_action.text = getString(R.string.labour_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@LabourKpiActivity,
                                    laborDataYesterdayViewDO?.actual?.amount,
                                    laborDataYesterdayViewDO?.actual?.percentage,
                                    laborDataYesterdayViewDO?.actual?.value
                                )

                            if (laborDataYesterdayViewDO?.status != null) {
                                when {
                                    laborDataYesterdayViewDO.status.toString() == resources.getString(
                                        R.string.out_of_range
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    laborDataYesterdayViewDO.status.toString() == resources.getString(
                                        R.string.under_limit
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    }
                                    else -> {
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.black_circle,
                                            0
                                        )
                                    }
                                }
                            }
                        }
                        y = labor_scroll.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            labour_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.goal?.amount,
                laborDataYesterdayViewDO?.goal?.percentage,
                laborDataYesterdayViewDO?.goal?.value
            )
            labour_variance_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.variance?.amount,
                laborDataYesterdayViewDO?.variance?.percentage,
                laborDataYesterdayViewDO?.variance?.value
            )
            showLaborNarrativeData(laborDataYesterdayViewDO?.narrative.toString())


            // Labor vs goal
            val laborGoalDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.laborvsGoal?.goal?.amount,
                laborDataYesterdayViewDO?.laborvsGoal?.goal?.percentage,
                laborDataYesterdayViewDO?.laborvsGoal?.goal?.value
            )
            val laborVarianceDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.laborvsGoal?.variance?.amount,
                laborDataYesterdayViewDO?.laborvsGoal?.variance?.percentage,
                laborDataYesterdayViewDO?.laborvsGoal?.variance?.value
            )
            val laborActualDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.laborvsGoal?.actual?.amount,
                laborDataYesterdayViewDO?.laborvsGoal?.actual?.percentage,
                laborDataYesterdayViewDO?.laborvsGoal?.actual?.value
            )
            labourGoal(
                laborGoalDOYesterday,
                laborVarianceDOYesterday,
                laborActualDOYesterday
            )

            if (laborDataYesterdayViewDO?.laborvsGoal?.status != null) {
                when {
                    laborDataYesterdayViewDO.laborvsGoal.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewDO.laborvsGoal.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // sales per labour
            val laborSalesHoursActualDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.salesPerLaborHour?.actual?.amount,
                laborDataYesterdayViewDO?.salesPerLaborHour?.actual?.percentage,
                laborDataYesterdayViewDO?.salesPerLaborHour?.actual?.value
            )
            labourSalesHrs(laborSalesHoursActualDOYesterday)

            if (laborDataYesterdayViewDO?.salesPerLaborHour?.status != null) {
                when {
                    laborDataYesterdayViewDO.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewDO.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }

            }


            // Labor vs management
            val laborManagementsGoalDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.laborvsManagerBudget?.goal?.amount,
                laborDataYesterdayViewDO?.laborvsManagerBudget?.goal?.percentage,
                laborDataYesterdayViewDO?.laborvsManagerBudget?.goal?.value
            )
            val laborManagementsVarianceDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.laborvsManagerBudget?.variance?.amount,
                laborDataYesterdayViewDO?.laborvsManagerBudget?.variance?.percentage,
                laborDataYesterdayViewDO?.laborvsManagerBudget?.variance?.value
            )
            val laborManagementsActualDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.laborvsManagerBudget?.actual?.amount,
                laborDataYesterdayViewDO?.laborvsManagerBudget?.actual?.percentage,
                laborDataYesterdayViewDO?.laborvsManagerBudget?.actual?.value
            )
            labourManagement(
                laborManagementsGoalDOYesterday,
                laborManagementsVarianceDOYesterday,
                laborManagementsActualDOYesterday
            )

            if (laborDataYesterdayViewDO?.laborvsManagerBudget?.status != null) {
                when {
                    laborDataYesterdayViewDO.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewDO.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // labor hour
            val laborHoursGoalDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.laborHours?.goal?.amount,
                laborDataYesterdayViewDO?.laborHours?.goal?.percentage,
                laborDataYesterdayViewDO?.laborHours?.goal?.value
            )
            val laborHoursVarianceDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.laborHours?.variance?.amount,
                laborDataYesterdayViewDO?.laborHours?.variance?.percentage,
                laborDataYesterdayViewDO?.laborHours?.variance?.value
            )
            val laborHoursActualDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.laborHours?.actual?.amount,
                laborDataYesterdayViewDO?.laborHours?.actual?.percentage,
                laborDataYesterdayViewDO?.laborHours?.actual?.value
            )
            labourHours(
                laborHoursGoalDOYesterday,
                laborHoursVarianceDOYesterday,
                laborHoursActualDOYesterday
            )

            if (laborDataYesterdayViewDO?.laborHours?.status != null) {
                when {
                    laborDataYesterdayViewDO.laborHours.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewDO.laborHours.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // breaks sales
            val breakPercentageDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.breaks?.total?.actual?.amount,
                laborDataYesterdayViewDO?.breaks?.total?.actual?.percentage,
                laborDataYesterdayViewDO?.breaks?.total?.actual?.value
            )
            if (breakPercentageDOYesterday.isEmpty()) {

                breaks_error.visibility = View.VISIBLE
                breaks_percentage.visibility = View.GONE
            } else {
                breaks_percentage.text = breakPercentageDOYesterday
            }

            if (laborDataYesterdayViewDO?.breaks?.total?.status != null) {
                when {
                    laborDataYesterdayViewDO.breaks.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewDO.breaks.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            //Missed Break
            val laborMissedBreakQuantityDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.breaks?.breakQuantity?.actual?.amount,
                laborDataYesterdayViewDO?.breaks?.breakQuantity?.actual?.percentage,
                laborDataYesterdayViewDO?.breaks?.breakQuantity?.actual?.value
            )
            val laborMissedBreakDollarDOYesterday: String = if(laborDataYesterdayViewDO?.breaks?.laborWithBreaks?.actual?.amount == null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataYesterdayViewDO.breaks.laborWithBreaks.actual.amount)
                )
            }

            val laborMissedBreakPercentageDOYesterday: String = if(laborDataYesterdayViewDO?.breaks?.laborWithBreaks?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataYesterdayViewDO.breaks.laborWithBreaks.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }

            val laborMissedBreakOTDollarDOYesterday: String = if(laborDataYesterdayViewDO?.breaks?.laborWithoutBreaksAndOT?.actual?.amount ==  null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataYesterdayViewDO.breaks.laborWithoutBreaksAndOT.actual.amount)
                )
            }

            val laborMissedBreakOTPercentageDOYesterday : String = if(laborDataYesterdayViewDO?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataYesterdayViewDO.breaks.laborWithoutBreaksAndOT.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }

            laborMissedBreaksData(
                laborMissedBreakQuantityDOYesterday,
                laborMissedBreakDollarDOYesterday,
                laborMissedBreakPercentageDOYesterday,
                laborMissedBreakOTDollarDOYesterday,
                laborMissedBreakOTPercentageDOYesterday
            )

            // Driver OT Full
            val laborOtDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.driverOTFull?.actual?.amount,
                laborDataYesterdayViewDO?.driverOTFull?.actual?.percentage,
                laborDataYesterdayViewDO?.driverOTFull?.actual?.value
            )
            labourDriverOT(laborOtDOYesterday)

            if (laborDataYesterdayViewDO?.driverOTFull?.status != null) {
                when {
                    laborDataYesterdayViewDO.driverOTFull.status.toString() == resources.getString(R.string.out_of_range) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewDO.driverOTFull.status.toString() == resources.getString(R.string.under_limit) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // driver ot premium
            val laborOtPremiumDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.driverOTPremium?.actual?.amount,
                laborDataYesterdayViewDO?.driverOTPremium?.actual?.percentage,
                laborDataYesterdayViewDO?.driverOTPremium?.actual?.value
            )
            labourDriverOTPremium(laborOtPremiumDOYesterday)

            if (laborDataYesterdayViewDO?.driverOTPremium?.status != null) {
                when {
                    laborDataYesterdayViewDO.driverOTPremium.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewDO.driverOTPremium.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // staffing
            staffing_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.staffing?.total?.actual?.amount,
                laborDataYesterdayViewDO?.staffing?.total?.actual?.percentage,
                laborDataYesterdayViewDO?.staffing?.total?.actual?.value
            )
            if (laborDataYesterdayViewDO?.staffing?.total?.status != null) {
                staffing_percentage.text =
                        Validation().ignoreZeroAfterDecimal(laborDataYesterdayViewDO.staffing.total.actual?.value)
                when {
                    laborDataYesterdayViewDO.staffing.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewDO.staffing.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val totalTmCountDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.staffing?.totalTMCount?.actual?.amount,
                laborDataYesterdayViewDO?.staffing?.totalTMCount?.actual?.percentage,
                laborDataYesterdayViewDO?.staffing?.totalTMCount?.actual?.value
            )
            val totalInsiderDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.staffing?.insiders?.actual?.amount,
                laborDataYesterdayViewDO?.staffing?.insiders?.actual?.percentage,
                laborDataYesterdayViewDO?.staffing?.insiders?.actual?.value
            )
            val totalDriversDOYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.staffing?.drivers?.actual?.amount,
                laborDataYesterdayViewDO?.staffing?.drivers?.actual?.percentage,
                laborDataYesterdayViewDO?.staffing?.drivers?.actual?.value
            )
            labourStaffing(
                totalTmCountDOYesterday,
                totalInsiderDOYesterday,
                totalDriversDOYesterday
            )

            tm_count_less_than_thirty_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.staffing?.tmCountLessThan30Hours?.actual?.amount,
                laborDataYesterdayViewDO?.staffing?.tmCountLessThan30Hours?.actual?.percentage,
                laborDataYesterdayViewDO?.staffing?.tmCountLessThan30Hours?.actual?.value
            )
            tm_count_grater_than_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewDO?.staffing?.tmCountMoreThan30Hours?.actual?.amount,
                laborDataYesterdayViewDO?.staffing?.tmCountMoreThan30Hours?.actual?.percentage,
                laborDataYesterdayViewDO?.staffing?.tmCountMoreThan30Hours?.actual?.value
            )

        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Labor Overview Yesterday KPI")
        }
    }

    private fun rangeOverViewDO(laborTodayDetail: DOOverviewRangeQuery.Do_) {
        try {
            val laborDataRangeOverViewDO = laborTodayDetail.kpis?.supervisors?.stores?.period?.labor

            Logger.info("Labor Period Range", "Labor Overview KPI")

            // display name
            Validation().checkNullValueToShowView(
                this,
                laborDataRangeOverViewDO?.staffing?.tmCountLessThan30Hours?.displayName,
                tm_count_less_than_thirty_parent
            )
            Validation().checkNullValueToShowView(
                this,
                laborDataRangeOverViewDO?.staffing?.tmCountMoreThan30Hours?.displayName,
                tm_count_grater_than_thirty_parent
            )

            labour_display.text = getString(R.string.labour_text)
            drive_ot_display.text = laborDataRangeOverViewDO?.driverOTFull?.displayName
            driver_ot_premium_display.text = laborDataRangeOverViewDO?.driverOTPremium?.displayName
            // display name below
            if (laborDataRangeOverViewDO?.laborvsGoal == null || laborDataRangeOverViewDO.laborvsGoal.displayName.isNullOrEmpty()) {
                ll_labor_display1.visibility = View.GONE
            } else {
                ll_labor_display1.visibility = View.VISIBLE
                labor_display1.text = laborDataRangeOverViewDO.laborvsGoal.displayName
            }
            if (laborDataRangeOverViewDO?.salesPerLaborHour == null || laborDataRangeOverViewDO.salesPerLaborHour.displayName.isNullOrEmpty()) {
                ll_labor_display2.visibility = View.GONE
            } else {
                ll_labor_display2.visibility = View.VISIBLE
                labor_display2.text = laborDataRangeOverViewDO.salesPerLaborHour.displayName
            }
            if (laborDataRangeOverViewDO?.laborHours == null || laborDataRangeOverViewDO.laborHours.displayName.isNullOrEmpty()) {
                ll_labor_display3.visibility = View.GONE
            } else {
                ll_labor_display3.visibility = View.VISIBLE
                labor_display3.text = laborDataRangeOverViewDO.laborHours.displayName
            }
            if (laborDataRangeOverViewDO?.laborvsManagerBudget == null || laborDataRangeOverViewDO.laborvsManagerBudget.displayName.isNullOrEmpty()) {
                ll_labor_display4.visibility = View.GONE
            } else {
                ll_labor_display4.visibility = View.VISIBLE
                labor_display4.text = laborDataRangeOverViewDO.laborvsManagerBudget.displayName
            }

            if (laborDataRangeOverViewDO?.staffing?.total == null || laborDataRangeOverViewDO.staffing.total.displayName.isNullOrEmpty()) {
                ll_staffing.visibility = View.GONE
            } else {
                ll_staffing.visibility = View.VISIBLE
                staffing_display.text = laborDataRangeOverViewDO.staffing.total.displayName
            }

            if (laborDataRangeOverViewDO?.staffing?.totalTMCount == null || laborDataRangeOverViewDO.staffing.totalTMCount.displayName.isNullOrEmpty()) {
                ll_total_tm_count_text.visibility = View.GONE
            } else {
                ll_total_tm_count_text.visibility = View.VISIBLE
                total_tm_count_text.text = laborDataRangeOverViewDO.staffing.totalTMCount.displayName
            }
            if (laborDataRangeOverViewDO?.staffing?.insiders == null || laborDataRangeOverViewDO.staffing.insiders.displayName.isNullOrEmpty()) {
                ll_insider_text.visibility = View.GONE
            } else {
                ll_insider_text.visibility = View.VISIBLE
                insider_text.text = laborDataRangeOverViewDO.staffing.insiders.displayName
            }
            if (laborDataRangeOverViewDO?.staffing?.drivers == null || laborDataRangeOverViewDO.staffing.drivers.displayName.isNullOrEmpty()) {
                ll_drivers_text.visibility = View.GONE
            } else {
                ll_drivers_text.visibility = View.VISIBLE
                drivers_text.text = laborDataRangeOverViewDO.staffing.drivers.displayName
            }
            if (laborDataRangeOverViewDO?.staffing?.tmCountLessThan30Hours == null || laborDataRangeOverViewDO.staffing.tmCountLessThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_less_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_less_than_thirty_parent.visibility = View.VISIBLE
                tm_count_less_than_thirty_text.text =
                    laborDataRangeOverViewDO.staffing.tmCountLessThan30Hours.displayName
            }
            if (laborDataRangeOverViewDO?.staffing?.tmCountMoreThan30Hours == null || laborDataRangeOverViewDO.staffing.tmCountMoreThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_grater_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_grater_than_thirty_parent.visibility = View.VISIBLE
                tm_count_grater_than_thirty_text.text =
                    laborDataRangeOverViewDO.staffing.tmCountMoreThan30Hours.displayName
            }

            // break display
            break_display.text = laborDataRangeOverViewDO?.breaks?.total?.displayName
            breaks_qty_text.text = laborDataRangeOverViewDO?.breaks?.breakQuantity?.displayName
            labor_breaks_dollar_text.text = laborDataRangeOverViewDO?.breaks?.laborWithBreaks?.displayName
            labor_breaks_percentage_text.text = laborDataRangeOverViewDO?.breaks?.laborWithBreaks?.displayName
            labor_breaks_ot_percentage_text.text = laborDataRangeOverViewDO?.breaks?.laborWithoutBreaksAndOT?.displayName
            labor_breaks_ot_dollar_text.text = laborDataRangeOverViewDO?.breaks?.laborWithoutBreaksAndOT?.displayName
            // end break

            labour_sales.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.actual?.amount,
                laborDataRangeOverViewDO?.actual?.percentage,
                laborDataRangeOverViewDO?.actual?.value
            )

            if (laborDataRangeOverViewDO?.status != null) {
                when {
                    laborDataRangeOverViewDO.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labour_sales.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewDO.status.toString() == resources.getString(R.string.under_limit) -> {
                        labour_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        labour_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // scroll detect

            labor_scroll.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (labor_scroll.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            /* level_two_scroll_data_action.text =
                                 laborDataRangeOverViewDO?.displayName ?: getString(R.string.labour_text)*/
                            level_two_scroll_data_action.text = getString(R.string.labour_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@LabourKpiActivity,
                                    laborDataRangeOverViewDO?.actual?.amount,
                                    laborDataRangeOverViewDO?.actual?.percentage,
                                    laborDataRangeOverViewDO?.actual?.value
                                )

                            if (laborDataRangeOverViewDO?.status != null) {
                                when {
                                    laborDataRangeOverViewDO.status.toString() == resources.getString(
                                        R.string.out_of_range
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    laborDataRangeOverViewDO.status.toString() == resources.getString(
                                        R.string.under_limit
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    }
                                    else -> {
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.black_circle,
                                            0
                                        )
                                    }
                                }
                            }
                        }
                        y = labor_scroll.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            labour_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.goal?.amount,
                laborDataRangeOverViewDO?.goal?.percentage,
                laborDataRangeOverViewDO?.goal?.value
            )
            labour_variance_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.variance?.amount,
                laborDataRangeOverViewDO?.variance?.percentage,
                laborDataRangeOverViewDO?.variance?.value
            )
            showLaborNarrativeData(laborDataRangeOverViewDO?.narrative.toString())

            // Labor vs goal
            val laborGoalDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.laborvsGoal?.goal?.amount,
                laborDataRangeOverViewDO?.laborvsGoal?.goal?.percentage,
                laborDataRangeOverViewDO?.laborvsGoal?.goal?.value
            )
            val laborVarianceDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.laborvsGoal?.variance?.amount,
                laborDataRangeOverViewDO?.laborvsGoal?.variance?.percentage,
                laborDataRangeOverViewDO?.laborvsGoal?.variance?.value
            )
            val laborActualDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.laborvsGoal?.actual?.amount,
                laborDataRangeOverViewDO?.laborvsGoal?.actual?.percentage,
                laborDataRangeOverViewDO?.laborvsGoal?.actual?.value
            )
            labourGoal(laborGoalDORange, laborVarianceDORange, laborActualDORange)

            if (laborDataRangeOverViewDO?.laborvsGoal?.status != null) {
                when {
                    laborDataRangeOverViewDO.laborvsGoal.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewDO.laborvsGoal.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // sales per labour
            val laborSalesHoursActualDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.salesPerLaborHour?.actual?.amount,
                laborDataRangeOverViewDO?.salesPerLaborHour?.actual?.percentage,
                laborDataRangeOverViewDO?.salesPerLaborHour?.actual?.value
            )
            labourSalesHrs(laborSalesHoursActualDORange)

            if (laborDataRangeOverViewDO?.salesPerLaborHour?.status != null) {
                when {
                    laborDataRangeOverViewDO.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewDO.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }

            }


            // Labor vs management
            val laborManagementsGoalDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.laborvsManagerBudget?.goal?.amount,
                laborDataRangeOverViewDO?.laborvsManagerBudget?.goal?.percentage,
                laborDataRangeOverViewDO?.laborvsManagerBudget?.goal?.value
            )
            val laborManagementsVarianceDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.laborvsManagerBudget?.variance?.amount,
                laborDataRangeOverViewDO?.laborvsManagerBudget?.variance?.percentage,
                laborDataRangeOverViewDO?.laborvsManagerBudget?.variance?.value
            )
            val laborManagementsActualDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.laborvsManagerBudget?.actual?.amount,
                laborDataRangeOverViewDO?.laborvsManagerBudget?.actual?.percentage,
                laborDataRangeOverViewDO?.laborvsManagerBudget?.actual?.value
            )
            labourManagement(
                laborManagementsGoalDORange,
                laborManagementsVarianceDORange,
                laborManagementsActualDORange
            )

            if (laborDataRangeOverViewDO?.laborvsManagerBudget?.status != null) {
                when {
                    laborDataRangeOverViewDO.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewDO.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // labor hour
            val laborHoursGoalDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.laborHours?.goal?.amount,
                laborDataRangeOverViewDO?.laborHours?.goal?.percentage,
                laborDataRangeOverViewDO?.laborHours?.goal?.value
            )
            val laborHoursVarianceDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.laborHours?.variance?.amount,
                laborDataRangeOverViewDO?.laborHours?.variance?.percentage,
                laborDataRangeOverViewDO?.laborHours?.variance?.value
            )
            val laborHoursActualDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.laborHours?.actual?.amount,
                laborDataRangeOverViewDO?.laborHours?.actual?.percentage,
                laborDataRangeOverViewDO?.laborHours?.actual?.value
            )
            labourHours(
                laborHoursGoalDORange,
                laborHoursVarianceDORange,
                laborHoursActualDORange
            )

            if (laborDataRangeOverViewDO?.laborHours?.status != null) {
                when {
                    laborDataRangeOverViewDO.laborHours.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewDO.laborHours.status.toString() == resources.getString(R.string.under_limit) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // breaks sales
            val breakPercentageDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.breaks?.total?.actual?.amount,
                laborDataRangeOverViewDO?.breaks?.total?.actual?.percentage,
                laborDataRangeOverViewDO?.breaks?.total?.actual?.value
            )
            if (breakPercentageDORange.isEmpty()) {

                breaks_error.visibility = View.VISIBLE
                breaks_percentage.visibility = View.GONE
            } else {
                breaks_percentage.text = breakPercentageDORange
            }

            if (laborDataRangeOverViewDO?.breaks?.total?.status != null) {
                when {
                    laborDataRangeOverViewDO.breaks.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewDO.breaks.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            //Missed Break
            val laborMissedBreakQuantityDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.breaks?.breakQuantity?.actual?.amount,
                laborDataRangeOverViewDO?.breaks?.breakQuantity?.actual?.percentage,
                laborDataRangeOverViewDO?.breaks?.breakQuantity?.actual?.value
            )
            val laborMissedBreakDollarDORange: String = if(laborDataRangeOverViewDO?.breaks?.laborWithBreaks?.actual?.amount == null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataRangeOverViewDO.breaks.laborWithBreaks.actual.amount)
                )
            }

            val laborMissedBreakPercentageDORange: String = if(laborDataRangeOverViewDO?.breaks?.laborWithBreaks?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataRangeOverViewDO.breaks.laborWithBreaks.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }

            val laborMissedBreakOTDollarDORange: String = if(laborDataRangeOverViewDO?.breaks?.laborWithoutBreaksAndOT?.actual?.amount ==  null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataRangeOverViewDO.breaks.laborWithoutBreaksAndOT.actual.amount)
                )
            }

            val laborMissedBreakOTPercentageDORange : String = if(laborDataRangeOverViewDO?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataRangeOverViewDO.breaks.laborWithoutBreaksAndOT.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }


            laborMissedBreaksData(
                laborMissedBreakQuantityDORange,
                laborMissedBreakDollarDORange,
                laborMissedBreakPercentageDORange,
                laborMissedBreakOTDollarDORange,
                laborMissedBreakOTPercentageDORange
            )

            // Driver OT Full
            val laborOtDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.driverOTFull?.actual?.amount,
                laborDataRangeOverViewDO?.driverOTFull?.actual?.percentage,
                laborDataRangeOverViewDO?.driverOTFull?.actual?.value
            )
            labourDriverOT(laborOtDORange)

            if (laborDataRangeOverViewDO?.driverOTFull?.status != null) {
                when {
                    laborDataRangeOverViewDO.driverOTFull.status.toString() == resources.getString(R.string.out_of_range) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewDO.driverOTFull.status.toString() == resources.getString(R.string.under_limit) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // driver ot premium
            val laborOtPremiumSupervisorYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.driverOTPremium?.actual?.amount,
                laborDataRangeOverViewDO?.driverOTPremium?.actual?.percentage,
                laborDataRangeOverViewDO?.driverOTPremium?.actual?.value
            )
            labourDriverOTPremium(laborOtPremiumSupervisorYesterday)

            if (laborDataRangeOverViewDO?.driverOTPremium?.status != null) {
                when {
                    laborDataRangeOverViewDO.driverOTPremium.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewDO.driverOTPremium.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // staffing
            staffing_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.staffing?.total?.actual?.amount,
                laborDataRangeOverViewDO?.staffing?.total?.actual?.percentage,
                laborDataRangeOverViewDO?.staffing?.total?.actual?.value
            )
            if (laborDataRangeOverViewDO?.staffing?.total?.status != null) {
                staffing_percentage.text =
                        Validation().ignoreZeroAfterDecimal(laborDataRangeOverViewDO.staffing.total.actual?.value)
                when {
                    laborDataRangeOverViewDO.staffing.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeOverViewDO.staffing.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val totalTmCountDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.staffing?.totalTMCount?.actual?.amount,
                laborDataRangeOverViewDO?.staffing?.totalTMCount?.actual?.percentage,
                laborDataRangeOverViewDO?.staffing?.totalTMCount?.actual?.value
            )
            val totalInsiderDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.staffing?.insiders?.actual?.amount,
                laborDataRangeOverViewDO?.staffing?.insiders?.actual?.percentage,
                laborDataRangeOverViewDO?.staffing?.insiders?.actual?.value
            )
            val totalDriversDORange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.staffing?.drivers?.actual?.amount,
                laborDataRangeOverViewDO?.staffing?.drivers?.actual?.percentage,
                laborDataRangeOverViewDO?.staffing?.drivers?.actual?.value
            )
            labourStaffing(totalTmCountDORange, totalInsiderDORange, totalDriversDORange)

            tm_count_less_than_thirty_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.staffing?.tmCountLessThan30Hours?.actual?.amount,
                laborDataRangeOverViewDO?.staffing?.tmCountLessThan30Hours?.actual?.percentage,
                laborDataRangeOverViewDO?.staffing?.tmCountLessThan30Hours?.actual?.value
            )
            tm_count_grater_than_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeOverViewDO?.staffing?.tmCountMoreThan30Hours?.actual?.amount,
                laborDataRangeOverViewDO?.staffing?.tmCountMoreThan30Hours?.actual?.percentage,
                laborDataRangeOverViewDO?.staffing?.tmCountMoreThan30Hours?.actual?.value
            )

        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Labor Overview Period Range KPI")
        }
    }


    // supervisor
    private fun todayViewSupervisorLabour(laborTodayDetail: SupervisorOverviewTodayQuery.Supervisor) {
        try {
            val laborDataTodayViewSupervisor = laborTodayDetail.kpis?.stores?.today?.labor

            Logger.info("Labor Today", "Labor Overview KPI")

            // display name
            labour_display.text = getString(R.string.labour_text)
            breaks_qty_text.text =
                laborDataTodayViewSupervisor?.breaks?.breakQuantity?.displayName
                    ?: getString(R.string.breaks_qty_text)
            labor_breaks_dollar_text.text =
                laborDataTodayViewSupervisor?.breaks?.laborWithBreaks?.displayName
            labor_breaks_percentage_text.text =
                laborDataTodayViewSupervisor?.breaks?.laborWithBreaks?.displayName
            labor_breaks_ot_percentage_text.text =
                laborDataTodayViewSupervisor?.breaks?.laborWithoutBreaksAndOT?.displayName
            labor_breaks_ot_dollar_text.text =
                laborDataTodayViewSupervisor?.breaks?.laborWithoutBreaksAndOT?.displayName
            drive_ot_display.text = laborDataTodayViewSupervisor?.driverOTFull?.displayName
            driver_ot_premium_display.text =
                laborDataTodayViewSupervisor?.driverOTPremium?.displayName
            // display name below
              if (laborDataTodayViewSupervisor?.laborvsGoal == null || laborDataTodayViewSupervisor.laborvsGoal.displayName.isNullOrEmpty()) {
                ll_labor_display1.visibility = View.GONE
            } else {
                ll_labor_display1.visibility = View.VISIBLE
                labor_display1.text = laborDataTodayViewSupervisor.laborvsGoal.displayName
            }
            if (laborDataTodayViewSupervisor?.salesPerLaborHour == null || laborDataTodayViewSupervisor.salesPerLaborHour.displayName.isNullOrEmpty()) {
                ll_labor_display2.visibility = View.GONE
            } else {
                ll_labor_display2.visibility = View.VISIBLE
                labor_display2.text = laborDataTodayViewSupervisor.salesPerLaborHour.displayName
            }
            if (laborDataTodayViewSupervisor?.laborHours == null || laborDataTodayViewSupervisor.laborHours.displayName.isNullOrEmpty()) {
                ll_labor_display3.visibility = View.GONE
            } else {
                ll_labor_display3.visibility = View.VISIBLE
                labor_display3.text = laborDataTodayViewSupervisor.laborHours.displayName
            }
            if (laborDataTodayViewSupervisor?.laborvsManagerBudget == null || laborDataTodayViewSupervisor.laborvsManagerBudget.displayName.isNullOrEmpty()) {
                ll_labor_display4.visibility = View.GONE
            } else {
                ll_labor_display4.visibility = View.VISIBLE
                labor_display4.text = laborDataTodayViewSupervisor.laborvsManagerBudget.displayName
            }

            if (laborDataTodayViewSupervisor?.staffing?.total == null || laborDataTodayViewSupervisor.staffing.total.displayName.isNullOrEmpty()) {
                ll_staffing.visibility = View.GONE
            } else {
                ll_staffing.visibility = View.VISIBLE
                staffing_display.text = laborDataTodayViewSupervisor.staffing.total.displayName
            }

            if (laborDataTodayViewSupervisor?.staffing?.totalTMCount == null || laborDataTodayViewSupervisor.staffing.totalTMCount.displayName.isNullOrEmpty()) {
                ll_total_tm_count_text.visibility = View.GONE
            } else {
                ll_total_tm_count_text.visibility = View.VISIBLE
                total_tm_count_text.text = laborDataTodayViewSupervisor.staffing.totalTMCount.displayName
            }
            if (laborDataTodayViewSupervisor?.staffing?.insiders == null || laborDataTodayViewSupervisor.staffing.insiders.displayName.isNullOrEmpty()) {
                ll_insider_text.visibility = View.GONE
            } else {
                ll_insider_text.visibility = View.VISIBLE
                insider_text.text = laborDataTodayViewSupervisor.staffing.insiders.displayName
            }
            if (laborDataTodayViewSupervisor?.staffing?.drivers == null || laborDataTodayViewSupervisor.staffing.drivers.displayName.isNullOrEmpty()) {
                ll_drivers_text.visibility = View.GONE
            } else {
                ll_drivers_text.visibility = View.VISIBLE
                drivers_text.text = laborDataTodayViewSupervisor.staffing.drivers.displayName
            }
            if (laborDataTodayViewSupervisor?.staffing?.tmCountLessThan30Hours == null || laborDataTodayViewSupervisor.staffing.tmCountLessThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_less_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_less_than_thirty_parent.visibility = View.VISIBLE
                tm_count_less_than_thirty_text.text =
                    laborDataTodayViewSupervisor.staffing.tmCountLessThan30Hours.displayName
            }
            if (laborDataTodayViewSupervisor?.staffing?.tmCountMoreThan30Hours == null || laborDataTodayViewSupervisor.staffing.tmCountMoreThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_grater_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_grater_than_thirty_parent.visibility = View.VISIBLE
                tm_count_grater_than_thirty_text.text =
                    laborDataTodayViewSupervisor.staffing.tmCountMoreThan30Hours.displayName
            }

            if (laborDataTodayViewSupervisor?.actual?.percentage?.isNaN() == false && laborDataTodayViewSupervisor.status != null) {
                labour_sales.text =
                    getString(R.string.dollar_text).plus(
                        Validation().dollarFormatting(
                            laborDataTodayViewSupervisor.laborvsGoal?.actual?.amount
                        )
                    )

                when {
                    laborDataTodayViewSupervisor.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labour_sales.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewSupervisor.status.toString() == resources.getString(R.string.under_limit) -> {
                        labour_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        labour_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // scroll detect

            labor_scroll.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (labor_scroll.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text =
                                laborDataTodayViewSupervisor?.displayName
                                    ?: getString(R.string.labour_text)

                            if (laborDataTodayViewSupervisor?.actual?.percentage?.isNaN() == false && laborDataTodayViewSupervisor.status != null) {
                                level_two_scroll_data_action_value.text =
                                    Validation().ignoreZeroAfterDecimal(laborDataTodayViewSupervisor.actual.percentage)
                                        .plus(getString(R.string.percentage_text))
                                when {
                                    laborDataTodayViewSupervisor.status.toString() == resources.getString(
                                        R.string.out_of_range
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    laborDataTodayViewSupervisor.status.toString() == resources.getString(
                                        R.string.under_limit
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    }
                                    else -> {
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.black_circle,
                                            0
                                        )
                                    }
                                }
                            }
                        }
                        y = labor_scroll.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })



            labour_goal_value.text =
                if (laborDataTodayViewSupervisor?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.laborvsGoal.goal.amount)
                )
                else ""
            labour_variance_value.text =
                if (laborDataTodayViewSupervisor?.laborvsGoal?.variance?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.laborvsGoal.variance.amount)
                )
                else ""
            showLaborNarrativeData(laborDataTodayViewSupervisor?.narrative.toString())


            // Labor vs goal
            labor_vs_goal_goal.text =
                if (laborDataTodayViewSupervisor?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.laborvsGoal.goal.amount)
                ) else ""

            labour_goal_value.text =
                if (laborDataTodayViewSupervisor?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.laborvsGoal.goal.amount)
                ) else ""
            labor_vs_goal_goal.text =
                if (laborDataTodayViewSupervisor?.laborvsGoal?.goal?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.laborvsGoal.goal.amount)
                ) else ""

            labor_vs_goal_variance.text =
                if (laborDataTodayViewSupervisor?.laborvsGoal?.variance?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.laborvsGoal.variance.amount)
                ) else ""

            if (laborDataTodayViewSupervisor?.laborvsGoal?.actual?.amount?.isNaN() == false && laborDataTodayViewSupervisor.laborvsGoal.status != null) {
                labor_vs_goal_actual.text =
                    getString(R.string.dollar_text).plus(
                        Validation().dollarFormatting(
                            laborDataTodayViewSupervisor.laborvsGoal.actual.amount
                        )
                    )
                when {
                    laborDataTodayViewSupervisor.laborvsGoal.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewSupervisor.laborvsGoal.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // sales per labour

            if (laborDataTodayViewSupervisor?.salesPerLaborHour?.actual?.value?.isNaN() == false && laborDataTodayViewSupervisor.salesPerLaborHour.status != null) {
                sales_labor_vs_goal_actual.text =
                    Validation().ignoreZeroAfterDecimal(laborDataTodayViewSupervisor.salesPerLaborHour.actual.value)
                when {
                    laborDataTodayViewSupervisor.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewSupervisor.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }

            }


            // Labor vs management

            labor_vs_mgmt_goal_goal.text =
                if (laborDataTodayViewSupervisor?.laborvsManagerBudget?.goal?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.laborvsManagerBudget.goal.amount)
                ) else ""
            labor_vs_mgmt_goal_variance.text =
                if (laborDataTodayViewSupervisor?.laborvsManagerBudget?.variance?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.laborvsManagerBudget.variance.amount)
                ) else ""

            if (laborDataTodayViewSupervisor?.laborvsManagerBudget?.actual?.amount?.isNaN() == false && laborDataTodayViewSupervisor.laborvsManagerBudget.status != null) {
                labor_vs_mgmt_goal_actual.text = getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.laborvsManagerBudget.actual.amount)
                )
                when {
                    laborDataTodayViewSupervisor.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewSupervisor.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // labor hour
            labor_hrs_vs_goal_goal.text =
                Validation().ignoreZeroAfterDecimal(laborDataTodayViewSupervisor?.laborHours?.goal?.value)
            labor_hrs_vs_goal_variance.text =
                Validation().ignoreZeroAfterDecimal(laborDataTodayViewSupervisor?.laborHours?.variance?.value)

            if (laborDataTodayViewSupervisor?.laborHours?.actual?.value?.isNaN() == false && laborDataTodayViewSupervisor.laborHours.status != null) {
                labor_hrs_vs_goal_actual.text =
                    Validation().ignoreZeroAfterDecimal(laborDataTodayViewSupervisor.laborHours.actual.value)
                when {
                    laborDataTodayViewSupervisor.laborHours.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewSupervisor.laborHours.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // breaks sales

            if (laborDataTodayViewSupervisor?.breaks?.total?.actual?.amount?.isNaN() == false && laborDataTodayViewSupervisor.breaks.total.status != null) {
                breaks_percentage.text = getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.breaks.total.actual.amount)
                )
                when {
                    laborDataTodayViewSupervisor.breaks.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewSupervisor.breaks.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            breaks_qty_percentage.text = Validation().ignoreZeroAfterDecimal(
                laborDataTodayViewSupervisor?.breaks?.total?.actual?.amount
            )

            breaks_break_dollar_percentage.text =
                if (laborDataTodayViewSupervisor?.breaks?.laborWithBreaks?.actual?.amount?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.breaks.laborWithBreaks.actual.amount)
                ) else ""

            labor_breaks_percentage.text =
                if (laborDataTodayViewSupervisor?.breaks?.laborWithBreaks?.actual?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewSupervisor.breaks.laborWithBreaks.actual.percentage
                )
                    .plus(getString(R.string.percentage_text)) else ""


            labor_breaks_ot_dollar.text =
                if (laborDataTodayViewSupervisor?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage?.isNaN() == false) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.breaks.laborWithoutBreaksAndOT.actual.percentage)
                ) else ""


            labor_breaks_ot_percentage.text =
                if (laborDataTodayViewSupervisor?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewSupervisor.breaks.laborWithoutBreaksAndOT.actual.percentage
                )
                    .plus(getString(R.string.percentage_text)) else ""

            // driver


            if (laborDataTodayViewSupervisor?.driverOTFull?.actual?.amount?.isNaN() == false && laborDataTodayViewSupervisor.driverOTFull.status != null) {
                driver_ot_percentage.text = getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.driverOTFull.actual.amount)
                )
                when {
                    laborDataTodayViewSupervisor.driverOTFull.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewSupervisor.driverOTFull.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // driver ot premium
            if (laborDataTodayViewSupervisor?.driverOTPremium?.actual?.amount?.isNaN() == false && laborDataTodayViewSupervisor.driverOTPremium.status != null) {
                driver_ot_premium_percentage.text = getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataTodayViewSupervisor.driverOTPremium.actual.amount)
                )
                when {
                    laborDataTodayViewSupervisor.driverOTPremium.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataTodayViewSupervisor.driverOTPremium.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // staffing
            if (laborDataTodayViewSupervisor?.staffing?.total != null) {
                if (laborDataTodayViewSupervisor.staffing.total.actual?.value?.isNaN() == false && laborDataTodayViewSupervisor.staffing.total.status != null) {
                    staffing_percentage.text =
                        Validation().ignoreZeroAfterDecimal(laborDataTodayViewSupervisor.staffing.total.actual.value)
                    when {
                        laborDataTodayViewSupervisor.staffing.total.status.toString() == resources.getString(
                            R.string.out_of_range
                        ) -> {
                            staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.red_circle,
                                0
                            )
                            staffing_percentage.setTextColor(getColor(R.color.red))
                        }
                        laborDataTodayViewSupervisor.staffing.total.status.toString() == resources.getString(
                            R.string.under_limit
                        ) -> {
                            staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.green_circle,
                                0
                            )
                            staffing_percentage.setTextColor(getColor(R.color.green))

                        }
                        else -> {
                            staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.black_circle,
                                0
                            )
                            staffing_percentage.setTextColor(getColor(R.color.text_color))

                        }
                    }
                }
            }

            total_tm_count_percentage.text =
                if (laborDataTodayViewSupervisor?.staffing?.totalTMCount?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewSupervisor.staffing.totalTMCount.actual.value
                ) else ""
            insider_percentage.text =
                if (laborDataTodayViewSupervisor?.staffing?.insiders?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewSupervisor.staffing.insiders.actual.value
                ) else ""
            drivers_percentage.text =
                if (laborDataTodayViewSupervisor?.staffing?.drivers?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewSupervisor.staffing.drivers.actual.value
                ) else ""
            tm_count_less_than_thirty_percentage.text =
                if (laborDataTodayViewSupervisor?.staffing?.tmCountLessThan30Hours?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewSupervisor.staffing.tmCountLessThan30Hours.actual.value
                ) else ""
            tm_count_grater_than_percentage.text =
                if (laborDataTodayViewSupervisor?.staffing?.tmCountMoreThan30Hours?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    laborDataTodayViewSupervisor.staffing.tmCountMoreThan30Hours.actual.value
                ) else ""
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun yesterdayViewSupervisor(laborTodayDetail: SupervisorOverviewYesterdayQuery.Supervisor) {
        try {
            val laborDataYesterdayViewSupervisor = laborTodayDetail.kpis?.stores?.yesterday?.labor

            Logger.info("Labor Yesterday", "Labor Overview KPI")

            // display name
            Validation().checkNullValueToShowView(
                this,
                laborDataYesterdayViewSupervisor?.staffing?.tmCountLessThan30Hours?.displayName,
                tm_count_less_than_thirty_parent
            )
            Validation().checkNullValueToShowView(
                this,
                laborDataYesterdayViewSupervisor?.staffing?.tmCountMoreThan30Hours?.displayName,
                tm_count_grater_than_thirty_parent
            )

            labour_display.text = getString(R.string.labour_text)
            drive_ot_display.text = laborDataYesterdayViewSupervisor?.driverOTFull?.displayName
            driver_ot_premium_display.text =
                laborDataYesterdayViewSupervisor?.driverOTPremium?.displayName
            // display name below
            if (laborDataYesterdayViewSupervisor?.laborvsGoal == null || laborDataYesterdayViewSupervisor.laborvsGoal.displayName.isNullOrEmpty()) {
                ll_labor_display1.visibility = View.GONE
            } else {
                ll_labor_display1.visibility = View.VISIBLE
                labor_display1.text = laborDataYesterdayViewSupervisor.laborvsGoal.displayName
            }
            if (laborDataYesterdayViewSupervisor?.salesPerLaborHour == null || laborDataYesterdayViewSupervisor.salesPerLaborHour.displayName.isNullOrEmpty()) {
                ll_labor_display2.visibility = View.GONE
            } else {
                ll_labor_display2.visibility = View.VISIBLE
                labor_display2.text = laborDataYesterdayViewSupervisor.salesPerLaborHour.displayName
            }
            if (laborDataYesterdayViewSupervisor?.laborHours == null || laborDataYesterdayViewSupervisor.laborHours.displayName.isNullOrEmpty()) {
                ll_labor_display3.visibility = View.GONE
            } else {
                ll_labor_display3.visibility = View.VISIBLE
                labor_display3.text = laborDataYesterdayViewSupervisor.laborHours.displayName
            }
            if (laborDataYesterdayViewSupervisor?.laborvsManagerBudget == null || laborDataYesterdayViewSupervisor.laborvsManagerBudget.displayName.isNullOrEmpty()) {
                ll_labor_display4.visibility = View.GONE
            } else {
                ll_labor_display4.visibility = View.VISIBLE
                labor_display4.text = laborDataYesterdayViewSupervisor.laborvsManagerBudget.displayName
            }

            if (laborDataYesterdayViewSupervisor?.staffing?.total == null || laborDataYesterdayViewSupervisor.staffing.total.displayName.isNullOrEmpty()) {
                ll_staffing.visibility = View.GONE
            } else {
                ll_staffing.visibility = View.VISIBLE
                staffing_display.text = laborDataYesterdayViewSupervisor.staffing.total.displayName
            }

            if (laborDataYesterdayViewSupervisor?.staffing?.totalTMCount == null || laborDataYesterdayViewSupervisor.staffing.totalTMCount.displayName.isNullOrEmpty()) {
                ll_total_tm_count_text.visibility = View.GONE
            } else {
                ll_total_tm_count_text.visibility = View.VISIBLE
                total_tm_count_text.text = laborDataYesterdayViewSupervisor.staffing.totalTMCount.displayName
            }
            if (laborDataYesterdayViewSupervisor?.staffing?.insiders == null || laborDataYesterdayViewSupervisor.staffing.insiders.displayName.isNullOrEmpty()) {
                ll_insider_text.visibility = View.GONE
            } else {
                ll_insider_text.visibility = View.VISIBLE
                insider_text.text = laborDataYesterdayViewSupervisor.staffing.insiders.displayName
            }
            if (laborDataYesterdayViewSupervisor?.staffing?.drivers == null || laborDataYesterdayViewSupervisor.staffing.drivers.displayName.isNullOrEmpty()) {
                ll_drivers_text.visibility = View.GONE
            } else {
                ll_drivers_text.visibility = View.VISIBLE
                drivers_text.text = laborDataYesterdayViewSupervisor.staffing.drivers.displayName
            }
            if (laborDataYesterdayViewSupervisor?.staffing?.tmCountLessThan30Hours == null || laborDataYesterdayViewSupervisor.staffing.tmCountLessThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_less_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_less_than_thirty_parent.visibility = View.VISIBLE
                tm_count_less_than_thirty_text.text =
                    laborDataYesterdayViewSupervisor.staffing.tmCountLessThan30Hours.displayName
            }
            if (laborDataYesterdayViewSupervisor?.staffing?.tmCountMoreThan30Hours == null || laborDataYesterdayViewSupervisor.staffing.tmCountMoreThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_grater_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_grater_than_thirty_parent.visibility = View.VISIBLE
                tm_count_grater_than_thirty_text.text =
                    laborDataYesterdayViewSupervisor.staffing.tmCountMoreThan30Hours.displayName
            }

            // break display
            break_display.text = laborDataYesterdayViewSupervisor?.breaks?.total?.displayName
            breaks_qty_text.text =
                laborDataYesterdayViewSupervisor?.breaks?.breakQuantity?.displayName
            labor_breaks_dollar_text.text =
                laborDataYesterdayViewSupervisor?.breaks?.laborWithBreaks?.displayName
            labor_breaks_percentage_text.text =
                laborDataYesterdayViewSupervisor?.breaks?.laborWithBreaks?.displayName
            labor_breaks_ot_percentage_text.text =
                laborDataYesterdayViewSupervisor?.breaks?.laborWithoutBreaksAndOT?.displayName
            labor_breaks_ot_dollar_text.text =
                laborDataYesterdayViewSupervisor?.breaks?.laborWithoutBreaksAndOT?.displayName
            // end break

            labour_sales.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.actual?.amount,
                laborDataYesterdayViewSupervisor?.actual?.percentage,
                laborDataYesterdayViewSupervisor?.actual?.value
            )

            if (laborDataYesterdayViewSupervisor?.status != null) {
                when {
                    laborDataYesterdayViewSupervisor.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labour_sales.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewSupervisor.status.toString() == resources.getString(R.string.under_limit) -> {
                        labour_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        labour_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // scroll detect

            labor_scroll.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (labor_scroll.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            /* level_two_scroll_data_action.text =
                                 laborDataYesterdayViewSupervisor?.displayName ?: getString(R.string.labour_text)*/
                            level_two_scroll_data_action.text = getString(R.string.labour_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@LabourKpiActivity,
                                    laborDataYesterdayViewSupervisor?.actual?.amount,
                                    laborDataYesterdayViewSupervisor?.actual?.percentage,
                                    laborDataYesterdayViewSupervisor?.actual?.value
                                )

                            if (laborDataYesterdayViewSupervisor?.status != null) {
                                when {
                                    laborDataYesterdayViewSupervisor.status.toString() == resources.getString(
                                        R.string.out_of_range
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    laborDataYesterdayViewSupervisor.status.toString() == resources.getString(
                                        R.string.under_limit
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    }
                                    else -> {
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.black_circle,
                                            0
                                        )
                                    }
                                }
                            }
                        }
                        y = labor_scroll.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            labour_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.goal?.amount,
                laborDataYesterdayViewSupervisor?.goal?.percentage,
                laborDataYesterdayViewSupervisor?.goal?.value
            )
            labour_variance_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.variance?.amount,
                laborDataYesterdayViewSupervisor?.variance?.percentage,
                laborDataYesterdayViewSupervisor?.variance?.value
            )
            showLaborNarrativeData(laborDataYesterdayViewSupervisor?.narrative.toString())

            // Labor vs goal
            val laborGoalSupervisorYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.laborvsGoal?.goal?.amount,
                laborDataYesterdayViewSupervisor?.laborvsGoal?.goal?.percentage,
                laborDataYesterdayViewSupervisor?.laborvsGoal?.goal?.value
            )
            val laborVarianceSupervisorYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.laborvsGoal?.variance?.amount,
                laborDataYesterdayViewSupervisor?.laborvsGoal?.variance?.percentage,
                laborDataYesterdayViewSupervisor?.laborvsGoal?.variance?.value
            )
            val laborActualSupervisorYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.laborvsGoal?.actual?.amount,
                laborDataYesterdayViewSupervisor?.laborvsGoal?.actual?.percentage,
                laborDataYesterdayViewSupervisor?.laborvsGoal?.actual?.value
            )
            labourGoal(
                laborGoalSupervisorYesterday,
                laborVarianceSupervisorYesterday,
                laborActualSupervisorYesterday
            )

            if (laborDataYesterdayViewSupervisor?.laborvsGoal?.status != null) {
                when {
                    laborDataYesterdayViewSupervisor.laborvsGoal.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewSupervisor.laborvsGoal.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // sales per labour
            val laborSalesHoursActualSupervisorYesterday =
                Validation().checkAmountPercentageValue(
                    this,
                    laborDataYesterdayViewSupervisor?.salesPerLaborHour?.actual?.amount,
                    laborDataYesterdayViewSupervisor?.salesPerLaborHour?.actual?.percentage,
                    laborDataYesterdayViewSupervisor?.salesPerLaborHour?.actual?.value
                )
            labourSalesHrs(laborSalesHoursActualSupervisorYesterday)

            if (laborDataYesterdayViewSupervisor?.salesPerLaborHour?.status != null) {
                when {
                    laborDataYesterdayViewSupervisor.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewSupervisor.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }

            }


            // Labor vs management
            val laborManagementsGoalSupervisorYesterday =
                Validation().checkAmountPercentageValue(
                    this,
                    laborDataYesterdayViewSupervisor?.laborvsManagerBudget?.goal?.amount,
                    laborDataYesterdayViewSupervisor?.laborvsManagerBudget?.goal?.percentage,
                    laborDataYesterdayViewSupervisor?.laborvsManagerBudget?.goal?.value
                )
            val laborManagementsVarianceSupervisorYesterday =
                Validation().checkAmountPercentageValue(
                    this,
                    laborDataYesterdayViewSupervisor?.laborvsManagerBudget?.variance?.amount,
                    laborDataYesterdayViewSupervisor?.laborvsManagerBudget?.variance?.percentage,
                    laborDataYesterdayViewSupervisor?.laborvsManagerBudget?.variance?.value
                )
            val laborManagementsActualSupervisorYesterday =
                Validation().checkAmountPercentageValue(
                    this,
                    laborDataYesterdayViewSupervisor?.laborvsManagerBudget?.actual?.amount,
                    laborDataYesterdayViewSupervisor?.laborvsManagerBudget?.actual?.percentage,
                    laborDataYesterdayViewSupervisor?.laborvsManagerBudget?.actual?.value
                )
            labourManagement(
                laborManagementsGoalSupervisorYesterday,
                laborManagementsVarianceSupervisorYesterday,
                laborManagementsActualSupervisorYesterday
            )

            if (laborDataYesterdayViewSupervisor?.laborvsManagerBudget?.status != null) {
                when {
                    laborDataYesterdayViewSupervisor.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewSupervisor.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // labor hour
            val laborHoursGoalSupervisorYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.laborHours?.goal?.amount,
                laborDataYesterdayViewSupervisor?.laborHours?.goal?.percentage,
                laborDataYesterdayViewSupervisor?.laborHours?.goal?.value
            )
            val laborHoursVarianceSupervisorYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.laborHours?.variance?.amount,
                laborDataYesterdayViewSupervisor?.laborHours?.variance?.percentage,
                laborDataYesterdayViewSupervisor?.laborHours?.variance?.value
            )
            val laborHoursActualSupervisorYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.laborHours?.actual?.amount,
                laborDataYesterdayViewSupervisor?.laborHours?.actual?.percentage,
                laborDataYesterdayViewSupervisor?.laborHours?.actual?.value
            )
            labourHours(
                laborHoursGoalSupervisorYesterday,
                laborHoursVarianceSupervisorYesterday,
                laborHoursActualSupervisorYesterday
            )

            if (laborDataYesterdayViewSupervisor?.laborHours?.status != null) {
                when {
                    laborDataYesterdayViewSupervisor.laborHours.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewSupervisor.laborHours.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // breaks sales
            val breakPercentageSupervisorYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.breaks?.total?.actual?.amount,
                laborDataYesterdayViewSupervisor?.breaks?.total?.actual?.percentage,
                laborDataYesterdayViewSupervisor?.breaks?.total?.actual?.value
            )
            if (breakPercentageSupervisorYesterday.isEmpty()) {

                breaks_error.visibility = View.VISIBLE
                breaks_percentage.visibility = View.GONE
            } else {
                breaks_percentage.text = breakPercentageSupervisorYesterday
            }

            //Missed Break
            if (laborDataYesterdayViewSupervisor?.breaks?.total?.status != null) {
                when {
                    laborDataYesterdayViewSupervisor.breaks.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewSupervisor.breaks.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val laborMissedBreakQuantitySupervisorYesterday =
                Validation().checkAmountPercentageValue(
                    this,
                    laborDataYesterdayViewSupervisor?.breaks?.breakQuantity?.actual?.amount,
                    laborDataYesterdayViewSupervisor?.breaks?.breakQuantity?.actual?.percentage,
                    laborDataYesterdayViewSupervisor?.breaks?.breakQuantity?.actual?.value
                )


            val laborMissedBreakDollarSupervisorYesterday: String = if(laborDataYesterdayViewSupervisor?.breaks?.laborWithBreaks?.actual?.amount == null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataYesterdayViewSupervisor.breaks.laborWithBreaks.actual.amount)
                )
            }

            val laborMissedBreakPercentageSupervisorYesterday: String = if(laborDataYesterdayViewSupervisor?.breaks?.laborWithBreaks?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataYesterdayViewSupervisor.breaks.laborWithBreaks.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }

            val laborMissedBreakOTDollarSupervisorYesterday: String = if(laborDataYesterdayViewSupervisor?.breaks?.laborWithoutBreaksAndOT?.actual?.amount ==  null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataYesterdayViewSupervisor.breaks.laborWithoutBreaksAndOT.actual.amount)
                )
            }

            val laborMissedBreakOTPercentageSupervisorYesterday : String = if(laborDataYesterdayViewSupervisor?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataYesterdayViewSupervisor.breaks.laborWithoutBreaksAndOT.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }

            laborMissedBreaksData(
                laborMissedBreakQuantitySupervisorYesterday,
                laborMissedBreakDollarSupervisorYesterday,
                laborMissedBreakPercentageSupervisorYesterday,
                laborMissedBreakOTDollarSupervisorYesterday,
                laborMissedBreakOTPercentageSupervisorYesterday
            )

            // driver OT full
            val laborOtSupervisorYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.driverOTFull?.actual?.amount,
                laborDataYesterdayViewSupervisor?.driverOTFull?.actual?.percentage,
                laborDataYesterdayViewSupervisor?.driverOTFull?.actual?.value
            )
            labourDriverOT(laborOtSupervisorYesterday)

            if (laborDataYesterdayViewSupervisor?.driverOTFull?.status != null) {
                when {
                    laborDataYesterdayViewSupervisor.driverOTFull.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewSupervisor.driverOTFull.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // driver ot premium
            val laborOtPremiumSupervisorYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.driverOTPremium?.actual?.amount,
                laborDataYesterdayViewSupervisor?.driverOTPremium?.actual?.percentage,
                laborDataYesterdayViewSupervisor?.driverOTPremium?.actual?.value
            )
            labourDriverOTPremium(laborOtPremiumSupervisorYesterday)

            if (laborDataYesterdayViewSupervisor?.driverOTPremium?.status != null) {
                when {
                    laborDataYesterdayViewSupervisor.driverOTPremium.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewSupervisor.driverOTPremium.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // staffing
            staffing_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.staffing?.total?.actual?.amount,
                laborDataYesterdayViewSupervisor?.staffing?.total?.actual?.percentage,
                laborDataYesterdayViewSupervisor?.staffing?.total?.actual?.value
            )
            if (laborDataYesterdayViewSupervisor?.staffing?.total?.status != null) {
                staffing_percentage.text =
                        Validation().ignoreZeroAfterDecimal(laborDataYesterdayViewSupervisor.staffing.total.actual?.value)
                when {
                    laborDataYesterdayViewSupervisor.staffing.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataYesterdayViewSupervisor.staffing.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val totalTmCountSupervisorYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.staffing?.totalTMCount?.actual?.amount,
                laborDataYesterdayViewSupervisor?.staffing?.totalTMCount?.actual?.percentage,
                laborDataYesterdayViewSupervisor?.staffing?.totalTMCount?.actual?.value
            )
            val totalInsiderSupervisorYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.staffing?.insiders?.actual?.amount,
                laborDataYesterdayViewSupervisor?.staffing?.insiders?.actual?.percentage,
                laborDataYesterdayViewSupervisor?.staffing?.insiders?.actual?.value
            )
            val totalDriversSupervisorYesterday = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.staffing?.drivers?.actual?.amount,
                laborDataYesterdayViewSupervisor?.staffing?.drivers?.actual?.percentage,
                laborDataYesterdayViewSupervisor?.staffing?.drivers?.actual?.value
            )
            labourStaffing(
                totalTmCountSupervisorYesterday,
                totalInsiderSupervisorYesterday,
                totalDriversSupervisorYesterday
            )

            tm_count_less_than_thirty_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.staffing?.tmCountLessThan30Hours?.actual?.amount,
                laborDataYesterdayViewSupervisor?.staffing?.tmCountLessThan30Hours?.actual?.percentage,
                laborDataYesterdayViewSupervisor?.staffing?.tmCountLessThan30Hours?.actual?.value
            )
            tm_count_grater_than_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataYesterdayViewSupervisor?.staffing?.tmCountMoreThan30Hours?.actual?.amount,
                laborDataYesterdayViewSupervisor?.staffing?.tmCountMoreThan30Hours?.actual?.percentage,
                laborDataYesterdayViewSupervisor?.staffing?.tmCountMoreThan30Hours?.actual?.value
            )

        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Labor Overview Yesterday KPI")
        }
    }

    private fun rangeViewSupervisor(laborTodayDetail: SupervisorOverviewRangeQuery.Supervisor) {
        try {
            val laborDataRangeViewSupervisor = laborTodayDetail.kpis?.stores?.period?.labor

            Logger.info("Labor Period Range", "Labor Overview KPI")

            // display name
            Validation().checkNullValueToShowView(
                this,
                laborDataRangeViewSupervisor?.staffing?.tmCountLessThan30Hours?.displayName,
                tm_count_less_than_thirty_parent
            )
            Validation().checkNullValueToShowView(
                this,
                laborDataRangeViewSupervisor?.staffing?.tmCountMoreThan30Hours?.displayName,
                tm_count_grater_than_thirty_parent
            )

            labour_display.text = getString(R.string.labour_text)
            drive_ot_display.text = laborDataRangeViewSupervisor?.driverOTFull?.displayName
            driver_ot_premium_display.text =
                laborDataRangeViewSupervisor?.driverOTPremium?.displayName
            // display name below
            if (laborDataRangeViewSupervisor?.laborvsGoal == null || laborDataRangeViewSupervisor.laborvsGoal.displayName.isNullOrEmpty()) {
                ll_labor_display1.visibility = View.GONE
            } else {
                ll_labor_display1.visibility = View.VISIBLE
                labor_display1.text = laborDataRangeViewSupervisor.laborvsGoal.displayName
            }
            if (laborDataRangeViewSupervisor?.salesPerLaborHour == null || laborDataRangeViewSupervisor.salesPerLaborHour.displayName.isNullOrEmpty()) {
                ll_labor_display2.visibility = View.GONE
            } else {
                ll_labor_display2.visibility = View.VISIBLE
                labor_display2.text = laborDataRangeViewSupervisor.salesPerLaborHour.displayName
            }
            if (laborDataRangeViewSupervisor?.laborHours == null || laborDataRangeViewSupervisor.laborHours.displayName.isNullOrEmpty()) {
                ll_labor_display3.visibility = View.GONE
            } else {
                ll_labor_display3.visibility = View.VISIBLE
                labor_display3.text = laborDataRangeViewSupervisor.laborHours.displayName
            }
            if (laborDataRangeViewSupervisor?.laborvsManagerBudget == null || laborDataRangeViewSupervisor.laborvsManagerBudget.displayName.isNullOrEmpty()) {
                ll_labor_display4.visibility = View.GONE
            } else {
                ll_labor_display4.visibility = View.VISIBLE
                labor_display4.text = laborDataRangeViewSupervisor.laborvsManagerBudget.displayName
            }

            if (laborDataRangeViewSupervisor?.staffing?.total == null || laborDataRangeViewSupervisor.staffing.total.displayName.isNullOrEmpty()) {
                ll_staffing.visibility = View.GONE
            } else {
                ll_staffing.visibility = View.VISIBLE
                staffing_display.text = laborDataRangeViewSupervisor.staffing.total.displayName
            }

            if (laborDataRangeViewSupervisor?.staffing?.totalTMCount == null || laborDataRangeViewSupervisor.staffing.totalTMCount.displayName.isNullOrEmpty()) {
                ll_total_tm_count_text.visibility = View.GONE
            } else {
                ll_total_tm_count_text.visibility = View.VISIBLE
                total_tm_count_text.text = laborDataRangeViewSupervisor.staffing.totalTMCount.displayName
            }
            if (laborDataRangeViewSupervisor?.staffing?.insiders == null || laborDataRangeViewSupervisor.staffing.insiders.displayName.isNullOrEmpty()) {
                ll_insider_text.visibility = View.GONE
            } else {
                ll_insider_text.visibility = View.VISIBLE
                insider_text.text = laborDataRangeViewSupervisor.staffing.insiders.displayName
            }
            if (laborDataRangeViewSupervisor?.staffing?.drivers == null || laborDataRangeViewSupervisor.staffing.drivers.displayName.isNullOrEmpty()) {
                ll_drivers_text.visibility = View.GONE
            } else {
                ll_drivers_text.visibility = View.VISIBLE
                drivers_text.text = laborDataRangeViewSupervisor.staffing.drivers.displayName
            }
            if (laborDataRangeViewSupervisor?.staffing?.tmCountLessThan30Hours == null || laborDataRangeViewSupervisor.staffing.tmCountLessThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_less_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_less_than_thirty_parent.visibility = View.VISIBLE
                tm_count_less_than_thirty_text.text =
                    laborDataRangeViewSupervisor.staffing.tmCountLessThan30Hours.displayName
            }
            if (laborDataRangeViewSupervisor?.staffing?.tmCountMoreThan30Hours == null || laborDataRangeViewSupervisor.staffing.tmCountMoreThan30Hours.displayName.isNullOrEmpty()) {
                tm_count_grater_than_thirty_parent.visibility = View.GONE
            } else {
                tm_count_grater_than_thirty_parent.visibility = View.VISIBLE
                tm_count_grater_than_thirty_text.text =
                    laborDataRangeViewSupervisor.staffing.tmCountMoreThan30Hours.displayName
            }

            // break display
            break_display.text = laborDataRangeViewSupervisor?.breaks?.total?.displayName
            breaks_qty_text.text = laborDataRangeViewSupervisor?.breaks?.breakQuantity?.displayName
            labor_breaks_dollar_text.text =
                laborDataRangeViewSupervisor?.breaks?.laborWithBreaks?.displayName
            labor_breaks_percentage_text.text =
                laborDataRangeViewSupervisor?.breaks?.laborWithBreaks?.displayName
            labor_breaks_ot_percentage_text.text =
                laborDataRangeViewSupervisor?.breaks?.laborWithoutBreaksAndOT?.displayName
            labor_breaks_ot_dollar_text.text =
                laborDataRangeViewSupervisor?.breaks?.laborWithoutBreaksAndOT?.displayName
            // end break

            labour_sales.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.actual?.amount,
                laborDataRangeViewSupervisor?.actual?.percentage,
                laborDataRangeViewSupervisor?.actual?.value
            )

            if (laborDataRangeViewSupervisor?.status != null) {
                when {
                    laborDataRangeViewSupervisor.status.toString() == resources.getString(R.string.out_of_range) -> {
                        labour_sales.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeViewSupervisor.status.toString() == resources.getString(R.string.under_limit) -> {
                        labour_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        labour_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // scroll detect

            labor_scroll.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (labor_scroll.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            /* level_two_scroll_data_action.text =
                                 laborDataRangeViewSupervisor?.displayName ?: getString(R.string.labour_text)*/
                            level_two_scroll_data_action.text = getString(R.string.labour_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@LabourKpiActivity,
                                    laborDataRangeViewSupervisor?.actual?.amount,
                                    laborDataRangeViewSupervisor?.actual?.percentage,
                                    laborDataRangeViewSupervisor?.actual?.value
                                )

                            if (laborDataRangeViewSupervisor?.status != null) {
                                when {
                                    laborDataRangeViewSupervisor.status.toString() == resources.getString(
                                        R.string.out_of_range
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    laborDataRangeViewSupervisor.status.toString() == resources.getString(
                                        R.string.under_limit
                                    ) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    }
                                    else -> {
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.black_circle,
                                            0
                                        )
                                    }
                                }
                            }
                        }
                        y = labor_scroll.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            labour_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.goal?.amount,
                laborDataRangeViewSupervisor?.goal?.percentage,
                laborDataRangeViewSupervisor?.goal?.value
            )
            labour_variance_value.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.variance?.amount,
                laborDataRangeViewSupervisor?.variance?.percentage,
                laborDataRangeViewSupervisor?.variance?.value
            )
            showLaborNarrativeData(laborDataRangeViewSupervisor?.narrative.toString())


            // Labor vs goal
            val laborGoalSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.laborvsGoal?.goal?.amount,
                laborDataRangeViewSupervisor?.laborvsGoal?.goal?.percentage,
                laborDataRangeViewSupervisor?.laborvsGoal?.goal?.value
            )
            val laborVarianceSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.laborvsGoal?.variance?.amount,
                laborDataRangeViewSupervisor?.laborvsGoal?.variance?.percentage,
                laborDataRangeViewSupervisor?.laborvsGoal?.variance?.value
            )
            val laborActualSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.laborvsGoal?.actual?.amount,
                laborDataRangeViewSupervisor?.laborvsGoal?.actual?.percentage,
                laborDataRangeViewSupervisor?.laborvsGoal?.actual?.value
            )
            labourGoal(
                laborGoalSupervisorRange,
                laborVarianceSupervisorRange,
                laborActualSupervisorRange
            )

            if (laborDataRangeViewSupervisor?.laborvsGoal?.status != null) {
                when {
                    laborDataRangeViewSupervisor.laborvsGoal.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeViewSupervisor.laborvsGoal.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // sales per labour
            val laborSalesHoursActualSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.salesPerLaborHour?.actual?.amount,
                laborDataRangeViewSupervisor?.salesPerLaborHour?.actual?.percentage,
                laborDataRangeViewSupervisor?.salesPerLaborHour?.actual?.value
            )
            labourSalesHrs(laborSalesHoursActualSupervisorRange)

            if (laborDataRangeViewSupervisor?.salesPerLaborHour?.status != null) {
                when {
                    laborDataRangeViewSupervisor.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeViewSupervisor.salesPerLaborHour.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        sales_labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        sales_labor_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }

            }


            // Labor vs management
            val laborManagementsGoalSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.laborvsManagerBudget?.goal?.amount,
                laborDataRangeViewSupervisor?.laborvsManagerBudget?.goal?.percentage,
                laborDataRangeViewSupervisor?.laborvsManagerBudget?.goal?.value
            )
            val laborManagementsVarianceSupervisorRange =
                Validation().checkAmountPercentageValue(
                    this,
                    laborDataRangeViewSupervisor?.laborvsManagerBudget?.variance?.amount,
                    laborDataRangeViewSupervisor?.laborvsManagerBudget?.variance?.percentage,
                    laborDataRangeViewSupervisor?.laborvsManagerBudget?.variance?.value
                )
            val laborManagementsActualSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.laborvsManagerBudget?.actual?.amount,
                laborDataRangeViewSupervisor?.laborvsManagerBudget?.actual?.percentage,
                laborDataRangeViewSupervisor?.laborvsManagerBudget?.actual?.value
            )
            labourManagement(
                laborManagementsGoalSupervisorRange,
                laborManagementsVarianceSupervisorRange,
                laborManagementsActualSupervisorRange
            )

            if (laborDataRangeViewSupervisor?.laborvsManagerBudget?.status != null) {
                when {
                    laborDataRangeViewSupervisor.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeViewSupervisor.laborvsManagerBudget.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_vs_mgmt_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // labor hour
            val laborHoursGoalSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.laborHours?.goal?.amount,
                laborDataRangeViewSupervisor?.laborHours?.goal?.percentage,
                laborDataRangeViewSupervisor?.laborHours?.goal?.value
            )
            val laborHoursVarianceSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.laborHours?.variance?.amount,
                laborDataRangeViewSupervisor?.laborHours?.variance?.percentage,
                laborDataRangeViewSupervisor?.laborHours?.variance?.value
            )
            val laborHoursActualSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.laborHours?.actual?.amount,
                laborDataRangeViewSupervisor?.laborHours?.actual?.percentage,
                laborDataRangeViewSupervisor?.laborHours?.actual?.value
            )
            labourHours(
                laborHoursGoalSupervisorRange,
                laborHoursVarianceSupervisorRange,
                laborHoursActualSupervisorRange
            )

            if (laborDataRangeViewSupervisor?.laborHours?.status != null) {
                when {
                    laborDataRangeViewSupervisor.laborHours.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeViewSupervisor.laborHours.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        labor_hrs_vs_goal_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // breaks sales
            val breakPercentageSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.breaks?.total?.actual?.amount,
                laborDataRangeViewSupervisor?.breaks?.total?.actual?.percentage,
                laborDataRangeViewSupervisor?.breaks?.total?.actual?.value
            )
            if (breakPercentageSupervisorRange.isEmpty()) {

                breaks_error.visibility = View.VISIBLE
                breaks_percentage.visibility = View.GONE
            } else {
                breaks_percentage.text = breakPercentageSupervisorRange
            }

            if (laborDataRangeViewSupervisor?.breaks?.total?.status != null) {
                when {
                    laborDataRangeViewSupervisor.breaks.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeViewSupervisor.breaks.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        breaks_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        breaks_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            //Missed Break
            val laborMissedBreakQuantitySupervisorRange =
                Validation().checkAmountPercentageValue(
                    this,
                    laborDataRangeViewSupervisor?.breaks?.breakQuantity?.actual?.amount,
                    laborDataRangeViewSupervisor?.breaks?.breakQuantity?.actual?.percentage,
                    laborDataRangeViewSupervisor?.breaks?.breakQuantity?.actual?.value
                )

            val laborMissedBreakDollarSupervisorRange: String = if(laborDataRangeViewSupervisor?.breaks?.laborWithBreaks?.actual?.amount == null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataRangeViewSupervisor.breaks.laborWithBreaks.actual.amount)
                )
            }

            val laborMissedBreakPercentageSupervisorRange: String = if(laborDataRangeViewSupervisor?.breaks?.laborWithBreaks?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataRangeViewSupervisor.breaks.laborWithBreaks.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }

            val laborMissedBreakOTDollarSupervisorRange: String = if(laborDataRangeViewSupervisor?.breaks?.laborWithoutBreaksAndOT?.actual?.amount ==  null){
                ""
            }else{
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(laborDataRangeViewSupervisor.breaks.laborWithoutBreaksAndOT.actual.amount)
                )
            }

            val laborMissedBreakOTPercentageSupervisorRange : String = if(laborDataRangeViewSupervisor?.breaks?.laborWithoutBreaksAndOT?.actual?.percentage == null){
                ""
            }else{
                Validation().dollarFormatting(laborDataRangeViewSupervisor.breaks.laborWithoutBreaksAndOT.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            }


            laborMissedBreaksData(
                laborMissedBreakQuantitySupervisorRange,
                laborMissedBreakDollarSupervisorRange,
                laborMissedBreakPercentageSupervisorRange,
                laborMissedBreakOTDollarSupervisorRange,
                laborMissedBreakOTPercentageSupervisorRange
            )

            // Driver OT Full
            val laborOtSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.driverOTFull?.actual?.amount,
                laborDataRangeViewSupervisor?.driverOTFull?.actual?.percentage,
                laborDataRangeViewSupervisor?.driverOTFull?.actual?.value
            )
            labourDriverOT(laborOtSupervisorRange)
            if (laborDataRangeViewSupervisor?.driverOTFull?.status != null) {
                when {
                    laborDataRangeViewSupervisor.driverOTFull.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeViewSupervisor.driverOTFull.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // Driver ot premium
            val laborOtPremiumSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.driverOTPremium?.actual?.amount,
                laborDataRangeViewSupervisor?.driverOTPremium?.actual?.percentage,
                laborDataRangeViewSupervisor?.driverOTPremium?.actual?.value
            )
            labourDriverOTPremium(laborOtPremiumSupervisorRange)
            if (laborDataRangeViewSupervisor?.driverOTPremium?.status != null) {
                when {
                    laborDataRangeViewSupervisor.driverOTPremium.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeViewSupervisor.driverOTPremium.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        driver_ot_premium_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        driver_ot_premium_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // staffing
            staffing_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.staffing?.total?.actual?.amount,
                laborDataRangeViewSupervisor?.staffing?.total?.actual?.percentage,
                laborDataRangeViewSupervisor?.staffing?.total?.actual?.value
            )
            if (laborDataRangeViewSupervisor?.staffing?.total?.status != null) {
                staffing_percentage.text =
                        Validation().ignoreZeroAfterDecimal(laborDataRangeViewSupervisor.staffing.total.actual?.value)
                when {
                    laborDataRangeViewSupervisor.staffing.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.red))
                    }
                    laborDataRangeViewSupervisor.staffing.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        staffing_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        staffing_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val totalTmCountSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.staffing?.totalTMCount?.actual?.amount,
                laborDataRangeViewSupervisor?.staffing?.totalTMCount?.actual?.percentage,
                laborDataRangeViewSupervisor?.staffing?.totalTMCount?.actual?.value
            )
            val totalInsiderSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.staffing?.insiders?.actual?.amount,
                laborDataRangeViewSupervisor?.staffing?.insiders?.actual?.percentage,
                laborDataRangeViewSupervisor?.staffing?.insiders?.actual?.value
            )
            val totalDriversSupervisorRange = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.staffing?.drivers?.actual?.amount,
                laborDataRangeViewSupervisor?.staffing?.drivers?.actual?.percentage,
                laborDataRangeViewSupervisor?.staffing?.drivers?.actual?.value
            )
            labourStaffing(
                totalTmCountSupervisorRange,
                totalInsiderSupervisorRange,
                totalDriversSupervisorRange
            )

            tm_count_less_than_thirty_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.staffing?.tmCountLessThan30Hours?.actual?.amount,
                laborDataRangeViewSupervisor?.staffing?.tmCountLessThan30Hours?.actual?.percentage,
                laborDataRangeViewSupervisor?.staffing?.tmCountLessThan30Hours?.actual?.value
            )
            tm_count_grater_than_percentage.text = Validation().checkAmountPercentageValue(
                this,
                laborDataRangeViewSupervisor?.staffing?.tmCountMoreThan30Hours?.actual?.amount,
                laborDataRangeViewSupervisor?.staffing?.tmCountMoreThan30Hours?.actual?.percentage,
                laborDataRangeViewSupervisor?.staffing?.tmCountMoreThan30Hours?.actual?.value
            )

        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Labor Overview Period Range KPI")
        }
    }

    override fun onBackPressed() {
        Logger.info("Back-pressed", "Labor Overview KPI")
        finish()
    }

    //Labour vs Goal
    private fun labourGoal(laborGoal: String, laborVariance: String, laborActual: String) {
        if (laborGoal.isEmpty() && laborVariance.isEmpty() && laborActual.isEmpty()) {

            labor_vs_goal_error.visibility = View.VISIBLE
            labor_vs_goal_goal.visibility = View.GONE
            labor_vs_goal_variance.visibility = View.GONE
            labor_vs_goal_actual.visibility = View.GONE
            val paramsCEOLaborError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsCEOLaborError.weight = 2.0f
            labor_display1.layoutParams = paramsCEOLaborError

        } else if (laborGoal.isEmpty() && laborVariance.isEmpty() && laborActual.isEmpty()) {
            labor_vs_goal_goal.text = laborGoal
            labor_vs_goal_variance.text = laborVariance
            labor_vs_goal_actual.text = laborActual
        } else {
            if (laborGoal.isEmpty()) {

                labor_vs_goal_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_error,
                    0
                )
            } else {
                labor_vs_goal_goal.text = laborGoal
            }
            if (laborVariance.isEmpty()) {

                labor_vs_goal_variance.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_error,
                    0
                )

            } else {
                labor_vs_goal_variance.text = laborVariance
            }
            if (laborActual.isEmpty()) {

                labor_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_error,
                    0
                )

            } else {
                labor_vs_goal_actual.text = laborActual
            }

        }

    }

    //Sales Per Labour hours
    private fun labourSalesHrs(laborSalesActual: String) {
        if (laborSalesActual.isEmpty()) {

            sales_labor_vs_goal_error.visibility = View.VISIBLE
            sales_labor_vs_goal_actual.visibility = View.GONE
            val paramsSalesLaborError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsSalesLaborError.weight = 2.0f
            labor_display2.layoutParams = paramsSalesLaborError
        } else {
            sales_labor_vs_goal_actual.text = laborSalesActual
        }
    }

    //Labour Hours
    private fun labourHours(
        laborHoursGoal: String,
        laborHoursVariance: String,
        laborHoursActual: String
    ) {
        if (laborHoursGoal.isEmpty() && laborHoursVariance.isEmpty() && laborHoursActual.isEmpty()) {

            labor_hrs_vs_goal_error.visibility = View.VISIBLE
            labor_hrs_vs_goal_goal.visibility = View.GONE
            labor_hrs_vs_goal_variance.visibility = View.GONE
            labor_hrs_vs_goal_actual.visibility = View.GONE
            val paramsLaborHoursError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsLaborHoursError.weight = 2.0f
            labor_display3.layoutParams = paramsLaborHoursError
        } else if (laborHoursGoal.isEmpty() && laborHoursVariance.isEmpty() && laborHoursActual.isEmpty()) {
            labor_hrs_vs_goal_goal.text = laborHoursGoal
            labor_hrs_vs_goal_variance.text = laborHoursVariance
            labor_hrs_vs_goal_actual.text = laborHoursActual
        } else {
            if (laborHoursGoal.isEmpty()) {

                labor_hrs_vs_goal_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_error,
                    0
                )

            } else {
                labor_hrs_vs_goal_goal.text = laborHoursGoal
            }

            if (laborHoursVariance.isEmpty()) {

                labor_hrs_vs_goal_variance.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_error,
                    0
                )

            } else {
                labor_hrs_vs_goal_variance.text = laborHoursVariance
            }

            if (laborHoursActual.isEmpty()) {

                labor_hrs_vs_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_error,
                    0
                )

            } else {
                labor_hrs_vs_goal_actual.text = laborHoursActual
            }

        }
    }

    //Labour vs Management
    private fun labourManagement(
        laborManagementGoal: String,
        laborManagementVariance: String,
        laborManagementActual: String
    ) {
        if (laborManagementGoal.isEmpty() && laborManagementVariance.isEmpty() && laborManagementActual.isEmpty()) {

            labor_vs_mgmt_goal_error.visibility = View.VISIBLE
            labor_vs_mgmt_goal_goal.visibility = View.GONE
            labor_vs_mgmt_goal_variance.visibility = View.GONE
            labor_vs_mgmt_goal_actual.visibility = View.GONE
            val paramsLaborManagementError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsLaborManagementError.weight = 2.0f
            labor_display4.layoutParams = paramsLaborManagementError
        } else if (laborManagementGoal.isEmpty() && laborManagementVariance.isEmpty() && laborManagementActual.isEmpty()) {
            labor_vs_mgmt_goal_goal.text = laborManagementGoal
            labor_vs_mgmt_goal_variance.text = laborManagementVariance
            labor_vs_mgmt_goal_actual.text = laborManagementActual
        } else {
            if (laborManagementGoal.isEmpty()) {

                labor_vs_mgmt_goal_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_error,
                    0
                )

            } else {
                labor_vs_mgmt_goal_goal.text = laborManagementGoal
            }

            if (laborManagementVariance.isEmpty()) {

                labor_vs_mgmt_goal_variance.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_error,
                    0
                )

            } else {
                labor_vs_mgmt_goal_variance.text = laborManagementVariance
            }

            if (laborManagementActual.isEmpty()) {

                labor_vs_mgmt_goal_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_error,
                    0
                )

            } else {
                labor_vs_mgmt_goal_actual.text = laborManagementActual
            }
        }
    }

    //Missed Breaks
    private fun laborMissedBreaksData(
        strCountActual: String,
        breakDollarActual: String,
        breakPercentageActual: String,
        breakOTSDollarActual: String,
        breakOTSPercentageActual: String
    ) {

        // Quantity
        if (strCountActual.isEmpty()) {
            breaks_qty_error.visibility = View.VISIBLE
            breaks_qty_percentage.visibility = View.GONE
        } else {
            breaks_qty_percentage.text = strCountActual
        }
        // Labour Without break $
        if (breakDollarActual.isEmpty()) {
            breaks_break_dollar_percentage_error.visibility = View.VISIBLE
            breaks_break_dollar_percentage.visibility = View.GONE
        } else {
            breaks_break_dollar_percentage.text = breakDollarActual
        }
        // Labour Without break %
        if (breakPercentageActual.isEmpty()) {
            breaks_break_percentage_error.visibility = View.VISIBLE
            labor_breaks_percentage.visibility = View.GONE
        } else {
            labor_breaks_percentage.text = breakPercentageActual
        }
        // Labour Without break & OT$
        if (breakOTSDollarActual.isEmpty()) {
            labor_breaks_ot_dollar_error.visibility = View.VISIBLE
            labor_breaks_ot_dollar.visibility = View.GONE
        } else {
            labor_breaks_ot_dollar.text = breakOTSDollarActual
        }
        // Labour Without break & OT%
        if (breakOTSPercentageActual.isEmpty()) {
            labor_breaks_ot_percentage_error.visibility = View.VISIBLE
            labor_breaks_ot_percentage.visibility = View.GONE
        } else {
            labor_breaks_ot_percentage.text = breakOTSPercentageActual
        }

    }

    //Driver OT Full
    private fun labourDriverOT(laborDriverOTActual: String) {
        if (laborDriverOTActual.isEmpty()) {
            driver_ot_error.visibility = View.VISIBLE
            driver_ot_percentage.visibility = View.GONE

        } else {
            driver_ot_percentage.text = laborDriverOTActual
        }
    }

    //Driver OT Premium
    private fun labourDriverOTPremium(laborDriverOTPremiumActual: String) {
        if (laborDriverOTPremiumActual.isEmpty()) {
            driver_ot_premium_error.visibility = View.VISIBLE
            driver_ot_premium_percentage.visibility = View.GONE
        } else {
            driver_ot_premium_percentage.text = laborDriverOTPremiumActual
        }

    }

    // Staffing
    private fun labourStaffing(
        staffCountActual: String,
        staffInsiderActual: String,
        staffDriversActual: String
    ) {

        if (staffCountActual.isEmpty()) {
            total_tm_count_error.visibility = View.VISIBLE
            total_tm_count_percentage.visibility = View.GONE
        } else {
            total_tm_count_percentage.text = staffCountActual
        }

        if (staffInsiderActual.isEmpty()) {
            insider_error.visibility = View.VISIBLE
            insider_percentage.visibility = View.GONE
        } else {
            insider_percentage.text = staffInsiderActual
        }

        if (staffDriversActual.isEmpty()) {
            drivers_error.visibility = View.VISIBLE
            drivers_percentage.visibility = View.GONE
        } else {
            drivers_percentage.text = staffDriversActual
        }


    }

    private fun showLaborNarrativeData(narrative: String?) {
        if (!narrative.isNullOrEmpty()) {
            var laborNarrative = narrative
            laborNarrative = laborNarrative.replace("</p>", "<br><br>")
            labour_narrative_value.text = Html.fromHtml(laborNarrative, Html.FROM_HTML_MODE_COMPACT)
        } else if (narrative.equals("null")) {
            labour_narrative_value.visibility = View.INVISIBLE
        } else {
            labour_narrative_value.visibility = View.INVISIBLE
        }
    }

    private fun callLabourOverviewNullApi() {
        val formattedStartDateValueLabour: String
        val formattedEndDateValueLabour: String

        val startDateValueLabour = StorePrefData.startDateValue
        val endDateValueLabour = StorePrefData.endDateValue

        if (StorePrefData.isCalendarSelected) {
            formattedStartDateValueLabour = startDateValueLabour
            formattedEndDateValueLabour = endDateValueLabour
        } else {
            formattedStartDateValueLabour = startDateValueLabour
            formattedEndDateValueLabour = endDateValueLabour
        }
        val progressDialogLabourOverview = CustomProgressDialog(this@LabourKpiActivity)
        progressDialogLabourOverview.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodeListLabour = dbHelperLabourOverview.getAllSelectedAreaList(true)
            val stateCodeListLabour = dbHelperLabourOverview.getAllSelectedStoreListState(true)
            val supervisorNumberListLabour =
                dbHelperLabourOverview.getAllSelectedStoreListSupervisor(true)
            val storeNumberListLabour = dbHelperLabourOverview.getAllSelectedStoreList(true)

            val responseMissingDataLabour = try {
                apolloClient(this@LabourKpiActivity).query(
                    MissingDataQuery(
                            areaCodeListLabour.toInput(),
                            stateCodeListLabour.toInput(),
                            supervisorNumberListLabour.toInput(),
                            storeNumberListLabour.toInput(),
                            EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType).toInput(),
                            formattedStartDateValueLabour.toInput(),
                            formattedEndDateValueLabour.toInput(),
                    )
                ).await()

            } catch (e: ApolloException) {
                Logger.error("Missing Data ${e.message.toString()}","Labor Overview KPI")
                progressDialogLabourOverview.dismissProgressDialog()
                return@launchWhenResumed
            }
            if (responseMissingDataLabour.data?.missingData != null) {
                progressDialogLabourOverview.dismissProgressDialog()
                labour_kpi_error_layout.visibility = View.VISIBLE
                labour_kpi_error_layout.header_data_title.text =
                    responseMissingDataLabour.data?.missingData!!.header
                labour_kpi_error_layout.header_data_description.text =
                    responseMissingDataLabour.data?.missingData!!.message
            } else {
                progressDialogLabourOverview.dismissProgressDialog()
                labour_kpi_error_layout.visibility = View.GONE
            }
        }
    }
}

