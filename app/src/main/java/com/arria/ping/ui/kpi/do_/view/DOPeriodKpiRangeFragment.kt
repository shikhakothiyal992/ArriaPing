package com.arria.ping.ui.kpi.do_.view

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import com.arria.ping.util.NetworkHelper
import com.arria.ping.kpi.*
import com.arria.ping.kpi._do.DoPeriodRangeLevelOneQuery
import com.arria.ping.kpi._do.DoPeriodRangeLevelTwoQuery
import com.arria.ping.log.Logger
import com.arria.ping.log.data.QueryData
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.model.StoreDetailPojo
import com.arria.ping.ui.filter.FilterActivity
import com.arria.ping.ui.generalview.MainActivity
import com.arria.ping.ui.kpi.do_.adapter.CustomExpandableListAdapterPeriodDO
import com.arria.ping.ui.kpi.overview.*
import com.arria.ping.ui.refreshtoken.model.Status
import com.arria.ping.ui.refreshtoken.viewmodel.RefreshTokenViewModel
import com.arria.ping.util.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.ceo_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.ceo_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.common_calendar.view.*
import kotlinx.android.synthetic.main.common_header_ceo.*
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.data_error_layout.view.*
import kotlinx.android.synthetic.main.do_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.do_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.expandable_list_supervisor.view.*
import kotlinx.android.synthetic.main.individual_supervisor_item.view.*
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class DOPeriodKpiRangeFragment : Fragment(), View.OnClickListener {
    lateinit var doSupervisorLevel3PeriodKpi: DOPeriodLVLThreeQuery.Do_
    private var lastExpandedPositionPeriodKpi = -1
    private val expandableListDetailPeriodKpi = HashMap<String, List<StoreDetailPojo>>()
    private var expandableListAdapterPeriodKpi: CustomExpandableListAdapterPeriodDO? = null
    private lateinit var dbHelperPeriodKpi: DatabaseHelperImpl
    private val gsonPeriodKpi = Gson()
    private var formattedDoPeriodKpiStartDateValue = ""
    private var formattedDoPeriodKpiEndDateValue = ""

    lateinit var doPeriodRangeLevelOne: DoPeriodRangeLevelOneQuery.Do_
    lateinit var doPeriodRangeLevelTwo: DoPeriodRangeLevelTwoQuery.Do_

    @Inject
    lateinit var networkHelper: NetworkHelper

    private val refreshTokenViewModelDoPeriodRange by viewModels<RefreshTokenViewModel>()

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
        return inflater.inflate(R.layout.do_period_range_fragment_kpi, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        dbHelperPeriodKpi = DatabaseHelperImpl(DatabaseBuilder.getInstance(requireActivity()))

        initialisePeriodKpi()

        formattedDoPeriodKpiStartDateValue = StorePrefData.startDateValue
        formattedDoPeriodKpiEndDateValue = StorePrefData.endDateValue

        if (StorePrefData.filterDate.isNotEmpty()) {
            setStoreFilterViewForDoPeriod(StorePrefData.filterDate)
        }

        if (networkHelper.isNetworkConnected()) {

            callMissingDataQueryForDoPeriodRangeQuary()
            callDoPeriodRangeLevelOneQuery()

        } else {
            setInternetErrorScreenVisibleStateForDoPeriod()
        }

        do_period_swipe_refresh_layout.setOnRefreshListener{
            Logger.info("Pull down to refresh DO Period Store Data", "Period KPI")
            callMissingDataQueryForDoPeriodRangeQuary()
            callDoPeriodRangeLevelOneQuery()
            collapseExpendedListVisibilityForDoPeriod()
            do_period_swipe_refresh_layout.isRefreshing = false
        }
    }

    private fun initialisePeriodKpi() {
        aws_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        food_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        labor_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        service_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        oer_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        cash_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

        aws_parent_layout_do_period_range_kpi.setOnClickListener(this)
        food_parent_layout_do_period_range_kpi.setOnClickListener(this)
        labor_parent_layout_do_period_range_kpi.setOnClickListener(this)
        service_parent_layout_do_period_range_kpi.setOnClickListener(this)
        cash_parent_layout_do_period_range_kpi.setOnClickListener(this)
        oer_parent_layout_do_period_range_kpi.setOnClickListener(this)

        filter_icon.setOnClickListener(this)
        filter_parent_linear.setOnClickListener(this)
        error_filter_parent_linear.setOnClickListener(this)

        aws_text_overview_do_period_range_kpi.setOnClickListener(this)
        food_text_overview_do_period_range_kpi.setOnClickListener(this)
        labour_text_overview_do_period_range_kpi.setOnClickListener(this)
        service_text_overview_do_period_range_kpi.setOnClickListener(this)
        oer_text_overview_do_period_range_kpi.setOnClickListener(this)
        cash_text_overview_do_period_range_kpi.setOnClickListener(this)

        Validation().setCustomCalendar(common_calendar_do_range.square_day)

    }

    private fun setExpandableDataDoPeriodRangeKpi(
            actionDoPeriodRangeKpi: String,
            rcvDoPeriodRangeKpi: NonScrollExpandableListView
    ) {
        val childDataDoPeriodRangeKpi: MutableList<StoreDetailPojo> = ArrayList()
        doPeriodRangeLevelTwo.kpis!!.individualSupervisors.forEachIndexed {_, item ->
            expandableListDetailPeriodKpi[item!!.supervisorName!!] = childDataDoPeriodRangeKpi
        }
        val titleListDoPeriodRangeKpi = ArrayList(expandableListDetailPeriodKpi.keys)


        expandableListAdapterPeriodKpi = CustomExpandableListAdapterPeriodDO(
                requireContext(),
                titleListDoPeriodRangeKpi as ArrayList<String>,
                expandableListDetailPeriodKpi, doPeriodRangeLevelTwo, actionDoPeriodRangeKpi
        )
        rcvDoPeriodRangeKpi.setAdapter(expandableListAdapterPeriodKpi)

        rcvDoPeriodRangeKpi.setOnGroupExpandListener {groupPosition ->

            val doRangeExpandableSuperVisorNumberValue =
                    doPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.supervisorNumber!!
            val doRangeExpandableSupervisorSalesKpiData =
                    doPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.period?.sales
            val doRangeExpandableSupervisorFoodKpiData =
                    doPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.period?.food
            val doRangeExpandableSupervisorLaborKpiData =
                    doPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.period?.labor
            val doRangeExpandableSupervisorOERKpiData =
                    doPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.period?.oerStart
            val doRangeExpandableSupervisorCashKpiData =
                    doPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.period?.cash

            //Sales
            val doRangeExpandableSupervisorSalesKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doRangeExpandableSupervisorSalesKpiData?.goal?.amount,
                    doRangeExpandableSupervisorSalesKpiData?.goal?.value,
                    doRangeExpandableSupervisorSalesKpiData?.goal?.percentage
            )
            val doRangeExpandableSupervisorSalesKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doRangeExpandableSupervisorSalesKpiData?.variance?.amount,
                    doRangeExpandableSupervisorSalesKpiData?.variance?.value,
                    doRangeExpandableSupervisorSalesKpiData?.variance?.percentage
            )
            val doRangeExpandableSupervisorSalesKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doRangeExpandableSupervisorSalesKpiData?.actual?.amount,
                    doRangeExpandableSupervisorSalesKpiData?.actual?.value,
                    doRangeExpandableSupervisorSalesKpiData?.actual?.percentage
            )

            //Food
            val doRangeExpandableSupervisorFoodKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doRangeExpandableSupervisorFoodKpiData?.goal?.amount,
                    doRangeExpandableSupervisorFoodKpiData?.goal?.value,
                    doRangeExpandableSupervisorFoodKpiData?.goal?.percentage
            )
            val doRangeExpandableSupervisorFoodKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doRangeExpandableSupervisorFoodKpiData?.variance?.amount,
                    doRangeExpandableSupervisorFoodKpiData?.variance?.value,
                    doRangeExpandableSupervisorFoodKpiData?.variance?.percentage
            )
            val doRangeExpandableSupervisorFoodKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doRangeExpandableSupervisorFoodKpiData?.actual?.amount,
                    doRangeExpandableSupervisorFoodKpiData?.actual?.value,
                    doRangeExpandableSupervisorFoodKpiData?.actual?.percentage
            )

            //Labor
            val doRangeExpandableSupervisorLaborKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doRangeExpandableSupervisorLaborKpiData?.goal?.amount,
                    doRangeExpandableSupervisorLaborKpiData?.goal?.value,
                    doRangeExpandableSupervisorLaborKpiData?.goal?.percentage
            )
            val doRangeExpandableSupervisorLaborKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doRangeExpandableSupervisorLaborKpiData?.variance?.amount,
                    doRangeExpandableSupervisorLaborKpiData?.variance?.value,
                    doRangeExpandableSupervisorLaborKpiData?.variance?.percentage
            )
            val doRangeExpandableSupervisorLaborKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doRangeExpandableSupervisorLaborKpiData?.actual?.amount,
                    doRangeExpandableSupervisorLaborKpiData?.actual?.value,
                    doRangeExpandableSupervisorLaborKpiData?.actual?.percentage
            )

            //OER
            val doRangeExpandableSupervisorOERKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doRangeExpandableSupervisorOERKpiData?.goal?.amount,
                    doRangeExpandableSupervisorOERKpiData?.goal?.value,
                    doRangeExpandableSupervisorOERKpiData?.goal?.percentage
            )
            val doRangeExpandableSupervisorOERKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doRangeExpandableSupervisorOERKpiData?.variance?.amount,
                    doRangeExpandableSupervisorOERKpiData?.variance?.value,
                    doRangeExpandableSupervisorOERKpiData?.variance?.percentage
            )
            val doRangeExpandableSupervisorOERKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doRangeExpandableSupervisorOERKpiData?.actual?.amount,
                    doRangeExpandableSupervisorOERKpiData?.actual?.value,
                    doRangeExpandableSupervisorOERKpiData?.actual?.percentage
            )

            //Cash
            val doRangeExpandableSupervisorCashKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    doRangeExpandableSupervisorCashKpiData?.actual?.amount,
                    doRangeExpandableSupervisorCashKpiData?.actual?.value,
                    doRangeExpandableSupervisorCashKpiData?.actual?.percentage
            )

            when {
                rcv_sales_do_period_range_kpi.visibility == View.VISIBLE -> {
                    if (doRangeExpandableSupervisorSalesKpiDataGoal.isEmpty() && doRangeExpandableSupervisorSalesKpiDataVariance.isEmpty() && doRangeExpandableSupervisorSalesKpiDataActual.isEmpty()) {
                        rcvDoPeriodRangeKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvDoPeriodRangeKpi.isGroupExpanded(groupPosition))
                            callStoreAgainstSupervisor(
                                    titleListDoPeriodRangeKpi[groupPosition],
                                    actionDoPeriodRangeKpi,
                                    doRangeExpandableSuperVisorNumberValue
                            )
                        else
                            rcvDoPeriodRangeKpi.collapseGroup(groupPosition)
                    }
                }

                rcv_food_do_period_range_kpi.visibility == View.VISIBLE -> {
                    if (doRangeExpandableSupervisorFoodKpiDataGoal.isEmpty() && doRangeExpandableSupervisorFoodKpiDataVariance.isEmpty() && doRangeExpandableSupervisorFoodKpiDataActual.isEmpty()) {
                        rcvDoPeriodRangeKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvDoPeriodRangeKpi.isGroupExpanded(groupPosition))
                            callStoreAgainstSupervisor(
                                    titleListDoPeriodRangeKpi[groupPosition],
                                    actionDoPeriodRangeKpi,
                                    doRangeExpandableSuperVisorNumberValue
                            )
                        else
                            rcvDoPeriodRangeKpi.collapseGroup(groupPosition)
                    }
                }

                rcv_labour_do_period_range_kpi.visibility == View.VISIBLE -> {
                    if (doRangeExpandableSupervisorLaborKpiDataGoal.isEmpty() && doRangeExpandableSupervisorLaborKpiDataVariance.isEmpty() && doRangeExpandableSupervisorLaborKpiDataActual.isEmpty()) {
                        rcvDoPeriodRangeKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvDoPeriodRangeKpi.isGroupExpanded(groupPosition))
                            callStoreAgainstSupervisor(
                                    titleListDoPeriodRangeKpi[groupPosition],
                                    actionDoPeriodRangeKpi,
                                    doRangeExpandableSuperVisorNumberValue
                            )
                        else
                            rcvDoPeriodRangeKpi.collapseGroup(groupPosition)
                    }
                }

                rcv_service_do_period_range_kpi.visibility == View.VISIBLE -> {

                    if (doPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.period == null) {
                        rcvDoPeriodRangeKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvDoPeriodRangeKpi.isGroupExpanded(groupPosition))
                            callStoreAgainstSupervisor(
                                    titleListDoPeriodRangeKpi[groupPosition],
                                    actionDoPeriodRangeKpi,
                                    doRangeExpandableSuperVisorNumberValue
                            )
                        else
                            rcvDoPeriodRangeKpi.collapseGroup(groupPosition)
                    }
                }

                rcv_oer_do_period_range_kpi.visibility == View.VISIBLE -> {
                    if (doRangeExpandableSupervisorOERKpiDataGoal.isEmpty() && doRangeExpandableSupervisorOERKpiDataVariance.isEmpty() && doRangeExpandableSupervisorOERKpiDataActual.isEmpty()) {
                        rcvDoPeriodRangeKpi.collapseGroup(groupPosition)

                    } else {
                        if (rcvDoPeriodRangeKpi.isGroupExpanded(groupPosition))
                            callStoreAgainstSupervisor(
                                    titleListDoPeriodRangeKpi[groupPosition],
                                    actionDoPeriodRangeKpi,
                                    doRangeExpandableSuperVisorNumberValue
                            )
                        else
                            rcvDoPeriodRangeKpi.collapseGroup(groupPosition)
                    }

                }

                rcv_cash_do_period_range_kpi.visibility == View.VISIBLE -> {
                    if (doRangeExpandableSupervisorCashKpiDataActual.isNotEmpty()) {

                        if (rcvDoPeriodRangeKpi.isGroupExpanded(groupPosition))
                            callStoreAgainstSupervisor(
                                    titleListDoPeriodRangeKpi[groupPosition],
                                    actionDoPeriodRangeKpi,
                                    doRangeExpandableSuperVisorNumberValue
                            )
                        else
                            rcvDoPeriodRangeKpi.collapseGroup(groupPosition)
                    } else {
                        rcvDoPeriodRangeKpi.collapseGroup(groupPosition)
                    }

                }
            }

            if (lastExpandedPositionPeriodKpi != -1 && groupPosition != lastExpandedPositionPeriodKpi) {
                rcvDoPeriodRangeKpi.collapseGroup(lastExpandedPositionPeriodKpi)
            }
            lastExpandedPositionPeriodKpi = groupPosition
        }

        rcvDoPeriodRangeKpi.setOnChildClickListener {_, _, groupPosition, childPosition, _ ->
            val superVisorNumber =
                    doPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.supervisorNumber!!

            if (childPosition == 0) {

                when {
                    rcv_sales_do_period_range_kpi.visibility == View.VISIBLE -> {
                        callOverViewDoPeriodRangeKpiApi(getString(R.string.awus_text), superVisorNumber, "")
                    }
                    rcv_food_do_period_range_kpi.visibility == View.VISIBLE -> {
                        callOverViewDoPeriodRangeKpiApi(getString(R.string.food_text), superVisorNumber, "")
                    }
                    rcv_labour_do_period_range_kpi.visibility == View.VISIBLE -> {
                        callOverViewDoPeriodRangeKpiApi(getString(R.string.labour_text), superVisorNumber, "")
                    }
                    rcv_service_do_period_range_kpi.visibility == View.VISIBLE -> {
                        callOverViewDoPeriodRangeKpiApi(getString(R.string.service_text), superVisorNumber, "")
                    }
                    rcv_oer_do_period_range_kpi.visibility == View.VISIBLE -> {
                        callOverViewDoPeriodRangeKpiApi(getString(R.string.oer_text), superVisorNumber, "")
                    }
                    rcv_cash_do_period_range_kpi.visibility == View.VISIBLE -> {
                        callOverViewDoPeriodRangeKpiApi(getString(R.string.cash_text), superVisorNumber, "")
                    }
                }
            }
            else{

            val storeNumber =
                    expandableListDetailPeriodKpi[titleListDoPeriodRangeKpi[groupPosition]]!![(childPosition)].storeNumber!!

            val doRangeKpiData = expandableListDetailPeriodKpi[titleListDoPeriodRangeKpi[groupPosition]]!![(childPosition)]
            if (doRangeKpiData.storeGoal?.isNotEmpty() == true || doRangeKpiData.storeVariance?.isNotEmpty() == true || doRangeKpiData.storeActual?.isNotEmpty() == true) {
                when {
                    rcv_sales_do_period_range_kpi.visibility == View.VISIBLE -> {
                        callOverViewDoPeriodRangeKpiApi(getString(R.string.awus_text), superVisorNumber, storeNumber)
                    }
                    rcv_food_do_period_range_kpi.visibility == View.VISIBLE -> {
                        callOverViewDoPeriodRangeKpiApi(getString(R.string.food_text), superVisorNumber, storeNumber)
                    }
                    rcv_labour_do_period_range_kpi.visibility == View.VISIBLE -> {
                        callOverViewDoPeriodRangeKpiApi(
                                getString(R.string.labour_text),
                                superVisorNumber,
                                storeNumber
                        )
                    }

                    rcv_oer_do_period_range_kpi.visibility == View.VISIBLE -> {
                        callOverViewDoPeriodRangeKpiApi(getString(R.string.oer_text), superVisorNumber, storeNumber)
                    }
                    rcv_cash_do_period_range_kpi.visibility == View.VISIBLE -> {
                        callOverViewDoPeriodRangeKpiApi(getString(R.string.cash_text), superVisorNumber, storeNumber)
                    }
                }
            }
            if (rcv_service_do_period_range_kpi.visibility == View.VISIBLE) {
                callOverViewDoPeriodRangeKpiApi(getString(R.string.service_text), superVisorNumber, storeNumber)
            }
            }


            false
        }

    }


    private fun callStoreAgainstSupervisor(
            title: String,
            action: String,
            doRangeSuperVisorNumberValue: String
    ) {

        val formattedStartDateValueDoPeriod: String
        val formattedEndDateValueDoPeriod: String

        if (StorePrefData.isCalendarSelected) {
            formattedStartDateValueDoPeriod = StorePrefData.startDateValue
            formattedEndDateValueDoPeriod = StorePrefData.endDateValue
        } else {
            formattedStartDateValueDoPeriod = StorePrefData.startDateValue
            formattedEndDateValueDoPeriod = StorePrefData.endDateValue
        }

        val progressDialog = CustomProgressDialog(requireActivity())
        lifecycleScope.launchWhenResumed {
            progressDialog.showProgressDialog()
            val areaCode = dbHelperPeriodKpi.getAllSelectedAreaList(true)
            val stateCode = dbHelperPeriodKpi.getAllSelectedStoreListState(true)
            var superVisorNumberList =
                    dbHelperPeriodKpi.getAllSelectedStoreListSupervisor(true)
            val storeListValue = dbHelperPeriodKpi.getAllSelectedStoreList(true)
            val doRangeSupervisorNumberListTemp = mutableListOf<String>()
            if (doRangeSuperVisorNumberValue.isNotEmpty() || doRangeSuperVisorNumberValue.isNotBlank()) {
                doRangeSupervisorNumberListTemp.add(doRangeSuperVisorNumberValue)
            }
            if (dbHelperPeriodKpi.getAllSelectedStoreListSupervisor(true)
                        .isEmpty()
            ) {
                superVisorNumberList = doRangeSupervisorNumberListTemp
            }


            Logger.info(
                    DOPeriodLVLThreeQuery.OPERATION_NAME.name(),
                    "Period Range Level 3 KPI",
                    mapQueryFilters(
                            QueryData(
                                    areaCode,
                                    stateCode,
                                    superVisorNumberList,
                                    storeListValue,
                                    formattedEndDateValueDoPeriod,
                                    formattedStartDateValueDoPeriod,
                                    StorePrefData.filterType,
                                    DOPeriodLVLThreeQuery.QUERY_DOCUMENT
                            )
                    )
            )
            try {
                val response =
                        apolloClient(requireContext()).query(
                                DOPeriodLVLThreeQuery(
                                        areaCode.toInput(),
                                        stateCode.toInput(),
                                        superVisorNumberList.toInput(),
                                        storeListValue.toInput(),
                                        formattedEndDateValueDoPeriod.toInput(),
                                        formattedStartDateValueDoPeriod.toInput(),
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                                .toInput()
                                )
                        )
                                .await()

                if (response.data?.do_ != null) {
                    progressDialog.dismissProgressDialog()
                    doSupervisorLevel3PeriodKpi = response.data?.do_!!

                    response.data?.do_?.kpis?.individualStores.let {
                        setDORangeDataForIndividualStore(
                                action,
                                title
                        )
                    }
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialog.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Level 3 KPI")
                        }
                refreshDoPeriodKpiToken()
                return@launchWhenResumed
            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialog.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForDoPeriod()
                }

            } catch (exception: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(exception.message.toString(), "Period Range Level 3 KPI")
                setErrorScreenVisibleStateForDoPeriod(
                        getString(R.string.exception_error_text_title),
                        getString(R.string.exception_error_text_description)
                )

            }

        }

    }

    private fun setDORangeDataForIndividualStore(
            actionPeriodKpi: String,
            titlePeriodKpi: String
    ) {
        val storeDetailsPeriodKpi = doSupervisorLevel3PeriodKpi.kpis!!.individualStores
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

        storeDetailsPeriodKpi.forEachIndexed {_, item ->
            when (actionPeriodKpi) {
                requireActivity().getString(R.string.awus_text) -> {
                    childData.add(
                            StoreDetailPojo(
                                    item?.storeNumber.toString(),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.sales?.goal?.amount,
                                            item?.period?.sales?.goal?.percentage,
                                            item?.period?.sales?.goal?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.sales?.variance?.amount,
                                            item?.period?.sales?.variance?.percentage,
                                            item?.period?.sales?.variance?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.sales?.actual?.amount,
                                            item?.period?.sales?.actual?.percentage,
                                            item?.period?.sales?.actual?.value
                                    ),
                                    item?.period?.sales?.status.toString()
                            )
                    )
                }
                requireActivity().getString(R.string.ideal_vs_food_variance_text) -> {
                    childData.add(
                            StoreDetailPojo(
                                    item?.storeNumber.toString(),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.food?.goal?.amount,
                                            item?.period?.food?.goal?.percentage,
                                            item?.period?.food?.goal?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.food?.variance?.amount,
                                            item?.period?.food?.variance?.percentage,
                                            item?.period?.food?.variance?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.food?.actual?.amount,
                                            item?.period?.food?.actual?.percentage,
                                            item?.period?.food?.actual?.value
                                    ),
                                    item?.period?.food?.status.toString()
                            )
                    )
                }
                requireActivity().getString(R.string.labour_text) -> {
                    childData.add(
                            StoreDetailPojo(
                                    item?.storeNumber.toString(),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.labor?.goal?.amount,
                                            item?.period?.labor?.goal?.percentage,
                                            item?.period?.labor?.goal?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.labor?.variance?.amount,
                                            item?.period?.labor?.variance?.percentage,
                                            item?.period?.labor?.variance?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.labor?.actual?.amount,
                                            item?.period?.labor?.actual?.percentage,
                                            item?.period?.labor?.actual?.value
                                    ),
                                    item?.period?.labor?.status.toString()
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
                                    item?.period?.labor?.status.toString()
                            )
                    )
                }
                requireActivity().getString(R.string.cash_text) -> {
                    childData.add(
                            StoreDetailPojo(
                                    item?.storeNumber.toString(),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.cash?.goal?.amount,
                                            item?.period?.cash?.goal?.percentage,
                                            item?.period?.cash?.goal?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.cash?.variance?.amount,
                                            item?.period?.cash?.variance?.percentage,
                                            item?.period?.cash?.variance?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.cash?.actual?.amount,
                                            item?.period?.cash?.actual?.percentage,
                                            item?.period?.cash?.actual?.value
                                    ),
                                    item?.period?.cash?.status.toString()
                            )
                    )
                }
                requireActivity().getString(R.string.oer_text) -> {
                    childData.add(
                            StoreDetailPojo(
                                    item?.storeNumber.toString(),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.oerStart?.goal?.amount,
                                            item?.period?.oerStart?.goal?.percentage,
                                            item?.period?.oerStart?.goal?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.oerStart?.variance?.amount,
                                            item?.period?.oerStart?.variance?.percentage,
                                            item?.period?.oerStart?.variance?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.period?.oerStart?.actual?.amount,
                                            item?.period?.oerStart?.actual?.percentage,
                                            item?.period?.oerStart?.actual?.value
                                    ),
                                    item?.period?.oerStart?.status.toString()
                            )
                    )
                }
            }
        }
        if (childData.size == 2) {
            childData.removeAt(0)
        }
        expandableListDetailPeriodKpi[titlePeriodKpi] = childData
        expandableListAdapterPeriodKpi!! setChild (expandableListDetailPeriodKpi)
    }


    private fun callMissingDataQueryForDoPeriodRangeQuary() {
        formattedDoPeriodKpiStartDateValue = StorePrefData.startDateValue
        formattedDoPeriodKpiEndDateValue = StorePrefData.endDateValue

        val progressDialogDoPeriodRangeKpi = CustomProgressDialog(requireActivity())
        progressDialogDoPeriodRangeKpi.showProgressDialog()

        lifecycleScope.launchWhenResumed {
            val areaCodeDoPeriodRangeKpi = dbHelperPeriodKpi.getAllSelectedAreaList(true)
            val stateCodeDoPeriodRangeKpi = dbHelperPeriodKpi.getAllSelectedStoreListState(true)
            val supervisorNumberDoPeriodRangeKpi = dbHelperPeriodKpi.getAllSelectedStoreListSupervisor(true)
            val storeNumberDoPeriodRangeKpi = dbHelperPeriodKpi.getAllSelectedStoreList(true)

            Logger.info(
                    MissingDataQuery.OPERATION_NAME.name(), "Period Range Missing Data",
                    mapQueryFilters(
                            areaCodeDoPeriodRangeKpi,
                            stateCodeDoPeriodRangeKpi,
                            supervisorNumberDoPeriodRangeKpi,
                            storeNumberDoPeriodRangeKpi,
                            MissingDataQuery.QUERY_DOCUMENT
                    )
            )


            try {
                val responseMissingDataDoPeriodRangeKpi =
                        apolloClient(requireContext()).query(
                                MissingDataQuery(
                                        areaCodeDoPeriodRangeKpi.toInput(),
                                        stateCodeDoPeriodRangeKpi.toInput(),
                                        supervisorNumberDoPeriodRangeKpi.toInput(),
                                        storeNumberDoPeriodRangeKpi.toInput(),
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                                .toInput(),
                                        formattedDoPeriodKpiStartDateValue.toInput(),
                                        formattedDoPeriodKpiEndDateValue.toInput(),
                                )
                        )
                                .await()

                if (responseMissingDataDoPeriodRangeKpi.data?.missingData != null) {
                    progressDialogDoPeriodRangeKpi.dismissProgressDialog()
                    setMissingDataViewVisibleStateForDoPeriod(
                            responseMissingDataDoPeriodRangeKpi.data?.missingData!!
                                    .header.toString(),
                            responseMissingDataDoPeriodRangeKpi.data?.missingData!!.message.toString()
                    )

                } else {
                    progressDialogDoPeriodRangeKpi.dismissProgressDialog()
                    do_period_range_missing_data_error_layout.visibility = View.GONE
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogDoPeriodRangeKpi.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Missing Data")
                        }

            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogDoPeriodRangeKpi.dismissProgressDialog()
                setInternetErrorScreenVisibleStateForDoPeriod()
            } catch (e: ApolloException) {
                progressDialogDoPeriodRangeKpi.dismissProgressDialog()
                Logger.error(e.message.toString(), "Period Range Missing Data")
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callDoPeriodRangeLevelOneQuery() {

        val progressDialogDoPeriodRangeKpi = CustomProgressDialog(requireActivity())
        progressDialogDoPeriodRangeKpi.showProgressDialog()

        lifecycleScope.launchWhenResumed {

            if (networkHelper.isNetworkConnected()) {
                hideErrorScreenVisibleStateForDoPeriod()
            }
            val areaCodeDoPeriodRangeKpi = dbHelperPeriodKpi.getAllSelectedAreaList(true)
            val stateCodeDoPeriodRangeKpi = dbHelperPeriodKpi.getAllSelectedStoreListState(true)
            val supervisorNumberDoPeriodRangeKpi = dbHelperPeriodKpi.getAllSelectedStoreListSupervisor(true)
            val storeNumberDoPeriodRangeKpi = dbHelperPeriodKpi.getAllSelectedStoreList(true)


            Logger.info(
                    DoPeriodRangeLevelOneQuery.OPERATION_NAME.name(),
                    "Period Range Level 1 KPI",
                    mapQueryFilters(
                            QueryData(
                                    areaCodeDoPeriodRangeKpi,
                                    stateCodeDoPeriodRangeKpi,
                                    supervisorNumberDoPeriodRangeKpi,
                                    storeNumberDoPeriodRangeKpi,
                                    formattedDoPeriodKpiEndDateValue,
                                    formattedDoPeriodKpiStartDateValue,
                                    StorePrefData.filterType,
                                    DoPeriodRangeLevelOneQuery.QUERY_DOCUMENT
                            )
                    )
            )
            try {

                val responseDoPeriodRangeLevelOne =
                        apolloClient(requireContext()).query(
                                DoPeriodRangeLevelOneQuery(
                                        areaCodeDoPeriodRangeKpi.toInput(),
                                        stateCodeDoPeriodRangeKpi.toInput(),
                                        supervisorNumberDoPeriodRangeKpi.toInput(),
                                        storeNumberDoPeriodRangeKpi.toInput(),
                                        formattedDoPeriodKpiStartDateValue,
                                        formattedDoPeriodKpiEndDateValue,
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                )
                        )
                                .await()
                if (responseDoPeriodRangeLevelOne.data?.do_ != null) {
                    progressDialogDoPeriodRangeKpi.dismissProgressDialog()
                    doPeriodRangeLevelOne = responseDoPeriodRangeLevelOne.data?.do_!!

                    if (doPeriodRangeLevelOne.kpis?.supervisors?.stores?.period != null) {
                        setDOPeriodRangeStoreData(doPeriodRangeLevelOne.kpis?.supervisors?.stores?.period)
                    } else {
                        setErrorScreenVisibleStateForDoPeriod(
                                getString(R.string.error_text_title),
                                getString(R.string.error_text_description)
                        )

                    }
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogDoPeriodRangeKpi.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Level 1 KPI")
                        }
                refreshDoPeriodKpiToken()
                return@launchWhenResumed

            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogDoPeriodRangeKpi.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForDoPeriod()
                }
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Period Range Level 1 KPI")
                progressDialogDoPeriodRangeKpi.dismissProgressDialog()
                setErrorScreenVisibleStateForDoPeriod(
                        getString(R.string.exception_error_text_title),
                        getString(R.string.exception_error_text_description)
                )

            }

        }
    }


    private fun callOverViewDoPeriodRangeKpiApi(
            actionDoPeriodRangeKpi: String,
            superVisorNumberDoPeriodRangeKpi: String,
            storeNumber: String
    ) {
        lifecycleScope.launchWhenResumed {
            val superVisorNumberListDoPeriodRangeKpi = mutableListOf<String>()
            if (superVisorNumberDoPeriodRangeKpi.isNotEmpty() || superVisorNumberDoPeriodRangeKpi.isNotBlank()) {
                superVisorNumberListDoPeriodRangeKpi.add(superVisorNumberDoPeriodRangeKpi)
            }

            val storeNumberListDoPeriodRangeKpi = mutableListOf<String>()
            if (storeNumber.isNotEmpty() || storeNumber.isNotBlank()) {
                storeNumberListDoPeriodRangeKpi.add(storeNumber)
            } else {

                storeNumberListDoPeriodRangeKpi.addAll(
                        dbHelperPeriodKpi.getAllSelectedStoreList(
                                true
                        )
                )
            }
            if (StorePrefData.isCalendarSelected) {
                formattedDoPeriodKpiStartDateValue = StorePrefData.startDateValue
                formattedDoPeriodKpiEndDateValue = StorePrefData.endDateValue
            } else {
                formattedDoPeriodKpiStartDateValue = StorePrefData.startDateValue
                formattedDoPeriodKpiEndDateValue = StorePrefData.endDateValue
            }
            val progressDialogPeriodKpi = CustomProgressDialog(requireActivity())
            progressDialogPeriodKpi.showProgressDialog()

            try {
                val response =
                        apolloClient(requireContext()).query(
                                DOOverviewRangeQuery(
                                        superVisorNumberListDoPeriodRangeKpi.toInput(),
                                        storeNumberListDoPeriodRangeKpi.toInput(),
                                        formattedDoPeriodKpiStartDateValue,
                                        formattedDoPeriodKpiEndDateValue,
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                )
                        )
                                .await()

                if (response.data?.do_ != null) {
                    progressDialogPeriodKpi.dismissProgressDialog()
                    when (actionDoPeriodRangeKpi) {
                        getString(R.string.awus_text) -> {
                            openDoPeriodKpiSalesDetail(response.data?.do_!!)
                        }
                        getString(R.string.food_text) -> {
                            openDoPeriodKpiFoodDetail(response.data?.do_!!)
                        }
                        getString(R.string.labour_text) -> {
                            openDoPeriodKpiLabourDetail(response.data?.do_!!)
                        }
                        getString(R.string.service_text) -> {
                            openDoPeriodKpiServiceDetail(response.data?.do_!!)
                        }
                        getString(R.string.oer_text) -> {
                            openDoPeriodKpiOERDetail(response.data?.do_!!)
                        }
                        getString(R.string.cash_text) -> {
                            openDoPeriodKpiCASHDetail(response.data?.do_!!)
                        }
                    }
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogPeriodKpi.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Overview KPI")
                        }
                refreshDoPeriodKpiToken()
                return@launchWhenResumed
            } catch (e: ApolloNetworkException) {
                progressDialogPeriodKpi.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForDoPeriod()
                }
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Period Range Overview KPI")
                progressDialogPeriodKpi.dismissProgressDialog()
                setErrorScreenVisibleStateForDoPeriod(
                        getString(R.string.exception_error_text_title),
                        getString(R.string.exception_error_text_description)
                )

            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDOPeriodRangeStoreData(detailDoPeriodRangeKpi: DoPeriodRangeLevelOneQuery.Period?) {

        val strDORangeSelectedDate: String? =
                detailDoPeriodRangeKpi?.periodFrom?.let {
                    detailDoPeriodRangeKpi.periodTo?.let {it1 ->
                        EnumMapperUtil.getSelectedDate(
                                it,
                                it1, EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                        )
                    }
                }

        if (strDORangeSelectedDate != null) {
            StorePrefData.filterDate = strDORangeSelectedDate
            setStoreFilterViewForDoPeriod(StorePrefData.filterDate)
        }

        val doPeriodSalesValue = Validation().checkAmountPercentageValue(
                requireActivity(),
                detailDoPeriodRangeKpi?.sales?.actual?.amount,
                detailDoPeriodRangeKpi?.sales?.actual?.percentage,
                detailDoPeriodRangeKpi?.sales?.actual?.value
        )

        if(doPeriodSalesValue.isEmpty()){
            hideVisibilityStateOfSalesDataForDOPeriod()
        }else{
            showVisibilityStateOfSalesDataForDOPeriod(doPeriodSalesValue)
        }


        displaySalesViewForDoPeriodRange(detailDoPeriodRangeKpi?.sales)
        displayFoodViewForDoPeriodRange(detailDoPeriodRangeKpi?.food)
        displayLaborViewForDoPeriodRange(detailDoPeriodRangeKpi?.labor)
        displayEADTServiceViewForDoPeriodRange(detailDoPeriodRangeKpi?.service)
        displayExtremeServiceViewForDoPeriodRange(detailDoPeriodRangeKpi?.service?.extremeDelivery)
        displaySinglesServiceViewForDoPeriodRange(detailDoPeriodRangeKpi?.service?.singles)
        displayCashViewForDoPeriodRange(detailDoPeriodRangeKpi?.cash)
        displayOERViewForDoPeriodRange(detailDoPeriodRangeKpi?.oerStart)
    }

    fun setStoreFilterViewForDoPeriod(date: String){
        val periodTextDoRange = "$date | ${StorePrefData.isSelectedPeriod}"

        Validation().validateFilterKPI(
                requireActivity(), dbHelperPeriodKpi, common_header_do_range.store_header!!,
                periodTextDoRange
        )
    }


    fun hideVisibilityStateOfSalesDataForDOPeriod(){
        common_header_do_range.total_sales_common_header.visibility = View.GONE
        common_header_do_range.sales_text_common_header.visibility = View.GONE
        common_header_do_range.sales_header_error_image.visibility = View.VISIBLE
        common_header_do_range.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)
    }
    fun showVisibilityStateOfSalesDataForDOPeriod(doPeriodSalesValue: String) {
        common_header_do_range.total_sales_common_header.visibility = View.VISIBLE
        common_header_do_range.total_sales_common_header.text = doPeriodSalesValue
        common_header_do_range.sales_text_common_header.visibility = View.VISIBLE
        common_header_do_range.sales_text_common_header.text = getString(R.string.sales_text)
        common_header_do_range.sales_header_error_image.visibility = View.GONE
    }

    fun displaySalesViewForDoPeriodRange(sales: DoPeriodRangeLevelOneQuery.Sales?) {
        if (sales?.displayName != null) {
            aws_display_do_period_range_kpi.text = sales.displayName
        }
        val salesDoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.goal?.amount,
                sales?.goal?.percentage,
                sales?.goal?.value
        )
        val salesDoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.variance?.amount,
                sales?.variance?.percentage,
                sales?.variance?.value
        )
        val salesDoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.actual?.amount,
                sales?.actual?.percentage,
                sales?.actual?.value
        )

        if (salesDoGoalRange.isEmpty() && salesDoVarianceRange.isEmpty() && salesDoActualRange.isEmpty()) {

            sales_error_do_period_range_kpi.visibility = View.VISIBLE
            val paramDOORangeAWUSError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOORangeAWUSError.weight = 2.0f
            aws_display_do_period_range_kpi.layoutParams = paramDOORangeAWUSError

            sales_goal_do_period_range_kpi.visibility = View.GONE
            sales_variance_do_period_range_kpi.visibility = View.GONE
            sales_actual_do_period_range_kpi.visibility = View.GONE
            aws_parent_img_do_period_range_kpi.visibility = View.GONE
            aws_parent_layout_do_period_range_kpi.isClickable = false
        } else if (salesDoGoalRange.isNotEmpty() && salesDoVarianceRange.isNotEmpty() && salesDoActualRange.isNotEmpty()) {

            sales_error_do_period_range_kpi.visibility = View.GONE

            sales_goal_do_period_range_kpi.visibility = View.VISIBLE
            sales_variance_do_period_range_kpi.visibility = View.VISIBLE
            sales_actual_do_period_range_kpi.visibility = View.VISIBLE

            sales_goal_do_period_range_kpi.text = salesDoGoalRange
            sales_variance_do_period_range_kpi.text = salesDoVarianceRange
            sales_actual_do_period_range_kpi.text = salesDoActualRange
        } else {

            sales_error_do_period_range_kpi.visibility = View.GONE
            sales_goal_do_period_range_kpi.visibility = View.VISIBLE
            sales_variance_do_period_range_kpi.visibility = View.VISIBLE
            sales_actual_do_period_range_kpi.visibility = View.VISIBLE

            if (salesDoGoalRange.isEmpty()) {
                sales_goal_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_goal_do_period_range_kpi.text = salesDoGoalRange
            }

            if (salesDoVarianceRange.isEmpty()) {
                sales_variance_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_variance_do_period_range_kpi.text = salesDoVarianceRange
            }

            if (salesDoActualRange.isEmpty()) {
                sales_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_actual_do_period_range_kpi.text = salesDoActualRange
            }
        }

        if (sales?.status?.toString() != null && salesDoActualRange.isNotEmpty()) {
            when {
                sales.status.toString() == resources.getString(R.string.out_of_range) -> {
                    sales_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    sales_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                sales.status.toString() == resources.getString(R.string.under_limit) -> {
                    sales_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    sales_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    sales_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    sales_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

    }

    fun displayFoodViewForDoPeriodRange(food: DoPeriodRangeLevelOneQuery.Food?) {
        if (food?.displayName != null) {
            food_display_do_period_range_kpi.text = food.displayName
        }

        val foodDoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.goal?.amount,
                food?.goal?.percentage,
                food?.goal?.value
        )
        val foodDoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.variance?.amount,
                food?.variance?.percentage,
                food?.variance?.value
        )
        val foodDoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.actual?.amount,
                food?.actual?.percentage,
                food?.actual?.value
        )

        if (foodDoGoalRange.isEmpty() && foodDoVarianceRange.isEmpty() && foodDoActualRange.isEmpty()) {

            food_error_do_period_range_kpi.visibility = View.VISIBLE
            val paramDOORangeFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOORangeFoodError.weight = 2.0f
            food_display_do_period_range_kpi.layoutParams = paramDOORangeFoodError

            food_goal_do_period_range_kpi.visibility = View.GONE
            food_variance_do_period_range_kpi.visibility = View.GONE
            food_actual_do_period_range_kpi.visibility = View.GONE
            food_parent_img_do_period_range_kpi.visibility = View.GONE
            food_parent_layout_do_period_range_kpi.isClickable = false
        } else if (foodDoGoalRange.isNotEmpty() && foodDoVarianceRange.isNotEmpty() && foodDoActualRange.isNotEmpty()) {

            food_error_do_period_range_kpi.visibility = View.GONE

            food_goal_do_period_range_kpi.visibility = View.VISIBLE
            food_variance_do_period_range_kpi.visibility = View.VISIBLE
            food_actual_do_period_range_kpi.visibility = View.VISIBLE

            food_goal_do_period_range_kpi.text = foodDoGoalRange
            food_variance_do_period_range_kpi.text = foodDoVarianceRange
            food_actual_do_period_range_kpi.text = foodDoActualRange
        } else {

            food_error_do_period_range_kpi.visibility = View.GONE

            food_goal_do_period_range_kpi.visibility = View.VISIBLE
            food_variance_do_period_range_kpi.visibility = View.VISIBLE
            food_actual_do_period_range_kpi.visibility = View.VISIBLE

            if (foodDoGoalRange.isEmpty()) {
                food_goal_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_goal_do_period_range_kpi.text = foodDoGoalRange
            }

            if (foodDoVarianceRange.isEmpty()) {
                food_variance_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_variance_do_period_range_kpi.text = foodDoVarianceRange
            }

            if (foodDoActualRange.isEmpty()) {
                food_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_actual_do_period_range_kpi.text = foodDoActualRange
            }

        }

        if (food?.status?.toString() != null && foodDoActualRange.isNotEmpty()) {
            when {
                food.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.red_circle, 0)
                    food_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                food.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.green_circle, 0)
                    food_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    food_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.black_circle, 0)
                    food_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayLaborViewForDoPeriodRange(labor: DoPeriodRangeLevelOneQuery.Labor?) {
        if (labor?.displayName != null) {
            labour_display_do_period_range_kpi.text = labor.displayName
        }

        val labourDoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.goal?.amount,
                labor?.goal?.percentage,
                labor?.goal?.value
        )
        val labourDoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.variance?.amount,
                labor?.variance?.percentage,
                labor?.variance?.value
        )
        val labourDoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.actual?.amount,
                labor?.actual?.percentage,
                labor?.actual?.value
        )

        if (labourDoGoalRange.isEmpty() && labourDoVarianceRange.isEmpty() && labourDoActualRange.isEmpty()) {

            labour_error_do_period_range_kpi.visibility = View.VISIBLE

            val paramDOORangeLabourError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOORangeLabourError.weight = 2.0f
            labour_display_do_period_range_kpi.layoutParams = paramDOORangeLabourError

            labour_goal_do_period_range_kpi.visibility = View.GONE
            labour_variance_do_period_range_kpi.visibility = View.GONE
            labour_actual_do_period_range_kpi.visibility = View.GONE
            labor_parent_img_do_period_range_kpi.visibility = View.GONE
            labor_parent_layout_do_period_range_kpi.isClickable = false
        } else if (labourDoGoalRange.isNotEmpty() && labourDoVarianceRange.isNotEmpty() && labourDoActualRange.isNotEmpty()) {

            labour_error_do_period_range_kpi.visibility = View.GONE

            labour_goal_do_period_range_kpi.visibility = View.VISIBLE
            labour_variance_do_period_range_kpi.visibility = View.VISIBLE
            labour_actual_do_period_range_kpi.visibility = View.VISIBLE

            labour_goal_do_period_range_kpi.text = labourDoGoalRange
            labour_variance_do_period_range_kpi.text = labourDoVarianceRange
            labour_actual_do_period_range_kpi.text = labourDoActualRange
        } else {

            labour_error_do_period_range_kpi.visibility = View.GONE

            labour_goal_do_period_range_kpi.visibility = View.VISIBLE
            labour_variance_do_period_range_kpi.visibility = View.VISIBLE
            labour_actual_do_period_range_kpi.visibility = View.VISIBLE

            if (labourDoGoalRange.isEmpty()) {
                labour_goal_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_goal_do_period_range_kpi.text = labourDoGoalRange
            }

            if (labourDoVarianceRange.isEmpty()) {
                labour_variance_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_variance_do_period_range_kpi.text = labourDoVarianceRange
            }

            if (labourDoActualRange.isEmpty()) {
                labour_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_actual_do_period_range_kpi.text = labourDoActualRange
            }

        }

        if (labor?.status != null && labourDoActualRange.isNotEmpty()) {
            when {
                labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    labour_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                labor.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    labour_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    labour_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    labour_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayEADTServiceViewForDoPeriodRange(service: DoPeriodRangeLevelOneQuery.Service?) {
        if (service?.displayName != null) {
            service_display_do_period_range_kpi.text = service.displayName
        }
        if (service?.eADT?.displayName != null) {
            eadt_display_do_period_range_kpi.text = service.eADT.displayName
        }
        if (service?.extremeDelivery?.displayName != null) {
            extreme_delivery_display_do_period_range_kpi.text = service.extremeDelivery.displayName
        }
        if (service?.singles?.displayName != null) {
            single_display_do_period_range_kpi.text = service.singles.displayName
        }

        val serviceDoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.goal?.amount,
                service?.eADT?.goal?.percentage,
                service?.eADT?.goal?.value
        )
        val serviceDoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.variance?.amount,
                service?.eADT?.variance?.percentage,
                service?.eADT?.variance?.value
        )
        val serviceDoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.actual?.amount,
                service?.eADT?.actual?.percentage,
                service?.eADT?.actual?.value
        )

        if (serviceDoGoalRange.isEmpty() && serviceDoVarianceRange.isEmpty() && serviceDoActualRange.isEmpty()) {

            service_error_do_period_range_kpi.visibility = View.VISIBLE
            val paramDOORangeEdtError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOORangeEdtError.weight = 2.0f
            eadt_display_do_period_range_kpi.layoutParams = paramDOORangeEdtError

            service_eadt_goal_do_period_range_kpi.visibility = View.GONE
            service_eadt_variance_do_period_range_kpi.visibility = View.GONE
            service_eadt_actual_do_period_range_kpi.visibility = View.GONE
            service_eADT_parent_img_do_period_range_kpi.visibility = View.GONE
        } else if (serviceDoGoalRange.isNotEmpty() && serviceDoVarianceRange.isNotEmpty() && serviceDoActualRange.isNotEmpty()) {

            service_error_do_period_range_kpi.visibility = View.GONE
            service_eadt_goal_do_period_range_kpi.visibility = View.VISIBLE
            service_eadt_variance_do_period_range_kpi.visibility = View.VISIBLE
            service_eadt_actual_do_period_range_kpi.visibility = View.VISIBLE

            service_eadt_goal_do_period_range_kpi.text = serviceDoGoalRange
            service_eadt_variance_do_period_range_kpi.text = serviceDoVarianceRange
            service_eadt_actual_do_period_range_kpi.text = serviceDoActualRange
        } else {

            service_error_do_period_range_kpi.visibility = View.GONE
            service_eadt_goal_do_period_range_kpi.visibility = View.VISIBLE
            service_eadt_variance_do_period_range_kpi.visibility = View.VISIBLE
            service_eadt_actual_do_period_range_kpi.visibility = View.VISIBLE

            if (serviceDoGoalRange.isEmpty()) {
                service_eadt_goal_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_goal_do_period_range_kpi.text = serviceDoGoalRange
            }

            if (serviceDoVarianceRange.isEmpty()) {
                service_eadt_variance_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_variance_do_period_range_kpi.text = serviceDoVarianceRange
            }

            if (serviceDoActualRange.isEmpty()) {
                service_eadt_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_actual_do_period_range_kpi.text = serviceDoActualRange
            }

        }

        if (service?.eADT?.status != null && serviceDoActualRange.isNotEmpty()) {
            when {
                service.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_eadt_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_eadt_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                service.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_eadt_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_eadt_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_eadt_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_eadt_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

        // service extreme
        val serviceExtremeDoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.extremeDelivery?.goal?.amount,
                service?.extremeDelivery?.goal?.percentage,
                service?.extremeDelivery?.goal?.value
        )
        val serviceExtremeDoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.extremeDelivery?.variance?.amount,
                service?.extremeDelivery?.variance?.percentage,
                service?.extremeDelivery?.variance?.value
        )
        val serviceExtremeDoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.extremeDelivery?.actual?.amount,
                service?.extremeDelivery?.actual?.percentage,
                service?.extremeDelivery?.actual?.value
        )

        if (serviceExtremeDoGoalRange.isEmpty() && serviceExtremeDoVarianceRange.isEmpty() && serviceExtremeDoActualRange.isEmpty()) {

            serviceExtreme_error_do_period_range_kpi.visibility = View.VISIBLE
            val paramDOORangeExtremeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOORangeExtremeError.weight = 2.0f
            extreme_delivery_display_do_period_range_kpi.layoutParams = paramDOORangeExtremeError

            service_extreme_goal_do_period_range_kpi.visibility = View.GONE
            service_extreme_variance_do_period_range_kpi.visibility = View.GONE
            service_extreme_actual_do_period_range_kpi.visibility = View.GONE
            service_extreme_parent_img_do_period_range_kpi.visibility = View.GONE
        } else if (serviceExtremeDoGoalRange.isNotEmpty() && serviceExtremeDoVarianceRange.isNotEmpty() && serviceExtremeDoActualRange.isNotEmpty()) {

            serviceExtreme_error_do_period_range_kpi.visibility = View.GONE

            service_extreme_goal_do_period_range_kpi.visibility = View.VISIBLE
            service_extreme_variance_do_period_range_kpi.visibility = View.VISIBLE
            service_extreme_actual_do_period_range_kpi.visibility = View.VISIBLE

            service_extreme_goal_do_period_range_kpi.text = serviceExtremeDoGoalRange
            service_extreme_variance_do_period_range_kpi.text = serviceExtremeDoVarianceRange
            service_extreme_actual_do_period_range_kpi.text = serviceExtremeDoActualRange
        } else {

            serviceExtreme_error_do_period_range_kpi.visibility = View.GONE
            service_extreme_goal_do_period_range_kpi.visibility = View.VISIBLE
            service_extreme_variance_do_period_range_kpi.visibility = View.VISIBLE
            service_extreme_actual_do_period_range_kpi.visibility = View.VISIBLE

            if (serviceExtremeDoGoalRange.isEmpty()) {
                service_extreme_goal_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_goal_do_period_range_kpi.text = serviceExtremeDoGoalRange
            }

            if (serviceExtremeDoVarianceRange.isEmpty()) {
                service_extreme_variance_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_variance_do_period_range_kpi.text = serviceExtremeDoVarianceRange
            }

            if (serviceExtremeDoActualRange.isEmpty()) {
                service_extreme_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_actual_do_period_range_kpi.text = serviceExtremeDoActualRange
            }

        }


        if (service?.extremeDelivery?.status != null && serviceExtremeDoActualRange.isNotEmpty()) {
            when {
                service.extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_extreme_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                service.extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_extreme_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_extreme_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    service_extreme_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_extreme_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }


        // service singles
        val serviceSinglesDoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.singles?.goal?.amount,
                service?.singles?.goal?.percentage,
                service?.singles?.goal?.value
        )
        val serviceSinglesDoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.singles?.variance?.amount,
                service?.singles?.variance?.percentage,
                service?.singles?.variance?.value
        )
        val serviceSinglesDoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.singles?.actual?.amount,
                service?.singles?.actual?.percentage,
                service?.singles?.actual?.value
        )

        if (serviceSinglesDoGoalRange.isEmpty() && serviceSinglesDoVarianceRange.isEmpty() && serviceSinglesDoActualRange.isEmpty()) {

            serviceSingles_error_do_period_range_kpi.visibility = View.VISIBLE
            val paramDOORangeSingleError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOORangeSingleError.weight = 2.0f
            single_display_do_period_range_kpi.layoutParams = paramDOORangeSingleError

            service_singles_goal_do_period_range_kpi.visibility = View.GONE
            service_singles_variance_do_period_range_kpi.visibility = View.GONE
            service_singles_actual_do_period_range_kpi.visibility = View.GONE
            service_single_parent_img_do_period_range_kpi.visibility = View.GONE
        } else if (serviceSinglesDoGoalRange.isNotEmpty() && serviceSinglesDoVarianceRange.isNotEmpty() && serviceSinglesDoActualRange.isNotEmpty()) {

            serviceSingles_error_do_period_range_kpi.visibility = View.GONE
            service_singles_goal_do_period_range_kpi.visibility = View.VISIBLE
            service_singles_variance_do_period_range_kpi.visibility = View.VISIBLE
            service_singles_actual_do_period_range_kpi.visibility = View.VISIBLE

            service_singles_goal_do_period_range_kpi.text = serviceSinglesDoGoalRange
            service_singles_variance_do_period_range_kpi.text = serviceSinglesDoVarianceRange
            service_singles_actual_do_period_range_kpi.text = serviceSinglesDoActualRange
        } else {

            serviceSingles_error_do_period_range_kpi.visibility = View.GONE
            service_singles_goal_do_period_range_kpi.visibility = View.VISIBLE
            service_singles_variance_do_period_range_kpi.visibility = View.VISIBLE
            service_singles_actual_do_period_range_kpi.visibility = View.VISIBLE

            if (serviceSinglesDoGoalRange.isEmpty()) {
                service_singles_goal_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_goal_do_period_range_kpi.text = serviceSinglesDoGoalRange
            }

            if (serviceSinglesDoVarianceRange.isEmpty()) {
                service_singles_variance_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_variance_do_period_range_kpi.text = serviceSinglesDoVarianceRange
            }

            if (serviceSinglesDoActualRange.isEmpty()) {
                service_singles_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_actual_do_period_range_kpi.text = serviceSinglesDoActualRange
            }

        }


        if (service?.singles?.status != null && serviceSinglesDoActualRange.isNotEmpty()) {
            when {
                service.singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_singles_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                service.singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_singles_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_singles_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_singles_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_singles_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }
    
    fun displayExtremeServiceViewForDoPeriodRange(extremeDelivery: DoPeriodRangeLevelOneQuery.ExtremeDelivery?) {
        if (extremeDelivery?.displayName != null) {
            extreme_delivery_display_do_period_range_kpi.text = extremeDelivery.displayName
        }
        val serviceExtremeDoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.goal?.amount,
                extremeDelivery?.goal?.percentage,
                extremeDelivery?.goal?.value
        )
        val serviceExtremeDoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.variance?.amount,
                extremeDelivery?.variance?.percentage,
                extremeDelivery?.variance?.value
        )
        val serviceExtremeDoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.actual?.amount,
                extremeDelivery?.actual?.percentage,
                extremeDelivery?.actual?.value
        )

        if (serviceExtremeDoGoalRange.isEmpty() && serviceExtremeDoVarianceRange.isEmpty() && serviceExtremeDoActualRange.isEmpty()) {

            serviceExtreme_error_do_period_range_kpi.visibility = View.VISIBLE
            val paramDOORangeExtremeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOORangeExtremeError.weight = 2.0f
            extreme_delivery_display_do_period_range_kpi.layoutParams = paramDOORangeExtremeError

            service_extreme_goal_do_period_range_kpi.visibility = View.GONE
            service_extreme_variance_do_period_range_kpi.visibility = View.GONE
            service_extreme_actual_do_period_range_kpi.visibility = View.GONE
            service_extreme_parent_img_do_period_range_kpi.visibility = View.GONE
        } else if (serviceExtremeDoGoalRange.isNotEmpty() && serviceExtremeDoVarianceRange.isNotEmpty() && serviceExtremeDoActualRange.isNotEmpty()) {

            serviceExtreme_error_do_period_range_kpi.visibility = View.GONE

            service_extreme_goal_do_period_range_kpi.visibility = View.VISIBLE
            service_extreme_variance_do_period_range_kpi.visibility = View.VISIBLE
            service_extreme_actual_do_period_range_kpi.visibility = View.VISIBLE

            service_extreme_goal_do_period_range_kpi.text = serviceExtremeDoGoalRange
            service_extreme_variance_do_period_range_kpi.text = serviceExtremeDoVarianceRange
            service_extreme_actual_do_period_range_kpi.text = serviceExtremeDoActualRange
        } else {

            serviceExtreme_error_do_period_range_kpi.visibility = View.GONE
            service_extreme_goal_do_period_range_kpi.visibility = View.VISIBLE
            service_extreme_variance_do_period_range_kpi.visibility = View.VISIBLE
            service_extreme_actual_do_period_range_kpi.visibility = View.VISIBLE

            if (serviceExtremeDoGoalRange.isEmpty()) {
                service_extreme_goal_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_goal_do_period_range_kpi.text = serviceExtremeDoGoalRange
            }

            if (serviceExtremeDoVarianceRange.isEmpty()) {
                service_extreme_variance_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_variance_do_period_range_kpi.text = serviceExtremeDoVarianceRange
            }

            if (serviceExtremeDoActualRange.isEmpty()) {
                service_extreme_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_actual_do_period_range_kpi.text = serviceExtremeDoActualRange
            }

        }


        if (extremeDelivery?.status != null && serviceExtremeDoActualRange.isNotEmpty()) {
            when {
                extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_extreme_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_extreme_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_extreme_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    service_extreme_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_extreme_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }
    }
    
    fun displaySinglesServiceViewForDoPeriodRange(singles: DoPeriodRangeLevelOneQuery.Singles?){
        if (singles?.displayName != null) {
            single_display_do_period_range_kpi.text = singles.displayName
        }

        val serviceSinglesDoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.goal?.amount,
                singles?.goal?.percentage,
                singles?.goal?.value
        )
        val serviceSinglesDoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.variance?.amount,
                singles?.variance?.percentage,
                singles?.variance?.value
        )
        val serviceSinglesDoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.actual?.amount,
                singles?.actual?.percentage,
                singles?.actual?.value
        )

        if (serviceSinglesDoGoalRange.isEmpty() && serviceSinglesDoVarianceRange.isEmpty() && serviceSinglesDoActualRange.isEmpty()) {

            serviceSingles_error_do_period_range_kpi.visibility = View.VISIBLE
            val paramDOORangeSingleError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOORangeSingleError.weight = 2.0f
            single_display_do_period_range_kpi.layoutParams = paramDOORangeSingleError

            service_singles_goal_do_period_range_kpi.visibility = View.GONE
            service_singles_variance_do_period_range_kpi.visibility = View.GONE
            service_singles_actual_do_period_range_kpi.visibility = View.GONE
            service_single_parent_img_do_period_range_kpi.visibility = View.GONE
        } else if (serviceSinglesDoGoalRange.isNotEmpty() && serviceSinglesDoVarianceRange.isNotEmpty() && serviceSinglesDoActualRange.isNotEmpty()) {

            serviceSingles_error_do_period_range_kpi.visibility = View.GONE
            service_singles_goal_do_period_range_kpi.visibility = View.VISIBLE
            service_singles_variance_do_period_range_kpi.visibility = View.VISIBLE
            service_singles_actual_do_period_range_kpi.visibility = View.VISIBLE

            service_singles_goal_do_period_range_kpi.text = serviceSinglesDoGoalRange
            service_singles_variance_do_period_range_kpi.text = serviceSinglesDoVarianceRange
            service_singles_actual_do_period_range_kpi.text = serviceSinglesDoActualRange
        } else {

            serviceSingles_error_do_period_range_kpi.visibility = View.GONE
            service_singles_goal_do_period_range_kpi.visibility = View.VISIBLE
            service_singles_variance_do_period_range_kpi.visibility = View.VISIBLE
            service_singles_actual_do_period_range_kpi.visibility = View.VISIBLE

            if (serviceSinglesDoGoalRange.isEmpty()) {
                service_singles_goal_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_goal_do_period_range_kpi.text = serviceSinglesDoGoalRange
            }

            if (serviceSinglesDoVarianceRange.isEmpty()) {
                service_singles_variance_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_variance_do_period_range_kpi.text = serviceSinglesDoVarianceRange
            }

            if (serviceSinglesDoActualRange.isEmpty()) {
                service_singles_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_actual_do_period_range_kpi.text = serviceSinglesDoActualRange
            }

        }


        if (singles?.status != null && serviceSinglesDoActualRange.isNotEmpty()) {
            when {
                singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_singles_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_singles_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_singles_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_singles_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_singles_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

    }

    fun displayCashViewForDoPeriodRange(cash: DoPeriodRangeLevelOneQuery.Cash?) {
        if (cash?.displayName != null) {
            cash_display_do_period_range_kpi.text = cash.displayName
        }

        val cashDoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.goal?.amount,
                cash?.goal?.percentage,
                cash?.goal?.value
        )
        val cashDoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.variance?.amount,
                cash?.variance?.percentage,
                cash?.variance?.value
        )
        val cashDoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.actual?.amount,
                cash?.actual?.percentage,
                cash?.actual?.value
        )

        if (cashDoGoalRange.isEmpty() && cashDoVarianceRange.isEmpty() && cashDoActualRange.isEmpty()) {

            cash_error_do_period_range_kpi.visibility = View.VISIBLE
            val paramDOORangeCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOORangeCashError.weight = 2.0f
            cash_display_do_period_range_kpi.layoutParams = paramDOORangeCashError

            cash_goal_do_period_range_kpi.visibility = View.GONE
            cash_variance_do_period_range_kpi.visibility = View.GONE
            cash_actual_do_period_range_kpi.visibility = View.GONE
            cash_parent_layout_do_period_range_kpi.isClickable = false
            cash_parent_img_do_period_range_kpi.visibility = View.GONE
        } else if (cashDoGoalRange.isNotEmpty() && cashDoVarianceRange.isNotEmpty() && cashDoActualRange.isNotEmpty()) {

            cash_error_do_period_range_kpi.visibility = View.GONE
            cash_goal_do_period_range_kpi.visibility = View.VISIBLE
            cash_variance_do_period_range_kpi.visibility = View.VISIBLE
            cash_actual_do_period_range_kpi.visibility = View.VISIBLE

            cash_goal_do_period_range_kpi.text = cashDoGoalRange
            cash_variance_do_period_range_kpi.text = cashDoVarianceRange
            cash_actual_do_period_range_kpi.text = cashDoActualRange
        } else {

            cash_error_do_period_range_kpi.visibility = View.GONE
            cash_goal_do_period_range_kpi.visibility = View.VISIBLE
            cash_variance_do_period_range_kpi.visibility = View.VISIBLE
            cash_actual_do_period_range_kpi.visibility = View.VISIBLE

            if (cashDoGoalRange.isEmpty()) {
                cash_goal_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_goal_do_period_range_kpi.text = cashDoGoalRange
            }

            if (cashDoVarianceRange.isEmpty()) {
                cash_variance_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_variance_do_period_range_kpi.text = cashDoVarianceRange
            }

            if (cashDoActualRange.isEmpty()) {
                cash_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_actual_do_period_range_kpi.text = cashDoActualRange
            }

        }

        if (cash?.status != null && cashDoActualRange.isNotEmpty()) {
            when {
                cash.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    cash_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                cash.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    cash_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    cash_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    cash_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }
    }

    fun displayOERViewForDoPeriodRange(oerStart: DoPeriodRangeLevelOneQuery.OerStart?) {

        if (oerStart?.displayName != null) {
            oer_display_do_period_range_kpi.text = oerStart.displayName
        }

        val oerDoRGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.goal?.amount,
                oerStart?.goal?.percentage,
                oerStart?.goal?.value
        )
        val oerDoRVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.variance?.amount,
                oerStart?.variance?.percentage,
                oerStart?.variance?.value
        )
        val oerDoRActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.actual?.amount,
                oerStart?.actual?.percentage,
                oerStart?.actual?.value
        )

        if (oerDoRGoalRange.isEmpty() && oerDoRVarianceRange.isEmpty() && oerDoRActualRange.isEmpty()) {

            oer_error_do_period_range_kpi.visibility = View.VISIBLE
            val paramDOORangeOERError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramDOORangeOERError.weight = 2.0f
            oer_display_do_period_range_kpi.layoutParams = paramDOORangeOERError

            oer_goal_do_period_range_kpi.visibility = View.GONE
            oer_variance_do_period_range_kpi.visibility = View.GONE
            oer_actual_do_period_range_kpi.visibility = View.GONE
            oer_parent_layout_do_period_range_kpi.isClickable = false
            oer_parent_img_do_period_range_kpi.visibility = View.GONE
        } else if (oerDoRGoalRange.isNotEmpty() && oerDoRVarianceRange.isNotEmpty() && oerDoRActualRange.isNotEmpty()) {

            oer_error_do_period_range_kpi.visibility = View.GONE
            oer_goal_do_period_range_kpi.visibility = View.VISIBLE
            oer_variance_do_period_range_kpi.visibility = View.VISIBLE
            oer_actual_do_period_range_kpi.visibility = View.VISIBLE

            oer_goal_do_period_range_kpi.text = oerDoRGoalRange
            oer_variance_do_period_range_kpi.text = oerDoRVarianceRange
            oer_actual_do_period_range_kpi.text = oerDoRActualRange
        } else {

            oer_error_do_period_range_kpi.visibility = View.GONE

            oer_goal_do_period_range_kpi.visibility = View.VISIBLE
            oer_variance_do_period_range_kpi.visibility = View.VISIBLE
            oer_actual_do_period_range_kpi.visibility = View.VISIBLE

            if (oerDoRGoalRange.isEmpty()) {
                oer_goal_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_goal_do_period_range_kpi.text = oerDoRGoalRange
            }

            if (oerDoRVarianceRange.isEmpty()) {
                oer_variance_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_variance_do_period_range_kpi.text = oerDoRVarianceRange
            }

            if (oerDoRActualRange.isEmpty()) {
                oer_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_actual_do_period_range_kpi.text = oerDoRActualRange
            }

        }
        if (oerStart?.status != null && oerDoRActualRange.isNotEmpty()) {
            when {
                oerStart.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oer_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    oer_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                oerStart.status.toString() == resources.getString(R.string.under_limit) -> {
                    oer_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    oer_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    oer_actual_do_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    oer_actual_do_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }

    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.aws_parent_layout_do_period_range_kpi -> {

                rcv_food_do_period_range_kpi.visibility = View.GONE
                rcv_labour_do_period_range_kpi.visibility = View.GONE
                rcv_service_do_period_range_kpi.visibility = View.GONE
                rcv_oer_do_period_range_kpi.visibility = View.GONE
                rcv_cash_do_period_range_kpi.visibility = View.GONE

                food_text_overview_do_period_range_kpi.visibility = View.GONE
                labour_text_overview_do_period_range_kpi.visibility = View.GONE
                service_text_overview_do_period_range_kpi.visibility = View.GONE
                oer_text_overview_do_period_range_kpi.visibility = View.GONE
                cash_text_overview_do_period_range_kpi.visibility = View.GONE

                food_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_sales_do_period_range_kpi.visibility == View.VISIBLE) {
                    rcv_sales_do_period_range_kpi.visibility = View.GONE
                    aws_text_overview_do_period_range_kpi.visibility = View.GONE
                    aws_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_sales_do_period_range_kpi.visibility = View.VISIBLE
                    aws_text_overview_do_period_range_kpi.visibility = View.VISIBLE
                    aws_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_up)
                }
                callDoPeriodRangeLevelTwoQuery(getString(R.string.awus_text), rcv_sales_do_period_range_kpi)
            }
            R.id.food_parent_layout_do_period_range_kpi -> {
                rcv_sales_do_period_range_kpi.visibility = View.GONE
                rcv_labour_do_period_range_kpi.visibility = View.GONE
                rcv_service_do_period_range_kpi.visibility = View.GONE
                rcv_oer_do_period_range_kpi.visibility = View.GONE
                rcv_cash_do_period_range_kpi.visibility = View.GONE

                aws_text_overview_do_period_range_kpi.visibility = View.GONE
                labour_text_overview_do_period_range_kpi.visibility = View.GONE
                service_text_overview_do_period_range_kpi.visibility = View.GONE
                oer_text_overview_do_period_range_kpi.visibility = View.GONE
                cash_text_overview_do_period_range_kpi.visibility = View.GONE

                aws_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_food_do_period_range_kpi.visibility == View.VISIBLE) {
                    rcv_food_do_period_range_kpi.visibility = View.GONE
                    food_text_overview_do_period_range_kpi.visibility = View.GONE
                    food_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_food_do_period_range_kpi.visibility = View.VISIBLE
                    food_text_overview_do_period_range_kpi.visibility = View.VISIBLE
                    food_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callDoPeriodRangeLevelTwoQuery(
                        getString(R.string.ideal_vs_food_variance_text),
                        rcv_food_do_period_range_kpi
                )
            }
            R.id.labor_parent_layout_do_period_range_kpi -> {
                rcv_sales_do_period_range_kpi.visibility = View.GONE
                rcv_food_do_period_range_kpi.visibility = View.GONE
                rcv_service_do_period_range_kpi.visibility = View.GONE
                rcv_oer_do_period_range_kpi.visibility = View.GONE
                rcv_cash_do_period_range_kpi.visibility = View.GONE

                aws_text_overview_do_period_range_kpi.visibility = View.GONE
                food_text_overview_do_period_range_kpi.visibility = View.GONE
                service_text_overview_do_period_range_kpi.visibility = View.GONE
                oer_text_overview_do_period_range_kpi.visibility = View.GONE
                cash_text_overview_do_period_range_kpi.visibility = View.GONE

                aws_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_labour_do_period_range_kpi.visibility == View.VISIBLE) {
                    rcv_labour_do_period_range_kpi.visibility = View.GONE
                    labour_text_overview_do_period_range_kpi.visibility = View.GONE
                    labor_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_labour_do_period_range_kpi.visibility = View.VISIBLE
                    labour_text_overview_do_period_range_kpi.visibility = View.VISIBLE
                    labor_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callDoPeriodRangeLevelTwoQuery(getString(R.string.labour_text), rcv_labour_do_period_range_kpi)

            }
            R.id.service_parent_layout_do_period_range_kpi -> {
                rcv_sales_do_period_range_kpi.visibility = View.GONE
                rcv_food_do_period_range_kpi.visibility = View.GONE
                rcv_labour_do_period_range_kpi.visibility = View.GONE
                rcv_oer_do_period_range_kpi.visibility = View.GONE
                rcv_cash_do_period_range_kpi.visibility = View.GONE

                aws_text_overview_do_period_range_kpi.visibility = View.GONE
                food_text_overview_do_period_range_kpi.visibility = View.GONE
                labour_text_overview_do_period_range_kpi.visibility = View.GONE
                oer_text_overview_do_period_range_kpi.visibility = View.GONE
                cash_text_overview_do_period_range_kpi.visibility = View.GONE

                aws_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_service_do_period_range_kpi.visibility == View.VISIBLE) {
                    rcv_service_do_period_range_kpi.visibility = View.GONE
                    service_text_overview_do_period_range_kpi.visibility = View.GONE
                    service_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_service_do_period_range_kpi.visibility = View.VISIBLE
                    service_text_overview_do_period_range_kpi.visibility = View.VISIBLE
                    service_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callDoPeriodRangeLevelTwoQuery(getString(R.string.service_text), rcv_service_do_period_range_kpi)

            }
            R.id.cash_parent_layout_do_period_range_kpi -> {
                rcv_sales_do_period_range_kpi.visibility = View.GONE
                rcv_food_do_period_range_kpi.visibility = View.GONE
                rcv_labour_do_period_range_kpi.visibility = View.GONE
                rcv_service_do_period_range_kpi.visibility = View.GONE
                rcv_oer_do_period_range_kpi.visibility = View.GONE

                aws_text_overview_do_period_range_kpi.visibility = View.GONE
                food_text_overview_do_period_range_kpi.visibility = View.GONE
                labour_text_overview_do_period_range_kpi.visibility = View.GONE
                service_text_overview_do_period_range_kpi.visibility = View.GONE
                oer_text_overview_do_period_range_kpi.visibility = View.GONE


                aws_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_cash_do_period_range_kpi.visibility == View.VISIBLE) {
                    rcv_cash_do_period_range_kpi.visibility = View.GONE
                    cash_text_overview_do_period_range_kpi.visibility = View.GONE
                    cash_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_cash_do_period_range_kpi.visibility = View.VISIBLE
                    cash_text_overview_do_period_range_kpi.visibility = View.VISIBLE
                    cash_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callDoPeriodRangeLevelTwoQuery(getString(R.string.cash_text), rcv_cash_do_period_range_kpi)

            }
            R.id.oer_parent_layout_do_period_range_kpi -> {
                rcv_sales_do_period_range_kpi.visibility = View.GONE
                rcv_food_do_period_range_kpi.visibility = View.GONE
                rcv_labour_do_period_range_kpi.visibility = View.GONE
                rcv_service_do_period_range_kpi.visibility = View.GONE
                rcv_cash_do_period_range_kpi.visibility = View.GONE

                aws_text_overview_do_period_range_kpi.visibility = View.GONE
                food_text_overview_do_period_range_kpi.visibility = View.GONE
                labour_text_overview_do_period_range_kpi.visibility = View.GONE
                service_text_overview_do_period_range_kpi.visibility = View.GONE
                cash_text_overview_do_period_range_kpi.visibility = View.GONE

                aws_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_oer_do_period_range_kpi.visibility == View.VISIBLE) {
                    rcv_oer_do_period_range_kpi.visibility = View.GONE
                    oer_text_overview_do_period_range_kpi.visibility = View.GONE
                    oer_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_oer_do_period_range_kpi.visibility = View.VISIBLE
                    oer_text_overview_do_period_range_kpi.visibility = View.VISIBLE
                    oer_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }

                callDoPeriodRangeLevelTwoQuery(getString(R.string.oer_text), rcv_oer_do_period_range_kpi)

            }
            R.id.filter_icon -> {
                openDoPeriodKpiFilter()
            }
            R.id.filter_parent_linear -> {
                openDoPeriodKpiFilter()
            }
            R.id.error_filter_parent_linear -> {
                openDoPeriodKpiFilter()
            }
            R.id.aws_text_overview_do_period_range_kpi -> {
                callOverViewDoPeriodRangeKpiApi(getString(R.string.awus_text), "", "")
            }
            R.id.food_text_overview_do_period_range_kpi -> {
                callOverViewDoPeriodRangeKpiApi(getString(R.string.food_text), "", "")
            }
            R.id.labour_text_overview_do_period_range_kpi -> {
                callOverViewDoPeriodRangeKpiApi(getString(R.string.labour_text), "", "")
            }
            R.id.service_text_overview_do_period_range_kpi -> {
                callOverViewDoPeriodRangeKpiApi(getString(R.string.service_text), "", "")
            }
            R.id.oer_text_overview_do_period_range_kpi -> {
                callOverViewDoPeriodRangeKpiApi(getString(R.string.oer_text), "", "")
            }
            R.id.cash_text_overview_do_period_range_kpi -> {
                callOverViewDoPeriodRangeKpiApi(getString(R.string.cash_text), "", "")
            }
        }
    }

    fun callDoPeriodRangeLevelTwoQuery(
            actionForDoPeriodRangeLevel2: String,
            rcvForDoPeriodRangeLevel2: NonScrollExpandableListView
    ) {
        val progressDialogDoPeriodRangeLevel2 = CustomProgressDialog(requireActivity())
        lifecycleScope.launchWhenResumed {
            progressDialogDoPeriodRangeLevel2.showProgressDialog()
            val areaCodeDoPeriodRangeLevel2 = dbHelperPeriodKpi.getAllSelectedAreaList(true)
            val stateCodeDoPeriodRangeLevel2 = dbHelperPeriodKpi.getAllSelectedStoreListState(true)
            val superVisorNumberListDoPeriodRangeLevel2 = dbHelperPeriodKpi.getAllSelectedStoreListSupervisor(true)

            Logger.info(
                    DoPeriodRangeLevelTwoQuery.OPERATION_NAME.name(),
                    "Period Range Level 2 KPI",
                    mapQueryFilters(
                            QueryData(
                                    areaCodeDoPeriodRangeLevel2,
                                    stateCodeDoPeriodRangeLevel2,
                                    superVisorNumberListDoPeriodRangeLevel2,
                                    Collections.emptyList(),
                                    formattedDoPeriodKpiEndDateValue,
                                    formattedDoPeriodKpiStartDateValue,
                                    StorePrefData.filterType,
                                    DoPeriodRangeLevelTwoQuery.QUERY_DOCUMENT
                            )
                    )
            )
            try {
                val responseDoPeriodRangeLevel2 =
                        apolloClient(requireContext()).query(
                                DoPeriodRangeLevelTwoQuery(
                                        areaCodeDoPeriodRangeLevel2.toInput(),
                                        stateCodeDoPeriodRangeLevel2.toInput(),
                                        superVisorNumberListDoPeriodRangeLevel2.toInput(),
                                        StorePrefData.startDateValue,
                                        StorePrefData.endDateValue,
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                )
                        )
                                .await()

                if (responseDoPeriodRangeLevel2.data?.do_ != null) {
                    progressDialogDoPeriodRangeLevel2.dismissProgressDialog()
                    doPeriodRangeLevelTwo = responseDoPeriodRangeLevel2.data?.do_!!

                    doPeriodRangeLevelTwo.kpis?.individualSupervisors.let {
                        setExpandableDataDoPeriodRangeKpi(actionForDoPeriodRangeLevel2, rcvForDoPeriodRangeLevel2)
                    }
                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogDoPeriodRangeLevel2.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Level 2 KPI")
                        }
                refreshDoPeriodKpiToken()
                return@launchWhenResumed

            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogDoPeriodRangeLevel2.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForDoPeriod()
                }
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Period Range Level 2 KPI")
                progressDialogDoPeriodRangeLevel2.dismissProgressDialog()
                setErrorScreenVisibleStateForDoPeriod(
                        getString(R.string.exception_error_text_title),
                        getString(R.string.exception_error_text_description)
                )

            }

        }
    }


    private fun openDoPeriodKpiFilter() {
        val intentDoPeriodKpiFilter = Intent(requireContext(), FilterActivity::class.java)
        startActivity(intentDoPeriodKpiFilter)
    }

    private fun openDoPeriodKpiSalesDetail(doPeriodKpiSalesDetail: DOOverviewRangeQuery.Do_) {
        val intentDoPeriodKpiSalesDetail = Intent(requireContext(), AWUSKpiActivity::class.java)
        intentDoPeriodKpiSalesDetail.putExtra("awus_data", gsonPeriodKpi.toJson(doPeriodKpiSalesDetail))
        intentDoPeriodKpiSalesDetail.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
        startActivity(intentDoPeriodKpiSalesDetail)

    }

    private fun openDoPeriodKpiLabourDetail(doPeriodKpiLabourDetail: DOOverviewRangeQuery.Do_) {
        val intentDoPeriodKpiLabourDetail = Intent(requireContext(), LabourKpiActivity::class.java)
        intentDoPeriodKpiLabourDetail.putExtra("labour_data", gsonPeriodKpi.toJson(doPeriodKpiLabourDetail))
        intentDoPeriodKpiLabourDetail.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
        startActivity(intentDoPeriodKpiLabourDetail)
    }

    private fun openDoPeriodKpiServiceDetail(doPeriodKpiServiceDetail: DOOverviewRangeQuery.Do_) {
        val intentDoPeriodKpiServiceDetail = Intent(requireContext(), ServiceKpiActivity::class.java)
        intentDoPeriodKpiServiceDetail.putExtra("service_data", gsonPeriodKpi.toJson(doPeriodKpiServiceDetail))
        intentDoPeriodKpiServiceDetail.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
        startActivity(intentDoPeriodKpiServiceDetail)
    }

    private fun openDoPeriodKpiOERDetail(doPeriodKpiOERDetail: DOOverviewRangeQuery.Do_) {
        val intentDoPeriodKpiOERDetail = Intent(requireContext(), OERStartActivity::class.java)
        intentDoPeriodKpiOERDetail.putExtra("oer_data", gsonPeriodKpi.toJson(doPeriodKpiOERDetail))
        intentDoPeriodKpiOERDetail.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
        startActivity(intentDoPeriodKpiOERDetail)
    }

    private fun openDoPeriodKpiFoodDetail(doPeriodKpiFoodDetail: DOOverviewRangeQuery.Do_) {
        val intentDoPeriodKpiFoodDetail = Intent(requireContext(), FoodKpiActivity::class.java)
        intentDoPeriodKpiFoodDetail.putExtra("food_data", gsonPeriodKpi.toJson(doPeriodKpiFoodDetail))
        intentDoPeriodKpiFoodDetail.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
        startActivity(intentDoPeriodKpiFoodDetail)
    }

    private fun openDoPeriodKpiCASHDetail(doPeriodKpiCASHDetail: DOOverviewRangeQuery.Do_) {
        val intentDoPeriodKpiCASHDetail = Intent(requireContext(), CashKpiActivity::class.java)
        intentDoPeriodKpiCASHDetail.putExtra("cash_data", gsonPeriodKpi.toJson(doPeriodKpiCASHDetail))
        intentDoPeriodKpiCASHDetail.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
        startActivity(intentDoPeriodKpiCASHDetail)
    }


    private fun refreshDoPeriodKpiToken() {

        refreshTokenViewModelDoPeriodRange.getRefreshToken()

        refreshTokenViewModelDoPeriodRange.refreshTokenResponseLiveData.observe(requireActivity(), {
            run {
                when (it.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        callDoPeriodRangeLevelOneQuery()
                    }
                    Status.UNSUCCESSFUL -> {
                        CommonUtil.navigateToLogin(mainActivity)
                    }
                    Status.ERROR -> {
                    }
                    Status.OFFLINE -> {
                        setInternetErrorScreenVisibleStateForDoPeriod()
                    }
                }
            }
        })
    }


    fun setInternetErrorScreenVisibleStateForDoPeriod() {
        do_range_no_internet_error_layout.visibility = View.VISIBLE
        common_header_do_range.sales_header_error_image.visibility = View.VISIBLE
        common_header_do_range.sales_header_error_image.setImageResource(R.drawable.ic_internet_error)
        do_period_range_missing_data_error_layout.visibility = View.GONE
        common_header_do_range.error_filter_parent_linear.visibility = View.GONE
        do_range_error_layout.visibility = View.GONE
        setCalendarViewVisibleStateForDoPeriod()
        setHeaderViewsVisibleStateForDoPeriod()
        showStoreFilterVisibilityStateForDoPeriod()
    }

    fun setErrorScreenVisibleStateForDoPeriod(
            title: String,
            description: String
    ) {
        do_range_error_layout.visibility = View.VISIBLE
        do_range_error_layout.exception_text_title.text = title
        do_range_error_layout.exception_text_description.text = description
        common_header_do_range.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)
        common_header_do_range.sales_header_error_image.visibility = View.VISIBLE
        common_header_do_range.error_filter_parent_linear.visibility = View.VISIBLE
        do_period_range_missing_data_error_layout.visibility = View.GONE
        setCalendarViewVisibleStateForDoPeriod()
        setHeaderViewsVisibleStateForDoPeriod()
        hideStoreFilterVisibilityStateForDoPeriod()
    }

    fun setHeaderViewsVisibleStateForDoPeriod() {
        do_range_header.visibility = View.GONE
        do_range_v1.visibility = View.GONE
        do_range_layout.visibility = View.INVISIBLE
        common_header_do_range.total_sales_common_header.visibility = View.GONE
        common_header_do_range.sales_text_common_header.visibility = View.GONE
    }

    fun hideStoreFilterVisibilityStateForDoPeriod(){
        common_header_do_range.filter_parent_linear.visibility = View.GONE
    }
    fun showStoreFilterVisibilityStateForDoPeriod(){
        common_header_do_range.filter_parent_linear.visibility = View.VISIBLE
    }

    fun setCalendarViewVisibleStateForDoPeriod() {
        common_calendar_do_range.visibility = View.GONE
    }

    fun setMissingDataViewVisibleStateForDoPeriod(
            missingDataTitle: String,
            missingDataDescription: String
    ) {
        do_period_range_missing_data_error_layout.visibility = View.VISIBLE
        do_period_range_missing_data_error_layout.header_data_title.text = missingDataTitle
        do_period_range_missing_data_error_layout.header_data_description.text = missingDataDescription

    }

    fun hideErrorScreenVisibleStateForDoPeriod(){
        do_range_no_internet_error_layout.visibility = View.GONE
        do_range_error_layout.visibility = View.GONE
        common_header_do_range.sales_header_error_image.visibility = View.GONE
        common_header_do_range.error_filter_parent_linear.visibility = View.GONE

        do_range_header.visibility = View.VISIBLE
        do_range_v1.visibility = View.VISIBLE
        do_range_layout.visibility = View.VISIBLE

        common_header_do_range.filter_parent_linear.visibility = View.VISIBLE
        common_header_do_range.total_sales_common_header.visibility = View.VISIBLE
        common_header_do_range.sales_text_common_header.visibility = View.VISIBLE

        common_calendar_do_range.visibility = View.VISIBLE
    }

    fun collapseExpendedListVisibilityForDoPeriod(){
        if (rcv_sales_do_period_range_kpi.visibility == View.VISIBLE) {
            rcv_sales_do_period_range_kpi.visibility = View.GONE
            aws_text_overview_do_period_range_kpi.visibility = View.GONE
            aws_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_food_do_period_range_kpi.visibility == View.VISIBLE) {
            rcv_food_do_period_range_kpi.visibility = View.GONE
            food_text_overview_do_period_range_kpi.visibility = View.GONE
            food_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_labour_do_period_range_kpi.visibility == View.VISIBLE) {
            rcv_labour_do_period_range_kpi.visibility = View.GONE
            labour_text_overview_do_period_range_kpi.visibility = View.GONE
            labor_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_service_do_period_range_kpi.visibility == View.VISIBLE) {
            rcv_service_do_period_range_kpi.visibility = View.GONE
            service_text_overview_do_period_range_kpi.visibility = View.GONE
            service_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_cash_do_period_range_kpi.visibility == View.VISIBLE) {
            rcv_cash_do_period_range_kpi.visibility = View.GONE
            cash_text_overview_do_period_range_kpi.visibility = View.GONE
            cash_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_oer_do_period_range_kpi.visibility == View.VISIBLE) {
            rcv_oer_do_period_range_kpi.visibility = View.GONE
            oer_text_overview_do_period_range_kpi.visibility = View.GONE
            oer_parent_img_do_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
    }

}