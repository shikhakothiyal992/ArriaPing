package com.arria.ping.ui.kpi.do_.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.arria.ping.R
import com.arria.ping.ui.kpi.do_.adapter.CustomExpandableListAdapterTodayDO
import com.arria.ping.adapter.StoreCheckinListAdapter
import com.arria.ping.adapter.StoreCheckinListAdapter2
import com.arria.ping.apiclient.ApiClientAuth
import com.arria.ping.apiclient.ApiInterface
import com.arria.ping.apollo.apolloClient
import com.arria.ping.log.Logger
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.kpi.*
import com.arria.ping.model.LoginFail
import com.arria.ping.model.SendRefreshRequest
import com.arria.ping.model.StoreDetailPojo
import com.arria.ping.model.successLogin.LoginSuccess
import com.arria.ping.ui.filter.FilterActivity
import com.arria.ping.ui.kpi.overview.AWUSKpiActivity
import com.arria.ping.ui.kpi.overview.LabourKpiActivity
import com.arria.ping.ui.kpi.overview.OERStartActivity
import com.arria.ping.ui.kpi.overview.ServiceKpiActivity
import com.arria.ping.util.NetworkHelper
import com.arria.ping.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.ceo_today_fragment_kpi.*
import kotlinx.android.synthetic.main.ceo_today_fragment_kpi.common_header_ceo
import kotlinx.android.synthetic.main.ceo_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.common_header.view.*
import kotlinx.android.synthetic.main.common_header_ceo.*
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.common_header_ceo.view.sales_text_common_header
import kotlinx.android.synthetic.main.common_header_ceo.view.store_header
import kotlinx.android.synthetic.main.common_header_ceo.view.total_sales_common_header
import kotlinx.android.synthetic.main.do_today_fragment_kpi.*
import kotlinx.android.synthetic.main.individual_supervisor_item.view.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DOTodayKpiFragment : Fragment(), View.OnClickListener {
    lateinit var superVisorDetailsTodayDOKpi: DODefaultTodayQuery.Do_
    private var lastExpandedPositionTodayDOKpi = -1
    private val expandableListDetailTodayDOKpi = HashMap<String, List<StoreDetailPojo>>()
    private var expandableListAdapterTodayDOKpi: CustomExpandableListAdapterTodayDO? = null
    private lateinit var dbHelperTodayDOKpi: DatabaseHelperImpl
    private val gsonTodayDOKpi = Gson()

    @Inject
    lateinit var networkHelper: NetworkHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.do_today_fragment_kpi, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelperTodayDOKpi = DatabaseHelperImpl(DatabaseBuilder.getInstance(requireActivity()))
        initTodayDOKpi()

        if (networkHelper.isNetworkConnected()) {
            checkNullDataValueDoToday()
            callDoTodayKpiApi()
        } else {
            Validation().showMessageToast(
                requireActivity(),
                resources.getString(R.string.internet_connection)
            )
        }


    }

    private fun initTodayDOKpi() {
        aws_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        labor_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        service_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        oer_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        cash_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

        aws_parent_layout_do_today_kpi.setOnClickListener(this)
        labor_parent_layout_do_today_kpi.setOnClickListener(this)
        service_parent_layout_do_today_kpi.setOnClickListener(this)
        oer_parent_layout_do_today_kpi.setOnClickListener(this)
        cash_parent_layout_do_today_kpi.setOnClickListener(this)

        filter_icon.setOnClickListener(this)
        filter_parent_linear.setOnClickListener(this)

        aws_text_overview_do_today_kpi.setOnClickListener(this)
        labour_text_overview_do_today_kpi.setOnClickListener(this)
        service_text_overview_do_today_kpi.setOnClickListener(this)
        oer_text_overview_do_today_kpi.setOnClickListener(this)
        cash_text_overview_do_today_kpi.setOnClickListener(this)
    }

    private fun setDoTodayExpandableData(actionDo: String, rcvDo: NonScrollExpandableListView) {
        val childDataDo: LinkedList<StoreDetailPojo> = LinkedList()
        superVisorDetailsTodayDOKpi.kpis!!.individualSupervisors.forEachIndexed { i, item ->
            expandableListDetailTodayDOKpi[item!!.supervisorName!!] = childDataDo

            Log.e("DO today",i.toString())
        }
        val titleListDo = LinkedList(expandableListDetailTodayDOKpi.keys)

        expandableListAdapterTodayDOKpi = CustomExpandableListAdapterTodayDO(
            requireContext(),
            titleListDo as LinkedList<String>,
            expandableListDetailTodayDOKpi, superVisorDetailsTodayDOKpi, actionDo)
        rcvDo.setAdapter(expandableListAdapterTodayDOKpi)

        rcvDo.setOnGroupExpandListener { groupPosition ->

            if (rcvDo.isGroupExpanded(groupPosition)) {
                callStoreAgainstSupervisorTodayDO(titleListDo[groupPosition], groupPosition, actionDo)
            } else {
                rcvDo.collapseGroup(groupPosition)
            }

            if (lastExpandedPositionTodayDOKpi != -1 && groupPosition != lastExpandedPositionTodayDOKpi) {
                rcvDo.collapseGroup(lastExpandedPositionTodayDOKpi)
            }
            lastExpandedPositionTodayDOKpi = groupPosition
        }

        rcvDo.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            if (childPosition == 0) {
                val superVisorNumber =
                    superVisorDetailsTodayDOKpi.kpis!!.individualSupervisors[0]!!.supervisorNumber!!
                when {
                    rcv_sales_do_today_kpi.visibility == View.VISIBLE -> {
                        callTodayDoOverViewKpiApi(getString(R.string.awus_text), superVisorNumber, "")
                    }
                    rcv_labour_do_today_kpi.visibility == View.VISIBLE -> {
                        callTodayDoOverViewKpiApi(getString(R.string.labour_text), superVisorNumber, "")
                    }
                    rcv_service_do_today_kpi.visibility == View.VISIBLE -> {
                        callTodayDoOverViewKpiApi(getString(R.string.service_text), superVisorNumber, "")
                    }
                    rcv_oer_do_today_kpi.visibility == View.VISIBLE -> {
                        callTodayDoOverViewKpiApi(getString(R.string.oer_text), superVisorNumber, "")
                    }
                    rcv_cash_do_today_kpi.visibility == View.VISIBLE -> {
                        callTodayDoOverViewKpiApi(getString(R.string.cash_text), superVisorNumber, "")
                    }
                }
            } else {
                val storeNumber =
                    expandableListDetailTodayDOKpi[titleListDo[groupPosition]]!![(childPosition)].storeNumber!!
                val superVisorNumber =
                    superVisorDetailsTodayDOKpi.kpis!!.individualSupervisors[groupPosition]!!.supervisorNumber!!
                when {
                    rcv_sales_do_today_kpi.visibility == View.VISIBLE -> {
                        callTodayDoOverViewKpiApi(getString(R.string.awus_text), superVisorNumber, storeNumber)
                    }
                    rcv_labour_do_today_kpi.visibility == View.VISIBLE -> {
                        callTodayDoOverViewKpiApi(getString(R.string.labour_text),
                            superVisorNumber,
                            storeNumber)
                    }
                    rcv_service_do_today_kpi.visibility == View.VISIBLE -> {
                        callTodayDoOverViewKpiApi(getString(R.string.service_text),
                            superVisorNumber,
                            storeNumber)
                    }
                    rcv_oer_do_today_kpi.visibility == View.VISIBLE -> {
                        callTodayDoOverViewKpiApi(getString(R.string.oer_text), superVisorNumber, storeNumber)
                    }
                    rcv_cash_do_today_kpi.visibility == View.VISIBLE -> {
                        callTodayDoOverViewKpiApi(getString(R.string.cash_text), superVisorNumber, storeNumber)
                    }
                }
            }
            false
        }

    }

    private fun callStoreAgainstSupervisorTodayDO(titleCallStoreSupervisor: String, groupPosition: Int, actionCallStoreSupervisor: String) {

        val storeDetailsTodayDo = superVisorDetailsTodayDOKpi.kpis!!.individualStores
        val childDataTodayDo = LinkedList<StoreDetailPojo>()
        Log.e("DO today",groupPosition.toString())
        childDataTodayDo.add(
            StoreDetailPojo("",
            "",
            "",
            "",
            "")
        )
        storeDetailsTodayDo.forEachIndexed { _, item ->
            when (actionCallStoreSupervisor) {
                requireActivity().getString(R.string.awus_text) -> {
                    childDataTodayDo.add(
                        StoreDetailPojo(item!!.storeNumber.toString(),
                        item.today!!.sales!!.goal?.value.toString(),
                        item.today.sales!!.variance?.value.toString(),
                        item.today.sales.actual?.value.toString(),
                        item.today.sales.status.toString())
                    )

                }
                requireActivity().getString(R.string.labour_vs_goal_text) -> {
                    childDataTodayDo.add(
                        StoreDetailPojo(item!!.storeNumber.toString(),
                        item.today!!.labor!!.goal?.value.toString(),
                        item.today.labor!!.variance?.value.toString(),
                        item.today.labor.actual?.value.toString(),
                        item.today.labor.status.toString())
                    )
                }
                requireActivity().getString(R.string.cash_text) -> {
                    childDataTodayDo.add(
                        StoreDetailPojo(item!!.storeNumber.toString(),
                        item.today!!.cash!!.goal?.value.toString(),
                        item.today.cash!!.variance?.value.toString(),
                        item.today.cash.actual?.value.toString(),
                        item.today.cash.status.toString())
                    )
                }
                requireActivity().getString(R.string.oer_text) -> {
                    childDataTodayDo.add(
                        StoreDetailPojo(item!!.storeNumber.toString(),
                        item.today!!.oerStart!!.goal?.value.toString(),
                        item.today.oerStart!!.variance?.value.toString(),
                        item.today.oerStart.actual?.value.toString(),
                        item.today.oerStart.status.toString())
                    )
                }
            }
        }
        if (childDataTodayDo.size < 3) {
            childDataTodayDo.removeAt(0)
        }
        expandableListDetailTodayDOKpi[titleCallStoreSupervisor] = childDataTodayDo
        expandableListAdapterTodayDOKpi!! setChild (expandableListDetailTodayDOKpi)
    }

    private fun checkNullDataValueDoToday(){

        val progressDialogTodayDOKpi = CustomProgressDialog(requireActivity())
        progressDialogTodayDOKpi.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodeTodayDOKpi = dbHelperTodayDOKpi.getAllSelectedAreaList(true)
            val stateCodeTodayDOKpi = dbHelperTodayDOKpi.getAllSelectedStoreListState(true)
            val supervisorNumberTodayDOKpi = dbHelperTodayDOKpi.getAllSelectedStoreListSupervisor(true)
            val storeNumberTodayDOKpi = dbHelperTodayDOKpi.getAllSelectedStoreList(true)

            val responseTodayDOKpi = try {
                apolloClient(requireContext()).query(
                    MissingDataQuery(
                        areaCodeTodayDOKpi.toInput(),
                        stateCodeTodayDOKpi.toInput(),
                        supervisorNumberTodayDOKpi.toInput(),
                        storeNumberTodayDOKpi.toInput())).await()


            } catch (e: ApolloException) {
                progressDialogTodayDOKpi.dismissProgressDialog()
                return@launchWhenResumed
            }
            Log.e("Do today","$responseTodayDOKpi")
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun callDoTodayKpiApi() {
        val progressDialogTodayDOKpi = CustomProgressDialog(requireActivity())
        progressDialogTodayDOKpi.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodeTodayDOKpi = dbHelperTodayDOKpi.getAllSelectedAreaList(true)
            val stateCodeTodayDOKpi = dbHelperTodayDOKpi.getAllSelectedStoreListState(true)
            val supervisorNumberTodayDOKpi = dbHelperTodayDOKpi.getAllSelectedStoreListSupervisor(true)
            val storeNumberTodayDOKpi = dbHelperTodayDOKpi.getAllSelectedStoreList(true)

            Logger.info(
                DODefaultTodayQuery.OPERATION_NAME.name(),
                "Today KPI",
                mapQueryFilters(
                    areaCodeTodayDOKpi, stateCodeTodayDOKpi, supervisorNumberTodayDOKpi,storeNumberTodayDOKpi,
                    DODefaultTodayQuery.QUERY_DOCUMENT
                )
            )

            val responseTodayDOKpi = try {
                apolloClient(requireContext()).query(DODefaultTodayQuery(areaCodeTodayDOKpi.toInput(),
                    stateCodeTodayDOKpi.toInput(),
                    supervisorNumberTodayDOKpi.toInput(),
                    storeNumberTodayDOKpi.toInput())).await()
            } catch (e: ApolloException) {
                progressDialogTodayDOKpi.dismissProgressDialog()
                refreshDOTodayKpiToken()
                return@launchWhenResumed
            }
            if (responseTodayDOKpi.data?.do_ != null) {
                progressDialogTodayDOKpi.dismissProgressDialog()
                superVisorDetailsTodayDOKpi = responseTodayDOKpi.data?.do_!!
                setTodayDOKpiData(responseTodayDOKpi.data?.do_?.kpis?.supervisors?.stores?.today)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setTodayDOKpiData(
        detailTodayDOKpi: DODefaultTodayQuery.Today2?,
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
        val doTodayKpiCheckInList = mutableListOf<String>()
        val doTodayKpiCheckInList2 = mutableListOf<String>()

        doTodayKpiCheckInList.add("11AM")
        doTodayKpiCheckInList.add("12PM")
        doTodayKpiCheckInList.add("1PM")
        doTodayKpiCheckInList.add("2PM")
        doTodayKpiCheckInList.add("3PM")
        doTodayKpiCheckInList.add("4PM")
        doTodayKpiCheckInList.add("5PM")
        doTodayKpiCheckInList.add("6PM")

        doTodayKpiCheckInList2.add("7PM")
        doTodayKpiCheckInList2.add("8PM")
        doTodayKpiCheckInList2.add("9PM")
        doTodayKpiCheckInList2.add("10PM")
        doTodayKpiCheckInList2.add("11PM")
        doTodayKpiCheckInList2.add("12AM")
        doTodayKpiCheckInList2.add("1AM")
        doTodayKpiCheckInList2.add("2AM")

        val adapter = StoreCheckinListAdapter(
            requireActivity(),
            doTodayKpiCheckInList,
            "4"
        )
        val adapter2 = StoreCheckinListAdapter2(
            requireActivity(),
            doTodayKpiCheckInList2,
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

        Validation().validateFilterKPI(requireActivity(), dbHelperTodayDOKpi, common_header_ceo.store_header, periodText)
        common_header_ceo.total_sales_common_header.text = getString(R.string.twenty_six_days)
        common_header_ceo.sales_text_common_header.text = getString(R.string.sales_text)
        common_header_ceo.total_sales_common_header.text =
            if (detailTodayDOKpi?.sales?.actual?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(detailTodayDOKpi.sales.actual.value)) else ""

        //display names
        aws_display_do_today_kpi.text =
            if (detailTodayDOKpi?.sales?.displayName != null) detailTodayDOKpi.sales.displayName else getString(R.string.awus_text)
        labour_display_do_today_kpi.text =
            if (detailTodayDOKpi?.labor?.displayName != null) detailTodayDOKpi.labor.displayName else getString(R.string.labour_text)
        service_display_do_today_kpi.text =
            if (detailTodayDOKpi?.service?.displayName != null) detailTodayDOKpi.service.displayName else getString(R.string.service_text)
        // service breakdown
        eadt_display_do_today_kpi.text =
            if (detailTodayDOKpi?.service?.eADT?.displayName != null) detailTodayDOKpi.service.eADT.displayName else getString(
                R.string.eadt_text)
        extreme_delivery_display_do_today_kpi.text =
            if (detailTodayDOKpi?.service?.extremeDelivery?.displayName != null) detailTodayDOKpi.service.extremeDelivery.displayName else getString(
                R.string.extreme_delivery_text)
        single_display_do_today_kpi.text =
            if (detailTodayDOKpi?.service?.singles?.displayName != null) detailTodayDOKpi.service.singles.displayName else getString(
                R.string.singles_percentage_text)

        //cash and oer display name
        cash_display_do_today_kpi.text =
            if (detailTodayDOKpi?.cash?.displayName != null) detailTodayDOKpi.cash.displayName else getString(R.string.cash_text)
        oer_display_do_today_kpi.text =
            if (detailTodayDOKpi?.oerStart?.displayName != null) detailTodayDOKpi.oerStart.displayName else getString(R.string.oer_text)

        sales_goal_do_today_kpi.text =
            if (detailTodayDOKpi?.sales?.goal?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    detailTodayDOKpi.sales.goal.value)) else ""
        sales_variance_do_today_kpi.text =
            if (detailTodayDOKpi?.sales?.variance?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    detailTodayDOKpi.sales.variance.value)) else ""
        sales_actual_do_today_kpi.text =
            if (detailTodayDOKpi?.sales?.actual?.value?.isNaN() == false) getString(R.string.dollar_text).plus(
                Validation().dollarFormatting(
                    detailTodayDOKpi.sales.actual.value)) else ""

        if (detailTodayDOKpi?.sales?.actual?.value?.isNaN() == false && detailTodayDOKpi.sales.status?.toString() != null) {
            if (detailTodayDOKpi.sales.status.toString() == resources.getString(R.string.out_of_range)) {
                sales_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.red_circle,
                    0
                )
                sales_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.red))

            } else {
                sales_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.green_circle,
                    0
                )
                sales_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.green))

            }
        }
         // labour

        labour_goal_do_today_kpi.text =
            if (detailTodayDOKpi?.labor?.goal?.percentage?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detailTodayDOKpi.labor.goal.percentage)).plus(
                getString(
                    R.string.percentage_text)) else ""
        labour_variance_do_today_kpi.text =
            if (detailTodayDOKpi?.labor?.variance?.percentage?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detailTodayDOKpi.labor.variance.percentage)).plus(
                getString(
                    R.string.percentage_text)) else ""

        if (detailTodayDOKpi?.labor?.actual?.percentage?.isNaN() == false && detailTodayDOKpi.labor.status != null) {
            labour_actual_do_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayDOKpi.labor.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                detailTodayDOKpi.labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    labour_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                detailTodayDOKpi.labor.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    labour_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    labour_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    labour_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }

       service_eadt_goal_do_today_kpi.text =
            if (detailTodayDOKpi?.service?.eADT?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayDOKpi.service.eADT.goal.value) else ""

        service_eadt_variance_do_today_kpi.text =
            if (detailTodayDOKpi?.service?.eADT?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayDOKpi.service.eADT.variance.value) else ""

        if (detailTodayDOKpi?.service?.eADT?.actual?.value?.isNaN() == false && detailTodayDOKpi.service.eADT.status != null) {
            service_eadt_actual_do_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayDOKpi.service.eADT.actual.value)

            when {
                detailTodayDOKpi.service.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_eadt_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    service_eadt_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                detailTodayDOKpi.service.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_eadt_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    service_eadt_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_eadt_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    service_eadt_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }

        // service extreme
        service_extreme_goal_do_today_kpi.text =
            if (detailTodayDOKpi?.service?.extremeDelivery?.goal?.value?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detailTodayDOKpi.service.extremeDelivery.goal.value)).plus(
                getString(
                    R.string.percentage_text)) else ""
        service_extreme_variance_do_today_kpi.text =
            if (detailTodayDOKpi?.service?.extremeDelivery?.variance?.value?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detailTodayDOKpi.service.extremeDelivery.variance.value)).plus(
                getString(
                    R.string.percentage_text)) else ""

        if (detailTodayDOKpi?.service?.extremeDelivery?.actual?.value?.isNaN() == false && detailTodayDOKpi.service.extremeDelivery.status != null) {
            service_extreme_actual_do_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayDOKpi.service.extremeDelivery.actual.value).plus(getString(R.string.percentage_text))

            when {
                detailTodayDOKpi.service.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    service_extreme_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                detailTodayDOKpi.service.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_extreme_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    service_extreme_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    service_extreme_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    service_extreme_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }


        // service singles
        service_singles_goal_do_today_kpi.text =
            if (detailTodayDOKpi?.service?.singles?.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayDOKpi.service.singles.goal.percentage)
                .plus(getString(R.string.percentage_text)) else ""
        service_singles_variance_do_today_kpi.text =
            if (detailTodayDOKpi?.service?.singles?.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayDOKpi.service.singles.variance.percentage)
                .plus(getString(R.string.percentage_text)) else ""

        if (detailTodayDOKpi?.service?.singles?.actual?.percentage?.isNaN() == false && detailTodayDOKpi.service.singles.status != null) {
            service_singles_actual_do_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayDOKpi.service.singles.actual.percentage)
                    .plus(getString(R.string.percentage_text))

            when {
                detailTodayDOKpi.service.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    service_singles_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                detailTodayDOKpi.service.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_singles_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    service_singles_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_singles_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    service_singles_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }


        // Cash

        cash_goal_do_today_kpi.text =
            if (detailTodayDOKpi?.cash?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayDOKpi.cash.goal.value) else ""
        cash_variance_do_today_kpi.text =
            if (detailTodayDOKpi?.cash?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayDOKpi.cash.variance.value) else ""

        if (detailTodayDOKpi?.cash?.actual?.value?.isNaN() == false && detailTodayDOKpi.cash.status != null) {
            cash_actual_do_today_kpi.text = Validation().ignoreZeroAfterDecimal(detailTodayDOKpi.cash.actual.value)
            when {
                detailTodayDOKpi.cash.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.red_circle, 0)
                    cash_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                detailTodayDOKpi.cash.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(0,
                        0,
                        R.drawable.green_circle,
                        0)
                    cash_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    cash_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(0,
                        0,
                        R.drawable.black_circle,
                        0)
                    cash_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }


            // Oer
            oer_goal_do_today_kpi.text =
                if (detailTodayDOKpi.oerStart?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    detailTodayDOKpi.oerStart.goal.value) else ""
            oer_variance_do_today_kpi.text =
                if (detailTodayDOKpi.oerStart?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    detailTodayDOKpi.oerStart.variance.value) else ""

            if (detailTodayDOKpi.oerStart?.actual?.value?.isNaN() == false && detailTodayDOKpi.oerStart.status != null) {
                oer_actual_do_today_kpi.text =
                    Validation().ignoreZeroAfterDecimal(detailTodayDOKpi.oerStart.actual.value)
                when {
                    detailTodayDOKpi.oerStart.status.toString() == resources.getString(R.string.out_of_range) -> {
                        oer_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(0,
                            0,
                            R.drawable.red_circle,
                            0)
                        oer_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.red))

                    }
                    detailTodayDOKpi.oerStart.status.toString() == resources.getString(R.string.under_limit) -> {
                        oer_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(0,
                            0,
                            R.drawable.green_circle,
                            0)
                        oer_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.green))
                    }
                    else -> {
                        oer_actual_do_today_kpi.setCompoundDrawablesWithIntrinsicBounds(0,
                            0,
                            R.drawable.black_circle,
                            0)
                        oer_actual_do_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                    }
                }
            }
        }

    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.aws_parent_layout_do_today_kpi -> {

                rcv_labour_do_today_kpi.visibility = View.GONE
                rcv_service_do_today_kpi.visibility = View.GONE
                rcv_oer_do_today_kpi.visibility = View.GONE
                rcv_cash_do_today_kpi.visibility = View.GONE

                labour_text_overview_do_today_kpi.visibility = View.GONE
                service_text_overview_do_today_kpi.visibility = View.GONE
                oer_text_overview_do_today_kpi.visibility = View.GONE
                cash_text_overview_do_today_kpi.visibility = View.GONE

                labor_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_sales_do_today_kpi.visibility == View.VISIBLE) {
                    rcv_sales_do_today_kpi.visibility = View.GONE
                    aws_text_overview_do_today_kpi.visibility = View.GONE
                    aws_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_sales_do_today_kpi.visibility = View.VISIBLE
                    aws_text_overview_do_today_kpi.visibility = View.VISIBLE
                    aws_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_up)
                }
                setDoTodayExpandableData(getString(R.string.awus_text), rcv_sales_do_today_kpi)
            }
            R.id.labor_parent_layout_do_today_kpi -> {
                rcv_sales_do_today_kpi.visibility = View.GONE
                rcv_service_do_today_kpi.visibility = View.GONE
                rcv_oer_do_today_kpi.visibility = View.GONE
                rcv_cash_do_today_kpi.visibility = View.GONE

                aws_text_overview_do_today_kpi.visibility = View.GONE
                service_text_overview_do_today_kpi.visibility = View.GONE
                oer_text_overview_do_today_kpi.visibility = View.GONE
                cash_text_overview_do_today_kpi.visibility = View.GONE


                aws_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_labour_do_today_kpi.visibility == View.VISIBLE) {
                    rcv_labour_do_today_kpi.visibility = View.GONE
                    labour_text_overview_do_today_kpi.visibility = View.GONE
                    labor_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_labour_do_today_kpi.visibility = View.VISIBLE
                    labour_text_overview_do_today_kpi.visibility = View.VISIBLE
                    labor_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                setDoTodayExpandableData(getString(R.string.labour_vs_goal_text), rcv_labour_do_today_kpi)

            }
            R.id.service_parent_layout_do_today_kpi -> {
                rcv_sales_do_today_kpi.visibility = View.GONE
                rcv_labour_do_today_kpi.visibility = View.GONE
                rcv_oer_do_today_kpi.visibility = View.GONE
                rcv_cash_do_today_kpi.visibility = View.GONE

                aws_text_overview_do_today_kpi.visibility = View.GONE
                labour_text_overview_do_today_kpi.visibility = View.GONE
                oer_text_overview_do_today_kpi.visibility = View.GONE
                cash_text_overview_do_today_kpi.visibility = View.GONE


                aws_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_service_do_today_kpi.visibility == View.VISIBLE) {
                    rcv_service_do_today_kpi.visibility = View.GONE
                    service_text_overview_do_today_kpi.visibility = View.GONE
                    service_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_service_do_today_kpi.visibility = View.VISIBLE
                    service_text_overview_do_today_kpi.visibility = View.VISIBLE
                    service_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                setDoTodayExpandableData(getString(R.string.service_text), rcv_service_do_today_kpi)

            }
            R.id.cash_parent_layout_do_today_kpi -> {
                rcv_sales_do_today_kpi.visibility = View.GONE
                rcv_labour_do_today_kpi.visibility = View.GONE
                rcv_service_do_today_kpi.visibility = View.GONE
                rcv_oer_do_today_kpi.visibility = View.GONE

                aws_text_overview_do_today_kpi.visibility = View.GONE
                labour_text_overview_do_today_kpi.visibility = View.GONE
                service_text_overview_do_today_kpi.visibility = View.GONE
                oer_text_overview_do_today_kpi.visibility = View.GONE


                aws_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_cash_do_today_kpi.visibility == View.VISIBLE) {
                    rcv_cash_do_today_kpi.visibility = View.GONE
                    cash_text_overview_do_today_kpi.visibility = View.GONE
                    cash_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_cash_do_today_kpi.visibility = View.VISIBLE
                    cash_text_overview_do_today_kpi.visibility = View.VISIBLE
                    cash_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                setDoTodayExpandableData(getString(R.string.cash_text), rcv_cash_do_today_kpi)

            }
            R.id.oer_parent_layout_do_today_kpi -> {
                rcv_sales_do_today_kpi.visibility = View.GONE
                rcv_labour_do_today_kpi.visibility = View.GONE
                rcv_service_do_today_kpi.visibility = View.GONE
                rcv_cash_do_today_kpi.visibility = View.GONE

                aws_text_overview_do_today_kpi.visibility = View.GONE
                labour_text_overview_do_today_kpi.visibility = View.GONE
                service_text_overview_do_today_kpi.visibility = View.GONE
                cash_text_overview_do_today_kpi.visibility = View.GONE


                aws_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_oer_do_today_kpi.visibility == View.VISIBLE) {
                    rcv_oer_do_today_kpi.visibility = View.GONE
                    oer_text_overview_do_today_kpi.visibility = View.GONE
                    oer_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_oer_do_today_kpi.visibility = View.VISIBLE
                    oer_text_overview_do_today_kpi.visibility = View.VISIBLE
                    oer_parent_img_do_today_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                setDoTodayExpandableData(getString(R.string.oer_text), rcv_oer_do_today_kpi)

            }
            R.id.filter_icon -> {
                openFilter()
            }
            R.id.filter_parent_linear ->{
                openFilter()
            }
            R.id.aws_text_overview_do_today_kpi -> {
                callTodayDoOverViewKpiApi(getString(R.string.awus_text), "", "")
            }

            R.id.labour_text_overview_do_today_kpi -> {
                callTodayDoOverViewKpiApi(getString(R.string.labour_text), "", "")
            }
            R.id.service_text_overview_do_today_kpi -> {
                callTodayDoOverViewKpiApi(getString(R.string.service_text), "", "")
            }
            R.id.oer_text_overview_do_today_kpi -> {
                callTodayDoOverViewKpiApi(getString(R.string.oer_text), "", "")
            }
            R.id.cash_text_overview_do_today_kpi -> {
                callTodayDoOverViewKpiApi(getString(R.string.cash_text), "", "")
            }
        }
    }

    private fun callTodayDoOverViewKpiApi(actionTodayDo: String, superVisorNumberTodayDo: String, storeNumberTodayDo: String) {
        val superVisorNumberListTodayDo = mutableListOf<String>()
        superVisorNumberListTodayDo.add(superVisorNumberTodayDo)
        val storeNumberListTodayDo = mutableListOf<String>()
        storeNumberListTodayDo.add(storeNumberTodayDo)

        val progressDialogTodayDo = CustomProgressDialog(requireActivity())
        progressDialogTodayDo.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val response = try {
                apolloClient(requireContext()).query(DOOverviewTodayQuery(superVisorNumberListTodayDo.toInput(),
                    storeNumberListTodayDo.toInput())).await()
            } catch (e: ApolloException) {
                progressDialogTodayDo.dismissProgressDialog()
                return@launchWhenResumed
            }
            if (response.data?.do_ != null) {
                progressDialogTodayDo.dismissProgressDialog()
                when (actionTodayDo) {
                    getString(R.string.awus_text) -> {
                        openSalesDOTodayKpiDetail(response.data?.do_!!)
                    }
                    getString(R.string.labour_text) -> {
                        openLabourDOTodayKpiDetail(response.data?.do_!!)
                    }
                    getString(R.string.service_text) -> {
                        openServiceDOTodayKpiDetail(response.data?.do_!!)
                    }
                    getString(R.string.oer_text) -> {
                        openOERDOTodayKpiDetail(response.data?.do_!!)
                    }

                }
            }
        }
    }

    private fun openFilter() {
        val intent = Intent(requireContext(), FilterActivity::class.java)
        startActivity(intent)
    }

    private fun openSalesDOTodayKpiDetail(doSalesTodayKpiDetail: DOOverviewTodayQuery.Do_) {
        val intentDoSalesTodayKpiDetail = Intent(requireContext(), AWUSKpiActivity::class.java)
        intentDoSalesTodayKpiDetail.putExtra("awus_data", gsonTodayDOKpi.toJson(doSalesTodayKpiDetail))
        intentDoSalesTodayKpiDetail.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentDoSalesTodayKpiDetail)

    }

    private fun openLabourDOTodayKpiDetail(doLabourTodayKpiDetail: DOOverviewTodayQuery.Do_) {
        val intentDoLabourTodayKpiDetail = Intent(requireContext(), LabourKpiActivity::class.java)
        intentDoLabourTodayKpiDetail.putExtra("labour_data", gsonTodayDOKpi.toJson(doLabourTodayKpiDetail))
        intentDoLabourTodayKpiDetail.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentDoLabourTodayKpiDetail)
    }

    private fun openServiceDOTodayKpiDetail(doServiceTodayKpiDetail: DOOverviewTodayQuery.Do_) {
        val intentDoServiceTodayKpiDetail = Intent(requireContext(), ServiceKpiActivity::class.java)
        intentDoServiceTodayKpiDetail.putExtra("service_data", gsonTodayDOKpi.toJson(doServiceTodayKpiDetail))
        intentDoServiceTodayKpiDetail.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentDoServiceTodayKpiDetail)
    }

    private fun openOERDOTodayKpiDetail(doOERTodayKpiDetail: DOOverviewTodayQuery.Do_) {
        val intentDoOERTodayKpiDetail = Intent(requireContext(), OERStartActivity::class.java)
        intentDoOERTodayKpiDetail.putExtra("oer_data", gsonTodayDOKpi.toJson(doOERTodayKpiDetail))
        intentDoOERTodayKpiDetail.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentDoOERTodayKpiDetail)
    }

    private fun refreshDOTodayKpiToken() {
        val progressDialogDoRefreshTokenTodayKpi = CustomProgressDialog(requireActivity())
        progressDialogDoRefreshTokenTodayKpi.showProgressDialog()
        val apiServiceDoRefreshTokenTodayKpi: ApiInterface = ApiClientAuth().getClient()!!.create(ApiInterface::class.java)
        val callDoRefreshTokenTodayKpi = apiServiceDoRefreshTokenTodayKpi.refreshToken(SendRefreshRequest(StorePrefData.refreshToken))
        callDoRefreshTokenTodayKpi.enqueue(object : retrofit2.Callback<LoginSuccess> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                callDoRefreshTokenTodayKpi: retrofit2.Call<LoginSuccess>,
                responseDoRefreshTokenTodayKpi: retrofit2.Response<LoginSuccess>,
            ) {
                progressDialogDoRefreshTokenTodayKpi.dismissProgressDialog()
                if (responseDoRefreshTokenTodayKpi.isSuccessful) {
                    Logger.info("Token Refreshed","Today Refresh Token")

                    StorePrefData.token = responseDoRefreshTokenTodayKpi.body()!!.authenticationResult.accessToken
                    callDoTodayKpiApi()
                } else {
                    val gsonDoRefreshTokenTodayKpi = Gson()
                    val typeDoRefreshTokenTodayKpi = object : TypeToken<LoginFail>() {
                    }.type
                    val errorResponseDoRefreshTokenTodayKpi = gsonDoRefreshTokenTodayKpi.fromJson<LoginFail>(
                        responseDoRefreshTokenTodayKpi.errorBody()!!.charStream(), typeDoRefreshTokenTodayKpi
                    )
                    Logger.error(errorResponseDoRefreshTokenTodayKpi.message, "Today Refresh Token")

                    Validation().showMessageToast(requireActivity(), errorResponseDoRefreshTokenTodayKpi.message)
                }


            }

            override fun onFailure(call: retrofit2.Call<LoginSuccess>, t: Throwable) {
                progressDialogDoRefreshTokenTodayKpi.dismissProgressDialog()
                if (networkHelper.isNetworkConnected()) {
                    Logger.error(t.message.toString(), "Today Refresh Token")
                }
            }
        })
    }

}