package com.arria.ping.ui.kpi.gm.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.arria.ping.R
import com.arria.ping.adapter.StoreCheckinListAdapter
import com.arria.ping.adapter.StoreCheckinListAdapter2
import com.arria.ping.apiclient.ApiClientAuth
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.apollo.apolloClient
import com.arria.ping.log.Logger
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.util.NetworkHelper
import com.arria.ping.kpi.StoreTodayKPIQuery
import com.arria.ping.model.LoginFail
import com.arria.ping.model.SendRefreshRequest
import com.arria.ping.model.successLogin.LoginSuccess
import com.arria.ping.ui.filter.FilterActivity
import com.arria.ping.ui.kpi.overview.*
import com.arria.ping.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.common_header.*
import kotlinx.android.synthetic.main.common_header.view.*
import kotlinx.android.synthetic.main.fragment_kpi_today.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class GMTodayKpiFragment : Fragment(), View.OnClickListener {

    private lateinit var storeDetailsTodayKpi: StoreTodayKPIQuery.GeneralManager

    @Inject
    lateinit var networkHelper: NetworkHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_kpi_today, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        check_in_rcv.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        check_in_rcv2.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        if (networkHelper.isNetworkConnected()) {
            callKpiTodayApi()
        } else {
            Validation().showMessageToast(
                requireActivity(),
                resources.getString(R.string.internet_connection)
            )
        }
        aws_parent_today_kpi.setOnClickListener(this)
        labour_parent_today_kpi.setOnClickListener(this)
        service_parent_today_kpi.setOnClickListener(this)
        oer_parent_today_kpi.setOnClickListener(this)
        filter_icon.setOnClickListener(this)
        cash_parent_today_kpi.setOnClickListener(this)
        filter_parent.setOnClickListener(this)
    }

    private fun callKpiTodayApi() {
        val progressDialogKpiToday = CustomProgressDialog(requireContext())
        progressDialogKpiToday.showProgressDialog()
        lifecycleScope.launchWhenResumed {


            val storeListValue = mutableListOf<String>()
            storeListValue.add(StorePrefData.StoreIdFromLogin)

            Logger.info(
                StoreTodayKPIQuery.OPERATION_NAME.name(),
                "Today KPI",
                mapQueryFilters(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    storeListValue,
                    StoreTodayKPIQuery.QUERY_DOCUMENT
                )
            )

            val response = try {
                apolloClient(requireContext()).query(StoreTodayKPIQuery(StorePrefData.StoreIdFromLogin)).await()
            } catch (e: ApolloException) {
                progressDialogKpiToday.dismissProgressDialog()
                refreshTokenKpiToday()
                return@launchWhenResumed
            }
            if (response.data?.generalManager != null) {
                progressDialogKpiToday.dismissProgressDialog()
                storeDetailsTodayKpi = response.data?.generalManager!!

                setDataTodayKpi(storeDetailsTodayKpi.kpis?.store)
            }
        }
    }

    private fun setDataTodayKpi(
        storeDetailTodayKpi: StoreTodayKPIQuery.Store?
    ) {
        val checkInListTodayKpi = mutableListOf<String>()
        val checkInListTodayKpi2 = mutableListOf<String>()

        checkInListTodayKpi.add("11AM")
        checkInListTodayKpi.add("12PM")
        checkInListTodayKpi.add("1PM")
        checkInListTodayKpi.add("2PM")
        checkInListTodayKpi.add("3PM")
        checkInListTodayKpi.add("4PM")
        checkInListTodayKpi.add("5PM")
        checkInListTodayKpi.add("6PM")

        checkInListTodayKpi2.add("7PM")
        checkInListTodayKpi2.add("8PM")
        checkInListTodayKpi2.add("9PM")
        checkInListTodayKpi2.add("10PM")
        checkInListTodayKpi2.add("11PM")
        checkInListTodayKpi2.add("12AM")
        checkInListTodayKpi2.add("1AM")
        checkInListTodayKpi2.add("2AM")
        val adapterTodayKpi = StoreCheckinListAdapter(
            requireActivity(),
            checkInListTodayKpi,
            storeDetailTodayKpi?.today?.checkInHour.toString()
        )
        val adapterTodayKpi2 = StoreCheckinListAdapter2(
            requireActivity(),
            checkInListTodayKpi2,
            storeDetailTodayKpi?.today?.checkInHour.toString()
        )

        check_in_rcv.adapter = adapterTodayKpi
        check_in_rcv2.adapter = adapterTodayKpi2

        val detailTodayKpi = storeDetailTodayKpi?.today

        // store name
        common_header_today.sales_text_common_header.text = getString(R.string.sales_text)
        common_header_today.total_sales_common_header.text =  if (detailTodayKpi?.sales?.actual?.value?.isNaN() == false) Validation().dollarFormatting(detailTodayKpi.sales.actual.value) else ""
        common_header_today.store_headers.text = getString(R.string.today_text)
        common_header_today.store_id.text = storeDetailTodayKpi?.storeName.toString()
        common_header_today.period_range.text = DateFormatterUtil.currentDate()

        //display names
        aws_display_today_kpi.text =
            if (detailTodayKpi?.sales?.displayName != null) detailTodayKpi.sales.displayName else getString(R.string.awus_text)
        labour_display_today_kpi.text =
            if (detailTodayKpi?.labor?.displayName != null) detailTodayKpi.labor.displayName else getString(R.string.labour_text)
        service_display_today_kpi.text =
            if (detailTodayKpi?.service?.displayName != null) detailTodayKpi.service.displayName else getString(R.string.service_text)
        // service breakdown
        eADT_display_today_kpi.text =
            if (detailTodayKpi?.service?.eADT?.displayName != null) detailTodayKpi.service.eADT.displayName else getString(
                R.string.eadt_text)
        extreme_delivery_display_today_kpi.text =
            if (detailTodayKpi?.service?.extremeDelivery?.displayName != null) detailTodayKpi.service.extremeDelivery.displayName else getString(
                R.string.extreme_delivery_text)
        single_display_today_kpi.text =
            if (detailTodayKpi?.service?.singles?.displayName != null) detailTodayKpi.service.singles.displayName else getString(
                R.string.singles_percentage_text)
        //cash and oer display name
        cash_display_today_kpi.text =
            if (detailTodayKpi?.cash?.displayName != null) detailTodayKpi.cash.displayName else getString(R.string.cash_text)
        oer_display_today_kpi.text =
            if (detailTodayKpi?.oerStart?.displayName != null) detailTodayKpi.oerStart.displayName else getString(R.string.oer_text)

        // sales
        sales_goal_today_kpi.text =
            if (detailTodayKpi?.sales?.goal?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(detailTodayKpi.sales.goal.value)
            ) else ""
        sales_variance_today_kpi.text =
            if (detailTodayKpi?.sales?.variance?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(detailTodayKpi.sales.variance.value)
            ) else ""

        if ( detailTodayKpi?.sales?.actual?.value?.isNaN() == false && detailTodayKpi.sales.status?.toString() != null) {
            sales_actual_today_kpi.text = getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(detailTodayKpi.sales.actual.value))
            when {
                detailTodayKpi.sales.status.toString() == resources.getString(R.string.out_of_range) -> {
                    sales_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    sales_actual_today_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                detailTodayKpi.sales.status.toString() == resources.getString(R.string.under_limit) -> {
                    sales_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    sales_actual_today_kpi.setTextColor(requireContext().getColor(R.color.green))

                } else -> {
                    sales_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    sales_actual_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        // labour

        labour_goal_today_kpi.text =
            if (detailTodayKpi?.labor?.goal?.percentage?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detailTodayKpi.labor.goal.percentage)).plus(
                getString(
                    R.string.percentage_text)) else ""
        labour_variance_today_kpi.text =
            if (detailTodayKpi?.labor?.variance?.percentage?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detailTodayKpi.labor.variance.percentage)).plus(
                getString(
                    R.string.percentage_text)) else ""

        if (detailTodayKpi?.labor?.actual?.percentage?.isNaN() == false && detailTodayKpi.labor.status != null) {
            labour_actual_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayKpi.labor.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                detailTodayKpi.labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    labour_actual_today_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                detailTodayKpi.labor.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    labour_actual_today_kpi.setTextColor(requireContext().getColor(R.color.green))

                } else -> {
                    labour_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    labour_actual_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
        service_eADT_goal_today_kpi.text =
            if (detailTodayKpi?.service?.eADT?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayKpi.service.eADT.goal.value) else ""

        service_eADT_variance_today_kpi.text =
            if (detailTodayKpi?.service?.eADT?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayKpi.service.eADT.variance.value) else ""

        if (detailTodayKpi?.service?.eADT?.actual?.value?.isNaN() == false && detailTodayKpi.service.eADT.status != null) {
            service_eADT_actual_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayKpi.service.eADT.actual.value)

            when {
                detailTodayKpi.service.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_eADT_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    service_eADT_actual_today_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                detailTodayKpi.service.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_eADT_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    service_eADT_actual_today_kpi.setTextColor(requireContext().getColor(R.color.green))

                } else -> {
                    service_eADT_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    service_eADT_actual_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        // service extreme
        service_extreme_goal_today_kpi.text =
            if (detailTodayKpi?.service?.extremeDelivery?.goal?.value?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detailTodayKpi.service.extremeDelivery.goal.value)).plus(
                getString(
                    R.string.percentage_text)) else ""
        service_extreme_variance_today_kpi.text =
            if (detailTodayKpi?.service?.extremeDelivery?.variance?.value?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detailTodayKpi.service.extremeDelivery.variance.value)).plus(
                getString(
                    R.string.percentage_text)) else ""

        if (detailTodayKpi?.service?.extremeDelivery?.actual?.value?.isNaN() == false && detailTodayKpi.service.extremeDelivery.status != null) {
            service_extreme_actual_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayKpi.service.extremeDelivery.actual.value).plus(getString(R.string.percentage_text))

            when {
                detailTodayKpi.service.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    service_extreme_actual_today_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                detailTodayKpi.service.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_extreme_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    service_extreme_actual_today_kpi.setTextColor(requireContext().getColor(R.color.green))
                } else -> {
                    service_extreme_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    service_extreme_actual_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }


        // service singles
        service_singles_goal_today_kpi.text =
            if (detailTodayKpi?.service?.singles?.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayKpi.service.singles.goal.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        service_singles_variance_today_kpi.text =
            if (detailTodayKpi?.service?.singles?.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayKpi.service.singles.variance.percentage)
                .plus(getString(R.string.percentage_text)) else ""

        if (detailTodayKpi?.service?.singles?.actual?.percentage?.isNaN() == false && detailTodayKpi.service.singles.status != null) {
            service_singles_actual_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayKpi.service.singles.actual.percentage)
                    .plus(getString(R.string.percentage_text))

            when {
                detailTodayKpi.service.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    service_singles_actual_today_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                detailTodayKpi.service.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_singles_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    service_singles_actual_today_kpi.setTextColor(requireContext().getColor(R.color.green))

                } else -> {
                    service_singles_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    service_singles_actual_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        // Cash

        cash_goal_today_kpi.text =
            if (detailTodayKpi?.cash?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayKpi.cash.goal.value) else ""
        cash_variance_today_kpi.text =
            if (detailTodayKpi?.cash?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayKpi.cash.variance.value) else ""

        if (detailTodayKpi?.cash?.actual?.value?.isNaN() == false && detailTodayKpi.cash.status != null) {
            cash_actual_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayKpi.cash.actual.value)
            when {
                detailTodayKpi.cash.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    cash_actual_today_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                detailTodayKpi.cash.status.toString() == resources.getString(R.string.under_limit) -> {
                    run {
                        cash_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        cash_actual_today_kpi.setTextColor(requireContext().getColor(R.color.green))
                    }
                }
                else -> {
                    cash_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    cash_actual_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }

            // Oer
            oer_goal_today_kpi.text =
                if (detailTodayKpi?.oerStart?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    detailTodayKpi.oerStart.goal.value) else ""
            oer_variance_today_kpi.text =
                if (detailTodayKpi?.oerStart?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    detailTodayKpi.oerStart.variance.value) else ""

            if (detailTodayKpi?.oerStart?.actual?.value?.isNaN() == false && detailTodayKpi.oerStart.status != null) {
                oer_actual_today_kpi.text =
                    Validation().ignoreZeroAfterDecimal(detailTodayKpi.oerStart.actual.value)
                when {
                    detailTodayKpi.oerStart.status.toString() == resources.getString(R.string.out_of_range) -> {
                        oer_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(0,
                            0,
                            R.drawable.red_circle,
                            0)
                        oer_actual_today_kpi.setTextColor(requireContext().getColor(R.color.red))

                    }
                    detailTodayKpi.oerStart.status.toString() == resources.getString(R.string.under_limit) -> {
                        oer_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(0,
                            0,
                            R.drawable.green_circle,
                            0)
                        oer_actual_today_kpi.setTextColor(requireContext().getColor(R.color.green))
                    } else -> {
                        oer_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(0,
                            0,
                            R.drawable.black_circle,
                            0)
                        oer_actual_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                    }
                }
            }

    }

    private fun openSalesDetailKpiToday(dataSalesDetailKpiToday: StoreTodayKPIQuery.GeneralManager) {
        val gsonSalesDetailKpiToday = Gson()
        val intentSalesDetailKpiToday = Intent(requireContext(), AWUSKpiActivity::class.java)
        intentSalesDetailKpiToday.putExtra("awus_data", gsonSalesDetailKpiToday.toJson(dataSalesDetailKpiToday))
        intentSalesDetailKpiToday.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentSalesDetailKpiToday)
    }

    private fun openLabourDetailKpiToday(dataLabourDetailKpiToday: StoreTodayKPIQuery.GeneralManager) {
        val gsonLabourDetailKpiToday = Gson()
        val intentLabourDetailKpiToday = Intent(requireContext(), LabourKpiActivity::class.java)
        intentLabourDetailKpiToday.putExtra("labour_data", gsonLabourDetailKpiToday.toJson(dataLabourDetailKpiToday))
        intentLabourDetailKpiToday.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentLabourDetailKpiToday)
    }

    private fun openServiceDetailKpiToday(dataServiceDetailKpiToday: StoreTodayKPIQuery.GeneralManager) {
        val gsonServiceDetailKpiToday = Gson()
        val intentServiceDetailKpiToday = Intent(requireContext(), ServiceKpiActivity::class.java)
        intentServiceDetailKpiToday.putExtra("service_data", gsonServiceDetailKpiToday.toJson(dataServiceDetailKpiToday))
        intentServiceDetailKpiToday.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentServiceDetailKpiToday)
    }

    private fun openOERDetailKpiToday(dataOERDetailKpiToday: StoreTodayKPIQuery.GeneralManager) {
        val gsonOERDetailKpiToday = Gson()
        val intentOERDetailKpiToday = Intent(requireContext(), OERStartActivity::class.java)
        intentOERDetailKpiToday.putExtra("oer_data", gsonOERDetailKpiToday.toJson(dataOERDetailKpiToday))
        intentOERDetailKpiToday.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentOERDetailKpiToday)
    }

    private fun openCASHDetailKpiToday(dataCASHDetailKpiToday: StoreTodayKPIQuery.GeneralManager) {
        val gsonCASHDetailKpiToday = Gson()
        val intentCASHDetailKpiToday = Intent(requireContext(), CashKpiActivity::class.java)
        intentCASHDetailKpiToday.putExtra("cash_data", gsonCASHDetailKpiToday.toJson(dataCASHDetailKpiToday))
        intentCASHDetailKpiToday.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentCASHDetailKpiToday)
    }

    private fun openFilterKpiToday() {
        val intentFilterKpiToday = Intent(requireContext(), FilterActivity::class.java)
        startActivity(intentFilterKpiToday)
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.aws_parent_today_kpi -> {
                if (this::storeDetailsTodayKpi.isInitialized) {
                    openSalesDetailKpiToday(storeDetailsTodayKpi)
                }
            }
            R.id.labour_parent_today_kpi -> {
                if (this::storeDetailsTodayKpi.isInitialized) {
                    openLabourDetailKpiToday(storeDetailsTodayKpi)
                }
            }
            R.id.service_parent_today_kpi -> {
                if (this::storeDetailsTodayKpi.isInitialized) {
                    openServiceDetailKpiToday(storeDetailsTodayKpi)
                }
            }
            R.id.oer_parent_today_kpi -> {
                if (this::storeDetailsTodayKpi.isInitialized) {
                    openOERDetailKpiToday(storeDetailsTodayKpi)
                }
            }
            R.id.cash_parent_today_kpi -> {
                if (this::storeDetailsTodayKpi.isInitialized) {
                    openCASHDetailKpiToday(storeDetailsTodayKpi)
                }
            }
            R.id.filter_icon -> {
                openFilterKpiToday()
            }
            R.id.filter_parent -> {
                openFilterKpiToday()
            }
        }
    }

    private fun refreshTokenKpiToday() {
        val progressDialogRefreshTokenKpiToday = CustomProgressDialog(requireActivity())
        progressDialogRefreshTokenKpiToday.showProgressDialog()
        val apiServiceRefreshTokenKpiToday: ApiInterface = ApiClientAuth().getClient()!!.create(ApiInterface::class.java)
        val callRefreshTokenKpiToday = apiServiceRefreshTokenKpiToday.refreshToken(SendRefreshRequest(StorePrefData.refreshToken))
        callRefreshTokenKpiToday.enqueue(object : retrofit2.Callback<LoginSuccess> {
            override fun onResponse(
                call: retrofit2.Call<LoginSuccess>,
                response: retrofit2.Response<LoginSuccess>,
            ) {
                progressDialogRefreshTokenKpiToday.dismissProgressDialog()
                if (response.isSuccessful) {

                    Logger.info("Token Refreshed","Today Refresh token")

                    StorePrefData.token = response.body()!!.authenticationResult.accessToken
                    callKpiTodayApi()
                } else {
                    val gsonKpiToday = Gson()
                    val typeKpiToday = object : TypeToken<LoginFail>() {
                    }.type
                    val errorResponse = gsonKpiToday.fromJson<LoginFail>(
                        response.errorBody()!!.charStream(), typeKpiToday
                    )

                        Logger.error(errorResponse.message,"Today Refresh token")

                    Validation().showMessageToast(requireActivity(), errorResponse.message)
                }


            }

            override fun onFailure(call: retrofit2.Call<LoginSuccess>, t: Throwable) {

                progressDialogRefreshTokenKpiToday.dismissProgressDialog()
                if (networkHelper.isNetworkConnected()) {
                    Logger.error(t.message.toString(),"Today Refresh token")
                }

            }
        })
    }
}