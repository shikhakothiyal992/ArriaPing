package com.arria.ping.ui.kpi.overview

import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
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
import com.arria.ping.kpi.gm.StorePeriodRangeKPIQuery
import com.arria.ping.kpi.gm.StoreYesterdayKPIQuery
import com.arria.ping.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_a_w_u_s_kpi.*
import kotlinx.android.synthetic.main.activity_a_w_u_s_kpi.delivery_order_count
import kotlinx.android.synthetic.main.activity_a_w_u_s_kpi.level_two_scroll_data_action
import kotlinx.android.synthetic.main.activity_a_w_u_s_kpi.level_two_scroll_data_action_value
import kotlinx.android.synthetic.main.activity_a_w_u_s_kpi.parent_data_on_scroll_linear
import kotlinx.android.synthetic.main.activity_a_w_u_s_kpi.parent_data_on_scroll_view
import kotlinx.android.synthetic.main.activity_service_kpi.*
import kotlinx.android.synthetic.main.ceo_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.data_error_layout.view.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AWUSKpiActivity : AppCompatActivity() {
    var apiAwsArgumentFromFilter = ""
    private val gsonAws = Gson()
    lateinit var dbHelper: DatabaseHelperImpl

    @Inject
    lateinit var networkHelper: NetworkHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a_w_u_s_kpi)
        this.setFinishOnTouchOutside(false)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dbHelper = DatabaseHelperImpl(DatabaseBuilder.getInstance(this@AWUSKpiActivity))
        setAwsData()
        cross_button.setOnClickListener {
            Logger.info("Cancel Button clicked","AWUS Overview KPI Screen")
            finish()
        }

    }

    private fun setAwsData() {
        when (StorePrefData.role) {
            getString(R.string.ceo_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiAwsArgumentFromFilter =
                        intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiAwsArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val awsYesterdayDetailCEO = gsonAws.fromJson(
                            intent.getStringExtra("awus_data"),
                            CEOOverviewRangeQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callAWUSOverviewNullApi()
                            rangeOverViewCEOAws(awsYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val awsYesterdayDetailCEO = gsonAws.fromJson(
                            intent.getStringExtra("awus_data"),
                            CEOOverviewYesterdayQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callAWUSOverviewNullApi()
                            yesterdayViewCEOAws(awsYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Today -> {
                        val awsTodayDetail = gsonAws.fromJson(
                            intent.getStringExtra("awus_data"),
                            CEOOverviewTodayQuery.Ceo::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callAWUSOverviewNullApi()
                            todayViewCEOAws(awsTodayDetail)
                        }
                    }
                }
            }
            getString(R.string.do_text) -> {

                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiAwsArgumentFromFilter =
                        intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiAwsArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val awsYesterdayDetailCEO = gsonAws.fromJson(
                            intent.getStringExtra("awus_data"),
                            DOOverviewRangeQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callAWUSOverviewNullApi()
                            rangeOverViewDOAws(awsYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val awsYesterdayDetailCEO = gsonAws.fromJson(
                            intent.getStringExtra("awus_data"),
                            DOOverviewYesterdayQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callAWUSOverviewNullApi()
                            yesterdayViewDOAws(awsYesterdayDetailCEO)
                        }
                    }
                    IpConstants.Today -> {
                        val awsTodayDetail = gsonAws.fromJson(
                            intent.getStringExtra("awus_data"),
                            DOOverviewTodayQuery.Do_::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callAWUSOverviewNullApi()
                            todayViewDOAws(awsTodayDetail)
                        }
                    }
                }
            }
            getString(R.string.gm_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiAwsArgumentFromFilter =
                        intent.extras!!.getString("api_argument_from_filter")!!
                }
                when (apiAwsArgumentFromFilter) {

                    IpConstants.rangeFrom -> {
                        val rangePeriod = gsonAws.fromJson(
                            intent.getStringExtra("awus_data"),
                            StorePeriodRangeKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callAWUSOverviewNullApi()
                            rangeViewAws(rangePeriod)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val awsYesterdayDetail = gsonAws.fromJson(
                            intent.getStringExtra("awus_data"),
                            StoreYesterdayKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callAWUSOverviewNullApi()
                            yesterdayViewAws(awsYesterdayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val awsTodayDetail = gsonAws.fromJson(
                            intent.getStringExtra("awus_data"),
                            StoreTodayKPIQuery.GeneralManager::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callAWUSOverviewNullApi()
                            todayViewAws(awsTodayDetail)
                        }
                    }
                }
            }
            getString(R.string.supervisor_text) -> {
                if (intent.extras != null && intent.extras!!.getString("api_argument_from_filter") != null) {
                    apiAwsArgumentFromFilter =
                        intent.extras!!.getString("api_argument_from_filter")!!
                }

                when (apiAwsArgumentFromFilter) {
                    IpConstants.rangeFrom -> {
                        val rangePeriod = gsonAws.fromJson(
                            intent.getStringExtra("awus_data"),
                            SupervisorOverviewRangeQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callAWUSOverviewNullApi()
                            rangeViewSupervisorAws(rangePeriod)
                        }
                    }
                    IpConstants.Yesterday -> {
                        val awusTodayDetail = gsonAws.fromJson(
                            intent.getStringExtra("awus_data"),
                            SupervisorOverviewYesterdayQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callAWUSOverviewNullApi()
                            yesterdayViewSupervisorAws(awusTodayDetail)
                        }
                    }
                    IpConstants.Today -> {
                        val awusTodayDetail = gsonAws.fromJson(
                            intent.getStringExtra("awus_data"),
                            SupervisorOverviewTodayQuery.Supervisor::class.java
                        )
                        if (networkHelper.isNetworkConnected()) {
                            callAWUSOverviewNullApi()
                            todayViewSupervisorAws(awusTodayDetail)
                        }
                    }
                }
            }
        }
    }

    //GM View
    private fun todayViewAws(todayDetail: StoreTodayKPIQuery.GeneralManager) {
        val storeDataTodayViewAws = todayDetail.kpis?.store?.today!!.sales
        Logger.info("AWUS Today Query", "AWUS Overview KPI")

        // display name
        awus_display.text = storeDataTodayViewAws!!.displayName ?: getString(R.string.awus_text)
        // pcy display name
        pcy_display_name.text =
            storeDataTodayViewAws.pcya?.total?.displayName ?: getString(R.string.pcy)
        olo_pcy_display.text =
            storeDataTodayViewAws.pcya?.olo?.displayName ?: getString(R.string.olo_pcy)
        phone_pcys.text =
            storeDataTodayViewAws.pcya?.phone?.displayName ?: getString(R.string.phone_pcys)
        walkin_pcy.text =
            storeDataTodayViewAws.pcya?.walkin?.displayName ?: getString(R.string.walkin_pcy)
        delivery_pcy.text =
            storeDataTodayViewAws.pcya?.delivery?.displayName ?: getString(R.string.delivery_pcy)
        delivery_pcy.text =
            storeDataTodayViewAws.pcya?.delivery?.displayName ?: getString(R.string.delivery_pcy)
        carry_out_pcy.text =
            storeDataTodayViewAws.pcya?.carryout?.displayName ?: getString(R.string.carry_out_pcy)
        // order count display
        order_count_display.text =
            storeDataTodayViewAws.orderCount?.total?.displayName ?: getString(R.string.order_count)
        olo_order_count.text =
            storeDataTodayViewAws.orderCount?.olo?.displayName
                ?: getString(R.string.olo_order_count)
        phone_order_count.text = storeDataTodayViewAws.orderCount?.phone?.displayName
            ?: getString(R.string.phone_order_count)
        walkin_order_count.text = storeDataTodayViewAws.orderCount?.walkin?.displayName
            ?: getString(R.string.walkin_order_count)
        delivery_order_count.text = storeDataTodayViewAws.orderCount?.delivery?.displayName
            ?: getString(R.string.delivery_order_count)
        carry_outorder_count.text = storeDataTodayViewAws.orderCount?.carryout?.displayName
            ?: getString(R.string.carry_outorder_count)

        // average ticket count display

         if (storeDataTodayViewAws.averageTicket?.total == null || storeDataTodayViewAws.averageTicket.total.displayName.isNullOrEmpty()) {
                    ll_average_ticket_display_name.visibility = View.GONE
            } else {
                ll_average_ticket_display_name.visibility = View.VISIBLE
                    average_ticket_display_name.text =
                        storeDataTodayViewAws.averageTicket.total.displayName
            }


             if (storeDataTodayViewAws.averageTicket?.olo == null || storeDataTodayViewAws.averageTicket.olo.displayName.isNullOrEmpty()) {
                 ll_olo_average_ticket.visibility = View.GONE
            } else {
                ll_olo_average_ticket.visibility = View.VISIBLE
                 olo_average_ticket.text =
                     storeDataTodayViewAws.averageTicket.olo.displayName
            }

             if (storeDataTodayViewAws.averageTicket?.phone == null || storeDataTodayViewAws.averageTicket.phone.displayName.isNullOrEmpty()) {
                 ll_average_phone_ticket.visibility = View.GONE
            } else {
                ll_average_phone_ticket.visibility = View.VISIBLE
                 average_phone_ticket.text =
                     storeDataTodayViewAws.averageTicket.phone.displayName
            }

             if (storeDataTodayViewAws.averageTicket?.walkin == null || storeDataTodayViewAws.averageTicket.walkin.displayName.isNullOrEmpty()) {
                 ll_average_walkin_ticket.visibility = View.GONE
            } else {
                ll_average_walkin_ticket.visibility = View.VISIBLE
                 average_walkin_ticket.text =
                     storeDataTodayViewAws.averageTicket.walkin.displayName
            }

             if (storeDataTodayViewAws.averageTicket?.delivery == null || storeDataTodayViewAws.averageTicket.delivery.displayName.isNullOrEmpty()) {
                 ll_delivery_average_tickets.visibility = View.GONE
            } else {
                ll_delivery_average_tickets.visibility = View.VISIBLE
                 delivery_average_tickets.text =
                     storeDataTodayViewAws.averageTicket.delivery.displayName
            }

               if (storeDataTodayViewAws.averageTicket?.carryout == null || storeDataTodayViewAws.averageTicket.carryout.displayName.isNullOrEmpty()) {
                   ll_carry_average_tickets.visibility = View.GONE
            } else {
                ll_carry_average_tickets.visibility = View.VISIBLE
                   carry_average_tickets.text =
                       storeDataTodayViewAws.averageTicket.carryout.displayName
            }

        // olo sales display
        if (storeDataTodayViewAws.oloSales?.total == null || storeDataTodayViewAws.oloSales.total.displayName.isNullOrEmpty()) {
            ll_olo_sales_display_name.visibility = View.GONE
        } else {
            ll_olo_sales_display_name.visibility = View.VISIBLE
            olo_sales_display_name.text =
                storeDataTodayViewAws.oloSales.total.displayName
        }

        if (storeDataTodayViewAws.oloSales?.pcya == null || storeDataTodayViewAws.oloSales.pcya.displayName.isNullOrEmpty()) {
            olo_sales_pcy_parent.visibility = View.GONE
        } else {
            olo_sales_pcy_parent.visibility = View.VISIBLE
            olo_pcy.text =
                storeDataTodayViewAws.oloSales.pcya.displayName
        }

        if (storeDataTodayViewAws.oloSales?.orderCount == null || storeDataTodayViewAws.oloSales.orderCount.displayName.isNullOrEmpty()) {
            ll_olo_order_count_sales_display.visibility = View.GONE
        } else {
            ll_olo_order_count_sales_display.visibility = View.VISIBLE
            olo_order_count_sales_display.text =
                storeDataTodayViewAws.oloSales.orderCount.displayName
        }

        if (storeDataTodayViewAws.oloSales?.averageTicket == null || storeDataTodayViewAws.oloSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_olo_average_ticket_sales_display.visibility = View.GONE
        } else {
            ll_olo_average_ticket_sales_display.visibility = View.VISIBLE
            olo_average_ticket_sales_display.text =
                storeDataTodayViewAws.oloSales.averageTicket.displayName
        }


        // phone sales display
        if (storeDataTodayViewAws.phoneSales?.total == null || storeDataTodayViewAws.phoneSales.total.displayName.isNullOrEmpty()) {
            ll_phone_sales_display_name.visibility = View.GONE
        } else {
            ll_phone_sales_display_name.visibility = View.VISIBLE
            phone_sales_display_name.text =
                storeDataTodayViewAws.phoneSales.total.displayName
        }


        if (storeDataTodayViewAws.phoneSales?.orderCount == null || storeDataTodayViewAws.phoneSales.orderCount.displayName.isNullOrEmpty()) {
            ll_phone_order_count_display.visibility = View.GONE
        } else {
            ll_phone_order_count_display.visibility = View.VISIBLE
            phone_order_count_display.text =
                storeDataTodayViewAws.phoneSales.orderCount.displayName
        }

        if (storeDataTodayViewAws.phoneSales?.averageTicket == null || storeDataTodayViewAws.phoneSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_average_phone_ticket_display.visibility = View.GONE
        } else {
            ll_average_phone_ticket_display.visibility = View.VISIBLE
            average_phone_ticket_display.text =
                storeDataTodayViewAws.phoneSales.averageTicket.displayName
        }


        // walk-in Sales
        if (storeDataTodayViewAws.walkinSales?.total == null || storeDataTodayViewAws.walkinSales.total.displayName.isNullOrEmpty()) {
            ll_walkin_sales_display_name.visibility = View.GONE
        } else {
            ll_walkin_sales_display_name.visibility = View.VISIBLE
            walkin_sales_display_name.text =
                storeDataTodayViewAws.walkinSales.total.displayName
        }

        if (storeDataTodayViewAws.walkinSales?.orderCount == null || storeDataTodayViewAws.walkinSales.orderCount.displayName.isNullOrEmpty()) {
            ll_walkin_order_count_sales_display.visibility = View.GONE
        } else {
            ll_walkin_order_count_sales_display.visibility = View.VISIBLE
            walkin_order_count_sales_display.text =
                storeDataTodayViewAws.walkinSales.orderCount.displayName
        }

        if (storeDataTodayViewAws.walkinSales?.averageTicket == null || storeDataTodayViewAws.walkinSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_average_walkin_ticket_display.visibility = View.GONE
        } else {
            ll_average_walkin_ticket_display.visibility = View.VISIBLE
            average_walkin_ticket_display.text =
                storeDataTodayViewAws.walkinSales.averageTicket.displayName
        }


        //  deliver Sales
        if (storeDataTodayViewAws.deliverySales?.total == null || storeDataTodayViewAws.deliverySales.total.displayName.isNullOrEmpty()) {
            ll_delivery_sales_display_name.visibility = View.GONE
        } else {
            ll_delivery_sales_display_name.visibility = View.VISIBLE
            delivery_sales_display_name.text =
                storeDataTodayViewAws.deliverySales.total.displayName
        }
        if (storeDataTodayViewAws.deliverySales?.orderCount == null || storeDataTodayViewAws.deliverySales.orderCount.displayName.isNullOrEmpty()) {
            ll_delivery_order_count_display.visibility = View.GONE
        } else {
            ll_delivery_order_count_display.visibility = View.VISIBLE
            delivery_order_count_display.text =
                storeDataTodayViewAws.deliverySales.orderCount.displayName
        }
        if (storeDataTodayViewAws.deliverySales?.averageTicket == null || storeDataTodayViewAws.deliverySales.averageTicket.displayName.isNullOrEmpty()) {
            ll_delivery_average_tickets_display.visibility = View.GONE
        } else {
            ll_delivery_average_tickets_display.visibility = View.VISIBLE
            delivery_average_tickets_display.text =
                storeDataTodayViewAws.deliverySales.averageTicket.displayName
        }


        //  carryout Sales
          if (storeDataTodayViewAws.carryoutSales?.total == null || storeDataTodayViewAws.carryoutSales.total.displayName.isNullOrEmpty()) {
                ll_carry_out_display_text.visibility = View.GONE
            } else {
                ll_carry_out_display_text.visibility = View.VISIBLE
                carry_out_display_text.text =
                    storeDataTodayViewAws.carryoutSales.total.displayName
            }
            if (storeDataTodayViewAws.carryoutSales?.orderCount == null || storeDataTodayViewAws.carryoutSales.orderCount.displayName.isNullOrEmpty()) {
                ll_carry_outorder_count_display.visibility = View.GONE
            } else {
                ll_carry_outorder_count_display.visibility = View.VISIBLE
                carry_outorder_count_display.text =
                    storeDataTodayViewAws.carryoutSales.orderCount.displayName
            }

            if (storeDataTodayViewAws.carryoutSales?.averageTicket == null || storeDataTodayViewAws.carryoutSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets_display.visibility = View.GONE
            } else {
                ll_carry_average_tickets_display.visibility = View.VISIBLE
                carry_average_tickets_display.text =
                    storeDataTodayViewAws.carryoutSales.averageTicket.displayName
            }

        if (storeDataTodayViewAws.actual?.value?.isNaN() == false && storeDataTodayViewAws.status?.toString() != null) {
            awus_sales.text = Validation().dollarFormatting(
                storeDataTodayViewAws.actual.value
            )
            when {
                storeDataTodayViewAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                    awus_sales.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewAws.status.toString() == resources.getString(R.string.under_limit) -> {
                    awus_sales.setTextColor(getColor(R.color.green))
                }
                else -> {
                    awus_sales.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        // scroll detect

        awus_scroll_parent.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 0f
                override fun onScrollChanged() {
                    if (awus_scroll_parent.scrollY > y) {
                        parent_data_on_scroll_linear.visibility = View.VISIBLE
                        parent_data_on_scroll_view.visibility = View.VISIBLE
                        level_two_scroll_data_action.text =
                            storeDataTodayViewAws.displayName ?: getString(R.string.awus_text)
                        if (storeDataTodayViewAws.actual?.value?.isNaN() == false && storeDataTodayViewAws.status?.toString() != null) {
                            level_two_scroll_data_action_value.text =
                                getString(R.string.dollar_text).plus(
                                    Validation().dollarFormatting(
                                        storeDataTodayViewAws.actual.value
                                    )
                                )
                            when {
                                storeDataTodayViewAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.red_circle,
                                        0
                                    )
                                }
                                storeDataTodayViewAws.status.toString() == resources.getString(R.string.under_limit) -> {
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
                    y = awus_scroll_parent.scrollY.toFloat()
                    if (y <= 0) {
                        parent_data_on_scroll_linear.visibility = View.INVISIBLE
                        parent_data_on_scroll_view.visibility = View.GONE
                    }
                }
            })


        showAWUSNarrativeData(storeDataTodayViewAws.narrative.toString())

        awus_goal_value.text =
            if (storeDataTodayViewAws.goal?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(storeDataTodayViewAws.goal.value)
            ) else ""
        awus__variance_value.text =
            if (storeDataTodayViewAws.variance?.value?.isNaN() == false) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(storeDataTodayViewAws.variance.value)
            ) else ""


        // PCY
        if (storeDataTodayViewAws.pcya?.total?.actual?.percentage?.isNaN() == false && storeDataTodayViewAws.pcya.total.status?.toString() != null) {
            pcy_percentage.text =
                Validation().ignoreZeroAfterDecimal(storeDataTodayViewAws.pcya.total.actual.percentage)
                    .plus(
                        getString(
                            R.string.percentage_text
                        )
                    )
            when {
                storeDataTodayViewAws.pcya.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    pcy_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewAws.pcya.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    pcy_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    pcy_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewAws.pcya?.olo?.actual?.percentage?.isNaN() == false) {
            olo_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.pcya.olo.actual.percentage
            )).plus(getString(R.string.percentage_text))
            olo_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_olo.visibility = View.GONE

        } else {
            olo_pcy_percentage.visibility = View.GONE

            awus_pcya_error_olo.visibility = View.VISIBLE
        }
        if (storeDataTodayViewAws.pcya?.phone?.actual?.percentage?.isNaN() == false) {
            phone_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.pcya.phone.actual.percentage
            )).plus(getString(R.string.percentage_text))
            phone_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_phone.visibility = View.GONE

        } else {
            phone_pcy_percentage.visibility = View.GONE

            awus_pcya_error_phone.visibility = View.VISIBLE
        }
        if (storeDataTodayViewAws.pcya?.walkin?.actual?.percentage?.isNaN() == false) {
            walkin_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.pcya.walkin.actual.percentage
            )).plus(getString(R.string.percentage_text))
            walkin_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_phone.visibility = View.GONE

        } else {
            walkin_pcy_percentage.visibility = View.GONE

            awus_pcya_error_walkin.visibility = View.VISIBLE
        }
        if (storeDataTodayViewAws.pcya?.delivery?.actual?.percentage?.isNaN() == false) {
            delivery_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.pcya.delivery.actual.percentage
            )).plus(getString(R.string.percentage_text))
            delivery_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_delivery.visibility = View.GONE

        } else {
            delivery_pcy_percentage.visibility = View.GONE

            awus_pcya_error_delivery.visibility = View.VISIBLE
        }
        if (storeDataTodayViewAws.pcya?.carryout?.actual?.percentage?.isNaN() == false) {
            carry_out_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.pcya.carryout.actual.percentage
            )).plus(getString(R.string.percentage_text))
            carry_out_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_carryout.visibility = View.GONE

        } else {
            carry_out_pcy_percentage.visibility = View.GONE

            awus_pcya_error_carryout.visibility = View.VISIBLE
        }


        // order count

        if (storeDataTodayViewAws.orderCount?.total?.actual?.value?.isNaN() == false && storeDataTodayViewAws.orderCount.total.status?.toString() != null) {
            oc_percentage.text =
                Validation().ignoreZeroAfterDecimal(storeDataTodayViewAws.orderCount.total.actual.value)
            when {
                storeDataTodayViewAws.orderCount.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    oc_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewAws.orderCount.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    oc_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    oc_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewAws.orderCount?.olo?.actual?.value?.isNaN() == false) {
            oc_pcy_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.orderCount.olo.actual.value
            )
            oc_pcy_percentage.visibility = View.VISIBLE
            awus_oc_error_olo.visibility = View.GONE

        } else {
            oc_pcy_percentage.visibility = View.GONE
            awus_oc_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.orderCount?.phone?.actual?.value?.isNaN() == false) {
            phone_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.orderCount.phone.actual.value
            )
            phone_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_phone.visibility = View.GONE

        } else {
            phone_oc_percentage.visibility = View.GONE
            awus_oc_error_phone.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.orderCount?.walkin?.actual?.value?.isNaN() == false) {
            walkin_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.orderCount.walkin.actual.value
            )
            walkin_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_walkin.visibility = View.GONE

        } else {
            walkin_oc_percentage.visibility = View.GONE
            awus_oc_error_walkin.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.orderCount?.delivery?.actual?.value?.isNaN() == false) {
            delivery_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.orderCount.delivery.actual.value
            )
            delivery_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_delivery.visibility = View.GONE

        } else {
            delivery_oc_percentage.visibility = View.GONE
            awus_oc_error_delivery.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.orderCount?.carryout?.actual?.value?.isNaN() == false) {
            carry_out_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.orderCount.carryout.actual.value
            )
            carry_out_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_carryout.visibility = View.GONE

        } else {
            carry_out_oc_percentage.visibility = View.GONE
            awus_oc_error_carryout.visibility = View.VISIBLE

        }


        // Average  Tickets
        if (storeDataTodayViewAws.averageTicket?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewAws.averageTicket.total.status?.toString() != null) {
            average_percentage.text =
                if (!storeDataTodayViewAws.averageTicket.total.actual.amount.isNaN()) getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(storeDataTodayViewAws.averageTicket.total.actual.amount)
                ) else ""

            when {
                storeDataTodayViewAws.averageTicket.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewAws.averageTicket.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    average_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewAws.averageTicket?.olo?.actual?.value?.isNaN() == false) {
            average_olo_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.averageTicket.olo.actual.value
            )
            average_olo_percentage.visibility = View.VISIBLE
            awus_avg_error_olo.visibility = View.GONE

        } else {
            average_olo_percentage.visibility = View.GONE
            awus_avg_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.averageTicket?.phone?.actual?.value?.isNaN() == false) {
            average_phone_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.averageTicket.phone.actual.value
            )
            average_phone_percentage.visibility = View.VISIBLE
            awus_avg_error_phone.visibility = View.GONE

        } else {
            average_phone_percentage.visibility = View.GONE
            awus_avg_error_phone.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.averageTicket?.walkin?.actual?.value?.isNaN() == false) {
            walkin_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.averageTicket.walkin.actual.value
            )
            walkin_average_percentage.visibility = View.VISIBLE
            awus_avg_error_walkin.visibility = View.GONE

        } else {
            walkin_average_percentage.visibility = View.GONE
            awus_avg_error_walkin.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.averageTicket?.delivery?.actual?.value?.isNaN() == false) {
            delivery_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.averageTicket.delivery.actual.value
            )
            delivery_percentage.visibility = View.VISIBLE
            awus_avg_error_delivery.visibility = View.GONE

        } else {
            delivery_percentage.visibility = View.GONE
            awus_avg_error_delivery.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.averageTicket?.carryout?.actual?.value?.isNaN() == false) {
            carry_out_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.averageTicket.carryout.actual.value
            )
            carry_out_average_percentage.visibility = View.VISIBLE
            awus_avg_error_carryout.visibility = View.GONE

        } else {
            carry_out_average_percentage.visibility = View.GONE
            awus_avg_error_carryout.visibility = View.VISIBLE

        }


        // OLO sales
        if (storeDataTodayViewAws.oloSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewAws.oloSales.total.status?.toString() != null) {
            olo_sales_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewAws.oloSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewAws.oloSales.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    olo_sales_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewAws.oloSales.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    olo_sales_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    olo_sales_percentage.setTextColor(getColor(R.color.text_color))

                }
            }


        }

        if (storeDataTodayViewAws.oloSales?.pcya?.actual?.percentage?.isNaN() == false) {
            olo_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.oloSales.pcya.actual.percentage
            )
            olo_percentage.visibility = View.VISIBLE
            awus_olo_sales_error_olo.visibility = View.GONE

        } else {
            olo_percentage.visibility = View.GONE
            awus_olo_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.oloSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            olo_order_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.oloSales.orderCount.actual.percentage
            )
            olo_order_percentage.visibility = View.VISIBLE
            awus_olo_sales_error_oc.visibility = View.GONE

        } else {
            olo_order_percentage.visibility = View.GONE
            awus_olo_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.oloSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            olo_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.oloSales.averageTicket.actual.percentage
            )
            olo_average_percentage.visibility = View.VISIBLE
            awus_olo_sales_error_at.visibility = View.GONE

        } else {
            olo_average_percentage.visibility = View.GONE
            awus_olo_sales_error_at.visibility = View.VISIBLE

        }



        // Phone sales order count
        if (storeDataTodayViewAws.phoneSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewAws.phoneSales.total.status?.toString() != null) {
            phone_sales_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewAws.phoneSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewAws.phoneSales.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    phone_sales_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewAws.phoneSales.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    phone_sales_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    phone_sales_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewAws.phoneSales?.pcya?.actual?.percentage?.isNaN() == false) {
            phone_pcys_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.phoneSales.pcya.actual.percentage
            )
            phone_pcys_percentage.visibility = View.VISIBLE
            awus_phones_sales_error_olo.visibility = View.GONE

        } else {
            phone_pcys_percentage.visibility = View.GONE
            awus_phones_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.phoneSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            phone_sales_order_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.phoneSales.orderCount.actual.percentage
            )
            phone_sales_order_percentage.visibility = View.VISIBLE
            awus_phones_sales_error_oc.visibility = View.GONE

        } else {
            phone_sales_order_percentage.visibility = View.GONE
            awus_phones_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.phoneSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            average_phone_ticket_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.phoneSales.averageTicket.actual.percentage
            )
            average_phone_ticket_percentage.visibility = View.VISIBLE
            awus_phones_sales_error_at.visibility = View.GONE

        } else {
            average_phone_ticket_percentage.visibility = View.GONE
            awus_phones_sales_error_at.visibility = View.VISIBLE

        }



        // walk-in sales

        if (storeDataTodayViewAws.walkinSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewAws.walkinSales.total.status?.toString() != null) {
            walkin_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewAws.walkinSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewAws.walkinSales.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    walkin_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewAws.walkinSales.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    walkin_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    walkin_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewAws.walkinSales?.pcya?.actual?.percentage?.isNaN() == false) {
            walkin_pcy_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.walkinSales.pcya.actual.percentage
            )
            walkin_pcy_value.visibility = View.VISIBLE
            awus_walkins_sales_error_olo.visibility = View.GONE

        } else {
            walkin_pcy_value.visibility = View.GONE
            awus_walkins_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.walkinSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            walkin_order_count_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.walkinSales.orderCount.actual.percentage
            )
            walkin_order_count_value.visibility = View.VISIBLE
            awus_walkins_sales_error_oc.visibility = View.GONE

        } else {
            walkin_order_count_value.visibility = View.GONE
            awus_walkins_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.walkinSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            average_walkin_ticket_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.walkinSales.averageTicket.actual.percentage
            )
            average_walkin_ticket_value.visibility = View.VISIBLE
            awus_walkins_sales_error_at.visibility = View.GONE

        } else {
            average_walkin_ticket_value.visibility = View.GONE
            awus_walkins_sales_error_at.visibility = View.VISIBLE

        }



        // delivery sales
        if (storeDataTodayViewAws.deliverySales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewAws.deliverySales.total.status?.toString() != null) {
            delivery_sales_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewAws.deliverySales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewAws.deliverySales.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    delivery_sales_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewAws.deliverySales.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    delivery_sales_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    delivery_sales_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewAws.deliverySales?.pcya?.actual?.percentage?.isNaN() == false) {
            delivery_pcy_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.deliverySales.pcya.actual.percentage
            )
            delivery_pcy_value.visibility = View.VISIBLE
            awus_delivery_sales_error_olo.visibility = View.GONE

        } else {
            delivery_pcy_value.visibility = View.GONE
            awus_delivery_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.deliverySales?.orderCount?.actual?.percentage?.isNaN() == false) {
            delivery_order_count_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.deliverySales.orderCount.actual.percentage
            )
            delivery_order_count_value.visibility = View.VISIBLE
            awus_walkins_sales_error_at.visibility = View.GONE

        } else {
            delivery_order_count_value.visibility = View.GONE
            awus_delivery_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.deliverySales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            delivery_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.deliverySales.averageTicket.actual.percentage
            )
            delivery_average_percentage.visibility = View.VISIBLE
            awus_walkins_sales_error_at.visibility = View.GONE


        } else {
            delivery_average_percentage.visibility = View.GONE
            awus_delivery_sales_error_at.visibility = View.VISIBLE

        }




        // carryout sales

        if (storeDataTodayViewAws.carryoutSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewAws.carryoutSales.total.status?.toString() != null) {
            carryout_average_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewAws.carryoutSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewAws.carryoutSales.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    carryout_average_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewAws.carryoutSales.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    carryout_average_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    carryout_average_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewAws.carryoutSales?.pcya?.actual?.percentage?.isNaN() == false) {
            carry_out_pcy_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.carryoutSales.pcya.actual.percentage
            )
            carry_out_pcy_value.visibility = View.VISIBLE
            awus_carryout_sales_error_olo.visibility = View.GONE

        } else {
            carry_out_pcy_value.visibility = View.GONE
            awus_carryout_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.carryoutSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            carry_outorder_count_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.carryoutSales.orderCount.actual.percentage
            )
            carry_outorder_count_value.visibility = View.VISIBLE
            awus_carryout_sales_error_oc.visibility = View.GONE

        } else {
            carry_outorder_count_value.visibility = View.GONE
            awus_carryout_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewAws.carryoutSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            carry_average_tickets_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewAws.carryoutSales.averageTicket.actual.percentage
            )
            carry_average_tickets_value.visibility = View.VISIBLE
            awus_carryout_sales_error_at.visibility = View.GONE

        } else {
            carry_average_tickets_value.visibility = View.GONE
            awus_carryout_sales_error_at.visibility = View.VISIBLE

        }


    }

    private fun yesterdayViewAws(todayDetail: StoreYesterdayKPIQuery.GeneralManager) {

        try {
            val storeDataYesterdayViewAws = todayDetail.kpis?.store?.yesterday!!.sales

            Logger.info("AWUS Yesterday", "AWUS Overview KPI")

            awus_display.text = getString(R.string.awus_text)
            // pcy display name
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewAws?.phoneSales?.pcya?.displayName,
                phone_sales_pcys_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewAws?.walkinSales?.pcya?.displayName,
                walkin_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewAws?.deliverySales?.pcya?.displayName,
                delivery_sales_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewAws?.carryoutSales?.pcya?.displayName,
                carry_out_sales_pcy_parent
            )

            pcy_display_name.text =
                storeDataYesterdayViewAws!!.pcya?.total?.displayName ?: getString(R.string.pcy)
            phone_pcys.text =
                storeDataYesterdayViewAws.pcya?.phone?.displayName ?: getString(R.string.phone_pcys)
            walkin_pcy.text =
                storeDataYesterdayViewAws.pcya?.walkin?.displayName
                    ?: getString(R.string.walkin_pcy)
            delivery_pcy.text =
                storeDataYesterdayViewAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            delivery_pcy.text =
                storeDataYesterdayViewAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            carry_out_pcy.text =
                storeDataYesterdayViewAws.pcya?.carryout?.displayName
                    ?: getString(R.string.carry_out_pcy)
            // order count display
            order_count_display.text =
                storeDataYesterdayViewAws.orderCount?.total?.displayName
                    ?: getString(R.string.order_count)
            olo_order_count.text =
                storeDataYesterdayViewAws.orderCount?.olo?.displayName
                    ?: getString(R.string.olo_order_count)
            phone_order_count.text = storeDataYesterdayViewAws.orderCount?.phone?.displayName
                ?: getString(R.string.phone_order_count)
            walkin_order_count.text = storeDataYesterdayViewAws.orderCount?.walkin?.displayName
                ?: getString(R.string.walkin_order_count)
            delivery_order_count.text = storeDataYesterdayViewAws.orderCount?.delivery?.displayName
                ?: getString(R.string.delivery_order_count)
            carry_outorder_count.text = storeDataYesterdayViewAws.orderCount?.carryout?.displayName
                ?: getString(R.string.carry_outorder_count)

            // average ticket count display
            if (storeDataYesterdayViewAws.averageTicket?.total == null || storeDataYesterdayViewAws.averageTicket.total.displayName.isNullOrEmpty()) {
                ll_average_ticket_display_name.visibility = View.GONE
            } else {
                ll_average_ticket_display_name.visibility = View.VISIBLE
                average_ticket_display_name.text =
                    storeDataYesterdayViewAws.averageTicket.total.displayName
            }


            if (storeDataYesterdayViewAws.averageTicket?.olo == null || storeDataYesterdayViewAws.averageTicket.olo.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket.visibility = View.GONE
            } else {
                ll_olo_average_ticket.visibility = View.VISIBLE
                olo_average_ticket.text =
                    storeDataYesterdayViewAws.averageTicket.olo.displayName
            }

            if (storeDataYesterdayViewAws.averageTicket?.phone == null || storeDataYesterdayViewAws.averageTicket.phone.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket.visibility = View.GONE
            } else {
                ll_average_phone_ticket.visibility = View.VISIBLE
                average_phone_ticket.text =
                    storeDataYesterdayViewAws.averageTicket.phone.displayName
            }

            if (storeDataYesterdayViewAws.averageTicket?.walkin == null || storeDataYesterdayViewAws.averageTicket.walkin.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket.visibility = View.GONE
            } else {
                ll_average_walkin_ticket.visibility = View.VISIBLE
                average_walkin_ticket.text =
                    storeDataYesterdayViewAws.averageTicket.walkin.displayName
            }

            if (storeDataYesterdayViewAws.averageTicket?.delivery == null || storeDataYesterdayViewAws.averageTicket.delivery.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets.visibility = View.GONE
            } else {
                ll_delivery_average_tickets.visibility = View.VISIBLE
                delivery_average_tickets.text =
                    storeDataYesterdayViewAws.averageTicket.delivery.displayName
            }

            if (storeDataYesterdayViewAws.averageTicket?.carryout == null || storeDataYesterdayViewAws.averageTicket.carryout.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets.visibility = View.GONE
            } else {
                ll_carry_average_tickets.visibility = View.VISIBLE
                carry_average_tickets.text =
                    storeDataYesterdayViewAws.averageTicket.carryout.displayName
            }


            // olo sales display
            if (storeDataYesterdayViewAws.oloSales?.total == null || storeDataYesterdayViewAws.oloSales.total.displayName.isNullOrEmpty()) {
                ll_olo_sales_display_name.visibility = View.GONE
            } else {
                ll_olo_sales_display_name.visibility = View.VISIBLE
                olo_sales_display_name.text =
                    storeDataYesterdayViewAws.oloSales.total.displayName
            }

            if (storeDataYesterdayViewAws.oloSales?.pcya == null || storeDataYesterdayViewAws.oloSales.pcya.displayName.isNullOrEmpty()) {
                olo_sales_pcy_parent.visibility = View.GONE
            } else {
                olo_sales_pcy_parent.visibility = View.VISIBLE
                olo_pcy.text =
                    storeDataYesterdayViewAws.oloSales.pcya.displayName
            }

            if (storeDataYesterdayViewAws.oloSales?.orderCount == null || storeDataYesterdayViewAws.oloSales.orderCount.displayName.isNullOrEmpty()) {
                ll_olo_order_count_sales_display.visibility = View.GONE
            } else {
                ll_olo_order_count_sales_display.visibility = View.VISIBLE
                olo_order_count_sales_display.text =
                    storeDataYesterdayViewAws.oloSales.orderCount.displayName
            }

            if (storeDataYesterdayViewAws.oloSales?.averageTicket == null || storeDataYesterdayViewAws.oloSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket_sales_display.visibility = View.GONE
            } else {
                ll_olo_average_ticket_sales_display.visibility = View.VISIBLE
                olo_average_ticket_sales_display.text =
                    storeDataYesterdayViewAws.oloSales.averageTicket.displayName
            }

            // phone sales display
            if (storeDataYesterdayViewAws.phoneSales?.total == null || storeDataYesterdayViewAws.phoneSales.total.displayName.isNullOrEmpty()) {
                ll_phone_sales_display_name.visibility = View.GONE
            } else {
                ll_phone_sales_display_name.visibility = View.VISIBLE
                phone_sales_display_name.text =
                    storeDataYesterdayViewAws.phoneSales.total.displayName
            }


            if (storeDataYesterdayViewAws.phoneSales?.orderCount == null || storeDataYesterdayViewAws.phoneSales.orderCount.displayName.isNullOrEmpty()) {
                ll_phone_order_count_display.visibility = View.GONE
            } else {
                ll_phone_order_count_display.visibility = View.VISIBLE
                phone_order_count_display.text =
                    storeDataYesterdayViewAws.phoneSales.orderCount.displayName
            }

            if (storeDataYesterdayViewAws.phoneSales?.averageTicket == null || storeDataYesterdayViewAws.phoneSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket_display.visibility = View.GONE
            } else {
                ll_average_phone_ticket_display.visibility = View.VISIBLE
                average_phone_ticket_display.text =
                    storeDataYesterdayViewAws.phoneSales.averageTicket.displayName
            }


            // walk-in Sales
            if (storeDataYesterdayViewAws.walkinSales?.total == null || storeDataYesterdayViewAws.walkinSales.total.displayName.isNullOrEmpty()) {
                ll_walkin_sales_display_name.visibility = View.GONE
            } else {
                ll_walkin_sales_display_name.visibility = View.VISIBLE
                walkin_sales_display_name.text =
                    storeDataYesterdayViewAws.walkinSales.total.displayName
            }

            if (storeDataYesterdayViewAws.walkinSales?.orderCount == null || storeDataYesterdayViewAws.walkinSales.orderCount.displayName.isNullOrEmpty()) {
                ll_walkin_order_count_sales_display.visibility = View.GONE
            } else {
                ll_walkin_order_count_sales_display.visibility = View.VISIBLE
                walkin_order_count_sales_display.text =
                    storeDataYesterdayViewAws.walkinSales.orderCount.displayName
            }

            if (storeDataYesterdayViewAws.walkinSales?.averageTicket == null || storeDataYesterdayViewAws.walkinSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket_display.visibility = View.GONE
            } else {
                ll_average_walkin_ticket_display.visibility = View.VISIBLE
                average_walkin_ticket_display.text =
                    storeDataYesterdayViewAws.walkinSales.averageTicket.displayName
            }


            //  deliver Sales

            if (storeDataYesterdayViewAws.deliverySales?.total == null || storeDataYesterdayViewAws.deliverySales.total.displayName.isNullOrEmpty()) {
                ll_delivery_sales_display_name.visibility = View.GONE
            } else {
                ll_delivery_sales_display_name.visibility = View.VISIBLE
                delivery_sales_display_name.text =
                    storeDataYesterdayViewAws.deliverySales.total.displayName
            }
            if (storeDataYesterdayViewAws.deliverySales?.orderCount == null || storeDataYesterdayViewAws.deliverySales.orderCount.displayName.isNullOrEmpty()) {
                ll_delivery_order_count_display.visibility = View.GONE
            } else {
                ll_delivery_order_count_display.visibility = View.VISIBLE
                delivery_order_count_display.text =
                    storeDataYesterdayViewAws.deliverySales.orderCount.displayName
            }
            if (storeDataYesterdayViewAws.deliverySales?.averageTicket == null || storeDataYesterdayViewAws.deliverySales.averageTicket.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets_display.visibility = View.GONE
            } else {
                ll_delivery_average_tickets_display.visibility = View.VISIBLE
                delivery_average_tickets_display.text =
                    storeDataYesterdayViewAws.deliverySales.averageTicket.displayName
            }


            //  carryout Sales
            if (storeDataYesterdayViewAws.carryoutSales?.total == null || storeDataYesterdayViewAws.carryoutSales.total.displayName.isNullOrEmpty()) {
                ll_carry_out_display_text.visibility = View.GONE
            } else {
                ll_carry_out_display_text.visibility = View.VISIBLE
                carry_out_display_text.text =
                    storeDataYesterdayViewAws.carryoutSales.total.displayName
            }
            if (storeDataYesterdayViewAws.carryoutSales?.orderCount == null || storeDataYesterdayViewAws.carryoutSales.orderCount.displayName.isNullOrEmpty()) {
                ll_carry_outorder_count_display.visibility = View.GONE
            } else {
                ll_carry_outorder_count_display.visibility = View.VISIBLE
                carry_outorder_count_display.text =
                    storeDataYesterdayViewAws.carryoutSales.orderCount.displayName
            }

            if (storeDataYesterdayViewAws.carryoutSales?.averageTicket == null || storeDataYesterdayViewAws.carryoutSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets_display.visibility = View.GONE
            } else {
                ll_carry_average_tickets_display.visibility = View.VISIBLE
                carry_average_tickets_display.text =
                    storeDataYesterdayViewAws.carryoutSales.averageTicket.displayName
            }


            // display null check for olo pcy
            if (storeDataYesterdayViewAws.pcya?.olo == null || storeDataYesterdayViewAws.pcya.olo.displayName.isNullOrEmpty()) {
                olo_pcy_parent.visibility = View.GONE
            } else {
                olo_pcy_parent.visibility = View.GONE
                olo_pcy_display.text = storeDataYesterdayViewAws.pcya.olo.displayName
            }

            if (storeDataYesterdayViewAws.pcya?.phone == null || storeDataYesterdayViewAws.pcya.phone.displayName.isNullOrEmpty()) {
                phone_pcys_parent.visibility = View.GONE
            } else {
                phone_pcys_parent.visibility = View.VISIBLE
                phone_pcys.text =
                    storeDataYesterdayViewAws.pcya.phone.displayName
            }
            if (storeDataYesterdayViewAws.pcya?.walkin == null || storeDataYesterdayViewAws.pcya.walkin.displayName.isNullOrEmpty()) {
                walkin_pcys_parent.visibility = View.GONE
            } else {
                walkin_pcys_parent.visibility = View.VISIBLE
                walkin_pcy.text =
                    storeDataYesterdayViewAws.pcya.walkin.displayName
            }
            if (storeDataYesterdayViewAws.pcya?.delivery == null ||
                storeDataYesterdayViewAws.pcya.delivery.displayName.isNullOrEmpty()
            ) {
                delivery_pcy_parent.visibility = View.GONE
            } else {
                delivery_pcy_parent.visibility = View.VISIBLE
                delivery_pcy.text =
                    storeDataYesterdayViewAws.pcya.delivery.displayName
            }
            if (storeDataYesterdayViewAws.pcya?.carryout == null || storeDataYesterdayViewAws.pcya.carryout.displayName.isNullOrEmpty()) {
                carry_out_pcy_parent.visibility = View.GONE
            } else {
                carry_out_pcy_parent.visibility = View.VISIBLE
                carry_out_pcy.text =
                    storeDataYesterdayViewAws.pcya.carryout.displayName
            }

            /// new
            awus_sales.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewAws.actual?.amount,
                storeDataYesterdayViewAws.actual?.percentage,
                storeDataYesterdayViewAws.actual?.value
            )

            if (storeDataYesterdayViewAws.status?.toString() != null) {
                when {
                    storeDataYesterdayViewAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                        awus_sales.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewAws.status.toString() == resources.getString(R.string.under_limit) -> {
                        awus_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        awus_sales.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // scroll detect

            awus_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (awus_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.awus_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@AWUSKpiActivity,
                                    storeDataYesterdayViewAws.actual?.amount,
                                    storeDataYesterdayViewAws.actual?.percentage,
                                    storeDataYesterdayViewAws.actual?.value
                                )
                            if (storeDataYesterdayViewAws.status?.toString() != null) {
                                when {
                                    storeDataYesterdayViewAws.status.toString() == resources.getString(
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
                                    storeDataYesterdayViewAws.status.toString() == resources.getString(
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
                        y = awus_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            awus_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewAws.goal?.amount,
                storeDataYesterdayViewAws.goal?.percentage,
                storeDataYesterdayViewAws.goal?.value
            )
            awus__variance_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewAws.variance?.amount,
                storeDataYesterdayViewAws.variance?.percentage,
                storeDataYesterdayViewAws.variance?.value
            )

            showAWUSNarrativeData(storeDataYesterdayViewAws.narrative.toString())

            // PCY
            pcy_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewAws.pcya?.total?.actual?.amount,
                storeDataYesterdayViewAws.pcya?.total?.actual?.percentage,
                storeDataYesterdayViewAws.pcya?.total?.actual?.value
            )

            if (storeDataYesterdayViewAws.pcya?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewAws.pcya.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewAws.pcya.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.pcya?.olo?.actual?.amount,
                    storeDataYesterdayViewAws.pcya?.olo?.actual?.percentage,
                    storeDataYesterdayViewAws.pcya?.olo?.actual?.value
                ).isEmpty()
            ) {
                olo_pcy_percentage.visibility = View.GONE
                awus_pcya_error_olo.visibility = View.VISIBLE

            } else {
                awus_pcya_error_olo.visibility = View.GONE

                olo_pcy_percentage.visibility = View.VISIBLE
                olo_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.pcya?.olo?.actual?.amount,
                    storeDataYesterdayViewAws.pcya?.olo?.actual?.percentage,
                    storeDataYesterdayViewAws.pcya?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.pcya?.phone?.actual?.amount,
                    storeDataYesterdayViewAws.pcya?.phone?.actual?.percentage,
                    storeDataYesterdayViewAws.pcya?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_pcy_percentage.visibility = View.GONE
                awus_pcya_error_phone.visibility = View.VISIBLE


            } else {
                phone_pcy_percentage.visibility = View.VISIBLE

                awus_pcya_error_phone.visibility = View.GONE
                phone_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.pcya?.phone?.actual?.amount,
                    storeDataYesterdayViewAws.pcya?.phone?.actual?.percentage,
                    storeDataYesterdayViewAws.pcya?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.pcya?.walkin?.actual?.amount,
                    storeDataYesterdayViewAws.pcya?.walkin?.actual?.percentage,
                    storeDataYesterdayViewAws.pcya?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_percentage.visibility = View.GONE

                awus_pcya_error_walkin.visibility = View.VISIBLE
            } else {
                walkin_pcy_percentage.visibility = View.VISIBLE

                awus_pcya_error_phone.visibility = View.GONE
                walkin_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.pcya?.walkin?.actual?.amount,
                    storeDataYesterdayViewAws.pcya?.walkin?.actual?.percentage,
                    storeDataYesterdayViewAws.pcya?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.pcya?.delivery?.actual?.amount,
                    storeDataYesterdayViewAws.pcya?.delivery?.actual?.percentage,
                    storeDataYesterdayViewAws.pcya?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_percentage.visibility = View.GONE
                awus_pcya_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_pcy_percentage.visibility = View.VISIBLE

                awus_pcya_error_delivery.visibility = View.GONE
                delivery_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.pcya?.delivery?.actual?.amount,
                    storeDataYesterdayViewAws.pcya?.delivery?.actual?.percentage,
                    storeDataYesterdayViewAws.pcya?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.pcya?.carryout?.actual?.amount,
                    storeDataYesterdayViewAws.pcya?.carryout?.actual?.percentage,
                    storeDataYesterdayViewAws.pcya?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_percentage.visibility = View.GONE
                awus_pcya_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_carryout.visibility = View.GONE

                carry_out_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.pcya?.carryout?.actual?.amount,
                    storeDataYesterdayViewAws.pcya?.carryout?.actual?.percentage,
                    storeDataYesterdayViewAws.pcya?.carryout?.actual?.value
                )
            }

            // order count
            oc_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewAws.orderCount?.total?.actual?.amount,
                storeDataYesterdayViewAws.orderCount?.total?.actual?.percentage,
                storeDataYesterdayViewAws.orderCount?.total?.actual?.value
            )

            if (storeDataYesterdayViewAws.orderCount?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewAws.orderCount.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewAws.orderCount.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.green))

                    } else -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.orderCount?.olo?.actual?.amount,
                    storeDataYesterdayViewAws.orderCount?.olo?.actual?.percentage,
                    storeDataYesterdayViewAws.orderCount?.olo?.actual?.value
                ).isEmpty()
            ) {
                oc_pcy_percentage.visibility = View.GONE
                awus_oc_error_olo.visibility = View.VISIBLE

            } else {
                oc_pcy_percentage.visibility = View.VISIBLE
                awus_oc_error_olo.visibility = View.GONE

                oc_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.orderCount?.olo?.actual?.amount,
                    storeDataYesterdayViewAws.orderCount?.olo?.actual?.percentage,
                    storeDataYesterdayViewAws.orderCount?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.orderCount?.phone?.actual?.amount,
                    storeDataYesterdayViewAws.orderCount?.phone?.actual?.percentage,
                    storeDataYesterdayViewAws.orderCount?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_oc_percentage.visibility = View.GONE
                awus_oc_error_phone.visibility = View.VISIBLE

            } else {
                phone_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_phone.visibility = View.GONE

                phone_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.orderCount?.phone?.actual?.amount,
                    storeDataYesterdayViewAws.orderCount?.phone?.actual?.percentage,
                    storeDataYesterdayViewAws.orderCount?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.orderCount?.walkin?.actual?.amount,
                    storeDataYesterdayViewAws.orderCount?.walkin?.actual?.percentage,
                    storeDataYesterdayViewAws.orderCount?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_oc_percentage.visibility = View.GONE
                awus_oc_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_walkin.visibility = View.GONE

                walkin_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.orderCount?.walkin?.actual?.amount,
                    storeDataYesterdayViewAws.orderCount?.walkin?.actual?.percentage,
                    storeDataYesterdayViewAws.orderCount?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.orderCount?.delivery?.actual?.amount,
                    storeDataYesterdayViewAws.orderCount?.delivery?.actual?.percentage,
                    storeDataYesterdayViewAws.orderCount?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_oc_percentage.visibility = View.GONE
                awus_oc_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_delivery.visibility = View.GONE

                delivery_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.orderCount?.delivery?.actual?.amount,
                    storeDataYesterdayViewAws.orderCount?.delivery?.actual?.percentage,
                    storeDataYesterdayViewAws.orderCount?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.orderCount?.carryout?.actual?.amount,
                    storeDataYesterdayViewAws.orderCount?.carryout?.actual?.percentage,
                    storeDataYesterdayViewAws.orderCount?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_oc_percentage.visibility = View.GONE
                awus_oc_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_carryout.visibility = View.GONE

                carry_out_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.orderCount?.carryout?.actual?.amount,
                    storeDataYesterdayViewAws.orderCount?.carryout?.actual?.percentage,
                    storeDataYesterdayViewAws.orderCount?.carryout?.actual?.value
                )
            }

            // Average  Tickets
            average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewAws.averageTicket?.total?.actual?.amount,
                storeDataYesterdayViewAws.averageTicket?.total?.actual?.percentage,
                storeDataYesterdayViewAws.averageTicket?.total?.actual?.value
            )

            if (storeDataYesterdayViewAws.averageTicket?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.averageTicket?.olo?.actual?.amount,
                    storeDataYesterdayViewAws.averageTicket?.olo?.actual?.percentage,
                    storeDataYesterdayViewAws.averageTicket?.olo?.actual?.value
                ).isEmpty()
            ) {
                average_olo_percentage.visibility = View.GONE
                awus_avg_error_olo.visibility = View.VISIBLE

            } else {
                average_olo_percentage.visibility = View.VISIBLE
                awus_avg_error_olo.visibility = View.GONE

                average_olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.averageTicket?.olo?.actual?.amount,
                    storeDataYesterdayViewAws.averageTicket?.olo?.actual?.percentage,
                    storeDataYesterdayViewAws.averageTicket?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.averageTicket?.phone?.actual?.amount,
                    storeDataYesterdayViewAws.averageTicket?.phone?.actual?.percentage,
                    storeDataYesterdayViewAws.averageTicket?.phone?.actual?.value
                ).isEmpty()
            ) {
                average_phone_percentage.visibility = View.GONE
                awus_avg_error_phone.visibility = View.VISIBLE

            } else {
                average_phone_percentage.visibility = View.VISIBLE
                awus_avg_error_phone.visibility = View.GONE


                average_phone_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.averageTicket?.phone?.actual?.amount,
                    storeDataYesterdayViewAws.averageTicket?.phone?.actual?.percentage,
                    storeDataYesterdayViewAws.averageTicket?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.averageTicket?.walkin?.actual?.amount,
                    storeDataYesterdayViewAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataYesterdayViewAws.averageTicket?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_average_percentage.visibility = View.GONE
                awus_avg_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_average_percentage.visibility = View.VISIBLE
                awus_avg_error_walkin.visibility = View.GONE

                walkin_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.averageTicket?.walkin?.actual?.amount,
                    storeDataYesterdayViewAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataYesterdayViewAws.averageTicket?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.averageTicket?.delivery?.actual?.amount,
                    storeDataYesterdayViewAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataYesterdayViewAws.averageTicket?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_percentage.visibility = View.GONE
                awus_avg_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_percentage.visibility = View.VISIBLE
                awus_avg_error_delivery.visibility = View.GONE

                delivery_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.averageTicket?.delivery?.actual?.amount,
                    storeDataYesterdayViewAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataYesterdayViewAws.averageTicket?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.averageTicket?.carryout?.actual?.amount,
                    storeDataYesterdayViewAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataYesterdayViewAws.averageTicket?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_average_percentage.visibility = View.GONE
                awus_avg_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_average_percentage.visibility = View.VISIBLE
                awus_avg_error_carryout.visibility = View.GONE

                carry_out_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.averageTicket?.carryout?.actual?.amount,
                    storeDataYesterdayViewAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataYesterdayViewAws.averageTicket?.carryout?.actual?.value
                )
            }

            // OLO sales
            olo_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewAws.oloSales?.total?.actual?.amount,
                storeDataYesterdayViewAws.oloSales?.total?.actual?.percentage,
                storeDataYesterdayViewAws.oloSales?.total?.actual?.value
            )

            if (storeDataYesterdayViewAws.oloSales?.total?.status?.toString() != null) {

                when {
                    storeDataYesterdayViewAws.oloSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewAws.oloSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.oloSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewAws.oloSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewAws.oloSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                olo_percentage.visibility = View.GONE
                awus_olo_sales_error_olo.visibility = View.VISIBLE

            } else {
                olo_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_olo.visibility = View.GONE

                olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.oloSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewAws.oloSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewAws.oloSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.oloSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewAws.oloSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                olo_order_percentage.visibility = View.GONE
                awus_olo_sales_error_oc.visibility = View.VISIBLE

            } else {
                olo_order_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_oc.visibility = View.GONE

                olo_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.oloSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewAws.oloSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewAws.oloSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                olo_average_percentage.visibility = View.GONE
                awus_olo_sales_error_at.visibility = View.VISIBLE

            } else {
                olo_average_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_at.visibility = View.GONE

                olo_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewAws.oloSales?.averageTicket?.actual?.value
                )
            }

            // Phone sales order count
            phone_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewAws.phoneSales?.total?.actual?.amount,
                storeDataYesterdayViewAws.phoneSales?.total?.actual?.percentage,
                storeDataYesterdayViewAws.phoneSales?.total?.actual?.value
            )
            if (storeDataYesterdayViewAws.phoneSales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.phoneSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewAws.phoneSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                phone_pcys_percentage.visibility = View.GONE
                awus_phones_sales_error_olo.visibility = View.VISIBLE

            } else {
                phone_pcys_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_olo.visibility = View.GONE

                phone_pcys_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.phoneSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewAws.phoneSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewAws.phoneSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_phone_ticket_percentage.visibility = View.GONE
                awus_phones_sales_error_at.visibility = View.VISIBLE

            } else {
                average_phone_ticket_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_at.visibility = View.GONE

                average_phone_ticket_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewAws.phoneSales?.averageTicket?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewAws.phoneSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewAws.phoneSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                phone_sales_order_percentage.visibility = View.GONE
                awus_phones_sales_error_oc.visibility = View.VISIBLE

            } else {
                phone_sales_order_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_oc.visibility = View.GONE

                phone_sales_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewAws.oloSales?.orderCount?.actual?.value
                )
            }

            // walk-in sales
            walkin_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewAws.walkinSales?.total?.actual?.amount,
                storeDataYesterdayViewAws.walkinSales?.total?.actual?.percentage,
                storeDataYesterdayViewAws.walkinSales?.total?.actual?.value
            )

            if (storeDataYesterdayViewAws.walkinSales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.walkinSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewAws.walkinSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_value.visibility = View.GONE
                awus_walkins_sales_error_olo.visibility = View.VISIBLE

            } else {
                walkin_pcy_value.visibility = View.VISIBLE
                awus_walkins_sales_error_olo.visibility = View.GONE

                walkin_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.walkinSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewAws.walkinSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewAws.walkinSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                walkin_order_count_value.visibility = View.GONE
                awus_walkins_sales_error_oc.visibility = View.VISIBLE

            } else {
                walkin_order_count_value.visibility = View.VISIBLE
                awus_walkins_sales_error_oc.visibility = View.GONE

                walkin_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewAws.walkinSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewAws.walkinSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_walkin_ticket_value.visibility = View.GONE
                awus_walkins_sales_error_at.visibility = View.VISIBLE

            } else {
                average_walkin_ticket_value.visibility = View.VISIBLE
                awus_walkins_sales_error_at.visibility = View.GONE

                average_walkin_ticket_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewAws.walkinSales?.averageTicket?.actual?.value
                )
            }

            // delivery sales
            delivery_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewAws.deliverySales?.total?.actual?.amount,
                storeDataYesterdayViewAws.deliverySales?.total?.actual?.percentage,
                storeDataYesterdayViewAws.deliverySales?.total?.actual?.value
            )

            if (storeDataYesterdayViewAws.deliverySales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.deliverySales?.pcya?.actual?.amount,
                    storeDataYesterdayViewAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewAws.deliverySales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_value.visibility = View.GONE
                awus_delivery_sales_error_olo.visibility = View.VISIBLE

            } else {
                delivery_pcy_value.visibility = View.VISIBLE
                awus_delivery_sales_error_olo.visibility = View.GONE

                delivery_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.deliverySales?.pcya?.actual?.amount,
                    storeDataYesterdayViewAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewAws.deliverySales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewAws.deliverySales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                delivery_order_count_value.visibility = View.GONE
                awus_delivery_sales_error_oc.visibility = View.VISIBLE

            } else {
                delivery_order_count_value.visibility = View.VISIBLE
                awus_delivery_sales_error_oc.visibility = View.GONE

                delivery_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewAws.deliverySales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewAws.deliverySales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                delivery_average_percentage.visibility = View.GONE
                awus_delivery_sales_error_at.visibility = View.VISIBLE

            } else {
                delivery_average_percentage.visibility = View.VISIBLE
                awus_delivery_sales_error_at.visibility = View.GONE

                delivery_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewAws.deliverySales?.averageTicket?.actual?.value
                )
            }

            // carryout sales
            carryout_average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewAws.carryoutSales?.total?.actual?.amount,
                storeDataYesterdayViewAws.carryoutSales?.total?.actual?.percentage,
                storeDataYesterdayViewAws.carryoutSales?.total?.actual?.value
            )

            if (storeDataYesterdayViewAws.carryoutSales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewAws.carryoutSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_value.visibility = View.GONE
                awus_carryout_sales_error_olo.visibility = View.VISIBLE

            } else {
                carry_out_pcy_value.visibility = View.VISIBLE
                awus_carryout_sales_error_olo.visibility = View.GONE

                carry_out_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewAws.carryoutSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewAws.carryoutSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                carry_outorder_count_value.visibility = View.GONE
                awus_carryout_sales_error_oc.visibility = View.VISIBLE

            } else {
                carry_outorder_count_value.visibility = View.VISIBLE
                awus_carryout_sales_error_oc.visibility = View.GONE

                carry_outorder_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewAws.carryoutSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewAws.carryoutSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                carry_average_tickets_value.visibility = View.GONE
                awus_carryout_sales_error_at.visibility = View.VISIBLE

            } else {
                carry_average_tickets_value.visibility = View.VISIBLE
                awus_carryout_sales_error_at.visibility = View.GONE

                carry_average_tickets_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewAws.carryoutSales?.averageTicket?.actual?.value
                )
            }
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"AWUS Overview Yesterday KPI")
        }
    }

    private fun rangeViewAws(todayDetail: StorePeriodRangeKPIQuery.GeneralManager) {
        try {
            val storeDataRangeViewAws = todayDetail.kpis?.store?.period!!.sales

            Logger.info("AWUS Period Range", "AWUS Overview KPI")
            // display name
            awus_display.text = getString(R.string.awus_text)
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeViewAws?.phoneSales?.pcya?.displayName,
                phone_sales_pcys_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeViewAws?.walkinSales?.pcya?.displayName,
                walkin_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeViewAws?.deliverySales?.pcya?.displayName,
                delivery_sales_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeViewAws?.carryoutSales?.pcya?.displayName,
                carry_out_sales_pcy_parent
            )

            // pcy display name
            pcy_display_name.text =
                storeDataRangeViewAws!!.pcya?.total?.displayName ?: getString(R.string.pcy)
            phone_pcys.text =
                storeDataRangeViewAws.pcya?.phone?.displayName ?: getString(R.string.phone_pcys)
            walkin_pcy.text =
                storeDataRangeViewAws.pcya?.walkin?.displayName ?: getString(R.string.walkin_pcy)
            delivery_pcy.text =
                storeDataRangeViewAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            delivery_pcy.text =
                storeDataRangeViewAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            carry_out_pcy.text =
                storeDataRangeViewAws.pcya?.carryout?.displayName
                    ?: getString(R.string.carry_out_pcy)
            // order count display
            order_count_display.text =
                storeDataRangeViewAws.orderCount?.total?.displayName
                    ?: getString(R.string.order_count)
            olo_order_count.text =
                storeDataRangeViewAws.orderCount?.olo?.displayName
                    ?: getString(R.string.olo_order_count)
            phone_order_count.text = storeDataRangeViewAws.orderCount?.phone?.displayName
                ?: getString(R.string.phone_order_count)
            walkin_order_count.text = storeDataRangeViewAws.orderCount?.walkin?.displayName
                ?: getString(R.string.walkin_order_count)
            delivery_order_count.text = storeDataRangeViewAws.orderCount?.delivery?.displayName
                ?: getString(R.string.delivery_order_count)
            carry_outorder_count.text = storeDataRangeViewAws.orderCount?.carryout?.displayName
                ?: getString(R.string.carry_outorder_count)

            // average ticket count display
            if (storeDataRangeViewAws.averageTicket?.total == null || storeDataRangeViewAws.averageTicket.total.displayName.isNullOrEmpty()) {
                ll_average_ticket_display_name.visibility = View.GONE
            } else {
                ll_average_ticket_display_name.visibility = View.VISIBLE
                average_ticket_display_name.text =
                    storeDataRangeViewAws.averageTicket.total.displayName
            }


            if (storeDataRangeViewAws.averageTicket?.olo == null || storeDataRangeViewAws.averageTicket.olo.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket.visibility = View.GONE
            } else {
                ll_olo_average_ticket.visibility = View.VISIBLE
                olo_average_ticket.text =
                    storeDataRangeViewAws.averageTicket.olo.displayName
            }

            if (storeDataRangeViewAws.averageTicket?.phone == null || storeDataRangeViewAws.averageTicket.phone.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket.visibility = View.GONE
            } else {
                ll_average_phone_ticket.visibility = View.VISIBLE
                average_phone_ticket.text =
                    storeDataRangeViewAws.averageTicket.phone.displayName
            }

            if (storeDataRangeViewAws.averageTicket?.walkin == null || storeDataRangeViewAws.averageTicket.walkin.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket.visibility = View.GONE
            } else {
                ll_average_walkin_ticket.visibility = View.VISIBLE
                average_walkin_ticket.text =
                    storeDataRangeViewAws.averageTicket.walkin.displayName
            }

            if (storeDataRangeViewAws.averageTicket?.delivery == null || storeDataRangeViewAws.averageTicket.delivery.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets.visibility = View.GONE
            } else {
                ll_delivery_average_tickets.visibility = View.VISIBLE
                delivery_average_tickets.text =
                    storeDataRangeViewAws.averageTicket.delivery.displayName
            }

            if (storeDataRangeViewAws.averageTicket?.carryout == null || storeDataRangeViewAws.averageTicket.carryout.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets.visibility = View.GONE
            } else {
                ll_carry_average_tickets.visibility = View.VISIBLE
                carry_average_tickets.text =
                    storeDataRangeViewAws.averageTicket.carryout.displayName
            }


            // olo sales display

            if (storeDataRangeViewAws.oloSales?.total == null || storeDataRangeViewAws.oloSales.total.displayName.isNullOrEmpty()) {
                ll_olo_sales_display_name.visibility = View.GONE
            } else {
                ll_olo_sales_display_name.visibility = View.VISIBLE
                olo_sales_display_name.text =
                    storeDataRangeViewAws.oloSales.total.displayName
            }

            if (storeDataRangeViewAws.oloSales?.pcya == null || storeDataRangeViewAws.oloSales.pcya.displayName.isNullOrEmpty()) {
                olo_sales_pcy_parent.visibility = View.GONE
            } else {
                olo_sales_pcy_parent.visibility = View.VISIBLE
                olo_pcy.text =
                    storeDataRangeViewAws.oloSales.pcya.displayName
            }

            if (storeDataRangeViewAws.oloSales?.orderCount == null || storeDataRangeViewAws.oloSales.orderCount.displayName.isNullOrEmpty()) {
                ll_olo_order_count_sales_display.visibility = View.GONE
            } else {
                ll_olo_order_count_sales_display.visibility = View.VISIBLE
                olo_order_count_sales_display.text =
                    storeDataRangeViewAws.oloSales.orderCount.displayName
            }

            if (storeDataRangeViewAws.oloSales?.averageTicket == null || storeDataRangeViewAws.oloSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket_sales_display.visibility = View.GONE
            } else {
                ll_olo_average_ticket_sales_display.visibility = View.VISIBLE
                olo_average_ticket_sales_display.text =
                    storeDataRangeViewAws.oloSales.averageTicket.displayName
            }


            // phone sales display
            if (storeDataRangeViewAws.phoneSales?.total == null || storeDataRangeViewAws.phoneSales.total.displayName.isNullOrEmpty()) {
                ll_phone_sales_display_name.visibility = View.GONE
            } else {
                ll_phone_sales_display_name.visibility = View.VISIBLE
                phone_sales_display_name.text =
                    storeDataRangeViewAws.phoneSales.total.displayName
            }


            if (storeDataRangeViewAws.phoneSales?.orderCount == null || storeDataRangeViewAws.phoneSales.orderCount.displayName.isNullOrEmpty()) {
                ll_phone_order_count_display.visibility = View.GONE
            } else {
                ll_phone_order_count_display.visibility = View.VISIBLE
                phone_order_count_display.text =
                    storeDataRangeViewAws.phoneSales.orderCount.displayName
            }

            if (storeDataRangeViewAws.phoneSales?.averageTicket == null || storeDataRangeViewAws.phoneSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket_display.visibility = View.GONE
            } else {
                ll_average_phone_ticket_display.visibility = View.VISIBLE
                average_phone_ticket_display.text =
                    storeDataRangeViewAws.phoneSales.averageTicket.displayName
            }


            // walk-in Sales
            if (storeDataRangeViewAws.walkinSales?.total == null || storeDataRangeViewAws.walkinSales.total.displayName.isNullOrEmpty()) {
                ll_walkin_sales_display_name.visibility = View.GONE
            } else {
                ll_walkin_sales_display_name.visibility = View.VISIBLE
                walkin_sales_display_name.text =
                    storeDataRangeViewAws.walkinSales.total.displayName
            }

            if (storeDataRangeViewAws.walkinSales?.orderCount == null || storeDataRangeViewAws.walkinSales.orderCount.displayName.isNullOrEmpty()) {
                ll_walkin_order_count_sales_display.visibility = View.GONE
            } else {
                ll_walkin_order_count_sales_display.visibility = View.VISIBLE
                walkin_order_count_sales_display.text =
                    storeDataRangeViewAws.walkinSales.orderCount.displayName
            }


            if (storeDataRangeViewAws.walkinSales?.averageTicket == null || storeDataRangeViewAws.walkinSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket_display.visibility = View.GONE
            } else {
                ll_average_walkin_ticket_display.visibility = View.VISIBLE
                average_walkin_ticket_display.text =
                    storeDataRangeViewAws.walkinSales.averageTicket.displayName
            }


            //  deliver Sales
            if (storeDataRangeViewAws.deliverySales?.total == null || storeDataRangeViewAws.deliverySales.total.displayName.isNullOrEmpty()) {
                ll_delivery_sales_display_name.visibility = View.GONE
            } else {
                ll_delivery_sales_display_name.visibility = View.VISIBLE
                delivery_sales_display_name.text =
                    storeDataRangeViewAws.deliverySales.total.displayName
            }
            if (storeDataRangeViewAws.deliverySales?.orderCount == null || storeDataRangeViewAws.deliverySales.orderCount.displayName.isNullOrEmpty()) {
                ll_delivery_order_count_display.visibility = View.GONE
            } else {
                ll_delivery_order_count_display.visibility = View.VISIBLE
                delivery_order_count_display.text =
                    storeDataRangeViewAws.deliverySales.orderCount.displayName
            }
            if (storeDataRangeViewAws.deliverySales?.averageTicket == null || storeDataRangeViewAws.deliverySales.averageTicket.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets_display.visibility = View.GONE
            } else {
                ll_delivery_average_tickets_display.visibility = View.VISIBLE
                delivery_average_tickets_display.text =
                    storeDataRangeViewAws.deliverySales.averageTicket.displayName
            }


            //  carryout Sales
            if (storeDataRangeViewAws.carryoutSales?.total == null || storeDataRangeViewAws.carryoutSales.total.displayName.isNullOrEmpty()) {
                ll_carry_out_display_text.visibility = View.GONE
            } else {
                ll_carry_out_display_text.visibility = View.VISIBLE
                carry_out_display_text.text =
                    storeDataRangeViewAws.carryoutSales.total.displayName
            }
            if (storeDataRangeViewAws.carryoutSales?.orderCount == null || storeDataRangeViewAws.carryoutSales.orderCount.displayName.isNullOrEmpty()) {
                ll_carry_outorder_count_display.visibility = View.GONE
            } else {
                ll_carry_outorder_count_display.visibility = View.VISIBLE
                carry_outorder_count_display.text =
                    storeDataRangeViewAws.carryoutSales.orderCount.displayName
            }

            if (storeDataRangeViewAws.carryoutSales?.averageTicket == null || storeDataRangeViewAws.carryoutSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets_display.visibility = View.GONE
            } else {
                ll_carry_average_tickets_display.visibility = View.VISIBLE
                carry_average_tickets_display.text =
                    storeDataRangeViewAws.carryoutSales.averageTicket.displayName
            }


            // display null check for olo pcy

            if (storeDataRangeViewAws.pcya?.olo == null || storeDataRangeViewAws.pcya.olo.displayName.isNullOrEmpty()) {
                olo_pcy_parent.visibility = View.GONE
            } else {
                olo_pcy_parent.visibility = View.GONE
                olo_pcy_display.text = storeDataRangeViewAws.pcya.olo.displayName
            }

            if (storeDataRangeViewAws.pcya?.phone == null || storeDataRangeViewAws.pcya.phone.displayName.isNullOrEmpty()) {
                phone_pcys_parent.visibility = View.GONE
            } else {
                phone_pcys_parent.visibility = View.VISIBLE
                phone_pcys.text =
                    storeDataRangeViewAws.pcya.phone.displayName
            }
            if (storeDataRangeViewAws.pcya?.walkin == null || storeDataRangeViewAws.pcya.walkin.displayName.isNullOrEmpty()) {
                walkin_pcys_parent.visibility = View.GONE
            } else {
                walkin_pcys_parent.visibility = View.VISIBLE
                walkin_pcy.text =
                    storeDataRangeViewAws.pcya.walkin.displayName
            }
            if (storeDataRangeViewAws.pcya?.delivery == null ||
                storeDataRangeViewAws.pcya.delivery.displayName.isNullOrEmpty()
            ) {
                delivery_pcy_parent.visibility = View.GONE
            } else {
                delivery_pcy_parent.visibility = View.VISIBLE
                delivery_pcy.text =
                    storeDataRangeViewAws.pcya.delivery.displayName
            }
            if (storeDataRangeViewAws.pcya?.carryout == null || storeDataRangeViewAws.pcya.carryout.displayName.isNullOrEmpty()) {
                carry_out_pcy_parent.visibility = View.GONE
            } else {
                carry_out_pcy_parent.visibility = View.VISIBLE
                carry_out_pcy.text =
                    storeDataRangeViewAws.pcya.carryout.displayName
            }

            /// new
            awus_sales.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewAws.actual?.amount,
                storeDataRangeViewAws.actual?.percentage,
                storeDataRangeViewAws.actual?.value
            )

            if (storeDataRangeViewAws.status?.toString() != null) {
                when {
                    storeDataRangeViewAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                        awus_sales.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewAws.status.toString() == resources.getString(R.string.under_limit) -> {
                        awus_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        awus_sales.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // scroll detect

            awus_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (awus_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.awus_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@AWUSKpiActivity,
                                    storeDataRangeViewAws.actual?.amount,
                                    storeDataRangeViewAws.actual?.percentage,
                                    storeDataRangeViewAws.actual?.value
                                )

                            if (storeDataRangeViewAws.status?.toString() != null) {
                                when {
                                    storeDataRangeViewAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                                        level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                        level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                            0,
                                            0,
                                            R.drawable.red_circle,
                                            0
                                        )
                                    }
                                    storeDataRangeViewAws.status.toString() == resources.getString(R.string.under_limit) -> {
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
                        y = awus_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            awus_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewAws.goal?.amount,
                storeDataRangeViewAws.goal?.percentage,
                storeDataRangeViewAws.goal?.value
            )
            awus__variance_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewAws.variance?.amount,
                storeDataRangeViewAws.variance?.percentage,
                storeDataRangeViewAws.variance?.value
            )

            showAWUSNarrativeData(storeDataRangeViewAws.narrative.toString())
            // PCY
            pcy_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewAws.pcya?.total?.actual?.amount,
                storeDataRangeViewAws.pcya?.total?.actual?.percentage,
                storeDataRangeViewAws.pcya?.total?.actual?.value
            )

            if (storeDataRangeViewAws.pcya?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewAws.pcya.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewAws.pcya.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.pcya?.olo?.actual?.amount,
                    storeDataRangeViewAws.pcya?.olo?.actual?.percentage,
                    storeDataRangeViewAws.pcya?.olo?.actual?.value
                ).isEmpty()
            ) {
                olo_pcy_percentage.visibility = View.GONE
                awus_pcya_error_olo.visibility = View.VISIBLE

            } else {
                olo_pcy_percentage.visibility = View.VISIBLE

                olo_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.pcya?.olo?.actual?.amount,
                    storeDataRangeViewAws.pcya?.olo?.actual?.percentage,
                    storeDataRangeViewAws.pcya?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.pcya?.phone?.actual?.amount,
                    storeDataRangeViewAws.pcya?.phone?.actual?.percentage,
                    storeDataRangeViewAws.pcya?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_pcy_percentage.visibility = View.GONE
                awus_pcya_error_phone.visibility = View.VISIBLE

            } else {
                phone_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                phone_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.pcya?.phone?.actual?.amount,
                    storeDataRangeViewAws.pcya?.phone?.actual?.percentage,
                    storeDataRangeViewAws.pcya?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.pcya?.walkin?.actual?.amount,
                    storeDataRangeViewAws.pcya?.walkin?.actual?.percentage,
                    storeDataRangeViewAws.pcya?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_percentage.visibility = View.GONE
                awus_pcya_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                walkin_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.pcya?.walkin?.actual?.amount,
                    storeDataRangeViewAws.pcya?.walkin?.actual?.percentage,
                    storeDataRangeViewAws.pcya?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.pcya?.delivery?.actual?.amount,
                    storeDataRangeViewAws.pcya?.delivery?.actual?.percentage,
                    storeDataRangeViewAws.pcya?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_percentage.visibility = View.GONE
                awus_pcya_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_delivery.visibility = View.GONE

                delivery_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.pcya?.delivery?.actual?.amount,
                    storeDataRangeViewAws.pcya?.delivery?.actual?.percentage,
                    storeDataRangeViewAws.pcya?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.pcya?.carryout?.actual?.amount,
                    storeDataRangeViewAws.pcya?.carryout?.actual?.percentage,
                    storeDataRangeViewAws.pcya?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_percentage.visibility = View.GONE
                awus_pcya_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_carryout.visibility = View.GONE

                carry_out_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.pcya?.carryout?.actual?.amount,
                    storeDataRangeViewAws.pcya?.carryout?.actual?.percentage,
                    storeDataRangeViewAws.pcya?.carryout?.actual?.value
                )
            }

            // order count
            oc_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewAws.orderCount?.total?.actual?.amount,
                storeDataRangeViewAws.orderCount?.total?.actual?.percentage,
                storeDataRangeViewAws.orderCount?.total?.actual?.value
            )

            if (storeDataRangeViewAws.orderCount?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewAws.orderCount.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewAws.orderCount.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.green))

                    } else -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.orderCount?.olo?.actual?.amount,
                    storeDataRangeViewAws.orderCount?.olo?.actual?.percentage,
                    storeDataRangeViewAws.orderCount?.olo?.actual?.value
                ).isEmpty()
            ) {
                oc_pcy_percentage.visibility = View.GONE
                awus_oc_error_olo.visibility = View.VISIBLE

            } else {
                oc_pcy_percentage.visibility = View.VISIBLE
                awus_oc_error_olo.visibility = View.GONE

                oc_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.orderCount?.olo?.actual?.amount,
                    storeDataRangeViewAws.orderCount?.olo?.actual?.percentage,
                    storeDataRangeViewAws.orderCount?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.orderCount?.phone?.actual?.amount,
                    storeDataRangeViewAws.orderCount?.phone?.actual?.percentage,
                    storeDataRangeViewAws.orderCount?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_oc_percentage.visibility = View.GONE
                awus_oc_error_phone.visibility = View.VISIBLE

            } else {
                phone_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_phone.visibility = View.GONE

                phone_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.orderCount?.phone?.actual?.amount,
                    storeDataRangeViewAws.orderCount?.phone?.actual?.percentage,
                    storeDataRangeViewAws.orderCount?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.orderCount?.walkin?.actual?.amount,
                    storeDataRangeViewAws.orderCount?.walkin?.actual?.percentage,
                    storeDataRangeViewAws.orderCount?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_oc_percentage.visibility = View.GONE
                awus_oc_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_walkin.visibility = View.GONE

                walkin_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.orderCount?.walkin?.actual?.amount,
                    storeDataRangeViewAws.orderCount?.walkin?.actual?.percentage,
                    storeDataRangeViewAws.orderCount?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.orderCount?.delivery?.actual?.amount,
                    storeDataRangeViewAws.orderCount?.delivery?.actual?.percentage,
                    storeDataRangeViewAws.orderCount?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_oc_percentage.visibility = View.GONE
                awus_oc_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_delivery.visibility = View.GONE

                delivery_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.orderCount?.delivery?.actual?.amount,
                    storeDataRangeViewAws.orderCount?.delivery?.actual?.percentage,
                    storeDataRangeViewAws.orderCount?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.orderCount?.carryout?.actual?.amount,
                    storeDataRangeViewAws.orderCount?.carryout?.actual?.percentage,
                    storeDataRangeViewAws.orderCount?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_oc_percentage.visibility = View.GONE
                awus_oc_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_carryout.visibility = View.GONE

                carry_out_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.orderCount?.carryout?.actual?.amount,
                    storeDataRangeViewAws.orderCount?.carryout?.actual?.percentage,
                    storeDataRangeViewAws.orderCount?.carryout?.actual?.value
                )
            }

            // Average  Tickets
            average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewAws.averageTicket?.total?.actual?.amount,
                storeDataRangeViewAws.averageTicket?.total?.actual?.percentage,
                storeDataRangeViewAws.averageTicket?.total?.actual?.value
            )

            if (storeDataRangeViewAws.averageTicket?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.averageTicket?.olo?.actual?.amount,
                    storeDataRangeViewAws.averageTicket?.olo?.actual?.percentage,
                    storeDataRangeViewAws.averageTicket?.olo?.actual?.value
                ).isEmpty()
            ) {
                average_olo_percentage.visibility = View.GONE
                awus_avg_error_olo.visibility = View.VISIBLE

            } else {
                average_olo_percentage.visibility = View.VISIBLE
                awus_avg_error_olo.visibility = View.GONE

                average_olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.averageTicket?.olo?.actual?.amount,
                    storeDataRangeViewAws.averageTicket?.olo?.actual?.percentage,
                    storeDataRangeViewAws.averageTicket?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.averageTicket?.phone?.actual?.amount,
                    storeDataRangeViewAws.averageTicket?.phone?.actual?.percentage,
                    storeDataRangeViewAws.averageTicket?.phone?.actual?.value
                ).isEmpty()
            ) {
                average_phone_percentage.visibility = View.GONE
                awus_avg_error_phone.visibility = View.VISIBLE

            } else {
                average_phone_percentage.visibility = View.VISIBLE
                awus_avg_error_phone.visibility = View.GONE

                average_phone_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.averageTicket?.phone?.actual?.amount,
                    storeDataRangeViewAws.averageTicket?.phone?.actual?.percentage,
                    storeDataRangeViewAws.averageTicket?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.averageTicket?.walkin?.actual?.amount,
                    storeDataRangeViewAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataRangeViewAws.averageTicket?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_average_percentage.visibility = View.GONE
                awus_avg_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_average_percentage.visibility = View.VISIBLE
                awus_avg_error_walkin.visibility = View.GONE

                walkin_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.averageTicket?.walkin?.actual?.amount,
                    storeDataRangeViewAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataRangeViewAws.averageTicket?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.averageTicket?.delivery?.actual?.amount,
                    storeDataRangeViewAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataRangeViewAws.averageTicket?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_percentage.visibility = View.GONE
                awus_avg_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_percentage.visibility = View.VISIBLE
                awus_avg_error_delivery.visibility = View.GONE

                delivery_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.averageTicket?.delivery?.actual?.amount,
                    storeDataRangeViewAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataRangeViewAws.averageTicket?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.averageTicket?.carryout?.actual?.amount,
                    storeDataRangeViewAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataRangeViewAws.averageTicket?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_average_percentage.visibility = View.GONE
                awus_avg_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_average_percentage.visibility = View.VISIBLE
                awus_avg_error_carryout.visibility = View.GONE

                carry_out_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.averageTicket?.carryout?.actual?.amount,
                    storeDataRangeViewAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataRangeViewAws.averageTicket?.carryout?.actual?.value
                )
            }

            // OLO sales
            olo_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewAws.oloSales?.total?.actual?.amount,
                storeDataRangeViewAws.oloSales?.total?.actual?.percentage,
                storeDataRangeViewAws.oloSales?.total?.actual?.value
            )

            if (storeDataRangeViewAws.oloSales?.total?.status?.toString() != null) {

                when {
                    storeDataRangeViewAws.oloSales.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewAws.oloSales.total.status.toString() == resources.getString(R.string.under_limit) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.oloSales?.pcya?.actual?.amount,
                    storeDataRangeViewAws.oloSales?.pcya?.actual?.percentage,
                    storeDataRangeViewAws.oloSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                olo_percentage.visibility = View.GONE
                awus_olo_sales_error_olo.visibility = View.VISIBLE

            } else {
                olo_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_olo.visibility = View.GONE

                olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.oloSales?.pcya?.actual?.amount,
                    storeDataRangeViewAws.oloSales?.pcya?.actual?.percentage,
                    storeDataRangeViewAws.oloSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.oloSales?.orderCount?.actual?.amount,
                    storeDataRangeViewAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewAws.oloSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                olo_order_percentage.visibility = View.GONE
                awus_olo_sales_error_oc.visibility = View.VISIBLE

            } else {
                olo_order_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_oc.visibility = View.GONE

                olo_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.oloSales?.orderCount?.actual?.amount,
                    storeDataRangeViewAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewAws.oloSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewAws.oloSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                olo_average_percentage.visibility = View.GONE
                awus_olo_sales_error_at.visibility = View.VISIBLE

            } else {
                olo_average_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_at.visibility = View.GONE

                olo_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewAws.oloSales?.averageTicket?.actual?.value
                )
            }

            // Phone sales order count
            phone_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewAws.phoneSales?.total?.actual?.amount,
                storeDataRangeViewAws.phoneSales?.total?.actual?.percentage,
                storeDataRangeViewAws.phoneSales?.total?.actual?.value
            )
            if (storeDataRangeViewAws.phoneSales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.phoneSales?.pcya?.actual?.amount,
                    storeDataRangeViewAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataRangeViewAws.phoneSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                phone_pcys_percentage.visibility = View.GONE
                awus_phones_sales_error_olo.visibility = View.VISIBLE

            } else {
                phone_pcys_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_olo.visibility = View.GONE

                phone_pcys_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.phoneSales?.pcya?.actual?.amount,
                    storeDataRangeViewAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataRangeViewAws.phoneSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewAws.phoneSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_phone_ticket_percentage.visibility = View.GONE
                awus_phones_sales_error_at.visibility = View.VISIBLE

            } else {
                average_phone_ticket_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_at.visibility = View.GONE

                average_phone_ticket_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewAws.phoneSales?.averageTicket?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataRangeViewAws.phoneSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewAws.phoneSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                phone_sales_order_percentage.visibility = View.GONE
                awus_phones_sales_error_oc.visibility = View.VISIBLE

            } else {
                phone_sales_order_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_oc.visibility = View.GONE

                phone_sales_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataRangeViewAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewAws.oloSales?.orderCount?.actual?.value
                )
            }

            // walk-in sales
            walkin_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewAws.walkinSales?.total?.actual?.amount,
                storeDataRangeViewAws.walkinSales?.total?.actual?.percentage,
                storeDataRangeViewAws.walkinSales?.total?.actual?.value
            )

            if (storeDataRangeViewAws.walkinSales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.walkinSales?.pcya?.actual?.amount,
                    storeDataRangeViewAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataRangeViewAws.walkinSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_value.visibility = View.GONE
                awus_walkins_sales_error_olo.visibility = View.VISIBLE

            } else {
                walkin_pcy_value.visibility = View.VISIBLE
                awus_walkins_sales_error_olo.visibility = View.GONE

                walkin_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.walkinSales?.pcya?.actual?.amount,
                    storeDataRangeViewAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataRangeViewAws.walkinSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataRangeViewAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewAws.walkinSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                walkin_order_count_value.visibility = View.GONE
                awus_walkins_sales_error_oc.visibility = View.VISIBLE

            } else {
                walkin_order_count_value.visibility = View.VISIBLE
                awus_walkins_sales_error_oc.visibility = View.GONE

                walkin_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataRangeViewAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewAws.walkinSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewAws.walkinSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_walkin_ticket_value.visibility = View.GONE
                awus_walkins_sales_error_at.visibility = View.VISIBLE

            } else {
                average_walkin_ticket_value.visibility = View.VISIBLE
                awus_walkins_sales_error_at.visibility = View.GONE

                average_walkin_ticket_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewAws.walkinSales?.averageTicket?.actual?.value
                )
            }

            // delivery sales
            delivery_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewAws.deliverySales?.total?.actual?.amount,
                storeDataRangeViewAws.deliverySales?.total?.actual?.percentage,
                storeDataRangeViewAws.deliverySales?.total?.actual?.value
            )

            if (storeDataRangeViewAws.deliverySales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.deliverySales?.pcya?.actual?.amount,
                    storeDataRangeViewAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataRangeViewAws.deliverySales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_value.visibility = View.GONE
                awus_delivery_sales_error_olo.visibility = View.VISIBLE

            } else {
                delivery_pcy_value.visibility = View.VISIBLE
                awus_delivery_sales_error_olo.visibility = View.GONE

                delivery_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.deliverySales?.pcya?.actual?.amount,
                    storeDataRangeViewAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataRangeViewAws.deliverySales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataRangeViewAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataRangeViewAws.deliverySales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                delivery_order_count_value.visibility = View.GONE
                awus_delivery_sales_error_oc.visibility = View.VISIBLE

            } else {
                delivery_order_count_value.visibility = View.VISIBLE
                awus_delivery_sales_error_oc.visibility = View.GONE

                delivery_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataRangeViewAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataRangeViewAws.deliverySales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataRangeViewAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewAws.deliverySales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                delivery_average_percentage.visibility = View.GONE
                awus_delivery_sales_error_at.visibility = View.VISIBLE

            } else {
                delivery_average_percentage.visibility = View.VISIBLE
                awus_delivery_sales_error_at.visibility = View.GONE

                delivery_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataRangeViewAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewAws.deliverySales?.averageTicket?.actual?.value
                )
            }

            // carryout sales
            carryout_average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewAws.carryoutSales?.total?.actual?.amount,
                storeDataRangeViewAws.carryoutSales?.total?.actual?.percentage,
                storeDataRangeViewAws.carryoutSales?.total?.actual?.value
            )

            if (storeDataRangeViewAws.carryoutSales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataRangeViewAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataRangeViewAws.carryoutSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_value.visibility = View.GONE
                awus_carryout_sales_error_olo.visibility = View.VISIBLE

            } else {
                carry_out_pcy_value.visibility = View.VISIBLE
                awus_carryout_sales_error_olo.visibility = View.GONE

                carry_out_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataRangeViewAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataRangeViewAws.carryoutSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataRangeViewAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewAws.carryoutSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                carry_outorder_count_value.visibility = View.GONE
                awus_carryout_sales_error_oc.visibility = View.VISIBLE

            } else {
                carry_outorder_count_value.visibility = View.VISIBLE
                awus_carryout_sales_error_oc.visibility = View.GONE

                carry_outorder_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataRangeViewAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewAws.carryoutSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewAws.carryoutSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                carry_average_tickets_value.visibility = View.GONE
                awus_carryout_sales_error_at.visibility = View.VISIBLE

            } else {
                carry_average_tickets_value.visibility = View.VISIBLE
                awus_carryout_sales_error_at.visibility = View.GONE

                carry_average_tickets_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewAws.carryoutSales?.averageTicket?.actual?.value
                )
            }
        }
        catch (e: Exception) {
            Logger.error(e.message.toString(),"AWUS Overview Period Range KPI")
        }
    }


// CEO view

    private fun todayViewCEOAws(todayDetail: CEOOverviewTodayQuery.Ceo) {

        val storeDataTodayViewCEOAws = todayDetail.kpis?.supervisors?.stores?.today!!.sales

        Logger.info("AWUS Today", "AWUS Overview KPI")


        // display name
        awus_display.text = storeDataTodayViewCEOAws!!.displayName ?: getString(R.string.awus_text)
        // pcy display name
        pcy_display_name.text =
            storeDataTodayViewCEOAws.pcya?.total?.displayName ?: getString(R.string.pcy)
        olo_pcy_display.text =
            storeDataTodayViewCEOAws.pcya?.olo?.displayName ?: getString(R.string.olo_pcy)
        phone_pcys.text =
            storeDataTodayViewCEOAws.pcya?.phone?.displayName ?: getString(R.string.phone_pcys)
        walkin_pcy.text =
            storeDataTodayViewCEOAws.pcya?.walkin?.displayName ?: getString(R.string.walkin_pcy)
        delivery_pcy.text =
            storeDataTodayViewCEOAws.pcya?.delivery?.displayName ?: getString(R.string.delivery_pcy)
        delivery_pcy.text =
            storeDataTodayViewCEOAws.pcya?.delivery?.displayName ?: getString(R.string.delivery_pcy)
        carry_out_pcy.text =
            storeDataTodayViewCEOAws.pcya?.carryout?.displayName
                ?: getString(R.string.carry_out_pcy)
        // order count display
        order_count_display.text =
            storeDataTodayViewCEOAws.orderCount?.total?.displayName
                ?: getString(R.string.order_count)
        olo_order_count.text =
            storeDataTodayViewCEOAws.orderCount?.olo?.displayName
                ?: getString(R.string.olo_order_count)
        phone_order_count.text = storeDataTodayViewCEOAws.orderCount?.phone?.displayName
            ?: getString(R.string.phone_order_count)
        walkin_order_count.text = storeDataTodayViewCEOAws.orderCount?.walkin?.displayName
            ?: getString(R.string.walkin_order_count)
        delivery_order_count.text = storeDataTodayViewCEOAws.orderCount?.delivery?.displayName
            ?: getString(R.string.delivery_order_count)
        carry_outorder_count.text = storeDataTodayViewCEOAws.orderCount?.carryout?.displayName
            ?: getString(R.string.carry_outorder_count)

        // average ticket count display
        if (storeDataTodayViewCEOAws.averageTicket?.total == null || storeDataTodayViewCEOAws.averageTicket.total.displayName.isNullOrEmpty()) {
            ll_average_ticket_display_name.visibility = View.GONE
        } else {
            ll_average_ticket_display_name.visibility = View.VISIBLE
            average_ticket_display_name.text =
                storeDataTodayViewCEOAws.averageTicket.total.displayName
        }


        if (storeDataTodayViewCEOAws.averageTicket?.olo == null || storeDataTodayViewCEOAws.averageTicket.olo.displayName.isNullOrEmpty()) {
            ll_olo_average_ticket.visibility = View.GONE
        } else {
            ll_olo_average_ticket.visibility = View.VISIBLE
            olo_average_ticket.text =
                storeDataTodayViewCEOAws.averageTicket.olo.displayName
        }

        if (storeDataTodayViewCEOAws.averageTicket?.phone == null || storeDataTodayViewCEOAws.averageTicket.phone.displayName.isNullOrEmpty()) {
            ll_average_phone_ticket.visibility = View.GONE
        } else {
            ll_average_phone_ticket.visibility = View.VISIBLE
            average_phone_ticket.text =
                storeDataTodayViewCEOAws.averageTicket.phone.displayName
        }

        if (storeDataTodayViewCEOAws.averageTicket?.walkin == null || storeDataTodayViewCEOAws.averageTicket.walkin.displayName.isNullOrEmpty()) {
            ll_average_walkin_ticket.visibility = View.GONE
        } else {
            ll_average_walkin_ticket.visibility = View.VISIBLE
            average_walkin_ticket.text =
                storeDataTodayViewCEOAws.averageTicket.walkin.displayName
        }

        if (storeDataTodayViewCEOAws.averageTicket?.delivery == null || storeDataTodayViewCEOAws.averageTicket.delivery.displayName.isNullOrEmpty()) {
            ll_delivery_average_tickets.visibility = View.GONE
        } else {
            ll_delivery_average_tickets.visibility = View.VISIBLE
            delivery_average_tickets.text =
                storeDataTodayViewCEOAws.averageTicket.delivery.displayName
        }

        if (storeDataTodayViewCEOAws.averageTicket?.carryout == null || storeDataTodayViewCEOAws.averageTicket.carryout.displayName.isNullOrEmpty()) {
            ll_carry_average_tickets.visibility = View.GONE
        } else {
            ll_carry_average_tickets.visibility = View.VISIBLE
            carry_average_tickets.text =
                storeDataTodayViewCEOAws.averageTicket.carryout.displayName
        }


        // olo sales display
        if (storeDataTodayViewCEOAws.oloSales?.total == null || storeDataTodayViewCEOAws.oloSales.total.displayName.isNullOrEmpty()) {
            ll_olo_sales_display_name.visibility = View.GONE
        } else {
            ll_olo_sales_display_name.visibility = View.VISIBLE
            olo_sales_display_name.text =
                storeDataTodayViewCEOAws.oloSales.total.displayName
        }

        if (storeDataTodayViewCEOAws.oloSales?.pcya == null || storeDataTodayViewCEOAws.oloSales.pcya.displayName.isNullOrEmpty()) {
            olo_sales_pcy_parent.visibility = View.GONE
        } else {
            olo_sales_pcy_parent.visibility = View.VISIBLE
            olo_pcy.text =
                storeDataTodayViewCEOAws.oloSales.pcya.displayName
        }

        if (storeDataTodayViewCEOAws.oloSales?.orderCount == null || storeDataTodayViewCEOAws.oloSales.orderCount.displayName.isNullOrEmpty()) {
            ll_olo_order_count_sales_display.visibility = View.GONE
        } else {
            ll_olo_order_count_sales_display.visibility = View.VISIBLE
            olo_order_count_sales_display.text =
                storeDataTodayViewCEOAws.oloSales.orderCount.displayName
        }

        if (storeDataTodayViewCEOAws.oloSales?.averageTicket == null || storeDataTodayViewCEOAws.oloSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_olo_average_ticket_sales_display.visibility = View.GONE
        } else {
            ll_olo_average_ticket_sales_display.visibility = View.VISIBLE
            olo_average_ticket_sales_display.text =
                storeDataTodayViewCEOAws.oloSales.averageTicket.displayName
        }


        // phone sales display
        if (storeDataTodayViewCEOAws.phoneSales?.total == null || storeDataTodayViewCEOAws.phoneSales.total.displayName.isNullOrEmpty()) {
            ll_phone_sales_display_name.visibility = View.GONE
        } else {
            ll_phone_sales_display_name.visibility = View.VISIBLE
            phone_sales_display_name.text =
                storeDataTodayViewCEOAws.phoneSales.total.displayName
        }



        if (storeDataTodayViewCEOAws.phoneSales?.orderCount == null || storeDataTodayViewCEOAws.phoneSales.orderCount.displayName.isNullOrEmpty()) {
            ll_phone_order_count_display.visibility = View.GONE
        } else {
            ll_phone_order_count_display.visibility = View.VISIBLE
            phone_order_count_display.text =
                storeDataTodayViewCEOAws.phoneSales.orderCount.displayName
        }

        if (storeDataTodayViewCEOAws.phoneSales?.averageTicket == null || storeDataTodayViewCEOAws.phoneSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_average_phone_ticket_display.visibility = View.GONE
        } else {
            ll_average_phone_ticket_display.visibility = View.VISIBLE
            average_phone_ticket_display.text =
                storeDataTodayViewCEOAws.phoneSales.averageTicket.displayName
        }


        // walk-in Sales
        if (storeDataTodayViewCEOAws.walkinSales?.total == null || storeDataTodayViewCEOAws.walkinSales.total.displayName.isNullOrEmpty()) {
            ll_walkin_sales_display_name.visibility = View.GONE
        } else {
            ll_walkin_sales_display_name.visibility = View.VISIBLE
            walkin_sales_display_name.text =
                storeDataTodayViewCEOAws.walkinSales.total.displayName
        }

        if (storeDataTodayViewCEOAws.walkinSales?.orderCount == null || storeDataTodayViewCEOAws.walkinSales.orderCount.displayName.isNullOrEmpty()) {
            ll_walkin_order_count_sales_display.visibility = View.GONE
        } else {
            ll_walkin_order_count_sales_display.visibility = View.VISIBLE
            walkin_order_count_sales_display.text =
                storeDataTodayViewCEOAws.walkinSales.orderCount.displayName
        }

        if (storeDataTodayViewCEOAws.walkinSales?.averageTicket == null || storeDataTodayViewCEOAws.walkinSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_average_walkin_ticket_display.visibility = View.GONE
        } else {
            ll_average_walkin_ticket_display.visibility = View.VISIBLE
            average_walkin_ticket_display.text =
                storeDataTodayViewCEOAws.walkinSales.averageTicket.displayName
        }


        //  deliver Sales
        if (storeDataTodayViewCEOAws.deliverySales?.total == null || storeDataTodayViewCEOAws.deliverySales.total.displayName.isNullOrEmpty()) {
            ll_delivery_sales_display_name.visibility = View.GONE
        } else {
            ll_delivery_sales_display_name.visibility = View.VISIBLE
            delivery_sales_display_name.text =
                storeDataTodayViewCEOAws.deliverySales.total.displayName
        }
        if (storeDataTodayViewCEOAws.deliverySales?.orderCount == null || storeDataTodayViewCEOAws.deliverySales.orderCount.displayName.isNullOrEmpty()) {
            ll_delivery_order_count_display.visibility = View.GONE
        } else {
            ll_delivery_order_count_display.visibility = View.VISIBLE
            delivery_order_count_display.text =
                storeDataTodayViewCEOAws.deliverySales.orderCount.displayName
        }
        if (storeDataTodayViewCEOAws.deliverySales?.averageTicket == null || storeDataTodayViewCEOAws.deliverySales.averageTicket.displayName.isNullOrEmpty()) {
            ll_delivery_average_tickets_display.visibility = View.GONE
        } else {
            ll_delivery_average_tickets_display.visibility = View.VISIBLE
            delivery_average_tickets_display.text =
                storeDataTodayViewCEOAws.deliverySales.averageTicket.displayName
        }


        //  carryout Sales
         if (storeDataTodayViewCEOAws.carryoutSales?.total == null || storeDataTodayViewCEOAws.carryoutSales.total.displayName.isNullOrEmpty()) {
                ll_carry_out_display_text.visibility = View.GONE
            } else {
                ll_carry_out_display_text.visibility = View.VISIBLE
                carry_out_display_text.text =
                    storeDataTodayViewCEOAws.carryoutSales.total.displayName
            }
            if (storeDataTodayViewCEOAws.carryoutSales?.orderCount == null || storeDataTodayViewCEOAws.carryoutSales.orderCount.displayName.isNullOrEmpty()) {
                ll_carry_outorder_count_display.visibility = View.GONE
            } else {
                ll_carry_outorder_count_display.visibility = View.VISIBLE
                carry_outorder_count_display.text =
                    storeDataTodayViewCEOAws.carryoutSales.orderCount.displayName
            }

            if (storeDataTodayViewCEOAws.carryoutSales?.averageTicket == null || storeDataTodayViewCEOAws.carryoutSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets_display.visibility = View.GONE
            } else {
                ll_carry_average_tickets_display.visibility = View.VISIBLE
                carry_average_tickets_display.text =
                    storeDataTodayViewCEOAws.carryoutSales.averageTicket.displayName
            }



        if (storeDataTodayViewCEOAws.actual?.value?.isNaN() == false && storeDataTodayViewCEOAws.status?.toString() != null) {
            awus_sales.text = getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    storeDataTodayViewCEOAws.actual.value
                )
            )
            when {
                storeDataTodayViewCEOAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                    awus_sales.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewCEOAws.status.toString() == resources.getString(R.string.under_limit) -> {
                    awus_sales.setTextColor(getColor(R.color.green))
                }
                else -> {
                    awus_sales.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        // scroll detect

        awus_scroll_parent.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 0f
                override fun onScrollChanged() {
                    if (awus_scroll_parent.scrollY > y) {
                        parent_data_on_scroll_linear.visibility = View.VISIBLE
                        parent_data_on_scroll_view.visibility = View.VISIBLE
                        level_two_scroll_data_action.text =
                            storeDataTodayViewCEOAws.displayName ?: getString(R.string.awus_text)
                        if (storeDataTodayViewCEOAws.actual?.value?.isNaN() == false && storeDataTodayViewCEOAws.status?.toString() != null) {
                            level_two_scroll_data_action_value.text =
                                getString(R.string.dollar_text).plus(
                                    Validation().dollarFormatting(
                                        storeDataTodayViewCEOAws.actual.value
                                    )
                                )
                            when {
                                storeDataTodayViewCEOAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.red_circle,
                                        0
                                    )
                                }
                                storeDataTodayViewCEOAws.status.toString() == resources.getString(R.string.under_limit) -> {
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
                    y = awus_scroll_parent.scrollY.toFloat()
                    if (y <= 0) {
                        parent_data_on_scroll_linear.visibility = View.INVISIBLE
                        parent_data_on_scroll_view.visibility = View.GONE
                    }
                }
            })

        showAWUSNarrativeData(storeDataTodayViewCEOAws.narrative.toString())

        awus_goal_value.text =
            if (storeDataTodayViewCEOAws.goal?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(storeDataTodayViewCEOAws.goal.value)
            ) else ""
        awus__variance_value.text =
            if (storeDataTodayViewCEOAws.variance?.value?.isNaN() == false) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(storeDataTodayViewCEOAws.variance.value)
            ) else ""

        // PCY
        if (storeDataTodayViewCEOAws.pcya?.total?.actual?.percentage?.isNaN() == false && storeDataTodayViewCEOAws.pcya.total.status?.toString() != null) {
            pcy_percentage.text =
                Validation().ignoreZeroAfterDecimal(storeDataTodayViewCEOAws.pcya.total.actual.percentage)
                    .plus(
                        getString(
                            R.string.percentage_text
                        )
                    )
            when {
                storeDataTodayViewCEOAws.pcya.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    pcy_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewCEOAws.pcya.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    pcy_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    pcy_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewCEOAws.pcya?.olo?.actual?.percentage?.isNaN() == false) {
            olo_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.pcya.olo.actual.percentage
            )).plus(getString(R.string.percentage_text))
            olo_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_olo.visibility = View.GONE

        } else {
            olo_pcy_percentage.visibility = View.GONE
            awus_pcya_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.pcya?.phone?.actual?.percentage?.isNaN() == false) {
            phone_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.pcya.phone.actual.percentage
            )).plus(getString(R.string.percentage_text))
            phone_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_phone.visibility = View.GONE

        } else {
            phone_pcy_percentage.visibility = View.GONE
            awus_pcya_error_phone.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.pcya?.walkin?.actual?.percentage?.isNaN() == false) {
            walkin_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.pcya.walkin.actual.percentage
            )).plus(getString(R.string.percentage_text))
            walkin_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_phone.visibility = View.GONE

        } else {
            walkin_pcy_percentage.visibility = View.GONE
            awus_pcya_error_walkin.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.pcya?.delivery?.actual?.percentage?.isNaN() == false) {
            delivery_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.pcya.delivery.actual.percentage
            )).plus(getString(R.string.percentage_text))
            delivery_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_delivery.visibility = View.GONE

        } else {
            delivery_pcy_percentage.visibility = View.GONE
            awus_pcya_error_delivery.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.pcya?.carryout?.actual?.percentage?.isNaN() == false) {
            carry_out_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.pcya.carryout.actual.percentage
            )).plus(getString(R.string.percentage_text))
            carry_out_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_carryout.visibility = View.GONE

        } else {
            carry_out_pcy_percentage.visibility = View.GONE
            awus_pcya_error_carryout.visibility = View.VISIBLE

        }

        // order count

        if (storeDataTodayViewCEOAws.orderCount?.total?.actual?.value?.isNaN() == false && storeDataTodayViewCEOAws.orderCount.total.status?.toString() != null) {
            oc_percentage.text =
                Validation().ignoreZeroAfterDecimal(storeDataTodayViewCEOAws.orderCount.total.actual.value)
            when {
                storeDataTodayViewCEOAws.orderCount.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    oc_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewCEOAws.orderCount.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    oc_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    oc_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewCEOAws.orderCount?.olo?.actual?.value?.isNaN() == false) {
            oc_pcy_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.orderCount.olo.actual.value
            )
            oc_pcy_percentage.visibility = View.VISIBLE
            awus_oc_error_olo.visibility = View.GONE

        } else {
            oc_pcy_percentage.visibility = View.GONE
            awus_oc_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.orderCount?.phone?.actual?.value?.isNaN() == false) {
            phone_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.orderCount.phone.actual.value
            )
            phone_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_phone.visibility = View.GONE

        } else {
            phone_oc_percentage.visibility = View.GONE
            awus_oc_error_phone.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.orderCount?.walkin?.actual?.value?.isNaN() == false) {
            walkin_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.orderCount.walkin.actual.value
            )
            walkin_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_walkin.visibility = View.GONE

        } else {
            walkin_oc_percentage.visibility = View.GONE
            awus_oc_error_walkin.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.orderCount?.delivery?.actual?.value?.isNaN() == false) {
            delivery_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.orderCount.delivery.actual.value
            )
            delivery_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_delivery.visibility = View.GONE

        } else {
            delivery_oc_percentage.visibility = View.GONE
            awus_oc_error_delivery.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.orderCount?.carryout?.actual?.value?.isNaN() == false) {
            carry_out_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.orderCount.carryout.actual.value
            )
            carry_out_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_carryout.visibility = View.GONE

        } else {
            carry_out_oc_percentage.visibility = View.GONE
            awus_oc_error_carryout.visibility = View.VISIBLE

        }

        // Average  Tickets
        if (storeDataTodayViewCEOAws.averageTicket?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewCEOAws.averageTicket.total.status?.toString() != null) {
            average_percentage.text =
                if (!storeDataTodayViewCEOAws.averageTicket.total.actual.amount.isNaN()) getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(storeDataTodayViewCEOAws.averageTicket.total.actual.amount)
                ) else ""

            when {
                storeDataTodayViewCEOAws.averageTicket.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewCEOAws.averageTicket.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    average_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewCEOAws.averageTicket?.olo?.actual?.value?.isNaN() == false) {
            average_olo_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.averageTicket.olo.actual.value
            )
            average_olo_percentage.visibility = View.VISIBLE
            awus_avg_error_olo.visibility = View.GONE

        } else {
            average_olo_percentage.visibility = View.GONE
            awus_avg_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.averageTicket?.phone?.actual?.value?.isNaN() == false) {
            average_phone_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.averageTicket.phone.actual.value
            )
            average_phone_percentage.visibility = View.VISIBLE
            awus_avg_error_phone.visibility = View.GONE

        } else {
            average_phone_percentage.visibility = View.GONE
            awus_avg_error_phone.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.averageTicket?.walkin?.actual?.value?.isNaN() == false) {
            walkin_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.averageTicket.walkin.actual.value
            )
            walkin_average_percentage.visibility = View.VISIBLE
            awus_avg_error_walkin.visibility = View.GONE

        } else {
            walkin_average_percentage.visibility = View.GONE
            awus_avg_error_walkin.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.averageTicket?.delivery?.actual?.value?.isNaN() == false) {
            delivery_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.averageTicket.delivery.actual.value
            )
            delivery_percentage.visibility = View.VISIBLE
            awus_avg_error_delivery.visibility = View.GONE

        } else {
            delivery_percentage.visibility = View.GONE
            awus_avg_error_delivery.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.averageTicket?.carryout?.actual?.value?.isNaN() == false) {
            carry_out_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.averageTicket.carryout.actual.value
            )
            carry_out_average_percentage.visibility = View.VISIBLE
            awus_avg_error_carryout.visibility = View.GONE

        } else {
            carry_out_average_percentage.visibility = View.GONE
            awus_avg_error_carryout.visibility = View.VISIBLE

        }

        // OLO sales
        if (storeDataTodayViewCEOAws.oloSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewCEOAws.oloSales.total.status?.toString() != null) {
            olo_sales_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewCEOAws.oloSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewCEOAws.oloSales.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    olo_sales_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewCEOAws.oloSales.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    olo_sales_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    olo_sales_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewCEOAws.oloSales?.pcya?.actual?.percentage?.isNaN() == false) {
            olo_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.oloSales.pcya.actual.percentage
            )
            olo_percentage.visibility = View.VISIBLE
            awus_olo_sales_error_olo.visibility = View.GONE

        } else {
            olo_percentage.visibility = View.GONE
            awus_olo_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.oloSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            olo_order_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.oloSales.orderCount.actual.percentage
            )
            olo_order_percentage.visibility = View.VISIBLE
            awus_olo_sales_error_oc.visibility = View.GONE

        } else {
            olo_order_percentage.visibility = View.GONE
            awus_olo_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.oloSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            olo_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.oloSales.averageTicket.actual.percentage
            )
            olo_average_percentage.visibility = View.VISIBLE
            awus_olo_sales_error_at.visibility = View.GONE

        } else {
            olo_average_percentage.visibility = View.GONE
            awus_olo_sales_error_at.visibility = View.VISIBLE

        }

        // Phone sales order count
        if (storeDataTodayViewCEOAws.phoneSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewCEOAws.phoneSales.total.status?.toString() != null) {
            phone_sales_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewCEOAws.phoneSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewCEOAws.phoneSales.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    phone_sales_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewCEOAws.phoneSales.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    phone_sales_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    phone_sales_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewCEOAws.phoneSales?.pcya?.actual?.percentage?.isNaN() == false) {
            phone_pcys_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.phoneSales.pcya.actual.percentage
            )
            phone_pcys_percentage.visibility = View.VISIBLE
            awus_phones_sales_error_olo.visibility = View.GONE

        } else {
            phone_pcys_percentage.visibility = View.GONE
            awus_phones_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.phoneSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            phone_sales_order_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.phoneSales.orderCount.actual.percentage
            )
            phone_sales_order_percentage.visibility = View.VISIBLE
            awus_phones_sales_error_oc.visibility = View.GONE

        } else {
            phone_sales_order_percentage.visibility = View.GONE
            awus_phones_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.phoneSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            average_phone_ticket_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.phoneSales.averageTicket.actual.percentage
            )
            average_phone_ticket_percentage.visibility = View.VISIBLE
            awus_phones_sales_error_at.visibility = View.GONE

        } else {
            average_phone_ticket_percentage.visibility = View.GONE
            awus_phones_sales_error_at.visibility = View.VISIBLE

        }

        // walk-in sales

        if (storeDataTodayViewCEOAws.walkinSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewCEOAws.walkinSales.total.status?.toString() != null) {
            walkin_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewCEOAws.walkinSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewCEOAws.walkinSales.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    walkin_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewCEOAws.walkinSales.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    walkin_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    walkin_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewCEOAws.walkinSales?.pcya?.actual?.percentage?.isNaN() == false) {
            walkin_pcy_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.walkinSales.pcya.actual.percentage
            )
            walkin_pcy_value.visibility = View.VISIBLE
            awus_walkins_sales_error_olo.visibility = View.GONE

        } else {
            walkin_pcy_value.visibility = View.GONE
            awus_walkins_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.walkinSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            walkin_order_count_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.walkinSales.orderCount.actual.percentage
            )
            walkin_order_count_value.visibility = View.VISIBLE
            awus_walkins_sales_error_oc.visibility = View.GONE

        } else {
            walkin_order_count_value.visibility = View.GONE
            awus_walkins_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.walkinSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            average_walkin_ticket_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.walkinSales.averageTicket.actual.percentage
            )
            average_walkin_ticket_value.visibility = View.VISIBLE
            awus_walkins_sales_error_at.visibility = View.GONE

        } else {
            average_walkin_ticket_value.visibility = View.GONE
            awus_walkins_sales_error_at.visibility = View.VISIBLE

        }

        // delivery sales
        if (storeDataTodayViewCEOAws.deliverySales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewCEOAws.deliverySales.total.status?.toString() != null) {
            delivery_sales_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewCEOAws.deliverySales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewCEOAws.deliverySales.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    delivery_sales_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewCEOAws.deliverySales.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    delivery_sales_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    delivery_sales_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewCEOAws.deliverySales?.pcya?.actual?.percentage?.isNaN() == false) {
            delivery_pcy_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.deliverySales.pcya.actual.percentage
            )
            delivery_pcy_value.visibility = View.VISIBLE
            awus_delivery_sales_error_olo.visibility = View.GONE

        } else {
            delivery_pcy_value.visibility = View.GONE
            awus_delivery_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.deliverySales?.orderCount?.actual?.percentage?.isNaN() == false) {
            delivery_order_count_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.deliverySales.orderCount.actual.percentage
            )
            delivery_order_count_value.visibility = View.VISIBLE
            awus_walkins_sales_error_at.visibility = View.GONE

        } else {
            delivery_order_count_value.visibility = View.GONE
            awus_delivery_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.deliverySales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            delivery_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.deliverySales.averageTicket.actual.percentage
            )
            delivery_average_percentage.visibility = View.VISIBLE
            awus_walkins_sales_error_at.visibility = View.GONE


        } else {
            delivery_average_percentage.visibility = View.GONE
            awus_delivery_sales_error_at.visibility = View.VISIBLE

        }


        // carryout sales

        if (storeDataTodayViewCEOAws.carryoutSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewCEOAws.carryoutSales.total.status?.toString() != null) {
            carryout_average_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewCEOAws.carryoutSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewCEOAws.carryoutSales.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    carryout_average_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewCEOAws.carryoutSales.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    carryout_average_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    carryout_average_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewCEOAws.carryoutSales?.pcya?.actual?.percentage?.isNaN() == false) {
            carry_out_pcy_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.carryoutSales.pcya.actual.percentage
            )
            carry_out_pcy_value.visibility = View.VISIBLE
            awus_carryout_sales_error_olo.visibility = View.GONE

        } else {
            carry_out_pcy_value.visibility = View.GONE
            awus_carryout_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.carryoutSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            carry_outorder_count_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.carryoutSales.orderCount.actual.percentage
            )
            carry_outorder_count_value.visibility = View.VISIBLE
            awus_carryout_sales_error_oc.visibility = View.GONE

        } else {
            carry_outorder_count_value.visibility = View.GONE
            awus_carryout_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewCEOAws.carryoutSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            carry_average_tickets_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewCEOAws.carryoutSales.averageTicket.actual.percentage
            )
            carry_average_tickets_value.visibility = View.VISIBLE
            awus_carryout_sales_error_at.visibility = View.GONE

        } else {
            carry_average_tickets_value.visibility = View.GONE
            awus_carryout_sales_error_at.visibility = View.VISIBLE

        }
    }

    private fun yesterdayViewCEOAws(todayDetail: CEOOverviewYesterdayQuery.Ceo) {
        try {
            val storeDataYesterdayViewCEOAws =
                todayDetail.kpis?.supervisors?.stores?.yesterday!!.sales

            Logger.info("AWUS Yesterday", "AWUS Overview KPI")


            awus_display.text = getString(R.string.awus_text)
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewCEOAws?.phoneSales?.pcya?.displayName,
                phone_sales_pcys_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewCEOAws?.walkinSales?.pcya?.displayName,
                walkin_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewCEOAws?.deliverySales?.pcya?.displayName,
                delivery_sales_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewCEOAws?.carryoutSales?.pcya?.displayName,
                carry_out_sales_pcy_parent
            )

            pcy_display_name.text =
                storeDataYesterdayViewCEOAws!!.pcya?.total?.displayName ?: getString(R.string.pcy)
            phone_pcys.text =
                storeDataYesterdayViewCEOAws.pcya?.phone?.displayName
                    ?: getString(R.string.phone_pcys)
            walkin_pcy.text =
                storeDataYesterdayViewCEOAws.pcya?.walkin?.displayName
                    ?: getString(R.string.walkin_pcy)
            delivery_pcy.text =
                storeDataYesterdayViewCEOAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            delivery_pcy.text =
                storeDataYesterdayViewCEOAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            carry_out_pcy.text =
                storeDataYesterdayViewCEOAws.pcya?.carryout?.displayName
                    ?: getString(R.string.carry_out_pcy)
            // order count display
            if (storeDataYesterdayViewCEOAws.orderCount?.total == null || storeDataYesterdayViewCEOAws.orderCount.total.displayName.isNullOrEmpty()) {
                ll_order_count_display.visibility = View.GONE
            } else {
                ll_order_count_display.visibility = View.VISIBLE
                order_count_display.text =
                    storeDataYesterdayViewCEOAws.orderCount.total.displayName
            }

            if (storeDataYesterdayViewCEOAws.orderCount?.olo == null || storeDataYesterdayViewCEOAws.orderCount.olo.displayName.isNullOrEmpty()) {
                ll_olo_order_count.visibility = View.GONE
            } else {
                ll_olo_order_count.visibility = View.VISIBLE
                olo_order_count.text =
                    storeDataYesterdayViewCEOAws.orderCount.total?.displayName
            }

            if (storeDataYesterdayViewCEOAws.orderCount?.phone == null || storeDataYesterdayViewCEOAws.orderCount.phone.displayName.isNullOrEmpty()) {
                ll_phone_order_count.visibility = View.GONE
            } else {
                ll_phone_order_count.visibility = View.VISIBLE
                phone_order_count.text = storeDataYesterdayViewCEOAws.orderCount.phone.displayName
            }

            if (storeDataYesterdayViewCEOAws.orderCount?.walkin == null || storeDataYesterdayViewCEOAws.orderCount.walkin.displayName.isNullOrEmpty()) {
                ll_walkin_order_count.visibility = View.GONE
            } else {
                ll_walkin_order_count.visibility = View.VISIBLE
                walkin_order_count.text = storeDataYesterdayViewCEOAws.orderCount.walkin.displayName
            }

            if (storeDataYesterdayViewCEOAws.orderCount?.delivery == null || storeDataYesterdayViewCEOAws.orderCount.delivery.displayName.isNullOrEmpty()) {
                ll_delivery_order_count.visibility = View.GONE
            } else {
                ll_delivery_order_count.visibility = View.VISIBLE
                delivery_order_count.text =
                    storeDataYesterdayViewCEOAws.orderCount.delivery.displayName
            }


            if (storeDataYesterdayViewCEOAws.orderCount?.carryout == null || storeDataYesterdayViewCEOAws.orderCount.carryout.displayName.isNullOrEmpty()) {
                ll_carry_outorder_count.visibility = View.GONE
            } else {
                ll_carry_outorder_count.visibility = View.VISIBLE
                carry_outorder_count.text =
                    storeDataYesterdayViewCEOAws.orderCount.carryout.displayName
            }



            // average ticket count display
                if (storeDataYesterdayViewCEOAws.averageTicket?.total == null || storeDataYesterdayViewCEOAws.averageTicket.total.displayName.isNullOrEmpty()) {
                    ll_average_ticket_display_name.visibility = View.GONE
            } else {
                ll_average_ticket_display_name.visibility = View.VISIBLE
                    average_ticket_display_name.text =
                        storeDataYesterdayViewCEOAws.averageTicket.total.displayName
            }


             if (storeDataYesterdayViewCEOAws.averageTicket?.olo == null || storeDataYesterdayViewCEOAws.averageTicket.olo.displayName.isNullOrEmpty()) {
                 ll_olo_average_ticket.visibility = View.GONE
            } else {
                ll_olo_average_ticket.visibility = View.VISIBLE
                 olo_average_ticket.text =
                        storeDataYesterdayViewCEOAws.averageTicket.olo.displayName
            }

             if (storeDataYesterdayViewCEOAws.averageTicket?.phone == null || storeDataYesterdayViewCEOAws.averageTicket.phone.displayName.isNullOrEmpty()) {
                 ll_average_phone_ticket.visibility = View.GONE
            } else {
                ll_average_phone_ticket.visibility = View.VISIBLE
                 average_phone_ticket.text =
                        storeDataYesterdayViewCEOAws.averageTicket.phone.displayName
            }

             if (storeDataYesterdayViewCEOAws.averageTicket?.walkin == null || storeDataYesterdayViewCEOAws.averageTicket.walkin.displayName.isNullOrEmpty()) {
                 ll_average_walkin_ticket.visibility = View.GONE
            } else {
                ll_average_walkin_ticket.visibility = View.VISIBLE
                 average_walkin_ticket.text =
                        storeDataYesterdayViewCEOAws.averageTicket.walkin.displayName
            }

             if (storeDataYesterdayViewCEOAws.averageTicket?.delivery == null || storeDataYesterdayViewCEOAws.averageTicket.delivery.displayName.isNullOrEmpty()) {
                 ll_delivery_average_tickets.visibility = View.GONE
            } else {
                ll_delivery_average_tickets.visibility = View.VISIBLE
                 delivery_average_tickets.text =
                        storeDataYesterdayViewCEOAws.averageTicket.delivery.displayName
            }

               if (storeDataYesterdayViewCEOAws.averageTicket?.carryout == null || storeDataYesterdayViewCEOAws.averageTicket.carryout.displayName.isNullOrEmpty()) {
                   ll_carry_average_tickets.visibility = View.GONE
            } else {
                ll_carry_average_tickets.visibility = View.VISIBLE
                   carry_average_tickets.text =
                        storeDataYesterdayViewCEOAws.averageTicket.carryout.displayName
            }

            // olo sales display

             if (storeDataYesterdayViewCEOAws.oloSales?.total == null || storeDataYesterdayViewCEOAws.oloSales.total.displayName.isNullOrEmpty()) {
                 ll_olo_sales_display_name.visibility = View.GONE
            } else {
                ll_olo_sales_display_name.visibility = View.VISIBLE
                 olo_sales_display_name.text =
                        storeDataYesterdayViewCEOAws.oloSales.total.displayName
            }

             if (storeDataYesterdayViewCEOAws.oloSales?.pcya == null || storeDataYesterdayViewCEOAws.oloSales.pcya.displayName.isNullOrEmpty()) {
                 olo_sales_pcy_parent.visibility = View.GONE
            } else {
                 olo_sales_pcy_parent.visibility = View.VISIBLE
                 olo_pcy.text =
                        storeDataYesterdayViewCEOAws.oloSales.pcya.displayName
            }

             if (storeDataYesterdayViewCEOAws.oloSales?.orderCount == null || storeDataYesterdayViewCEOAws.oloSales.orderCount.displayName.isNullOrEmpty()) {
                 ll_olo_order_count_sales_display.visibility = View.GONE
            } else {
                ll_olo_order_count_sales_display.visibility = View.VISIBLE
                 olo_order_count_sales_display.text =
                        storeDataYesterdayViewCEOAws.oloSales.orderCount.displayName
            }

            if (storeDataYesterdayViewCEOAws.oloSales?.averageTicket == null || storeDataYesterdayViewCEOAws.oloSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket_sales_display.visibility = View.GONE
            } else {
                ll_olo_average_ticket_sales_display.visibility = View.VISIBLE
                olo_average_ticket_sales_display.text =
                        storeDataYesterdayViewCEOAws.oloSales.averageTicket.displayName
            }


            // phone sales display

              if (storeDataYesterdayViewCEOAws.phoneSales?.total == null || storeDataYesterdayViewCEOAws.phoneSales.total.displayName.isNullOrEmpty()) {
                  ll_phone_sales_display_name.visibility = View.GONE
            } else {
                ll_phone_sales_display_name.visibility = View.VISIBLE
                  phone_sales_display_name.text =
                        storeDataYesterdayViewCEOAws.phoneSales.total.displayName
            }



              if (storeDataYesterdayViewCEOAws.phoneSales?.orderCount == null || storeDataYesterdayViewCEOAws.phoneSales.orderCount.displayName.isNullOrEmpty()) {
                  ll_phone_order_count_display.visibility = View.GONE
            } else {
                ll_phone_order_count_display.visibility = View.VISIBLE
                  phone_order_count_display.text =
                        storeDataYesterdayViewCEOAws.phoneSales.orderCount.displayName
            }

            if (storeDataYesterdayViewCEOAws.phoneSales?.averageTicket == null || storeDataYesterdayViewCEOAws.phoneSales.averageTicket.displayName.isNullOrEmpty()) {
                  ll_average_phone_ticket_display.visibility = View.GONE
            } else {
                ll_average_phone_ticket_display.visibility = View.VISIBLE
                average_phone_ticket_display.text =
                        storeDataYesterdayViewCEOAws.phoneSales.averageTicket.displayName
            }


            // walk-in Sales
              if (storeDataYesterdayViewCEOAws.walkinSales?.total == null || storeDataYesterdayViewCEOAws.walkinSales.total.displayName.isNullOrEmpty()) {
                  ll_walkin_sales_display_name.visibility = View.GONE
            } else {
                ll_walkin_sales_display_name.visibility = View.VISIBLE
                  walkin_sales_display_name.text =
                        storeDataYesterdayViewCEOAws.walkinSales.total.displayName
            }


              if (storeDataYesterdayViewCEOAws.walkinSales?.orderCount == null || storeDataYesterdayViewCEOAws.walkinSales.orderCount.displayName.isNullOrEmpty()) {
                  ll_walkin_order_count_sales_display.visibility = View.GONE
            } else {
                ll_walkin_order_count_sales_display.visibility = View.VISIBLE
                  walkin_order_count_sales_display.text =
                        storeDataYesterdayViewCEOAws.walkinSales.orderCount.displayName
            }



             if (storeDataYesterdayViewCEOAws.walkinSales?.averageTicket == null || storeDataYesterdayViewCEOAws.walkinSales.averageTicket.displayName.isNullOrEmpty()) {
                 ll_average_walkin_ticket_display.visibility = View.GONE
            } else {
                ll_average_walkin_ticket_display.visibility = View.VISIBLE
                 average_walkin_ticket_display.text =
                        storeDataYesterdayViewCEOAws.walkinSales.averageTicket.displayName
            }


            //  deliver Sales

             if (storeDataYesterdayViewCEOAws.deliverySales?.total == null || storeDataYesterdayViewCEOAws.deliverySales.total.displayName.isNullOrEmpty()) {
                 ll_delivery_sales_display_name.visibility = View.GONE
            } else {
                ll_delivery_sales_display_name.visibility = View.VISIBLE
                 delivery_sales_display_name.text =
                        storeDataYesterdayViewCEOAws.deliverySales.total.displayName
            }

            if (storeDataYesterdayViewCEOAws.deliverySales?.orderCount == null || storeDataYesterdayViewCEOAws.deliverySales.orderCount.displayName.isNullOrEmpty()) {
                ll_delivery_order_count_display.visibility = View.GONE
            } else {
                ll_delivery_order_count_display.visibility = View.VISIBLE
                delivery_order_count_display.text =
                        storeDataYesterdayViewCEOAws.deliverySales.orderCount.displayName
            }

             if (storeDataYesterdayViewCEOAws.deliverySales?.averageTicket == null || storeDataYesterdayViewCEOAws.deliverySales.averageTicket.displayName.isNullOrEmpty()) {
                 ll_delivery_average_tickets_display.visibility = View.GONE
            } else {
                ll_delivery_average_tickets_display.visibility = View.VISIBLE
                 delivery_average_tickets_display.text =
                        storeDataYesterdayViewCEOAws.deliverySales.averageTicket.displayName
            }


            //  carryout Sales
            if (storeDataYesterdayViewCEOAws.carryoutSales?.total == null || storeDataYesterdayViewCEOAws.carryoutSales.total.displayName.isNullOrEmpty()) {
                ll_carry_out_display_text.visibility = View.GONE
            } else {
                ll_carry_out_display_text.visibility = View.VISIBLE
                carry_out_display_text.text =
                    storeDataYesterdayViewCEOAws.carryoutSales.total.displayName
            }
            if (storeDataYesterdayViewCEOAws.carryoutSales?.orderCount == null || storeDataYesterdayViewCEOAws.carryoutSales.orderCount.displayName.isNullOrEmpty()) {
                ll_carry_outorder_count_display.visibility = View.GONE
            } else {
                ll_carry_outorder_count_display.visibility = View.VISIBLE
                carry_outorder_count_display.text =
                    storeDataYesterdayViewCEOAws.carryoutSales.orderCount.displayName
            }

            if (storeDataYesterdayViewCEOAws.carryoutSales?.averageTicket == null || storeDataYesterdayViewCEOAws.carryoutSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets_display.visibility = View.GONE
            } else {
                ll_carry_average_tickets_display.visibility = View.VISIBLE
                carry_average_tickets_display.text =
                    storeDataYesterdayViewCEOAws.carryoutSales.averageTicket.displayName
            }


            // display null check for olo pcy

            if (storeDataYesterdayViewCEOAws.pcya?.olo == null || storeDataYesterdayViewCEOAws.pcya.olo.displayName.isNullOrEmpty()) {
                olo_pcy_parent.visibility = View.GONE
            } else {
                olo_pcy_parent.visibility = View.GONE
                olo_pcy_display.text = storeDataYesterdayViewCEOAws.pcya.olo.displayName
            }

            if (storeDataYesterdayViewCEOAws.pcya?.phone == null || storeDataYesterdayViewCEOAws.pcya.phone.displayName.isNullOrEmpty()) {
                phone_pcys_parent.visibility = View.GONE
            } else {
                phone_pcys_parent.visibility = View.VISIBLE
                phone_pcys.text =
                    storeDataYesterdayViewCEOAws.pcya.phone.displayName
            }
            if (storeDataYesterdayViewCEOAws.pcya?.walkin == null || storeDataYesterdayViewCEOAws.pcya.walkin.displayName.isNullOrEmpty()) {
                walkin_pcys_parent.visibility = View.GONE
            } else {
                walkin_pcys_parent.visibility = View.VISIBLE
                walkin_pcy.text =
                    storeDataYesterdayViewCEOAws.pcya.walkin.displayName
            }
            if (storeDataYesterdayViewCEOAws.pcya?.delivery == null ||
                storeDataYesterdayViewCEOAws.pcya.delivery.displayName.isNullOrEmpty()
            ) {
                delivery_pcy_parent.visibility = View.GONE
            } else {
                delivery_pcy_parent.visibility = View.VISIBLE
                delivery_pcy.text =
                    storeDataYesterdayViewCEOAws.pcya.delivery.displayName
            }
            if (storeDataYesterdayViewCEOAws.pcya?.carryout == null || storeDataYesterdayViewCEOAws.pcya.carryout.displayName.isNullOrEmpty()) {
                carry_out_pcy_parent.visibility = View.GONE
            } else {
                carry_out_pcy_parent.visibility = View.VISIBLE
                carry_out_pcy.text =
                    storeDataYesterdayViewCEOAws.pcya.carryout.displayName
            }

            /// new
            awus_sales.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewCEOAws.actual?.amount,
                storeDataYesterdayViewCEOAws.actual?.percentage,
                storeDataYesterdayViewCEOAws.actual?.value
            )

            if (storeDataYesterdayViewCEOAws.status?.toString() != null) {
                when {
                    storeDataYesterdayViewCEOAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                        awus_sales.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewCEOAws.status.toString() == resources.getString(R.string.under_limit) -> {
                        awus_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        awus_sales.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // scroll detect

            awus_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (awus_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.awus_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@AWUSKpiActivity,
                                    storeDataYesterdayViewCEOAws.actual?.amount,
                                    storeDataYesterdayViewCEOAws.actual?.percentage,
                                    storeDataYesterdayViewCEOAws.actual?.value
                                )
                            if (storeDataYesterdayViewCEOAws.status?.toString() != null) {
                                when {
                                    storeDataYesterdayViewCEOAws.status.toString() == resources.getString(
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
                                    storeDataYesterdayViewCEOAws.status.toString() == resources.getString(
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
                        y = awus_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            awus_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewCEOAws.goal?.amount,
                storeDataYesterdayViewCEOAws.goal?.percentage,
                storeDataYesterdayViewCEOAws.goal?.value
            )
            awus__variance_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewCEOAws.variance?.amount,
                storeDataYesterdayViewCEOAws.variance?.percentage,
                storeDataYesterdayViewCEOAws.variance?.value
            )

            showAWUSNarrativeData(storeDataYesterdayViewCEOAws.narrative.toString())

            // PCY
            pcy_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewCEOAws.pcya?.total?.actual?.amount,
                storeDataYesterdayViewCEOAws.pcya?.total?.actual?.percentage,
                storeDataYesterdayViewCEOAws.pcya?.total?.actual?.value
            )

            if (storeDataYesterdayViewCEOAws.pcya?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewCEOAws.pcya.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewCEOAws.pcya.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.pcya?.olo?.actual?.amount,
                    storeDataYesterdayViewCEOAws.pcya?.olo?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.pcya?.olo?.actual?.value
                ).isEmpty()
            ) {
                olo_pcy_percentage.visibility = View.GONE
                awus_pcya_error_olo.visibility = View.VISIBLE


            } else {
                olo_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_olo.visibility = View.GONE

                olo_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.pcya?.olo?.actual?.amount,
                    storeDataYesterdayViewCEOAws.pcya?.olo?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.pcya?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.pcya?.phone?.actual?.amount,
                    storeDataYesterdayViewCEOAws.pcya?.phone?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.pcya?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_pcy_percentage.visibility = View.GONE
                awus_pcya_error_phone.visibility = View.VISIBLE

            } else {
                phone_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                phone_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.pcya?.phone?.actual?.amount,
                    storeDataYesterdayViewCEOAws.pcya?.phone?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.pcya?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.pcya?.walkin?.actual?.amount,
                    storeDataYesterdayViewCEOAws.pcya?.walkin?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.pcya?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_percentage.visibility = View.GONE
                awus_pcya_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                walkin_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.pcya?.walkin?.actual?.amount,
                    storeDataYesterdayViewCEOAws.pcya?.walkin?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.pcya?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.pcya?.delivery?.actual?.amount,
                    storeDataYesterdayViewCEOAws.pcya?.delivery?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.pcya?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_percentage.visibility = View.GONE
                awus_pcya_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_delivery.visibility = View.GONE

                delivery_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.pcya?.delivery?.actual?.amount,
                    storeDataYesterdayViewCEOAws.pcya?.delivery?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.pcya?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.pcya?.carryout?.actual?.amount,
                    storeDataYesterdayViewCEOAws.pcya?.carryout?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.pcya?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_percentage.visibility = View.GONE
                awus_pcya_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_carryout.visibility = View.GONE

                carry_out_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.pcya?.carryout?.actual?.amount,
                    storeDataYesterdayViewCEOAws.pcya?.carryout?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.pcya?.carryout?.actual?.value
                )
            }

            // order count
            oc_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewCEOAws.orderCount?.total?.actual?.amount,
                storeDataYesterdayViewCEOAws.orderCount?.total?.actual?.percentage,
                storeDataYesterdayViewCEOAws.orderCount?.total?.actual?.value
            )

            if (storeDataYesterdayViewCEOAws.orderCount?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewCEOAws.orderCount.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewCEOAws.orderCount.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.green))

                    } else -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.orderCount?.olo?.actual?.amount,
                    storeDataYesterdayViewCEOAws.orderCount?.olo?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.orderCount?.olo?.actual?.value
                ).isEmpty()
            ) {
                oc_pcy_percentage.visibility = View.GONE
                awus_oc_error_olo.visibility = View.VISIBLE

            } else {
                oc_pcy_percentage.visibility = View.VISIBLE
                awus_oc_error_olo.visibility = View.GONE

                oc_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.orderCount?.olo?.actual?.amount,
                    storeDataYesterdayViewCEOAws.orderCount?.olo?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.orderCount?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.orderCount?.phone?.actual?.amount,
                    storeDataYesterdayViewCEOAws.orderCount?.phone?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.orderCount?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_oc_percentage.visibility = View.GONE
                awus_oc_error_phone.visibility = View.VISIBLE

            } else {
                phone_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_phone.visibility = View.GONE

                phone_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.orderCount?.phone?.actual?.amount,
                    storeDataYesterdayViewCEOAws.orderCount?.phone?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.orderCount?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.orderCount?.walkin?.actual?.amount,
                    storeDataYesterdayViewCEOAws.orderCount?.walkin?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.orderCount?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_oc_percentage.visibility = View.GONE
                awus_oc_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_walkin.visibility = View.GONE

                walkin_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.orderCount?.walkin?.actual?.amount,
                    storeDataYesterdayViewCEOAws.orderCount?.walkin?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.orderCount?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.orderCount?.delivery?.actual?.amount,
                    storeDataYesterdayViewCEOAws.orderCount?.delivery?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.orderCount?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_oc_percentage.visibility = View.GONE
                awus_oc_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_delivery.visibility = View.GONE

                delivery_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.orderCount?.delivery?.actual?.amount,
                    storeDataYesterdayViewCEOAws.orderCount?.delivery?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.orderCount?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.orderCount?.carryout?.actual?.amount,
                    storeDataYesterdayViewCEOAws.orderCount?.carryout?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.orderCount?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_oc_percentage.visibility = View.GONE
                awus_oc_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_carryout.visibility = View.GONE

                carry_out_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.orderCount?.carryout?.actual?.amount,
                    storeDataYesterdayViewCEOAws.orderCount?.carryout?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.orderCount?.carryout?.actual?.value
                )
            }

            // Average  Tickets
            average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewCEOAws.averageTicket?.total?.actual?.amount,
                storeDataYesterdayViewCEOAws.averageTicket?.total?.actual?.percentage,
                storeDataYesterdayViewCEOAws.averageTicket?.total?.actual?.value
            )

            if (storeDataYesterdayViewCEOAws.averageTicket?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewCEOAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewCEOAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.averageTicket?.olo?.actual?.amount,
                    storeDataYesterdayViewCEOAws.averageTicket?.olo?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.averageTicket?.olo?.actual?.value
                ).isEmpty()
            ) {
                average_olo_percentage.visibility = View.GONE
                awus_avg_error_olo.visibility = View.VISIBLE

            } else {
                average_olo_percentage.visibility = View.VISIBLE
                awus_avg_error_olo.visibility = View.GONE

                average_olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.averageTicket?.olo?.actual?.amount,
                    storeDataYesterdayViewCEOAws.averageTicket?.olo?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.averageTicket?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.averageTicket?.phone?.actual?.amount,
                    storeDataYesterdayViewCEOAws.averageTicket?.phone?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.averageTicket?.phone?.actual?.value
                ).isEmpty()
            ) {
                average_phone_percentage.visibility = View.GONE
                awus_avg_error_phone.visibility = View.VISIBLE

            } else {
                average_phone_percentage.visibility = View.VISIBLE
                awus_avg_error_phone.visibility = View.GONE

                average_phone_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.averageTicket?.phone?.actual?.amount,
                    storeDataYesterdayViewCEOAws.averageTicket?.phone?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.averageTicket?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.averageTicket?.walkin?.actual?.amount,
                    storeDataYesterdayViewCEOAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.averageTicket?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_average_percentage.visibility = View.GONE
                awus_avg_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_average_percentage.visibility = View.VISIBLE
                awus_avg_error_walkin.visibility = View.GONE

                walkin_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.averageTicket?.walkin?.actual?.amount,
                    storeDataYesterdayViewCEOAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.averageTicket?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.averageTicket?.delivery?.actual?.amount,
                    storeDataYesterdayViewCEOAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.averageTicket?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_percentage.visibility = View.GONE
                awus_avg_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_percentage.visibility = View.VISIBLE
                awus_avg_error_delivery.visibility = View.GONE

                delivery_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.averageTicket?.delivery?.actual?.amount,
                    storeDataYesterdayViewCEOAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.averageTicket?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.averageTicket?.carryout?.actual?.amount,
                    storeDataYesterdayViewCEOAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.averageTicket?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_average_percentage.visibility = View.GONE
                awus_avg_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_average_percentage.visibility = View.VISIBLE
                awus_avg_error_carryout.visibility = View.GONE

                carry_out_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.averageTicket?.carryout?.actual?.amount,
                    storeDataYesterdayViewCEOAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.averageTicket?.carryout?.actual?.value
                )
            }

            // OLO sales
            olo_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewCEOAws.oloSales?.total?.actual?.amount,
                storeDataYesterdayViewCEOAws.oloSales?.total?.actual?.percentage,
                storeDataYesterdayViewCEOAws.oloSales?.total?.actual?.value
            )

            if (storeDataYesterdayViewCEOAws.oloSales?.total?.status?.toString() != null) {

                when {
                    storeDataYesterdayViewCEOAws.oloSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewCEOAws.oloSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.oloSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewCEOAws.oloSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.oloSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                olo_percentage.visibility = View.GONE
                awus_olo_sales_error_olo.visibility = View.VISIBLE

            } else {
                olo_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_olo.visibility = View.GONE

                olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.oloSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewCEOAws.oloSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.oloSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.oloSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewCEOAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.oloSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                olo_order_percentage.visibility = View.GONE
                awus_olo_sales_error_oc.visibility = View.VISIBLE

            } else {
                olo_order_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_oc.visibility = View.GONE

                olo_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.oloSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewCEOAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.oloSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewCEOAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.oloSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                olo_average_percentage.visibility = View.GONE
                awus_olo_sales_error_at.visibility = View.VISIBLE

            } else {
                olo_average_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_at.visibility = View.GONE

                olo_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewCEOAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.oloSales?.averageTicket?.actual?.value
                )
            }

            // Phone sales order count
            phone_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewCEOAws.phoneSales?.total?.actual?.amount,
                storeDataYesterdayViewCEOAws.phoneSales?.total?.actual?.percentage,
                storeDataYesterdayViewCEOAws.phoneSales?.total?.actual?.value
            )
            if (storeDataYesterdayViewCEOAws.phoneSales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewCEOAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewCEOAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.phoneSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewCEOAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.phoneSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                phone_pcys_percentage.visibility = View.GONE
                awus_phones_sales_error_olo.visibility = View.VISIBLE

            } else {
                phone_pcys_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_olo.visibility = View.GONE

                phone_pcys_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.phoneSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewCEOAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.phoneSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewCEOAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.phoneSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_phone_ticket_percentage.visibility = View.GONE
                awus_phones_sales_error_at.visibility = View.VISIBLE

            } else {
                average_phone_ticket_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_at.visibility = View.GONE

                average_phone_ticket_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewCEOAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.phoneSales?.averageTicket?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewCEOAws.phoneSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.phoneSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                phone_sales_order_percentage.visibility = View.GONE
                awus_phones_sales_error_oc.visibility = View.VISIBLE

            } else {
                phone_sales_order_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_oc.visibility = View.GONE

                phone_sales_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewCEOAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.oloSales?.orderCount?.actual?.value
                )
            }

            // walk-in sales
            walkin_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewCEOAws.walkinSales?.total?.actual?.amount,
                storeDataYesterdayViewCEOAws.walkinSales?.total?.actual?.percentage,
                storeDataYesterdayViewCEOAws.walkinSales?.total?.actual?.value
            )

            if (storeDataYesterdayViewCEOAws.walkinSales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewCEOAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewCEOAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.walkinSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewCEOAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.walkinSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_value.visibility = View.GONE
                awus_walkins_sales_error_olo.visibility = View.VISIBLE

            } else {
                walkin_pcy_value.visibility = View.VISIBLE
                awus_walkins_sales_error_olo.visibility = View.GONE

                walkin_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.walkinSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewCEOAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.walkinSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewCEOAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.walkinSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                walkin_order_count_value.visibility = View.GONE
                awus_walkins_sales_error_oc.visibility = View.VISIBLE

            } else {
                walkin_order_count_value.visibility = View.VISIBLE
                awus_walkins_sales_error_oc.visibility = View.GONE

                walkin_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewCEOAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.walkinSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewCEOAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.walkinSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_walkin_ticket_value.visibility = View.GONE
                awus_walkins_sales_error_at.visibility = View.VISIBLE

            } else {
                average_walkin_ticket_value.visibility = View.VISIBLE
                awus_walkins_sales_error_at.visibility = View.GONE

                average_walkin_ticket_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewCEOAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.walkinSales?.averageTicket?.actual?.value
                )
            }

            // delivery sales
            delivery_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewCEOAws.deliverySales?.total?.actual?.amount,
                storeDataYesterdayViewCEOAws.deliverySales?.total?.actual?.percentage,
                storeDataYesterdayViewCEOAws.deliverySales?.total?.actual?.value
            )

            if (storeDataYesterdayViewCEOAws.deliverySales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewCEOAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewCEOAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.deliverySales?.pcya?.actual?.amount,
                    storeDataYesterdayViewCEOAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.deliverySales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_value.visibility = View.GONE
                awus_delivery_sales_error_olo.visibility = View.VISIBLE

            } else {
                delivery_pcy_value.visibility = View.VISIBLE
                awus_delivery_sales_error_olo.visibility = View.GONE

                delivery_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.deliverySales?.pcya?.actual?.amount,
                    storeDataYesterdayViewCEOAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.deliverySales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewCEOAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.deliverySales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                delivery_order_count_value.visibility = View.GONE
                awus_delivery_sales_error_oc.visibility = View.VISIBLE

            } else {
                delivery_order_count_value.visibility = View.VISIBLE
                awus_delivery_sales_error_oc.visibility = View.GONE

                delivery_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewCEOAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.deliverySales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewCEOAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.deliverySales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                delivery_average_percentage.visibility = View.GONE
                awus_delivery_sales_error_at.visibility = View.VISIBLE

            } else {
                delivery_average_percentage.visibility = View.VISIBLE
                awus_delivery_sales_error_at.visibility = View.GONE

                delivery_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewCEOAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.deliverySales?.averageTicket?.actual?.value
                )
            }

            // carryout sales
            carryout_average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewCEOAws.carryoutSales?.total?.actual?.amount,
                storeDataYesterdayViewCEOAws.carryoutSales?.total?.actual?.percentage,
                storeDataYesterdayViewCEOAws.carryoutSales?.total?.actual?.value
            )

            if (storeDataYesterdayViewCEOAws.carryoutSales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewCEOAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewCEOAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewCEOAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.carryoutSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_value.visibility = View.GONE
                awus_carryout_sales_error_olo.visibility = View.VISIBLE

            } else {
                carry_out_pcy_value.visibility = View.VISIBLE
                awus_carryout_sales_error_olo.visibility = View.GONE

                carry_out_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewCEOAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.carryoutSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewCEOAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.carryoutSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                carry_outorder_count_value.visibility = View.GONE
                awus_carryout_sales_error_oc.visibility = View.VISIBLE

            } else {
                carry_outorder_count_value.visibility = View.VISIBLE
                awus_carryout_sales_error_oc.visibility = View.GONE

                carry_outorder_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewCEOAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.carryoutSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewCEOAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.carryoutSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                carry_average_tickets_value.visibility = View.GONE
                awus_carryout_sales_error_at.visibility = View.VISIBLE

            } else {
                carry_average_tickets_value.visibility = View.VISIBLE
                awus_carryout_sales_error_at.visibility = View.GONE

                carry_average_tickets_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewCEOAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewCEOAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewCEOAws.carryoutSales?.averageTicket?.actual?.value
                )
            }
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"AWUS Overview Yesterday KPI")
        }
    }

    private fun rangeOverViewCEOAws(todayDetail: CEOOverviewRangeQuery.Ceo) {
        try {
            val storeDataRangeOverViewCEOAws = todayDetail.kpis?.supervisors?.stores?.period!!.sales

            Logger.info("AWUS Period Range", "AWUS Overview KPI")


            awus_display.text = getString(R.string.awus_text)
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeOverViewCEOAws?.phoneSales?.pcya?.displayName,
                phone_sales_pcys_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeOverViewCEOAws?.walkinSales?.pcya?.displayName,
                walkin_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeOverViewCEOAws?.deliverySales?.pcya?.displayName,
                delivery_sales_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeOverViewCEOAws?.carryoutSales?.pcya?.displayName,
                carry_out_sales_pcy_parent
            )

            pcy_display_name.text =
                storeDataRangeOverViewCEOAws!!.pcya?.total?.displayName ?: getString(R.string.pcy)
            phone_pcys.text =
                storeDataRangeOverViewCEOAws.pcya?.phone?.displayName
                    ?: getString(R.string.phone_pcys)
            walkin_pcy.text =
                storeDataRangeOverViewCEOAws.pcya?.walkin?.displayName
                    ?: getString(R.string.walkin_pcy)
            delivery_pcy.text =
                storeDataRangeOverViewCEOAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            delivery_pcy.text =
                storeDataRangeOverViewCEOAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            carry_out_pcy.text =
                storeDataRangeOverViewCEOAws.pcya?.carryout?.displayName
                    ?: getString(R.string.carry_out_pcy)
            // order count display
            order_count_display.text =
                storeDataRangeOverViewCEOAws.orderCount?.total?.displayName
                    ?: getString(R.string.order_count)
            olo_order_count.text =
                storeDataRangeOverViewCEOAws.orderCount?.olo?.displayName
                    ?: getString(R.string.olo_order_count)
            phone_order_count.text = storeDataRangeOverViewCEOAws.orderCount?.phone?.displayName
                ?: getString(R.string.phone_order_count)
            walkin_order_count.text = storeDataRangeOverViewCEOAws.orderCount?.walkin?.displayName
                ?: getString(R.string.walkin_order_count)
            delivery_order_count.text =
                storeDataRangeOverViewCEOAws.orderCount?.delivery?.displayName
                    ?: getString(R.string.delivery_order_count)
            carry_outorder_count.text =
                storeDataRangeOverViewCEOAws.orderCount?.carryout?.displayName
                    ?: getString(R.string.carry_outorder_count)

            // average ticket count display
            if (storeDataRangeOverViewCEOAws.averageTicket?.total == null || storeDataRangeOverViewCEOAws.averageTicket.total.displayName.isNullOrEmpty()) {
                ll_average_ticket_display_name.visibility = View.GONE
            } else {
                ll_average_ticket_display_name.visibility = View.VISIBLE
                average_ticket_display_name.text =
                    storeDataRangeOverViewCEOAws.averageTicket.total.displayName
            }


            if (storeDataRangeOverViewCEOAws.averageTicket?.olo == null || storeDataRangeOverViewCEOAws.averageTicket.olo.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket.visibility = View.GONE
            } else {
                ll_olo_average_ticket.visibility = View.VISIBLE
                olo_average_ticket.text =
                    storeDataRangeOverViewCEOAws.averageTicket.olo.displayName
            }

            if (storeDataRangeOverViewCEOAws.averageTicket?.phone == null || storeDataRangeOverViewCEOAws.averageTicket.phone.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket.visibility = View.GONE
            } else {
                ll_average_phone_ticket.visibility = View.VISIBLE
                average_phone_ticket.text =
                    storeDataRangeOverViewCEOAws.averageTicket.phone.displayName
            }

            if (storeDataRangeOverViewCEOAws.averageTicket?.walkin == null || storeDataRangeOverViewCEOAws.averageTicket.walkin.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket.visibility = View.GONE
            } else {
                ll_average_walkin_ticket.visibility = View.VISIBLE
                average_walkin_ticket.text =
                    storeDataRangeOverViewCEOAws.averageTicket.walkin.displayName
            }

            if (storeDataRangeOverViewCEOAws.averageTicket?.delivery == null || storeDataRangeOverViewCEOAws.averageTicket.delivery.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets.visibility = View.GONE
            } else {
                ll_delivery_average_tickets.visibility = View.VISIBLE
                delivery_average_tickets.text =
                    storeDataRangeOverViewCEOAws.averageTicket.delivery.displayName
            }

            if (storeDataRangeOverViewCEOAws.averageTicket?.carryout == null || storeDataRangeOverViewCEOAws.averageTicket.carryout.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets.visibility = View.GONE
            } else {
                ll_carry_average_tickets.visibility = View.VISIBLE
                carry_average_tickets.text =
                    storeDataRangeOverViewCEOAws.averageTicket.carryout.displayName
            }


            // olo sales display
            if (storeDataRangeOverViewCEOAws.oloSales?.total == null || storeDataRangeOverViewCEOAws.oloSales.total.displayName.isNullOrEmpty()) {
                ll_olo_sales_display_name.visibility = View.GONE
            } else {
                ll_olo_sales_display_name.visibility = View.VISIBLE
                olo_sales_display_name.text =
                    storeDataRangeOverViewCEOAws.oloSales.total.displayName
            }

            if (storeDataRangeOverViewCEOAws.oloSales?.pcya == null || storeDataRangeOverViewCEOAws.oloSales.pcya.displayName.isNullOrEmpty()) {
                olo_sales_pcy_parent.visibility = View.GONE
            } else {
                olo_sales_pcy_parent.visibility = View.VISIBLE
                olo_pcy.text =
                    storeDataRangeOverViewCEOAws.oloSales.pcya.displayName
            }

            if (storeDataRangeOverViewCEOAws.oloSales?.orderCount == null || storeDataRangeOverViewCEOAws.oloSales.orderCount.displayName.isNullOrEmpty()) {
                ll_olo_order_count_sales_display.visibility = View.GONE
            } else {
                ll_olo_order_count_sales_display.visibility = View.VISIBLE
                olo_order_count_sales_display.text =
                    storeDataRangeOverViewCEOAws.oloSales.orderCount.displayName
            }

            if (storeDataRangeOverViewCEOAws.oloSales?.averageTicket == null || storeDataRangeOverViewCEOAws.oloSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket_sales_display.visibility = View.GONE
            } else {
                ll_olo_average_ticket_sales_display.visibility = View.VISIBLE
                olo_average_ticket_sales_display.text =
                    storeDataRangeOverViewCEOAws.oloSales.averageTicket.displayName
            }


            // phone sales display
            if (storeDataRangeOverViewCEOAws.phoneSales?.total == null || storeDataRangeOverViewCEOAws.phoneSales.total.displayName.isNullOrEmpty()) {
                ll_phone_sales_display_name.visibility = View.GONE
            } else {
                ll_phone_sales_display_name.visibility = View.VISIBLE
                phone_sales_display_name.text =
                    storeDataRangeOverViewCEOAws.phoneSales.total.displayName
            }


            if (storeDataRangeOverViewCEOAws.phoneSales?.orderCount == null || storeDataRangeOverViewCEOAws.phoneSales.orderCount.displayName.isNullOrEmpty()) {
                ll_phone_order_count_display.visibility = View.GONE
            } else {
                ll_phone_order_count_display.visibility = View.VISIBLE
                phone_order_count_display.text =
                    storeDataRangeOverViewCEOAws.phoneSales.orderCount.displayName
            }

            if (storeDataRangeOverViewCEOAws.phoneSales?.averageTicket == null || storeDataRangeOverViewCEOAws.phoneSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket_display.visibility = View.GONE
            } else {
                ll_average_phone_ticket_display.visibility = View.VISIBLE
                average_phone_ticket_display.text =
                    storeDataRangeOverViewCEOAws.phoneSales.averageTicket.displayName
            }


            // walk-in Sales
            if (storeDataRangeOverViewCEOAws.walkinSales?.total == null || storeDataRangeOverViewCEOAws.walkinSales.total.displayName.isNullOrEmpty()) {
                ll_walkin_sales_display_name.visibility = View.GONE
            } else {
                ll_walkin_sales_display_name.visibility = View.VISIBLE
                walkin_sales_display_name.text =
                    storeDataRangeOverViewCEOAws.walkinSales.total.displayName
            }

            if (storeDataRangeOverViewCEOAws.walkinSales?.orderCount == null || storeDataRangeOverViewCEOAws.walkinSales.orderCount.displayName.isNullOrEmpty()) {
                ll_walkin_order_count_sales_display.visibility = View.GONE
            } else {
                ll_walkin_order_count_sales_display.visibility = View.VISIBLE
                walkin_order_count_sales_display.text =
                    storeDataRangeOverViewCEOAws.walkinSales.orderCount.displayName
            }

            if (storeDataRangeOverViewCEOAws.walkinSales?.averageTicket == null || storeDataRangeOverViewCEOAws.walkinSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket_display.visibility = View.GONE
            } else {
                ll_average_walkin_ticket_display.visibility = View.VISIBLE
                average_walkin_ticket_display.text =
                    storeDataRangeOverViewCEOAws.walkinSales.averageTicket.displayName
            }


            //  deliver Sales
            if (storeDataRangeOverViewCEOAws.deliverySales?.total == null || storeDataRangeOverViewCEOAws.deliverySales.total.displayName.isNullOrEmpty()) {
                ll_delivery_sales_display_name.visibility = View.GONE
            } else {
                ll_delivery_sales_display_name.visibility = View.VISIBLE
                delivery_sales_display_name.text =
                    storeDataRangeOverViewCEOAws.deliverySales.total.displayName
            }
            if (storeDataRangeOverViewCEOAws.deliverySales?.orderCount == null || storeDataRangeOverViewCEOAws.deliverySales.orderCount.displayName.isNullOrEmpty()) {
                ll_delivery_order_count_display.visibility = View.GONE
            } else {
                ll_delivery_order_count_display.visibility = View.VISIBLE
                delivery_order_count_display.text =
                    storeDataRangeOverViewCEOAws.deliverySales.orderCount.displayName
            }
            if (storeDataRangeOverViewCEOAws.deliverySales?.averageTicket == null || storeDataRangeOverViewCEOAws.deliverySales.averageTicket.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets_display.visibility = View.GONE
            } else {
                ll_delivery_average_tickets_display.visibility = View.VISIBLE
                delivery_average_tickets_display.text =
                    storeDataRangeOverViewCEOAws.deliverySales.averageTicket.displayName
            }


            //  carryout Sales
            if (storeDataRangeOverViewCEOAws.carryoutSales?.total == null || storeDataRangeOverViewCEOAws.carryoutSales.total.displayName.isNullOrEmpty()) {
                ll_carry_out_display_text.visibility = View.GONE
            } else {
                ll_carry_out_display_text.visibility = View.VISIBLE
                carry_out_display_text.text = storeDataRangeOverViewCEOAws.carryoutSales.total.displayName
            }
            if (storeDataRangeOverViewCEOAws.carryoutSales?.orderCount == null || storeDataRangeOverViewCEOAws.carryoutSales.orderCount.displayName.isNullOrEmpty()) {
                ll_carry_outorder_count_display.visibility = View.GONE
            } else {
                ll_carry_outorder_count_display.visibility = View.VISIBLE
                carry_outorder_count_display.text =
                    storeDataRangeOverViewCEOAws.carryoutSales.orderCount.displayName
            }

            if (storeDataRangeOverViewCEOAws.carryoutSales?.averageTicket == null || storeDataRangeOverViewCEOAws.carryoutSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets_display.visibility = View.GONE
            } else {
                ll_carry_average_tickets_display.visibility = View.VISIBLE
                carry_average_tickets_display.text =
                    storeDataRangeOverViewCEOAws.carryoutSales.averageTicket.displayName
            }



            // display null check for olo pcy

            if (storeDataRangeOverViewCEOAws.pcya?.olo == null || storeDataRangeOverViewCEOAws.pcya.olo.displayName.isNullOrEmpty()) {
                olo_pcy_parent.visibility = View.GONE
            } else {
                olo_pcy_parent.visibility = View.GONE
                olo_pcy_display.text = storeDataRangeOverViewCEOAws.pcya.olo.displayName
            }

            if (storeDataRangeOverViewCEOAws.pcya?.phone == null || storeDataRangeOverViewCEOAws.pcya.phone.displayName.isNullOrEmpty()) {
                phone_pcys_parent.visibility = View.GONE
            } else {
                phone_pcys_parent.visibility = View.VISIBLE
                phone_pcys.text =
                    storeDataRangeOverViewCEOAws.pcya.phone.displayName
            }
            if (storeDataRangeOverViewCEOAws.pcya?.walkin == null || storeDataRangeOverViewCEOAws.pcya.walkin.displayName.isNullOrEmpty()) {
                walkin_pcys_parent.visibility = View.GONE
            } else {
                walkin_pcys_parent.visibility = View.VISIBLE
                walkin_pcy.text =
                    storeDataRangeOverViewCEOAws.pcya.walkin.displayName
            }
            if (storeDataRangeOverViewCEOAws.pcya?.delivery == null ||
                storeDataRangeOverViewCEOAws.pcya.delivery.displayName.isNullOrEmpty()
            ) {
                delivery_pcy_parent.visibility = View.GONE
            } else {
                delivery_pcy_parent.visibility = View.VISIBLE
                delivery_pcy.text =
                    storeDataRangeOverViewCEOAws.pcya.delivery.displayName
            }
            if (storeDataRangeOverViewCEOAws.pcya?.carryout == null || storeDataRangeOverViewCEOAws.pcya.carryout.displayName.isNullOrEmpty()) {
                carry_out_pcy_parent.visibility = View.GONE
            } else {
                carry_out_pcy_parent.visibility = View.VISIBLE
                carry_out_pcy.text =
                    storeDataRangeOverViewCEOAws.pcya.carryout.displayName
            }

            /// new
            awus_sales.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewCEOAws.actual?.amount,
                storeDataRangeOverViewCEOAws.actual?.percentage,
                storeDataRangeOverViewCEOAws.actual?.value
            )

            if (storeDataRangeOverViewCEOAws.status?.toString() != null) {
                when {
                    storeDataRangeOverViewCEOAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                        awus_sales.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewCEOAws.status.toString() == resources.getString(R.string.under_limit) -> {
                        awus_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        awus_sales.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // scroll detect

            awus_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (awus_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.awus_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@AWUSKpiActivity,
                                    storeDataRangeOverViewCEOAws.actual?.amount,
                                    storeDataRangeOverViewCEOAws.actual?.percentage,
                                    storeDataRangeOverViewCEOAws.actual?.value
                                )
                            if (storeDataRangeOverViewCEOAws.status?.toString() != null) {
                                when {
                                    storeDataRangeOverViewCEOAws.status.toString() == resources.getString(
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
                                    storeDataRangeOverViewCEOAws.status.toString() == resources.getString(
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
                        y = awus_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            awus_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewCEOAws.goal?.amount,
                storeDataRangeOverViewCEOAws.goal?.percentage,
                storeDataRangeOverViewCEOAws.goal?.value
            )
            awus__variance_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewCEOAws.variance?.amount,
                storeDataRangeOverViewCEOAws.variance?.percentage,
                storeDataRangeOverViewCEOAws.variance?.value
            )

            showAWUSNarrativeData(storeDataRangeOverViewCEOAws.narrative.toString())

            // PCY
            pcy_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewCEOAws.pcya?.total?.actual?.amount,
                storeDataRangeOverViewCEOAws.pcya?.total?.actual?.percentage,
                storeDataRangeOverViewCEOAws.pcya?.total?.actual?.value
            )

            if (storeDataRangeOverViewCEOAws.pcya?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewCEOAws.pcya.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewCEOAws.pcya.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.pcya?.olo?.actual?.amount,
                    storeDataRangeOverViewCEOAws.pcya?.olo?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.pcya?.olo?.actual?.value
                ).isEmpty()
            ) {
                olo_pcy_percentage.visibility = View.GONE
                awus_pcya_error_olo.visibility = View.VISIBLE

            } else {
                olo_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_olo.visibility = View.GONE

                olo_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.pcya?.olo?.actual?.amount,
                    storeDataRangeOverViewCEOAws.pcya?.olo?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.pcya?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.pcya?.phone?.actual?.amount,
                    storeDataRangeOverViewCEOAws.pcya?.phone?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.pcya?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_pcy_percentage.visibility = View.GONE
                awus_pcya_error_phone.visibility = View.VISIBLE

            } else {
                phone_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                phone_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.pcya?.phone?.actual?.amount,
                    storeDataRangeOverViewCEOAws.pcya?.phone?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.pcya?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.pcya?.walkin?.actual?.amount,
                    storeDataRangeOverViewCEOAws.pcya?.walkin?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.pcya?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_percentage.visibility = View.GONE
                awus_pcya_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                walkin_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.pcya?.walkin?.actual?.amount,
                    storeDataRangeOverViewCEOAws.pcya?.walkin?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.pcya?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.pcya?.delivery?.actual?.amount,
                    storeDataRangeOverViewCEOAws.pcya?.delivery?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.pcya?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_percentage.visibility = View.GONE
                awus_pcya_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_delivery.visibility = View.GONE

                delivery_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.pcya?.delivery?.actual?.amount,
                    storeDataRangeOverViewCEOAws.pcya?.delivery?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.pcya?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.pcya?.carryout?.actual?.amount,
                    storeDataRangeOverViewCEOAws.pcya?.carryout?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.pcya?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_percentage.visibility = View.GONE
                awus_pcya_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_carryout.visibility = View.GONE

                carry_out_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.pcya?.carryout?.actual?.amount,
                    storeDataRangeOverViewCEOAws.pcya?.carryout?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.pcya?.carryout?.actual?.value
                )
            }

            // order count
            oc_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewCEOAws.orderCount?.total?.actual?.amount,
                storeDataRangeOverViewCEOAws.orderCount?.total?.actual?.percentage,
                storeDataRangeOverViewCEOAws.orderCount?.total?.actual?.value
            )

            if (storeDataRangeOverViewCEOAws.orderCount?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewCEOAws.orderCount.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewCEOAws.orderCount.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.green))

                    } else -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.orderCount?.olo?.actual?.amount,
                    storeDataRangeOverViewCEOAws.orderCount?.olo?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.orderCount?.olo?.actual?.value
                ).isEmpty()
            ) {
                oc_pcy_percentage.visibility = View.GONE
                awus_oc_error_olo.visibility = View.VISIBLE

            } else {
                oc_pcy_percentage.visibility = View.VISIBLE
                awus_oc_error_olo.visibility = View.GONE

                oc_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.orderCount?.olo?.actual?.amount,
                    storeDataRangeOverViewCEOAws.orderCount?.olo?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.orderCount?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.orderCount?.phone?.actual?.amount,
                    storeDataRangeOverViewCEOAws.orderCount?.phone?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.orderCount?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_oc_percentage.visibility = View.GONE
                awus_oc_error_phone.visibility = View.VISIBLE

            } else {
                phone_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_phone.visibility = View.GONE

                phone_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.orderCount?.phone?.actual?.amount,
                    storeDataRangeOverViewCEOAws.orderCount?.phone?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.orderCount?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.orderCount?.walkin?.actual?.amount,
                    storeDataRangeOverViewCEOAws.orderCount?.walkin?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.orderCount?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_oc_percentage.visibility = View.GONE
                awus_oc_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_walkin.visibility = View.GONE

                walkin_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.orderCount?.walkin?.actual?.amount,
                    storeDataRangeOverViewCEOAws.orderCount?.walkin?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.orderCount?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.orderCount?.delivery?.actual?.amount,
                    storeDataRangeOverViewCEOAws.orderCount?.delivery?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.orderCount?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_oc_percentage.visibility = View.GONE
                awus_oc_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_delivery.visibility = View.GONE

                delivery_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.orderCount?.delivery?.actual?.amount,
                    storeDataRangeOverViewCEOAws.orderCount?.delivery?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.orderCount?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.orderCount?.carryout?.actual?.amount,
                    storeDataRangeOverViewCEOAws.orderCount?.carryout?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.orderCount?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_oc_percentage.visibility = View.GONE
                awus_oc_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_carryout.visibility = View.GONE

                carry_out_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.orderCount?.carryout?.actual?.amount,
                    storeDataRangeOverViewCEOAws.orderCount?.carryout?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.orderCount?.carryout?.actual?.value
                )
            }

            // Average  Tickets
            average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewCEOAws.averageTicket?.total?.actual?.amount,
                storeDataRangeOverViewCEOAws.averageTicket?.total?.actual?.percentage,
                storeDataRangeOverViewCEOAws.averageTicket?.total?.actual?.value
            )

            if (storeDataRangeOverViewCEOAws.averageTicket?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewCEOAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewCEOAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.averageTicket?.olo?.actual?.amount,
                    storeDataRangeOverViewCEOAws.averageTicket?.olo?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.averageTicket?.olo?.actual?.value
                ).isEmpty()
            ) {
                average_olo_percentage.visibility = View.GONE
                awus_avg_error_olo.visibility = View.VISIBLE

            } else {
                average_olo_percentage.visibility = View.VISIBLE
                awus_avg_error_olo.visibility = View.GONE

                average_olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.averageTicket?.olo?.actual?.amount,
                    storeDataRangeOverViewCEOAws.averageTicket?.olo?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.averageTicket?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.averageTicket?.phone?.actual?.amount,
                    storeDataRangeOverViewCEOAws.averageTicket?.phone?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.averageTicket?.phone?.actual?.value
                ).isEmpty()
            ) {
                average_phone_percentage.visibility = View.GONE
                awus_avg_error_phone.visibility = View.VISIBLE

            } else {
                average_phone_percentage.visibility = View.VISIBLE
                awus_avg_error_phone.visibility = View.GONE

                average_phone_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.averageTicket?.phone?.actual?.amount,
                    storeDataRangeOverViewCEOAws.averageTicket?.phone?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.averageTicket?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.averageTicket?.walkin?.actual?.amount,
                    storeDataRangeOverViewCEOAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.averageTicket?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_average_percentage.visibility = View.GONE
                awus_avg_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_average_percentage.visibility = View.VISIBLE
                awus_avg_error_walkin.visibility = View.GONE

                walkin_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.averageTicket?.walkin?.actual?.amount,
                    storeDataRangeOverViewCEOAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.averageTicket?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.averageTicket?.delivery?.actual?.amount,
                    storeDataRangeOverViewCEOAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.averageTicket?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_percentage.visibility = View.GONE
                awus_avg_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_percentage.visibility = View.VISIBLE
                awus_avg_error_delivery.visibility = View.GONE

                delivery_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.averageTicket?.delivery?.actual?.amount,
                    storeDataRangeOverViewCEOAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.averageTicket?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.averageTicket?.carryout?.actual?.amount,
                    storeDataRangeOverViewCEOAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.averageTicket?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_average_percentage.visibility = View.GONE
                awus_avg_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_average_percentage.visibility = View.VISIBLE
                awus_avg_error_carryout.visibility = View.GONE

                carry_out_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.averageTicket?.carryout?.actual?.amount,
                    storeDataRangeOverViewCEOAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.averageTicket?.carryout?.actual?.value
                )
            }


            // OLO sales
            olo_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewCEOAws.oloSales?.total?.actual?.amount,
                storeDataRangeOverViewCEOAws.oloSales?.total?.actual?.percentage,
                storeDataRangeOverViewCEOAws.oloSales?.total?.actual?.value
            )

            if (storeDataRangeOverViewCEOAws.oloSales?.total?.status?.toString() != null) {

                when {
                    storeDataRangeOverViewCEOAws.oloSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewCEOAws.oloSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.oloSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewCEOAws.oloSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.oloSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                olo_percentage.visibility = View.GONE
                awus_olo_sales_error_olo.visibility = View.VISIBLE

            } else {
                olo_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_olo.visibility = View.GONE

                olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.oloSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewCEOAws.oloSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.oloSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.oloSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewCEOAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.oloSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                olo_order_percentage.visibility = View.GONE
                awus_olo_sales_error_oc.visibility = View.VISIBLE

            } else {
                olo_order_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_oc.visibility = View.GONE

                olo_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.oloSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewCEOAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.oloSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewCEOAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.oloSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                olo_average_percentage.visibility = View.GONE
                awus_olo_sales_error_at.visibility = View.VISIBLE

            } else {
                olo_average_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_at.visibility = View.GONE

                olo_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewCEOAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.oloSales?.averageTicket?.actual?.value
                )
            }

            // Phone sales order count
            phone_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewCEOAws.phoneSales?.total?.actual?.amount,
                storeDataRangeOverViewCEOAws.phoneSales?.total?.actual?.percentage,
                storeDataRangeOverViewCEOAws.phoneSales?.total?.actual?.value
            )
            if (storeDataRangeOverViewCEOAws.phoneSales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewCEOAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewCEOAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.phoneSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewCEOAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.phoneSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                phone_pcys_percentage.visibility = View.GONE
                awus_phones_sales_error_olo.visibility = View.VISIBLE

            } else {
                phone_pcys_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_olo.visibility = View.GONE

                phone_pcys_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.phoneSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewCEOAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.phoneSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewCEOAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.phoneSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_phone_ticket_percentage.visibility = View.GONE
                awus_phones_sales_error_at.visibility = View.VISIBLE

            } else {
                average_phone_ticket_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_at.visibility = View.GONE

                average_phone_ticket_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewCEOAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.phoneSales?.averageTicket?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewCEOAws.phoneSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.phoneSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                phone_sales_order_percentage.visibility = View.GONE
                awus_phones_sales_error_oc.visibility = View.VISIBLE

            } else {
                phone_sales_order_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_oc.visibility = View.GONE

                phone_sales_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewCEOAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.oloSales?.orderCount?.actual?.value
                )
            }

            // walk-in sales
            walkin_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewCEOAws.walkinSales?.total?.actual?.amount,
                storeDataRangeOverViewCEOAws.walkinSales?.total?.actual?.percentage,
                storeDataRangeOverViewCEOAws.walkinSales?.total?.actual?.value
            )

            if (storeDataRangeOverViewCEOAws.walkinSales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewCEOAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewCEOAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.walkinSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewCEOAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.walkinSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_value.visibility = View.GONE
                awus_walkins_sales_error_olo.visibility = View.VISIBLE

            } else {
                walkin_pcy_value.visibility = View.VISIBLE
                awus_walkins_sales_error_olo.visibility = View.GONE

                walkin_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.walkinSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewCEOAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.walkinSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewCEOAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.walkinSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                walkin_order_count_value.visibility = View.GONE
                awus_walkins_sales_error_oc.visibility = View.VISIBLE

            } else {
                walkin_order_count_value.visibility = View.VISIBLE
                awus_walkins_sales_error_oc.visibility = View.GONE

                walkin_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewCEOAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.walkinSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewCEOAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.walkinSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_walkin_ticket_value.visibility = View.GONE
                awus_walkins_sales_error_at.visibility = View.VISIBLE

            } else {
                average_walkin_ticket_value.visibility = View.VISIBLE
                awus_walkins_sales_error_at.visibility = View.GONE

                average_walkin_ticket_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewCEOAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.walkinSales?.averageTicket?.actual?.value
                )
            }

            // delivery sales
            delivery_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewCEOAws.deliverySales?.total?.actual?.amount,
                storeDataRangeOverViewCEOAws.deliverySales?.total?.actual?.percentage,
                storeDataRangeOverViewCEOAws.deliverySales?.total?.actual?.value
            )

            if (storeDataRangeOverViewCEOAws.deliverySales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewCEOAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewCEOAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.deliverySales?.pcya?.actual?.amount,
                    storeDataRangeOverViewCEOAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.deliverySales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_value.visibility = View.GONE
                awus_delivery_sales_error_olo.visibility = View.VISIBLE

            } else {
                delivery_pcy_value.visibility = View.VISIBLE
                awus_delivery_sales_error_olo.visibility = View.GONE

                delivery_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.deliverySales?.pcya?.actual?.amount,
                    storeDataRangeOverViewCEOAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.deliverySales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewCEOAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.deliverySales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                delivery_order_count_value.visibility = View.GONE
                awus_delivery_sales_error_oc.visibility = View.VISIBLE

            } else {
                delivery_order_count_value.visibility = View.VISIBLE
                awus_delivery_sales_error_oc.visibility = View.GONE

                delivery_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewCEOAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.deliverySales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewCEOAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.deliverySales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                delivery_average_percentage.visibility = View.GONE
                awus_delivery_sales_error_at.visibility = View.VISIBLE

            } else {
                delivery_average_percentage.visibility = View.VISIBLE
                awus_delivery_sales_error_at.visibility = View.GONE

                delivery_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewCEOAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.deliverySales?.averageTicket?.actual?.value
                )
            }

            // carryout sales
            carryout_average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewCEOAws.carryoutSales?.total?.actual?.amount,
                storeDataRangeOverViewCEOAws.carryoutSales?.total?.actual?.percentage,
                storeDataRangeOverViewCEOAws.carryoutSales?.total?.actual?.value
            )

            if (storeDataRangeOverViewCEOAws.carryoutSales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewCEOAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewCEOAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewCEOAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.carryoutSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_value.visibility = View.GONE
                awus_carryout_sales_error_olo.visibility = View.VISIBLE

            } else {
                carry_out_pcy_value.visibility = View.VISIBLE
                awus_carryout_sales_error_olo.visibility = View.GONE

                carry_out_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewCEOAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.carryoutSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewCEOAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.carryoutSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                carry_outorder_count_value.visibility = View.GONE
                awus_carryout_sales_error_oc.visibility = View.VISIBLE

            } else {
                carry_outorder_count_value.visibility = View.VISIBLE
                awus_carryout_sales_error_oc.visibility = View.GONE

                carry_outorder_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewCEOAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.carryoutSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewCEOAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.carryoutSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                carry_average_tickets_value.visibility = View.GONE
                awus_carryout_sales_error_at.visibility = View.VISIBLE

            } else {
                carry_average_tickets_value.visibility = View.VISIBLE
                awus_carryout_sales_error_at.visibility = View.GONE

                carry_average_tickets_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewCEOAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewCEOAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewCEOAws.carryoutSales?.averageTicket?.actual?.value
                )
            }

        } catch (e: Exception) {
            Logger.error(e.message.toString(),"AWUS Overview Period Range KPI")
        }
    }


    // DO View
    private fun todayViewDOAws(todayDetail: DOOverviewTodayQuery.Do_) {
        val storeDataTodayViewDoAws = todayDetail.kpis?.supervisors?.stores?.today!!.sales

        Logger.info("AWUS Today Query", "AWUS Overview KPI")


        // display name
        awus_display.text = storeDataTodayViewDoAws!!.displayName ?: getString(R.string.awus_text)
        // pcy display name
        pcy_display_name.text =
            storeDataTodayViewDoAws.pcya?.total?.displayName ?: getString(R.string.pcy)
        olo_pcy_display.text =
            storeDataTodayViewDoAws.pcya?.olo?.displayName ?: getString(R.string.olo_pcy)
        phone_pcys.text =
            storeDataTodayViewDoAws.pcya?.phone?.displayName ?: getString(R.string.phone_pcys)
        walkin_pcy.text =
            storeDataTodayViewDoAws.pcya?.walkin?.displayName ?: getString(R.string.walkin_pcy)
        delivery_pcy.text =
            storeDataTodayViewDoAws.pcya?.delivery?.displayName ?: getString(R.string.delivery_pcy)
        delivery_pcy.text =
            storeDataTodayViewDoAws.pcya?.delivery?.displayName ?: getString(R.string.delivery_pcy)
        carry_out_pcy.text =
            storeDataTodayViewDoAws.pcya?.carryout?.displayName ?: getString(R.string.carry_out_pcy)
        // order count display
        order_count_display.text =
            storeDataTodayViewDoAws.orderCount?.total?.displayName
                ?: getString(R.string.order_count)
        olo_order_count.text =
            storeDataTodayViewDoAws.orderCount?.olo?.displayName
                ?: getString(R.string.olo_order_count)
        phone_order_count.text = storeDataTodayViewDoAws.orderCount?.phone?.displayName
            ?: getString(R.string.phone_order_count)
        walkin_order_count.text = storeDataTodayViewDoAws.orderCount?.walkin?.displayName
            ?: getString(R.string.walkin_order_count)
        delivery_order_count.text = storeDataTodayViewDoAws.orderCount?.delivery?.displayName
            ?: getString(R.string.delivery_order_count)
        carry_outorder_count.text = storeDataTodayViewDoAws.orderCount?.carryout?.displayName
            ?: getString(R.string.carry_outorder_count)

        // average ticket count display
        if (storeDataTodayViewDoAws.averageTicket?.total == null || storeDataTodayViewDoAws.averageTicket.total.displayName.isNullOrEmpty()) {
            ll_average_ticket_display_name.visibility = View.GONE
        } else {
            ll_average_ticket_display_name.visibility = View.VISIBLE
            average_ticket_display_name.text =
                storeDataTodayViewDoAws.averageTicket.total.displayName
        }


        if (storeDataTodayViewDoAws.averageTicket?.olo == null || storeDataTodayViewDoAws.averageTicket.olo.displayName.isNullOrEmpty()) {
            ll_olo_average_ticket.visibility = View.GONE
        } else {
            ll_olo_average_ticket.visibility = View.VISIBLE
            olo_average_ticket.text =
                storeDataTodayViewDoAws.averageTicket.olo.displayName
        }

        if (storeDataTodayViewDoAws.averageTicket?.phone == null || storeDataTodayViewDoAws.averageTicket.phone.displayName.isNullOrEmpty()) {
            ll_average_phone_ticket.visibility = View.GONE
        } else {
            ll_average_phone_ticket.visibility = View.VISIBLE
            average_phone_ticket.text =
                storeDataTodayViewDoAws.averageTicket.phone.displayName
        }

        if (storeDataTodayViewDoAws.averageTicket?.walkin == null || storeDataTodayViewDoAws.averageTicket.walkin.displayName.isNullOrEmpty()) {
            ll_average_walkin_ticket.visibility = View.GONE
        } else {
            ll_average_walkin_ticket.visibility = View.VISIBLE
            average_walkin_ticket.text =
                storeDataTodayViewDoAws.averageTicket.walkin.displayName
        }

        if (storeDataTodayViewDoAws.averageTicket?.delivery == null || storeDataTodayViewDoAws.averageTicket.delivery.displayName.isNullOrEmpty()) {
            ll_delivery_average_tickets.visibility = View.GONE
        } else {
            ll_delivery_average_tickets.visibility = View.VISIBLE
            delivery_average_tickets.text =
                storeDataTodayViewDoAws.averageTicket.delivery.displayName
        }

        if (storeDataTodayViewDoAws.averageTicket?.carryout == null || storeDataTodayViewDoAws.averageTicket.carryout.displayName.isNullOrEmpty()) {
            ll_carry_average_tickets.visibility = View.GONE
        } else {
            ll_carry_average_tickets.visibility = View.VISIBLE
            carry_average_tickets.text =
                storeDataTodayViewDoAws.averageTicket.carryout.displayName
        }


        // olo sales display
        if (storeDataTodayViewDoAws.oloSales?.total == null || storeDataTodayViewDoAws.oloSales.total.displayName.isNullOrEmpty()) {
            ll_olo_sales_display_name.visibility = View.GONE
        } else {
            ll_olo_sales_display_name.visibility = View.VISIBLE
            olo_sales_display_name.text =
                storeDataTodayViewDoAws.oloSales.total.displayName
        }

        if (storeDataTodayViewDoAws.oloSales?.pcya == null || storeDataTodayViewDoAws.oloSales.pcya.displayName.isNullOrEmpty()) {
            olo_sales_pcy_parent.visibility = View.GONE
        } else {
            olo_sales_pcy_parent.visibility = View.VISIBLE
            olo_pcy.text =
                storeDataTodayViewDoAws.oloSales.pcya.displayName
        }

        if (storeDataTodayViewDoAws.oloSales?.orderCount == null || storeDataTodayViewDoAws.oloSales.orderCount.displayName.isNullOrEmpty()) {
            ll_olo_order_count_sales_display.visibility = View.GONE
        } else {
            ll_olo_order_count_sales_display.visibility = View.VISIBLE
            olo_order_count_sales_display.text =
                storeDataTodayViewDoAws.oloSales.orderCount.displayName
        }

        if (storeDataTodayViewDoAws.oloSales?.averageTicket == null || storeDataTodayViewDoAws.oloSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_olo_average_ticket_sales_display.visibility = View.GONE
        } else {
            ll_olo_average_ticket_sales_display.visibility = View.VISIBLE
            olo_average_ticket_sales_display.text =
                storeDataTodayViewDoAws.oloSales.averageTicket.displayName
        }


        // phone sales display
        if (storeDataTodayViewDoAws.phoneSales?.total == null || storeDataTodayViewDoAws.phoneSales.total.displayName.isNullOrEmpty()) {
            ll_phone_sales_display_name.visibility = View.GONE
        } else {
            ll_phone_sales_display_name.visibility = View.VISIBLE
            phone_sales_display_name.text =
                storeDataTodayViewDoAws.phoneSales.total.displayName
        }


        if (storeDataTodayViewDoAws.phoneSales?.orderCount == null || storeDataTodayViewDoAws.phoneSales.orderCount.displayName.isNullOrEmpty()) {
            ll_phone_order_count_display.visibility = View.GONE
        } else {
            ll_phone_order_count_display.visibility = View.VISIBLE
            phone_order_count_display.text =
                storeDataTodayViewDoAws.phoneSales.orderCount.displayName
        }

        if (storeDataTodayViewDoAws.phoneSales?.averageTicket == null || storeDataTodayViewDoAws.phoneSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_average_phone_ticket_display.visibility = View.GONE
        } else {
            ll_average_phone_ticket_display.visibility = View.VISIBLE
            average_phone_ticket_display.text =
                storeDataTodayViewDoAws.phoneSales.averageTicket.displayName
        }


        // walk-in Sales
        if (storeDataTodayViewDoAws.walkinSales?.total == null || storeDataTodayViewDoAws.walkinSales.total.displayName.isNullOrEmpty()) {
            ll_walkin_sales_display_name.visibility = View.GONE
        } else {
            ll_walkin_sales_display_name.visibility = View.VISIBLE
            walkin_sales_display_name.text =
                storeDataTodayViewDoAws.walkinSales.total.displayName
        }

        if (storeDataTodayViewDoAws.walkinSales?.orderCount == null || storeDataTodayViewDoAws.walkinSales.orderCount.displayName.isNullOrEmpty()) {
            ll_walkin_order_count_sales_display.visibility = View.GONE
        } else {
            ll_walkin_order_count_sales_display.visibility = View.VISIBLE
            walkin_order_count_sales_display.text =
                storeDataTodayViewDoAws.walkinSales.orderCount.displayName
        }

        if (storeDataTodayViewDoAws.walkinSales?.averageTicket == null || storeDataTodayViewDoAws.walkinSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_average_walkin_ticket_display.visibility = View.GONE
        } else {
            ll_average_walkin_ticket_display.visibility = View.VISIBLE
            average_walkin_ticket_display.text =
                storeDataTodayViewDoAws.walkinSales.averageTicket.displayName
        }

        //  deliver Sales
        if (storeDataTodayViewDoAws.deliverySales?.total == null || storeDataTodayViewDoAws.deliverySales.total.displayName.isNullOrEmpty()) {
            ll_delivery_sales_display_name.visibility = View.GONE
        } else {
            ll_delivery_sales_display_name.visibility = View.VISIBLE
            delivery_sales_display_name.text =
                storeDataTodayViewDoAws.deliverySales.total.displayName
        }
        if (storeDataTodayViewDoAws.deliverySales?.orderCount == null || storeDataTodayViewDoAws.deliverySales.orderCount.displayName.isNullOrEmpty()) {
            ll_delivery_order_count_display.visibility = View.GONE
        } else {
            ll_delivery_order_count_display.visibility = View.VISIBLE
            delivery_order_count_display.text =
                storeDataTodayViewDoAws.deliverySales.orderCount.displayName
        }
        if (storeDataTodayViewDoAws.deliverySales?.averageTicket == null || storeDataTodayViewDoAws.deliverySales.averageTicket.displayName.isNullOrEmpty()) {
            ll_delivery_average_tickets_display.visibility = View.GONE
        } else {
            ll_delivery_average_tickets_display.visibility = View.VISIBLE
            delivery_average_tickets_display.text =
                storeDataTodayViewDoAws.deliverySales.averageTicket.displayName
        }


        //  carryout Sales
        if (storeDataTodayViewDoAws.carryoutSales?.total == null || storeDataTodayViewDoAws.carryoutSales.total.displayName.isNullOrEmpty()) {
            ll_carry_out_display_text.visibility = View.GONE
        } else {
            ll_carry_out_display_text.visibility = View.VISIBLE
            carry_out_display_text.text =
                storeDataTodayViewDoAws.carryoutSales.total.displayName
        }
        if (storeDataTodayViewDoAws.carryoutSales?.orderCount == null || storeDataTodayViewDoAws.carryoutSales.orderCount.displayName.isNullOrEmpty()) {
            ll_carry_outorder_count_display.visibility = View.GONE
        } else {
            ll_carry_outorder_count_display.visibility = View.VISIBLE
            carry_outorder_count_display.text =
                storeDataTodayViewDoAws.carryoutSales.orderCount.displayName
        }

        if (storeDataTodayViewDoAws.carryoutSales?.averageTicket == null || storeDataTodayViewDoAws.carryoutSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_carry_average_tickets_display.visibility = View.GONE
        } else {
            ll_carry_average_tickets_display.visibility = View.VISIBLE
            carry_average_tickets_display.text =
                storeDataTodayViewDoAws.carryoutSales.averageTicket.displayName
        }




        if (storeDataTodayViewDoAws.actual?.value?.isNaN() == false && storeDataTodayViewDoAws.status?.toString() != null) {
            awus_sales.text = getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    storeDataTodayViewDoAws.actual.value
                )
            )
            when {
                storeDataTodayViewDoAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                    awus_sales.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewDoAws.status.toString() == resources.getString(R.string.under_limit) -> {
                    awus_sales.setTextColor(getColor(R.color.green))
                }
                else -> {
                    awus_sales.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        // scroll detect

        awus_scroll_parent.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 0f
                override fun onScrollChanged() {
                    if (awus_scroll_parent.scrollY > y) {
                        parent_data_on_scroll_linear.visibility = View.VISIBLE
                        parent_data_on_scroll_view.visibility = View.VISIBLE
                        level_two_scroll_data_action.text =
                            storeDataTodayViewDoAws.displayName ?: getString(R.string.awus_text)
                        if (storeDataTodayViewDoAws.actual?.value?.isNaN() == false && storeDataTodayViewDoAws.status?.toString() != null) {
                            level_two_scroll_data_action_value.text =
                                getString(R.string.dollar_text).plus(
                                    Validation().dollarFormatting(
                                        storeDataTodayViewDoAws.actual.value
                                    )
                                )
                            when {
                                storeDataTodayViewDoAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                                    level_two_scroll_data_action_value.setTextColor(getColor(R.color.red))
                                    level_two_scroll_data_action_value.setCompoundDrawablesWithIntrinsicBounds(
                                        0,
                                        0,
                                        R.drawable.red_circle,
                                        0
                                    )
                                }
                                storeDataTodayViewDoAws.status.toString() == resources.getString(R.string.under_limit) -> {
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
                    y = awus_scroll_parent.scrollY.toFloat()
                    if (y <= 0) {
                        parent_data_on_scroll_linear.visibility = View.INVISIBLE
                        parent_data_on_scroll_view.visibility = View.GONE
                    }
                }
            })

        showAWUSNarrativeData(storeDataTodayViewDoAws.narrative.toString())

        awus_goal_value.text =
            if (storeDataTodayViewDoAws.goal?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(storeDataTodayViewDoAws.goal.value)
            ) else ""
        awus__variance_value.text =
            if (storeDataTodayViewDoAws.variance?.value?.isNaN() == false) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(storeDataTodayViewDoAws.variance.value)
            ) else ""

        // PCY
        if (storeDataTodayViewDoAws.pcya?.total?.actual?.percentage?.isNaN() == false && storeDataTodayViewDoAws.pcya.total.status?.toString() != null) {
            pcy_percentage.text =
                Validation().ignoreZeroAfterDecimal(storeDataTodayViewDoAws.pcya.total.actual.percentage)
                    .plus(
                        getString(
                            R.string.percentage_text
                        )
                    )
            when {
                storeDataTodayViewDoAws.pcya.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    pcy_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewDoAws.pcya.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    pcy_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    pcy_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewDoAws.pcya?.olo?.actual?.percentage?.isNaN() == false) {
            olo_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.pcya.olo.actual.percentage
            )).plus(getString(R.string.percentage_text))
            olo_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_olo.visibility = View.GONE

        } else {
            olo_pcy_percentage.visibility = View.GONE
            awus_pcya_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.pcya?.phone?.actual?.percentage?.isNaN() == false) {
            phone_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.pcya.phone.actual.percentage
            )).plus(getString(R.string.percentage_text))
            phone_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_phone.visibility = View.GONE

        } else {
            phone_pcy_percentage.visibility = View.GONE
            awus_pcya_error_phone.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.pcya?.walkin?.actual?.percentage?.isNaN() == false) {
            walkin_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.pcya.walkin.actual.percentage
            )).plus(getString(R.string.percentage_text))
            walkin_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_phone.visibility = View.GONE

        } else {
            walkin_pcy_percentage.visibility = View.GONE
            awus_pcya_error_walkin.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.pcya?.delivery?.actual?.percentage?.isNaN() == false) {
            delivery_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.pcya.delivery.actual.percentage
            )).plus(getString(R.string.percentage_text))
            delivery_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_delivery.visibility = View.GONE

        } else {
            delivery_pcy_percentage.visibility = View.GONE
            awus_pcya_error_delivery.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.pcya?.carryout?.actual?.percentage?.isNaN() == false) {
            carry_out_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.pcya.carryout.actual.percentage
            )).plus(getString(R.string.percentage_text))
            carry_out_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_carryout.visibility = View.GONE

        } else {
            carry_out_pcy_percentage.visibility = View.GONE
            awus_pcya_error_carryout.visibility = View.VISIBLE

        }

        // order count

        if (storeDataTodayViewDoAws.orderCount?.total?.actual?.value?.isNaN() == false && storeDataTodayViewDoAws.orderCount.total.status?.toString() != null) {
            oc_percentage.text =
                Validation().ignoreZeroAfterDecimal(storeDataTodayViewDoAws.orderCount.total.actual.value)
            when {
                storeDataTodayViewDoAws.orderCount.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    oc_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewDoAws.orderCount.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    oc_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    oc_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewDoAws.orderCount?.olo?.actual?.value?.isNaN() == false) {
            oc_pcy_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.orderCount.olo.actual.value
            )
            oc_pcy_percentage.visibility = View.VISIBLE
            awus_oc_error_olo.visibility = View.GONE

        } else {
            oc_pcy_percentage.visibility = View.GONE
            awus_oc_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.orderCount?.phone?.actual?.value?.isNaN() == false) {
            phone_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.orderCount.phone.actual.value
            )
            phone_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_phone.visibility = View.GONE

        } else {
            phone_oc_percentage.visibility = View.GONE
            awus_oc_error_phone.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.orderCount?.walkin?.actual?.value?.isNaN() == false) {
            walkin_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.orderCount.walkin.actual.value
            )
            walkin_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_walkin.visibility = View.GONE

        } else {
            walkin_oc_percentage.visibility = View.GONE
            awus_oc_error_walkin.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.orderCount?.delivery?.actual?.value?.isNaN() == false) {
            delivery_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.orderCount.delivery.actual.value
            )
            delivery_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_delivery.visibility = View.GONE

        } else {
            delivery_oc_percentage.visibility = View.GONE
            awus_oc_error_delivery.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.orderCount?.carryout?.actual?.value?.isNaN() == false) {
            carry_out_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.orderCount.carryout.actual.value
            )
            carry_out_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_carryout.visibility = View.GONE

        } else {
            carry_out_oc_percentage.visibility = View.GONE
            awus_oc_error_carryout.visibility = View.VISIBLE

        }

        // Average  Tickets
        if (storeDataTodayViewDoAws.averageTicket?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewDoAws.averageTicket.total.status?.toString() != null) {
            average_percentage.text =
                if (!storeDataTodayViewDoAws.averageTicket.total.actual.amount.isNaN()) getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(storeDataTodayViewDoAws.averageTicket.total.actual.amount)
                ) else ""

            when {
                storeDataTodayViewDoAws.averageTicket.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewDoAws.averageTicket.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    average_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewDoAws.averageTicket?.olo?.actual?.value?.isNaN() == false) {
            average_olo_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.averageTicket.olo.actual.value
            )
            average_olo_percentage.visibility = View.VISIBLE
            awus_avg_error_olo.visibility = View.GONE

        } else {
            average_olo_percentage.visibility = View.GONE
            awus_avg_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.averageTicket?.phone?.actual?.value?.isNaN() == false) {
            average_phone_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.averageTicket.phone.actual.value
            )
            average_phone_percentage.visibility = View.VISIBLE
            awus_avg_error_phone.visibility = View.GONE

        } else {
            average_phone_percentage.visibility = View.GONE
            awus_avg_error_phone.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.averageTicket?.walkin?.actual?.value?.isNaN() == false) {
            walkin_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.averageTicket.walkin.actual.value
            )
            walkin_average_percentage.visibility = View.VISIBLE
            awus_avg_error_walkin.visibility = View.GONE

        } else {
            walkin_average_percentage.visibility = View.GONE
            awus_avg_error_walkin.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.averageTicket?.delivery?.actual?.value?.isNaN() == false) {
            delivery_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.averageTicket.delivery.actual.value
            )
            delivery_percentage.visibility = View.VISIBLE
            awus_avg_error_delivery.visibility = View.GONE

        } else {
            delivery_percentage.visibility = View.GONE
            awus_avg_error_delivery.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.averageTicket?.carryout?.actual?.value?.isNaN() == false) {
            carry_out_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.averageTicket.carryout.actual.value
            )
            carry_out_average_percentage.visibility = View.GONE
            awus_avg_error_carryout.visibility = View.VISIBLE

        } else {
            carry_out_average_percentage.visibility = View.GONE
            awus_avg_error_carryout.visibility = View.VISIBLE

        }

        // OLO sales
        if (storeDataTodayViewDoAws.oloSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewDoAws.oloSales.total.status?.toString() != null) {
            olo_sales_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewDoAws.oloSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewDoAws.oloSales.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    olo_sales_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewDoAws.oloSales.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    olo_sales_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    olo_sales_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewDoAws.oloSales?.pcya?.actual?.percentage?.isNaN() == false) {
            olo_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.oloSales.pcya.actual.percentage
            )
            olo_percentage.visibility = View.VISIBLE
            awus_olo_sales_error_olo.visibility = View.GONE

        } else {
            olo_percentage.visibility = View.GONE
            awus_olo_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.oloSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            olo_order_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.oloSales.orderCount.actual.percentage
            )
            olo_order_percentage.visibility = View.VISIBLE
            awus_olo_sales_error_oc.visibility = View.GONE

        } else {
            olo_order_percentage.visibility = View.GONE
            awus_olo_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.oloSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            olo_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.oloSales.averageTicket.actual.percentage
            )
            olo_average_percentage.visibility = View.VISIBLE
            awus_olo_sales_error_at.visibility = View.GONE

        } else {
            olo_average_percentage.visibility = View.GONE
            awus_olo_sales_error_at.visibility = View.VISIBLE

        }

        // Phone sales order count
        if (storeDataTodayViewDoAws.phoneSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewDoAws.phoneSales.total.status?.toString() != null) {
            phone_sales_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewDoAws.phoneSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewDoAws.phoneSales.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    phone_sales_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewDoAws.phoneSales.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    phone_sales_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    phone_sales_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewDoAws.phoneSales?.pcya?.actual?.percentage?.isNaN() == false) {
            phone_pcys_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.phoneSales.pcya.actual.percentage
            )
            phone_pcys_percentage.visibility = View.VISIBLE
            awus_phones_sales_error_olo.visibility = View.GONE

        } else {
            phone_pcys_percentage.visibility = View.GONE
            awus_phones_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.phoneSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            phone_sales_order_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.phoneSales.orderCount.actual.percentage
            )
            phone_sales_order_percentage.visibility = View.VISIBLE
            awus_phones_sales_error_oc.visibility = View.GONE

        } else {
            phone_sales_order_percentage.visibility = View.GONE
            awus_phones_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.phoneSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            average_phone_ticket_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.phoneSales.averageTicket.actual.percentage
            )
            average_phone_ticket_percentage.visibility = View.VISIBLE
            awus_phones_sales_error_at.visibility = View.GONE

        } else {
            average_phone_ticket_percentage.visibility = View.GONE
            awus_phones_sales_error_at.visibility = View.VISIBLE

        }

        // walk-in sales

        if (storeDataTodayViewDoAws.walkinSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewDoAws.walkinSales.total.status?.toString() != null) {
            walkin_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewDoAws.walkinSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewDoAws.walkinSales.total.status.toString() == resources.getString(R.string.out_of_range) -> {
                    walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    walkin_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewDoAws.walkinSales.total.status.toString() == resources.getString(R.string.under_limit) -> {
                    walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    walkin_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    walkin_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewDoAws.walkinSales?.pcya?.actual?.percentage?.isNaN() == false) {
            walkin_pcy_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.walkinSales.pcya.actual.percentage
            )
            walkin_pcy_value.visibility = View.VISIBLE
            awus_walkins_sales_error_olo.visibility = View.GONE

        } else {
            walkin_pcy_value.visibility = View.GONE
            awus_walkins_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.walkinSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            walkin_order_count_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.walkinSales.orderCount.actual.percentage
            )
            walkin_order_count_value.visibility = View.VISIBLE
            awus_walkins_sales_error_oc.visibility = View.GONE

        } else {
            walkin_order_count_value.visibility = View.GONE
            awus_walkins_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.walkinSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            average_walkin_ticket_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.walkinSales.averageTicket.actual.percentage
            )
            average_walkin_ticket_value.visibility = View.VISIBLE
            awus_walkins_sales_error_at.visibility = View.GONE

        } else {
            average_walkin_ticket_value.visibility = View.GONE
            awus_walkins_sales_error_at.visibility = View.VISIBLE

        }

        // delivery sales
        if (storeDataTodayViewDoAws.deliverySales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewDoAws.deliverySales.total.status?.toString() != null) {
            delivery_sales_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewDoAws.deliverySales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewDoAws.deliverySales.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    delivery_sales_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewDoAws.deliverySales.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    delivery_sales_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    delivery_sales_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewDoAws.deliverySales?.pcya?.actual?.percentage?.isNaN() == false) {
            delivery_pcy_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.deliverySales.pcya.actual.percentage
            )
            delivery_pcy_value.visibility = View.VISIBLE
            awus_delivery_sales_error_olo.visibility = View.GONE

        } else {
            delivery_pcy_value.visibility = View.GONE
            awus_delivery_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.deliverySales?.orderCount?.actual?.percentage?.isNaN() == false) {
            delivery_order_count_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.deliverySales.orderCount.actual.percentage
            )
            delivery_order_count_value.visibility = View.VISIBLE
            awus_walkins_sales_error_at.visibility = View.GONE

        } else {
            delivery_order_count_value.visibility = View.GONE
            awus_delivery_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.deliverySales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            delivery_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.deliverySales.averageTicket.actual.percentage
            )
            delivery_average_percentage.visibility = View.VISIBLE
            awus_walkins_sales_error_at.visibility = View.GONE


        } else {
            delivery_average_percentage.visibility = View.GONE
            awus_delivery_sales_error_at.visibility = View.VISIBLE

        }


        // carryout sales

        if (storeDataTodayViewDoAws.carryoutSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewDoAws.carryoutSales.total.status?.toString() != null) {
            carryout_average_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewDoAws.carryoutSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewDoAws.carryoutSales.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    carryout_average_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewDoAws.carryoutSales.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    carryout_average_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    carryout_average_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewDoAws.carryoutSales?.pcya?.actual?.percentage?.isNaN() == false) {
            carry_out_pcy_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.carryoutSales.pcya.actual.percentage
            )
            carry_out_pcy_value.visibility = View.VISIBLE
            awus_carryout_sales_error_olo.visibility = View.GONE

        } else {
            carry_out_pcy_value.visibility = View.GONE
            awus_carryout_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.carryoutSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            carry_outorder_count_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.carryoutSales.orderCount.actual.percentage
            )
            carry_outorder_count_value.visibility = View.VISIBLE
            awus_carryout_sales_error_oc.visibility = View.GONE

        } else {
            carry_outorder_count_value.visibility = View.GONE
            awus_carryout_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewDoAws.carryoutSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            carry_average_tickets_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewDoAws.carryoutSales.averageTicket.actual.percentage
            )
            carry_average_tickets_value.visibility = View.VISIBLE
            awus_carryout_sales_error_at.visibility = View.GONE

        } else {
            carry_average_tickets_value.visibility = View.GONE
            awus_carryout_sales_error_at.visibility = View.VISIBLE

        }


    }

    private fun yesterdayViewDOAws(todayDetail: DOOverviewYesterdayQuery.Do_) {
        try {
            val storeDataYesterdayViewDoAws =
                todayDetail.kpis?.supervisors?.stores?.yesterday!!.sales

            Logger.info("AWUS Yesterday", "AWUS Overview KPI")

            awus_display.text = getString(R.string.awus_text)
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewDoAws?.phoneSales?.pcya?.displayName,
                phone_sales_pcys_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewDoAws?.walkinSales?.pcya?.displayName,
                walkin_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewDoAws?.deliverySales?.pcya?.displayName,
                delivery_sales_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewDoAws?.carryoutSales?.pcya?.displayName,
                carry_out_sales_pcy_parent
            )

            pcy_display_name.text =
                storeDataYesterdayViewDoAws!!.pcya?.total?.displayName ?: getString(R.string.pcy)
            phone_pcys.text =
                storeDataYesterdayViewDoAws.pcya?.phone?.displayName
                    ?: getString(R.string.phone_pcys)
            walkin_pcy.text =
                storeDataYesterdayViewDoAws.pcya?.walkin?.displayName
                    ?: getString(R.string.walkin_pcy)
            delivery_pcy.text =
                storeDataYesterdayViewDoAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            delivery_pcy.text =
                storeDataYesterdayViewDoAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            carry_out_pcy.text =
                storeDataYesterdayViewDoAws.pcya?.carryout?.displayName
                    ?: getString(R.string.carry_out_pcy)
            // order count display
            order_count_display.text =
                storeDataYesterdayViewDoAws.orderCount?.total?.displayName
                    ?: getString(R.string.order_count)
            olo_order_count.text =
                storeDataYesterdayViewDoAws.orderCount?.olo?.displayName
                    ?: getString(R.string.olo_order_count)
            phone_order_count.text = storeDataYesterdayViewDoAws.orderCount?.phone?.displayName
                ?: getString(R.string.phone_order_count)
            walkin_order_count.text = storeDataYesterdayViewDoAws.orderCount?.walkin?.displayName
                ?: getString(R.string.walkin_order_count)
            delivery_order_count.text =
                storeDataYesterdayViewDoAws.orderCount?.delivery?.displayName
                    ?: getString(R.string.delivery_order_count)
            carry_outorder_count.text =
                storeDataYesterdayViewDoAws.orderCount?.carryout?.displayName
                    ?: getString(R.string.carry_outorder_count)

            // average ticket count display
            if (storeDataYesterdayViewDoAws.averageTicket?.total == null || storeDataYesterdayViewDoAws.averageTicket.total.displayName.isNullOrEmpty()) {
                ll_average_ticket_display_name.visibility = View.GONE
            } else {
                ll_average_ticket_display_name.visibility = View.VISIBLE
                average_ticket_display_name.text =
                    storeDataYesterdayViewDoAws.averageTicket.total.displayName
            }


            if (storeDataYesterdayViewDoAws.averageTicket?.olo == null || storeDataYesterdayViewDoAws.averageTicket.olo.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket.visibility = View.GONE
            } else {
                ll_olo_average_ticket.visibility = View.VISIBLE
                olo_average_ticket.text =
                    storeDataYesterdayViewDoAws.averageTicket.olo.displayName
            }

            if (storeDataYesterdayViewDoAws.averageTicket?.phone == null || storeDataYesterdayViewDoAws.averageTicket.phone.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket.visibility = View.GONE
            } else {
                ll_average_phone_ticket.visibility = View.VISIBLE
                average_phone_ticket.text =
                    storeDataYesterdayViewDoAws.averageTicket.phone.displayName
            }

            if (storeDataYesterdayViewDoAws.averageTicket?.walkin == null || storeDataYesterdayViewDoAws.averageTicket.walkin.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket.visibility = View.GONE
            } else {
                ll_average_walkin_ticket.visibility = View.VISIBLE
                average_walkin_ticket.text =
                    storeDataYesterdayViewDoAws.averageTicket.walkin.displayName
            }

            if (storeDataYesterdayViewDoAws.averageTicket?.delivery == null || storeDataYesterdayViewDoAws.averageTicket.delivery.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets.visibility = View.GONE
            } else {
                ll_delivery_average_tickets.visibility = View.VISIBLE
                delivery_average_tickets.text =
                    storeDataYesterdayViewDoAws.averageTicket.delivery.displayName
            }

            if (storeDataYesterdayViewDoAws.averageTicket?.carryout == null || storeDataYesterdayViewDoAws.averageTicket.carryout.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets.visibility = View.GONE
            } else {
                ll_carry_average_tickets.visibility = View.VISIBLE
                carry_average_tickets.text =
                    storeDataYesterdayViewDoAws.averageTicket.carryout.displayName
            }

            // olo sales display
            if (storeDataYesterdayViewDoAws.oloSales?.total == null || storeDataYesterdayViewDoAws.oloSales.total.displayName.isNullOrEmpty()) {
                ll_olo_sales_display_name.visibility = View.GONE
            } else {
                ll_olo_sales_display_name.visibility = View.VISIBLE
                olo_sales_display_name.text =
                    storeDataYesterdayViewDoAws.oloSales.total.displayName
            }

            if (storeDataYesterdayViewDoAws.oloSales?.pcya == null || storeDataYesterdayViewDoAws.oloSales.pcya.displayName.isNullOrEmpty()) {
                olo_sales_pcy_parent.visibility = View.GONE
            } else {
                olo_sales_pcy_parent.visibility = View.VISIBLE
                olo_pcy.text =
                    storeDataYesterdayViewDoAws.oloSales.pcya.displayName
            }

            if (storeDataYesterdayViewDoAws.oloSales?.orderCount == null || storeDataYesterdayViewDoAws.oloSales.orderCount.displayName.isNullOrEmpty()) {
                ll_olo_order_count_sales_display.visibility = View.GONE
            } else {
                ll_olo_order_count_sales_display.visibility = View.VISIBLE
                olo_order_count_sales_display.text =
                    storeDataYesterdayViewDoAws.oloSales.orderCount.displayName
            }

            if (storeDataYesterdayViewDoAws.oloSales?.averageTicket == null || storeDataYesterdayViewDoAws.oloSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket_sales_display.visibility = View.GONE
            } else {
                ll_olo_average_ticket_sales_display.visibility = View.VISIBLE
                olo_average_ticket_sales_display.text =
                    storeDataYesterdayViewDoAws.oloSales.averageTicket.displayName
            }


            // phone sales display
            if (storeDataYesterdayViewDoAws.phoneSales?.total == null || storeDataYesterdayViewDoAws.phoneSales.total.displayName.isNullOrEmpty()) {
                ll_phone_sales_display_name.visibility = View.GONE
            } else {
                ll_phone_sales_display_name.visibility = View.VISIBLE
                phone_sales_display_name.text =
                    storeDataYesterdayViewDoAws.phoneSales.total.displayName
            }


            if (storeDataYesterdayViewDoAws.phoneSales?.orderCount == null || storeDataYesterdayViewDoAws.phoneSales.orderCount.displayName.isNullOrEmpty()) {
                ll_phone_order_count_display.visibility = View.GONE
            } else {
                ll_phone_order_count_display.visibility = View.VISIBLE
                phone_order_count_display.text =
                    storeDataYesterdayViewDoAws.phoneSales.orderCount.displayName
            }

            if (storeDataYesterdayViewDoAws.phoneSales?.averageTicket == null || storeDataYesterdayViewDoAws.phoneSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket_display.visibility = View.GONE
            } else {
                ll_average_phone_ticket_display.visibility = View.VISIBLE
                average_phone_ticket_display.text =
                    storeDataYesterdayViewDoAws.phoneSales.averageTicket.displayName
            }


            // walk-in Sales
            if (storeDataYesterdayViewDoAws.walkinSales?.total == null || storeDataYesterdayViewDoAws.walkinSales.total.displayName.isNullOrEmpty()) {
                ll_walkin_sales_display_name.visibility = View.GONE
            } else {
                ll_walkin_sales_display_name.visibility = View.VISIBLE
                walkin_sales_display_name.text =
                    storeDataYesterdayViewDoAws.walkinSales.total.displayName
            }

            if (storeDataYesterdayViewDoAws.walkinSales?.orderCount == null || storeDataYesterdayViewDoAws.walkinSales.orderCount.displayName.isNullOrEmpty()) {
                ll_walkin_order_count_sales_display.visibility = View.GONE
            } else {
                ll_walkin_order_count_sales_display.visibility = View.VISIBLE
                walkin_order_count_sales_display.text =
                    storeDataYesterdayViewDoAws.walkinSales.orderCount.displayName
            }

            if (storeDataYesterdayViewDoAws.walkinSales?.averageTicket == null || storeDataYesterdayViewDoAws.walkinSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket_display.visibility = View.GONE
            } else {
                ll_average_walkin_ticket_display.visibility = View.VISIBLE
                average_walkin_ticket_display.text =
                    storeDataYesterdayViewDoAws.walkinSales.averageTicket.displayName
            }


            //  deliver Sales
            if (storeDataYesterdayViewDoAws.deliverySales?.total == null || storeDataYesterdayViewDoAws.deliverySales.total.displayName.isNullOrEmpty()) {
                ll_delivery_sales_display_name.visibility = View.GONE
            } else {
                ll_delivery_sales_display_name.visibility = View.VISIBLE
                delivery_sales_display_name.text =
                    storeDataYesterdayViewDoAws.deliverySales.total.displayName
            }
            if (storeDataYesterdayViewDoAws.deliverySales?.orderCount == null || storeDataYesterdayViewDoAws.deliverySales.orderCount.displayName.isNullOrEmpty()) {
                ll_delivery_order_count_display.visibility = View.GONE
            } else {
                ll_delivery_order_count_display.visibility = View.VISIBLE
                delivery_order_count_display.text =
                    storeDataYesterdayViewDoAws.deliverySales.orderCount.displayName
            }
            if (storeDataYesterdayViewDoAws.deliverySales?.averageTicket == null || storeDataYesterdayViewDoAws.deliverySales.averageTicket.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets_display.visibility = View.GONE
            } else {
                ll_delivery_average_tickets_display.visibility = View.VISIBLE
                delivery_average_tickets_display.text =
                    storeDataYesterdayViewDoAws.deliverySales.averageTicket.displayName
            }


            //  carryout Sales
            if (storeDataYesterdayViewDoAws.carryoutSales?.total == null || storeDataYesterdayViewDoAws.carryoutSales.total.displayName.isNullOrEmpty()) {
                ll_carry_out_display_text.visibility = View.GONE
            } else {
                ll_carry_out_display_text.visibility = View.VISIBLE
                carry_out_display_text.text =
                    storeDataYesterdayViewDoAws.carryoutSales.total.displayName
            }
            if (storeDataYesterdayViewDoAws.carryoutSales?.orderCount == null || storeDataYesterdayViewDoAws.carryoutSales.orderCount.displayName.isNullOrEmpty()) {
                ll_carry_outorder_count_display.visibility = View.GONE
            } else {
                ll_carry_outorder_count_display.visibility = View.VISIBLE
                carry_outorder_count_display.text =
                    storeDataYesterdayViewDoAws.carryoutSales.orderCount.displayName
            }

            if (storeDataYesterdayViewDoAws.carryoutSales?.averageTicket == null || storeDataYesterdayViewDoAws.carryoutSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets_display.visibility = View.GONE
            } else {
                ll_carry_average_tickets_display.visibility = View.VISIBLE
                carry_average_tickets_display.text =
                    storeDataYesterdayViewDoAws.carryoutSales.averageTicket.displayName
            }



            // display null check for olo pcy

            if (storeDataYesterdayViewDoAws.pcya?.olo == null || storeDataYesterdayViewDoAws.pcya.olo.displayName.isNullOrEmpty()) {
                olo_pcy_parent.visibility = View.GONE
            } else {
                olo_pcy_parent.visibility = View.GONE
                olo_pcy_display.text = storeDataYesterdayViewDoAws.pcya.olo.displayName
            }

            if (storeDataYesterdayViewDoAws.pcya?.phone == null || storeDataYesterdayViewDoAws.pcya.phone.displayName.isNullOrEmpty()) {
                phone_pcys_parent.visibility = View.GONE
            } else {
                phone_pcys_parent.visibility = View.VISIBLE
                phone_pcys.text =
                    storeDataYesterdayViewDoAws.pcya.phone.displayName
            }
            if (storeDataYesterdayViewDoAws.pcya?.walkin == null || storeDataYesterdayViewDoAws.pcya.walkin.displayName.isNullOrEmpty()) {
                walkin_pcys_parent.visibility = View.GONE
            } else {
                walkin_pcys_parent.visibility = View.VISIBLE
                walkin_pcy.text =
                    storeDataYesterdayViewDoAws.pcya.walkin.displayName
            }
            if (storeDataYesterdayViewDoAws.pcya?.delivery == null ||
                storeDataYesterdayViewDoAws.pcya.delivery.displayName.isNullOrEmpty()
            ) {
                delivery_pcy_parent.visibility = View.GONE
            } else {
                delivery_pcy_parent.visibility = View.VISIBLE
                delivery_pcy.text =
                    storeDataYesterdayViewDoAws.pcya.delivery.displayName
            }
            if (storeDataYesterdayViewDoAws.pcya?.carryout == null || storeDataYesterdayViewDoAws.pcya.carryout.displayName.isNullOrEmpty()) {
                carry_out_pcy_parent.visibility = View.GONE
            } else {
                carry_out_pcy_parent.visibility = View.VISIBLE
                carry_out_pcy.text =
                    storeDataYesterdayViewDoAws.pcya.carryout.displayName
            }

            /// new
            awus_sales.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewDoAws.actual?.amount,
                storeDataYesterdayViewDoAws.actual?.percentage,
                storeDataYesterdayViewDoAws.actual?.value
            )

            if (storeDataYesterdayViewDoAws.status?.toString() != null) {
                when {
                    storeDataYesterdayViewDoAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                        awus_sales.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewDoAws.status.toString() == resources.getString(R.string.under_limit) -> {
                        awus_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        awus_sales.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // scroll detect

            awus_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (awus_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.awus_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@AWUSKpiActivity,
                                    storeDataYesterdayViewDoAws.actual?.amount,
                                    storeDataYesterdayViewDoAws.actual?.percentage,
                                    storeDataYesterdayViewDoAws.actual?.value
                                )
                            if (storeDataYesterdayViewDoAws.status?.toString() != null) {
                                when {
                                    storeDataYesterdayViewDoAws.status.toString() == resources.getString(
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
                                    storeDataYesterdayViewDoAws.status.toString() == resources.getString(
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
                        y = awus_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            awus_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewDoAws.goal?.amount,
                storeDataYesterdayViewDoAws.goal?.percentage,
                storeDataYesterdayViewDoAws.goal?.value
            )
            awus__variance_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewDoAws.variance?.amount,
                storeDataYesterdayViewDoAws.variance?.percentage,
                storeDataYesterdayViewDoAws.variance?.value
            )

            showAWUSNarrativeData(storeDataYesterdayViewDoAws.narrative.toString())

            // PCY
            pcy_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewDoAws.pcya?.total?.actual?.amount,
                storeDataYesterdayViewDoAws.pcya?.total?.actual?.percentage,
                storeDataYesterdayViewDoAws.pcya?.total?.actual?.value
            )

            if (storeDataYesterdayViewDoAws.pcya?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewDoAws.pcya.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewDoAws.pcya.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.pcya?.olo?.actual?.amount,
                    storeDataYesterdayViewDoAws.pcya?.olo?.actual?.percentage,
                    storeDataYesterdayViewDoAws.pcya?.olo?.actual?.value
                ).isEmpty()
            ) {
                olo_pcy_percentage.visibility = View.GONE
                awus_pcya_error_olo.visibility = View.VISIBLE

            } else {
                olo_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_olo.visibility = View.GONE

                olo_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.pcya?.olo?.actual?.amount,
                    storeDataYesterdayViewDoAws.pcya?.olo?.actual?.percentage,
                    storeDataYesterdayViewDoAws.pcya?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.pcya?.phone?.actual?.amount,
                    storeDataYesterdayViewDoAws.pcya?.phone?.actual?.percentage,
                    storeDataYesterdayViewDoAws.pcya?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_pcy_percentage.visibility = View.GONE
                awus_pcya_error_phone.visibility = View.VISIBLE

            } else {
                phone_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                phone_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.pcya?.phone?.actual?.amount,
                    storeDataYesterdayViewDoAws.pcya?.phone?.actual?.percentage,
                    storeDataYesterdayViewDoAws.pcya?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.pcya?.walkin?.actual?.amount,
                    storeDataYesterdayViewDoAws.pcya?.walkin?.actual?.percentage,
                    storeDataYesterdayViewDoAws.pcya?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_percentage.visibility = View.GONE
                awus_pcya_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                walkin_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.pcya?.walkin?.actual?.amount,
                    storeDataYesterdayViewDoAws.pcya?.walkin?.actual?.percentage,
                    storeDataYesterdayViewDoAws.pcya?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.pcya?.delivery?.actual?.amount,
                    storeDataYesterdayViewDoAws.pcya?.delivery?.actual?.percentage,
                    storeDataYesterdayViewDoAws.pcya?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_percentage.visibility = View.GONE
                awus_pcya_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_delivery.visibility = View.GONE

                delivery_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.pcya?.delivery?.actual?.amount,
                    storeDataYesterdayViewDoAws.pcya?.delivery?.actual?.percentage,
                    storeDataYesterdayViewDoAws.pcya?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.pcya?.carryout?.actual?.amount,
                    storeDataYesterdayViewDoAws.pcya?.carryout?.actual?.percentage,
                    storeDataYesterdayViewDoAws.pcya?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_percentage.visibility = View.GONE
                awus_pcya_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_carryout.visibility = View.GONE

                carry_out_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.pcya?.carryout?.actual?.amount,
                    storeDataYesterdayViewDoAws.pcya?.carryout?.actual?.percentage,
                    storeDataYesterdayViewDoAws.pcya?.carryout?.actual?.value
                )
            }

            // order count
            oc_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewDoAws.orderCount?.total?.actual?.amount,
                storeDataYesterdayViewDoAws.orderCount?.total?.actual?.percentage,
                storeDataYesterdayViewDoAws.orderCount?.total?.actual?.value
            )

            if (storeDataYesterdayViewDoAws.orderCount?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewDoAws.orderCount.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewDoAws.orderCount.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.green))

                    } else -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.orderCount?.olo?.actual?.amount,
                    storeDataYesterdayViewDoAws.orderCount?.olo?.actual?.percentage,
                    storeDataYesterdayViewDoAws.orderCount?.olo?.actual?.value
                ).isEmpty()
            ) {
                oc_pcy_percentage.visibility = View.GONE
                awus_oc_error_olo.visibility = View.VISIBLE

            } else {
                oc_pcy_percentage.visibility = View.VISIBLE
                awus_oc_error_olo.visibility = View.GONE

                oc_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.orderCount?.olo?.actual?.amount,
                    storeDataYesterdayViewDoAws.orderCount?.olo?.actual?.percentage,
                    storeDataYesterdayViewDoAws.orderCount?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.orderCount?.phone?.actual?.amount,
                    storeDataYesterdayViewDoAws.orderCount?.phone?.actual?.percentage,
                    storeDataYesterdayViewDoAws.orderCount?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_oc_percentage.visibility = View.GONE
                awus_oc_error_phone.visibility = View.VISIBLE

            } else {
                phone_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_phone.visibility = View.GONE

                phone_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.orderCount?.phone?.actual?.amount,
                    storeDataYesterdayViewDoAws.orderCount?.phone?.actual?.percentage,
                    storeDataYesterdayViewDoAws.orderCount?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.orderCount?.walkin?.actual?.amount,
                    storeDataYesterdayViewDoAws.orderCount?.walkin?.actual?.percentage,
                    storeDataYesterdayViewDoAws.orderCount?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_oc_percentage.visibility = View.GONE
                awus_oc_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_walkin.visibility = View.GONE

                walkin_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.orderCount?.walkin?.actual?.amount,
                    storeDataYesterdayViewDoAws.orderCount?.walkin?.actual?.percentage,
                    storeDataYesterdayViewDoAws.orderCount?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.orderCount?.delivery?.actual?.amount,
                    storeDataYesterdayViewDoAws.orderCount?.delivery?.actual?.percentage,
                    storeDataYesterdayViewDoAws.orderCount?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_oc_percentage.visibility = View.GONE
                awus_oc_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_delivery.visibility = View.GONE

                delivery_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.orderCount?.delivery?.actual?.amount,
                    storeDataYesterdayViewDoAws.orderCount?.delivery?.actual?.percentage,
                    storeDataYesterdayViewDoAws.orderCount?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.orderCount?.carryout?.actual?.amount,
                    storeDataYesterdayViewDoAws.orderCount?.carryout?.actual?.percentage,
                    storeDataYesterdayViewDoAws.orderCount?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_oc_percentage.visibility = View.GONE
                awus_oc_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_carryout.visibility = View.GONE

                carry_out_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.orderCount?.carryout?.actual?.amount,
                    storeDataYesterdayViewDoAws.orderCount?.carryout?.actual?.percentage,
                    storeDataYesterdayViewDoAws.orderCount?.carryout?.actual?.value
                )
            }

            // Average  Tickets
            average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewDoAws.averageTicket?.total?.actual?.amount,
                storeDataYesterdayViewDoAws.averageTicket?.total?.actual?.percentage,
                storeDataYesterdayViewDoAws.averageTicket?.total?.actual?.value
            )

            if (storeDataYesterdayViewDoAws.averageTicket?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewDoAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewDoAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.averageTicket?.olo?.actual?.amount,
                    storeDataYesterdayViewDoAws.averageTicket?.olo?.actual?.percentage,
                    storeDataYesterdayViewDoAws.averageTicket?.olo?.actual?.value
                ).isEmpty()
            ) {
                average_olo_percentage.visibility = View.GONE
                awus_avg_error_olo.visibility = View.VISIBLE

            } else {
                average_olo_percentage.visibility = View.VISIBLE
                awus_avg_error_olo.visibility = View.GONE

                average_olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.averageTicket?.olo?.actual?.amount,
                    storeDataYesterdayViewDoAws.averageTicket?.olo?.actual?.percentage,
                    storeDataYesterdayViewDoAws.averageTicket?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.averageTicket?.phone?.actual?.amount,
                    storeDataYesterdayViewDoAws.averageTicket?.phone?.actual?.percentage,
                    storeDataYesterdayViewDoAws.averageTicket?.phone?.actual?.value
                ).isEmpty()
            ) {
                average_phone_percentage.visibility = View.GONE
                awus_avg_error_phone.visibility = View.VISIBLE

            } else {
                average_phone_percentage.visibility = View.VISIBLE
                awus_avg_error_phone.visibility = View.GONE

                average_phone_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.averageTicket?.phone?.actual?.amount,
                    storeDataYesterdayViewDoAws.averageTicket?.phone?.actual?.percentage,
                    storeDataYesterdayViewDoAws.averageTicket?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.averageTicket?.walkin?.actual?.amount,
                    storeDataYesterdayViewDoAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataYesterdayViewDoAws.averageTicket?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_average_percentage.visibility = View.GONE
                awus_avg_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_average_percentage.visibility = View.VISIBLE
                awus_avg_error_walkin.visibility = View.GONE

                walkin_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.averageTicket?.walkin?.actual?.amount,
                    storeDataYesterdayViewDoAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataYesterdayViewDoAws.averageTicket?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.averageTicket?.delivery?.actual?.amount,
                    storeDataYesterdayViewDoAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataYesterdayViewDoAws.averageTicket?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_percentage.visibility = View.GONE
                awus_avg_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_percentage.visibility = View.VISIBLE
                awus_avg_error_delivery.visibility = View.GONE

                delivery_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.averageTicket?.delivery?.actual?.amount,
                    storeDataYesterdayViewDoAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataYesterdayViewDoAws.averageTicket?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.averageTicket?.carryout?.actual?.amount,
                    storeDataYesterdayViewDoAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataYesterdayViewDoAws.averageTicket?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_average_percentage.visibility = View.GONE
                awus_avg_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_average_percentage.visibility = View.VISIBLE
                awus_avg_error_carryout.visibility = View.GONE

                carry_out_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.averageTicket?.carryout?.actual?.amount,
                    storeDataYesterdayViewDoAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataYesterdayViewDoAws.averageTicket?.carryout?.actual?.value
                )
            }

            // OLO sales
            olo_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewDoAws.oloSales?.total?.actual?.amount,
                storeDataYesterdayViewDoAws.oloSales?.total?.actual?.percentage,
                storeDataYesterdayViewDoAws.oloSales?.total?.actual?.value
            )

            if (storeDataYesterdayViewDoAws.oloSales?.total?.status?.toString() != null) {

                when {
                    storeDataYesterdayViewDoAws.oloSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewDoAws.oloSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.oloSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewDoAws.oloSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewDoAws.oloSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                olo_percentage.visibility = View.GONE
                awus_olo_sales_error_olo.visibility = View.VISIBLE

            } else {
                olo_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_olo.visibility = View.GONE

                olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.oloSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewDoAws.oloSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewDoAws.oloSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.oloSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewDoAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewDoAws.oloSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                olo_order_percentage.visibility = View.GONE
                awus_olo_sales_error_oc.visibility = View.VISIBLE

            } else {
                olo_order_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_oc.visibility = View.GONE

                olo_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.oloSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewDoAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewDoAws.oloSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewDoAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewDoAws.oloSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                olo_average_percentage.visibility = View.GONE
                awus_olo_sales_error_at.visibility = View.VISIBLE

            } else {
                olo_average_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_at.visibility = View.GONE

                olo_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewDoAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewDoAws.oloSales?.averageTicket?.actual?.value
                )
            }

            // Phone sales order count
            phone_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewDoAws.phoneSales?.total?.actual?.amount,
                storeDataYesterdayViewDoAws.phoneSales?.total?.actual?.percentage,
                storeDataYesterdayViewDoAws.phoneSales?.total?.actual?.value
            )
            if (storeDataYesterdayViewDoAws.phoneSales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewDoAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewDoAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.phoneSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewDoAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewDoAws.phoneSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                phone_pcys_percentage.visibility = View.GONE
                awus_phones_sales_error_olo.visibility = View.VISIBLE

            } else {
                phone_pcys_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_olo.visibility = View.GONE

                phone_pcys_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.phoneSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewDoAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewDoAws.phoneSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewDoAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewDoAws.phoneSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_phone_ticket_percentage.visibility = View.GONE
                awus_phones_sales_error_at.visibility = View.VISIBLE

            } else {
                average_phone_ticket_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_at.visibility = View.GONE

                average_phone_ticket_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewDoAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewDoAws.phoneSales?.averageTicket?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewDoAws.phoneSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewDoAws.phoneSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                phone_sales_order_percentage.visibility = View.GONE
                awus_phones_sales_error_oc.visibility = View.VISIBLE

            } else {
                phone_sales_order_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_oc.visibility = View.GONE

                phone_sales_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewDoAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewDoAws.oloSales?.orderCount?.actual?.value
                )
            }

            // walk-in sales
            walkin_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewDoAws.walkinSales?.total?.actual?.amount,
                storeDataYesterdayViewDoAws.walkinSales?.total?.actual?.percentage,
                storeDataYesterdayViewDoAws.walkinSales?.total?.actual?.value
            )

            if (storeDataYesterdayViewDoAws.walkinSales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewDoAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewDoAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.walkinSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewDoAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewDoAws.walkinSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_value.visibility = View.GONE
                awus_walkins_sales_error_olo.visibility = View.VISIBLE

            } else {
                walkin_pcy_value.visibility = View.VISIBLE
                awus_walkins_sales_error_olo.visibility = View.GONE

                walkin_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.walkinSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewDoAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewDoAws.walkinSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewDoAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewDoAws.walkinSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                walkin_order_count_value.visibility = View.GONE
                awus_walkins_sales_error_oc.visibility = View.VISIBLE

            } else {
                walkin_order_count_value.visibility = View.VISIBLE
                awus_walkins_sales_error_oc.visibility = View.GONE

                walkin_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewDoAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewDoAws.walkinSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewDoAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewDoAws.walkinSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_walkin_ticket_value.visibility = View.GONE
                awus_walkins_sales_error_at.visibility = View.VISIBLE

            } else {
                average_walkin_ticket_value.visibility = View.VISIBLE
                awus_walkins_sales_error_at.visibility = View.GONE

                average_walkin_ticket_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewDoAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewDoAws.walkinSales?.averageTicket?.actual?.value
                )
            }

            // delivery sales
            delivery_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewDoAws.deliverySales?.total?.actual?.amount,
                storeDataYesterdayViewDoAws.deliverySales?.total?.actual?.percentage,
                storeDataYesterdayViewDoAws.deliverySales?.total?.actual?.value
            )

            if (storeDataYesterdayViewDoAws.deliverySales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewDoAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewDoAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.deliverySales?.pcya?.actual?.amount,
                    storeDataYesterdayViewDoAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewDoAws.deliverySales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_value.visibility = View.GONE
                awus_delivery_sales_error_olo.visibility = View.VISIBLE

            } else {
                delivery_pcy_value.visibility = View.VISIBLE
                awus_delivery_sales_error_olo.visibility = View.GONE

                delivery_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.deliverySales?.pcya?.actual?.amount,
                    storeDataYesterdayViewDoAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewDoAws.deliverySales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewDoAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewDoAws.deliverySales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                delivery_order_count_value.visibility = View.GONE
                awus_delivery_sales_error_oc.visibility = View.VISIBLE

            } else {
                delivery_order_count_value.visibility = View.VISIBLE
                awus_delivery_sales_error_oc.visibility = View.GONE

                delivery_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewDoAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewDoAws.deliverySales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewDoAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewDoAws.deliverySales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                delivery_average_percentage.visibility = View.GONE
                awus_delivery_sales_error_at.visibility = View.VISIBLE

            } else {
                delivery_average_percentage.visibility = View.VISIBLE
                awus_delivery_sales_error_at.visibility = View.GONE

                delivery_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewDoAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewDoAws.deliverySales?.averageTicket?.actual?.value
                )
            }

            // carryout sales
            carryout_average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewDoAws.carryoutSales?.total?.actual?.amount,
                storeDataYesterdayViewDoAws.carryoutSales?.total?.actual?.percentage,
                storeDataYesterdayViewDoAws.carryoutSales?.total?.actual?.value
            )

            if (storeDataYesterdayViewDoAws.carryoutSales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewDoAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewDoAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewDoAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewDoAws.carryoutSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_value.visibility = View.GONE
                awus_carryout_sales_error_olo.visibility = View.VISIBLE

            } else {
                carry_out_pcy_value.visibility = View.VISIBLE
                awus_carryout_sales_error_olo.visibility = View.GONE

                carry_out_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewDoAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewDoAws.carryoutSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewDoAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewDoAws.carryoutSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                carry_outorder_count_value.visibility = View.GONE
                awus_carryout_sales_error_oc.visibility = View.VISIBLE

            } else {
                carry_outorder_count_value.visibility = View.VISIBLE
                awus_carryout_sales_error_oc.visibility = View.GONE

                carry_outorder_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewDoAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewDoAws.carryoutSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewDoAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewDoAws.carryoutSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                carry_average_tickets_value.visibility = View.GONE
                awus_carryout_sales_error_at.visibility = View.VISIBLE

            } else {
                carry_average_tickets_value.visibility = View.VISIBLE
                awus_carryout_sales_error_at.visibility = View.GONE

                carry_average_tickets_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewDoAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewDoAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewDoAws.carryoutSales?.averageTicket?.actual?.value
                )
            }
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"AWUS Overview Yesterday KPI")
        }
    }

    private fun rangeOverViewDOAws(todayDetail: DOOverviewRangeQuery.Do_) {
        try {
            val storeDataRangeOverViewDoAws = todayDetail.kpis?.supervisors?.stores?.period!!.sales
            Logger.info("AWUS Period Range", "AWUS Overview KPI")


            awus_display.text = getString(R.string.awus_text)
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeOverViewDoAws?.phoneSales?.pcya?.displayName,
                phone_sales_pcys_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeOverViewDoAws?.walkinSales?.pcya?.displayName,
                walkin_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeOverViewDoAws?.deliverySales?.pcya?.displayName,
                delivery_sales_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeOverViewDoAws?.carryoutSales?.pcya?.displayName,
                carry_out_sales_pcy_parent
            )

            pcy_display_name.text =
                storeDataRangeOverViewDoAws!!.pcya?.total?.displayName ?: getString(R.string.pcy)
            phone_pcys.text =
                storeDataRangeOverViewDoAws.pcya?.phone?.displayName
                    ?: getString(R.string.phone_pcys)
            walkin_pcy.text =
                storeDataRangeOverViewDoAws.pcya?.walkin?.displayName
                    ?: getString(R.string.walkin_pcy)
            delivery_pcy.text =
                storeDataRangeOverViewDoAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            delivery_pcy.text =
                storeDataRangeOverViewDoAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            carry_out_pcy.text =
                storeDataRangeOverViewDoAws.pcya?.carryout?.displayName
                    ?: getString(R.string.carry_out_pcy)
            // order count display
            order_count_display.text =
                storeDataRangeOverViewDoAws.orderCount?.total?.displayName
                    ?: getString(R.string.order_count)
            olo_order_count.text =
                storeDataRangeOverViewDoAws.orderCount?.olo?.displayName
                    ?: getString(R.string.olo_order_count)
            phone_order_count.text = storeDataRangeOverViewDoAws.orderCount?.phone?.displayName
                ?: getString(R.string.phone_order_count)
            walkin_order_count.text = storeDataRangeOverViewDoAws.orderCount?.walkin?.displayName
                ?: getString(R.string.walkin_order_count)
            delivery_order_count.text =
                storeDataRangeOverViewDoAws.orderCount?.delivery?.displayName
                    ?: getString(R.string.delivery_order_count)
            carry_outorder_count.text =
                storeDataRangeOverViewDoAws.orderCount?.carryout?.displayName
                    ?: getString(R.string.carry_outorder_count)

            // average ticket count display
            if (storeDataRangeOverViewDoAws.averageTicket?.total == null || storeDataRangeOverViewDoAws.averageTicket.total.displayName.isNullOrEmpty()) {
                ll_average_ticket_display_name.visibility = View.GONE
            } else {
                ll_average_ticket_display_name.visibility = View.VISIBLE
                average_ticket_display_name.text =
                    storeDataRangeOverViewDoAws.averageTicket.total.displayName
            }


            if (storeDataRangeOverViewDoAws.averageTicket?.olo == null || storeDataRangeOverViewDoAws.averageTicket.olo.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket.visibility = View.GONE
            } else {
                ll_olo_average_ticket.visibility = View.VISIBLE
                olo_average_ticket.text =
                    storeDataRangeOverViewDoAws.averageTicket.olo.displayName
            }

            if (storeDataRangeOverViewDoAws.averageTicket?.phone == null || storeDataRangeOverViewDoAws.averageTicket.phone.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket.visibility = View.GONE
            } else {
                ll_average_phone_ticket.visibility = View.VISIBLE
                average_phone_ticket.text =
                    storeDataRangeOverViewDoAws.averageTicket.phone.displayName
            }

            if (storeDataRangeOverViewDoAws.averageTicket?.walkin == null || storeDataRangeOverViewDoAws.averageTicket.walkin.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket.visibility = View.GONE
            } else {
                ll_average_walkin_ticket.visibility = View.VISIBLE
                average_walkin_ticket.text =
                    storeDataRangeOverViewDoAws.averageTicket.walkin.displayName
            }

            if (storeDataRangeOverViewDoAws.averageTicket?.delivery == null || storeDataRangeOverViewDoAws.averageTicket.delivery.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets.visibility = View.GONE
            } else {
                ll_delivery_average_tickets.visibility = View.VISIBLE
                delivery_average_tickets.text =
                    storeDataRangeOverViewDoAws.averageTicket.delivery.displayName
            }

            if (storeDataRangeOverViewDoAws.averageTicket?.carryout == null || storeDataRangeOverViewDoAws.averageTicket.carryout.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets.visibility = View.GONE
            } else {
                ll_carry_average_tickets.visibility = View.VISIBLE
                carry_average_tickets.text =
                    storeDataRangeOverViewDoAws.averageTicket.carryout.displayName
            }

            // olo sales display
            if (storeDataRangeOverViewDoAws.oloSales?.total == null || storeDataRangeOverViewDoAws.oloSales.total.displayName.isNullOrEmpty()) {
                ll_olo_sales_display_name.visibility = View.GONE
            } else {
                ll_olo_sales_display_name.visibility = View.VISIBLE
                olo_sales_display_name.text =
                    storeDataRangeOverViewDoAws.oloSales.total.displayName
            }

            if (storeDataRangeOverViewDoAws.oloSales?.pcya == null || storeDataRangeOverViewDoAws.oloSales.pcya.displayName.isNullOrEmpty()) {
                olo_sales_pcy_parent.visibility = View.GONE
            } else {
                olo_sales_pcy_parent.visibility = View.VISIBLE
                olo_pcy.text =
                    storeDataRangeOverViewDoAws.oloSales.pcya.displayName
            }

            if (storeDataRangeOverViewDoAws.oloSales?.orderCount == null || storeDataRangeOverViewDoAws.oloSales.orderCount.displayName.isNullOrEmpty()) {
                ll_olo_order_count_sales_display.visibility = View.GONE
            } else {
                ll_olo_order_count_sales_display.visibility = View.VISIBLE
                olo_order_count_sales_display.text =
                    storeDataRangeOverViewDoAws.oloSales.orderCount.displayName
            }

            if (storeDataRangeOverViewDoAws.oloSales?.averageTicket == null || storeDataRangeOverViewDoAws.oloSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket_sales_display.visibility = View.GONE
            } else {
                ll_olo_average_ticket_sales_display.visibility = View.VISIBLE
                olo_average_ticket_sales_display.text =
                    storeDataRangeOverViewDoAws.oloSales.averageTicket.displayName
            }


            // phone sales display
            if (storeDataRangeOverViewDoAws.phoneSales?.total == null || storeDataRangeOverViewDoAws.phoneSales.total.displayName.isNullOrEmpty()) {
                ll_phone_sales_display_name.visibility = View.GONE
            } else {
                ll_phone_sales_display_name.visibility = View.VISIBLE
                phone_sales_display_name.text =
                    storeDataRangeOverViewDoAws.phoneSales.total.displayName
            }


            if (storeDataRangeOverViewDoAws.phoneSales?.orderCount == null || storeDataRangeOverViewDoAws.phoneSales.orderCount.displayName.isNullOrEmpty()) {
                ll_phone_order_count_display.visibility = View.GONE
            } else {
                ll_phone_order_count_display.visibility = View.VISIBLE
                phone_order_count_display.text =
                    storeDataRangeOverViewDoAws.phoneSales.orderCount.displayName
            }

            if (storeDataRangeOverViewDoAws.phoneSales?.averageTicket == null || storeDataRangeOverViewDoAws.phoneSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket_display.visibility = View.GONE
            } else {
                ll_average_phone_ticket_display.visibility = View.VISIBLE
                average_phone_ticket_display.text =
                    storeDataRangeOverViewDoAws.phoneSales.averageTicket.displayName
            }


            // walk-in Sales
            if (storeDataRangeOverViewDoAws.walkinSales?.total == null || storeDataRangeOverViewDoAws.walkinSales.total.displayName.isNullOrEmpty()) {
                ll_walkin_sales_display_name.visibility = View.GONE
            } else {
                ll_walkin_sales_display_name.visibility = View.VISIBLE
                walkin_sales_display_name.text =
                    storeDataRangeOverViewDoAws.walkinSales.total.displayName
            }

            if (storeDataRangeOverViewDoAws.walkinSales?.orderCount == null || storeDataRangeOverViewDoAws.walkinSales.orderCount.displayName.isNullOrEmpty()) {
                ll_walkin_order_count_sales_display.visibility = View.GONE
            } else {
                ll_walkin_order_count_sales_display.visibility = View.VISIBLE
                walkin_order_count_sales_display.text =
                    storeDataRangeOverViewDoAws.walkinSales.orderCount.displayName
            }

            if (storeDataRangeOverViewDoAws.walkinSales?.averageTicket == null || storeDataRangeOverViewDoAws.walkinSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket_display.visibility = View.GONE
            } else {
                ll_average_walkin_ticket_display.visibility = View.VISIBLE
                average_walkin_ticket_display.text =
                    storeDataRangeOverViewDoAws.walkinSales.averageTicket.displayName
            }


            //  deliver Sales
            if (storeDataRangeOverViewDoAws.deliverySales?.total == null || storeDataRangeOverViewDoAws.deliverySales.total.displayName.isNullOrEmpty()) {
                ll_delivery_sales_display_name.visibility = View.GONE
            } else {
                ll_delivery_sales_display_name.visibility = View.VISIBLE
                delivery_sales_display_name.text =
                    storeDataRangeOverViewDoAws.deliverySales.total.displayName
            }
            if (storeDataRangeOverViewDoAws.deliverySales?.orderCount == null || storeDataRangeOverViewDoAws.deliverySales.orderCount.displayName.isNullOrEmpty()) {
                ll_delivery_order_count_display.visibility = View.GONE
            } else {
                ll_delivery_order_count_display.visibility = View.VISIBLE
                delivery_order_count_display.text =
                    storeDataRangeOverViewDoAws.deliverySales.orderCount.displayName
            }
            if (storeDataRangeOverViewDoAws.deliverySales?.averageTicket == null || storeDataRangeOverViewDoAws.deliverySales.averageTicket.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets_display.visibility = View.GONE
            } else {
                ll_delivery_average_tickets_display.visibility = View.VISIBLE
                delivery_average_tickets_display.text =
                    storeDataRangeOverViewDoAws.deliverySales.averageTicket.displayName
            }


            //  carryout Sales
            if (storeDataRangeOverViewDoAws.carryoutSales?.total == null || storeDataRangeOverViewDoAws.carryoutSales.total.displayName.isNullOrEmpty()) {
                ll_carry_out_display_text.visibility = View.GONE
            } else {
                ll_carry_out_display_text.visibility = View.VISIBLE
                carry_out_display_text.text =
                    storeDataRangeOverViewDoAws.carryoutSales.total.displayName
            }
            if (storeDataRangeOverViewDoAws.carryoutSales?.orderCount == null || storeDataRangeOverViewDoAws.carryoutSales.orderCount.displayName.isNullOrEmpty()) {
                ll_carry_outorder_count_display.visibility = View.GONE
            } else {
                ll_carry_outorder_count_display.visibility = View.VISIBLE
                carry_outorder_count_display.text =
                    storeDataRangeOverViewDoAws.carryoutSales.orderCount.displayName
            }

            if (storeDataRangeOverViewDoAws.carryoutSales?.averageTicket == null || storeDataRangeOverViewDoAws.carryoutSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets_display.visibility = View.GONE
            } else {
                ll_carry_average_tickets_display.visibility = View.VISIBLE
                carry_average_tickets_display.text =
                    storeDataRangeOverViewDoAws.carryoutSales.averageTicket.displayName
            }



            // display null check for olo pcy

            if (storeDataRangeOverViewDoAws.pcya?.olo == null || storeDataRangeOverViewDoAws.pcya.olo.displayName.isNullOrEmpty()) {
                olo_pcy_parent.visibility = View.GONE
            } else {
                olo_pcy_parent.visibility = View.GONE
                olo_pcy_display.text = storeDataRangeOverViewDoAws.pcya.olo.displayName
            }

            if (storeDataRangeOverViewDoAws.pcya?.phone == null || storeDataRangeOverViewDoAws.pcya.phone.displayName.isNullOrEmpty()) {
                phone_pcys_parent.visibility = View.GONE
            } else {
                phone_pcys_parent.visibility = View.VISIBLE
                phone_pcys.text =
                    storeDataRangeOverViewDoAws.pcya.phone.displayName
            }
            if (storeDataRangeOverViewDoAws.pcya?.walkin == null || storeDataRangeOverViewDoAws.pcya.walkin.displayName.isNullOrEmpty()) {
                walkin_pcys_parent.visibility = View.GONE
            } else {
                walkin_pcys_parent.visibility = View.VISIBLE
                walkin_pcy.text =
                    storeDataRangeOverViewDoAws.pcya.walkin.displayName
            }
            if (storeDataRangeOverViewDoAws.pcya?.delivery == null ||
                storeDataRangeOverViewDoAws.pcya.delivery.displayName.isNullOrEmpty()
            ) {
                delivery_pcy_parent.visibility = View.GONE
            } else {
                delivery_pcy_parent.visibility = View.VISIBLE
                delivery_pcy.text =
                    storeDataRangeOverViewDoAws.pcya.delivery.displayName
            }
            if (storeDataRangeOverViewDoAws.pcya?.carryout == null || storeDataRangeOverViewDoAws.pcya.carryout.displayName.isNullOrEmpty()) {
                carry_out_pcy_parent.visibility = View.GONE
            } else {
                carry_out_pcy_parent.visibility = View.VISIBLE
                carry_out_pcy.text =
                    storeDataRangeOverViewDoAws.pcya.carryout.displayName
            }

            /// new
            awus_sales.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewDoAws.actual?.amount,
                storeDataRangeOverViewDoAws.actual?.percentage,
                storeDataRangeOverViewDoAws.actual?.value
            )

            if (storeDataRangeOverViewDoAws.status?.toString() != null) {
                when {
                    storeDataRangeOverViewDoAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                        awus_sales.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewDoAws.status.toString() == resources.getString(R.string.under_limit) -> {
                        awus_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        awus_sales.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // scroll detect

            awus_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (awus_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.awus_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@AWUSKpiActivity,
                                    storeDataRangeOverViewDoAws.actual?.amount,
                                    storeDataRangeOverViewDoAws.actual?.percentage,
                                    storeDataRangeOverViewDoAws.actual?.value
                                )
                            if (storeDataRangeOverViewDoAws.status?.toString() != null) {
                                when {
                                    storeDataRangeOverViewDoAws.status.toString() == resources.getString(
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
                                    storeDataRangeOverViewDoAws.status.toString() == resources.getString(
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
                        y = awus_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            awus_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewDoAws.goal?.amount,
                storeDataRangeOverViewDoAws.goal?.percentage,
                storeDataRangeOverViewDoAws.goal?.value
            )
            awus__variance_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewDoAws.variance?.amount,
                storeDataRangeOverViewDoAws.variance?.percentage,
                storeDataRangeOverViewDoAws.variance?.value
            )

            showAWUSNarrativeData(storeDataRangeOverViewDoAws.narrative.toString())

            // PCA
            pcy_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewDoAws.pcya?.total?.actual?.amount,
                storeDataRangeOverViewDoAws.pcya?.total?.actual?.percentage,
                storeDataRangeOverViewDoAws.pcya?.total?.actual?.value
            )

            if (storeDataRangeOverViewDoAws.pcya?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewDoAws.pcya.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewDoAws.pcya.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.pcya?.olo?.actual?.amount,
                    storeDataRangeOverViewDoAws.pcya?.olo?.actual?.percentage,
                    storeDataRangeOverViewDoAws.pcya?.olo?.actual?.value
                ).isEmpty()
            ) {
                olo_pcy_percentage.visibility = View.GONE
                awus_pcya_error_olo.visibility = View.VISIBLE

            } else {
                olo_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_olo.visibility = View.GONE

                olo_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.pcya?.olo?.actual?.amount,
                    storeDataRangeOverViewDoAws.pcya?.olo?.actual?.percentage,
                    storeDataRangeOverViewDoAws.pcya?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.pcya?.phone?.actual?.amount,
                    storeDataRangeOverViewDoAws.pcya?.phone?.actual?.percentage,
                    storeDataRangeOverViewDoAws.pcya?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_pcy_percentage.visibility = View.GONE
                awus_pcya_error_phone.visibility = View.VISIBLE

            } else {
                phone_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                phone_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.pcya?.phone?.actual?.amount,
                    storeDataRangeOverViewDoAws.pcya?.phone?.actual?.percentage,
                    storeDataRangeOverViewDoAws.pcya?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.pcya?.walkin?.actual?.amount,
                    storeDataRangeOverViewDoAws.pcya?.walkin?.actual?.percentage,
                    storeDataRangeOverViewDoAws.pcya?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_percentage.visibility = View.GONE
                awus_pcya_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                walkin_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.pcya?.walkin?.actual?.amount,
                    storeDataRangeOverViewDoAws.pcya?.walkin?.actual?.percentage,
                    storeDataRangeOverViewDoAws.pcya?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.pcya?.delivery?.actual?.amount,
                    storeDataRangeOverViewDoAws.pcya?.delivery?.actual?.percentage,
                    storeDataRangeOverViewDoAws.pcya?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_percentage.visibility = View.GONE
                awus_pcya_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_delivery.visibility = View.GONE

                delivery_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.pcya?.delivery?.actual?.amount,
                    storeDataRangeOverViewDoAws.pcya?.delivery?.actual?.percentage,
                    storeDataRangeOverViewDoAws.pcya?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.pcya?.carryout?.actual?.amount,
                    storeDataRangeOverViewDoAws.pcya?.carryout?.actual?.percentage,
                    storeDataRangeOverViewDoAws.pcya?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_percentage.visibility = View.GONE
                awus_pcya_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_carryout.visibility = View.GONE

                carry_out_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.pcya?.carryout?.actual?.amount,
                    storeDataRangeOverViewDoAws.pcya?.carryout?.actual?.percentage,
                    storeDataRangeOverViewDoAws.pcya?.carryout?.actual?.value
                )
            }

            // order count
            oc_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewDoAws.orderCount?.total?.actual?.amount,
                storeDataRangeOverViewDoAws.orderCount?.total?.actual?.percentage,
                storeDataRangeOverViewDoAws.orderCount?.total?.actual?.value
            )

            if (storeDataRangeOverViewDoAws.orderCount?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewDoAws.orderCount.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewDoAws.orderCount.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.green))

                    } else -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.orderCount?.olo?.actual?.amount,
                    storeDataRangeOverViewDoAws.orderCount?.olo?.actual?.percentage,
                    storeDataRangeOverViewDoAws.orderCount?.olo?.actual?.value
                ).isEmpty()
            ) {
                oc_pcy_percentage.visibility = View.GONE
                awus_oc_error_olo.visibility = View.VISIBLE

            } else {
                oc_pcy_percentage.visibility = View.VISIBLE
                awus_oc_error_olo.visibility = View.GONE

                oc_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.orderCount?.olo?.actual?.amount,
                    storeDataRangeOverViewDoAws.orderCount?.olo?.actual?.percentage,
                    storeDataRangeOverViewDoAws.orderCount?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.orderCount?.phone?.actual?.amount,
                    storeDataRangeOverViewDoAws.orderCount?.phone?.actual?.percentage,
                    storeDataRangeOverViewDoAws.orderCount?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_oc_percentage.visibility = View.GONE
                awus_oc_error_phone.visibility = View.VISIBLE

            } else {
                phone_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_phone.visibility = View.GONE

                phone_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.orderCount?.phone?.actual?.amount,
                    storeDataRangeOverViewDoAws.orderCount?.phone?.actual?.percentage,
                    storeDataRangeOverViewDoAws.orderCount?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.orderCount?.walkin?.actual?.amount,
                    storeDataRangeOverViewDoAws.orderCount?.walkin?.actual?.percentage,
                    storeDataRangeOverViewDoAws.orderCount?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_oc_percentage.visibility = View.GONE
                awus_oc_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_walkin.visibility = View.GONE

                walkin_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.orderCount?.walkin?.actual?.amount,
                    storeDataRangeOverViewDoAws.orderCount?.walkin?.actual?.percentage,
                    storeDataRangeOverViewDoAws.orderCount?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.orderCount?.delivery?.actual?.amount,
                    storeDataRangeOverViewDoAws.orderCount?.delivery?.actual?.percentage,
                    storeDataRangeOverViewDoAws.orderCount?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_oc_percentage.visibility = View.GONE
                awus_oc_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_delivery.visibility = View.GONE

                delivery_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.orderCount?.delivery?.actual?.amount,
                    storeDataRangeOverViewDoAws.orderCount?.delivery?.actual?.percentage,
                    storeDataRangeOverViewDoAws.orderCount?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.orderCount?.carryout?.actual?.amount,
                    storeDataRangeOverViewDoAws.orderCount?.carryout?.actual?.percentage,
                    storeDataRangeOverViewDoAws.orderCount?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_oc_percentage.visibility = View.GONE
                awus_oc_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_carryout.visibility = View.GONE

                carry_out_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.orderCount?.carryout?.actual?.amount,
                    storeDataRangeOverViewDoAws.orderCount?.carryout?.actual?.percentage,
                    storeDataRangeOverViewDoAws.orderCount?.carryout?.actual?.value
                )
            }

            // Average  Tickets
            average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewDoAws.averageTicket?.total?.actual?.amount,
                storeDataRangeOverViewDoAws.averageTicket?.total?.actual?.percentage,
                storeDataRangeOverViewDoAws.averageTicket?.total?.actual?.value
            )

            if (storeDataRangeOverViewDoAws.averageTicket?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewDoAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewDoAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.averageTicket?.olo?.actual?.amount,
                    storeDataRangeOverViewDoAws.averageTicket?.olo?.actual?.percentage,
                    storeDataRangeOverViewDoAws.averageTicket?.olo?.actual?.value
                ).isEmpty()
            ) {
                average_olo_percentage.visibility = View.GONE
                awus_avg_error_olo.visibility = View.VISIBLE

            } else {
                average_olo_percentage.visibility = View.VISIBLE
                awus_avg_error_olo.visibility = View.GONE

                average_olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.averageTicket?.olo?.actual?.amount,
                    storeDataRangeOverViewDoAws.averageTicket?.olo?.actual?.percentage,
                    storeDataRangeOverViewDoAws.averageTicket?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.averageTicket?.phone?.actual?.amount,
                    storeDataRangeOverViewDoAws.averageTicket?.phone?.actual?.percentage,
                    storeDataRangeOverViewDoAws.averageTicket?.phone?.actual?.value
                ).isEmpty()
            ) {
                average_phone_percentage.visibility = View.GONE
                awus_avg_error_phone.visibility = View.VISIBLE

            } else {
                average_phone_percentage.visibility = View.VISIBLE
                awus_avg_error_phone.visibility = View.GONE

                average_phone_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.averageTicket?.phone?.actual?.amount,
                    storeDataRangeOverViewDoAws.averageTicket?.phone?.actual?.percentage,
                    storeDataRangeOverViewDoAws.averageTicket?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.averageTicket?.walkin?.actual?.amount,
                    storeDataRangeOverViewDoAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataRangeOverViewDoAws.averageTicket?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_average_percentage.visibility = View.GONE
                awus_avg_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_average_percentage.visibility = View.VISIBLE
                awus_avg_error_walkin.visibility = View.GONE

                walkin_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.averageTicket?.walkin?.actual?.amount,
                    storeDataRangeOverViewDoAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataRangeOverViewDoAws.averageTicket?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.averageTicket?.delivery?.actual?.amount,
                    storeDataRangeOverViewDoAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataRangeOverViewDoAws.averageTicket?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_percentage.visibility = View.GONE
                awus_avg_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_percentage.visibility = View.VISIBLE
                awus_avg_error_delivery.visibility = View.GONE

                delivery_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.averageTicket?.delivery?.actual?.amount,
                    storeDataRangeOverViewDoAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataRangeOverViewDoAws.averageTicket?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.averageTicket?.carryout?.actual?.amount,
                    storeDataRangeOverViewDoAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataRangeOverViewDoAws.averageTicket?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_average_percentage.visibility = View.GONE
                awus_avg_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_average_percentage.visibility = View.VISIBLE
                awus_avg_error_carryout.visibility = View.GONE

                carry_out_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.averageTicket?.carryout?.actual?.amount,
                    storeDataRangeOverViewDoAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataRangeOverViewDoAws.averageTicket?.carryout?.actual?.value
                )
            }

            // OLO sales
            olo_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewDoAws.oloSales?.total?.actual?.amount,
                storeDataRangeOverViewDoAws.oloSales?.total?.actual?.percentage,
                storeDataRangeOverViewDoAws.oloSales?.total?.actual?.value
            )

            if (storeDataRangeOverViewDoAws.oloSales?.total?.status?.toString() != null) {

                when {
                    storeDataRangeOverViewDoAws.oloSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewDoAws.oloSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.oloSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewDoAws.oloSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewDoAws.oloSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                olo_percentage.visibility = View.GONE
                awus_olo_sales_error_olo.visibility = View.VISIBLE

            } else {
                olo_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_olo.visibility = View.GONE

                olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.oloSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewDoAws.oloSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewDoAws.oloSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.oloSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewDoAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewDoAws.oloSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                olo_order_percentage.visibility = View.GONE
                awus_olo_sales_error_oc.visibility = View.VISIBLE

            } else {
                olo_order_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_oc.visibility = View.GONE

                olo_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.oloSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewDoAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewDoAws.oloSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewDoAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewDoAws.oloSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                olo_average_percentage.visibility = View.GONE
                awus_olo_sales_error_at.visibility = View.VISIBLE

            } else {
                olo_average_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_at.visibility = View.GONE

                olo_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewDoAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewDoAws.oloSales?.averageTicket?.actual?.value
                )
            }

            // Phone sales order count
            phone_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewDoAws.phoneSales?.total?.actual?.amount,
                storeDataRangeOverViewDoAws.phoneSales?.total?.actual?.percentage,
                storeDataRangeOverViewDoAws.phoneSales?.total?.actual?.value
            )
            if (storeDataRangeOverViewDoAws.phoneSales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewDoAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewDoAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.phoneSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewDoAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewDoAws.phoneSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                phone_pcys_percentage.visibility = View.GONE
                awus_phones_sales_error_olo.visibility = View.VISIBLE

            } else {
                phone_pcys_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_olo.visibility = View.GONE

                phone_pcys_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.phoneSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewDoAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewDoAws.phoneSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewDoAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewDoAws.phoneSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_phone_ticket_percentage.visibility = View.GONE
                awus_phones_sales_error_at.visibility = View.VISIBLE

            } else {
                average_phone_ticket_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_at.visibility = View.GONE

                average_phone_ticket_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewDoAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewDoAws.phoneSales?.averageTicket?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewDoAws.phoneSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewDoAws.phoneSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                phone_sales_order_percentage.visibility = View.GONE
                awus_phones_sales_error_oc.visibility = View.VISIBLE

            } else {
                phone_sales_order_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_oc.visibility = View.GONE

                phone_sales_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewDoAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewDoAws.oloSales?.orderCount?.actual?.value
                )
            }

            // walk-in sales
            walkin_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewDoAws.walkinSales?.total?.actual?.amount,
                storeDataRangeOverViewDoAws.walkinSales?.total?.actual?.percentage,
                storeDataRangeOverViewDoAws.walkinSales?.total?.actual?.value
            )

            if (storeDataRangeOverViewDoAws.walkinSales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewDoAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewDoAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.walkinSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewDoAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewDoAws.walkinSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_value.visibility = View.GONE
                awus_walkins_sales_error_olo.visibility = View.VISIBLE

            } else {
                walkin_pcy_value.visibility = View.VISIBLE
                awus_walkins_sales_error_olo.visibility = View.GONE

                walkin_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.walkinSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewDoAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewDoAws.walkinSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewDoAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewDoAws.walkinSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                walkin_order_count_value.visibility = View.GONE
                awus_walkins_sales_error_oc.visibility = View.VISIBLE

            } else {
                walkin_order_count_value.visibility = View.VISIBLE
                awus_walkins_sales_error_oc.visibility = View.GONE

                walkin_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewDoAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewDoAws.walkinSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewDoAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewDoAws.walkinSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_walkin_ticket_value.visibility = View.GONE
                awus_walkins_sales_error_at.visibility = View.VISIBLE

            } else {
                average_walkin_ticket_value.visibility = View.VISIBLE
                awus_walkins_sales_error_at.visibility = View.GONE

                average_walkin_ticket_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewDoAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewDoAws.walkinSales?.averageTicket?.actual?.value
                )
            }

            // delivery sales
            delivery_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewDoAws.deliverySales?.total?.actual?.amount,
                storeDataRangeOverViewDoAws.deliverySales?.total?.actual?.percentage,
                storeDataRangeOverViewDoAws.deliverySales?.total?.actual?.value
            )

            if (storeDataRangeOverViewDoAws.deliverySales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewDoAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewDoAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.deliverySales?.pcya?.actual?.amount,
                    storeDataRangeOverViewDoAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewDoAws.deliverySales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_value.visibility = View.GONE
                awus_delivery_sales_error_olo.visibility = View.VISIBLE

            } else {
                delivery_pcy_value.visibility = View.VISIBLE
                awus_delivery_sales_error_olo.visibility = View.GONE

                delivery_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.deliverySales?.pcya?.actual?.amount,
                    storeDataRangeOverViewDoAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewDoAws.deliverySales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewDoAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewDoAws.deliverySales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                delivery_order_count_value.visibility = View.GONE
                awus_delivery_sales_error_oc.visibility = View.VISIBLE

            } else {
                delivery_order_count_value.visibility = View.VISIBLE
                awus_delivery_sales_error_oc.visibility = View.GONE

                delivery_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewDoAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewDoAws.deliverySales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewDoAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewDoAws.deliverySales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                delivery_average_percentage.visibility = View.GONE
                awus_delivery_sales_error_at.visibility = View.VISIBLE

            } else {
                delivery_average_percentage.visibility = View.VISIBLE
                awus_delivery_sales_error_at.visibility = View.GONE

                delivery_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewDoAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewDoAws.deliverySales?.averageTicket?.actual?.value
                )
            }

            // carryout sales
            carryout_average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeOverViewDoAws.carryoutSales?.total?.actual?.amount,
                storeDataRangeOverViewDoAws.carryoutSales?.total?.actual?.percentage,
                storeDataRangeOverViewDoAws.carryoutSales?.total?.actual?.value
            )

            if (storeDataRangeOverViewDoAws.carryoutSales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeOverViewDoAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeOverViewDoAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewDoAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewDoAws.carryoutSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_value.visibility = View.GONE
                awus_carryout_sales_error_olo.visibility = View.VISIBLE

            } else {
                carry_out_pcy_value.visibility = View.VISIBLE
                awus_carryout_sales_error_olo.visibility = View.GONE

                carry_out_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataRangeOverViewDoAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataRangeOverViewDoAws.carryoutSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewDoAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewDoAws.carryoutSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                carry_outorder_count_value.visibility = View.GONE
                awus_carryout_sales_error_oc.visibility = View.VISIBLE

            } else {
                carry_outorder_count_value.visibility = View.VISIBLE
                awus_carryout_sales_error_oc.visibility = View.GONE

                carry_outorder_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataRangeOverViewDoAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataRangeOverViewDoAws.carryoutSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewDoAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewDoAws.carryoutSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                carry_average_tickets_value.visibility = View.GONE
                awus_carryout_sales_error_at.visibility = View.VISIBLE

            } else {
                carry_average_tickets_value.visibility = View.VISIBLE
                awus_carryout_sales_error_at.visibility = View.GONE

                carry_average_tickets_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeOverViewDoAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataRangeOverViewDoAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataRangeOverViewDoAws.carryoutSales?.averageTicket?.actual?.value
                )
            }
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"AWUS Overview Period Range KPI")
        }
    }

// Supervisor View

    private fun todayViewSupervisorAws(todayDetail: SupervisorOverviewTodayQuery.Supervisor) {
        val storeDataTodayViewSupervisorAws = todayDetail.kpis?.stores?.today!!.sales

        Logger.info("AWUS Today Query", "AWUS Overview KPI")

        // display name
        awus_display.text =
            storeDataTodayViewSupervisorAws!!.displayName ?: getString(R.string.awus_text)
        // pcy display name
        pcy_display_name.text =
            storeDataTodayViewSupervisorAws.pcya?.total?.displayName ?: getString(R.string.pcy)
        olo_pcy_display.text =
            storeDataTodayViewSupervisorAws.pcya?.olo?.displayName ?: getString(R.string.olo_pcy)
        phone_pcys.text =
            storeDataTodayViewSupervisorAws.pcya?.phone?.displayName
                ?: getString(R.string.phone_pcys)
        walkin_pcy.text =
            storeDataTodayViewSupervisorAws.pcya?.walkin?.displayName
                ?: getString(R.string.walkin_pcy)
        delivery_pcy.text =
            storeDataTodayViewSupervisorAws.pcya?.delivery?.displayName
                ?: getString(R.string.delivery_pcy)
        delivery_pcy.text =
            storeDataTodayViewSupervisorAws.pcya?.delivery?.displayName
                ?: getString(R.string.delivery_pcy)
        carry_out_pcy.text =
            storeDataTodayViewSupervisorAws.pcya?.carryout?.displayName
                ?: getString(R.string.carry_out_pcy)
        // order count display
        order_count_display.text =
            storeDataTodayViewSupervisorAws.orderCount?.total?.displayName
                ?: getString(R.string.order_count)
        olo_order_count.text =
            storeDataTodayViewSupervisorAws.orderCount?.olo?.displayName
                ?: getString(R.string.olo_order_count)
        phone_order_count.text = storeDataTodayViewSupervisorAws.orderCount?.phone?.displayName
            ?: getString(R.string.phone_order_count)
        walkin_order_count.text = storeDataTodayViewSupervisorAws.orderCount?.walkin?.displayName
            ?: getString(R.string.walkin_order_count)
        delivery_order_count.text =
            storeDataTodayViewSupervisorAws.orderCount?.delivery?.displayName
                ?: getString(R.string.delivery_order_count)
        carry_outorder_count.text =
            storeDataTodayViewSupervisorAws.orderCount?.carryout?.displayName
                ?: getString(R.string.carry_outorder_count)

        // average ticket count display
        if (storeDataTodayViewSupervisorAws.averageTicket?.total == null || storeDataTodayViewSupervisorAws.averageTicket.total.displayName.isNullOrEmpty()) {
            ll_average_ticket_display_name.visibility = View.GONE
        } else {
            ll_average_ticket_display_name.visibility = View.VISIBLE
            average_ticket_display_name.text =
                storeDataTodayViewSupervisorAws.averageTicket.total.displayName
        }


        if (storeDataTodayViewSupervisorAws.averageTicket?.olo == null || storeDataTodayViewSupervisorAws.averageTicket.olo.displayName.isNullOrEmpty()) {
            ll_olo_average_ticket.visibility = View.GONE
        } else {
            ll_olo_average_ticket.visibility = View.VISIBLE
            olo_average_ticket.text =
                storeDataTodayViewSupervisorAws.averageTicket.olo.displayName
        }

        if (storeDataTodayViewSupervisorAws.averageTicket?.phone == null || storeDataTodayViewSupervisorAws.averageTicket.phone.displayName.isNullOrEmpty()) {
            ll_average_phone_ticket.visibility = View.GONE
        } else {
            ll_average_phone_ticket.visibility = View.VISIBLE
            average_phone_ticket.text =
                storeDataTodayViewSupervisorAws.averageTicket.phone.displayName
        }

        if (storeDataTodayViewSupervisorAws.averageTicket?.walkin == null || storeDataTodayViewSupervisorAws.averageTicket.walkin.displayName.isNullOrEmpty()) {
            ll_average_walkin_ticket.visibility = View.GONE
        } else {
            ll_average_walkin_ticket.visibility = View.VISIBLE
            average_walkin_ticket.text =
                storeDataTodayViewSupervisorAws.averageTicket.walkin.displayName
        }

        if (storeDataTodayViewSupervisorAws.averageTicket?.delivery == null || storeDataTodayViewSupervisorAws.averageTicket.delivery.displayName.isNullOrEmpty()) {
            ll_delivery_average_tickets.visibility = View.GONE
        } else {
            ll_delivery_average_tickets.visibility = View.VISIBLE
            delivery_average_tickets.text =
                storeDataTodayViewSupervisorAws.averageTicket.delivery.displayName
        }

        if (storeDataTodayViewSupervisorAws.averageTicket?.carryout == null || storeDataTodayViewSupervisorAws.averageTicket.carryout.displayName.isNullOrEmpty()) {
            ll_carry_average_tickets.visibility = View.GONE
        } else {
            ll_carry_average_tickets.visibility = View.VISIBLE
            carry_average_tickets.text =
                storeDataTodayViewSupervisorAws.averageTicket.carryout.displayName
        }


        // olo sales display
        if (storeDataTodayViewSupervisorAws.oloSales?.total == null || storeDataTodayViewSupervisorAws.oloSales.total.displayName.isNullOrEmpty()) {
            ll_olo_sales_display_name.visibility = View.GONE
        } else {
            ll_olo_sales_display_name.visibility = View.VISIBLE
            olo_sales_display_name.text =
                storeDataTodayViewSupervisorAws.oloSales.total.displayName
        }

        if (storeDataTodayViewSupervisorAws.oloSales?.pcya == null || storeDataTodayViewSupervisorAws.oloSales.pcya.displayName.isNullOrEmpty()) {
            olo_sales_pcy_parent.visibility = View.GONE
        } else {
            olo_sales_pcy_parent.visibility = View.VISIBLE
            olo_pcy.text =
                storeDataTodayViewSupervisorAws.oloSales.pcya.displayName
        }

        if (storeDataTodayViewSupervisorAws.oloSales?.orderCount == null || storeDataTodayViewSupervisorAws.oloSales.orderCount.displayName.isNullOrEmpty()) {
            ll_olo_order_count_sales_display.visibility = View.GONE
        } else {
            ll_olo_order_count_sales_display.visibility = View.VISIBLE
            olo_order_count_sales_display.text =
                storeDataTodayViewSupervisorAws.oloSales.orderCount.displayName
        }

        if (storeDataTodayViewSupervisorAws.oloSales?.averageTicket == null || storeDataTodayViewSupervisorAws.oloSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_olo_average_ticket_sales_display.visibility = View.GONE
        } else {
            ll_olo_average_ticket_sales_display.visibility = View.VISIBLE
            olo_average_ticket_sales_display.text =
                storeDataTodayViewSupervisorAws.oloSales.averageTicket.displayName
        }


        // phone sales display
        if (storeDataTodayViewSupervisorAws.phoneSales?.total == null || storeDataTodayViewSupervisorAws.phoneSales.total.displayName.isNullOrEmpty()) {
            ll_phone_sales_display_name.visibility = View.GONE
        } else {
            ll_phone_sales_display_name.visibility = View.VISIBLE
            phone_sales_display_name.text =
                storeDataTodayViewSupervisorAws.phoneSales.total.displayName
        }


        if (storeDataTodayViewSupervisorAws.phoneSales?.orderCount == null || storeDataTodayViewSupervisorAws.phoneSales.orderCount.displayName.isNullOrEmpty()) {
            ll_phone_order_count_display.visibility = View.GONE
        } else {
            ll_phone_order_count_display.visibility = View.VISIBLE
            phone_order_count_display.text =
                storeDataTodayViewSupervisorAws.phoneSales.orderCount.displayName
        }

        if (storeDataTodayViewSupervisorAws.phoneSales?.averageTicket == null || storeDataTodayViewSupervisorAws.phoneSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_average_phone_ticket_display.visibility = View.GONE
        } else {
            ll_average_phone_ticket_display.visibility = View.VISIBLE
            average_phone_ticket_display.text =
                storeDataTodayViewSupervisorAws.phoneSales.averageTicket.displayName
        }


        // walk-in Sales
        if (storeDataTodayViewSupervisorAws.walkinSales?.total == null || storeDataTodayViewSupervisorAws.walkinSales.total.displayName.isNullOrEmpty()) {
            ll_walkin_sales_display_name.visibility = View.GONE
        } else {
            ll_walkin_sales_display_name.visibility = View.VISIBLE
            walkin_sales_display_name.text =
                storeDataTodayViewSupervisorAws.walkinSales.total.displayName
        }
        if (storeDataTodayViewSupervisorAws.walkinSales?.orderCount == null || storeDataTodayViewSupervisorAws.walkinSales.orderCount.displayName.isNullOrEmpty()) {
            ll_walkin_order_count_sales_display.visibility = View.GONE
        } else {
            ll_walkin_order_count_sales_display.visibility = View.VISIBLE
            walkin_order_count_sales_display.text =
                storeDataTodayViewSupervisorAws.walkinSales.orderCount.displayName
        }

        if (storeDataTodayViewSupervisorAws.walkinSales?.averageTicket == null || storeDataTodayViewSupervisorAws.walkinSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_average_walkin_ticket_display.visibility = View.GONE
        } else {
            ll_average_walkin_ticket_display.visibility = View.VISIBLE
            average_walkin_ticket_display.text =
                storeDataTodayViewSupervisorAws.walkinSales.averageTicket.displayName
        }

        //  deliver Sales
        if (storeDataTodayViewSupervisorAws.deliverySales?.total == null || storeDataTodayViewSupervisorAws.deliverySales.total.displayName.isNullOrEmpty()) {
            ll_delivery_sales_display_name.visibility = View.GONE
        } else {
            ll_delivery_sales_display_name.visibility = View.VISIBLE
            delivery_sales_display_name.text =
                storeDataTodayViewSupervisorAws.deliverySales.total.displayName
        }
        if (storeDataTodayViewSupervisorAws.deliverySales?.orderCount == null || storeDataTodayViewSupervisorAws.deliverySales.orderCount.displayName.isNullOrEmpty()) {
            ll_delivery_order_count_display.visibility = View.GONE
        } else {
            ll_delivery_order_count_display.visibility = View.VISIBLE
            delivery_order_count_display.text =
                storeDataTodayViewSupervisorAws.deliverySales.orderCount.displayName
        }
        if (storeDataTodayViewSupervisorAws.deliverySales?.averageTicket == null || storeDataTodayViewSupervisorAws.deliverySales.averageTicket.displayName.isNullOrEmpty()) {
            ll_delivery_average_tickets_display.visibility = View.GONE
        } else {
            ll_delivery_average_tickets_display.visibility = View.VISIBLE
            delivery_average_tickets_display.text =
                storeDataTodayViewSupervisorAws.deliverySales.averageTicket.displayName
        }


        //  carryout Sales
        if (storeDataTodayViewSupervisorAws.carryoutSales?.total == null || storeDataTodayViewSupervisorAws.carryoutSales.total.displayName.isNullOrEmpty()) {
            ll_carry_out_display_text.visibility = View.GONE
        } else {
            ll_carry_out_display_text.visibility = View.VISIBLE
            carry_out_display_text.text =
                storeDataTodayViewSupervisorAws.carryoutSales.total.displayName
        }
        if (storeDataTodayViewSupervisorAws.carryoutSales?.orderCount == null || storeDataTodayViewSupervisorAws.carryoutSales.orderCount.displayName.isNullOrEmpty()) {
            ll_carry_outorder_count_display.visibility = View.GONE
        } else {
            ll_carry_outorder_count_display.visibility = View.VISIBLE
            carry_outorder_count_display.text =
                storeDataTodayViewSupervisorAws.carryoutSales.orderCount.displayName
        }

        if (storeDataTodayViewSupervisorAws.carryoutSales?.averageTicket == null || storeDataTodayViewSupervisorAws.carryoutSales.averageTicket.displayName.isNullOrEmpty()) {
            ll_carry_average_tickets_display.visibility = View.GONE
        } else {
            ll_carry_average_tickets_display.visibility = View.VISIBLE
            carry_average_tickets_display.text =
                storeDataTodayViewSupervisorAws.carryoutSales.averageTicket.displayName
        }




        if (storeDataTodayViewSupervisorAws.actual?.value?.isNaN() == false && storeDataTodayViewSupervisorAws.status?.toString() != null) {
            awus_sales.text = getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    storeDataTodayViewSupervisorAws.actual.value
                )
            )
            when {
                storeDataTodayViewSupervisorAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                    awus_sales.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewSupervisorAws.status.toString() == resources.getString(R.string.under_limit) -> {
                    awus_sales.setTextColor(getColor(R.color.green))
                }
                else -> {
                    awus_sales.setTextColor(getColor(R.color.text_color))

                }
            }
        }
        // scroll detect

        awus_scroll_parent.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 0f
                override fun onScrollChanged() {
                    if (awus_scroll_parent.scrollY > y) {
                        parent_data_on_scroll_linear.visibility = View.VISIBLE
                        parent_data_on_scroll_view.visibility = View.VISIBLE
                        level_two_scroll_data_action.text =
                            storeDataTodayViewSupervisorAws.displayName
                                ?: getString(R.string.awus_text)
                        if (storeDataTodayViewSupervisorAws.actual?.value?.isNaN() == false && storeDataTodayViewSupervisorAws.status?.toString() != null) {
                            level_two_scroll_data_action_value.text =
                                getString(R.string.dollar_text).plus(
                                    Validation().dollarFormatting(
                                        storeDataTodayViewSupervisorAws.actual.value
                                    )
                                )
                            when {
                                storeDataTodayViewSupervisorAws.status.toString() == resources.getString(
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
                                storeDataTodayViewSupervisorAws.status.toString() == resources.getString(
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
                    y = awus_scroll_parent.scrollY.toFloat()
                    if (y <= 0) {
                        parent_data_on_scroll_linear.visibility = View.INVISIBLE
                        parent_data_on_scroll_view.visibility = View.GONE
                    }
                }
            })


        awus_goal_value.text =
            if (storeDataTodayViewSupervisorAws.goal?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(storeDataTodayViewSupervisorAws.goal.value)
            ) else ""
        awus__variance_value.text =
            if (storeDataTodayViewSupervisorAws.variance?.value?.isNaN() == false) getString(
                R.string.dollar_text
            ).plus(
                Validation().dollarFormatting(storeDataTodayViewSupervisorAws.variance.value)
            ) else ""

        showAWUSNarrativeData(storeDataTodayViewSupervisorAws.narrative.toString())

        // PCA
        if (storeDataTodayViewSupervisorAws.pcya?.total?.actual?.percentage?.isNaN() == false && storeDataTodayViewSupervisorAws.pcya.total.status?.toString() != null) {
            pcy_percentage.text =
                Validation().ignoreZeroAfterDecimal(storeDataTodayViewSupervisorAws.pcya.total.actual.percentage)
                    .plus(
                        getString(
                            R.string.percentage_text
                        )
                    )
            when {
                storeDataTodayViewSupervisorAws.pcya.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    pcy_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewSupervisorAws.pcya.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    pcy_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    pcy_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewSupervisorAws.pcya?.olo?.actual?.percentage?.isNaN() == false) {
            olo_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.pcya.olo.actual.percentage
            )).plus(getString(R.string.percentage_text))
            olo_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_olo.visibility = View.GONE

        } else {
            olo_pcy_percentage.visibility = View.GONE
            awus_pcya_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.pcya?.phone?.actual?.percentage?.isNaN() == false) {
            phone_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.pcya.phone.actual.percentage
            )).plus(getString(R.string.percentage_text))
            phone_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_phone.visibility = View.GONE

        } else {
            phone_pcy_percentage.visibility = View.GONE
            awus_pcya_error_phone.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.pcya?.walkin?.actual?.percentage?.isNaN() == false) {
            walkin_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.pcya.walkin.actual.percentage
            )).plus(getString(R.string.percentage_text))
            walkin_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_phone.visibility = View.GONE

        } else {
            walkin_pcy_percentage.visibility = View.GONE
            awus_pcya_error_walkin.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.pcya?.delivery?.actual?.percentage?.isNaN() == false) {
            delivery_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.pcya.delivery.actual.percentage
            )).plus(getString(R.string.percentage_text))
            delivery_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_delivery.visibility = View.GONE

        } else {
            delivery_pcy_percentage.visibility = View.GONE
            awus_pcya_error_delivery.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.pcya?.carryout?.actual?.percentage?.isNaN() == false) {
            carry_out_pcy_percentage.text = (Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.pcya.carryout.actual.percentage
            )).plus(getString(R.string.percentage_text))
            carry_out_pcy_percentage.visibility = View.VISIBLE
            awus_pcya_error_carryout.visibility = View.GONE

        } else {
            carry_out_pcy_percentage.visibility = View.GONE
            awus_pcya_error_carryout.visibility = View.VISIBLE

        }


        // order count

        if (storeDataTodayViewSupervisorAws.orderCount?.total?.actual?.value?.isNaN() == false && storeDataTodayViewSupervisorAws.orderCount.total.status?.toString() != null) {
            oc_percentage.text =
                Validation().ignoreZeroAfterDecimal(storeDataTodayViewSupervisorAws.orderCount.total.actual.value)
            when {
                storeDataTodayViewSupervisorAws.orderCount.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    oc_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewSupervisorAws.orderCount.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    oc_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    oc_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewSupervisorAws.orderCount?.olo?.actual?.value?.isNaN() == false) {
            oc_pcy_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.orderCount.olo.actual.value
            )
            oc_pcy_percentage.visibility = View.VISIBLE
            awus_oc_error_olo.visibility = View.GONE

        } else {
            oc_pcy_percentage.visibility = View.GONE
            awus_oc_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.orderCount?.phone?.actual?.value?.isNaN() == false) {
            phone_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.orderCount.phone.actual.value
            )
            phone_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_phone.visibility = View.GONE

        } else {
            phone_oc_percentage.visibility = View.GONE
            awus_oc_error_phone.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.orderCount?.walkin?.actual?.value?.isNaN() == false) {
            walkin_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.orderCount.walkin.actual.value
            )
            walkin_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_walkin.visibility = View.GONE

        } else {
            walkin_oc_percentage.visibility = View.GONE
            awus_oc_error_walkin.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.orderCount?.delivery?.actual?.value?.isNaN() == false) {
            delivery_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.orderCount.delivery.actual.value
            )
            delivery_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_delivery.visibility = View.GONE

        } else {
            delivery_oc_percentage.visibility = View.GONE
            awus_oc_error_delivery.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.orderCount?.carryout?.actual?.value?.isNaN() == false) {
            carry_out_oc_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.orderCount.carryout.actual.value
            )
            carry_out_oc_percentage.visibility = View.VISIBLE
            awus_oc_error_carryout.visibility = View.GONE

        } else {
            carry_out_oc_percentage.visibility = View.GONE
            awus_oc_error_carryout.visibility = View.VISIBLE

        }

        // Average  Tickets
        if (storeDataTodayViewSupervisorAws.averageTicket?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewSupervisorAws.averageTicket.total.status?.toString() != null) {
            average_percentage.text =
                if (!storeDataTodayViewSupervisorAws.averageTicket.total.actual.amount.isNaN()) getString(
                    R.string.dollar_text
                ).plus(
                    Validation().dollarFormatting(storeDataTodayViewSupervisorAws.averageTicket.total.actual.amount)
                ) else ""

            when {
                storeDataTodayViewSupervisorAws.averageTicket.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewSupervisorAws.averageTicket.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    average_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewSupervisorAws.averageTicket?.olo?.actual?.value?.isNaN() == false) {
            average_olo_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.averageTicket.olo.actual.value
            )
            average_olo_percentage.visibility = View.VISIBLE
            awus_avg_error_olo.visibility = View.GONE

        } else {
            average_olo_percentage.visibility = View.GONE
            awus_avg_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.averageTicket?.phone?.actual?.value?.isNaN() == false) {
            average_phone_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.averageTicket.phone.actual.value
            )
            average_phone_percentage.visibility = View.VISIBLE
            awus_avg_error_phone.visibility = View.GONE

        } else {
            average_phone_percentage.visibility = View.GONE
            awus_avg_error_phone.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.averageTicket?.walkin?.actual?.value?.isNaN() == false) {
            walkin_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.averageTicket.walkin.actual.value
            )
            walkin_average_percentage.visibility = View.VISIBLE
            awus_avg_error_walkin.visibility = View.GONE

        } else {
            walkin_average_percentage.visibility = View.GONE
            awus_avg_error_walkin.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.averageTicket?.delivery?.actual?.value?.isNaN() == false) {
            delivery_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.averageTicket.delivery.actual.value
            )
            delivery_percentage.visibility = View.VISIBLE
            awus_avg_error_delivery.visibility = View.GONE

        } else {
            delivery_percentage.visibility = View.GONE
            awus_avg_error_delivery.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.averageTicket?.carryout?.actual?.value?.isNaN() == false) {
            carry_out_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.averageTicket.carryout.actual.value
            )
            carry_out_average_percentage.visibility = View.VISIBLE
            awus_avg_error_carryout.visibility = View.GONE

        } else {
            carry_out_average_percentage.visibility = View.GONE
            awus_avg_error_carryout.visibility = View.VISIBLE

        }

        // OLO sales
        if (storeDataTodayViewSupervisorAws.oloSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewSupervisorAws.oloSales.total.status?.toString() != null) {
            olo_sales_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewSupervisorAws.oloSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewSupervisorAws.oloSales.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    olo_sales_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewSupervisorAws.oloSales.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    olo_sales_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    olo_sales_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewSupervisorAws.oloSales?.pcya?.actual?.percentage?.isNaN() == false) {
            olo_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.oloSales.pcya.actual.percentage
            )
            olo_percentage.visibility = View.VISIBLE
            awus_olo_sales_error_olo.visibility = View.GONE

        } else {
            olo_percentage.visibility = View.GONE
            awus_olo_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.oloSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            olo_order_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.oloSales.orderCount.actual.percentage
            )
            olo_order_percentage.visibility = View.VISIBLE
            awus_olo_sales_error_oc.visibility = View.GONE

        } else {
            olo_order_percentage.visibility = View.GONE
            awus_olo_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.oloSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            olo_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.oloSales.averageTicket.actual.percentage
            )
            olo_average_percentage.visibility = View.VISIBLE
            awus_olo_sales_error_at.visibility = View.GONE

        } else {
            olo_average_percentage.visibility = View.GONE
            awus_olo_sales_error_at.visibility = View.VISIBLE

        }

        // Phone sales order count
        if (storeDataTodayViewSupervisorAws.phoneSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewSupervisorAws.phoneSales.total.status?.toString() != null) {
            phone_sales_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewSupervisorAws.phoneSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewSupervisorAws.phoneSales.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    phone_sales_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewSupervisorAws.phoneSales.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    phone_sales_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    phone_sales_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewSupervisorAws.phoneSales?.pcya?.actual?.percentage?.isNaN() == false) {
            phone_pcys_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.phoneSales.pcya.actual.percentage
            )
            phone_pcys_percentage.visibility = View.VISIBLE
            awus_phones_sales_error_olo.visibility = View.GONE

        } else {
            phone_pcys_percentage.visibility = View.GONE
            awus_phones_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.phoneSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            phone_sales_order_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.phoneSales.orderCount.actual.percentage
            )
            phone_sales_order_percentage.visibility = View.VISIBLE
            awus_phones_sales_error_oc.visibility = View.GONE

        } else {
            phone_sales_order_percentage.visibility = View.GONE
            awus_phones_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.phoneSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            average_phone_ticket_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.phoneSales.averageTicket.actual.percentage
            )
            average_phone_ticket_percentage.visibility = View.VISIBLE
            awus_phones_sales_error_at.visibility = View.GONE

        } else {
            average_phone_ticket_percentage.visibility = View.GONE
            awus_phones_sales_error_at.visibility = View.VISIBLE

        }

        // walk-in sales

        if (storeDataTodayViewSupervisorAws.walkinSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewSupervisorAws.walkinSales.total.status?.toString() != null) {
            walkin_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewSupervisorAws.walkinSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewSupervisorAws.walkinSales.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    walkin_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewSupervisorAws.walkinSales.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    walkin_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    walkin_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewSupervisorAws.walkinSales?.pcya?.actual?.percentage?.isNaN() == false) {
            walkin_pcy_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.walkinSales.pcya.actual.percentage
            )
            walkin_pcy_value.visibility = View.VISIBLE
            awus_walkins_sales_error_olo.visibility = View.VISIBLE

        } else {
            walkin_pcy_value.visibility = View.GONE
            awus_walkins_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.walkinSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            walkin_order_count_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.walkinSales.orderCount.actual.percentage
            )
            walkin_order_count_value.visibility = View.VISIBLE
            awus_walkins_sales_error_oc.visibility = View.VISIBLE

        } else {
            walkin_order_count_value.visibility = View.GONE
            awus_walkins_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.walkinSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            average_walkin_ticket_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.walkinSales.averageTicket.actual.percentage
            )
            average_walkin_ticket_value.visibility = View.VISIBLE
            awus_walkins_sales_error_at.visibility = View.VISIBLE

        } else {
            average_walkin_ticket_value.visibility = View.VISIBLE
            awus_walkins_sales_error_at.visibility = View.VISIBLE

        }

        // delivery sales
        if (storeDataTodayViewSupervisorAws.deliverySales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewSupervisorAws.deliverySales.total.status?.toString() != null) {
            delivery_sales_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewSupervisorAws.deliverySales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewSupervisorAws.deliverySales.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    delivery_sales_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewSupervisorAws.deliverySales.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    delivery_sales_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    delivery_sales_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewSupervisorAws.deliverySales?.pcya?.actual?.percentage?.isNaN() == false) {
            delivery_pcy_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.deliverySales.pcya.actual.percentage
            )
            delivery_pcy_value.visibility = View.VISIBLE
            awus_delivery_sales_error_olo.visibility = View.GONE

        } else {
            delivery_pcy_value.visibility = View.GONE
            awus_delivery_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.deliverySales?.orderCount?.actual?.percentage?.isNaN() == false) {
            delivery_order_count_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.deliverySales.orderCount.actual.percentage
            )
            delivery_order_count_value.visibility = View.VISIBLE
            awus_walkins_sales_error_at.visibility = View.GONE

        } else {
            delivery_order_count_value.visibility = View.GONE
            awus_delivery_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.deliverySales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            delivery_average_percentage.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.deliverySales.averageTicket.actual.percentage
            )
            delivery_average_percentage.visibility = View.VISIBLE
            awus_walkins_sales_error_at.visibility = View.GONE


        } else {
            delivery_average_percentage.visibility = View.GONE
            awus_delivery_sales_error_at.visibility = View.VISIBLE

        }


        // carryout sales

        if (storeDataTodayViewSupervisorAws.carryoutSales?.total?.actual?.amount?.isNaN() == false && storeDataTodayViewSupervisorAws.carryoutSales.total.status?.toString() != null) {
            carryout_average_percentage.text =
                getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        storeDataTodayViewSupervisorAws.carryoutSales.total.actual.amount
                    )
                )
            when {
                storeDataTodayViewSupervisorAws.carryoutSales.total.status.toString() == resources.getString(
                    R.string.out_of_range
                ) -> {
                    carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    carryout_average_percentage.setTextColor(getColor(R.color.red))
                }
                storeDataTodayViewSupervisorAws.carryoutSales.total.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
                    carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    carryout_average_percentage.setTextColor(getColor(R.color.green))

                }
                else -> {
                    carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    carryout_average_percentage.setTextColor(getColor(R.color.text_color))

                }
            }
        }

        if (storeDataTodayViewSupervisorAws.carryoutSales?.pcya?.actual?.percentage?.isNaN() == false) {
            carry_out_pcy_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.carryoutSales.pcya.actual.percentage
            )
            carry_out_pcy_value.visibility = View.VISIBLE
            awus_carryout_sales_error_olo.visibility = View.GONE

        } else {
            carry_out_pcy_value.visibility = View.GONE
            awus_carryout_sales_error_olo.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.carryoutSales?.orderCount?.actual?.percentage?.isNaN() == false) {
            carry_outorder_count_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.carryoutSales.orderCount.actual.percentage
            )
            carry_outorder_count_value.visibility = View.VISIBLE
            awus_carryout_sales_error_oc.visibility = View.GONE

        } else {
            carry_outorder_count_value.visibility = View.GONE
            awus_carryout_sales_error_oc.visibility = View.VISIBLE

        }
        if (storeDataTodayViewSupervisorAws.carryoutSales?.averageTicket?.actual?.percentage?.isNaN() == false) {
            carry_average_tickets_value.text = Validation().ignoreZeroAfterDecimal(
                storeDataTodayViewSupervisorAws.carryoutSales.averageTicket.actual.percentage
            )
            carry_average_tickets_value.visibility = View.VISIBLE
            awus_carryout_sales_error_at.visibility = View.GONE

        } else {
            carry_average_tickets_value.visibility = View.GONE
            awus_carryout_sales_error_at.visibility = View.VISIBLE

        }
    }

    private fun yesterdayViewSupervisorAws(todayDetail: SupervisorOverviewYesterdayQuery.Supervisor) {
        try {
            val storeDataYesterdayViewSupervisorAws = todayDetail.kpis?.stores?.yesterday!!.sales

            Logger.info("AWUS Yesterday", "AWUS Overview KPI")


            awus_display.text = getString(R.string.awus_text)
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewSupervisorAws?.phoneSales?.pcya?.displayName,
                phone_sales_pcys_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewSupervisorAws?.walkinSales?.pcya?.displayName,
                walkin_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewSupervisorAws?.deliverySales?.pcya?.displayName,
                delivery_sales_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataYesterdayViewSupervisorAws?.carryoutSales?.pcya?.displayName,
                carry_out_sales_pcy_parent
            )

            pcy_display_name.text =
                storeDataYesterdayViewSupervisorAws!!.pcya?.total?.displayName
                    ?: getString(R.string.pcy)
            phone_pcys.text =
                storeDataYesterdayViewSupervisorAws.pcya?.phone?.displayName
                    ?: getString(R.string.phone_pcys)
            walkin_pcy.text =
                storeDataYesterdayViewSupervisorAws.pcya?.walkin?.displayName
                    ?: getString(R.string.walkin_pcy)
            delivery_pcy.text =
                storeDataYesterdayViewSupervisorAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            delivery_pcy.text =
                storeDataYesterdayViewSupervisorAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            carry_out_pcy.text =
                storeDataYesterdayViewSupervisorAws.pcya?.carryout?.displayName
                    ?: getString(R.string.carry_out_pcy)
            // order count display
            order_count_display.text =
                storeDataYesterdayViewSupervisorAws.orderCount?.total?.displayName
                    ?: getString(R.string.order_count)
            olo_order_count.text =
                storeDataYesterdayViewSupervisorAws.orderCount?.olo?.displayName
                    ?: getString(R.string.olo_order_count)
            phone_order_count.text =
                storeDataYesterdayViewSupervisorAws.orderCount?.phone?.displayName
                    ?: getString(R.string.phone_order_count)
            walkin_order_count.text =
                storeDataYesterdayViewSupervisorAws.orderCount?.walkin?.displayName
                    ?: getString(R.string.walkin_order_count)
            delivery_order_count.text =
                storeDataYesterdayViewSupervisorAws.orderCount?.delivery?.displayName
                    ?: getString(R.string.delivery_order_count)
            carry_outorder_count.text =
                storeDataYesterdayViewSupervisorAws.orderCount?.carryout?.displayName
                    ?: getString(R.string.carry_outorder_count)

            // average ticket count display
            if (storeDataYesterdayViewSupervisorAws.averageTicket?.total == null || storeDataYesterdayViewSupervisorAws.averageTicket.total.displayName.isNullOrEmpty()) {
                ll_average_ticket_display_name.visibility = View.GONE
            } else {
                ll_average_ticket_display_name.visibility = View.VISIBLE
                average_ticket_display_name.text =
                    storeDataYesterdayViewSupervisorAws.averageTicket.total.displayName
            }


            if (storeDataYesterdayViewSupervisorAws.averageTicket?.olo == null || storeDataYesterdayViewSupervisorAws.averageTicket.olo.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket.visibility = View.GONE
            } else {
                ll_olo_average_ticket.visibility = View.VISIBLE
                olo_average_ticket.text =
                    storeDataYesterdayViewSupervisorAws.averageTicket.olo.displayName
            }

            if (storeDataYesterdayViewSupervisorAws.averageTicket?.phone == null || storeDataYesterdayViewSupervisorAws.averageTicket.phone.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket.visibility = View.GONE
            } else {
                ll_average_phone_ticket.visibility = View.VISIBLE
                average_phone_ticket.text =
                    storeDataYesterdayViewSupervisorAws.averageTicket.phone.displayName
            }

            if (storeDataYesterdayViewSupervisorAws.averageTicket?.walkin == null || storeDataYesterdayViewSupervisorAws.averageTicket.walkin.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket.visibility = View.GONE
            } else {
                ll_average_walkin_ticket.visibility = View.VISIBLE
                average_walkin_ticket.text =
                    storeDataYesterdayViewSupervisorAws.averageTicket.walkin.displayName
            }

            if (storeDataYesterdayViewSupervisorAws.averageTicket?.delivery == null || storeDataYesterdayViewSupervisorAws.averageTicket.delivery.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets.visibility = View.GONE
            } else {
                ll_delivery_average_tickets.visibility = View.VISIBLE
                delivery_average_tickets.text =
                    storeDataYesterdayViewSupervisorAws.averageTicket.delivery.displayName
            }

            if (storeDataYesterdayViewSupervisorAws.averageTicket?.carryout == null || storeDataYesterdayViewSupervisorAws.averageTicket.carryout.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets.visibility = View.GONE
            } else {
                ll_carry_average_tickets.visibility = View.VISIBLE
                carry_average_tickets.text =
                    storeDataYesterdayViewSupervisorAws.averageTicket.carryout.displayName
            }

            // olo sales display
            if (storeDataYesterdayViewSupervisorAws.oloSales?.total == null || storeDataYesterdayViewSupervisorAws.oloSales.total.displayName.isNullOrEmpty()) {
                ll_olo_sales_display_name.visibility = View.GONE
            } else {
                ll_olo_sales_display_name.visibility = View.VISIBLE
                olo_sales_display_name.text =
                    storeDataYesterdayViewSupervisorAws.oloSales.total.displayName
            }

            if (storeDataYesterdayViewSupervisorAws.oloSales?.pcya == null || storeDataYesterdayViewSupervisorAws.oloSales.pcya.displayName.isNullOrEmpty()) {
                olo_sales_pcy_parent.visibility = View.GONE
            } else {
                olo_sales_pcy_parent.visibility = View.VISIBLE
                olo_pcy.text =
                    storeDataYesterdayViewSupervisorAws.oloSales.pcya.displayName
            }

            if (storeDataYesterdayViewSupervisorAws.oloSales?.orderCount == null || storeDataYesterdayViewSupervisorAws.oloSales.orderCount.displayName.isNullOrEmpty()) {
                ll_olo_order_count_sales_display.visibility = View.GONE
            } else {
                ll_olo_order_count_sales_display.visibility = View.VISIBLE
                olo_order_count_sales_display.text =
                    storeDataYesterdayViewSupervisorAws.oloSales.orderCount.displayName
            }

            if (storeDataYesterdayViewSupervisorAws.oloSales?.averageTicket == null || storeDataYesterdayViewSupervisorAws.oloSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket_sales_display.visibility = View.GONE
            } else {
                ll_olo_average_ticket_sales_display.visibility = View.VISIBLE
                olo_average_ticket_sales_display.text =
                    storeDataYesterdayViewSupervisorAws.oloSales.averageTicket.displayName
            }


            // phone sales display
            if (storeDataYesterdayViewSupervisorAws.phoneSales?.total == null || storeDataYesterdayViewSupervisorAws.phoneSales.total.displayName.isNullOrEmpty()) {
                ll_phone_sales_display_name.visibility = View.GONE
            } else {
                ll_phone_sales_display_name.visibility = View.VISIBLE
                phone_sales_display_name.text =
                    storeDataYesterdayViewSupervisorAws.phoneSales.total.displayName
            }


            if (storeDataYesterdayViewSupervisorAws.phoneSales?.orderCount == null || storeDataYesterdayViewSupervisorAws.phoneSales.orderCount.displayName.isNullOrEmpty()) {
                ll_phone_order_count_display.visibility = View.GONE
            } else {
                ll_phone_order_count_display.visibility = View.VISIBLE
                phone_order_count_display.text =
                    storeDataYesterdayViewSupervisorAws.phoneSales.orderCount.displayName
            }

            if (storeDataYesterdayViewSupervisorAws.phoneSales?.averageTicket == null || storeDataYesterdayViewSupervisorAws.phoneSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket_display.visibility = View.GONE
            } else {
                ll_average_phone_ticket_display.visibility = View.VISIBLE
                average_phone_ticket_display.text =
                    storeDataYesterdayViewSupervisorAws.phoneSales.averageTicket.displayName
            }


            // walk-in Sales
            if (storeDataYesterdayViewSupervisorAws.walkinSales?.total == null || storeDataYesterdayViewSupervisorAws.walkinSales.total.displayName.isNullOrEmpty()) {
                ll_walkin_sales_display_name.visibility = View.GONE
            } else {
                ll_walkin_sales_display_name.visibility = View.VISIBLE
                walkin_sales_display_name.text =
                    storeDataYesterdayViewSupervisorAws.walkinSales.total.displayName
            }


            if (storeDataYesterdayViewSupervisorAws.walkinSales?.orderCount == null || storeDataYesterdayViewSupervisorAws.walkinSales.orderCount.displayName.isNullOrEmpty()) {
                ll_walkin_order_count_sales_display.visibility = View.GONE
            } else {
                ll_walkin_order_count_sales_display.visibility = View.VISIBLE
                walkin_order_count_sales_display.text =
                    storeDataYesterdayViewSupervisorAws.walkinSales.orderCount.displayName
            }

            if (storeDataYesterdayViewSupervisorAws.walkinSales?.averageTicket == null || storeDataYesterdayViewSupervisorAws.walkinSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket_display.visibility = View.GONE
            } else {
                ll_average_walkin_ticket_display.visibility = View.VISIBLE
                average_walkin_ticket_display.text =
                    storeDataYesterdayViewSupervisorAws.walkinSales.averageTicket.displayName
            }


            //  deliver Sales
            if (storeDataYesterdayViewSupervisorAws.deliverySales?.total == null || storeDataYesterdayViewSupervisorAws.deliverySales.total.displayName.isNullOrEmpty()) {
                ll_delivery_sales_display_name.visibility = View.GONE
            } else {
                ll_delivery_sales_display_name.visibility = View.VISIBLE
                delivery_sales_display_name.text =
                    storeDataYesterdayViewSupervisorAws.deliverySales.total.displayName
            }
            if (storeDataYesterdayViewSupervisorAws.deliverySales?.orderCount == null || storeDataYesterdayViewSupervisorAws.deliverySales.orderCount.displayName.isNullOrEmpty()) {
                ll_delivery_order_count_display.visibility = View.GONE
            } else {
                ll_delivery_order_count_display.visibility = View.VISIBLE
                delivery_order_count_display.text =
                    storeDataYesterdayViewSupervisorAws.deliverySales.orderCount.displayName
            }
            if (storeDataYesterdayViewSupervisorAws.deliverySales?.averageTicket == null || storeDataYesterdayViewSupervisorAws.deliverySales.averageTicket.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets_display.visibility = View.GONE
            } else {
                ll_delivery_average_tickets_display.visibility = View.VISIBLE
                delivery_average_tickets_display.text =
                    storeDataYesterdayViewSupervisorAws.deliverySales.averageTicket.displayName
            }


            //  carryout Sales
            if (storeDataYesterdayViewSupervisorAws.carryoutSales?.total == null || storeDataYesterdayViewSupervisorAws.carryoutSales.total.displayName.isNullOrEmpty()) {
                ll_carry_out_display_text.visibility = View.GONE
            } else {
                ll_carry_out_display_text.visibility = View.VISIBLE
                carry_out_display_text.text =
                    storeDataYesterdayViewSupervisorAws.carryoutSales.total.displayName
            }
            if (storeDataYesterdayViewSupervisorAws.carryoutSales?.orderCount == null || storeDataYesterdayViewSupervisorAws.carryoutSales.orderCount.displayName.isNullOrEmpty()) {
                ll_carry_outorder_count_display.visibility = View.GONE
            } else {
                ll_carry_outorder_count_display.visibility = View.VISIBLE
                carry_outorder_count_display.text =
                    storeDataYesterdayViewSupervisorAws.carryoutSales.orderCount.displayName
            }

            if (storeDataYesterdayViewSupervisorAws.carryoutSales?.averageTicket == null || storeDataYesterdayViewSupervisorAws.carryoutSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets_display.visibility = View.GONE
            } else {
                ll_carry_average_tickets_display.visibility = View.VISIBLE
                carry_average_tickets_display.text =
                    storeDataYesterdayViewSupervisorAws.carryoutSales.averageTicket.displayName
            }



            // display null check for olo pcy

            if (storeDataYesterdayViewSupervisorAws.pcya?.olo == null || storeDataYesterdayViewSupervisorAws.pcya.olo.displayName.isNullOrEmpty()) {
                olo_pcy_parent.visibility = View.GONE
            } else {
                olo_pcy_parent.visibility = View.GONE
                olo_pcy_display.text = storeDataYesterdayViewSupervisorAws.pcya.olo.displayName
            }

            if (storeDataYesterdayViewSupervisorAws.pcya?.phone == null || storeDataYesterdayViewSupervisorAws.pcya.phone.displayName.isNullOrEmpty()) {
                phone_pcys_parent.visibility = View.GONE
            } else {
                phone_pcys_parent.visibility = View.VISIBLE
                phone_pcys.text =
                    storeDataYesterdayViewSupervisorAws.pcya.phone.displayName
            }
            if (storeDataYesterdayViewSupervisorAws.pcya?.walkin == null || storeDataYesterdayViewSupervisorAws.pcya.walkin.displayName.isNullOrEmpty()) {
                walkin_pcys_parent.visibility = View.GONE
            } else {
                walkin_pcys_parent.visibility = View.VISIBLE
                walkin_pcy.text =
                    storeDataYesterdayViewSupervisorAws.pcya.walkin.displayName
            }
            if (storeDataYesterdayViewSupervisorAws.pcya?.delivery == null ||
                storeDataYesterdayViewSupervisorAws.pcya.delivery.displayName.isNullOrEmpty()
            ) {
                delivery_pcy_parent.visibility = View.GONE
            } else {
                delivery_pcy_parent.visibility = View.VISIBLE
                delivery_pcy.text =
                    storeDataYesterdayViewSupervisorAws.pcya.delivery.displayName
            }
            if (storeDataYesterdayViewSupervisorAws.pcya?.carryout == null || storeDataYesterdayViewSupervisorAws.pcya.carryout.displayName.isNullOrEmpty()) {
                carry_out_pcy_parent.visibility = View.GONE
            } else {
                carry_out_pcy_parent.visibility = View.VISIBLE
                carry_out_pcy.text =
                    storeDataYesterdayViewSupervisorAws.pcya.carryout.displayName
            }

            /// new
            awus_sales.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewSupervisorAws.actual?.amount,
                storeDataYesterdayViewSupervisorAws.actual?.percentage,
                storeDataYesterdayViewSupervisorAws.actual?.value
            )

            if (storeDataYesterdayViewSupervisorAws.status?.toString() != null) {
                when {
                    storeDataYesterdayViewSupervisorAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                        awus_sales.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewSupervisorAws.status.toString() == resources.getString(R.string.under_limit) -> {
                        awus_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        awus_sales.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // scroll detect

            awus_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (awus_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.awus_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@AWUSKpiActivity,
                                    storeDataYesterdayViewSupervisorAws.actual?.amount,
                                    storeDataYesterdayViewSupervisorAws.actual?.percentage,
                                    storeDataYesterdayViewSupervisorAws.actual?.value
                                )
                            if (storeDataYesterdayViewSupervisorAws.status?.toString() != null) {
                                when {
                                    storeDataYesterdayViewSupervisorAws.status.toString() == resources.getString(
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
                                    storeDataYesterdayViewSupervisorAws.status.toString() == resources.getString(
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
                        y = awus_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            awus_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewSupervisorAws.goal?.amount,
                storeDataYesterdayViewSupervisorAws.goal?.percentage,
                storeDataYesterdayViewSupervisorAws.goal?.value
            )
            awus__variance_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewSupervisorAws.variance?.amount,
                storeDataYesterdayViewSupervisorAws.variance?.percentage,
                storeDataYesterdayViewSupervisorAws.variance?.value
            )

            showAWUSNarrativeData(storeDataYesterdayViewSupervisorAws.narrative.toString())


            // PCY
            pcy_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewSupervisorAws.pcya?.total?.actual?.amount,
                storeDataYesterdayViewSupervisorAws.pcya?.total?.actual?.percentage,
                storeDataYesterdayViewSupervisorAws.pcya?.total?.actual?.value
            )

            if (storeDataYesterdayViewSupervisorAws.pcya?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewSupervisorAws.pcya.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewSupervisorAws.pcya.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.pcya?.olo?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.pcya?.olo?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.pcya?.olo?.actual?.value
                ).isEmpty()
            ) {
                olo_pcy_percentage.visibility = View.GONE
                awus_pcya_error_olo.visibility = View.VISIBLE

            } else {
                olo_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_olo.visibility = View.GONE

                olo_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.pcya?.olo?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.pcya?.olo?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.pcya?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.pcya?.phone?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.pcya?.phone?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.pcya?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_pcy_percentage.visibility = View.GONE
                awus_pcya_error_phone.visibility = View.VISIBLE

            } else {
                phone_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                phone_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.pcya?.phone?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.pcya?.phone?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.pcya?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.pcya?.walkin?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.pcya?.walkin?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.pcya?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_percentage.visibility = View.GONE
                awus_pcya_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                walkin_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.pcya?.walkin?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.pcya?.walkin?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.pcya?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.pcya?.delivery?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.pcya?.delivery?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.pcya?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_percentage.visibility = View.GONE
                awus_pcya_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_delivery.visibility = View.GONE

                delivery_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.pcya?.delivery?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.pcya?.delivery?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.pcya?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.pcya?.carryout?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.pcya?.carryout?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.pcya?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_percentage.visibility = View.GONE
                awus_pcya_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_carryout.visibility = View.GONE

                carry_out_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.pcya?.carryout?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.pcya?.carryout?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.pcya?.carryout?.actual?.value
                )
            }

            // order count
            oc_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewSupervisorAws.orderCount?.total?.actual?.amount,
                storeDataYesterdayViewSupervisorAws.orderCount?.total?.actual?.percentage,
                storeDataYesterdayViewSupervisorAws.orderCount?.total?.actual?.value
            )

            if (storeDataYesterdayViewSupervisorAws.orderCount?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewSupervisorAws.orderCount.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewSupervisorAws.orderCount.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.green))

                    } else -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.orderCount?.olo?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.orderCount?.olo?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.orderCount?.olo?.actual?.value
                ).isEmpty()
            ) {
                oc_pcy_percentage.visibility = View.GONE
                awus_oc_error_olo.visibility = View.VISIBLE

            } else {
                oc_pcy_percentage.visibility = View.VISIBLE
                awus_oc_error_olo.visibility = View.GONE

                oc_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.orderCount?.olo?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.orderCount?.olo?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.orderCount?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.orderCount?.phone?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.orderCount?.phone?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.orderCount?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_oc_percentage.visibility = View.GONE
                awus_oc_error_phone.visibility = View.VISIBLE

            } else {
                phone_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_phone.visibility = View.GONE

                phone_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.orderCount?.phone?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.orderCount?.phone?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.orderCount?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.orderCount?.walkin?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.orderCount?.walkin?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.orderCount?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_oc_percentage.visibility = View.GONE
                awus_oc_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_walkin.visibility = View.GONE

                walkin_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.orderCount?.walkin?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.orderCount?.walkin?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.orderCount?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.orderCount?.delivery?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.orderCount?.delivery?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.orderCount?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_oc_percentage.visibility = View.GONE
                awus_oc_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_delivery.visibility = View.GONE

                delivery_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.orderCount?.delivery?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.orderCount?.delivery?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.orderCount?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.orderCount?.carryout?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.orderCount?.carryout?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.orderCount?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_oc_percentage.visibility = View.GONE
                awus_oc_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_carryout.visibility = View.GONE

                carry_out_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.orderCount?.carryout?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.orderCount?.carryout?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.orderCount?.carryout?.actual?.value
                )
            }

            // Average  Tickets
            average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewSupervisorAws.averageTicket?.total?.actual?.amount,
                storeDataYesterdayViewSupervisorAws.averageTicket?.total?.actual?.percentage,
                storeDataYesterdayViewSupervisorAws.averageTicket?.total?.actual?.value
            )

            if (storeDataYesterdayViewSupervisorAws.averageTicket?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewSupervisorAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewSupervisorAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.olo?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.olo?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.olo?.actual?.value
                ).isEmpty()
            ) {
                average_olo_percentage.visibility = View.GONE
                awus_avg_error_olo.visibility = View.VISIBLE

            } else {
                average_olo_percentage.visibility = View.VISIBLE
                awus_avg_error_olo.visibility = View.GONE

                average_olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.olo?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.olo?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.phone?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.phone?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.phone?.actual?.value
                ).isEmpty()
            ) {
                average_phone_percentage.visibility = View.GONE
                awus_avg_error_phone.visibility = View.VISIBLE

            } else {
                average_phone_percentage.visibility = View.VISIBLE
                awus_avg_error_phone.visibility = View.GONE

                average_phone_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.phone?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.phone?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.walkin?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_average_percentage.visibility = View.GONE
                awus_avg_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_average_percentage.visibility = View.VISIBLE
                awus_avg_error_walkin.visibility = View.GONE

                walkin_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.walkin?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.delivery?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_percentage.visibility = View.GONE
                awus_avg_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_percentage.visibility = View.VISIBLE
                awus_avg_error_delivery.visibility = View.GONE

                delivery_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.delivery?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.carryout?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_average_percentage.visibility = View.GONE
                awus_avg_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_average_percentage.visibility = View.VISIBLE
                awus_avg_error_carryout.visibility = View.GONE

                carry_out_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.carryout?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.averageTicket?.carryout?.actual?.value
                )
            }

            // OLO sales
            olo_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewSupervisorAws.oloSales?.total?.actual?.amount,
                storeDataYesterdayViewSupervisorAws.oloSales?.total?.actual?.percentage,
                storeDataYesterdayViewSupervisorAws.oloSales?.total?.actual?.value
            )

            if (storeDataYesterdayViewSupervisorAws.oloSales?.total?.status?.toString() != null) {

                when {
                    storeDataYesterdayViewSupervisorAws.oloSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewSupervisorAws.oloSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.oloSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.oloSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.oloSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                olo_percentage.visibility = View.GONE
                awus_olo_sales_error_olo.visibility = View.VISIBLE

            } else {
                olo_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_olo.visibility = View.GONE

                olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.oloSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.oloSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.oloSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.oloSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.oloSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                olo_order_percentage.visibility = View.GONE
                awus_olo_sales_error_oc.visibility = View.VISIBLE

            } else {
                olo_order_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_oc.visibility = View.GONE

                olo_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.oloSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.oloSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.oloSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                olo_average_percentage.visibility = View.GONE
                awus_olo_sales_error_at.visibility = View.VISIBLE

            } else {
                olo_average_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_at.visibility = View.GONE

                olo_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.oloSales?.averageTicket?.actual?.value
                )
            }

            // Phone sales order count
            phone_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewSupervisorAws.phoneSales?.total?.actual?.amount,
                storeDataYesterdayViewSupervisorAws.phoneSales?.total?.actual?.percentage,
                storeDataYesterdayViewSupervisorAws.phoneSales?.total?.actual?.value
            )
            if (storeDataYesterdayViewSupervisorAws.phoneSales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewSupervisorAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewSupervisorAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                phone_pcys_percentage.visibility = View.GONE
                awus_phones_sales_error_olo.visibility = View.VISIBLE

            } else {
                phone_pcys_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_olo.visibility = View.GONE

                phone_pcys_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_phone_ticket_percentage.visibility = View.GONE
                awus_phones_sales_error_at.visibility = View.VISIBLE

            } else {
                average_phone_ticket_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_at.visibility = View.GONE

                average_phone_ticket_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.averageTicket?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                phone_sales_order_percentage.visibility = View.GONE
                awus_phones_sales_error_oc.visibility = View.VISIBLE

            } else {
                phone_sales_order_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_oc.visibility = View.GONE

                phone_sales_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.oloSales?.orderCount?.actual?.value
                )
            }

            // walk-in sales
            walkin_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewSupervisorAws.walkinSales?.total?.actual?.amount,
                storeDataYesterdayViewSupervisorAws.walkinSales?.total?.actual?.percentage,
                storeDataYesterdayViewSupervisorAws.walkinSales?.total?.actual?.value
            )

            if (storeDataYesterdayViewSupervisorAws.walkinSales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewSupervisorAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewSupervisorAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_value.visibility = View.GONE
                awus_walkins_sales_error_olo.visibility = View.VISIBLE

            } else {
                walkin_pcy_value.visibility = View.VISIBLE
                awus_walkins_sales_error_olo.visibility = View.GONE

                walkin_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                walkin_order_count_value.visibility = View.GONE
                awus_walkins_sales_error_oc.visibility = View.VISIBLE

            } else {
                walkin_order_count_value.visibility = View.VISIBLE
                awus_walkins_sales_error_oc.visibility = View.GONE

                walkin_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_walkin_ticket_value.visibility = View.GONE
                awus_walkins_sales_error_at.visibility = View.VISIBLE

            } else {
                average_walkin_ticket_value.visibility = View.VISIBLE
                awus_walkins_sales_error_at.visibility = View.GONE

                average_walkin_ticket_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.walkinSales?.averageTicket?.actual?.value
                )
            }

            // delivery sales
            delivery_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewSupervisorAws.deliverySales?.total?.actual?.amount,
                storeDataYesterdayViewSupervisorAws.deliverySales?.total?.actual?.percentage,
                storeDataYesterdayViewSupervisorAws.deliverySales?.total?.actual?.value
            )

            if (storeDataYesterdayViewSupervisorAws.deliverySales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewSupervisorAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewSupervisorAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.pcya?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_value.visibility = View.GONE
                awus_delivery_sales_error_olo.visibility = View.VISIBLE

            } else {
                delivery_pcy_value.visibility = View.VISIBLE
                awus_delivery_sales_error_olo.visibility = View.GONE

                delivery_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.pcya?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                delivery_order_count_value.visibility = View.GONE
                awus_delivery_sales_error_oc.visibility = View.VISIBLE

            } else {
                delivery_order_count_value.visibility = View.VISIBLE
                awus_delivery_sales_error_oc.visibility = View.GONE

                delivery_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                delivery_average_percentage.visibility = View.GONE
                awus_delivery_sales_error_at.visibility = View.VISIBLE

            } else {
                delivery_average_percentage.visibility = View.VISIBLE
                awus_delivery_sales_error_at.visibility = View.GONE

                delivery_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.deliverySales?.averageTicket?.actual?.value
                )
            }

            // carryout sales
            carryout_average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataYesterdayViewSupervisorAws.carryoutSales?.total?.actual?.amount,
                storeDataYesterdayViewSupervisorAws.carryoutSales?.total?.actual?.percentage,
                storeDataYesterdayViewSupervisorAws.carryoutSales?.total?.actual?.value
            )

            if (storeDataYesterdayViewSupervisorAws.carryoutSales?.total?.status?.toString() != null) {
                when {
                    storeDataYesterdayViewSupervisorAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataYesterdayViewSupervisorAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_value.visibility = View.GONE
                awus_carryout_sales_error_olo.visibility = View.VISIBLE

            } else {
                carry_out_pcy_value.visibility = View.VISIBLE
                awus_carryout_sales_error_olo.visibility = View.GONE

                carry_out_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                carry_outorder_count_value.visibility = View.GONE
                awus_carryout_sales_error_oc.visibility = View.VISIBLE

            } else {
                carry_outorder_count_value.visibility = View.VISIBLE
                awus_carryout_sales_error_oc.visibility = View.GONE

                carry_outorder_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                carry_average_tickets_value.visibility = View.GONE
                awus_carryout_sales_error_at.visibility = View.VISIBLE

            } else {
                carry_average_tickets_value.visibility = View.VISIBLE
                awus_carryout_sales_error_at.visibility = View.GONE

                carry_average_tickets_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataYesterdayViewSupervisorAws.carryoutSales?.averageTicket?.actual?.value
                )
            }
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"AWUS Overview Yesterday KPI")
        }
    }

    private fun rangeViewSupervisorAws(todayDetail: SupervisorOverviewRangeQuery.Supervisor) {
        try {
            val storeDataRangeViewSupervisorAws = todayDetail.kpis?.stores?.period!!.sales

            Logger.info("AWUS Period Range", "AWUS Overview KPI")


            awus_display.text = getString(R.string.awus_text)
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeViewSupervisorAws?.phoneSales?.pcya?.displayName,
                phone_sales_pcys_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeViewSupervisorAws?.walkinSales?.pcya?.displayName,
                walkin_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeViewSupervisorAws?.deliverySales?.pcya?.displayName,
                delivery_sales_pcy_parent
            )
            Validation().checkNullValueToShowView(
                this,
                storeDataRangeViewSupervisorAws?.carryoutSales?.pcya?.displayName,
                carry_out_sales_pcy_parent
            )

            pcy_display_name.text =
                storeDataRangeViewSupervisorAws!!.pcya?.total?.displayName
                    ?: getString(R.string.pcy)
            phone_pcys.text =
                storeDataRangeViewSupervisorAws.pcya?.phone?.displayName
                    ?: getString(R.string.phone_pcys)
            walkin_pcy.text =
                storeDataRangeViewSupervisorAws.pcya?.walkin?.displayName
                    ?: getString(R.string.walkin_pcy)
            delivery_pcy.text =
                storeDataRangeViewSupervisorAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            delivery_pcy.text =
                storeDataRangeViewSupervisorAws.pcya?.delivery?.displayName
                    ?: getString(R.string.delivery_pcy)
            carry_out_pcy.text =
                storeDataRangeViewSupervisorAws.pcya?.carryout?.displayName
                    ?: getString(R.string.carry_out_pcy)
            // order count display
            order_count_display.text =
                storeDataRangeViewSupervisorAws.orderCount?.total?.displayName
                    ?: getString(R.string.order_count)
            olo_order_count.text =
                storeDataRangeViewSupervisorAws.orderCount?.olo?.displayName
                    ?: getString(R.string.olo_order_count)
            phone_order_count.text = storeDataRangeViewSupervisorAws.orderCount?.phone?.displayName
                ?: getString(R.string.phone_order_count)
            walkin_order_count.text =
                storeDataRangeViewSupervisorAws.orderCount?.walkin?.displayName
                    ?: getString(R.string.walkin_order_count)
            delivery_order_count.text =
                storeDataRangeViewSupervisorAws.orderCount?.delivery?.displayName
                    ?: getString(R.string.delivery_order_count)
            carry_outorder_count.text =
                storeDataRangeViewSupervisorAws.orderCount?.carryout?.displayName
                    ?: getString(R.string.carry_outorder_count)

            // average ticket count display
            if (storeDataRangeViewSupervisorAws.averageTicket?.total == null || storeDataRangeViewSupervisorAws.averageTicket.total.displayName.isNullOrEmpty()) {
                ll_average_ticket_display_name.visibility = View.GONE
            } else {
                ll_average_ticket_display_name.visibility = View.VISIBLE
                average_ticket_display_name.text =
                    storeDataRangeViewSupervisorAws.averageTicket.total.displayName
            }


            if (storeDataRangeViewSupervisorAws.averageTicket?.olo == null || storeDataRangeViewSupervisorAws.averageTicket.olo.displayName.isNullOrEmpty()) {
                ll_olo_average_ticket.visibility = View.GONE
            } else {
                ll_olo_average_ticket.visibility = View.VISIBLE
                olo_average_ticket.text =
                    storeDataRangeViewSupervisorAws.averageTicket.olo.displayName
            }

            if (storeDataRangeViewSupervisorAws.averageTicket?.phone == null || storeDataRangeViewSupervisorAws.averageTicket.phone.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket.visibility = View.GONE
            } else {
                ll_average_phone_ticket.visibility = View.VISIBLE
                average_phone_ticket.text =
                    storeDataRangeViewSupervisorAws.averageTicket.phone.displayName
            }

            if (storeDataRangeViewSupervisorAws.averageTicket?.walkin == null || storeDataRangeViewSupervisorAws.averageTicket.walkin.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket.visibility = View.GONE
            } else {
                ll_average_walkin_ticket.visibility = View.VISIBLE
                average_walkin_ticket.text =
                    storeDataRangeViewSupervisorAws.averageTicket.walkin.displayName
            }

            if (storeDataRangeViewSupervisorAws.averageTicket?.delivery == null || storeDataRangeViewSupervisorAws.averageTicket.delivery.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets.visibility = View.GONE
            } else {
                ll_delivery_average_tickets.visibility = View.VISIBLE
                delivery_average_tickets.text =
                    storeDataRangeViewSupervisorAws.averageTicket.delivery.displayName
            }

            if (storeDataRangeViewSupervisorAws.averageTicket?.carryout == null || storeDataRangeViewSupervisorAws.averageTicket.carryout.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets.visibility = View.GONE
            } else {
                ll_carry_average_tickets.visibility = View.VISIBLE
                carry_average_tickets.text =
                    storeDataRangeViewSupervisorAws.averageTicket.carryout.displayName
            }


            // olo sales display
            olo_sales_display_name.text =
                storeDataRangeViewSupervisorAws.oloSales?.total?.displayName
                    ?: getString(R.string.olo_sales)
            olo_pcy.text = storeDataRangeViewSupervisorAws.oloSales?.pcya?.displayName ?: getString(
                R.string.olo_pcy
            )
            olo_order_count_sales_display.text =
                storeDataRangeViewSupervisorAws.oloSales?.orderCount?.displayName
                    ?: getString(R.string.olo_order_count)
            olo_average_ticket_sales_display.text =
                storeDataRangeViewSupervisorAws.oloSales?.averageTicket?.displayName
                    ?: getString(R.string.olo_average_ticket)

            // phone sales display
            if (storeDataRangeViewSupervisorAws.phoneSales?.total == null || storeDataRangeViewSupervisorAws.phoneSales.total.displayName.isNullOrEmpty()) {
                ll_phone_sales_display_name.visibility = View.GONE
            } else {
                ll_phone_sales_display_name.visibility = View.VISIBLE
                phone_sales_display_name.text =
                    storeDataRangeViewSupervisorAws.phoneSales.total.displayName
            }


            if (storeDataRangeViewSupervisorAws.phoneSales?.orderCount == null || storeDataRangeViewSupervisorAws.phoneSales.orderCount.displayName.isNullOrEmpty()) {
                ll_phone_order_count_display.visibility = View.GONE
            } else {
                ll_phone_order_count_display.visibility = View.VISIBLE
                phone_order_count_display.text =
                    storeDataRangeViewSupervisorAws.phoneSales.orderCount.displayName
            }

            if (storeDataRangeViewSupervisorAws.phoneSales?.averageTicket == null || storeDataRangeViewSupervisorAws.phoneSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_phone_ticket_display.visibility = View.GONE
            } else {
                ll_average_phone_ticket_display.visibility = View.VISIBLE
                average_phone_ticket_display.text =
                    storeDataRangeViewSupervisorAws.phoneSales.averageTicket.displayName
            }


            // walk-in Sales
            if (storeDataRangeViewSupervisorAws.walkinSales?.total == null || storeDataRangeViewSupervisorAws.walkinSales.total.displayName.isNullOrEmpty()) {
                ll_walkin_sales_display_name.visibility = View.GONE
            } else {
                ll_walkin_sales_display_name.visibility = View.VISIBLE
                walkin_sales_display_name.text =
                    storeDataRangeViewSupervisorAws.walkinSales.total.displayName
            }
            if (storeDataRangeViewSupervisorAws.walkinSales?.orderCount == null || storeDataRangeViewSupervisorAws.walkinSales.orderCount.displayName.isNullOrEmpty()) {
                ll_walkin_order_count_sales_display.visibility = View.GONE
            } else {
                ll_walkin_order_count_sales_display.visibility = View.VISIBLE
                walkin_order_count_sales_display.text =
                    storeDataRangeViewSupervisorAws.walkinSales.orderCount.displayName
            }



            if (storeDataRangeViewSupervisorAws.walkinSales?.averageTicket == null || storeDataRangeViewSupervisorAws.walkinSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_average_walkin_ticket_display.visibility = View.GONE
            } else {
                ll_average_walkin_ticket_display.visibility = View.VISIBLE
                average_walkin_ticket_display.text =
                    storeDataRangeViewSupervisorAws.walkinSales.averageTicket.displayName
            }



            //  deliver Sales
            if (storeDataRangeViewSupervisorAws.deliverySales?.total == null || storeDataRangeViewSupervisorAws.deliverySales.total.displayName.isNullOrEmpty()) {
                ll_delivery_sales_display_name.visibility = View.GONE
            } else {
                ll_delivery_sales_display_name.visibility = View.VISIBLE
                delivery_sales_display_name.text =
                    storeDataRangeViewSupervisorAws.deliverySales.total.displayName
            }
            if (storeDataRangeViewSupervisorAws.deliverySales?.orderCount == null || storeDataRangeViewSupervisorAws.deliverySales.orderCount.displayName.isNullOrEmpty()) {
                ll_delivery_order_count_display.visibility = View.GONE
            } else {
                ll_delivery_order_count_display.visibility = View.VISIBLE
                delivery_order_count_display.text =
                    storeDataRangeViewSupervisorAws.deliverySales.orderCount.displayName
            }
            if (storeDataRangeViewSupervisorAws.deliverySales?.averageTicket == null || storeDataRangeViewSupervisorAws.deliverySales.averageTicket.displayName.isNullOrEmpty()) {
                ll_delivery_average_tickets_display.visibility = View.GONE
            } else {
                ll_delivery_average_tickets_display.visibility = View.VISIBLE
                delivery_average_tickets_display.text =
                    storeDataRangeViewSupervisorAws.deliverySales.averageTicket.displayName
            }

            //  carryout Sales
            if (storeDataRangeViewSupervisorAws.carryoutSales?.total == null || storeDataRangeViewSupervisorAws.carryoutSales.total.displayName.isNullOrEmpty()) {
                ll_carry_out_display_text.visibility = View.GONE
            } else {
                ll_carry_out_display_text.visibility = View.VISIBLE
                carry_out_display_text.text =
                    storeDataRangeViewSupervisorAws.carryoutSales.total.displayName
            }
            if (storeDataRangeViewSupervisorAws.carryoutSales?.orderCount == null || storeDataRangeViewSupervisorAws.carryoutSales.orderCount.displayName.isNullOrEmpty()) {
                ll_carry_outorder_count_display.visibility = View.GONE
            } else {
                ll_carry_outorder_count_display.visibility = View.VISIBLE
                carry_outorder_count_display.text =
                    storeDataRangeViewSupervisorAws.carryoutSales.orderCount.displayName
            }

            if (storeDataRangeViewSupervisorAws.carryoutSales?.averageTicket == null || storeDataRangeViewSupervisorAws.carryoutSales.averageTicket.displayName.isNullOrEmpty()) {
                ll_carry_average_tickets_display.visibility = View.GONE
            } else {
                ll_carry_average_tickets_display.visibility = View.VISIBLE
                carry_average_tickets_display.text =
                    storeDataRangeViewSupervisorAws.carryoutSales.averageTicket.displayName
            }


            // display null check for olo pcy

            if (storeDataRangeViewSupervisorAws.pcya?.olo == null || storeDataRangeViewSupervisorAws.pcya.olo.displayName.isNullOrEmpty()) {
                olo_pcy_parent.visibility = View.GONE
            } else {
                olo_pcy_parent.visibility = View.GONE
                olo_pcy_display.text = storeDataRangeViewSupervisorAws.pcya.olo.displayName
            }

            if (storeDataRangeViewSupervisorAws.pcya?.phone == null || storeDataRangeViewSupervisorAws.pcya.phone.displayName.isNullOrEmpty()) {
                phone_pcys_parent.visibility = View.GONE
            } else {
                phone_pcys_parent.visibility = View.VISIBLE
                phone_pcys.text =
                    storeDataRangeViewSupervisorAws.pcya.phone.displayName
            }
            if (storeDataRangeViewSupervisorAws.pcya?.walkin == null || storeDataRangeViewSupervisorAws.pcya.walkin.displayName.isNullOrEmpty()) {
                walkin_pcys_parent.visibility = View.GONE
            } else {
                walkin_pcys_parent.visibility = View.VISIBLE
                walkin_pcy.text =
                    storeDataRangeViewSupervisorAws.pcya.walkin.displayName
            }
            if (storeDataRangeViewSupervisorAws.pcya?.delivery == null ||
                storeDataRangeViewSupervisorAws.pcya.delivery.displayName.isNullOrEmpty()
            ) {
                delivery_pcy_parent.visibility = View.GONE
            } else {
                delivery_pcy_parent.visibility = View.VISIBLE
                delivery_pcy.text =
                    storeDataRangeViewSupervisorAws.pcya.delivery.displayName
            }
            if (storeDataRangeViewSupervisorAws.pcya?.carryout == null || storeDataRangeViewSupervisorAws.pcya.carryout.displayName.isNullOrEmpty()) {
                carry_out_pcy_parent.visibility = View.GONE
            } else {
                carry_out_pcy_parent.visibility = View.VISIBLE
                carry_out_pcy.text =
                    storeDataRangeViewSupervisorAws.pcya.carryout.displayName
            }

            /// new
            awus_sales.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewSupervisorAws.actual?.amount,
                storeDataRangeViewSupervisorAws.actual?.percentage,
                storeDataRangeViewSupervisorAws.actual?.value
            )

            if (storeDataRangeViewSupervisorAws.status?.toString() != null) {
                when {
                    storeDataRangeViewSupervisorAws.status.toString() == resources.getString(R.string.out_of_range) -> {
                        awus_sales.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewSupervisorAws.status.toString() == resources.getString(R.string.under_limit) -> {
                        awus_sales.setTextColor(getColor(R.color.green))
                    }
                    else -> {
                        awus_sales.setTextColor(getColor(R.color.text_color))

                    }
                }
            }
            // scroll detect

            awus_scroll_parent.viewTreeObserver
                .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                    var y = 0f
                    override fun onScrollChanged() {
                        if (awus_scroll_parent.scrollY > y) {
                            parent_data_on_scroll_linear.visibility = View.VISIBLE
                            parent_data_on_scroll_view.visibility = View.VISIBLE
                            level_two_scroll_data_action.text = getString(R.string.awus_text)
                            level_two_scroll_data_action_value.text =
                                Validation().checkAmountPercentageValue(
                                    this@AWUSKpiActivity,
                                    storeDataRangeViewSupervisorAws.actual?.amount,
                                    storeDataRangeViewSupervisorAws.actual?.percentage,
                                    storeDataRangeViewSupervisorAws.actual?.value
                                )
                            if (storeDataRangeViewSupervisorAws.status?.toString() != null) {
                                when {
                                    storeDataRangeViewSupervisorAws.status.toString() == resources.getString(
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
                                    storeDataRangeViewSupervisorAws.status.toString() == resources.getString(
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
                        y = awus_scroll_parent.scrollY.toFloat()
                        if (y <= 0) {
                            parent_data_on_scroll_linear.visibility = View.INVISIBLE
                            parent_data_on_scroll_view.visibility = View.GONE
                        }
                    }
                })

            awus_goal_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewSupervisorAws.goal?.amount,
                storeDataRangeViewSupervisorAws.goal?.percentage,
                storeDataRangeViewSupervisorAws.goal?.value
            )
            awus__variance_value.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewSupervisorAws.variance?.amount,
                storeDataRangeViewSupervisorAws.variance?.percentage,
                storeDataRangeViewSupervisorAws.variance?.value
            )

            showAWUSNarrativeData(storeDataRangeViewSupervisorAws.narrative.toString())

            // PCY
            pcy_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewSupervisorAws.pcya?.total?.actual?.amount,
                storeDataRangeViewSupervisorAws.pcya?.total?.actual?.percentage,
                storeDataRangeViewSupervisorAws.pcya?.total?.actual?.value
            )

            if (storeDataRangeViewSupervisorAws.pcya?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewSupervisorAws.pcya.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewSupervisorAws.pcya.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        pcy_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        pcy_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.pcya?.olo?.actual?.amount,
                    storeDataRangeViewSupervisorAws.pcya?.olo?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.pcya?.olo?.actual?.value
                ).isEmpty()
            ) {
                olo_pcy_percentage.visibility = View.GONE
                awus_pcya_error_olo.visibility = View.VISIBLE

            } else {
                olo_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_olo.visibility = View.GONE

                olo_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.pcya?.olo?.actual?.amount,
                    storeDataRangeViewSupervisorAws.pcya?.olo?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.pcya?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.pcya?.phone?.actual?.amount,
                    storeDataRangeViewSupervisorAws.pcya?.phone?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.pcya?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_pcy_percentage.visibility = View.GONE
                awus_pcya_error_phone.visibility = View.VISIBLE

            } else {
                phone_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                phone_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.pcya?.phone?.actual?.amount,
                    storeDataRangeViewSupervisorAws.pcya?.phone?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.pcya?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.pcya?.walkin?.actual?.amount,
                    storeDataRangeViewSupervisorAws.pcya?.walkin?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.pcya?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_percentage.visibility = View.GONE
                awus_pcya_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_phone.visibility = View.GONE

                walkin_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.pcya?.walkin?.actual?.amount,
                    storeDataRangeViewSupervisorAws.pcya?.walkin?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.pcya?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.pcya?.delivery?.actual?.amount,
                    storeDataRangeViewSupervisorAws.pcya?.delivery?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.pcya?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_percentage.visibility = View.GONE
                awus_pcya_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_delivery.visibility = View.GONE

                delivery_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.pcya?.delivery?.actual?.amount,
                    storeDataRangeViewSupervisorAws.pcya?.delivery?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.pcya?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.pcya?.carryout?.actual?.amount,
                    storeDataRangeViewSupervisorAws.pcya?.carryout?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.pcya?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_percentage.visibility = View.GONE
                awus_pcya_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_pcy_percentage.visibility = View.VISIBLE
                awus_pcya_error_carryout.visibility = View.GONE

                carry_out_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.pcya?.carryout?.actual?.amount,
                    storeDataRangeViewSupervisorAws.pcya?.carryout?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.pcya?.carryout?.actual?.value
                )
            }

            // order count
            oc_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewSupervisorAws.orderCount?.total?.actual?.amount,
                storeDataRangeViewSupervisorAws.orderCount?.total?.actual?.percentage,
                storeDataRangeViewSupervisorAws.orderCount?.total?.actual?.value
            )

            if (storeDataRangeViewSupervisorAws.orderCount?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewSupervisorAws.orderCount.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewSupervisorAws.orderCount.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.green))

                    } else -> {
                        oc_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        oc_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.orderCount?.olo?.actual?.amount,
                    storeDataRangeViewSupervisorAws.orderCount?.olo?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.orderCount?.olo?.actual?.value
                ).isEmpty()
            ) {
                oc_pcy_percentage.visibility = View.GONE
                awus_oc_error_olo.visibility = View.VISIBLE

            } else {
                oc_pcy_percentage.visibility = View.VISIBLE
                awus_oc_error_olo.visibility = View.GONE

                oc_pcy_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.orderCount?.olo?.actual?.amount,
                    storeDataRangeViewSupervisorAws.orderCount?.olo?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.orderCount?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.orderCount?.phone?.actual?.amount,
                    storeDataRangeViewSupervisorAws.orderCount?.phone?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.orderCount?.phone?.actual?.value
                ).isEmpty()
            ) {
                phone_oc_percentage.visibility = View.GONE
                awus_oc_error_phone.visibility = View.VISIBLE

            } else {
                phone_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_phone.visibility = View.GONE

                phone_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.orderCount?.phone?.actual?.amount,
                    storeDataRangeViewSupervisorAws.orderCount?.phone?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.orderCount?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.orderCount?.walkin?.actual?.amount,
                    storeDataRangeViewSupervisorAws.orderCount?.walkin?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.orderCount?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_oc_percentage.visibility = View.GONE
                awus_oc_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_walkin.visibility = View.GONE

                walkin_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.orderCount?.walkin?.actual?.amount,
                    storeDataRangeViewSupervisorAws.orderCount?.walkin?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.orderCount?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.orderCount?.delivery?.actual?.amount,
                    storeDataRangeViewSupervisorAws.orderCount?.delivery?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.orderCount?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_oc_percentage.visibility = View.GONE
                awus_oc_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_delivery.visibility = View.GONE

                delivery_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.orderCount?.delivery?.actual?.amount,
                    storeDataRangeViewSupervisorAws.orderCount?.delivery?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.orderCount?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.orderCount?.carryout?.actual?.amount,
                    storeDataRangeViewSupervisorAws.orderCount?.carryout?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.orderCount?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_oc_percentage.visibility = View.GONE
                awus_oc_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_oc_percentage.visibility = View.VISIBLE
                awus_oc_error_carryout.visibility = View.GONE

                carry_out_oc_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.orderCount?.carryout?.actual?.amount,
                    storeDataRangeViewSupervisorAws.orderCount?.carryout?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.orderCount?.carryout?.actual?.value
                )
            }

            // Average  Tickets
            average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewSupervisorAws.averageTicket?.total?.actual?.amount,
                storeDataRangeViewSupervisorAws.averageTicket?.total?.actual?.percentage,
                storeDataRangeViewSupervisorAws.averageTicket?.total?.actual?.value
            )

            if (storeDataRangeViewSupervisorAws.averageTicket?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewSupervisorAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewSupervisorAws.averageTicket.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.averageTicket?.olo?.actual?.amount,
                    storeDataRangeViewSupervisorAws.averageTicket?.olo?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.averageTicket?.olo?.actual?.value
                ).isEmpty()
            ) {
                average_olo_percentage.visibility = View.GONE
                awus_avg_error_olo.visibility = View.VISIBLE

            } else {
                average_olo_percentage.visibility = View.VISIBLE
                awus_avg_error_olo.visibility = View.GONE

                average_olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.averageTicket?.olo?.actual?.amount,
                    storeDataRangeViewSupervisorAws.averageTicket?.olo?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.averageTicket?.olo?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.averageTicket?.phone?.actual?.amount,
                    storeDataRangeViewSupervisorAws.averageTicket?.phone?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.averageTicket?.phone?.actual?.value
                ).isEmpty()
            ) {
                average_phone_percentage.visibility = View.GONE
                awus_avg_error_phone.visibility = View.VISIBLE

            } else {
                average_phone_percentage.visibility = View.VISIBLE
                awus_avg_error_phone.visibility = View.GONE

                average_phone_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.averageTicket?.phone?.actual?.amount,
                    storeDataRangeViewSupervisorAws.averageTicket?.phone?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.averageTicket?.phone?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.averageTicket?.walkin?.actual?.amount,
                    storeDataRangeViewSupervisorAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.averageTicket?.walkin?.actual?.value
                ).isEmpty()
            ) {
                walkin_average_percentage.visibility = View.GONE
                awus_avg_error_walkin.visibility = View.VISIBLE

            } else {
                walkin_average_percentage.visibility = View.VISIBLE
                awus_avg_error_walkin.visibility = View.GONE

                walkin_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.averageTicket?.walkin?.actual?.amount,
                    storeDataRangeViewSupervisorAws.averageTicket?.walkin?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.averageTicket?.walkin?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.averageTicket?.delivery?.actual?.amount,
                    storeDataRangeViewSupervisorAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.averageTicket?.delivery?.actual?.value
                ).isEmpty()
            ) {
                delivery_percentage.visibility = View.GONE
                awus_avg_error_delivery.visibility = View.VISIBLE

            } else {
                delivery_percentage.visibility = View.VISIBLE
                awus_avg_error_delivery.visibility = View.GONE

                delivery_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.averageTicket?.delivery?.actual?.amount,
                    storeDataRangeViewSupervisorAws.averageTicket?.delivery?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.averageTicket?.delivery?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.averageTicket?.carryout?.actual?.amount,
                    storeDataRangeViewSupervisorAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.averageTicket?.carryout?.actual?.value
                ).isEmpty()
            ) {
                carry_out_average_percentage.visibility = View.GONE
                awus_avg_error_carryout.visibility = View.VISIBLE

            } else {
                carry_out_average_percentage.visibility = View.VISIBLE
                awus_avg_error_carryout.visibility = View.GONE

                carry_out_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.averageTicket?.carryout?.actual?.amount,
                    storeDataRangeViewSupervisorAws.averageTicket?.carryout?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.averageTicket?.carryout?.actual?.value
                )
            }

            // OLO sales
            olo_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewSupervisorAws.oloSales?.total?.actual?.amount,
                storeDataRangeViewSupervisorAws.oloSales?.total?.actual?.percentage,
                storeDataRangeViewSupervisorAws.oloSales?.total?.actual?.value
            )

            if (storeDataRangeViewSupervisorAws.oloSales?.total?.status?.toString() != null) {

                when {
                    storeDataRangeViewSupervisorAws.oloSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewSupervisorAws.oloSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        olo_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        olo_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.oloSales?.pcya?.actual?.amount,
                    storeDataRangeViewSupervisorAws.oloSales?.pcya?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.oloSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                olo_percentage.visibility = View.GONE
                awus_olo_sales_error_olo.visibility = View.VISIBLE

            } else {
                olo_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_olo.visibility = View.GONE

                olo_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.oloSales?.pcya?.actual?.amount,
                    storeDataRangeViewSupervisorAws.oloSales?.pcya?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.oloSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.oloSales?.orderCount?.actual?.amount,
                    storeDataRangeViewSupervisorAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.oloSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                olo_order_percentage.visibility = View.GONE
                awus_olo_sales_error_oc.visibility = View.VISIBLE

            } else {
                olo_order_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_oc.visibility = View.GONE

                olo_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.oloSales?.orderCount?.actual?.amount,
                    storeDataRangeViewSupervisorAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.oloSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewSupervisorAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.oloSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                olo_average_percentage.visibility = View.GONE
                awus_olo_sales_error_at.visibility = View.VISIBLE

            } else {
                olo_average_percentage.visibility = View.VISIBLE
                awus_olo_sales_error_at.visibility = View.GONE

                olo_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.oloSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewSupervisorAws.oloSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.oloSales?.averageTicket?.actual?.value
                )
            }

            // Phone sales order count
            phone_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewSupervisorAws.phoneSales?.total?.actual?.amount,
                storeDataRangeViewSupervisorAws.phoneSales?.total?.actual?.percentage,
                storeDataRangeViewSupervisorAws.phoneSales?.total?.actual?.value
            )
            if (storeDataRangeViewSupervisorAws.phoneSales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewSupervisorAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewSupervisorAws.phoneSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        phone_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        phone_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.phoneSales?.pcya?.actual?.amount,
                    storeDataRangeViewSupervisorAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.phoneSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                phone_pcys_percentage.visibility = View.GONE
                awus_phones_sales_error_olo.visibility = View.VISIBLE

            } else {
                phone_pcys_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_olo.visibility = View.GONE

                phone_pcys_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.phoneSales?.pcya?.actual?.amount,
                    storeDataRangeViewSupervisorAws.phoneSales?.pcya?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.phoneSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewSupervisorAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.phoneSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_phone_ticket_percentage.visibility = View.GONE
                awus_phones_sales_error_at.visibility = View.VISIBLE

            } else {
                average_phone_ticket_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_at.visibility = View.GONE

                average_phone_ticket_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.phoneSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewSupervisorAws.phoneSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.phoneSales?.averageTicket?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataRangeViewSupervisorAws.phoneSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.phoneSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                phone_sales_order_percentage.visibility = View.GONE
                awus_phones_sales_error_oc.visibility = View.VISIBLE

            } else {
                phone_sales_order_percentage.visibility = View.VISIBLE
                awus_phones_sales_error_oc.visibility = View.GONE

                phone_sales_order_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.phoneSales?.orderCount?.actual?.amount,
                    storeDataRangeViewSupervisorAws.oloSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.oloSales?.orderCount?.actual?.value
                )
            }

            // walk-in sales
            walkin_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewSupervisorAws.walkinSales?.total?.actual?.amount,
                storeDataRangeViewSupervisorAws.walkinSales?.total?.actual?.percentage,
                storeDataRangeViewSupervisorAws.walkinSales?.total?.actual?.value
            )

            if (storeDataRangeViewSupervisorAws.walkinSales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewSupervisorAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewSupervisorAws.walkinSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        walkin_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        walkin_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.walkinSales?.pcya?.actual?.amount,
                    storeDataRangeViewSupervisorAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.walkinSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                walkin_pcy_value.visibility = View.GONE
                awus_walkins_sales_error_olo.visibility = View.VISIBLE

            } else {
                walkin_pcy_value.visibility = View.VISIBLE
                awus_walkins_sales_error_olo.visibility = View.GONE

                walkin_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.walkinSales?.pcya?.actual?.amount,
                    storeDataRangeViewSupervisorAws.walkinSales?.pcya?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.walkinSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataRangeViewSupervisorAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.walkinSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                walkin_order_count_value.visibility = View.GONE
                awus_walkins_sales_error_oc.visibility = View.VISIBLE

            } else {
                walkin_order_count_value.visibility = View.VISIBLE
                awus_walkins_sales_error_oc.visibility = View.GONE

                walkin_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.walkinSales?.orderCount?.actual?.amount,
                    storeDataRangeViewSupervisorAws.walkinSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.walkinSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewSupervisorAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.walkinSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                average_walkin_ticket_value.visibility = View.GONE
                awus_walkins_sales_error_at.visibility = View.VISIBLE

            } else {
                average_walkin_ticket_value.visibility = View.VISIBLE
                awus_walkins_sales_error_at.visibility = View.GONE

                average_walkin_ticket_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.walkinSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewSupervisorAws.walkinSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.walkinSales?.averageTicket?.actual?.value
                )
            }

            // delivery sales
            delivery_sales_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewSupervisorAws.deliverySales?.total?.actual?.amount,
                storeDataRangeViewSupervisorAws.deliverySales?.total?.actual?.percentage,
                storeDataRangeViewSupervisorAws.deliverySales?.total?.actual?.value
            )

            if (storeDataRangeViewSupervisorAws.deliverySales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewSupervisorAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewSupervisorAws.deliverySales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        delivery_sales_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        delivery_sales_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.deliverySales?.pcya?.actual?.amount,
                    storeDataRangeViewSupervisorAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.deliverySales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                delivery_pcy_value.visibility = View.GONE
                awus_delivery_sales_error_olo.visibility = View.VISIBLE

            } else {
                delivery_pcy_value.visibility = View.VISIBLE
                awus_delivery_sales_error_olo.visibility = View.GONE

                delivery_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.deliverySales?.pcya?.actual?.amount,
                    storeDataRangeViewSupervisorAws.deliverySales?.pcya?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.deliverySales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataRangeViewSupervisorAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.deliverySales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                delivery_order_count_value.visibility = View.GONE
                awus_delivery_sales_error_oc.visibility = View.VISIBLE

            } else {
                delivery_order_count_value.visibility = View.VISIBLE
                awus_delivery_sales_error_oc.visibility = View.GONE

                delivery_order_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.deliverySales?.orderCount?.actual?.amount,
                    storeDataRangeViewSupervisorAws.deliverySales?.orderCount?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.deliverySales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataRangeViewSupervisorAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.deliverySales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                delivery_average_percentage.visibility = View.GONE
                awus_delivery_sales_error_at.visibility = View.VISIBLE

            } else {
                delivery_average_percentage.visibility = View.VISIBLE
                awus_delivery_sales_error_at.visibility = View.GONE

                delivery_average_percentage.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.deliverySales?.averageTicket?.actual?.amount,
                    storeDataRangeViewSupervisorAws.deliverySales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.deliverySales?.averageTicket?.actual?.value
                )
            }

            // carryout sales
            carryout_average_percentage.text = Validation().checkAmountPercentageValue(
                this,
                storeDataRangeViewSupervisorAws.carryoutSales?.total?.actual?.amount,
                storeDataRangeViewSupervisorAws.carryoutSales?.total?.actual?.percentage,
                storeDataRangeViewSupervisorAws.carryoutSales?.total?.actual?.value
            )

            if (storeDataRangeViewSupervisorAws.carryoutSales?.total?.status?.toString() != null) {
                when {
                    storeDataRangeViewSupervisorAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.out_of_range
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.red))
                    }
                    storeDataRangeViewSupervisorAws.carryoutSales.total.status.toString() == resources.getString(
                        R.string.under_limit
                    ) -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.green))

                    }
                    else -> {
                        carryout_average_percentage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        carryout_average_percentage.setTextColor(getColor(R.color.text_color))

                    }
                }
            }

            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataRangeViewSupervisorAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.carryoutSales?.pcya?.actual?.value
                ).isEmpty()
            ) {
                carry_out_pcy_value.visibility = View.GONE
                awus_carryout_sales_error_olo.visibility = View.VISIBLE

            } else {
                carry_out_pcy_value.visibility = View.VISIBLE
                awus_carryout_sales_error_olo.visibility = View.GONE

                carry_out_pcy_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.carryoutSales?.pcya?.actual?.amount,
                    storeDataRangeViewSupervisorAws.carryoutSales?.pcya?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.carryoutSales?.pcya?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataRangeViewSupervisorAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.carryoutSales?.orderCount?.actual?.value
                ).isEmpty()
            ) {
                carry_outorder_count_value.visibility = View.GONE
                awus_carryout_sales_error_oc.visibility = View.VISIBLE

            } else {
                carry_outorder_count_value.visibility = View.VISIBLE
                awus_carryout_sales_error_oc.visibility = View.GONE

                carry_outorder_count_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.carryoutSales?.orderCount?.actual?.amount,
                    storeDataRangeViewSupervisorAws.carryoutSales?.orderCount?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.carryoutSales?.orderCount?.actual?.value
                )
            }
            if (Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewSupervisorAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.carryoutSales?.averageTicket?.actual?.value
                ).isEmpty()
            ) {
                carry_average_tickets_value.visibility = View.GONE
                awus_carryout_sales_error_at.visibility = View.VISIBLE

            } else {
                carry_average_tickets_value.visibility = View.VISIBLE
                awus_carryout_sales_error_at.visibility = View.GONE

                carry_average_tickets_value.text = Validation().checkAmountPercentageValue(
                    this,
                    storeDataRangeViewSupervisorAws.carryoutSales?.averageTicket?.actual?.amount,
                    storeDataRangeViewSupervisorAws.carryoutSales?.averageTicket?.actual?.percentage,
                    storeDataRangeViewSupervisorAws.carryoutSales?.averageTicket?.actual?.value
                )
            }

        } catch (e: Exception) {
            Logger.error(e.message.toString(),"AWUS Overview Period Range KPI")
        }
    }

    override fun onBackPressed() {
        Logger.info("Back-pressed","AWUS KPI Screen")
        finish()

    }

    private fun showAWUSNarrativeData(salesNarrative: String?) {
        if (!salesNarrative.isNullOrEmpty()) {
            var awusNarrative = salesNarrative
            awusNarrative = awusNarrative.replace("</p>", "<br><br>")
            awus_narrative_value.text = Html.fromHtml(awusNarrative, Html.FROM_HTML_MODE_COMPACT)
        } else {
            awus_narrative_value.visibility = View.INVISIBLE
        }
    }

    private fun callAWUSOverviewNullApi() {
        val formattedStartDateValue: String
        val formattedEndDateValue: String

        val startDateValue1 = StorePrefData.startDateValue
        val endDateValue1 = StorePrefData.endDateValue

        if (StorePrefData.isCalendarSelected) {
            formattedStartDateValue = startDateValue1
            formattedEndDateValue = endDateValue1
        } else {
            formattedStartDateValue = startDateValue1
            formattedEndDateValue = endDateValue1
        }
        val progressDialogCEOPeriodKpiRange = CustomProgressDialog(this@AWUSKpiActivity)
        progressDialogCEOPeriodKpiRange.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodeList = dbHelper.getAllSelectedAreaList(true)
            val stateCodeList = dbHelper.getAllSelectedStoreListState(true)
            val supervisorNumberList = dbHelper.getAllSelectedStoreListSupervisor(true)
            val storeNumberList = dbHelper.getAllSelectedStoreList(true)

            val responseMissingDataAWUS = try {
                apolloClient(this@AWUSKpiActivity).query(
                    MissingDataQuery(
                            areaCodeList.toInput(),
                            stateCodeList.toInput(),
                            supervisorNumberList.toInput(),
                            storeNumberList.toInput(),
                            EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType).toInput(),
                            formattedStartDateValue.toInput(),
                            formattedEndDateValue.toInput(),
                    )
                ).await()

            } catch (e: ApolloException) {
                Logger.error("Missing Data ${e.message.toString()}","AWUS Overview KPI")
                progressDialogCEOPeriodKpiRange.dismissProgressDialog()
                return@launchWhenResumed
            }
            if (responseMissingDataAWUS.data?.missingData != null) {
                progressDialogCEOPeriodKpiRange.dismissProgressDialog()
                awus_kpi_error_layout.visibility = View.VISIBLE
                awus_kpi_error_layout.header_data_title.text =
                    responseMissingDataAWUS.data?.missingData!!.header
                awus_kpi_error_layout.header_data_description.text =
                    responseMissingDataAWUS.data?.missingData!!.message
            } else {
                progressDialogCEOPeriodKpiRange.dismissProgressDialog()
                awus_kpi_error_layout.visibility = View.GONE
            }
        }
    }

}