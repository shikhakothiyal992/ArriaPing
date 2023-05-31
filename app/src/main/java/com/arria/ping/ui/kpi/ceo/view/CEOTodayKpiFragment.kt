package com.arria.ping.ui.kpi.ceo.view

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
import com.arria.ping.ui.kpi.ceo.adapter.CustomExpandableListAdapterTodayCEO
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
import kotlinx.android.synthetic.main.ceo_today_fragment_kpi.aws_parent_img_today_kpi
import kotlinx.android.synthetic.main.ceo_today_fragment_kpi.cash_parent_img_today_kpi
import kotlinx.android.synthetic.main.ceo_today_fragment_kpi.common_header_ceo
import kotlinx.android.synthetic.main.ceo_today_fragment_kpi.labor_parent_img_today_kpi
import kotlinx.android.synthetic.main.ceo_today_fragment_kpi.oer_parent_img_today_kpi
import kotlinx.android.synthetic.main.ceo_today_fragment_kpi.rcv_cash_today_kpi
import kotlinx.android.synthetic.main.ceo_today_fragment_kpi.rcv_oer_today_kpi
import kotlinx.android.synthetic.main.ceo_today_fragment_kpi.rcv_sales_today_kpi
import kotlinx.android.synthetic.main.ceo_today_fragment_kpi.rcv_service_today_kpi
import kotlinx.android.synthetic.main.ceo_today_fragment_kpi.service_parent_img_today_kpi
import kotlinx.android.synthetic.main.ceo_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.common_header.view.*
import kotlinx.android.synthetic.main.common_header_ceo.*
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.common_header_ceo.view.sales_text_common_header
import kotlinx.android.synthetic.main.common_header_ceo.view.store_header
import kotlinx.android.synthetic.main.common_header_ceo.view.total_sales_common_header
import kotlinx.android.synthetic.main.individual_supervisor_item.view.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CEOTodayKpiFragment : Fragment(), View.OnClickListener {
    lateinit var superVisorDetailsTodayCEOKpi: CEODefaultTodayQuery.Ceo
    private var lastExpandedPosition = -1
    private val expandableListDetailTodayCEOKpi = HashMap<String, List<StoreDetailPojo>>()
    private var expandableListAdapterTodayCEOKpi: CustomExpandableListAdapterTodayCEO? = null
    private lateinit var dbHelperTodayCEOKpi: DatabaseHelperImpl
    private val gsonTodayCEOKpi = Gson()

    @Inject
    lateinit var networkHelper: NetworkHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.ceo_today_fragment_kpi, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelperTodayCEOKpi = DatabaseHelperImpl(DatabaseBuilder.getInstance(requireActivity()))
        initialise()

        if (networkHelper.isNetworkConnected()) {
            checkNullDataValueCeoToday()
            callTodayCEOKpiApi()
        } else {
            Validation().showMessageToast(
                requireActivity(),
                resources.getString(R.string.internet_connection)
            )
        }


    }

    private fun initialise() {
        aws_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        labor_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        service_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        oer_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        cash_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

        aws_parent_layout_today_kpi.setOnClickListener(this)
        labor_parent_layout_today_kpi.setOnClickListener(this)
        service_parent_layout_today_kpi.setOnClickListener(this)
        oer_parent_layout_today_kpi.setOnClickListener(this)
        cash_parent_layout_today_kpi.setOnClickListener(this)

        filter_icon.setOnClickListener(this)
        filter_parent_linear.setOnClickListener(this)

        aws_text_overview_today_kpi.setOnClickListener(this)
        labour_text_overview_today_kpi.setOnClickListener(this)
        service_text_overview_today_kpi.setOnClickListener(this)
        oer_text_overview_today_kpi.setOnClickListener(this)
        cash_text_overview_today_kpi.setOnClickListener(this)
    }

    private fun setExpandableTodayCEOKpiData(
        actionTodayCEOKpi: String,
        rcvTodayCEOKpi: NonScrollExpandableListView
    ) {

        val childDataTodayCEOKpi: MutableList<StoreDetailPojo> = ArrayList()
        superVisorDetailsTodayCEOKpi.kpis!!.individualSupervisors.forEachIndexed { i, item ->
            expandableListDetailTodayCEOKpi[item!!.supervisorName!!] = childDataTodayCEOKpi

            Log.e("ceo today", i.toString())
        }

        val titleListTodayCEOKpi = ArrayList(expandableListDetailTodayCEOKpi.keys)


        expandableListAdapterTodayCEOKpi = CustomExpandableListAdapterTodayCEO(
            requireContext(),
            titleListTodayCEOKpi as ArrayList<String>,
            expandableListDetailTodayCEOKpi, superVisorDetailsTodayCEOKpi, actionTodayCEOKpi
        )
        rcvTodayCEOKpi.setAdapter(expandableListAdapterTodayCEOKpi)

        rcvTodayCEOKpi.setOnGroupExpandListener { groupPosition ->

            if (rcvTodayCEOKpi.isGroupExpanded(groupPosition))
                callStoreAgainstSupervisorTodayCEOKpi(
                    titleListTodayCEOKpi[groupPosition],
                    groupPosition,
                    actionTodayCEOKpi
                )
            else
                rcvTodayCEOKpi.collapseGroup(groupPosition)

            if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
                rcvTodayCEOKpi.collapseGroup(lastExpandedPosition)
            }
            lastExpandedPosition = groupPosition
        }

        rcvTodayCEOKpi.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            if (childPosition == 0) {
                val superVisorNumber =
                    superVisorDetailsTodayCEOKpi.kpis!!.individualSupervisors[0]!!.supervisorNumber!!
                when {
                    rcv_sales_today_kpi.visibility == View.VISIBLE -> {
                        callOverViewTodayCEOKpiApi(
                            getString(R.string.awus_text),
                            superVisorNumber,
                            ""
                        )
                    }
                    rcv_labour_today_kpi.visibility == View.VISIBLE -> {
                        callOverViewTodayCEOKpiApi(
                            getString(R.string.labour_text),
                            superVisorNumber,
                            ""
                        )
                    }
                    rcv_service_today_kpi.visibility == View.VISIBLE -> {
                        callOverViewTodayCEOKpiApi(
                            getString(R.string.service_text),
                            superVisorNumber,
                            ""
                        )
                    }
                    rcv_oer_today_kpi.visibility == View.VISIBLE -> {
                        callOverViewTodayCEOKpiApi(
                            getString(R.string.oer_text),
                            superVisorNumber,
                            ""
                        )
                    }
                    rcv_cash_today_kpi.visibility == View.VISIBLE -> {
                        callOverViewTodayCEOKpiApi(
                            getString(R.string.cash_text),
                            superVisorNumber,
                            ""
                        )
                    }
                }
            } else {
                val storeNumberTodayCEOKpi =
                    expandableListDetailTodayCEOKpi[titleListTodayCEOKpi[groupPosition]]!![(childPosition)].storeNumber!!
                val superVisorNumber =
                    superVisorDetailsTodayCEOKpi.kpis!!.individualSupervisors[groupPosition]!!.supervisorNumber!!
                when {
                    rcv_sales_today_kpi.visibility == View.VISIBLE -> {
                        callOverViewTodayCEOKpiApi(
                            getString(R.string.awus_text),
                            superVisorNumber,
                            storeNumberTodayCEOKpi
                        )
                    }
                    rcv_labour_today_kpi.visibility == View.VISIBLE -> {
                        callOverViewTodayCEOKpiApi(
                            getString(R.string.labour_text),
                            superVisorNumber,
                            storeNumberTodayCEOKpi
                        )
                    }
                    rcv_service_today_kpi.visibility == View.VISIBLE -> {
                        callOverViewTodayCEOKpiApi(
                            getString(R.string.service_text),
                            superVisorNumber,
                            storeNumberTodayCEOKpi
                        )
                    }
                    rcv_oer_today_kpi.visibility == View.VISIBLE -> {
                        callOverViewTodayCEOKpiApi(
                            getString(R.string.oer_text),
                            superVisorNumber,
                            storeNumberTodayCEOKpi
                        )
                    }
                    rcv_cash_today_kpi.visibility == View.VISIBLE -> {
                        callOverViewTodayCEOKpiApi(
                            getString(R.string.cash_text),
                            superVisorNumber,
                            storeNumberTodayCEOKpi
                        )
                    }
                }
            }
            false
        }

    }


    private fun callStoreAgainstSupervisorTodayCEOKpi(
        titleTodayCEOKpi: String,
        groupPositionTodayCEOKpi: Int,
        actionTodayCEOKpi: String
    ) {

        val storeDetailsTodayCEOKpi = superVisorDetailsTodayCEOKpi.kpis!!.individualStores
        val childDataTodayCEOKpi = mutableListOf<StoreDetailPojo>()
        childDataTodayCEOKpi.add(
            StoreDetailPojo(
                "",
                "",
                "",
                "",
                ""
            )
        )
        Log.e("ceo period","$groupPositionTodayCEOKpi")

        storeDetailsTodayCEOKpi.forEachIndexed { _, item ->
            when (actionTodayCEOKpi) {
                requireActivity().getString(R.string.awus_text) -> {
                    childDataTodayCEOKpi.add(
                        StoreDetailPojo(
                            item!!.storeNumber.toString(),
                            item.today!!.sales!!.goal?.value.toString(),
                            item.today.sales!!.variance?.value.toString(),
                            item.today.sales.actual?.value.toString(),
                            item.today.sales.status.toString()
                        )
                    )
                }
                requireActivity().getString(R.string.labour_vs_goal_text) -> {
                    childDataTodayCEOKpi.add(
                        StoreDetailPojo(
                            item!!.storeNumber.toString(),
                            item.today!!.labor!!.goal?.value.toString(),
                            item.today.labor!!.variance?.value.toString(),
                            item.today.labor.actual?.value.toString(),
                            item.today.labor.status.toString()
                        )
                    )
                }
                requireActivity().getString(R.string.cash_text) -> {
                    childDataTodayCEOKpi.add(
                        StoreDetailPojo(
                            item!!.storeNumber.toString(),
                            item.today!!.cash!!.goal?.value.toString(),
                            item.today.cash!!.variance?.value.toString(),
                            item.today.cash.actual?.value.toString(),
                            item.today.cash.status.toString()
                        )
                    )
                }
                requireActivity().getString(R.string.oer_text) -> {
                    childDataTodayCEOKpi.add(
                        StoreDetailPojo(
                            item!!.storeNumber.toString(),
                            item.today!!.oerStart!!.goal?.value.toString(),
                            item.today.oerStart!!.variance?.value.toString(),
                            item.today.oerStart.actual?.value.toString(),
                            item.today.oerStart.status.toString()
                        )
                    )
                }
            }
        }
        if (childDataTodayCEOKpi.size < 3) {
            childDataTodayCEOKpi.removeAt(0)
        }
        expandableListDetailTodayCEOKpi[titleTodayCEOKpi] = childDataTodayCEOKpi
        expandableListAdapterTodayCEOKpi!! setChild (expandableListDetailTodayCEOKpi)
    }

    private fun checkNullDataValueCeoToday(){

        val progressDialogTodayCEOKpi = CustomProgressDialog(requireActivity())
        progressDialogTodayCEOKpi.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodeTodayCEOKpi = dbHelperTodayCEOKpi.getAllSelectedAreaList(true)
            val stateCodeTodayCEOKpi = dbHelperTodayCEOKpi.getAllSelectedStoreListState(true)
            val supervisorNumberTodayCEOKpi =
                dbHelperTodayCEOKpi.getAllSelectedStoreListSupervisor(true)
            val storeNumberTodayCEOKpi = dbHelperTodayCEOKpi.getAllSelectedStoreList(true)
            val responseMissingDataTodayCEOKpi = try {
                apolloClient(requireContext()).query(
                    MissingDataQuery(
                        areaCodeTodayCEOKpi.toInput(),
                        stateCodeTodayCEOKpi.toInput(),
                        supervisorNumberTodayCEOKpi.toInput(),
                        storeNumberTodayCEOKpi.toInput()
                    )
                ).await()
            } catch (e: ApolloException) {
                progressDialogTodayCEOKpi.dismissProgressDialog()
                return@launchWhenResumed
            }
            if(responseMissingDataTodayCEOKpi.data?.missingData!=null){
                progressDialogTodayCEOKpi.dismissProgressDialog()
            }
            else{
                progressDialogTodayCEOKpi.dismissProgressDialog()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun callTodayCEOKpiApi() {
        val progressDialogTodayCEOKpi = CustomProgressDialog(requireActivity())
        progressDialogTodayCEOKpi.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodeTodayCEOKpi = dbHelperTodayCEOKpi.getAllSelectedAreaList(true)
            val stateCodeTodayCEOKpi = dbHelperTodayCEOKpi.getAllSelectedStoreListState(true)
            val supervisorNumberTodayCEOKpi =
                dbHelperTodayCEOKpi.getAllSelectedStoreListSupervisor(true)
            val storeNumberTodayCEOKpi = dbHelperTodayCEOKpi.getAllSelectedStoreList(true)


            Logger.info(
                CEODefaultTodayQuery.OPERATION_NAME.name(),
                "Today KPI",
                mapQueryFilters(
                    areaCodeTodayCEOKpi, stateCodeTodayCEOKpi, supervisorNumberTodayCEOKpi,storeNumberTodayCEOKpi,
                    CEODefaultTodayQuery.QUERY_DOCUMENT
                )
            )

            val response = try {
                apolloClient(requireContext()).query(
                    CEODefaultTodayQuery(
                        areaCodeTodayCEOKpi.toInput(),
                        stateCodeTodayCEOKpi.toInput(),
                        supervisorNumberTodayCEOKpi.toInput(),
                        storeNumberTodayCEOKpi.toInput()
                    )
                ).await()
            } catch (e: ApolloException) {
                progressDialogTodayCEOKpi.dismissProgressDialog()
                refreshTodayCEOKpiToken()
                return@launchWhenResumed
            }
            if (response.data?.ceo != null) {
                progressDialogTodayCEOKpi.dismissProgressDialog()
                superVisorDetailsTodayCEOKpi = response.data?.ceo!!
                setTodayCEOKpiData(response.data?.ceo?.kpis?.supervisors?.stores?.today)

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setTodayCEOKpiData(
        detailTodayCEOKpi: CEODefaultTodayQuery.Today2?,
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
        val checkInListTodayCEOKpi = mutableListOf<String>()
        val checkInListTodayCEOKpi2 = mutableListOf<String>()

        checkInListTodayCEOKpi.add("11AM")
        checkInListTodayCEOKpi.add("12PM")
        checkInListTodayCEOKpi.add("1PM")
        checkInListTodayCEOKpi.add("2PM")
        checkInListTodayCEOKpi.add("3PM")
        checkInListTodayCEOKpi.add("4PM")
        checkInListTodayCEOKpi.add("5PM")
        checkInListTodayCEOKpi.add("6PM")

        checkInListTodayCEOKpi2.add("7PM")
        checkInListTodayCEOKpi2.add("8PM")
        checkInListTodayCEOKpi2.add("9PM")
        checkInListTodayCEOKpi2.add("10PM")
        checkInListTodayCEOKpi2.add("11PM")
        checkInListTodayCEOKpi2.add("12AM")
        checkInListTodayCEOKpi2.add("1AM")
        checkInListTodayCEOKpi2.add("2AM")

        val adapter = StoreCheckinListAdapter(
            requireActivity(),
            checkInListTodayCEOKpi,
            "4"
        )
        val adapter2 = StoreCheckinListAdapter2(
            requireActivity(),
            checkInListTodayCEOKpi2,
            ""
        )
        check_in_rcv.adapter = adapter
        check_in_rcv2.adapter = adapter2
        val periodText: String?
        periodText = if (StorePrefData.isSelectedPeriod.isEmpty()) {
            StorePrefData.isSelectedDate + " | " + getString(R.string.today_text)
        } else {
            StorePrefData.isSelectedDate + " | " + StorePrefData.isSelectedPeriod
        }
        Validation().validateFilterKPI(
            requireActivity(),
            dbHelperTodayCEOKpi,
            common_header_ceo.store_header,
            periodText
        )


        common_header_ceo.sales_text_common_header.text = getString(R.string.sales_text)
        common_header_ceo.total_sales_common_header.text =
            if (detailTodayCEOKpi?.sales?.actual?.value?.isNaN() == false)
                detailTodayCEOKpi.sales.actual.value.toString() else ""

        common_header_ceo.total_sales_common_header.text =
            if (detailTodayCEOKpi?.sales?.actual?.value?.isNaN() == false)
                Validation().dollarFormatting(detailTodayCEOKpi.sales.actual.value) else ""

        //display names
        aws_display_today_kpi.text =
            if (detailTodayCEOKpi?.sales?.displayName != null) detailTodayCEOKpi.sales.displayName else getString(
                R.string.awus_text
            )
        labour_display_today_kpi.text =
            if (detailTodayCEOKpi?.labor?.displayName != null) detailTodayCEOKpi.labor.displayName else getString(
                R.string.labour_text
            )
        service_display_today_kpi.text =
            if (detailTodayCEOKpi?.service?.displayName != null) detailTodayCEOKpi.service.displayName else getString(
                R.string.service_text
            )
        // service breakdown
        eadt_display_today_kpi.text =
            if (detailTodayCEOKpi?.service?.eADT?.displayName != null) detailTodayCEOKpi.service.eADT.displayName else getString(
                R.string.eadt_text
            )
        extreme_delivery_display_today_kpi.text =
            if (detailTodayCEOKpi?.service?.extremeDelivery?.displayName != null) detailTodayCEOKpi.service.extremeDelivery.displayName else getString(
                R.string.extreme_delivery_text
            )
        single_display_today_kpi.text =
            if (detailTodayCEOKpi?.service?.singles?.displayName != null) detailTodayCEOKpi.service.singles.displayName else getString(
                R.string.singles_percentage_text
            )

        cash_display_today_kpi.text =
            if (detailTodayCEOKpi?.cash?.displayName != null) detailTodayCEOKpi.cash.displayName else getString(
                R.string.cash_text
            )
        oer_display_today_kpi.text =
            if (detailTodayCEOKpi?.oerStart?.displayName != null) detailTodayCEOKpi.oerStart.displayName else getString(
                R.string.oer_text
            )
        // sales
        sales_goal_today_kpi.text = Validation().checkAmountPercentageValue(
            requireActivity(),
            detailTodayCEOKpi?.sales?.goal?.value,
            detailTodayCEOKpi?.sales?.goal?.value,
            detailTodayCEOKpi?.sales?.goal?.value
        )
        sales_variance_today_kpi.text = Validation().checkAmountPercentageValue(
            requireActivity(),
            detailTodayCEOKpi?.sales?.variance?.value,
            detailTodayCEOKpi?.sales?.variance?.value,
            detailTodayCEOKpi?.sales?.variance?.value
        )
        sales_actual_today_kpi.text = Validation().checkAmountPercentageValue(
            requireActivity(),
            detailTodayCEOKpi?.sales?.actual?.value,
            detailTodayCEOKpi?.sales?.actual?.value,
            detailTodayCEOKpi?.sales?.actual?.value
        )


        if (detailTodayCEOKpi?.sales?.status?.toString() != null) {
            when {
                detailTodayCEOKpi.sales.status.toString() == resources.getString(R.string.out_of_range) -> {
                    sales_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    sales_actual_today_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                detailTodayCEOKpi.sales.status.toString() == resources.getString(R.string.under_limit) -> {
                    sales_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    sales_actual_today_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
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
            if (detailTodayCEOKpi?.labor?.goal?.percentage?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detailTodayCEOKpi.labor.goal.percentage
            )).plus(
                getString(
                    R.string.percentage_text
                )
            ) else ""
        labour_variance_today_kpi.text =
            if (detailTodayCEOKpi?.labor?.variance?.percentage?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detailTodayCEOKpi.labor.variance.percentage
            )).plus(
                getString(
                    R.string.percentage_text
                )
            ) else ""

        if (detailTodayCEOKpi?.labor?.actual?.percentage?.isNaN() == false && detailTodayCEOKpi.labor.status != null) {
            labour_actual_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayCEOKpi.labor.actual.percentage)
                    .plus(getString(R.string.percentage_text))
            when {
                detailTodayCEOKpi.labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    labour_actual_today_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                detailTodayCEOKpi.labor.status.toString() == resources.getString(R.string.under_limit) -> {
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
        // service
        service_eadt_goal_today_kpi.text =
            if (detailTodayCEOKpi?.service?.eADT?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayCEOKpi.service.eADT.goal.value
            ) else ""

        service_eadt_variance_today_kpi.text =
            if (detailTodayCEOKpi?.service?.eADT?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayCEOKpi.service.eADT.variance.value
            ) else ""

        if (detailTodayCEOKpi?.service?.eADT?.actual?.value?.isNaN() == false && detailTodayCEOKpi.service.eADT.status != null) {
            service_eadt_actual_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayCEOKpi.service.eADT.actual.value)

            when {
                detailTodayCEOKpi.service.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_eadt_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    service_eadt_actual_today_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                detailTodayCEOKpi.service.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_eadt_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    service_eadt_actual_today_kpi.setTextColor(requireContext().getColor(R.color.green))

                } else -> {
                    service_eadt_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    service_eadt_actual_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        // service extreme
        service_extreme_goal_today_kpi.text =
            if (detailTodayCEOKpi?.service?.extremeDelivery?.goal?.value?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detailTodayCEOKpi.service.extremeDelivery.goal.value
            )).plus(
                getString(
                    R.string.percentage_text
                )
            ) else ""
        service_extreme_variance_today_kpi.text =
            if (detailTodayCEOKpi?.service?.extremeDelivery?.variance?.value?.isNaN() == false) (Validation().ignoreZeroAfterDecimal(
                detailTodayCEOKpi.service.extremeDelivery.variance.value
            )).plus(
                getString(
                    R.string.percentage_text
                )
            ) else ""

        if (detailTodayCEOKpi?.service?.extremeDelivery?.actual?.value?.isNaN() == false && detailTodayCEOKpi.service.extremeDelivery.status != null) {
            service_extreme_actual_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayCEOKpi.service.extremeDelivery.actual.value)
                    .plus(getString(R.string.percentage_text))

            when {
                detailTodayCEOKpi.service.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    service_extreme_actual_today_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                detailTodayCEOKpi.service.extremeDelivery.status.toString() == resources.getString(
                    R.string.under_limit
                ) -> {
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
            if (detailTodayCEOKpi?.service?.singles?.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayCEOKpi.service.singles.goal.percentage
            )
                .plus(getString(R.string.percentage_text)) else ""
        service_singles_variance_today_kpi.text =
            if (detailTodayCEOKpi?.service?.singles?.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayCEOKpi.service.singles.variance.percentage
            )
                .plus(getString(R.string.percentage_text)) else ""

        if (detailTodayCEOKpi?.service?.singles?.actual?.percentage?.isNaN() == false && detailTodayCEOKpi.service.singles.status != null) {
            service_singles_actual_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayCEOKpi.service.singles.actual.percentage)
                    .plus(getString(R.string.percentage_text))

            when {
                detailTodayCEOKpi.service.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    service_singles_actual_today_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                detailTodayCEOKpi.service.singles.status.toString() == resources.getString(R.string.under_limit) -> {
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
            if (detailTodayCEOKpi?.cash?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayCEOKpi.cash.goal.value
            ) else ""
        cash_variance_today_kpi.text =
            if (detailTodayCEOKpi?.cash?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                detailTodayCEOKpi.cash.variance.value
            ) else ""

        if (detailTodayCEOKpi?.cash?.actual?.value?.isNaN() == false && detailTodayCEOKpi.cash.status != null) {
            cash_actual_today_kpi.text =
                Validation().ignoreZeroAfterDecimal(detailTodayCEOKpi.cash.actual.value)
            when {
                detailTodayCEOKpi.cash.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    cash_actual_today_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                detailTodayCEOKpi.cash.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    cash_actual_today_kpi.setTextColor(requireContext().getColor(R.color.green))
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
            // Oer
            oer_goal_today_kpi.text =
                if (detailTodayCEOKpi.oerStart?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    detailTodayCEOKpi.oerStart.goal.value
                ) else ""
            oer_variance_today_kpi.text =
                if (detailTodayCEOKpi.oerStart?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(
                    detailTodayCEOKpi.oerStart.variance.value
                ) else ""

            if (detailTodayCEOKpi.oerStart?.actual?.value?.isNaN() == false && detailTodayCEOKpi.oerStart.status != null) {
                oer_actual_today_kpi.text =
                    Validation().ignoreZeroAfterDecimal(detailTodayCEOKpi.oerStart.actual.value)
                when {
                    detailTodayCEOKpi.oerStart.status.toString() == resources.getString(R.string.out_of_range) -> {
                        oer_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        oer_actual_today_kpi.setTextColor(requireContext().getColor(R.color.red))

                    }
                    detailTodayCEOKpi.oerStart.status.toString() == resources.getString(R.string.under_limit) -> {
                        oer_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        oer_actual_today_kpi.setTextColor(requireContext().getColor(R.color.green))
                    } else -> {
                        oer_actual_today_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        oer_actual_today_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                    }
                }
            }
        }


    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.aws_parent_layout_today_kpi -> {

                rcv_labour_today_kpi.visibility = View.GONE
                rcv_service_today_kpi.visibility = View.GONE
                rcv_oer_today_kpi.visibility = View.GONE
                rcv_cash_today_kpi.visibility = View.GONE

                labour_text_overview_today_kpi.visibility = View.GONE
                service_text_overview_today_kpi.visibility = View.GONE
                oer_text_overview_today_kpi.visibility = View.GONE
                cash_text_overview_today_kpi.visibility = View.GONE

                labor_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_sales_today_kpi.visibility == View.VISIBLE) {
                    rcv_sales_today_kpi.visibility = View.GONE
                    aws_text_overview_today_kpi.visibility = View.GONE
                    aws_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_sales_today_kpi.visibility = View.VISIBLE
                    aws_text_overview_today_kpi.visibility = View.VISIBLE
                    aws_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_up)
                }
                setExpandableTodayCEOKpiData(getString(R.string.awus_text), rcv_sales_today_kpi)
            }
            R.id.labor_parent_layout_today_kpi -> {
                rcv_sales_today_kpi.visibility = View.GONE
                rcv_service_today_kpi.visibility = View.GONE
                rcv_oer_today_kpi.visibility = View.GONE
                rcv_cash_today_kpi.visibility = View.GONE

                aws_text_overview_today_kpi.visibility = View.GONE
                service_text_overview_today_kpi.visibility = View.GONE
                oer_text_overview_today_kpi.visibility = View.GONE
                cash_text_overview_today_kpi.visibility = View.GONE


                aws_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_labour_today_kpi.visibility == View.VISIBLE) {
                    rcv_labour_today_kpi.visibility = View.GONE
                    labour_text_overview_today_kpi.visibility = View.GONE
                    labor_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_labour_today_kpi.visibility = View.VISIBLE
                    labour_text_overview_today_kpi.visibility = View.VISIBLE
                    labor_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                setExpandableTodayCEOKpiData(
                    getString(R.string.labour_vs_goal_text),
                    rcv_labour_today_kpi
                )

            }
            R.id.service_parent_layout_today_kpi -> {
                rcv_sales_today_kpi.visibility = View.GONE
                rcv_labour_today_kpi.visibility = View.GONE
                rcv_oer_today_kpi.visibility = View.GONE
                rcv_cash_today_kpi.visibility = View.GONE

                aws_text_overview_today_kpi.visibility = View.GONE
                labour_text_overview_today_kpi.visibility = View.GONE
                oer_text_overview_today_kpi.visibility = View.GONE
                cash_text_overview_today_kpi.visibility = View.GONE


                aws_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_service_today_kpi.visibility == View.VISIBLE) {
                    rcv_service_today_kpi.visibility = View.GONE
                    service_text_overview_today_kpi.visibility = View.GONE
                    service_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_service_today_kpi.visibility = View.VISIBLE
                    service_text_overview_today_kpi.visibility = View.VISIBLE
                    service_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                setExpandableTodayCEOKpiData(
                    getString(R.string.service_text),
                    rcv_service_today_kpi
                )

            }
            R.id.cash_parent_layout_today_kpi -> {
                rcv_sales_today_kpi.visibility = View.GONE
                rcv_labour_today_kpi.visibility = View.GONE
                rcv_service_today_kpi.visibility = View.GONE
                rcv_oer_today_kpi.visibility = View.GONE

                aws_text_overview_today_kpi.visibility = View.GONE
                labour_text_overview_today_kpi.visibility = View.GONE
                service_text_overview_today_kpi.visibility = View.GONE
                oer_text_overview_today_kpi.visibility = View.GONE


                aws_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_cash_today_kpi.visibility == View.VISIBLE) {
                    rcv_cash_today_kpi.visibility = View.GONE
                    cash_text_overview_today_kpi.visibility = View.GONE
                    cash_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_cash_today_kpi.visibility = View.VISIBLE
                    cash_text_overview_today_kpi.visibility = View.VISIBLE
                    cash_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                setExpandableTodayCEOKpiData(getString(R.string.cash_text), rcv_cash_today_kpi)

            }
            R.id.oer_parent_layout_today_kpi -> {
                rcv_sales_today_kpi.visibility = View.GONE
                rcv_labour_today_kpi.visibility = View.GONE
                rcv_service_today_kpi.visibility = View.GONE
                rcv_cash_today_kpi.visibility = View.GONE

                aws_text_overview_today_kpi.visibility = View.GONE
                labour_text_overview_today_kpi.visibility = View.GONE
                service_text_overview_today_kpi.visibility = View.GONE
                cash_text_overview_today_kpi.visibility = View.GONE


                aws_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_oer_today_kpi.visibility == View.VISIBLE) {
                    rcv_oer_today_kpi.visibility = View.GONE
                    oer_text_overview_today_kpi.visibility = View.GONE
                    oer_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                } else {
                    rcv_oer_today_kpi.visibility = View.VISIBLE
                    oer_text_overview_today_kpi.visibility = View.VISIBLE
                    oer_parent_img_today_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                setExpandableTodayCEOKpiData(getString(R.string.oer_text), rcv_oer_today_kpi)

            }

            R.id.filter_icon -> {
                openFilterTodayCEOKpi()
            }
            R.id.filter_parent_linear -> {
                openFilterTodayCEOKpi()
            }

            R.id.aws_text_overview_today_kpi -> {
                callOverViewTodayCEOKpiApi(getString(R.string.awus_text), "", "")
            }
            R.id.labour_text_overview_today_kpi -> {
                callOverViewTodayCEOKpiApi(getString(R.string.labour_text), "", "")
            }
            R.id.service_text_overview_today_kpi -> {
                callOverViewTodayCEOKpiApi(getString(R.string.service_text), "", "")
            }
            R.id.oer_text_overview_today_kpi -> {
                callOverViewTodayCEOKpiApi(getString(R.string.oer_text), "", "")
            }
            R.id.cash_text_overview_today_kpi -> {
                callOverViewTodayCEOKpiApi(getString(R.string.cash_text), "", "")
            }
        }
    }

    private fun callOverViewTodayCEOKpiApi(
        action: String,
        superVisorNumber: String,
        storeNumber: String
    ) {
        val superVisorNumberListOverViewTodayCEOKpi = mutableListOf<String>()
        superVisorNumberListOverViewTodayCEOKpi.add(superVisorNumber)
        val storeNumberListOverViewTodayCEOKpi = mutableListOf<String>()
        storeNumberListOverViewTodayCEOKpi.add(storeNumber)

        val progressDialogOverViewTodayCEOKpi = CustomProgressDialog(requireActivity())
        progressDialogOverViewTodayCEOKpi.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val response = try {
                apolloClient(requireContext()).query(
                    CEOOverviewTodayQuery(
                        superVisorNumberListOverViewTodayCEOKpi.toInput(),
                        storeNumberListOverViewTodayCEOKpi.toInput()
                    )
                ).await()
            } catch (e: ApolloException) {
                progressDialogOverViewTodayCEOKpi.dismissProgressDialog()
                return@launchWhenResumed
            }
            if (response.data?.ceo != null) {
                progressDialogOverViewTodayCEOKpi.dismissProgressDialog()
                when (action) {
                    getString(R.string.awus_text) -> {
                        openSalesTodayCEOKpiDetail(response.data?.ceo!!)
                    }
                    getString(R.string.labour_text) -> {
                        openLabourTodayCEOKpiDetail(response.data?.ceo!!)
                    }
                    getString(R.string.service_text) -> {
                        openServiceTodayCEOKpiDetail(response.data?.ceo!!)
                    }
                    getString(R.string.oer_text) -> {
                        openOERTodayCEOKpiDetail(response.data?.ceo!!)
                    }

                }
            }
        }
    }

    private fun openFilterTodayCEOKpi() {
        val intentFilterTodayCEOKpi = Intent(requireContext(), FilterActivity::class.java)
        startActivity(intentFilterTodayCEOKpi)
    }

    private fun openSalesTodayCEOKpiDetail(ceoSalesTodayKpi: CEOOverviewTodayQuery.Ceo) {
        val intentCeoSalesTodayKpi = Intent(requireContext(), AWUSKpiActivity::class.java)
        intentCeoSalesTodayKpi.putExtra("awus_data", gsonTodayCEOKpi.toJson(ceoSalesTodayKpi))
        intentCeoSalesTodayKpi.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentCeoSalesTodayKpi)

    }

    private fun openLabourTodayCEOKpiDetail(ceoLabourTodayKpi: CEOOverviewTodayQuery.Ceo) {
        val intentLabourTodayKpi = Intent(requireContext(), LabourKpiActivity::class.java)
        intentLabourTodayKpi.putExtra("labour_data", gsonTodayCEOKpi.toJson(ceoLabourTodayKpi))
        intentLabourTodayKpi.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentLabourTodayKpi)
    }

    private fun openServiceTodayCEOKpiDetail(ceoServiceTodayKpi: CEOOverviewTodayQuery.Ceo) {
        val intentServiceTodayKpi = Intent(requireContext(), ServiceKpiActivity::class.java)
        intentServiceTodayKpi.putExtra("service_data", gsonTodayCEOKpi.toJson(ceoServiceTodayKpi))
        intentServiceTodayKpi.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentServiceTodayKpi)
    }

    private fun openOERTodayCEOKpiDetail(ceoOERTodayKpi: CEOOverviewTodayQuery.Ceo) {
        val intentOERTodayKpi = Intent(requireContext(), OERStartActivity::class.java)
        intentOERTodayKpi.putExtra("oer_data", gsonTodayCEOKpi.toJson(ceoOERTodayKpi))
        intentOERTodayKpi.putExtra("api_argument_from_filter", IpConstants.Today)
        startActivity(intentOERTodayKpi)
    }

    private fun refreshTodayCEOKpiToken() {
        val progressDialogRefreshTodayCEOKpiToken = CustomProgressDialog(requireActivity())
        progressDialogRefreshTodayCEOKpiToken.showProgressDialog()
        val apiServiceRefreshTodayCEOKpiToken: ApiInterface =
            ApiClientAuth().getClient()!!.create(ApiInterface::class.java)
        val callRefreshTodayCEOKpiToken =
            apiServiceRefreshTodayCEOKpiToken.refreshToken(SendRefreshRequest(StorePrefData.refreshToken))
        callRefreshTodayCEOKpiToken.enqueue(object : retrofit2.Callback<LoginSuccess> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                callRefreshTodayCEOKpiToken: retrofit2.Call<LoginSuccess>,
                responseRefreshTodayCEOKpiToken: retrofit2.Response<LoginSuccess>,
            ) {
                progressDialogRefreshTodayCEOKpiToken.dismissProgressDialog()
                if (responseRefreshTodayCEOKpiToken.isSuccessful) {
                    Logger.info("Token Refreshed","Today Refresh Token")

                    StorePrefData.token =
                        responseRefreshTodayCEOKpiToken.body()!!.authenticationResult.accessToken
                    callTodayCEOKpiApi()
                } else {
                    val gsonRefreshTodayCEOKpiToken = Gson()
                    val typeRefreshTodayCEOKpiToken = object : TypeToken<LoginFail>() {
                    }.type
                    val errorResponseRefreshTodayCEOKpiToken =
                        gsonRefreshTodayCEOKpiToken.fromJson<LoginFail>(
                            responseRefreshTodayCEOKpiToken.errorBody()!!.charStream(),
                            typeRefreshTodayCEOKpiToken
                        )
                    Logger.error(errorResponseRefreshTodayCEOKpiToken.message.toString(),"Today Refresh Token")

                    Validation().showMessageToast(
                        requireActivity(),
                        errorResponseRefreshTodayCEOKpiToken.message
                    )
                }


            }

            override fun onFailure(call: retrofit2.Call<LoginSuccess>, t: Throwable) {
                progressDialogRefreshTodayCEOKpiToken.dismissProgressDialog()
                if (networkHelper.isNetworkConnected()) {
                    Logger.error(t.message.toString(),"Today Refresh Token")
                }
            }
        })
    }
}