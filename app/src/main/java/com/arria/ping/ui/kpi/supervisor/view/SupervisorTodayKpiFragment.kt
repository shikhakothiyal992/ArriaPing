package com.arria.ping.ui.kpi.supervisor.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.arria.ping.R
import com.arria.ping.adapter.StoreCheckinListAdapter
import com.arria.ping.adapter.StoreCheckinListAdapter2
import com.arria.ping.ui.kpi.supervisor.adapter.SupervisorTodayAdapter
import com.arria.ping.apiclient.ApiClientAuth
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.apollo.apolloClient
import com.arria.ping.log.Logger
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.kpi.MissingDataQuery
import com.arria.ping.kpi.SupervisorDefaultTodayQuery
import com.arria.ping.kpi.SupervisorOverviewTodayQuery
import com.arria.ping.model.LoginFail
import com.arria.ping.model.SendRefreshRequest
import com.arria.ping.model.StoreDetailPojo
import com.arria.ping.util.Validation
import com.arria.ping.model.successLogin.LoginSuccess
import com.arria.ping.util.CustomProgressDialog
import com.arria.ping.util.IpConstants
import com.arria.ping.ui.filter.FilterActivity
import com.arria.ping.ui.kpi.overview.AWUSKpiActivity
import com.arria.ping.ui.kpi.overview.LabourKpiActivity
import com.arria.ping.ui.kpi.overview.OERStartActivity
import com.arria.ping.ui.kpi.overview.ServiceKpiActivity
import com.arria.ping.util.NetworkHelper
import com.arria.ping.util.StorePrefData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.activity_store_filter.*
import kotlinx.android.synthetic.main.ceo_today_fragment_kpi.*
import kotlinx.android.synthetic.main.common_header.view.*
import kotlinx.android.synthetic.main.common_header_ceo.*
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.common_header_ceo.view.sales_text_common_header
import kotlinx.android.synthetic.main.common_header_ceo.view.store_header
import kotlinx.android.synthetic.main.common_header_ceo.view.total_sales_common_header
import kotlinx.android.synthetic.main.individual_supervisor_item.view.*
import kotlinx.android.synthetic.main.supervisor_today_fragment_kpi.*
import kotlinx.android.synthetic.main.supervisor_today_fragment_kpi.check_in_rcv
import kotlinx.android.synthetic.main.supervisor_today_fragment_kpi.check_in_rcv2
import kotlinx.android.synthetic.main.supervisor_today_fragment_kpi.common_header_ceo
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SupervisorTodayKpiFragment : Fragment(), View.OnClickListener {
    lateinit var superVisorTodayKpiDetails: SupervisorDefaultTodayQuery.Supervisor
    private var supervisorTodayAdapter: SupervisorTodayAdapter? = null
    private lateinit var dbHelperSuperVisorToday: DatabaseHelperImpl
    private val gsonSuperVisorToday = Gson()

    @Inject
    lateinit var networkHelper: NetworkHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
         return inflater.inflate(R.layout.supervisor_today_fragment_kpi, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelperSuperVisorToday = DatabaseHelperImpl(DatabaseBuilder.getInstance(requireActivity()))
        initSupervisorTodayKpi()

        if (networkHelper.isNetworkConnected()) {
            checkNullDataSupervisorToday()
            callSupervisorTodayKpiApi()
        } else {
            Validation().showMessageToast(
                requireActivity(),
                resources.getString(R.string.internet_connection)
            )
        }


    }

    private fun initSupervisorTodayKpi() {
        awus_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
        labor_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
        service_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
        oer_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
        cash_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)

        aws_parent_layout_supervisor_today.setOnClickListener(this)
        labor_parent_layout_supervisor_today.setOnClickListener(this)
        service_parent_layout_supervisor_today.setOnClickListener(this)
        oer_parent_layout_supervisor_today.setOnClickListener(this)
        cash_parent_layout_supervisor_today.setOnClickListener(this)

        filter_icon.setOnClickListener(this)
        filter_parent_linear.setOnClickListener(this)

        aws_text_overview_supervisor_today.setOnClickListener(this)
        labour_text_overview_supervisor_today.setOnClickListener(this)
        service_text_overview_supervisor_today.setOnClickListener(this)
        oer_text_overview_supervisor_today.setOnClickListener(this)
        cash_text_overview_supervisor_today.setOnClickListener(this)
    }

    private fun setSupervisorTodayKpiExpandableData(actionSupervisorTodayExpandable: String, rcvSupervisorTodayExpandable: RecyclerView) {
        rcvSupervisorTodayExpandable.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.VERTICAL,
            false
        )
        val storeDetailsSupervisorTodayExpandable = superVisorTodayKpiDetails.kpis!!.individualStores
        val childDataSupervisorTodayExpandable = mutableListOf<StoreDetailPojo>()
        storeDetailsSupervisorTodayExpandable.forEachIndexed { _, item ->
            when (actionSupervisorTodayExpandable) {
                requireActivity().getString(R.string.awus_text) -> {
                    childDataSupervisorTodayExpandable.add(
                        StoreDetailPojo(item!!.storeNumber.toString(),
                        item.today!!.sales!!.goal?.value.toString(),
                        item.today.sales!!.variance?.value.toString(),
                        item.today.sales.actual?.value.toString(),
                        item.today.sales.status.toString())
                    )
                }
                requireActivity().getString(R.string.labour_vs_goal_text) -> {
                    childDataSupervisorTodayExpandable.add(
                        StoreDetailPojo(item!!.storeNumber.toString(),
                        item.today!!.labor!!.goal?.value.toString(),
                        item.today.labor!!.variance?.value.toString(),
                        item.today.labor.actual?.value.toString(),
                        item.today.labor.status.toString())
                    )
                }
                requireActivity().getString(R.string.cash_text) -> {
                    childDataSupervisorTodayExpandable.add(
                        StoreDetailPojo(item!!.storeNumber.toString(),
                        item.today!!.cash!!.goal?.value.toString(),
                        item.today.cash!!.variance?.value.toString(),
                        item.today.cash.actual?.value.toString(),
                        item.today.cash.status.toString())
                    )
                }
                requireActivity().getString(R.string.oer_text) -> {
                    childDataSupervisorTodayExpandable.add(
                        StoreDetailPojo(item!!.storeNumber.toString(),
                        item.today!!.oerStart!!.goal?.value.toString(),
                        item.today.oerStart!!.variance?.value.toString(),
                        item.today.oerStart.actual?.value.toString(),
                        item.today.oerStart.status.toString())
                    )
                }
            }
        }
        if (childDataSupervisorTodayExpandable.size!= 0 && childDataSupervisorTodayExpandable.size < 3) {
            childDataSupervisorTodayExpandable.removeAt(0)
        }
        supervisorTodayAdapter = SupervisorTodayAdapter(requireContext(), childDataSupervisorTodayExpandable, actionSupervisorTodayExpandable, "")
        rcvSupervisorTodayExpandable.adapter = supervisorTodayAdapter

        supervisorTodayAdapter?.setOnItemClickListener(object :
                                                               SupervisorTodayAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, storeNumber: String?, action: String) {
                callSupervisorTodayOverViewKpiApi(action, storeNumber!!)
            }

        })
    }

    private fun checkNullDataSupervisorToday(){

        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val supervisorNumber = mutableListOf("1")
            val storeNumber = dbHelperSuperVisorToday.getAllSelectedStoreList(true)

            val responseCEOYesterdayKpi = try {
                apolloClient(requireContext()).query(
                    MissingDataQuery(
                        supervisorNumber.toInput(),
                        storeNumber.toInput()
                    )
                ).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                return@launchWhenResumed
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callSupervisorTodayKpiApi() {
        val progressDialog = CustomProgressDialog(requireActivity())
        progressDialog.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val supervisorNumber = mutableListOf("1")
            val storeNumber = dbHelperSuperVisorToday.getAllSelectedStoreList(true)


            Logger.info(
                SupervisorDefaultTodayQuery.OPERATION_NAME.name(),
                "Today KPI",
                mapQueryFilters(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    supervisorNumber,
                    storeNumber,
                    SupervisorDefaultTodayQuery.QUERY_DOCUMENT
                )
            )



            val response = try {
                apolloClient(requireContext()).query(
                    SupervisorDefaultTodayQuery(
                        supervisorNumber.toInput(),
                        storeNumber.toInput()
                    )
                ).await()
            } catch (e: ApolloException) {
                progressDialog.dismissProgressDialog()
                refreshSupervisorTodayKpiToken()
                return@launchWhenResumed
            }
            if (response.data?.supervisor != null) {

                progressDialog.dismissProgressDialog()
                superVisorTodayKpiDetails = response.data?.supervisor!!

                if(!response.data?.supervisor?.kpis?.individualStores.isNullOrEmpty()){
                    setSupervisorTodayKpiData(response.data?.supervisor?.kpis?.stores?.today)
                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setSupervisorTodayKpiData(
        detail: SupervisorDefaultTodayQuery.Today1?,
    ) {
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
        val supervisorTodayCheckInList = mutableListOf<String>()
        val supervisorTodayCheckInList2 = mutableListOf<String>()

        supervisorTodayCheckInList.add("11AM")
        supervisorTodayCheckInList.add("12PM")
        supervisorTodayCheckInList.add("1PM")
        supervisorTodayCheckInList.add("2PM")
        supervisorTodayCheckInList.add("3PM")
        supervisorTodayCheckInList.add("4PM")
        supervisorTodayCheckInList.add("5PM")
        supervisorTodayCheckInList.add("6PM")

        supervisorTodayCheckInList2.add("7PM")
        supervisorTodayCheckInList2.add("8PM")
        supervisorTodayCheckInList2.add("9PM")
        supervisorTodayCheckInList2.add("10PM")
        supervisorTodayCheckInList2.add("11PM")
        supervisorTodayCheckInList2.add("12AM")
        supervisorTodayCheckInList2.add("1AM")
        supervisorTodayCheckInList2.add("2AM")


        val adapter = StoreCheckinListAdapter(
            requireActivity(),
            supervisorTodayCheckInList,
            "4"
        )
        val adapter2 = StoreCheckinListAdapter2(
            requireActivity(),
            supervisorTodayCheckInList2,
            ""
        )

        check_in_rcv.adapter = adapter
        check_in_rcv2.adapter = adapter2

        val periodText: String?
        periodText = if (StorePrefData.isSelectedPeriod.isEmpty()) {
            StorePrefData.isSelectedDate+" | "+ getString(R.string.today_text)
        } else {
            StorePrefData.isSelectedDate+" | "+ StorePrefData.isSelectedPeriod
        }
        Validation().validateFilterKPI(requireActivity(), dbHelperSuperVisorToday, common_header_ceo.store_header, periodText)
        common_header_ceo.sales_text_common_header.text = "SALES"
        common_header_ceo.total_sales_common_header.text =
            if (detail?.sales?.actual?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(detail.sales.actual.value)) else ""

        common_header_ceo.total_sales_common_header.text =
            if (detail?.sales?.actual?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(detail.sales.actual.value)) else ""

        //display names
        awus_display_supervisor_today.text =
            if (detail?.sales?.displayName != null) detail.sales.displayName else getString(R.string.awus_text)
        labour_display_supervisor_today.text =
            if (detail?.labor?.displayName != null) detail.labor.displayName else getString(R.string.labour_text)
        service_display_supervisor_today.text =
            if (detail?.service?.displayName != null) detail.service.displayName else getString(R.string.service_text)
        // service breakdown
        eadt_display_supervisor_today.text =
            if (detail?.service?.eADT?.displayName != null) detail.service.eADT.displayName else getString(
                R.string.eadt_text)
        extreme_delivery_display_supervisor_today.text =
            if (detail?.service?.extremeDelivery?.displayName != null) detail.service.extremeDelivery.displayName else getString(
                R.string.extreme_delivery_text)
        single_display_supervisor_today.text =
            if (detail?.service?.singles?.displayName != null) detail.service.singles.displayName else getString(
                R.string.singles_percentage_text)

        //cash and oer display name
        cash_display_supervisor_today.text =
            if (detail?.cash?.displayName != null) detail.cash.displayName else getString(R.string.cash_text)
        oer_display_supervisor_today.text =
            if (detail?.oerStart?.displayName != null) detail.oerStart.displayName else getString(R.string.oer_text)


        // sales
        sales_goal_supervisor_today.text =
            if (detail?.sales?.goal?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    detail.sales.goal.value)) else ""
        sales_variance_supervisor_today.text =
            if (detail?.sales?.variance?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    detail.sales.variance.value)) else ""
        sales_actual_supervisor_today.text =
            if (detail?.sales?.actual?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    detail.sales.actual.value)) else ""

        if (detail?.sales?.actual?.value?.isNaN() == false && detail.sales.status?.toString() != null) {
            when {
                detail.sales.status.toString() == resources.getString(R.string.out_of_range) -> {
                    sales_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    sales_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.red))

                }
                detail.sales.status.toString() == resources.getString(R.string.under_limit) -> {
                    sales_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    sales_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.green))

                } else -> {
                    sales_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    sales_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
       // labour

        labour_goal_supervisor_today.text =
            if (detail?.labor?.goal?.percentage?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detail.labor.goal.percentage)).plus(
                getString(
                    R.string.percentage_text)) else ""
        labour_variance_supervisor_today.text =
            if (detail?.labor?.variance?.percentage?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detail.labor.variance.percentage)).plus(
                getString(
                    R.string.percentage_text)) else ""

        if (detail?.labor?.actual?.percentage?.isNaN() == false && detail.labor.status != null) {
            labour_actual_supervisor_today.text =
                Validation().ignoreZeroAfterDecimal(detail.labor.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                detail.labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    labour_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.green))

                }
                detail.labor.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    labour_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.green))

                } else -> {
                    labour_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    labour_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }



       service_eadt_goal_supervisor_today.text =
            if (detail?.service?.eADT?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detail.service.eADT.goal.value) else ""

        service_eadt_variance_supervisor_today.text =
            if (detail?.service?.eADT?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detail.service.eADT.variance.value) else ""

        if (detail?.service?.eADT?.actual?.value?.isNaN() == false && detail.service.eADT.status != null) {
            service_eadt_actual_supervisor_today.text =
                Validation().ignoreZeroAfterDecimal(detail.service.eADT.actual.value)

            when {
                detail.service.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_eadt_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    service_eadt_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.red))

                }
                detail.service.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_eadt_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    service_eadt_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.green))

                } else -> {
                    service_eadt_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    service_eadt_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        // service extreme
        service_extreme_goal_supervisor_today.text =
            if (detail?.service?.extremeDelivery?.goal?.value?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detail.service.extremeDelivery.goal.value)).plus(
                getString(
                    R.string.percentage_text)) else ""
        service_extreme_variance_supervisor_today.text =
            if (detail?.service?.extremeDelivery?.variance?.value?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detail.service.extremeDelivery.variance.value)).plus(
                getString(
                    R.string.percentage_text)) else ""

        if (detail?.service?.extremeDelivery?.actual?.value?.isNaN() == false && detail.service.extremeDelivery.status != null) {
            service_extreme_actual_supervisor_today.text =
                Validation().ignoreZeroAfterDecimal(detail.service.extremeDelivery.actual.value).plus(getString(R.string.percentage_text))

            when {
                detail.service.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    service_extreme_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.red))
                }
                detail.service.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_extreme_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    service_extreme_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.green))
                } else -> {
                    service_extreme_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    service_extreme_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }


        // service singles
        service_singles_goal_supervisor_today.text =
            if (detail?.service?.singles?.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detail.service.singles.goal.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        service_singles_variance_supervisor_today.text =
            if (detail?.service?.singles?.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detail.service.singles.variance.percentage)
                .plus(getString(R.string.percentage_text)) else ""

        if (detail?.service?.singles?.actual?.percentage?.isNaN() == false && detail.service.singles.status != null) {
            service_singles_actual_supervisor_today.text =
                Validation().ignoreZeroAfterDecimal(detail.service.singles.actual.percentage)
                    .plus(getString(R.string.percentage_text))

            when {
                detail.service.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    service_singles_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.red))

                }
                detail.service.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_singles_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    service_singles_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.green))

                } else -> {
                    service_singles_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    service_singles_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }


        // Cash

        cash_goal_supervisor_today.text =
            if (detail?.cash?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detail.cash.goal.value) else ""
        cash_variance_supervisor_today.text =
            if (detail?.cash?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detail.cash.variance.value) else ""

        if (detail?.cash?.actual?.value?.isNaN() == false && detail.cash.status != null) {
            cash_actual_supervisor_today.text = Validation().ignoreZeroAfterDecimal(detail.cash.actual.value)
            when {
                detail.cash.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.red_circle, 0)
                    cash_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.red))
                }
                detail.cash.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(0,
                        0,
                        R.drawable.green_circle,
                        0)
                    cash_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.green))
                } else -> {
                    cash_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(0,
                        0,
                        R.drawable.black_circle,
                        0)
                    cash_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }


            // Oer
            oer_goal_supervisor_today.text =
                if (detail.oerStart?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    detail.oerStart.goal.value) else ""
            oer_variance_supervisor_today.text =
                if (detail.oerStart?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    detail.oerStart.variance.value) else ""

            if (detail.oerStart?.actual?.value?.isNaN() == false && detail.oerStart.status != null) {
                oer_actual_supervisor_today.text =
                    Validation().ignoreZeroAfterDecimal(detail.oerStart.actual.value)
                when {
                    detail.oerStart.status.toString() == resources.getString(R.string.out_of_range) -> {
                        oer_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(0,
                            0,
                            R.drawable.red_circle,
                            0)
                        oer_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.red))

                    }
                    detail.oerStart.status.toString() == resources.getString(R.string.under_limit) -> {
                        oer_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(0,
                            0,
                            R.drawable.green_circle,
                            0)
                        oer_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.green))
                    } else -> {
                        oer_actual_supervisor_today.setCompoundDrawablesWithIntrinsicBounds(0,
                            0,
                            R.drawable.black_circle,
                            0)
                        oer_actual_supervisor_today.setTextColor(requireContext().getColor(R.color.text_color))
                    }
                }
            }
        }

    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.aws_parent_layout_supervisor_today -> {

                rcv_labour_supervisor_today.visibility = View.GONE
                rcv_service_supervisor_today.visibility = View.GONE
                rcv_oer_supervisor_today.visibility = View.GONE
                rcv_cash_supervisor_today.visibility = View.GONE

                labour_text_overview_supervisor_today.visibility = View.GONE
                service_text_overview_supervisor_today.visibility = View.GONE
                oer_text_overview_supervisor_today.visibility = View.GONE
                cash_text_overview_supervisor_today.visibility = View.GONE

                labor_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_sales_supervisor_today.visibility == View.VISIBLE) {
                    rcv_sales_supervisor_today.visibility = View.GONE
                    aws_text_overview_supervisor_today.visibility = View.GONE
                    awus_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_sales_supervisor_today.visibility = View.VISIBLE
                    aws_text_overview_supervisor_today.visibility = View.VISIBLE
                    awus_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_up)
                }
                setSupervisorTodayKpiExpandableData(getString(R.string.awus_text), rcv_sales_supervisor_today)
            }
            R.id.labor_parent_layout_supervisor_today -> {
                rcv_sales_supervisor_today.visibility = View.GONE
                rcv_service_supervisor_today.visibility = View.GONE
                rcv_oer_supervisor_today.visibility = View.GONE
                rcv_cash_supervisor_today.visibility = View.GONE

                aws_text_overview_supervisor_today.visibility = View.GONE
                service_text_overview_supervisor_today.visibility = View.GONE
                oer_text_overview_supervisor_today.visibility = View.GONE
                cash_text_overview_supervisor_today.visibility = View.GONE


                awus_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_labour_supervisor_today.visibility == View.VISIBLE) {
                    rcv_labour_supervisor_today.visibility = View.GONE
                    labour_text_overview_supervisor_today.visibility = View.GONE
                    labor_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_labour_supervisor_today.visibility = View.VISIBLE
                    labour_text_overview_supervisor_today.visibility = View.VISIBLE
                    labor_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                setSupervisorTodayKpiExpandableData(getString(R.string.labour_vs_goal_text), rcv_labour_supervisor_today)

            }
            R.id.service_parent_layout_supervisor_today -> {
                rcv_sales_supervisor_today.visibility = View.GONE
                rcv_labour_supervisor_today.visibility = View.GONE
                rcv_oer_supervisor_today.visibility = View.GONE
                rcv_cash_supervisor_today.visibility = View.GONE

                aws_text_overview_supervisor_today.visibility = View.GONE
                labour_text_overview_supervisor_today.visibility = View.GONE
                oer_text_overview_supervisor_today.visibility = View.GONE
                cash_text_overview_supervisor_today.visibility = View.GONE


                awus_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_service_supervisor_today.visibility == View.VISIBLE) {
                    rcv_service_supervisor_today.visibility = View.GONE
                    service_text_overview_supervisor_today.visibility = View.GONE
                    service_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_service_supervisor_today.visibility = View.VISIBLE
                    service_text_overview_supervisor_today.visibility = View.VISIBLE
                    service_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                setSupervisorTodayKpiExpandableData(getString(R.string.service_text), rcv_service_supervisor_today)

            }
            R.id.cash_parent_layout_supervisor_today -> {
                rcv_sales_supervisor_today.visibility = View.GONE
                rcv_labour_supervisor_today.visibility = View.GONE
                rcv_service_supervisor_today.visibility = View.GONE
                rcv_oer_supervisor_today.visibility = View.GONE

                aws_text_overview_supervisor_today.visibility = View.GONE
                labour_text_overview_supervisor_today.visibility = View.GONE
                service_text_overview_supervisor_today.visibility = View.GONE
                oer_text_overview_supervisor_today.visibility = View.GONE


                awus_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_cash_supervisor_today.visibility == View.VISIBLE) {
                    rcv_cash_supervisor_today.visibility = View.GONE
                    cash_text_overview_supervisor_today.visibility = View.GONE
                    cash_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_cash_supervisor_today.visibility = View.VISIBLE
                    cash_text_overview_supervisor_today.visibility = View.VISIBLE
                    cash_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                setSupervisorTodayKpiExpandableData(getString(R.string.cash_text), rcv_cash_supervisor_today)

            }
            R.id.oer_parent_layout_supervisor_today -> {
                rcv_sales_supervisor_today.visibility = View.GONE
                rcv_labour_supervisor_today.visibility = View.GONE
                rcv_service_supervisor_today.visibility = View.GONE
                rcv_cash_supervisor_today.visibility = View.GONE

                aws_text_overview_supervisor_today.visibility = View.GONE
                labour_text_overview_supervisor_today.visibility = View.GONE
                service_text_overview_supervisor_today.visibility = View.GONE
                cash_text_overview_supervisor_today.visibility = View.GONE


                awus_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_text_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_oer_supervisor_today.visibility == View.VISIBLE) {
                    rcv_oer_supervisor_today.visibility = View.GONE
                    oer_text_overview_supervisor_today.visibility = View.GONE
                    oer_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_oer_supervisor_today.visibility = View.VISIBLE
                    oer_text_overview_supervisor_today.visibility = View.VISIBLE
                    oer_parent_img_supervisor_today.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                setSupervisorTodayKpiExpandableData(getString(R.string.oer_text), rcv_oer_supervisor_today)

            }

            R.id.filter_icon -> {
                openSupervisorTodayKpiFilter()
            }
            R.id.filter_parent_linear ->{
                openSupervisorTodayKpiFilter()
            }

            R.id.aws_text_overview_supervisor_today -> {
                callSupervisorTodayOverViewKpiApi(getString(R.string.awus_text), "")
            }
            R.id.labour_text_overview_supervisor_today -> {
                callSupervisorTodayOverViewKpiApi(getString(R.string.labour_text), "")
            }
            R.id.service_text_overview_supervisor_today -> {
                callSupervisorTodayOverViewKpiApi(getString(R.string.service_text), "")
            }
            R.id.oer_text_overview_supervisor_today -> {
                callSupervisorTodayOverViewKpiApi(getString(R.string.oer_text), "")
            }
            R.id.cash_text_overview_supervisor_today -> {
                callSupervisorTodayOverViewKpiApi(getString(R.string.cash_text), "")
            }
        }
    }

    private fun callSupervisorTodayOverViewKpiApi(actionSuperVisorTodayKpi: String, storeNumberSuperVisorTodayKpi: String) {
        val storeNumberListSuperVisorTodayKpi = mutableListOf<String>()
        storeNumberListSuperVisorTodayKpi.add(storeNumberSuperVisorTodayKpi)
        val progressDialogSuperVisorTodayKpi = CustomProgressDialog(requireActivity())
        progressDialogSuperVisorTodayKpi.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val response = try {
                apolloClient(requireContext()).query(SupervisorOverviewTodayQuery(storeNumberListSuperVisorTodayKpi.toInput()))
                    .await()
            } catch (e: ApolloException) {
                progressDialogSuperVisorTodayKpi.dismissProgressDialog()
                return@launchWhenResumed
            }
            if (response.data?.supervisor != null) {
                progressDialogSuperVisorTodayKpi.dismissProgressDialog()
                when (actionSuperVisorTodayKpi) {
                    getString(R.string.awus_text) -> {
                        openSupervisorTodayKpiSalesDetail(response.data?.supervisor!!)
                    }
                    getString(R.string.labour_text) -> {
                        openSupervisorTodayKpiLabourDetail(response.data?.supervisor!!)
                    }
                    getString(R.string.service_text) -> {
                        openSupervisorTodayKpiServiceDetail(response.data?.supervisor!!)
                    }
                    getString(R.string.oer_text) -> {
                        openSupervisorTodayKpiOERDetail(response.data?.supervisor!!)
                    }

                }
            }
        }
    }


    private fun openSupervisorTodayKpiFilter() {
        val intentSupervisorTodayKpiFilter = Intent(requireContext(), FilterActivity::class.java)
        startActivity(intentSupervisorTodayKpiFilter)
    }

    private fun openSupervisorTodayKpiSalesDetail(supervisorSalesDetails: SupervisorOverviewTodayQuery.Supervisor) {
        val intentSupervisorSalesDetails = Intent(requireContext(), AWUSKpiActivity::class.java)
        intentSupervisorSalesDetails.putExtra("awus_data", gsonSuperVisorToday.toJson(supervisorSalesDetails))
        intentSupervisorSalesDetails.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentSupervisorSalesDetails)

    }

    private fun openSupervisorTodayKpiLabourDetail(supervisorLabourDetail: SupervisorOverviewTodayQuery.Supervisor) {
        val intentSupervisorLabourDetail = Intent(requireContext(), LabourKpiActivity::class.java)
        intentSupervisorLabourDetail.putExtra("labour_data", gsonSuperVisorToday.toJson(supervisorLabourDetail))
        intentSupervisorLabourDetail.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentSupervisorLabourDetail)
    }

    private fun openSupervisorTodayKpiServiceDetail(supervisorServiceDetail: SupervisorOverviewTodayQuery.Supervisor) {
        val intentSupervisorServiceDetail = Intent(requireContext(), ServiceKpiActivity::class.java)
        intentSupervisorServiceDetail.putExtra("service_data", gsonSuperVisorToday.toJson(supervisorServiceDetail))
        intentSupervisorServiceDetail.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentSupervisorServiceDetail)
    }

    private fun openSupervisorTodayKpiOERDetail(supervisorOERDetail: SupervisorOverviewTodayQuery.Supervisor) {
        val intentSupervisorOERDetail = Intent(requireContext(), OERStartActivity::class.java)
        intentSupervisorOERDetail.putExtra("oer_data", gsonSuperVisorToday.toJson(supervisorOERDetail))
        intentSupervisorOERDetail.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentSupervisorOERDetail)
    }

       private fun refreshSupervisorTodayKpiToken() {
        val progressDialogRefreshSupervisorToday = CustomProgressDialog(requireActivity())
        progressDialogRefreshSupervisorToday.showProgressDialog()
        val apiServiceRefreshSupervisorToday: ApiInterface = ApiClientAuth().getClient()!!.create(ApiInterface::class.java)
        val callRefreshSupervisorToday = apiServiceRefreshSupervisorToday.refreshToken(SendRefreshRequest(
            StorePrefData.refreshToken))
        callRefreshSupervisorToday.enqueue(object : retrofit2.Callback<LoginSuccess> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                callRefreshSupervisorToday: retrofit2.Call<LoginSuccess>,
                responseRefreshSupervisorToday: retrofit2.Response<LoginSuccess>,
            ) {
                progressDialogRefreshSupervisorToday.dismissProgressDialog()
                if (responseRefreshSupervisorToday.isSuccessful) {

                    Logger.info("Token Refreshed","Today KPI")


                    StorePrefData.token = responseRefreshSupervisorToday.body()!!.authenticationResult.accessToken
                    callSupervisorTodayKpiApi()
                } else {
                    val gsonRefreshSupervisorToday = Gson()
                    val typeRefreshSupervisorToday = object : TypeToken<LoginFail>() {
                    }.type
                    val errorResponseRefreshSupervisorToday = gsonRefreshSupervisorToday.fromJson<LoginFail>(
                        responseRefreshSupervisorToday.errorBody()!!.charStream(), typeRefreshSupervisorToday
                    )
                        Logger.error(errorResponseRefreshSupervisorToday.message,"Today Refresh Token")

                    Validation().showMessageToast(requireActivity(), errorResponseRefreshSupervisorToday.message)
                }


            }

            override fun onFailure(call: retrofit2.Call<LoginSuccess>, t: Throwable) {

                progressDialogRefreshSupervisorToday.dismissProgressDialog()
                if (networkHelper.isNetworkConnected()) {
                    Logger.error(t.message.toString(),"Today Refresh Token")
                }
            }
        })
    }

}
