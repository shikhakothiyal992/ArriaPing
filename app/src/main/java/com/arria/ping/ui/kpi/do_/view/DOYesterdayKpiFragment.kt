package com.arria.ping.ui.kpi.do_.view

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import com.arria.ping.R
import com.arria.ping.apollo.apolloClient
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.kpi.*
import com.arria.ping.kpi._do.DOYesterdayLevelTwoQuery
import com.arria.ping.kpi._do.DoYesterdayLevelOneQuery
import com.arria.ping.log.Logger
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.model.StoreDetailPojo
import com.arria.ping.ui.filter.FilterActivity
import com.arria.ping.ui.generalview.MainActivity
import com.arria.ping.ui.kpi.do_.adapter.CustomExpandableListAdapterYesterdayDO
import com.arria.ping.ui.kpi.overview.*
import com.arria.ping.ui.refreshtoken.model.Status
import com.arria.ping.ui.refreshtoken.viewmodel.RefreshTokenViewModel
import com.arria.ping.util.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.ceo_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.common_calendar.view.*
import kotlinx.android.synthetic.main.common_header.view.*
import kotlinx.android.synthetic.main.common_header_ceo.*
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.common_header_ceo.view.sales_text_common_header
import kotlinx.android.synthetic.main.common_header_ceo.view.total_sales_common_header
import kotlinx.android.synthetic.main.data_error_layout.view.*
import kotlinx.android.synthetic.main.do_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.do_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.individual_supervisor_item.view.*
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class DOYesterdayKpiFragment : Fragment(), View.OnClickListener {

    lateinit var doYesterdayLevelOne: DoYesterdayLevelOneQuery.Do_
    lateinit var doYesterdayLevelTwo: DOYesterdayLevelTwoQuery.Do_
    lateinit var individualStoreDetails: DOYesterdayLvlThreeQuery.Do_

    private var lastExpandedPosition = -1
    val expandableListDetail = HashMap<String, List<StoreDetailPojo>>()
    private var expandableListAdapterCEO: CustomExpandableListAdapterYesterdayDO? = null
    lateinit var dbHelper: DatabaseHelperImpl
    val gson = Gson()

    @Inject
    lateinit var networkHelper: NetworkHelper
    private val refreshTokenViewModelDoYesterday by viewModels<RefreshTokenViewModel>()

    private lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.do_yesterday_fragment_kpi, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelperImpl(DatabaseBuilder.getInstance(requireActivity()))

        initialise()
        if(StorePrefData.filterDate.isNotEmpty()){
            setStoreFilterViewForDoYesterday(StorePrefData.filterDate)
        }

        if (networkHelper.isNetworkConnected()) {
            callMissingDataQueryForDoYesterdayQuary()
            callDoYesterdayLevelOneQuery()
        } else {
            setInternetErrorScreenVisibleStateForDoYesterday()
        }

        do_yesterday_swipe_refresh_layout.setOnRefreshListener{
            Logger.info("Pull down to refresh DO Yesterday Store Data", "Yesterday KPI")
            callMissingDataQueryForDoYesterdayQuary()
            callDoYesterdayLevelOneQuery()
            collapseExpendedListVisibilityForDOYesterday()
            do_yesterday_swipe_refresh_layout.isRefreshing = false
        }
    }


    private fun initialise() {
        aws_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        food_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        labor_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        service_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        oer_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        cash_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

        aws_parent_layout_do_yesterday_kpi.setOnClickListener(this)
        food_parent_layout_do_yesterday_kpi.setOnClickListener(this)
        labor_parent_layout_do_yesterday_kpi.setOnClickListener(this)
        service_parent_layout_do_yesterday_kpi.setOnClickListener(this)
        cash_parent_layout_do_yesterday_kpi.setOnClickListener(this)
        oer_parent_layout_do_yesterday_kpi.setOnClickListener(this)

        filter_icon.setOnClickListener(this)
        filter_parent_linear.setOnClickListener(this)
        error_filter_parent_linear.setOnClickListener(this)

        aws_text_overview_do_yesterday_kpi.setOnClickListener(this)
        food_text_overview_do_yesterday_kpi.setOnClickListener(this)
        labour_text_overview_do_yesterday_kpi.setOnClickListener(this)
        service_text_overview_do_yesterday_kpi.setOnClickListener(this)
        oer_text_overview_do_yesterday_kpi.setOnClickListener(this)
        cash_text_overview_do_yesterday_kpi.setOnClickListener(this)

        Validation().setCustomCalendar(common_calendar_do_yesterday.square_day)
    }

    private fun setExpandableDataDoYesterdayKpi(
            actionDOYesterdayKpi: String,
            rcvDoYesterdayKpi: NonScrollExpandableListView
    ) {
        val childExpandableDataDoYesterdayKpi: MutableList<StoreDetailPojo> = ArrayList()
        doYesterdayLevelTwo.kpis!!.individualSupervisors.forEachIndexed {_, item ->
            expandableListDetail[item!!.supervisorName!!] = childExpandableDataDoYesterdayKpi
        }
        val titleListExpandableDataDoYesterdayKpi = ArrayList(expandableListDetail.keys)


        expandableListAdapterCEO = CustomExpandableListAdapterYesterdayDO(
                requireContext(),
                titleListExpandableDataDoYesterdayKpi as ArrayList<String>,
                expandableListDetail, doYesterdayLevelTwo, actionDOYesterdayKpi
        )
        rcvDoYesterdayKpi.setAdapter(expandableListAdapterCEO)

        rcvDoYesterdayKpi.setOnGroupExpandListener {groupPosition ->
            val superVisorNumberValue = doYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.supervisorNumber!!

            val doYesterdayExpandableSupervisorSalesKpiData =
                    doYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.yesterday?.sales
            val doYesterdayExpandableSupervisorFoodKpiData =
                    doYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.yesterday?.food
            val doYesterdayExpandableSupervisorLaborKpiData =
                    doYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.yesterday?.labor
            val doYesterdayExpandableSupervisorOERKpiData =
                    doYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.yesterday?.oerStart
            val doYesterdayExpandableSupervisorCashKpiData =
                    doYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.yesterday?.cash

            //Sales
            val doYesterdayExpandableSupervisorSalesKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doYesterdayExpandableSupervisorSalesKpiData?.goal?.amount,
                    doYesterdayExpandableSupervisorSalesKpiData?.goal?.value,
                    doYesterdayExpandableSupervisorSalesKpiData?.goal?.percentage
            )
            val doYesterdayExpandableSupervisorSalesKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doYesterdayExpandableSupervisorSalesKpiData?.variance?.amount,
                    doYesterdayExpandableSupervisorSalesKpiData?.variance?.value,
                    doYesterdayExpandableSupervisorSalesKpiData?.variance?.percentage
            )
            val doYesterdayExpandableSupervisorSalesKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doYesterdayExpandableSupervisorSalesKpiData?.actual?.amount,
                    doYesterdayExpandableSupervisorSalesKpiData?.actual?.value,
                    doYesterdayExpandableSupervisorSalesKpiData?.actual?.percentage
            )

            //Food
            val doYesterdayExpandableSupervisorFoodKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doYesterdayExpandableSupervisorFoodKpiData?.goal?.amount,
                    doYesterdayExpandableSupervisorFoodKpiData?.goal?.value,
                    doYesterdayExpandableSupervisorFoodKpiData?.goal?.percentage
            )
            val doYesterdayExpandableSupervisorFoodKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doYesterdayExpandableSupervisorFoodKpiData?.variance?.amount,
                    doYesterdayExpandableSupervisorFoodKpiData?.variance?.value,
                    doYesterdayExpandableSupervisorFoodKpiData?.variance?.percentage
            )
            val doYesterdayExpandableSupervisorFoodKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doYesterdayExpandableSupervisorFoodKpiData?.actual?.amount,
                    doYesterdayExpandableSupervisorFoodKpiData?.actual?.value,
                    doYesterdayExpandableSupervisorFoodKpiData?.actual?.percentage
            )

            //Labor
            val doYesterdayExpandableSupervisorLaborKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doYesterdayExpandableSupervisorLaborKpiData?.goal?.amount,
                    doYesterdayExpandableSupervisorLaborKpiData?.goal?.value,
                    doYesterdayExpandableSupervisorLaborKpiData?.goal?.percentage
            )
            val doYesterdayExpandableSupervisorLaborKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doYesterdayExpandableSupervisorLaborKpiData?.variance?.amount,
                    doYesterdayExpandableSupervisorLaborKpiData?.variance?.value,
                    doYesterdayExpandableSupervisorLaborKpiData?.variance?.percentage
            )
            val doYesterdayExpandableSupervisorLaborKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doYesterdayExpandableSupervisorLaborKpiData?.actual?.amount,
                    doYesterdayExpandableSupervisorLaborKpiData?.actual?.value,
                    doYesterdayExpandableSupervisorLaborKpiData?.actual?.percentage
            )

            //OER
            val doYesterdayExpandableSupervisorOERKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doYesterdayExpandableSupervisorOERKpiData?.goal?.amount,
                    doYesterdayExpandableSupervisorOERKpiData?.goal?.value,
                    doYesterdayExpandableSupervisorOERKpiData?.goal?.percentage
            )
            val doYesterdayExpandableSupervisorOERKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doYesterdayExpandableSupervisorOERKpiData?.variance?.amount,
                    doYesterdayExpandableSupervisorOERKpiData?.variance?.value,
                    doYesterdayExpandableSupervisorOERKpiData?.variance?.percentage
            )
            val doYesterdayExpandableSupervisorOERKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doYesterdayExpandableSupervisorOERKpiData?.actual?.amount,
                    doYesterdayExpandableSupervisorOERKpiData?.actual?.value,
                    doYesterdayExpandableSupervisorOERKpiData?.actual?.percentage
            )

            //Cash
            val doYesterdayExpandableSupervisorCashKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doYesterdayExpandableSupervisorCashKpiData?.actual?.amount,
                    doYesterdayExpandableSupervisorCashKpiData?.actual?.value,
                    doYesterdayExpandableSupervisorCashKpiData?.actual?.percentage
            )

            when {
                rcv_sales_do_yesterday_kpi.visibility == View.VISIBLE -> {
                    if (doYesterdayExpandableSupervisorSalesKpiDataGoal.isEmpty() && doYesterdayExpandableSupervisorSalesKpiDataVariance.isEmpty() && doYesterdayExpandableSupervisorSalesKpiDataActual.isEmpty()) {
                        rcvDoYesterdayKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvDoYesterdayKpi.isGroupExpanded(groupPosition))
                            callStoreAgainstSupervisorDoYesterdayKpi(
                                    titleListExpandableDataDoYesterdayKpi[groupPosition],
                                    groupPosition,
                                    actionDOYesterdayKpi,
                                    superVisorNumberValue
                            )
                        else
                            rcvDoYesterdayKpi.collapseGroup(groupPosition)
                    }
                }

                rcv_food_do_yesterday_kpi.visibility == View.VISIBLE -> {
                    if (doYesterdayExpandableSupervisorFoodKpiDataGoal.isEmpty() && doYesterdayExpandableSupervisorFoodKpiDataVariance.isEmpty() && doYesterdayExpandableSupervisorFoodKpiDataActual.isEmpty()) {
                        rcvDoYesterdayKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvDoYesterdayKpi.isGroupExpanded(groupPosition))
                            callStoreAgainstSupervisorDoYesterdayKpi(
                                    titleListExpandableDataDoYesterdayKpi[groupPosition],
                                    groupPosition,
                                    actionDOYesterdayKpi,
                                    superVisorNumberValue
                            )
                        else
                            rcvDoYesterdayKpi.collapseGroup(groupPosition)
                    }
                }

                rcv_labour_do_yesterday_kpi.visibility == View.VISIBLE -> {
                    if (doYesterdayExpandableSupervisorLaborKpiDataGoal.isEmpty() && doYesterdayExpandableSupervisorLaborKpiDataVariance.isEmpty() && doYesterdayExpandableSupervisorLaborKpiDataActual.isEmpty()) {
                        rcvDoYesterdayKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvDoYesterdayKpi.isGroupExpanded(groupPosition))
                            callStoreAgainstSupervisorDoYesterdayKpi(
                                    titleListExpandableDataDoYesterdayKpi[groupPosition],
                                    groupPosition,
                                    actionDOYesterdayKpi,
                                    superVisorNumberValue
                            )
                        else
                            rcvDoYesterdayKpi.collapseGroup(groupPosition)
                    }
                }

                rcv_service_do_yesterday_kpi.visibility == View.VISIBLE -> {
                    if (doYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.yesterday == null) {
                        rcvDoYesterdayKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvDoYesterdayKpi.isGroupExpanded(groupPosition))
                            callStoreAgainstSupervisorDoYesterdayKpi(
                                    titleListExpandableDataDoYesterdayKpi[groupPosition],
                                    groupPosition,
                                    actionDOYesterdayKpi,
                                    superVisorNumberValue
                            )
                        else
                            rcvDoYesterdayKpi.collapseGroup(groupPosition)
                    }
                }

                rcv_oer_do_yesterday_kpi.visibility == View.VISIBLE -> {
                    if (doYesterdayExpandableSupervisorOERKpiDataGoal.isEmpty() && doYesterdayExpandableSupervisorOERKpiDataVariance.isEmpty() && doYesterdayExpandableSupervisorOERKpiDataActual.isEmpty()) {
                        rcvDoYesterdayKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvDoYesterdayKpi.isGroupExpanded(groupPosition))
                            callStoreAgainstSupervisorDoYesterdayKpi(
                                    titleListExpandableDataDoYesterdayKpi[groupPosition],
                                    groupPosition,
                                    actionDOYesterdayKpi,
                                    superVisorNumberValue
                            )
                        else
                            rcvDoYesterdayKpi.collapseGroup(groupPosition)
                    }
                }

                rcv_cash_do_yesterday_kpi.visibility == View.VISIBLE -> {
                    if (doYesterdayExpandableSupervisorCashKpiDataActual.isNotEmpty()) {

                        if (rcvDoYesterdayKpi.isGroupExpanded(groupPosition))

                            callStoreAgainstSupervisorDoYesterdayKpi(
                                    titleListExpandableDataDoYesterdayKpi[groupPosition],
                                    groupPosition,
                                    actionDOYesterdayKpi,
                                    superVisorNumberValue
                            )
                        else
                            rcvDoYesterdayKpi.collapseGroup(groupPosition)
                    } else {
                        rcvDoYesterdayKpi.collapseGroup(groupPosition)
                    }

                }
            }

            if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
                rcvDoYesterdayKpi.collapseGroup(lastExpandedPosition)
            }
            lastExpandedPosition = groupPosition
        }

        rcvDoYesterdayKpi.setOnChildClickListener {_, _, groupPosition, childPosition, _ ->
            val superVisorNumber = doYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.supervisorNumber!!

            if (childPosition == 0) {

                when {
                    rcv_sales_do_yesterday_kpi.visibility == View.VISIBLE -> {
                        callOverViewDOYesterdayKpiApi(getString(R.string.awus_text), superVisorNumber, "")
                    }
                    rcv_food_do_yesterday_kpi.visibility == View.VISIBLE -> {
                        callOverViewDOYesterdayKpiApi(getString(R.string.food_text), superVisorNumber, "")
                    }
                    rcv_labour_do_yesterday_kpi.visibility == View.VISIBLE -> {
                        callOverViewDOYesterdayKpiApi(getString(R.string.labour_text), superVisorNumber, "")
                    }
                    rcv_service_do_yesterday_kpi.visibility == View.VISIBLE -> {
                        callOverViewDOYesterdayKpiApi(getString(R.string.service_text), superVisorNumber, "")
                    }
                    rcv_oer_do_yesterday_kpi.visibility == View.VISIBLE -> {
                        callOverViewDOYesterdayKpiApi(getString(R.string.oer_text), superVisorNumber, "")
                    }
                    rcv_cash_do_yesterday_kpi.visibility == View.VISIBLE -> {
                        callOverViewDOYesterdayKpiApi(getString(R.string.cash_text), superVisorNumber, "")
                    }
                }
            }
            else {
                val storeNumber =
                        expandableListDetail[titleListExpandableDataDoYesterdayKpi[groupPosition]]!![(childPosition)].storeNumber!!

                val doYesterdayKpiData =
                        expandableListDetail[titleListExpandableDataDoYesterdayKpi[groupPosition]]!![(childPosition)]
                if (doYesterdayKpiData.storeGoal?.isNotEmpty() == true || doYesterdayKpiData.storeVariance?.isNotEmpty() == true || doYesterdayKpiData.storeActual?.isNotEmpty() == true) {

                    when {
                        rcv_sales_do_yesterday_kpi.visibility == View.VISIBLE -> {
                            callOverViewDOYesterdayKpiApi(getString(R.string.awus_text), superVisorNumber, storeNumber)
                        }
                        rcv_food_do_yesterday_kpi.visibility == View.VISIBLE -> {
                            callOverViewDOYesterdayKpiApi(getString(R.string.food_text), superVisorNumber, storeNumber)
                        }
                        rcv_labour_do_yesterday_kpi.visibility == View.VISIBLE -> {
                            callOverViewDOYesterdayKpiApi(
                                    getString(R.string.labour_text),
                                    superVisorNumber,
                                    storeNumber
                            )
                        }

                        rcv_oer_do_yesterday_kpi.visibility == View.VISIBLE -> {
                            callOverViewDOYesterdayKpiApi(getString(R.string.oer_text), superVisorNumber, storeNumber)
                        }
                        rcv_cash_do_yesterday_kpi.visibility == View.VISIBLE -> {
                            callOverViewDOYesterdayKpiApi(getString(R.string.cash_text), superVisorNumber, storeNumber)
                        }
                    }
                }

                if (rcv_service_do_yesterday_kpi.visibility == View.VISIBLE) {
                    callOverViewDOYesterdayKpiApi(
                            getString(R.string.service_text),
                            superVisorNumber,
                            storeNumber
                    )
                }
            }


            false
        }

    }

    private fun callStoreAgainstSupervisorDoYesterdayKpi(
            titleYesterdayKpi: String,
            groupPositionYesterdayKpi: Int,
            actionDOYesterdayKpi: String,
            superVisorNumberValueYesterdayKpi: String
    ) {
        val childDataDoYesterdayKpi = mutableListOf<StoreDetailPojo>()
        childDataDoYesterdayKpi.add(
                StoreDetailPojo(
                        "",
                        "",
                        "",
                        "",
                        ""
                )
        )

        val progressDialog = CustomProgressDialog(requireActivity())
        lifecycleScope.launchWhenResumed {
            progressDialog.showProgressDialog()
            val areaCodeDoYesterdayKpi = dbHelper.getAllSelectedAreaList(true)
            val stateCodeDoYesterdayKpi = dbHelper.getAllSelectedStoreListState(true)
            var superVisorNumberListDoYesterdayKpi = dbHelper.getAllSelectedStoreListSupervisor(true)
            val storeListValueDoYesterdayKpi = dbHelper.getAllSelectedStoreList(true)
            val doSuperVisorNumberListTemp = mutableListOf<String>()
            if (superVisorNumberValueYesterdayKpi.isNotEmpty() || superVisorNumberValueYesterdayKpi.isNotBlank()) {
                doSuperVisorNumberListTemp.add(superVisorNumberValueYesterdayKpi)
            }

            if (dbHelper.getAllSelectedStoreListSupervisor(true)
                        .isEmpty()
            ) {
                superVisorNumberListDoYesterdayKpi = doSuperVisorNumberListTemp
            }



            Logger.info(
                    DOYesterdayLvlThreeQuery.OPERATION_NAME.name(),
                    "Yesterday Level 3 KPI",
                    mapQueryFilters(
                            areaCodeDoYesterdayKpi,
                            stateCodeDoYesterdayKpi,
                            superVisorNumberListDoYesterdayKpi,
                            storeListValueDoYesterdayKpi,
                            DOYesterdayLvlThreeQuery.QUERY_DOCUMENT
                    )
            )
            Log.e("groupPosition", "$groupPositionYesterdayKpi")
            try {
                val responseDoYesterdayKpi =
                        apolloClient(requireContext()).query(
                                DOYesterdayLvlThreeQuery(
                                        areaCodeDoYesterdayKpi.toInput(),
                                        stateCodeDoYesterdayKpi.toInput(),
                                        superVisorNumberListDoYesterdayKpi.toInput(),
                                        storeListValueDoYesterdayKpi.toInput()
                                )
                        )
                                .await()

                if (responseDoYesterdayKpi.data?.do_ != null) {
                    progressDialog.dismissProgressDialog()
                    individualStoreDetails = responseDoYesterdayKpi.data?.do_!!

                    responseDoYesterdayKpi.data?.do_?.kpis?.individualStores.let {
                        setDataForIndividualStore(
                                actionDOYesterdayKpi,
                                titleYesterdayKpi
                        )
                    }
                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialog.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Level 3 KPI")
                        }
                refreshYesterdayDoToken()
                return@launchWhenResumed
            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialog.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForDoYesterday()
                }
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Yesterday Level 3 KPI")
                refreshYesterdayDoToken()
                setErrorScreenVisibleStateForDoYesterday(
                        getString(R.string.exception_error_text_title), getString(
                        R.string
                                .exception_error_text_description
                )
                )

            }

        }

    }

    private fun setDataForIndividualStore(
            action: String,
            title: String
    ) {
        val storeDetails = individualStoreDetails.kpis!!.individualStores
        val childData = mutableListOf<StoreDetailPojo>()
        childData.add(
                StoreDetailPojo(
                        "",
                        "",
                        "",
                        "",
                        ""
                )
        )

        storeDetails.forEachIndexed {_, item ->
            when (action) {
                requireActivity().getString(R.string.awus_text) -> {
                    childData.add(
                            StoreDetailPojo(
                                    item?.storeNumber.toString(),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.sales?.goal?.amount,
                                            item?.yesterday?.sales?.goal?.percentage,
                                            item?.yesterday?.sales?.goal?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.sales?.variance?.amount,
                                            item?.yesterday?.sales?.variance?.percentage,
                                            item?.yesterday?.sales?.variance?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.sales?.actual?.amount,
                                            item?.yesterday?.sales?.actual?.percentage,
                                            item?.yesterday?.sales?.actual?.value
                                    ),
                                    item?.yesterday?.sales?.status.toString()
                            )
                    )
                }
                requireActivity().getString(R.string.ideal_vs_food_variance_text) -> {
                    childData.add(
                            StoreDetailPojo(
                                    item?.storeNumber.toString(),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.food?.goal?.amount,
                                            item?.yesterday?.food?.goal?.percentage,
                                            item?.yesterday?.food?.goal?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.food?.variance?.amount,
                                            item?.yesterday?.food?.variance?.percentage,
                                            item?.yesterday?.food?.variance?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.food?.actual?.amount,
                                            item?.yesterday?.food?.actual?.percentage,
                                            item?.yesterday?.food?.actual?.value
                                    ),
                                    item?.yesterday?.food?.status.toString()
                            )
                    )
                }
                requireActivity().getString(R.string.labour_text) -> {
                    childData.add(
                            StoreDetailPojo(
                                    item?.storeNumber.toString(),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.labor?.goal?.amount,
                                            item?.yesterday?.labor?.goal?.percentage,
                                            item?.yesterday?.labor?.goal?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.labor?.variance?.amount,
                                            item?.yesterday?.labor?.variance?.percentage,
                                            item?.yesterday?.labor?.variance?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.labor?.actual?.amount,
                                            item?.yesterday?.labor?.actual?.percentage,
                                            item?.yesterday?.labor?.actual?.value
                                    ),
                                    item?.yesterday?.labor?.status.toString()
                            )
                    )
                }
                requireActivity().getString(R.string.service_text) -> {
                    childData.add(
                            StoreDetailPojo(
                                    item?.storeNumber.toString(),
                                    Validation().checkAmountPercentageValue(requireActivity(), null, null, null),
                                    Validation().checkAmountPercentageValue(requireActivity(), null, null, null),
                                    Validation().checkAmountPercentageValue(requireActivity(), null, null, null),
                                    item?.yesterday?.labor?.status.toString()
                            )
                    )
                }


                requireActivity().getString(R.string.cash_text) -> {
                    childData.add(
                            StoreDetailPojo(
                                    item?.storeNumber.toString(),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.cash?.goal?.amount,
                                            item?.yesterday?.cash?.goal?.percentage,
                                            item?.yesterday?.cash?.goal?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.cash?.variance?.amount,
                                            item?.yesterday?.cash?.variance?.percentage,
                                            item?.yesterday?.cash?.variance?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.cash?.actual?.amount,
                                            item?.yesterday?.cash?.actual?.percentage,
                                            item?.yesterday?.cash?.actual?.value
                                    ),
                                    item?.yesterday?.cash?.status.toString()
                            )
                    )
                }
                requireActivity().getString(R.string.oer_text) -> {
                    childData.add(
                            StoreDetailPojo(
                                    item?.storeNumber.toString(),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.oerStart?.goal?.amount,
                                            item?.yesterday?.oerStart?.goal?.percentage,
                                            item?.yesterday?.oerStart?.goal?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.oerStart?.variance?.amount,
                                            item?.yesterday?.oerStart?.variance?.percentage,
                                            item?.yesterday?.oerStart?.variance?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.oerStart?.actual?.amount,
                                            item?.yesterday?.oerStart?.actual?.percentage,
                                            item?.yesterday?.oerStart?.actual?.value
                                    ),
                                    item?.yesterday?.oerStart?.status.toString()
                            )
                    )
                }
            }
        }
        if (childData.size < 3) {
            childData.removeAt(0)
        }
        expandableListDetail[title] = childData
        expandableListAdapterCEO!! setChild (expandableListDetail)
    }

    private fun callMissingDataQueryForDoYesterdayQuary() {
        val progressDialogDoKpiYesterday = CustomProgressDialog(requireActivity())
        progressDialogDoKpiYesterday.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodeDoKpiYesterday = dbHelper.getAllSelectedAreaList(true)
            val stateCodeDoKpiYesterday = dbHelper.getAllSelectedStoreListState(true)
            val supervisorNumberDoKpiYesterday = dbHelper.getAllSelectedStoreListSupervisor(true)
            val storeNumberDoKpiYesterday = dbHelper.getAllSelectedStoreList(true)

            Logger.info(
                    MissingDataQuery.OPERATION_NAME.name(), "Yesterday Missing Data",
                    mapQueryFilters(
                            areaCodeDoKpiYesterday,
                            stateCodeDoKpiYesterday,
                            supervisorNumberDoKpiYesterday,
                            storeNumberDoKpiYesterday,
                            MissingDataQuery.QUERY_DOCUMENT
                    )
            )


            try {
                val responseMissingDataDoKpiYesterday =
                        apolloClient(requireContext()).query(
                                MissingDataQuery(
                                        areaCodeDoKpiYesterday.toInput(),
                                        stateCodeDoKpiYesterday.toInput(),
                                        supervisorNumberDoKpiYesterday.toInput(),
                                        storeNumberDoKpiYesterday.toInput()
                                )
                        )
                                .await()
                if (responseMissingDataDoKpiYesterday.data?.missingData != null) {
                    progressDialogDoKpiYesterday.dismissProgressDialog()
                    showMissingDataViewForDoYesterday(
                            responseMissingDataDoKpiYesterday.data?.missingData!!
                                    .header.toString(),
                            responseMissingDataDoKpiYesterday.data?.missingData!!.message.toString()
                    )
                } else {
                    progressDialogDoKpiYesterday.dismissProgressDialog()
                    do_yesterday_missing_data_error_layout.visibility = View.GONE
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogDoKpiYesterday.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Missing Data")
                        }

            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogDoKpiYesterday.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForDoYesterday()
                }
            } catch (e: ApolloException) {
                progressDialogDoKpiYesterday.dismissProgressDialog()
                Logger.error(e.message.toString(), "Yesterday Missing Data")
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun callDoYesterdayLevelOneQuery() {
        val progressDialogDoKpiYesterday = CustomProgressDialog(requireActivity())
        progressDialogDoKpiYesterday.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            if (networkHelper.isNetworkConnected()) {
                hideErrorScreenVisibleStateForDOYesterday()
            }
            val areaCodeDoKpiYesterday = dbHelper.getAllSelectedAreaList(true)
            val stateCodeDoKpiYesterday = dbHelper.getAllSelectedStoreListState(true)
            val supervisorNumberDoKpiYesterday = dbHelper.getAllSelectedStoreListSupervisor(true)
            val storeNumberDoKpiYesterday = dbHelper.getAllSelectedStoreList(true)

            Logger.info(
                    DoYesterdayLevelOneQuery.OPERATION_NAME.name(), "Yesterday Level 1 KPI",
                    mapQueryFilters(
                            areaCodeDoKpiYesterday,
                            stateCodeDoKpiYesterday,
                            supervisorNumberDoKpiYesterday,
                            storeNumberDoKpiYesterday,
                            DoYesterdayLevelOneQuery.QUERY_DOCUMENT
                    )
            )
            try {

                val responseDoYesterdayLevelOne =
                        apolloClient(requireContext()).query(
                                DoYesterdayLevelOneQuery(
                                        areaCodeDoKpiYesterday.toInput(),
                                        stateCodeDoKpiYesterday.toInput(),
                                        supervisorNumberDoKpiYesterday.toInput(),
                                        storeNumberDoKpiYesterday.toInput()
                                )
                        )
                                .await()
                if (responseDoYesterdayLevelOne.data?.do_ != null) {
                    progressDialogDoKpiYesterday.dismissProgressDialog()
                    doYesterdayLevelOne = responseDoYesterdayLevelOne.data?.do_!!
                    if (doYesterdayLevelOne.kpis?.supervisors?.stores?.yesterday != null) {
                        setDoYesterdayLevelOneStoreData(doYesterdayLevelOne.kpis?.supervisors?.stores?.yesterday)
                    } else {
                        setErrorScreenVisibleStateForDoYesterday(
                                getString(R.string.error_text_title),
                                getString(R.string.error_text_description)
                        )
                    }

                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogDoKpiYesterday.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Level 1 KPI")
                        }
                refreshYesterdayDoToken()
                return@launchWhenResumed

            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogDoKpiYesterday.dismissProgressDialog()
                setInternetErrorScreenVisibleStateForDoYesterday()
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Yesterday Level 1 KPI")
                progressDialogDoKpiYesterday.dismissProgressDialog()
                setErrorScreenVisibleStateForDoYesterday(
                        getString(R.string.exception_error_text_title), getString(
                        R.string
                                .exception_error_text_description
                )
                )

            }

        }
    }


    fun callDoYesterdayLevelTwoQuery(
            actionForDoYesterdayLevel2: String,
            rcvForDoYesterdayLevel2: NonScrollExpandableListView
    ) {
        val progressDialogDoYesterdayLevel2 = CustomProgressDialog(requireActivity())
        lifecycleScope.launchWhenResumed {
            progressDialogDoYesterdayLevel2.showProgressDialog()
            val areaCodeDoYesterdayLevel2 = dbHelper.getAllSelectedAreaList(true)
            val stateCodeDoYesterdayLevel2 = dbHelper.getAllSelectedStoreListState(true)
            val superVisorNumberListDoYesterdayLevel2 = dbHelper.getAllSelectedStoreListSupervisor(true)

            Logger.info(
                    DOYesterdayLevelTwoQuery.OPERATION_NAME.name(), "Yesterday Level 2 KPI",
                    mapQueryFilters(
                            areaCodeDoYesterdayLevel2,
                            stateCodeDoYesterdayLevel2,
                            superVisorNumberListDoYesterdayLevel2,
                            Collections.emptyList(),
                            DOYesterdayLevelTwoQuery.QUERY_DOCUMENT
                    )
            )


            try {
                val responseDoYesterdayKpi =
                        apolloClient(requireContext()).query(
                                DOYesterdayLevelTwoQuery(
                                        areaCodeDoYesterdayLevel2.toInput(),
                                        stateCodeDoYesterdayLevel2.toInput(),
                                        superVisorNumberListDoYesterdayLevel2.toInput(),
                                )
                        )
                                .await()

                if (responseDoYesterdayKpi.data?.do_ != null) {
                    progressDialogDoYesterdayLevel2.dismissProgressDialog()
                    doYesterdayLevelTwo = responseDoYesterdayKpi.data?.do_!!

                    responseDoYesterdayKpi.data?.do_?.kpis?.individualSupervisors.let {
                        setExpandableDataDoYesterdayKpi(actionForDoYesterdayLevel2, rcvForDoYesterdayLevel2)
                    }
                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogDoYesterdayLevel2.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Level 2 KPI")
                        }
                refreshYesterdayDoToken()
                return@launchWhenResumed

            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogDoYesterdayLevel2.dismissProgressDialog()
                setInternetErrorScreenVisibleStateForDoYesterday()
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Yesterday Level 2 KPI")
                progressDialogDoYesterdayLevel2.dismissProgressDialog()
                setErrorScreenVisibleStateForDoYesterday(
                        getString(R.string.exception_error_text_title), getString(
                        R.string.exception_error_text_description
                )
                )

            }

        }
    }

    private fun callOverViewDOYesterdayKpiApi(
            actionYesterdayDo: String,
            superVisorNumber: String,
            storeNumber: String
    ) {
        lifecycleScope.launchWhenResumed {
            val superVisorNumberListOverViewDO = mutableListOf<String>()
            if (superVisorNumber.isNotEmpty() || superVisorNumber.isNotBlank()) {
                superVisorNumberListOverViewDO.add(superVisorNumber)
            }

            val storeNumberListOverViewDO = mutableListOf<String>()
            if (storeNumber.isNotEmpty() || storeNumber.isNotBlank()) {
                storeNumberListOverViewDO.add(storeNumber)
            } else {

                storeNumberListOverViewDO.addAll(dbHelper.getAllSelectedStoreList(true))
            }

            val progressDialogOverViewDO = CustomProgressDialog(requireActivity())
            progressDialogOverViewDO.showProgressDialog()

            try {
                val response =
                        apolloClient(requireContext()).query(
                                DOOverviewYesterdayQuery(
                                        superVisorNumberListOverViewDO.toInput(),
                                        storeNumberListOverViewDO.toInput()
                                )
                        )
                                .await()

                if (response.data?.do_ != null) {
                    progressDialogOverViewDO.dismissProgressDialog()
                    when (actionYesterdayDo) {
                        getString(R.string.awus_text) -> {
                            openYesterdayDoSalesDetail(response.data?.do_!!)
                        }
                        getString(R.string.food_text) -> {
                            openYesterdayDoFoodDetail(response.data?.do_!!)
                        }
                        getString(R.string.labour_text) -> {
                            openYesterdayDoLabourDetail(response.data?.do_!!)
                        }
                        getString(R.string.service_text) -> {
                            openYesterdayDoServiceDetail(response.data?.do_!!)
                        }
                        getString(R.string.oer_text) -> {
                            openYesterdayDoOERDetail(response.data?.do_!!)
                        }
                        getString(R.string.cash_text) -> {
                            openYesterdayDoCashDetail(response.data?.do_!!)
                        }
                    }
                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogOverViewDO.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Overview KPI")
                        }
                refreshYesterdayDoToken()
                return@launchWhenResumed
            } catch (e: ApolloNetworkException) {
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForDoYesterday()
                }
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Yesterday Overview")
                progressDialogOverViewDO.dismissProgressDialog()
                setErrorScreenVisibleStateForDoYesterday(
                        getString(R.string.exception_error_text_title), getString(
                        R.string
                                .exception_error_text_description
                )
                )

            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setDoYesterdayLevelOneStoreData(detailYesterdayKpi: DoYesterdayLevelOneQuery.Yesterday?) {

        val strSelectedDate: String? =
                detailYesterdayKpi?.periodFrom?.let {
                    detailYesterdayKpi.periodTo?.let {it1 ->
                        EnumMapperUtil.getSelectedDate(
                                it,
                                it1, EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                        )
                    }
                }
        if (strSelectedDate != null) {
            StorePrefData.filterDate = strSelectedDate
            setStoreFilterViewForDoYesterday(StorePrefData.filterDate)
        }

        val doYesterdaySalesValue = Validation().checkAmountPercentageValue(
                requireActivity(),
                detailYesterdayKpi?.sales?.actual?.amount,
                detailYesterdayKpi?.sales?.actual?.percentage,
                detailYesterdayKpi?.sales?.actual?.value
        )

        if (doYesterdaySalesValue.isEmpty()) {
            hideVisibilityStateOfSalesDataForDOYesterday()
        } else {
            showVisibilityStateOfSalesDataForDOYesterday(doYesterdaySalesValue)
        }

        displaySalesViewForDoYesterday(detailYesterdayKpi?.sales)
        displayFoodViewForDoYesterday(detailYesterdayKpi?.food)
        displayLaborViewForDoYesterday(detailYesterdayKpi?.labor)
        displayViewForServiceValues(detailYesterdayKpi?.service)
        displayExtremeServiceViewForDoYesterday(detailYesterdayKpi?.service?.extremeDelivery)
        displaySinglesServiceViewForDoYesterday(detailYesterdayKpi?.service?.singles)
        displayCashViewForDoYesterday(detailYesterdayKpi?.cash)
        displayOERViewForDoYesterday(detailYesterdayKpi?.oerStart)
    }

    fun setStoreFilterViewForDoYesterday(date: String){
            val periodText = "$date | ${getString(R.string.yesterday_text)}"
            Validation().validateFilterKPI(requireActivity(), dbHelper, common_header_do_yesterday.store_header!!, periodText)
    }

    fun hideVisibilityStateOfSalesDataForDOYesterday(){
        common_header_do_yesterday.total_sales_common_header.visibility = View.GONE
        common_header_do_yesterday.sales_text_common_header.visibility = View.GONE
        common_header_do_yesterday.sales_header_error_image.visibility = View.VISIBLE
        common_header_do_yesterday.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)

    }
    fun showVisibilityStateOfSalesDataForDOYesterday(doYesterdaySalesValue: String) {
        common_header_do_yesterday.total_sales_common_header.visibility = View.VISIBLE
        common_header_do_yesterday.total_sales_common_header.text = doYesterdaySalesValue
        common_header_do_yesterday.sales_text_common_header.visibility = View.VISIBLE
        common_header_do_yesterday.sales_text_common_header.text = getString(R.string.sales_text)
        common_header_do_yesterday.sales_header_error_image.visibility = View.GONE
    }


    fun displaySalesViewForDoYesterday(sales: DoYesterdayLevelOneQuery.Sales?) {
        if (sales?.displayName != null) {
            aws_display_do_yesterday_kpi.text = sales.displayName
        }
        val salesDoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.goal?.amount,
                sales?.goal?.percentage,
                sales?.goal?.value
        )
        val salesDoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.variance?.amount,
                sales?.variance?.percentage,
                sales?.variance?.value
        )
        val salesDoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.actual?.amount,
                sales?.actual?.percentage,
                sales?.actual?.value
        )

        if (salesDoGoalYesterday.isEmpty() && salesDoVarianceYesterday.isEmpty() && salesDoActualYesterday.isEmpty()) {

            sales_error_do_yesterday_kpi.visibility = View.VISIBLE
            val paramDOOYesterdayAWUSError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOOYesterdayAWUSError.weight = 2.0f
            aws_display_do_yesterday_kpi.layoutParams = paramDOOYesterdayAWUSError

            sales_goal_do_yesterday_kpi.visibility = View.GONE
            sales_variance_do_yesterday_kpi.visibility = View.GONE
            sales_actual_do_yesterday_kpi.visibility = View.GONE
            aws_parent_layout_do_yesterday_kpi.isClickable = false
            aws_parent_img_do_yesterday_kpi.visibility = View.GONE
        } else if (salesDoGoalYesterday.isNotEmpty() && salesDoVarianceYesterday.isNotEmpty() && salesDoActualYesterday.isNotEmpty()) {

            sales_error_do_yesterday_kpi.visibility = View.GONE
            sales_goal_do_yesterday_kpi.visibility = View.VISIBLE
            sales_variance_do_yesterday_kpi.visibility = View.VISIBLE
            sales_actual_do_yesterday_kpi.visibility = View.VISIBLE

            sales_goal_do_yesterday_kpi.text = salesDoGoalYesterday
            sales_variance_do_yesterday_kpi.text = salesDoVarianceYesterday
            sales_actual_do_yesterday_kpi.text = salesDoActualYesterday
        } else {

            sales_error_do_yesterday_kpi.visibility = View.GONE
            sales_goal_do_yesterday_kpi.visibility = View.VISIBLE
            sales_variance_do_yesterday_kpi.visibility = View.VISIBLE
            sales_actual_do_yesterday_kpi.visibility = View.VISIBLE

            if (salesDoGoalYesterday.isEmpty()) {
                sales_goal_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_goal_do_yesterday_kpi.text = salesDoGoalYesterday
            }

            if (salesDoVarianceYesterday.isEmpty()) {
                sales_variance_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_variance_do_yesterday_kpi.text = salesDoVarianceYesterday
            }

            if (salesDoActualYesterday.isEmpty()) {
                sales_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_actual_do_yesterday_kpi.text = salesDoActualYesterday
            }
        }

        if (sales?.status?.toString() != null && salesDoActualYesterday.isNotEmpty()) {
            when {
                sales.status.toString() == resources.getString(R.string.out_of_range) -> {
                    sales_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    sales_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                sales.status.toString() == resources.getString(R.string.under_limit) -> {
                    sales_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    sales_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    sales_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    sales_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

    }

    fun displayFoodViewForDoYesterday(food: DoYesterdayLevelOneQuery.Food?) {
        if (food?.displayName != null) {
            food_display_do_yesterday_kpi.text = food.displayName
        }

        val foodDoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.goal?.amount,
                food?.goal?.percentage,
                food?.goal?.value
        )
        val foodDoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.variance?.amount,
                food?.variance?.percentage,
                food?.variance?.value
        )
        val foodDoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.actual?.amount,
                food?.actual?.percentage,
                food?.actual?.value
        )

        if (foodDoGoalYesterday.isEmpty() && foodDoVarianceYesterday.isEmpty() && foodDoActualYesterday.isEmpty()) {

            food_error_do_yesterday_kpi.visibility = View.VISIBLE
            val paramDOOYesterdayFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOOYesterdayFoodError.weight = 2.0f
            food_display_do_yesterday_kpi.layoutParams = paramDOOYesterdayFoodError

            food_goal_do_yesterday_kpi.visibility = View.GONE
            food_variance_do_yesterday_kpi.visibility = View.GONE
            food_actual_do_yesterday_kpi.visibility = View.GONE
            food_parent_layout_do_yesterday_kpi.isClickable = false
            food_parent_layout_do_yesterday_kpi.visibility = View.GONE
        } else if (foodDoGoalYesterday.isNotEmpty() && foodDoVarianceYesterday.isNotEmpty() && foodDoActualYesterday.isNotEmpty()) {

            food_error_do_yesterday_kpi.visibility = View.GONE

            food_goal_do_yesterday_kpi.visibility = View.VISIBLE
            food_variance_do_yesterday_kpi.visibility = View.VISIBLE
            food_actual_do_yesterday_kpi.visibility = View.VISIBLE

            food_goal_do_yesterday_kpi.text = foodDoGoalYesterday
            food_variance_do_yesterday_kpi.text = foodDoVarianceYesterday
            food_actual_do_yesterday_kpi.text = foodDoActualYesterday
        } else {

            food_error_do_yesterday_kpi.visibility = View.GONE
            food_goal_do_yesterday_kpi.visibility = View.VISIBLE
            food_variance_do_yesterday_kpi.visibility = View.VISIBLE
            food_actual_do_yesterday_kpi.visibility = View.VISIBLE

            if (foodDoGoalYesterday.isEmpty()) {
                food_goal_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_goal_do_yesterday_kpi.text = foodDoGoalYesterday
            }

            if (foodDoVarianceYesterday.isEmpty()) {
                food_variance_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_variance_do_yesterday_kpi.text = foodDoVarianceYesterday
            }

            if (foodDoActualYesterday.isEmpty()) {
                food_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_actual_do_yesterday_kpi.text = foodDoActualYesterday
            }

        }

        if (food?.status?.toString() != null && foodDoActualYesterday.isNotEmpty()) {
            when {
                food.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.red_circle, 0)
                    food_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                food.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.green_circle, 0)
                    food_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    food_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.black_circle, 0)
                    food_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

    }

    fun displayLaborViewForDoYesterday(labor: DoYesterdayLevelOneQuery.Labor?) {
        if (labor?.displayName != null) {
            labour_display_do_yesterday_kpi.text = labor.displayName
        }

        val labourDoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.goal?.amount,
                labor?.goal?.percentage,
                labor?.goal?.value
        )
        val labourDoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.variance?.amount,
                labor?.variance?.percentage,
                labor?.variance?.value
        )
        val labourDoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.actual?.amount,
                labor?.actual?.percentage,
                labor?.actual?.value
        )

        if (labourDoGoalYesterday.isEmpty() && labourDoVarianceYesterday.isEmpty() && labourDoActualYesterday.isEmpty()) {

            labour_error_do_yesterday_kpi.visibility = View.VISIBLE

            val paramDOOYesterdayLabourError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOOYesterdayLabourError.weight = 2.0f
            labour_display_do_yesterday_kpi.layoutParams = paramDOOYesterdayLabourError

            labour_goal_do_yesterday_kpi.visibility = View.GONE
            labour_variance_do_yesterday_kpi.visibility = View.GONE
            labour_actual_do_yesterday_kpi.visibility = View.GONE
            labor_parent_layout_do_yesterday_kpi.isClickable = false
            labor_parent_img_do_yesterday_kpi.visibility = View.GONE
        } else if (labourDoGoalYesterday.isNotEmpty() && labourDoVarianceYesterday.isNotEmpty() && labourDoActualYesterday.isNotEmpty()) {

            labour_error_do_yesterday_kpi.visibility = View.GONE

            labour_goal_do_yesterday_kpi.visibility = View.VISIBLE
            labour_variance_do_yesterday_kpi.visibility = View.VISIBLE
            labour_actual_do_yesterday_kpi.visibility = View.VISIBLE

            labour_goal_do_yesterday_kpi.text = labourDoGoalYesterday
            labour_variance_do_yesterday_kpi.text = labourDoVarianceYesterday
            labour_actual_do_yesterday_kpi.text = labourDoActualYesterday
        } else {

            labour_error_do_yesterday_kpi.visibility = View.GONE
            labour_goal_do_yesterday_kpi.visibility = View.VISIBLE
            labour_variance_do_yesterday_kpi.visibility = View.VISIBLE
            labour_actual_do_yesterday_kpi.visibility = View.VISIBLE

            if (labourDoGoalYesterday.isEmpty()) {
                labour_goal_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_goal_do_yesterday_kpi.text = labourDoGoalYesterday
            }

            if (labourDoVarianceYesterday.isEmpty()) {
                labour_variance_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_variance_do_yesterday_kpi.text = labourDoVarianceYesterday
            }

            if (labourDoActualYesterday.isEmpty()) {
                labour_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_actual_do_yesterday_kpi.text = labourDoActualYesterday
            }

        }

        if (labor?.status != null && labourDoActualYesterday.isNotEmpty()) {
            when {
                labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    labour_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                labor.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    labour_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    labour_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    labour_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

    }

    fun displayViewForServiceValues(service: DoYesterdayLevelOneQuery.Service?) {
        if (service?.displayName != null) {
            service_display_do_yesterday_kpi.text = service.displayName
        }
        if (service?.eADT?.displayName != null) {
            eadt_display_do_yesterday_kpi.text = service.eADT.displayName
        }
        if (service?.extremeDelivery?.displayName != null) {
            extreme_delivery_display_do_yesterday_kpi.text = service.extremeDelivery.displayName
        }
        if (service?.singles?.displayName != null) {
            single_display_do_yesterday_kpi.text = service.singles.displayName
        }

        val serviceDoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.goal?.amount,
                service?.eADT?.goal?.percentage,
                service?.eADT?.goal?.value
        )
        val serviceDoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.variance?.amount,
                service?.eADT?.variance?.percentage,
                service?.eADT?.variance?.value
        )
        val serviceDoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.actual?.amount,
                service?.eADT?.actual?.percentage,
                service?.eADT?.actual?.value
        )

        if (serviceDoGoalYesterday.isEmpty() && serviceDoVarianceYesterday.isEmpty() && serviceDoActualYesterday.isEmpty()) {

            service_error_do_yesterday_kpi.visibility = View.VISIBLE
            val paramDOOYesterdayEADTError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOOYesterdayEADTError.weight = 2.0f
            eadt_display_do_yesterday_kpi.layoutParams = paramDOOYesterdayEADTError

            service_eadt_goal_do_yesterday_kpi.visibility = View.GONE
            service_eadt_variance_do_yesterday_kpi.visibility = View.GONE
            service_eadt_actual_do_yesterday_kpi.visibility = View.GONE
            service_eADT_parent_img_do_yesterday_kpi.visibility = View.GONE
        } else if (serviceDoGoalYesterday.isNotEmpty() && serviceDoVarianceYesterday.isNotEmpty() && serviceDoActualYesterday.isNotEmpty()) {

            service_error_do_yesterday_kpi.visibility = View.GONE
            service_eadt_goal_do_yesterday_kpi.visibility = View.VISIBLE
            service_eadt_variance_do_yesterday_kpi.visibility = View.VISIBLE
            service_eadt_actual_do_yesterday_kpi.visibility = View.VISIBLE

            service_eadt_goal_do_yesterday_kpi.text = serviceDoGoalYesterday
            service_eadt_variance_do_yesterday_kpi.text = serviceDoVarianceYesterday
            service_eadt_actual_do_yesterday_kpi.text = serviceDoActualYesterday
        } else {

            service_error_do_yesterday_kpi.visibility = View.GONE
            service_eadt_goal_do_yesterday_kpi.visibility = View.VISIBLE
            service_eadt_variance_do_yesterday_kpi.visibility = View.VISIBLE
            service_eadt_actual_do_yesterday_kpi.visibility = View.VISIBLE

            if (serviceDoGoalYesterday.isEmpty()) {
                service_eadt_goal_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_goal_do_yesterday_kpi.text = serviceDoGoalYesterday
            }

            if (serviceDoVarianceYesterday.isEmpty()) {
                service_eadt_variance_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_variance_do_yesterday_kpi.text = serviceDoVarianceYesterday
            }

            if (serviceDoActualYesterday.isEmpty()) {
                service_eadt_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_actual_do_yesterday_kpi.text = serviceDoActualYesterday
            }

        }

        if (service?.eADT?.status != null && serviceDoActualYesterday.isNotEmpty()) {
            when {
                service.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_eadt_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_eadt_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                service.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_eadt_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_eadt_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_eadt_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_eadt_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        // service extreme
        val serviceExtremeDoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.extremeDelivery?.goal?.amount,
                service?.extremeDelivery?.goal?.percentage,
                service?.extremeDelivery?.goal?.value
        )
        val serviceExtremeDoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.extremeDelivery?.variance?.amount,
                service?.extremeDelivery?.variance?.percentage,
                service?.extremeDelivery?.variance?.value
        )
        val serviceExtremeDoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.extremeDelivery?.actual?.amount,
                service?.extremeDelivery?.actual?.percentage,
                service?.extremeDelivery?.actual?.value
        )

        if (serviceExtremeDoGoalYesterday.isEmpty() && serviceExtremeDoVarianceYesterday.isEmpty() && serviceExtremeDoActualYesterday.isEmpty()) {

            serviceExtreme_error_do_yesterday_kpi.visibility = View.VISIBLE
            val paramDOOYesterdayExtremeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOOYesterdayExtremeError.weight = 2.0f
            extreme_delivery_display_do_yesterday_kpi.layoutParams = paramDOOYesterdayExtremeError

            service_extreme_goal_do_yesterday_kpi.visibility = View.GONE
            service_extreme_variance_do_yesterday_kpi.visibility = View.GONE
            service_extreme_actual_do_yesterday_kpi.visibility = View.GONE
            service_extreme_parent_img_do_yesterday_kpi.visibility = View.GONE
        } else if (serviceExtremeDoGoalYesterday.isNotEmpty() && serviceExtremeDoVarianceYesterday.isNotEmpty() && serviceExtremeDoActualYesterday.isNotEmpty()) {

            serviceExtreme_error_do_yesterday_kpi.visibility = View.GONE
            service_extreme_goal_do_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_variance_do_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_actual_do_yesterday_kpi.visibility = View.VISIBLE

            service_extreme_goal_do_yesterday_kpi.text = serviceExtremeDoGoalYesterday
            service_extreme_variance_do_yesterday_kpi.text = serviceExtremeDoVarianceYesterday
            service_extreme_actual_do_yesterday_kpi.text = serviceExtremeDoActualYesterday
        } else {

            serviceExtreme_error_do_yesterday_kpi.visibility = View.GONE
            service_extreme_goal_do_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_variance_do_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_actual_do_yesterday_kpi.visibility = View.VISIBLE

            if (serviceExtremeDoGoalYesterday.isEmpty()) {
                service_extreme_goal_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_goal_do_yesterday_kpi.text = serviceExtremeDoGoalYesterday
            }

            if (serviceExtremeDoVarianceYesterday.isEmpty()) {
                service_extreme_variance_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_variance_do_yesterday_kpi.text = serviceExtremeDoVarianceYesterday
            }

            if (serviceExtremeDoActualYesterday.isEmpty()) {
                service_extreme_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_actual_do_yesterday_kpi.text = serviceExtremeDoActualYesterday
            }

        }


        if (service?.extremeDelivery?.status != null && serviceExtremeDoActualYesterday.isNotEmpty()) {
            when {
                service.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_extreme_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                service.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_extreme_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_extreme_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    service_extreme_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_extreme_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }


        // service singles
        val serviceSinglesDoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.singles?.goal?.amount,
                service?.singles?.goal?.percentage,
                service?.singles?.goal?.value
        )
        val serviceSinglesDoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.singles?.variance?.amount,
                service?.singles?.variance?.percentage,
                service?.singles?.variance?.value
        )
        val serviceSinglesDoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.singles?.actual?.amount,
                service?.singles?.actual?.percentage,
                service?.singles?.actual?.value
        )

        if (serviceSinglesDoGoalYesterday.isEmpty() && serviceSinglesDoVarianceYesterday.isEmpty() && serviceSinglesDoActualYesterday.isEmpty()) {

            serviceSingles_error_do_yesterday_kpi.visibility = View.VISIBLE
            val paramDOOYesterdaySingleError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOOYesterdaySingleError.weight = 2.0f
            single_display_do_yesterday_kpi.layoutParams = paramDOOYesterdaySingleError

            service_singles_goal_do_yesterday_kpi.visibility = View.GONE
            service_singles_variance_do_yesterday_kpi.visibility = View.GONE
            service_singles_actual_do_yesterday_kpi.visibility = View.GONE
            service_single_parent_img_do_yesterday_kpi.visibility = View.GONE
        } else if (serviceSinglesDoGoalYesterday.isNotEmpty() && serviceSinglesDoVarianceYesterday.isNotEmpty() && serviceSinglesDoActualYesterday.isNotEmpty()) {

            serviceSingles_error_do_yesterday_kpi.visibility = View.GONE
            service_singles_goal_do_yesterday_kpi.visibility = View.VISIBLE
            service_singles_variance_do_yesterday_kpi.visibility = View.VISIBLE
            service_singles_actual_do_yesterday_kpi.visibility = View.VISIBLE

            service_singles_goal_do_yesterday_kpi.text = serviceSinglesDoGoalYesterday
            service_singles_variance_do_yesterday_kpi.text = serviceSinglesDoVarianceYesterday
            service_singles_actual_do_yesterday_kpi.text = serviceSinglesDoActualYesterday
        } else {

            serviceSingles_error_do_yesterday_kpi.visibility = View.GONE
            service_singles_goal_do_yesterday_kpi.visibility = View.VISIBLE
            service_singles_variance_do_yesterday_kpi.visibility = View.VISIBLE
            service_singles_actual_do_yesterday_kpi.visibility = View.VISIBLE

            if (serviceSinglesDoGoalYesterday.isEmpty()) {
                service_singles_goal_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_goal_do_yesterday_kpi.text = serviceSinglesDoGoalYesterday
            }

            if (serviceSinglesDoVarianceYesterday.isEmpty()) {
                service_singles_variance_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_variance_do_yesterday_kpi.text = serviceSinglesDoVarianceYesterday
            }

            if (serviceSinglesDoActualYesterday.isEmpty()) {
                service_singles_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_actual_do_yesterday_kpi.text = serviceSinglesDoActualYesterday
            }

        }

        if (service?.singles?.status != null && serviceSinglesDoActualYesterday.isNotEmpty()) {
            when {
                service.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_singles_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                service.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_singles_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_singles_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_singles_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_singles_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }


    }

    fun displayExtremeServiceViewForDoYesterday(extremeDelivery: DoYesterdayLevelOneQuery.ExtremeDelivery?) {
        if (extremeDelivery?.displayName != null) {
            extreme_delivery_display_do_yesterday_kpi.text = extremeDelivery.displayName
        }

        val serviceExtremeDoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.goal?.amount,
                extremeDelivery?.goal?.percentage,
                extremeDelivery?.goal?.value
        )
        val serviceExtremeDoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.variance?.amount,
                extremeDelivery?.variance?.percentage,
                extremeDelivery?.variance?.value
        )
        val serviceExtremeDoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.actual?.amount,
                extremeDelivery?.actual?.percentage,
                extremeDelivery?.actual?.value
        )

        if (serviceExtremeDoGoalYesterday.isEmpty() && serviceExtremeDoVarianceYesterday.isEmpty() && serviceExtremeDoActualYesterday.isEmpty()) {

            serviceExtreme_error_do_yesterday_kpi.visibility = View.VISIBLE
            val paramDOOYesterdayExtremeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOOYesterdayExtremeError.weight = 2.0f
            extreme_delivery_display_do_yesterday_kpi.layoutParams = paramDOOYesterdayExtremeError

            service_extreme_goal_do_yesterday_kpi.visibility = View.GONE
            service_extreme_variance_do_yesterday_kpi.visibility = View.GONE
            service_extreme_actual_do_yesterday_kpi.visibility = View.GONE
            service_extreme_parent_img_do_yesterday_kpi.visibility = View.GONE
        } else if (serviceExtremeDoGoalYesterday.isNotEmpty() && serviceExtremeDoVarianceYesterday.isNotEmpty() && serviceExtremeDoActualYesterday.isNotEmpty()) {

            serviceExtreme_error_do_yesterday_kpi.visibility = View.GONE
            service_extreme_goal_do_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_variance_do_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_actual_do_yesterday_kpi.visibility = View.VISIBLE

            service_extreme_goal_do_yesterday_kpi.text = serviceExtremeDoGoalYesterday
            service_extreme_variance_do_yesterday_kpi.text = serviceExtremeDoVarianceYesterday
            service_extreme_actual_do_yesterday_kpi.text = serviceExtremeDoActualYesterday
        } else {

            serviceExtreme_error_do_yesterday_kpi.visibility = View.GONE
            service_extreme_goal_do_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_variance_do_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_actual_do_yesterday_kpi.visibility = View.VISIBLE

            if (serviceExtremeDoGoalYesterday.isEmpty()) {
                service_extreme_goal_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_goal_do_yesterday_kpi.text = serviceExtremeDoGoalYesterday
            }

            if (serviceExtremeDoVarianceYesterday.isEmpty()) {
                service_extreme_variance_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_variance_do_yesterday_kpi.text = serviceExtremeDoVarianceYesterday
            }

            if (serviceExtremeDoActualYesterday.isEmpty()) {
                service_extreme_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_actual_do_yesterday_kpi.text = serviceExtremeDoActualYesterday
            }

        }


        if (extremeDelivery?.status != null && serviceExtremeDoActualYesterday.isNotEmpty()) {
            when {
                extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_extreme_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_extreme_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_extreme_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    service_extreme_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_extreme_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }


    }

    fun displaySinglesServiceViewForDoYesterday(singles: DoYesterdayLevelOneQuery.Singles?) {
        if (singles?.displayName != null) {
            single_display_do_yesterday_kpi.text = singles.displayName
        }
        val serviceSinglesDoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.goal?.amount,
                singles?.goal?.percentage,
                singles?.goal?.value
        )
        val serviceSinglesDoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.variance?.amount,
                singles?.variance?.percentage,
                singles?.variance?.value
        )
        val serviceSinglesDoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.actual?.amount,
                singles?.actual?.percentage,
                singles?.actual?.value
        )

        if (serviceSinglesDoGoalYesterday.isEmpty() && serviceSinglesDoVarianceYesterday.isEmpty() && serviceSinglesDoActualYesterday.isEmpty()) {

            serviceSingles_error_do_yesterday_kpi.visibility = View.VISIBLE
            val paramDOOYesterdaySingleError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOOYesterdaySingleError.weight = 2.0f
            single_display_do_yesterday_kpi.layoutParams = paramDOOYesterdaySingleError

            service_singles_goal_do_yesterday_kpi.visibility = View.GONE
            service_singles_variance_do_yesterday_kpi.visibility = View.GONE
            service_singles_actual_do_yesterday_kpi.visibility = View.GONE
            service_single_parent_img_do_yesterday_kpi.visibility = View.GONE
        } else if (serviceSinglesDoGoalYesterday.isNotEmpty() && serviceSinglesDoVarianceYesterday.isNotEmpty() && serviceSinglesDoActualYesterday.isNotEmpty()) {

            serviceSingles_error_do_yesterday_kpi.visibility = View.GONE
            service_singles_goal_do_yesterday_kpi.visibility = View.VISIBLE
            service_singles_variance_do_yesterday_kpi.visibility = View.VISIBLE
            service_singles_actual_do_yesterday_kpi.visibility = View.VISIBLE

            service_singles_goal_do_yesterday_kpi.text = serviceSinglesDoGoalYesterday
            service_singles_variance_do_yesterday_kpi.text = serviceSinglesDoVarianceYesterday
            service_singles_actual_do_yesterday_kpi.text = serviceSinglesDoActualYesterday
        } else {

            serviceSingles_error_do_yesterday_kpi.visibility = View.GONE
            service_singles_goal_do_yesterday_kpi.visibility = View.VISIBLE
            service_singles_variance_do_yesterday_kpi.visibility = View.VISIBLE
            service_singles_actual_do_yesterday_kpi.visibility = View.VISIBLE

            if (serviceSinglesDoGoalYesterday.isEmpty()) {
                service_singles_goal_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_goal_do_yesterday_kpi.text = serviceSinglesDoGoalYesterday
            }

            if (serviceSinglesDoVarianceYesterday.isEmpty()) {
                service_singles_variance_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_variance_do_yesterday_kpi.text = serviceSinglesDoVarianceYesterday
            }

            if (serviceSinglesDoActualYesterday.isEmpty()) {
                service_singles_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_actual_do_yesterday_kpi.text = serviceSinglesDoActualYesterday
            }

        }

        if (singles?.status != null && serviceSinglesDoActualYesterday.isNotEmpty()) {
            when {
                singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_singles_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_singles_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_singles_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_singles_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_singles_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayCashViewForDoYesterday(cash: DoYesterdayLevelOneQuery.Cash?) {
        if (cash?.displayName != null) {
            cash_display_do_yesterday_kpi.text = cash.displayName
        }

        val cashDoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.goal?.amount,
                cash?.goal?.percentage,
                cash?.goal?.value
        )
        val cashDoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.variance?.amount,
                cash?.variance?.percentage,
                cash?.variance?.value
        )
        val cashDoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.actual?.amount,
                cash?.actual?.percentage,
                cash?.actual?.value
        )

        if (cashDoGoalYesterday.isEmpty() && cashDoVarianceYesterday.isEmpty() && cashDoActualYesterday.isEmpty()) {

            cash_error_do_yesterday_kpi.visibility = View.VISIBLE
            val paramDOOYesterdayCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOOYesterdayCashError.weight = 2.0f
            cash_display_do_yesterday_kpi.layoutParams = paramDOOYesterdayCashError

            cash_goal_do_yesterday_kpi.visibility = View.GONE
            cash_variance_do_yesterday_kpi.visibility = View.GONE
            cash_actual_do_yesterday_kpi.visibility = View.GONE
            cash_parent_layout_do_yesterday_kpi.isClickable = false
            cash_parent_img_do_yesterday_kpi.visibility = View.GONE
        } else if (cashDoGoalYesterday.isNotEmpty() && cashDoVarianceYesterday.isNotEmpty() && cashDoActualYesterday.isNotEmpty()) {

            cash_error_do_yesterday_kpi.visibility = View.GONE
            cash_goal_do_yesterday_kpi.visibility = View.VISIBLE
            cash_variance_do_yesterday_kpi.visibility = View.VISIBLE
            cash_actual_do_yesterday_kpi.visibility = View.VISIBLE

            cash_goal_do_yesterday_kpi.text = cashDoGoalYesterday
            cash_variance_do_yesterday_kpi.text = cashDoVarianceYesterday
            cash_actual_do_yesterday_kpi.text = cashDoActualYesterday
        } else {

            cash_error_do_yesterday_kpi.visibility = View.GONE
            cash_goal_do_yesterday_kpi.visibility = View.VISIBLE
            cash_variance_do_yesterday_kpi.visibility = View.VISIBLE
            cash_actual_do_yesterday_kpi.visibility = View.VISIBLE

            if (cashDoGoalYesterday.isEmpty()) {
                cash_goal_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_goal_do_yesterday_kpi.text = cashDoGoalYesterday
            }

            if (cashDoVarianceYesterday.isEmpty()) {
                cash_variance_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_variance_do_yesterday_kpi.text = cashDoVarianceYesterday
            }

            if (cashDoActualYesterday.isEmpty()) {
                cash_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_actual_do_yesterday_kpi.text = cashDoActualYesterday
            }

        }

        if (cash?.status != null && cashDoActualYesterday.isNotEmpty()) {
            when {
                cash.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    cash_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                cash.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    cash_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    cash_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    cash_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }

        }


    }

    fun displayOERViewForDoYesterday(oerStart: DoYesterdayLevelOneQuery.OerStart?) {
        if (oerStart?.displayName != null) {
            oer_display_do_yesterday_kpi.text = oerStart.displayName
        }

        val oerDoRGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.goal?.amount,
                oerStart?.goal?.percentage,
                oerStart?.goal?.value
        )
        val oerDoRVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.variance?.amount,
                oerStart?.variance?.percentage,
                oerStart?.variance?.value
        )
        val oerDoRActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.actual?.amount,
                oerStart?.actual?.percentage,
                oerStart?.actual?.value
        )

        if (oerDoRGoalYesterday.isEmpty() && oerDoRVarianceYesterday.isEmpty() && oerDoRActualYesterday.isEmpty()) {

            oer_error_do_yesterday_kpi.visibility = View.VISIBLE
            val paramDOOYesterdayOERError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOOYesterdayOERError.weight = 2.0f
            oer_display_do_yesterday_kpi.layoutParams = paramDOOYesterdayOERError

            oer_goal_do_yesterday_kpi.visibility = View.GONE
            oer_variance_do_yesterday_kpi.visibility = View.GONE
            oer_actual_do_yesterday_kpi.visibility = View.GONE
            oer_parent_layout_do_yesterday_kpi.isClickable = false
            oer_parent_img_do_yesterday_kpi.visibility = View.GONE
        } else if (oerDoRGoalYesterday.isNotEmpty() && oerDoRVarianceYesterday.isNotEmpty() && oerDoRActualYesterday.isNotEmpty()) {

            oer_error_do_yesterday_kpi.visibility = View.GONE
            oer_goal_do_yesterday_kpi.visibility = View.VISIBLE
            oer_variance_do_yesterday_kpi.visibility = View.VISIBLE
            oer_actual_do_yesterday_kpi.visibility = View.VISIBLE

            oer_goal_do_yesterday_kpi.text = oerDoRGoalYesterday
            oer_variance_do_yesterday_kpi.text = oerDoRVarianceYesterday
            oer_actual_do_yesterday_kpi.text = oerDoRActualYesterday
        } else {

            oer_error_do_yesterday_kpi.visibility = View.GONE
            oer_goal_do_yesterday_kpi.visibility = View.VISIBLE
            oer_variance_do_yesterday_kpi.visibility = View.VISIBLE
            oer_actual_do_yesterday_kpi.visibility = View.VISIBLE

            if (oerDoRGoalYesterday.isEmpty()) {
                oer_goal_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_goal_do_yesterday_kpi.text = oerDoRGoalYesterday
            }

            if (oerDoRVarianceYesterday.isEmpty()) {
                oer_variance_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_variance_do_yesterday_kpi.text = oerDoRVarianceYesterday
            }

            if (oerDoRActualYesterday.isEmpty()) {
                oer_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_actual_do_yesterday_kpi.text = oerDoRActualYesterday
            }

        }

        if (oerStart?.status != null && oerDoRActualYesterday.isNotEmpty()) {
            when {
                oerStart.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oer_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    oer_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                oerStart.status.toString() == resources.getString(R.string.under_limit) -> {
                    oer_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    oer_actual_do_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    oer_actual_do_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    oer_actual_do_yesterday_kpi
                            .setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }

    }


    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.aws_parent_layout_do_yesterday_kpi -> {

                rcv_food_do_yesterday_kpi.visibility = View.GONE
                rcv_labour_do_yesterday_kpi.visibility = View.GONE
                rcv_service_do_yesterday_kpi.visibility = View.GONE
                rcv_oer_do_yesterday_kpi.visibility = View.GONE
                rcv_cash_do_yesterday_kpi.visibility = View.GONE

                food_text_overview_do_yesterday_kpi.visibility = View.GONE
                labour_text_overview_do_yesterday_kpi.visibility = View.GONE
                service_text_overview_do_yesterday_kpi.visibility = View.GONE
                oer_text_overview_do_yesterday_kpi.visibility = View.GONE
                cash_text_overview_do_yesterday_kpi.visibility = View.GONE

                food_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_sales_do_yesterday_kpi.visibility == View.VISIBLE) {
                    rcv_sales_do_yesterday_kpi.visibility = View.GONE
                    aws_text_overview_do_yesterday_kpi.visibility = View.GONE
                    aws_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_sales_do_yesterday_kpi.visibility = View.VISIBLE
                    aws_text_overview_do_yesterday_kpi.visibility = View.VISIBLE
                    aws_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_up)
                }
                callDoYesterdayLevelTwoQuery(getString(R.string.awus_text), rcv_sales_do_yesterday_kpi)
            }
            R.id.food_parent_layout_do_yesterday_kpi -> {
                rcv_sales_do_yesterday_kpi.visibility = View.GONE
                rcv_labour_do_yesterday_kpi.visibility = View.GONE
                rcv_service_do_yesterday_kpi.visibility = View.GONE
                rcv_oer_do_yesterday_kpi.visibility = View.GONE
                rcv_cash_do_yesterday_kpi.visibility = View.GONE

                aws_text_overview_do_yesterday_kpi.visibility = View.GONE
                labour_text_overview_do_yesterday_kpi.visibility = View.GONE
                service_text_overview_do_yesterday_kpi.visibility = View.GONE
                oer_text_overview_do_yesterday_kpi.visibility = View.GONE
                cash_text_overview_do_yesterday_kpi.visibility = View.GONE

                aws_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_food_do_yesterday_kpi.visibility == View.VISIBLE) {
                    rcv_food_do_yesterday_kpi.visibility = View.GONE
                    food_text_overview_do_yesterday_kpi.visibility = View.GONE
                    food_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_food_do_yesterday_kpi.visibility = View.VISIBLE
                    food_text_overview_do_yesterday_kpi.visibility = View.VISIBLE
                    food_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callDoYesterdayLevelTwoQuery(getString(R.string.ideal_vs_food_variance_text), rcv_food_do_yesterday_kpi)
            }
            R.id.labor_parent_layout_do_yesterday_kpi -> {
                rcv_sales_do_yesterday_kpi.visibility = View.GONE
                rcv_food_do_yesterday_kpi.visibility = View.GONE
                rcv_service_do_yesterday_kpi.visibility = View.GONE
                rcv_oer_do_yesterday_kpi.visibility = View.GONE
                rcv_cash_do_yesterday_kpi.visibility = View.GONE

                aws_text_overview_do_yesterday_kpi.visibility = View.GONE
                food_text_overview_do_yesterday_kpi.visibility = View.GONE
                service_text_overview_do_yesterday_kpi.visibility = View.GONE
                oer_text_overview_do_yesterday_kpi.visibility = View.GONE
                cash_text_overview_do_yesterday_kpi.visibility = View.GONE

                aws_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_labour_do_yesterday_kpi.visibility == View.VISIBLE) {
                    rcv_labour_do_yesterday_kpi.visibility = View.GONE
                    labour_text_overview_do_yesterday_kpi.visibility = View.GONE
                    labor_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_labour_do_yesterday_kpi.visibility = View.VISIBLE
                    labour_text_overview_do_yesterday_kpi.visibility = View.VISIBLE
                    labor_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callDoYesterdayLevelTwoQuery(getString(R.string.labour_text), rcv_labour_do_yesterday_kpi)

            }
            R.id.service_parent_layout_do_yesterday_kpi -> {
                rcv_sales_do_yesterday_kpi.visibility = View.GONE
                rcv_food_do_yesterday_kpi.visibility = View.GONE
                rcv_labour_do_yesterday_kpi.visibility = View.GONE
                rcv_oer_do_yesterday_kpi.visibility = View.GONE
                rcv_cash_do_yesterday_kpi.visibility = View.GONE

                aws_text_overview_do_yesterday_kpi.visibility = View.GONE
                food_text_overview_do_yesterday_kpi.visibility = View.GONE
                labour_text_overview_do_yesterday_kpi.visibility = View.GONE
                oer_text_overview_do_yesterday_kpi.visibility = View.GONE
                cash_text_overview_do_yesterday_kpi.visibility = View.GONE

                aws_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_service_do_yesterday_kpi.visibility == View.VISIBLE) {
                    rcv_service_do_yesterday_kpi.visibility = View.GONE
                    service_text_overview_do_yesterday_kpi.visibility = View.GONE
                    service_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_service_do_yesterday_kpi.visibility = View.VISIBLE
                    service_text_overview_do_yesterday_kpi.visibility = View.VISIBLE
                    service_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callDoYesterdayLevelTwoQuery(getString(R.string.service_text), rcv_service_do_yesterday_kpi)

            }
            R.id.cash_parent_layout_do_yesterday_kpi -> {
                rcv_sales_do_yesterday_kpi.visibility = View.GONE
                rcv_food_do_yesterday_kpi.visibility = View.GONE
                rcv_labour_do_yesterday_kpi.visibility = View.GONE
                rcv_service_do_yesterday_kpi.visibility = View.GONE
                rcv_oer_do_yesterday_kpi.visibility = View.GONE

                aws_text_overview_do_yesterday_kpi.visibility = View.GONE
                food_text_overview_do_yesterday_kpi.visibility = View.GONE
                labour_text_overview_do_yesterday_kpi.visibility = View.GONE
                service_text_overview_do_yesterday_kpi.visibility = View.GONE
                oer_text_overview_do_yesterday_kpi.visibility = View.GONE


                aws_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_cash_do_yesterday_kpi.visibility == View.VISIBLE) {
                    rcv_cash_do_yesterday_kpi.visibility = View.GONE
                    cash_text_overview_do_yesterday_kpi.visibility = View.GONE
                    cash_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_cash_do_yesterday_kpi.visibility = View.VISIBLE
                    cash_text_overview_do_yesterday_kpi.visibility = View.VISIBLE
                    cash_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callDoYesterdayLevelTwoQuery(getString(R.string.cash_text), rcv_cash_do_yesterday_kpi)

            }
            R.id.oer_parent_layout_do_yesterday_kpi -> {
                rcv_sales_do_yesterday_kpi.visibility = View.GONE
                rcv_food_do_yesterday_kpi.visibility = View.GONE
                rcv_labour_do_yesterday_kpi.visibility = View.GONE
                rcv_service_do_yesterday_kpi.visibility = View.GONE
                rcv_cash_do_yesterday_kpi.visibility = View.GONE

                aws_text_overview_do_yesterday_kpi.visibility = View.GONE
                food_text_overview_do_yesterday_kpi.visibility = View.GONE
                labour_text_overview_do_yesterday_kpi.visibility = View.GONE
                service_text_overview_do_yesterday_kpi.visibility = View.GONE
                cash_text_overview_do_yesterday_kpi.visibility = View.GONE

                aws_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_oer_do_yesterday_kpi.visibility == View.VISIBLE) {
                    rcv_oer_do_yesterday_kpi.visibility = View.GONE
                    oer_text_overview_do_yesterday_kpi.visibility = View.GONE
                    oer_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_oer_do_yesterday_kpi.visibility = View.VISIBLE
                    oer_text_overview_do_yesterday_kpi.visibility = View.VISIBLE
                    oer_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callDoYesterdayLevelTwoQuery(getString(R.string.oer_text), rcv_oer_do_yesterday_kpi)

            }
            R.id.filter_icon -> {
                openYesterdayDoFilter()
            }
            R.id.filter_parent_linear -> {
                openYesterdayDoFilter()
            }
            R.id.error_filter_parent_linear -> {
                openYesterdayDoFilter()
            }
            R.id.aws_text_overview_do_yesterday_kpi -> {
                callOverViewDOYesterdayKpiApi(getString(R.string.awus_text), "", "")
            }
            R.id.food_text_overview_do_yesterday_kpi -> {
                callOverViewDOYesterdayKpiApi(getString(R.string.food_text), "", "")
            }
            R.id.labour_text_overview_do_yesterday_kpi -> {
                callOverViewDOYesterdayKpiApi(getString(R.string.labour_text), "", "")
            }
            R.id.service_text_overview_do_yesterday_kpi -> {
                callOverViewDOYesterdayKpiApi(getString(R.string.service_text), "", "")
            }
            R.id.oer_text_overview_do_yesterday_kpi -> {
                callOverViewDOYesterdayKpiApi(getString(R.string.oer_text), "", "")
            }
            R.id.cash_text_overview_do_yesterday_kpi -> {
                callOverViewDOYesterdayKpiApi(getString(R.string.cash_text), "", "")
            }
        }
    }

    private fun openYesterdayDoFilter() {
        val intentYesterdayDoFilter = Intent(requireContext(), FilterActivity::class.java)
        startActivity(intentYesterdayDoFilter)
    }

    private fun openYesterdayDoSalesDetail(awsDoSalesDetail: DOOverviewYesterdayQuery.Do_) {
        val intentAwsDoSalesDetail = Intent(requireContext(), AWUSKpiActivity::class.java)
        intentAwsDoSalesDetail.putExtra("awus_data", gson.toJson(awsDoSalesDetail))
        intentAwsDoSalesDetail.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentAwsDoSalesDetail)

    }

    private fun openYesterdayDoLabourDetail(labourDoDetail: DOOverviewYesterdayQuery.Do_) {
        val intentLabourDoDetail = Intent(requireContext(), LabourKpiActivity::class.java)
        intentLabourDoDetail.putExtra("labour_data", gson.toJson(labourDoDetail))
        intentLabourDoDetail.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentLabourDoDetail)
    }

    private fun openYesterdayDoServiceDetail(serviceDoDetail: DOOverviewYesterdayQuery.Do_) {
        val intentServiceDoDetail = Intent(requireContext(), ServiceKpiActivity::class.java)
        intentServiceDoDetail.putExtra("service_data", gson.toJson(serviceDoDetail))
        intentServiceDoDetail.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentServiceDoDetail)
    }

    private fun openYesterdayDoOERDetail(oERDoDetail: DOOverviewYesterdayQuery.Do_) {
        val intentOERDoDetail = Intent(requireContext(), OERStartActivity::class.java)
        intentOERDoDetail.putExtra("oer_data", gson.toJson(oERDoDetail))
        intentOERDoDetail.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentOERDoDetail)
    }

    private fun openYesterdayDoFoodDetail(foodDoDetail: DOOverviewYesterdayQuery.Do_) {
        val intentFoodDoDetail = Intent(requireContext(), FoodKpiActivity::class.java)
        intentFoodDoDetail.putExtra("food_data", gson.toJson(foodDoDetail))
        intentFoodDoDetail.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentFoodDoDetail)
    }

    private fun openYesterdayDoCashDetail(cashDoDetail: DOOverviewYesterdayQuery.Do_) {
        val intentCashDoDetail = Intent(requireContext(), CashKpiActivity::class.java)
        intentCashDoDetail.putExtra("cash_data", gson.toJson(cashDoDetail))
        intentCashDoDetail.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentCashDoDetail)
    }

    private fun refreshYesterdayDoToken() {

        refreshTokenViewModelDoYesterday.getRefreshToken()
        refreshTokenViewModelDoYesterday.refreshTokenResponseLiveData.observe(requireActivity(), {
            run {
                when (it.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        callDoYesterdayLevelOneQuery()
                    }
                    Status.UNSUCCESSFUL -> {
                        CommonUtil.navigateToLogin(mainActivity)
                    }
                    Status.ERROR -> {
                    }
                    Status.OFFLINE -> {
                        setInternetErrorScreenVisibleStateForDoYesterday()
                    }
                }
            }
        })
    }

    fun setInternetErrorScreenVisibleStateForDoYesterday() {
        do_yesterday_no_internet_error_layout.visibility = View.VISIBLE
        common_header_do_yesterday.sales_header_error_image.visibility = View.VISIBLE
        common_header_do_yesterday.sales_header_error_image.setImageResource(R.drawable.ic_internet_error)
        do_yesterday_missing_data_error_layout.visibility = View.GONE
        common_header_do_yesterday.error_filter_parent_linear.visibility = View.GONE
        do_yesterday_data_error_layout.visibility = View.GONE
        hideCalendarViewForDoYesterday()
        setHeaderViewsVisibleStateForDoYesterday()
        showStoreFilterVisibilityStateForDoYesterday()
    }

    fun setErrorScreenVisibleStateForDoYesterday(
            title: String,
            description: String
    ) {
        do_yesterday_data_error_layout.visibility = View.VISIBLE
        do_yesterday_data_error_layout.exception_text_title.text = title
        do_yesterday_data_error_layout.exception_text_description.text = description
        common_header_do_yesterday.sales_header_error_image.visibility = View.VISIBLE
        common_header_do_yesterday.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)
        common_header_do_yesterday.error_filter_parent_linear.visibility = View.VISIBLE
        do_yesterday_missing_data_error_layout.visibility = View.GONE
        hideCalendarViewForDoYesterday()
        setHeaderViewsVisibleStateForDoYesterday()
        hideStoreFilterVisibilityStateForDoYesterday()
    }

    fun setHeaderViewsVisibleStateForDoYesterday() {
        do_yesterday_header.visibility = View.GONE
        do_yesterday_v1.visibility = View.GONE
        do_yesterday_layout.visibility = View.INVISIBLE
        common_header_do_yesterday.total_sales_common_header.visibility = View.GONE
        common_header_do_yesterday.sales_text_common_header.visibility = View.GONE
    }
    fun hideStoreFilterVisibilityStateForDoYesterday(){
        common_header_do_yesterday.filter_parent_linear.visibility = View.GONE
    }
    fun showStoreFilterVisibilityStateForDoYesterday(){
        common_header_do_yesterday.filter_parent_linear.visibility = View.VISIBLE
    }

    fun hideCalendarViewForDoYesterday() {
        common_calendar_do_yesterday.visibility = View.GONE
    }

    fun showMissingDataViewForDoYesterday(
            missingDataTitle: String,
            missingDataDescription: String
    ) {
        do_yesterday_missing_data_error_layout.visibility = View.VISIBLE
        do_yesterday_missing_data_error_layout.header_data_title.text = missingDataTitle
        do_yesterday_missing_data_error_layout.header_data_description.text = missingDataDescription
    }

    fun hideErrorScreenVisibleStateForDOYesterday(){
        do_yesterday_no_internet_error_layout.visibility = View.GONE
        do_yesterday_data_error_layout.visibility = View.GONE
        common_header_do_yesterday.sales_header_error_image.visibility = View.GONE
        common_header_do_yesterday.error_filter_parent_linear.visibility = View.GONE

        do_yesterday_header.visibility = View.VISIBLE
        do_yesterday_v1.visibility = View.VISIBLE
        do_yesterday_layout.visibility = View.VISIBLE

        common_header_do_yesterday.filter_parent_linear.visibility = View.VISIBLE
        common_header_do_yesterday.total_sales_common_header.visibility = View.VISIBLE
        common_header_do_yesterday.sales_text_common_header.visibility = View.VISIBLE

        common_calendar_do_yesterday.visibility = View.VISIBLE
    }

    fun collapseExpendedListVisibilityForDOYesterday(){
        if (rcv_sales_do_yesterday_kpi.visibility == View.VISIBLE) {
            rcv_sales_do_yesterday_kpi.visibility = View.GONE
            aws_text_overview_do_yesterday_kpi.visibility = View.GONE
            aws_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_food_do_yesterday_kpi.visibility == View.VISIBLE) {
            rcv_food_do_yesterday_kpi.visibility = View.GONE
            food_text_overview_do_yesterday_kpi.visibility = View.GONE
            food_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_labour_do_yesterday_kpi.visibility == View.VISIBLE) {
            rcv_labour_do_yesterday_kpi.visibility = View.GONE
            labour_text_overview_do_yesterday_kpi.visibility = View.GONE
            labor_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_service_do_yesterday_kpi.visibility == View.VISIBLE) {
            rcv_service_do_yesterday_kpi.visibility = View.GONE
            service_text_overview_do_yesterday_kpi.visibility = View.GONE
            service_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_cash_do_yesterday_kpi.visibility == View.VISIBLE) {
            rcv_cash_do_yesterday_kpi.visibility = View.GONE
            cash_text_overview_do_yesterday_kpi.visibility = View.GONE
            cash_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_oer_do_yesterday_kpi.visibility == View.VISIBLE) {
            rcv_oer_do_yesterday_kpi.visibility = View.GONE
            oer_text_overview_do_yesterday_kpi.visibility = View.GONE
            oer_parent_img_do_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
    }


}