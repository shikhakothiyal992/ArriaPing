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
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.kpi.*
import kotlinx.android.synthetic.main.activity_a_w_u_s_kpi.*
import kotlinx.android.synthetic.main.activity_foodkpi.*
import kotlinx.android.synthetic.main.activity_o_e_r_start.*
import kotlinx.android.synthetic.main.activity_service_kpi.*
import kotlinx.android.synthetic.main.activity_service_kpi.delivery_order_count
import kotlinx.android.synthetic.main.activity_service_kpi.level_two_scroll_data_action
import kotlinx.android.synthetic.main.activity_service_kpi.level_two_scroll_data_action_value
import kotlinx.android.synthetic.main.activity_service_kpi.parent_data_on_scroll_linear
import kotlinx.android.synthetic.main.activity_service_kpi.parent_data_on_scroll_view
import kotlinx.android.synthetic.main.data_error_layout.view.*
import com.arria.ping.log.Logger
import com.arria.ping.util.NetworkHelper
import com.arria.ping.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.arria.ping.kpi.gm.StorePeriodRangeKPIQuery
import com.arria.ping.kpi.gm.StoreYesterdayKPIQuery

@AndroidEntryPoint
class ServiceKpiActivity : AppCompatActivity() {
    var apiServiceArgumentFromFilter = ""
    val gsonService = Gson()
    private lateinit var dbHelperService: DatabaseHelperImpl

    @Inject
    lateinit var networkHelper: NetworkHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_kpi)
        this.setFinishOnTouchOutside(false)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dbHelperService = DatabaseHelperImpl(DatabaseBuilder.getInstance(this))
        setServiceData()
        cross_button_service.setOnClickListener {
            Logger.info("Cancel Button clicked","Service Overview KPI Screen")
            finish()
        }
    }

    private fun setServiceData() {
        when (StorePrefData.role) {
            getString(R.string.ceo_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiServiceArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiServiceArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val serviceYesterdayDetailCEO = gsonService.fromJson(
                            intent.getStringExtra("service_data"),
                            CEOOverviewRangeQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callServicesOverviewNullApi()
                            rangeOverViewCEOService(serviceYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val serviceYesterdayDetail = gsonService.fromJson(
                            intent.getStringExtra("service_data"),
                            CEOOverviewYesterdayQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callServicesOverviewNullApi()
                            yesterdayViewCEOService(serviceYesterdayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val serviceTodayDetail = gsonService.fromJson(
                            intent.getStringExtra("service_data"),
                            CEOOverviewTodayQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callServicesOverviewNullApi()
                            todayViewCEOService(serviceTodayDetail)
                        }
                    }
                }
            }
            getString(R.string.do_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiServiceArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiServiceArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val serviceYesterdayDetailCEO = gsonService.fromJson(
                            intent.getStringExtra("service_data"),
                            DOOverviewRangeQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callServicesOverviewNullApi()
                            rangeOverViewDOService(serviceYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val serviceYesterdayDetail = gsonService.fromJson(
                            intent.getStringExtra("service_data"),
                            DOOverviewYesterdayQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callServicesOverviewNullApi()
                            yesterdayViewDOService(serviceYesterdayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val serviceTodayDetail = gsonService.fromJson(
                            intent.getStringExtra("service_data"),
                            DOOverviewTodayQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callServicesOverviewNullApi()
                            todayViewDO(serviceTodayDetail)
                        }
                    }
                }
            }
            getString(R.string.gm_text) -> {

                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiServiceArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiServiceArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val serviceGmData = gsonService.fromJson(
                            intent.getStringExtra("service_data"),
                            StorePeriodRangeKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callServicesOverviewNullApi()
                            rangeViewService(serviceGmData)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val serviceYesterdayDetail = gsonService.fromJson(
                            intent.getStringExtra("service_data"),
                            StoreYesterdayKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callServicesOverviewNullApi()
                            yesterdayViewService(serviceYesterdayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val serviceTodayDetail = gsonService.fromJson(
                            intent.getStringExtra("service_data"),
                            StoreTodayKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callServicesOverviewNullApi()
                            todayViewService(serviceTodayDetail)
                        }
                    }
                }
            }
            getString(R.string.supervisor_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiServiceArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiServiceArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val serviceYesterdayDetailCEO = gsonService.fromJson(
                            intent.getStringExtra("service_data"),
                            SupervisorOverviewRangeQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callServicesOverviewNullApi()
                            rangeViewSupervisorService(serviceYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val serviceYesterdayDetail = gsonService.fromJson(
                            intent.getStringExtra("service_data"),
                            SupervisorOverviewYesterdayQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callServicesOverviewNullApi()
                            yesterdayViewSupervisorService(serviceYesterdayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val serviceTodayDetail = gsonService.fromJson(
                            intent.getStringExtra("service_data"),
                            SupervisorOverviewTodayQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callServicesOverviewNullApi()
                            todayViewSupervisor(serviceTodayDetail)
                        }
                    }
                }
            }
        }
    }

    // gm
    private fun todayViewService(serviceTodayDetail: StoreTodayKPIQuery.GeneralManager) {

        val todayViewServiceData = serviceTodayDetail.kpis?.store?.today?.service

        Logger.info("Service Today", "Service Overview KPI")


        // display name
        service_text_small.text =
            todayViewServiceData!!.__typename
        if (todayViewServiceData.eADT?.legTime == null || todayViewServiceData.eADT.legTime.displayName.isNullOrEmpty()) {
            ll_leg_time_text.visibility = View.GONE
        } else {
            ll_leg_time_text.visibility = View.VISIBLE
            leg_time_text.text = todayViewServiceData.eADT.legTime.displayName
        }
        if (todayViewServiceData.extremeDelivery?.deliveryOrderCount == null || todayViewServiceData.extremeDelivery.deliveryOrderCount.displayName.isNullOrEmpty()) {
            ll_delivery_order_count_service.visibility = View.GONE
        } else {
            ll_delivery_order_count_service.visibility = View.VISIBLE
            delivery_order_count.text = todayViewServiceData.extremeDelivery.deliveryOrderCount.displayName
        }
        if (todayViewServiceData.extremeDelivery?.extremeDeliveryCount == null || todayViewServiceData.extremeDelivery.extremeDeliveryCount.displayName.isNullOrEmpty()) {
            ll_extreme_delivery_oc_text.visibility = View.GONE
        } else {
            ll_extreme_delivery_oc_text.visibility = View.VISIBLE
            extreme_delivery_oc_text.text = todayViewServiceData.extremeDelivery.extremeDeliveryCount.displayName
        }
        if (todayViewServiceData.singles?._doubles == null || todayViewServiceData.singles._doubles.displayName.isNullOrEmpty()) {
            ll_double_text.visibility = View.GONE
        } else {
            ll_double_text.visibility = View.VISIBLE
            double_text.text = todayViewServiceData.singles._doubles.displayName
        }
        if (todayViewServiceData.singles?.triples == null || todayViewServiceData.singles.triples.displayName.isNullOrEmpty()) {
            ll_triples_text.visibility = View.GONE
        } else {
            ll_triples_text.visibility = View.VISIBLE
            triples_text.text =  todayViewServiceData.singles.triples.displayName
        }
        if (todayViewServiceData.loadTime?.carryoutLoadTime == null || todayViewServiceData.loadTime.carryoutLoadTime.displayName.isNullOrEmpty()) {
            ll_carry_out_laod_time_text.visibility = View.GONE
        } else {
            ll_carry_out_laod_time_text.visibility = View.VISIBLE
            carry_out_laod_time_text.text = todayViewServiceData.loadTime.carryoutLoadTime.displayName
        }
        if (todayViewServiceData.aot?.hangUps == null || todayViewServiceData.aot.hangUps.displayName.isNullOrEmpty()) {
            hang_up_parent.visibility = View.GONE
        } else {
            hang_up_parent.visibility = View.VISIBLE
            hang_up_text.text =  todayViewServiceData.aot.hangUps.displayName
        }



        service_scroll_parent.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 0f
                override fun onScrollChanged() {
                    if (service_scroll_parent.scrollY > y) {
                        parent_data_on_scroll_linear.visibility = View.VISIBLE
                        parent_data_on_scroll_view.visibility = View.VISIBLE
                        level_two_scroll_data_action.text =
                            todayViewServiceData.__typename

                    }
                    y = service_scroll_parent.scrollY.toFloat()
                    if (y <= 0) {
                        parent_data_on_scroll_linear.visibility = View.INVISIBLE
                        parent_data_on_scroll_view.visibility = View.GONE

                    }
                }
            })

        showServiceNarrativeData(todayViewServiceData.narrative.toString())

        if (todayViewServiceData.eADT?.actual?.value?.isNaN() == false && todayViewServiceData.eADT.status != null) {
            eadt_actual.text = Validation().ignoreZeroAfterDecimal(todayViewServiceData.eADT.actual.value)
            when {
                todayViewServiceData.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    eadt_actual.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    eadt_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    eadt_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        eadt_goal.text =
            if (todayViewServiceData.eADT?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.eADT.goal.value) else ""
        eadt_variance.text =
            if (todayViewServiceData.eADT?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.eADT.variance.value) else ""
        // leg time
        if (todayViewServiceData.eADT?.legTime?.actual?.value?.isNaN() == false && todayViewServiceData.eADT.legTime.status != null) {
            leg_time_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewServiceData.eADT.legTime.actual.value)
            when {
                todayViewServiceData.eADT.legTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    leg_time_actual.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.eADT.legTime.status.toString() == resources.getString(R.string.under_limit) -> {
                    leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    leg_time_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    leg_time_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (todayViewServiceData.extremeDelivery?.actual?.value?.isNaN() == false && todayViewServiceData.extremeDelivery.status != null) {
            extreme_delivery_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewServiceData.extremeDelivery.actual.value)

            when {
                todayViewServiceData.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    extreme_delivery_actual.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    extreme_delivery_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    extreme_delivery_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        extreme_delivery_goal.text =
            if (todayViewServiceData.extremeDelivery?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.extremeDelivery.goal.value)
            else ""
        extreme_delivery_variance.text =
            if (todayViewServiceData.extremeDelivery?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.extremeDelivery.variance.value)
            else ""

        // delivery   order count
        if (todayViewServiceData.extremeDelivery?.deliveryOrderCount?.actual?.value?.isNaN() == false && todayViewServiceData.extremeDelivery.deliveryOrderCount.status != null) {
            delivery_order_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewServiceData.extremeDelivery.deliveryOrderCount.actual.value)
            when {
                todayViewServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    delivery_order_actual.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    delivery_order_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    delivery_order_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        // extreme_delivery_oc_actual
        if (todayViewServiceData.extremeDelivery?.extremeDeliveryCount?.actual?.value?.isNaN() == false && todayViewServiceData.extremeDelivery.extremeDeliveryCount.status != null) {
            extreme_delivery_oc_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewServiceData.extremeDelivery.extremeDeliveryCount.actual.value)
            when {
                todayViewServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    extreme_delivery_oc_actual.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    extreme_delivery_oc_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    extreme_delivery_oc_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }


        // Singles
        if (todayViewServiceData.singles?.actual?.percentage?.isNaN() == false && todayViewServiceData.singles.status != null) {
            single_percentage_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewServiceData.singles.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                todayViewServiceData.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    single_percentage_actual.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    single_percentage_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    single_percentage_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        single_percentage_goal.text =
            if (todayViewServiceData.singles?.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.singles.goal.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        single_percentage_variance.text =
            if (todayViewServiceData.singles?.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.singles.variance.percentage)
                .plus(getString(R.string.percentage_text)) else ""

        // double
        if (todayViewServiceData.singles?._doubles?.actual?.percentage?.isNaN() == false && todayViewServiceData.singles._doubles.status != null) {
            double_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewServiceData.singles._doubles.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                todayViewServiceData.singles._doubles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    double_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.singles._doubles.status.toString() == resources.getString(R.string.under_limit) -> {
                    double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    double_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    double_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        // triple
        if (todayViewServiceData.singles?.triples?.actual?.percentage?.isNaN() == false && todayViewServiceData.singles.triples.status != null) {
            triple_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewServiceData.singles.triples.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                todayViewServiceData.singles.triples.status.toString() == resources.getString(R.string.out_of_range) -> {
                    triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    triple_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.singles.triples.status.toString() == resources.getString(R.string.under_limit) -> {
                    triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    triple_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    triple_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        //Load time
        if (todayViewServiceData.loadTime?.goal?.value?.isNaN() == false) {
            load_time_goal.text =
                Validation().ignoreZeroAfterDecimal(todayViewServiceData.loadTime.goal.value)
        }
        if (todayViewServiceData.loadTime?.variance?.value?.isNaN() == false) {
            load_time_variance.text =
                Validation().ignoreZeroAfterDecimal(todayViewServiceData.loadTime.variance.value)
        }
        if (todayViewServiceData.loadTime?.actual?.value?.isNaN() == false && todayViewServiceData.loadTime.status != null) {
            load_time_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewServiceData.loadTime.actual.value)
            when {
                todayViewServiceData.loadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    load_time_actual.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.loadTime.status.toString() == resources.getString(R.string.under_limit) -> {
                    load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    load_time_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    load_time_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        //carry_out_load_time_percentage

        if (todayViewServiceData.loadTime?.carryoutLoadTime?.actual?.value?.isNaN() == false && todayViewServiceData.loadTime.carryoutLoadTime.status != null) {
            carry_out_laod_time_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewServiceData.loadTime.carryoutLoadTime.actual.value)
            when {
                todayViewServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    carry_out_laod_time_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    carry_out_laod_time_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    carry_out_laod_time_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        //wait time
        if (todayViewServiceData.waitTime?.actual?.value?.isNaN() == false && todayViewServiceData.waitTime.status != null) {
            wait_time_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewServiceData.waitTime.actual.value)
            when {
                todayViewServiceData.waitTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    wait_time_actual.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.waitTime.status.toString() == resources.getString(R.string.under_limit) -> {
                    wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    wait_time_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    wait_time_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        wait_time_goal.text =
            if (todayViewServiceData.waitTime?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.waitTime.goal.value) else ""
        wait_time_variance.text =
            if (todayViewServiceData.waitTime?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.waitTime.variance.value) else ""

        // OTD
        if (todayViewServiceData.otd?.actual?.value?.isNaN() == false && todayViewServiceData.otd.status != null) {
            otd_actual.text = Validation().ignoreZeroAfterDecimal(todayViewServiceData.otd.actual.value)
            when {
                todayViewServiceData.otd.status.toString() == resources.getString(R.string.out_of_range) -> {
                    otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    otd_actual.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.otd.status.toString() == resources.getString(R.string.under_limit) -> {
                    otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    otd_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    otd_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        otd_goal.text =
            if (todayViewServiceData.otd?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.otd.goal.value) else ""
        otd_variance.text =
            if (todayViewServiceData.otd?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.otd.variance.value) else ""

        // AOT
        if (todayViewServiceData.aot?.actual?.percentage?.isNaN() == false && todayViewServiceData.aot.status != null) {
            aot_actual.text = Validation().ignoreZeroAfterDecimal(todayViewServiceData.aot.actual.percentage)
                .plus(getString(R.string.percentage_text))
            when {
                todayViewServiceData.aot.status.toString() == resources.getString(R.string.out_of_range) -> {
                    aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    aot_actual.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.aot.status.toString() == resources.getString(R.string.under_limit) -> {
                    aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    aot_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    aot_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        aot_goal.text =
            if (todayViewServiceData.aot?.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.aot.goal.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        aot_variance.text =
            if (todayViewServiceData.aot?.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.aot.variance.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        hang_up_percentage.text =
            if (todayViewServiceData.aot?.hangUps?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.aot.hangUps.actual.value) else ""


        // Out the door
        if (todayViewServiceData.outTheDoor?.actual?.value?.isNaN() == false && todayViewServiceData.outTheDoor.status != null) {
            out_door_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewServiceData.outTheDoor.actual.value)
            when {
                todayViewServiceData.outTheDoor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    out_door_actual.setTextColor(getColor(R.color.red))
                }
                todayViewServiceData.outTheDoor.status.toString() == resources.getString(R.string.under_limit) -> {
                    out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    out_door_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    out_door_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        out_door_goal.text =
            if (todayViewServiceData.outTheDoor?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.outTheDoor.goal.value) else ""
        out_door_variance.text =
            if (todayViewServiceData.outTheDoor?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewServiceData.outTheDoor.variance.value) else ""


        // csat

        // csat
        if (todayViewServiceData.csatMeterRating90dayAvg != null) {
            csat_display.text =
                todayViewServiceData.csatMeterRating90dayAvg.total?.displayName
                    ?: getString(R.string.csat_text)
            weekly_text.text =
                todayViewServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate?.displayName
                    ?: getString(R.string.weekly_text)
            cc_per_100_text.text =
                todayViewServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg?.displayName
                    ?: getString(
                        R.string.cc_per_100_text)

            if (todayViewServiceData.csatMeterRating90dayAvg.total?.actual?.value?.isNaN() == false && todayViewServiceData.csatMeterRating90dayAvg.total.status != null) {
                csat_goal.text =
                    Validation().ignoreZeroAfterDecimal(todayViewServiceData.csatMeterRating90dayAvg.total.actual.value)
                when {
                    todayViewServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.red))
                    }
                    todayViewServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.green))
                    } else -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if (todayViewServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate?.actual?.value?.isNaN() == false && todayViewServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status != null) {
                weekly_percentage.text =
                    Validation().ignoreZeroAfterDecimal(todayViewServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.actual.value)
                when {
                    todayViewServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.red))
                    }
                    todayViewServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if (todayViewServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg?.actual?.value?.isNaN() == false && todayViewServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status != null) {
                cc_per_100_percentage.text =
                    Validation().ignoreZeroAfterDecimal(todayViewServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.actual.value)
                when {
                    todayViewServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.red))
                    }
                    todayViewServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
        }

    }

    private fun yesterdayViewService(serviceTodayDetail: StoreYesterdayKPIQuery.GeneralManager) {
        try{

            val yesterdayViewServiceData = serviceTodayDetail.kpis?.store?.yesterday?.service

            Logger.info("Service Yesterday", "Service Overview KPI")

            // display name

            Validation().checkNullValueToShowView(this, yesterdayViewServiceData?.aot?.displayName, aot_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewServiceData?.aot?.hangUps?.displayName, hang_up_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewServiceData?.aot?.hangUps?.displayName, hang_up_parent)

            Validation().checkNullValueToShowView(this, yesterdayViewServiceData?.csatMeterRating90dayAvg?.total?.displayName, csat_meter_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName, weekly_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName, cc_per_100_text_parent)

            if (yesterdayViewServiceData != null) {
                showServiceNarrativeData(yesterdayViewServiceData.narrative.toString())
            }

             if(yesterdayViewServiceData?.aot!= null){
                aot_view.visibility= View.VISIBLE
            }else{
                aot_view.visibility= View.GONE
            }

            out_the_door_view.visibility= View.GONE
            csat_view.visibility= View.GONE

            if (yesterdayViewServiceData?.eADT?.legTime == null || yesterdayViewServiceData.eADT.legTime.displayName.isNullOrEmpty()) {
                ll_leg_time_text.visibility = View.GONE
            } else {
                ll_leg_time_text.visibility = View.VISIBLE
                leg_time_text.text = yesterdayViewServiceData.eADT.legTime.displayName
            }
            if (yesterdayViewServiceData?.extremeDelivery?.deliveryOrderCount == null || yesterdayViewServiceData.extremeDelivery.deliveryOrderCount.displayName.isNullOrEmpty()) {
                ll_delivery_order_count_service.visibility = View.GONE
            } else {
                ll_delivery_order_count_service.visibility = View.VISIBLE
                delivery_order_count.text = yesterdayViewServiceData.extremeDelivery.deliveryOrderCount.displayName
            }
            if (yesterdayViewServiceData?.extremeDelivery?.extremeDeliveryCount == null || yesterdayViewServiceData.extremeDelivery.extremeDeliveryCount.displayName.isNullOrEmpty()) {
                ll_extreme_delivery_oc_text.visibility = View.GONE
            } else {
                ll_extreme_delivery_oc_text.visibility = View.VISIBLE
                extreme_delivery_oc_text.text = yesterdayViewServiceData.extremeDelivery.extremeDeliveryCount.displayName
            }
            if (yesterdayViewServiceData?.singles?._doubles == null || yesterdayViewServiceData.singles._doubles.displayName.isNullOrEmpty()) {
                ll_double_text.visibility = View.GONE
            } else {
                ll_double_text.visibility = View.VISIBLE
                double_text.text = yesterdayViewServiceData.singles._doubles.displayName
            }
            if (yesterdayViewServiceData?.singles?.triples == null || yesterdayViewServiceData.singles.triples.displayName.isNullOrEmpty()) {
                ll_triples_text.visibility = View.GONE
            } else {
                ll_triples_text.visibility = View.VISIBLE
                triples_text.text =  yesterdayViewServiceData.singles.triples.displayName
            }
            if (yesterdayViewServiceData?.loadTime?.carryoutLoadTime == null || yesterdayViewServiceData.loadTime.carryoutLoadTime.displayName.isNullOrEmpty()) {
                ll_carry_out_laod_time_text.visibility = View.GONE
            } else {
                ll_carry_out_laod_time_text.visibility = View.VISIBLE
                carry_out_laod_time_text.text = yesterdayViewServiceData.loadTime.carryoutLoadTime.displayName
            }
            if (yesterdayViewServiceData?.aot?.hangUps == null || yesterdayViewServiceData.aot.hangUps.displayName.isNullOrEmpty()) {
                hang_up_parent.visibility = View.GONE
            } else {
                hang_up_parent.visibility = View.VISIBLE
                hang_up_text.text =  yesterdayViewServiceData.aot.hangUps.displayName
            }


            // display headers
            eadt_display.text = yesterdayViewServiceData?.eADT?.displayName
            extreme_delivery_display.text = yesterdayViewServiceData?.extremeDelivery?.displayName
            single_display.text = yesterdayViewServiceData?.singles?.displayName
            load_time_display.text = yesterdayViewServiceData?.loadTime?.displayName
            wait_time_display.text = yesterdayViewServiceData?.waitTime?.displayName
            otd_display.text = yesterdayViewServiceData?.otd?.displayName
            aot_display.text = yesterdayViewServiceData?.aot?.displayName
            out_the_door_display.text = yesterdayViewServiceData?.outTheDoor?.displayName

            service_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (service_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.service_text)

                        }
                        y = service_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            if ( yesterdayViewServiceData?.eADT?.status != null) {
                when {
                    yesterdayViewServiceData.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val eatServiceGoalGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.eADT?.goal?.amount, yesterdayViewServiceData?.eADT?.goal?.percentage, yesterdayViewServiceData?.eADT?.goal?.value)
            val eatServiceVarianceGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.eADT?.variance?.amount, yesterdayViewServiceData?.eADT?.variance?.percentage, yesterdayViewServiceData?.eADT?.variance?.value)
            val eatServiceActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.eADT?.actual?.amount, yesterdayViewServiceData?.eADT?.actual?.percentage, yesterdayViewServiceData?.eADT?.actual?.value)
            eatData(eatServiceGoalGMYesterday,eatServiceVarianceGMYesterday,eatServiceActualGMYesterday)

            // leg time
            val legTimeServiceActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.eADT?.legTime?.actual?.amount, yesterdayViewServiceData?.eADT?.legTime?.actual?.percentage, yesterdayViewServiceData?.eADT?.legTime?.actual?.value)
            if(legTimeServiceActualGMYesterday.isEmpty()){
                
                leg_time_error.visibility = View.VISIBLE
                leg_time_actual.visibility = View.GONE
            }else{
                leg_time_actual.text =  legTimeServiceActualGMYesterday
            }

            if (yesterdayViewServiceData?.eADT?.legTime?.status != null) {
                when {
                    yesterdayViewServiceData.eADT.legTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.eADT.legTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (yesterdayViewServiceData?.extremeDelivery?.status != null) {
                when {
                    yesterdayViewServiceData.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val extremeDeliveryServiceGoalGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.extremeDelivery?.goal?.amount, yesterdayViewServiceData?.extremeDelivery?.goal?.percentage, yesterdayViewServiceData?.extremeDelivery?.goal?.value)
            val extremeDeliveryServiceVarianceGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.extremeDelivery?.variance?.amount, yesterdayViewServiceData?.extremeDelivery?.variance?.percentage, yesterdayViewServiceData?.extremeDelivery?.variance?.value)
            val extremeDeliveryServiceActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.extremeDelivery?.actual?.amount, yesterdayViewServiceData?.extremeDelivery?.actual?.percentage, yesterdayViewServiceData?.extremeDelivery?.actual?.value)
            extremeDeliveryData(extremeDeliveryServiceGoalGMYesterday,extremeDeliveryServiceVarianceGMYesterday,extremeDeliveryServiceActualGMYesterday)

            // delivery   order count
            val deliveryCountActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.amount, yesterdayViewServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.percentage, yesterdayViewServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.value)
            if(deliveryCountActualGMYesterday.isEmpty()){
                
                delivery_order_error.visibility = View.VISIBLE
                delivery_order_actual.visibility = View.GONE
            }else{
                delivery_order_actual.text =  deliveryCountActualGMYesterday
            }

            if (yesterdayViewServiceData?.extremeDelivery?.deliveryOrderCount?.status != null) {

                when {
                    yesterdayViewServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // extreme_delivery_oc_actual
            val extremeDeliveryCountActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.amount, yesterdayViewServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.percentage, yesterdayViewServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.value)
            if(extremeDeliveryCountActualGMYesterday.isEmpty()){
                
                extreme_delivery_oc_actual_error.visibility = View.VISIBLE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
            }else{
                extreme_delivery_oc_actual.text =  extremeDeliveryCountActualGMYesterday
            }

            if (yesterdayViewServiceData?.extremeDelivery?.extremeDeliveryCount?.status != null) {
                when {
                    yesterdayViewServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // Singles
            if ( yesterdayViewServiceData?.singles?.status != null) {
                when {
                    yesterdayViewServiceData.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val singleGoalGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.singles?.goal?.amount, yesterdayViewServiceData?.singles?.goal?.percentage, yesterdayViewServiceData?.singles?.goal?.value)
            val singleServiceVarianceGMYesterday= Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.singles?.variance?.amount, yesterdayViewServiceData?.singles?.variance?.percentage, yesterdayViewServiceData?.singles?.variance?.value)
            val singleServiceActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.singles?.actual?.amount, yesterdayViewServiceData?.singles?.actual?.percentage, yesterdayViewServiceData?.singles?.actual?.value)
            singleData(singleGoalGMYesterday,singleServiceVarianceGMYesterday,singleServiceActualGMYesterday)

            // double
            val doubleServiceActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.singles?._doubles?.actual?.amount, yesterdayViewServiceData?.singles?._doubles?.actual?.percentage, yesterdayViewServiceData?.singles?._doubles?.actual?.value)
            if(doubleServiceActualGMYesterday.isEmpty()){
                
                double_percentage_error.visibility = View.VISIBLE
                double_percentage.visibility = View.GONE
            }else{
                double_percentage.text =  doubleServiceActualGMYesterday
            }

            if (yesterdayViewServiceData?.singles?._doubles?.status != null) {

                when {
                    yesterdayViewServiceData.singles._doubles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.singles._doubles.status.toString() == resources.getString(R.string.under_limit) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // triple
            val tripleServiceActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.singles?.triples?.actual?.amount, yesterdayViewServiceData?.singles?.triples?.actual?.percentage, yesterdayViewServiceData?.singles?.triples?.actual?.value)
            if(tripleServiceActualGMYesterday.isEmpty()){
                
                triple_percentage_error.visibility = View.VISIBLE
                triple_percentage.visibility = View.GONE
            }else{
                triple_percentage.text =  tripleServiceActualGMYesterday
            }

            if (yesterdayViewServiceData?.singles?.triples?.status != null) {
                when {
                    yesterdayViewServiceData.singles.triples.status.toString() == resources.getString(R.string.out_of_range) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.singles.triples.status.toString() == resources.getString(R.string.under_limit) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //Load time
            val loadTimeGoalGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.loadTime?.goal?.amount, yesterdayViewServiceData?.loadTime?.goal?.percentage, yesterdayViewServiceData?.loadTime?.goal?.value)
            val loadTimeServiceVarianceGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.loadTime?.variance?.amount, yesterdayViewServiceData?.loadTime?.variance?.percentage, yesterdayViewServiceData?.loadTime?.variance?.value)
            val loadTimeServiceActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.loadTime?.actual?.amount, yesterdayViewServiceData?.loadTime?.actual?.percentage, yesterdayViewServiceData?.loadTime?.actual?.value)
            loadTimeData(loadTimeGoalGMYesterday,loadTimeServiceVarianceGMYesterday,loadTimeServiceActualGMYesterday)

            if ( yesterdayViewServiceData?.loadTime?.status != null) {
                when {
                    yesterdayViewServiceData.loadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.loadTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

             //carry_out_load_time_percentage
            val carryOutServiceActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.loadTime?.carryoutLoadTime?.actual?.amount, yesterdayViewServiceData?.loadTime?.carryoutLoadTime?.actual?.percentage, yesterdayViewServiceData?.loadTime?.carryoutLoadTime?.actual?.value)
            if(carryOutServiceActualGMYesterday.isEmpty()){
                
                carry_out_laod_time_percentage_error.visibility = View.VISIBLE
                carry_out_laod_time_percentage.visibility = View.GONE
            }else{
                carry_out_laod_time_percentage.text =  carryOutServiceActualGMYesterday
            }

            if ( yesterdayViewServiceData?.loadTime?.carryoutLoadTime?.status != null) {
                when {
                    yesterdayViewServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //wait time

            if (yesterdayViewServiceData?.waitTime?.status != null) {

                when {
                    yesterdayViewServiceData.waitTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.waitTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val waitTimeGoalGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.waitTime?.goal?.amount, yesterdayViewServiceData?.waitTime?.goal?.percentage, yesterdayViewServiceData?.waitTime?.goal?.value)
            val waitTimeServiceVarianceGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.waitTime?.variance?.amount, yesterdayViewServiceData?.waitTime?.variance?.percentage, yesterdayViewServiceData?.waitTime?.variance?.value)
            val waitTimeServiceActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.waitTime?.actual?.amount, yesterdayViewServiceData?.waitTime?.actual?.percentage, yesterdayViewServiceData?.waitTime?.actual?.value)
            waitTimeData(waitTimeGoalGMYesterday,waitTimeServiceVarianceGMYesterday,waitTimeServiceActualGMYesterday)

            // OTD
            val otdGoalGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.otd?.goal?.amount, yesterdayViewServiceData?.otd?.goal?.percentage, yesterdayViewServiceData?.otd?.goal?.value)
            val otdServiceVarianceGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.otd?.variance?.amount, yesterdayViewServiceData?.otd?.variance?.percentage, yesterdayViewServiceData?.otd?.variance?.value)
            val otdServiceActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.otd?.actual?.amount, yesterdayViewServiceData?.otd?.actual?.percentage, yesterdayViewServiceData?.otd?.actual?.value)
            otdData(otdGoalGMYesterday,otdServiceVarianceGMYesterday,otdServiceActualGMYesterday)

            if (yesterdayViewServiceData?.otd?.status != null) {
                when {
                    yesterdayViewServiceData.otd.status.toString() == resources.getString(R.string.out_of_range) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.otd.status.toString() == resources.getString(R.string.under_limit) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // AOT
            if ( yesterdayViewServiceData?.aot?.status != null) {
                when {
                    yesterdayViewServiceData.aot.status.toString() == resources.getString(R.string.out_of_range) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.aot.status.toString() == resources.getString(R.string.under_limit) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val aotGoalGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.aot?.goal?.amount, yesterdayViewServiceData?.aot?.goal?.percentage, yesterdayViewServiceData?.aot?.goal?.value)
            val aotServiceVarianceGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.aot?.variance?.amount, yesterdayViewServiceData?.aot?.variance?.percentage, yesterdayViewServiceData?.aot?.variance?.value)
            val aotServiceActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.aot?.actual?.amount, yesterdayViewServiceData?.aot?.actual?.percentage, yesterdayViewServiceData?.aot?.actual?.value)
            aotData(aotGoalGMYesterday,aotServiceVarianceGMYesterday,aotServiceActualGMYesterday)

            //HangUp Service
            val hangUpServiceActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.aot?.hangUps?.actual?.amount, yesterdayViewServiceData?.aot?.hangUps?.actual?.percentage, yesterdayViewServiceData?.aot?.hangUps?.actual?.value)
            if(hangUpServiceActualGMYesterday.isEmpty()){
                
                hang_up_percentage_error.visibility = View.VISIBLE
                hang_up_percentage.visibility = View.GONE
            }else{
                hang_up_percentage.text =  hangUpServiceActualGMYesterday
            }


            // Out the door
            if ( yesterdayViewServiceData?.outTheDoor?.status != null) {

                when {
                    yesterdayViewServiceData.outTheDoor.status.toString() == resources.getString(R.string.out_of_range) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.outTheDoor.status.toString() == resources.getString(R.string.under_limit) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val outDoorGoalGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.outTheDoor?.goal?.amount, yesterdayViewServiceData?.outTheDoor?.goal?.percentage, yesterdayViewServiceData?.outTheDoor?.goal?.value)
            val outDoorServiceVarianceGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.outTheDoor?.variance?.amount, yesterdayViewServiceData?.outTheDoor?.variance?.percentage, yesterdayViewServiceData?.outTheDoor?.variance?.value)
            val outDoorServiceActualGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.outTheDoor?.actual?.amount, yesterdayViewServiceData?.outTheDoor?.actual?.percentage, yesterdayViewServiceData?.outTheDoor?.actual?.value)
            outDoorData(outDoorGoalGMYesterday,outDoorServiceVarianceGMYesterday,outDoorServiceActualGMYesterday)

            // csat
            val csatGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.csatMeterRating90dayAvg?.total?.actual?.amount, yesterdayViewServiceData?.csatMeterRating90dayAvg?.total?.actual?.percentage, yesterdayViewServiceData?.csatMeterRating90dayAvg?.total?.actual?.value)
            if(csatGMYesterday.isEmpty()){
                
                csat_goal_error.visibility = View.VISIBLE
                csat_goal.visibility = View.GONE
            }else{
                csat_goal.text =  csatGMYesterday
            }
            val weeklyGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.amount, yesterdayViewServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.percentage, yesterdayViewServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.value)
            if(weeklyGMYesterday.isEmpty()){
                
                weekly_percentage_error.visibility = View.VISIBLE
                weekly_percentage.visibility = View.GONE
            }else{
                weekly_percentage.text =  weeklyGMYesterday
            }
            val ccGMYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.amount, yesterdayViewServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.percentage, yesterdayViewServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.value)
            if(ccGMYesterday.isEmpty()){
                
                cc_per_100_percentage_error.visibility = View.VISIBLE
                cc_per_100_percentage.visibility = View.GONE
            }else{
                cc_per_100_percentage.text =  ccGMYesterday
            }

            csat_display.text =
                yesterdayViewServiceData?.csatMeterRating90dayAvg?.total?.displayName
                    ?: getString(R.string.csat_text)
            weekly_text.text =
                yesterdayViewServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName
                    ?: getString(R.string.weekly_text)
            cc_per_100_text.text =
                yesterdayViewServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName
                    ?: getString(
                        R.string.cc_per_100_text)

            if ( yesterdayViewServiceData?.csatMeterRating90dayAvg?.total?.status != null) {
                when {
                    yesterdayViewServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.green))
                    } else -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            if ( yesterdayViewServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.status != null) {

                when {
                    yesterdayViewServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if ( yesterdayViewServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.status != null) {
                when {
                    yesterdayViewServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
        }catch (e:Exception){
            Logger.error(e.message.toString(),"Service Overview Yesterday KPI")
        }

    }

    private fun rangeViewService(serviceTodayDetail: StorePeriodRangeKPIQuery.GeneralManager) {
        try{

            val rangeViewServiceData = serviceTodayDetail.kpis?.store?.period?.service

            Logger.info("Service Period Range", "Service Overview KPI")

            Validation().checkNullValueToShowView(this, rangeViewServiceData?.aot?.displayName, aot_parent)
            Validation().checkNullValueToShowView(this, rangeViewServiceData?.aot?.hangUps?.displayName, hang_up_parent)
            Validation().checkNullValueToShowView(this, rangeViewServiceData?.aot?.hangUps?.displayName, hang_up_parent)
            
            Validation().checkNullValueToShowView(this, rangeViewServiceData?.csatMeterRating90dayAvg?.total?.displayName, csat_meter_parent)
            Validation().checkNullValueToShowView(this, rangeViewServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName, weekly_parent)
            Validation().checkNullValueToShowView(this, rangeViewServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName, cc_per_100_text_parent)

            showServiceNarrativeData(rangeViewServiceData?.narrative.toString())

            if(rangeViewServiceData?.aot!= null){
                aot_view.visibility= View.VISIBLE
            }else{
                aot_view.visibility= View.GONE
            }

            out_the_door_view.visibility= View.GONE
            csat_view.visibility= View.GONE
            if (rangeViewServiceData?.eADT?.legTime == null || rangeViewServiceData.eADT.legTime.displayName.isNullOrEmpty()) {
                ll_leg_time_text.visibility = View.GONE
            } else {
                ll_leg_time_text.visibility = View.VISIBLE
                leg_time_text.text = rangeViewServiceData.eADT.legTime.displayName
            }
            if (rangeViewServiceData?.extremeDelivery?.deliveryOrderCount == null || rangeViewServiceData.extremeDelivery.deliveryOrderCount.displayName.isNullOrEmpty()) {
                ll_delivery_order_count_service.visibility = View.GONE
            } else {
                ll_delivery_order_count_service.visibility = View.VISIBLE
                delivery_order_count.text = rangeViewServiceData.extremeDelivery.deliveryOrderCount.displayName
            }
            if (rangeViewServiceData?.extremeDelivery?.extremeDeliveryCount == null || rangeViewServiceData.extremeDelivery.extremeDeliveryCount.displayName.isNullOrEmpty()) {
                ll_extreme_delivery_oc_text.visibility = View.GONE
            } else {
                ll_extreme_delivery_oc_text.visibility = View.VISIBLE
                extreme_delivery_oc_text.text = rangeViewServiceData.extremeDelivery.extremeDeliveryCount.displayName
            }
            if (rangeViewServiceData?.singles?._doubles == null || rangeViewServiceData.singles._doubles.displayName.isNullOrEmpty()) {
                ll_double_text.visibility = View.GONE
            } else {
                ll_double_text.visibility = View.VISIBLE
                double_text.text = rangeViewServiceData.singles._doubles.displayName
            }
            if (rangeViewServiceData?.singles?.triples == null || rangeViewServiceData.singles.triples.displayName.isNullOrEmpty()) {
                ll_triples_text.visibility = View.GONE
            } else {
                ll_triples_text.visibility = View.VISIBLE
                triples_text.text =  rangeViewServiceData.singles.triples.displayName
            }
            if (rangeViewServiceData?.loadTime?.carryoutLoadTime == null || rangeViewServiceData.loadTime.carryoutLoadTime.displayName.isNullOrEmpty()) {
                ll_carry_out_laod_time_text.visibility = View.GONE
            } else {
                ll_carry_out_laod_time_text.visibility = View.VISIBLE
                carry_out_laod_time_text.text = rangeViewServiceData.loadTime.carryoutLoadTime.displayName
            }
            if (rangeViewServiceData?.aot?.hangUps == null || rangeViewServiceData.aot.hangUps.displayName.isNullOrEmpty()) {
                hang_up_parent.visibility = View.GONE
            } else {
                hang_up_parent.visibility = View.VISIBLE
                hang_up_text.text =  rangeViewServiceData.aot.hangUps.displayName
            }


            // display headers
            eadt_display.text = rangeViewServiceData?.eADT?.displayName
            extreme_delivery_display.text = rangeViewServiceData?.extremeDelivery?.displayName
            single_display.text = rangeViewServiceData?.singles?.displayName
            load_time_display.text = rangeViewServiceData?.loadTime?.displayName
            wait_time_display.text = rangeViewServiceData?.waitTime?.displayName
            otd_display.text = rangeViewServiceData?.otd?.displayName
            aot_display.text = rangeViewServiceData?.aot?.displayName
            out_the_door_display.text = rangeViewServiceData?.outTheDoor?.displayName

            service_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (service_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.service_text)
                        }
                        y = service_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            showServiceNarrativeData(rangeViewServiceData?.narrative.toString())

            if ( rangeViewServiceData?.eADT?.status != null) {
                when {
                    rangeViewServiceData.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val eatServiceGoalGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.eADT?.goal?.amount, rangeViewServiceData?.eADT?.goal?.percentage, rangeViewServiceData?.eADT?.goal?.value)
            val eatServiceVarianceGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.eADT?.variance?.amount, rangeViewServiceData?.eADT?.variance?.percentage, rangeViewServiceData?.eADT?.variance?.value)
            val eatServiceActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.eADT?.actual?.amount, rangeViewServiceData?.eADT?.actual?.percentage, rangeViewServiceData?.eADT?.actual?.value)
            eatData(eatServiceGoalGMRange,eatServiceVarianceGMRange,eatServiceActualGMRange)

            // leg time
            val legTimeServiceActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.eADT?.legTime?.actual?.amount, rangeViewServiceData?.eADT?.legTime?.actual?.percentage, rangeViewServiceData?.eADT?.legTime?.actual?.value)
            if(legTimeServiceActualGMRange.isEmpty()){
                
                leg_time_error.visibility = View.VISIBLE
                leg_time_actual.visibility = View.GONE
            }else{
                leg_time_actual.text =  legTimeServiceActualGMRange
            }

            if (rangeViewServiceData?.eADT?.legTime?.status != null) {
                when {
                    rangeViewServiceData.eADT.legTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.eADT.legTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (rangeViewServiceData?.extremeDelivery?.status != null) {
                when {
                    rangeViewServiceData.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val extremeDeliveryServiceGoalGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.extremeDelivery?.goal?.amount, rangeViewServiceData?.extremeDelivery?.goal?.percentage, rangeViewServiceData?.extremeDelivery?.goal?.value)
            val extremeDeliveryServiceVarianceGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.extremeDelivery?.variance?.amount, rangeViewServiceData?.extremeDelivery?.variance?.percentage, rangeViewServiceData?.extremeDelivery?.variance?.value)
            val extremeDeliveryServiceActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.extremeDelivery?.actual?.amount, rangeViewServiceData?.extremeDelivery?.actual?.percentage, rangeViewServiceData?.extremeDelivery?.actual?.value)
            extremeDeliveryData(extremeDeliveryServiceGoalGMRange,extremeDeliveryServiceVarianceGMRange,extremeDeliveryServiceActualGMRange)

            // delivery   order count
            val deliveryCountActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.amount, rangeViewServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.percentage, rangeViewServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.value)
            if(deliveryCountActualGMRange.isEmpty()){
                
                delivery_order_error.visibility = View.VISIBLE
                delivery_order_actual.visibility = View.GONE
            }else{
                delivery_order_actual.text =  deliveryCountActualGMRange
            }

            if (rangeViewServiceData?.extremeDelivery?.deliveryOrderCount?.status != null) {

                when {
                    rangeViewServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // extreme_delivery_oc_actual
            val extremeDeliveryCountActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.amount, rangeViewServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.percentage, rangeViewServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.value)
            if(extremeDeliveryCountActualGMRange.isEmpty()){
                
                extreme_delivery_oc_actual_error.visibility = View.VISIBLE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
            }else{
                extreme_delivery_oc_actual.text =  extremeDeliveryCountActualGMRange
            }

            if (rangeViewServiceData?.extremeDelivery?.extremeDeliveryCount?.status != null) {
                when {
                    rangeViewServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // Singles
            if ( rangeViewServiceData?.singles?.status != null) {
                when {
                    rangeViewServiceData.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val singleGoalGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.singles?.goal?.amount, rangeViewServiceData?.singles?.goal?.percentage, rangeViewServiceData?.singles?.goal?.value)
            val singleServiceVarianceGMRange= Validation().checkAmountPercentageValue(this, rangeViewServiceData?.singles?.variance?.amount, rangeViewServiceData?.singles?.variance?.percentage, rangeViewServiceData?.singles?.variance?.value)
            val singleServiceActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.singles?.actual?.amount, rangeViewServiceData?.singles?.actual?.percentage, rangeViewServiceData?.singles?.actual?.value)
            singleData(singleGoalGMRange,singleServiceVarianceGMRange,singleServiceActualGMRange)

            // double
            val doubleServiceActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.singles?._doubles?.actual?.amount, rangeViewServiceData?.singles?._doubles?.actual?.percentage, rangeViewServiceData?.singles?._doubles?.actual?.value)
            if(doubleServiceActualGMRange.isEmpty()){
                
                double_percentage_error.visibility = View.VISIBLE
                double_percentage.visibility = View.GONE
            }else{
                double_percentage.text =  doubleServiceActualGMRange
            }

            if (rangeViewServiceData?.singles?._doubles?.status != null) {

                when {
                    rangeViewServiceData.singles._doubles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.singles._doubles.status.toString() == resources.getString(R.string.under_limit) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // triple
            val tripleServiceActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.singles?.triples?.actual?.amount, rangeViewServiceData?.singles?.triples?.actual?.percentage, rangeViewServiceData?.singles?.triples?.actual?.value)
            if(tripleServiceActualGMRange.isEmpty()){
                
                triple_percentage_error.visibility = View.VISIBLE
                triple_percentage.visibility = View.GONE
            }else{
                triple_percentage.text =  tripleServiceActualGMRange
            }

            if (rangeViewServiceData?.singles?.triples?.status != null) {
                when {
                    rangeViewServiceData.singles.triples.status.toString() == resources.getString(R.string.out_of_range) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.singles.triples.status.toString() == resources.getString(R.string.under_limit) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //Load time
            val loadTimeGoalGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.loadTime?.goal?.amount, rangeViewServiceData?.loadTime?.goal?.percentage, rangeViewServiceData?.loadTime?.goal?.value)
            val loadTimeServiceVarianceGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.loadTime?.variance?.amount, rangeViewServiceData?.loadTime?.variance?.percentage, rangeViewServiceData?.loadTime?.variance?.value)
            val loadTimeServiceActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.loadTime?.actual?.amount, rangeViewServiceData?.loadTime?.actual?.percentage, rangeViewServiceData?.loadTime?.actual?.value)
            loadTimeData(loadTimeGoalGMRange,loadTimeServiceVarianceGMRange,loadTimeServiceActualGMRange)

            if ( rangeViewServiceData?.loadTime?.status != null) {
                when {
                    rangeViewServiceData.loadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.loadTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

             //carry_out_load_time_percentage
            val carryOutServiceActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.loadTime?.carryoutLoadTime?.actual?.amount, rangeViewServiceData?.loadTime?.carryoutLoadTime?.actual?.percentage, rangeViewServiceData?.loadTime?.carryoutLoadTime?.actual?.value)
            if(carryOutServiceActualGMRange.isEmpty()){
                
                carry_out_laod_time_percentage_error.visibility = View.VISIBLE
                carry_out_laod_time_percentage.visibility = View.GONE
            }else{
                carry_out_laod_time_percentage.text =  carryOutServiceActualGMRange
            }

            if ( rangeViewServiceData?.loadTime?.carryoutLoadTime?.status != null) {
                when {
                    rangeViewServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //wait time
            val waitTimeGoalGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.waitTime?.goal?.amount, rangeViewServiceData?.waitTime?.goal?.percentage, rangeViewServiceData?.waitTime?.goal?.value)
            val waitTimeServiceVarianceGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.waitTime?.variance?.amount, rangeViewServiceData?.waitTime?.variance?.percentage, rangeViewServiceData?.waitTime?.variance?.value)
            val waitTimeServiceActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.waitTime?.actual?.amount, rangeViewServiceData?.waitTime?.actual?.percentage, rangeViewServiceData?.waitTime?.actual?.value)

            if (rangeViewServiceData?.waitTime?.status != null) {

                when {
                    rangeViewServiceData.waitTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.waitTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            waitTimeData(waitTimeGoalGMRange,waitTimeServiceVarianceGMRange,waitTimeServiceActualGMRange)

            // OTD
            val otdGoalGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.otd?.goal?.amount, rangeViewServiceData?.otd?.goal?.percentage, rangeViewServiceData?.otd?.goal?.value)
            val otdServiceVarianceGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.otd?.variance?.amount, rangeViewServiceData?.otd?.variance?.percentage, rangeViewServiceData?.otd?.variance?.value)
            val otdServiceActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.otd?.actual?.amount, rangeViewServiceData?.otd?.actual?.percentage, rangeViewServiceData?.otd?.actual?.value)
            otdData(otdGoalGMRange,otdServiceVarianceGMRange,otdServiceActualGMRange)

            if (rangeViewServiceData?.otd?.status != null) {
                when {
                    rangeViewServiceData.otd.status.toString() == resources.getString(R.string.out_of_range) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.otd.status.toString() == resources.getString(R.string.under_limit) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.green))
                    }else -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // AOT
            if ( rangeViewServiceData?.aot?.status != null) {
                when {
                    rangeViewServiceData.aot.status.toString() == resources.getString(R.string.out_of_range) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.aot.status.toString() == resources.getString(R.string.under_limit) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val aotGoalGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.aot?.goal?.amount, rangeViewServiceData?.aot?.goal?.percentage, rangeViewServiceData?.aot?.goal?.value)
            val aotServiceVarianceGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.aot?.variance?.amount, rangeViewServiceData?.aot?.variance?.percentage, rangeViewServiceData?.aot?.variance?.value)
            val aotServiceActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.aot?.actual?.amount, rangeViewServiceData?.aot?.actual?.percentage, rangeViewServiceData?.aot?.actual?.value)
            aotData(aotGoalGMRange,aotServiceVarianceGMRange,aotServiceActualGMRange)

            //HangUp Service
            val hangUpServiceActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.aot?.hangUps?.actual?.amount, rangeViewServiceData?.aot?.hangUps?.actual?.percentage, rangeViewServiceData?.aot?.hangUps?.actual?.value)
            if(hangUpServiceActualGMRange.isEmpty()){
                
                hang_up_percentage_error.visibility = View.VISIBLE
                hang_up_percentage.visibility = View.GONE
            }else{
                hang_up_percentage.text =  hangUpServiceActualGMRange
            }


            // Out the door
            if ( rangeViewServiceData?.outTheDoor?.status != null) {

                when {
                    rangeViewServiceData.outTheDoor.status.toString() == resources.getString(R.string.out_of_range) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.outTheDoor.status.toString() == resources.getString(R.string.under_limit) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val outDoorGoalGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.outTheDoor?.goal?.amount, rangeViewServiceData?.outTheDoor?.goal?.percentage, rangeViewServiceData?.outTheDoor?.goal?.value)
            val outDoorServiceVarianceGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.outTheDoor?.variance?.amount, rangeViewServiceData?.outTheDoor?.variance?.percentage, rangeViewServiceData?.outTheDoor?.variance?.value)
            val outDoorServiceActualGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.outTheDoor?.actual?.amount, rangeViewServiceData?.outTheDoor?.actual?.percentage, rangeViewServiceData?.outTheDoor?.actual?.value)
            outDoorData(outDoorGoalGMRange,outDoorServiceVarianceGMRange,outDoorServiceActualGMRange)

            // csat
            val csatGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.csatMeterRating90dayAvg?.total?.actual?.amount, rangeViewServiceData?.csatMeterRating90dayAvg?.total?.actual?.percentage, rangeViewServiceData?.csatMeterRating90dayAvg?.total?.actual?.value)
            if(csatGMRange.isEmpty()){
                
                csat_goal_error.visibility = View.VISIBLE
                csat_goal.visibility = View.GONE
            }else{
                csat_goal.text =  csatGMRange
            }
            val weeklyGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.amount, rangeViewServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.percentage, rangeViewServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.value)
            if(weeklyGMRange.isEmpty()){
                
                weekly_percentage_error.visibility = View.VISIBLE
                weekly_percentage.visibility = View.GONE
            }else{
                weekly_percentage.text =  weeklyGMRange
            }
            val ccGMRange = Validation().checkAmountPercentageValue(this, rangeViewServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.amount, rangeViewServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.percentage, rangeViewServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.value)
            if(ccGMRange.isEmpty()){
                
                cc_per_100_percentage_error.visibility = View.VISIBLE
                cc_per_100_percentage.visibility = View.GONE
            }else{
                cc_per_100_percentage.text =  ccGMRange
            }

            csat_display.text =
                rangeViewServiceData?.csatMeterRating90dayAvg?.total?.displayName
                    ?: getString(R.string.csat_text)
            weekly_text.text =
                rangeViewServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName
                    ?: getString(R.string.weekly_text)
            cc_per_100_text.text =
                rangeViewServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName
                    ?: getString(
                        R.string.cc_per_100_text)

            if ( rangeViewServiceData?.csatMeterRating90dayAvg?.total?.status != null) {
                when {
                    rangeViewServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.green))
                    } else -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            if ( rangeViewServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.status != null) {

                when {
                    rangeViewServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if ( rangeViewServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.status != null) {
                when {
                    rangeViewServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeViewServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
        }catch (e:Exception){
            Logger.error(e.message.toString(),"Service Overview Period Range KPI")
        }

    }

    // ceo
    fun todayViewCEOService(serviceTodayDetail: CEOOverviewTodayQuery.Ceo) {
        val todayViewCeoServiceData = serviceTodayDetail.kpis?.supervisors?.stores?.today?.service

        Logger.info("Service Today", "Service Overview KPI")

        // display name
        service_text_small.text =
            todayViewCeoServiceData!!.displayName ?: getString(R.string.service_text_small)

        if (todayViewCeoServiceData.eADT?.legTime == null || todayViewCeoServiceData.eADT.legTime.displayName.isNullOrEmpty()) {
            ll_leg_time_text.visibility = View.GONE
        } else {
            ll_leg_time_text.visibility = View.VISIBLE
            leg_time_text.text = todayViewCeoServiceData.eADT.legTime.displayName
        }
        if (todayViewCeoServiceData.extremeDelivery?.deliveryOrderCount == null || todayViewCeoServiceData.extremeDelivery.deliveryOrderCount.displayName.isNullOrEmpty()) {
            ll_delivery_order_count_service.visibility = View.GONE
        } else {
            ll_delivery_order_count_service.visibility = View.VISIBLE
            delivery_order_count.text = todayViewCeoServiceData.extremeDelivery.deliveryOrderCount.displayName
        }
        if (todayViewCeoServiceData.extremeDelivery?.extremeDeliveryCount == null || todayViewCeoServiceData.extremeDelivery.extremeDeliveryCount.displayName.isNullOrEmpty()) {
            ll_extreme_delivery_oc_text.visibility = View.GONE
        } else {
            ll_extreme_delivery_oc_text.visibility = View.VISIBLE
            extreme_delivery_oc_text.text = todayViewCeoServiceData.extremeDelivery.extremeDeliveryCount.displayName
        }
        if (todayViewCeoServiceData.singles?._doubles == null || todayViewCeoServiceData.singles._doubles.displayName.isNullOrEmpty()) {
            ll_double_text.visibility = View.GONE
        } else {
            ll_double_text.visibility = View.VISIBLE
            double_text.text = todayViewCeoServiceData.singles._doubles.displayName
        }
        if (todayViewCeoServiceData.singles?.triples == null || todayViewCeoServiceData.singles.triples.displayName.isNullOrEmpty()) {
            ll_triples_text.visibility = View.GONE
        } else {
            ll_triples_text.visibility = View.VISIBLE
            triples_text.text =  todayViewCeoServiceData.singles.triples.displayName
        }
        if (todayViewCeoServiceData.loadTime?.carryoutLoadTime == null || todayViewCeoServiceData.loadTime.carryoutLoadTime.displayName.isNullOrEmpty()) {
            ll_carry_out_laod_time_text.visibility = View.GONE
        } else {
            ll_carry_out_laod_time_text.visibility = View.VISIBLE
            carry_out_laod_time_text.text = todayViewCeoServiceData.loadTime.carryoutLoadTime.displayName
        }
        if (todayViewCeoServiceData.aot?.hangUps == null || todayViewCeoServiceData.aot.hangUps.displayName.isNullOrEmpty()) {
            hang_up_parent.visibility = View.GONE
        } else {
            hang_up_parent.visibility = View.VISIBLE
            hang_up_text.text =  todayViewCeoServiceData.aot.hangUps.displayName
        }


        weekly_text.text = todayViewCeoServiceData.csatMeterRating90dayAvg!!.weeklyCCCClosureRate!!.displayName
            ?: getString(R.string.weekly_text)
        cc_per_100_text.text =
            todayViewCeoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg!!.displayName ?: getString(
                R.string.cc_per_100_text)
        // display headers
        eadt_display.text = todayViewCeoServiceData.eADT?.displayName
        extreme_delivery_display.text = todayViewCeoServiceData.extremeDelivery?.displayName
        single_display.text = todayViewCeoServiceData.singles?.displayName
        load_time_display.text = todayViewCeoServiceData.loadTime?.displayName
        wait_time_display.text = todayViewCeoServiceData.waitTime?.displayName
        otd_display.text = todayViewCeoServiceData.otd?.displayName
        aot_display.text = todayViewCeoServiceData.aot?.displayName
        out_the_door_display.text = todayViewCeoServiceData.outTheDoor?.displayName


        if (todayViewCeoServiceData.actual?.value?.isNaN() == false && todayViewCeoServiceData.status != null) {
            service_sales.text = Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.actual.value)
            when {
                todayViewCeoServiceData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_sales.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_sales.setTextColor(getColor(R.color.green))
                } else -> {
                    service_sales.setTextColor(getColor(R.color.text_color))
                }
            }
        }



        service_scroll_parent.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 0f
                override fun onScrollChanged() {
                    if (service_scroll_parent.scrollY > y) {
                        parent_data_on_scroll_linear.visibility = View.VISIBLE
                        parent_data_on_scroll_view.visibility = View.VISIBLE
                        level_two_scroll_data_action.text =
                            todayViewCeoServiceData.displayName ?: getString(R.string.service_text_small)

                        if (todayViewCeoServiceData.actual?.value?.isNaN() == false && todayViewCeoServiceData.status != null) {
                            level_two_scroll_data_action_value.text =
                                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.goal!!.value)
                            when {
                                todayViewCeoServiceData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.red_circle,
                                        0
                                    )
                                }
                                todayViewCeoServiceData.status.toString() == resources.getString(R.string.under_limit) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.green))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.green_circle,
                                        0
                                    )
                                }
                                else -> {
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
                    y = service_scroll_parent.scrollY.toFloat()
                    if (y <= 0) {
                        parent_data_on_scroll_linear.visibility = View.INVISIBLE
                        parent_data_on_scroll_view.visibility = View.GONE

                    }
                }
            })


        service_goal_value.text =
            if (todayViewCeoServiceData.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.goal.value
            ) else ""
        service_variance_value.text =
            if (todayViewCeoServiceData.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.variance.value) else ""

        showServiceNarrativeData(todayViewCeoServiceData.narrative.toString())


        if (todayViewCeoServiceData.eADT?.actual?.value?.isNaN() == false && todayViewCeoServiceData.eADT.status != null) {
            eadt_actual.text = Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.eADT.actual.value)
            when {
                todayViewCeoServiceData.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    eadt_actual.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    eadt_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    eadt_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        eadt_goal.text =
            if (todayViewCeoServiceData.eADT?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.eADT.goal.value) else ""
        eadt_variance.text =
            if (todayViewCeoServiceData.eADT?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.eADT.variance.value) else ""
        // leg time
        if (todayViewCeoServiceData.eADT?.legTime?.actual?.value?.isNaN() == false && todayViewCeoServiceData.eADT.legTime.status != null) {
            leg_time_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.eADT.legTime.actual.value)
            when {
                todayViewCeoServiceData.eADT.legTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    leg_time_actual.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.eADT.legTime.status.toString() == resources.getString(R.string.under_limit) -> {
                    leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    leg_time_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    leg_time_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        leg_time_goal.text =
            if (todayViewCeoServiceData.eADT?.legTime?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.eADT.legTime.goal.value) else ""
        leg_time_variance.text =
            if (todayViewCeoServiceData.eADT?.legTime?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.eADT.legTime.variance.value) else ""


        // extreme_delivery
        if (todayViewCeoServiceData.extremeDelivery?.actual?.value?.isNaN() == false && todayViewCeoServiceData.extremeDelivery.status != null) {
            extreme_delivery_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.extremeDelivery.actual.value)
                    .plus(getString(R.string.percentage_text))
            when {
                todayViewCeoServiceData.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    extreme_delivery_actual.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    extreme_delivery_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    extreme_delivery_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        extreme_delivery_goal.text =
            if (todayViewCeoServiceData.extremeDelivery?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.extremeDelivery.goal.value)
            else ""
        extreme_delivery_variance.text =
            if (todayViewCeoServiceData.extremeDelivery?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.extremeDelivery.variance.value)
            else ""

        // delivery order count
        if (todayViewCeoServiceData.extremeDelivery?.deliveryOrderCount?.actual?.value?.isNaN() == false && todayViewCeoServiceData.extremeDelivery.deliveryOrderCount.status != null) {
            delivery_order_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.extremeDelivery.deliveryOrderCount.actual.value)
            when {
                todayViewCeoServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    delivery_order_actual.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    delivery_order_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    delivery_order_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        // extreme_delivery_oc_actual
        if (todayViewCeoServiceData.extremeDelivery?.extremeDeliveryCount?.actual?.value?.isNaN() == false && todayViewCeoServiceData.extremeDelivery.extremeDeliveryCount.status != null) {
            extreme_delivery_oc_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.extremeDelivery.extremeDeliveryCount.actual.value)
            when {
                todayViewCeoServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    extreme_delivery_oc_actual.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    extreme_delivery_oc_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    extreme_delivery_oc_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }


        // Singles
        if (todayViewCeoServiceData.singles?.actual?.percentage?.isNaN() == false && todayViewCeoServiceData.singles.status != null) {
            single_percentage_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.singles.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                todayViewCeoServiceData.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    single_percentage_actual.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    single_percentage_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    single_percentage_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        single_percentage_goal.text =
            if (todayViewCeoServiceData.singles?.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.singles.goal.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        single_percentage_variance.text =
            if (todayViewCeoServiceData.singles?.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.singles.variance.percentage)
                .plus(getString(R.string.percentage_text)) else ""

        // double
        if (todayViewCeoServiceData.singles?._doubles?.actual?.percentage?.isNaN() == false && todayViewCeoServiceData.singles._doubles.status != null) {
            double_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.singles._doubles.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                todayViewCeoServiceData.singles._doubles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    double_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.singles._doubles.status.toString() == resources.getString(R.string.under_limit) -> {
                    double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    double_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    double_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        // triple
        if (todayViewCeoServiceData.singles?.triples?.actual?.percentage?.isNaN() == false && todayViewCeoServiceData.singles.triples.status != null) {
            triple_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.singles.triples.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                todayViewCeoServiceData.singles.triples.status.toString() == resources.getString(R.string.out_of_range) -> {
                    triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    triple_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.singles.triples.status.toString() == resources.getString(R.string.under_limit) -> {
                    triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    triple_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    triple_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        //Load time
        if (todayViewCeoServiceData.loadTime?.goal?.value?.isNaN() == false) {
            load_time_goal.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.loadTime.goal.value)
        }
        if (todayViewCeoServiceData.loadTime?.variance?.value?.isNaN() == false) {
            load_time_variance.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.loadTime.variance.value)
        }
        if (todayViewCeoServiceData.loadTime?.actual?.value?.isNaN() == false && todayViewCeoServiceData.loadTime.status != null) {
            load_time_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.loadTime.actual.value)
            when {
                todayViewCeoServiceData.loadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    load_time_actual.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.loadTime.status.toString() == resources.getString(R.string.under_limit) -> {
                    load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    load_time_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    load_time_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }

         //carry_out_load_time_percentage

        if (todayViewCeoServiceData.loadTime?.carryoutLoadTime?.actual?.value?.isNaN() == false && todayViewCeoServiceData.loadTime.carryoutLoadTime.status != null) {
            carry_out_laod_time_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.loadTime.carryoutLoadTime.actual.value)
            when {
                todayViewCeoServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    carry_out_laod_time_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    carry_out_laod_time_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    carry_out_laod_time_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        //wait time
        if (todayViewCeoServiceData.waitTime?.actual?.value?.isNaN() == false && todayViewCeoServiceData.waitTime.status != null) {
            wait_time_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.waitTime.actual.value)
            when {
                todayViewCeoServiceData.waitTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    wait_time_actual.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.waitTime.status.toString() == resources.getString(R.string.under_limit) -> {
                    wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    wait_time_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    wait_time_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        wait_time_goal.text =
            if (todayViewCeoServiceData.waitTime?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.waitTime.goal.value) else ""
        wait_time_variance.text =
            if (todayViewCeoServiceData.waitTime?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.waitTime.variance.value) else ""

        // OTD
        if (todayViewCeoServiceData.otd?.actual?.value?.isNaN() == false && todayViewCeoServiceData.otd.status != null) {
            otd_actual.text = Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.otd.actual.value)
            when {
                todayViewCeoServiceData.otd.status.toString() == resources.getString(R.string.out_of_range) -> {
                    otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    otd_actual.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.otd.status.toString() == resources.getString(R.string.under_limit) -> {
                    otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    otd_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    otd_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        otd_goal.text =
            if (todayViewCeoServiceData.otd?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.otd.goal.value) else ""
        otd_variance.text =
            if (todayViewCeoServiceData.otd?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.otd.variance.value) else ""

        // AOT
        if (todayViewCeoServiceData.aot?.actual?.percentage?.isNaN() == false && todayViewCeoServiceData.aot.status != null) {
            aot_actual.text = Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.aot.actual.percentage)
                .plus(getString(R.string.percentage_text))
            when {
                todayViewCeoServiceData.aot.status.toString() == resources.getString(R.string.out_of_range) -> {
                    aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    aot_actual.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.aot.status.toString() == resources.getString(R.string.under_limit) -> {
                    aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    aot_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    aot_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        aot_goal.text =
            if (todayViewCeoServiceData.aot?.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.aot.goal.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        aot_variance.text =
            if (todayViewCeoServiceData.aot?.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.aot.variance.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        hang_up_percentage.text =
            if (todayViewCeoServiceData.aot?.hangUps?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.aot.hangUps.actual.value) else ""


        // Out the door
        if (todayViewCeoServiceData.outTheDoor?.actual?.value?.isNaN() == false && todayViewCeoServiceData.outTheDoor.status != null) {
            out_door_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.outTheDoor.actual.value)
            when {
                todayViewCeoServiceData.outTheDoor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    out_door_actual.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.outTheDoor.status.toString() == resources.getString(R.string.under_limit) -> {
                    out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    out_door_actual.setTextColor(getColor(R.color.green))
                }else -> {
                    out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    out_door_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        out_door_goal.text =
            if (todayViewCeoServiceData.outTheDoor?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.outTheDoor.goal.value) else ""
        out_door_variance.text =
            if (todayViewCeoServiceData.outTheDoor?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewCeoServiceData.outTheDoor.variance.value) else ""


        // csat

        if (todayViewCeoServiceData.csatMeterRating90dayAvg.total?.actual?.value?.isNaN() == false && todayViewCeoServiceData.csatMeterRating90dayAvg.total.status != null) {
            csat_goal.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.csatMeterRating90dayAvg.total.actual.value)
            when {
                todayViewCeoServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    csat_goal.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    csat_goal.setTextColor(getColor(R.color.green))
                } else -> {
                    csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    csat_goal.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        if (todayViewCeoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate?.actual?.value?.isNaN() == false && todayViewCeoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status != null) {
            weekly_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.actual.value)
            when {
                todayViewCeoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    weekly_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    weekly_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    weekly_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        if (todayViewCeoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.actual?.value?.isNaN() == false && todayViewCeoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status != null) {
            cc_per_100_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewCeoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.actual.value)
            when {
                todayViewCeoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    cc_per_100_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewCeoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    cc_per_100_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    cc_per_100_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }

    }

    private fun yesterdayViewCEOService(serviceTodayDetail: CEOOverviewYesterdayQuery.Ceo) {
        try{

            val yesterdayViewCeoServiceData = serviceTodayDetail.kpis?.supervisors?.stores?.yesterday?.service

            Logger.info("Service Yesterday", "Service Overview KPI")

            // display name

            Validation().checkNullValueToShowView(this, yesterdayViewCeoServiceData?.aot?.displayName, aot_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoServiceData?.aot?.hangUps?.displayName, hang_up_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoServiceData?.aot?.hangUps?.displayName, hang_up_parent)

            Validation().checkNullValueToShowView(this, yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.total?.displayName, csat_meter_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName, weekly_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName, cc_per_100_text_parent)

            showServiceNarrativeData(yesterdayViewCeoServiceData?.narrative.toString())

             if(yesterdayViewCeoServiceData?.aot!= null){
                aot_view.visibility= View.VISIBLE
            }else{
                aot_view.visibility= View.GONE
            }
            out_the_door_view.visibility= View.GONE
            csat_view.visibility= View.GONE


            if (yesterdayViewCeoServiceData?.eADT?.legTime == null || yesterdayViewCeoServiceData.eADT.legTime.displayName.isNullOrEmpty()) {
                ll_leg_time_text.visibility = View.GONE
            } else {
                ll_leg_time_text.visibility = View.VISIBLE
                leg_time_text.text = yesterdayViewCeoServiceData.eADT.legTime.displayName
            }
            if (yesterdayViewCeoServiceData?.extremeDelivery?.deliveryOrderCount == null || yesterdayViewCeoServiceData.extremeDelivery.deliveryOrderCount.displayName.isNullOrEmpty()) {
                ll_delivery_order_count_service.visibility = View.GONE
            } else {
                ll_delivery_order_count_service.visibility = View.VISIBLE
                delivery_order_count.text = yesterdayViewCeoServiceData.extremeDelivery.deliveryOrderCount.displayName
            }
            if (yesterdayViewCeoServiceData?.extremeDelivery?.extremeDeliveryCount == null || yesterdayViewCeoServiceData.extremeDelivery.extremeDeliveryCount.displayName.isNullOrEmpty()) {
                ll_extreme_delivery_oc_text.visibility = View.GONE
            } else {
                ll_extreme_delivery_oc_text.visibility = View.VISIBLE
                extreme_delivery_oc_text.text = yesterdayViewCeoServiceData.extremeDelivery.extremeDeliveryCount.displayName
            }
            if (yesterdayViewCeoServiceData?.singles?._doubles == null || yesterdayViewCeoServiceData.singles._doubles.displayName.isNullOrEmpty()) {
                ll_double_text.visibility = View.GONE
            } else {
                ll_double_text.visibility = View.VISIBLE
                double_text.text = yesterdayViewCeoServiceData.singles._doubles.displayName
            }
            if (yesterdayViewCeoServiceData?.singles?.triples == null || yesterdayViewCeoServiceData.singles.triples.displayName.isNullOrEmpty()) {
                ll_triples_text.visibility = View.GONE
            } else {
                ll_triples_text.visibility = View.VISIBLE
                triples_text.text =  yesterdayViewCeoServiceData.singles.triples.displayName
            }
            if (yesterdayViewCeoServiceData?.loadTime?.carryoutLoadTime == null || yesterdayViewCeoServiceData.loadTime.carryoutLoadTime.displayName.isNullOrEmpty()) {
                ll_carry_out_laod_time_text.visibility = View.GONE
            } else {
                ll_carry_out_laod_time_text.visibility = View.VISIBLE
                carry_out_laod_time_text.text = yesterdayViewCeoServiceData.loadTime.carryoutLoadTime.displayName
            }
            if (yesterdayViewCeoServiceData?.aot?.hangUps == null || yesterdayViewCeoServiceData.aot.hangUps.displayName.isNullOrEmpty()) {
                hang_up_parent.visibility = View.GONE
            } else {
                hang_up_parent.visibility = View.VISIBLE
                hang_up_text.text =  yesterdayViewCeoServiceData.aot.hangUps.displayName
            }




            // display headers
            eadt_display.text = yesterdayViewCeoServiceData?.eADT?.displayName
            extreme_delivery_display.text = yesterdayViewCeoServiceData?.extremeDelivery?.displayName
            single_display.text = yesterdayViewCeoServiceData?.singles?.displayName
            load_time_display.text = yesterdayViewCeoServiceData?.loadTime?.displayName
            wait_time_display.text = yesterdayViewCeoServiceData?.waitTime?.displayName
            otd_display.text = yesterdayViewCeoServiceData?.otd?.displayName
            aot_display.text = yesterdayViewCeoServiceData?.aot?.displayName
            out_the_door_display.text = yesterdayViewCeoServiceData?.outTheDoor?.displayName

            service_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (service_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.service_text)
                        }
                        y = service_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            if ( yesterdayViewCeoServiceData?.eADT?.status != null) {
                when {
                    yesterdayViewCeoServiceData.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val eatServiceGoalCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.eADT?.goal?.amount, yesterdayViewCeoServiceData?.eADT?.goal?.percentage, yesterdayViewCeoServiceData?.eADT?.goal?.value)
            val eatServiceVarianceCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.eADT?.variance?.amount, yesterdayViewCeoServiceData?.eADT?.variance?.percentage, yesterdayViewCeoServiceData?.eADT?.variance?.value)
            val eatServiceActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.eADT?.actual?.amount, yesterdayViewCeoServiceData?.eADT?.actual?.percentage, yesterdayViewCeoServiceData?.eADT?.actual?.value)
            eatData(eatServiceGoalCEOYesterday,eatServiceVarianceCEOYesterday,eatServiceActualCEOYesterday)

            // leg time
            val legTimeServiceActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.eADT?.legTime?.actual?.amount, yesterdayViewCeoServiceData?.eADT?.legTime?.actual?.percentage, yesterdayViewCeoServiceData?.eADT?.legTime?.actual?.value)
            if(legTimeServiceActualCEOYesterday.isEmpty()){
                
                leg_time_error.visibility = View.VISIBLE
                leg_time_actual.visibility = View.GONE
            }else{
                leg_time_actual.text =  legTimeServiceActualCEOYesterday
            }

           if (yesterdayViewCeoServiceData?.eADT?.legTime?.status != null) {
                when {
                    yesterdayViewCeoServiceData.eADT.legTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.eADT.legTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (yesterdayViewCeoServiceData?.extremeDelivery?.status != null) {
                when {
                    yesterdayViewCeoServiceData.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val extremeDeliveryServiceGoalCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.extremeDelivery?.goal?.amount, yesterdayViewCeoServiceData?.extremeDelivery?.goal?.percentage, yesterdayViewCeoServiceData?.extremeDelivery?.goal?.value)
            val extremeDeliveryServiceVarianceCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.extremeDelivery?.variance?.amount, yesterdayViewCeoServiceData?.extremeDelivery?.variance?.percentage, yesterdayViewCeoServiceData?.extremeDelivery?.variance?.value)
            val extremeDeliveryServiceActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.extremeDelivery?.actual?.amount, yesterdayViewCeoServiceData?.extremeDelivery?.actual?.percentage, yesterdayViewCeoServiceData?.extremeDelivery?.actual?.value)
            extremeDeliveryData(extremeDeliveryServiceGoalCEOYesterday,extremeDeliveryServiceVarianceCEOYesterday,extremeDeliveryServiceActualCEOYesterday)


            // delivery   order count
            val deliveryCountActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.amount, yesterdayViewCeoServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.percentage, yesterdayViewCeoServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.value)
            if(deliveryCountActualCEOYesterday.isEmpty()){
                
                delivery_order_error.visibility = View.VISIBLE
                delivery_order_actual.visibility = View.GONE
            }else{
                delivery_order_actual.text =  deliveryCountActualCEOYesterday
            }


            if (yesterdayViewCeoServiceData?.extremeDelivery?.deliveryOrderCount?.status != null) {

                when {
                    yesterdayViewCeoServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // extreme_delivery_oc_actual
            val extremeDeliveryCountActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.amount, yesterdayViewCeoServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.percentage, yesterdayViewCeoServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.value)
            if(extremeDeliveryCountActualCEOYesterday.isEmpty()){
                
                extreme_delivery_oc_actual_error.visibility = View.VISIBLE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
            }else{
                extreme_delivery_oc_actual.text =  extremeDeliveryCountActualCEOYesterday
            }

            if (yesterdayViewCeoServiceData?.extremeDelivery?.extremeDeliveryCount?.status != null) {
                when {
                    yesterdayViewCeoServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // Singles
            if ( yesterdayViewCeoServiceData?.singles?.status != null) {
                when {
                    yesterdayViewCeoServiceData.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val singleGoalCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.singles?.goal?.amount, yesterdayViewCeoServiceData?.singles?.goal?.percentage, yesterdayViewCeoServiceData?.singles?.goal?.value)
            val singleServiceVarianceCEOYesterday= Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.singles?.variance?.amount, yesterdayViewCeoServiceData?.singles?.variance?.percentage, yesterdayViewCeoServiceData?.singles?.variance?.value)
            val singleServiceActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.singles?.actual?.amount, yesterdayViewCeoServiceData?.singles?.actual?.percentage, yesterdayViewCeoServiceData?.singles?.actual?.value)
            singleData(singleGoalCEOYesterday,singleServiceVarianceCEOYesterday,singleServiceActualCEOYesterday)

            // double
            val doubleServiceActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.singles?._doubles?.actual?.amount, yesterdayViewCeoServiceData?.singles?._doubles?.actual?.percentage, yesterdayViewCeoServiceData?.singles?._doubles?.actual?.value)
            if(doubleServiceActualCEOYesterday.isEmpty()){
                
                double_percentage_error.visibility = View.VISIBLE
                double_percentage.visibility = View.GONE
            }else{
                double_percentage.text =  doubleServiceActualCEOYesterday
            }

            if (yesterdayViewCeoServiceData?.singles?._doubles?.status != null) {

                when {
                    yesterdayViewCeoServiceData.singles._doubles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.singles._doubles.status.toString() == resources.getString(R.string.under_limit) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // triple
            val tripleServiceActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.singles?.triples?.actual?.amount, yesterdayViewCeoServiceData?.singles?.triples?.actual?.percentage, yesterdayViewCeoServiceData?.singles?.triples?.actual?.value)
            if(tripleServiceActualCEOYesterday.isEmpty()){
                
                triple_percentage_error.visibility = View.VISIBLE
                triple_percentage.visibility = View.GONE
            }else{
                triple_percentage.text =  tripleServiceActualCEOYesterday
            }

            if (yesterdayViewCeoServiceData?.singles?.triples?.status != null) {
                when {
                    yesterdayViewCeoServiceData.singles.triples.status.toString() == resources.getString(R.string.out_of_range) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.singles.triples.status.toString() == resources.getString(R.string.under_limit) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //Load time
            val loadTimeGoalCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.loadTime?.goal?.amount, yesterdayViewCeoServiceData?.loadTime?.goal?.percentage, yesterdayViewCeoServiceData?.loadTime?.goal?.value)
            val loadTimeServiceVarianceCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.loadTime?.variance?.amount, yesterdayViewCeoServiceData?.loadTime?.variance?.percentage, yesterdayViewCeoServiceData?.loadTime?.variance?.value)
            val loadTimeServiceActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.loadTime?.actual?.amount, yesterdayViewCeoServiceData?.loadTime?.actual?.percentage, yesterdayViewCeoServiceData?.loadTime?.actual?.value)
            loadTimeData(loadTimeGoalCEOYesterday,loadTimeServiceVarianceCEOYesterday,loadTimeServiceActualCEOYesterday)

            if ( yesterdayViewCeoServiceData?.loadTime?.status != null) {
                when {
                    yesterdayViewCeoServiceData.loadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.loadTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

             //carry_out_load_time_percentage
            val carryOutServiceActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.loadTime?.carryoutLoadTime?.actual?.amount, yesterdayViewCeoServiceData?.loadTime?.carryoutLoadTime?.actual?.percentage, yesterdayViewCeoServiceData?.loadTime?.carryoutLoadTime?.actual?.value)
            if(carryOutServiceActualCEOYesterday.isEmpty()){
                
                carry_out_laod_time_percentage_error.visibility = View.VISIBLE
                carry_out_laod_time_percentage.visibility = View.GONE
            }else{
                carry_out_laod_time_percentage.text =  carryOutServiceActualCEOYesterday
            }

            if ( yesterdayViewCeoServiceData?.loadTime?.carryoutLoadTime?.status != null) {
                when {
                    yesterdayViewCeoServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //wait time

            if (yesterdayViewCeoServiceData?.waitTime?.status != null) {

                when {
                    yesterdayViewCeoServiceData.waitTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.waitTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val waitTimeGoalCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.waitTime?.goal?.amount, yesterdayViewCeoServiceData?.waitTime?.goal?.percentage, yesterdayViewCeoServiceData?.waitTime?.goal?.value)
            val waitTimeServiceVarianceCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.waitTime?.variance?.amount, yesterdayViewCeoServiceData?.waitTime?.variance?.percentage, yesterdayViewCeoServiceData?.waitTime?.variance?.value)
            val waitTimeServiceActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.waitTime?.actual?.amount, yesterdayViewCeoServiceData?.waitTime?.actual?.percentage, yesterdayViewCeoServiceData?.waitTime?.actual?.value)
            waitTimeData(waitTimeGoalCEOYesterday,waitTimeServiceVarianceCEOYesterday,waitTimeServiceActualCEOYesterday)

            // OTD
            val otdGoalCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.otd?.goal?.amount, yesterdayViewCeoServiceData?.otd?.goal?.percentage, yesterdayViewCeoServiceData?.otd?.goal?.value)
            val otdServiceVarianceCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.otd?.variance?.amount, yesterdayViewCeoServiceData?.otd?.variance?.percentage, yesterdayViewCeoServiceData?.otd?.variance?.value)
            val otdServiceActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.otd?.actual?.amount, yesterdayViewCeoServiceData?.otd?.actual?.percentage, yesterdayViewCeoServiceData?.otd?.actual?.value)
            otdData(otdGoalCEOYesterday,otdServiceVarianceCEOYesterday,otdServiceActualCEOYesterday)

            if (yesterdayViewCeoServiceData?.otd?.status != null) {
                when {
                    yesterdayViewCeoServiceData.otd.status.toString() == resources.getString(R.string.out_of_range) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.otd.status.toString() == resources.getString(R.string.under_limit) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // AOT
            if ( yesterdayViewCeoServiceData?.aot?.status != null) {
                when {
                    yesterdayViewCeoServiceData.aot.status.toString() == resources.getString(R.string.out_of_range) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.aot.status.toString() == resources.getString(R.string.under_limit) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val aotGoalCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.aot?.goal?.amount, yesterdayViewCeoServiceData?.aot?.goal?.percentage, yesterdayViewCeoServiceData?.aot?.goal?.value)
            val aotServiceVarianceCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.aot?.variance?.amount, yesterdayViewCeoServiceData?.aot?.variance?.percentage, yesterdayViewCeoServiceData?.aot?.variance?.value)
            val aotServiceActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.aot?.actual?.amount, yesterdayViewCeoServiceData?.aot?.actual?.percentage, yesterdayViewCeoServiceData?.aot?.actual?.value)
            aotData(aotGoalCEOYesterday,aotServiceVarianceCEOYesterday,aotServiceActualCEOYesterday)

            //HangUp Service
            val hangUpServiceActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.aot?.hangUps?.actual?.amount, yesterdayViewCeoServiceData?.aot?.hangUps?.actual?.percentage, yesterdayViewCeoServiceData?.aot?.hangUps?.actual?.value)
            if(hangUpServiceActualCEOYesterday.isEmpty()){
                hang_up_percentage_error.visibility = View.VISIBLE
                hang_up_percentage.visibility = View.GONE
            }else{
                hang_up_percentage.text =  hangUpServiceActualCEOYesterday
            }


            // Out the door
            if ( yesterdayViewCeoServiceData?.outTheDoor?.status != null) {

                when {
                    yesterdayViewCeoServiceData.outTheDoor.status.toString() == resources.getString(R.string.out_of_range) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.outTheDoor.status.toString() == resources.getString(R.string.under_limit) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.green))
                    } else-> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val outDoorGoalCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.outTheDoor?.goal?.amount, yesterdayViewCeoServiceData?.outTheDoor?.goal?.percentage, yesterdayViewCeoServiceData?.outTheDoor?.goal?.value)
            val outDoorServiceVarianceCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.outTheDoor?.variance?.amount, yesterdayViewCeoServiceData?.outTheDoor?.variance?.percentage, yesterdayViewCeoServiceData?.outTheDoor?.variance?.value)
            val outDoorServiceActualCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.outTheDoor?.actual?.amount, yesterdayViewCeoServiceData?.outTheDoor?.actual?.percentage, yesterdayViewCeoServiceData?.outTheDoor?.actual?.value)
            outDoorData(outDoorGoalCEOYesterday,outDoorServiceVarianceCEOYesterday,outDoorServiceActualCEOYesterday)

            // csat
            val csatCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.total?.actual?.amount, yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.total?.actual?.percentage, yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.total?.actual?.value)
            if(csatCEOYesterday.isEmpty()){
                
                csat_goal_error.visibility = View.VISIBLE
                csat_goal.visibility = View.GONE
            }else{
                csat_goal.text =  csatCEOYesterday
            }
            val weeklyCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.amount, yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.percentage, yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.value)
            if(weeklyCEOYesterday.isEmpty()){
                
                weekly_percentage_error.visibility = View.VISIBLE
                weekly_percentage.visibility = View.GONE
            }else{
                weekly_percentage.text =  weeklyCEOYesterday
            }
            val ccCEOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.amount, yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.percentage, yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.value)
            if(ccCEOYesterday.isEmpty()){
                
                cc_per_100_percentage_error.visibility = View.VISIBLE
                cc_per_100_percentage.visibility = View.GONE
            }else{
                cc_per_100_percentage.text =  ccCEOYesterday
            }

            csat_display.text =
                yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.total?.displayName
                    ?: getString(R.string.csat_text)
            weekly_text.text =
                yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName
                    ?: getString(R.string.weekly_text)
            cc_per_100_text.text =
                yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName
                    ?: getString(
                        R.string.cc_per_100_text)

            if ( yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.total?.status != null) {
                when {
                    yesterdayViewCeoServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.green))
                    } else -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            if ( yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.status != null) {

                when {
                    yesterdayViewCeoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if ( yesterdayViewCeoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.status != null) {
                when {
                    yesterdayViewCeoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewCeoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
        }catch (e:Exception){
            Logger.error(e.message.toString(),"Service Overview Yesterday KPI")
        }

    }

    private fun rangeOverViewCEOService(serviceTodayDetail: CEOOverviewRangeQuery.Ceo) {
        try{

            val rangeOverViewCEOServiceData = serviceTodayDetail.kpis?.supervisors?.stores?.period?.service

            Logger.info("Service Period Range", "Service Overview KPI")

            // display name

            Validation().checkNullValueToShowView(this, rangeOverViewCEOServiceData?.aot?.displayName, aot_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCEOServiceData?.aot?.hangUps?.displayName, hang_up_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCEOServiceData?.aot?.hangUps?.displayName, hang_up_parent)

            Validation().checkNullValueToShowView(this, rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.total?.displayName, csat_meter_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName, weekly_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName, cc_per_100_text_parent)

            showServiceNarrativeData(rangeOverViewCEOServiceData?.narrative.toString())

            if(rangeOverViewCEOServiceData?.aot!= null){
                aot_view.visibility= View.VISIBLE
            }else{
                aot_view.visibility= View.GONE
            }
            out_the_door_view.visibility= View.GONE
            csat_view.visibility= View.GONE

            leg_time_text.text = rangeOverViewCEOServiceData?.eADT?.legTime?.displayName ?: ""
            delivery_order_count.text = rangeOverViewCEOServiceData?.extremeDelivery?.deliveryOrderCount?.displayName ?: ""
            extreme_delivery_oc_text.text = rangeOverViewCEOServiceData?.extremeDelivery?.extremeDeliveryCount?.displayName ?: ""
            double_text.text = rangeOverViewCEOServiceData?.singles?._doubles?.displayName ?: ""
            triples_text.text = rangeOverViewCEOServiceData?.singles?.triples?.displayName ?: ""
            carry_out_laod_time_text.text = rangeOverViewCEOServiceData?.loadTime?.carryoutLoadTime?.displayName ?: ""
            hang_up_text.text = rangeOverViewCEOServiceData?.aot?.hangUps?.displayName ?: ""

            // display headers
            eadt_display.text = rangeOverViewCEOServiceData?.eADT?.displayName ?: ""
            extreme_delivery_display.text = rangeOverViewCEOServiceData?.extremeDelivery?.displayName ?: ""
            single_display.text = rangeOverViewCEOServiceData?.singles?.displayName ?: ""
            load_time_display.text = rangeOverViewCEOServiceData?.loadTime?.displayName ?: ""
            wait_time_display.text = rangeOverViewCEOServiceData?.waitTime?.displayName ?: ""
            otd_display.text = rangeOverViewCEOServiceData?.otd?.displayName ?: ""
            aot_display.text = rangeOverViewCEOServiceData?.aot?.displayName ?: ""
            out_the_door_display.text = rangeOverViewCEOServiceData?.outTheDoor?.displayName ?: ""

            service_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (service_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.service_text)
                        }
                        y = service_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            if ( rangeOverViewCEOServiceData?.eADT?.status != null) {
                when {
                    rangeOverViewCEOServiceData.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.green))

                    } else-> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val eatServiceGoalCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.eADT?.goal?.amount, rangeOverViewCEOServiceData?.eADT?.goal?.percentage, rangeOverViewCEOServiceData?.eADT?.goal?.value)
            val eatServiceVarianceCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.eADT?.variance?.amount, rangeOverViewCEOServiceData?.eADT?.variance?.percentage, rangeOverViewCEOServiceData?.eADT?.variance?.value)
            val eatServiceActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.eADT?.actual?.amount, rangeOverViewCEOServiceData?.eADT?.actual?.percentage, rangeOverViewCEOServiceData?.eADT?.actual?.value)
            eatData(eatServiceGoalCEORange,eatServiceVarianceCEORange,eatServiceActualCEORange)

            // leg time
            val legTimeServiceActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.eADT?.legTime?.actual?.amount, rangeOverViewCEOServiceData?.eADT?.legTime?.actual?.percentage, rangeOverViewCEOServiceData?.eADT?.legTime?.actual?.value)
            if(legTimeServiceActualCEORange.isEmpty()){
                
                leg_time_error.visibility = View.VISIBLE
                leg_time_actual.visibility = View.GONE
            }else{
                leg_time_actual.text =  legTimeServiceActualCEORange
            }

            if (rangeOverViewCEOServiceData?.eADT?.legTime?.status != null) {
                when {
                    rangeOverViewCEOServiceData.eADT.legTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.eADT.legTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (rangeOverViewCEOServiceData?.extremeDelivery?.status != null) {
                when {
                    rangeOverViewCEOServiceData.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val extremeDeliveryServiceGoalCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.extremeDelivery?.goal?.amount, rangeOverViewCEOServiceData?.extremeDelivery?.goal?.percentage, rangeOverViewCEOServiceData?.extremeDelivery?.goal?.value)
            val extremeDeliveryServiceVarianceCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.extremeDelivery?.variance?.amount, rangeOverViewCEOServiceData?.extremeDelivery?.variance?.percentage, rangeOverViewCEOServiceData?.extremeDelivery?.variance?.value)
            val extremeDeliveryServiceActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.extremeDelivery?.actual?.amount, rangeOverViewCEOServiceData?.extremeDelivery?.actual?.percentage, rangeOverViewCEOServiceData?.extremeDelivery?.actual?.value)
            extremeDeliveryData(extremeDeliveryServiceGoalCEORange,extremeDeliveryServiceVarianceCEORange,extremeDeliveryServiceActualCEORange)


            // delivery   order count
            val deliveryCountActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.amount, rangeOverViewCEOServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.percentage, rangeOverViewCEOServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.value)
            if(deliveryCountActualCEORange.isEmpty()){
                
                delivery_order_error.visibility = View.VISIBLE
                delivery_order_actual.visibility = View.GONE
            }else{
                delivery_order_actual.text =  deliveryCountActualCEORange
            }

            if (rangeOverViewCEOServiceData?.extremeDelivery?.deliveryOrderCount?.status != null) {

                when {
                    rangeOverViewCEOServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // extreme_delivery_oc_actual
            val extremeDeliveryCountActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.amount, rangeOverViewCEOServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.percentage, rangeOverViewCEOServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.value)
            if(extremeDeliveryCountActualCEORange.isEmpty()){
                
                extreme_delivery_oc_actual_error.visibility = View.VISIBLE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
            }else{
                extreme_delivery_oc_actual.text =  extremeDeliveryCountActualCEORange
            }

            if (rangeOverViewCEOServiceData?.extremeDelivery?.extremeDeliveryCount?.status != null) {
                when {
                    rangeOverViewCEOServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // Singles
            if ( rangeOverViewCEOServiceData?.singles?.status != null) {
                when {
                    rangeOverViewCEOServiceData.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val singleGoalCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.singles?.goal?.amount, rangeOverViewCEOServiceData?.singles?.goal?.percentage, rangeOverViewCEOServiceData?.singles?.goal?.value)
            val singleServiceVarianceCEORange= Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.singles?.variance?.amount, rangeOverViewCEOServiceData?.singles?.variance?.percentage, rangeOverViewCEOServiceData?.singles?.variance?.value)
            val singleServiceActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.singles?.actual?.amount, rangeOverViewCEOServiceData?.singles?.actual?.percentage, rangeOverViewCEOServiceData?.singles?.actual?.value)
            singleData(singleGoalCEORange,singleServiceVarianceCEORange,singleServiceActualCEORange)

            // double
            val doubleServiceActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.singles?._doubles?.actual?.amount, rangeOverViewCEOServiceData?.singles?._doubles?.actual?.percentage, rangeOverViewCEOServiceData?.singles?._doubles?.actual?.value)
            if(doubleServiceActualCEORange.isEmpty()){
                
                double_percentage_error.visibility = View.VISIBLE
                double_percentage.visibility = View.GONE
            }else{
                double_percentage.text =  doubleServiceActualCEORange
            }

            if (rangeOverViewCEOServiceData?.singles?._doubles?.status != null) {

                when {
                    rangeOverViewCEOServiceData.singles._doubles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.singles._doubles.status.toString() == resources.getString(R.string.under_limit) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // triple
            val tripleServiceActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.singles?.triples?.actual?.amount, rangeOverViewCEOServiceData?.singles?.triples?.actual?.percentage, rangeOverViewCEOServiceData?.singles?.triples?.actual?.value)
            if(tripleServiceActualCEORange.isEmpty()){
                
                triple_percentage_error.visibility = View.VISIBLE
                triple_percentage.visibility = View.GONE
            }else{
                triple_percentage.text =  tripleServiceActualCEORange
            }

            if (rangeOverViewCEOServiceData?.singles?.triples?.status != null) {
                when {
                    rangeOverViewCEOServiceData.singles.triples.status.toString() == resources.getString(R.string.out_of_range) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.singles.triples.status.toString() == resources.getString(R.string.under_limit) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //Load time
            val loadTimeGoalCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.loadTime?.goal?.amount, rangeOverViewCEOServiceData?.loadTime?.goal?.percentage, rangeOverViewCEOServiceData?.loadTime?.goal?.value)
            val loadTimeServiceVarianceCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.loadTime?.variance?.amount, rangeOverViewCEOServiceData?.loadTime?.variance?.percentage, rangeOverViewCEOServiceData?.loadTime?.variance?.value)
            val loadTimeServiceActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.loadTime?.actual?.amount, rangeOverViewCEOServiceData?.loadTime?.actual?.percentage, rangeOverViewCEOServiceData?.loadTime?.actual?.value)
            loadTimeData(loadTimeGoalCEORange,loadTimeServiceVarianceCEORange,loadTimeServiceActualCEORange)

            if ( rangeOverViewCEOServiceData?.loadTime?.status != null) {
                when {
                    rangeOverViewCEOServiceData.loadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.loadTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

             //carry_out_load_time_percentage
            val carryOutServiceActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.loadTime?.carryoutLoadTime?.actual?.amount, rangeOverViewCEOServiceData?.loadTime?.carryoutLoadTime?.actual?.percentage, rangeOverViewCEOServiceData?.loadTime?.carryoutLoadTime?.actual?.value)
            if(carryOutServiceActualCEORange.isEmpty()){
                
                carry_out_laod_time_percentage_error.visibility = View.VISIBLE
                carry_out_laod_time_percentage.visibility = View.GONE
            }else{
                carry_out_laod_time_percentage.text =  carryOutServiceActualCEORange
            }

            if ( rangeOverViewCEOServiceData?.loadTime?.carryoutLoadTime?.status != null) {
                when {
                    rangeOverViewCEOServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //wait time

            if (rangeOverViewCEOServiceData?.waitTime?.status != null) {

                when {
                    rangeOverViewCEOServiceData.waitTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.waitTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val waitTimeGoalCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.waitTime?.goal?.amount, rangeOverViewCEOServiceData?.waitTime?.goal?.percentage, rangeOverViewCEOServiceData?.waitTime?.goal?.value)
            val waitTimeServiceVarianceCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.waitTime?.variance?.amount, rangeOverViewCEOServiceData?.waitTime?.variance?.percentage, rangeOverViewCEOServiceData?.waitTime?.variance?.value)
            val waitTimeServiceActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.waitTime?.actual?.amount, rangeOverViewCEOServiceData?.waitTime?.actual?.percentage, rangeOverViewCEOServiceData?.waitTime?.actual?.value)
            waitTimeData(waitTimeGoalCEORange,waitTimeServiceVarianceCEORange,waitTimeServiceActualCEORange)

            // OTD
            val otdGoalCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.otd?.goal?.amount, rangeOverViewCEOServiceData?.otd?.goal?.percentage, rangeOverViewCEOServiceData?.otd?.goal?.value)
            val otdServiceVarianceCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.otd?.variance?.amount, rangeOverViewCEOServiceData?.otd?.variance?.percentage, rangeOverViewCEOServiceData?.otd?.variance?.value)
            val otdServiceActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.otd?.actual?.amount, rangeOverViewCEOServiceData?.otd?.actual?.percentage, rangeOverViewCEOServiceData?.otd?.actual?.value)
            otdData(otdGoalCEORange,otdServiceVarianceCEORange,otdServiceActualCEORange)

            if (rangeOverViewCEOServiceData?.otd?.status != null) {
                when {
                    rangeOverViewCEOServiceData.otd.status.toString() == resources.getString(R.string.out_of_range) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.otd.status.toString() == resources.getString(R.string.under_limit) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // AOT
            if ( rangeOverViewCEOServiceData?.aot?.status != null) {
                when {
                    rangeOverViewCEOServiceData.aot.status.toString() == resources.getString(R.string.out_of_range) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.aot.status.toString() == resources.getString(R.string.under_limit) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val aotGoalCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.aot?.goal?.amount, rangeOverViewCEOServiceData?.aot?.goal?.percentage, rangeOverViewCEOServiceData?.aot?.goal?.value)
            val aotServiceVarianceCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.aot?.variance?.amount, rangeOverViewCEOServiceData?.aot?.variance?.percentage, rangeOverViewCEOServiceData?.aot?.variance?.value)
            val aotServiceActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.aot?.actual?.amount, rangeOverViewCEOServiceData?.aot?.actual?.percentage, rangeOverViewCEOServiceData?.aot?.actual?.value)
            aotData(aotGoalCEORange,aotServiceVarianceCEORange,aotServiceActualCEORange)

            //HangUp Service
            val hangUpServiceActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.aot?.hangUps?.actual?.amount, rangeOverViewCEOServiceData?.aot?.hangUps?.actual?.percentage, rangeOverViewCEOServiceData?.aot?.hangUps?.actual?.value)
            if(hangUpServiceActualCEORange.isEmpty()){
                
                hang_up_percentage_error.visibility = View.VISIBLE
                hang_up_percentage.visibility = View.GONE
            }else{
                hang_up_percentage.text =  hangUpServiceActualCEORange
            }


            // Out the door
            if ( rangeOverViewCEOServiceData?.outTheDoor?.status != null) {

                when {
                    rangeOverViewCEOServiceData.outTheDoor.status.toString() == resources.getString(R.string.out_of_range) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.outTheDoor.status.toString() == resources.getString(R.string.under_limit) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val outDoorGoalCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.outTheDoor?.goal?.amount, rangeOverViewCEOServiceData?.outTheDoor?.goal?.percentage, rangeOverViewCEOServiceData?.outTheDoor?.goal?.value)
            val outDoorServiceVarianceCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.outTheDoor?.variance?.amount, rangeOverViewCEOServiceData?.outTheDoor?.variance?.percentage, rangeOverViewCEOServiceData?.outTheDoor?.variance?.value)
            val outDoorServiceActualCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.outTheDoor?.actual?.amount, rangeOverViewCEOServiceData?.outTheDoor?.actual?.percentage, rangeOverViewCEOServiceData?.outTheDoor?.actual?.value)
            outDoorData(outDoorGoalCEORange,outDoorServiceVarianceCEORange,outDoorServiceActualCEORange)

            val csatCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.total?.actual?.amount, rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.total?.actual?.percentage, rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.total?.actual?.value)
            if(csatCEORange.isEmpty()){
                
                csat_goal_error.visibility = View.VISIBLE
                csat_goal.visibility = View.GONE
            }else{
                csat_goal.text =  csatCEORange
            }
            val weeklyCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.amount, rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.percentage, rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.value)
            if(weeklyCEORange.isEmpty()){
                
                weekly_percentage_error.visibility = View.VISIBLE
                weekly_percentage.visibility = View.GONE
            }else{
                weekly_percentage.text =  weeklyCEORange
            }
            val ccCEORange = Validation().checkAmountPercentageValue(this, rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.amount, rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.percentage, rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.value)
            if(ccCEORange.isEmpty()){
                
                cc_per_100_percentage_error.visibility = View.VISIBLE
                cc_per_100_percentage.visibility = View.GONE
            }else{
                cc_per_100_percentage.text =  ccCEORange
            }

            csat_display.text =
                rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.total?.displayName
                    ?: getString(R.string.csat_text)
            weekly_text.text =
                rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName
                    ?: getString(R.string.weekly_text)
            cc_per_100_text.text =
                rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName
                    ?: getString(
                        R.string.cc_per_100_text)

            if ( rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.total?.status != null) {
                when {
                    rangeOverViewCEOServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.green))
                    } else -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            if ( rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.status != null) {

                when {
                    rangeOverViewCEOServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if ( rangeOverViewCEOServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.status != null) {
                when {
                    rangeOverViewCEOServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
        }catch (e:Exception){
            Logger.error(e.message.toString(),"Service Overview Period Range KPI")
        }

    }

    // do overview

    private fun todayViewDO(serviceTodayDetail: DOOverviewTodayQuery.Do_) {
        val todayViewDoServiceData = serviceTodayDetail.kpis?.supervisors?.stores?.today?.service

        Logger.info("Service Today", "Service Overview KPI")

        // display name
        service_text_small.text = todayViewDoServiceData!!.displayName ?: ""
        leg_time_text.text = todayViewDoServiceData.eADT!!.legTime!!.displayName ?: ""
        delivery_order_count.text = todayViewDoServiceData.extremeDelivery!!.deliveryOrderCount!!.displayName ?: ""
        extreme_delivery_oc_text.text = todayViewDoServiceData.extremeDelivery.extremeDeliveryCount!!.displayName ?: ""
        double_text.text = todayViewDoServiceData.singles!!._doubles!!.displayName ?: ""
        triples_text.text = todayViewDoServiceData.singles.triples!!.displayName ?: ""
        carry_out_laod_time_text.text = todayViewDoServiceData.loadTime!!.carryoutLoadTime!!.displayName ?: ""
        hang_up_text.text = todayViewDoServiceData.aot!!.hangUps!!.displayName ?: ""
        weekly_text.text = todayViewDoServiceData.csatMeterRating90dayAvg!!.weeklyCCCClosureRate!!.displayName ?: ""
        cc_per_100_text.text = todayViewDoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg!!.displayName ?: ""
        eadt_display.text = todayViewDoServiceData.eADT.displayName ?: ""
        extreme_delivery_display.text = todayViewDoServiceData.extremeDelivery.displayName ?: ""
        single_display.text = todayViewDoServiceData.singles.displayName ?: ""
        load_time_display.text = todayViewDoServiceData.loadTime.displayName ?: ""
        wait_time_display.text = todayViewDoServiceData.waitTime?.displayName ?: ""
        otd_display.text = todayViewDoServiceData.otd?.displayName ?: ""
        aot_display.text = todayViewDoServiceData.aot.displayName ?: ""
        out_the_door_display.text = todayViewDoServiceData.outTheDoor?.displayName ?: ""


        if (todayViewDoServiceData.actual?.value?.isNaN() == false && todayViewDoServiceData.status != null) {
            service_sales.text = Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.actual.value)
            when {
                todayViewDoServiceData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_sales.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_sales.setTextColor(getColor(R.color.green))
                } else -> {
                    service_sales.setTextColor(getColor(R.color.text_color))
                }
            }
        }



        service_scroll_parent.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 0f
                override fun onScrollChanged() {
                    if (service_scroll_parent.scrollY > y) {
                        parent_data_on_scroll_linear.visibility = View.VISIBLE
                        parent_data_on_scroll_view.visibility = View.VISIBLE
                        level_two_scroll_data_action.text =
                            todayViewDoServiceData.displayName ?: getString(R.string.service_text_small)

                        if (todayViewDoServiceData.actual?.value?.isNaN() == false && todayViewDoServiceData.status != null) {
                            level_two_scroll_data_action_value.text =
                                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.goal!!.value)
                            when {
                                todayViewDoServiceData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.red_circle,
                                        0
                                    )
                                }
                                todayViewDoServiceData.status.toString() == resources.getString(R.string.under_limit) -> {
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
                    y = service_scroll_parent.scrollY.toFloat()
                    if (y <= 0) {
                        parent_data_on_scroll_linear.visibility = View.INVISIBLE
                        parent_data_on_scroll_view.visibility = View.GONE

                    }
                }
            })


        service_goal_value.text =
            if (todayViewDoServiceData.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.goal.value) else ""
        service_variance_value.text =
            if (todayViewDoServiceData.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.variance.value) else ""
        showServiceNarrativeData(todayViewDoServiceData.narrative.toString())



        if (todayViewDoServiceData.eADT.actual?.value?.isNaN() == false && todayViewDoServiceData.eADT.status != null) {
            eadt_actual.text = Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.eADT.actual.value)
            when {
                todayViewDoServiceData.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    eadt_actual.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    eadt_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    eadt_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        eadt_goal.text =
            if (todayViewDoServiceData.eADT.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.eADT.goal.value) else ""
        eadt_variance.text =
            if (todayViewDoServiceData.eADT.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.eADT.variance.value) else ""
        // leg time
        if (todayViewDoServiceData.eADT.legTime?.actual?.value?.isNaN() == false && todayViewDoServiceData.eADT.legTime.status != null) {
            leg_time_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.eADT.legTime.actual.value)
            when {
                todayViewDoServiceData.eADT.legTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    leg_time_actual.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.eADT.legTime.status.toString() == resources.getString(R.string.under_limit) -> {
                    leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    leg_time_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    leg_time_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        leg_time_goal.text =
            if (todayViewDoServiceData.eADT.legTime?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.eADT.legTime.goal.value) else ""
        leg_time_variance.text =
            if (todayViewDoServiceData.eADT.legTime?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.eADT.legTime.variance.value) else ""


        // extreme_delivery
        if (todayViewDoServiceData.extremeDelivery.actual?.value?.isNaN() == false && todayViewDoServiceData.extremeDelivery.status != null) {
            extreme_delivery_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.extremeDelivery.actual.value)

            when {
                todayViewDoServiceData.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    extreme_delivery_actual.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    extreme_delivery_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    extreme_delivery_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        extreme_delivery_goal.text =
            if (todayViewDoServiceData.extremeDelivery.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.extremeDelivery.goal.value)
            else ""
        extreme_delivery_variance.text =
            if (todayViewDoServiceData.extremeDelivery.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.extremeDelivery.variance.value)
            else ""

        // delivery   order count
        if (todayViewDoServiceData.extremeDelivery.deliveryOrderCount?.actual?.value?.isNaN() == false && todayViewDoServiceData.extremeDelivery.deliveryOrderCount.status != null) {
            delivery_order_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.extremeDelivery.deliveryOrderCount.actual.value)
            when {
                todayViewDoServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    delivery_order_actual.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    delivery_order_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    delivery_order_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        // extreme_delivery_oc_actual
        if (todayViewDoServiceData.extremeDelivery.extremeDeliveryCount.actual?.value?.isNaN() == false && todayViewDoServiceData.extremeDelivery.extremeDeliveryCount.status != null) {
            extreme_delivery_oc_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.extremeDelivery.extremeDeliveryCount.actual.value)
            when {
                todayViewDoServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    extreme_delivery_oc_actual.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    extreme_delivery_oc_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    extreme_delivery_oc_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }


        // Singles
        if (todayViewDoServiceData.singles.actual?.percentage?.isNaN() == false && todayViewDoServiceData.singles.status != null) {
            single_percentage_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.singles.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                todayViewDoServiceData.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    single_percentage_actual.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    single_percentage_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    single_percentage_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        single_percentage_goal.text =
            if (todayViewDoServiceData.singles.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.singles.goal.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        single_percentage_variance.text =
            if (todayViewDoServiceData.singles.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.singles.variance.percentage)
                .plus(getString(R.string.percentage_text)) else ""

        // double
        if (todayViewDoServiceData.singles._doubles?.actual?.percentage?.isNaN() == false && todayViewDoServiceData.singles._doubles.status != null) {
            double_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.singles._doubles.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                todayViewDoServiceData.singles._doubles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    double_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.singles._doubles.status.toString() == resources.getString(R.string.under_limit) -> {
                    double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    double_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    double_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        // triple
        if (todayViewDoServiceData.singles.triples.actual?.percentage?.isNaN() == false && todayViewDoServiceData.singles.triples.status != null) {
            triple_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.singles.triples.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                todayViewDoServiceData.singles.triples.status.toString() == resources.getString(R.string.out_of_range) -> {
                    triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    triple_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.singles.triples.status.toString() == resources.getString(R.string.under_limit) -> {
                    triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    triple_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    triple_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        //Load time
        if (todayViewDoServiceData.loadTime.goal?.value?.isNaN() == false) {
            load_time_goal.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.loadTime.goal.value)
        }
        if (todayViewDoServiceData.loadTime.variance?.value?.isNaN() == false) {
            load_time_variance.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.loadTime.variance.value)
        }
        if (todayViewDoServiceData.loadTime.actual?.value?.isNaN() == false && todayViewDoServiceData.loadTime.status != null) {
            load_time_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.loadTime.actual.value)
            when {
                todayViewDoServiceData.loadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    load_time_actual.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.loadTime.status.toString() == resources.getString(R.string.under_limit) -> {
                    load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    load_time_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    load_time_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }

         //carry_out_load_time_percentage

        if (todayViewDoServiceData.loadTime.carryoutLoadTime?.actual?.value?.isNaN() == false && todayViewDoServiceData.loadTime.carryoutLoadTime.status != null) {
            carry_out_laod_time_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.loadTime.carryoutLoadTime.actual.value)
            when {
                todayViewDoServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    carry_out_laod_time_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    carry_out_laod_time_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    carry_out_laod_time_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        //wait time
        if (todayViewDoServiceData.waitTime?.actual?.value?.isNaN() == false && todayViewDoServiceData.waitTime.status != null) {
            wait_time_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.waitTime.actual.value)
            when {
                todayViewDoServiceData.waitTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    wait_time_actual.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.waitTime.status.toString() == resources.getString(R.string.under_limit) -> {
                    wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    wait_time_actual.setTextColor(getColor(R.color.green))
                } else-> {
                    wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    wait_time_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        wait_time_goal.text =
            if (todayViewDoServiceData.waitTime?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.waitTime.goal.value) else ""
        wait_time_variance.text =
            if (todayViewDoServiceData.waitTime?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.waitTime.variance.value) else ""

        // OTD
        if (todayViewDoServiceData.otd?.actual?.value?.isNaN() == false && todayViewDoServiceData.otd.status != null) {
            otd_actual.text = Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.otd.actual.value)
            when {
                todayViewDoServiceData.otd.status.toString() == resources.getString(R.string.out_of_range) -> {
                    otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    otd_actual.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.otd.status.toString() == resources.getString(R.string.under_limit) -> {
                    otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    otd_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    otd_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        otd_goal.text =
            if (todayViewDoServiceData.otd?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.otd.goal.value) else ""
        otd_variance.text =
            if (todayViewDoServiceData.otd?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.otd.variance.value) else ""

        // AOT
        if (todayViewDoServiceData.aot.actual?.percentage?.isNaN() == false && todayViewDoServiceData.aot.status != null) {
            aot_actual.text = Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.aot.actual.percentage)
                .plus(getString(R.string.percentage_text))
            when {
                todayViewDoServiceData.aot.status.toString() == resources.getString(R.string.out_of_range) -> {
                    aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    aot_actual.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.aot.status.toString() == resources.getString(R.string.under_limit) -> {
                    aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    aot_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    aot_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        aot_goal.text =
            if (todayViewDoServiceData.aot.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.aot.goal.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        aot_variance.text =
            if (todayViewDoServiceData.aot.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.aot.variance.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        hang_up_percentage.text =
            if (todayViewDoServiceData.aot.hangUps?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.aot.hangUps.actual.value) else ""


        // Out the door
        if (todayViewDoServiceData.outTheDoor?.actual?.value?.isNaN() == false && todayViewDoServiceData.outTheDoor.status != null) {
            out_door_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.outTheDoor.actual.value)
            when {
                todayViewDoServiceData.outTheDoor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    out_door_actual.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.outTheDoor.status.toString() == resources.getString(R.string.under_limit) -> {
                    out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    out_door_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    out_door_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        out_door_goal.text =
            if (todayViewDoServiceData.outTheDoor?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.outTheDoor.goal.value) else ""
        out_door_variance.text =
            if (todayViewDoServiceData.outTheDoor?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewDoServiceData.outTheDoor.variance.value) else ""


        // csat

        if (todayViewDoServiceData.csatMeterRating90dayAvg.total?.actual?.value?.isNaN() == false && todayViewDoServiceData.csatMeterRating90dayAvg.total.status != null) {
            csat_goal.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.csatMeterRating90dayAvg.total.actual.value)
            when {
                todayViewDoServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    csat_goal.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    csat_goal.setTextColor(getColor(R.color.green))
                } else -> {
                    csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    csat_goal.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        if (todayViewDoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate?.actual?.value?.isNaN() == false && todayViewDoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status != null) {
            weekly_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.actual.value)
            when {
                todayViewDoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    weekly_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    weekly_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    weekly_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        if (todayViewDoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.actual?.value?.isNaN() == false && todayViewDoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status != null) {
            cc_per_100_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewDoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.actual.value)
            when {
                todayViewDoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    cc_per_100_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewDoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    cc_per_100_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    cc_per_100_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }

    }

    private fun yesterdayViewDOService(serviceTodayDetail: DOOverviewYesterdayQuery.Do_) {
        try{

            val yesterdayViewDoServiceData = serviceTodayDetail.kpis?.supervisors?.stores?.yesterday?.service

            Logger.info("Service Yesterday", "Service Overview KPI")

            // display name
            /*  service_text_small.text =
                  yesterdayViewDoServiceData?.displayName ?: getString(R.string.service_text_small)*/
            Validation().checkNullValueToShowView(this, yesterdayViewDoServiceData?.aot?.displayName, aot_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoServiceData?.aot?.hangUps?.displayName, hang_up_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoServiceData?.aot?.hangUps?.displayName, hang_up_parent)

            Validation().checkNullValueToShowView(this, yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.total?.displayName, csat_meter_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName, weekly_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName, cc_per_100_text_parent)

            showServiceNarrativeData(yesterdayViewDoServiceData?.narrative.toString())

            if(yesterdayViewDoServiceData?.aot!= null){
                aot_view.visibility= View.VISIBLE
            }else{
                aot_view.visibility= View.GONE
            }

            out_the_door_view.visibility= View.GONE
            csat_view.visibility= View.GONE

            leg_time_text.text = yesterdayViewDoServiceData?.eADT?.legTime?.displayName ?: ""
            delivery_order_count.text = yesterdayViewDoServiceData?.extremeDelivery?.deliveryOrderCount?.displayName ?: ""
            extreme_delivery_oc_text.text = yesterdayViewDoServiceData?.extremeDelivery?.extremeDeliveryCount?.displayName ?: ""
            double_text.text = yesterdayViewDoServiceData?.singles?._doubles?.displayName ?: ""
            triples_text.text = yesterdayViewDoServiceData?.singles?.triples?.displayName ?: ""
            carry_out_laod_time_text.text = yesterdayViewDoServiceData?.loadTime?.carryoutLoadTime?.displayName ?: ""
            hang_up_text.text = yesterdayViewDoServiceData?.aot?.hangUps?.displayName ?: ""
            eadt_display.text = yesterdayViewDoServiceData?.eADT?.displayName
            extreme_delivery_display.text = yesterdayViewDoServiceData?.extremeDelivery?.displayName
            single_display.text = yesterdayViewDoServiceData?.singles?.displayName
            load_time_display.text = yesterdayViewDoServiceData?.loadTime?.displayName
            wait_time_display.text = yesterdayViewDoServiceData?.waitTime?.displayName
            otd_display.text = yesterdayViewDoServiceData?.otd?.displayName
            aot_display.text = yesterdayViewDoServiceData?.aot?.displayName
            out_the_door_display.text = yesterdayViewDoServiceData?.outTheDoor?.displayName

            service_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (service_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.service_text)
                        }
                        y = service_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            if ( yesterdayViewDoServiceData?.eADT?.status != null) {
                when {
                    yesterdayViewDoServiceData.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val eatServiceGoalDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.eADT?.goal?.amount, yesterdayViewDoServiceData?.eADT?.goal?.percentage, yesterdayViewDoServiceData?.eADT?.goal?.value)
            val eatServiceVarianceDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.eADT?.variance?.amount, yesterdayViewDoServiceData?.eADT?.variance?.percentage, yesterdayViewDoServiceData?.eADT?.variance?.value)
            val eatServiceActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.eADT?.actual?.amount, yesterdayViewDoServiceData?.eADT?.actual?.percentage, yesterdayViewDoServiceData?.eADT?.actual?.value)
            eatData(eatServiceGoalDOYesterday,eatServiceVarianceDOYesterday,eatServiceActualDOYesterday)

            // leg time
            val legTimeServiceActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.eADT?.legTime?.actual?.amount, yesterdayViewDoServiceData?.eADT?.legTime?.actual?.percentage, yesterdayViewDoServiceData?.eADT?.legTime?.actual?.value)
            if(legTimeServiceActualDOYesterday.isEmpty()){
                
                leg_time_error.visibility = View.VISIBLE
                leg_time_actual.visibility = View.GONE
            }else{
                leg_time_actual.text =  legTimeServiceActualDOYesterday
            }

            if (yesterdayViewDoServiceData?.eADT?.legTime?.status != null) {
                when {
                    yesterdayViewDoServiceData.eADT.legTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.eADT.legTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (yesterdayViewDoServiceData?.extremeDelivery?.status != null) {
                when {
                    yesterdayViewDoServiceData.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val extremeDeliveryServiceGoalDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.extremeDelivery?.goal?.amount, yesterdayViewDoServiceData?.extremeDelivery?.goal?.percentage, yesterdayViewDoServiceData?.extremeDelivery?.goal?.value)
            val extremeDeliveryServiceVarianceDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.extremeDelivery?.variance?.amount, yesterdayViewDoServiceData?.extremeDelivery?.variance?.percentage, yesterdayViewDoServiceData?.extremeDelivery?.variance?.value)
            val extremeDeliveryServiceActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.extremeDelivery?.actual?.amount, yesterdayViewDoServiceData?.extremeDelivery?.actual?.percentage, yesterdayViewDoServiceData?.extremeDelivery?.actual?.value)
            extremeDeliveryData(extremeDeliveryServiceGoalDOYesterday,extremeDeliveryServiceVarianceDOYesterday,extremeDeliveryServiceActualDOYesterday)

            // delivery   order count
            val deliveryCountActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.amount, yesterdayViewDoServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.percentage, yesterdayViewDoServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.value)
            if(deliveryCountActualDOYesterday.isEmpty()){
                
                delivery_order_error.visibility = View.VISIBLE
                delivery_order_actual.visibility = View.GONE
            }else{
                delivery_order_actual.text =  deliveryCountActualDOYesterday
            }

            if (yesterdayViewDoServiceData?.extremeDelivery?.deliveryOrderCount?.status != null) {

                when {
                    yesterdayViewDoServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // extreme_delivery_oc_actual
            val extremeDeliveryCountActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.amount, yesterdayViewDoServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.percentage, yesterdayViewDoServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.value)
            if(extremeDeliveryCountActualDOYesterday.isEmpty()){
                
                extreme_delivery_oc_actual_error.visibility = View.VISIBLE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
            }else{
                extreme_delivery_oc_actual.text =  extremeDeliveryCountActualDOYesterday
            }

            if (yesterdayViewDoServiceData?.extremeDelivery?.extremeDeliveryCount?.status != null) {
                when {
                    yesterdayViewDoServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // Singles
            if ( yesterdayViewDoServiceData?.singles?.status != null) {
                when {
                    yesterdayViewDoServiceData.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val singleGoalDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.singles?.goal?.amount, yesterdayViewDoServiceData?.singles?.goal?.percentage, yesterdayViewDoServiceData?.singles?.goal?.value)
            val singleServiceVarianceDOYesterday= Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.singles?.variance?.amount, yesterdayViewDoServiceData?.singles?.variance?.percentage, yesterdayViewDoServiceData?.singles?.variance?.value)
            val singleServiceActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.singles?.actual?.amount, yesterdayViewDoServiceData?.singles?.actual?.percentage, yesterdayViewDoServiceData?.singles?.actual?.value)
            singleData(singleGoalDOYesterday,singleServiceVarianceDOYesterday,singleServiceActualDOYesterday)

            // double
            val doubleServiceActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.singles?._doubles?.actual?.amount, yesterdayViewDoServiceData?.singles?._doubles?.actual?.percentage, yesterdayViewDoServiceData?.singles?._doubles?.actual?.value)
            if(doubleServiceActualDOYesterday.isEmpty()){
                
                double_percentage_error.visibility = View.VISIBLE
                double_percentage.visibility = View.GONE
            }else{
                double_percentage.text =  doubleServiceActualDOYesterday
            }

            if (yesterdayViewDoServiceData?.singles?._doubles?.status != null) {

                when {
                    yesterdayViewDoServiceData.singles._doubles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.singles._doubles.status.toString() == resources.getString(R.string.under_limit) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // triple
            val tripleServiceActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.singles?.triples?.actual?.amount, yesterdayViewDoServiceData?.singles?.triples?.actual?.percentage, yesterdayViewDoServiceData?.singles?.triples?.actual?.value)
            if(tripleServiceActualDOYesterday.isEmpty()){
                
                triple_percentage_error.visibility = View.VISIBLE
                triple_percentage.visibility = View.GONE
            }else{
                triple_percentage.text =  tripleServiceActualDOYesterday
            }

            if (yesterdayViewDoServiceData?.singles?.triples?.status != null) {
                when {
                    yesterdayViewDoServiceData.singles.triples.status.toString() == resources.getString(R.string.out_of_range) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.singles.triples.status.toString() == resources.getString(R.string.under_limit) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //Load time
            val loadTimeGoalDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.loadTime?.goal?.amount, yesterdayViewDoServiceData?.loadTime?.goal?.percentage, yesterdayViewDoServiceData?.loadTime?.goal?.value)
            val loadTimeServiceVarianceDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.loadTime?.variance?.amount, yesterdayViewDoServiceData?.loadTime?.variance?.percentage, yesterdayViewDoServiceData?.loadTime?.variance?.value)
            val loadTimeServiceActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.loadTime?.actual?.amount, yesterdayViewDoServiceData?.loadTime?.actual?.percentage, yesterdayViewDoServiceData?.loadTime?.actual?.value)
            loadTimeData(loadTimeGoalDOYesterday,loadTimeServiceVarianceDOYesterday,loadTimeServiceActualDOYesterday)

            if ( yesterdayViewDoServiceData?.loadTime?.status != null) {
                when {
                    yesterdayViewDoServiceData.loadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.loadTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

             //carry_out_load_time_percentage
            val carryOutServiceActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.loadTime?.carryoutLoadTime?.actual?.amount, yesterdayViewDoServiceData?.loadTime?.carryoutLoadTime?.actual?.percentage, yesterdayViewDoServiceData?.loadTime?.carryoutLoadTime?.actual?.value)
            if(carryOutServiceActualDOYesterday.isEmpty()){
                
                carry_out_laod_time_percentage_error.visibility = View.VISIBLE
                carry_out_laod_time_percentage.visibility = View.GONE
            }else{
                carry_out_laod_time_percentage.text =  carryOutServiceActualDOYesterday
            }

            if ( yesterdayViewDoServiceData?.loadTime?.carryoutLoadTime?.status != null) {
                when {
                    yesterdayViewDoServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //wait time

            if (yesterdayViewDoServiceData?.waitTime?.status != null) {

                when {
                    yesterdayViewDoServiceData.waitTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.waitTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val waitTimeGoalDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.waitTime?.goal?.amount, yesterdayViewDoServiceData?.waitTime?.goal?.percentage, yesterdayViewDoServiceData?.waitTime?.goal?.value)
            val waitTimeServiceVarianceDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.waitTime?.variance?.amount, yesterdayViewDoServiceData?.waitTime?.variance?.percentage, yesterdayViewDoServiceData?.waitTime?.variance?.value)
            val waitTimeServiceActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.waitTime?.actual?.amount, yesterdayViewDoServiceData?.waitTime?.actual?.percentage, yesterdayViewDoServiceData?.waitTime?.actual?.value)
            waitTimeData(waitTimeGoalDOYesterday,waitTimeServiceVarianceDOYesterday,waitTimeServiceActualDOYesterday)

            // OTD
            val otdGoalDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.otd?.goal?.amount, yesterdayViewDoServiceData?.otd?.goal?.percentage, yesterdayViewDoServiceData?.otd?.goal?.value)
            val otdServiceVarianceDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.otd?.variance?.amount, yesterdayViewDoServiceData?.otd?.variance?.percentage, yesterdayViewDoServiceData?.otd?.variance?.value)
            val otdServiceActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.otd?.actual?.amount, yesterdayViewDoServiceData?.otd?.actual?.percentage, yesterdayViewDoServiceData?.otd?.actual?.value)
            otdData(otdGoalDOYesterday,otdServiceVarianceDOYesterday,otdServiceActualDOYesterday)

            if (yesterdayViewDoServiceData?.otd?.status != null) {
                when {
                    yesterdayViewDoServiceData.otd.status.toString() == resources.getString(R.string.out_of_range) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.otd.status.toString() == resources.getString(R.string.under_limit) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // AOT
            if ( yesterdayViewDoServiceData?.aot?.status != null) {
                when {
                    yesterdayViewDoServiceData.aot.status.toString() == resources.getString(R.string.out_of_range) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.aot.status.toString() == resources.getString(R.string.under_limit) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val aotGoalDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.aot?.goal?.amount, yesterdayViewDoServiceData?.aot?.goal?.percentage, yesterdayViewDoServiceData?.aot?.goal?.value)
            val aotServiceVarianceDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.aot?.variance?.amount, yesterdayViewDoServiceData?.aot?.variance?.percentage, yesterdayViewDoServiceData?.aot?.variance?.value)
            val aotServiceActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.aot?.actual?.amount, yesterdayViewDoServiceData?.aot?.actual?.percentage, yesterdayViewDoServiceData?.aot?.actual?.value)
            aotData(aotGoalDOYesterday,aotServiceVarianceDOYesterday,aotServiceActualDOYesterday)

            //HangUp Service
            val hangUpServiceActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.aot?.hangUps?.actual?.amount, yesterdayViewDoServiceData?.aot?.hangUps?.actual?.percentage, yesterdayViewDoServiceData?.aot?.hangUps?.actual?.value)
            if(hangUpServiceActualDOYesterday.isEmpty()){
                
                hang_up_percentage_error.visibility = View.VISIBLE
                hang_up_percentage.visibility = View.GONE
            }else{
                hang_up_percentage.text =  hangUpServiceActualDOYesterday
            }


            // Out the door
            if ( yesterdayViewDoServiceData?.outTheDoor?.status != null) {

                when {
                    yesterdayViewDoServiceData.outTheDoor.status.toString() == resources.getString(R.string.out_of_range) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.outTheDoor.status.toString() == resources.getString(R.string.under_limit) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val outDoorGoalDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.outTheDoor?.goal?.amount, yesterdayViewDoServiceData?.outTheDoor?.goal?.percentage, yesterdayViewDoServiceData?.outTheDoor?.goal?.value)
            val outDoorServiceVarianceDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.outTheDoor?.variance?.amount, yesterdayViewDoServiceData?.outTheDoor?.variance?.percentage, yesterdayViewDoServiceData?.outTheDoor?.variance?.value)
            val outDoorServiceActualDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.outTheDoor?.actual?.amount, yesterdayViewDoServiceData?.outTheDoor?.actual?.percentage, yesterdayViewDoServiceData?.outTheDoor?.actual?.value)
            outDoorData(outDoorGoalDOYesterday,outDoorServiceVarianceDOYesterday,outDoorServiceActualDOYesterday)

            // csat
            val csatDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.total?.actual?.amount, yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.total?.actual?.percentage, yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.total?.actual?.value)
            if(csatDOYesterday.isEmpty()){
                
                csat_goal_error.visibility = View.VISIBLE
                csat_goal.visibility = View.GONE
            }else{
                csat_goal.text =  csatDOYesterday
            }
            val weeklyDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.amount, yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.percentage, yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.value)
            if(weeklyDOYesterday.isEmpty()){
                
                weekly_percentage_error.visibility = View.VISIBLE
                weekly_percentage.visibility = View.GONE
            }else{
                weekly_percentage.text =  weeklyDOYesterday
            }
            val ccDOYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.amount, yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.percentage, yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.value)
            if(ccDOYesterday.isEmpty()){
                
                cc_per_100_percentage_error.visibility = View.VISIBLE
                cc_per_100_percentage.visibility = View.GONE
            }else{
                cc_per_100_percentage.text =  ccDOYesterday
            }

            csat_display.text =
                yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.total?.displayName
                    ?: getString(R.string.csat_text)
            weekly_text.text =
                yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName
                    ?: getString(R.string.weekly_text)
            cc_per_100_text.text =
                yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName
                    ?: getString(
                        R.string.cc_per_100_text)

            if ( yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.total?.status != null) {
                when {
                    yesterdayViewDoServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.green))
                    } else -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            if ( yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.status != null) {

                when {
                    yesterdayViewDoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if ( yesterdayViewDoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.status != null) {
                when {
                    yesterdayViewDoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewDoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
        }catch (e:Exception){
            Logger.error(e.message.toString(),"Service Overview Yesterday KPI")
        }

    }

    private fun rangeOverViewDOService(serviceTodayDetail: DOOverviewRangeQuery.Do_) {
        try{

            val rangeOverViewDoServiceData = serviceTodayDetail.kpis?.supervisors?.stores?.period?.service

            Logger.info("Service Period Range", "Service Overview KPI")


            // display name
            Validation().checkNullValueToShowView(this, rangeOverViewDoServiceData?.aot?.displayName, aot_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoServiceData?.aot?.hangUps?.displayName, hang_up_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoServiceData?.aot?.hangUps?.displayName, hang_up_parent)

            Validation().checkNullValueToShowView(this, rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.total?.displayName, csat_meter_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName, weekly_parent)
            Validation().checkNullValueToShowView(this, rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName, cc_per_100_text_parent)

            showServiceNarrativeData(rangeOverViewDoServiceData?.narrative.toString())

            if(rangeOverViewDoServiceData?.aot!= null){
                aot_view.visibility= View.VISIBLE
            }else{
                aot_view.visibility= View.GONE
            }

            out_the_door_view.visibility= View.GONE
            csat_view.visibility= View.GONE


            leg_time_text.text = rangeOverViewDoServiceData?.eADT?.legTime?.displayName ?: ""

            delivery_order_count.text = rangeOverViewDoServiceData?.extremeDelivery?.deliveryOrderCount?.displayName ?: ""
            extreme_delivery_oc_text.text = rangeOverViewDoServiceData?.extremeDelivery?.extremeDeliveryCount?.displayName ?: ""
            double_text.text = rangeOverViewDoServiceData?.singles?._doubles?.displayName ?: ""
            triples_text.text = rangeOverViewDoServiceData?.singles?.triples?.displayName ?: ""
            carry_out_laod_time_text.text = rangeOverViewDoServiceData?.loadTime?.carryoutLoadTime?.displayName ?: ""
            hang_up_text.text = rangeOverViewDoServiceData?.aot?.hangUps?.displayName ?: ""
            eadt_display.text = rangeOverViewDoServiceData?.eADT?.displayName
            extreme_delivery_display.text = rangeOverViewDoServiceData?.extremeDelivery?.displayName
            single_display.text = rangeOverViewDoServiceData?.singles?.displayName
            load_time_display.text = rangeOverViewDoServiceData?.loadTime?.displayName
            wait_time_display.text = rangeOverViewDoServiceData?.waitTime?.displayName
            otd_display.text = rangeOverViewDoServiceData?.otd?.displayName
            aot_display.text = rangeOverViewDoServiceData?.aot?.displayName
            out_the_door_display.text = rangeOverViewDoServiceData?.outTheDoor?.displayName

            service_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (service_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.service_text)
                        }
                        y = service_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            if ( rangeOverViewDoServiceData?.eADT?.status != null) {
                when {
                    rangeOverViewDoServiceData.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val eatServiceGoalDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.eADT?.goal?.amount, rangeOverViewDoServiceData?.eADT?.goal?.percentage, rangeOverViewDoServiceData?.eADT?.goal?.value)
            val eatServiceVarianceDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.eADT?.variance?.amount, rangeOverViewDoServiceData?.eADT?.variance?.percentage, rangeOverViewDoServiceData?.eADT?.variance?.value)
            val eatServiceActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.eADT?.actual?.amount, rangeOverViewDoServiceData?.eADT?.actual?.percentage, rangeOverViewDoServiceData?.eADT?.actual?.value)
            eatData(eatServiceGoalDORange,eatServiceVarianceDORange,eatServiceActualDORange)

            // leg time
            val legTimeServiceActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.eADT?.legTime?.actual?.amount, rangeOverViewDoServiceData?.eADT?.legTime?.actual?.percentage, rangeOverViewDoServiceData?.eADT?.legTime?.actual?.value)
            if(legTimeServiceActualDORange.isEmpty()){
                
                leg_time_error.visibility = View.VISIBLE
                leg_time_actual.visibility = View.GONE
            }else{
                leg_time_actual.text =  legTimeServiceActualDORange
            }



            if (rangeOverViewDoServiceData?.eADT?.legTime?.status != null) {
                when {
                    rangeOverViewDoServiceData.eADT.legTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.eADT.legTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (rangeOverViewDoServiceData?.extremeDelivery?.status != null) {
                when {
                    rangeOverViewDoServiceData.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val extremeDeliveryServiceGoalDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.extremeDelivery?.goal?.amount, rangeOverViewDoServiceData?.extremeDelivery?.goal?.percentage, rangeOverViewDoServiceData?.extremeDelivery?.goal?.value)
            val extremeDeliveryServiceVarianceDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.extremeDelivery?.variance?.amount, rangeOverViewDoServiceData?.extremeDelivery?.variance?.percentage, rangeOverViewDoServiceData?.extremeDelivery?.variance?.value)
            val extremeDeliveryServiceActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.extremeDelivery?.actual?.amount, rangeOverViewDoServiceData?.extremeDelivery?.actual?.percentage, rangeOverViewDoServiceData?.extremeDelivery?.actual?.value)
            extremeDeliveryData(extremeDeliveryServiceGoalDORange,extremeDeliveryServiceVarianceDORange,extremeDeliveryServiceActualDORange)

            // delivery   order count
            val deliveryCountActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.amount, rangeOverViewDoServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.percentage, rangeOverViewDoServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.value)
            if(deliveryCountActualDORange.isEmpty()){
                
                delivery_order_error.visibility = View.VISIBLE
                delivery_order_actual.visibility = View.GONE
            }else{
                delivery_order_actual.text =  deliveryCountActualDORange
            }

            if (rangeOverViewDoServiceData?.extremeDelivery?.deliveryOrderCount?.status != null) {

                when {
                    rangeOverViewDoServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // extreme_delivery_oc_actual
            val extremeDeliveryCountActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.amount, rangeOverViewDoServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.percentage, rangeOverViewDoServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.value)
            if(extremeDeliveryCountActualDORange.isEmpty()){
                
                extreme_delivery_oc_actual_error.visibility = View.VISIBLE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
            }else{
                extreme_delivery_oc_actual.text =  extremeDeliveryCountActualDORange
            }

            if (rangeOverViewDoServiceData?.extremeDelivery?.extremeDeliveryCount?.status != null) {
                when {
                    rangeOverViewDoServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // Singles
            if ( rangeOverViewDoServiceData?.singles?.status != null) {
                when {
                    rangeOverViewDoServiceData.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            single_percentage_goal.text = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.singles?.goal?.amount, rangeOverViewDoServiceData?.singles?.goal?.percentage, rangeOverViewDoServiceData?.singles?.goal?.value)
            val singleGoalDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.singles?.goal?.amount, rangeOverViewDoServiceData?.singles?.goal?.percentage, rangeOverViewDoServiceData?.singles?.goal?.value)
            val singleServiceVarianceDORange= Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.singles?.variance?.amount, rangeOverViewDoServiceData?.singles?.variance?.percentage, rangeOverViewDoServiceData?.singles?.variance?.value)
            val singleServiceActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.singles?.actual?.amount, rangeOverViewDoServiceData?.singles?.actual?.percentage, rangeOverViewDoServiceData?.singles?.actual?.value)
            singleData(singleGoalDORange,singleServiceVarianceDORange,singleServiceActualDORange)

            // double
            val doubleServiceActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.singles?._doubles?.actual?.amount, rangeOverViewDoServiceData?.singles?._doubles?.actual?.percentage, rangeOverViewDoServiceData?.singles?._doubles?.actual?.value)
            if(doubleServiceActualDORange.isEmpty()){
                
                double_percentage_error.visibility = View.VISIBLE
                double_percentage.visibility = View.GONE
            }else{
                double_percentage.text =  doubleServiceActualDORange
            }

            if (rangeOverViewDoServiceData?.singles?._doubles?.status != null) {

                when {
                    rangeOverViewDoServiceData.singles._doubles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.singles._doubles.status.toString() == resources.getString(R.string.under_limit) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            // triple
            val tripleServiceActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.singles?.triples?.actual?.amount, rangeOverViewDoServiceData?.singles?.triples?.actual?.percentage, rangeOverViewDoServiceData?.singles?.triples?.actual?.value)
            if(tripleServiceActualDORange.isEmpty()){
                
                triple_percentage_error.visibility = View.VISIBLE
                triple_percentage.visibility = View.GONE
            }else{
                triple_percentage.text =  tripleServiceActualDORange
            }

            if (rangeOverViewDoServiceData?.singles?.triples?.status != null) {
                when {
                    rangeOverViewDoServiceData.singles.triples.status.toString() == resources.getString(R.string.out_of_range) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.singles.triples.status.toString() == resources.getString(R.string.under_limit) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //Load time
            val loadTimeGoalDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.loadTime?.goal?.amount, rangeOverViewDoServiceData?.loadTime?.goal?.percentage, rangeOverViewDoServiceData?.loadTime?.goal?.value)
            val loadTimeServiceVarianceDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.loadTime?.variance?.amount, rangeOverViewDoServiceData?.loadTime?.variance?.percentage, rangeOverViewDoServiceData?.loadTime?.variance?.value)
            val loadTimeServiceActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.loadTime?.actual?.amount, rangeOverViewDoServiceData?.loadTime?.actual?.percentage, rangeOverViewDoServiceData?.loadTime?.actual?.value)
            loadTimeData(loadTimeGoalDORange,loadTimeServiceVarianceDORange,loadTimeServiceActualDORange)

            if ( rangeOverViewDoServiceData?.loadTime?.status != null) {
                when {
                    rangeOverViewDoServiceData.loadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.loadTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

             //carry_out_load_time_percentage
            val carryOutServiceActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.loadTime?.carryoutLoadTime?.actual?.amount, rangeOverViewDoServiceData?.loadTime?.carryoutLoadTime?.actual?.percentage, rangeOverViewDoServiceData?.loadTime?.carryoutLoadTime?.actual?.value)
            if(carryOutServiceActualDORange.isEmpty()){
                
                carry_out_laod_time_percentage_error.visibility = View.VISIBLE
                carry_out_laod_time_percentage.visibility = View.GONE
            }else{
                carry_out_laod_time_percentage.text =  carryOutServiceActualDORange
            }

            if ( rangeOverViewDoServiceData?.loadTime?.carryoutLoadTime?.status != null) {
                when {
                    rangeOverViewDoServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //wait time

            if (rangeOverViewDoServiceData?.waitTime?.status != null) {

                when {
                    rangeOverViewDoServiceData.waitTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.waitTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val waitTimeGoalDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.waitTime?.goal?.amount, rangeOverViewDoServiceData?.waitTime?.goal?.percentage, rangeOverViewDoServiceData?.waitTime?.goal?.value)
            val waitTimeServiceVarianceDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.waitTime?.variance?.amount, rangeOverViewDoServiceData?.waitTime?.variance?.percentage, rangeOverViewDoServiceData?.waitTime?.variance?.value)
            val waitTimeServiceActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.waitTime?.actual?.amount, rangeOverViewDoServiceData?.waitTime?.actual?.percentage, rangeOverViewDoServiceData?.waitTime?.actual?.value)
            waitTimeData(waitTimeGoalDORange,waitTimeServiceVarianceDORange,waitTimeServiceActualDORange)

            // OTD
            val otdGoalDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.otd?.goal?.amount, rangeOverViewDoServiceData?.otd?.goal?.percentage, rangeOverViewDoServiceData?.otd?.goal?.value)
            val otdServiceVarianceDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.otd?.variance?.amount, rangeOverViewDoServiceData?.otd?.variance?.percentage, rangeOverViewDoServiceData?.otd?.variance?.value)
            val otdServiceActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.otd?.actual?.amount, rangeOverViewDoServiceData?.otd?.actual?.percentage, rangeOverViewDoServiceData?.otd?.actual?.value)
            otdData(otdGoalDORange,otdServiceVarianceDORange,otdServiceActualDORange)

            if (rangeOverViewDoServiceData?.otd?.status != null) {
                when {
                    rangeOverViewDoServiceData.otd.status.toString() == resources.getString(R.string.out_of_range) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.otd.status.toString() == resources.getString(R.string.under_limit) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // AOT
            if ( rangeOverViewDoServiceData?.aot?.status != null) {
                when {
                    rangeOverViewDoServiceData.aot.status.toString() == resources.getString(R.string.out_of_range) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.aot.status.toString() == resources.getString(R.string.under_limit) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val aotGoalDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.aot?.goal?.amount, rangeOverViewDoServiceData?.aot?.goal?.percentage, rangeOverViewDoServiceData?.aot?.goal?.value)
            val aotServiceVarianceDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.aot?.variance?.amount, rangeOverViewDoServiceData?.aot?.variance?.percentage, rangeOverViewDoServiceData?.aot?.variance?.value)
            val aotServiceActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.aot?.actual?.amount, rangeOverViewDoServiceData?.aot?.actual?.percentage, rangeOverViewDoServiceData?.aot?.actual?.value)
            aotData(aotGoalDORange,aotServiceVarianceDORange,aotServiceActualDORange)

            //HangUp Service
            val hangUpServiceActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.aot?.hangUps?.actual?.amount, rangeOverViewDoServiceData?.aot?.hangUps?.actual?.percentage, rangeOverViewDoServiceData?.aot?.hangUps?.actual?.value)
            if(hangUpServiceActualDORange.isEmpty()){
                
                hang_up_percentage_error.visibility = View.VISIBLE
                hang_up_percentage.visibility = View.GONE
            }else{
                hang_up_percentage.text =  hangUpServiceActualDORange
            }


            // Out the door
            if ( rangeOverViewDoServiceData?.outTheDoor?.status != null) {

                when {
                    rangeOverViewDoServiceData.outTheDoor.status.toString() == resources.getString(R.string.out_of_range) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.outTheDoor.status.toString() == resources.getString(R.string.under_limit) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val outDoorGoalDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.outTheDoor?.goal?.amount, rangeOverViewDoServiceData?.outTheDoor?.goal?.percentage, rangeOverViewDoServiceData?.outTheDoor?.goal?.value)
            val outDoorServiceVarianceDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.outTheDoor?.variance?.amount, rangeOverViewDoServiceData?.outTheDoor?.variance?.percentage, rangeOverViewDoServiceData?.outTheDoor?.variance?.value)
            val outDoorServiceActualDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.outTheDoor?.actual?.amount, rangeOverViewDoServiceData?.outTheDoor?.actual?.percentage, rangeOverViewDoServiceData?.outTheDoor?.actual?.value)
            outDoorData(outDoorGoalDORange,outDoorServiceVarianceDORange,outDoorServiceActualDORange)

            // csat

            val csatDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.total?.actual?.amount, rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.total?.actual?.percentage, rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.total?.actual?.value)
            if(csatDORange.isEmpty()){
                
                csat_goal_error.visibility = View.VISIBLE
                csat_goal.visibility = View.GONE
            }else{
                csat_goal.text =  csatDORange
            }
            val weeklyDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.amount, rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.percentage, rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.value)
            if(weeklyDORange.isEmpty()){
                
                weekly_percentage_error.visibility = View.VISIBLE
                weekly_percentage.visibility = View.GONE
            }else{
                weekly_percentage.text =  weeklyDORange
            }
            val strCCDORange = Validation().checkAmountPercentageValue(this, rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.amount, rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.percentage, rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.value)
            if(strCCDORange.isEmpty()){
                
                cc_per_100_percentage_error.visibility = View.VISIBLE
                cc_per_100_percentage.visibility = View.GONE
            }else{
                cc_per_100_percentage.text =  strCCDORange
            }
            csat_display.text =
                rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.total?.displayName
                    ?: getString(R.string.csat_text)
            weekly_text.text =
                rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName
                    ?: getString(R.string.weekly_text)
            cc_per_100_text.text =
                rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName
                    ?: getString(
                        R.string.cc_per_100_text)

            if ( rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.total?.status != null) {
                when {
                    rangeOverViewDoServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.green))
                    } else -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            if ( rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.status != null) {

                when {
                    rangeOverViewDoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if ( rangeOverViewDoServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.status != null) {
                when {
                    rangeOverViewDoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDoServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
        }catch (e:Exception){
            Logger.error(e.message.toString(),"Service Overview Period Range KPI")
        }

    }


    // supervisor
    fun todayViewSupervisor(serviceTodayDetail: SupervisorOverviewTodayQuery.Supervisor) {
        val todayViewSupervisorServiceData = serviceTodayDetail.kpis?.stores?.today?.service

        Logger.info("Service Today", "Service Overview KPI")

        // display name
        service_text_small.text = todayViewSupervisorServiceData!!.displayName ?: ""
        leg_time_text.text = todayViewSupervisorServiceData.eADT?.legTime?.displayName?: ""
        delivery_order_count.text = todayViewSupervisorServiceData.extremeDelivery?.deliveryOrderCount?.displayName ?: ""
        extreme_delivery_oc_text.text = todayViewSupervisorServiceData.extremeDelivery?.extremeDeliveryCount!!.displayName ?: ""
        double_text.text = todayViewSupervisorServiceData.singles!!._doubles!!.displayName ?: ""
        triples_text.text = todayViewSupervisorServiceData.singles.triples!!.displayName ?: ""
        carry_out_laod_time_text.text = todayViewSupervisorServiceData.loadTime!!.carryoutLoadTime!!.displayName ?: ""
        hang_up_text.text = todayViewSupervisorServiceData.aot!!.hangUps!!.displayName ?: ""
        weekly_text.text = todayViewSupervisorServiceData.csatMeterRating90dayAvg!!.weeklyCCCClosureRate!!.displayName ?: ""
        cc_per_100_text.text = todayViewSupervisorServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg!!.displayName ?: ""
        // display headers
        eadt_display.text = todayViewSupervisorServiceData.eADT?.displayName
        extreme_delivery_display.text = todayViewSupervisorServiceData.extremeDelivery.displayName
        single_display.text = todayViewSupervisorServiceData.singles.displayName
        load_time_display.text = todayViewSupervisorServiceData.loadTime.displayName
        wait_time_display.text = todayViewSupervisorServiceData.waitTime?.displayName
        otd_display.text = todayViewSupervisorServiceData.otd?.displayName
        aot_display.text = todayViewSupervisorServiceData.aot.displayName
        out_the_door_display.text = todayViewSupervisorServiceData.outTheDoor?.displayName

        if (todayViewSupervisorServiceData.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.status != null) {
            service_sales.text = Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.actual.value)
            when {
                todayViewSupervisorServiceData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_sales.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_sales.setTextColor(getColor(R.color.green))
                } else -> {
                    service_sales.setTextColor(getColor(R.color.text_color))
                }
            }
        }



        service_scroll_parent.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 0f
                override fun onScrollChanged() {
                    if (service_scroll_parent.scrollY > y) {
                        parent_data_on_scroll_linear.visibility = View.VISIBLE
                        parent_data_on_scroll_view.visibility = View.VISIBLE
                        level_two_scroll_data_action.text =
                            todayViewSupervisorServiceData.displayName ?: getString(R.string.service_text_small)

                        if (todayViewSupervisorServiceData.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.status != null) {
                            level_two_scroll_data_action_value.text =
                                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.goal!!.value)
                            when {
                                todayViewSupervisorServiceData.status.toString() == resources.getString(R.string.out_of_range) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.red_circle,
                                        0
                                    )
                                }
                                todayViewSupervisorServiceData.status.toString() == resources.getString(R.string.under_limit) -> {
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
                    y = service_scroll_parent.scrollY.toFloat()
                    if (y <= 0) {
                        parent_data_on_scroll_linear.visibility = View.INVISIBLE
                        parent_data_on_scroll_view.visibility = View.GONE

                    }
                }
            })


        service_goal_value.text =
            if (todayViewSupervisorServiceData.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.goal.value) else ""
        service_variance_value.text =
            if (todayViewSupervisorServiceData.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.variance.value) else ""
        showServiceNarrativeData(todayViewSupervisorServiceData.narrative.toString())


        if (todayViewSupervisorServiceData.eADT?.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.eADT.status != null) {
            eadt_actual.text = Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.eADT.actual.value)
            when {
                todayViewSupervisorServiceData.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    eadt_actual.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    eadt_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    eadt_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        eadt_goal.text =
            if (todayViewSupervisorServiceData.eADT?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.eADT.goal.value) else ""
        eadt_variance.text =
            if (todayViewSupervisorServiceData.eADT?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.eADT.variance.value) else ""
        // leg time
        if (todayViewSupervisorServiceData.eADT?.legTime?.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.eADT.legTime.status != null) {
            leg_time_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.eADT.legTime.actual.value)
            when {
                todayViewSupervisorServiceData.eADT.legTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    leg_time_actual.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.eADT.legTime.status.toString() == resources.getString(R.string.under_limit) -> {
                    leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    leg_time_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    leg_time_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        leg_time_goal.text =
            if (todayViewSupervisorServiceData.eADT?.legTime?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.eADT.legTime.goal.value) else ""
        leg_time_variance.text =
            if (todayViewSupervisorServiceData.eADT?.legTime?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.eADT.legTime.variance.value) else ""


        // extreme_delivery
        if (todayViewSupervisorServiceData.extremeDelivery.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.extremeDelivery.status != null) {
            extreme_delivery_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.extremeDelivery.actual.value)

            when {
                todayViewSupervisorServiceData.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    extreme_delivery_actual.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    extreme_delivery_actual.setTextColor(getColor(R.color.green))

                } else-> {
                    extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    extreme_delivery_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        extreme_delivery_goal.text =
            if (todayViewSupervisorServiceData.extremeDelivery.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.extremeDelivery.goal.value)
            else ""
        extreme_delivery_variance.text =
            if (todayViewSupervisorServiceData.extremeDelivery.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.extremeDelivery.variance.value)
            else ""

        // delivery order count
        if (todayViewSupervisorServiceData.extremeDelivery.deliveryOrderCount?.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.extremeDelivery.deliveryOrderCount.status != null) {
            delivery_order_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.extremeDelivery.deliveryOrderCount.actual.value)
            when {
                todayViewSupervisorServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    delivery_order_actual.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    delivery_order_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    delivery_order_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        // extreme_delivery_oc_actual
        if (todayViewSupervisorServiceData.extremeDelivery.extremeDeliveryCount.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.extremeDelivery.extremeDeliveryCount.status != null) {
            extreme_delivery_oc_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.extremeDelivery.extremeDeliveryCount.actual.value)
            when {
                todayViewSupervisorServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    extreme_delivery_oc_actual.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    extreme_delivery_oc_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    extreme_delivery_oc_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }


        // Singles
        if (todayViewSupervisorServiceData.singles.actual?.percentage?.isNaN() == false && todayViewSupervisorServiceData.singles.status != null) {
            single_percentage_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.singles.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                todayViewSupervisorServiceData.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    single_percentage_actual.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    single_percentage_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    single_percentage_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        single_percentage_goal.text =
            if (todayViewSupervisorServiceData.singles.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.singles.goal.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        single_percentage_variance.text =
            if (todayViewSupervisorServiceData.singles.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.singles.variance.percentage)
                .plus(getString(R.string.percentage_text)) else ""

        // double
        if (todayViewSupervisorServiceData.singles._doubles?.actual?.percentage?.isNaN() == false && todayViewSupervisorServiceData.singles._doubles.status != null) {
            double_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.singles._doubles.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                todayViewSupervisorServiceData.singles._doubles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    double_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.singles._doubles.status.toString() == resources.getString(R.string.under_limit) -> {
                    double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    double_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    double_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        // triple
        if (todayViewSupervisorServiceData.singles.triples.actual?.percentage?.isNaN() == false && todayViewSupervisorServiceData.singles.triples.status != null) {
            triple_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.singles.triples.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                todayViewSupervisorServiceData.singles.triples.status.toString() == resources.getString(R.string.out_of_range) -> {
                    triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    triple_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.singles.triples.status.toString() == resources.getString(R.string.under_limit) -> {
                    triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    triple_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    triple_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        //Load time
        if (todayViewSupervisorServiceData.loadTime.goal?.value?.isNaN() == false) {
            load_time_goal.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.loadTime.goal.value)
        }
        if (todayViewSupervisorServiceData.loadTime.variance?.value?.isNaN() == false) {
            load_time_variance.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.loadTime.variance.value)
        }
        if (todayViewSupervisorServiceData.loadTime.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.loadTime.status != null) {
            load_time_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.loadTime.actual.value)
            when {
                todayViewSupervisorServiceData.loadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    load_time_actual.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.loadTime.status.toString() == resources.getString(R.string.under_limit) -> {
                    load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    load_time_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    load_time_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        //carry_out_load_time_percentage

        if (todayViewSupervisorServiceData.loadTime.carryoutLoadTime?.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.loadTime.carryoutLoadTime.status != null) {
            carry_out_laod_time_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.loadTime.carryoutLoadTime.actual.value)
            when {
                todayViewSupervisorServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    carry_out_laod_time_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    carry_out_laod_time_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    carry_out_laod_time_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        //wait time
        if (todayViewSupervisorServiceData.waitTime?.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.waitTime.status != null) {
            wait_time_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.waitTime.actual.value)
            when {
                todayViewSupervisorServiceData.waitTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                    wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    wait_time_actual.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.waitTime.status.toString() == resources.getString(R.string.under_limit) -> {
                    wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    wait_time_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    wait_time_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        wait_time_goal.text =
            if (todayViewSupervisorServiceData.waitTime?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.waitTime.goal.value) else ""
        wait_time_variance.text =
            if (todayViewSupervisorServiceData.waitTime?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.waitTime.variance.value) else ""

        // OTD
        if (todayViewSupervisorServiceData.otd?.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.otd.status != null) {
            otd_actual.text = Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.otd.actual.value)
            when {
                todayViewSupervisorServiceData.otd.status.toString() == resources.getString(R.string.out_of_range) -> {
                    otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    otd_actual.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.otd.status.toString() == resources.getString(R.string.under_limit) -> {
                    otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    otd_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    otd_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        otd_goal.text =
            if (todayViewSupervisorServiceData.otd?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.otd.goal.value) else ""
        otd_variance.text =
            if (todayViewSupervisorServiceData.otd?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.otd.variance.value) else ""

        // AOT
        if (todayViewSupervisorServiceData.aot.actual?.percentage?.isNaN() == false && todayViewSupervisorServiceData.aot.status != null) {
            aot_actual.text = Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.aot.actual.percentage)
                .plus(getString(R.string.percentage_text))
            when {
                todayViewSupervisorServiceData.aot.status.toString() == resources.getString(R.string.out_of_range) -> {
                    aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    aot_actual.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.aot.status.toString() == resources.getString(R.string.under_limit) -> {
                    aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    aot_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    aot_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        aot_goal.text =
            if (todayViewSupervisorServiceData.aot.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.aot.goal.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        aot_variance.text =
            if (todayViewSupervisorServiceData.aot.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.aot.variance.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        hang_up_percentage.text =
            if (todayViewSupervisorServiceData.aot.hangUps?.actual?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.aot.hangUps.actual.value) else ""


        // Out the door
        if (todayViewSupervisorServiceData.outTheDoor?.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.outTheDoor.status != null) {
            out_door_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.outTheDoor.actual.value)
            when {
                todayViewSupervisorServiceData.outTheDoor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    out_door_actual.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.outTheDoor.status.toString() == resources.getString(R.string.under_limit) -> {
                    out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    out_door_actual.setTextColor(getColor(R.color.green))
                } else -> {
                    out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    out_door_actual.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        out_door_goal.text =
            if (todayViewSupervisorServiceData.outTheDoor?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.outTheDoor.goal.value) else ""
        out_door_variance.text =
            if (todayViewSupervisorServiceData.outTheDoor?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                todayViewSupervisorServiceData.outTheDoor.variance.value) else ""


        // csat

        if (todayViewSupervisorServiceData.csatMeterRating90dayAvg.total?.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.csatMeterRating90dayAvg.total.status != null) {
            csat_goal.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.csatMeterRating90dayAvg.total.actual.value)
            when {
                todayViewSupervisorServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    csat_goal.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    csat_goal.setTextColor(getColor(R.color.green))
                } else -> {
                    csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    csat_goal.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        if (todayViewSupervisorServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate?.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status != null) {
            weekly_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.actual.value)
            when {
                todayViewSupervisorServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    weekly_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    weekly_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    weekly_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        if (todayViewSupervisorServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.actual?.value?.isNaN() == false && todayViewSupervisorServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status != null) {
            cc_per_100_percentage.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.actual.value)
            when {
                todayViewSupervisorServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    cc_per_100_percentage.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    cc_per_100_percentage.setTextColor(getColor(R.color.green))
                } else -> {
                    cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    cc_per_100_percentage.setTextColor(getColor(R.color.text_color))
                }
            }
        }

    }

    private fun yesterdayViewSupervisorService(serviceTodayDetail: SupervisorOverviewYesterdayQuery.Supervisor) {
        try{

            val yesterdayViewSupervisorServiceData = serviceTodayDetail.kpis?.stores?.yesterday?.service

            Logger.info("Service Yesterday", "Service Overview KPI")

            // display name

            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorServiceData?.aot?.displayName, aot_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorServiceData?.aot?.hangUps?.displayName, hang_up_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorServiceData?.aot?.hangUps?.displayName, hang_up_parent)

            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.total?.displayName, csat_meter_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName, weekly_parent)
            Validation().checkNullValueToShowView(this, yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName, cc_per_100_text_parent)

            showServiceNarrativeData(yesterdayViewSupervisorServiceData?.narrative.toString())

            if(yesterdayViewSupervisorServiceData?.aot!= null){
                aot_view.visibility= View.VISIBLE
            }else{
                aot_view.visibility= View.GONE
            }
            out_the_door_view.visibility= View.GONE
            csat_view.visibility= View.GONE


            leg_time_text.text = yesterdayViewSupervisorServiceData?.eADT?.legTime?.displayName ?: ""
            delivery_order_count.text = yesterdayViewSupervisorServiceData?.extremeDelivery?.deliveryOrderCount?.displayName ?: ""
            extreme_delivery_oc_text.text = yesterdayViewSupervisorServiceData?.extremeDelivery?.extremeDeliveryCount?.displayName ?: ""
            double_text.text = yesterdayViewSupervisorServiceData?.singles?._doubles?.displayName ?: ""
            triples_text.text = yesterdayViewSupervisorServiceData?.singles?.triples?.displayName ?: ""
            carry_out_laod_time_text.text = yesterdayViewSupervisorServiceData?.loadTime?.carryoutLoadTime?.displayName ?: ""
            hang_up_text.text = yesterdayViewSupervisorServiceData?.aot?.hangUps?.displayName ?: ""
            eadt_display.text = yesterdayViewSupervisorServiceData?.eADT?.displayName ?: ""
            extreme_delivery_display.text = yesterdayViewSupervisorServiceData?.extremeDelivery?.displayName ?: ""
            single_display.text = yesterdayViewSupervisorServiceData?.singles?.displayName ?: ""
            load_time_display.text = yesterdayViewSupervisorServiceData?.loadTime?.displayName ?: ""
            wait_time_display.text = yesterdayViewSupervisorServiceData?.waitTime?.displayName?: ""
            otd_display.text = yesterdayViewSupervisorServiceData?.otd?.displayName ?: ""
            aot_display.text = yesterdayViewSupervisorServiceData?.aot?.displayName ?: ""
            out_the_door_display.text = yesterdayViewSupervisorServiceData?.outTheDoor?.displayName ?: ""

            service_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (service_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.service_text)

                        }
                        y = service_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            if ( yesterdayViewSupervisorServiceData?.eADT?.status != null) {
                when {
                    yesterdayViewSupervisorServiceData.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val eatServiceGoalSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.eADT?.goal?.amount, yesterdayViewSupervisorServiceData?.eADT?.goal?.percentage, yesterdayViewSupervisorServiceData?.eADT?.goal?.value)
            val eatServiceVarianceSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.eADT?.variance?.amount, yesterdayViewSupervisorServiceData?.eADT?.variance?.percentage, yesterdayViewSupervisorServiceData?.eADT?.variance?.value)
            val eatServiceActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.eADT?.actual?.amount, yesterdayViewSupervisorServiceData?.eADT?.actual?.percentage, yesterdayViewSupervisorServiceData?.eADT?.actual?.value)
            eatData(eatServiceGoalSupervisorYesterday,eatServiceVarianceSupervisorYesterday,eatServiceActualSupervisorYesterday)

            // leg time
            val legTimeServiceActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.eADT?.legTime?.actual?.amount, yesterdayViewSupervisorServiceData?.eADT?.legTime?.actual?.percentage, yesterdayViewSupervisorServiceData?.eADT?.legTime?.actual?.value)
            if(legTimeServiceActualSupervisorYesterday.isEmpty()){
                
                leg_time_error.visibility = View.VISIBLE
                leg_time_actual.visibility = View.GONE
            }else{
                leg_time_actual.text =  legTimeServiceActualSupervisorYesterday
            }


            if (yesterdayViewSupervisorServiceData?.eADT?.legTime?.status != null) {
                when {
                    yesterdayViewSupervisorServiceData.eADT.legTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.eADT.legTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (yesterdayViewSupervisorServiceData?.extremeDelivery?.status != null) {
                when {
                    yesterdayViewSupervisorServiceData.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            //Extreme Delivery
            val extremeDeliveryServiceGoalSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.extremeDelivery?.goal?.amount, yesterdayViewSupervisorServiceData?.extremeDelivery?.goal?.percentage, yesterdayViewSupervisorServiceData?.extremeDelivery?.goal?.value)
            val extremeDeliveryServiceVarianceSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.extremeDelivery?.variance?.amount, yesterdayViewSupervisorServiceData?.extremeDelivery?.variance?.percentage, yesterdayViewSupervisorServiceData?.extremeDelivery?.variance?.value)
            val extremeDeliveryServiceActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.extremeDelivery?.actual?.amount, yesterdayViewSupervisorServiceData?.extremeDelivery?.actual?.percentage, yesterdayViewSupervisorServiceData?.extremeDelivery?.actual?.value)
            extremeDeliveryData(extremeDeliveryServiceGoalSupervisorYesterday,extremeDeliveryServiceVarianceSupervisorYesterday,extremeDeliveryServiceActualSupervisorYesterday)


            // delivery   order count
            val deliveryCountActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.amount, yesterdayViewSupervisorServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.percentage, yesterdayViewSupervisorServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.value)
            if(deliveryCountActualSupervisorYesterday.isEmpty()){
                
                delivery_order_error.visibility = View.VISIBLE
                delivery_order_actual.visibility = View.GONE
            }else{
                delivery_order_actual.text =  deliveryCountActualSupervisorYesterday
            }

            if (yesterdayViewSupervisorServiceData?.extremeDelivery?.deliveryOrderCount?.status != null) {

                when {
                    yesterdayViewSupervisorServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // extreme_delivery_oc_actual
            val extremeDeliveryCountActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.amount, yesterdayViewSupervisorServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.percentage, yesterdayViewSupervisorServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.value)
            if(extremeDeliveryCountActualSupervisorYesterday.isEmpty()){
                
                extreme_delivery_oc_actual_error.visibility = View.VISIBLE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
            }else{
                extreme_delivery_oc_actual.text =  extremeDeliveryCountActualSupervisorYesterday
            }

            if (yesterdayViewSupervisorServiceData?.extremeDelivery?.extremeDeliveryCount?.status != null) {
                when {
                    yesterdayViewSupervisorServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // Singles
            if ( yesterdayViewSupervisorServiceData?.singles?.status != null) {
                when {
                    yesterdayViewSupervisorServiceData.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val singleGoalSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.singles?.goal?.amount, yesterdayViewSupervisorServiceData?.singles?.goal?.percentage, yesterdayViewSupervisorServiceData?.singles?.goal?.value)
            val singleServiceVarianceSupervisorYesterday= Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.singles?.variance?.amount, yesterdayViewSupervisorServiceData?.singles?.variance?.percentage, yesterdayViewSupervisorServiceData?.singles?.variance?.value)
            val singleServiceActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.singles?.actual?.amount, yesterdayViewSupervisorServiceData?.singles?.actual?.percentage, yesterdayViewSupervisorServiceData?.singles?.actual?.value)
            singleData(singleGoalSupervisorYesterday,singleServiceVarianceSupervisorYesterday,singleServiceActualSupervisorYesterday)

            // double
            val doubleServiceActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.singles?._doubles?.actual?.amount, yesterdayViewSupervisorServiceData?.singles?._doubles?.actual?.percentage, yesterdayViewSupervisorServiceData?.singles?._doubles?.actual?.value)
            if(doubleServiceActualSupervisorYesterday.isEmpty()){
                
                double_percentage_error.visibility = View.VISIBLE
                double_percentage.visibility = View.GONE
            }else{
                double_percentage.text =  doubleServiceActualSupervisorYesterday
            }

            if (yesterdayViewSupervisorServiceData?.singles?._doubles?.status != null) {

                when {
                    yesterdayViewSupervisorServiceData.singles._doubles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.singles._doubles.status.toString() == resources.getString(R.string.under_limit) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // triple
            val tripleServiceActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.singles?.triples?.actual?.amount, yesterdayViewSupervisorServiceData?.singles?.triples?.actual?.percentage, yesterdayViewSupervisorServiceData?.singles?.triples?.actual?.value)
            if(tripleServiceActualSupervisorYesterday.isEmpty()){
                
                triple_percentage_error.visibility = View.VISIBLE
                triple_percentage.visibility = View.GONE
            }else{
                triple_percentage.text =  tripleServiceActualSupervisorYesterday
            }

            if (yesterdayViewSupervisorServiceData?.singles?.triples?.status != null) {
                when {
                    yesterdayViewSupervisorServiceData.singles.triples.status.toString() == resources.getString(R.string.out_of_range) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.singles.triples.status.toString() == resources.getString(R.string.under_limit) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //Load time
            val loadTimeGoalSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.loadTime?.goal?.amount, yesterdayViewSupervisorServiceData?.loadTime?.goal?.percentage, yesterdayViewSupervisorServiceData?.loadTime?.goal?.value)
            val loadTimeServiceVarianceSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.loadTime?.variance?.amount, yesterdayViewSupervisorServiceData?.loadTime?.variance?.percentage, yesterdayViewSupervisorServiceData?.loadTime?.variance?.value)
            val loadTimeServiceActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.loadTime?.actual?.amount, yesterdayViewSupervisorServiceData?.loadTime?.actual?.percentage, yesterdayViewSupervisorServiceData?.loadTime?.actual?.value)
            loadTimeData(loadTimeGoalSupervisorYesterday,loadTimeServiceVarianceSupervisorYesterday,loadTimeServiceActualSupervisorYesterday)

            if ( yesterdayViewSupervisorServiceData?.loadTime?.status != null) {
                when {
                    yesterdayViewSupervisorServiceData.loadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.loadTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

             //carry_out_load_time_percentage
            val carryOutServiceActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.loadTime?.carryoutLoadTime?.actual?.amount, yesterdayViewSupervisorServiceData?.loadTime?.carryoutLoadTime?.actual?.percentage, yesterdayViewSupervisorServiceData?.loadTime?.carryoutLoadTime?.actual?.value)
            if(carryOutServiceActualSupervisorYesterday.isEmpty()){
                
                carry_out_laod_time_percentage_error.visibility = View.VISIBLE
                carry_out_laod_time_percentage.visibility = View.GONE
            }else{
                carry_out_laod_time_percentage.text =  carryOutServiceActualSupervisorYesterday
            }

            if ( yesterdayViewSupervisorServiceData?.loadTime?.carryoutLoadTime?.status != null) {
                when {
                    yesterdayViewSupervisorServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //wait time
            if (yesterdayViewSupervisorServiceData?.waitTime?.status != null) {

                when {
                    yesterdayViewSupervisorServiceData.waitTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.waitTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val waitTimeGoalSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.waitTime?.goal?.amount, yesterdayViewSupervisorServiceData?.waitTime?.goal?.percentage, yesterdayViewSupervisorServiceData?.waitTime?.goal?.value)
            val waitTimeServiceVarianceSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.waitTime?.variance?.amount, yesterdayViewSupervisorServiceData?.waitTime?.variance?.percentage, yesterdayViewSupervisorServiceData?.waitTime?.variance?.value)
            val waitTimeServiceActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.waitTime?.actual?.amount, yesterdayViewSupervisorServiceData?.waitTime?.actual?.percentage, yesterdayViewSupervisorServiceData?.waitTime?.actual?.value)
            waitTimeData(waitTimeGoalSupervisorYesterday,waitTimeServiceVarianceSupervisorYesterday,waitTimeServiceActualSupervisorYesterday)

            // OTD
            val otdGoalSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.otd?.goal?.amount, yesterdayViewSupervisorServiceData?.otd?.goal?.percentage, yesterdayViewSupervisorServiceData?.otd?.goal?.value)
            val otdServiceVarianceSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.otd?.variance?.amount, yesterdayViewSupervisorServiceData?.otd?.variance?.percentage, yesterdayViewSupervisorServiceData?.otd?.variance?.value)
            val otdServiceActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.otd?.actual?.amount, yesterdayViewSupervisorServiceData?.otd?.actual?.percentage, yesterdayViewSupervisorServiceData?.otd?.actual?.value)
            otdData(otdGoalSupervisorYesterday,otdServiceVarianceSupervisorYesterday,otdServiceActualSupervisorYesterday)


            if (yesterdayViewSupervisorServiceData?.otd?.status != null) {
                when {
                    yesterdayViewSupervisorServiceData.otd.status.toString() == resources.getString(R.string.out_of_range) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.otd.status.toString() == resources.getString(R.string.under_limit) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // AOT
            if ( yesterdayViewSupervisorServiceData?.aot?.status != null) {
                when {
                    yesterdayViewSupervisorServiceData.aot.status.toString() == resources.getString(R.string.out_of_range) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.aot.status.toString() == resources.getString(R.string.under_limit) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            val aotGoalSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.aot?.goal?.amount, yesterdayViewSupervisorServiceData?.aot?.goal?.percentage, yesterdayViewSupervisorServiceData?.aot?.goal?.value)
            val aotServiceVarianceSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.aot?.variance?.amount, yesterdayViewSupervisorServiceData?.aot?.variance?.percentage, yesterdayViewSupervisorServiceData?.aot?.variance?.value)
            val aotServiceActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.aot?.actual?.amount, yesterdayViewSupervisorServiceData?.aot?.actual?.percentage, yesterdayViewSupervisorServiceData?.aot?.actual?.value)
            aotData(aotGoalSupervisorYesterday,aotServiceVarianceSupervisorYesterday,aotServiceActualSupervisorYesterday)

            //HangUp Service
            val hangUpServiceActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.aot?.hangUps?.actual?.amount, yesterdayViewSupervisorServiceData?.aot?.hangUps?.actual?.percentage, yesterdayViewSupervisorServiceData?.aot?.hangUps?.actual?.value)
            if(hangUpServiceActualSupervisorYesterday.isEmpty()){
                
                hang_up_percentage_error.visibility = View.VISIBLE
                hang_up_percentage.visibility = View.GONE
            }else{
                hang_up_percentage.text =  hangUpServiceActualSupervisorYesterday
            }


            // Out the door
            if ( yesterdayViewSupervisorServiceData?.outTheDoor?.status != null) {

                when {
                    yesterdayViewSupervisorServiceData.outTheDoor.status.toString() == resources.getString(R.string.out_of_range) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.outTheDoor.status.toString() == resources.getString(R.string.under_limit) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            val outDoorGoalSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.outTheDoor?.goal?.amount, yesterdayViewSupervisorServiceData?.outTheDoor?.goal?.percentage, yesterdayViewSupervisorServiceData?.outTheDoor?.goal?.value)
            val outDoorServiceVarianceSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.outTheDoor?.variance?.amount, yesterdayViewSupervisorServiceData?.outTheDoor?.variance?.percentage, yesterdayViewSupervisorServiceData?.outTheDoor?.variance?.value)
            val outDoorServiceActualSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.outTheDoor?.actual?.amount, yesterdayViewSupervisorServiceData?.outTheDoor?.actual?.percentage, yesterdayViewSupervisorServiceData?.outTheDoor?.actual?.value)
            outDoorData(outDoorGoalSupervisorYesterday,outDoorServiceVarianceSupervisorYesterday,outDoorServiceActualSupervisorYesterday)

            // csat

            val csatSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.total?.actual?.amount, yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.total?.actual?.percentage, yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.total?.actual?.value)
            if(csatSupervisorYesterday.isEmpty()){
                
                csat_goal_error.visibility = View.VISIBLE
                csat_goal.visibility = View.GONE
            }else{
                csat_goal.text =  csatSupervisorYesterday
            }

            val weeklySupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.amount, yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.percentage, yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.value)
            if(weeklySupervisorYesterday.isEmpty()){
                
                weekly_percentage_error.visibility = View.VISIBLE
                weekly_percentage.visibility = View.GONE
            }else{
                weekly_percentage.text =  weeklySupervisorYesterday
            }

            val ccSupervisorYesterday = Validation().checkAmountPercentageValue(this, yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.amount, yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.percentage, yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.value)
            if(ccSupervisorYesterday.isEmpty()){
                
                cc_per_100_percentage_error.visibility = View.VISIBLE
                cc_per_100_percentage.visibility = View.GONE
            }else{
                cc_per_100_percentage.text =  ccSupervisorYesterday
            }

            csat_display.text =
                yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.total?.displayName
                    ?: getString(R.string.csat_text)
            weekly_text.text =
                yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName
                    ?: getString(R.string.weekly_text)
            cc_per_100_text.text =
                yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName
                    ?: getString(
                        R.string.cc_per_100_text)

            if ( yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.total?.status != null) {
                when {
                    yesterdayViewSupervisorServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.green))
                    } else -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            if ( yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.status != null) {

                when {
                    yesterdayViewSupervisorServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if ( yesterdayViewSupervisorServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.status != null) {
                when {
                    yesterdayViewSupervisorServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.red))
                    }
                    yesterdayViewSupervisorServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
        }catch (e:Exception){
            Logger.error(e.message.toString(),"Service Overview Yesterday KPI")
        }

    }

    private fun rangeViewSupervisorService(serviceTodayDetail: SupervisorOverviewRangeQuery.Supervisor) {
        try{

            val rangeViewSupervisorServiceData = serviceTodayDetail.kpis?.stores?.period?.service

            Logger.info("Service Period Range", "Service Overview KPI")

            // display name
          
            Validation().checkNullValueToShowView(this, rangeViewSupervisorServiceData?.aot?.displayName, aot_parent)
            Validation().checkNullValueToShowView(this, rangeViewSupervisorServiceData?.aot?.hangUps?.displayName, hang_up_parent)
            Validation().checkNullValueToShowView(this, rangeViewSupervisorServiceData?.aot?.hangUps?.displayName, hang_up_parent)

            Validation().checkNullValueToShowView(this, rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.total?.displayName, csat_meter_parent)
            Validation().checkNullValueToShowView(this, rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName, weekly_parent)
            Validation().checkNullValueToShowView(this, rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName, cc_per_100_text_parent)

            if (rangeViewSupervisorServiceData != null) {
                showServiceNarrativeData(rangeViewSupervisorServiceData.narrative.toString())
            }

            if(rangeViewSupervisorServiceData?.aot!= null){
                aot_view.visibility= View.VISIBLE
            }else{
                aot_view.visibility= View.GONE
            }

            out_the_door_view.visibility= View.GONE
            csat_view.visibility= View.GONE


            leg_time_text.text = rangeViewSupervisorServiceData?.eADT?.legTime?.displayName?: ""
            delivery_order_count.text = rangeViewSupervisorServiceData?.extremeDelivery?.deliveryOrderCount?.displayName?: ""
            extreme_delivery_oc_text.text = rangeViewSupervisorServiceData?.extremeDelivery?.extremeDeliveryCount?.displayName ?: ""
            double_text.text = rangeViewSupervisorServiceData?.singles?._doubles?.displayName ?: ""
            triples_text.text = rangeViewSupervisorServiceData?.singles?.triples?.displayName ?: ""
            carry_out_laod_time_text.text = rangeViewSupervisorServiceData?.loadTime?.carryoutLoadTime?.displayName ?: ""
            hang_up_text.text = rangeViewSupervisorServiceData?.aot?.hangUps?.displayName ?: ""
            eadt_display.text = rangeViewSupervisorServiceData?.eADT?.displayName
            extreme_delivery_display.text = rangeViewSupervisorServiceData?.extremeDelivery?.displayName
            single_display.text = rangeViewSupervisorServiceData?.singles?.displayName
            load_time_display.text = rangeViewSupervisorServiceData?.loadTime?.displayName
            wait_time_display.text = rangeViewSupervisorServiceData?.waitTime?.displayName
            otd_display.text = rangeViewSupervisorServiceData?.otd?.displayName
            aot_display.text = rangeViewSupervisorServiceData?.aot?.displayName
            out_the_door_display.text = rangeViewSupervisorServiceData?.outTheDoor?.displayName

            service_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (service_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.service_text)
                        }
                        y = service_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            if ( rangeViewSupervisorServiceData?.eADT?.status != null) {
                when {
                    rangeViewSupervisorServiceData.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        eadt_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val eatServiceGoalSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.eADT?.goal?.amount, rangeViewSupervisorServiceData?.eADT?.goal?.percentage, rangeViewSupervisorServiceData?.eADT?.goal?.value)
            val eatServiceVarianceSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.eADT?.variance?.amount, rangeViewSupervisorServiceData?.eADT?.variance?.percentage, rangeViewSupervisorServiceData?.eADT?.variance?.value)
            val eatServiceActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.eADT?.actual?.amount, rangeViewSupervisorServiceData?.eADT?.actual?.percentage, rangeViewSupervisorServiceData?.eADT?.actual?.value)
            eatData(eatServiceGoalSupervisorRange,eatServiceVarianceSupervisorRange,eatServiceActualSupervisorRange)

            // leg time
            val legTimeServiceActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.eADT?.legTime?.actual?.amount, rangeViewSupervisorServiceData?.eADT?.legTime?.actual?.percentage, rangeViewSupervisorServiceData?.eADT?.legTime?.actual?.value)
            if(legTimeServiceActualSupervisorRange.isEmpty()){
               
               leg_time_error.visibility = View.VISIBLE
               leg_time_actual.visibility = View.GONE
           }else{
               leg_time_actual.text =  legTimeServiceActualSupervisorRange
           }

            if (rangeViewSupervisorServiceData?.eADT?.legTime?.status != null) {
                when {
                    rangeViewSupervisorServiceData.eADT.legTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.eADT.legTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        leg_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        leg_time_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (rangeViewSupervisorServiceData?.extremeDelivery?.status != null) {
                when {
                    rangeViewSupervisorServiceData.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            //Extreme Delivery
            val extremeDeliveryServiceGoalSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.extremeDelivery?.goal?.amount, rangeViewSupervisorServiceData?.extremeDelivery?.goal?.percentage, rangeViewSupervisorServiceData?.extremeDelivery?.goal?.value)
            val extremeDeliveryServiceVarianceSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.extremeDelivery?.variance?.amount, rangeViewSupervisorServiceData?.extremeDelivery?.variance?.percentage, rangeViewSupervisorServiceData?.extremeDelivery?.variance?.value)
            val extremeDeliveryServiceActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.extremeDelivery?.actual?.amount, rangeViewSupervisorServiceData?.extremeDelivery?.actual?.percentage, rangeViewSupervisorServiceData?.extremeDelivery?.actual?.value)
            extremeDeliveryData(extremeDeliveryServiceGoalSupervisorRange,extremeDeliveryServiceVarianceSupervisorRange,extremeDeliveryServiceActualSupervisorRange)

            // delivery   order count
            val deliveryCountActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.amount, rangeViewSupervisorServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.percentage, rangeViewSupervisorServiceData?.extremeDelivery?.deliveryOrderCount?.actual?.value)
            if(deliveryCountActualSupervisorRange.isEmpty()){
                
                delivery_order_error.visibility = View.VISIBLE
                delivery_order_actual.visibility = View.GONE
            }else{
                delivery_order_actual.text =  deliveryCountActualSupervisorRange
            }

            if (rangeViewSupervisorServiceData?.extremeDelivery?.deliveryOrderCount?.status != null) {

                when {
                    rangeViewSupervisorServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.extremeDelivery.deliveryOrderCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        delivery_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_order_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // extreme_delivery_oc_actual
            val extremeDeliveryCountActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.amount, rangeViewSupervisorServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.percentage, rangeViewSupervisorServiceData?.extremeDelivery?.extremeDeliveryCount?.actual?.value)
            if(extremeDeliveryCountActualSupervisorRange.isEmpty()){
                
                extreme_delivery_oc_actual_error.visibility = View.VISIBLE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
                extreme_delivery_oc_actual.visibility = View.GONE
            }else{
                extreme_delivery_oc_actual.text =  extremeDeliveryCountActualSupervisorRange
            }

            if (rangeViewSupervisorServiceData?.extremeDelivery?.extremeDeliveryCount?.status != null) {
                when {
                    rangeViewSupervisorServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.extremeDelivery.extremeDeliveryCount.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        extreme_delivery_oc_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        extreme_delivery_oc_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // Singles
            if ( rangeViewSupervisorServiceData?.singles?.status != null) {
                when {
                    rangeViewSupervisorServiceData.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        single_percentage_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            val singleGoalSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.singles?.goal?.amount, rangeViewSupervisorServiceData?.singles?.goal?.percentage, rangeViewSupervisorServiceData?.singles?.goal?.value)
            val singleServiceVarianceSupervisorRange= Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.singles?.variance?.amount, rangeViewSupervisorServiceData?.singles?.variance?.percentage, rangeViewSupervisorServiceData?.singles?.variance?.value)
            val singleServiceActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.singles?.actual?.amount, rangeViewSupervisorServiceData?.singles?.actual?.percentage, rangeViewSupervisorServiceData?.singles?.actual?.value)
            singleData(singleGoalSupervisorRange,singleServiceVarianceSupervisorRange,singleServiceActualSupervisorRange)

            // double
            val doubleServiceActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.singles?._doubles?.actual?.amount, rangeViewSupervisorServiceData?.singles?._doubles?.actual?.percentage, rangeViewSupervisorServiceData?.singles?._doubles?.actual?.value)
            if(doubleServiceActualSupervisorRange.isEmpty()){
                
                double_percentage_error.visibility = View.VISIBLE
                double_percentage.visibility = View.GONE
            }else{
                double_percentage.text =  doubleServiceActualSupervisorRange
            }

            if (rangeViewSupervisorServiceData?.singles?._doubles?.status != null) {

                when {
                    rangeViewSupervisorServiceData.singles._doubles.status.toString() == resources.getString(R.string.out_of_range) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.singles._doubles.status.toString() == resources.getString(R.string.under_limit) -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        double_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        double_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // triple
            val tripleServiceActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.singles?.triples?.actual?.amount, rangeViewSupervisorServiceData?.singles?.triples?.actual?.percentage, rangeViewSupervisorServiceData?.singles?.triples?.actual?.value)
            if(tripleServiceActualSupervisorRange.isEmpty()){
                
                triple_percentage_error.visibility = View.VISIBLE
                triple_percentage.visibility = View.GONE
            }else{
                triple_percentage.text =  tripleServiceActualSupervisorRange
            }

            if (rangeViewSupervisorServiceData?.singles?.triples?.status != null) {
                when {
                    rangeViewSupervisorServiceData.singles.triples.status.toString() == resources.getString(R.string.out_of_range) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.singles.triples.status.toString() == resources.getString(R.string.under_limit) -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        triple_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        triple_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //Load time
            val loadTimeGoalSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.loadTime?.goal?.amount, rangeViewSupervisorServiceData?.loadTime?.goal?.percentage, rangeViewSupervisorServiceData?.loadTime?.goal?.value)
            val loadTimeServiceVarianceSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.loadTime?.variance?.amount, rangeViewSupervisorServiceData?.loadTime?.variance?.percentage, rangeViewSupervisorServiceData?.loadTime?.variance?.value)
            val loadTimeServiceActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.loadTime?.actual?.amount, rangeViewSupervisorServiceData?.loadTime?.actual?.percentage, rangeViewSupervisorServiceData?.loadTime?.actual?.value)
            loadTimeData(loadTimeGoalSupervisorRange,loadTimeServiceVarianceSupervisorRange,loadTimeServiceActualSupervisorRange)

            if ( rangeViewSupervisorServiceData?.loadTime?.status != null) {
                when {
                    rangeViewSupervisorServiceData.loadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.loadTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        load_time_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //carry_out_load_time_percentage
            val carryOutServiceActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.loadTime?.carryoutLoadTime?.actual?.amount, rangeViewSupervisorServiceData?.loadTime?.carryoutLoadTime?.actual?.percentage, rangeViewSupervisorServiceData?.loadTime?.carryoutLoadTime?.actual?.value)
            if(carryOutServiceActualSupervisorRange.isEmpty()){
                
                carry_out_laod_time_percentage_error.visibility = View.VISIBLE
                carry_out_laod_time_percentage.visibility = View.GONE
            }else{
                carry_out_laod_time_percentage.text =  carryOutServiceActualSupervisorRange
            }
            if ( rangeViewSupervisorServiceData?.loadTime?.carryoutLoadTime?.status != null) {
                when {
                    rangeViewSupervisorServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.loadTime.carryoutLoadTime.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        carry_out_laod_time_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carry_out_laod_time_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            //wait time
            val waitTimeGoalSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.waitTime?.goal?.amount, rangeViewSupervisorServiceData?.waitTime?.goal?.percentage, rangeViewSupervisorServiceData?.waitTime?.goal?.value)
            val waitTimeServiceVarianceSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.waitTime?.variance?.amount, rangeViewSupervisorServiceData?.waitTime?.variance?.percentage, rangeViewSupervisorServiceData?.waitTime?.variance?.value)
            val waitTimeServiceActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.waitTime?.actual?.amount, rangeViewSupervisorServiceData?.waitTime?.actual?.percentage, rangeViewSupervisorServiceData?.waitTime?.actual?.value)
            waitTimeData(waitTimeGoalSupervisorRange,waitTimeServiceVarianceSupervisorRange,waitTimeServiceActualSupervisorRange)
            if (rangeViewSupervisorServiceData?.waitTime?.status != null) {

                when {
                    rangeViewSupervisorServiceData.waitTime.status.toString() == resources.getString(R.string.out_of_range) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.red_circle,
                                0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.waitTime.status.toString() == resources.getString(R.string.under_limit) -> {
                        wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.green_circle,
                                0
                        )
                        wait_time_actual.setTextColor(getColor(R.color.green))
                    } else-> {
                    wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    wait_time_actual.setTextColor(getColor(R.color.text_color))
                }
                }
            }

            // OTD
            val otdGoalSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.otd?.goal?.amount, rangeViewSupervisorServiceData?.otd?.goal?.percentage, rangeViewSupervisorServiceData?.otd?.goal?.value)
            val otdServiceVarianceSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.otd?.variance?.amount, rangeViewSupervisorServiceData?.otd?.variance?.percentage, rangeViewSupervisorServiceData?.otd?.variance?.value)
            val otdServiceActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.otd?.actual?.amount, rangeViewSupervisorServiceData?.otd?.actual?.percentage, rangeViewSupervisorServiceData?.otd?.actual?.value)
            otdData(otdGoalSupervisorRange,otdServiceVarianceSupervisorRange,otdServiceActualSupervisorRange)
            if (rangeViewSupervisorServiceData?.otd?.status != null) {
                when {
                    rangeViewSupervisorServiceData.otd.status.toString() == resources.getString(R.string.out_of_range) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.otd.status.toString() == resources.getString(R.string.under_limit) -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        otd_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            // AOT
            if ( rangeViewSupervisorServiceData?.aot?.status != null) {
                when {
                    rangeViewSupervisorServiceData.aot.status.toString() == resources.getString(R.string.out_of_range) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.aot.status.toString() == resources.getString(R.string.under_limit) -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.green))
                    } else -> {
                        aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        aot_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val aotGoalSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.aot?.goal?.amount, rangeViewSupervisorServiceData?.aot?.goal?.percentage, rangeViewSupervisorServiceData?.aot?.goal?.value)
            val aotServiceVarianceSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.aot?.variance?.amount, rangeViewSupervisorServiceData?.aot?.variance?.percentage, rangeViewSupervisorServiceData?.aot?.variance?.value)
            val aotServiceActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.aot?.actual?.amount, rangeViewSupervisorServiceData?.aot?.actual?.percentage, rangeViewSupervisorServiceData?.aot?.actual?.value)
            aotData(aotGoalSupervisorRange,aotServiceVarianceSupervisorRange,aotServiceActualSupervisorRange)

            //HangUp Service
            val hangUpServiceActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.aot?.hangUps?.actual?.amount, rangeViewSupervisorServiceData?.aot?.hangUps?.actual?.percentage, rangeViewSupervisorServiceData?.aot?.hangUps?.actual?.value)
            if(hangUpServiceActualSupervisorRange.isEmpty()){
                
                hang_up_percentage_error.visibility = View.VISIBLE
                hang_up_percentage.visibility = View.GONE
            }else{
                hang_up_percentage.text =  hangUpServiceActualSupervisorRange
            }

            // Out the door
            if ( rangeViewSupervisorServiceData?.outTheDoor?.status != null) {

                when {
                    rangeViewSupervisorServiceData.outTheDoor.status.toString() == resources.getString(R.string.out_of_range) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.outTheDoor.status.toString() == resources.getString(R.string.under_limit) -> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.green))
                    } else-> {
                        out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        out_door_actual.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            val outDoorGoalSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.outTheDoor?.goal?.amount, rangeViewSupervisorServiceData?.outTheDoor?.goal?.percentage, rangeViewSupervisorServiceData?.outTheDoor?.goal?.value)
            val outDoorServiceVarianceSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.outTheDoor?.variance?.amount, rangeViewSupervisorServiceData?.outTheDoor?.variance?.percentage, rangeViewSupervisorServiceData?.outTheDoor?.variance?.value)
            val outDoorServiceActualSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.outTheDoor?.actual?.amount, rangeViewSupervisorServiceData?.outTheDoor?.actual?.percentage, rangeViewSupervisorServiceData?.outTheDoor?.actual?.value)
            outDoorData(outDoorGoalSupervisorRange,outDoorServiceVarianceSupervisorRange,outDoorServiceActualSupervisorRange)

            // csat
            val csatSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.total?.actual?.amount, rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.total?.actual?.percentage, rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.total?.actual?.value)
            if(csatSupervisorRange.isEmpty()){
                
                csat_goal_error.visibility = View.VISIBLE
                csat_goal.visibility = View.GONE
            }else{
                csat_goal.text =  csatSupervisorRange
            }

            val weeklySupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.amount, rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.percentage, rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.actual?.value)
            if(weeklySupervisorRange.isEmpty()){
                
                weekly_percentage_error.visibility = View.VISIBLE
                weekly_percentage.visibility = View.GONE
            }else{
                weekly_percentage.text =  weeklySupervisorRange
            }

            val ccSupervisorRange = Validation().checkAmountPercentageValue(this, rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.amount, rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.percentage, rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.actual?.value)
            if(ccSupervisorRange.isEmpty()){
                
                cc_per_100_percentage_error.visibility = View.VISIBLE
                cc_per_100_percentage.visibility = View.GONE
            }else{
                cc_per_100_percentage.text =  ccSupervisorRange
            }
            csat_display.text =
                rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.total?.displayName
                   
            weekly_text.text =
                rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.displayName
                    
            cc_per_100_text.text =
                rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.displayName
                   

            if ( rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.total?.status != null) {
                when {
                    rangeViewSupervisorServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.csatMeterRating90dayAvg.total.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.green))
                    } else -> {
                        csat_goal.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        csat_goal.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            if ( rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.weeklyCCCClosureRate?.status != null) {

                when {
                    rangeViewSupervisorServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.csatMeterRating90dayAvg.weeklyCCCClosureRate.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        weekly_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        weekly_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if ( rangeViewSupervisorServiceData?.csatMeterRating90dayAvg?.cccPer1000Orders90DayAvg?.status != null) {
                when {
                    rangeViewSupervisorServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.out_of_range) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.red))
                    }
                    rangeViewSupervisorServiceData.csatMeterRating90dayAvg.cccPer1000Orders90DayAvg.status.toString() == resources.getString(
                        R.string.under_limit) -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.green))
                    } else -> {
                        cc_per_100_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        cc_per_100_percentage.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
        }catch (e:Exception){
            Logger.error(e.message.toString(),"Service Overview Period Range KPI")
        }

    }

    override fun onBackPressed() {
        Logger.info("Back-pressed", "Service Overview KPI")
        finish()
    }

    private fun eatData(eatGoal: String, eatVariance: String, eatActual: String){
        if(eatGoal.isEmpty() && eatVariance.isEmpty() && eatActual.isEmpty()){
            
            eadt_error.visibility = View.VISIBLE
            eadt_goal.visibility = View.GONE
            eadt_variance.visibility = View.GONE
            eadt_actual.visibility = View.GONE
            val paramsEatServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsEatServiceError.weight = 2.0f
            eadt_display.layoutParams = paramsEatServiceError
        }
        else if(eatGoal.isNotEmpty() && eatVariance.isNotEmpty() && eatActual.isNotEmpty()){
            eadt_goal.text = eatGoal
            eadt_variance.text = eatVariance
            eadt_actual.text = eatActual
        }else{
            if(eatGoal.isEmpty()){
                
                eadt_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                eadt_goal.text = eatGoal
            }
            if(eatVariance.isEmpty()){
                
                eadt_variance.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                eadt_variance.text = eatVariance
            }
            if(eatActual.isEmpty()){
                
                eadt_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                eadt_actual.text = eatActual
            }
        }
    }

    private fun extremeDeliveryData(extremeGoal: String, extremeVariance: String, extremeActual: String){
        if(extremeGoal.isEmpty() && extremeVariance.isEmpty() && extremeActual.isEmpty()){
            
            extreme_delivery_error.visibility = View.VISIBLE
            extreme_delivery_goal.visibility = View.GONE
            extreme_delivery_variance.visibility = View.GONE
            extreme_delivery_actual.visibility = View.GONE

            val paramsExtremeDeliveryServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsExtremeDeliveryServiceError.weight = 2.0f
            extreme_delivery_display.layoutParams = paramsExtremeDeliveryServiceError
        }
        else if(extremeGoal.isNotEmpty() && extremeVariance.isNotEmpty() && extremeActual.isNotEmpty()){
            extreme_delivery_goal.text = extremeGoal
            extreme_delivery_variance.text = extremeVariance
            extreme_delivery_actual.text = extremeActual
        }else{
            if(extremeGoal.isEmpty()){
                
                extreme_delivery_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                extreme_delivery_goal.text = extremeGoal
            }
            if(extremeVariance.isEmpty()){
                
                extreme_delivery_variance.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                extreme_delivery_variance.text = extremeVariance
            }
            if(extremeActual.isEmpty()){
                
                extreme_delivery_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                extreme_delivery_actual.text = extremeActual
            }
        }
    }

    private fun singleData(singleGoal: String, singleVariance: String, singleActual: String){
        if(singleGoal.isEmpty() && singleVariance.isEmpty() && singleActual.isEmpty()){
            
            single_percentage_error.visibility = View.VISIBLE

            single_percentage_goal.visibility = View.GONE
            single_percentage_variance.visibility = View.GONE
            single_percentage_actual.visibility = View.GONE

            val paramsSingleServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsSingleServiceError.weight = 2.0f
            single_display.layoutParams = paramsSingleServiceError
        }
        else if(singleGoal.isNotEmpty() && singleVariance.isNotEmpty() && singleActual.isNotEmpty()){
            single_percentage_goal.text = singleGoal
            single_percentage_variance.text = singleVariance
            single_percentage_actual.text = singleActual
        }else{
            if(singleGoal.isEmpty()){
                
                single_percentage_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                single_percentage_goal.text = singleGoal
            }
            if(singleVariance.isEmpty()){
                
                single_percentage_variance.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                single_percentage_variance.text = singleVariance
            }
            if(singleActual.isEmpty()){
                
                single_percentage_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                single_percentage_actual.text = singleActual
            }
        }
    }

    private fun waitTimeData(waitTimeGoal: String, waitTimeVariance: String, waitTimeActual: String){
        if(waitTimeGoal.isEmpty() && waitTimeVariance.isEmpty() && waitTimeActual.isEmpty()){
            
            wait_time_error.visibility = View.VISIBLE
            wait_time_goal.visibility = View.GONE
            wait_time_variance.visibility = View.GONE
            wait_time_actual.visibility = View.GONE

            val paramsWaitTimeServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsWaitTimeServiceError.weight = 2.0f
            wait_time_display.layoutParams = paramsWaitTimeServiceError
        }
        else if(waitTimeGoal.isNotEmpty() && waitTimeVariance.isNotEmpty() && waitTimeActual.isNotEmpty()){
            wait_time_goal.text = waitTimeGoal
            wait_time_variance.text = waitTimeVariance
            wait_time_actual.text = waitTimeActual
        }else{
            if(waitTimeGoal.isEmpty()){
                
                wait_time_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                wait_time_goal.text = waitTimeGoal
            }
            if(waitTimeVariance.isEmpty()){
                
                wait_time_variance.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                wait_time_variance.text = waitTimeVariance
            }
            if(waitTimeActual.isEmpty()){
                
                wait_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                wait_time_actual.text = waitTimeActual
            }
        }
    }

    private fun otdData(otdGoal: String, otdVariance: String, otdActual: String){
        if(otdGoal.isEmpty() && otdVariance.isEmpty() && otdActual.isEmpty()){
            
            otd_error.visibility = View.VISIBLE
            otd_goal.visibility = View.GONE
            otd_variance.visibility = View.GONE
            otd_actual.visibility = View.GONE

            val paramsOtdServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsOtdServiceError.weight = 2.0f
            otd_display.layoutParams = paramsOtdServiceError
        }
        else if(otdGoal.isNotEmpty() && otdVariance.isNotEmpty() && otdActual.isNotEmpty()){
            otd_goal.text = otdGoal
            otd_variance.text = otdVariance
            otd_actual.text = otdActual
        }else{
            if(otdGoal.isEmpty()){
                
                otd_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                otd_goal.text = otdGoal
            }
            if(otdVariance.isEmpty()){
                
                otd_variance.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                otd_variance.text = otdVariance
            }
            if(otdActual.isEmpty()){
                
                otd_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                otd_actual.text = otdActual
            }
        }
    }

    private fun loadTimeData(loadTimeGoal: String, loadTimeVariance: String, loadTimeActual: String){
        if(loadTimeGoal.isEmpty() && loadTimeVariance.isEmpty() && loadTimeActual.isEmpty()){
            
            load_time_error.visibility = View.VISIBLE
            load_time_goal.visibility = View.GONE
            load_time_variance.visibility = View.GONE
            load_time_actual.visibility = View.GONE

            val paramsLoadTimeServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsLoadTimeServiceError.weight = 2.0f
            load_time_display.layoutParams = paramsLoadTimeServiceError
        }
        else if(loadTimeGoal.isNotEmpty() && loadTimeVariance.isNotEmpty() && loadTimeActual.isNotEmpty()){
            load_time_goal.text = loadTimeGoal
            load_time_variance.text = loadTimeVariance
            load_time_actual.text = loadTimeActual
        }else{
            if(loadTimeGoal.isEmpty()){
                
                load_time_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                load_time_goal.text = loadTimeGoal
            }
            if(loadTimeVariance.isEmpty()){
                
                load_time_variance.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                load_time_variance.text = loadTimeVariance
            }
            if(loadTimeActual.isEmpty()){
                
                load_time_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                load_time_actual.text = loadTimeActual
            }
        }
    }

    private fun aotData(aotGoal: String, aotVariance: String, aotActual: String){
        if(aotGoal.isEmpty() && aotVariance.isEmpty() && aotActual.isEmpty()){
            
            aot_error.visibility = View.VISIBLE
            aot_goal.visibility = View.GONE
            aot_variance.visibility = View.GONE
            aot_actual.visibility = View.GONE

            val paramsAOTServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsAOTServiceError.weight = 2.0f
            aot_display.layoutParams = paramsAOTServiceError
        }
        else if(aotGoal.isNotEmpty() && aotVariance.isNotEmpty() && aotActual.isNotEmpty()){
            aot_goal.text = aotGoal
            aot_variance.text = aotVariance
            aot_actual.text = aotActual
        }else{
            if(aotGoal.isEmpty()){
                
                aot_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                aot_goal.text = aotGoal
            }
            if(aotVariance.isEmpty()){
                
                aot_variance.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                aot_variance.text = aotVariance
            }
            if(aotActual.isEmpty()){
                
                aot_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                aot_actual.text = aotActual
            }
        }
    }

    private fun outDoorData(outDoorGoal: String, outDoorVariance: String, outDoorActual: String){
        if(outDoorGoal.isEmpty() && outDoorVariance.isEmpty() && outDoorActual.isEmpty()){
            
            out_door_error.visibility = View.VISIBLE
            out_door_goal.visibility = View.GONE
            out_door_variance.visibility = View.GONE
            out_door_actual.visibility = View.GONE

            val paramsOutDoorServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsOutDoorServiceError.weight = 2.0f
            out_the_door_display.layoutParams = paramsOutDoorServiceError
        }
        else if(outDoorGoal.isNotEmpty() && outDoorVariance.isNotEmpty() && outDoorActual.isNotEmpty()){
            out_door_goal.text = outDoorGoal
            out_door_variance.text = outDoorVariance
            out_door_actual.text = outDoorActual
        }else{
            if(outDoorGoal.isEmpty()){
                
                out_door_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                out_door_goal.text = outDoorGoal
            }
            if(outDoorVariance.isEmpty()){
                
                out_door_variance.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                out_door_variance.text = outDoorVariance
            }
            if(outDoorActual.isEmpty()){
                
                out_door_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                out_door_actual.text = outDoorActual
            }
        }
    }

    private fun showServiceNarrativeData (narrative :String?){
        if (!narrative.isNullOrEmpty()){
            var serviceNarrative = narrative
            serviceNarrative = serviceNarrative.replace("</p>", "<br><br>")
            service_narrative_value.text = Html.fromHtml(serviceNarrative,Html.FROM_HTML_MODE_COMPACT)
        }else{
            service_narrative_value.visibility = View.INVISIBLE
        }
    }

    private fun callServicesOverviewNullApi(){
        val formattedStartDateValueService: String
        val formattedEndDateValueService: String

        val startDateValueService = StorePrefData.startDateValue
        val endDateValueService = StorePrefData.endDateValue

        if (StorePrefData.isCalendarSelected) {
            formattedStartDateValueService = startDateValueService
            formattedEndDateValueService = endDateValueService
        } else {
            formattedStartDateValueService = startDateValueService
            formattedEndDateValueService = endDateValueService
        }
        val progressDialogServiceOverview = CustomProgressDialog(this@ServiceKpiActivity)
        progressDialogServiceOverview.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodeListService = dbHelperService.getAllSelectedAreaList(true)
            val stateCodeListService = dbHelperService.getAllSelectedStoreListState(true)
            val supervisorNumberListService = dbHelperService.getAllSelectedStoreListSupervisor(true)
            val storeNumberListService = dbHelperService.getAllSelectedStoreList(true)

            val responseMissingDataService = try {
                apolloClient(this@ServiceKpiActivity).query(
                    MissingDataQuery(
                            areaCodeListService.toInput(),
                            stateCodeListService.toInput(),
                            supervisorNumberListService.toInput(),
                            storeNumberListService.toInput(),
                            EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType).toInput(),
                            formattedStartDateValueService.toInput(),
                            formattedEndDateValueService.toInput(),
                    )
                ).await()

            } catch (e: ApolloException) {
                Logger.error("Missing Data ${e.message.toString()}","Service Overview KPI")
                progressDialogServiceOverview.dismissProgressDialog()
                return@launchWhenResumed
            }
            if(responseMissingDataService.data?.missingData!=null){
                progressDialogServiceOverview.dismissProgressDialog()
                service_kpi_error_layout.visibility = View.VISIBLE
                service_kpi_error_layout.header_data_title.text  = responseMissingDataService.data?.missingData!!.header
                service_kpi_error_layout.header_data_description.text  = responseMissingDataService.data?.missingData!!.message
            }
            else{
                progressDialogServiceOverview.dismissProgressDialog()
                service_kpi_error_layout.visibility = View.GONE
            }
        }
    }

}
