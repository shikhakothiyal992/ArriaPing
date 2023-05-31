package com.arria.ping.ui.kpi.overview

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.activity_a_w_u_s_kpi.*
import kotlinx.android.synthetic.main.activity_cash.*
import kotlinx.android.synthetic.main.data_error_layout.view.*
import com.arria.ping.kpi.gm.StorePeriodRangeKPIQuery
import com.arria.ping.kpi.gm.StoreYesterdayKPIQuery
import javax.inject.Inject

@AndroidEntryPoint
class CashKpiActivity : AppCompatActivity() {

    private val gsonCashKpi = Gson()
    lateinit var dbHelperCashOverview: DatabaseHelperImpl

    @Inject
    lateinit var networkHelper: NetworkHelper

    var apiCashKpiArgumentFromFilter = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cash)
        this.setFinishOnTouchOutside(false)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dbHelperCashOverview = DatabaseHelperImpl(DatabaseBuilder.getInstance(this@CashKpiActivity))
        setCashKpiData()
        cross_button_food.setOnClickListener {
            Logger.info("Cancel Button clicked","Cash Overview KPI Screen")
            finish()
        }
    }

    private fun setCashKpiData() {

        when (StorePrefData.role) {
            getString(R.string.ceo_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiCashKpiArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiCashKpiArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val cashKpiYesterdayDetailCEO = gsonCashKpi.fromJson(
                            intent.getStringExtra("cash_data"),
                            CEOOverviewRangeQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callCashOverviewNullApi()
                            rangeOverViewCEOCashKpi(cashKpiYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val cashKpiYesterdayDetail = gsonCashKpi.fromJson(
                            intent.getStringExtra("cash_data"),
                            CEOOverviewYesterdayQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callCashOverviewNullApi()
                            yesterdayViewCEOCashKpi(cashKpiYesterdayDetail)
                        }
                        
                    }
                    IpConstants.Today -> {
                        val cashKpiTodayDetail = gsonCashKpi.fromJson(
                            intent.getStringExtra("cash_data"),
                            CEOOverviewTodayQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callCashOverviewNullApi()
                            todayViewCEOCashKpi(cashKpiTodayDetail)
                        }
                    }
                }
            }
            getString(R.string.do_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiCashKpiArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiCashKpiArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val cashKpiYesterdayDetailCEO = gsonCashKpi.fromJson(
                            intent.getStringExtra("cash_data"),
                            DOOverviewRangeQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callCashOverviewNullApi()
                            rangeOverViewDOCashKpi(cashKpiYesterdayDetailCEO)
                        }
                       
                    }
                    IpConstants.Yesterday -> {
                        val cashKpiYesterdayDetail = gsonCashKpi.fromJson(
                            intent.getStringExtra("cash_data"),
                            DOOverviewYesterdayQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callCashOverviewNullApi()
                            yesterdayViewDOCashKpi(cashKpiYesterdayDetail)
                        }
                       
                    }
                    IpConstants.Today -> {
                        val cashKpiTodayDetail = gsonCashKpi.fromJson(
                            intent.getStringExtra("cash_data"),
                            DOOverviewTodayQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callCashOverviewNullApi()
                            todayViewDOCashKpi(cashKpiTodayDetail)
                        }
                       
                    }
                }
            }
            getString(R.string.gm_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiCashKpiArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiCashKpiArgumentFromFilter) {
                    IpConstants.Yesterday -> {
                        val cashKpiYesterdayDetail = gsonCashKpi.fromJson(
                            intent.getStringExtra("cash_data"),
                            StoreYesterdayKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callCashOverviewNullApi()
                            yesterdayViewGMCashKpi(cashKpiYesterdayDetail)
                        }
                     
                    }
                    IpConstants.Today -> {
                        val cashKpiTodayDetail = gsonCashKpi.fromJson(
                            intent.getStringExtra("cash_data"),
                            StoreTodayKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callCashOverviewNullApi()
                            todayViewGMCashKpi(cashKpiTodayDetail)
                        }
                       
                    }
                    IpConstants.rangeFrom -> {
                        val cashKpiTodayDetail = gsonCashKpi.fromJson(
                            intent.getStringExtra("cash_data"),
                            StorePeriodRangeKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callCashOverviewNullApi()
                            rangeOverviewGMCashKpi(cashKpiTodayDetail)
                        }
                        
                    }
                }
            }
            getString(R.string.supervisor_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiCashKpiArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiCashKpiArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val cashKpiYesterdayDetailCEO = gsonCashKpi.fromJson(
                            intent.getStringExtra("cash_data"),
                            SupervisorOverviewRangeQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callCashOverviewNullApi()
                            rangeOverviewSupervisorCashKpi(cashKpiYesterdayDetailCEO)
                        }
                        
                    }
                    IpConstants.Yesterday -> {
                        val cashKpiYesterdayDetail = gsonCashKpi.fromJson(
                            intent.getStringExtra("cash_data"),
                            SupervisorOverviewYesterdayQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callCashOverviewNullApi()
                            yesterdayViewSupervisorCashKpi(cashKpiYesterdayDetail)
                        }
                        
                    }
                    IpConstants.Today -> {
                        val cashKpiTodayDetail = gsonCashKpi.fromJson(
                            intent.getStringExtra("cash_data"),
                            SupervisorOverviewTodayQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callCashOverviewNullApi()
                            todayViewSupervisorCashKpi(cashKpiTodayDetail)
                        }
                       
                    }
                }
            }
        }
    }

    //gm
    private fun yesterdayViewGMCashKpi(yesterdayViewGMData: StoreYesterdayKPIQuery.GeneralManager?) {
        try {
            val yesterdayGMData = yesterdayViewGMData?.kpis?.store?.yesterday?.cash

            Logger.info("Cash Yesterday", "Cash Overview KPI")

            ideal_vs_food_variance_text.text = yesterdayGMData!!.displayName ?: getString(R.string.cash_text)

            food_goal_value.text = Validation().checkAmountPercentageValue(this,
                                                                           yesterdayGMData.goal?.amount,
                                                                           yesterdayGMData.goal?.percentage,
                                                                           yesterdayGMData.goal?.value)
            food_variance_value.text = Validation().checkAmountPercentageValue(this,
                                                                               yesterdayGMData.variance?.amount,
                                                                               yesterdayGMData.variance?.percentage,
                                                                               yesterdayGMData.variance?.value)
            food_actual_value.text = Validation().checkAmountPercentageValue(this,
                                                                             yesterdayGMData.actual?.amount,
                                                                             yesterdayGMData.actual?.percentage,
                                                                             yesterdayGMData.actual?.value)
            food_sales.text = Validation().checkAmountPercentageValue(this,
                                                                      yesterdayGMData.actual?.amount,
                                                                      yesterdayGMData.actual?.percentage,
                                                                      yesterdayGMData.actual?.value)

            if (yesterdayGMData.status?.toString() != null) {

                when {
                    yesterdayGMData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    yesterdayGMData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }

                }
            }
            if (yesterdayGMData.displayName.isNullOrEmpty()) {
                cash_parent_yesterday.visibility = View.GONE
            } else {
                cash_parent_yesterday.visibility = View.VISIBLE
                cash_display.text = yesterdayGMData.displayName
            }

            val gmYesterdayCashGoal = Validation().checkAmountPercentageValue(this,
                                                                              yesterdayGMData.goal?.amount,
                                                                              yesterdayGMData.goal?.percentage,
                                                                              yesterdayGMData.goal?.value)

            val gmYesterdayCashVariance = Validation().checkAmountPercentageValue(this,
                                                                                  yesterdayGMData.variance?.amount,
                                                                                  yesterdayGMData.variance?.percentage,
                                                                                  yesterdayGMData.variance?.value)

            val gmYesterdayCashActual = Validation().checkAmountPercentageValue(this,
                                                                                yesterdayGMData.actual?.amount,
                                                                                yesterdayGMData.actual?.percentage,
                                                                                yesterdayGMData.actual?.value)

            if(gmYesterdayCashGoal.isEmpty() && gmYesterdayCashVariance.isEmpty() && gmYesterdayCashActual.isEmpty()){

                cash_goal.visibility = View.GONE
                cash_varince.visibility = View.GONE
                cash_actual.visibility = View.GONE
                cash_error.visibility = View.VISIBLE
                

                val paramsGMYesterdayCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsGMYesterdayCashError.weight = 4.0f
                cash_display.layoutParams = paramsGMYesterdayCashError

            }
            else if(gmYesterdayCashGoal.isNotEmpty() && gmYesterdayCashVariance.isNotEmpty() && gmYesterdayCashActual.isNotEmpty()){
                cash_goal.visibility = View.VISIBLE
                cash_varince.visibility = View.VISIBLE
                cash_actual.visibility = View.VISIBLE
                cash_goal.text = gmYesterdayCashGoal
                cash_varince.text = gmYesterdayCashVariance
                cash_actual.text = gmYesterdayCashActual

            }
            else{
                if(gmYesterdayCashGoal.isEmpty()){
                    
                    cash_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }
                else{
                    cash_goal.text = gmYesterdayCashGoal
                }

                if(gmYesterdayCashVariance.isEmpty()){
                    
                    cash_varince.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_varince.text = gmYesterdayCashVariance
                }

                if(gmYesterdayCashActual.isEmpty()){
                    
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_actual.text = gmYesterdayCashActual
                }
            }


            if (yesterdayGMData.status != null && gmYesterdayCashActual.isNotEmpty()) {
                when {
                    yesterdayGMData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        cash_actual.setTextColor(getColor(R.color.red))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                    }
                    yesterdayGMData.status.toString() == resources.getString(R.string.under_limit) -> {
                        cash_actual.setTextColor(getColor(R.color.green))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                    }
                    else -> {
                        cash_actual.setTextColor(getColor(R.color.text_color))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                    }

                }
            }
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Cash Overview Yesterday KPI")
        }
    }

    private fun rangeOverviewGMCashKpi(dataRangeOverviewGM: StorePeriodRangeKPIQuery.GeneralManager?) {
        try {
            val rangeOverviewGMData = dataRangeOverviewGM?.kpis?.store?.period?.cash

            Logger.info("Cash Period Range", "Cash Overview KPI")

            ideal_vs_food_variance_text.text = rangeOverviewGMData!!.displayName ?: getString(R.string.cash_text)

            food_goal_value.text = Validation().checkAmountPercentageValue(this,
                                                                           rangeOverviewGMData.goal?.amount,
                                                                           rangeOverviewGMData.goal?.percentage,
                                                                           rangeOverviewGMData.goal?.value)
            food_variance_value.text = Validation().checkAmountPercentageValue(this,
                                                                               rangeOverviewGMData.variance?.amount,
                                                                               rangeOverviewGMData.variance?.percentage,
                                                                               rangeOverviewGMData.variance?.value)
            food_actual_value.text = Validation().checkAmountPercentageValue(this,
                                                                             rangeOverviewGMData.actual?.amount,
                                                                             rangeOverviewGMData.actual?.percentage,
                                                                             rangeOverviewGMData.actual?.value)
            food_sales.text = Validation().checkAmountPercentageValue(this,
                                                                      rangeOverviewGMData.actual?.amount,
                                                                      rangeOverviewGMData.actual?.percentage,
                                                                      rangeOverviewGMData.actual?.value)

            if (rangeOverviewGMData.status?.toString() != null) {

                when {
                    rangeOverviewGMData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    rangeOverviewGMData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if ( rangeOverviewGMData.displayName.isNullOrEmpty()) {
                cash_parent_yesterday.visibility = View.GONE
            } else {
                cash_parent_yesterday.visibility = View.VISIBLE
                cash_display.text = rangeOverviewGMData.displayName
            }

            val gmRangeCashGoal = Validation().checkAmountPercentageValue(this,
                                                                          rangeOverviewGMData.goal?.amount,
                                                                          rangeOverviewGMData.goal?.percentage,
                                                                          rangeOverviewGMData.goal?.value)

            val gmRangeCashVariance = Validation().checkAmountPercentageValue(this,
                                                                              rangeOverviewGMData.variance?.amount,
                                                                              rangeOverviewGMData.variance?.percentage,
                                                                              rangeOverviewGMData.variance?.value)

            val gmRangeCashActual = Validation().checkAmountPercentageValue(this,
                                                                            rangeOverviewGMData.actual?.amount,
                                                                            rangeOverviewGMData.actual?.percentage,
                                                                            rangeOverviewGMData.actual?.value)

            if(gmRangeCashGoal.isEmpty() && gmRangeCashVariance.isEmpty() && gmRangeCashActual.isEmpty()){

                cash_goal.visibility = View.GONE
                cash_varince.visibility = View.GONE
                cash_actual.visibility = View.GONE
                cash_error.visibility = View.VISIBLE
                

                val paramsGMRangeCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsGMRangeCashError.weight = 4.0f
                cash_display.layoutParams = paramsGMRangeCashError

            }
            else if(gmRangeCashGoal.isNotEmpty() && gmRangeCashVariance.isNotEmpty() && gmRangeCashActual.isNotEmpty()){
                cash_goal.visibility = View.VISIBLE
                cash_varince.visibility = View.VISIBLE
                cash_actual.visibility = View.VISIBLE
                cash_goal.text = gmRangeCashGoal
                cash_varince.text = gmRangeCashVariance
                cash_actual.text =gmRangeCashActual

            }
            else{
                if(gmRangeCashGoal.isEmpty()){
                    
                    cash_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_goal.text = gmRangeCashGoal
                }

                if(gmRangeCashVariance.isEmpty()){
                    
                    cash_varince.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_varince.text = gmRangeCashVariance
                }

                if(gmRangeCashActual.isEmpty()){
                    
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_actual.visibility = View.VISIBLE
                }
            }

            if (rangeOverviewGMData.status != null && gmRangeCashActual.isNotEmpty()) {
                when {
                    rangeOverviewGMData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        cash_actual.setTextColor(getColor(R.color.red))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                    }
                    rangeOverviewGMData.status.toString() == resources.getString(R.string.under_limit) -> {
                        cash_actual.setTextColor(getColor(R.color.green))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                    } else -> {
                        cash_actual.setTextColor(getColor(R.color.text_color))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Cash Overview Period Range KPI")
        }
    }

    private fun todayViewGMCashKpi(dataTodayViewGM: StoreTodayKPIQuery.GeneralManager?) {

        val todayViewGMData = dataTodayViewGM!!.kpis!!.store!!.today!!.cash

        Logger.info("Cash Today", "Cash Overview KPI")


        ideal_vs_food_variance_text.text = todayViewGMData!!.displayName ?: getString(R.string.cash_text)

        food_goal_value.text = Validation().ignoreZeroAfterDecimal(todayViewGMData.goal!!.value)
        food_variance_value.text = Validation().ignoreZeroAfterDecimal(todayViewGMData.variance!!.value)
        food_actual_value.text = Validation().ignoreZeroAfterDecimal(todayViewGMData.actual!!.value)
        if (todayViewGMData.actual.value?.isNaN() == false && todayViewGMData.status?.toString() != null) {
            food_sales.text =
                Validation().ignoreZeroAfterDecimal(todayViewGMData.actual.value)
            when {
                todayViewGMData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_sales.setTextColor(getColor(R.color.red))
                }
                todayViewGMData.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_sales.setTextColor(getColor(R.color.green))
                } else -> {
                    food_sales.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        if (todayViewGMData.displayName.isNullOrEmpty()) {
            cash_parent_yesterday.visibility = View.GONE
        } else {
            cash_parent_yesterday.visibility = View.VISIBLE
            cash_display.text = todayViewGMData.displayName
        }

        val gmTodayCashGoal = Validation().ignoreZeroAfterDecimal(todayViewGMData.goal.value)
        val gmTodayCashVariance = Validation().ignoreZeroAfterDecimal(todayViewGMData.variance.value)
        val gmTodayCashActual = Validation().ignoreZeroAfterDecimal(todayViewGMData.actual.value)

        if(gmTodayCashGoal.isEmpty() && gmTodayCashVariance.isEmpty() && gmTodayCashActual.isEmpty()){

            cash_goal.visibility = View.GONE
            cash_varince.visibility = View.GONE
            cash_actual.visibility = View.GONE
            cash_error.visibility = View.VISIBLE
            

            val paramsGMTodayCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMTodayCashError.weight = 4.0f
            cash_display.layoutParams = paramsGMTodayCashError

        }
        else if(gmTodayCashGoal.isNotEmpty() && gmTodayCashVariance.isNotEmpty() && gmTodayCashActual.isNotEmpty()){
            cash_goal.visibility = View.VISIBLE
            cash_varince.visibility = View.VISIBLE
            cash_actual.visibility = View.VISIBLE
            cash_goal.text = gmTodayCashGoal
            cash_varince.text = gmTodayCashVariance
            cash_actual.text = gmTodayCashActual

        }
        else{
            if(gmTodayCashGoal.isEmpty()){
                
                cash_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }
            else{
                cash_goal.text = gmTodayCashGoal
            }

            if(gmTodayCashVariance.isEmpty()){
                
                cash_varince.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                cash_varince.text = gmTodayCashVariance
            }

            if(gmTodayCashActual.isEmpty()){
                
                cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                cash_actual.text = gmTodayCashActual
            }
        }

        if (todayViewGMData.actual.value?.isNaN() == false && todayViewGMData.status != null) {
            cash_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewGMData.actual.value)
            when {
                todayViewGMData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual.setTextColor(getColor(R.color.red))
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                }
                todayViewGMData.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual.setTextColor(getColor(R.color.green))
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                } else -> {
                    cash_actual.setTextColor(getColor(R.color.text_color))
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                }
            }
        }
    }

    // ceo
    private fun yesterdayViewCEOCashKpi(dataYesterdayCEO: CEOOverviewYesterdayQuery.Ceo?) {
        try {
            val yesterdayCEOData = dataYesterdayCEO!!.kpis!!.supervisors?.stores!!.yesterday!!.cash

            Logger.info("Cash Yesterday", "Cash Overview KPI")

            ideal_vs_food_variance_text.text = yesterdayCEOData!!.displayName ?: getString(R.string.cash_text)

            food_goal_value.text = Validation().checkAmountPercentageValue(this,
                                                                           yesterdayCEOData.goal?.amount,
                                                                           yesterdayCEOData.goal?.percentage,
                                                                           yesterdayCEOData.goal?.value)
            food_variance_value.text = Validation().checkAmountPercentageValue(this,
                                                                               yesterdayCEOData.variance?.amount,
                                                                               yesterdayCEOData.variance?.percentage,
                                                                               yesterdayCEOData.variance?.value)
            food_actual_value.text = Validation().checkAmountPercentageValue(this,
                                                                             yesterdayCEOData.actual?.amount,
                                                                             yesterdayCEOData.actual?.percentage,
                                                                             yesterdayCEOData.actual?.value)
            food_sales.text = Validation().checkAmountPercentageValue(this,
                                                                      yesterdayCEOData.actual?.amount,
                                                                      yesterdayCEOData.actual?.percentage,
                                                                      yesterdayCEOData.actual?.value)

            if (yesterdayCEOData.status?.toString() != null) {

                when {
                    yesterdayCEOData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    yesterdayCEOData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if (yesterdayCEOData.displayName.isNullOrEmpty()) {
                cash_parent_yesterday.visibility = View.GONE
            } else {
                cash_parent_yesterday.visibility = View.VISIBLE
                cash_display.text = yesterdayCEOData.displayName
            }

            val ceoYesterdayCashGoal = Validation().checkAmountPercentageValue(this,
                                                                               yesterdayCEOData.goal?.amount,
                                                                               yesterdayCEOData.goal?.percentage,
                                                                               yesterdayCEOData.goal?.value)

            val ceoYesterdayCashVariance = Validation().checkAmountPercentageValue(this,
                                                                                   yesterdayCEOData.variance?.amount,
                                                                                   yesterdayCEOData.variance?.percentage,
                                                                                   yesterdayCEOData.variance?.value)

            val ceoYesterdayCashActual = Validation().checkAmountPercentageValue(this,
                                                                                 yesterdayCEOData.actual?.amount,
                                                                                 yesterdayCEOData.actual?.percentage,
                                                                                 yesterdayCEOData.actual?.value)

            if(ceoYesterdayCashGoal.isEmpty() && ceoYesterdayCashVariance.isEmpty() && ceoYesterdayCashActual.isEmpty()){

                cash_goal.visibility = View.GONE
                cash_varince.visibility = View.GONE
                cash_actual.visibility = View.GONE
                cash_error.visibility = View.VISIBLE
                

                val paramsCEOYesterdayCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsCEOYesterdayCashError.weight = 4.0f
                cash_display.layoutParams = paramsCEOYesterdayCashError

            }
            else if(ceoYesterdayCashGoal.isNotEmpty() && ceoYesterdayCashVariance.isNotEmpty() && ceoYesterdayCashActual.isNotEmpty()){
                cash_goal.visibility = View.VISIBLE
                cash_varince.visibility = View.VISIBLE
                cash_actual.visibility = View.VISIBLE
                cash_goal.text = ceoYesterdayCashGoal
                cash_varince.text = ceoYesterdayCashVariance
                cash_actual.text =ceoYesterdayCashActual

            }
            else{
                if(ceoYesterdayCashGoal.isEmpty()){
                    
                    cash_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_goal.text = ceoYesterdayCashGoal
                }

                if(ceoYesterdayCashVariance.isEmpty()){
                    
                    cash_varince.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_varince.text = ceoYesterdayCashVariance
                }

                if(ceoYesterdayCashActual.isEmpty()){
                    
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_actual.text = ceoYesterdayCashActual
                }
            }

            if (yesterdayCEOData.status != null && ceoYesterdayCashActual.isNotEmpty()) {
                when {
                    yesterdayCEOData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        cash_actual.setTextColor(getColor(R.color.red))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                    }
                    yesterdayCEOData.status.toString() == resources.getString(R.string.under_limit) -> {
                        cash_actual.setTextColor(getColor(R.color.green))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                    } else -> {
                        cash_actual.setTextColor(getColor(R.color.text_color))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Cash Overview Yesterday KPI")
        }
    }

    private fun rangeOverViewCEOCashKpi(dataRangeOverCEO: CEOOverviewRangeQuery.Ceo?) {
        try {
            val rangeOverViewCEOData = dataRangeOverCEO!!.kpis!!.supervisors?.stores!!.period!!.cash

            Logger.info("Cash Period Range", "Cash Overview KPI")

            ideal_vs_food_variance_text.text = rangeOverViewCEOData!!.displayName ?: getString(R.string.cash_text)

            food_goal_value.text = Validation().checkAmountPercentageValue(this,
                                                                           rangeOverViewCEOData.goal?.amount,
                                                                           rangeOverViewCEOData.goal?.percentage,
                                                                           rangeOverViewCEOData.goal?.value)
            food_variance_value.text = Validation().checkAmountPercentageValue(this,
                                                                               rangeOverViewCEOData.variance?.amount,
                                                                               rangeOverViewCEOData.variance?.percentage,
                                                                               rangeOverViewCEOData.variance?.value)
            food_actual_value.text = Validation().checkAmountPercentageValue(this,
                                                                             rangeOverViewCEOData.actual?.amount,
                                                                             rangeOverViewCEOData.actual?.percentage,
                                                                             rangeOverViewCEOData.actual?.value)
            food_sales.text = Validation().checkAmountPercentageValue(this,
                                                                      rangeOverViewCEOData.actual?.amount,
                                                                      rangeOverViewCEOData.actual?.percentage,
                                                                      rangeOverViewCEOData.actual?.value)

            if (rangeOverViewCEOData.status?.toString() != null) {

                when {
                    rangeOverViewCEOData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewCEOData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if (rangeOverViewCEOData.displayName.isNullOrEmpty()) {
                cash_parent_yesterday.visibility = View.GONE
            } else {
                cash_parent_yesterday.visibility = View.VISIBLE
                cash_display.text = rangeOverViewCEOData.displayName
            }

            val ceoRangeCashGoal = Validation().checkAmountPercentageValue(this,
                                                                           rangeOverViewCEOData.goal?.amount,
                                                                           rangeOverViewCEOData.goal?.percentage,
                                                                           rangeOverViewCEOData.goal?.value)

            val ceoRangeCashVariance = Validation().checkAmountPercentageValue(this,
                                                                               rangeOverViewCEOData.variance?.amount,
                                                                               rangeOverViewCEOData.variance?.percentage,
                                                                               rangeOverViewCEOData.variance?.value)

            val ceoRangeCashActual = Validation().checkAmountPercentageValue(this,
                                                                             rangeOverViewCEOData.actual?.amount,
                                                                             rangeOverViewCEOData.actual?.percentage,
                                                                             rangeOverViewCEOData.actual?.value)
            
            if(ceoRangeCashGoal.isEmpty() && ceoRangeCashVariance.isEmpty() && ceoRangeCashActual.isEmpty()){
                
                cash_goal.visibility = View.GONE
                cash_varince.visibility = View.GONE
                cash_actual.visibility = View.GONE
                cash_error.visibility = View.VISIBLE
                
                
                val paramsCEORangeCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsCEORangeCashError.weight = 4.0f
                cash_display.layoutParams = paramsCEORangeCashError
                
            }
            else if(ceoRangeCashGoal.isNotEmpty() && ceoRangeCashVariance.isNotEmpty() && ceoRangeCashActual.isNotEmpty()){
                cash_goal.visibility = View.VISIBLE
                cash_varince.visibility = View.VISIBLE
                cash_actual.visibility = View.VISIBLE
                cash_goal.text = ceoRangeCashGoal
                cash_varince.text = ceoRangeCashVariance
                cash_actual.text =ceoRangeCashActual
                
            }
            else{
                if(ceoRangeCashGoal.isEmpty()){
                    
                    cash_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_goal.text = ceoRangeCashGoal
                }
                
                if(ceoRangeCashVariance.isEmpty()){
                    
                    cash_varince.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_varince.text = ceoRangeCashVariance
                }
                
                if(ceoRangeCashActual.isEmpty()){
                    
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_actual.visibility = View.VISIBLE
                }
            }

            if (rangeOverViewCEOData.status != null && ceoRangeCashActual.isNotEmpty()) {
                when {
                    rangeOverViewCEOData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        cash_actual.setTextColor(getColor(R.color.red))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                    }
                    rangeOverViewCEOData.status.toString() == resources.getString(R.string.under_limit) -> {
                        cash_actual.setTextColor(getColor(R.color.green))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                    } else -> {
                        cash_actual.setTextColor(getColor(R.color.text_color))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Cash Overview Period Range KPI")
        }
    }

    private fun todayViewCEOCashKpi(dataTodayViewCEO: CEOOverviewTodayQuery.Ceo?) {

        val todayViewCEOData = dataTodayViewCEO!!.kpis!!.supervisors?.stores!!.today!!.cash

        Logger.info("Cash Today", "Cash Overview KPI")

        ideal_vs_food_variance_text.text = todayViewCEOData!!.displayName ?: getString(R.string.cash_text)

        food_goal_value.text = Validation().ignoreZeroAfterDecimal(todayViewCEOData.goal!!.value)
        food_variance_value.text = Validation().ignoreZeroAfterDecimal(todayViewCEOData.variance!!.value)
        food_actual_value.text = Validation().ignoreZeroAfterDecimal(todayViewCEOData.actual!!.value)
        if (todayViewCEOData.actual.value?.isNaN() == false && todayViewCEOData.status?.toString() != null) {
            food_sales.text =
                Validation().ignoreZeroAfterDecimal(todayViewCEOData.actual.value)
            when {
                todayViewCEOData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_sales.setTextColor(getColor(R.color.red))
                }
                todayViewCEOData.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_sales.setTextColor(getColor(R.color.green))
                } else -> {
                    food_sales.setTextColor(getColor(R.color.text_color))
                }
            }
        }
        if (todayViewCEOData.displayName.isNullOrEmpty()) {
            cash_parent_yesterday.visibility = View.GONE
        } else {
            cash_parent_yesterday.visibility = View.VISIBLE
            cash_display.text = todayViewCEOData.displayName
        }

        val ceoTodayCashGoal = Validation().ignoreZeroAfterDecimal(todayViewCEOData.goal.value)
        val ceoTodayCashVariance = Validation().ignoreZeroAfterDecimal(todayViewCEOData.variance.value)
        val ceoTodayCashActual = Validation().ignoreZeroAfterDecimal(todayViewCEOData.actual.value)

        if(ceoTodayCashGoal.isEmpty() && ceoTodayCashVariance.isEmpty() && ceoTodayCashActual.isEmpty()){

            cash_goal.visibility = View.GONE
            cash_varince.visibility = View.GONE
            cash_actual.visibility = View.GONE
            cash_error.visibility = View.VISIBLE
            

            val paramsCEOTodayCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsCEOTodayCashError.weight = 4.0f
            cash_display.layoutParams = paramsCEOTodayCashError

        }
        else if(ceoTodayCashGoal.isNotEmpty() && ceoTodayCashVariance.isNotEmpty() && ceoTodayCashActual.isNotEmpty()){
            cash_goal.visibility = View.VISIBLE
            cash_varince.visibility = View.VISIBLE
            cash_actual.visibility = View.VISIBLE
            cash_goal.text = ceoTodayCashGoal
            cash_varince.text = ceoTodayCashVariance
            cash_actual.text = ceoTodayCashActual

        }
        else{
            if(ceoTodayCashGoal.isEmpty()){
                
                cash_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }
            else{
                cash_goal.text = ceoTodayCashGoal
            }

            if(ceoTodayCashVariance.isEmpty()){
                
                cash_varince.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                cash_varince.text = ceoTodayCashVariance
            }

            if(ceoTodayCashActual.isEmpty()){
                
                cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                cash_actual.text = ceoTodayCashActual
            }
        }

        if (todayViewCEOData.actual.value?.isNaN() == false && todayViewCEOData.status != null) {
            cash_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewCEOData.actual.value)
            when {
                todayViewCEOData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual.setTextColor(getColor(R.color.red))
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                }
                todayViewCEOData.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual.setTextColor(getColor(R.color.green))
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                } else -> {
                    cash_actual.setTextColor(getColor(R.color.text_color))
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                }
            }
        }
    }

    // do overview
    private fun yesterdayViewDOCashKpi(dataYesterdayDO: DOOverviewYesterdayQuery.Do_?) {
        try {
            val yesterdayDOData = dataYesterdayDO!!.kpis!!.supervisors?.stores!!.yesterday!!.cash

            Logger.info("Cash Yesterday", "Cash Overview KPI")

            ideal_vs_food_variance_text.text = yesterdayDOData!!.displayName ?: getString(R.string.cash_text)

            food_goal_value.text = Validation().checkAmountPercentageValue(this,
                                                                           yesterdayDOData.goal?.amount,
                                                                           yesterdayDOData.goal?.percentage,
                                                                           yesterdayDOData.goal?.value)
            food_variance_value.text = Validation().checkAmountPercentageValue(this,
                                                                               yesterdayDOData.variance?.amount,
                                                                               yesterdayDOData.variance?.percentage,
                                                                               yesterdayDOData.variance?.value)
            food_actual_value.text = Validation().checkAmountPercentageValue(this,
                                                                             yesterdayDOData.actual?.amount,
                                                                             yesterdayDOData.actual?.percentage,
                                                                             yesterdayDOData.actual?.value)
            food_sales.text = Validation().checkAmountPercentageValue(this,
                                                                      yesterdayDOData.actual?.amount,
                                                                      yesterdayDOData.actual?.percentage,
                                                                      yesterdayDOData.actual?.value)

            if (yesterdayDOData.status?.toString() != null) {

                when {
                    yesterdayDOData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    yesterdayDOData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            if (yesterdayDOData.displayName.isNullOrEmpty()) {
                cash_parent_yesterday.visibility = View.GONE
            } else {
                cash_parent_yesterday.visibility = View.VISIBLE
                cash_display.text = yesterdayDOData.displayName
            }

            val doYesterdayCashGoal = Validation().checkAmountPercentageValue(this,
                                                                              yesterdayDOData.goal?.amount,
                                                                              yesterdayDOData.goal?.percentage,
                                                                              yesterdayDOData.goal?.value)

            val doYesterdayCashVariance = Validation().checkAmountPercentageValue(this,
                                                                                  yesterdayDOData.variance?.amount,
                                                                                  yesterdayDOData.variance?.percentage,
                                                                                  yesterdayDOData.variance?.value)

            val doYesterdayCashActual = Validation().checkAmountPercentageValue(this,
                                                                                yesterdayDOData.actual?.amount,
                                                                                yesterdayDOData.actual?.percentage,
                                                                                yesterdayDOData.actual?.value)

            if(doYesterdayCashGoal.isEmpty() && doYesterdayCashVariance.isEmpty() && doYesterdayCashActual.isEmpty()){

                cash_goal.visibility = View.GONE
                cash_varince.visibility = View.GONE
                cash_actual.visibility = View.GONE
                cash_error.visibility = View.VISIBLE
                

                val paramsDOYesterdayCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsDOYesterdayCashError.weight = 4.0f
                cash_display.layoutParams = paramsDOYesterdayCashError

            }
            else if(doYesterdayCashGoal.isNotEmpty() && doYesterdayCashVariance.isNotEmpty() && doYesterdayCashActual.isNotEmpty()){
                cash_goal.visibility = View.VISIBLE
                cash_varince.visibility = View.VISIBLE
                cash_actual.visibility = View.VISIBLE
                cash_goal.text = doYesterdayCashGoal
                cash_varince.text = doYesterdayCashVariance
                cash_actual.text = doYesterdayCashActual

            }
            else{
                if(doYesterdayCashGoal.isEmpty()){
                    
                    cash_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }
                else{
                    cash_goal.text = doYesterdayCashGoal
                }

                if(doYesterdayCashVariance.isEmpty()){
                    
                    cash_varince.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_varince.text = doYesterdayCashVariance
                }

                if(doYesterdayCashActual.isEmpty()){
                    
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_actual.text = doYesterdayCashActual
                }
            }

            if (yesterdayDOData.status != null && doYesterdayCashActual.isNotEmpty()) {
                when {
                    yesterdayDOData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        cash_actual.setTextColor(getColor(R.color.red))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                    }
                    yesterdayDOData.status.toString() == resources.getString(R.string.under_limit) -> {
                        cash_actual.setTextColor(getColor(R.color.green))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                    } else -> {
                        cash_actual.setTextColor(getColor(R.color.text_color))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                    }
                }
            }
       
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Cash Overview Yesterday KPI")
        }
    }

    private fun rangeOverViewDOCashKpi(dataRangeOverDO: DOOverviewRangeQuery.Do_?) {
        try {
            val rangeOverViewDOData = dataRangeOverDO!!.kpis!!.supervisors?.stores!!.period!!.cash

            Logger.info("Cash Period Range", "Cash Overview KPI")

            ideal_vs_food_variance_text.text = rangeOverViewDOData!!.displayName ?: getString(R.string.cash_text)

            food_goal_value.text = Validation().checkAmountPercentageValue(this,
                                                                           rangeOverViewDOData.goal?.amount,
                                                                           rangeOverViewDOData.goal?.percentage,
                                                                           rangeOverViewDOData.goal?.value)
            food_variance_value.text = Validation().checkAmountPercentageValue(this,
                                                                               rangeOverViewDOData.variance?.amount,
                                                                               rangeOverViewDOData.variance?.percentage,
                                                                               rangeOverViewDOData.variance?.value)
            food_actual_value.text = Validation().checkAmountPercentageValue(this,
                                                                             rangeOverViewDOData.actual?.amount,
                                                                             rangeOverViewDOData.actual?.percentage,
                                                                             rangeOverViewDOData.actual?.value)
            food_sales.text = Validation().checkAmountPercentageValue(this,
                                                                      rangeOverViewDOData.actual?.amount,
                                                                      rangeOverViewDOData.actual?.percentage,
                                                                      rangeOverViewDOData.actual?.value)

            if (rangeOverViewDOData.status?.toString() != null) {

                when {
                    rangeOverViewDOData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    rangeOverViewDOData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            if (rangeOverViewDOData.displayName.isNullOrEmpty()) {
                cash_parent_yesterday.visibility = View.GONE
            } else {
                cash_parent_yesterday.visibility = View.VISIBLE
                cash_display.text = rangeOverViewDOData.displayName
            }

            val doRangeCashGoal = Validation().checkAmountPercentageValue(this,
                                                                          rangeOverViewDOData.goal?.amount,
                                                                          rangeOverViewDOData.goal?.percentage,
                                                                          rangeOverViewDOData.goal?.value)

            val doRangeCashVariance = Validation().checkAmountPercentageValue(this,
                                                                              rangeOverViewDOData.variance?.amount,
                                                                              rangeOverViewDOData.variance?.percentage,
                                                                              rangeOverViewDOData.variance?.value)

            val doRangeCashActual = Validation().checkAmountPercentageValue(this,
                                                                            rangeOverViewDOData.actual?.amount,
                                                                            rangeOverViewDOData.actual?.percentage,
                                                                            rangeOverViewDOData.actual?.value)

            if(doRangeCashGoal.isEmpty() && doRangeCashVariance.isEmpty() && doRangeCashActual.isEmpty()){

                cash_goal.visibility = View.GONE
                cash_varince.visibility = View.GONE
                cash_actual.visibility = View.GONE
                cash_error.visibility = View.VISIBLE
                

                val paramsDORangeCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsDORangeCashError.weight = 4.0f
                cash_display.layoutParams = paramsDORangeCashError

            }
            else if(doRangeCashGoal.isNotEmpty() && doRangeCashVariance.isNotEmpty() && doRangeCashActual.isNotEmpty()){
                cash_goal.visibility = View.VISIBLE
                cash_varince.visibility = View.VISIBLE
                cash_actual.visibility = View.VISIBLE
                cash_goal.text = doRangeCashGoal
                cash_varince.text = doRangeCashVariance
                cash_actual.text =doRangeCashActual

            }
            else{
                if(doRangeCashGoal.isEmpty()){
                    
                    cash_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_goal.text = doRangeCashGoal
                }

                if(doRangeCashVariance.isEmpty()){
                    
                    cash_varince.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_varince.text = doRangeCashVariance
                }

                if(doRangeCashActual.isEmpty()){
                    
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_actual.text = doRangeCashActual
                }
            }

            if (rangeOverViewDOData.status != null && doRangeCashActual.isNotEmpty()) {
                when {
                    rangeOverViewDOData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        cash_actual.setTextColor(getColor(R.color.red))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                    }
                    rangeOverViewDOData.status.toString() == resources.getString(R.string.under_limit) -> {
                        cash_actual.setTextColor(getColor(R.color.green))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                    } else -> {
                        cash_actual.setTextColor(getColor(R.color.text_color))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Cash Overview Period Range KPI")
        }
    }

    private fun todayViewDOCashKpi(dataTodayViewDO: DOOverviewTodayQuery.Do_?) {

        val todayViewDOData = dataTodayViewDO!!.kpis!!.supervisors?.stores!!.today!!.cash

        Logger.info("Cash Today", "Cash Overview KPI")

        ideal_vs_food_variance_text.text = todayViewDOData!!.displayName ?: getString(R.string.cash_text)

        food_goal_value.text = Validation().ignoreZeroAfterDecimal(todayViewDOData.goal!!.value)
        food_variance_value.text = Validation().ignoreZeroAfterDecimal(todayViewDOData.variance!!.value)
        food_actual_value.text = Validation().ignoreZeroAfterDecimal(todayViewDOData.actual!!.value)
        if (todayViewDOData.actual.value?.isNaN() == false && todayViewDOData.status?.toString() != null) {
            food_sales.text =
                Validation().ignoreZeroAfterDecimal(todayViewDOData.actual.value)
            when {
                todayViewDOData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_sales.setTextColor(getColor(R.color.red))
                }
                todayViewDOData.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_sales.setTextColor(getColor(R.color.green))
                } else -> {
                    food_sales.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        if (todayViewDOData.displayName.isNullOrEmpty()) {
            cash_parent_yesterday.visibility = View.GONE
        } else {
            cash_parent_yesterday.visibility = View.VISIBLE
            cash_display.text = todayViewDOData.displayName
        }
        val doTodayCashGoal = Validation().ignoreZeroAfterDecimal(todayViewDOData.goal.value)
        val doTodayCashVariance = Validation().ignoreZeroAfterDecimal(todayViewDOData.variance.value)
        val doTodayCashActual = Validation().ignoreZeroAfterDecimal(todayViewDOData.actual.value)

        if(doTodayCashGoal.isEmpty() && doTodayCashVariance.isEmpty() && doTodayCashActual.isEmpty()){

            cash_goal.visibility = View.GONE
            cash_varince.visibility = View.GONE
            cash_actual.visibility = View.GONE
            cash_error.visibility = View.VISIBLE
            

            val paramsDOTodayCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsDOTodayCashError.weight = 4.0f
            cash_display.layoutParams = paramsDOTodayCashError

        }
        else if(doTodayCashGoal.isNotEmpty() && doTodayCashVariance.isNotEmpty() && doTodayCashActual.isNotEmpty()){
            cash_goal.visibility = View.VISIBLE
            cash_varince.visibility = View.VISIBLE
            cash_actual.visibility = View.VISIBLE
            cash_goal.text = doTodayCashGoal
            cash_varince.text = doTodayCashVariance
            cash_actual.text = doTodayCashActual

        }
        else{
            if(doTodayCashGoal.isEmpty()){
                
                cash_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }
            else{
                cash_goal.text = doTodayCashGoal
            }

            if(doTodayCashVariance.isEmpty()){
                
                cash_varince.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                cash_varince.text = doTodayCashVariance
            }

            if(doTodayCashActual.isEmpty()){
                
                cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                cash_actual.text = doTodayCashActual
            }
        }

        if (todayViewDOData.actual.value?.isNaN() == false && todayViewDOData.status != null) {
            cash_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewDOData.actual.value)
            when {
                todayViewDOData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual.setTextColor(getColor(R.color.red))
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                }
                todayViewDOData.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual.setTextColor(getColor(R.color.green))
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                } else -> {
                    cash_actual.setTextColor(getColor(R.color.text_color))
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                }
            }
        }
    }

    // supervisor overview
    private fun yesterdayViewSupervisorCashKpi(dataYesterdaySupervisor: SupervisorOverviewYesterdayQuery.Supervisor?) {
        try {
            val yesterdaySupervisorData = dataYesterdaySupervisor!!.kpis?.stores?.yesterday?.cash

            Logger.info("Cash Yesterday", "Cash Overview KPI")

            ideal_vs_food_variance_text.text = yesterdaySupervisorData!!.displayName ?: getString(R.string.cash_text)

            food_goal_value.text = Validation().checkAmountPercentageValue(this,
                                                                           yesterdaySupervisorData.goal?.amount,
                                                                           yesterdaySupervisorData.goal?.percentage,
                                                                           yesterdaySupervisorData.goal?.value)
            food_variance_value.text = Validation().checkAmountPercentageValue(this,
                                                                               yesterdaySupervisorData.variance?.amount,
                                                                               yesterdaySupervisorData.variance?.percentage,
                                                                               yesterdaySupervisorData.variance?.value)
            food_actual_value.text = Validation().checkAmountPercentageValue(this,
                                                                             yesterdaySupervisorData.actual?.amount,
                                                                             yesterdaySupervisorData.actual?.percentage,
                                                                             yesterdaySupervisorData.actual?.value)
            food_sales.text = Validation().checkAmountPercentageValue(this,
                                                                      yesterdaySupervisorData.actual?.amount,
                                                                      yesterdaySupervisorData.actual?.percentage,
                                                                      yesterdaySupervisorData.actual?.value)

            if (yesterdaySupervisorData.status?.toString() != null) {

                when {
                    yesterdaySupervisorData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    yesterdaySupervisorData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            if (yesterdaySupervisorData.displayName.isNullOrEmpty()) {
                cash_parent_yesterday.visibility = View.GONE
            } else {
                cash_parent_yesterday.visibility = View.VISIBLE
                cash_display.text = yesterdaySupervisorData.displayName
            }


            val supervisorYesterdayCashGoal = Validation().checkAmountPercentageValue(this,
                                                                                      yesterdaySupervisorData.goal?.amount,
                                                                                      yesterdaySupervisorData.goal?.percentage,
                                                                                      yesterdaySupervisorData.goal?.value)

            val supervisorYesterdayCashVariance = Validation().checkAmountPercentageValue(this,
                                                                                          yesterdaySupervisorData.variance?.amount,
                                                                                          yesterdaySupervisorData.variance?.percentage,
                                                                                          yesterdaySupervisorData.variance?.value)

            val supervisorYesterdayCashActual = Validation().checkAmountPercentageValue(this,
                                                                                        yesterdaySupervisorData.actual?.amount,
                                                                                        yesterdaySupervisorData.actual?.percentage,
                                                                                        yesterdaySupervisorData.actual?.value)

            if(supervisorYesterdayCashGoal.isEmpty() && supervisorYesterdayCashVariance.isEmpty() && supervisorYesterdayCashActual.isEmpty()){

                cash_goal.visibility = View.GONE
                cash_varince.visibility = View.GONE
                cash_actual.visibility = View.GONE
                cash_error.visibility = View.VISIBLE
                

                val paramsSupervisorYesterdayCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsSupervisorYesterdayCashError.weight = 4.0f
                cash_display.layoutParams = paramsSupervisorYesterdayCashError

            }
            else if(supervisorYesterdayCashGoal.isNotEmpty() && supervisorYesterdayCashVariance.isNotEmpty() && supervisorYesterdayCashActual.isNotEmpty()){
                cash_goal.visibility = View.VISIBLE
                cash_varince.visibility = View.VISIBLE
                cash_actual.visibility = View.VISIBLE
                cash_goal.text = supervisorYesterdayCashGoal
                cash_varince.text = supervisorYesterdayCashVariance
                cash_actual.text = supervisorYesterdayCashActual

            }
            else{
                if(supervisorYesterdayCashGoal.isEmpty()){
                    
                    cash_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }
                else{
                    cash_goal.text = supervisorYesterdayCashGoal
                }

                if(supervisorYesterdayCashVariance.isEmpty()){
                    
                    cash_varince.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_varince.text = supervisorYesterdayCashVariance
                }

                if(supervisorYesterdayCashActual.isEmpty()){
                    
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_actual.text = supervisorYesterdayCashActual
                }
            }

            if (yesterdaySupervisorData.status != null && supervisorYesterdayCashActual.isNotEmpty()) {
                when {
                    yesterdaySupervisorData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        cash_actual.setTextColor(getColor(R.color.red))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                    }
                    yesterdaySupervisorData.status.toString() == resources.getString(R.string.under_limit) -> {
                        cash_actual.setTextColor(getColor(R.color.green))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                    } else -> {
                        cash_actual.setTextColor(getColor(R.color.text_color))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Cash Overview Yesterday KPI")
        }
    }

    private fun rangeOverviewSupervisorCashKpi(dataRangeOverSupervisor: SupervisorOverviewRangeQuery.Supervisor?) {
        try {
            val rangeSupervisorData = dataRangeOverSupervisor!!.kpis?.stores?.period?.cash

            Logger.info("Cash Period Range", "Cash Overview KPI")

            ideal_vs_food_variance_text.text = rangeSupervisorData!!.displayName ?: getString(R.string.cash_text)

            food_goal_value.text = Validation().checkAmountPercentageValue(this,
                                                                           rangeSupervisorData.goal?.amount,
                                                                           rangeSupervisorData.goal?.percentage,
                                                                           rangeSupervisorData.goal?.value)
            food_variance_value.text = Validation().checkAmountPercentageValue(this,
                                                                               rangeSupervisorData.variance?.amount,
                                                                               rangeSupervisorData.variance?.percentage,
                                                                               rangeSupervisorData.variance?.value)
            food_actual_value.text = Validation().checkAmountPercentageValue(this,
                                                                             rangeSupervisorData.actual?.amount,
                                                                             rangeSupervisorData.actual?.percentage,
                                                                             rangeSupervisorData.actual?.value)
            food_sales.text = Validation().checkAmountPercentageValue(this,
                                                                      rangeSupervisorData.actual?.amount,
                                                                      rangeSupervisorData.actual?.percentage,
                                                                      rangeSupervisorData.actual?.value)

            if (rangeSupervisorData.status?.toString() != null) {

                when {
                    rangeSupervisorData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    rangeSupervisorData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }

            if (rangeSupervisorData.displayName.isNullOrEmpty()) {
                cash_parent_yesterday.visibility = View.GONE
            } else {
                cash_parent_yesterday.visibility = View.VISIBLE
                cash_display.text = rangeSupervisorData.displayName
            }


            val supervisorRangeCashGoal = Validation().checkAmountPercentageValue(this,
                                                                                  rangeSupervisorData.goal?.amount,
                                                                                  rangeSupervisorData.goal?.percentage,
                                                                                  rangeSupervisorData.goal?.value)

            val supervisorRangeCashVariance = Validation().checkAmountPercentageValue(this,
                                                                                      rangeSupervisorData.variance?.amount,
                                                                                      rangeSupervisorData.variance?.percentage,
                                                                                      rangeSupervisorData.variance?.value)

            val supervisorRangeCashActual = Validation().checkAmountPercentageValue(this,
                                                                                    rangeSupervisorData.actual?.amount,
                                                                                    rangeSupervisorData.actual?.percentage,
                                                                                    rangeSupervisorData.actual?.value)

            if(supervisorRangeCashGoal.isEmpty() && supervisorRangeCashVariance.isEmpty() && supervisorRangeCashActual.isEmpty()){

                cash_goal.visibility = View.GONE
                cash_varince.visibility = View.GONE
                cash_actual.visibility = View.GONE
                cash_error.visibility = View.VISIBLE
                

                val paramsSupervisorRangeCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsSupervisorRangeCashError.weight = 4.0f
                cash_display.layoutParams = paramsSupervisorRangeCashError

            }
            else if(supervisorRangeCashGoal.isNotEmpty() && supervisorRangeCashVariance.isNotEmpty() && supervisorRangeCashActual.isNotEmpty()){
                cash_goal.visibility = View.VISIBLE
                cash_varince.visibility = View.VISIBLE
                cash_actual.visibility = View.VISIBLE
                cash_goal.text = supervisorRangeCashGoal
                cash_varince.text = supervisorRangeCashVariance
                cash_actual.text =supervisorRangeCashActual

            }
            else{
                if(supervisorRangeCashGoal.isEmpty()){
                    
                    cash_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_goal.text = supervisorRangeCashGoal
                }

                if(supervisorRangeCashVariance.isEmpty()){
                    
                    cash_varince.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_varince.text = supervisorRangeCashVariance
                }

                if(supervisorRangeCashActual.isEmpty()){
                    
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0)
                }else{
                    cash_actual.text = supervisorRangeCashActual
                }
            }

            if (rangeSupervisorData.status != null && supervisorRangeCashActual.isNotEmpty()) {
                when {
                    rangeSupervisorData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        cash_actual.setTextColor(getColor(R.color.red))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                    }
                    rangeSupervisorData.status.toString() == resources.getString(R.string.under_limit) -> {
                        cash_actual.setTextColor(getColor(R.color.green))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                    } else -> {
                        cash_actual.setTextColor(getColor(R.color.text_color))
                        cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Cash Overview Period Range KPI")
        }
    }

    private fun todayViewSupervisorCashKpi(dataTodayViewSupervisor: SupervisorOverviewTodayQuery.Supervisor?) {

        val todayViewSupervisorData = dataTodayViewSupervisor!!.kpis!!.stores!!.today!!.cash

        Logger.info("Cash Today", "Cash Overview KPI")

        ideal_vs_food_variance_text.text = todayViewSupervisorData!!.displayName ?: getString(R.string.cash_text)

        food_goal_value.text = Validation().ignoreZeroAfterDecimal(todayViewSupervisorData.goal!!.value)
        food_variance_value.text = Validation().ignoreZeroAfterDecimal(todayViewSupervisorData.variance!!.value)
        food_actual_value.text = Validation().ignoreZeroAfterDecimal(todayViewSupervisorData.actual!!.value)
        if (todayViewSupervisorData.actual.value?.isNaN() == false && todayViewSupervisorData.status?.toString() != null) {
            food_sales.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorData.actual.value)
            when {
                todayViewSupervisorData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_sales.setTextColor(getColor(R.color.red))
                }
                todayViewSupervisorData.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_sales.setTextColor(getColor(R.color.green))
                } else -> {
                    food_sales.setTextColor(getColor(R.color.text_color))
                }
            }
        }

        if (todayViewSupervisorData.displayName.isNullOrEmpty()) {
            cash_parent_yesterday.visibility = View.GONE
        } else {
            cash_parent_yesterday.visibility = View.VISIBLE
            cash_display.text = todayViewSupervisorData.displayName
        }


        val supervisorTodayCashGoal = Validation().ignoreZeroAfterDecimal(todayViewSupervisorData.goal.value)
        val supervisorTodayCashVariance = Validation().ignoreZeroAfterDecimal(todayViewSupervisorData.variance.value)
        val supervisorTodayCashActual = Validation().ignoreZeroAfterDecimal(todayViewSupervisorData.actual.value)

        if(supervisorTodayCashGoal.isEmpty() && supervisorTodayCashVariance.isEmpty() && supervisorTodayCashActual.isEmpty()){

            cash_goal.visibility = View.GONE
            cash_varince.visibility = View.GONE
            cash_actual.visibility = View.GONE
            cash_error.visibility = View.VISIBLE
            

            val paramsSupervisorTodayCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsSupervisorTodayCashError.weight = 4.0f
            cash_display.layoutParams = paramsSupervisorTodayCashError

        }
        else if(supervisorTodayCashGoal.isNotEmpty() && supervisorTodayCashVariance.isNotEmpty() && supervisorTodayCashActual.isNotEmpty()){
            cash_goal.visibility = View.VISIBLE
            cash_varince.visibility = View.VISIBLE
            cash_actual.visibility = View.VISIBLE
            cash_goal.text = supervisorTodayCashGoal
            cash_varince.text = supervisorTodayCashVariance
            cash_actual.text = supervisorTodayCashActual

        }
        else{
            if(supervisorTodayCashGoal.isEmpty()){
                
                cash_goal.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }
            else{
                cash_goal.text = supervisorTodayCashGoal
            }

            if(supervisorTodayCashVariance.isEmpty()){
                
                cash_varince.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                cash_varince.text = supervisorTodayCashVariance
            }

            if(supervisorTodayCashActual.isEmpty()){
                
                cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0)
            }else{
                cash_actual.text = supervisorTodayCashActual
            }
        }
        if (todayViewSupervisorData.actual.value?.isNaN() == false && todayViewSupervisorData.status != null) {
            cash_actual.text =
                Validation().ignoreZeroAfterDecimal(todayViewSupervisorData.actual.value)
            when {
                todayViewSupervisorData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual.setTextColor(getColor(R.color.red))
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                }
                todayViewSupervisorData.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual.setTextColor(getColor(R.color.green))
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                } else -> {
                    cash_actual.setTextColor(getColor(R.color.text_color))
                    cash_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        Logger.info("Back-pressed","Cash KPI Screen")
        finish()
    }

    private fun callCashOverviewNullApi(){
        val formattedStartDateValueCash: String
        val formattedEndDateValueCash: String

        val startDateValueCash = StorePrefData.startDateValue
        val endDateValueCash = StorePrefData.endDateValue

        if (StorePrefData.isCalendarSelected) {
            formattedStartDateValueCash = startDateValueCash
            formattedEndDateValueCash = endDateValueCash
        } else {
            formattedStartDateValueCash = startDateValueCash
            formattedEndDateValueCash = endDateValueCash
        }
        val progressDialogCashOverview = CustomProgressDialog(this@CashKpiActivity)
        progressDialogCashOverview.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodeListCash = dbHelperCashOverview.getAllSelectedAreaList(true)
            val stateCodeListCash = dbHelperCashOverview.getAllSelectedStoreListState(true)
            val supervisorNumberListCash = dbHelperCashOverview.getAllSelectedStoreListSupervisor(true)
            val storeNumberListCash = dbHelperCashOverview.getAllSelectedStoreList(true)

            val responseMissingDataCash= try {
                apolloClient(this@CashKpiActivity).query(
                    MissingDataQuery(
                            areaCodeListCash.toInput(),
                            stateCodeListCash.toInput(),
                            supervisorNumberListCash.toInput(),
                            storeNumberListCash.toInput(),
                            EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType).toInput(),
                            formattedStartDateValueCash.toInput(),
                            formattedEndDateValueCash.toInput(),
                    )
                ).await()

            } catch (e: ApolloException) {
                Logger.error("Missing Data ${e.message.toString()}","Cash Overview KPI")
                progressDialogCashOverview.dismissProgressDialog()
                return@launchWhenResumed
            }
            if(responseMissingDataCash.data?.missingData!=null){
                progressDialogCashOverview.dismissProgressDialog()
                cash_kpi_error_layout.visibility = View.VISIBLE
                cash_kpi_error_layout.header_data_title.text  = responseMissingDataCash.data?.missingData!!.header
                cash_kpi_error_layout.header_data_description.text  = responseMissingDataCash.data?.missingData!!.message
            }
            else{
                progressDialogCashOverview.dismissProgressDialog()
                cash_kpi_error_layout.visibility = View.GONE
            }
        }
    }

}
