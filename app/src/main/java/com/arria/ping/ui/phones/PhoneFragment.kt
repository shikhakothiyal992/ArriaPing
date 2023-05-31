package com.arria.ping.ui.phones

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.arria.ping.R
import com.arria.ping.apiclient.ApiClientAuth
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.apollo.apolloClient
import com.arria.ping.log.Logger
import com.arria.ping.log.data.QueryData
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.kpi.*
import com.arria.ping.model.LoginFail
import com.arria.ping.model.SendRefreshRequest
import com.arria.ping.model.successLogin.LoginSuccess
import com.arria.ping.ui.filter.FilterActivity
import com.arria.ping.util.*
import kotlinx.android.synthetic.main.bonus_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.ceo_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.common_header_phone.view.*
import kotlinx.android.synthetic.main.common_header_phone.view.filter_icon
import kotlinx.android.synthetic.main.common_header_phone.view.sales_text_common_header
import kotlinx.android.synthetic.main.common_header_phone.view.store_header
import kotlinx.android.synthetic.main.common_header_phone.view.total_sales_common_header
import kotlinx.android.synthetic.main.fragment_phone.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class PhoneFragment(private val periodRange: String) : Fragment() {
    lateinit var dbHelper: DatabaseHelperImpl
    private var formattedStartDateValue = ""
    private var formattedEndDateValue = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        return inflater.inflate(R.layout.fragment_phone, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialise()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialise() {
        common_header.filter_icon.visibility = View.VISIBLE
        dbHelper = DatabaseHelperImpl(DatabaseBuilder.getInstance(requireActivity()))
        hitApiAfterRefreshToken()
        common_header.filter_icon.setOnClickListener {
            openFilter()
        }
        common_header.filter_parent_linear_phone.setOnClickListener {
            openFilter()
        }
    }

    // CEO
    @RequiresApi(Build.VERSION_CODES.O)
    private fun callPhoneCEOAPIToday() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {


            Logger.info(PhoneCEOTodayQuery.OPERATION_NAME.name(),"PHONE")

            val response = try {
                apolloClient(requireContext()).query(PhoneCEOTodayQuery()).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"PHONE")
                refreshToken()
                return@launchWhenResumed
            }
            if (response.data?.ceo != null) {
                progressDialog.dismissProgressDialog()
                setDataCEOToday(response.data?.ceo?.phones)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callPhoneCEOAPIYesterday() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {


            Logger.info(PhoneCEOYesterdayQuery.OPERATION_NAME.name(),"PHONE")

            val response = try {
                apolloClient(requireContext()).query(PhoneCEOYesterdayQuery()).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"PHONE")
                refreshToken()
                return@launchWhenResumed
            }
            if (response.data?.ceo != null) {
                progressDialog.dismissProgressDialog()
                setDataCEOYesterday(response.data?.ceo?.phones)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callPhoneCEOAPIPeriod() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val storeNumber = dbHelper.getAllSelectedStoreList(true)
            val areaCode = dbHelper.getAllSelectedAreaList(true)
            val stateCode = dbHelper.getAllSelectedStoreListState(true)
            if (StorePrefData.isCalendarSelected) {
                formattedStartDateValue = getFormattedDate(StorePrefData.startDateValue)
                formattedEndDateValue = getFormattedDate(StorePrefData.endDateValue)
            } else {
                formattedStartDateValue = StorePrefData.startDateValue
                formattedEndDateValue = StorePrefData.endDateValue
            }


            Logger.info(
                PhoneCEOPeriodRangeQuery.OPERATION_NAME.name(), "PHONE",
                mapQueryFilters(
                    QueryData(
                        areaCode,
                        stateCode,
                        Collections.emptyList(),
                        storeNumber,
                        formattedEndDateValue,
                        formattedStartDateValue,
                        "",
                        PhoneCEOPeriodRangeQuery.QUERY_DOCUMENT
                    )
                )
            )

            val response = try {
                apolloClient(requireContext()).query(PhoneCEOPeriodRangeQuery(areaCode.toInput(),
                    stateCode.toInput(),
                    storeNumber.toInput(),
                    formattedStartDateValue,
                    formattedEndDateValue)).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"PHONE")
                refreshToken()
                return@launchWhenResumed
            }
            if (response.data?.ceo != null) {
                progressDialog.dismissProgressDialog()
                setDataCEOPeriodRange(response.data?.ceo?.phones)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataCEOToday(phones: PhoneCEOTodayQuery.Phones?) {
        val ceoDetailData = phones?.supervisors?.stores?.today
        common_header.total_sales_common_header.text = Validation().ignoreZeroAfterDecimal(ceoDetailData?.aot?.actual?.percentage)
        common_header.sales_text_common_header.text = getString(R.string.aot_text_normal)

        setHeaderPhone()
        // calls
        total_calls_display.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.displayName != null) ceoDetailData.phoneCallAnalysis.totalCalls.displayName else getString(
                R.string.total_call)
        total_calls_actual.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value != null) Validation().dollarFormatting(
                ceoDetailData.phoneCallAnalysis.totalCalls.actual.value) else ""

        if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value?.isNaN() == false && ceoDetailData.phoneCallAnalysis.totalCalls.status?.toString() != null) {
            when {
                ceoDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // total order
        total_order_display.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.displayName != null) ceoDetailData.phoneCallAnalysis.totalCalls.totalOrders.displayName else getString(
                R.string.total_orders)
        total_order_goal.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.goal?.value != null) Validation().dollarFormatting(
                ceoDetailData.phoneCallAnalysis.totalCalls.totalOrders.goal.value) else ""
        total_order_varince.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.variance?.value != null) Validation().dollarFormatting(
                ceoDetailData.phoneCallAnalysis.totalCalls.totalOrders.variance.value) else ""
        total_order_actual.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value != null) Validation().dollarFormatting(
                ceoDetailData.phoneCallAnalysis.totalCalls.totalOrders.actual.value) else ""

        if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value?.isNaN() == false && ceoDetailData.phoneCallAnalysis.totalCalls.totalOrders.status?.toString() != null) {
            when {
                ceoDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // % order
        percentage_order_display.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.displayName != null) ceoDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.displayName else getString(
                R.string.percentage_order)
        percentage_order_goal.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.goal?.percentage != null) Validation().ignoreZeroAfterDecimal(
                ceoDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.goal.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_varince.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.variance?.percentage != null) Validation().ignoreZeroAfterDecimal(
                ceoDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.variance.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_actual.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.percentage != null) Validation().ignoreZeroAfterDecimal(
                ceoDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.actual.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""

        if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.value?.isNaN() == false && ceoDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status?.toString() != null) {
            when {
                ceoDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // Average cost
        average_cost_display.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.displayName != null) ceoDetailData.phoneCallAnalysis.totalCalls.averageCost.displayName else getString(
                R.string.average_cost)
        average_cost_goal.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.goal?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoDetailData.phoneCallAnalysis.totalCalls.averageCost.goal.amount)) else ""
        average_cost_varince.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.variance?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoDetailData.phoneCallAnalysis.totalCalls.averageCost.variance.amount)) else ""

        average_cost_actual.text =
            if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoDetailData.phoneCallAnalysis.totalCalls.averageCost.actual.amount)) else ""


        if (ceoDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.value?.isNaN() == false && ceoDetailData.phoneCallAnalysis.totalCalls.averageCost.status != null) {
            when {
                ceoDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        potential_awus_display.text =
            if (ceoDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.displayName != null) ceoDetailData.phoneCallAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        potential_awus_actual.text =
            if (ceoDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount != null) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoDetailData.phoneCallAnalysis.potentialAWUSGrowth.actual.amount)) else ""

        if (ceoDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && ceoDetailData.phoneCallAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            when {
                ceoDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        existing_customer_display.text =
            if (ceoDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) ceoDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        existing_customer_actual.text =
            if (ceoDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null)getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount) )else ""

        new_customer_display.text =
            if (ceoDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.displayName != null) ceoDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.displayName else getString(
                R.string.unknown_caller)
        new_customer_actual.text =
            if (ceoDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.actual.amount)) else ""

        // hang ups
        total_hungs_up_display.text =
            if (ceoDetailData?.hangUpAnalysis?.totalHangUps?.displayName != null) ceoDetailData.hangUpAnalysis.totalHangUps.displayName else getString(
                R.string.potential_awus_growth)
        total_hungs_up_actual.text =
            Validation().dollarFormatting(ceoDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value)

        if (ceoDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value?.isNaN() == false && ceoDetailData.hangUpAnalysis.totalHangUps.status?.toString() != null) {
            when {
                ceoDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        before_queue_display.text =
            if (ceoDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.displayName != null) ceoDetailData.hangUpAnalysis.totalHangUps.beforeQueue.displayName else getString(
                R.string.before_queue)
        in_queue_display.text =
            if (ceoDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.displayName != null) ceoDetailData.hangUpAnalysis.totalHangUps.inQueue.displayName else getString(
                R.string.in_queue)
        customer_in_queue_display.text =
            if (ceoDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.displayName != null) ceoDetailData.hangUpAnalysis.totalHangUps.customersInQueue.displayName else getString(
                R.string.customer_in_queue)
        unknown_in_queue_display.text =
            if (ceoDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.displayName != null) ceoDetailData.hangUpAnalysis.totalHangUps.unknownsInQueue.displayName else getString(
                R.string.unknwon_queue)

        before_queuer_actual.text =
            Validation().dollarFormatting(ceoDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.actual?.value)
        in_queue_actual.text =
            Validation().dollarFormatting(ceoDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.actual?.value)
        customer_in_queue_actual.text =
            Validation().dollarFormatting(ceoDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.actual?.value)
        unknown_in_queue_actual.text =
            Validation().dollarFormatting(ceoDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.actual?.value)

        hangs_ups_potential_awus_display.text =
            if (ceoDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.displayName != null) ceoDetailData.hangUpAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        if (ceoDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && ceoDetailData.hangUpAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            hangs_ups_potential_awus_actual.text =
                getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(ceoDetailData.hangUpAnalysis.potentialAWUSGrowth.actual.amount))

            when {
                ceoDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        hangs_ups_existing_customer_display.text =
            if (ceoDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) ceoDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        hangs_ups_existing_customer_actual.text =
            if (ceoDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount)) else ""

        hangs_ups_new_customer_display.text =
            if (ceoDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.displayName != null) ceoDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.displayName else getString(
                R.string.unknown_caller)
        hangs_ups_new_customer_actual.text =
            if (ceoDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.actual.amount)) else ""
        // potential bonus growth

        potential_bonus_growth_display.text =
            if (ceoDetailData?.hangUpAnalysis?.potentialBonusGrowth?.displayName != null) ceoDetailData.hangUpAnalysis.potentialBonusGrowth.displayName else getString(
                R.string.potential_bonus_growth)

        if (ceoDetailData?.hangUpAnalysis?.potentialBonusGrowth?.actual?.amount?.isNaN() == false && ceoDetailData.hangUpAnalysis.potentialBonusGrowth.status?.toString() != null) {
            potential_bonus_growth_actual.text =
                if (!ceoDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount.isNaN()) getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        ceoDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount)) else ""

            when {
                ceoDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        phone_narrative.text =if (ceoDetailData?.summary != null) Html.fromHtml(ceoDetailData.summary, Html.FROM_HTML_MODE_COMPACT) else ""

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataCEOYesterday(phones: PhoneCEOYesterdayQuery.Phones?) {
        val ceoYesterdayDetailData = phones?.supervisors?.stores?.yesterday
        common_header.total_sales_common_header.text = Validation().ignoreZeroAfterDecimal(ceoYesterdayDetailData?.aot?.actual?.percentage)
        common_header.sales_text_common_header.text = getString(R.string.aot_text_normal)

        setHeaderPhone()
        // calls
        total_calls_display.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.displayName != null) ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.displayName else getString(
                R.string.total_call)
        total_calls_actual.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value != null) Validation().dollarFormatting(
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.actual.value) else ""

        if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value?.isNaN() == false && ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.status?.toString() != null) {
            when {
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // total order
        total_order_display.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.displayName != null) ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.displayName else getString(
                R.string.total_orders)
        total_order_goal.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.goal?.value != null) Validation().dollarFormatting(
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.goal.value) else ""
        total_order_varince.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.variance?.value != null) Validation().dollarFormatting(
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.variance.value) else ""
        total_order_actual.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value != null) Validation().dollarFormatting(
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.actual.value) else ""

        if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value?.isNaN() == false && ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status?.toString() != null) {
            when {
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // % order
        percentage_order_display.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.displayName != null) ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.displayName else getString(
                R.string.percentage_order)
        percentage_order_goal.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.goal?.percentage != null) Validation().ignoreZeroAfterDecimal(
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.goal.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_varince.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.variance?.percentage != null) Validation().ignoreZeroAfterDecimal(
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.variance.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_actual.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.percentage != null) Validation().ignoreZeroAfterDecimal(
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.actual.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""

        if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.value?.isNaN() == false && ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status?.toString() != null) {
            when {
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // Average cost
        average_cost_display.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.displayName != null) ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.displayName else getString(
                R.string.average_cost)
        average_cost_goal.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.goal?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.goal.amount)) else ""
        average_cost_varince.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.variance?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.variance.amount)) else ""

        average_cost_actual.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.actual.amount)) else ""


        if (ceoYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.value?.isNaN() == false && ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status != null) {
            when {
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        potential_awus_display.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.displayName != null) ceoYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        potential_awus_actual.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount != null) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.actual.amount)) else ""

        if (ceoYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && ceoYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            when {
                ceoYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        existing_customer_display.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) ceoYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        existing_customer_actual.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null)getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount) )else ""

        new_customer_display.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.displayName != null) ceoYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.displayName else getString(
                R.string.unknown_caller)
        new_customer_actual.text =
            if (ceoYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.actual.amount)) else ""
    // hang ups
        total_hungs_up_display.text =
            if (ceoYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.displayName != null) ceoYesterdayDetailData.hangUpAnalysis.totalHangUps.displayName else getString(
                R.string.potential_awus_growth)
        total_hungs_up_actual.text =
            Validation().dollarFormatting(ceoYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value)

        if (ceoYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value?.isNaN() == false && ceoYesterdayDetailData.hangUpAnalysis.totalHangUps.status?.toString() != null) {
            when {
                ceoYesterdayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoYesterdayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoYesterdayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        before_queue_display.text =
            if (ceoYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.displayName != null) ceoYesterdayDetailData.hangUpAnalysis.totalHangUps.beforeQueue.displayName else getString(
                R.string.before_queue)
        in_queue_display.text =
            if (ceoYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.displayName != null) ceoYesterdayDetailData.hangUpAnalysis.totalHangUps.inQueue.displayName else getString(
                R.string.in_queue)
        customer_in_queue_display.text =
            if (ceoYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.displayName != null) ceoYesterdayDetailData.hangUpAnalysis.totalHangUps.customersInQueue.displayName else getString(
                R.string.customer_in_queue)
        unknown_in_queue_display.text =
            if (ceoYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.displayName != null) ceoYesterdayDetailData.hangUpAnalysis.totalHangUps.unknownsInQueue.displayName else getString(
                R.string.unknwon_queue)

        before_queuer_actual.text =
            Validation().dollarFormatting(ceoYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.actual?.value)
        in_queue_actual.text =
            Validation().dollarFormatting(ceoYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.actual?.value)
        customer_in_queue_actual.text =
            Validation().dollarFormatting(ceoYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.actual?.value)
        unknown_in_queue_actual.text =
            Validation().dollarFormatting(ceoYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.actual?.value)

        hangs_ups_potential_awus_display.text =
            if (ceoYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.displayName != null) ceoYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        if (ceoYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && ceoYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            hangs_ups_potential_awus_actual.text =
                getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(ceoYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.actual.amount))

            when {
                ceoYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        hangs_ups_existing_customer_display.text =
            if (ceoYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) ceoYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        hangs_ups_existing_customer_actual.text =
            if (ceoYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount)) else ""

        hangs_ups_new_customer_display.text =
            if (ceoYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.displayName != null) ceoYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.displayName else getString(
                R.string.unknown_caller)
        hangs_ups_new_customer_actual.text =
            if (ceoYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.actual.amount)) else ""
        // potential bonus growth

        potential_bonus_growth_display.text =
            if (ceoYesterdayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.displayName != null) ceoYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.displayName else getString(
                R.string.potential_bonus_growth)

        if (ceoYesterdayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.actual?.amount?.isNaN() == false && ceoYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status?.toString() != null) {
            potential_bonus_growth_actual.text =
                if (!ceoYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount.isNaN()) getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        ceoYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount)) else ""

            when {
                ceoYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        phone_narrative.text =if (ceoYesterdayDetailData?.summary != null) Html.fromHtml(ceoYesterdayDetailData.summary, Html.FROM_HTML_MODE_COMPACT) else ""

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataCEOPeriodRange(phones: PhoneCEOPeriodRangeQuery.Phones?) {
        val ceoPeriodRangeDetailData = phones?.supervisors?.stores?.period
        common_header.total_sales_common_header.text = Validation().ignoreZeroAfterDecimal(ceoPeriodRangeDetailData?.aot?.actual?.percentage)
        common_header.sales_text_common_header.text = getString(R.string.aot_text_normal)
        setHeaderPhone()
        // calls
        total_calls_display.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.displayName != null) ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.displayName else getString(
                R.string.total_call)
        total_calls_actual.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value != null) Validation().dollarFormatting(
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.actual.value) else ""

        if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value?.isNaN() == false && ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status?.toString() != null) {
            when {
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // total order
        total_order_display.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.displayName != null) ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.displayName else getString(
                R.string.total_orders)
        total_order_goal.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.goal?.value != null) Validation().dollarFormatting(
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.goal.value) else ""
        total_order_varince.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.variance?.value != null) Validation().dollarFormatting(
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.variance.value) else ""
        total_order_actual.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value != null) Validation().dollarFormatting(
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.actual.value) else ""

        if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value?.isNaN() == false && ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status?.toString() != null) {
            when {
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // % order
        percentage_order_display.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.displayName != null) ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.displayName else getString(
                R.string.percentage_order)
        percentage_order_goal.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.goal?.percentage != null) Validation().ignoreZeroAfterDecimal(
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.goal.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_varince.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.variance?.percentage != null) Validation().ignoreZeroAfterDecimal(
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.variance.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_actual.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.percentage != null) Validation().ignoreZeroAfterDecimal(
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.actual.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""

        if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.value?.isNaN() == false && ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status?.toString() != null) {
            when {
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // Average cost
        average_cost_display.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.displayName != null) ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.displayName else getString(
                R.string.average_cost)
        average_cost_goal.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.goal?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.goal.amount)) else ""
        average_cost_varince.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.variance?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.variance.amount)) else ""

        average_cost_actual.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.actual.amount)) else ""


        if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.value?.isNaN() == false && ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status != null) {
            when {
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        potential_awus_display.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.displayName != null) ceoPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        potential_awus_actual.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount != null) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.actual.amount)) else ""

        if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && ceoPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            when {
                ceoPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        existing_customer_display.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) ceoPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        existing_customer_actual.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null)getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount) )else ""

        new_customer_display.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.displayName != null) ceoPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.displayName else getString(
                R.string.unknown_caller)
        new_customer_actual.text =
            if (ceoPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.actual.amount)) else ""

        // hang ups
        total_hungs_up_display.text =
            if (ceoPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.displayName != null) ceoPeriodRangeDetailData.hangUpAnalysis.totalHangUps.displayName else getString(
                R.string.potential_awus_growth)
        total_hungs_up_actual.text =
            Validation().dollarFormatting(ceoPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value)

        if (ceoPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value?.isNaN() == false && ceoPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status?.toString() != null) {
            when {
                ceoPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        before_queue_display.text =
            if (ceoPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.displayName != null) ceoPeriodRangeDetailData.hangUpAnalysis.totalHangUps.beforeQueue.displayName else getString(
                R.string.before_queue)
        in_queue_display.text =
            if (ceoPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.displayName != null) ceoPeriodRangeDetailData.hangUpAnalysis.totalHangUps.inQueue.displayName else getString(
                R.string.in_queue)
        customer_in_queue_display.text =
            if (ceoPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.displayName != null) ceoPeriodRangeDetailData.hangUpAnalysis.totalHangUps.customersInQueue.displayName else getString(
                R.string.customer_in_queue)
        unknown_in_queue_display.text =
            if (ceoPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.displayName != null) ceoPeriodRangeDetailData.hangUpAnalysis.totalHangUps.unknownsInQueue.displayName else getString(
                R.string.unknwon_queue)

        before_queuer_actual.text =
            Validation().dollarFormatting(ceoPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.actual?.value)
        in_queue_actual.text =
            Validation().dollarFormatting(ceoPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.actual?.value)
        customer_in_queue_actual.text =
            Validation().dollarFormatting(ceoPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.actual?.value)
        unknown_in_queue_actual.text =
            Validation().dollarFormatting(ceoPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.actual?.value)

        hangs_ups_potential_awus_display.text =
            if (ceoPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.displayName != null) ceoPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        if (ceoPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && ceoPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            hangs_ups_potential_awus_actual.text =
                getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(ceoPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.actual.amount))

            when {
                ceoPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        hangs_ups_existing_customer_display.text =
            if (ceoPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) ceoPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        hangs_ups_existing_customer_actual.text =
            if (ceoPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount)) else ""

        hangs_ups_new_customer_display.text =
            if (ceoPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.displayName != null) ceoPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.displayName else getString(
                R.string.unknown_caller)
        hangs_ups_new_customer_actual.text =
            if (ceoPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    ceoPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.actual.amount)) else ""
        // potential bonus growth

        potential_bonus_growth_display.text =
            if (ceoPeriodRangeDetailData?.hangUpAnalysis?.potentialBonusGrowth?.displayName != null) ceoPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.displayName else getString(
                R.string.potential_bonus_growth)

        if (ceoPeriodRangeDetailData?.hangUpAnalysis?.potentialBonusGrowth?.actual?.amount?.isNaN() == false && ceoPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status?.toString() != null) {
            potential_bonus_growth_actual.text =
                if (!ceoPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount.isNaN()) getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        ceoPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount)) else ""

            when {
                ceoPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                ceoPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                ceoPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        phone_narrative.text =if (ceoPeriodRangeDetailData?.summary != null) Html.fromHtml(ceoPeriodRangeDetailData.summary, Html.FROM_HTML_MODE_COMPACT) else ""

    }

    // DO
    @RequiresApi(Build.VERSION_CODES.O)
    private fun callPhoneDOAPIToday() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {

            Logger.info(PhoneDOTodayQuery.OPERATION_NAME.name(),"PHONE")

            val response = try {
                apolloClient(requireContext()).query(PhoneDOTodayQuery()).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"PHONES")
                refreshToken()
                return@launchWhenResumed
            }
            if (response.data?.do_ != null) {
                progressDialog.dismissProgressDialog()
               setDataDOToday(response.data?.do_?.phones)

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callPhoneDOAPIYesterday() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {


            Logger.info(PhoneDOYesterdayQuery.OPERATION_NAME.name(),"PHONE")

            val response = try {
                apolloClient(requireContext()).query(PhoneDOYesterdayQuery()).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"PHONE")
                refreshToken()
                return@launchWhenResumed
            }
            if (response.data?.do_ != null) {
                progressDialog.dismissProgressDialog()
                setDataDOYesterday(response.data?.do_?.phones)

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callPhoneDOAPIPeriod() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val storeNumber = dbHelper.getAllSelectedStoreList(true)
            val areaCode = dbHelper.getAllSelectedAreaList(true)
            val stateCode = dbHelper.getAllSelectedStoreListState(true)
            if (StorePrefData.isCalendarSelected) {
                formattedStartDateValue = getFormattedDate(StorePrefData.startDateValue)
                formattedEndDateValue = getFormattedDate(StorePrefData.endDateValue)
            } else {
                formattedStartDateValue = StorePrefData.startDateValue
                formattedEndDateValue = StorePrefData.endDateValue
            }


            Logger.info(
                PhoneDOPeriodRangeQuery.OPERATION_NAME.name(), "PHONE",
                mapQueryFilters(
                    QueryData(
                        areaCode,
                        stateCode,
                        Collections.emptyList(),
                        storeNumber,
                        formattedEndDateValue,
                        formattedStartDateValue,
                        "",
                        PhoneDOPeriodRangeQuery.QUERY_DOCUMENT
                    )
                )
            )

            val response = try {
                apolloClient(requireContext()).query(PhoneDOPeriodRangeQuery(areaCode.toInput(),
                    stateCode.toInput(),
                    storeNumber.toInput(),
                    formattedStartDateValue,
                    formattedEndDateValue)).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"PHONE")
                refreshToken()
                return@launchWhenResumed
            }
            if (response.data?.do_ != null) {
                progressDialog.dismissProgressDialog()
                setDataDOPeriodRange(response.data?.do_?.phones)
            }
        }
    }
    // do

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataDOToday(phones: PhoneDOTodayQuery.Phones?) {
        val doTodayDetailData = phones?.supervisors?.stores?.today
        common_header.total_sales_common_header.text = Validation().ignoreZeroAfterDecimal(doTodayDetailData?.aot?.actual?.percentage)
        common_header.sales_text_common_header.text = getString(R.string.aot_text_normal)

        setHeaderPhone()
        // calls
        total_calls_display.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.displayName != null) doTodayDetailData.phoneCallAnalysis.totalCalls.displayName else getString(
                R.string.total_call)
        total_calls_actual.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value != null) Validation().dollarFormatting(
                doTodayDetailData.phoneCallAnalysis.totalCalls.actual.value) else ""

        if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value?.isNaN() == false && doTodayDetailData.phoneCallAnalysis.totalCalls.status?.toString() != null) {
            when {
                doTodayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doTodayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doTodayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // total order
        total_order_display.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.displayName != null) doTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.displayName else getString(
                R.string.total_orders)
        total_order_goal.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.goal?.value != null) Validation().dollarFormatting(
                doTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.goal.value) else ""
        total_order_varince.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.variance?.value != null) Validation().dollarFormatting(
                doTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.variance.value) else ""
        total_order_actual.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value != null) Validation().dollarFormatting(
                doTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.actual.value) else ""

        if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value?.isNaN() == false && doTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status?.toString() != null) {
            when {
                doTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // % order
        percentage_order_display.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.displayName != null) doTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.displayName else getString(
                R.string.percentage_order)
        percentage_order_goal.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.goal?.percentage != null) Validation().ignoreZeroAfterDecimal(
                doTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.goal.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_varince.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.variance?.percentage != null) Validation().ignoreZeroAfterDecimal(
                doTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.variance.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_actual.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.percentage != null) Validation().ignoreZeroAfterDecimal(
                doTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.actual.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""

        if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.value?.isNaN() == false && doTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status?.toString() != null) {
            when {
                doTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // Average cost
        average_cost_display.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.displayName != null) doTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.displayName else getString(
                R.string.average_cost)
        average_cost_goal.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.goal?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.goal.amount)) else ""
        average_cost_varince.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.variance?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.variance.amount)) else ""

        average_cost_actual.text =
            if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.actual.amount)) else ""


        if (doTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.value?.isNaN() == false && doTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.status != null) {
            when {
                doTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        potential_awus_display.text =
            if (doTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.displayName != null) doTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        potential_awus_actual.text =
            if (doTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount != null) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.actual.amount)) else ""

        if (doTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && doTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            when {
                doTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        existing_customer_display.text =
            if (doTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) doTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        existing_customer_actual.text =
            if (doTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null)getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount) )else ""

        new_customer_display.text =
            if (doTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.displayName != null) doTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.displayName else getString(
                R.string.unknown_caller)
        new_customer_actual.text =
            if (doTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.actual.amount)) else ""

        // hang ups
        total_hungs_up_display.text =
            if (doTodayDetailData?.hangUpAnalysis?.totalHangUps?.displayName != null) doTodayDetailData.hangUpAnalysis.totalHangUps.displayName else getString(
                R.string.potential_awus_growth)
        total_hungs_up_actual.text =
            Validation().dollarFormatting(doTodayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value)

        if (doTodayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value?.isNaN() == false && doTodayDetailData.hangUpAnalysis.totalHangUps.status?.toString() != null) {
            when {
                doTodayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doTodayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doTodayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        before_queue_display.text =
            if (doTodayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.displayName != null) doTodayDetailData.hangUpAnalysis.totalHangUps.beforeQueue.displayName else getString(
                R.string.before_queue)
        in_queue_display.text =
            if (doTodayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.displayName != null) doTodayDetailData.hangUpAnalysis.totalHangUps.inQueue.displayName else getString(
                R.string.in_queue)
        customer_in_queue_display.text =
            if (doTodayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.displayName != null) doTodayDetailData.hangUpAnalysis.totalHangUps.customersInQueue.displayName else getString(
                R.string.customer_in_queue)
        unknown_in_queue_display.text =
            if (doTodayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.displayName != null) doTodayDetailData.hangUpAnalysis.totalHangUps.unknownsInQueue.displayName else getString(
                R.string.unknwon_queue)

        before_queuer_actual.text =
            Validation().dollarFormatting(doTodayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.actual?.value)
        in_queue_actual.text =
            Validation().dollarFormatting(doTodayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.actual?.value)
        customer_in_queue_actual.text =
            Validation().dollarFormatting(doTodayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.actual?.value)
        unknown_in_queue_actual.text =
            Validation().dollarFormatting(doTodayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.actual?.value)

        hangs_ups_potential_awus_display.text =
            if (doTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.displayName != null) doTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        if (doTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && doTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            hangs_ups_potential_awus_actual.text =
                getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(doTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.actual.amount))

            when {
                doTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        hangs_ups_existing_customer_display.text =
            if (doTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) doTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        hangs_ups_existing_customer_actual.text =
            if (doTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount)) else ""

        hangs_ups_new_customer_display.text =
            if (doTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.displayName != null) doTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.displayName else getString(
                R.string.unknown_caller)
        hangs_ups_new_customer_actual.text =
            if (doTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.actual.amount)) else ""
        // potential bonus growth

        potential_bonus_growth_display.text =
            if (doTodayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.displayName != null) doTodayDetailData.hangUpAnalysis.potentialBonusGrowth.displayName else getString(
                R.string.potential_bonus_growth)

        if (doTodayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.actual?.amount?.isNaN() == false && doTodayDetailData.hangUpAnalysis.potentialBonusGrowth.status?.toString() != null) {
            potential_bonus_growth_actual.text =
                if (!doTodayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount.isNaN()) getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        doTodayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount)) else ""

            when {
                doTodayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doTodayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doTodayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        phone_narrative.text =if (doTodayDetailData?.summary != null) Html.fromHtml(doTodayDetailData.summary, Html.FROM_HTML_MODE_COMPACT) else ""

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataDOYesterday(phones: PhoneDOYesterdayQuery.Phones?) {
        val doYesterdayDetailData = phones?.supervisors?.stores?.yesterday
        common_header.total_sales_common_header.text = Validation().ignoreZeroAfterDecimal(doYesterdayDetailData?.aot?.actual?.percentage)
        common_header.sales_text_common_header.text = getString(R.string.aot_text_normal)

        setHeaderPhone()
        // calls
        total_calls_display.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.displayName != null) doYesterdayDetailData.phoneCallAnalysis.totalCalls.displayName else getString(
                R.string.total_call)
        total_calls_actual.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value != null) Validation().dollarFormatting(
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.actual.value) else ""

        if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value?.isNaN() == false && doYesterdayDetailData.phoneCallAnalysis.totalCalls.status?.toString() != null) {
            when {
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // total order
        total_order_display.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.displayName != null) doYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.displayName else getString(
                R.string.total_orders)
        total_order_goal.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.goal?.value != null) Validation().dollarFormatting(
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.goal.value) else ""
        total_order_varince.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.variance?.value != null) Validation().dollarFormatting(
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.variance.value) else ""
        total_order_actual.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value != null) Validation().dollarFormatting(
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.actual.value) else ""

        if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value?.isNaN() == false && doYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status?.toString() != null) {
            when {
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // % order
        percentage_order_display.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.displayName != null) doYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.displayName else getString(
                R.string.percentage_order)
        percentage_order_goal.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.goal?.percentage != null) Validation().ignoreZeroAfterDecimal(
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.goal.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_varince.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.variance?.percentage != null) Validation().ignoreZeroAfterDecimal(
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.variance.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_actual.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.percentage != null) Validation().ignoreZeroAfterDecimal(
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.actual.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""

        if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.value?.isNaN() == false && doYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status?.toString() != null) {
            when {
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // Average cost
        average_cost_display.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.displayName != null) doYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.displayName else getString(
                R.string.average_cost)
        average_cost_goal.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.goal?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.goal.amount)) else ""
        average_cost_varince.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.variance?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.variance.amount)) else ""

        average_cost_actual.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.actual.amount)) else ""


        if (doYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.value?.isNaN() == false && doYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status != null) {
            when {
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        potential_awus_display.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.displayName != null) doYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        potential_awus_actual.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount != null) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.actual.amount)) else ""

        if (doYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && doYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            when {
                doYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        existing_customer_display.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) doYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        existing_customer_actual.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null)getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount) )else ""

        new_customer_display.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.displayName != null) doYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.displayName else getString(
                R.string.unknown_caller)
        new_customer_actual.text =
            if (doYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.actual.amount)) else ""

        // hang ups
        total_hungs_up_display.text =
            if (doYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.displayName != null) doYesterdayDetailData.hangUpAnalysis.totalHangUps.displayName else getString(
                R.string.potential_awus_growth)
        total_hungs_up_actual.text =
            Validation().dollarFormatting(doYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value)

        if (doYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value?.isNaN() == false && doYesterdayDetailData.hangUpAnalysis.totalHangUps.status?.toString() != null) {
            when {
                doYesterdayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doYesterdayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doYesterdayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        before_queue_display.text =
            if (doYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.displayName != null) doYesterdayDetailData.hangUpAnalysis.totalHangUps.beforeQueue.displayName else getString(
                R.string.before_queue)
        in_queue_display.text =
            if (doYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.displayName != null) doYesterdayDetailData.hangUpAnalysis.totalHangUps.inQueue.displayName else getString(
                R.string.in_queue)
        customer_in_queue_display.text =
            if (doYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.displayName != null) doYesterdayDetailData.hangUpAnalysis.totalHangUps.customersInQueue.displayName else getString(
                R.string.customer_in_queue)
        unknown_in_queue_display.text =
            if (doYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.displayName != null) doYesterdayDetailData.hangUpAnalysis.totalHangUps.unknownsInQueue.displayName else getString(
                R.string.unknwon_queue)

        before_queuer_actual.text =
            Validation().dollarFormatting(doYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.actual?.value)
        in_queue_actual.text =
            Validation().dollarFormatting(doYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.actual?.value)
        customer_in_queue_actual.text =
            Validation().dollarFormatting(doYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.actual?.value)
        unknown_in_queue_actual.text =
            Validation().dollarFormatting(doYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.actual?.value)

        hangs_ups_potential_awus_display.text =
            if (doYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.displayName != null) doYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        if (doYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && doYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            hangs_ups_potential_awus_actual.text =
                getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(doYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.actual.amount))

            when {
                doYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        hangs_ups_existing_customer_display.text =
            if (doYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) doYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        hangs_ups_existing_customer_actual.text =
            if (doYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount)) else ""

        hangs_ups_new_customer_display.text =
            if (doYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.displayName != null) doYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.displayName else getString(
                R.string.unknown_caller)
        hangs_ups_new_customer_actual.text =
            if (doYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.actual.amount)) else ""
        // potential bonus growth

        potential_bonus_growth_display.text =
            if (doYesterdayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.displayName != null) doYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.displayName else getString(
                R.string.potential_bonus_growth)

        if (doYesterdayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.actual?.amount?.isNaN() == false && doYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status?.toString() != null) {
            potential_bonus_growth_actual.text =
                if (!doYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount.isNaN()) getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        doYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount)) else ""

            when {
                doYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        phone_narrative.text =if (doYesterdayDetailData?.summary != null) Html.fromHtml(doYesterdayDetailData.summary, Html.FROM_HTML_MODE_COMPACT) else ""

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataDOPeriodRange(phones: PhoneDOPeriodRangeQuery.Phones?) {
        val doPeriodRangeDetailData = phones?.supervisors?.stores?.period
        common_header.total_sales_common_header.text = Validation().ignoreZeroAfterDecimal(doPeriodRangeDetailData?.aot?.actual?.percentage)
        common_header.sales_text_common_header.text = getString(R.string.aot_text_normal)

        setHeaderPhone()
        // calls
        total_calls_display.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.displayName != null) doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.displayName else getString(
                R.string.total_call)
        total_calls_actual.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value != null) Validation().dollarFormatting(
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.actual.value) else ""

        if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value?.isNaN() == false && doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status?.toString() != null) {
            when {
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // total order
        total_order_display.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.displayName != null) doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.displayName else getString(
                R.string.total_orders)
        total_order_goal.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.goal?.value != null) Validation().dollarFormatting(
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.goal.value) else ""
        total_order_varince.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.variance?.value != null) Validation().dollarFormatting(
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.variance.value) else ""
        total_order_actual.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value != null) Validation().dollarFormatting(
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.actual.value) else ""

        if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value?.isNaN() == false && doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status?.toString() != null) {
            when {
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // % order
        percentage_order_display.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.displayName != null) doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.displayName else getString(
                R.string.percentage_order)
        percentage_order_goal.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.goal?.percentage != null) Validation().ignoreZeroAfterDecimal(
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.goal.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_varince.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.variance?.percentage != null) Validation().ignoreZeroAfterDecimal(
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.variance.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_actual.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.percentage != null) Validation().ignoreZeroAfterDecimal(
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.actual.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""

        if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.value?.isNaN() == false && doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status?.toString() != null) {
            when {
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // Average cost
        average_cost_display.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.displayName != null) doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.displayName else getString(
                R.string.average_cost)
        average_cost_goal.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.goal?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.goal.amount)) else ""
        average_cost_varince.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.variance?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.variance.amount)) else ""

        average_cost_actual.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.actual.amount)) else ""


        if (doPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.value?.isNaN() == false && doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status != null) {
            when {
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        potential_awus_display.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.displayName != null) doPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        potential_awus_actual.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount != null) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.actual.amount)) else ""

        if (doPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && doPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            when {
                doPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        existing_customer_display.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) doPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        existing_customer_actual.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null)getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount) )else ""

        new_customer_display.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.displayName != null) doPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.displayName else getString(
                R.string.unknown_caller)
        new_customer_actual.text =
            if (doPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.actual.amount)) else ""

        // hang ups
        total_hungs_up_display.text =
            if (doPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.displayName != null) doPeriodRangeDetailData.hangUpAnalysis.totalHangUps.displayName else getString(
                R.string.potential_awus_growth)
        total_hungs_up_actual.text =
            Validation().dollarFormatting(doPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value)

        if (doPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value?.isNaN() == false && doPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status?.toString() != null) {
            when {
                doPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        before_queue_display.text =
            if (doPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.displayName != null) doPeriodRangeDetailData.hangUpAnalysis.totalHangUps.beforeQueue.displayName else getString(
                R.string.before_queue)
        in_queue_display.text =
            if (doPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.displayName != null) doPeriodRangeDetailData.hangUpAnalysis.totalHangUps.inQueue.displayName else getString(
                R.string.in_queue)
        customer_in_queue_display.text =
            if (doPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.displayName != null) doPeriodRangeDetailData.hangUpAnalysis.totalHangUps.customersInQueue.displayName else getString(
                R.string.customer_in_queue)
        unknown_in_queue_display.text =
            if (doPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.displayName != null) doPeriodRangeDetailData.hangUpAnalysis.totalHangUps.unknownsInQueue.displayName else getString(
                R.string.unknwon_queue)

        before_queuer_actual.text =
            Validation().dollarFormatting(doPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.actual?.value)
        in_queue_actual.text =
            Validation().dollarFormatting(doPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.actual?.value)
        customer_in_queue_actual.text =
            Validation().dollarFormatting(doPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.actual?.value)
        unknown_in_queue_actual.text =
            Validation().dollarFormatting(doPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.actual?.value)

        hangs_ups_potential_awus_display.text =
            if (doPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.displayName != null) doPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        if (doPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && doPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            hangs_ups_potential_awus_actual.text =
                getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(doPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.actual.amount))

            when {
                doPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        hangs_ups_existing_customer_display.text =
            if (doPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) doPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        hangs_ups_existing_customer_actual.text =
            if (doPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount)) else ""

        hangs_ups_new_customer_display.text =
            if (doPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.displayName != null) doPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.displayName else getString(
                R.string.unknown_caller)
        hangs_ups_new_customer_actual.text =
            if (doPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    doPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.actual.amount)) else ""
        // potential bonus growth

        potential_bonus_growth_display.text =
            if (doPeriodRangeDetailData?.hangUpAnalysis?.potentialBonusGrowth?.displayName != null) doPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.displayName else getString(
                R.string.potential_bonus_growth)

        if (doPeriodRangeDetailData?.hangUpAnalysis?.potentialBonusGrowth?.actual?.amount?.isNaN() == false && doPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status?.toString() != null) {
            potential_bonus_growth_actual.text =
                if (!doPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount.isNaN()) getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        doPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount)) else ""

            when {
                doPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                doPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                doPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        phone_narrative.text =if (doPeriodRangeDetailData?.summary != null) Html.fromHtml(doPeriodRangeDetailData.summary, Html.FROM_HTML_MODE_COMPACT) else ""

    }


    // SUPERVISOR
    @RequiresApi(Build.VERSION_CODES.O)
    private fun callPhoneSupervisorAPIToday() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val storeNumber = dbHelper.getAllSelectedStoreList(true)
            val areaCode = dbHelper.getAllSelectedAreaList(true)
            val stateCode = dbHelper.getAllSelectedStoreListState(true)


            Logger.info(
                PhoneSupervisorTodayQuery.OPERATION_NAME.name(),
                "PHONE",
                mapQueryFilters(
                        areaCode, stateCode, Collections.emptyList(),storeNumber, PhoneSupervisorTodayQuery.QUERY_DOCUMENT
                )
            )

            val response = try {
                apolloClient(requireContext()).query(PhoneSupervisorTodayQuery(areaCode.toInput(),
                    stateCode.toInput(),
                    storeNumber.toInput(),
                1)).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"PHONE")
                refreshToken()
                return@launchWhenResumed
            }
            if (response.data?.supervisor != null) {
                progressDialog.dismissProgressDialog()
                setDataSupervisorToday(response.data?.supervisor?.phones)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callPhoneSupervisorAPIYesterday() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val storeNumber = dbHelper.getAllSelectedStoreList(true)
            val areaCode = dbHelper.getAllSelectedAreaList(true)
            val stateCode = dbHelper.getAllSelectedStoreListState(true)

            Logger.info(
                PhonesSupervisorYesterdayQuery.OPERATION_NAME.name(),
                "PHONE",
                mapQueryFilters(
                    areaCode, stateCode, Collections.emptyList(),storeNumber, PhonesSupervisorYesterdayQuery.QUERY_DOCUMENT
                )
            )

            val response = try {
                apolloClient(requireContext()).query(PhonesSupervisorYesterdayQuery(areaCode.toInput(),
                    stateCode.toInput(),
                    storeNumber.toInput())).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"PHONE")

                refreshToken()
                return@launchWhenResumed
            }
            if (response.data?.supervisor != null) {
                progressDialog.dismissProgressDialog()
                setDataSupervisorYesterday(response.data?.supervisor?.phones)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callPhoneSupervisorAPIPeriod() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val storeNumber = dbHelper.getAllSelectedStoreList(true)
            val areaCode = dbHelper.getAllSelectedAreaList(true)
            val stateCode = dbHelper.getAllSelectedStoreListState(true)

            if (StorePrefData.isCalendarSelected) {
                formattedStartDateValue = getFormattedDate(StorePrefData.startDateValue)
                formattedEndDateValue = getFormattedDate(StorePrefData.endDateValue)
            } else {
                formattedStartDateValue = StorePrefData.startDateValue
                formattedEndDateValue = StorePrefData.endDateValue
            }


            Logger.info(
                PhoneSupervisorPeriodRangeQuery.OPERATION_NAME.name(), "PHONE",
                mapQueryFilters(
                    QueryData(
                        areaCode,
                        stateCode,
                        Collections.emptyList(),
                        storeNumber,
                        formattedEndDateValue,
                        formattedStartDateValue,
                        "",
                        PhoneSupervisorPeriodRangeQuery.QUERY_DOCUMENT
                    )
                )
            )

            val response = try {
                apolloClient(requireContext()).query(PhoneSupervisorPeriodRangeQuery(areaCode.toInput(),
                    stateCode.toInput(),
                    storeNumber.toInput(),
                    formattedStartDateValue,
                    formattedEndDateValue)).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"PHONE")
                refreshToken()
                return@launchWhenResumed
            }
            if (response.data?.supervisor != null) {
                progressDialog.dismissProgressDialog()
                setDataSupervisorPeriodRange(response.data?.supervisor?.phones)
            }
        }
    }

    // supervisor
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataSupervisorToday(phones: PhoneSupervisorTodayQuery.Phones?) {
        val supervisorTodayDetailData = phones?.stores?.today
        common_header.total_sales_common_header.text = Validation().ignoreZeroAfterDecimal(supervisorTodayDetailData?.aot?.actual?.percentage)
        common_header.sales_text_common_header.text = getString(R.string.aot_text_normal)

        setHeaderPhone()
        // calls
        total_calls_display.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.displayName != null) supervisorTodayDetailData.phoneCallAnalysis.totalCalls.displayName else getString(
                R.string.total_call)
        total_calls_actual.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value != null) Validation().dollarFormatting(
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.actual.value) else ""

        if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value?.isNaN() == false && supervisorTodayDetailData.phoneCallAnalysis.totalCalls.status?.toString() != null) {
            when {
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // total order
        total_order_display.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.displayName != null) supervisorTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.displayName else getString(
                R.string.total_orders)
        total_order_goal.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.goal?.value != null) Validation().dollarFormatting(
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.goal.value) else ""
        total_order_varince.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.variance?.value != null) Validation().dollarFormatting(
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.variance.value) else ""
        total_order_actual.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value != null) Validation().dollarFormatting(
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.actual.value) else ""

        if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value?.isNaN() == false && supervisorTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status?.toString() != null) {
            when {
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // % order
        percentage_order_display.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.displayName != null) supervisorTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.displayName else getString(
                R.string.percentage_order)
        percentage_order_goal.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.goal?.percentage != null) Validation().ignoreZeroAfterDecimal(
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.goal.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_varince.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.variance?.percentage != null) Validation().ignoreZeroAfterDecimal(
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.variance.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_actual.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.percentage != null) Validation().ignoreZeroAfterDecimal(
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.actual.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""

        if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.value?.isNaN() == false && supervisorTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status?.toString() != null) {
            when {
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // Average cost
        average_cost_display.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.displayName != null) supervisorTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.displayName else getString(
                R.string.average_cost)
        average_cost_goal.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.goal?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.goal.amount)) else ""
        average_cost_varince.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.variance?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.variance.amount)) else ""

        average_cost_actual.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.actual.amount)) else ""


        if (supervisorTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.value?.isNaN() == false && supervisorTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.status != null) {
            when {
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        potential_awus_display.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.displayName != null) supervisorTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        potential_awus_actual.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount != null) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.actual.amount)) else ""

        if (supervisorTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && supervisorTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            when {
                supervisorTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        existing_customer_display.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) supervisorTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        existing_customer_actual.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null)getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount) )else ""

        new_customer_display.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.displayName != null) supervisorTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.displayName else getString(
                R.string.unknown_caller)
        new_customer_actual.text =
            if (supervisorTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.actual.amount)) else ""


        // hang ups
        total_hungs_up_display.text =
            if (supervisorTodayDetailData?.hangUpAnalysis?.totalHangUps?.displayName != null) supervisorTodayDetailData.hangUpAnalysis.totalHangUps.displayName else getString(
                R.string.potential_awus_growth)
        total_hungs_up_actual.text =
            Validation().dollarFormatting(supervisorTodayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value)

        if (supervisorTodayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value?.isNaN() == false && supervisorTodayDetailData.hangUpAnalysis.totalHangUps.status?.toString() != null) {
            when {
                supervisorTodayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorTodayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorTodayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        before_queue_display.text =
            if (supervisorTodayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.displayName != null) supervisorTodayDetailData.hangUpAnalysis.totalHangUps.beforeQueue.displayName else getString(
                R.string.before_queue)
        in_queue_display.text =
            if (supervisorTodayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.displayName != null) supervisorTodayDetailData.hangUpAnalysis.totalHangUps.inQueue.displayName else getString(
                R.string.in_queue)
        customer_in_queue_display.text =
            if (supervisorTodayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.displayName != null) supervisorTodayDetailData.hangUpAnalysis.totalHangUps.customersInQueue.displayName else getString(
                R.string.customer_in_queue)
        unknown_in_queue_display.text =
            if (supervisorTodayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.displayName != null) supervisorTodayDetailData.hangUpAnalysis.totalHangUps.unknownsInQueue.displayName else getString(
                R.string.unknwon_queue)

        before_queuer_actual.text =
            Validation().dollarFormatting(supervisorTodayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.actual?.value)
        in_queue_actual.text =
            Validation().dollarFormatting(supervisorTodayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.actual?.value)
        customer_in_queue_actual.text =
            Validation().dollarFormatting(supervisorTodayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.actual?.value)
        unknown_in_queue_actual.text =
            Validation().dollarFormatting(supervisorTodayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.actual?.value)

        hangs_ups_potential_awus_display.text =
            if (supervisorTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.displayName != null) supervisorTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        if (supervisorTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && supervisorTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            hangs_ups_potential_awus_actual.text =
                getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(supervisorTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.actual.amount))

            when {
                supervisorTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        hangs_ups_existing_customer_display.text =
            if (supervisorTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) supervisorTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        hangs_ups_existing_customer_actual.text =
            if (supervisorTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount)) else ""

        hangs_ups_new_customer_display.text =
            if (supervisorTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.displayName != null) supervisorTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.displayName else getString(
                R.string.unknown_caller)
        hangs_ups_new_customer_actual.text =
            if (supervisorTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.actual.amount)) else ""
        // potential bonus growth

        potential_bonus_growth_display.text =
            if (supervisorTodayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.displayName != null) supervisorTodayDetailData.hangUpAnalysis.potentialBonusGrowth.displayName else getString(
                R.string.potential_bonus_growth)

        if (supervisorTodayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.actual?.amount?.isNaN() == false && supervisorTodayDetailData.hangUpAnalysis.potentialBonusGrowth.status?.toString() != null) {
            potential_bonus_growth_actual.text =
                if (!supervisorTodayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount.isNaN()) getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        supervisorTodayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount)) else ""

            when {
                supervisorTodayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorTodayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorTodayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        phone_narrative.text =if (supervisorTodayDetailData?.summary != null) Html.fromHtml(supervisorTodayDetailData.summary, Html.FROM_HTML_MODE_COMPACT) else ""

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataSupervisorYesterday(phones: PhonesSupervisorYesterdayQuery.Phones?) {
        val supervisorYesterdayDetailData = phones?.stores?.yesterday
        common_header.total_sales_common_header.text = Validation().ignoreZeroAfterDecimal(supervisorYesterdayDetailData?.aot?.actual?.percentage)
        common_header.sales_text_common_header.text = getString(R.string.aot_text_normal)

        setHeaderPhone()
        // calls
        total_calls_display.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.displayName != null) supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.displayName else getString(
                R.string.total_call)
        total_calls_actual.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value != null) Validation().dollarFormatting(
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.actual.value) else ""

        if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value?.isNaN() == false && supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.status?.toString() != null) {
            when {
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // total order
        total_order_display.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.displayName != null) supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.displayName else getString(
                R.string.total_orders)
        total_order_goal.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.goal?.value != null) Validation().dollarFormatting(
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.goal.value) else ""
        total_order_varince.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.variance?.value != null) Validation().dollarFormatting(
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.variance.value) else ""
        total_order_actual.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value != null) Validation().dollarFormatting(
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.actual.value) else ""

        if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value?.isNaN() == false && supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status?.toString() != null) {
            when {
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // % order
        percentage_order_display.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.displayName != null) supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.displayName else getString(
                R.string.percentage_order)
        percentage_order_goal.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.goal?.percentage != null) Validation().ignoreZeroAfterDecimal(
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.goal.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_varince.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.variance?.percentage != null) Validation().ignoreZeroAfterDecimal(
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.variance.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_actual.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.percentage != null) Validation().ignoreZeroAfterDecimal(
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.actual.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""

        if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.value?.isNaN() == false && supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status?.toString() != null) {
            when {
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // Average cost
        average_cost_display.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.displayName != null) supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.displayName else getString(
                R.string.average_cost)
        average_cost_goal.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.goal?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.goal.amount)) else ""
        average_cost_varince.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.variance?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.variance.amount)) else ""

        average_cost_actual.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.actual.amount)) else ""


        if (supervisorYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.value?.isNaN() == false && supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status != null) {
            when {
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        potential_awus_display.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.displayName != null) supervisorYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        potential_awus_actual.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount != null) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.actual.amount)) else ""

        if (supervisorYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && supervisorYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            when {
                supervisorYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        existing_customer_display.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) supervisorYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        existing_customer_actual.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null)getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount) )else ""

        new_customer_display.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.displayName != null) supervisorYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.displayName else getString(
                R.string.unknown_caller)
        new_customer_actual.text =
            if (supervisorYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.actual.amount)) else ""

        // hang ups
        total_hungs_up_display.text =
            if (supervisorYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.displayName != null) supervisorYesterdayDetailData.hangUpAnalysis.totalHangUps.displayName else getString(
                R.string.potential_awus_growth)
        total_hungs_up_actual.text =
            Validation().dollarFormatting(supervisorYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value)

        if (supervisorYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value?.isNaN() == false && supervisorYesterdayDetailData.hangUpAnalysis.totalHangUps.status?.toString() != null) {
            when {
                supervisorYesterdayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorYesterdayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorYesterdayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        before_queue_display.text =
            if (supervisorYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.displayName != null) supervisorYesterdayDetailData.hangUpAnalysis.totalHangUps.beforeQueue.displayName else getString(
                R.string.before_queue)
        in_queue_display.text =
            if (supervisorYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.displayName != null) supervisorYesterdayDetailData.hangUpAnalysis.totalHangUps.inQueue.displayName else getString(
                R.string.in_queue)
        customer_in_queue_display.text =
            if (supervisorYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.displayName != null) supervisorYesterdayDetailData.hangUpAnalysis.totalHangUps.customersInQueue.displayName else getString(
                R.string.customer_in_queue)
        unknown_in_queue_display.text =
            if (supervisorYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.displayName != null) supervisorYesterdayDetailData.hangUpAnalysis.totalHangUps.unknownsInQueue.displayName else getString(
                R.string.unknwon_queue)

        before_queuer_actual.text =
            Validation().dollarFormatting(supervisorYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.actual?.value)
        in_queue_actual.text =
            Validation().dollarFormatting(supervisorYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.actual?.value)
        customer_in_queue_actual.text =
            Validation().dollarFormatting(supervisorYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.actual?.value)
        unknown_in_queue_actual.text =
            Validation().dollarFormatting(supervisorYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.actual?.value)

        hangs_ups_potential_awus_display.text =
            if (supervisorYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.displayName != null) supervisorYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        if (supervisorYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && supervisorYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            hangs_ups_potential_awus_actual.text =
                getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(supervisorYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.actual.amount))

            when {
                supervisorYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        hangs_ups_existing_customer_display.text =
            if (supervisorYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) supervisorYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        hangs_ups_existing_customer_actual.text =
            if (supervisorYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount)) else ""

        hangs_ups_new_customer_display.text =
            if (supervisorYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.displayName != null) supervisorYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.displayName else getString(
                R.string.unknown_caller)
        hangs_ups_new_customer_actual.text =
            if (supervisorYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.actual.amount)) else ""
        // potential bonus growth

        potential_bonus_growth_display.text =
            if (supervisorYesterdayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.displayName != null) supervisorYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.displayName else getString(
                R.string.potential_bonus_growth)

        if (supervisorYesterdayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.actual?.amount?.isNaN() == false && supervisorYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status?.toString() != null) {
            potential_bonus_growth_actual.text =
                if (!supervisorYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount.isNaN()) getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        supervisorYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount)) else ""

            when {
                supervisorYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        phone_narrative.text =if (supervisorYesterdayDetailData?.summary != null) Html.fromHtml(supervisorYesterdayDetailData.summary, Html.FROM_HTML_MODE_COMPACT) else ""

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataSupervisorPeriodRange(phones: PhoneSupervisorPeriodRangeQuery.Phones?) {
        val supervisorPeriodRangeDetailData = phones?.stores?.period
        common_header.total_sales_common_header.text = Validation().ignoreZeroAfterDecimal(supervisorPeriodRangeDetailData?.aot?.actual?.percentage)
        common_header.sales_text_common_header.text = getString(R.string.aot_text_normal)

        setHeaderPhone()
        // calls
        total_calls_display.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.displayName != null) supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.displayName else getString(
                R.string.total_call)
        total_calls_actual.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value != null) Validation().dollarFormatting(
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.actual.value) else ""

        if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value?.isNaN() == false && supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status?.toString() != null) {
            when {
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // total order
        total_order_display.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.displayName != null) supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.displayName else getString(
                R.string.total_orders)
        total_order_goal.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.goal?.value != null) Validation().dollarFormatting(
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.goal.value) else ""
        total_order_varince.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.variance?.value != null) Validation().dollarFormatting(
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.variance.value) else ""
        total_order_actual.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value != null) Validation().dollarFormatting(
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.actual.value) else ""

        if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value?.isNaN() == false && supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status?.toString() != null) {
            when {
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // % order
        percentage_order_display.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.displayName != null) supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.displayName else getString(
                R.string.percentage_order)
        percentage_order_goal.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.goal?.percentage != null) Validation().ignoreZeroAfterDecimal(
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.goal.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_varince.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.variance?.percentage != null) Validation().ignoreZeroAfterDecimal(
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.variance.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_actual.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.percentage != null) Validation().ignoreZeroAfterDecimal(
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.actual.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""

        if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.value?.isNaN() == false && supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status?.toString() != null) {
            when {
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // Average cost
        average_cost_display.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.displayName != null) supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.displayName else getString(
                R.string.average_cost)
        average_cost_goal.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.goal?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.goal.amount)) else ""
        average_cost_varince.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.variance?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.variance.amount)) else ""

        average_cost_actual.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.actual.amount)) else ""


        if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.value?.isNaN() == false && supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status != null) {
            when {
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        potential_awus_display.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.displayName != null) supervisorPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        potential_awus_actual.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount != null) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.actual.amount)) else ""

        if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && supervisorPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            when {
                supervisorPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        existing_customer_display.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) supervisorPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        existing_customer_actual.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null)getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount) )else ""

        new_customer_display.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.displayName != null) supervisorPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.displayName else getString(
                R.string.unknown_caller)
        new_customer_actual.text =
            if (supervisorPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.actual.amount)) else ""

        // hang ups
        total_hungs_up_display.text =
            if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.displayName != null) supervisorPeriodRangeDetailData.hangUpAnalysis.totalHangUps.displayName else getString(
                R.string.potential_awus_growth)
        total_hungs_up_actual.text =
            Validation().dollarFormatting(supervisorPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value)

        if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value?.isNaN() == false && supervisorPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status?.toString() != null) {
            when {
                supervisorPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        before_queue_display.text =
            if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.displayName != null) supervisorPeriodRangeDetailData.hangUpAnalysis.totalHangUps.beforeQueue.displayName else getString(
                R.string.before_queue)
        in_queue_display.text =
            if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.displayName != null) supervisorPeriodRangeDetailData.hangUpAnalysis.totalHangUps.inQueue.displayName else getString(
                R.string.in_queue)
        customer_in_queue_display.text =
            if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.displayName != null) supervisorPeriodRangeDetailData.hangUpAnalysis.totalHangUps.customersInQueue.displayName else getString(
                R.string.customer_in_queue)
        unknown_in_queue_display.text =
            if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.displayName != null) supervisorPeriodRangeDetailData.hangUpAnalysis.totalHangUps.unknownsInQueue.displayName else getString(
                R.string.unknwon_queue)

        before_queuer_actual.text =
            Validation().dollarFormatting(supervisorPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.actual?.value)
        in_queue_actual.text =
            Validation().dollarFormatting(supervisorPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.actual?.value)
        customer_in_queue_actual.text =
            Validation().dollarFormatting(supervisorPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.actual?.value)
        unknown_in_queue_actual.text =
            Validation().dollarFormatting(supervisorPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.actual?.value)

        hangs_ups_potential_awus_display.text =
            if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.displayName != null) supervisorPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && supervisorPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            hangs_ups_potential_awus_actual.text =
                getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(supervisorPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.actual.amount))

            when {
                supervisorPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        hangs_ups_existing_customer_display.text =
            if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) supervisorPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        hangs_ups_existing_customer_actual.text =
            if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount)) else ""

        hangs_ups_new_customer_display.text =
            if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.displayName != null) supervisorPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.displayName else getString(
                R.string.unknown_caller)
        hangs_ups_new_customer_actual.text =
            if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    supervisorPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.actual.amount)) else ""
        // potential bonus growth

        potential_bonus_growth_display.text =
            if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.potentialBonusGrowth?.displayName != null) supervisorPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.displayName else getString(
                R.string.potential_bonus_growth)

        if (supervisorPeriodRangeDetailData?.hangUpAnalysis?.potentialBonusGrowth?.actual?.amount?.isNaN() == false && supervisorPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status?.toString() != null) {
            potential_bonus_growth_actual.text =
                if (!supervisorPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount.isNaN()) getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        supervisorPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount)) else ""

            when {
                supervisorPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                supervisorPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                supervisorPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        phone_narrative.text =if (supervisorPeriodRangeDetailData?.summary != null) Html.fromHtml(supervisorPeriodRangeDetailData.summary, Html.FROM_HTML_MODE_COMPACT) else ""

    }
    // gm

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callPhoneGMAPIToday() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {


            val phoneStoreListValue = mutableListOf<String>()
            phoneStoreListValue.add(StorePrefData.StoreIdFromLogin)
            Logger.info(
                PhoneGMTodayQuery.OPERATION_NAME.name(), "PHONE", mapQueryFilters(
                    QueryData(
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        phoneStoreListValue,
                        "",
                        "",
                        "",
                        PhoneGMTodayQuery.QUERY_DOCUMENT
                    )
                )
            )

            val response = try {
                apolloClient(requireContext()).query(PhoneGMTodayQuery(StorePrefData.StoreIdFromLogin)).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"PHONES")
                refreshToken()
                return@launchWhenResumed
            }
            if (response.data?.generalManager != null) {
                progressDialog.dismissProgressDialog()
                setDataGMToday(response.data?.generalManager?.phones)

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callPhoneGMAPIYesterday() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {

            val phoneStoreListValue = mutableListOf<String>()
            phoneStoreListValue.add(StorePrefData.StoreIdFromLogin)
            Logger.info(
                PhoneGMYesterdayQuery.OPERATION_NAME.name(), "PHONE", mapQueryFilters(
                    QueryData(
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        phoneStoreListValue,
                        "",
                        "",
                        "",
                        PhoneGMYesterdayQuery.QUERY_DOCUMENT
                    )
                )
            )

            val response = try {
                apolloClient(requireContext()).query(PhoneGMYesterdayQuery(StorePrefData.StoreIdFromLogin)).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"PHONES")
                refreshToken()
                return@launchWhenResumed
            }
            if (response.data?.generalManager != null) {
                progressDialog.dismissProgressDialog()
                setDataGMYesterday(response.data?.generalManager?.phones)

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callPhoneGMAPIPeriod() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            if (StorePrefData.isCalendarSelected) {
                formattedStartDateValue = getFormattedDate(StorePrefData.startDateValue)
                formattedEndDateValue = getFormattedDate(StorePrefData.endDateValue)
            } else {
                formattedStartDateValue = StorePrefData.startDateValue
                formattedEndDateValue = StorePrefData.endDateValue
            }

            val phoneGMStoreListValue = mutableListOf<String>()
            phoneGMStoreListValue.add(StorePrefData.StoreIdFromLogin)
            Logger.info(
                PhoneGMPeriodRangeQuery.OPERATION_NAME.name(), "PHONE", mapQueryFilters(
                    QueryData(
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        phoneGMStoreListValue,
                        formattedEndDateValue,
                        formattedStartDateValue,
                        "",
                        PhoneGMPeriodRangeQuery.QUERY_DOCUMENT
                    )
                )
            )



            val response = try {
                apolloClient(requireContext()).query(PhoneGMPeriodRangeQuery(
                    formattedStartDateValue,
                    formattedEndDateValue, StorePrefData.StoreIdFromLogin)).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(e.message.toString(),"PHONES")
                refreshToken()
                return@launchWhenResumed
            }
            if (response.data?.generalManager != null) {
                progressDialog.dismissProgressDialog()
                setDataGMPeriodRange(response.data?.generalManager?.phones)

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataGMToday(phones: PhoneGMTodayQuery.Phones?) {
        val gmTodayDetailData = phones?.store?.today
        common_header.total_sales_common_header.text = Validation().ignoreZeroAfterDecimal(gmTodayDetailData?.aot?.actual?.percentage)
        common_header.sales_text_common_header.text = getString(R.string.aot_text_normal)

        setHeaderPhone()
        // calls
        total_calls_display.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.displayName != null) gmTodayDetailData.phoneCallAnalysis.totalCalls.displayName else getString(
                R.string.total_call)
        total_calls_actual.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value != null) Validation().dollarFormatting(
                gmTodayDetailData.phoneCallAnalysis.totalCalls.actual.value) else ""

        if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value?.isNaN() == false && gmTodayDetailData.phoneCallAnalysis.totalCalls.status?.toString() != null) {
            when {
                gmTodayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmTodayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmTodayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // total order
        total_order_display.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.displayName != null) gmTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.displayName else getString(
                R.string.total_orders)
        total_order_goal.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.goal?.value != null) Validation().dollarFormatting(
                gmTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.goal.value) else ""
        total_order_varince.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.variance?.value != null) Validation().dollarFormatting(
                gmTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.variance.value) else ""
        total_order_actual.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value != null) Validation().dollarFormatting(
                gmTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.actual.value) else ""

        if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value?.isNaN() == false && gmTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status?.toString() != null) {
            when {
                gmTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmTodayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // % order
        percentage_order_display.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.displayName != null) gmTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.displayName else getString(
                R.string.percentage_order)
        percentage_order_goal.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.goal?.percentage != null) Validation().ignoreZeroAfterDecimal(
                gmTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.goal.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_varince.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.variance?.percentage != null) Validation().ignoreZeroAfterDecimal(
                gmTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.variance.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_actual.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.percentage != null) Validation().ignoreZeroAfterDecimal(
                gmTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.actual.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""

        if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.value?.isNaN() == false && gmTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status?.toString() != null) {
            when {
                gmTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmTodayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // Average cost
        average_cost_display.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.displayName != null) gmTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.displayName else getString(
                R.string.average_cost)
        average_cost_goal.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.goal?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.goal.amount)) else ""
        average_cost_varince.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.variance?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.variance.amount)) else ""

        average_cost_actual.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.actual.amount)) else ""


        if (gmTodayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.value?.isNaN() == false && gmTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.status != null) {
            when {
                gmTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmTodayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        potential_awus_display.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.displayName != null) gmTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        potential_awus_actual.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount != null) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.actual.amount)) else ""

        if (gmTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && gmTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            when {
                gmTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        existing_customer_display.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) gmTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        existing_customer_actual.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null)getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount) )else ""

        new_customer_display.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.displayName != null) gmTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.displayName else getString(
                R.string.unknown_caller)
        new_customer_actual.text =
            if (gmTodayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmTodayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.actual.amount)) else ""

        // hang ups
        total_hungs_up_display.text =
            if (gmTodayDetailData?.hangUpAnalysis?.totalHangUps?.displayName != null) gmTodayDetailData.hangUpAnalysis.totalHangUps.displayName else getString(
                R.string.potential_awus_growth)
        total_hungs_up_actual.text =
            Validation().dollarFormatting(gmTodayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value)

        if (gmTodayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value?.isNaN() == false && gmTodayDetailData.hangUpAnalysis.totalHangUps.status?.toString() != null) {
            when {
                gmTodayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmTodayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmTodayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        before_queue_display.text =
            if (gmTodayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.displayName != null) gmTodayDetailData.hangUpAnalysis.totalHangUps.beforeQueue.displayName else getString(
                R.string.before_queue)
        in_queue_display.text =
            if (gmTodayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.displayName != null) gmTodayDetailData.hangUpAnalysis.totalHangUps.inQueue.displayName else getString(
                R.string.in_queue)
        customer_in_queue_display.text =
            if (gmTodayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.displayName != null) gmTodayDetailData.hangUpAnalysis.totalHangUps.customersInQueue.displayName else getString(
                R.string.customer_in_queue)
        unknown_in_queue_display.text =
            if (gmTodayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.displayName != null) gmTodayDetailData.hangUpAnalysis.totalHangUps.unknownsInQueue.displayName else getString(
                R.string.unknwon_queue)

        before_queuer_actual.text =
            Validation().dollarFormatting(gmTodayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.actual?.value)
        in_queue_actual.text =
            Validation().dollarFormatting(gmTodayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.actual?.value)
        customer_in_queue_actual.text =
            Validation().dollarFormatting(gmTodayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.actual?.value)
        unknown_in_queue_actual.text =
            Validation().dollarFormatting(gmTodayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.actual?.value)

        hangs_ups_potential_awus_display.text =
            if (gmTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.displayName != null) gmTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        if (gmTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && gmTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            hangs_ups_potential_awus_actual.text =
                getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(gmTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.actual.amount))

            when {
                gmTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        hangs_ups_existing_customer_display.text =
            if (gmTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) gmTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        hangs_ups_existing_customer_actual.text =
            if (gmTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount)) else ""

        hangs_ups_new_customer_display.text =
            if (gmTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.displayName != null) gmTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.displayName else getString(
                R.string.unknown_caller)
        hangs_ups_new_customer_actual.text =
            if (gmTodayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmTodayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.actual.amount)) else ""
        // potential bonus growth

        potential_bonus_growth_display.text =
            if (gmTodayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.displayName != null) gmTodayDetailData.hangUpAnalysis.potentialBonusGrowth.displayName else getString(
                R.string.potential_bonus_growth)

        if (gmTodayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.actual?.amount?.isNaN() == false && gmTodayDetailData.hangUpAnalysis.potentialBonusGrowth.status?.toString() != null) {
            potential_bonus_growth_actual.text =
                if (!gmTodayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount.isNaN()) getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        gmTodayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount)) else ""

            when {
                gmTodayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmTodayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmTodayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        phone_narrative.text =if (gmTodayDetailData?.summary != null) Html.fromHtml(gmTodayDetailData.summary, Html.FROM_HTML_MODE_COMPACT) else ""

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataGMYesterday(phones: PhoneGMYesterdayQuery.Phones?) {
        val gmYesterdayDetailData = phones?.store?.yesterday
        common_header.total_sales_common_header.text = Validation().ignoreZeroAfterDecimal(gmYesterdayDetailData?.aot?.actual?.percentage)
        common_header.sales_text_common_header.text = getString(R.string.aot_text_normal)

        setHeaderPhone()
        // calls
        total_calls_display.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.displayName != null) gmYesterdayDetailData.phoneCallAnalysis.totalCalls.displayName else getString(
                R.string.total_call)
        total_calls_actual.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value != null) Validation().dollarFormatting(
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.actual.value) else ""

        if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value?.isNaN() == false && gmYesterdayDetailData.phoneCallAnalysis.totalCalls.status?.toString() != null) {
            when {
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // total order
        total_order_display.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.displayName != null) gmYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.displayName else getString(
                R.string.total_orders)
        total_order_goal.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.goal?.value != null) Validation().dollarFormatting(
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.goal.value) else ""
        total_order_varince.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.variance?.value != null) Validation().dollarFormatting(
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.variance.value) else ""
        total_order_actual.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value != null) Validation().dollarFormatting(
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.actual.value) else ""

        if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value?.isNaN() == false && gmYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status?.toString() != null) {
            when {
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // % order
        percentage_order_display.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.displayName != null) gmYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.displayName else getString(
                R.string.percentage_order)
        percentage_order_goal.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.goal?.percentage != null) Validation().ignoreZeroAfterDecimal(
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.goal.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_varince.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.variance?.percentage != null) Validation().ignoreZeroAfterDecimal(
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.variance.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_actual.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.percentage != null) Validation().ignoreZeroAfterDecimal(
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.actual.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""

        if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.value?.isNaN() == false && gmYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status?.toString() != null) {
            when {
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // Average cost
        average_cost_display.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.displayName != null) gmYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.displayName else getString(
                R.string.average_cost)
        average_cost_goal.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.goal?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.goal.amount)) else ""
        average_cost_varince.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.variance?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.variance.amount)) else ""

        average_cost_actual.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.actual.amount)) else ""


        if (gmYesterdayDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.value?.isNaN() == false && gmYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status != null) {
            when {
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmYesterdayDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        potential_awus_display.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.displayName != null) gmYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        potential_awus_actual.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount != null) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.actual.amount)) else ""

        if (gmYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && gmYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            when {
                gmYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        existing_customer_display.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) gmYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        existing_customer_actual.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null)getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount) )else ""

        new_customer_display.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.displayName != null) gmYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.displayName else getString(
                R.string.unknown_caller)
        new_customer_actual.text =
            if (gmYesterdayDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmYesterdayDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.actual.amount)) else ""

        // hang ups
        total_hungs_up_display.text =
            if (gmYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.displayName != null) gmYesterdayDetailData.hangUpAnalysis.totalHangUps.displayName else getString(
                R.string.potential_awus_growth)
        total_hungs_up_actual.text =
            Validation().dollarFormatting(gmYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value)

        if (gmYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value?.isNaN() == false && gmYesterdayDetailData.hangUpAnalysis.totalHangUps.status?.toString() != null) {
            when {
                gmYesterdayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmYesterdayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmYesterdayDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        before_queue_display.text =
            if (gmYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.displayName != null) gmYesterdayDetailData.hangUpAnalysis.totalHangUps.beforeQueue.displayName else getString(
                R.string.before_queue)
        in_queue_display.text =
            if (gmYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.displayName != null) gmYesterdayDetailData.hangUpAnalysis.totalHangUps.inQueue.displayName else getString(
                R.string.in_queue)
        customer_in_queue_display.text =
            if (gmYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.displayName != null) gmYesterdayDetailData.hangUpAnalysis.totalHangUps.customersInQueue.displayName else getString(
                R.string.customer_in_queue)
        unknown_in_queue_display.text =
            if (gmYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.displayName != null) gmYesterdayDetailData.hangUpAnalysis.totalHangUps.unknownsInQueue.displayName else getString(
                R.string.unknwon_queue)

        before_queuer_actual.text =
            Validation().dollarFormatting(gmYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.actual?.value)
        in_queue_actual.text =
            Validation().dollarFormatting(gmYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.actual?.value)
        customer_in_queue_actual.text =
            Validation().dollarFormatting(gmYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.actual?.value)
        unknown_in_queue_actual.text =
            Validation().dollarFormatting(gmYesterdayDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.actual?.value)

        hangs_ups_potential_awus_display.text =
            if (gmYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.displayName != null) gmYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        if (gmYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && gmYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            hangs_ups_potential_awus_actual.text =
                getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(gmYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.actual.amount))

            when {
                gmYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        hangs_ups_existing_customer_display.text =
            if (gmYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) gmYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        hangs_ups_existing_customer_actual.text =
            if (gmYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount)) else ""

        hangs_ups_new_customer_display.text =
            if (gmYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.displayName != null) gmYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.displayName else getString(
                R.string.unknown_caller)
        hangs_ups_new_customer_actual.text =
            if (gmYesterdayDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmYesterdayDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.actual.amount)) else ""
        // potential bonus growth

        potential_bonus_growth_display.text =
            if (gmYesterdayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.displayName != null) gmYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.displayName else getString(
                R.string.potential_bonus_growth)

        if (gmYesterdayDetailData?.hangUpAnalysis?.potentialBonusGrowth?.actual?.amount?.isNaN() == false && gmYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status?.toString() != null) {
            potential_bonus_growth_actual.text =
                if (!gmYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount.isNaN()) getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        gmYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount)) else ""

            when {
                gmYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmYesterdayDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        phone_narrative.text =if (gmYesterdayDetailData?.summary != null) Html.fromHtml(gmYesterdayDetailData.summary, Html.FROM_HTML_MODE_COMPACT) else ""

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataGMPeriodRange(phones: PhoneGMPeriodRangeQuery.Phones?) {
        val gmPeriodRangeDetailData = phones?.store?.period
        common_header.total_sales_common_header.text = Validation().ignoreZeroAfterDecimal(gmPeriodRangeDetailData?.aot?.actual?.percentage)
        common_header.sales_text_common_header.text = getString(R.string.aot_text_normal)

        setHeaderPhone()
        // calls
        total_calls_display.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.displayName != null) gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.displayName else getString(
                R.string.total_call)
        total_calls_actual.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value != null) Validation().dollarFormatting(
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.actual.value) else ""

        if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.actual?.value?.isNaN() == false && gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status?.toString() != null) {
            when {
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_calls_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_calls_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // total order
        total_order_display.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.displayName != null) gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.displayName else getString(
                R.string.total_orders)
        total_order_goal.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.goal?.value != null) Validation().dollarFormatting(
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.goal.value) else ""
        total_order_varince.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.variance?.value != null) Validation().dollarFormatting(
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.variance.value) else ""
        total_order_actual.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value != null) Validation().dollarFormatting(
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.actual.value) else ""

        if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.totalOrders?.actual?.value?.isNaN() == false && gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status?.toString() != null) {
            when {
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.totalOrders.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // % order
        percentage_order_display.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.displayName != null) gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.displayName else getString(
                R.string.percentage_order)
        percentage_order_goal.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.goal?.percentage != null) Validation().ignoreZeroAfterDecimal(
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.goal.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_varince.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.variance?.percentage != null) Validation().ignoreZeroAfterDecimal(
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.variance.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""
        percentage_order_actual.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.percentage != null) Validation().ignoreZeroAfterDecimal(
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.actual.percentage)
                .plus(
                    getString(
                        R.string.percentage_text)) else ""

        if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.percentageOrdered?.actual?.value?.isNaN() == false && gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status?.toString() != null) {
            when {
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.percentageOrdered.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    percentage_order_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    percentage_order_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        // Average cost
        average_cost_display.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.displayName != null) gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.displayName else getString(
                R.string.average_cost)
        average_cost_goal.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.goal?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.goal.amount)) else ""
        average_cost_varince.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.variance?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.variance.amount)) else ""

        average_cost_actual.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.actual.amount)) else ""


        if (gmPeriodRangeDetailData?.phoneCallAnalysis?.totalCalls?.averageCost?.actual?.value?.isNaN() == false && gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status != null) {
            when {
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmPeriodRangeDetailData.phoneCallAnalysis.totalCalls.averageCost.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    average_cost_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    average_cost_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        potential_awus_display.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.displayName != null) gmPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        potential_awus_actual.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount != null) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.actual.amount)) else ""

        if (gmPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && gmPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            when {
                gmPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        existing_customer_display.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) gmPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        existing_customer_actual.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null)getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount) )else ""

        new_customer_display.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.displayName != null) gmPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.displayName else getString(
                R.string.unknown_caller)
        new_customer_actual.text =
            if (gmPeriodRangeDetailData?.phoneCallAnalysis?.potentialAWUSGrowth?.newCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmPeriodRangeDetailData.phoneCallAnalysis.potentialAWUSGrowth.newCustomers.actual.amount)) else ""

        // hang ups
        total_hungs_up_display.text =
            if (gmPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.displayName != null) gmPeriodRangeDetailData.hangUpAnalysis.totalHangUps.displayName else getString(
                R.string.potential_awus_growth)
        total_hungs_up_actual.text =
            Validation().dollarFormatting(gmPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value)

        if (gmPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.actual?.value?.isNaN() == false && gmPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status?.toString() != null) {
            when {
                gmPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(R.string.out_of_range) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmPeriodRangeDetailData.hangUpAnalysis.totalHangUps.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    total_hungs_up_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    total_hungs_up_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        before_queue_display.text =
            if (gmPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.displayName != null) gmPeriodRangeDetailData.hangUpAnalysis.totalHangUps.beforeQueue.displayName else getString(
                R.string.before_queue)
        in_queue_display.text =
            if (gmPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.displayName != null) gmPeriodRangeDetailData.hangUpAnalysis.totalHangUps.inQueue.displayName else getString(
                R.string.in_queue)
        customer_in_queue_display.text =
            if (gmPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.displayName != null) gmPeriodRangeDetailData.hangUpAnalysis.totalHangUps.customersInQueue.displayName else getString(
                R.string.customer_in_queue)
        unknown_in_queue_display.text =
            if (gmPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.displayName != null) gmPeriodRangeDetailData.hangUpAnalysis.totalHangUps.unknownsInQueue.displayName else getString(
                R.string.unknwon_queue)

        before_queuer_actual.text =
            Validation().dollarFormatting(gmPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.beforeQueue?.actual?.value)
        in_queue_actual.text =
            Validation().dollarFormatting(gmPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.inQueue?.actual?.value)
        customer_in_queue_actual.text =
            Validation().dollarFormatting(gmPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.customersInQueue?.actual?.value)
        unknown_in_queue_actual.text =
            Validation().dollarFormatting(gmPeriodRangeDetailData?.hangUpAnalysis?.totalHangUps?.unknownsInQueue?.actual?.value)

        hangs_ups_potential_awus_display.text =
            if (gmPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.displayName != null) gmPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.displayName else getString(
                R.string.potential_awus_growth)
        if (gmPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.actual?.amount?.isNaN() == false && gmPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status?.toString() != null) {
            hangs_ups_potential_awus_actual.text =
                getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(gmPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.actual.amount))

            when {
                gmPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    hangs_ups_potential_awus_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    hangs_ups_potential_awus_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        hangs_ups_existing_customer_display.text =
            if (gmPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.displayName != null) gmPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.displayName else getString(
                R.string.existing_customer)
        hangs_ups_existing_customer_actual.text =
            if (gmPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.existingCustomers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.existingCustomers.actual.amount)) else ""

        hangs_ups_new_customer_display.text =
            if (gmPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.displayName != null) gmPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.displayName else getString(
                R.string.unknown_caller)
        hangs_ups_new_customer_actual.text =
            if (gmPeriodRangeDetailData?.hangUpAnalysis?.potentialAWUSGrowth?.unknownCallers?.actual?.amount != null) getString(
                R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    gmPeriodRangeDetailData.hangUpAnalysis.potentialAWUSGrowth.unknownCallers.actual.amount)) else ""
        // potential bonus growth

        potential_bonus_growth_display.text =
            if (gmPeriodRangeDetailData?.hangUpAnalysis?.potentialBonusGrowth?.displayName != null) gmPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.displayName else getString(
                R.string.potential_bonus_growth)

        if (gmPeriodRangeDetailData?.hangUpAnalysis?.potentialBonusGrowth?.actual?.amount?.isNaN() == false && gmPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status?.toString() != null) {
            potential_bonus_growth_actual.text =
                if (!gmPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount.isNaN()) getString(
                    R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        gmPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.actual.amount)) else ""

            when {
                gmPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.out_of_range) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.red))
                }
                gmPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.under_limit) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.green))

                }
                gmPeriodRangeDetailData.hangUpAnalysis.potentialBonusGrowth.status.toString() == resources.getString(
                    R.string.neutral) -> {
                    potential_bonus_growth_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.empty_circle,
                        0
                    )
                    potential_bonus_growth_actual.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        phone_narrative.text =if (gmPeriodRangeDetailData?.summary != null) Html.fromHtml(gmPeriodRangeDetailData.summary, Html.FROM_HTML_MODE_COMPACT) else ""

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshToken() {
        val progressDialogPhoneFragment = CustomProgressDialog(requireActivity())
        progressDialogPhoneFragment.showProgressDialog()
        val apiServicePhoneFragment: ApiInterface = ApiClientAuth().getClient()!!.create(ApiInterface::class.java)
        val callPhoneFragment = apiServicePhoneFragment.refreshToken(SendRefreshRequest(
            StorePrefData.refreshToken))
        callPhoneFragment.enqueue(object : retrofit2.Callback<LoginSuccess> {
            override fun onResponse(
                callPhoneFragment: retrofit2.Call<LoginSuccess>,
                responsePhoneFragment: retrofit2.Response<LoginSuccess>,
            ) {
                progressDialogPhoneFragment.dismissProgressDialog()
                if (responsePhoneFragment.isSuccessful) {

                    Logger.info("Token Refreshed","PHONE")

                    StorePrefData.token = responsePhoneFragment.body()!!.authenticationResult.accessToken
                    hitApiAfterRefreshToken()
                } else {
                    val gsonPhoneFragment = Gson()
                    val typePhoneFragment = object : TypeToken<LoginFail>() {
                    }.type
                    val errorResponsePhoneFragment = gsonPhoneFragment.fromJson<LoginFail>(
                        responsePhoneFragment.errorBody()!!.charStream(), typePhoneFragment
                    )
                    Logger.error(errorResponsePhoneFragment.message,"PHONE")

                    Validation().showMessageToast(requireActivity(), errorResponsePhoneFragment.message)
                }


            }

            override fun onFailure(call: retrofit2.Call<LoginSuccess>, t: Throwable) {
                Logger.error(t.message.toString(),"PHONE")

            }
        })
    }

    private fun getFormattedDate(selectedDate: String): String {
        val date: Date?
        val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
        try {
            date = formatter.parse(selectedDate)
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date!!)
        } catch (e: ParseException) {
            Logger.error(e.message.toString(),"PHONES")
            e.printStackTrace()
        }
        return ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setHeaderPhone(){
        val periodText: String?
        periodText = if (StorePrefData.isSelectedPeriod.isEmpty()) {
            DateFormatterUtil.previousDate()+" | "+getString(R.string.yesterday_text)
        } else {
            if (StorePrefData.startDateValue.isEmpty()) {
                DateFormatterUtil.previousDate()+" | "+ StorePrefData.isSelectedPeriod
            }else{
                StorePrefData.isSelectedDate+" | "+ StorePrefData.isSelectedPeriod
            }

        }
        Validation().validateFilterKPI(requireActivity(), dbHelper, common_header.store_header!!, periodText)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun hitApiAfterRefreshToken() {
        when (StorePrefData.role) {
            getString(R.string.ceo_text) -> {
                when (periodRange) {
                    IpConstants.rangeFrom -> {
                        callPhoneCEOAPIPeriod()
                    }
                    IpConstants.Today -> {
                        callPhoneCEOAPIToday()
                    }
                    IpConstants.Yesterday -> {
                        callPhoneCEOAPIYesterday()
                    }
                }
            }
            getString(R.string.do_text) -> {
                when (periodRange) {
                    IpConstants.rangeFrom -> {
                        callPhoneDOAPIPeriod()
                    }
                    IpConstants.Today -> {
                        callPhoneDOAPIToday()
                    }
                    IpConstants.Yesterday -> {
                        callPhoneDOAPIYesterday()
                    }
                }
            }
            getString(R.string.supervisor_text) -> {
                when (periodRange) {
                    IpConstants.rangeFrom -> {
                        callPhoneSupervisorAPIPeriod()
                    }
                    IpConstants.Today -> {
                        callPhoneSupervisorAPIToday()
                    }
                    IpConstants.Yesterday -> {
                        callPhoneSupervisorAPIYesterday()
                    }
                }
            }
            getString(R.string.gm_text) -> {
                when (periodRange) {
                    IpConstants.rangeFrom -> {
                        callPhoneGMAPIPeriod()
                    }
                    IpConstants.Today -> {
                        callPhoneGMAPIToday()
                    }
                    IpConstants.Yesterday -> {
                        callPhoneGMAPIYesterday()
                    }
                }
            }
        }
    }

    fun openFilter() {
        StorePrefData.whichBottomNavigationClicked = getString(R.string.title_phone)
        val intent = Intent(requireContext(), FilterActivity::class.java)
        startActivity(intent)
    }


}