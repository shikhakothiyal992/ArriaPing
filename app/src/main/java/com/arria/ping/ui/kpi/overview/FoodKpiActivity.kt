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
import com.arria.ping.R
import com.arria.ping.apollo.apolloClient
import com.arria.ping.log.Logger
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.kpi.*
import com.arria.ping.util.NetworkHelper
import com.arria.ping.util.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_cash.*
import kotlinx.android.synthetic.main.activity_foodkpi.*
import kotlinx.android.synthetic.main.activity_foodkpi.cross_button_food
import kotlinx.android.synthetic.main.activity_foodkpi.food_goal_value
import kotlinx.android.synthetic.main.activity_foodkpi.food_narrative_value
import kotlinx.android.synthetic.main.activity_foodkpi.food_sales
import kotlinx.android.synthetic.main.activity_foodkpi.food_scroll_parent
import kotlinx.android.synthetic.main.activity_foodkpi.food_variance_value
import kotlinx.android.synthetic.main.activity_foodkpi.level_two_scroll_data_action
import kotlinx.android.synthetic.main.activity_foodkpi.level_two_scroll_data_action_value
import kotlinx.android.synthetic.main.activity_foodkpi.parent_data_on_scroll_linear
import kotlinx.android.synthetic.main.activity_foodkpi.parent_data_on_scroll_view
import kotlinx.android.synthetic.main.activity_service_kpi.*
import kotlinx.android.synthetic.main.data_error_layout.view.*
import kotlinx.android.synthetic.main.expandable_list_supervisor.view.*
import javax.inject.Inject
import com.arria.ping.kpi.gm.StorePeriodRangeKPIQuery
import com.arria.ping.kpi.gm.StoreYesterdayKPIQuery

@AndroidEntryPoint
class FoodKpiActivity : AppCompatActivity() {
    private val gsonFoodKpi = Gson()
    var apiFoodArgumentFromFilter = ""
    private lateinit var dbHelperFoodOverview: DatabaseHelperImpl

    @Inject
    lateinit var networkHelper: NetworkHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foodkpi)
        this.setFinishOnTouchOutside(false)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dbHelperFoodOverview = DatabaseHelperImpl(DatabaseBuilder.getInstance(this@FoodKpiActivity))

        setFoodKpiData()
        cross_button_food.setOnClickListener {
            Logger.info("Cancel Button clicked","Food Overview KPI Screen")
            finish()
        }
    }

    private fun setFoodKpiData() {
        if (StorePrefData.role == getString(R.string.ceo_text)) {
            if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                apiFoodArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
            }
            if (apiFoodArgumentFromFilter == IpConstants.rangeFrom) {
                val foodKpiYesterdayDetailCEO = gsonFoodKpi.fromJson(
                    intent.getStringExtra("food_data"),
                    CEOOverviewRangeQuery.Ceo::class.java
                )
                if (networkHelper.isNetworkConnected()) {
                    callFoodOverviewNullApi()
                    rangeOverViewCEOFoodKpi(foodKpiYesterdayDetailCEO)
                }


            } else if (apiFoodArgumentFromFilter == IpConstants.Yesterday) {
                val foodCeoDetail = gsonFoodKpi.fromJson(
                    intent.getStringExtra("food_data"),
                    CEOOverviewYesterdayQuery.Ceo::class.java
                )
                if (networkHelper.isNetworkConnected()) {
                    callFoodOverviewNullApi()
                    yesterdayViewCEOFoodKpi(foodCeoDetail)
                }

            }
        } else if (StorePrefData.role == getString(R.string.do_text)) {
            if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                apiFoodArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
            }
            if (apiFoodArgumentFromFilter == IpConstants.rangeFrom) {
                val foodKpiYesterdayDetailCEO = gsonFoodKpi.fromJson(
                    intent.getStringExtra("food_data"),
                    DOOverviewRangeQuery.Do_::class.java
                )
                if (networkHelper.isNetworkConnected()) {
                    callFoodOverviewNullApi()
                    rangeOverViewDOFoodKpi(foodKpiYesterdayDetailCEO)
                }

            } else if (apiFoodArgumentFromFilter == IpConstants.Yesterday) {
                val foodCeoDetail = gsonFoodKpi.fromJson(
                    intent.getStringExtra("food_data"),
                    DOOverviewYesterdayQuery.Do_::class.java
                )
                if (networkHelper.isNetworkConnected()) {
                    callFoodOverviewNullApi()
                    yesterdayViewDOFoodKpi(foodCeoDetail)
                }

            }
        } else if (StorePrefData.role == getString(R.string.gm_text)) {
            if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                apiFoodArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
            }
            if (apiFoodArgumentFromFilter == IpConstants.rangeFrom) {
                val foodDetail = gsonFoodKpi.fromJson(
                    intent.getStringExtra("food_data"),
                    StorePeriodRangeKPIQuery.GeneralManager::class.java
                )
                if (networkHelper.isNetworkConnected()) {
                    callFoodOverviewNullApi()
                    rangeOverViewViewGMFoodKpi(foodDetail)
                }


            } else if (apiFoodArgumentFromFilter == IpConstants.Yesterday) {
                val foodDetail = gsonFoodKpi.fromJson(
                    intent.getStringExtra("food_data"),
                    StoreYesterdayKPIQuery.GeneralManager::class.java
                )
                if (networkHelper.isNetworkConnected()) {
                    callFoodOverviewNullApi()
                    yesterdayViewGMFoodKpi(foodDetail)
                }

            }
        }else if (StorePrefData.role == getString(R.string.supervisor_text)) {
            if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                apiFoodArgumentFromFilter = intent.extras!!.getString("api_argument_from_filter")!!
            }

            if (apiFoodArgumentFromFilter == IpConstants.rangeFrom) {
                val foodKpiYesterdayDetailCEO = gsonFoodKpi.fromJson(
                    intent.getStringExtra("food_data"),
                    SupervisorOverviewRangeQuery.Supervisor::class.java
                )
                 if (networkHelper.isNetworkConnected()) {
                    callFoodOverviewNullApi()
                     rangeOverViewSupervisorFoodKpi(foodKpiYesterdayDetailCEO)
                }

            } else if (apiFoodArgumentFromFilter == IpConstants.Yesterday) {
                val foodCeoDetail = gsonFoodKpi.fromJson(
                    intent.getStringExtra("food_data"),
                    SupervisorOverviewYesterdayQuery.Supervisor::class.java
                )
                if (networkHelper.isNetworkConnected()) {
                    callFoodOverviewNullApi()
                    yesterdayViewSupervisorFoodKpi(foodCeoDetail)
                }

            }
        }

    }

    // gm
    private fun yesterdayViewGMFoodKpi(data: StoreYesterdayKPIQuery.GeneralManager) {
        try {
        val foodKpiYesterdayGmData =  data.kpis?.store?.yesterday?.food!!

            Logger.info("Food Yesterday", "Food Overview KPI")

            Validation().checkNullValueToShowView(this, foodKpiYesterdayGmData.actualFood?.top5?.displayName, top5_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdayGmData.actualFood?.allItems?.displayName, all_item_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdayGmData.actualFood?.twentyOZ?.displayName, twenty_oz_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdayGmData.actualFood?.dough?.displayName, dough_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdayGmData.actualFood?.cheese?.displayName, cheese_parent)


            if (foodKpiYesterdayGmData.actualFood?.cheese == null || foodKpiYesterdayGmData.actualFood.cheese.displayName.isNullOrEmpty()) {
                cheese_parent.visibility = View.GONE
            } else {
                cheese_parent.visibility = View.VISIBLE
                cheese_text.text = foodKpiYesterdayGmData.actualFood.cheese.displayName
            }
            if (foodKpiYesterdayGmData.actualFood?.dough == null || foodKpiYesterdayGmData.actualFood.dough.displayName.isNullOrEmpty()) {
                dough_parent.visibility = View.GONE
            } else {
                dough_parent.visibility = View.VISIBLE
                dough_text.text = foodKpiYesterdayGmData.actualFood.dough.displayName
            }

                   if (foodKpiYesterdayGmData.actualFood?.twentyOZ == null || foodKpiYesterdayGmData.actualFood.twentyOZ.displayName.isNullOrEmpty()) {
                       twenty_oz_parent.visibility = View.GONE
            } else {
                       twenty_oz_parent.visibility = View.VISIBLE
                       twenty_oz_text.text = foodKpiYesterdayGmData.actualFood.twentyOZ.displayName
            }
            if (foodKpiYesterdayGmData.actualFood?.top5 == null || foodKpiYesterdayGmData.actualFood.top5.displayName.isNullOrEmpty()) {
                top5_parent.visibility = View.GONE
            } else {
                top5_parent.visibility = View.VISIBLE
                top_five_text.text = foodKpiYesterdayGmData.actualFood.top5.displayName
            }

            if (foodKpiYesterdayGmData.actualFood?.allItems == null || foodKpiYesterdayGmData.actualFood.allItems.displayName.isNullOrEmpty()) {
                all_item_parent.visibility = View.GONE
            } else {
                all_item_parent.visibility = View.VISIBLE
                all_items_text.text = foodKpiYesterdayGmData.actualFood.allItems.displayName
            }

            if (foodKpiYesterdayGmData.endingInventory == null || foodKpiYesterdayGmData.endingInventory.displayName.isNullOrEmpty()) {
                ll_ending_inventory.visibility = View.GONE
            } else {
                ll_ending_inventory.visibility = View.VISIBLE
                ending_inventory_display.text = foodKpiYesterdayGmData.endingInventory.displayName
            }

            if (foodKpiYesterdayGmData.foodBought == null || foodKpiYesterdayGmData.foodBought.displayName.isNullOrEmpty()) {
                ll_food_bought_display_parent.visibility = View.GONE
            } else {
                ll_food_bought_display_parent.visibility = View.VISIBLE
                food_bought_display.text = foodKpiYesterdayGmData.foodBought.displayName
            }

            actual_food_display.text = foodKpiYesterdayGmData.actualFood?.total?.displayName
            ideal_food_display.text = foodKpiYesterdayGmData.idealFood?.displayName

        food_scroll_parent.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 0f
                override fun onScrollChanged() {
                    if (food_scroll_parent.scrollY > y) {
                        parent_data_on_scroll_linear.visibility = View.VISIBLE
                        parent_data_on_scroll_view.visibility = View.VISIBLE
                        level_two_scroll_data_action.text = getString(R.string.food_text)

                        level_two_scroll_data_action_value.text = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actual?.amount, foodKpiYesterdayGmData.actual?.percentage, foodKpiYesterdayGmData.actual?.value)

                        if (foodKpiYesterdayGmData.status != null) {
                            when {
                                foodKpiYesterdayGmData.status.toString() == resources.getString(
                                    R.string.out_of_range) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.red_circle,
                                        0
                                    )
                                }
                                foodKpiYesterdayGmData.status.toString() == resources.getString(
                                    R.string.under_limit) -> {
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
                    y = food_scroll_parent.scrollY.toFloat()
                    if (y <= 0) {
                        parent_data_on_scroll_linear.visibility = View.INVISIBLE
                        parent_data_on_scroll_view.visibility = View.GONE

                    }
                }
            })

            food_sales.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actual?.amount, foodKpiYesterdayGmData.actual?.percentage, foodKpiYesterdayGmData.actual?.value)

            when {
                foodKpiYesterdayGmData.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_sales.setTextColor(getColor(R.color.red))
                }
                foodKpiYesterdayGmData.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_sales.setTextColor(getColor(R.color.green))
                } else -> {
                    food_sales.setTextColor(getColor(R.color.text_color))
                }
            }
            food_goal_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.goal?.amount, foodKpiYesterdayGmData.goal?.percentage, foodKpiYesterdayGmData.goal?.value)
            food_variance_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.variance?.amount, foodKpiYesterdayGmData.variance?.percentage, foodKpiYesterdayGmData.variance?.value)
            showFoodNarrativeData(foodKpiYesterdayGmData.narrative.toString())

            // ideal vs Actual food
            if (foodKpiYesterdayGmData.idealvsActualFoodVariance == null || foodKpiYesterdayGmData.idealvsActualFoodVariance.displayName.isNullOrEmpty()) {
                ll_ideal_vs_actual_food_variance.visibility = View.GONE
            } else {
                ll_ideal_vs_actual_food_variance.visibility = View.VISIBLE
                ideal_vs_actual_food_variance_display_name.text = foodKpiYesterdayGmData.idealvsActualFoodVariance.displayName
            }

            val idealActualFoodGoalGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.idealvsActualFoodVariance?.goal?.amount, foodKpiYesterdayGmData.idealvsActualFoodVariance?.goal?.percentage, foodKpiYesterdayGmData.idealvsActualFoodVariance?.goal?.value)
            val idealActualFoodVarianceGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.idealvsActualFoodVariance?.variance?.amount, foodKpiYesterdayGmData.idealvsActualFoodVariance?.variance?.percentage, foodKpiYesterdayGmData.idealvsActualFoodVariance?.variance?.value)
            val idealActualFoodActualGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.idealvsActualFoodVariance?.actual?.amount, foodKpiYesterdayGmData.idealvsActualFoodVariance?.actual?.percentage, foodKpiYesterdayGmData.idealvsActualFoodVariance?.actual?.value)
            idealActualFoodData(idealActualFoodGoalGMYesterday,idealActualFoodVarianceGMYesterday,idealActualFoodActualGMYesterday)

        if (foodKpiYesterdayGmData.idealvsActualFoodVariance?.status != null) {
            when {
                foodKpiYesterdayGmData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.out_of_range) -> {
                    ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.red))
                }
                foodKpiYesterdayGmData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.under_limit) -> {
                    ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }

            // Ideal Food
            val idealFoodGoalGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.idealFood?.goal?.amount, foodKpiYesterdayGmData.idealFood?.goal?.percentage, foodKpiYesterdayGmData.idealFood?.goal?.value)
            val idealFoodVarianceGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.idealFood?.variance?.amount, foodKpiYesterdayGmData.idealFood?.variance?.percentage, foodKpiYesterdayGmData.idealFood?.variance?.value)
            val idealFoodActualGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.idealFood?.actual?.amount, foodKpiYesterdayGmData.idealFood?.actual?.percentage, foodKpiYesterdayGmData.idealFood?.actual?.value)
            idealFoodData(idealFoodGoalGMYesterday,idealFoodVarianceGMYesterday,idealFoodActualGMYesterday)

        if (foodKpiYesterdayGmData.idealFood?.status != null) {
            when {
                foodKpiYesterdayGmData.idealFood.status.toString() == resources.getString(R.string.out_of_range) -> {
                    ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    ideal_food_actual.setTextColor(getColor(R.color.red))
                }
                foodKpiYesterdayGmData.idealFood.status.toString() == resources.getString(R.string.under_limit) -> {
                    ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    ideal_food_actual.setTextColor(getColor(R.color.green))

                } else -> {
                    ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    ideal_food_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }

            // actual food
            val actualFoodTotalGMYesterday =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.total?.actual?.amount, foodKpiYesterdayGmData.actualFood?.total?.actual?.percentage, foodKpiYesterdayGmData.actualFood?.total?.actual?.value)
            actualFoodTotalData(actualFoodTotalGMYesterday)
            if (foodKpiYesterdayGmData.actualFood?.total?.status != null) {
                when {
                    foodKpiYesterdayGmData.actualFood.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayGmData.actualFood.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.green))

                    } else -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.text_color))

                    }
                }
        }


        // cheese
            val cheeseGoalGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.cheese?.goal?.amount, foodKpiYesterdayGmData.actualFood?.cheese?.goal?.percentage, foodKpiYesterdayGmData.actualFood?.cheese?.goal?.value)
            val cheeseVarianceGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.cheese?.variance?.amount, foodKpiYesterdayGmData.actualFood?.cheese?.variance?.percentage, foodKpiYesterdayGmData.actualFood?.cheese?.variance?.value)
            val cheeseActualGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.cheese?.actual?.amount, foodKpiYesterdayGmData.actualFood?.cheese?.actual?.percentage, foodKpiYesterdayGmData.actualFood?.cheese?.actual?.value)
            cheeseActualFoodData(cheeseGoalGMYesterday,cheeseVarianceGMYesterday,cheeseActualGMYesterday)

            
        // dough
            val doughGoalGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.dough?.goal?.amount, foodKpiYesterdayGmData.actualFood?.dough?.goal?.percentage, foodKpiYesterdayGmData.actualFood?.dough?.goal?.value)
            val doughVarianceGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.dough?.variance?.amount, foodKpiYesterdayGmData.actualFood?.dough?.variance?.percentage, foodKpiYesterdayGmData.actualFood?.dough?.variance?.value)
            val doughActualGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.dough?.actual?.amount, foodKpiYesterdayGmData.actualFood?.dough?.actual?.percentage, foodKpiYesterdayGmData.actualFood?.dough?.actual?.value)
            doughActualFoodData(doughGoalGMYesterday,doughVarianceGMYesterday,doughActualGMYesterday)


        // twenty
            val twentyOzGoalGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.twentyOZ?.goal?.amount, foodKpiYesterdayGmData.actualFood?.twentyOZ?.goal?.percentage, foodKpiYesterdayGmData.actualFood?.twentyOZ?.goal?.value)
            val twentyOzVarianceGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.twentyOZ?.variance?.amount, foodKpiYesterdayGmData.actualFood?.twentyOZ?.variance?.percentage, foodKpiYesterdayGmData.actualFood?.twentyOZ?.variance?.value)
            val twentyOzActualGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.twentyOZ?.actual?.amount, foodKpiYesterdayGmData.actualFood?.twentyOZ?.actual?.percentage, foodKpiYesterdayGmData.actualFood?.twentyOZ?.actual?.value)
            twentyOzActualFoodData(twentyOzGoalGMYesterday,twentyOzVarianceGMYesterday,twentyOzActualGMYesterday)


        // top_five
            val top5GoalGMYesterday= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.top5?.goal?.amount, foodKpiYesterdayGmData.actualFood?.top5?.goal?.percentage, foodKpiYesterdayGmData.actualFood?.top5?.goal?.value)
            val top5VarianceGMYesterday= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.top5?.variance?.amount, foodKpiYesterdayGmData.actualFood?.top5?.variance?.percentage, foodKpiYesterdayGmData.actualFood?.top5?.variance?.value)
            val top5ActualGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.top5?.actual?.amount, foodKpiYesterdayGmData.actualFood?.top5?.actual?.percentage, foodKpiYesterdayGmData.actualFood?.top5?.actual?.value)
            top5ActualFoodData(top5GoalGMYesterday,top5VarianceGMYesterday,top5ActualGMYesterday)

        // all_items
            val allItemsGoalGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.allItems?.goal?.amount, foodKpiYesterdayGmData.actualFood?.allItems?.goal?.percentage, foodKpiYesterdayGmData.actualFood?.allItems?.goal?.value)
            val allItemsVarianceGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.allItems?.variance?.amount, foodKpiYesterdayGmData.actualFood?.allItems?.variance?.percentage, foodKpiYesterdayGmData.actualFood?.allItems?.variance?.value)
            val allItemsActualGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.actualFood?.allItems?.actual?.amount, foodKpiYesterdayGmData.actualFood?.allItems?.actual?.percentage, foodKpiYesterdayGmData.actualFood?.allItems?.actual?.value)
            allItemsData(allItemsGoalGMYesterday,allItemsVarianceGMYesterday,allItemsActualGMYesterday)

       // ending even try 
            val endingInventoryActualGMYesterday =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.endingInventory?.actual?.amount, foodKpiYesterdayGmData.endingInventory?.actual?.percentage, foodKpiYesterdayGmData.endingInventory?.actual?.value)
            endingInventoryData(endingInventoryActualGMYesterday)

            if (foodKpiYesterdayGmData.endingInventory?.status != null) {
                when {
                    foodKpiYesterdayGmData.endingInventory.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayGmData.endingInventory.status.toString() == resources.getString(R.string.under_limit) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
        }
           
            // food bought
            val foodBoughtActualGMYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayGmData.foodBought?.actual?.amount, foodKpiYesterdayGmData.foodBought?.actual?.percentage, foodKpiYesterdayGmData.foodBought?.actual?.value)
            foodBoughtData(foodBoughtActualGMYesterday)

        if (foodKpiYesterdayGmData.foodBought?.status != null) {
            when {
                foodKpiYesterdayGmData.foodBought.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    food_bought_actual.setTextColor(getColor(R.color.red))
                }
                foodKpiYesterdayGmData.foodBought.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    food_bought_actual.setTextColor(getColor(R.color.green))

                }else -> {
                    food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    food_bought_actual.setTextColor(getColor(R.color.text_color))

                }
            }
        }
     
        }catch (e:Exception){
            Logger.error(e.message.toString(),"Food Overview Yesterday KPI")
        }

    }

    private fun rangeOverViewViewGMFoodKpi(data: StorePeriodRangeKPIQuery.GeneralManager) {
        try {
            val foodKpiRangeOverViewGmData =  data.kpis?.store?.period?.food!!

            Logger.info("Food Period Range", "Food Overview KPI")

            // scroll detect
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewGmData.actualFood?.top5?.displayName, top5_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewGmData.actualFood?.allItems?.displayName, all_item_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewGmData.actualFood?.twentyOZ?.displayName, twenty_oz_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewGmData.actualFood?.dough?.displayName, dough_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewGmData.actualFood?.cheese?.displayName, cheese_parent)

                if (foodKpiRangeOverViewGmData.actualFood?.cheese == null || foodKpiRangeOverViewGmData.actualFood.cheese.displayName.isNullOrEmpty()) {
                cheese_parent.visibility = View.GONE
            } else {
                cheese_parent.visibility = View.VISIBLE
                cheese_text.text = foodKpiRangeOverViewGmData.actualFood.cheese.displayName
            }
            if (foodKpiRangeOverViewGmData.actualFood?.dough == null || foodKpiRangeOverViewGmData.actualFood.dough.displayName.isNullOrEmpty()) {
                dough_parent.visibility = View.GONE
            } else {
                dough_parent.visibility = View.VISIBLE
                dough_text.text = foodKpiRangeOverViewGmData.actualFood.dough.displayName
            }
                   if (foodKpiRangeOverViewGmData.actualFood?.twentyOZ == null || foodKpiRangeOverViewGmData.actualFood.twentyOZ.displayName.isNullOrEmpty()) {
                       twenty_oz_parent.visibility = View.GONE
            } else {
                       twenty_oz_parent.visibility = View.VISIBLE
                       twenty_oz_text.text = foodKpiRangeOverViewGmData.actualFood.twentyOZ.displayName
            }
         if (foodKpiRangeOverViewGmData.actualFood?.top5 == null || foodKpiRangeOverViewGmData.actualFood.top5.displayName.isNullOrEmpty()) {
                top5_parent.visibility = View.GONE
            } else {
                top5_parent.visibility = View.VISIBLE
                top_five_text.text = foodKpiRangeOverViewGmData.actualFood.top5.displayName
            }
            if (foodKpiRangeOverViewGmData.actualFood?.allItems == null || foodKpiRangeOverViewGmData.actualFood.allItems.displayName.isNullOrEmpty()) {
                all_item_parent.visibility = View.GONE
            } else {
                all_item_parent.visibility = View.VISIBLE
                all_items_text.text = foodKpiRangeOverViewGmData.actualFood.allItems.displayName
            }
            if (foodKpiRangeOverViewGmData.endingInventory == null || foodKpiRangeOverViewGmData.endingInventory.displayName.isNullOrEmpty()) {
                ll_ending_inventory.visibility = View.GONE
            } else {
                ll_ending_inventory.visibility = View.VISIBLE
                ending_inventory_display.text = foodKpiRangeOverViewGmData.endingInventory.displayName
            }

            if (foodKpiRangeOverViewGmData.foodBought == null || foodKpiRangeOverViewGmData.foodBought.displayName.isNullOrEmpty()) {
                ll_food_bought_display_parent.visibility = View.GONE
            } else {
                ll_food_bought_display_parent.visibility = View.VISIBLE
                food_bought_display.text = foodKpiRangeOverViewGmData.foodBought.displayName
            }


            actual_food_display.text = foodKpiRangeOverViewGmData.actualFood?.total?.displayName
            ideal_food_display.text = foodKpiRangeOverViewGmData.idealFood?.displayName

            food_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (food_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.food_text)
                           // level_two_scroll_data_action.text = foodKpiRangeOverViewGmData.displayName ?: getString(R.string.food_text)
                            level_two_scroll_data_action_value.text = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actual?.amount, foodKpiRangeOverViewGmData.actual?.percentage, foodKpiRangeOverViewGmData.actual?.value)

                            if (foodKpiRangeOverViewGmData.status != null) {
                                when {
                                    foodKpiRangeOverViewGmData.status.toString() == resources.getString(
                                        R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    foodKpiRangeOverViewGmData.status.toString() == resources.getString(
                                        R.string.under_limit) -> {
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
                        y = food_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            food_sales.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actual?.amount, foodKpiRangeOverViewGmData.actual?.percentage, foodKpiRangeOverViewGmData.actual?.value)

            if (foodKpiRangeOverViewGmData.status?.toString() != null) {
                when {
                    foodKpiRangeOverViewGmData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewGmData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            food_goal_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.goal?.amount, foodKpiRangeOverViewGmData.goal?.percentage, foodKpiRangeOverViewGmData.goal?.value)
            food_variance_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.variance?.amount, foodKpiRangeOverViewGmData.variance?.percentage, foodKpiRangeOverViewGmData.variance?.value)
            showFoodNarrativeData(foodKpiRangeOverViewGmData.narrative.toString())

            // ideal vs actual food
            if (foodKpiRangeOverViewGmData.idealvsActualFoodVariance == null || foodKpiRangeOverViewGmData.idealvsActualFoodVariance.displayName.isNullOrEmpty()) {
                ll_ideal_vs_actual_food_variance.visibility = View.GONE
            } else {
                ll_ideal_vs_actual_food_variance.visibility = View.VISIBLE
                ideal_vs_actual_food_variance_display_name.text = foodKpiRangeOverViewGmData.idealvsActualFoodVariance.displayName
            }

            val idealActualFoodGoalGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.idealvsActualFoodVariance?.goal?.amount, foodKpiRangeOverViewGmData.idealvsActualFoodVariance?.goal?.percentage, foodKpiRangeOverViewGmData.idealvsActualFoodVariance?.goal?.value)
            val idealActualFoodVarianceGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.idealvsActualFoodVariance?.variance?.amount, foodKpiRangeOverViewGmData.idealvsActualFoodVariance?.variance?.percentage, foodKpiRangeOverViewGmData.idealvsActualFoodVariance?.variance?.value)
            val idealActualFoodActualGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.idealvsActualFoodVariance?.actual?.amount, foodKpiRangeOverViewGmData.idealvsActualFoodVariance?.actual?.percentage, foodKpiRangeOverViewGmData.idealvsActualFoodVariance?.actual?.value)
            idealActualFoodData(idealActualFoodGoalGMRange,idealActualFoodVarianceGMRange,idealActualFoodActualGMRange)
            if (foodKpiRangeOverViewGmData.idealvsActualFoodVariance?.status != null) {
                when {
                    foodKpiRangeOverViewGmData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewGmData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            //Ideal Food
            val idealFoodGoalGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.idealFood?.goal?.amount, foodKpiRangeOverViewGmData.idealFood?.goal?.percentage, foodKpiRangeOverViewGmData.idealFood?.goal?.value)
            val idealFoodVarianceGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.idealFood?.variance?.amount, foodKpiRangeOverViewGmData.idealFood?.variance?.percentage, foodKpiRangeOverViewGmData.idealFood?.variance?.value)
            val idealFoodActualGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.idealFood?.actual?.amount, foodKpiRangeOverViewGmData.idealFood?.actual?.percentage, foodKpiRangeOverViewGmData.idealFood?.actual?.value)
            idealFoodData(idealFoodGoalGMRange,idealFoodVarianceGMRange,idealFoodActualGMRange)

            if (foodKpiRangeOverViewGmData.idealFood?.status != null) {
                when {
                    foodKpiRangeOverViewGmData.idealFood.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewGmData.idealFood.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // actual food
            val actualFoodTotalGMRange=  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.total?.actual?.amount, foodKpiRangeOverViewGmData.actualFood?.total?.actual?.percentage, foodKpiRangeOverViewGmData.actualFood?.total?.actual?.value)
            actualFoodTotalData(actualFoodTotalGMRange)
            if (foodKpiRangeOverViewGmData.actualFood?.total?.status != null) {
                when {
                    foodKpiRangeOverViewGmData.actualFood.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewGmData.actualFood.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.green))

                    } else -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // cheese
            val cheeseGoalGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.cheese?.goal?.amount, foodKpiRangeOverViewGmData.actualFood?.cheese?.goal?.percentage, foodKpiRangeOverViewGmData.actualFood?.cheese?.goal?.value)
            val cheeseVarianceGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.cheese?.variance?.amount, foodKpiRangeOverViewGmData.actualFood?.cheese?.variance?.percentage, foodKpiRangeOverViewGmData.actualFood?.cheese?.variance?.value)
            val cheeseActualGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.cheese?.actual?.amount, foodKpiRangeOverViewGmData.actualFood?.cheese?.actual?.percentage, foodKpiRangeOverViewGmData.actualFood?.cheese?.actual?.value)
            cheeseActualFoodData(cheeseGoalGMRange,cheeseVarianceGMRange,cheeseActualGMRange)

            // dough
            val doughGoalGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.dough?.goal?.amount, foodKpiRangeOverViewGmData.actualFood?.dough?.goal?.percentage, foodKpiRangeOverViewGmData.actualFood?.dough?.goal?.value)
            val doughVarianceGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.dough?.variance?.amount, foodKpiRangeOverViewGmData.actualFood?.dough?.variance?.percentage, foodKpiRangeOverViewGmData.actualFood?.dough?.variance?.value)
            val doughActualGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.dough?.actual?.amount, foodKpiRangeOverViewGmData.actualFood?.dough?.actual?.percentage, foodKpiRangeOverViewGmData.actualFood?.dough?.actual?.value)
            doughActualFoodData(doughGoalGMRange,doughVarianceGMRange,doughActualGMRange)


            // twenty
            val twentyOzGoalGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.twentyOZ?.goal?.amount, foodKpiRangeOverViewGmData.actualFood?.twentyOZ?.goal?.percentage, foodKpiRangeOverViewGmData.actualFood?.twentyOZ?.goal?.value)
            val twentyOzVarianceGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.twentyOZ?.variance?.amount, foodKpiRangeOverViewGmData.actualFood?.twentyOZ?.variance?.percentage, foodKpiRangeOverViewGmData.actualFood?.twentyOZ?.variance?.value)
            val twentyOzActualGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.twentyOZ?.actual?.amount, foodKpiRangeOverViewGmData.actualFood?.twentyOZ?.actual?.percentage, foodKpiRangeOverViewGmData.actualFood?.twentyOZ?.actual?.value)
            twentyOzActualFoodData(twentyOzGoalGMRange,twentyOzVarianceGMRange,twentyOzActualGMRange)


            // top_five
            val top5GoalGMRange= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.top5?.goal?.amount, foodKpiRangeOverViewGmData.actualFood?.top5?.goal?.percentage, foodKpiRangeOverViewGmData.actualFood?.top5?.goal?.value)
            val top5VarianceGMRange= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.top5?.variance?.amount, foodKpiRangeOverViewGmData.actualFood?.top5?.variance?.percentage, foodKpiRangeOverViewGmData.actualFood?.top5?.variance?.value)
            val top5ActualGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.top5?.actual?.amount, foodKpiRangeOverViewGmData.actualFood?.top5?.actual?.percentage, foodKpiRangeOverViewGmData.actualFood?.top5?.actual?.value)
            top5ActualFoodData(top5GoalGMRange,top5VarianceGMRange,top5ActualGMRange)

            // all_items
            val allItemsGoalGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.allItems?.goal?.amount, foodKpiRangeOverViewGmData.actualFood?.allItems?.goal?.percentage, foodKpiRangeOverViewGmData.actualFood?.allItems?.goal?.value)
            val allItemsVarianceGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.allItems?.variance?.amount, foodKpiRangeOverViewGmData.actualFood?.allItems?.variance?.percentage, foodKpiRangeOverViewGmData.actualFood?.allItems?.variance?.value)
            val allItemsActualGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.actualFood?.allItems?.actual?.amount, foodKpiRangeOverViewGmData.actualFood?.allItems?.actual?.percentage, foodKpiRangeOverViewGmData.actualFood?.allItems?.actual?.value)
            allItemsData(allItemsGoalGMRange,allItemsVarianceGMRange,allItemsActualGMRange)

            // ending even try
            val endingInventoryActualGMRange =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.endingInventory?.actual?.amount, foodKpiRangeOverViewGmData.endingInventory?.actual?.percentage, foodKpiRangeOverViewGmData.endingInventory?.actual?.value)
            endingInventoryData(endingInventoryActualGMRange)

            if (foodKpiRangeOverViewGmData.endingInventory?.status != null) {
                when {
                    foodKpiRangeOverViewGmData.endingInventory.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewGmData.endingInventory.status.toString() == resources.getString(R.string.under_limit) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
           
            // food bought
            val foodBoughtActualGMRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewGmData.foodBought?.actual?.amount, foodKpiRangeOverViewGmData.foodBought?.actual?.percentage, foodKpiRangeOverViewGmData.foodBought?.actual?.value)
            foodBoughtData(foodBoughtActualGMRange)

            if (foodKpiRangeOverViewGmData.foodBought?.status != null) {
                when {
                    foodKpiRangeOverViewGmData.foodBought.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewGmData.foodBought.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
        }catch (e:Exception){
            Logger.error(e.message.toString(),"Food Overview Period Range KPI")
        }

    }

    // ceo
    private fun yesterdayViewCEOFoodKpi(data: CEOOverviewYesterdayQuery.Ceo) {
        try {
            val foodKpiYesterdayCeoData =  data.kpis?.supervisors?.stores?.yesterday?.food!!

            Logger.info("Food Yesterday", "Food Overview KPI")

            // scroll detect
            // ideal_vs_food_variance_text.text = foodKpiYesterdayCeoData.displayName ?: getString(R.string.food_text)
            Validation().checkNullValueToShowView(this, foodKpiYesterdayCeoData.actualFood?.top5?.displayName, top5_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdayCeoData.actualFood?.allItems?.displayName, all_item_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdayCeoData.actualFood?.twentyOZ?.displayName, twenty_oz_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdayCeoData.actualFood?.dough?.displayName, dough_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdayCeoData.actualFood?.cheese?.displayName, cheese_parent)

             if (foodKpiYesterdayCeoData.actualFood?.cheese == null || foodKpiYesterdayCeoData.actualFood.cheese.displayName.isNullOrEmpty()) {
                cheese_parent.visibility = View.GONE
            } else {
                cheese_parent.visibility = View.VISIBLE
                cheese_text.text = foodKpiYesterdayCeoData.actualFood.cheese.displayName
            }
            if (foodKpiYesterdayCeoData.actualFood?.dough == null || foodKpiYesterdayCeoData.actualFood.dough.displayName.isNullOrEmpty()) {
                dough_parent.visibility = View.GONE
            } else {
                dough_parent.visibility = View.VISIBLE
                dough_text.text = foodKpiYesterdayCeoData.actualFood.dough.displayName
            }
                   if (foodKpiYesterdayCeoData.actualFood?.twentyOZ == null || foodKpiYesterdayCeoData.actualFood.twentyOZ.displayName.isNullOrEmpty()) {
                       twenty_oz_parent.visibility = View.GONE
            } else {
                       twenty_oz_parent.visibility = View.VISIBLE
                       twenty_oz_text.text = foodKpiYesterdayCeoData.actualFood.twentyOZ.displayName
            }
         if (foodKpiYesterdayCeoData.actualFood?.top5 == null || foodKpiYesterdayCeoData.actualFood.top5.displayName.isNullOrEmpty()) {
                top5_parent.visibility = View.GONE
            } else {
                top5_parent.visibility = View.VISIBLE
                top_five_text.text = foodKpiYesterdayCeoData.actualFood.top5.displayName
            }
            if (foodKpiYesterdayCeoData.actualFood?.allItems == null || foodKpiYesterdayCeoData.actualFood.allItems.displayName.isNullOrEmpty()) {
                all_item_parent.visibility = View.GONE
            } else {
                all_item_parent.visibility = View.VISIBLE
                all_items_text.text = foodKpiYesterdayCeoData.actualFood.allItems.displayName
            }
            if (foodKpiYesterdayCeoData.endingInventory == null || foodKpiYesterdayCeoData.endingInventory.displayName.isNullOrEmpty()) {
                ll_ending_inventory.visibility = View.GONE
            } else {
                ll_ending_inventory.visibility = View.VISIBLE
                ending_inventory_display.text = foodKpiYesterdayCeoData.endingInventory.displayName
            }

            if (foodKpiYesterdayCeoData.foodBought == null || foodKpiYesterdayCeoData.foodBought.displayName.isNullOrEmpty()) {
                ll_food_bought_display_parent.visibility = View.GONE
            } else {
                ll_food_bought_display_parent.visibility = View.VISIBLE
                food_bought_display.text = foodKpiYesterdayCeoData.foodBought.displayName
            }


            actual_food_display.text = foodKpiYesterdayCeoData.actualFood?.total?.displayName
            ideal_food_display.text = foodKpiYesterdayCeoData.idealFood?.displayName

            food_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (food_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.food_text)

                            level_two_scroll_data_action_value.text = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actual?.amount, foodKpiYesterdayCeoData.actual?.percentage, foodKpiYesterdayCeoData.actual?.value)

                            if (foodKpiYesterdayCeoData.status != null) {
                                when {
                                    foodKpiYesterdayCeoData.status.toString() == resources.getString(
                                        R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    foodKpiYesterdayCeoData.status.toString() == resources.getString(
                                        R.string.under_limit) -> {
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
                        y = food_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            food_sales.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actual?.amount, foodKpiYesterdayCeoData.actual?.percentage, foodKpiYesterdayCeoData.actual?.value)

            if (foodKpiYesterdayCeoData.status?.toString() != null) {
                when {
                    foodKpiYesterdayCeoData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayCeoData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            food_goal_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.goal?.amount, foodKpiYesterdayCeoData.goal?.percentage, foodKpiYesterdayCeoData.goal?.value)
            food_variance_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.variance?.amount, foodKpiYesterdayCeoData.variance?.percentage, foodKpiYesterdayCeoData.variance?.value)
            showFoodNarrativeData(foodKpiYesterdayCeoData.narrative.toString())

            // ideal vs Actual food
            if (foodKpiYesterdayCeoData.idealvsActualFoodVariance == null || foodKpiYesterdayCeoData.idealvsActualFoodVariance.displayName.isNullOrEmpty()) {
                ll_ideal_vs_actual_food_variance.visibility = View.GONE
            } else {
                ll_ideal_vs_actual_food_variance.visibility = View.VISIBLE
                ideal_vs_actual_food_variance_display_name.text = foodKpiYesterdayCeoData.idealvsActualFoodVariance.displayName
            }

            val idealActualFoodGoalCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.idealvsActualFoodVariance?.goal?.amount, foodKpiYesterdayCeoData.idealvsActualFoodVariance?.goal?.percentage, foodKpiYesterdayCeoData.idealvsActualFoodVariance?.goal?.value)
            val idealActualFoodVarianceCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.idealvsActualFoodVariance?.variance?.amount, foodKpiYesterdayCeoData.idealvsActualFoodVariance?.variance?.percentage, foodKpiYesterdayCeoData.idealvsActualFoodVariance?.variance?.value)
            val idealActualFoodActualCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.idealvsActualFoodVariance?.actual?.amount, foodKpiYesterdayCeoData.idealvsActualFoodVariance?.actual?.percentage, foodKpiYesterdayCeoData.idealvsActualFoodVariance?.actual?.value)
            idealActualFoodData(idealActualFoodGoalCeoYesterday,idealActualFoodVarianceCeoYesterday,idealActualFoodActualCeoYesterday)
            if (foodKpiYesterdayCeoData.idealvsActualFoodVariance?.status != null) {
                when {
                    foodKpiYesterdayCeoData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayCeoData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            //Ideal Food

            val idealFoodGoalCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.idealFood?.goal?.amount, foodKpiYesterdayCeoData.idealFood?.goal?.percentage, foodKpiYesterdayCeoData.idealFood?.goal?.value)
            val idealFoodVarianceCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.idealFood?.variance?.amount, foodKpiYesterdayCeoData.idealFood?.variance?.percentage, foodKpiYesterdayCeoData.idealFood?.variance?.value)
            val idealFoodActualCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.idealFood?.actual?.amount, foodKpiYesterdayCeoData.idealFood?.actual?.percentage, foodKpiYesterdayCeoData.idealFood?.actual?.value)
            idealFoodData(idealFoodGoalCeoYesterday,idealFoodVarianceCeoYesterday,idealFoodActualCeoYesterday)

            if (foodKpiYesterdayCeoData.idealFood?.status != null) {
                when {
                    foodKpiYesterdayCeoData.idealFood.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayCeoData.idealFood.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // actual food
            val actualFoodTotalCEOYesterday =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.total?.actual?.amount, foodKpiYesterdayCeoData.actualFood?.total?.actual?.percentage, foodKpiYesterdayCeoData.actualFood?.total?.actual?.value)
            actualFoodTotalData(actualFoodTotalCEOYesterday)
            if (foodKpiYesterdayCeoData.actualFood?.total?.status != null) {
                when {
                    foodKpiYesterdayCeoData.actualFood.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayCeoData.actualFood.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.green))

                    } else -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val cheeseGoalCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.cheese?.goal?.amount, foodKpiYesterdayCeoData.actualFood?.cheese?.goal?.percentage, foodKpiYesterdayCeoData.actualFood?.cheese?.goal?.value)
            val cheeseVarianceCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.cheese?.variance?.amount, foodKpiYesterdayCeoData.actualFood?.cheese?.variance?.percentage, foodKpiYesterdayCeoData.actualFood?.cheese?.variance?.value)
            val cheeseActualCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.cheese?.actual?.amount, foodKpiYesterdayCeoData.actualFood?.cheese?.actual?.percentage, foodKpiYesterdayCeoData.actualFood?.cheese?.actual?.value)
            cheeseActualFoodData(cheeseGoalCeoYesterday,cheeseVarianceCeoYesterday,cheeseActualCeoYesterday)

            // dough
            val doughGoalCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.dough?.goal?.amount, foodKpiYesterdayCeoData.actualFood?.dough?.goal?.percentage, foodKpiYesterdayCeoData.actualFood?.dough?.goal?.value)
            val doughVarianceCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.dough?.variance?.amount, foodKpiYesterdayCeoData.actualFood?.dough?.variance?.percentage, foodKpiYesterdayCeoData.actualFood?.dough?.variance?.value)
            val doughActualCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.dough?.actual?.amount, foodKpiYesterdayCeoData.actualFood?.dough?.actual?.percentage, foodKpiYesterdayCeoData.actualFood?.dough?.actual?.value)
            doughActualFoodData(doughGoalCeoYesterday,doughVarianceCeoYesterday,doughActualCeoYesterday)

            // twenty
            val twentyOzGoalCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.twentyOZ?.goal?.amount, foodKpiYesterdayCeoData.actualFood?.twentyOZ?.goal?.percentage, foodKpiYesterdayCeoData.actualFood?.twentyOZ?.goal?.value)
            val twentyOzVarianceCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.twentyOZ?.variance?.amount, foodKpiYesterdayCeoData.actualFood?.twentyOZ?.variance?.percentage, foodKpiYesterdayCeoData.actualFood?.twentyOZ?.variance?.value)
            val twentyOzActualCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.twentyOZ?.actual?.amount, foodKpiYesterdayCeoData.actualFood?.twentyOZ?.actual?.percentage, foodKpiYesterdayCeoData.actualFood?.twentyOZ?.actual?.value)
            twentyOzActualFoodData(twentyOzGoalCeoYesterday,twentyOzVarianceCeoYesterday,twentyOzActualCeoYesterday)

            // top_five
            val top5GoalCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.top5?.goal?.amount, foodKpiYesterdayCeoData.actualFood?.top5?.goal?.percentage, foodKpiYesterdayCeoData.actualFood?.top5?.goal?.value)
            val top5VarianceCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.top5?.variance?.amount, foodKpiYesterdayCeoData.actualFood?.top5?.variance?.percentage, foodKpiYesterdayCeoData.actualFood?.top5?.variance?.value)
            val top5ActualCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.top5?.actual?.amount, foodKpiYesterdayCeoData.actualFood?.top5?.actual?.percentage, foodKpiYesterdayCeoData.actualFood?.top5?.actual?.value)
            top5ActualFoodData(top5GoalCeoYesterday,top5VarianceCeoYesterday,top5ActualCeoYesterday)

            // all_items
            val allItemsGoalCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.allItems?.goal?.amount, foodKpiYesterdayCeoData.actualFood?.allItems?.goal?.percentage, foodKpiYesterdayCeoData.actualFood?.allItems?.goal?.value)
            val allItemsVarianceCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.allItems?.variance?.amount, foodKpiYesterdayCeoData.actualFood?.allItems?.variance?.percentage, foodKpiYesterdayCeoData.actualFood?.allItems?.variance?.value)
            val allItemsActualCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.actualFood?.allItems?.actual?.amount, foodKpiYesterdayCeoData.actualFood?.allItems?.actual?.percentage, foodKpiYesterdayCeoData.actualFood?.allItems?.actual?.value)
            allItemsData(allItemsGoalCeoYesterday,allItemsVarianceCeoYesterday,allItemsActualCeoYesterday)

            // ending even try
            val endingInventoryActualCeoYesterday =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.endingInventory?.actual?.amount, foodKpiYesterdayCeoData.endingInventory?.actual?.percentage, foodKpiYesterdayCeoData.endingInventory?.actual?.value)
            endingInventoryData(endingInventoryActualCeoYesterday)

            if (foodKpiYesterdayCeoData.endingInventory?.status != null) {
                when {
                    foodKpiYesterdayCeoData.endingInventory.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayCeoData.endingInventory.status.toString() == resources.getString(R.string.under_limit) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // food bought
            val foodBoughtActualCeoYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayCeoData.foodBought?.actual?.amount, foodKpiYesterdayCeoData.foodBought?.actual?.percentage, foodKpiYesterdayCeoData.foodBought?.actual?.value)
            foodBoughtData(foodBoughtActualCeoYesterday)

            if (foodKpiYesterdayCeoData.foodBought?.status != null) {
                when {
                    foodKpiYesterdayCeoData.foodBought.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayCeoData.foodBought.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

        }catch (e:Exception){
            Logger.error(e.message.toString(),"Food Overview Yesterday KPI")
        }

    }

    private fun rangeOverViewCEOFoodKpi(data: CEOOverviewRangeQuery.Ceo) {
        try {
            val foodKpiRangeOverViewCeoData =  data.kpis?.supervisors?.stores?.period?.food!!

            Logger.info("Food Period Range", "Food Overview KPI")


            // scroll detect
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewCeoData.actualFood?.top5?.displayName, top5_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewCeoData.actualFood?.allItems?.displayName, all_item_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewCeoData.actualFood?.twentyOZ?.displayName, twenty_oz_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewCeoData.actualFood?.dough?.displayName, dough_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewCeoData.actualFood?.cheese?.displayName, cheese_parent)

             if (foodKpiRangeOverViewCeoData.actualFood?.cheese == null || foodKpiRangeOverViewCeoData.actualFood.cheese.displayName.isNullOrEmpty()) {
                cheese_parent.visibility = View.GONE
            } else {
                cheese_parent.visibility = View.VISIBLE
                cheese_text.text = foodKpiRangeOverViewCeoData.actualFood.cheese.displayName
            }
            if (foodKpiRangeOverViewCeoData.actualFood?.dough == null || foodKpiRangeOverViewCeoData.actualFood.dough.displayName.isNullOrEmpty()) {
                dough_parent.visibility = View.GONE
            } else {
                dough_parent.visibility = View.VISIBLE
                dough_text.text = foodKpiRangeOverViewCeoData.actualFood.dough.displayName
            }
                   if (foodKpiRangeOverViewCeoData.actualFood?.twentyOZ == null || foodKpiRangeOverViewCeoData.actualFood.twentyOZ.displayName.isNullOrEmpty()) {
                       twenty_oz_parent.visibility = View.GONE
            } else {
                       twenty_oz_parent.visibility = View.VISIBLE
                       twenty_oz_text.text = foodKpiRangeOverViewCeoData.actualFood.twentyOZ.displayName
            }
         if (foodKpiRangeOverViewCeoData.actualFood?.top5 == null || foodKpiRangeOverViewCeoData.actualFood.top5.displayName.isNullOrEmpty()) {
                top5_parent.visibility = View.GONE
            } else {
                top5_parent.visibility = View.VISIBLE
                top_five_text.text = foodKpiRangeOverViewCeoData.actualFood.top5.displayName
            }
            if (foodKpiRangeOverViewCeoData.actualFood?.allItems == null || foodKpiRangeOverViewCeoData.actualFood.allItems.displayName.isNullOrEmpty()) {
                all_item_parent.visibility = View.GONE
            } else {
                all_item_parent.visibility = View.VISIBLE
                all_items_text.text = foodKpiRangeOverViewCeoData.actualFood.allItems.displayName
            }
            if (foodKpiRangeOverViewCeoData.endingInventory == null || foodKpiRangeOverViewCeoData.endingInventory.displayName.isNullOrEmpty()) {
                ll_ending_inventory.visibility = View.GONE
            } else {
                ll_ending_inventory.visibility = View.VISIBLE
                ending_inventory_display.text = foodKpiRangeOverViewCeoData.endingInventory.displayName
            }

            if (foodKpiRangeOverViewCeoData.foodBought == null || foodKpiRangeOverViewCeoData.foodBought.displayName.isNullOrEmpty()) {
                ll_food_bought_display_parent.visibility = View.GONE
            } else {
                ll_food_bought_display_parent.visibility = View.VISIBLE
                food_bought_display.text = foodKpiRangeOverViewCeoData.foodBought.displayName
            }



            actual_food_display.text = foodKpiRangeOverViewCeoData.actualFood?.total?.displayName
            ideal_food_display.text = foodKpiRangeOverViewCeoData.idealFood?.displayName

            food_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (food_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            // level_two_scroll_data_action.text = foodKpiRangeOverViewCeoData.displayName ?: getString(R.string.food_text)
                            level_two_scroll_data_action.text = getString(R.string.food_text)

                            level_two_scroll_data_action_value.text = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actual?.amount, foodKpiRangeOverViewCeoData.actual?.percentage, foodKpiRangeOverViewCeoData.actual?.value)

                            if (foodKpiRangeOverViewCeoData.status != null) {
                                when {
                                    foodKpiRangeOverViewCeoData.status.toString() == resources.getString(
                                        R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    foodKpiRangeOverViewCeoData.status.toString() == resources.getString(
                                        R.string.under_limit) -> {
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
                        y = food_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            food_sales.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actual?.amount, foodKpiRangeOverViewCeoData.actual?.percentage, foodKpiRangeOverViewCeoData.actual?.value)

            if (foodKpiRangeOverViewCeoData.status?.toString() != null) {
                when {
                    foodKpiRangeOverViewCeoData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewCeoData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            food_goal_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.goal?.amount, foodKpiRangeOverViewCeoData.goal?.percentage, foodKpiRangeOverViewCeoData.goal?.value)
            food_variance_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.variance?.amount, foodKpiRangeOverViewCeoData.variance?.percentage, foodKpiRangeOverViewCeoData.variance?.value)
            showFoodNarrativeData(foodKpiRangeOverViewCeoData.narrative.toString())

            // Ideal vs Actual food variance
            if (foodKpiRangeOverViewCeoData.idealvsActualFoodVariance == null || foodKpiRangeOverViewCeoData.idealvsActualFoodVariance.displayName.isNullOrEmpty()) {
                ll_ideal_vs_actual_food_variance.visibility = View.GONE
            } else {
                ll_ideal_vs_actual_food_variance.visibility = View.VISIBLE
                ideal_vs_actual_food_variance_display_name.text = foodKpiRangeOverViewCeoData.idealvsActualFoodVariance.displayName
            }

            val idealActualFoodGoalCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.idealvsActualFoodVariance?.goal?.amount, foodKpiRangeOverViewCeoData.idealvsActualFoodVariance?.goal?.percentage, foodKpiRangeOverViewCeoData.idealvsActualFoodVariance?.goal?.value)
            val idealActualFoodVarianceCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.idealvsActualFoodVariance?.variance?.amount, foodKpiRangeOverViewCeoData.idealvsActualFoodVariance?.variance?.percentage, foodKpiRangeOverViewCeoData.idealvsActualFoodVariance?.variance?.value)
            val idealActualFoodActualCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.idealvsActualFoodVariance?.actual?.amount, foodKpiRangeOverViewCeoData.idealvsActualFoodVariance?.actual?.percentage, foodKpiRangeOverViewCeoData.idealvsActualFoodVariance?.actual?.value)
            idealActualFoodData(idealActualFoodGoalCeoRange,idealActualFoodVarianceCeoRange,idealActualFoodActualCeoRange)

            if (foodKpiRangeOverViewCeoData.idealvsActualFoodVariance?.status != null) {
                when {
                    foodKpiRangeOverViewCeoData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewCeoData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            //Ideal Food
            val idealFoodGoalCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.idealFood?.goal?.amount, foodKpiRangeOverViewCeoData.idealFood?.goal?.percentage, foodKpiRangeOverViewCeoData.idealFood?.goal?.value)
            val idealFoodVarianceCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.idealFood?.variance?.amount, foodKpiRangeOverViewCeoData.idealFood?.variance?.percentage, foodKpiRangeOverViewCeoData.idealFood?.variance?.value)
            val idealFoodActualCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.idealFood?.actual?.amount, foodKpiRangeOverViewCeoData.idealFood?.actual?.percentage, foodKpiRangeOverViewCeoData.idealFood?.actual?.value)
            idealFoodData(idealFoodGoalCeoRange,idealFoodVarianceCeoRange,idealFoodActualCeoRange)

            if (foodKpiRangeOverViewCeoData.idealFood?.status != null) {
                when {
                    foodKpiRangeOverViewCeoData.idealFood.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewCeoData.idealFood.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // actual food
            val actualFoodTotalCEORange =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.total?.actual?.amount, foodKpiRangeOverViewCeoData.actualFood?.total?.actual?.percentage, foodKpiRangeOverViewCeoData.actualFood?.total?.actual?.value)
            actualFoodTotalData(actualFoodTotalCEORange)

            if (foodKpiRangeOverViewCeoData.actualFood?.total?.status != null) {
                when {
                    foodKpiRangeOverViewCeoData.actualFood.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewCeoData.actualFood.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.green))

                    } else -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // cheese
            val cheeseGoalCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.cheese?.goal?.amount, foodKpiRangeOverViewCeoData.actualFood?.cheese?.goal?.percentage, foodKpiRangeOverViewCeoData.actualFood?.cheese?.goal?.value)
            val cheeseVarianceCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.cheese?.variance?.amount, foodKpiRangeOverViewCeoData.actualFood?.cheese?.variance?.percentage, foodKpiRangeOverViewCeoData.actualFood?.cheese?.variance?.value)
            val cheeseActualCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.cheese?.actual?.amount, foodKpiRangeOverViewCeoData.actualFood?.cheese?.actual?.percentage, foodKpiRangeOverViewCeoData.actualFood?.cheese?.actual?.value)
            cheeseActualFoodData(cheeseGoalCeoRange,cheeseVarianceCeoRange,cheeseActualCeoRange)

            // dough
            val doughGoalCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.dough?.goal?.amount, foodKpiRangeOverViewCeoData.actualFood?.dough?.goal?.percentage, foodKpiRangeOverViewCeoData.actualFood?.dough?.goal?.value)
            val doughVarianceCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.dough?.variance?.amount, foodKpiRangeOverViewCeoData.actualFood?.dough?.variance?.percentage, foodKpiRangeOverViewCeoData.actualFood?.dough?.variance?.value)
            val doughActualCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.dough?.actual?.amount, foodKpiRangeOverViewCeoData.actualFood?.dough?.actual?.percentage, foodKpiRangeOverViewCeoData.actualFood?.dough?.actual?.value)
            doughActualFoodData(doughGoalCeoRange,doughVarianceCeoRange,doughActualCeoRange)

            // twenty
            val twentyOzGoalCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.twentyOZ?.goal?.amount, foodKpiRangeOverViewCeoData.actualFood?.twentyOZ?.goal?.percentage, foodKpiRangeOverViewCeoData.actualFood?.twentyOZ?.goal?.value)
            val twentyOzVarianceCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.twentyOZ?.variance?.amount, foodKpiRangeOverViewCeoData.actualFood?.twentyOZ?.variance?.percentage, foodKpiRangeOverViewCeoData.actualFood?.twentyOZ?.variance?.value)
            val twentyOzActualCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.twentyOZ?.actual?.amount, foodKpiRangeOverViewCeoData.actualFood?.twentyOZ?.actual?.percentage, foodKpiRangeOverViewCeoData.actualFood?.twentyOZ?.actual?.value)
            twentyOzActualFoodData(twentyOzGoalCeoRange,twentyOzVarianceCeoRange,twentyOzActualCeoRange)

            // top_five
            val top5GoalCeoRange= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.top5?.goal?.amount, foodKpiRangeOverViewCeoData.actualFood?.top5?.goal?.percentage, foodKpiRangeOverViewCeoData.actualFood?.top5?.goal?.value)
            val top5VarianceCeoRange= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.top5?.variance?.amount, foodKpiRangeOverViewCeoData.actualFood?.top5?.variance?.percentage, foodKpiRangeOverViewCeoData.actualFood?.top5?.variance?.value)
            val top5ActualCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.top5?.actual?.amount, foodKpiRangeOverViewCeoData.actualFood?.top5?.actual?.percentage, foodKpiRangeOverViewCeoData.actualFood?.top5?.actual?.value)
            top5ActualFoodData(top5GoalCeoRange,top5VarianceCeoRange,top5ActualCeoRange)

            // all_items
            val allItemsGoalCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.allItems?.goal?.amount, foodKpiRangeOverViewCeoData.actualFood?.allItems?.goal?.percentage, foodKpiRangeOverViewCeoData.actualFood?.allItems?.goal?.value)
            val allItemsVarianceCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.allItems?.variance?.amount, foodKpiRangeOverViewCeoData.actualFood?.allItems?.variance?.percentage, foodKpiRangeOverViewCeoData.actualFood?.allItems?.variance?.value)
            val allItemsActualCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.actualFood?.allItems?.actual?.amount, foodKpiRangeOverViewCeoData.actualFood?.allItems?.actual?.percentage, foodKpiRangeOverViewCeoData.actualFood?.allItems?.actual?.value)
            allItemsData(allItemsGoalCeoRange,allItemsVarianceCeoRange,allItemsActualCeoRange)

            // ending even try
            val endingInventoryActualCeoRange =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.endingInventory?.actual?.amount, foodKpiRangeOverViewCeoData.endingInventory?.actual?.percentage, foodKpiRangeOverViewCeoData.endingInventory?.actual?.value)
            endingInventoryData(endingInventoryActualCeoRange)
            if (foodKpiRangeOverViewCeoData.endingInventory?.status != null) {
                when {
                    foodKpiRangeOverViewCeoData.endingInventory.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewCeoData.endingInventory.status.toString() == resources.getString(R.string.under_limit) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // food bought
            val foodBoughtActualCeoRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewCeoData.foodBought?.actual?.amount, foodKpiRangeOverViewCeoData.foodBought?.actual?.percentage, foodKpiRangeOverViewCeoData.foodBought?.actual?.value)
            foodBoughtData(foodBoughtActualCeoRange)
            if (foodKpiRangeOverViewCeoData.foodBought?.status != null) {
                when {
                    foodKpiRangeOverViewCeoData.foodBought.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewCeoData.foodBought.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

        }catch (e:Exception){
            Logger.error(e.message.toString(),"Food Overview Period Range KPI")
        }

    }

    // do overview
    private fun yesterdayViewDOFoodKpi(data: DOOverviewYesterdayQuery.Do_) {
        try {
            val foodKpiYesterdayDoData =  data.kpis?.supervisors?.stores?.yesterday?.food!!

            Logger.info("Food Yesterday", "Food Overview KPI")


            // scroll detect
            Validation().checkNullValueToShowView(this, foodKpiYesterdayDoData.actualFood?.top5?.displayName, top5_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdayDoData.actualFood?.allItems?.displayName, all_item_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdayDoData.actualFood?.twentyOZ?.displayName, twenty_oz_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdayDoData.actualFood?.dough?.displayName, dough_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdayDoData.actualFood?.cheese?.displayName, cheese_parent)

             if (foodKpiYesterdayDoData.actualFood?.cheese == null || foodKpiYesterdayDoData.actualFood.cheese.displayName.isNullOrEmpty()) {
                cheese_parent.visibility = View.GONE
            } else {
                cheese_parent.visibility = View.VISIBLE
                cheese_text.text = foodKpiYesterdayDoData.actualFood.cheese.displayName
            }
            if (foodKpiYesterdayDoData.actualFood?.dough == null || foodKpiYesterdayDoData.actualFood.dough.displayName.isNullOrEmpty()) {
                dough_parent.visibility = View.GONE
            } else {
                dough_parent.visibility = View.VISIBLE
                dough_text.text = foodKpiYesterdayDoData.actualFood.dough.displayName
            }
                   if (foodKpiYesterdayDoData.actualFood?.twentyOZ == null || foodKpiYesterdayDoData.actualFood.twentyOZ.displayName.isNullOrEmpty()) {
                       twenty_oz_parent.visibility = View.GONE
            } else {
                       twenty_oz_parent.visibility = View.VISIBLE
                       twenty_oz_text.text = foodKpiYesterdayDoData.actualFood.twentyOZ.displayName
            }
         if (foodKpiYesterdayDoData.actualFood?.top5 == null || foodKpiYesterdayDoData.actualFood.top5.displayName.isNullOrEmpty()) {
                top5_parent.visibility = View.GONE
            } else {
                top5_parent.visibility = View.VISIBLE
                top_five_text.text = foodKpiYesterdayDoData.actualFood.top5.displayName
            }
            if (foodKpiYesterdayDoData.actualFood?.allItems == null || foodKpiYesterdayDoData.actualFood.allItems.displayName.isNullOrEmpty()) {
                all_item_parent.visibility = View.GONE
            } else {
                all_item_parent.visibility = View.VISIBLE
                all_items_text.text = foodKpiYesterdayDoData.actualFood.allItems.displayName
            }
            if (foodKpiYesterdayDoData.endingInventory == null || foodKpiYesterdayDoData.endingInventory.displayName.isNullOrEmpty()) {
                ll_ending_inventory.visibility = View.GONE
            } else {
                ll_ending_inventory.visibility = View.VISIBLE
                ending_inventory_display.text = foodKpiYesterdayDoData.endingInventory.displayName
            }

            if (foodKpiYesterdayDoData.foodBought == null || foodKpiYesterdayDoData.foodBought.displayName.isNullOrEmpty()) {
                ll_food_bought_display_parent.visibility = View.GONE
            } else {
                ll_food_bought_display_parent.visibility = View.VISIBLE
                food_bought_display.text = foodKpiYesterdayDoData.foodBought.displayName
            }


            actual_food_display.text = foodKpiYesterdayDoData.actualFood?.total?.displayName
            ideal_food_display.text = foodKpiYesterdayDoData.idealFood?.displayName

            food_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (food_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.food_text)

                            level_two_scroll_data_action_value.text = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actual?.amount, foodKpiYesterdayDoData.actual?.percentage, foodKpiYesterdayDoData.actual?.value)

                            if (foodKpiYesterdayDoData.status != null) {
                                when {
                                    foodKpiYesterdayDoData.status.toString() == resources.getString(
                                        R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    foodKpiYesterdayDoData.status.toString() == resources.getString(
                                        R.string.under_limit) -> {
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
                        y = food_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            food_sales.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actual?.amount, foodKpiYesterdayDoData.actual?.percentage, foodKpiYesterdayDoData.actual?.value)

            if (foodKpiYesterdayDoData.status?.toString() != null) {
                when {
                    foodKpiYesterdayDoData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayDoData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            food_goal_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.goal?.amount, foodKpiYesterdayDoData.goal?.percentage, foodKpiYesterdayDoData.goal?.value)
            food_variance_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.variance?.amount, foodKpiYesterdayDoData.variance?.percentage, foodKpiYesterdayDoData.variance?.value)
            showFoodNarrativeData(foodKpiYesterdayDoData.narrative.toString())

            // ideal vs actual food
            if (foodKpiYesterdayDoData.idealvsActualFoodVariance == null || foodKpiYesterdayDoData.idealvsActualFoodVariance.displayName.isNullOrEmpty()) {
                ll_ideal_vs_actual_food_variance.visibility = View.GONE
            } else {
                ll_ideal_vs_actual_food_variance.visibility = View.VISIBLE
                ideal_vs_actual_food_variance_display_name.text = foodKpiYesterdayDoData.idealvsActualFoodVariance.displayName
            }
            val idealActualFoodGoalDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.idealvsActualFoodVariance?.goal?.amount, foodKpiYesterdayDoData.idealvsActualFoodVariance?.goal?.percentage, foodKpiYesterdayDoData.idealvsActualFoodVariance?.goal?.value)
            val idealActualFoodVarianceDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.idealvsActualFoodVariance?.variance?.amount, foodKpiYesterdayDoData.idealvsActualFoodVariance?.variance?.percentage, foodKpiYesterdayDoData.idealvsActualFoodVariance?.variance?.value)
            val idealActualFoodActualDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.idealvsActualFoodVariance?.actual?.amount, foodKpiYesterdayDoData.idealvsActualFoodVariance?.actual?.percentage, foodKpiYesterdayDoData.idealvsActualFoodVariance?.actual?.value)

            idealActualFoodData(idealActualFoodGoalDOYesterday,idealActualFoodVarianceDOYesterday,idealActualFoodActualDOYesterday)

            if (foodKpiYesterdayDoData.idealvsActualFoodVariance?.status != null) {
                when {
                    foodKpiYesterdayDoData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayDoData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // Ideal Food
            val idealFoodGoalDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.idealFood?.goal?.amount, foodKpiYesterdayDoData.idealFood?.goal?.percentage, foodKpiYesterdayDoData.idealFood?.goal?.value)
            val idealFoodVarianceDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.idealFood?.variance?.amount, foodKpiYesterdayDoData.idealFood?.variance?.percentage, foodKpiYesterdayDoData.idealFood?.variance?.value)
            val idealFoodActualDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.idealFood?.actual?.amount, foodKpiYesterdayDoData.idealFood?.actual?.percentage, foodKpiYesterdayDoData.idealFood?.actual?.value)
            idealFoodData(idealFoodGoalDOYesterday,idealFoodVarianceDOYesterday,idealFoodActualDOYesterday)

            if (foodKpiYesterdayDoData.idealFood?.status != null) {
                when {
                    foodKpiYesterdayDoData.idealFood.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayDoData.idealFood.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // actual food
            val actualFoodTotalDOYesterday =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.total?.actual?.amount, foodKpiYesterdayDoData.actualFood?.total?.actual?.percentage, foodKpiYesterdayDoData.actualFood?.total?.actual?.value)
            actualFoodTotalData(actualFoodTotalDOYesterday)

            if (foodKpiYesterdayDoData.actualFood?.total?.status != null) {
                when {
                    foodKpiYesterdayDoData.actualFood.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayDoData.actualFood.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.green))

                    } else -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // cheese
            val cheeseGoalDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.cheese?.goal?.amount, foodKpiYesterdayDoData.actualFood?.cheese?.goal?.percentage, foodKpiYesterdayDoData.actualFood?.cheese?.goal?.value)
            val cheeseVarianceDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.cheese?.variance?.amount, foodKpiYesterdayDoData.actualFood?.cheese?.variance?.percentage, foodKpiYesterdayDoData.actualFood?.cheese?.variance?.value)
            val cheeseActualDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.cheese?.actual?.amount, foodKpiYesterdayDoData.actualFood?.cheese?.actual?.percentage, foodKpiYesterdayDoData.actualFood?.cheese?.actual?.value)
            cheeseActualFoodData(cheeseGoalDOYesterday,cheeseVarianceDOYesterday,cheeseActualDOYesterday)

            // dough
            val doughGoalDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.dough?.goal?.amount, foodKpiYesterdayDoData.actualFood?.dough?.goal?.percentage, foodKpiYesterdayDoData.actualFood?.dough?.goal?.value)
            val doughVarianceDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.dough?.variance?.amount, foodKpiYesterdayDoData.actualFood?.dough?.variance?.percentage, foodKpiYesterdayDoData.actualFood?.dough?.variance?.value)
            val doughActualDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.dough?.actual?.amount, foodKpiYesterdayDoData.actualFood?.dough?.actual?.percentage, foodKpiYesterdayDoData.actualFood?.dough?.actual?.value)
            doughActualFoodData(doughGoalDOYesterday,doughVarianceDOYesterday,doughActualDOYesterday)


            // twenty
            val twentyOzGoalDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.twentyOZ?.goal?.amount, foodKpiYesterdayDoData.actualFood?.twentyOZ?.goal?.percentage, foodKpiYesterdayDoData.actualFood?.twentyOZ?.goal?.value)
            val twentyOzVarianceDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.twentyOZ?.variance?.amount, foodKpiYesterdayDoData.actualFood?.twentyOZ?.variance?.percentage, foodKpiYesterdayDoData.actualFood?.twentyOZ?.variance?.value)
            val twentyOzActualDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.twentyOZ?.actual?.amount, foodKpiYesterdayDoData.actualFood?.twentyOZ?.actual?.percentage, foodKpiYesterdayDoData.actualFood?.twentyOZ?.actual?.value)
            twentyOzActualFoodData(twentyOzGoalDOYesterday,twentyOzVarianceDOYesterday,twentyOzActualDOYesterday)

            // top_five
            val top5GoalDOYesterday= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.top5?.goal?.amount, foodKpiYesterdayDoData.actualFood?.top5?.goal?.percentage, foodKpiYesterdayDoData.actualFood?.top5?.goal?.value)
            val top5VarianceDOYesterday= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.top5?.variance?.amount, foodKpiYesterdayDoData.actualFood?.top5?.variance?.percentage, foodKpiYesterdayDoData.actualFood?.top5?.variance?.value)
            val top5ActualDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.top5?.actual?.amount, foodKpiYesterdayDoData.actualFood?.top5?.actual?.percentage, foodKpiYesterdayDoData.actualFood?.top5?.actual?.value)
            top5ActualFoodData(top5GoalDOYesterday,top5VarianceDOYesterday,top5ActualDOYesterday)

            
            // all_items
            val allItemsGoalDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.allItems?.goal?.amount, foodKpiYesterdayDoData.actualFood?.allItems?.goal?.percentage, foodKpiYesterdayDoData.actualFood?.allItems?.goal?.value)
            val allItemsVarianceDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.allItems?.variance?.amount, foodKpiYesterdayDoData.actualFood?.allItems?.variance?.percentage, foodKpiYesterdayDoData.actualFood?.allItems?.variance?.value)
            val allItemsActualDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.actualFood?.allItems?.actual?.amount, foodKpiYesterdayDoData.actualFood?.allItems?.actual?.percentage, foodKpiYesterdayDoData.actualFood?.allItems?.actual?.value)
            allItemsData(allItemsGoalDOYesterday,allItemsVarianceDOYesterday,allItemsActualDOYesterday)
            
            // ending even try
            val endingInventoryActualDOYesterday =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.endingInventory?.actual?.amount, foodKpiYesterdayDoData.endingInventory?.actual?.percentage, foodKpiYesterdayDoData.endingInventory?.actual?.value)
            endingInventoryData(endingInventoryActualDOYesterday)

            if (foodKpiYesterdayDoData.endingInventory?.status != null) {
                when {
                    foodKpiYesterdayDoData.endingInventory.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayDoData.endingInventory.status.toString() == resources.getString(R.string.under_limit) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // food bought
            val foodBoughtActualDOYesterday = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdayDoData.foodBought?.actual?.amount, foodKpiYesterdayDoData.foodBought?.actual?.percentage, foodKpiYesterdayDoData.foodBought?.actual?.value)
            foodBoughtData(foodBoughtActualDOYesterday)

            if (foodKpiYesterdayDoData.foodBought?.status != null) {
                when {
                    foodKpiYesterdayDoData.foodBought.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdayDoData.foodBought.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

        }catch (e:Exception){
            Logger.error(e.message.toString(),"Food Overview Yesterday KPI")
        }

    }

    private fun rangeOverViewDOFoodKpi(data: DOOverviewRangeQuery.Do_) {
        try {
            val foodKpiRangeOverViewDoData =  data.kpis?.supervisors?.stores?.period?.food!!

            Logger.info("Food Period Range", "Food Overview KPI")
            // scroll detect
            // ideal_vs_food_variance_text.text = foodKpiRangeOverViewDoData.displayName ?: getString(R.string.food_text)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewDoData.actualFood?.top5?.displayName, top5_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewDoData.actualFood?.allItems?.displayName, all_item_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewDoData.actualFood?.twentyOZ?.displayName, twenty_oz_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewDoData.actualFood?.dough?.displayName, dough_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewDoData.actualFood?.cheese?.displayName, cheese_parent)


             if (foodKpiRangeOverViewDoData.actualFood?.cheese == null || foodKpiRangeOverViewDoData.actualFood.cheese.displayName.isNullOrEmpty()) {
                cheese_parent.visibility = View.GONE
            } else {
                cheese_parent.visibility = View.VISIBLE
                cheese_text.text = foodKpiRangeOverViewDoData.actualFood.cheese.displayName
            }
            if (foodKpiRangeOverViewDoData.actualFood?.dough == null || foodKpiRangeOverViewDoData.actualFood.dough.displayName.isNullOrEmpty()) {
                dough_parent.visibility = View.GONE
            } else {
                dough_parent.visibility = View.VISIBLE
                dough_text.text = foodKpiRangeOverViewDoData.actualFood.dough.displayName
            }
                   if (foodKpiRangeOverViewDoData.actualFood?.twentyOZ == null || foodKpiRangeOverViewDoData.actualFood.twentyOZ.displayName.isNullOrEmpty()) {
                       twenty_oz_parent.visibility = View.GONE
            } else {
                       twenty_oz_parent.visibility = View.VISIBLE
                       twenty_oz_text.text = foodKpiRangeOverViewDoData.actualFood.twentyOZ.displayName
            }
         if (foodKpiRangeOverViewDoData.actualFood?.top5 == null || foodKpiRangeOverViewDoData.actualFood.top5.displayName.isNullOrEmpty()) {
                top5_parent.visibility = View.GONE
            } else {
                top5_parent.visibility = View.VISIBLE
                top_five_text.text = foodKpiRangeOverViewDoData.actualFood.top5.displayName
            }
            if (foodKpiRangeOverViewDoData.actualFood?.allItems == null || foodKpiRangeOverViewDoData.actualFood.allItems.displayName.isNullOrEmpty()) {
                all_item_parent.visibility = View.GONE
            } else {
                all_item_parent.visibility = View.VISIBLE
                all_items_text.text = foodKpiRangeOverViewDoData.actualFood.allItems.displayName
            }
            if (foodKpiRangeOverViewDoData.endingInventory == null || foodKpiRangeOverViewDoData.endingInventory.displayName.isNullOrEmpty()) {
                ll_ending_inventory.visibility = View.GONE
            } else {
                ll_ending_inventory.visibility = View.VISIBLE
                ending_inventory_display.text = foodKpiRangeOverViewDoData.endingInventory.displayName
            }

            if (foodKpiRangeOverViewDoData.foodBought == null || foodKpiRangeOverViewDoData.foodBought.displayName.isNullOrEmpty()) {
                ll_food_bought_display_parent.visibility = View.GONE
            } else {
                ll_food_bought_display_parent.visibility = View.VISIBLE
                food_bought_display.text = foodKpiRangeOverViewDoData.foodBought.displayName
            }


            actual_food_display.text = foodKpiRangeOverViewDoData.actualFood?.total?.displayName
            ideal_food_display.text = foodKpiRangeOverViewDoData.idealFood?.displayName

            food_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (food_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            // level_two_scroll_data_action.text = foodKpiRangeOverViewDoData.displayName ?: getString(R.string.food_text)
                            level_two_scroll_data_action.text = getString(R.string.food_text)

                            level_two_scroll_data_action_value.text = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actual?.amount, foodKpiRangeOverViewDoData.actual?.percentage, foodKpiRangeOverViewDoData.actual?.value)

                            if (foodKpiRangeOverViewDoData.status != null) {
                                when {
                                    foodKpiRangeOverViewDoData.status.toString() == resources.getString(
                                        R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    foodKpiRangeOverViewDoData.status.toString() == resources.getString(
                                        R.string.under_limit) -> {
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
                        y = food_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            food_sales.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actual?.amount, foodKpiRangeOverViewDoData.actual?.percentage, foodKpiRangeOverViewDoData.actual?.value)

            if (foodKpiRangeOverViewDoData.status?.toString() != null) {
                when {
                    foodKpiRangeOverViewDoData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewDoData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            food_goal_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.goal?.amount, foodKpiRangeOverViewDoData.goal?.percentage, foodKpiRangeOverViewDoData.goal?.value)
            food_variance_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.variance?.amount, foodKpiRangeOverViewDoData.variance?.percentage, foodKpiRangeOverViewDoData.variance?.value)
            showFoodNarrativeData(foodKpiRangeOverViewDoData.narrative.toString())

            // ideal vs Actual food
            if (foodKpiRangeOverViewDoData.idealvsActualFoodVariance == null || foodKpiRangeOverViewDoData.idealvsActualFoodVariance.displayName.isNullOrEmpty()) {
                ll_ideal_vs_actual_food_variance.visibility = View.GONE
            } else {
                ll_ideal_vs_actual_food_variance.visibility = View.VISIBLE
                ideal_vs_actual_food_variance_display_name.text = foodKpiRangeOverViewDoData.idealvsActualFoodVariance.displayName
            }

            val idealActualFoodGoalDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.idealvsActualFoodVariance?.goal?.amount, foodKpiRangeOverViewDoData.idealvsActualFoodVariance?.goal?.percentage, foodKpiRangeOverViewDoData.idealvsActualFoodVariance?.goal?.value)
            val idealActualFoodVarianceDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.idealvsActualFoodVariance?.variance?.amount, foodKpiRangeOverViewDoData.idealvsActualFoodVariance?.variance?.percentage, foodKpiRangeOverViewDoData.idealvsActualFoodVariance?.variance?.value)
            val idealActualFoodActualDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.idealvsActualFoodVariance?.actual?.amount, foodKpiRangeOverViewDoData.idealvsActualFoodVariance?.actual?.percentage, foodKpiRangeOverViewDoData.idealvsActualFoodVariance?.actual?.value)
            idealActualFoodData(idealActualFoodGoalDORange,idealActualFoodVarianceDORange,idealActualFoodActualDORange)

            if (foodKpiRangeOverViewDoData.idealvsActualFoodVariance?.status != null) {
                when {
                    foodKpiRangeOverViewDoData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewDoData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val idealFoodGoalDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.idealFood?.goal?.amount, foodKpiRangeOverViewDoData.idealFood?.goal?.percentage, foodKpiRangeOverViewDoData.idealFood?.goal?.value)
            val idealFoodVarianceDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.idealFood?.variance?.amount, foodKpiRangeOverViewDoData.idealFood?.variance?.percentage, foodKpiRangeOverViewDoData.idealFood?.variance?.value)
            val idealFoodActualDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.idealFood?.actual?.amount, foodKpiRangeOverViewDoData.idealFood?.actual?.percentage, foodKpiRangeOverViewDoData.idealFood?.actual?.value)
            idealFoodData(idealFoodGoalDORange,idealFoodVarianceDORange,idealFoodActualDORange)

            if (foodKpiRangeOverViewDoData.idealFood?.status != null) {
                when {
                    foodKpiRangeOverViewDoData.idealFood.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewDoData.idealFood.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // actual food
            val actualFoodTotalDORange=  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.total?.actual?.amount, foodKpiRangeOverViewDoData.actualFood?.total?.actual?.percentage, foodKpiRangeOverViewDoData.actualFood?.total?.actual?.value)
            actualFoodTotalData(actualFoodTotalDORange)
            if (foodKpiRangeOverViewDoData.actualFood?.total?.status != null) {
                when {
                    foodKpiRangeOverViewDoData.actualFood.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewDoData.actualFood.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.green))

                    } else -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // cheese
            val cheeseGoalDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.cheese?.goal?.amount, foodKpiRangeOverViewDoData.actualFood?.cheese?.goal?.percentage, foodKpiRangeOverViewDoData.actualFood?.cheese?.goal?.value)
            val cheeseVarianceDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.cheese?.variance?.amount, foodKpiRangeOverViewDoData.actualFood?.cheese?.variance?.percentage, foodKpiRangeOverViewDoData.actualFood?.cheese?.variance?.value)
            val cheeseActualDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.cheese?.actual?.amount, foodKpiRangeOverViewDoData.actualFood?.cheese?.actual?.percentage, foodKpiRangeOverViewDoData.actualFood?.cheese?.actual?.value)
            cheeseActualFoodData(cheeseGoalDORange,cheeseVarianceDORange,cheeseActualDORange)

            // dough
            val doughGoalDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.dough?.goal?.amount, foodKpiRangeOverViewDoData.actualFood?.dough?.goal?.percentage, foodKpiRangeOverViewDoData.actualFood?.dough?.goal?.value)
            val doughVarianceDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.dough?.variance?.amount, foodKpiRangeOverViewDoData.actualFood?.dough?.variance?.percentage, foodKpiRangeOverViewDoData.actualFood?.dough?.variance?.value)
            val doughActualDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.dough?.actual?.amount, foodKpiRangeOverViewDoData.actualFood?.dough?.actual?.percentage, foodKpiRangeOverViewDoData.actualFood?.dough?.actual?.value)
            doughActualFoodData(doughGoalDORange,doughVarianceDORange,doughActualDORange)


            // twenty
            val twentyOzGoalDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.twentyOZ?.goal?.amount, foodKpiRangeOverViewDoData.actualFood?.twentyOZ?.goal?.percentage, foodKpiRangeOverViewDoData.actualFood?.twentyOZ?.goal?.value)
            val twentyOzVarianceDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.twentyOZ?.variance?.amount, foodKpiRangeOverViewDoData.actualFood?.twentyOZ?.variance?.percentage, foodKpiRangeOverViewDoData.actualFood?.twentyOZ?.variance?.value)
            val twentyOzActualDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.twentyOZ?.actual?.amount, foodKpiRangeOverViewDoData.actualFood?.twentyOZ?.actual?.percentage, foodKpiRangeOverViewDoData.actualFood?.twentyOZ?.actual?.value)
            twentyOzActualFoodData(twentyOzGoalDORange,twentyOzVarianceDORange,twentyOzActualDORange)


            // top_five
            val top5GoalDORange= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.top5?.goal?.amount, foodKpiRangeOverViewDoData.actualFood?.top5?.goal?.percentage, foodKpiRangeOverViewDoData.actualFood?.top5?.goal?.value)
            val top5VarianceDORange= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.top5?.variance?.amount, foodKpiRangeOverViewDoData.actualFood?.top5?.variance?.percentage, foodKpiRangeOverViewDoData.actualFood?.top5?.variance?.value)
            val top5ActualDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.top5?.actual?.amount, foodKpiRangeOverViewDoData.actualFood?.top5?.actual?.percentage, foodKpiRangeOverViewDoData.actualFood?.top5?.actual?.value)
            top5ActualFoodData(top5GoalDORange,top5VarianceDORange,top5ActualDORange)

            // all_items
            val allItemsGoalDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.allItems?.goal?.amount, foodKpiRangeOverViewDoData.actualFood?.allItems?.goal?.percentage, foodKpiRangeOverViewDoData.actualFood?.allItems?.goal?.value)
            val allItemsVarianceDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.allItems?.variance?.amount, foodKpiRangeOverViewDoData.actualFood?.allItems?.variance?.percentage, foodKpiRangeOverViewDoData.actualFood?.allItems?.variance?.value)
            val allItemsActualDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.actualFood?.allItems?.actual?.amount, foodKpiRangeOverViewDoData.actualFood?.allItems?.actual?.percentage, foodKpiRangeOverViewDoData.actualFood?.allItems?.actual?.value)
            allItemsData(allItemsGoalDORange,allItemsVarianceDORange,allItemsActualDORange)

            // ending even try
            val endingInventoryActualDORange =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.endingInventory?.actual?.amount, foodKpiRangeOverViewDoData.endingInventory?.actual?.percentage, foodKpiRangeOverViewDoData.endingInventory?.actual?.value)
            endingInventoryData(endingInventoryActualDORange)

            if (foodKpiRangeOverViewDoData.endingInventory?.status != null) {
                when {
                    foodKpiRangeOverViewDoData.endingInventory.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewDoData.endingInventory.status.toString() == resources.getString(R.string.under_limit) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // food bought
            val foodBoughtActualDORange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewDoData.foodBought?.actual?.amount, foodKpiRangeOverViewDoData.foodBought?.actual?.percentage, foodKpiRangeOverViewDoData.foodBought?.actual?.value)
            foodBoughtData(foodBoughtActualDORange)

            if (foodKpiRangeOverViewDoData.foodBought?.status != null) {
                when {
                    foodKpiRangeOverViewDoData.foodBought.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewDoData.foodBought.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
        }catch (e:Exception){
            Logger.error(e.message.toString(),"Food Overview Period Range KPI")
        }

    }


    // supervisor overview
    private fun yesterdayViewSupervisorFoodKpi(data: SupervisorOverviewYesterdayQuery.Supervisor) {
        try {
            val foodKpiYesterdaySupervisorData =  data.kpis?.stores?.yesterday?.food!!

            Logger.info("Food Yesterday", "Food Overview KPI")

            // scroll detect
            // ideal_vs_food_variance_text.text = foodKpiYesterdaySupervisorData.displayName ?: getString(R.string.food_text)
            Validation().checkNullValueToShowView(this, foodKpiYesterdaySupervisorData.actualFood?.top5?.displayName, top5_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdaySupervisorData.actualFood?.allItems?.displayName, all_item_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdaySupervisorData.actualFood?.twentyOZ?.displayName, twenty_oz_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdaySupervisorData.actualFood?.dough?.displayName, dough_parent)
            Validation().checkNullValueToShowView(this, foodKpiYesterdaySupervisorData.actualFood?.cheese?.displayName, cheese_parent)

             if (foodKpiYesterdaySupervisorData.actualFood?.cheese == null || foodKpiYesterdaySupervisorData.actualFood.cheese.displayName.isNullOrEmpty()) {
                cheese_parent.visibility = View.GONE
            } else {
                cheese_parent.visibility = View.VISIBLE
                cheese_text.text = foodKpiYesterdaySupervisorData.actualFood.cheese.displayName
            }
            if (foodKpiYesterdaySupervisorData.actualFood?.dough == null || foodKpiYesterdaySupervisorData.actualFood.dough.displayName.isNullOrEmpty()) {
                dough_parent.visibility = View.GONE
            } else {
                dough_parent.visibility = View.VISIBLE
                dough_text.text = foodKpiYesterdaySupervisorData.actualFood.dough.displayName
            }
                   if (foodKpiYesterdaySupervisorData.actualFood?.twentyOZ == null || foodKpiYesterdaySupervisorData.actualFood.twentyOZ.displayName.isNullOrEmpty()) {
                       twenty_oz_parent.visibility = View.GONE
            } else {
                       twenty_oz_parent.visibility = View.VISIBLE
                       twenty_oz_text.text = foodKpiYesterdaySupervisorData.actualFood.twentyOZ.displayName
            }
         if (foodKpiYesterdaySupervisorData.actualFood?.top5 == null || foodKpiYesterdaySupervisorData.actualFood.top5.displayName.isNullOrEmpty()) {
                top5_parent.visibility = View.GONE
            } else {
                top5_parent.visibility = View.VISIBLE
                top_five_text.text = foodKpiYesterdaySupervisorData.actualFood.top5.displayName
            }
            if (foodKpiYesterdaySupervisorData.actualFood?.allItems == null || foodKpiYesterdaySupervisorData.actualFood.allItems.displayName.isNullOrEmpty()) {
                all_item_parent.visibility = View.GONE
            } else {
                all_item_parent.visibility = View.VISIBLE
                all_items_text.text = foodKpiYesterdaySupervisorData.actualFood.allItems.displayName
            }
            if (foodKpiYesterdaySupervisorData.endingInventory == null || foodKpiYesterdaySupervisorData.endingInventory.displayName.isNullOrEmpty()) {
                ll_ending_inventory.visibility = View.GONE
            } else {
                ll_ending_inventory.visibility = View.VISIBLE
                ending_inventory_display.text = foodKpiYesterdaySupervisorData.endingInventory.displayName
            }

            if (foodKpiYesterdaySupervisorData.foodBought == null || foodKpiYesterdaySupervisorData.foodBought.displayName.isNullOrEmpty()) {
                ll_food_bought_display_parent.visibility = View.GONE
            } else {
                ll_food_bought_display_parent.visibility = View.VISIBLE
                food_bought_display.text = foodKpiYesterdaySupervisorData.foodBought.displayName
            }



            actual_food_display.text = foodKpiYesterdaySupervisorData.actualFood?.total?.displayName
            ideal_food_display.text = foodKpiYesterdaySupervisorData.idealFood?.displayName

            food_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (food_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            // level_two_scroll_data_action.text = foodKpiYesterdaySupervisorData.displayName ?: getString(R.string.food_text)
                            level_two_scroll_data_action.text = getString(R.string.food_text)

                            level_two_scroll_data_action_value.text = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actual?.amount, foodKpiYesterdaySupervisorData.actual?.percentage, foodKpiYesterdaySupervisorData.actual?.value)

                            if (foodKpiYesterdaySupervisorData.status != null) {
                                when {
                                    foodKpiYesterdaySupervisorData.status.toString() == resources.getString(
                                        R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    foodKpiYesterdaySupervisorData.status.toString() == resources.getString(
                                        R.string.under_limit) -> {
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
                        y = food_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            food_sales.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actual?.amount, foodKpiYesterdaySupervisorData.actual?.percentage, foodKpiYesterdaySupervisorData.actual?.value)

            if (foodKpiYesterdaySupervisorData.status?.toString() != null) {
                when {
                    foodKpiYesterdaySupervisorData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdaySupervisorData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            food_goal_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.goal?.amount, foodKpiYesterdaySupervisorData.goal?.percentage, foodKpiYesterdaySupervisorData.goal?.value)
            food_variance_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.variance?.amount, foodKpiYesterdaySupervisorData.variance?.percentage, foodKpiYesterdaySupervisorData.variance?.value)
            showFoodNarrativeData(foodKpiYesterdaySupervisorData.narrative.toString())


            // ideal vs actual food
            if (foodKpiYesterdaySupervisorData.idealvsActualFoodVariance == null || foodKpiYesterdaySupervisorData.idealvsActualFoodVariance.displayName.isNullOrEmpty()) {
                ll_ideal_vs_actual_food_variance.visibility = View.GONE
            } else {
                ll_ideal_vs_actual_food_variance.visibility = View.VISIBLE
                ideal_vs_actual_food_variance_display_name.text = foodKpiYesterdaySupervisorData.idealvsActualFoodVariance.displayName
            }
            val idealActualFoodGoalYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.idealvsActualFoodVariance?.goal?.amount, foodKpiYesterdaySupervisorData.idealvsActualFoodVariance?.goal?.percentage, foodKpiYesterdaySupervisorData.idealvsActualFoodVariance?.goal?.value)
            val idealActualFoodVarianceYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.idealvsActualFoodVariance?.variance?.amount, foodKpiYesterdaySupervisorData.idealvsActualFoodVariance?.variance?.percentage, foodKpiYesterdaySupervisorData.idealvsActualFoodVariance?.variance?.value)
            val idealActualFoodActualYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.idealvsActualFoodVariance?.actual?.amount, foodKpiYesterdaySupervisorData.idealvsActualFoodVariance?.actual?.percentage, foodKpiYesterdaySupervisorData.idealvsActualFoodVariance?.actual?.value)
           idealActualFoodData(idealActualFoodGoalYesterdaySupervisor,idealActualFoodVarianceYesterdaySupervisor,idealActualFoodActualYesterdaySupervisor)

            if (foodKpiYesterdaySupervisorData.idealvsActualFoodVariance?.status != null) {
                when {
                    foodKpiYesterdaySupervisorData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdaySupervisorData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // Ideal Food
            val idealFoodGoalYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.idealFood?.goal?.amount, foodKpiYesterdaySupervisorData.idealFood?.goal?.percentage, foodKpiYesterdaySupervisorData.idealFood?.goal?.value)
            val idealFoodVarianceYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.idealFood?.variance?.amount, foodKpiYesterdaySupervisorData.idealFood?.variance?.percentage, foodKpiYesterdaySupervisorData.idealFood?.variance?.value)
            val idealFoodActualYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.idealFood?.actual?.amount, foodKpiYesterdaySupervisorData.idealFood?.actual?.percentage, foodKpiYesterdaySupervisorData.idealFood?.actual?.value)
            idealFoodData(idealFoodGoalYesterdaySupervisor,idealFoodVarianceYesterdaySupervisor,idealFoodActualYesterdaySupervisor)
            
            if (foodKpiYesterdaySupervisorData.idealFood?.status != null) {
                when {
                    foodKpiYesterdaySupervisorData.idealFood.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdaySupervisorData.idealFood.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // actual food
            val actualFoodTotalSupervisorYesterday =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.total?.actual?.amount, foodKpiYesterdaySupervisorData.actualFood?.total?.actual?.percentage, foodKpiYesterdaySupervisorData.actualFood?.total?.actual?.value)
            actualFoodTotalData(actualFoodTotalSupervisorYesterday)
            if (foodKpiYesterdaySupervisorData.actualFood?.total?.status != null) {
                when {
                    foodKpiYesterdaySupervisorData.actualFood.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdaySupervisorData.actualFood.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.green))

                    } else -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.text_color))

                    }
                }
            }


            // cheese
            val cheeseGoalYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.cheese?.goal?.amount, foodKpiYesterdaySupervisorData.actualFood?.cheese?.goal?.percentage, foodKpiYesterdaySupervisorData.actualFood?.cheese?.goal?.value)
            val cheeseVarianceYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.cheese?.variance?.amount, foodKpiYesterdaySupervisorData.actualFood?.cheese?.variance?.percentage, foodKpiYesterdaySupervisorData.actualFood?.cheese?.variance?.value)
            val cheeseActualYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.cheese?.actual?.amount, foodKpiYesterdaySupervisorData.actualFood?.cheese?.actual?.percentage, foodKpiYesterdaySupervisorData.actualFood?.cheese?.actual?.value)
            cheeseActualFoodData(cheeseGoalYesterdaySupervisor,cheeseVarianceYesterdaySupervisor,cheeseActualYesterdaySupervisor)

            // dough
            val doughGoalYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.dough?.goal?.amount, foodKpiYesterdaySupervisorData.actualFood?.dough?.goal?.percentage, foodKpiYesterdaySupervisorData.actualFood?.dough?.goal?.value)
            val doughVarianceYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.dough?.variance?.amount, foodKpiYesterdaySupervisorData.actualFood?.dough?.variance?.percentage, foodKpiYesterdaySupervisorData.actualFood?.dough?.variance?.value)
            val doughActualYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.dough?.actual?.amount, foodKpiYesterdaySupervisorData.actualFood?.dough?.actual?.percentage, foodKpiYesterdaySupervisorData.actualFood?.dough?.actual?.value)
            doughActualFoodData(doughGoalYesterdaySupervisor,doughVarianceYesterdaySupervisor,doughActualYesterdaySupervisor)


            // twenty
            val twentyOzGoalYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.twentyOZ?.goal?.amount, foodKpiYesterdaySupervisorData.actualFood?.twentyOZ?.goal?.percentage, foodKpiYesterdaySupervisorData.actualFood?.twentyOZ?.goal?.value)
            val twentyOzVarianceYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.twentyOZ?.variance?.amount, foodKpiYesterdaySupervisorData.actualFood?.twentyOZ?.variance?.percentage, foodKpiYesterdaySupervisorData.actualFood?.twentyOZ?.variance?.value)
            val twentyOzActualYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.twentyOZ?.actual?.amount, foodKpiYesterdaySupervisorData.actualFood?.twentyOZ?.actual?.percentage, foodKpiYesterdaySupervisorData.actualFood?.twentyOZ?.actual?.value)
            twentyOzActualFoodData(twentyOzGoalYesterdaySupervisor,twentyOzVarianceYesterdaySupervisor,twentyOzActualYesterdaySupervisor)

            // top_five
            val top5GoalYesterdaySupervisor= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.top5?.goal?.amount, foodKpiYesterdaySupervisorData.actualFood?.top5?.goal?.percentage, foodKpiYesterdaySupervisorData.actualFood?.top5?.goal?.value)
            val top5VarianceYesterdaySupervisor= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.top5?.variance?.amount, foodKpiYesterdaySupervisorData.actualFood?.top5?.variance?.percentage, foodKpiYesterdaySupervisorData.actualFood?.top5?.variance?.value)
            val top5ActualYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.top5?.actual?.amount, foodKpiYesterdaySupervisorData.actualFood?.top5?.actual?.percentage, foodKpiYesterdaySupervisorData.actualFood?.top5?.actual?.value)
            top5ActualFoodData(top5GoalYesterdaySupervisor,top5VarianceYesterdaySupervisor,top5ActualYesterdaySupervisor)



            // all_items
            val allItemsGoalYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.allItems?.goal?.amount, foodKpiYesterdaySupervisorData.actualFood?.allItems?.goal?.percentage, foodKpiYesterdaySupervisorData.actualFood?.allItems?.goal?.value)
            val allItemsVarianceYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.allItems?.variance?.amount, foodKpiYesterdaySupervisorData.actualFood?.allItems?.variance?.percentage, foodKpiYesterdaySupervisorData.actualFood?.allItems?.variance?.value)
            val allItemsActualYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.actualFood?.allItems?.actual?.amount, foodKpiYesterdaySupervisorData.actualFood?.allItems?.actual?.percentage, foodKpiYesterdaySupervisorData.actualFood?.allItems?.actual?.value)
            allItemsData(allItemsGoalYesterdaySupervisor,allItemsVarianceYesterdaySupervisor,allItemsActualYesterdaySupervisor)

            // ending even try
            val endingInventoryActualYesterdaySupervisor =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.endingInventory?.actual?.amount, foodKpiYesterdaySupervisorData.endingInventory?.actual?.percentage, foodKpiYesterdaySupervisorData.endingInventory?.actual?.value)
            endingInventoryData(endingInventoryActualYesterdaySupervisor)
            
            if (foodKpiYesterdaySupervisorData.endingInventory?.status != null) {
                when {
                    foodKpiYesterdaySupervisorData.endingInventory.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdaySupervisorData.endingInventory.status.toString() == resources.getString(R.string.under_limit) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
          
            // food bought
            val foodBoughtActualYesterdaySupervisor = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiYesterdaySupervisorData.foodBought?.actual?.amount, foodKpiYesterdaySupervisorData.foodBought?.actual?.percentage, foodKpiYesterdaySupervisorData.foodBought?.actual?.value)

            foodBoughtData(foodBoughtActualYesterdaySupervisor)

            if (foodKpiYesterdaySupervisorData.foodBought?.status != null) {
                when {
                    foodKpiYesterdaySupervisorData.foodBought.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiYesterdaySupervisorData.foodBought.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
        }catch (e:Exception){
            Logger.error(e.message.toString(),"Food Overview Yesterday KPI")
        }

    }

    private fun rangeOverViewSupervisorFoodKpi(data: SupervisorOverviewRangeQuery.Supervisor) {
        try {
            val foodKpiRangeOverViewSupervisorData =  data.kpis?.stores?.period?.food!!

            Logger.info("Food Period Range", "Food Overview KPI")

            // scroll detect
            // ideal_vs_food_variance_text.text = foodKpiRangeOverViewSupervisorData.displayName ?: getString(R.string.food_text)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewSupervisorData.actualFood?.top5?.displayName, top5_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.displayName, all_item_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewSupervisorData.actualFood?.twentyOZ?.displayName, twenty_oz_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewSupervisorData.actualFood?.dough?.displayName, dough_parent)
            Validation().checkNullValueToShowView(this, foodKpiRangeOverViewSupervisorData.actualFood?.cheese?.displayName, cheese_parent)

             if (foodKpiRangeOverViewSupervisorData.actualFood?.cheese == null || foodKpiRangeOverViewSupervisorData.actualFood.cheese.displayName.isNullOrEmpty()) {
                cheese_parent.visibility = View.GONE
            } else {
                cheese_parent.visibility = View.VISIBLE
                cheese_text.text = foodKpiRangeOverViewSupervisorData.actualFood.cheese.displayName
            }
            if (foodKpiRangeOverViewSupervisorData.actualFood?.dough == null || foodKpiRangeOverViewSupervisorData.actualFood.dough.displayName.isNullOrEmpty()) {
                dough_parent.visibility = View.GONE
            } else {
                dough_parent.visibility = View.VISIBLE
                dough_text.text = foodKpiRangeOverViewSupervisorData.actualFood.dough.displayName
            }
                   if (foodKpiRangeOverViewSupervisorData.actualFood?.twentyOZ == null || foodKpiRangeOverViewSupervisorData.actualFood.twentyOZ.displayName.isNullOrEmpty()) {
                       twenty_oz_parent.visibility = View.GONE
            } else {
                       twenty_oz_parent.visibility = View.VISIBLE
                       twenty_oz_text.text = foodKpiRangeOverViewSupervisorData.actualFood.twentyOZ.displayName
            }
         if (foodKpiRangeOverViewSupervisorData.actualFood?.top5 == null || foodKpiRangeOverViewSupervisorData.actualFood.top5.displayName.isNullOrEmpty()) {
                top5_parent.visibility = View.GONE
            } else {
                top5_parent.visibility = View.VISIBLE
                top_five_text.text = foodKpiRangeOverViewSupervisorData.actualFood.top5.displayName
            }
            if (foodKpiRangeOverViewSupervisorData.actualFood?.allItems == null || foodKpiRangeOverViewSupervisorData.actualFood.allItems.displayName.isNullOrEmpty()) {
                all_item_parent.visibility = View.GONE
            } else {
                all_item_parent.visibility = View.VISIBLE
                all_items_text.text = foodKpiRangeOverViewSupervisorData.actualFood.allItems.displayName
            }
            if (foodKpiRangeOverViewSupervisorData.endingInventory == null || foodKpiRangeOverViewSupervisorData.endingInventory.displayName.isNullOrEmpty()) {
                ll_ending_inventory.visibility = View.GONE
            } else {
                ll_ending_inventory.visibility = View.VISIBLE
                ending_inventory_display.text = foodKpiRangeOverViewSupervisorData.endingInventory.displayName
            }

            if (foodKpiRangeOverViewSupervisorData.foodBought == null || foodKpiRangeOverViewSupervisorData.foodBought.displayName.isNullOrEmpty()) {
                ll_food_bought_display_parent.visibility = View.GONE
            } else {
                ll_food_bought_display_parent.visibility = View.VISIBLE
                food_bought_display.text = foodKpiRangeOverViewSupervisorData.foodBought.displayName
            }



            ideal_food_display.text = foodKpiRangeOverViewSupervisorData.idealFood?.displayName

            food_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (food_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            // level_two_scroll_data_action.text = foodKpiRangeOverViewSupervisorData.displayName ?: getString(R.string.food_text)
                            level_two_scroll_data_action.text = getString(R.string.food_text)

                            level_two_scroll_data_action_value.text = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actual?.amount, foodKpiRangeOverViewSupervisorData.actual?.percentage, foodKpiRangeOverViewSupervisorData.actual?.value)

                            if (foodKpiRangeOverViewSupervisorData.status != null) {
                                when {
                                    foodKpiRangeOverViewSupervisorData.status.toString() == resources.getString(
                                        R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    foodKpiRangeOverViewSupervisorData.status.toString() == resources.getString(
                                        R.string.under_limit) -> {
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
                        y = food_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE

                        }
                    }
                })

            food_sales.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actual?.amount, foodKpiRangeOverViewSupervisorData.actual?.percentage, foodKpiRangeOverViewSupervisorData.actual?.value)

            if (foodKpiRangeOverViewSupervisorData.status?.toString() != null) {
                when {
                    foodKpiRangeOverViewSupervisorData.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_sales.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewSupervisorData.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_sales.setTextColor(getColor(R.color.green))
                    } else -> {
                        food_sales.setTextColor(getColor(R.color.text_color))
                    }
                }
            }
            food_goal_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.goal?.amount, foodKpiRangeOverViewSupervisorData.goal?.percentage, foodKpiRangeOverViewSupervisorData.goal?.value)
            food_variance_value.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.variance?.amount, foodKpiRangeOverViewSupervisorData.variance?.percentage, foodKpiRangeOverViewSupervisorData.variance?.value)

            showFoodNarrativeData(foodKpiRangeOverViewSupervisorData.narrative.toString())

            // ideal vs Actual food
            if (foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance == null || foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance.displayName.isNullOrEmpty()) {
                ll_ideal_vs_actual_food_variance.visibility = View.GONE
            } else {
                ll_ideal_vs_actual_food_variance.visibility = View.VISIBLE
                ideal_vs_actual_food_variance_display_name.text = foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance.displayName
            }

            val idealActualFoodGoalSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance?.goal?.amount, foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance?.goal?.percentage, foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance?.goal?.value)
            val idealActualFoodVarianceSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance?.variance?.amount, foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance?.variance?.percentage, foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance?.variance?.value)
            val idealActualFoodActualSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance?.actual?.amount, foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance?.actual?.percentage, foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance?.actual?.value)

           idealActualFoodData(idealActualFoodGoalSupervisorRange,idealActualFoodVarianceSupervisorRange,idealActualFoodActualSupervisorRange)

            if (foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance?.status != null) {
                when {
                    foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewSupervisorData.idealvsActualFoodVariance.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_vs_food_dollar_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            val idealFoodGoalSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.idealFood?.goal?.amount, foodKpiRangeOverViewSupervisorData.idealFood?.goal?.percentage, foodKpiRangeOverViewSupervisorData.idealFood?.goal?.value)
            val idealFoodVarianceSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.idealFood?.variance?.amount, foodKpiRangeOverViewSupervisorData.idealFood?.variance?.percentage, foodKpiRangeOverViewSupervisorData.idealFood?.variance?.value)
            val idealFoodActualSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.idealFood?.actual?.amount, foodKpiRangeOverViewSupervisorData.idealFood?.actual?.percentage, foodKpiRangeOverViewSupervisorData.idealFood?.actual?.value)
            idealFoodData(idealFoodGoalSupervisorRange,idealFoodVarianceSupervisorRange,idealFoodActualSupervisorRange)

            if (foodKpiRangeOverViewSupervisorData.idealFood?.status != null) {
                when {
                    foodKpiRangeOverViewSupervisorData.idealFood.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewSupervisorData.idealFood.status.toString() == resources.getString(R.string.under_limit) -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ideal_food_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ideal_food_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // actual food
            val actualFoodTotalSupervisorRange =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.total?.actual?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.total?.actual?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.total?.actual?.value)
            actualFoodTotalData(actualFoodTotalSupervisorRange)
            if (foodKpiRangeOverViewSupervisorData.actualFood?.total?.status != null) {
                when {
                    foodKpiRangeOverViewSupervisorData.actualFood.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewSupervisorData.actualFood.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.green))

                    } else -> {
                        actual_food_total.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        actual_food_total.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            // cheese
            val cheeseGoalSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.cheese?.goal?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.cheese?.goal?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.cheese?.goal?.value)
            val cheeseVarianceSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.cheese?.variance?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.cheese?.variance?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.cheese?.variance?.value)
            val cheeseActualSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.cheese?.actual?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.cheese?.actual?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.cheese?.actual?.value)
            cheeseActualFoodData(cheeseGoalSupervisorRange,cheeseVarianceSupervisorRange,cheeseActualSupervisorRange)

            // dough
            val doughGoalSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.dough?.goal?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.dough?.goal?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.dough?.goal?.value)
            val doughVarianceSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.dough?.variance?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.dough?.variance?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.dough?.variance?.value)
            val doughActualSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.dough?.actual?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.dough?.actual?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.dough?.actual?.value)
            doughActualFoodData(doughGoalSupervisorRange,doughVarianceSupervisorRange,doughActualSupervisorRange)


            // twenty
            val twentyOzGoalSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.twentyOZ?.goal?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.twentyOZ?.goal?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.twentyOZ?.goal?.value)
            val twentyOzVarianceSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.twentyOZ?.variance?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.twentyOZ?.variance?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.twentyOZ?.variance?.value)
            val twentyOzActualSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.twentyOZ?.actual?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.twentyOZ?.actual?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.twentyOZ?.actual?.value)
            twentyOzActualFoodData(twentyOzGoalSupervisorRange,twentyOzVarianceSupervisorRange,twentyOzActualSupervisorRange)


            // top_five
            val top5GoalSupervisorRange= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.top5?.goal?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.top5?.goal?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.top5?.goal?.value)
            val top5VarianceSupervisorRange= Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.top5?.variance?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.top5?.variance?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.top5?.variance?.value)
            val top5ActualSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.top5?.actual?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.top5?.actual?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.top5?.actual?.value)
            top5ActualFoodData(top5GoalSupervisorRange,top5VarianceSupervisorRange,top5ActualSupervisorRange)

            // all_items
            all_items_goal.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.goal?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.goal?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.goal?.value)
            all_items_variance.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.variance?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.variance?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.variance?.value)
            all_items_actual.text =  Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.actual?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.actual?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.actual?.value)
            val allItemsGoalSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.goal?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.goal?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.goal?.value)
            val allItemsVarianceSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.variance?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.variance?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.variance?.value)
            val allItemsActualSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.actual?.amount, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.actual?.percentage, foodKpiRangeOverViewSupervisorData.actualFood?.allItems?.actual?.value)
            allItemsData(allItemsGoalSupervisorRange,allItemsVarianceSupervisorRange,allItemsActualSupervisorRange)
            
            // ending even try
            val endingInventoryActualSupervisorRange =  Validation().checkAmountPercentageValue(this@FoodKpiActivity,
                                                                                                foodKpiRangeOverViewSupervisorData.endingInventory?.actual?.amount, foodKpiRangeOverViewSupervisorData.endingInventory?.actual?.percentage, foodKpiRangeOverViewSupervisorData.endingInventory?.actual?.value)
            endingInventoryData(endingInventoryActualSupervisorRange)

            if (foodKpiRangeOverViewSupervisorData.endingInventory?.status != null) {
                when {
                    foodKpiRangeOverViewSupervisorData.endingInventory.status.toString() == resources.getString(R.string.out_of_range) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewSupervisorData.endingInventory.status.toString() == resources.getString(R.string.under_limit) -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        ending_inventory_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        ending_inventory_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // food bought
            val foodBoughtActualSupervisorRange = Validation().checkAmountPercentageValue(this@FoodKpiActivity, foodKpiRangeOverViewSupervisorData.foodBought?.actual?.amount, foodKpiRangeOverViewSupervisorData.foodBought?.actual?.percentage, foodKpiRangeOverViewSupervisorData.foodBought?.actual?.value)

            foodBoughtData(foodBoughtActualSupervisorRange)

            if (foodKpiRangeOverViewSupervisorData.foodBought?.status != null) {
                when {
                    foodKpiRangeOverViewSupervisorData.foodBought.status.toString() == resources.getString(R.string.out_of_range) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.red))
                    }
                    foodKpiRangeOverViewSupervisorData.foodBought.status.toString() == resources.getString(R.string.under_limit) -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.green))

                    } else -> {
                        food_bought_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        food_bought_actual.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
        }catch (e:Exception){
            Logger.error(e.message.toString(),"Food Overview Period Range KPI")
        }

    }

    override fun onBackPressed() {
        Logger.info("Back-pressed", "Food Overview KPI")
        finish()
    }

    private fun actualFoodTotalData(actualFoodTotal: String) {
        if (actualFoodTotal.isEmpty()) {

            actual_food_total_error.visibility = View.VISIBLE
            actual_food_total.visibility = View.GONE
            val paramsActualFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsActualFoodError.weight = 1.0f
            actual_food_display.layoutParams = paramsActualFoodError

        } else {
            actual_food_total.text = actualFoodTotal
        }

    }

    private fun idealActualFoodData(
            idealActualFoodGoal: String,
            idealActualFoodVariance: String,
            idealActualFood: String
    ) {

        if (idealActualFoodGoal.isEmpty() && idealActualFoodVariance.isEmpty() &&
            idealActualFood.isEmpty()
        ) {

            ideal_vs_food_dollar_error.visibility = View.VISIBLE
            ideal_vs_food_dollar_goal.visibility = View.GONE
            ideal_vs_food_dollar_variance.visibility = View.GONE
            ideal_vs_food_dollar_actual.visibility = View.GONE

            val paramsFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsFoodError.weight = 2.0f
            ideal_vs_actual_food_variance_display_name.layoutParams = paramsFoodError

        } else if (idealActualFoodGoal.isNotEmpty() && idealActualFoodVariance.isNotEmpty() &&
            idealActualFood.isNotEmpty()
        ) {
            ideal_vs_food_dollar_goal.text = idealActualFoodGoal
            ideal_vs_food_dollar_variance.text = idealActualFoodVariance
            ideal_vs_food_dollar_actual.text = idealActualFood

        } else {
            if (idealActualFoodGoal.isEmpty()) {

                ideal_vs_food_dollar_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                ideal_vs_food_dollar_goal.text = idealActualFoodGoal
            }
            if (idealActualFoodVariance.isEmpty()) {

                ideal_vs_food_dollar_variance.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                ideal_vs_food_dollar_variance.text = idealActualFoodVariance
            }
            if (idealActualFood.isEmpty()) {

                ideal_vs_food_dollar_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                ideal_vs_food_dollar_actual.text = idealActualFood
            }

        }

    }

    private fun idealFoodData(
            idealFoodGoal: String,
            idealFoodVariance: String,
            idealFoodActual: String
    ) {
        if (idealFoodGoal.isEmpty() && idealFoodVariance.isEmpty() &&
            idealFoodActual.isEmpty()
        ) {

            ideal_food_error.visibility = View.VISIBLE
            ideal_food_actual.visibility = View.GONE

            val paramsIdealFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsIdealFoodError.weight = 1.0f
            ideal_food_display.layoutParams = paramsIdealFoodError

        } else {
            ideal_food_actual.text = idealFoodActual
        }

    }

    private fun cheeseActualFoodData(
            cheeseGoal: String,
            cheeseVariance: String,
            cheeseActual: String
    ) {
        if (cheeseGoal.isEmpty() && cheeseVariance.isEmpty() &&
            cheeseActual.isEmpty()
        ) {

            cheese_error.visibility = View.VISIBLE
            cheese_goal.visibility = View.GONE
            cheese_variance.visibility = View.GONE
            cheese_actual.visibility = View.GONE

            val paramsCheeseFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsCheeseFoodError.weight = 2.0f
            cheese_text.layoutParams = paramsCheeseFoodError

        } else if (cheeseGoal.isNotEmpty() && cheeseVariance.isNotEmpty() &&
            cheeseActual.isNotEmpty()
        ) {
            cheese_goal.text = cheeseGoal
            cheese_variance.text = cheeseVariance
            cheese_actual.text = cheeseActual

        } else {
            if (cheeseGoal.isEmpty()) {

                cheese_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cheese_goal.text = cheeseGoal
            }
            if (cheeseVariance.isEmpty()) {

                cheese_variance.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cheese_variance.text = cheeseVariance
            }
            if (cheeseActual.isEmpty()) {

                cheese_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cheese_actual.text = cheeseActual
            }

        }

    }

    private fun doughActualFoodData(
            doughGoal: String,
            doughVariance: String,
            doughActual: String
    ) {
        if (doughGoal.isEmpty() && doughVariance.isEmpty() &&
            doughActual.isEmpty()
        ) {

            dough_error.visibility = View.VISIBLE
            dough_goal.visibility = View.GONE
            dough_variance.visibility = View.GONE
            dough_actual.visibility = View.GONE

            val paramsDoughFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsDoughFoodError.weight = 2.0f
            dough_text.layoutParams = paramsDoughFoodError

        } else if (doughGoal.isNotEmpty() && doughVariance.isNotEmpty() &&
            doughActual.isNotEmpty()
        ) {
            dough_goal.text = doughGoal
            dough_variance.text = doughVariance
            dough_actual.text = doughActual

        } else {
            if (doughGoal.isEmpty()) {

                dough_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                dough_goal.text = doughGoal
            }
            if (doughVariance.isEmpty()) {

                dough_variance.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                dough_variance.text = doughVariance
            }
            if (doughActual.isEmpty()) {

                dough_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                dough_actual.text = doughActual
            }

        }

    }

    private fun twentyOzActualFoodData(
            twentyOzGoal: String,
            twentyOzVariance: String,
            twentyOzActual: String
    ) {

        if (twentyOzGoal.isEmpty() && twentyOzVariance.isEmpty() &&
            twentyOzActual.isEmpty()
        ) {

            twentyOz_error.visibility = View.VISIBLE
            twenty_oz_goal.visibility = View.GONE
            twenty_oz_variance.visibility = View.GONE
            twenty_oz_actual.visibility = View.GONE

            val paramsTwentyOzFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsTwentyOzFoodError.weight = 2.0f
            twenty_oz_text.layoutParams = paramsTwentyOzFoodError

        } else if (twentyOzGoal.isNotEmpty() && twentyOzVariance.isNotEmpty() &&
            twentyOzActual.isNotEmpty()
        ) {
            twenty_oz_goal.text = twentyOzGoal
            twenty_oz_variance.text = twentyOzVariance
            twenty_oz_actual.text = twentyOzActual

        } else {
            if (twentyOzGoal.isEmpty()) {

                twenty_oz_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                twenty_oz_goal.text = twentyOzGoal
            }
            if (twentyOzVariance.isEmpty()) {

                twenty_oz_variance.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                twenty_oz_variance.text = twentyOzVariance
            }
            if (twentyOzActual.isEmpty()) {

                twenty_oz_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                twenty_oz_actual.text = twentyOzActual
            }

        }

    }

    private fun top5ActualFoodData(
            top5Goal: String,
            top5Variance: String,
            top5Actual: String
    ) {
        if (top5Goal.isEmpty() && top5Variance.isEmpty() &&
            top5Actual.isEmpty()
        ) {

            top_five_error.visibility = View.VISIBLE
            top_five_goal.visibility = View.GONE
            top_five_variance.visibility = View.GONE
            top_five_actual.visibility = View.GONE

            val paramsTop5FoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsTop5FoodError.weight = 2.0f
            top_five_text.layoutParams = paramsTop5FoodError

        } else if (top5Goal.isNotEmpty() && top5Variance.isNotEmpty() &&
            top5Actual.isNotEmpty()
        ) {
            top_five_goal.text = top5Goal
            top_five_variance.text = top5Variance
            top_five_actual.text = top5Actual

        } else {
            if (top5Goal.isEmpty()) {

                top_five_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                top_five_goal.text = top5Goal
            }
            if (top5Variance.isEmpty()) {

                top_five_variance.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                top_five_variance.text = top5Variance
            }
            if (top5Actual.isEmpty()) {

                top_five_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                top_five_actual.text = top5Actual
            }
        }

    }

    private fun allItemsData(
            allItemsGoal: String,
            allItemsVariance: String,
            allItemsActual: String
    ) {
        if (allItemsGoal.isEmpty() && allItemsVariance.isEmpty() &&
            allItemsActual.isEmpty()
        ) {

            all_items_error.visibility = View.VISIBLE
            all_items_goal.visibility = View.GONE
            all_items_variance.visibility = View.GONE
            all_items_actual.visibility = View.GONE

            val paramsAllItemsFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsAllItemsFoodError.weight = 2.0f
            all_items_text.layoutParams = paramsAllItemsFoodError

        } else if (allItemsGoal.isNotEmpty() && allItemsVariance.isNotEmpty() &&
            allItemsActual.isNotEmpty()
        ) {
            all_items_goal.text = allItemsGoal
            all_items_variance.text = allItemsVariance
            all_items_actual.text = allItemsActual

        } else {
            if (allItemsGoal.isEmpty()) {

                all_items_goal.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                all_items_goal.text = allItemsGoal
            }
            if (allItemsVariance.isEmpty()) {

                all_items_variance.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                all_items_variance.text = allItemsVariance
            }
            if (allItemsActual.isEmpty()) {

                all_items_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                all_items_actual.text = allItemsActual
            }
        }

    }

    private fun endingInventoryData(endingInventoryActual: String) {
        if (endingInventoryActual.isEmpty()) {

            ending_inventory_error.visibility = View.VISIBLE
            ending_inventory_actual.visibility = View.GONE
            val paramsEndingInventoryError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsEndingInventoryError.weight = 1.0f
            ending_inventory_display.layoutParams = paramsEndingInventoryError

        } else {
            ending_inventory_actual.text = endingInventoryActual
        }

    }

    private fun foodBoughtData(foodBought: String) {

        if (foodBought.isEmpty()) {
            food_bought_error.visibility = View.VISIBLE
            food_bought_actual.visibility = View.GONE
            val paramsBoughtFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsBoughtFoodError.weight = 1.0F
            food_bought_display.layoutParams = paramsBoughtFoodError

        } else {
            food_bought_actual.text = foodBought
        }
    }

    private fun showFoodNarrativeData(narrative: String?) {
        if (!narrative.isNullOrEmpty()) {
            var foodNarrative = narrative
            foodNarrative = foodNarrative.replace("</p>", "<br><br>")
            food_narrative_value.text = Html.fromHtml(foodNarrative, Html.FROM_HTML_MODE_COMPACT)
        } else {
            food_narrative_value.visibility = View.INVISIBLE
        }
    }

    private fun callFoodOverviewNullApi(){
        val formattedStartDateValueFood: String
        val formattedEndDateValueFood: String

        val startDateValueFood = StorePrefData.startDateValue
        val endDateValueFood = StorePrefData.endDateValue

        if (StorePrefData.isCalendarSelected) {
            formattedStartDateValueFood = startDateValueFood
            formattedEndDateValueFood = endDateValueFood
        } else {
            formattedStartDateValueFood = startDateValueFood
            formattedEndDateValueFood = endDateValueFood
        }
        val progressDialogFoodOverview = CustomProgressDialog(this@FoodKpiActivity)
        progressDialogFoodOverview.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodeListFood = dbHelperFoodOverview.getAllSelectedAreaList(true)
            val stateCodeListFood = dbHelperFoodOverview.getAllSelectedStoreListState(true)
            val supervisorNumberListFood = dbHelperFoodOverview.getAllSelectedStoreListSupervisor(true)
            val storeNumberListFood = dbHelperFoodOverview.getAllSelectedStoreList(true)

            val responseMissingDataFood = try {
                apolloClient(this@FoodKpiActivity).query(
                    MissingDataQuery(
                            areaCodeListFood.toInput(),
                            stateCodeListFood.toInput(),
                            supervisorNumberListFood.toInput(),
                            storeNumberListFood.toInput(),
                            EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType).toInput(),
                            formattedStartDateValueFood.toInput(),
                            formattedEndDateValueFood.toInput(),
                    )
                ).await()

            } catch (e: ApolloException) {
                Logger.error("Missing Data ${e.message.toString()}","Food Overview KPI")
                progressDialogFoodOverview.dismissProgressDialog()
                return@launchWhenResumed
            }
            if(responseMissingDataFood.data?.missingData!=null){
                progressDialogFoodOverview.dismissProgressDialog()
                food_kpi_error_layout.visibility = View.VISIBLE
                food_kpi_error_layout.header_data_title.text  = responseMissingDataFood.data?.missingData!!.header
                food_kpi_error_layout.header_data_description.text  = responseMissingDataFood.data?.missingData!!.message
            }
            else{
                progressDialogFoodOverview.dismissProgressDialog()
                food_kpi_error_layout.visibility = View.GONE
            }
        }
    }
}
