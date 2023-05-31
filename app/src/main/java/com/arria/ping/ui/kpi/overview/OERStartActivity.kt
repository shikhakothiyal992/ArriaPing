package com.arria.ping.ui.kpi.overview

import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_cash.*
import kotlinx.android.synthetic.main.activity_labour_kpi.*
import kotlinx.android.synthetic.main.activity_o_e_r_start.*
import kotlinx.android.synthetic.main.activity_o_e_r_start.level_two_scroll_data_action
import kotlinx.android.synthetic.main.activity_o_e_r_start.level_two_scroll_data_action_value
import kotlinx.android.synthetic.main.activity_o_e_r_start.parent_data_on_scroll_linear
import kotlinx.android.synthetic.main.activity_o_e_r_start.parent_data_on_scroll_view
import kotlinx.android.synthetic.main.activity_service_kpi.*
import kotlinx.android.synthetic.main.ceo_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.data_error_layout.view.*
import javax.inject.Inject
import com.arria.ping.kpi.gm.StorePeriodRangeKPIQuery
import com.arria.ping.kpi.gm.StoreYesterdayKPIQuery

@AndroidEntryPoint
class OERStartActivity : AppCompatActivity() {
    var apiOerArgumentFromFilter = ""
    val gson = Gson()
    private lateinit var dbHelperOEROverview: DatabaseHelperImpl

    @Inject
    lateinit var networkHelper: NetworkHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_o_e_r_start)
        this.setFinishOnTouchOutside(false) 
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dbHelperOEROverview = DatabaseHelperImpl(DatabaseBuilder.getInstance(this@OERStartActivity))
        setOerData()
        cross_button_oer.setOnClickListener {
            Logger.info("Cancel Button clicked","OER Overview KPI Screen")
            finish()
        }
    }

    private fun setOerData() {
        when (StorePrefData.role) {
            getString(R.string.ceo_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                     apiOerArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
                }
                when ( apiOerArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val oerYesterdayDetailCEO = gson.fromJson(
                            intent.getStringExtra("oer_data"),
                            CEOOverviewRangeQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callOEROverviewNullApi()
                            rangeOverViewCeoOer(oerYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val oerYesterdayDetail = gson.fromJson(
                            intent.getStringExtra("oer_data"),
                            CEOOverviewYesterdayQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callOEROverviewNullApi()
                            yesterdayViewCeoOer(oerYesterdayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val oerTodayDetail = gson.fromJson(
                            intent.getStringExtra("oer_data"),
                            CEOOverviewTodayQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callOEROverviewNullApi()
                            todayViewCeoOer(oerTodayDetail)
                        }
                    }
                }
            }
            getString(R.string.do_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                     apiOerArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
                }
                when ( apiOerArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val oerYesterdayDetailCEO = gson.fromJson(
                            intent.getStringExtra("oer_data"),
                            DOOverviewRangeQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callOEROverviewNullApi()
                            rangeOverViewDoOer(oerYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val oerYesterdayDetail = gson.fromJson(
                            intent.getStringExtra("oer_data"),
                            DOOverviewYesterdayQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callOEROverviewNullApi()
                            yesterdayViewDoOer(oerYesterdayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val oerTodayDetail = gson.fromJson(
                            intent.getStringExtra("oer_data"),
                            DOOverviewTodayQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callOEROverviewNullApi()
                            todayViewDoOer(oerTodayDetail)
                        }
                    }
                }
            }
            getString(R.string.gm_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                     apiOerArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
                }
                when ( apiOerArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val oerData = gson.fromJson(
                            intent.getStringExtra("oer_data"),
                            StorePeriodRangeKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callOEROverviewNullApi()
                            rangeViewOer(oerData)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val oerYesterdayDetail = gson.fromJson(
                            intent.getStringExtra("oer_data"),
                            StoreYesterdayKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callOEROverviewNullApi()
                            yesterdayViewOer(oerYesterdayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val oerTodayDetail = gson.fromJson(
                            intent.getStringExtra("oer_data"),
                            StoreTodayKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callOEROverviewNullApi()
                            todayViewOer(oerTodayDetail)
                        }
                    }
                }
            }
            getString(R.string.supervisor_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                     apiOerArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
                }
                when ( apiOerArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val oerYesterdayDetailCEO = gson.fromJson(
                            intent.getStringExtra("oer_data"),
                            SupervisorOverviewRangeQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callOEROverviewNullApi()
                            rangeOverViewSupervisorOer(oerYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val oerYesterdayDetail = gson.fromJson(
                            intent.getStringExtra("oer_data"),
                            SupervisorOverviewYesterdayQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callOEROverviewNullApi()
                            yesterdayViewSupervisorOer(oerYesterdayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val oerTodayDetail = gson.fromJson(
                            intent.getStringExtra("oer_data"),
                            SupervisorOverviewTodayQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callOEROverviewNullApi()
                            todayViewSupervisorOer(oerTodayDetail)
                        }
                    }
                }
            }
        }
    }

    //GM
    private fun todayViewOer(oerTodayDetail: StoreTodayKPIQuery.GeneralManager) {
        try {
            val todayViewOerData = oerTodayDetail.kpis?.store?.today?.oerStart

            Logger.info("OER Today", "OER Overview KPI")

            // display name
            if (todayViewOerData == null || todayViewOerData.displayName.isNullOrEmpty()) {
                ll_oer_start_text.visibility = View.GONE
            } else {
                ll_oer_start_text.visibility = View.VISIBLE
                oer_start_text.text = todayViewOerData.displayName
            }
            if (todayViewOerData?.twentyEightDayeADT == null || todayViewOerData.twentyEightDayeADT.displayName.isNullOrEmpty()) {
                twenty_day_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_eadt_parent.visibility = View.VISIBLE
                twenty_day_eadt_display.text = todayViewOerData.twentyEightDayeADT.displayName
            }
            if (todayViewOerData?.twentyEightExtremeDeliveries == null || todayViewOerData.twentyEightExtremeDeliveries.displayName.isNullOrEmpty()) {
                twenty_day_extreme_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_extreme_eadt_parent.visibility = View.VISIBLE
                twenty_day_extreme_adt_display.text = todayViewOerData.twentyEightExtremeDeliveries.displayName
            }
            if (todayViewOerData?.twentyEightSingles == null || todayViewOerData.twentyEightSingles.displayName.isNullOrEmpty()) {
                twenty_eight_singles_parent.visibility = View.GONE
            } else {
                twenty_eight_singles_parent.visibility = View.VISIBLE
                twenty_eight_singles_display.text = todayViewOerData.twentyEightSingles.displayName
            }
            if (todayViewOerData?.twentyEightLoad == null || todayViewOerData.twentyEightLoad.displayName.isNullOrEmpty()) {
                twenty_day_load_parent.visibility = View.GONE
            } else {
                twenty_day_load_parent.visibility = View.VISIBLE
                twenty_day_load_display.text = todayViewOerData.twentyEightLoad.displayName
            }



            if (todayViewOerData?.actual?.value?.isNaN() == false && todayViewOerData.status != null) {
                oer_sales.text = Validation().ignoreZeroAfterDecimal(todayViewOerData.actual.value)
                when {
                    todayViewOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        oer_sales.setTextColor(getColor(R.color.red))
                    }
                    todayViewOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                        oer_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        oer_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // scroll detect
            scroll_oer_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (scroll_oer_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text =
                                todayViewOerData?.displayName ?: getString(R.string.oer_start_text)

                            level_two_scroll_data_action_value.text =
                                todayViewOerData?.actual?.value.toString()
                            if (todayViewOerData?.actual?.value?.isNaN() == false && todayViewOerData.status != null) {
                                level_two_scroll_data_action_value.text =
                                    Validation().ignoreZeroAfterDecimal(todayViewOerData.actual.value)
                                when {
                                    todayViewOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    todayViewOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    } else -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.text_color))
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
                        y = scroll_oer_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })
            oer_goal_value.text =
                if (todayViewOerData?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    todayViewOerData.goal.value) else ""
            oer_variance_value.text =
                if (todayViewOerData?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    todayViewOerData.variance.value) else ""



            // 28 days

            if (todayViewOerData?.twentyEightDayeADT?.actual?.value?.isNaN() == false && todayViewOerData.twentyEightDayeADT.status != null) {
                twenty_eight_days_eat_value.text =
                    Validation().ignoreZeroAfterDecimal(todayViewOerData.twentyEightDayeADT.actual.value)
                when {
                    todayViewOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.red))
                    }
                    todayViewOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            if (todayViewOerData?.twentyEightExtremeDeliveries?.actual?.value?.isNaN() == false && todayViewOerData.twentyEightExtremeDeliveries.status != null) {
                twenty_eight_extreme_delivery_value.text =
                    Validation().ignoreZeroAfterDecimal(todayViewOerData.twentyEightExtremeDeliveries.actual.value)
                when {
                    todayViewOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.red))
                    }
                    todayViewOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (todayViewOerData?.twentyEightSingles?.actual?.value?.isNaN() == false && todayViewOerData.twentyEightSingles.status != null) {
                twenty_eight_single_value.text =
                    Validation().ignoreZeroAfterDecimal(todayViewOerData.twentyEightSingles.actual.value)
                when {
                    todayViewOerData.twentyEightSingles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.red))
                    }
                    todayViewOerData.twentyEightSingles.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            if (todayViewOerData?.twentyEightLoad?.actual?.value?.isNaN() == false && todayViewOerData.twentyEightLoad.status != null) {
                twenty_eight_days_load_value.text =
                    Validation().ignoreZeroAfterDecimal(todayViewOerData.twentyEightLoad.actual.value)
                when {
                    todayViewOerData.twentyEightLoad.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.red))
                    }
                    todayViewOerData.twentyEightLoad.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            if (todayViewOerData?.lastOERScore != null) {
                last_food_score.text = todayViewOerData.lastOERScore.lastFoodSafetyScore?.displayName
                    ?: getString(R.string.last_food_score)
                last_image_score.text = todayViewOerData.lastOERScore.lastImageScore?.displayName
                    ?: getString(R.string.last_image_score)
                last_product_score.text = todayViewOerData.lastOERScore.lastProductScore?.displayName
                    ?: getString(R.string.last_product_score)
                last_eADT_score.text =
                    todayViewOerData.lastOERScore.lasteADTScore?.displayName
                        ?: getString(R.string.last_eADT_score)
                last_extreme_delivery_score.text =
                    todayViewOerData.lastOERScore.lastExtremeDeliveriesScore?.displayName
                        ?: getString(R.string.last_extreme_delivery_score)
                last_single_score.text = todayViewOerData.lastOERScore.lastSinglesScore?.displayName
                    ?: getString(R.string.last_single_score)
                last_load_score.text =
                    todayViewOerData.lastOERScore.lastLoadScore?.displayName
                        ?: getString(R.string.last_load_score)
                last_oer_score_display.text =
                    todayViewOerData.lastOERScore.total?.displayName ?: getString(R.string.last_oer_score)
                if (todayViewOerData.lastOERScore.total?.actual?.value?.isNaN() == false && todayViewOerData.lastOERScore.total.status != null) {
                    last_oer_score_value.text =
                        Validation().ignoreZeroAfterDecimal(todayViewOerData.lastOERScore.total.actual.value)
                    when {
                        todayViewOerData.lastOERScore.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                            last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.red_circle,
                                0
                            )
                            last_oer_score_value.setTextColor(getColor(R.color.red))
                        }
                        todayViewOerData.lastOERScore.total.status.toString() == resources.getString(
                            R.string.under_limit) -> {
                            last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.green_circle,
                                0
                            )
                            last_oer_score_value.setTextColor(getColor(R.color.green))

                        }
                        else -> {
                            last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.black_circle,
                                0
                            )
                            last_oer_score_value.setTextColor(getColor(R.color.text_color))

                        }
                    }
                }
                // last data
                last_food_score_value.text =
                    Validation().ignoreZeroAfterDecimal(todayViewOerData.lastOERScore.lastFoodSafetyScore?.actual?.value)
                last_image_score_value.text =
                    Validation().ignoreZeroAfterDecimal(todayViewOerData.lastOERScore.lastImageScore?.actual?.value)
                last_product_score_value.text =
                    Validation().ignoreZeroAfterDecimal(todayViewOerData.lastOERScore.lastProductScore?.actual?.value)
                last_eADT_score_value.text =
                    Validation().ignoreZeroAfterDecimal(todayViewOerData.lastOERScore.lasteADTScore?.actual?.value)
                last_extreme_delivery_score_value.text =
                    Validation().ignoreZeroAfterDecimal(todayViewOerData.lastOERScore.lastExtremeDeliveriesScore?.actual?.value)

                last_single_score_value.text =
                    Validation().ignoreZeroAfterDecimal(todayViewOerData.lastOERScore.lastSinglesScore?.actual?.value)

                last_load_score_values.text =
                    Validation().ignoreZeroAfterDecimal(todayViewOerData.lastOERScore.lastLoadScore?.actual?.value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun yesterdayViewOer(oerTodayDetail: StoreYesterdayKPIQuery.GeneralManager) {
        try {
            val yesterdayViewOerData = oerTodayDetail.kpis?.store?.yesterday?.oerStart

            Logger.info("OER Yesterday", "OER Overview KPI")

            // display name
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            if(yesterdayViewOerData?.lastOERScore!=null){
                last_oer_parent_view.visibility = View.VISIBLE
            }else{
                last_oer_parent_view.visibility = View.GONE
            }

            if (yesterdayViewOerData == null || yesterdayViewOerData.displayName.isNullOrEmpty()) {
                ll_oer_start_text.visibility = View.GONE
            } else {
                ll_oer_start_text.visibility = View.VISIBLE
                oer_start_text.text = yesterdayViewOerData.displayName
            }
            if (yesterdayViewOerData?.twentyEightDayeADT == null || yesterdayViewOerData.twentyEightDayeADT.displayName.isNullOrEmpty()) {
                twenty_day_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_eadt_parent.visibility = View.VISIBLE
                twenty_day_eadt_display.text = yesterdayViewOerData.twentyEightDayeADT.displayName
            }
            if (yesterdayViewOerData?.twentyEightExtremeDeliveries == null || yesterdayViewOerData.twentyEightExtremeDeliveries.displayName.isNullOrEmpty()) {
                twenty_day_extreme_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_extreme_eadt_parent.visibility = View.VISIBLE
                twenty_day_extreme_adt_display.text = yesterdayViewOerData.twentyEightExtremeDeliveries.displayName
            }
            if (yesterdayViewOerData?.twentyEightSingles == null || yesterdayViewOerData.twentyEightSingles.displayName.isNullOrEmpty()) {
                twenty_eight_singles_parent.visibility = View.GONE
            } else {
                twenty_eight_singles_parent.visibility = View.VISIBLE
                twenty_eight_singles_display.text = yesterdayViewOerData.twentyEightSingles.displayName
            }
            if (yesterdayViewOerData?.twentyEightLoad == null || yesterdayViewOerData.twentyEightLoad.displayName.isNullOrEmpty()) {
                twenty_day_load_parent.visibility = View.GONE
            } else {
                twenty_day_load_parent.visibility = View.VISIBLE
                twenty_day_load_display.text = yesterdayViewOerData.twentyEightLoad.displayName
            }



            oer_sales.text = Validation().checkAmountPercentageValue(this,
                                                                     yesterdayViewOerData?.actual?.amount,
                                                                     yesterdayViewOerData?.actual?.percentage,
                                                                     yesterdayViewOerData?.actual?.value)

            if (yesterdayViewOerData?.status != null) {
                when {
                    yesterdayViewOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        oer_sales.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                        oer_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        oer_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // scroll detect
            scroll_oer_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (scroll_oer_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text =
                                yesterdayViewOerData?.displayName ?: getString(R.string.oer_start_text)

                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                        yesterdayViewOerData?.actual?.amount,
                                                                        yesterdayViewOerData?.actual?.percentage,
                                                                        yesterdayViewOerData?.actual?.value)

                            if (yesterdayViewOerData?.status != null) {
                                when {
                                    yesterdayViewOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    yesterdayViewOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    } else -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.text_color))
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
                        y = scroll_oer_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })
            oer_goal_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                          yesterdayViewOerData?.goal?.amount,
                                                                          yesterdayViewOerData?.goal?.percentage,
                                                                          yesterdayViewOerData?.goal?.value)
            oer_variance_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                              yesterdayViewOerData?.variance?.amount,
                                                                              yesterdayViewOerData?.variance?.percentage,
                                                                              yesterdayViewOerData?.variance?.value)


            // 28 days
            
            val gm28DayEat = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                     yesterdayViewOerData?.twentyEightDayeADT?.actual?.amount,
                                                                     yesterdayViewOerData?.twentyEightDayeADT?.actual?.percentage,
                                                                     yesterdayViewOerData?.twentyEightDayeADT?.actual?.value)

            val gm28DayExtreme = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                         yesterdayViewOerData?.twentyEightExtremeDeliveries?.actual?.amount,
                                                                         yesterdayViewOerData?.twentyEightExtremeDeliveries?.actual?.percentage,
                                                                         yesterdayViewOerData?.twentyEightExtremeDeliveries?.actual?.value)

            val gm28DaySingle = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                        yesterdayViewOerData?.twentyEightSingles?.actual?.amount,
                                                                        yesterdayViewOerData?.twentyEightSingles?.actual?.percentage,
                                                                        yesterdayViewOerData?.twentyEightSingles?.actual?.value)

            val gm28DayLoad = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                      yesterdayViewOerData?.twentyEightLoad?.actual?.amount,
                                                                      yesterdayViewOerData?.twentyEightLoad?.actual?.percentage,
                                                                      yesterdayViewOerData?.twentyEightLoad?.actual?.value)
            
                if(gm28DayEat.isEmpty()){
                    
                    oer_error_28days_eat.visibility = View.VISIBLE
                    twenty_eight_days_eat_value.visibility = View.GONE
                    val param28DaysEatGMYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    param28DaysEatGMYesterdayError.weight = 1.0f
                    twenty_day_eadt_display.layoutParams = param28DaysEatGMYesterdayError
                }else{
                    twenty_eight_days_eat_value.text = gm28DayEat
                }
                if(gm28DayExtreme.isEmpty()){
                    
                    oer_error_28days_extreme.visibility = View.VISIBLE
                    twenty_eight_extreme_delivery_value.visibility = View.GONE
                    val param28DaysExtremeGMYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    param28DaysExtremeGMYesterdayError.weight = 1.0f
                    twenty_day_extreme_adt_display.layoutParams = param28DaysExtremeGMYesterdayError
                }else{
                    twenty_eight_extreme_delivery_value.text = gm28DayExtreme
                }
                if(gm28DaySingle.isEmpty()){
                    
                    oer_error_28days_single.visibility = View.VISIBLE
                    twenty_eight_single_value.visibility = View.GONE
                    val param28DaysSingleGMYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    param28DaysSingleGMYesterdayError.weight = 1.0f
                    twenty_eight_singles_display.layoutParams = param28DaysSingleGMYesterdayError
                }else{
                    twenty_eight_single_value.text = gm28DaySingle
                }
                if(gm28DayLoad.isEmpty()){
                    
                    oer_error_28days_load.visibility = View.VISIBLE
                    twenty_eight_days_load_value.visibility = View.GONE
                    val param28DaysLoadGMYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    param28DaysLoadGMYesterdayError.weight = 1.0f
                    twenty_day_load_display.layoutParams = param28DaysLoadGMYesterdayError
                }else{
                    twenty_eight_days_load_value.text = gm28DayLoad
                }
                
            if (yesterdayViewOerData?.twentyEightDayeADT?.status != null) {
                when {
                    yesterdayViewOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (yesterdayViewOerData?.twentyEightExtremeDeliveries?.status != null) {

                when {
                    yesterdayViewOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (yesterdayViewOerData?.twentyEightSingles?.status != null) {

                when {
                    yesterdayViewOerData.twentyEightSingles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewOerData.twentyEightSingles.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            //
            

            if (yesterdayViewOerData?.twentyEightLoad?.status != null) {

                when {
                    yesterdayViewOerData.twentyEightLoad.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewOerData.twentyEightLoad.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // if (yesterdayViewOerData?.lastOERScore != null) {
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            last_oer_score_display.text =
                yesterdayViewOerData?.lastOERScore?.total?.displayName ?: getString(R.string.last_oer_score)

            last_food_score.text = yesterdayViewOerData?.lastOERScore?.lastFoodSafetyScore?.displayName
                ?: getString(R.string.last_food_score)
            last_image_score.text = yesterdayViewOerData?.lastOERScore?.lastImageScore?.displayName
                ?: getString(R.string.last_image_score)
            last_product_score.text = yesterdayViewOerData?.lastOERScore?.lastProductScore?.displayName
                ?: getString(R.string.last_product_score)
            last_eADT_score.text =
                yesterdayViewOerData?.lastOERScore?.lasteADTScore?.displayName
                    ?: getString(R.string.last_eADT_score)
            last_extreme_delivery_score.text =
                yesterdayViewOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName
                    ?: getString(R.string.last_extreme_delivery_score)
            last_single_score.text = yesterdayViewOerData?.lastOERScore?.lastSinglesScore?.displayName
                ?: getString(R.string.last_single_score)
            last_load_score.text =
                yesterdayViewOerData?.lastOERScore?.lastLoadScore?.displayName
                    ?: getString(R.string.last_load_score)


            if (yesterdayViewOerData?.lastOERScore?.total?.status != null) {

                when {
                    yesterdayViewOerData.lastOERScore.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewOerData.lastOERScore.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val gmYesterdayOERLoadScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                  yesterdayViewOerData?.lastOERScore?.total?.actual?.amount,
                                                                                  yesterdayViewOerData?.lastOERScore?.total?.actual?.percentage,
                                                                                  yesterdayViewOerData?.lastOERScore?.total?.actual?.value)

            val gmYesterdayOERLoadFoodScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                      yesterdayViewOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.amount,
                                                                                      yesterdayViewOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.percentage,
                                                                                      yesterdayViewOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.value)

            val gmYesterdayOERLoadImageScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                       yesterdayViewOerData?.lastOERScore?.lastImageScore?.actual?.amount,
                                                                                       yesterdayViewOerData?.lastOERScore?.lastImageScore?.actual?.percentage,
                                                                                       yesterdayViewOerData?.lastOERScore?.lastImageScore?.actual?.value)

            val gmYesterdayOERLoadProductScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                         yesterdayViewOerData?.lastOERScore?.lastProductScore?.actual?.amount,
                                                                                         yesterdayViewOerData?.lastOERScore?.lastProductScore?.actual?.percentage,
                                                                                         yesterdayViewOerData?.lastOERScore?.lastProductScore?.actual?.value)

            val gmYesterdayOERLoadEatScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                      yesterdayViewOerData?.lastOERScore?.lasteADTScore?.actual?.amount,
                                                                                      yesterdayViewOerData?.lastOERScore?.lasteADTScore?.actual?.percentage,
                                                                                      yesterdayViewOerData?.lastOERScore?.lasteADTScore?.actual?.value)

            val gmYesterdayOERLoadExtremeScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                         yesterdayViewOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.amount,
                                                                                         yesterdayViewOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.percentage,
                                                                                         yesterdayViewOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.value)

            val gmYesterdayOERLoadSingleScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                        yesterdayViewOerData?.lastOERScore?.lastSinglesScore?.actual?.amount,
                                                                                        yesterdayViewOerData?.lastOERScore?.lastSinglesScore?.actual?.percentage,
                                                                                        yesterdayViewOerData?.lastOERScore?.lastSinglesScore?.actual?.value)


            val gmYesterdayOERLoadLastScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                       yesterdayViewOerData?.lastOERScore?.lastLoadScore?.actual?.amount,
                                                                                       yesterdayViewOerData?.lastOERScore?.lastLoadScore?.actual?.percentage,
                                                                                       yesterdayViewOerData?.lastOERScore?.lastLoadScore?.actual?.value)


            if(gmYesterdayOERLoadScore.isEmpty()){
                
                oer_error_last_score.visibility = View.VISIBLE
                last_oer_score_value.visibility = View.GONE
            }else{
                last_oer_score_value.text = gmYesterdayOERLoadScore
            }
            if(gmYesterdayOERLoadFoodScore.isEmpty()){
                
                oer_error_last_score_food.visibility = View.VISIBLE
                last_food_score_value.visibility = View.GONE
            }else{
                last_food_score_value.text = gmYesterdayOERLoadFoodScore
            }
            if(gmYesterdayOERLoadImageScore.isEmpty()){
                
                oer_error_last_score_image.visibility = View.VISIBLE
                last_image_score_value.visibility = View.GONE
            }else{
                last_image_score_value.text = gmYesterdayOERLoadImageScore
            }
            if(gmYesterdayOERLoadProductScore.isEmpty()){
                
                oer_error_last_score_product.visibility = View.VISIBLE
                last_product_score_value.visibility = View.GONE
            }else{
                last_product_score_value.text = gmYesterdayOERLoadProductScore
            }

            if(gmYesterdayOERLoadEatScore.isEmpty()){
                
                oer_error_last_score_eat.visibility = View.VISIBLE
                last_eADT_score_value.visibility = View.GONE
            }else{
                last_eADT_score_value.text = gmYesterdayOERLoadEatScore
            }
            if(gmYesterdayOERLoadExtremeScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_extreme_delivery_score_value.visibility = View.GONE
            }else{
                last_extreme_delivery_score_value.text = gmYesterdayOERLoadExtremeScore
            }
            if(gmYesterdayOERLoadSingleScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_single_score_value.visibility = View.GONE
            }else{
                last_single_score_value.text = gmYesterdayOERLoadSingleScore
            }
            if(gmYesterdayOERLoadLastScore.isEmpty()){
                
                oer_error_last_score_load.visibility = View.VISIBLE
                last_load_score_values.visibility = View.GONE
            }else{
                last_load_score_values.text = gmYesterdayOERLoadLastScore
            }

            // last data
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"OER Overview Yesterday KPI")
        }

    }

    private fun rangeViewOer(oerTodayDetail: StorePeriodRangeKPIQuery.GeneralManager) {
        try {
            val rangeViewOerData = oerTodayDetail.kpis?.store?.period?.oerStart

            Logger.info("OER Period Range", "OER Overview KPI")

            // display name
            Validation().checkNullValueToShowView(this, rangeViewOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeViewOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeViewOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, rangeViewOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, rangeViewOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, rangeViewOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, rangeViewOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, rangeViewOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, rangeViewOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, rangeViewOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, rangeViewOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, rangeViewOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            if(rangeViewOerData?.lastOERScore!=null){
                last_oer_parent_view.visibility = View.VISIBLE
            }else{
                last_oer_parent_view.visibility = View.GONE
            }

            if (rangeViewOerData == null || rangeViewOerData.displayName.isNullOrEmpty()) {
                ll_oer_start_text.visibility = View.GONE
            } else {
                ll_oer_start_text.visibility = View.VISIBLE
                oer_start_text.text = rangeViewOerData.displayName
            }
            if (rangeViewOerData?.twentyEightDayeADT == null || rangeViewOerData.twentyEightDayeADT.displayName.isNullOrEmpty()) {
                twenty_day_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_eadt_parent.visibility = View.VISIBLE
                twenty_day_eadt_display.text = rangeViewOerData.twentyEightDayeADT.displayName
            }
            if (rangeViewOerData?.twentyEightExtremeDeliveries == null || rangeViewOerData.twentyEightExtremeDeliveries.displayName.isNullOrEmpty()) {
                twenty_day_extreme_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_extreme_eadt_parent.visibility = View.VISIBLE
                twenty_day_extreme_adt_display.text = rangeViewOerData.twentyEightExtremeDeliveries.displayName
            }
            if (rangeViewOerData?.twentyEightSingles == null || rangeViewOerData.twentyEightSingles.displayName.isNullOrEmpty()) {
                twenty_eight_singles_parent.visibility = View.GONE
            } else {
                twenty_eight_singles_parent.visibility = View.VISIBLE
                twenty_eight_singles_display.text = rangeViewOerData.twentyEightSingles.displayName
            }
            if (rangeViewOerData?.twentyEightLoad == null || rangeViewOerData.twentyEightLoad.displayName.isNullOrEmpty()) {
                twenty_day_load_parent.visibility = View.GONE
            } else {
                twenty_day_load_parent.visibility = View.VISIBLE
                twenty_day_load_display.text = rangeViewOerData.twentyEightLoad.displayName
            }

            oer_sales.text = Validation().checkAmountPercentageValue(this,
                                                                     rangeViewOerData?.actual?.amount,
                                                                     rangeViewOerData?.actual?.percentage,
                                                                     rangeViewOerData?.actual?.value)

            if (rangeViewOerData?.status != null) {
                when {
                    rangeViewOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        oer_sales.setTextColor(getColor(R.color.red))
                    }
                    rangeViewOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                        oer_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        oer_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // scroll detect
            scroll_oer_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (scroll_oer_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text =
                                rangeViewOerData?.displayName ?: getString(R.string.oer_start_text)

                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                        rangeViewOerData?.actual?.amount,
                                                                        rangeViewOerData?.actual?.percentage,
                                                                        rangeViewOerData?.actual?.value)

                            if (rangeViewOerData?.status != null) {
                                when {
                                    rangeViewOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    rangeViewOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    } else -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.text_color))
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
                        y = scroll_oer_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })
            oer_goal_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                          rangeViewOerData?.goal?.amount,
                                                                          rangeViewOerData?.goal?.percentage,
                                                                          rangeViewOerData?.goal?.value)
            oer_variance_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                              rangeViewOerData?.variance?.amount,
                                                                              rangeViewOerData?.variance?.percentage,
                                                                              rangeViewOerData?.variance?.value)




            // 28 days
            val gmRange28DayEat = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                          rangeViewOerData?.twentyEightDayeADT?.actual?.amount,
                                                                          rangeViewOerData?.twentyEightDayeADT?.actual?.percentage,
                                                                          rangeViewOerData?.twentyEightDayeADT?.actual?.value)

            val gmRanger28DayExtreme = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                               rangeViewOerData?.twentyEightExtremeDeliveries?.actual?.amount,
                                                                               rangeViewOerData?.twentyEightExtremeDeliveries?.actual?.percentage,
                                                                               rangeViewOerData?.twentyEightExtremeDeliveries?.actual?.value)

            val gmRanger28DaySingle = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                              rangeViewOerData?.twentyEightSingles?.actual?.amount,
                                                                              rangeViewOerData?.twentyEightSingles?.actual?.percentage,
                                                                              rangeViewOerData?.twentyEightSingles?.actual?.value)

            val gmRanger28DayLoad = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                            rangeViewOerData?.twentyEightLoad?.actual?.amount,
                                                                            rangeViewOerData?.twentyEightLoad?.actual?.percentage,
                                                                            rangeViewOerData?.twentyEightLoad?.actual?.value)

            if(gmRange28DayEat.isEmpty()){
                
                oer_error_28days_eat.visibility = View.VISIBLE
                twenty_eight_days_eat_value.visibility = View.GONE
                val param28DaysEatGMRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysEatGMRangeError.weight = 1.0f
                twenty_day_eadt_display.layoutParams = param28DaysEatGMRangeError
            }else{
                twenty_eight_days_eat_value.text = gmRange28DayEat
            }
            if(gmRanger28DayExtreme.isEmpty()){
                
                oer_error_28days_extreme.visibility = View.VISIBLE
                twenty_eight_extreme_delivery_value.visibility = View.GONE
                val param28DaysExtremeGMRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysExtremeGMRangeError.weight = 1.0f
                twenty_day_extreme_adt_display.layoutParams = param28DaysExtremeGMRangeError
            }else{
                twenty_eight_extreme_delivery_value.text = gmRanger28DayExtreme
            }
            if(gmRanger28DaySingle.isEmpty()){
                
                oer_error_28days_single.visibility = View.VISIBLE
                twenty_eight_single_value.visibility = View.GONE
                val param28DaysSingleGMRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysSingleGMRangeError.weight = 1.0f
                twenty_eight_singles_display.layoutParams = param28DaysSingleGMRangeError
            }else{
                twenty_eight_single_value.text = gmRanger28DaySingle
            }
            if(gmRanger28DayLoad.isEmpty()){
                
                oer_error_28days_load.visibility = View.VISIBLE
                twenty_eight_days_load_value.visibility = View.GONE
                val param28DaysLoadGMRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysLoadGMRangeError.weight = 1.0f
                twenty_day_load_display.layoutParams = param28DaysLoadGMRangeError
            }else{
                twenty_eight_days_load_value.text = gmRanger28DayLoad
            }

            
            if (rangeViewOerData?.twentyEightDayeADT?.status != null) {
                when {
                    rangeViewOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.red))
                    }
                    rangeViewOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            
            if (rangeViewOerData?.twentyEightExtremeDeliveries?.status != null) {

                when {
                    rangeViewOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.red))
                    }
                    rangeViewOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.green))

                    }
                    else-> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            

            if (rangeViewOerData?.twentyEightSingles?.status != null) {

                when {
                    rangeViewOerData.twentyEightSingles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.red))
                    }
                    rangeViewOerData.twentyEightSingles.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            //
            
            if (rangeViewOerData?.twentyEightLoad?.status != null) {

                when {
                    rangeViewOerData.twentyEightLoad.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.red))
                    }
                    rangeViewOerData.twentyEightLoad.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
 
 
 
 
            // if (rangeViewOerData?.lastOERScore != null) {
            last_food_score.text = rangeViewOerData?.lastOERScore?.lastFoodSafetyScore?.displayName
                ?: getString(R.string.last_food_score)
            last_image_score.text = rangeViewOerData?.lastOERScore?.lastImageScore?.displayName
                ?: getString(R.string.last_image_score)
            last_product_score.text = rangeViewOerData?.lastOERScore?.lastProductScore?.displayName
                ?: getString(R.string.last_product_score)
            last_eADT_score.text =
                rangeViewOerData?.lastOERScore?.lasteADTScore?.displayName
                    ?: getString(R.string.last_eADT_score)
            last_extreme_delivery_score.text =
                rangeViewOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName
                    ?: getString(R.string.last_extreme_delivery_score)
            last_single_score.text = rangeViewOerData?.lastOERScore?.lastSinglesScore?.displayName
                ?: getString(R.string.last_single_score)
            last_load_score.text =
                rangeViewOerData?.lastOERScore?.lastLoadScore?.displayName
                    ?: getString(R.string.last_load_score)
            last_oer_score_display.text =
                rangeViewOerData?.lastOERScore?.total?.displayName ?: getString(R.string.last_oer_score)

            val gmRangeOERLoadScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                              rangeViewOerData?.lastOERScore?.total?.actual?.amount,
                                                                              rangeViewOerData?.lastOERScore?.total?.actual?.percentage,
                                                                              rangeViewOerData?.lastOERScore?.total?.actual?.value)

            val gmRangeOERLoadFoodScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                  rangeViewOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.amount,
                                                                                  rangeViewOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.percentage,
                                                                                  rangeViewOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.value)

            val gmRangeOERLoadImageScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                   rangeViewOerData?.lastOERScore?.lastImageScore?.actual?.amount,
                                                                                   rangeViewOerData?.lastOERScore?.lastImageScore?.actual?.percentage,
                                                                                   rangeViewOerData?.lastOERScore?.lastImageScore?.actual?.value)

            val gmRangeOERLoadProductScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                     rangeViewOerData?.lastOERScore?.lastProductScore?.actual?.amount,
                                                                                     rangeViewOerData?.lastOERScore?.lastProductScore?.actual?.percentage,
                                                                                     rangeViewOerData?.lastOERScore?.lastProductScore?.actual?.value)

            val gmRangeOERLoadEatScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                  rangeViewOerData?.lastOERScore?.lasteADTScore?.actual?.amount,
                                                                                  rangeViewOerData?.lastOERScore?.lasteADTScore?.actual?.percentage,
                                                                                  rangeViewOerData?.lastOERScore?.lasteADTScore?.actual?.value)

            val gmRangeOERLoadExtremeScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                     rangeViewOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.amount,
                                                                                     rangeViewOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.percentage,
                                                                                     rangeViewOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.value)

            val gmRangeOERLoadSingleScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                    rangeViewOerData?.lastOERScore?.lastSinglesScore?.actual?.amount,
                                                                                    rangeViewOerData?.lastOERScore?.lastSinglesScore?.actual?.percentage,
                                                                                    rangeViewOerData?.lastOERScore?.lastSinglesScore?.actual?.value)


            val gmRangeOERLoadLastScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                   rangeViewOerData?.lastOERScore?.lastLoadScore?.actual?.amount,
                                                                                   rangeViewOerData?.lastOERScore?.lastLoadScore?.actual?.percentage,
                                                                                   rangeViewOerData?.lastOERScore?.lastLoadScore?.actual?.value)


            if(gmRangeOERLoadScore.isEmpty()){
                
                oer_error_last_score.visibility = View.VISIBLE
                last_oer_score_value.visibility = View.GONE
            }else{
                last_oer_score_value.text = gmRangeOERLoadScore
            }
            if(gmRangeOERLoadFoodScore.isEmpty()){
                
                oer_error_last_score_food.visibility = View.VISIBLE
                last_food_score_value.visibility = View.GONE
            }else{
                last_food_score_value.text = gmRangeOERLoadFoodScore
            }
            if(gmRangeOERLoadImageScore.isEmpty()){
                
                oer_error_last_score_image.visibility = View.VISIBLE
                last_image_score_value.visibility = View.GONE
            }else{
                last_image_score_value.text = gmRangeOERLoadImageScore
            }
            if(gmRangeOERLoadProductScore.isEmpty()){
                
                oer_error_last_score_product.visibility = View.VISIBLE
                last_product_score_value.visibility = View.GONE
            }else{
                last_product_score_value.text = gmRangeOERLoadProductScore
            }

            if(gmRangeOERLoadEatScore.isEmpty()){
                
                oer_error_last_score_eat.visibility = View.VISIBLE
                last_eADT_score_value.visibility = View.GONE
            }else{
                last_eADT_score_value.text = gmRangeOERLoadEatScore
            }
            if(gmRangeOERLoadExtremeScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_extreme_delivery_score_value.visibility = View.GONE
            }else{
                last_extreme_delivery_score_value.text = gmRangeOERLoadExtremeScore
            }
            if(gmRangeOERLoadSingleScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_single_score_value.visibility = View.GONE
            }else{
                last_single_score_value.text = gmRangeOERLoadSingleScore
            }
            if(gmRangeOERLoadLastScore.isEmpty()){
                
                oer_error_last_score_load.visibility = View.VISIBLE
                last_load_score_values.visibility = View.GONE
            }else{
                last_load_score_values.text = gmRangeOERLoadLastScore
            }

            if (rangeViewOerData?.lastOERScore?.total?.status != null) {

                when {
                    rangeViewOerData.lastOERScore.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.red))
                    }
                    rangeViewOerData.lastOERScore.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // last data
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"OER Overview Period Range KPI")
        }

    }

    // ceo
    private fun todayViewCeoOer(oerTodayDetail: CEOOverviewTodayQuery.Ceo) {
        val todayViewCeoOerData = oerTodayDetail.kpis?.supervisors?.stores?.today?.oerStart
        Logger.info("OER Today", "OER Overview KPI")

        // display name
        oer_start_text.text = todayViewCeoOerData!!.displayName ?: getString(R.string.oer_start_text)
        last_food_score.text = todayViewCeoOerData.lastOERScore!!.lastFoodSafetyScore!!.displayName
            ?: getString(R.string.last_food_score)
        last_image_score.text = todayViewCeoOerData.lastOERScore.lastImageScore!!.displayName
            ?: getString(R.string.last_image_score)
        last_product_score.text = todayViewCeoOerData.lastOERScore.lastProductScore!!.displayName
            ?: getString(R.string.last_product_score)
        last_eADT_score.text =
            todayViewCeoOerData.lastOERScore.lasteADTScore!!.displayName ?: getString(R.string.last_eADT_score)
        last_extreme_delivery_score.text =
            todayViewCeoOerData.lastOERScore.lastExtremeDeliveriesScore!!.displayName
                ?: getString(R.string.last_extreme_delivery_score)
        last_single_score.text = todayViewCeoOerData.lastOERScore.lastSinglesScore!!.displayName
            ?: getString(R.string.last_single_score)
        last_load_score.text =
            todayViewCeoOerData.lastOERScore.lastLoadScore!!.displayName ?: getString(R.string.last_load_score)

        twenty_day_eadt_display.text =
            todayViewCeoOerData.twentyEightDayeADT?.displayName ?: getString(R.string.twenty_eight_days_eat)
        twenty_day_extreme_adt_display.text = todayViewCeoOerData.twentyEightExtremeDeliveries?.displayName
            ?: getString(R.string.twenty_eight_extreme_delivery)
        twenty_eight_singles_display.text =
            todayViewCeoOerData.twentyEightSingles?.displayName ?: getString(R.string.twenty_eight_single)
        twenty_day_load_display.text =
            todayViewCeoOerData.twentyEightLoad?.displayName ?: getString(R.string.twenty_eight_days_load)
        last_oer_score_display.text =
            todayViewCeoOerData.lastOERScore.total?.displayName ?: getString(R.string.last_oer_score)


        if (todayViewCeoOerData.actual?.value?.isNaN() == false && todayViewCeoOerData.status != null) {
            oer_sales.text = Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.actual.value)
            when {
                todayViewCeoOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oer_sales.setTextColor(getColor(R.color.red))
                }
                todayViewCeoOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                    oer_sales.setTextColor(getColor(R.color.green))
                } else -> {
                    oer_sales.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        // scroll detect
        scroll_oer_parent.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 0f
                override fun onScrollChanged() {
                    if (scroll_oer_parent.scrollY > y) {
                        parent_data_on_scroll_linear.visibility = View.VISIBLE
                        parent_data_on_scroll_view.visibility = View.VISIBLE
                        level_two_scroll_data_action.text =
                            todayViewCeoOerData.displayName ?: getString(R.string.oer_start_text)

                        level_two_scroll_data_action_value.text = todayViewCeoOerData.actual?.value.toString()
                        if (todayViewCeoOerData.actual?.value?.isNaN() == false && todayViewCeoOerData.status != null) {
                            level_two_scroll_data_action_value.text =
                                Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.actual.value)
                            when {
                                todayViewCeoOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.red_circle,
                                        0
                                    )
                                }
                                todayViewCeoOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.green_circle,
                                        0
                                    )
                                } else -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.text_color))
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
                    y = scroll_oer_parent.scrollY.toFloat()
                    if (y <= 0) {
                        parent_data_on_scroll_linear.visibility = View.INVISIBLE
                        parent_data_on_scroll_view.visibility = View.GONE

                    }
                }
            })
        oer_goal_value.text =
            if (todayViewCeoOerData.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.goal.value) else ""
        oer_variance_value.text =
            if (todayViewCeoOerData.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoOerData.variance.value) else ""


        // 28 days

        if (todayViewCeoOerData.twentyEightDayeADT?.actual?.value?.isNaN() == false && todayViewCeoOerData.twentyEightDayeADT.status != null) {
            twenty_eight_days_eat_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.twentyEightDayeADT.actual.value)
            when {
                todayViewCeoOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    twenty_eight_days_eat_value.setTextColor(getColor(R.color.red))
                }
                todayViewCeoOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    twenty_eight_days_eat_value.setTextColor(getColor(R.color.green))

                } else -> {
                    twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    twenty_eight_days_eat_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        if (todayViewCeoOerData.twentyEightExtremeDeliveries?.actual?.value?.isNaN() == false && todayViewCeoOerData.twentyEightExtremeDeliveries.status != null) {
            twenty_eight_extreme_delivery_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.twentyEightExtremeDeliveries.actual.value)
            when {
                todayViewCeoOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(R.string.out_of_range) -> {
                    twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.red))
                }
                todayViewCeoOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.green))

                } else -> {
                    twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (todayViewCeoOerData.twentyEightSingles?.actual?.value?.isNaN() == false && todayViewCeoOerData.twentyEightSingles.status != null) {
            twenty_eight_single_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.twentyEightSingles.actual.value)
            when {
                todayViewCeoOerData.twentyEightSingles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    twenty_eight_single_value.setTextColor(getColor(R.color.red))
                }
                todayViewCeoOerData.twentyEightSingles.status.toString() == resources.getString(R.string.under_limit) -> {
                    twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    twenty_eight_single_value.setTextColor(getColor(R.color.green))

                } else -> {
                    twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    twenty_eight_single_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        if (todayViewCeoOerData.twentyEightLoad?.actual?.value?.isNaN() == false && todayViewCeoOerData.twentyEightLoad.status != null) {
            twenty_eight_days_load_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.twentyEightLoad.actual.value)
            when {
                todayViewCeoOerData.twentyEightLoad.status.toString() == resources.getString(R.string.out_of_range) -> {
                    twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    twenty_eight_days_load_value.setTextColor(getColor(R.color.red))
                }
                todayViewCeoOerData.twentyEightLoad.status.toString() == resources.getString(R.string.under_limit) -> {
                    twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    twenty_eight_days_load_value.setTextColor(getColor(R.color.green))

                } else -> {
                    twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    twenty_eight_days_load_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        if (todayViewCeoOerData.lastOERScore.total?.actual?.value?.isNaN() == false && todayViewCeoOerData.lastOERScore.total.status != null) {
            last_oer_score_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.lastOERScore.total.actual.value)
            when {
                todayViewCeoOerData.lastOERScore.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    last_oer_score_value.setTextColor(getColor(R.color.red))
                }
                todayViewCeoOerData.lastOERScore.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    last_oer_score_value.setTextColor(getColor(R.color.green))

                } else -> {
                    last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    last_oer_score_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        // last data
        last_food_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.lastOERScore.lastFoodSafetyScore?.actual?.value)
        last_image_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.lastOERScore.lastImageScore.actual?.value)
        last_product_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.lastOERScore.lastProductScore.actual?.value)
        last_eADT_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.lastOERScore.lasteADTScore.actual?.value)
        last_extreme_delivery_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.lastOERScore.lastExtremeDeliveriesScore.actual?.value)

        last_single_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.lastOERScore.lastSinglesScore.actual?.value)

        last_load_score_values.text =
            Validation().ignoreZeroAfterDecimal(todayViewCeoOerData.lastOERScore.lastLoadScore.actual?.value)


    }

    private fun yesterdayViewCeoOer(oerTodayDetail: CEOOverviewYesterdayQuery.Ceo) {
        try {
            val yesterdayViewCeoOerData = oerTodayDetail.kpis?.supervisors?.stores?.yesterday?.oerStart

            Logger.info("OER Yesterday", "OER Overview KPI")
            // display name
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            if(yesterdayViewCeoOerData?.lastOERScore!=null){
                last_oer_parent_view.visibility = View.VISIBLE
            }else{
                last_oer_parent_view.visibility = View.GONE
            }
            if (yesterdayViewCeoOerData == null || yesterdayViewCeoOerData.displayName.isNullOrEmpty()) {
                ll_oer_start_text.visibility = View.GONE
            } else {
                ll_oer_start_text.visibility = View.VISIBLE
                oer_start_text.text = yesterdayViewCeoOerData.displayName
            }
            if (yesterdayViewCeoOerData?.twentyEightDayeADT == null || yesterdayViewCeoOerData.twentyEightDayeADT.displayName.isNullOrEmpty()) {
                twenty_day_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_eadt_parent.visibility = View.VISIBLE
                twenty_day_eadt_display.text = yesterdayViewCeoOerData.twentyEightDayeADT.displayName
            }
            if (yesterdayViewCeoOerData?.twentyEightExtremeDeliveries == null || yesterdayViewCeoOerData.twentyEightExtremeDeliveries.displayName.isNullOrEmpty()) {
                twenty_day_extreme_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_extreme_eadt_parent.visibility = View.VISIBLE
                twenty_day_extreme_adt_display.text = yesterdayViewCeoOerData.twentyEightExtremeDeliveries.displayName
            }
            if (yesterdayViewCeoOerData?.twentyEightSingles == null || yesterdayViewCeoOerData.twentyEightSingles.displayName.isNullOrEmpty()) {
                twenty_eight_singles_parent.visibility = View.GONE
            } else {
                twenty_eight_singles_parent.visibility = View.VISIBLE
                twenty_eight_singles_display.text = yesterdayViewCeoOerData.twentyEightSingles.displayName
            }
            if (yesterdayViewCeoOerData?.twentyEightLoad == null || yesterdayViewCeoOerData.twentyEightLoad.displayName.isNullOrEmpty()) {
                twenty_day_load_parent.visibility = View.GONE
            } else {
                twenty_day_load_parent.visibility = View.VISIBLE
                twenty_day_load_display.text = yesterdayViewCeoOerData.twentyEightLoad.displayName
            }



            oer_sales.text = Validation().checkAmountPercentageValue(this,
                                                                     yesterdayViewCeoOerData?.actual?.amount,
                                                                     yesterdayViewCeoOerData?.actual?.percentage,
                                                                     yesterdayViewCeoOerData?.actual?.value)

            if (yesterdayViewCeoOerData?.status != null) {
                when {
                    yesterdayViewCeoOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        oer_sales.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                        oer_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        oer_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // scroll detect
            scroll_oer_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (scroll_oer_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text =
                                yesterdayViewCeoOerData?.displayName ?: getString(R.string.oer_start_text)

                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                        yesterdayViewCeoOerData?.actual?.amount,
                                                                        yesterdayViewCeoOerData?.actual?.percentage,
                                                                        yesterdayViewCeoOerData?.actual?.value)

                            if (yesterdayViewCeoOerData?.status != null) {
                                when {
                                    yesterdayViewCeoOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    yesterdayViewCeoOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    } else -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.text_color))
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
                        y = scroll_oer_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })
            oer_goal_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                          yesterdayViewCeoOerData?.goal?.amount,
                                                                          yesterdayViewCeoOerData?.goal?.percentage,
                                                                          yesterdayViewCeoOerData?.goal?.value)
            oer_variance_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                              yesterdayViewCeoOerData?.variance?.amount,
                                                                              yesterdayViewCeoOerData?.variance?.percentage,
                                                                              yesterdayViewCeoOerData?.variance?.value)



            // 28 days
            val ceoYesterday28DayEat = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                               yesterdayViewCeoOerData?.twentyEightDayeADT?.actual?.amount,
                                                                               yesterdayViewCeoOerData?.twentyEightDayeADT?.actual?.percentage,
                                                                               yesterdayViewCeoOerData?.twentyEightDayeADT?.actual?.value)

            val ceoYesterday28DayExtreme = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                   yesterdayViewCeoOerData?.twentyEightExtremeDeliveries?.actual?.amount,
                                                                                   yesterdayViewCeoOerData?.twentyEightExtremeDeliveries?.actual?.percentage,
                                                                                   yesterdayViewCeoOerData?.twentyEightExtremeDeliveries?.actual?.value)

            val ceoYesterday28DaySingle = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                  yesterdayViewCeoOerData?.twentyEightSingles?.actual?.amount,
                                                                                  yesterdayViewCeoOerData?.twentyEightSingles?.actual?.percentage,
                                                                                  yesterdayViewCeoOerData?.twentyEightSingles?.actual?.value)

            val ceoYesterday28DayLoad = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                yesterdayViewCeoOerData?.twentyEightLoad?.actual?.amount,
                                                                                yesterdayViewCeoOerData?.twentyEightLoad?.actual?.percentage,
                                                                                yesterdayViewCeoOerData?.twentyEightLoad?.actual?.value)

            if(ceoYesterday28DayEat.isEmpty()){
                
                oer_error_28days_eat.visibility = View.VISIBLE
                twenty_eight_days_eat_value.visibility = View.GONE
                val param28DaysEatCEOYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysEatCEOYesterdayError.weight = 1.0f
                twenty_day_eadt_display.layoutParams = param28DaysEatCEOYesterdayError
            }else{
                twenty_eight_days_eat_value.text = ceoYesterday28DayEat
            }
            if(ceoYesterday28DayExtreme.isEmpty()){
                
                oer_error_28days_extreme.visibility = View.VISIBLE
                twenty_eight_extreme_delivery_value.visibility = View.GONE
                val param28DaysExtremeCEOYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysExtremeCEOYesterdayError.weight = 1.0f
                twenty_day_extreme_adt_display.layoutParams = param28DaysExtremeCEOYesterdayError
            }else{
                twenty_eight_extreme_delivery_value.text = ceoYesterday28DayExtreme
            }
            if(ceoYesterday28DaySingle.isEmpty()){
                
                oer_error_28days_single.visibility = View.VISIBLE
                twenty_eight_single_value.visibility = View.GONE
                val param28DaysSingleCEOYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysSingleCEOYesterdayError.weight = 1.0f
                twenty_eight_singles_display.layoutParams = param28DaysSingleCEOYesterdayError
            }else{
                twenty_eight_single_value.text = ceoYesterday28DaySingle
            }
            if(ceoYesterday28DayLoad.isEmpty()){
                
                oer_error_28days_load.visibility = View.VISIBLE
                twenty_eight_days_load_value.visibility = View.GONE
                val param28DaysLoadCEOYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysLoadCEOYesterdayError.weight = 1.0f
                twenty_day_load_display.layoutParams = param28DaysLoadCEOYesterdayError
            }else{
                twenty_eight_days_load_value.text = ceoYesterday28DayLoad
            }

            if (yesterdayViewCeoOerData?.twentyEightDayeADT?.status != null) {
                when {
                    yesterdayViewCeoOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (yesterdayViewCeoOerData?.twentyEightExtremeDeliveries?.status != null) {

                when {
                    yesterdayViewCeoOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            if (yesterdayViewCeoOerData?.twentyEightSingles?.status != null) {

                when {
                    yesterdayViewCeoOerData.twentyEightSingles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoOerData.twentyEightSingles.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            //

            if (yesterdayViewCeoOerData?.twentyEightLoad?.status != null) {

                when {
                    yesterdayViewCeoOerData.twentyEightLoad.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoOerData.twentyEightLoad.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            last_oer_score_display.text =
                yesterdayViewCeoOerData?.lastOERScore?.total?.displayName ?: getString(R.string.last_oer_score)

            last_food_score.text = yesterdayViewCeoOerData?.lastOERScore?.lastFoodSafetyScore?.displayName
                ?: getString(R.string.last_food_score)
            last_image_score.text = yesterdayViewCeoOerData?.lastOERScore?.lastImageScore?.displayName
                ?: getString(R.string.last_image_score)
            last_product_score.text = yesterdayViewCeoOerData?.lastOERScore?.lastProductScore?.displayName
                ?: getString(R.string.last_product_score)
            last_eADT_score.text =
                yesterdayViewCeoOerData?.lastOERScore?.lasteADTScore?.displayName
                    ?: getString(R.string.last_eADT_score)
            last_extreme_delivery_score.text =
                yesterdayViewCeoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName
                    ?: getString(R.string.last_extreme_delivery_score)
            last_single_score.text = yesterdayViewCeoOerData?.lastOERScore?.lastSinglesScore?.displayName
                ?: getString(R.string.last_single_score)
            last_load_score.text =
                yesterdayViewCeoOerData?.lastOERScore?.lastLoadScore?.displayName
                    ?: getString(R.string.last_load_score)

            val ceoYesterdayOERLoadScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                   yesterdayViewCeoOerData?.lastOERScore?.total?.actual?.amount,
                                                                                   yesterdayViewCeoOerData?.lastOERScore?.total?.actual?.percentage,
                                                                                   yesterdayViewCeoOerData?.lastOERScore?.total?.actual?.value)

            val ceoYesterdayOERLoadFoodScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                       yesterdayViewCeoOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.amount,
                                                                                       yesterdayViewCeoOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.percentage,
                                                                                       yesterdayViewCeoOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.value)

            val ceoYesterdayOERLoadImageScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                        yesterdayViewCeoOerData?.lastOERScore?.lastImageScore?.actual?.amount,
                                                                                        yesterdayViewCeoOerData?.lastOERScore?.lastImageScore?.actual?.percentage,
                                                                                        yesterdayViewCeoOerData?.lastOERScore?.lastImageScore?.actual?.value)

            val ceoYesterdayOERLoadProductScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                          yesterdayViewCeoOerData?.lastOERScore?.lastProductScore?.actual?.amount,
                                                                                          yesterdayViewCeoOerData?.lastOERScore?.lastProductScore?.actual?.percentage,
                                                                                          yesterdayViewCeoOerData?.lastOERScore?.lastProductScore?.actual?.value)

            val ceoYesterdayOERLoadEatScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                       yesterdayViewCeoOerData?.lastOERScore?.lasteADTScore?.actual?.amount,
                                                                                       yesterdayViewCeoOerData?.lastOERScore?.lasteADTScore?.actual?.percentage,
                                                                                       yesterdayViewCeoOerData?.lastOERScore?.lasteADTScore?.actual?.value)

            val ceoYesterdayOERLoadExtremeScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                          yesterdayViewCeoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.amount,
                                                                                          yesterdayViewCeoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.percentage,
                                                                                          yesterdayViewCeoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.value)

            val ceoYesterdayOERLoadSingleScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                         yesterdayViewCeoOerData?.lastOERScore?.lastSinglesScore?.actual?.amount,
                                                                                         yesterdayViewCeoOerData?.lastOERScore?.lastSinglesScore?.actual?.percentage,
                                                                                         yesterdayViewCeoOerData?.lastOERScore?.lastSinglesScore?.actual?.value)


            val ceoYesterdayOERLoadLastScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                        yesterdayViewCeoOerData?.lastOERScore?.lastLoadScore?.actual?.amount,
                                                                                        yesterdayViewCeoOerData?.lastOERScore?.lastLoadScore?.actual?.percentage,
                                                                                        yesterdayViewCeoOerData?.lastOERScore?.lastLoadScore?.actual?.value)


            if(ceoYesterdayOERLoadScore.isEmpty()){
                
                oer_error_last_score.visibility = View.VISIBLE
                last_oer_score_value.visibility = View.GONE
            }else{
                last_oer_score_value.text = ceoYesterdayOERLoadScore
            }
            if(ceoYesterdayOERLoadFoodScore.isEmpty()){
                
                oer_error_last_score_food.visibility = View.VISIBLE
                last_food_score_value.visibility = View.GONE
            }else{
                last_food_score_value.text = ceoYesterdayOERLoadFoodScore
            }
            if(ceoYesterdayOERLoadImageScore.isEmpty()){
                
                oer_error_last_score_image.visibility = View.VISIBLE
                last_image_score_value.visibility = View.GONE
            }else{
                last_image_score_value.text = ceoYesterdayOERLoadImageScore
            }
            if(ceoYesterdayOERLoadProductScore.isEmpty()){
                
                oer_error_last_score_product.visibility = View.VISIBLE
                last_product_score_value.visibility = View.GONE
            }else{
                last_product_score_value.text = ceoYesterdayOERLoadProductScore
            }

            if(ceoYesterdayOERLoadEatScore.isEmpty()){
                
                oer_error_last_score_eat.visibility = View.VISIBLE
                last_eADT_score_value.visibility = View.GONE
            }else{
                last_eADT_score_value.text = ceoYesterdayOERLoadEatScore
            }
            if(ceoYesterdayOERLoadExtremeScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_extreme_delivery_score_value.visibility = View.GONE
            }else{
                last_extreme_delivery_score_value.text = ceoYesterdayOERLoadExtremeScore
            }
            if(ceoYesterdayOERLoadSingleScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_single_score_value.visibility = View.GONE
            }else{
                last_single_score_value.text = ceoYesterdayOERLoadSingleScore
            }
            if(ceoYesterdayOERLoadLastScore.isEmpty()){
                
                oer_error_last_score_load.visibility = View.VISIBLE
                last_load_score_values.visibility = View.GONE
            }else{
                last_load_score_values.text = ceoYesterdayOERLoadLastScore
            }

            if (yesterdayViewCeoOerData?.lastOERScore?.total?.status != null) {

                when {
                    yesterdayViewCeoOerData.lastOERScore.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoOerData.lastOERScore.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // last data
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"OER Overview Yesterday KPI")
        }

    }

    private fun rangeOverViewCeoOer(oerTodayDetail: CEOOverviewRangeQuery.Ceo) {
        try {
            val rangeOverViewCeoOerData = oerTodayDetail.kpis?.supervisors?.stores?.period?.oerStart

            Logger.info("OER Period Range", "OER Overview KPI")
            // display name
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            if(rangeOverViewCeoOerData?.lastOERScore!=null){
                last_oer_parent_view.visibility = View.VISIBLE
            }else{
                last_oer_parent_view.visibility = View.GONE
            }
            if (rangeOverViewCeoOerData == null || rangeOverViewCeoOerData.displayName.isNullOrEmpty()) {
                ll_oer_start_text.visibility = View.GONE
            } else {
                ll_oer_start_text.visibility = View.VISIBLE
                oer_start_text.text = rangeOverViewCeoOerData.displayName
            }
            if (rangeOverViewCeoOerData?.twentyEightDayeADT == null || rangeOverViewCeoOerData.twentyEightDayeADT.displayName.isNullOrEmpty()) {
                twenty_day_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_eadt_parent.visibility = View.VISIBLE
                twenty_day_eadt_display.text = rangeOverViewCeoOerData.twentyEightDayeADT.displayName
            }
            if (rangeOverViewCeoOerData?.twentyEightExtremeDeliveries == null || rangeOverViewCeoOerData.twentyEightExtremeDeliveries.displayName.isNullOrEmpty()) {
                twenty_day_extreme_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_extreme_eadt_parent.visibility = View.VISIBLE
                twenty_day_extreme_adt_display.text = rangeOverViewCeoOerData.twentyEightExtremeDeliveries.displayName
            }
            if (rangeOverViewCeoOerData?.twentyEightSingles == null || rangeOverViewCeoOerData.twentyEightSingles.displayName.isNullOrEmpty()) {
                twenty_eight_singles_parent.visibility = View.GONE
            } else {
                twenty_eight_singles_parent.visibility = View.VISIBLE
                twenty_eight_singles_display.text = rangeOverViewCeoOerData.twentyEightSingles.displayName
            }
            if (rangeOverViewCeoOerData?.twentyEightLoad == null || rangeOverViewCeoOerData.twentyEightLoad.displayName.isNullOrEmpty()) {
                twenty_day_load_parent.visibility = View.GONE
            } else {
                twenty_day_load_parent.visibility = View.VISIBLE
                twenty_day_load_display.text = rangeOverViewCeoOerData.twentyEightLoad.displayName
            }



            oer_sales.text = Validation().checkAmountPercentageValue(this,
                                                                     rangeOverViewCeoOerData?.actual?.amount,
                                                                     rangeOverViewCeoOerData?.actual?.percentage,
                                                                     rangeOverViewCeoOerData?.actual?.value)

            if (rangeOverViewCeoOerData?.status != null) {
                when {
                    rangeOverViewCeoOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        oer_sales.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCeoOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                        oer_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        oer_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // scroll detect
            scroll_oer_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (scroll_oer_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text =
                                rangeOverViewCeoOerData?.displayName ?: getString(R.string.oer_start_text)

                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                        rangeOverViewCeoOerData?.actual?.amount,
                                                                        rangeOverViewCeoOerData?.actual?.percentage,
                                                                        rangeOverViewCeoOerData?.actual?.value)

                            if (rangeOverViewCeoOerData?.status != null) {
                                when {
                                    rangeOverViewCeoOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    rangeOverViewCeoOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    } else -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.text_color))
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
                        y = scroll_oer_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })
            oer_goal_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                          rangeOverViewCeoOerData?.goal?.amount,
                                                                          rangeOverViewCeoOerData?.goal?.percentage,
                                                                          rangeOverViewCeoOerData?.goal?.value)
            oer_variance_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                              rangeOverViewCeoOerData?.variance?.amount,
                                                                              rangeOverViewCeoOerData?.variance?.percentage,
                                                                              rangeOverViewCeoOerData?.variance?.value)



            // 28 days

            val ceoRange28DayEat = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                           rangeOverViewCeoOerData?.twentyEightDayeADT?.actual?.amount,
                                                                           rangeOverViewCeoOerData?.twentyEightDayeADT?.actual?.percentage,
                                                                           rangeOverViewCeoOerData?.twentyEightDayeADT?.actual?.value)

            val ceoRanger28DayExtreme = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                rangeOverViewCeoOerData?.twentyEightExtremeDeliveries?.actual?.amount,
                                                                                rangeOverViewCeoOerData?.twentyEightExtremeDeliveries?.actual?.percentage,
                                                                                rangeOverViewCeoOerData?.twentyEightExtremeDeliveries?.actual?.value)

            val ceoRanger28DaySingle = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                               rangeOverViewCeoOerData?.twentyEightSingles?.actual?.amount,
                                                                               rangeOverViewCeoOerData?.twentyEightSingles?.actual?.percentage,
                                                                               rangeOverViewCeoOerData?.twentyEightSingles?.actual?.value)

            val ceoRanger28DayLoad = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                             rangeOverViewCeoOerData?.twentyEightLoad?.actual?.amount,
                                                                             rangeOverViewCeoOerData?.twentyEightLoad?.actual?.percentage,
                                                                             rangeOverViewCeoOerData?.twentyEightLoad?.actual?.value)

            if(ceoRange28DayEat.isEmpty()){
                
                oer_error_28days_eat.visibility = View.VISIBLE
                twenty_eight_days_eat_value.visibility = View.GONE
                val param28DaysEatCEORangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysEatCEORangeError.weight = 1.0f
                twenty_day_eadt_display.layoutParams = param28DaysEatCEORangeError
            }else{
                twenty_eight_days_eat_value.text = ceoRange28DayEat
            }
            if(ceoRanger28DayExtreme.isEmpty()){
                
                oer_error_28days_extreme.visibility = View.VISIBLE
                twenty_eight_extreme_delivery_value.visibility = View.GONE
                val param28DaysExtremeCEORangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysExtremeCEORangeError.weight = 1.0f
                twenty_day_extreme_adt_display.layoutParams = param28DaysExtremeCEORangeError
            }else{
                twenty_eight_extreme_delivery_value.text = ceoRanger28DayExtreme
            }
            if(ceoRanger28DaySingle.isEmpty()){
                
                oer_error_28days_single.visibility = View.VISIBLE
                twenty_eight_single_value.visibility = View.GONE
                val param28DaysSingleCEORangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysSingleCEORangeError.weight = 1.0f
                twenty_eight_singles_display.layoutParams = param28DaysSingleCEORangeError
            }else{
                twenty_eight_single_value.text = ceoRanger28DaySingle
            }
            if(ceoRanger28DayLoad.isEmpty()){
                
                oer_error_28days_load.visibility = View.VISIBLE
                twenty_eight_days_load_value.visibility = View.GONE
                val param28DaysLoadCEORangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysLoadCEORangeError.weight = 1.0f
                twenty_day_load_display.layoutParams = param28DaysLoadCEORangeError
            }else{
                twenty_eight_days_load_value.text = ceoRanger28DayLoad
            }

            if (rangeOverViewCeoOerData?.twentyEightDayeADT?.status != null) {
                when {
                    rangeOverViewCeoOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCeoOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (rangeOverViewCeoOerData?.twentyEightExtremeDeliveries?.status != null) {

                when {
                    rangeOverViewCeoOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCeoOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            if (rangeOverViewCeoOerData?.twentyEightSingles?.status != null) {

                when {
                    rangeOverViewCeoOerData.twentyEightSingles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCeoOerData.twentyEightSingles.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            //

            if (rangeOverViewCeoOerData?.twentyEightLoad?.status != null) {

                when {
                    rangeOverViewCeoOerData.twentyEightLoad.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCeoOerData.twentyEightLoad.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // if (rangeOverViewCeoOerData?.lastOERScore != null) {
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCeoOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            last_oer_score_display.text =
                rangeOverViewCeoOerData?.lastOERScore?.total?.displayName ?: getString(R.string.last_oer_score)

            last_food_score.text = rangeOverViewCeoOerData?.lastOERScore?.lastFoodSafetyScore?.displayName
                ?: getString(R.string.last_food_score)
            last_image_score.text = rangeOverViewCeoOerData?.lastOERScore?.lastImageScore?.displayName
                ?: getString(R.string.last_image_score)
            last_product_score.text = rangeOverViewCeoOerData?.lastOERScore?.lastProductScore?.displayName
                ?: getString(R.string.last_product_score)
            last_eADT_score.text =
                rangeOverViewCeoOerData?.lastOERScore?.lasteADTScore?.displayName
                    ?: getString(R.string.last_eADT_score)
            last_extreme_delivery_score.text =
                rangeOverViewCeoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName
                    ?: getString(R.string.last_extreme_delivery_score)
            last_single_score.text = rangeOverViewCeoOerData?.lastOERScore?.lastSinglesScore?.displayName
                ?: getString(R.string.last_single_score)
            last_load_score.text =
                rangeOverViewCeoOerData?.lastOERScore?.lastLoadScore?.displayName
                    ?: getString(R.string.last_load_score)

            val ceoRangeOERLoadScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                               rangeOverViewCeoOerData?.lastOERScore?.total?.actual?.amount,
                                                                               rangeOverViewCeoOerData?.lastOERScore?.total?.actual?.percentage,
                                                                               rangeOverViewCeoOerData?.lastOERScore?.total?.actual?.value)

            val ceoRangeOERLoadFoodScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                   rangeOverViewCeoOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.amount,
                                                                                   rangeOverViewCeoOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.percentage,
                                                                                   rangeOverViewCeoOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.value)

            val ceoRangeOERLoadImageScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                    rangeOverViewCeoOerData?.lastOERScore?.lastImageScore?.actual?.amount,
                                                                                    rangeOverViewCeoOerData?.lastOERScore?.lastImageScore?.actual?.percentage,
                                                                                    rangeOverViewCeoOerData?.lastOERScore?.lastImageScore?.actual?.value)

            val ceoRangeOERLoadProductScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                      rangeOverViewCeoOerData?.lastOERScore?.lastProductScore?.actual?.amount,
                                                                                      rangeOverViewCeoOerData?.lastOERScore?.lastProductScore?.actual?.percentage,
                                                                                      rangeOverViewCeoOerData?.lastOERScore?.lastProductScore?.actual?.value)

            val ceoRangeOERLoadEatScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                   rangeOverViewCeoOerData?.lastOERScore?.lasteADTScore?.actual?.amount,
                                                                                   rangeOverViewCeoOerData?.lastOERScore?.lasteADTScore?.actual?.percentage,
                                                                                   rangeOverViewCeoOerData?.lastOERScore?.lasteADTScore?.actual?.value)

            val ceoRangeOERLoadExtremeScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                      rangeOverViewCeoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.amount,
                                                                                      rangeOverViewCeoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.percentage,
                                                                                      rangeOverViewCeoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.value)

            val ceoRangeOERLoadSingleScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                     rangeOverViewCeoOerData?.lastOERScore?.lastSinglesScore?.actual?.amount,
                                                                                     rangeOverViewCeoOerData?.lastOERScore?.lastSinglesScore?.actual?.percentage,
                                                                                     rangeOverViewCeoOerData?.lastOERScore?.lastSinglesScore?.actual?.value)


            val ceoRangeOERLoadLastScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                    rangeOverViewCeoOerData?.lastOERScore?.lastLoadScore?.actual?.amount,
                                                                                    rangeOverViewCeoOerData?.lastOERScore?.lastLoadScore?.actual?.percentage,
                                                                                    rangeOverViewCeoOerData?.lastOERScore?.lastLoadScore?.actual?.value)


            if(ceoRangeOERLoadScore.isEmpty()){
                
                oer_error_last_score.visibility = View.VISIBLE
                last_oer_score_value.visibility = View.GONE
            }else{
                last_oer_score_value.text = ceoRangeOERLoadScore
            }
            if(ceoRangeOERLoadFoodScore.isEmpty()){
                
                oer_error_last_score_food.visibility = View.VISIBLE
                last_food_score_value.visibility = View.GONE
            }else{
                last_food_score_value.text = ceoRangeOERLoadFoodScore
            }
            if(ceoRangeOERLoadImageScore.isEmpty()){
                
                oer_error_last_score_image.visibility = View.VISIBLE
                last_image_score_value.visibility = View.GONE
            }else{
                last_image_score_value.text = ceoRangeOERLoadImageScore
            }
            if(ceoRangeOERLoadProductScore.isEmpty()){
                
                oer_error_last_score_product.visibility = View.VISIBLE
                last_product_score_value.visibility = View.GONE
            }else{
                last_product_score_value.text = ceoRangeOERLoadProductScore
            }

            if(ceoRangeOERLoadEatScore.isEmpty()){
                
                oer_error_last_score_eat.visibility = View.VISIBLE
                last_eADT_score_value.visibility = View.GONE
            }else{
                last_eADT_score_value.text = ceoRangeOERLoadEatScore
            }
            if(ceoRangeOERLoadExtremeScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_extreme_delivery_score_value.visibility = View.GONE
            }else{
                last_extreme_delivery_score_value.text = ceoRangeOERLoadExtremeScore
            }
            if(ceoRangeOERLoadSingleScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_single_score_value.visibility = View.GONE
            }else{
                last_single_score_value.text = ceoRangeOERLoadSingleScore
            }
            if(ceoRangeOERLoadLastScore.isEmpty()){
                
                oer_error_last_score_load.visibility = View.VISIBLE
                last_load_score_values.visibility = View.GONE
            }else{
                last_load_score_values.text = ceoRangeOERLoadLastScore
            }

            if (rangeOverViewCeoOerData?.lastOERScore?.total?.status != null) {

                when {
                    rangeOverViewCeoOerData.lastOERScore.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCeoOerData.lastOERScore.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // last data
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"OER Overview Period Range KPI")
        }

    }

    // do overview
    private fun todayViewDoOer(oerTodayDetail: DOOverviewTodayQuery.Do_) {
        val todayViewDoOerData = oerTodayDetail.kpis?.supervisors?.stores?.today?.oerStart

        Logger.info("OER Today", "OER Overview KPI")
        // display name
        oer_start_text.text = todayViewDoOerData!!.displayName ?: getString(R.string.oer_start_text)
        last_food_score.text = todayViewDoOerData.lastOERScore!!.lastFoodSafetyScore!!.displayName
            ?: getString(R.string.last_food_score)
        last_image_score.text = todayViewDoOerData.lastOERScore.lastImageScore!!.displayName
            ?: getString(R.string.last_image_score)
        last_product_score.text = todayViewDoOerData.lastOERScore.lastProductScore!!.displayName
            ?: getString(R.string.last_product_score)
        last_eADT_score.text =
            todayViewDoOerData.lastOERScore.lasteADTScore!!.displayName ?: getString(R.string.last_eADT_score)
        last_extreme_delivery_score.text =
            todayViewDoOerData.lastOERScore.lastExtremeDeliveriesScore!!.displayName
                ?: getString(R.string.last_extreme_delivery_score)
        last_single_score.text = todayViewDoOerData.lastOERScore.lastSinglesScore!!.displayName
            ?: getString(R.string.last_single_score)
        last_load_score.text =
            todayViewDoOerData.lastOERScore.lastLoadScore!!.displayName ?: getString(R.string.last_load_score)

        twenty_day_eadt_display.text =
            todayViewDoOerData.twentyEightDayeADT?.displayName ?: getString(R.string.twenty_eight_days_eat)
        twenty_day_extreme_adt_display.text = todayViewDoOerData.twentyEightExtremeDeliveries?.displayName
            ?: getString(R.string.twenty_eight_extreme_delivery)
        twenty_eight_singles_display.text =
            todayViewDoOerData.twentyEightSingles?.displayName ?: getString(R.string.twenty_eight_single)
        twenty_day_load_display.text =
            todayViewDoOerData.twentyEightLoad?.displayName ?: getString(R.string.twenty_eight_days_load)
        last_oer_score_display.text =
            todayViewDoOerData.lastOERScore.total?.displayName ?: getString(R.string.last_oer_score)


        if (todayViewDoOerData.actual?.value?.isNaN() == false && todayViewDoOerData.status != null) {
            oer_sales.text = Validation().ignoreZeroAfterDecimal(todayViewDoOerData.actual.value)
            when {
                todayViewDoOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oer_sales.setTextColor(getColor(R.color.red))
                }
                todayViewDoOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                    oer_sales.setTextColor(getColor(R.color.green))
                } else -> {
                    oer_sales.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        // scroll detect
        scroll_oer_parent.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 0f
                override fun onScrollChanged() {
                    if (scroll_oer_parent.scrollY > y) {
                        parent_data_on_scroll_linear.visibility = View.VISIBLE
                        parent_data_on_scroll_view.visibility = View.VISIBLE
                        level_two_scroll_data_action.text =
                            todayViewDoOerData.displayName ?: getString(R.string.oer_start_text)

                        level_two_scroll_data_action_value.text = todayViewDoOerData.actual?.value.toString()
                        if (todayViewDoOerData.actual?.value?.isNaN() == false && todayViewDoOerData.status != null) {
                            level_two_scroll_data_action_value.text =
                                Validation().ignoreZeroAfterDecimal(todayViewDoOerData.actual.value)
                            when {
                                todayViewDoOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.red_circle,
                                        0
                                    )
                                }
                                todayViewDoOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.green_circle,
                                        0
                                    )
                                } else -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.text_color))
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
                    y = scroll_oer_parent.scrollY.toFloat()
                    if (y <= 0) {
                        parent_data_on_scroll_linear.visibility = View.INVISIBLE
                        parent_data_on_scroll_view.visibility = View.GONE

                    }
                }
            })
        oer_goal_value.text =
            if (todayViewDoOerData.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(todayViewDoOerData.goal.value) else ""
        oer_variance_value.text =
            if (todayViewDoOerData.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoOerData.variance.value) else ""


        // 28 days

        if (todayViewDoOerData.twentyEightDayeADT?.actual?.value?.isNaN() == false && todayViewDoOerData.twentyEightDayeADT.status != null) {
            twenty_eight_days_eat_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoOerData.twentyEightDayeADT.actual.value)
            when {
                todayViewDoOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    twenty_eight_days_eat_value.setTextColor(getColor(R.color.red))
                }
                todayViewDoOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    twenty_eight_days_eat_value.setTextColor(getColor(R.color.green))

                } else -> {
                    twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    twenty_eight_days_eat_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        if (todayViewDoOerData.twentyEightExtremeDeliveries?.actual?.value?.isNaN() == false && todayViewDoOerData.twentyEightExtremeDeliveries.status != null) {
            twenty_eight_extreme_delivery_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoOerData.twentyEightExtremeDeliveries.actual.value)
            when {
                todayViewDoOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(R.string.out_of_range) -> {
                    twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.red))
                }
                todayViewDoOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.green))

                } else -> {
                    twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (todayViewDoOerData.twentyEightSingles?.actual?.value?.isNaN() == false && todayViewDoOerData.twentyEightSingles.status != null) {
            twenty_eight_single_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoOerData.twentyEightSingles.actual.value)
            when {
                todayViewDoOerData.twentyEightSingles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    twenty_eight_single_value.setTextColor(getColor(R.color.red))
                }
                todayViewDoOerData.twentyEightSingles.status.toString() == resources.getString(R.string.under_limit) -> {
                    twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    twenty_eight_single_value.setTextColor(getColor(R.color.green))

                } else -> {
                    twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    twenty_eight_single_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        if (todayViewDoOerData.twentyEightLoad?.actual?.value?.isNaN() == false && todayViewDoOerData.twentyEightLoad.status != null) {
            twenty_eight_days_load_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoOerData.twentyEightLoad.actual.value)
            when {
                todayViewDoOerData.twentyEightLoad.status.toString() == resources.getString(R.string.out_of_range) -> {
                    twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    twenty_eight_days_load_value.setTextColor(getColor(R.color.red))
                }
                todayViewDoOerData.twentyEightLoad.status.toString() == resources.getString(R.string.under_limit) -> {
                    twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    twenty_eight_days_load_value.setTextColor(getColor(R.color.green))

                } else -> {
                    twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    twenty_eight_days_load_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        if (todayViewDoOerData.lastOERScore.total?.actual?.value?.isNaN() == false && todayViewDoOerData.lastOERScore.total.status != null) {
            last_oer_score_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoOerData.lastOERScore.total.actual.value)
            when {
                todayViewDoOerData.lastOERScore.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    last_oer_score_value.setTextColor(getColor(R.color.red))
                }
                todayViewDoOerData.lastOERScore.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    last_oer_score_value.setTextColor(getColor(R.color.green))

                } else -> {
                    last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    last_oer_score_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        // last data
        last_food_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewDoOerData.lastOERScore.lastFoodSafetyScore?.actual?.value)
        last_image_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewDoOerData.lastOERScore.lastImageScore.actual?.value)
        last_product_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewDoOerData.lastOERScore.lastProductScore.actual?.value)
        last_eADT_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewDoOerData.lastOERScore.lasteADTScore.actual?.value)
        last_extreme_delivery_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewDoOerData.lastOERScore.lastExtremeDeliveriesScore.actual?.value)

        last_single_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewDoOerData.lastOERScore.lastSinglesScore.actual?.value)

        last_load_score_values.text =
            Validation().ignoreZeroAfterDecimal(todayViewDoOerData.lastOERScore.lastLoadScore.actual?.value)


    }

    private fun yesterdayViewDoOer(oerTodayDetail: DOOverviewYesterdayQuery.Do_) {
        try {
            val yesterdayViewDoOerData = oerTodayDetail.kpis?.supervisors?.stores?.yesterday?.oerStart

            Logger.info("OER Yesterday", "OER Overview KPI")

            // display name
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            if(yesterdayViewDoOerData?.lastOERScore!=null){
                last_oer_parent_view.visibility = View.VISIBLE
            }else{
                last_oer_parent_view.visibility = View.GONE
            }
            if (yesterdayViewDoOerData == null || yesterdayViewDoOerData.displayName.isNullOrEmpty()) {
                ll_oer_start_text.visibility = View.GONE
            } else {
                ll_oer_start_text.visibility = View.VISIBLE
                oer_start_text.text = yesterdayViewDoOerData.displayName
            }
            if (yesterdayViewDoOerData?.twentyEightDayeADT == null || yesterdayViewDoOerData.twentyEightDayeADT.displayName.isNullOrEmpty()) {
                twenty_day_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_eadt_parent.visibility = View.VISIBLE
                twenty_day_eadt_display.text = yesterdayViewDoOerData.twentyEightDayeADT.displayName
            }
            if (yesterdayViewDoOerData?.twentyEightExtremeDeliveries == null || yesterdayViewDoOerData.twentyEightExtremeDeliveries.displayName.isNullOrEmpty()) {
                twenty_day_extreme_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_extreme_eadt_parent.visibility = View.VISIBLE
                twenty_day_extreme_adt_display.text = yesterdayViewDoOerData.twentyEightExtremeDeliveries.displayName
            }
            if (yesterdayViewDoOerData?.twentyEightSingles == null || yesterdayViewDoOerData.twentyEightSingles.displayName.isNullOrEmpty()) {
                twenty_eight_singles_parent.visibility = View.GONE
            } else {
                twenty_eight_singles_parent.visibility = View.VISIBLE
                twenty_eight_singles_display.text = yesterdayViewDoOerData.twentyEightSingles.displayName
            }
            if (yesterdayViewDoOerData?.twentyEightLoad == null || yesterdayViewDoOerData.twentyEightLoad.displayName.isNullOrEmpty()) {
                twenty_day_load_parent.visibility = View.GONE
            } else {
                twenty_day_load_parent.visibility = View.VISIBLE
                twenty_day_load_display.text = yesterdayViewDoOerData.twentyEightLoad.displayName
            }



            oer_sales.text = Validation().checkAmountPercentageValue(this,
                                                                     yesterdayViewDoOerData?.actual?.amount,
                                                                     yesterdayViewDoOerData?.actual?.percentage,
                                                                     yesterdayViewDoOerData?.actual?.value)

            if (yesterdayViewDoOerData?.status != null) {
                when {
                    yesterdayViewDoOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        oer_sales.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                        oer_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        oer_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // scroll detect
            scroll_oer_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (scroll_oer_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text =
                                yesterdayViewDoOerData?.displayName ?: getString(R.string.oer_start_text)

                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                        yesterdayViewDoOerData?.actual?.amount,
                                                                        yesterdayViewDoOerData?.actual?.percentage,
                                                                        yesterdayViewDoOerData?.actual?.value)

                            if (yesterdayViewDoOerData?.status != null) {
                                when {
                                    yesterdayViewDoOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    yesterdayViewDoOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    } else -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.text_color))
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
                        y = scroll_oer_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })
            oer_goal_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                          yesterdayViewDoOerData?.goal?.amount,
                                                                          yesterdayViewDoOerData?.goal?.percentage,
                                                                          yesterdayViewDoOerData?.goal?.value)
            oer_variance_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                              yesterdayViewDoOerData?.variance?.amount,
                                                                              yesterdayViewDoOerData?.variance?.percentage,
                                                                              yesterdayViewDoOerData?.variance?.value)



            // 28 days
            val doYesterday28DayEat = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                              yesterdayViewDoOerData?.twentyEightDayeADT?.actual?.amount,
                                                                              yesterdayViewDoOerData?.twentyEightDayeADT?.actual?.percentage,
                                                                              yesterdayViewDoOerData?.twentyEightDayeADT?.actual?.value)

            val doYesterday28DayExtreme = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                  yesterdayViewDoOerData?.twentyEightExtremeDeliveries?.actual?.amount,
                                                                                  yesterdayViewDoOerData?.twentyEightExtremeDeliveries?.actual?.percentage,
                                                                                  yesterdayViewDoOerData?.twentyEightExtremeDeliveries?.actual?.value)

            val doYesterday28DaySingle = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                 yesterdayViewDoOerData?.twentyEightSingles?.actual?.amount,
                                                                                 yesterdayViewDoOerData?.twentyEightSingles?.actual?.percentage,
                                                                                 yesterdayViewDoOerData?.twentyEightSingles?.actual?.value)

            val doYesterday28DayLoad = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                               yesterdayViewDoOerData?.twentyEightLoad?.actual?.amount,
                                                                               yesterdayViewDoOerData?.twentyEightLoad?.actual?.percentage,
                                                                               yesterdayViewDoOerData?.twentyEightLoad?.actual?.value)

            if(doYesterday28DayEat.isEmpty()){
                
                oer_error_28days_eat.visibility = View.VISIBLE
                twenty_eight_days_eat_value.visibility = View.GONE
                val param28DaysEatDOYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysEatDOYesterdayError.weight = 1.0f
                twenty_day_eadt_display.layoutParams = param28DaysEatDOYesterdayError
            }else{
                twenty_eight_days_eat_value.text = doYesterday28DayEat
            }
            if(doYesterday28DayExtreme.isEmpty()){
                
                oer_error_28days_extreme.visibility = View.VISIBLE
                twenty_eight_extreme_delivery_value.visibility = View.GONE
                val param28DaysExtremeDOYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysExtremeDOYesterdayError.weight = 1.0f
                twenty_day_extreme_adt_display.layoutParams = param28DaysExtremeDOYesterdayError
            }else{
                twenty_eight_extreme_delivery_value.text = doYesterday28DayExtreme
            }
            if(doYesterday28DaySingle.isEmpty()){
                
                oer_error_28days_single.visibility = View.VISIBLE
                twenty_eight_single_value.visibility = View.GONE
                val param28DaysSingleDOYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysSingleDOYesterdayError.weight = 1.0f
                twenty_eight_singles_display.layoutParams = param28DaysSingleDOYesterdayError
            }else{
                twenty_eight_single_value.text = doYesterday28DaySingle
            }
            if(doYesterday28DayLoad.isEmpty()){
                
                oer_error_28days_load.visibility = View.VISIBLE
                twenty_eight_days_load_value.visibility = View.GONE
                val param28DaysLoadDOYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysLoadDOYesterdayError.weight = 1.0f
                twenty_day_load_display.layoutParams = param28DaysLoadDOYesterdayError
            }else{
                twenty_eight_days_load_value.text = doYesterday28DayLoad
            }

            if (yesterdayViewDoOerData?.twentyEightDayeADT?.status != null) {
                when {
                    yesterdayViewDoOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (yesterdayViewDoOerData?.twentyEightExtremeDeliveries?.status != null) {

                when {
                    yesterdayViewDoOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (yesterdayViewDoOerData?.twentyEightSingles?.status != null) {

                when {
                    yesterdayViewDoOerData.twentyEightSingles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoOerData.twentyEightSingles.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (yesterdayViewDoOerData?.twentyEightLoad?.status != null) {

                when {
                    yesterdayViewDoOerData.twentyEightLoad.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoOerData.twentyEightLoad.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // if (yesterdayViewDoOerData?.lastOERScore != null) {
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            last_oer_score_display.text =
                yesterdayViewDoOerData?.lastOERScore?.total?.displayName ?: getString(R.string.last_oer_score)

            last_food_score.text = yesterdayViewDoOerData?.lastOERScore?.lastFoodSafetyScore?.displayName
                ?: getString(R.string.last_food_score)
            last_image_score.text = yesterdayViewDoOerData?.lastOERScore?.lastImageScore?.displayName
                ?: getString(R.string.last_image_score)
            last_product_score.text = yesterdayViewDoOerData?.lastOERScore?.lastProductScore?.displayName
                ?: getString(R.string.last_product_score)
            last_eADT_score.text =
                yesterdayViewDoOerData?.lastOERScore?.lasteADTScore?.displayName
                    ?: getString(R.string.last_eADT_score)
            last_extreme_delivery_score.text =
                yesterdayViewDoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName
                    ?: getString(R.string.last_extreme_delivery_score)
            last_single_score.text = yesterdayViewDoOerData?.lastOERScore?.lastSinglesScore?.displayName
                ?: getString(R.string.last_single_score)
            last_load_score.text =
                yesterdayViewDoOerData?.lastOERScore?.lastLoadScore?.displayName
                    ?: getString(R.string.last_load_score)

            val doYesterdayOERLoadScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                  yesterdayViewDoOerData?.lastOERScore?.total?.actual?.amount,
                                                                                  yesterdayViewDoOerData?.lastOERScore?.total?.actual?.percentage,
                                                                                  yesterdayViewDoOerData?.lastOERScore?.total?.actual?.value)

            val doYesterdayOERLoadFoodScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                      yesterdayViewDoOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.amount,
                                                                                      yesterdayViewDoOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.percentage,
                                                                                      yesterdayViewDoOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.value)

            val doYesterdayOERLoadImageScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                       yesterdayViewDoOerData?.lastOERScore?.lastImageScore?.actual?.amount,
                                                                                       yesterdayViewDoOerData?.lastOERScore?.lastImageScore?.actual?.percentage,
                                                                                       yesterdayViewDoOerData?.lastOERScore?.lastImageScore?.actual?.value)

            val doYesterdayOERLoadProductScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                         yesterdayViewDoOerData?.lastOERScore?.lastProductScore?.actual?.amount,
                                                                                         yesterdayViewDoOerData?.lastOERScore?.lastProductScore?.actual?.percentage,
                                                                                         yesterdayViewDoOerData?.lastOERScore?.lastProductScore?.actual?.value)

            val doYesterdayOERLoadEatScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                      yesterdayViewDoOerData?.lastOERScore?.lasteADTScore?.actual?.amount,
                                                                                      yesterdayViewDoOerData?.lastOERScore?.lasteADTScore?.actual?.percentage,
                                                                                      yesterdayViewDoOerData?.lastOERScore?.lasteADTScore?.actual?.value)

            val doYesterdayOERLoadExtremeScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                         yesterdayViewDoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.amount,
                                                                                         yesterdayViewDoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.percentage,
                                                                                         yesterdayViewDoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.value)

            val doYesterdayOERLoadSingleScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                        yesterdayViewDoOerData?.lastOERScore?.lastSinglesScore?.actual?.amount,
                                                                                        yesterdayViewDoOerData?.lastOERScore?.lastSinglesScore?.actual?.percentage,
                                                                                        yesterdayViewDoOerData?.lastOERScore?.lastSinglesScore?.actual?.value)


            val doYesterdayOERLoadLastScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                       yesterdayViewDoOerData?.lastOERScore?.lastLoadScore?.actual?.amount,
                                                                                       yesterdayViewDoOerData?.lastOERScore?.lastLoadScore?.actual?.percentage,
                                                                                       yesterdayViewDoOerData?.lastOERScore?.lastLoadScore?.actual?.value)


            if(doYesterdayOERLoadScore.isEmpty()){
                
                oer_error_last_score.visibility = View.VISIBLE
                last_oer_score_value.visibility = View.GONE
            }else{
                last_oer_score_value.text = doYesterdayOERLoadScore
            }
            if(doYesterdayOERLoadFoodScore.isEmpty()){
                
                oer_error_last_score_food.visibility = View.VISIBLE
                last_food_score_value.visibility = View.GONE
            }else{
                last_food_score_value.text = doYesterdayOERLoadFoodScore
            }
            if(doYesterdayOERLoadImageScore.isEmpty()){
                
                oer_error_last_score_image.visibility = View.VISIBLE
                last_image_score_value.visibility = View.GONE
            }else{
                last_image_score_value.text = doYesterdayOERLoadImageScore
            }
            if(doYesterdayOERLoadProductScore.isEmpty()){
                
                oer_error_last_score_product.visibility = View.VISIBLE
                last_product_score_value.visibility = View.GONE
            }else{
                last_product_score_value.text = doYesterdayOERLoadProductScore
            }

            if(doYesterdayOERLoadEatScore.isEmpty()){
                
                oer_error_last_score_eat.visibility = View.VISIBLE
                last_eADT_score_value.visibility = View.GONE
            }else{
                last_eADT_score_value.text = doYesterdayOERLoadEatScore
            }
            if(doYesterdayOERLoadExtremeScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_extreme_delivery_score_value.visibility = View.GONE
            }else{
                last_extreme_delivery_score_value.text = doYesterdayOERLoadExtremeScore
            }
            if(doYesterdayOERLoadSingleScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_single_score_value.visibility = View.GONE
            }else{
                last_single_score_value.text = doYesterdayOERLoadSingleScore
            }
            if(doYesterdayOERLoadLastScore.isEmpty()){
                
                oer_error_last_score_load.visibility = View.VISIBLE
                last_load_score_values.visibility = View.GONE
            }else{
                last_load_score_values.text = doYesterdayOERLoadLastScore
            }

            if (yesterdayViewDoOerData?.lastOERScore?.total?.status != null) {

                when {
                    yesterdayViewDoOerData.lastOERScore.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoOerData.lastOERScore.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // last data
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"OER Overview Yesterday KPI")
        }

    }

    private fun rangeOverViewDoOer(oerTodayDetail: DOOverviewRangeQuery.Do_) {
        try {
            val rangeOverViewDoOerData = oerTodayDetail.kpis?.supervisors?.stores?.period?.oerStart

            Logger.info("OER Period Range", "OER Overview KPI")

            // display name
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            if(rangeOverViewDoOerData?.lastOERScore!=null){
                last_oer_parent_view.visibility = View.VISIBLE
            }else{
                last_oer_parent_view.visibility = View.GONE
            }

            if (rangeOverViewDoOerData == null || rangeOverViewDoOerData.displayName.isNullOrEmpty()) {
                ll_oer_start_text.visibility = View.GONE
            } else {
                ll_oer_start_text.visibility = View.VISIBLE
                oer_start_text.text = rangeOverViewDoOerData.displayName
            }
            if (rangeOverViewDoOerData?.twentyEightDayeADT == null || rangeOverViewDoOerData.twentyEightDayeADT.displayName.isNullOrEmpty()) {
                twenty_day_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_eadt_parent.visibility = View.VISIBLE
                twenty_day_eadt_display.text = rangeOverViewDoOerData.twentyEightDayeADT.displayName
            }
            if (rangeOverViewDoOerData?.twentyEightExtremeDeliveries == null || rangeOverViewDoOerData.twentyEightExtremeDeliveries.displayName.isNullOrEmpty()) {
                twenty_day_extreme_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_extreme_eadt_parent.visibility = View.VISIBLE
                twenty_day_extreme_adt_display.text = rangeOverViewDoOerData.twentyEightExtremeDeliveries.displayName
            }
            if (rangeOverViewDoOerData?.twentyEightSingles == null || rangeOverViewDoOerData.twentyEightSingles.displayName.isNullOrEmpty()) {
                twenty_eight_singles_parent.visibility = View.GONE
            } else {
                twenty_eight_singles_parent.visibility = View.VISIBLE
                twenty_eight_singles_display.text = rangeOverViewDoOerData.twentyEightSingles.displayName
            }
            if (rangeOverViewDoOerData?.twentyEightLoad == null || rangeOverViewDoOerData.twentyEightLoad.displayName.isNullOrEmpty()) {
                twenty_day_load_parent.visibility = View.GONE
            } else {
                twenty_day_load_parent.visibility = View.VISIBLE
                twenty_day_load_display.text = rangeOverViewDoOerData.twentyEightLoad.displayName
            }


            oer_sales.text = Validation().checkAmountPercentageValue(this,
                                                                     rangeOverViewDoOerData?.actual?.amount,
                                                                     rangeOverViewDoOerData?.actual?.percentage,
                                                                     rangeOverViewDoOerData?.actual?.value)

            if (rangeOverViewDoOerData?.status != null) {
                when {
                    rangeOverViewDoOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        oer_sales.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                        oer_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        oer_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // scroll detect
            scroll_oer_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (scroll_oer_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text =
                                rangeOverViewDoOerData?.displayName ?: getString(R.string.oer_start_text)

                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                        rangeOverViewDoOerData?.actual?.amount,
                                                                        rangeOverViewDoOerData?.actual?.percentage,
                                                                        rangeOverViewDoOerData?.actual?.value)

                            if (rangeOverViewDoOerData?.status != null) {
                                when {
                                    rangeOverViewDoOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    rangeOverViewDoOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    } else -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.text_color))
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
                        y = scroll_oer_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })
            oer_goal_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                          rangeOverViewDoOerData?.goal?.amount,
                                                                          rangeOverViewDoOerData?.goal?.percentage,
                                                                          rangeOverViewDoOerData?.goal?.value)
            oer_variance_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                              rangeOverViewDoOerData?.variance?.amount,
                                                                              rangeOverViewDoOerData?.variance?.percentage,
                                                                              rangeOverViewDoOerData?.variance?.value)



            // 28 days

            val doRange28DayEat = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                          rangeOverViewDoOerData?.twentyEightDayeADT?.actual?.amount,
                                                                          rangeOverViewDoOerData?.twentyEightDayeADT?.actual?.percentage,
                                                                          rangeOverViewDoOerData?.twentyEightDayeADT?.actual?.value)

            val doRange28DayExtreme = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                              rangeOverViewDoOerData?.twentyEightExtremeDeliveries?.actual?.amount,
                                                                              rangeOverViewDoOerData?.twentyEightExtremeDeliveries?.actual?.percentage,
                                                                              rangeOverViewDoOerData?.twentyEightExtremeDeliveries?.actual?.value)

            val doRange28DaySingle = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                             rangeOverViewDoOerData?.twentyEightSingles?.actual?.amount,
                                                                             rangeOverViewDoOerData?.twentyEightSingles?.actual?.percentage,
                                                                             rangeOverViewDoOerData?.twentyEightSingles?.actual?.value)

            val doRange28DayLoad = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                           rangeOverViewDoOerData?.twentyEightLoad?.actual?.amount,
                                                                           rangeOverViewDoOerData?.twentyEightLoad?.actual?.percentage,
                                                                           rangeOverViewDoOerData?.twentyEightLoad?.actual?.value)

            if(doRange28DayEat.isEmpty()){
                
                oer_error_28days_eat.visibility = View.VISIBLE
                twenty_eight_days_eat_value.visibility = View.GONE
                val param28DaysEatDORangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysEatDORangeError.weight = 1.0f
                twenty_day_eadt_display.layoutParams = param28DaysEatDORangeError
            }else{
                twenty_eight_days_eat_value.text = doRange28DayEat
            }
            if(doRange28DayExtreme.isEmpty()){
                
                oer_error_28days_extreme.visibility = View.VISIBLE
                twenty_eight_extreme_delivery_value.visibility = View.GONE
                val param28DaysExtremeDORangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysExtremeDORangeError.weight = 1.0f
                twenty_day_extreme_adt_display.layoutParams = param28DaysExtremeDORangeError
            }else{
                twenty_eight_extreme_delivery_value.text = doRange28DayExtreme
            }
            if(doRange28DaySingle.isEmpty()){
                
                oer_error_28days_single.visibility = View.VISIBLE
                twenty_eight_single_value.visibility = View.GONE
                val param28DaysSingleDORangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysSingleDORangeError.weight = 1.0f
                twenty_eight_singles_display.layoutParams = param28DaysSingleDORangeError
            }else{
                twenty_eight_single_value.text = doRange28DaySingle
            }
            if(doRange28DayLoad.isEmpty()){
                
                oer_error_28days_load.visibility = View.VISIBLE
                twenty_eight_days_load_value.visibility = View.GONE
                val param28DaysLoadDORangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysLoadDORangeError.weight = 1.0f
                twenty_day_load_display.layoutParams = param28DaysLoadDORangeError
            }else{
                twenty_eight_days_load_value.text = doRange28DayLoad
            }

            if (rangeOverViewDoOerData?.twentyEightDayeADT?.status != null) {
                when {
                    rangeOverViewDoOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (rangeOverViewDoOerData?.twentyEightExtremeDeliveries?.status != null) {

                when {
                    rangeOverViewDoOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (rangeOverViewDoOerData?.twentyEightSingles?.status != null) {

                when {
                    rangeOverViewDoOerData.twentyEightSingles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoOerData.twentyEightSingles.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (rangeOverViewDoOerData?.twentyEightLoad?.status != null) {

                when {
                    rangeOverViewDoOerData.twentyEightLoad.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoOerData.twentyEightLoad.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // if (rangeOverViewDoOerData?.lastOERScore != null) {
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            last_oer_score_display.text =
                rangeOverViewDoOerData?.lastOERScore?.total?.displayName ?: getString(R.string.last_oer_score)

            last_food_score.text = rangeOverViewDoOerData?.lastOERScore?.lastFoodSafetyScore?.displayName
                ?: getString(R.string.last_food_score)
            last_image_score.text = rangeOverViewDoOerData?.lastOERScore?.lastImageScore?.displayName
                ?: getString(R.string.last_image_score)
            last_product_score.text = rangeOverViewDoOerData?.lastOERScore?.lastProductScore?.displayName
                ?: getString(R.string.last_product_score)
            last_eADT_score.text =
                rangeOverViewDoOerData?.lastOERScore?.lasteADTScore?.displayName
                    ?: getString(R.string.last_eADT_score)
            last_extreme_delivery_score.text =
                rangeOverViewDoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName
                    ?: getString(R.string.last_extreme_delivery_score)
            last_single_score.text = rangeOverViewDoOerData?.lastOERScore?.lastSinglesScore?.displayName
                ?: getString(R.string.last_single_score)
            last_load_score.text =
                rangeOverViewDoOerData?.lastOERScore?.lastLoadScore?.displayName
                    ?: getString(R.string.last_load_score)

            val doRangeOERLoadScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                              rangeOverViewDoOerData?.lastOERScore?.total?.actual?.amount,
                                                                              rangeOverViewDoOerData?.lastOERScore?.total?.actual?.percentage,
                                                                              rangeOverViewDoOerData?.lastOERScore?.total?.actual?.value)

            val doRangeOERLoadFoodScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                  rangeOverViewDoOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.amount,
                                                                                  rangeOverViewDoOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.percentage,
                                                                                  rangeOverViewDoOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.value)

            val doRangeOERLoadImageScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                   rangeOverViewDoOerData?.lastOERScore?.lastImageScore?.actual?.amount,
                                                                                   rangeOverViewDoOerData?.lastOERScore?.lastImageScore?.actual?.percentage,
                                                                                   rangeOverViewDoOerData?.lastOERScore?.lastImageScore?.actual?.value)

            val doRangeOERLoadProductScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                     rangeOverViewDoOerData?.lastOERScore?.lastProductScore?.actual?.amount,
                                                                                     rangeOverViewDoOerData?.lastOERScore?.lastProductScore?.actual?.percentage,
                                                                                     rangeOverViewDoOerData?.lastOERScore?.lastProductScore?.actual?.value)

            val doRangeOERLoadEatScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                  rangeOverViewDoOerData?.lastOERScore?.lasteADTScore?.actual?.amount,
                                                                                  rangeOverViewDoOerData?.lastOERScore?.lasteADTScore?.actual?.percentage,
                                                                                  rangeOverViewDoOerData?.lastOERScore?.lasteADTScore?.actual?.value)

            val doRangeOERLoadExtremeScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                     rangeOverViewDoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.amount,
                                                                                     rangeOverViewDoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.percentage,
                                                                                     rangeOverViewDoOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.value)

            val doRangeOERLoadSingleScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                    rangeOverViewDoOerData?.lastOERScore?.lastSinglesScore?.actual?.amount,
                                                                                    rangeOverViewDoOerData?.lastOERScore?.lastSinglesScore?.actual?.percentage,
                                                                                    rangeOverViewDoOerData?.lastOERScore?.lastSinglesScore?.actual?.value)


            val doRangeOERLoadLastScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                   rangeOverViewDoOerData?.lastOERScore?.lastLoadScore?.actual?.amount,
                                                                                   rangeOverViewDoOerData?.lastOERScore?.lastLoadScore?.actual?.percentage,
                                                                                   rangeOverViewDoOerData?.lastOERScore?.lastLoadScore?.actual?.value)


            if(doRangeOERLoadScore.isEmpty()){
                
                oer_error_last_score.visibility = View.VISIBLE
                last_oer_score_value.visibility = View.GONE
            }else{
                last_oer_score_value.text = doRangeOERLoadScore
            }
            if(doRangeOERLoadFoodScore.isEmpty()){
                
                oer_error_last_score_food.visibility = View.VISIBLE
                last_food_score_value.visibility = View.GONE
            }else{
                last_food_score_value.text = doRangeOERLoadFoodScore
            }
            if(doRangeOERLoadImageScore.isEmpty()){
                
                oer_error_last_score_image.visibility = View.VISIBLE
                last_image_score_value.visibility = View.GONE
            }else{
                last_image_score_value.text = doRangeOERLoadImageScore
            }
            if(doRangeOERLoadProductScore.isEmpty()){
                
                oer_error_last_score_product.visibility = View.VISIBLE
                last_product_score_value.visibility = View.GONE
            }else{
                last_product_score_value.text = doRangeOERLoadProductScore
            }

            if(doRangeOERLoadEatScore.isEmpty()){
                
                oer_error_last_score_eat.visibility = View.VISIBLE
                last_eADT_score_value.visibility = View.GONE
            }else{
                last_eADT_score_value.text = doRangeOERLoadEatScore
            }
            if(doRangeOERLoadExtremeScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_extreme_delivery_score_value.visibility = View.GONE
            }else{
                last_extreme_delivery_score_value.text = doRangeOERLoadExtremeScore
            }
            if(doRangeOERLoadSingleScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_single_score_value.visibility = View.GONE
            }else{
                last_single_score_value.text = doRangeOERLoadSingleScore
            }
            if(doRangeOERLoadLastScore.isEmpty()){
                
                oer_error_last_score_load.visibility = View.VISIBLE
                last_load_score_values.visibility = View.GONE
            }else{
                last_load_score_values.text = doRangeOERLoadLastScore
            }



            if (rangeOverViewDoOerData?.lastOERScore?.total?.status != null) {

                when {
                    rangeOverViewDoOerData.lastOERScore.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoOerData.lastOERScore.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // last data
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"OER Overview Period Range KPI")
        }

    }

    // supervisor
    private fun todayViewSupervisorOer(oerTodayDetail: SupervisorOverviewTodayQuery.Supervisor) {
        val todayViewSupervisorOerData = oerTodayDetail.kpis?.stores?.today?.oerStart

        Logger.info("OER Today", "OER Overview KPI")

        // display name
        oer_start_text.text = todayViewSupervisorOerData!!.displayName ?: getString(R.string.oer_start_text)
        last_food_score.text = todayViewSupervisorOerData.lastOERScore!!.lastFoodSafetyScore!!.displayName
            ?: getString(R.string.last_food_score)
        last_image_score.text = todayViewSupervisorOerData.lastOERScore.lastImageScore!!.displayName
            ?: getString(R.string.last_image_score)
        last_product_score.text = todayViewSupervisorOerData.lastOERScore.lastProductScore!!.displayName
            ?: getString(R.string.last_product_score)
        last_eADT_score.text =
            todayViewSupervisorOerData.lastOERScore.lasteADTScore!!.displayName ?: getString(R.string.last_eADT_score)
        last_extreme_delivery_score.text =
            todayViewSupervisorOerData.lastOERScore.lastExtremeDeliveriesScore!!.displayName
                ?: getString(R.string.last_extreme_delivery_score)
        last_single_score.text = todayViewSupervisorOerData.lastOERScore.lastSinglesScore!!.displayName
            ?: getString(R.string.last_single_score)
        last_load_score.text =
            todayViewSupervisorOerData.lastOERScore.lastLoadScore!!.displayName ?: getString(R.string.last_load_score)

        twenty_day_eadt_display.text =
            todayViewSupervisorOerData.twentyEightDayeADT?.displayName ?: getString(R.string.twenty_eight_days_eat)
        twenty_day_extreme_adt_display.text = todayViewSupervisorOerData.twentyEightExtremeDeliveries?.displayName
            ?: getString(R.string.twenty_eight_extreme_delivery)
        twenty_eight_singles_display.text =
            todayViewSupervisorOerData.twentyEightSingles?.displayName ?: getString(R.string.twenty_eight_single)
        twenty_day_load_display.text =
            todayViewSupervisorOerData.twentyEightLoad?.displayName ?: getString(R.string.twenty_eight_days_load)
        last_oer_score_display.text =
            todayViewSupervisorOerData.lastOERScore.total?.displayName ?: getString(R.string.last_oer_score)


        if (todayViewSupervisorOerData.actual?.value?.isNaN() == false && todayViewSupervisorOerData.status != null) {
            oer_sales.text = Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.actual.value)
            when {
                todayViewSupervisorOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oer_sales.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                    oer_sales.setTextColor(getColor(R.color.green))
                } else -> {
                    oer_sales.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        // scroll detect
        scroll_oer_parent.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 0f
                override fun onScrollChanged() {
                    if (scroll_oer_parent.scrollY > y) {
                        parent_data_on_scroll_linear.visibility = View.VISIBLE
                        parent_data_on_scroll_view.visibility = View.VISIBLE
                        level_two_scroll_data_action.text =
                            todayViewSupervisorOerData.displayName ?: getString(R.string.oer_start_text)

                        level_two_scroll_data_action_value.text = todayViewSupervisorOerData.actual?.value.toString()
                        if (todayViewSupervisorOerData.actual?.value?.isNaN() == false && todayViewSupervisorOerData.status != null) {
                            level_two_scroll_data_action_value.text =
                                Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.actual.value)
                            when {
                                todayViewSupervisorOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.red_circle,
                                        0
                                    )
                                }
                                todayViewSupervisorOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.green_circle,
                                        0
                                    )
                                } else -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.text_color))
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
                    y = scroll_oer_parent.scrollY.toFloat()
                    if (y <= 0) {
                        parent_data_on_scroll_linear.visibility = View.INVISIBLE
                        parent_data_on_scroll_view.visibility = View.GONE

                    }
                }
            })
        oer_goal_value.text =
            if (todayViewSupervisorOerData.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.goal.value) else ""
        oer_variance_value.text =
            if (todayViewSupervisorOerData.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorOerData.variance.value) else ""


        // 28 days

        if (todayViewSupervisorOerData.twentyEightDayeADT?.actual?.value?.isNaN() == false && todayViewSupervisorOerData.twentyEightDayeADT.status != null) {
            twenty_eight_days_eat_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.twentyEightDayeADT.actual.value)
            when {
                todayViewSupervisorOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    twenty_eight_days_eat_value.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    twenty_eight_days_eat_value.setTextColor(getColor(R.color.green))

                } else -> {
                    twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    twenty_eight_days_eat_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        if (todayViewSupervisorOerData.twentyEightExtremeDeliveries?.actual?.value?.isNaN() == false && todayViewSupervisorOerData.twentyEightExtremeDeliveries.status != null) {
            twenty_eight_extreme_delivery_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.twentyEightExtremeDeliveries.actual.value)
            when {
                todayViewSupervisorOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(R.string.out_of_range) -> {
                    twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.green))

                } else -> {
                    twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (todayViewSupervisorOerData.twentyEightSingles?.actual?.value?.isNaN() == false && todayViewSupervisorOerData.twentyEightSingles.status != null) {
            twenty_eight_single_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.twentyEightSingles.actual.value)
            when {
                todayViewSupervisorOerData.twentyEightSingles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    twenty_eight_single_value.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorOerData.twentyEightSingles.status.toString() == resources.getString(R.string.under_limit) -> {
                    twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    twenty_eight_single_value.setTextColor(getColor(R.color.green))

                } else -> {
                    twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    twenty_eight_single_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        if (todayViewSupervisorOerData.twentyEightLoad?.actual?.value?.isNaN() == false && todayViewSupervisorOerData.twentyEightLoad.status != null) {
            twenty_eight_days_load_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.twentyEightLoad.actual.value)
            when {
                todayViewSupervisorOerData.twentyEightLoad.status.toString() == resources.getString(R.string.out_of_range) -> {
                    twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    twenty_eight_days_load_value.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorOerData.twentyEightLoad.status.toString() == resources.getString(R.string.under_limit) -> {
                    twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    twenty_eight_days_load_value.setTextColor(getColor(R.color.green))

                } else -> {
                    twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    twenty_eight_days_load_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        if (todayViewSupervisorOerData.lastOERScore.total?.actual?.value?.isNaN() == false && todayViewSupervisorOerData.lastOERScore.total.status != null) {
            last_oer_score_value.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.lastOERScore.total.actual.value)
            when {
                todayViewSupervisorOerData.lastOERScore.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    last_oer_score_value.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorOerData.lastOERScore.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    last_oer_score_value.setTextColor(getColor(R.color.green))

                } else -> {
                    last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    last_oer_score_value.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        // last data
        last_food_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.lastOERScore.lastFoodSafetyScore?.actual?.value)
        last_image_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.lastOERScore.lastImageScore.actual?.value)
        last_product_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.lastOERScore.lastProductScore.actual?.value)
        last_eADT_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.lastOERScore.lasteADTScore.actual?.value)
        last_extreme_delivery_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.lastOERScore.lastExtremeDeliveriesScore.actual?.value)

        last_single_score_value.text =
            Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.lastOERScore.lastSinglesScore.actual?.value)

        last_load_score_values.text =
            Validation().ignoreZeroAfterDecimal(todayViewSupervisorOerData.lastOERScore.lastLoadScore.actual?.value)


    }

    private fun yesterdayViewSupervisorOer(oerTodayDetail: SupervisorOverviewYesterdayQuery.Supervisor) {
        try {
            val yesterdayViewSupervisorOerData = oerTodayDetail.kpis?.stores?.yesterday?.oerStart

            Logger.info("OER Yesterday", "OER Overview KPI")

            // display name
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            if(yesterdayViewSupervisorOerData?.lastOERScore!=null){
                last_oer_parent_view.visibility = View.VISIBLE
            }else{
                last_oer_parent_view.visibility = View.GONE
            }

            if (yesterdayViewSupervisorOerData == null || yesterdayViewSupervisorOerData.displayName.isNullOrEmpty()) {
                ll_oer_start_text.visibility = View.GONE
            } else {
                ll_oer_start_text.visibility = View.VISIBLE
                oer_start_text.text = yesterdayViewSupervisorOerData.displayName
            }
            if (yesterdayViewSupervisorOerData?.twentyEightDayeADT == null || yesterdayViewSupervisorOerData.twentyEightDayeADT.displayName.isNullOrEmpty()) {
                twenty_day_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_eadt_parent.visibility = View.VISIBLE
                twenty_day_eadt_display.text = yesterdayViewSupervisorOerData.twentyEightDayeADT.displayName
            }
            if (yesterdayViewSupervisorOerData?.twentyEightExtremeDeliveries == null || yesterdayViewSupervisorOerData.twentyEightExtremeDeliveries.displayName.isNullOrEmpty()) {
                twenty_day_extreme_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_extreme_eadt_parent.visibility = View.VISIBLE
                twenty_day_extreme_adt_display.text = yesterdayViewSupervisorOerData.twentyEightExtremeDeliveries.displayName
            }
            if (yesterdayViewSupervisorOerData?.twentyEightSingles == null || yesterdayViewSupervisorOerData.twentyEightSingles.displayName.isNullOrEmpty()) {
                twenty_eight_singles_parent.visibility = View.GONE
            } else {
                twenty_eight_singles_parent.visibility = View.VISIBLE
                twenty_eight_singles_display.text = yesterdayViewSupervisorOerData.twentyEightSingles.displayName
            }
            if (yesterdayViewSupervisorOerData?.twentyEightLoad == null || yesterdayViewSupervisorOerData.twentyEightLoad.displayName.isNullOrEmpty()) {
                twenty_day_load_parent.visibility = View.GONE
            } else {
                twenty_day_load_parent.visibility = View.VISIBLE
                twenty_day_load_display.text = yesterdayViewSupervisorOerData.twentyEightLoad.displayName
            }


            oer_sales.text = Validation().checkAmountPercentageValue(this,
                                                                     yesterdayViewSupervisorOerData?.actual?.amount,
                                                                     yesterdayViewSupervisorOerData?.actual?.percentage,
                                                                     yesterdayViewSupervisorOerData?.actual?.value)

            if (yesterdayViewSupervisorOerData?.status != null) {
                when {
                    yesterdayViewSupervisorOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        oer_sales.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                        oer_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        oer_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // scroll detect
            scroll_oer_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (scroll_oer_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text =
                                yesterdayViewSupervisorOerData?.displayName ?: getString(R.string.oer_start_text)

                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                        yesterdayViewSupervisorOerData?.actual?.amount,
                                                                        yesterdayViewSupervisorOerData?.actual?.percentage,
                                                                        yesterdayViewSupervisorOerData?.actual?.value)

                            if (yesterdayViewSupervisorOerData?.status != null) {
                                when {
                                    yesterdayViewSupervisorOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    yesterdayViewSupervisorOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    } else -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.text_color))
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
                        y = scroll_oer_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })
            oer_goal_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                          yesterdayViewSupervisorOerData?.goal?.amount,
                                                                          yesterdayViewSupervisorOerData?.goal?.percentage,
                                                                          yesterdayViewSupervisorOerData?.goal?.value)
            oer_variance_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                              yesterdayViewSupervisorOerData?.variance?.amount,
                                                                              yesterdayViewSupervisorOerData?.variance?.percentage,
                                                                              yesterdayViewSupervisorOerData?.variance?.value)


            // 28 days

            val supervisorYesterday28DayEat = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                      yesterdayViewSupervisorOerData?.twentyEightDayeADT?.actual?.amount,
                                                                                      yesterdayViewSupervisorOerData?.twentyEightDayeADT?.actual?.percentage,
                                                                                      yesterdayViewSupervisorOerData?.twentyEightDayeADT?.actual?.value)

            val supervisorYesterday28DayExtreme = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                          yesterdayViewSupervisorOerData?.twentyEightExtremeDeliveries?.actual?.amount,
                                                                                          yesterdayViewSupervisorOerData?.twentyEightExtremeDeliveries?.actual?.percentage,
                                                                                          yesterdayViewSupervisorOerData?.twentyEightExtremeDeliveries?.actual?.value)

            val supervisorYesterday28DaySingle = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                         yesterdayViewSupervisorOerData?.twentyEightSingles?.actual?.amount,
                                                                                         yesterdayViewSupervisorOerData?.twentyEightSingles?.actual?.percentage,
                                                                                         yesterdayViewSupervisorOerData?.twentyEightSingles?.actual?.value)

            val supervisorYesterday28DayLoad = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                       yesterdayViewSupervisorOerData?.twentyEightLoad?.actual?.amount,
                                                                                       yesterdayViewSupervisorOerData?.twentyEightLoad?.actual?.percentage,
                                                                                       yesterdayViewSupervisorOerData?.twentyEightLoad?.actual?.value)

            if(supervisorYesterday28DayEat.isEmpty()){
                
                oer_error_28days_eat.visibility = View.VISIBLE
                twenty_eight_days_eat_value.visibility = View.GONE
                val param28DaysEatSupervisorYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysEatSupervisorYesterdayError.weight = 1.0f
                twenty_day_eadt_display.layoutParams = param28DaysEatSupervisorYesterdayError
            }else{
                twenty_eight_days_eat_value.text = supervisorYesterday28DayEat
            }
            if(supervisorYesterday28DayExtreme.isEmpty()){
                
                oer_error_28days_extreme.visibility = View.VISIBLE
                twenty_eight_extreme_delivery_value.visibility = View.GONE
                val param28DaysExtremeSupervisorYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysExtremeSupervisorYesterdayError.weight = 1.0f
                twenty_day_extreme_adt_display.layoutParams = param28DaysExtremeSupervisorYesterdayError
            }else{
                twenty_eight_extreme_delivery_value.text = supervisorYesterday28DayExtreme
            }
            if(supervisorYesterday28DaySingle.isEmpty()){
                
                oer_error_28days_single.visibility = View.VISIBLE
                twenty_eight_single_value.visibility = View.GONE
                val param28DaysSingleSupervisorYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysSingleSupervisorYesterdayError.weight = 1.0f
                twenty_eight_singles_display.layoutParams = param28DaysSingleSupervisorYesterdayError
            }else{
                twenty_eight_single_value.text = supervisorYesterday28DaySingle
            }
            if(supervisorYesterday28DayLoad.isEmpty()){
                
                oer_error_28days_load.visibility = View.VISIBLE
                twenty_eight_days_load_value.visibility = View.GONE
                val param28DaysLoadSupervisorYesterdayError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysLoadSupervisorYesterdayError.weight = 1.0f
                twenty_day_load_display.layoutParams = param28DaysLoadSupervisorYesterdayError
            }else{
                twenty_eight_days_load_value.text = supervisorYesterday28DayLoad
            }
            
            if (yesterdayViewSupervisorOerData?.twentyEightDayeADT?.status != null) {
                when {
                    yesterdayViewSupervisorOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (yesterdayViewSupervisorOerData?.twentyEightExtremeDeliveries?.status != null) {

                when {
                    yesterdayViewSupervisorOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (yesterdayViewSupervisorOerData?.twentyEightSingles?.status != null) {

                when {
                    yesterdayViewSupervisorOerData.twentyEightSingles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorOerData.twentyEightSingles.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (yesterdayViewSupervisorOerData?.twentyEightLoad?.status != null) {

                when {
                    yesterdayViewSupervisorOerData.twentyEightLoad.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorOerData.twentyEightLoad.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // if (yesterdayViewSupervisorOerData?.lastOERScore != null) {
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            last_oer_score_display.text =
                yesterdayViewSupervisorOerData?.lastOERScore?.total?.displayName ?: getString(R.string.last_oer_score)

            last_food_score.text = yesterdayViewSupervisorOerData?.lastOERScore?.lastFoodSafetyScore?.displayName
                ?: getString(R.string.last_food_score)
            last_image_score.text = yesterdayViewSupervisorOerData?.lastOERScore?.lastImageScore?.displayName
                ?: getString(R.string.last_image_score)
            last_product_score.text = yesterdayViewSupervisorOerData?.lastOERScore?.lastProductScore?.displayName
                ?: getString(R.string.last_product_score)
            last_eADT_score.text =
                yesterdayViewSupervisorOerData?.lastOERScore?.lasteADTScore?.displayName
                    ?: getString(R.string.last_eADT_score)
            last_extreme_delivery_score.text =
                yesterdayViewSupervisorOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName
                    ?: getString(R.string.last_extreme_delivery_score)
            last_single_score.text = yesterdayViewSupervisorOerData?.lastOERScore?.lastSinglesScore?.displayName
                ?: getString(R.string.last_single_score)
            last_load_score.text =
                yesterdayViewSupervisorOerData?.lastOERScore?.lastLoadScore?.displayName
                    ?: getString(R.string.last_load_score)

            val supervisorYesterdayOERLoadScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                          yesterdayViewSupervisorOerData?.lastOERScore?.total?.actual?.amount,
                                                                                          yesterdayViewSupervisorOerData?.lastOERScore?.total?.actual?.percentage,
                                                                                          yesterdayViewSupervisorOerData?.lastOERScore?.total?.actual?.value)

            val supervisorYesterdayOERLoadFoodScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                              yesterdayViewSupervisorOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.amount,
                                                                                              yesterdayViewSupervisorOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.percentage,
                                                                                              yesterdayViewSupervisorOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.value)

            val supervisorYesterdayOERLoadImageScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                               yesterdayViewSupervisorOerData?.lastOERScore?.lastImageScore?.actual?.amount,
                                                                                               yesterdayViewSupervisorOerData?.lastOERScore?.lastImageScore?.actual?.percentage,
                                                                                               yesterdayViewSupervisorOerData?.lastOERScore?.lastImageScore?.actual?.value)

            val supervisorYesterdayOERLoadProductScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                                 yesterdayViewSupervisorOerData?.lastOERScore?.lastProductScore?.actual?.amount,
                                                                                                 yesterdayViewSupervisorOerData?.lastOERScore?.lastProductScore?.actual?.percentage,
                                                                                                 yesterdayViewSupervisorOerData?.lastOERScore?.lastProductScore?.actual?.value)

            val supervisorYesterdayOERLoadEatScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                              yesterdayViewSupervisorOerData?.lastOERScore?.lasteADTScore?.actual?.amount,
                                                                                              yesterdayViewSupervisorOerData?.lastOERScore?.lasteADTScore?.actual?.percentage,
                                                                                              yesterdayViewSupervisorOerData?.lastOERScore?.lasteADTScore?.actual?.value)

            val supervisorYesterdayOERLoadExtremeScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                                 yesterdayViewSupervisorOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.amount,
                                                                                                 yesterdayViewSupervisorOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.percentage,
                                                                                                 yesterdayViewSupervisorOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.value)

            val supervisorYesterdayOERLoadSingleScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                                yesterdayViewSupervisorOerData?.lastOERScore?.lastSinglesScore?.actual?.amount,
                                                                                                yesterdayViewSupervisorOerData?.lastOERScore?.lastSinglesScore?.actual?.percentage,
                                                                                                yesterdayViewSupervisorOerData?.lastOERScore?.lastSinglesScore?.actual?.value)


            val supervisorYesterdayOERLoadLastScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                               yesterdayViewSupervisorOerData?.lastOERScore?.lastLoadScore?.actual?.amount,
                                                                                               yesterdayViewSupervisorOerData?.lastOERScore?.lastLoadScore?.actual?.percentage,
                                                                                               yesterdayViewSupervisorOerData?.lastOERScore?.lastLoadScore?.actual?.value)


            if(supervisorYesterdayOERLoadScore.isEmpty()){
                
                oer_error_last_score.visibility = View.VISIBLE
                last_oer_score_value.visibility = View.GONE
            }else{
                last_oer_score_value.text = supervisorYesterdayOERLoadScore
            }
            if(supervisorYesterdayOERLoadFoodScore.isEmpty()){
                
                oer_error_last_score_food.visibility = View.VISIBLE
                last_food_score_value.visibility = View.GONE
            }else{
                last_food_score_value.text = supervisorYesterdayOERLoadFoodScore
            }
            if(supervisorYesterdayOERLoadImageScore.isEmpty()){
                
                oer_error_last_score_image.visibility = View.VISIBLE
                last_image_score_value.visibility = View.GONE
            }else{
                last_image_score_value.text = supervisorYesterdayOERLoadImageScore
            }
            if(supervisorYesterdayOERLoadProductScore.isEmpty()){
                
                oer_error_last_score_product.visibility = View.VISIBLE
                last_product_score_value.visibility = View.GONE
            }else{
                last_product_score_value.text = supervisorYesterdayOERLoadProductScore
            }

            if(supervisorYesterdayOERLoadEatScore.isEmpty()){
                
                oer_error_last_score_eat.visibility = View.VISIBLE
                last_eADT_score_value.visibility = View.GONE
            }else{
                last_eADT_score_value.text = supervisorYesterdayOERLoadEatScore
            }
            if(supervisorYesterdayOERLoadExtremeScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_extreme_delivery_score_value.visibility = View.GONE
            }else{
                last_extreme_delivery_score_value.text = supervisorYesterdayOERLoadExtremeScore
            }
            if(supervisorYesterdayOERLoadSingleScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_single_score_value.visibility = View.GONE
            }else{
                last_single_score_value.text = supervisorYesterdayOERLoadSingleScore
            }
            if(supervisorYesterdayOERLoadLastScore.isEmpty()){
                
                oer_error_last_score_load.visibility = View.VISIBLE
                last_load_score_values.visibility = View.GONE
            }else{
                last_load_score_values.text = supervisorYesterdayOERLoadLastScore
            }

            if (yesterdayViewSupervisorOerData?.lastOERScore?.total?.status != null) {

                when {
                    yesterdayViewSupervisorOerData.lastOERScore.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorOerData.lastOERScore.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // last data
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"OER Overview Yesterday KPI")
        }

    }

    private fun rangeOverViewSupervisorOer(oerTodayDetail: SupervisorOverviewRangeQuery.Supervisor) {
        try {
            val rangeOverViewSupervisorOerData = oerTodayDetail.kpis?.stores?.period?.oerStart

            Logger.info("OER Period Range", "OER Overview KPI")

            // display name
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            if(rangeOverViewSupervisorOerData?.lastOERScore!=null){
                last_oer_parent_view.visibility = View.VISIBLE
            }else{
                last_oer_parent_view.visibility = View.GONE
            }

            if (rangeOverViewSupervisorOerData == null || rangeOverViewSupervisorOerData.displayName.isNullOrEmpty()) {
                ll_oer_start_text.visibility = View.GONE
            } else {
                ll_oer_start_text.visibility = View.VISIBLE
                oer_start_text.text = rangeOverViewSupervisorOerData.displayName
            }
            if (rangeOverViewSupervisorOerData?.twentyEightDayeADT == null || rangeOverViewSupervisorOerData.twentyEightDayeADT.displayName.isNullOrEmpty()) {
                twenty_day_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_eadt_parent.visibility = View.VISIBLE
                twenty_day_eadt_display.text = rangeOverViewSupervisorOerData.twentyEightDayeADT.displayName
            }
            if (rangeOverViewSupervisorOerData?.twentyEightExtremeDeliveries == null || rangeOverViewSupervisorOerData.twentyEightExtremeDeliveries.displayName.isNullOrEmpty()) {
                twenty_day_extreme_eadt_parent.visibility = View.GONE
            } else {
                twenty_day_extreme_eadt_parent.visibility = View.VISIBLE
                twenty_day_extreme_adt_display.text = rangeOverViewSupervisorOerData.twentyEightExtremeDeliveries.displayName
            }
            if (rangeOverViewSupervisorOerData?.twentyEightSingles == null || rangeOverViewSupervisorOerData.twentyEightSingles.displayName.isNullOrEmpty()) {
                twenty_eight_singles_parent.visibility = View.GONE
            } else {
                twenty_eight_singles_parent.visibility = View.VISIBLE
                twenty_eight_singles_display.text = rangeOverViewSupervisorOerData.twentyEightSingles.displayName
            }
            if (rangeOverViewSupervisorOerData?.twentyEightLoad == null || rangeOverViewSupervisorOerData.twentyEightLoad.displayName.isNullOrEmpty()) {
                twenty_day_load_parent.visibility = View.GONE
            } else {
                twenty_day_load_parent.visibility = View.VISIBLE
                twenty_day_load_display.text = rangeOverViewSupervisorOerData.twentyEightLoad.displayName
            }


            oer_sales.text = Validation().checkAmountPercentageValue(this,
                                                                     rangeOverViewSupervisorOerData?.actual?.amount,
                                                                     rangeOverViewSupervisorOerData?.actual?.percentage,
                                                                     rangeOverViewSupervisorOerData?.actual?.value)

            if (rangeOverViewSupervisorOerData?.status != null) {
                when {
                    rangeOverViewSupervisorOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        oer_sales.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewSupervisorOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                        oer_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        oer_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // scroll detect
            scroll_oer_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (scroll_oer_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text =
                                rangeOverViewSupervisorOerData?.displayName ?: getString(R.string.oer_start_text)

                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                        rangeOverViewSupervisorOerData?.actual?.amount,
                                                                        rangeOverViewSupervisorOerData?.actual?.percentage,
                                                                        rangeOverViewSupervisorOerData?.actual?.value)

                            if (rangeOverViewSupervisorOerData?.status != null) {
                                when {
                                    rangeOverViewSupervisorOerData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    rangeOverViewSupervisorOerData.status.toString() == resources.getString(R.string.under_limit) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.green_circle,
                                            0
                                        )
                                    } else -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.text_color))
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
                        y = scroll_oer_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })
            oer_goal_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                          rangeOverViewSupervisorOerData?.goal?.amount,
                                                                          rangeOverViewSupervisorOerData?.goal?.percentage,
                                                                          rangeOverViewSupervisorOerData?.goal?.value)
            oer_variance_value.text = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                              rangeOverViewSupervisorOerData?.variance?.amount,
                                                                              rangeOverViewSupervisorOerData?.variance?.percentage,
                                                                              rangeOverViewSupervisorOerData?.variance?.value)



            // 28 days

            val supervisorRange28DayEat = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                  rangeOverViewSupervisorOerData?.twentyEightDayeADT?.actual?.amount,
                                                                                  rangeOverViewSupervisorOerData?.twentyEightDayeADT?.actual?.percentage,
                                                                                  rangeOverViewSupervisorOerData?.twentyEightDayeADT?.actual?.value)

            val supervisorRange28DayExtreme = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                      rangeOverViewSupervisorOerData?.twentyEightExtremeDeliveries?.actual?.amount,
                                                                                      rangeOverViewSupervisorOerData?.twentyEightExtremeDeliveries?.actual?.percentage,
                                                                                      rangeOverViewSupervisorOerData?.twentyEightExtremeDeliveries?.actual?.value)

            val supervisorRange28DaySingle = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                     rangeOverViewSupervisorOerData?.twentyEightSingles?.actual?.amount,
                                                                                     rangeOverViewSupervisorOerData?.twentyEightSingles?.actual?.percentage,
                                                                                     rangeOverViewSupervisorOerData?.twentyEightSingles?.actual?.value)

            val supervisorRange28DayLoad = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                   rangeOverViewSupervisorOerData?.twentyEightLoad?.actual?.amount,
                                                                                   rangeOverViewSupervisorOerData?.twentyEightLoad?.actual?.percentage,
                                                                                   rangeOverViewSupervisorOerData?.twentyEightLoad?.actual?.value)

            if(supervisorRange28DayEat.isEmpty()){
                
                oer_error_28days_eat.visibility = View.VISIBLE
                twenty_eight_days_eat_value.visibility = View.GONE
                val param28DaysEatSupervisorRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysEatSupervisorRangeError.weight = 1.0f
                twenty_day_eadt_display.layoutParams = param28DaysEatSupervisorRangeError
            }else{
                twenty_eight_days_eat_value.text = supervisorRange28DayEat
            }
            if(supervisorRange28DayExtreme.isEmpty()){
                
                oer_error_28days_extreme.visibility = View.VISIBLE
                twenty_eight_extreme_delivery_value.visibility = View.GONE
                val param28DaysExtremeSupervisorRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysExtremeSupervisorRangeError.weight = 1.0f
                twenty_day_extreme_adt_display.layoutParams = param28DaysExtremeSupervisorRangeError
            }else{
                twenty_eight_extreme_delivery_value.text = supervisorRange28DayExtreme
            }
            if(supervisorRange28DaySingle.isEmpty()){
                
                oer_error_28days_single.visibility = View.VISIBLE
                twenty_eight_single_value.visibility = View.GONE
                val param28DaysSingleSupervisorRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysSingleSupervisorRangeError.weight = 1.0f
                twenty_eight_singles_display.layoutParams = param28DaysSingleSupervisorRangeError
            }else{
                twenty_eight_single_value.text = supervisorRange28DaySingle
            }
            if(supervisorRange28DayLoad.isEmpty()){
                
                oer_error_28days_load.visibility = View.VISIBLE
                twenty_eight_days_load_value.visibility = View.GONE
                val param28DaysLoadSupervisorRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param28DaysLoadSupervisorRangeError.weight = 1.0f
                twenty_day_load_display.layoutParams = param28DaysLoadSupervisorRangeError
            }else{
                twenty_eight_days_load_value.text = supervisorRange28DayLoad
            }

            if (rangeOverViewSupervisorOerData?.twentyEightDayeADT?.status != null) {
                when {
                    rangeOverViewSupervisorOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewSupervisorOerData.twentyEightDayeADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_eat_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (rangeOverViewSupervisorOerData?.twentyEightExtremeDeliveries?.status != null) {

                when {
                    rangeOverViewSupervisorOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_eat_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewSupervisorOerData.twentyEightExtremeDeliveries.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_extreme_delivery_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_extreme_delivery_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (rangeOverViewSupervisorOerData?.twentyEightSingles?.status != null) {

                when {
                    rangeOverViewSupervisorOerData.twentyEightSingles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewSupervisorOerData.twentyEightSingles.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_single_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_single_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (rangeOverViewSupervisorOerData?.twentyEightLoad?.status != null) {

                when {
                    rangeOverViewSupervisorOerData.twentyEightLoad.status.toString() == resources.getString(R.string.out_of_range) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewSupervisorOerData.twentyEightLoad.status.toString() == resources.getString(R.string.under_limit) -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        twenty_eight_days_load_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        twenty_eight_days_load_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // if (rangeOverViewSupervisorOerData?.lastOERScore != null) {
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.twentyEightDayeADT?.displayName, twenty_day_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.twentyEightExtremeDeliveries?.displayName, twenty_day_extreme_eadt_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.twentyEightSingles?.displayName, twenty_eight_singles_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.twentyEightLoad?.displayName, twenty_day_load_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.total?.displayName, last_oer_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lastFoodSafetyScore?.displayName, last_food_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lastImageScore?.displayName, last_image_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lastProductScore?.displayName, last_product_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lasteADTScore?.displayName, last_eADT_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName, last_extreme_delivery_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lastSinglesScore?.displayName, last_single_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewSupervisorOerData?.lastOERScore?.lastLoadScore?.displayName, last_load_parent)

            last_oer_score_display.text =
                rangeOverViewSupervisorOerData?.lastOERScore?.total?.displayName ?: getString(R.string.last_oer_score)

            last_food_score.text = rangeOverViewSupervisorOerData?.lastOERScore?.lastFoodSafetyScore?.displayName
                ?: getString(R.string.last_food_score)
            last_image_score.text = rangeOverViewSupervisorOerData?.lastOERScore?.lastImageScore?.displayName
                ?: getString(R.string.last_image_score)
            last_product_score.text = rangeOverViewSupervisorOerData?.lastOERScore?.lastProductScore?.displayName
                ?: getString(R.string.last_product_score)
            last_eADT_score.text =
                rangeOverViewSupervisorOerData?.lastOERScore?.lasteADTScore?.displayName
                    ?: getString(R.string.last_eADT_score)
            last_extreme_delivery_score.text =
                rangeOverViewSupervisorOerData?.lastOERScore?.lastExtremeDeliveriesScore?.displayName
                    ?: getString(R.string.last_extreme_delivery_score)
            last_single_score.text = rangeOverViewSupervisorOerData?.lastOERScore?.lastSinglesScore?.displayName
                ?: getString(R.string.last_single_score)
            last_load_score.text =
                rangeOverViewSupervisorOerData?.lastOERScore?.lastLoadScore?.displayName
                    ?: getString(R.string.last_load_score)

            val supervisorRangeOERLoadScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                      rangeOverViewSupervisorOerData?.lastOERScore?.total?.actual?.amount,
                                                                                      rangeOverViewSupervisorOerData?.lastOERScore?.total?.actual?.percentage,
                                                                                      rangeOverViewSupervisorOerData?.lastOERScore?.total?.actual?.value)

            val supervisorRangeOERLoadFoodScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                          rangeOverViewSupervisorOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.amount,
                                                                                          rangeOverViewSupervisorOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.percentage,
                                                                                          rangeOverViewSupervisorOerData?.lastOERScore?.lastFoodSafetyScore?.actual?.value)

            val supervisorRangeOERLoadImageScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                           rangeOverViewSupervisorOerData?.lastOERScore?.lastImageScore?.actual?.amount,
                                                                                           rangeOverViewSupervisorOerData?.lastOERScore?.lastImageScore?.actual?.percentage,
                                                                                           rangeOverViewSupervisorOerData?.lastOERScore?.lastImageScore?.actual?.value)

            val supervisorRangeOERLoadProductScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                             rangeOverViewSupervisorOerData?.lastOERScore?.lastProductScore?.actual?.amount,
                                                                                             rangeOverViewSupervisorOerData?.lastOERScore?.lastProductScore?.actual?.percentage,
                                                                                             rangeOverViewSupervisorOerData?.lastOERScore?.lastProductScore?.actual?.value)

            val supervisorRangeOERLoadEatScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                          rangeOverViewSupervisorOerData?.lastOERScore?.lasteADTScore?.actual?.amount,
                                                                                          rangeOverViewSupervisorOerData?.lastOERScore?.lasteADTScore?.actual?.percentage,
                                                                                          rangeOverViewSupervisorOerData?.lastOERScore?.lasteADTScore?.actual?.value)

            val supervisorRangeOERLoadExtremeScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                             rangeOverViewSupervisorOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.amount,
                                                                                             rangeOverViewSupervisorOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.percentage,
                                                                                             rangeOverViewSupervisorOerData?.lastOERScore?.lastExtremeDeliveriesScore?.actual?.value)

            val supervisorRangeOERLoadSingleScore = Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                            rangeOverViewSupervisorOerData?.lastOERScore?.lastSinglesScore?.actual?.amount,
                                                                                            rangeOverViewSupervisorOerData?.lastOERScore?.lastSinglesScore?.actual?.percentage,
                                                                                            rangeOverViewSupervisorOerData?.lastOERScore?.lastSinglesScore?.actual?.value)


            val supervisorRangeOERLoadLastScore =  Validation().checkAmountPercentageValue(this@OERStartActivity,
                                                                                           rangeOverViewSupervisorOerData?.lastOERScore?.lastLoadScore?.actual?.amount,
                                                                                           rangeOverViewSupervisorOerData?.lastOERScore?.lastLoadScore?.actual?.percentage,
                                                                                           rangeOverViewSupervisorOerData?.lastOERScore?.lastLoadScore?.actual?.value)


            if(supervisorRangeOERLoadScore.isEmpty()){
                
                oer_error_last_score.visibility = View.VISIBLE
                last_oer_score_value.visibility = View.GONE
            }else{
                last_oer_score_value.text = supervisorRangeOERLoadScore
            }
            if(supervisorRangeOERLoadFoodScore.isEmpty()){
                
                oer_error_last_score_food.visibility = View.VISIBLE
                last_food_score_value.visibility = View.GONE
            }else{
                last_food_score_value.text = supervisorRangeOERLoadFoodScore
            }
            if(supervisorRangeOERLoadImageScore.isEmpty()){
                
                oer_error_last_score_image.visibility = View.VISIBLE
                last_image_score_value.visibility = View.GONE
            }else{
                last_image_score_value.text = supervisorRangeOERLoadImageScore
            }
            if(supervisorRangeOERLoadProductScore.isEmpty()){
                
                oer_error_last_score_product.visibility = View.VISIBLE
                last_product_score_value.visibility = View.GONE
            }else{
                last_product_score_value.text = supervisorRangeOERLoadProductScore
            }

            if(supervisorRangeOERLoadEatScore.isEmpty()){
                
                oer_error_last_score_eat.visibility = View.VISIBLE
                last_eADT_score_value.visibility = View.GONE
            }else{
                last_eADT_score_value.text = supervisorRangeOERLoadEatScore
            }
            if(supervisorRangeOERLoadExtremeScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_extreme_delivery_score_value.visibility = View.GONE
            }else{
                last_extreme_delivery_score_value.text = supervisorRangeOERLoadExtremeScore
            }
            if(supervisorRangeOERLoadSingleScore.isEmpty()){
                
                oer_error_last_score_extreme.visibility = View.VISIBLE
                last_single_score_value.visibility = View.GONE
            }else{
                last_single_score_value.text = supervisorRangeOERLoadSingleScore
            }
            if(supervisorRangeOERLoadLastScore.isEmpty()){
                
                oer_error_last_score_load.visibility = View.VISIBLE
                last_load_score_values.visibility = View.GONE
            }else{
                last_load_score_values.text = supervisorRangeOERLoadLastScore
            }

            if (rangeOverViewSupervisorOerData?.lastOERScore?.total?.status != null) {

                when {
                    rangeOverViewSupervisorOerData.lastOERScore.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewSupervisorOerData.lastOERScore.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.green))

                    } else -> {
                        last_oer_score_value.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        last_oer_score_value.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // last data
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"OER Overview Period Range KPI")
        }

    }

    override fun onBackPressed() {
        Logger.info("Back-pressed", "OER Overview KPI")
        finish()
    }

    private fun callOEROverviewNullApi(){
        val formattedStartDateValueOER: String
        val formattedEndDateValueOER: String

        val startDateValueOER = StorePrefData.startDateValue
        val endDateValueOER = StorePrefData.endDateValue

        if (StorePrefData.isCalendarSelected) {
            formattedStartDateValueOER = startDateValueOER
            formattedEndDateValueOER = endDateValueOER
        } else {
            formattedStartDateValueOER = startDateValueOER
            formattedEndDateValueOER = endDateValueOER
        }
        val progressDialogOEROverview = CustomProgressDialog(this@OERStartActivity)
        progressDialogOEROverview.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodeListOER = dbHelperOEROverview.getAllSelectedAreaList(true)
            val stateCodeListOER = dbHelperOEROverview.getAllSelectedStoreListState(true)
            val supervisorNumberListOER = dbHelperOEROverview.getAllSelectedStoreListSupervisor(true)
            val storeNumberListOER = dbHelperOEROverview.getAllSelectedStoreList(true)

            val responseMissingDataOER = try {
                apolloClient(this@OERStartActivity).query(
                    MissingDataQuery(
                            areaCodeListOER.toInput(),
                            stateCodeListOER.toInput(),
                            supervisorNumberListOER.toInput(),
                            storeNumberListOER.toInput(),
                            EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType).toInput(),
                            formattedStartDateValueOER.toInput(),
                            formattedEndDateValueOER.toInput(),
                    )
                ).await()

            } catch (e: ApolloException) {
                Logger.error("Missing Data ${e.message.toString()}","OER Overview KPI")
                progressDialogOEROverview.dismissProgressDialog()
                return@launchWhenResumed
            }
            if(responseMissingDataOER.data?.missingData!=null){
                progressDialogOEROverview.dismissProgressDialog()
                oer_kpi_error_layout.visibility = View.VISIBLE
                oer_kpi_error_layout.header_data_title.text  = responseMissingDataOER.data?.missingData!!.header
                oer_kpi_error_layout.header_data_description.text  = responseMissingDataOER.data?.missingData!!.message
            }
            else{
                progressDialogOEROverview.dismissProgressDialog()
                oer_kpi_error_layout.visibility = View.GONE
            }
        }
    }
}
