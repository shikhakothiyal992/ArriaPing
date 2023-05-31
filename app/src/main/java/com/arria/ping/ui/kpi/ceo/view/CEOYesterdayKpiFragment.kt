package com.arria.ping.ui.kpi.ceo.view

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
import com.arria.ping.kpi.*
import com.arria.ping.kpi.ceo.CEOYesterdayLevelOneQuery
import com.arria.ping.kpi.ceo.CEOYesterdayLevelTwoQuery
import com.arria.ping.log.Logger
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.model.StoreDetailPojo
import com.arria.ping.ui.filter.FilterActivity
import com.arria.ping.ui.generalview.MainActivity
import com.arria.ping.ui.kpi.ceo.adapter.CustomExpandableListAdapterYesterdayCEO
import com.arria.ping.ui.kpi.overview.*
import com.arria.ping.ui.refreshtoken.model.Status
import com.arria.ping.ui.refreshtoken.viewmodel.RefreshTokenViewModel
import com.arria.ping.util.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.activity_login_activty.*
import kotlinx.android.synthetic.main.ceo_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.ceo_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.common_calendar.view.*
import kotlinx.android.synthetic.main.common_header_ceo.*
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.data_error_layout.view.*
import kotlinx.android.synthetic.main.do_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.do_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.gm_yesterday_fragment_kpi.*
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class CEOYesterdayKpiFragment : Fragment(), View.OnClickListener {

    lateinit var ceoYesterdayLevelOne: CEOYesterdayLevelOneQuery.Ceo
    lateinit var ceoYesterdayLevelTwo: CEOYesterdayLevelTwoQuery.Ceo

    lateinit var individualStoreDetails: CEOYesterdayLvlThreeQuery.Ceo
    private var lastExpandedPositionCEOYesterdayKpi = -1
    private var expandableListDetailCEOYesterdayKpi = HashMap<String, List<StoreDetailPojo>>()
    private var expandableListAdapterCEOYesterdayKpi: CustomExpandableListAdapterYesterdayCEO? =
            null
    lateinit var dbHelperCEOYesterdayKpi: DatabaseHelperImpl
    private val gsonCEOYesterdayKpi = Gson()


    private val refreshTokenViewModelCeoYesterday by viewModels<RefreshTokenViewModel>()

    @Inject
    lateinit var networkHelper: NetworkHelper

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
        return inflater.inflate(R.layout.ceo_yesterday_fragment_kpi, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        dbHelperCEOYesterdayKpi = DatabaseHelperImpl(DatabaseBuilder.getInstance(requireActivity()))

        initYesterdayCEO()

        if (StorePrefData.filterDate.isNotEmpty()) {
            setStoreFilterViewForCEOYesterday(StorePrefData.filterDate)
        }

        if (networkHelper.isNetworkConnected()) {
            callMissingDataQueryForCeoYesterday()
            callCeoYesterdayLevelOneQuery()
        } else {
            setInternetErrorScreenVisibleStateForCeoYesterday()
        }

        ceo_yesterday_swipe_refresh_layout.setOnRefreshListener{
            Logger.info("Pull down to refresh CEO Yesterday Store Data", "Yesterday KPI")
            callMissingDataQueryForCeoYesterday()
            callCeoYesterdayLevelOneQuery()
            collapseExpendedListVisibilityForCEOYesterday()
            ceo_yesterday_swipe_refresh_layout.isRefreshing = false
        }
    }


    private fun initYesterdayCEO() {
        aws_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        food_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        labor_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        service_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        oer_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        cash_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

        aws_parent_layout_yesterday_kpi.setOnClickListener(this)
        food_parent_layout_yesterday_kpi.setOnClickListener(this)
        labor_parent_layout_yesterday_kpi.setOnClickListener(this)
        service_parent_layout_yesterday_kpi.setOnClickListener(this)
        cash_parent_layout_yesterday_kpi.setOnClickListener(this)
        oer_parent_layout_yesterday_kpi.setOnClickListener(this)

        filter_icon.setOnClickListener(this)
        filter_parent_linear.setOnClickListener(this)
        error_filter_parent_linear.setOnClickListener(this)

        aws_text_overview_yesterday_kpi.setOnClickListener(this)
        ideal_vs_food_variance_text_overview_yesterday_kpi.setOnClickListener(this)
        labour_text_overview_yesterday_kpi.setOnClickListener(this)
        service_text_overview_yesterday_kpi.setOnClickListener(this)
        oer_text_overview_yesterday_kpi.setOnClickListener(this)
        cash_text_overview_yesterday_kpi.setOnClickListener(this)

        // set calendar for previous day
        Validation().setCustomCalendar(common_calendar.square_day)
    }

    fun callCeoYesterdayLevelTwoQuery(
            ceoYesterdayActionForLevel2: String,
            ceoYesterdayRcvForLevel2: NonScrollExpandableListView
    ) {
        val progressDialogCeoYesterdayLevel2 = CustomProgressDialog(requireActivity())
        lifecycleScope.launchWhenResumed {
            progressDialogCeoYesterdayLevel2.showProgressDialog()
            val areaCodeCEOYesterdayLevel2 = dbHelperCEOYesterdayKpi.getAllSelectedAreaList(true)
            val stateCodeCEOYesterdayLevel2 = dbHelperCEOYesterdayKpi.getAllSelectedStoreListState(true)
            val superVisorNumberListCEOYesterdayLevel2 = dbHelperCEOYesterdayKpi.getAllSelectedStoreListSupervisor(true)

            Logger.info(
                    CEOYesterdayLevelTwoQuery.OPERATION_NAME.name(), "Yesterday Level 2 KPI",
                    mapQueryFilters(
                            areaCodeCEOYesterdayLevel2,
                            stateCodeCEOYesterdayLevel2,
                            superVisorNumberListCEOYesterdayLevel2,
                            Collections.emptyList(),
                            CEOYesterdayLevelTwoQuery.QUERY_DOCUMENT
                    )
            )


            try {
                val responseCEOYesterdayLevelTwo =
                        apolloClient(requireContext()).query(
                                CEOYesterdayLevelTwoQuery(
                                        areaCodeCEOYesterdayLevel2.toInput(),
                                        stateCodeCEOYesterdayLevel2.toInput(),
                                        superVisorNumberListCEOYesterdayLevel2.toInput(),
                                )
                        )
                                .await()

                if (responseCEOYesterdayLevelTwo.data?.ceo != null) {
                    progressDialogCeoYesterdayLevel2.dismissProgressDialog()
                    ceoYesterdayLevelTwo = responseCEOYesterdayLevelTwo.data?.ceo!!
                    ceoYesterdayLevelTwo.kpis?.individualSupervisors.let {
                        setExpandableCEOYesterdayKpiData(ceoYesterdayActionForLevel2, ceoYesterdayRcvForLevel2)
                    }
                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogCeoYesterdayLevel2.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Level 2 KPI")
                        }
                refreshYesterdayKpiToken()
                return@launchWhenResumed
            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogCeoYesterdayLevel2.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForCeoYesterday()
                }
            } catch (e: ApolloException) {
                progressDialogCeoYesterdayLevel2.dismissProgressDialog()
                Logger.error(e.message.toString(), "Yesterday Level 2 KPI")
                setErrorScreenVisibleStateForCeoYesterday(
                        getString(R.string.exception_error_text_title),
                        getString(R.string.exception_error_text_description)
                )
            }
        }
    }


    private fun setExpandableCEOYesterdayKpiData(
            actionExpandableCEOYesterdayKpi: String,
            rcvExpandableCEOYesterdayKpi: NonScrollExpandableListView
    ) {
        val childDataExpandableCEOYesterdayKpi: MutableList<StoreDetailPojo> = ArrayList()
        val titleListExpandableCEOYesterdayKpi = ArrayList<String>()

        ceoYesterdayLevelTwo.kpis!!.individualSupervisors.forEachIndexed {_, item ->
            expandableListDetailCEOYesterdayKpi[item!!.supervisorName!!] = childDataExpandableCEOYesterdayKpi
            titleListExpandableCEOYesterdayKpi.add(item.supervisorName.toString())

        }
        expandableListAdapterCEOYesterdayKpi = CustomExpandableListAdapterYesterdayCEO(
                requireContext(),
                titleListExpandableCEOYesterdayKpi,
                expandableListDetailCEOYesterdayKpi,
                ceoYesterdayLevelTwo,
                actionExpandableCEOYesterdayKpi
        )
        rcvExpandableCEOYesterdayKpi.setAdapter(expandableListAdapterCEOYesterdayKpi)

        rcvExpandableCEOYesterdayKpi.setOnGroupExpandListener {groupPosition ->
            val ceoYesterdayExpandableSuperVisorNumberValue =
                    ceoYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.supervisorNumber!!

            val ceoYesterdayExpandableSupervisorSalesKpiData =
                    ceoYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.yesterday?.sales
            val ceoYesterdayExpandableSupervisorFoodKpiData =
                    ceoYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.yesterday?.food
            val ceoYesterdayExpandableSupervisorLaborKpiData =
                    ceoYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.yesterday?.labor

            val ceoYesterdayExpandableSupervisorOERKpiData =
                    ceoYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.yesterday?.oerStart
            val ceoYesterdayExpandableSupervisorCashKpiData =
                    ceoYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.yesterday?.cash

            //Sales
            val ceoYesterdayExpandableSupervisorSalesKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoYesterdayExpandableSupervisorSalesKpiData?.goal?.amount,
                    ceoYesterdayExpandableSupervisorSalesKpiData?.goal?.value,
                    ceoYesterdayExpandableSupervisorSalesKpiData?.goal?.percentage
            )
            val ceoYesterdayExpandableSupervisorSalesKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoYesterdayExpandableSupervisorSalesKpiData?.variance?.amount,
                    ceoYesterdayExpandableSupervisorSalesKpiData?.variance?.value,
                    ceoYesterdayExpandableSupervisorSalesKpiData?.variance?.percentage
            )
            val ceoYesterdayExpandableSupervisorSalesKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoYesterdayExpandableSupervisorSalesKpiData?.actual?.amount,
                    ceoYesterdayExpandableSupervisorSalesKpiData?.actual?.value,
                    ceoYesterdayExpandableSupervisorSalesKpiData?.actual?.percentage
            )

            //Food
            val ceoYesterdayExpandableSupervisorFoodKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoYesterdayExpandableSupervisorFoodKpiData?.goal?.amount,
                    ceoYesterdayExpandableSupervisorFoodKpiData?.goal?.value,
                    ceoYesterdayExpandableSupervisorFoodKpiData?.goal?.percentage
            )
            val ceoYesterdayExpandableSupervisorFoodKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoYesterdayExpandableSupervisorFoodKpiData?.variance?.amount,
                    ceoYesterdayExpandableSupervisorFoodKpiData?.variance?.value,
                    ceoYesterdayExpandableSupervisorFoodKpiData?.variance?.percentage
            )
            val ceoYesterdayExpandableSupervisorFoodKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoYesterdayExpandableSupervisorFoodKpiData?.actual?.amount,
                    ceoYesterdayExpandableSupervisorFoodKpiData?.actual?.value,
                    ceoYesterdayExpandableSupervisorFoodKpiData?.actual?.percentage
            )

            //Labor
            val ceoYesterdayExpandableSupervisorLaborKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoYesterdayExpandableSupervisorLaborKpiData?.goal?.amount,
                    ceoYesterdayExpandableSupervisorLaborKpiData?.goal?.value,
                    ceoYesterdayExpandableSupervisorLaborKpiData?.goal?.percentage
            )
            val ceoYesterdayExpandableSupervisorLaborKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoYesterdayExpandableSupervisorLaborKpiData?.variance?.amount,
                    ceoYesterdayExpandableSupervisorLaborKpiData?.variance?.value,
                    ceoYesterdayExpandableSupervisorLaborKpiData?.variance?.percentage
            )
            val ceoYesterdayExpandableSupervisorLaborKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoYesterdayExpandableSupervisorLaborKpiData?.actual?.amount,
                    ceoYesterdayExpandableSupervisorLaborKpiData?.actual?.value,
                    ceoYesterdayExpandableSupervisorLaborKpiData?.actual?.percentage
            )

            //OER
            val ceoYesterdayExpandableSupervisorOERKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoYesterdayExpandableSupervisorOERKpiData?.goal?.amount,
                    ceoYesterdayExpandableSupervisorOERKpiData?.goal?.value,
                    ceoYesterdayExpandableSupervisorOERKpiData?.goal?.percentage
            )
            val ceoYesterdayExpandableSupervisorOERKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoYesterdayExpandableSupervisorOERKpiData?.variance?.amount,
                    ceoYesterdayExpandableSupervisorOERKpiData?.variance?.value,
                    ceoYesterdayExpandableSupervisorOERKpiData?.variance?.percentage
            )
            val ceoYesterdayExpandableSupervisorOERKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoYesterdayExpandableSupervisorOERKpiData?.actual?.amount,
                    ceoYesterdayExpandableSupervisorOERKpiData?.actual?.value,
                    ceoYesterdayExpandableSupervisorOERKpiData?.actual?.percentage
            )

            //Cash
            val ceoYesterdayExpandableSupervisorCashKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoYesterdayExpandableSupervisorCashKpiData?.actual?.amount,
                    ceoYesterdayExpandableSupervisorCashKpiData?.actual?.value,
                    ceoYesterdayExpandableSupervisorCashKpiData?.actual?.percentage
            )

            when {
                rcv_sales_yesterday_kpi.visibility == View.VISIBLE -> {
                    if (ceoYesterdayExpandableSupervisorSalesKpiDataGoal.isEmpty() && ceoYesterdayExpandableSupervisorSalesKpiDataVariance.isEmpty() && ceoYesterdayExpandableSupervisorSalesKpiDataActual.isEmpty()) {
                        rcvExpandableCEOYesterdayKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvExpandableCEOYesterdayKpi.isGroupExpanded(groupPosition)) {
                            callStoreAgainstSupervisor(
                                    titleListExpandableCEOYesterdayKpi[groupPosition],
                                    actionExpandableCEOYesterdayKpi,
                                    ceoYesterdayExpandableSuperVisorNumberValue
                            )
                        } else
                            rcvExpandableCEOYesterdayKpi.collapseGroup(groupPosition)
                    }
                }

                rcv_food_yesterday_kpi.visibility == View.VISIBLE -> {
                    if (ceoYesterdayExpandableSupervisorFoodKpiDataGoal.isEmpty() && ceoYesterdayExpandableSupervisorFoodKpiDataVariance.isEmpty() && ceoYesterdayExpandableSupervisorFoodKpiDataActual.isEmpty()) {
                        rcvExpandableCEOYesterdayKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvExpandableCEOYesterdayKpi.isGroupExpanded(groupPosition)) {
                            callStoreAgainstSupervisor(
                                    titleListExpandableCEOYesterdayKpi[groupPosition],
                                    actionExpandableCEOYesterdayKpi,
                                    ceoYesterdayExpandableSuperVisorNumberValue
                            )
                        } else
                            rcvExpandableCEOYesterdayKpi.collapseGroup(groupPosition)
                    }
                }

                rcv_labour_yesterday_kpi.visibility == View.VISIBLE -> {
                    if (ceoYesterdayExpandableSupervisorLaborKpiDataGoal.isEmpty() && ceoYesterdayExpandableSupervisorLaborKpiDataVariance.isEmpty() && ceoYesterdayExpandableSupervisorLaborKpiDataActual.isEmpty()) {
                        rcvExpandableCEOYesterdayKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvExpandableCEOYesterdayKpi.isGroupExpanded(groupPosition)) {
                            callStoreAgainstSupervisor(
                                    titleListExpandableCEOYesterdayKpi[groupPosition],
                                    actionExpandableCEOYesterdayKpi,
                                    ceoYesterdayExpandableSuperVisorNumberValue
                            )
                        } else
                            rcvExpandableCEOYesterdayKpi.collapseGroup(groupPosition)
                    }
                }

                rcv_service_yesterday_kpi.visibility == View.VISIBLE -> {

                    if (ceoYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.yesterday == null) {
                        rcvExpandableCEOYesterdayKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvExpandableCEOYesterdayKpi.isGroupExpanded(groupPosition)) {
                            callStoreAgainstSupervisor(
                                    titleListExpandableCEOYesterdayKpi[groupPosition],
                                    actionExpandableCEOYesterdayKpi,
                                    ceoYesterdayExpandableSuperVisorNumberValue
                            )
                        } else
                            rcvExpandableCEOYesterdayKpi.collapseGroup(groupPosition)
                    }
                }

                rcv_oer_yesterday_kpi.visibility == View.VISIBLE -> {
                    if (ceoYesterdayExpandableSupervisorOERKpiDataGoal.isEmpty() && ceoYesterdayExpandableSupervisorOERKpiDataVariance.isEmpty() && ceoYesterdayExpandableSupervisorOERKpiDataActual.isEmpty()) {
                        rcvExpandableCEOYesterdayKpi.collapseGroup(groupPosition)
                    } else {
                        if (rcvExpandableCEOYesterdayKpi.isGroupExpanded(groupPosition)) {
                            callStoreAgainstSupervisor(
                                    titleListExpandableCEOYesterdayKpi[groupPosition],
                                    actionExpandableCEOYesterdayKpi,
                                    ceoYesterdayExpandableSuperVisorNumberValue
                            )
                        } else
                            rcvExpandableCEOYesterdayKpi.collapseGroup(groupPosition)
                    }

                }

                rcv_cash_yesterday_kpi.visibility == View.VISIBLE -> {
                    if (ceoYesterdayExpandableSupervisorCashKpiDataActual.isNotEmpty()) {
                        if (rcvExpandableCEOYesterdayKpi.isGroupExpanded(groupPosition)) {
                            callStoreAgainstSupervisor(
                                    titleListExpandableCEOYesterdayKpi[groupPosition],
                                    actionExpandableCEOYesterdayKpi,
                                    ceoYesterdayExpandableSuperVisorNumberValue
                            )
                        } else
                            rcvExpandableCEOYesterdayKpi.collapseGroup(groupPosition)
                    } else {
                        rcvExpandableCEOYesterdayKpi.collapseGroup(groupPosition)
                    }

                }
            }

            if (lastExpandedPositionCEOYesterdayKpi != -1 && groupPosition != lastExpandedPositionCEOYesterdayKpi) {
                rcvExpandableCEOYesterdayKpi.collapseGroup(lastExpandedPositionCEOYesterdayKpi)
            }
            lastExpandedPositionCEOYesterdayKpi = groupPosition
        }

        rcvExpandableCEOYesterdayKpi.setOnChildClickListener {_, _, groupPosition, childPosition, _ ->
            val superVisorNumber = ceoYesterdayLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.supervisorNumber!!

            if (childPosition == 0) {

                when {
                    rcv_sales_yesterday_kpi.visibility == View.VISIBLE -> {
                        callOverViewCEOYesterdayKpiApi(getString(R.string.awus_text), superVisorNumber, "")
                    }
                    rcv_food_yesterday_kpi.visibility == View.VISIBLE -> {
                        callOverViewCEOYesterdayKpiApi(getString(R.string.food_text), superVisorNumber, "")
                    }
                    rcv_labour_yesterday_kpi.visibility == View.VISIBLE -> {
                        callOverViewCEOYesterdayKpiApi(getString(R.string.labour_text), superVisorNumber, "")
                    }
                    rcv_service_yesterday_kpi.visibility == View.VISIBLE -> {
                        callOverViewCEOYesterdayKpiApi(getString(R.string.service_text), superVisorNumber, "")
                    }
                    rcv_oer_yesterday_kpi.visibility == View.VISIBLE -> {
                        callOverViewCEOYesterdayKpiApi(getString(R.string.oer_text), superVisorNumber, "")
                    }
                    rcv_cash_yesterday_kpi.visibility == View.VISIBLE -> {
                        callOverViewCEOYesterdayKpiApi(getString(R.string.cash_text), superVisorNumber, "")
                    }
                }
            }else{

                val storeNumber =
                        expandableListDetailCEOYesterdayKpi[titleListExpandableCEOYesterdayKpi[groupPosition]]!![(childPosition)].storeNumber!!

                val ceoYesterdayKpiData =
                        expandableListDetailCEOYesterdayKpi[titleListExpandableCEOYesterdayKpi[groupPosition]]!![(childPosition)]
                if (ceoYesterdayKpiData.storeGoal?.isNotEmpty() == true || ceoYesterdayKpiData.storeVariance?.isNotEmpty() == true || ceoYesterdayKpiData.storeActual?.isNotEmpty() == true) {
                    when {
                        rcv_sales_yesterday_kpi.visibility == View.VISIBLE -> {
                            callOverViewCEOYesterdayKpiApi(getString(R.string.awus_text), superVisorNumber, storeNumber)
                        }
                        rcv_food_yesterday_kpi.visibility == View.VISIBLE -> {
                            callOverViewCEOYesterdayKpiApi(getString(R.string.food_text), superVisorNumber, storeNumber)
                        }
                        rcv_labour_yesterday_kpi.visibility == View.VISIBLE -> {
                            callOverViewCEOYesterdayKpiApi(getString(R.string.labour_text), superVisorNumber, storeNumber)
                        }

                        rcv_oer_yesterday_kpi.visibility == View.VISIBLE -> {
                            callOverViewCEOYesterdayKpiApi(getString(R.string.oer_text), superVisorNumber, storeNumber)
                        }
                        rcv_cash_yesterday_kpi.visibility == View.VISIBLE -> {
                            callOverViewCEOYesterdayKpiApi(getString(R.string.cash_text), superVisorNumber, storeNumber)
                        }
                    }
                }

                if (rcv_service_yesterday_kpi.visibility == View.VISIBLE) {
                    callOverViewCEOYesterdayKpiApi(getString(R.string.service_text), superVisorNumber, storeNumber)
                }
            }
            false
        }

    }


    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.aws_parent_layout_yesterday_kpi -> {

                rcv_food_yesterday_kpi.visibility = View.GONE
                rcv_labour_yesterday_kpi.visibility = View.GONE
                rcv_service_yesterday_kpi.visibility = View.GONE
                rcv_oer_yesterday_kpi.visibility = View.GONE
                rcv_cash_yesterday_kpi.visibility = View.GONE

                ideal_vs_food_variance_text_overview_yesterday_kpi.visibility = View.GONE
                labour_text_overview_yesterday_kpi.visibility = View.GONE
                service_text_overview_yesterday_kpi.visibility = View.GONE
                oer_text_overview_yesterday_kpi.visibility = View.GONE
                cash_text_overview_yesterday_kpi.visibility = View.GONE

                food_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_sales_yesterday_kpi.visibility == View.VISIBLE) {
                    rcv_sales_yesterday_kpi.visibility = View.GONE
                    aws_text_overview_yesterday_kpi.visibility = View.GONE
                    aws_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_sales_yesterday_kpi.visibility = View.VISIBLE
                    aws_text_overview_yesterday_kpi.visibility = View.VISIBLE
                    aws_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_up)
                }
                this.callCeoYesterdayLevelTwoQuery(
                        getString(R.string.awus_text),
                        rcv_sales_yesterday_kpi
                )
            }
            R.id.food_parent_layout_yesterday_kpi -> {
                rcv_sales_yesterday_kpi.visibility = View.GONE
                rcv_labour_yesterday_kpi.visibility = View.GONE
                rcv_service_yesterday_kpi.visibility = View.GONE
                rcv_oer_yesterday_kpi.visibility = View.GONE
                rcv_cash_yesterday_kpi.visibility = View.GONE

                aws_text_overview_yesterday_kpi.visibility = View.GONE
                labour_text_overview_yesterday_kpi.visibility = View.GONE
                service_text_overview_yesterday_kpi.visibility = View.GONE
                oer_text_overview_yesterday_kpi.visibility = View.GONE
                cash_text_overview_yesterday_kpi.visibility = View.GONE

                aws_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_food_yesterday_kpi.visibility == View.VISIBLE) {
                    rcv_food_yesterday_kpi.visibility = View.GONE
                    ideal_vs_food_variance_text_overview_yesterday_kpi.visibility = View.GONE
                    food_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_food_yesterday_kpi.visibility = View.VISIBLE
                    ideal_vs_food_variance_text_overview_yesterday_kpi.visibility = View.VISIBLE
                    food_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callCeoYesterdayLevelTwoQuery(
                        getString(R.string.ideal_vs_food_variance_text),
                        rcv_food_yesterday_kpi
                )
            }
            R.id.labor_parent_layout_yesterday_kpi -> {
                rcv_sales_yesterday_kpi.visibility = View.GONE
                rcv_food_yesterday_kpi.visibility = View.GONE
                rcv_service_yesterday_kpi.visibility = View.GONE
                rcv_oer_yesterday_kpi.visibility = View.GONE
                rcv_cash_yesterday_kpi.visibility = View.GONE

                aws_text_overview_yesterday_kpi.visibility = View.GONE
                ideal_vs_food_variance_text_overview_yesterday_kpi.visibility = View.GONE
                service_text_overview_yesterday_kpi.visibility = View.GONE
                oer_text_overview_yesterday_kpi.visibility = View.GONE
                cash_text_overview_yesterday_kpi.visibility = View.GONE

                aws_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_labour_yesterday_kpi.visibility == View.VISIBLE) {
                    rcv_labour_yesterday_kpi.visibility = View.GONE
                    labour_text_overview_yesterday_kpi.visibility = View.GONE
                    labor_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_labour_yesterday_kpi.visibility = View.VISIBLE
                    labour_text_overview_yesterday_kpi.visibility = View.VISIBLE
                    labor_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callCeoYesterdayLevelTwoQuery(getString(R.string.labour_text), rcv_labour_yesterday_kpi)

            }
            R.id.service_parent_layout_yesterday_kpi -> {
                rcv_sales_yesterday_kpi.visibility = View.GONE
                rcv_food_yesterday_kpi.visibility = View.GONE
                rcv_labour_yesterday_kpi.visibility = View.GONE
                rcv_oer_yesterday_kpi.visibility = View.GONE
                rcv_cash_yesterday_kpi.visibility = View.GONE

                aws_text_overview_yesterday_kpi.visibility = View.GONE
                ideal_vs_food_variance_text_overview_yesterday_kpi.visibility = View.GONE
                labour_text_overview_yesterday_kpi.visibility = View.GONE
                oer_text_overview_yesterday_kpi.visibility = View.GONE
                cash_text_overview_yesterday_kpi.visibility = View.GONE

                aws_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_service_yesterday_kpi.visibility == View.VISIBLE) {
                    rcv_service_yesterday_kpi.visibility = View.GONE
                    service_text_overview_yesterday_kpi.visibility = View.GONE
                    service_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_service_yesterday_kpi.visibility = View.VISIBLE
                    service_text_overview_yesterday_kpi.visibility = View.VISIBLE
                    service_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callCeoYesterdayLevelTwoQuery(getString(R.string.service_text), rcv_service_yesterday_kpi)

            }
            R.id.cash_parent_layout_yesterday_kpi -> {
                rcv_sales_yesterday_kpi.visibility = View.GONE
                rcv_food_yesterday_kpi.visibility = View.GONE
                rcv_labour_yesterday_kpi.visibility = View.GONE
                rcv_service_yesterday_kpi.visibility = View.GONE
                rcv_oer_yesterday_kpi.visibility = View.GONE

                aws_text_overview_yesterday_kpi.visibility = View.GONE
                ideal_vs_food_variance_text_overview_yesterday_kpi.visibility = View.GONE
                labour_text_overview_yesterday_kpi.visibility = View.GONE
                service_text_overview_yesterday_kpi.visibility = View.GONE
                oer_text_overview_yesterday_kpi.visibility = View.GONE


                aws_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_cash_yesterday_kpi.visibility == View.VISIBLE) {
                    rcv_cash_yesterday_kpi.visibility = View.GONE
                    cash_text_overview_yesterday_kpi.visibility = View.GONE
                    cash_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_cash_yesterday_kpi.visibility = View.VISIBLE
                    cash_text_overview_yesterday_kpi.visibility = View.VISIBLE
                    cash_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callCeoYesterdayLevelTwoQuery(getString(R.string.cash_text), rcv_cash_yesterday_kpi)

            }
            R.id.oer_parent_layout_yesterday_kpi -> {
                rcv_sales_yesterday_kpi.visibility = View.GONE
                rcv_food_yesterday_kpi.visibility = View.GONE
                rcv_labour_yesterday_kpi.visibility = View.GONE
                rcv_service_yesterday_kpi.visibility = View.GONE
                rcv_cash_yesterday_kpi.visibility = View.GONE

                aws_text_overview_yesterday_kpi.visibility = View.GONE
                ideal_vs_food_variance_text_overview_yesterday_kpi.visibility = View.GONE
                labour_text_overview_yesterday_kpi.visibility = View.GONE
                service_text_overview_yesterday_kpi.visibility = View.GONE
                cash_text_overview_yesterday_kpi.visibility = View.GONE

                aws_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_oer_yesterday_kpi.visibility == View.VISIBLE) {
                    rcv_oer_yesterday_kpi.visibility = View.GONE
                    oer_text_overview_yesterday_kpi.visibility = View.GONE
                    oer_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_oer_yesterday_kpi.visibility = View.VISIBLE
                    oer_text_overview_yesterday_kpi.visibility = View.VISIBLE
                    oer_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callCeoYesterdayLevelTwoQuery(getString(R.string.oer_text), rcv_oer_yesterday_kpi)

            }
            R.id.filter_icon -> {
                openFilterCEOYesterdayKpi()
            }
            R.id.filter_parent_linear -> {
                openFilterCEOYesterdayKpi()
            }
            R.id.error_filter_parent_linear -> {
                openFilterCEOYesterdayKpi()
            }
            R.id.aws_text_overview_yesterday_kpi -> {
                callOverViewCEOYesterdayKpiApi(getString(R.string.awus_text), "", "")
            }
            R.id.ideal_vs_food_variance_text_overview_yesterday_kpi -> {
                callOverViewCEOYesterdayKpiApi(getString(R.string.food_text), "", "")
            }
            R.id.labour_text_overview_yesterday_kpi -> {
                callOverViewCEOYesterdayKpiApi(getString(R.string.labour_text), "", "")
            }
            R.id.service_text_overview_yesterday_kpi -> {
                callOverViewCEOYesterdayKpiApi(getString(R.string.service_text), "", "")
            }
            R.id.oer_text_overview_yesterday_kpi -> {
                callOverViewCEOYesterdayKpiApi(getString(R.string.oer_text), "", "")
            }
            R.id.cash_text_overview_yesterday_kpi -> {
                callOverViewCEOYesterdayKpiApi(getString(R.string.cash_text), "", "")
            }
        }
    }

    private fun openFilterCEOYesterdayKpi() {
        val intentCEOYesterdayKpi = Intent(requireContext(), FilterActivity::class.java)
        startActivity(intentCEOYesterdayKpi)
    }

    private fun openSalesCEOYesterdayKpiDetail(ceoSalesYesterdayKpi: CEOOverviewYesterdayQuery.Ceo) {
        val intentSalesCEOYesterdayKpi = Intent(requireContext(), AWUSKpiActivity::class.java)
        intentSalesCEOYesterdayKpi.putExtra("awus_data", gsonCEOYesterdayKpi.toJson(ceoSalesYesterdayKpi))
        intentSalesCEOYesterdayKpi.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentSalesCEOYesterdayKpi)

    }

    private fun openLabourCEOYesterdayKpiDetail(ceoLabourYesterdayKpi: CEOOverviewYesterdayQuery.Ceo) {
        val intentLabourCEOYesterdayKpi = Intent(requireContext(), LabourKpiActivity::class.java)
        intentLabourCEOYesterdayKpi.putExtra("labour_data", gsonCEOYesterdayKpi.toJson(ceoLabourYesterdayKpi))
        intentLabourCEOYesterdayKpi.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentLabourCEOYesterdayKpi)
    }

    private fun openServiceCEOYesterdayKpiDetail(ceoServiceYesterdayKpi: CEOOverviewYesterdayQuery.Ceo) {
        val intentServiceCEOYesterdayKpi = Intent(requireContext(), ServiceKpiActivity::class.java)
        intentServiceCEOYesterdayKpi.putExtra("service_data", gsonCEOYesterdayKpi.toJson(ceoServiceYesterdayKpi))
        intentServiceCEOYesterdayKpi.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentServiceCEOYesterdayKpi)
    }

    private fun openOERCEOYesterdayKpiDetail(ceoOERYesterdayKpi: CEOOverviewYesterdayQuery.Ceo) {
        val intentOERCEOYesterdayKpi = Intent(requireContext(), OERStartActivity::class.java)
        intentOERCEOYesterdayKpi.putExtra("oer_data", gsonCEOYesterdayKpi.toJson(ceoOERYesterdayKpi))
        intentOERCEOYesterdayKpi.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentOERCEOYesterdayKpi)
    }

    private fun openFoodCEOYesterdayKpiDetail(ceoFoodYesterdayKpi: CEOOverviewYesterdayQuery.Ceo) {
        val intentFoodCEOYesterdayKpi = Intent(requireContext(), FoodKpiActivity::class.java)
        intentFoodCEOYesterdayKpi.putExtra("food_data", gsonCEOYesterdayKpi.toJson(ceoFoodYesterdayKpi))
        intentFoodCEOYesterdayKpi.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentFoodCEOYesterdayKpi)
    }

    private fun openCASHCEOYesterdayKpiDetail(ceoCashYesterdayKpi: CEOOverviewYesterdayQuery.Ceo) {
        val intentCashCEOYesterdayKpi = Intent(requireContext(), CashKpiActivity::class.java)
        intentCashCEOYesterdayKpi.putExtra("cash_data", gsonCEOYesterdayKpi.toJson(ceoCashYesterdayKpi))
        intentCashCEOYesterdayKpi.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentCashCEOYesterdayKpi)
    }


    private fun refreshYesterdayKpiToken() {
        refreshTokenViewModelCeoYesterday.getRefreshToken()

        refreshTokenViewModelCeoYesterday.refreshTokenResponseLiveData.observe(requireActivity()) {
            run {
                when (it.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        callCeoYesterdayLevelOneQuery()
                    }
                    Status.UNSUCCESSFUL -> {
                        CommonUtil.navigateToLogin(mainActivity)
                    }
                    Status.ERROR -> {
                    }
                    Status.OFFLINE -> {
                        setInternetErrorScreenVisibleStateForCeoYesterday()
                    }
                }
            }
        }
    }

    private fun callStoreAgainstSupervisor(
            title: String,
            action: String,
            superVisorNumberValue: String
    ) {
        val progressDialog = CustomProgressDialog(requireActivity())
        lifecycleScope.launchWhenResumed {
            progressDialog.showProgressDialog()
            val areaCode = dbHelperCEOYesterdayKpi.getAllSelectedAreaList(true)
            val stateCode = dbHelperCEOYesterdayKpi.getAllSelectedStoreListState(true)
            var superVisorNumberList = dbHelperCEOYesterdayKpi.getAllSelectedStoreListSupervisor(true)
            val storeListValue = dbHelperCEOYesterdayKpi.getAllSelectedStoreList(true)
            val superVisorNumberListTemp = mutableListOf<String>()
            if (superVisorNumberValue.isNotEmpty() || superVisorNumberValue.isNotBlank()) {
                superVisorNumberListTemp.add(superVisorNumberValue)
            }

            if (dbHelperCEOYesterdayKpi.getAllSelectedStoreListSupervisor(true)
                        .isEmpty()
            ) {
                superVisorNumberList = superVisorNumberListTemp
            }
            Logger.info(
                    CEOYesterdayLvlThreeQuery.OPERATION_NAME.name(),
                    "Yesterday Level 3 KPI", mapQueryFilters(
                    areaCode,
                    stateCode,
                    superVisorNumberList,
                    storeListValue,
                    CEOYesterdayLvlThreeQuery.QUERY_DOCUMENT
            )
            )
            try {
                val response =
                        apolloClient(requireContext()).query(
                                CEOYesterdayLvlThreeQuery(
                                        areaCode.toInput(),
                                        stateCode.toInput(),
                                        superVisorNumberList.toInput(),
                                        storeListValue.toInput()
                                )
                        )
                                .await()

                if (response.data?.ceo != null) {
                    progressDialog.dismissProgressDialog()
                    individualStoreDetails = response.data?.ceo!!

                    response.data?.ceo?.kpis?.individualStores.let {
                        setDataForIndividualStore(
                                action,
                                title
                        )
                    }
                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialog.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Level 3 KPI")
                        }
                refreshYesterdayKpiToken()
                return@launchWhenResumed
            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialog.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForCeoYesterday()
                }

            } catch (exception: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(exception.message.toString(), "Yesterday Level 3 KPI")
                setErrorScreenVisibleStateForCeoYesterday(
                        getString(R.string.exception_error_text_title),
                        getString(R.string.exception_error_text_description)
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
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            null,
                                            null,
                                            null
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            null,
                                            null,
                                            null
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            null,
                                            null,
                                            null
                                    ),
                                    item?.yesterday?.service?.status.toString()
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
        expandableListDetailCEOYesterdayKpi[title] = childData
        this.expandableListAdapterCEOYesterdayKpi!! setChild (expandableListDetailCEOYesterdayKpi)


    }

    private fun callMissingDataQueryForCeoYesterday() {

        val progressDialogCEOYesterdayKpi = CustomProgressDialog(requireActivity())
        progressDialogCEOYesterdayKpi.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodeCEOYesterdayKpi = dbHelperCEOYesterdayKpi.getAllSelectedAreaList(true)
            val stateCodeCEOYesterdayKpi = dbHelperCEOYesterdayKpi.getAllSelectedStoreListState(true)
            val supervisorNumberCEOYesterdayKpi = dbHelperCEOYesterdayKpi.getAllSelectedStoreListSupervisor(true)
            val storeNumberCEOYesterdayKpi = dbHelperCEOYesterdayKpi.getAllSelectedStoreList(true)

            Logger.info(
                    MissingDataQuery.OPERATION_NAME.name(), "Yesterday Missing Data",
                    mapQueryFilters(
                            areaCodeCEOYesterdayKpi,
                            stateCodeCEOYesterdayKpi,
                            supervisorNumberCEOYesterdayKpi,
                            storeNumberCEOYesterdayKpi,
                            MissingDataQuery.QUERY_DOCUMENT
                    )
            )

            try {
                val responseMissingDataCEOYesterdayMissingDataKpi =
                        apolloClient(requireContext()).query(
                                MissingDataQuery(
                                        areaCodeCEOYesterdayKpi.toInput(),
                                        stateCodeCEOYesterdayKpi.toInput(),
                                        supervisorNumberCEOYesterdayKpi.toInput(),
                                        storeNumberCEOYesterdayKpi.toInput()
                                )
                        )
                                .await()


                if (responseMissingDataCEOYesterdayMissingDataKpi.data?.missingData != null) {
                    progressDialogCEOYesterdayKpi.dismissProgressDialog()
                    val headerMessage = responseMissingDataCEOYesterdayMissingDataKpi.data?.missingData!!.header.toString()
                    val message = responseMissingDataCEOYesterdayMissingDataKpi.data?.missingData!!.message.toString()
                    setMissingDataViewVisibleStateForCeoYesterday(headerMessage, message)
                } else {
                    progressDialogCEOYesterdayKpi.dismissProgressDialog()
                    ceo_yesterday_missing_data_error_layout.visibility = View.GONE
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogCEOYesterdayKpi.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Missing Data")
                        }

            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogCEOYesterdayKpi.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForCeoYesterday()
                }
            } catch (e: ApolloException) {
                progressDialogCEOYesterdayKpi.dismissProgressDialog()
                Logger.error(e.message.toString(), "Yesterday Missing Data")
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callCeoYesterdayLevelOneQuery() {
        val progressDialogCEOYesterdayKpi = CustomProgressDialog(requireActivity())
        progressDialogCEOYesterdayKpi.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            if (networkHelper.isNetworkConnected()) {
                hideErrorScreenVisibleStateForCEOYesterday()
            }

            val areaCodeCEOYesterdayKpi = dbHelperCEOYesterdayKpi.getAllSelectedAreaList(true)
            val stateCodeCEOYesterdayKpi = dbHelperCEOYesterdayKpi.getAllSelectedStoreListState(true)

            val supervisorNumberCEOYesterdayKpi = dbHelperCEOYesterdayKpi.getAllSelectedStoreListSupervisor(true)
            val storeNumberCEOYesterdayKpi = dbHelperCEOYesterdayKpi.getAllSelectedStoreList(true)


            Logger.info(
                    CEOYesterdayLevelOneQuery.OPERATION_NAME.name(),
                    "Yesterday Level 1 KPI",
                    mapQueryFilters(
                            areaCodeCEOYesterdayKpi,
                            stateCodeCEOYesterdayKpi,
                            supervisorNumberCEOYesterdayKpi,
                            storeNumberCEOYesterdayKpi,
                            CEOYesterdayLevelOneQuery.QUERY_DOCUMENT
                    )
            )
            try {

                val responseCEOYesterdayLevelOne = apolloClient(requireContext()).query(
                        CEOYesterdayLevelOneQuery(
                                areaCodeCEOYesterdayKpi.toInput(),
                                stateCodeCEOYesterdayKpi.toInput(),
                                supervisorNumberCEOYesterdayKpi.toInput(),
                                storeNumberCEOYesterdayKpi.toInput()
                        )
                )
                        .await()
                if (responseCEOYesterdayLevelOne.data?.ceo != null) {
                    progressDialogCEOYesterdayKpi.dismissProgressDialog()
                    ceoYesterdayLevelOne = responseCEOYesterdayLevelOne.data?.ceo!!

                    if (ceoYesterdayLevelOne.kpis?.supervisors?.stores?.yesterday != null) {
                        setCeoYesterdayLevelOneStoreValues(ceoYesterdayLevelOne.kpis?.supervisors?.stores?.yesterday!!)
                    } else {
                        setErrorScreenVisibleStateForCeoYesterday(
                                getString(R.string.error_text_title),
                                getString(R.string.error_text_description)
                        )
                    }

                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogCEOYesterdayKpi.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Level 1 KPI")
                        }
                refreshYesterdayKpiToken()
                return@launchWhenResumed
            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogCEOYesterdayKpi.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForCeoYesterday()
                }
            } catch (e: ApolloException) {
                progressDialogCEOYesterdayKpi.dismissProgressDialog()
                Logger.error(e.message.toString(), "Yesterday Level 1 KPI")
                setErrorScreenVisibleStateForCeoYesterday(
                        getString(R.string.exception_error_text_title),
                        getString(R.string.exception_error_text_description)
                )
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setCeoYesterdayLevelOneStoreValues(ceoYesterday: CEOYesterdayLevelOneQuery.Yesterday) {

        val strCEOYesterdaySelectedDate: String? =
                ceoYesterday.periodFrom?.let {
                    ceoYesterday.periodTo?.let {it1 ->
                        EnumMapperUtil.getSelectedDate(
                                it,
                                it1, EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                        )
                    }
                }

        if (strCEOYesterdaySelectedDate != null) {
            StorePrefData.filterDate = strCEOYesterdaySelectedDate
            setStoreFilterViewForCEOYesterday(StorePrefData.filterDate)
        }


        val ceoYesterdaySalesValue = Validation().checkAmountPercentageValue(
                requireActivity(),
                ceoYesterday.sales?.actual?.amount,
                ceoYesterday.sales?.actual?.percentage,
                ceoYesterday.sales?.actual?.value
        )
        if (ceoYesterdaySalesValue.isEmpty()) {
            hideVisibilityStateOfSalesDataForCEOYesterday()

        } else {
            showVisibilityStateOfSalesDataForCEOYesterday(ceoYesterdaySalesValue)
        }


        displaySalesViewForCEOYesterday(ceoYesterday.sales)
        displayFoodViewForCEOYesterday(ceoYesterday.food)
        displayLaborViewForCEOYesterday(ceoYesterday.labor)
        displayEADTServiceViewForCEOYesterday(ceoYesterday.service)
        displayExtremeServiceViewForCEOYesterday(ceoYesterday.service?.extremeDelivery)
        displaySinglesServiceViewForCEOYesterday(ceoYesterday.service?.singles)
        displayCashViewForCEOYesterday(ceoYesterday.cash)
        displayOERViewForCEOYesterday(ceoYesterday.oerStart)

    }

    fun setStoreFilterViewForCEOYesterday(date : String){
        val periodTextCEOYesterday = "$date | ${getString(R.string.yesterday_text)}"
        Validation().validateFilterKPI(
                requireActivity(),
                dbHelperCEOYesterdayKpi,
                common_header_ceo.store_header!!,
                periodTextCEOYesterday
        )

    }
    fun hideVisibilityStateOfSalesDataForCEOYesterday(){
        common_header_ceo.sales_header_error_image.visibility = View.VISIBLE
        common_header_ceo.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)
        common_header_ceo.sales_text_common_header.visibility = View.GONE
        common_header_ceo.total_sales_common_header.visibility = View.GONE
    }
    fun showVisibilityStateOfSalesDataForCEOYesterday(ceoYesterdaySalesValue: String) {
        common_header_ceo.total_sales_common_header.visibility = View.VISIBLE
        common_header_ceo.total_sales_common_header.text = ceoYesterdaySalesValue
        common_header_ceo.sales_text_common_header.visibility = View.VISIBLE
        common_header_ceo.sales_text_common_header.text = getString(R.string.sales_text)
        common_header_ceo.sales_header_error_image.visibility = View.GONE
    }

    fun displaySalesViewForCEOYesterday(sales: CEOYesterdayLevelOneQuery.Sales?) {
        if (sales?.displayName != null) {
            aws_display_yesterday_kpi.text = sales.displayName
        }
        val salesCeoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.goal?.amount,
                sales?.goal?.percentage,
                sales?.goal?.value
        )
        val salesCeoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.variance?.amount,
                sales?.variance?.percentage,
                sales?.variance?.value
        )
        val salesCeoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.actual?.amount,
                sales?.actual?.percentage,
                sales?.actual?.value
        )
        if (salesCeoGoalYesterday.isEmpty() && salesCeoVarianceYesterday.isEmpty() && salesCeoActualYesterday.isEmpty()) {

            sales_error_ceo_yesterday_kpi.visibility = View.VISIBLE

            val paramCEOYesterdayAWUSError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEOYesterdayAWUSError.weight = 2.0f
            aws_display_yesterday_kpi.layoutParams = paramCEOYesterdayAWUSError

            sales_goal_yesterday_kpi.visibility = View.GONE
            sales_variance_yesterday_kpi.visibility = View.GONE
            sales_actual_yesterday_kpi.visibility = View.GONE
            aws_parent_img_yesterday_kpi.visibility = View.GONE
            aws_parent_layout_yesterday_kpi.isClickable = false
        } else if (salesCeoGoalYesterday.isNotEmpty() && salesCeoVarianceYesterday.isNotEmpty() && salesCeoActualYesterday.isNotEmpty()) {

            sales_error_ceo_yesterday_kpi.visibility = View.GONE

            sales_goal_yesterday_kpi.visibility = View.VISIBLE
            sales_variance_yesterday_kpi.visibility = View.VISIBLE
            sales_actual_yesterday_kpi.visibility = View.VISIBLE

            sales_goal_yesterday_kpi.text = salesCeoGoalYesterday
            sales_variance_yesterday_kpi.text = salesCeoVarianceYesterday
            sales_actual_yesterday_kpi.text = salesCeoActualYesterday
        } else {

            sales_error_ceo_yesterday_kpi.visibility = View.GONE

            sales_goal_yesterday_kpi.visibility = View.VISIBLE
            sales_variance_yesterday_kpi.visibility = View.VISIBLE
            sales_actual_yesterday_kpi.visibility = View.VISIBLE

            if (salesCeoGoalYesterday.isEmpty()) {
                sales_goal_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_goal_yesterday_kpi.text = salesCeoGoalYesterday
            }

            if (salesCeoVarianceYesterday.isEmpty()) {
                sales_variance_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_variance_yesterday_kpi.text = salesCeoVarianceYesterday
            }

            if (salesCeoActualYesterday.isEmpty()) {
                sales_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_actual_yesterday_kpi.text = salesCeoActualYesterday
            }
        }


        if (sales?.status?.toString() != null && salesCeoActualYesterday.isNotEmpty()) {
            when {
                sales.status.toString() == resources.getString(R.string.out_of_range) -> {
                    sales_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    sales_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                sales.status.toString() == resources.getString(R.string.under_limit) -> {
                    sales_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    sales_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    sales_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    sales_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayFoodViewForCEOYesterday(food: CEOYesterdayLevelOneQuery.Food?) {
        if (food?.displayName != null) {
            food_display_yesterday_kpi.text = food.displayName
        }
        val foodCeoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.goal?.amount,
                food?.goal?.percentage,
                food?.goal?.value
        )
        val foodCeoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.variance?.amount,
                food?.variance?.percentage,
                food?.variance?.value
        )
        val foodCeoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.actual?.amount,
                food?.actual?.percentage,
                food?.actual?.value
        )

        if (foodCeoGoalYesterday.isEmpty() && foodCeoVarianceYesterday.isEmpty() && foodCeoActualYesterday.isEmpty()) {

            food_error_ceo_yesterday_kpi.visibility = View.VISIBLE
            val paramCEOYesterdayFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEOYesterdayFoodError.weight = 2.0f
            food_display_yesterday_kpi.layoutParams = paramCEOYesterdayFoodError

            food_goal_yesterday_kpi.visibility = View.GONE
            food_variance_yesterday_kpi.visibility = View.GONE
            food_actual_yesterday_kpi.visibility = View.GONE
            food_parent_img_yesterday_kpi.visibility = View.GONE
            food_parent_layout_yesterday_kpi.isClickable = false
        } else if (foodCeoGoalYesterday.isNotEmpty() && foodCeoVarianceYesterday.isNotEmpty() && foodCeoActualYesterday.isNotEmpty()) {

            food_error_ceo_yesterday_kpi.visibility = View.GONE

            food_goal_yesterday_kpi.visibility = View.VISIBLE
            food_variance_yesterday_kpi.visibility = View.VISIBLE
            food_actual_yesterday_kpi.visibility = View.VISIBLE

            food_goal_yesterday_kpi.text = foodCeoGoalYesterday
            food_variance_yesterday_kpi.text = foodCeoVarianceYesterday
            food_actual_yesterday_kpi.text = foodCeoActualYesterday
        } else {

            food_error_ceo_yesterday_kpi.visibility = View.GONE

            food_goal_yesterday_kpi.visibility = View.VISIBLE
            food_variance_yesterday_kpi.visibility = View.VISIBLE
            food_actual_yesterday_kpi.visibility = View.VISIBLE

            if (foodCeoGoalYesterday.isEmpty()) {
                food_goal_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_goal_yesterday_kpi.text = foodCeoGoalYesterday
            }

            if (foodCeoVarianceYesterday.isEmpty()) {
                food_variance_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_variance_yesterday_kpi.text = foodCeoVarianceYesterday
            }

            if (foodCeoActualYesterday.isEmpty()) {
                food_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_actual_yesterday_kpi.text = foodCeoActualYesterday
            }

        }

        if (food?.status?.toString() != null && foodCeoActualYesterday.isNotEmpty()) {
            when {
                food.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    food_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                food.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    food_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    food_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    food_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayLaborViewForCEOYesterday(labor: CEOYesterdayLevelOneQuery.Labor?) {

        if (labor?.displayName != null) {
            labour_display_yesterday_kpi.text = labor.displayName
        }

        val labourCeoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.goal?.amount,
                labor?.goal?.percentage,
                labor?.goal?.value
        )
        val labourCeoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.variance?.amount,
                labor?.variance?.percentage,
                labor?.variance?.value
        )
        val labourCeoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.actual?.amount,
                labor?.actual?.percentage,
                labor?.actual?.value
        )

        if (labourCeoGoalYesterday.isEmpty() && labourCeoVarianceYesterday.isEmpty() && labourCeoActualYesterday.isEmpty()) {

            labour_error_ceo_yesterday_kpi.visibility = View.VISIBLE
            val paramCEOYesterdayLabourError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEOYesterdayLabourError.weight = 2.0f
            labour_display_yesterday_kpi.layoutParams = paramCEOYesterdayLabourError

            labour_goal_yesterday_kpi.visibility = View.GONE
            labour_variance_yesterday_kpi.visibility = View.GONE
            labour_actual_yesterday_kpi.visibility = View.GONE
            labor_parent_img_yesterday_kpi.visibility = View.GONE
            labor_parent_layout_yesterday_kpi.isClickable = false
        } else if (labourCeoGoalYesterday.isNotEmpty() && labourCeoVarianceYesterday.isNotEmpty() && labourCeoActualYesterday.isNotEmpty()) {

            labour_error_ceo_yesterday_kpi.visibility = View.GONE

            labour_goal_yesterday_kpi.visibility = View.VISIBLE
            labour_variance_yesterday_kpi.visibility = View.VISIBLE
            labour_actual_yesterday_kpi.visibility = View.VISIBLE

            labour_goal_yesterday_kpi.text = labourCeoGoalYesterday
            labour_variance_yesterday_kpi.text = labourCeoVarianceYesterday
            labour_actual_yesterday_kpi.text = labourCeoActualYesterday
        } else {

            labour_error_ceo_yesterday_kpi.visibility = View.GONE

            labour_goal_yesterday_kpi.visibility = View.VISIBLE
            labour_variance_yesterday_kpi.visibility = View.VISIBLE
            labour_actual_yesterday_kpi.visibility = View.VISIBLE

            if (labourCeoGoalYesterday.isEmpty()) {
                labour_goal_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_goal_yesterday_kpi.text = labourCeoGoalYesterday
            }

            if (labourCeoVarianceYesterday.isEmpty()) {
                labour_variance_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_variance_yesterday_kpi.text = labourCeoVarianceYesterday
            }

            if (labourCeoActualYesterday.isEmpty()) {
                labour_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_actual_yesterday_kpi.text = labourCeoActualYesterday
            }

        }


        if (labor?.status != null && labourCeoActualYesterday.isNotEmpty()) {
            when {
                labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    labour_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                labor.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    labour_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    labour_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    labour_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayEADTServiceViewForCEOYesterday(service: CEOYesterdayLevelOneQuery.Service?) {
        if (service?.displayName != null) {
            service_display_yesterday_kpi.text = service.displayName
        }
        if (service?.eADT?.displayName != null) {
            eadt_display_yesterday_kpi.text = service.eADT.displayName
        }

        val serviceCeoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.goal?.amount,
                service?.eADT?.goal?.percentage,
                service?.eADT?.goal?.value
        )
        val serviceCeoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.variance?.amount,
                service?.eADT?.variance?.percentage,
                service?.eADT?.variance?.value
        )
        val serviceCeoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.actual?.amount,
                service?.eADT?.actual?.percentage,
                service?.eADT?.actual?.value
        )

        if (serviceCeoGoalYesterday.isEmpty() && serviceCeoVarianceYesterday.isEmpty() && serviceCeoActualYesterday.isEmpty()) {

            service_error_ceo_yesterday_kpi.visibility = View.VISIBLE
            val paramCEOYesterdayServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEOYesterdayServiceError.weight = 2.0f
            eadt_display_yesterday_kpi.layoutParams = paramCEOYesterdayServiceError


            service_eadt_goal_yesterday_kpi.visibility = View.GONE
            service_eadt_variance_yesterday_kpi.visibility = View.GONE
            service_eadt_actual_yesterday_kpi.visibility = View.GONE
        } else if (serviceCeoGoalYesterday.isNotEmpty() && serviceCeoVarianceYesterday.isNotEmpty() && serviceCeoActualYesterday.isNotEmpty()) {

            service_error_ceo_yesterday_kpi.visibility = View.GONE

            service_eadt_goal_yesterday_kpi.visibility = View.VISIBLE
            service_eadt_variance_yesterday_kpi.visibility = View.VISIBLE
            service_eadt_actual_yesterday_kpi.visibility = View.VISIBLE

            service_eadt_goal_yesterday_kpi.text = serviceCeoGoalYesterday
            service_eadt_variance_yesterday_kpi.text = serviceCeoVarianceYesterday
            service_eadt_actual_yesterday_kpi.text = serviceCeoActualYesterday
        } else {

            service_error_ceo_yesterday_kpi.visibility = View.GONE

            service_eadt_goal_yesterday_kpi.visibility = View.VISIBLE
            service_eadt_variance_yesterday_kpi.visibility = View.VISIBLE
            service_eadt_actual_yesterday_kpi.visibility = View.VISIBLE

            if (serviceCeoGoalYesterday.isEmpty()) {
                service_eadt_goal_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_goal_yesterday_kpi.text = serviceCeoGoalYesterday
            }

            if (serviceCeoVarianceYesterday.isEmpty()) {
                service_eadt_variance_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_variance_yesterday_kpi.text = serviceCeoVarianceYesterday
            }

            if (serviceCeoActualYesterday.isEmpty()) {
                service_eadt_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_actual_yesterday_kpi.text = serviceCeoActualYesterday
            }

        }

        if (service?.eADT?.status != null && serviceCeoActualYesterday.isNotEmpty()) {
            when {
                service.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_eadt_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_eadt_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                service.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_eadt_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_eadt_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_eadt_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_eadt_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayExtremeServiceViewForCEOYesterday(extremeDelivery: CEOYesterdayLevelOneQuery.ExtremeDelivery?) {
        if (extremeDelivery?.displayName != null) {
            extreme_delivery_display_yesterday_kpi.text = extremeDelivery.displayName
        }

        val serviceExtremeCeoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.goal?.amount,
                extremeDelivery?.goal?.percentage,
                extremeDelivery?.goal?.value
        )
        val serviceExtremeCeoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.variance?.amount,
                extremeDelivery?.variance?.percentage,
                extremeDelivery?.variance?.value
        )
        val serviceExtremeCeoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.actual?.amount,
                extremeDelivery?.actual?.percentage,
                extremeDelivery?.actual?.value
        )

        if (serviceExtremeCeoGoalYesterday.isEmpty() && serviceExtremeCeoVarianceYesterday.isEmpty() && serviceExtremeCeoActualYesterday.isEmpty()) {

            serviceExtreme_error_ceo_yesterday_kpi.visibility = View.VISIBLE
            val paramCEOYesterdayServiceExtremeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEOYesterdayServiceExtremeError.weight = 2.0f
            extreme_delivery_display_yesterday_kpi.layoutParams = paramCEOYesterdayServiceExtremeError

            service_extreme_goal_yesterday_kpi.visibility = View.GONE
            service_extreme_variance_yesterday_kpi.visibility = View.GONE
            service_extreme_actual_yesterday_kpi.visibility = View.GONE
        } else if (serviceExtremeCeoGoalYesterday.isNotEmpty() && serviceExtremeCeoVarianceYesterday.isNotEmpty() && serviceExtremeCeoActualYesterday.isNotEmpty()) {

            serviceExtreme_error_ceo_yesterday_kpi.visibility = View.GONE

            service_extreme_goal_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_variance_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_actual_yesterday_kpi.visibility = View.VISIBLE

            service_extreme_goal_yesterday_kpi.text = serviceExtremeCeoGoalYesterday
            service_extreme_variance_yesterday_kpi.text = serviceExtremeCeoVarianceYesterday
            service_extreme_actual_yesterday_kpi.text = serviceExtremeCeoActualYesterday
        } else {

            serviceExtreme_error_ceo_yesterday_kpi.visibility = View.GONE
            service_extreme_goal_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_variance_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_actual_yesterday_kpi.visibility = View.VISIBLE

            if (serviceExtremeCeoGoalYesterday.isEmpty()) {
                service_extreme_goal_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_goal_yesterday_kpi.text = serviceExtremeCeoGoalYesterday
            }

            if (serviceExtremeCeoVarianceYesterday.isEmpty()) {
                service_extreme_variance_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_variance_yesterday_kpi.text = serviceExtremeCeoVarianceYesterday
            }

            if (serviceExtremeCeoActualYesterday.isEmpty()) {
                service_extreme_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_actual_yesterday_kpi.text = serviceExtremeCeoActualYesterday
            }

        }


        if (extremeDelivery?.status != null && serviceExtremeCeoActualYesterday.isNotEmpty()) {
            when {
                extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_extreme_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_extreme_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_extreme_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    service_extreme_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_extreme_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }
    }

    fun displaySinglesServiceViewForCEOYesterday(singles: CEOYesterdayLevelOneQuery.Singles?) {
        if (singles?.displayName != null) {
            single_display_yesterday_kpi.text = singles.displayName
        }

        val serviceSinglesCeoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.goal?.amount,
                singles?.goal?.percentage,
                singles?.goal?.value
        )
        val serviceSinglesCeoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.variance?.amount,
                singles?.variance?.percentage,
                singles?.variance?.value
        )
        val serviceSinglesCeoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.actual?.amount,
                singles?.actual?.percentage,
                singles?.actual?.value
        )

        if (serviceSinglesCeoGoalYesterday.isEmpty() && serviceSinglesCeoVarianceYesterday.isEmpty() && serviceSinglesCeoActualYesterday.isEmpty()) {

            serviceSingles_error_ceo_yesterday_kpi.visibility = View.VISIBLE
            val paramCEOYesterdayServiceSingleError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEOYesterdayServiceSingleError.weight = 2.0f
            single_display_yesterday_kpi.layoutParams = paramCEOYesterdayServiceSingleError

            service_singles_goal_yesterday_kpi.visibility = View.GONE
            service_singles_variance_yesterday_kpi.visibility = View.GONE
            service_singles_actual_yesterday_kpi.visibility = View.GONE
        } else if (serviceSinglesCeoGoalYesterday.isNotEmpty() && serviceSinglesCeoVarianceYesterday.isNotEmpty() && serviceSinglesCeoActualYesterday.isNotEmpty()) {

            serviceSingles_error_ceo_yesterday_kpi.visibility = View.GONE

            service_singles_goal_yesterday_kpi.visibility = View.VISIBLE
            service_singles_variance_yesterday_kpi.visibility = View.VISIBLE
            service_singles_actual_yesterday_kpi.visibility = View.VISIBLE

            service_singles_goal_yesterday_kpi.text = serviceSinglesCeoGoalYesterday
            service_singles_variance_yesterday_kpi.text = serviceSinglesCeoVarianceYesterday
            service_singles_actual_yesterday_kpi.text = serviceSinglesCeoActualYesterday
        } else {

            serviceSingles_error_ceo_yesterday_kpi.visibility = View.GONE
            service_singles_goal_yesterday_kpi.visibility = View.VISIBLE
            service_singles_variance_yesterday_kpi.visibility = View.VISIBLE
            service_singles_actual_yesterday_kpi.visibility = View.VISIBLE

            if (serviceSinglesCeoGoalYesterday.isEmpty()) {
                service_singles_goal_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_goal_yesterday_kpi.text = serviceSinglesCeoGoalYesterday
            }

            if (serviceSinglesCeoVarianceYesterday.isEmpty()) {
                service_singles_variance_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_variance_yesterday_kpi.text = serviceSinglesCeoVarianceYesterday
            }

            if (serviceSinglesCeoActualYesterday.isEmpty()) {
                service_singles_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_actual_yesterday_kpi.text = serviceSinglesCeoActualYesterday
            }

        }


        if (singles?.status != null && serviceSinglesCeoActualYesterday.isNotEmpty()) {
            when {
                singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_singles_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_singles_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_singles_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_singles_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_singles_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayCashViewForCEOYesterday(cash: CEOYesterdayLevelOneQuery.Cash?) {

        if (cash?.displayName != null) {
            cash_display_yesterday_kpi.text = cash.displayName
        }
        val cashCeoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.goal?.amount,
                cash?.goal?.percentage,
                cash?.goal?.value
        )
        val cashCeoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.variance?.amount,
                cash?.variance?.percentage,
                cash?.variance?.value
        )
        val cashCeoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.actual?.amount,
                cash?.actual?.percentage,
                cash?.actual?.value
        )

        if (cashCeoGoalYesterday.isEmpty() && cashCeoVarianceYesterday.isEmpty() && cashCeoActualYesterday.isEmpty()) {

            cash_error_ceo_yesterday_kpi.visibility = View.VISIBLE
            val paramCEOYesterdayCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEOYesterdayCashError.weight = 2.0f
            cash_display_yesterday_kpi.layoutParams = paramCEOYesterdayCashError

            cash_goal_yesterday_kpi.visibility = View.GONE
            cash_variance_yesterday_kpi.visibility = View.GONE
            cash_actual_yesterday_kpi.visibility = View.GONE
            cash_parent_img_yesterday_kpi.visibility = View.GONE
            cash_parent_layout_yesterday_kpi.isClickable = false
        } else if (cashCeoGoalYesterday.isNotEmpty() && cashCeoVarianceYesterday.isNotEmpty() && cashCeoActualYesterday.isNotEmpty()) {

            cash_error_ceo_yesterday_kpi.visibility = View.GONE

            cash_goal_yesterday_kpi.visibility = View.VISIBLE
            cash_variance_yesterday_kpi.visibility = View.VISIBLE
            cash_actual_yesterday_kpi.visibility = View.VISIBLE

            cash_goal_yesterday_kpi.text = cashCeoGoalYesterday
            cash_variance_yesterday_kpi.text = cashCeoVarianceYesterday
            cash_actual_yesterday_kpi.text = cashCeoActualYesterday
        } else {

            cash_error_ceo_yesterday_kpi.visibility = View.GONE
            cash_goal_yesterday_kpi.visibility = View.VISIBLE
            cash_variance_yesterday_kpi.visibility = View.VISIBLE
            cash_actual_yesterday_kpi.visibility = View.VISIBLE

            if (cashCeoGoalYesterday.isEmpty()) {
                cash_goal_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_goal_yesterday_kpi.text = cashCeoGoalYesterday
            }

            if (cashCeoVarianceYesterday.isEmpty()) {
                cash_variance_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_variance_yesterday_kpi.text = cashCeoVarianceYesterday
            }

            if (cashCeoActualYesterday.isEmpty()) {
                cash_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_actual_yesterday_kpi.text = cashCeoActualYesterday
            }

        }

        if (cash?.status != null && cashCeoActualYesterday.isNotEmpty()) {
            when {
                cash.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    cash_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                cash.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    cash_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    cash_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    cash_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }

    }

    fun displayOERViewForCEOYesterday(oerStart: CEOYesterdayLevelOneQuery.OerStart?) {
        if (oerStart?.displayName != null) {
            oer_display_yesterday_kpi.text = oerStart.displayName
        }
        val oerCeoGoalYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.goal?.amount,
                oerStart?.goal?.percentage,
                oerStart?.goal?.value
        )
        val oerCeoVarianceYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.variance?.amount,
                oerStart?.variance?.percentage,
                oerStart?.variance?.value
        )
        val oerCeoActualYesterday = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.actual?.amount,
                oerStart?.actual?.percentage,
                oerStart?.actual?.value
        )

        if (oerCeoGoalYesterday.isEmpty() && oerCeoVarianceYesterday.isEmpty() && oerCeoActualYesterday.isEmpty()) {

            oer_error_ceo_yesterday_kpi.visibility = View.VISIBLE
            val paramCEOYesterdayOERError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEOYesterdayOERError.weight = 2.0f
            oer_display_yesterday_kpi.layoutParams = paramCEOYesterdayOERError

            oer_goal_yesterday_kpi.visibility = View.GONE
            oer_variance_yesterday_kpi.visibility = View.GONE
            oer_actual_yesterday_kpi.visibility = View.GONE
            oer_parent_img_yesterday_kpi.visibility = View.GONE
            oer_parent_layout_yesterday_kpi.isClickable = false
        } else if (oerCeoGoalYesterday.isNotEmpty() && oerCeoVarianceYesterday.isNotEmpty() && oerCeoActualYesterday.isNotEmpty()) {

            oer_error_ceo_yesterday_kpi.visibility = View.GONE

            oer_goal_yesterday_kpi.visibility = View.VISIBLE
            oer_variance_yesterday_kpi.visibility = View.VISIBLE
            oer_actual_yesterday_kpi.visibility = View.VISIBLE

            oer_goal_yesterday_kpi.text = oerCeoGoalYesterday
            oer_variance_yesterday_kpi.text = oerCeoVarianceYesterday
            oer_actual_yesterday_kpi.text = oerCeoActualYesterday
        } else {

            oer_error_ceo_yesterday_kpi.visibility = View.GONE
            oer_goal_yesterday_kpi.visibility = View.VISIBLE
            oer_variance_yesterday_kpi.visibility = View.VISIBLE
            oer_actual_yesterday_kpi.visibility = View.VISIBLE

            if (oerCeoGoalYesterday.isEmpty()) {
                oer_goal_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_goal_yesterday_kpi.text = oerCeoGoalYesterday
            }

            if (oerCeoVarianceYesterday.isEmpty()) {
                oer_variance_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_variance_yesterday_kpi.text = oerCeoVarianceYesterday
            }

            if (oerCeoActualYesterday.isEmpty()) {
                oer_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_actual_yesterday_kpi.text = oerCeoActualYesterday
            }

        }

        if (oerStart?.status != null && oerCeoActualYesterday.isNotEmpty()) {
            when {
                oerStart.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oer_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    oer_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                oerStart.status.toString() == resources.getString(R.string.under_limit) -> {
                    oer_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    oer_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    oer_actual_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    oer_actual_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }
    }


    private fun callOverViewCEOYesterdayKpiApi(
            actionOverViewCEOYesterday: String,
            superVisorNumberOverViewCEOYesterday: String,
            storeNumberOverViewCEOYesterday: String
    ) {
        lifecycleScope.launchWhenResumed {
            val superVisorNumberListOverViewCEOYesterdayKpi = mutableListOf<String>()
            if (superVisorNumberOverViewCEOYesterday.isNotEmpty() || superVisorNumberOverViewCEOYesterday.isNotBlank()) {
                superVisorNumberListOverViewCEOYesterdayKpi.add(superVisorNumberOverViewCEOYesterday)
            }
            val storeNumberListOverViewCEOYesterdayKpi = mutableListOf<String>()
            if (storeNumberOverViewCEOYesterday.isNotEmpty() || storeNumberOverViewCEOYesterday.isNotBlank()) {
                storeNumberListOverViewCEOYesterdayKpi.add(storeNumberOverViewCEOYesterday)
            } else {
                storeNumberListOverViewCEOYesterdayKpi.addAll(dbHelperCEOYesterdayKpi.getAllSelectedStoreList(true))
            }
            val progressDialogOverViewCEOYesterdayKpi = CustomProgressDialog(requireActivity())
            progressDialogOverViewCEOYesterdayKpi.showProgressDialog()
            try {
                val response =
                        apolloClient(requireContext()).query(
                                CEOOverviewYesterdayQuery(
                                        superVisorNumberListOverViewCEOYesterdayKpi.toInput(),
                                        storeNumberListOverViewCEOYesterdayKpi.toInput()
                                )
                        )
                                .await()

                if (response.data?.ceo != null) {
                    progressDialogOverViewCEOYesterdayKpi.dismissProgressDialog()
                    when (actionOverViewCEOYesterday) {
                        getString(R.string.awus_text) -> {
                            openSalesCEOYesterdayKpiDetail(response.data?.ceo!!)
                        }
                        getString(R.string.food_text) -> {
                            openFoodCEOYesterdayKpiDetail(response.data?.ceo!!)
                        }
                        getString(R.string.labour_text) -> {
                            openLabourCEOYesterdayKpiDetail(response.data?.ceo!!)
                        }
                        getString(R.string.service_text) -> {
                            openServiceCEOYesterdayKpiDetail(response.data?.ceo!!)
                        }
                        getString(R.string.oer_text) -> {
                            openOERCEOYesterdayKpiDetail(response.data?.ceo!!)
                        }
                        getString(R.string.cash_text) -> {
                            openCASHCEOYesterdayKpiDetail(response.data?.ceo!!)
                        }
                    }
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogOverViewCEOYesterdayKpi.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Overview KPI")
                        }
                refreshYesterdayKpiToken()
                return@launchWhenResumed
            } catch (e: ApolloNetworkException) {
                progressDialogOverViewCEOYesterdayKpi.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForCeoYesterday()
                }
            } catch (e: ApolloException) {
                progressDialogOverViewCEOYesterdayKpi.dismissProgressDialog()
                Logger.error(e.message.toString(), "Yesterday Overview KPI")
                setErrorScreenVisibleStateForCeoYesterday(
                        getString(R.string.exception_error_text_title),
                        getString(R.string.exception_error_text_description)
                )

            }

        }
    }

    fun setInternetErrorScreenVisibleStateForCeoYesterday() {
        ceo_yesterday_no_internet_error_layout.visibility = View.VISIBLE
        common_header_ceo.sales_header_error_image.visibility = View.VISIBLE
        common_header_ceo.sales_header_error_image.setImageResource(R.drawable.ic_internet_error)

        setHeaderViewsVisibleStateForCeoYesterday()
        setCalendarViewVisibleStateForCeoYesterday()
        showStoreFilterVisibilityStateForCEOYesterday()
        ceo_yesterday_missing_data_error_layout.visibility = View.GONE
        common_header_ceo.error_filter_parent_linear.visibility = View.GONE
        ceo_yesterday_data_error_layout.visibility = View.GONE
    }

    fun setErrorScreenVisibleStateForCeoYesterday(
            title: String,
            description: String
    ) {
        ceo_yesterday_data_error_layout.visibility = View.VISIBLE
        ceo_yesterday_data_error_layout.exception_text_title.text = title
        ceo_yesterday_data_error_layout.exception_text_description.text = description
        common_header_ceo.sales_header_error_image.visibility = View.VISIBLE
        common_header_ceo.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)
        common_header_ceo.error_filter_parent_linear.visibility = View.VISIBLE
        ceo_yesterday_missing_data_error_layout.visibility = View.GONE
        setHeaderViewsVisibleStateForCeoYesterday()
        setCalendarViewVisibleStateForCeoYesterday()
        hideStoreFilterVisibilityStateForCEOYesterday()
    }


    fun setHeaderViewsVisibleStateForCeoYesterday() {
        ceo_yesterday_header.visibility = View.GONE
        ceo_yesterday_v1.visibility = View.GONE
        ceo_yesterday_layout.visibility = View.INVISIBLE
        common_header_ceo.total_sales_common_header.visibility = View.GONE
        common_header_ceo.sales_text_common_header.visibility = View.GONE
    }

    fun hideStoreFilterVisibilityStateForCEOYesterday(){
        common_header_ceo.filter_parent_linear.visibility = View.GONE
    }
    fun showStoreFilterVisibilityStateForCEOYesterday(){
        common_header_ceo.filter_parent_linear.visibility = View.VISIBLE
    }

    fun setCalendarViewVisibleStateForCeoYesterday() {
        common_calendar.visibility = View.GONE
    }

    fun setMissingDataViewVisibleStateForCeoYesterday(
            missingDataTitle: String,
            missingDataDescription: String
    ) {
        ceo_yesterday_missing_data_error_layout.visibility = View.VISIBLE
        ceo_yesterday_missing_data_error_layout.header_data_title.text = missingDataTitle
        ceo_yesterday_missing_data_error_layout.header_data_description.text = missingDataDescription
    }

    fun hideErrorScreenVisibleStateForCEOYesterday(){
        ceo_yesterday_no_internet_error_layout.visibility = View.GONE
        ceo_yesterday_data_error_layout.visibility = View.GONE
        common_header_ceo.sales_header_error_image.visibility = View.GONE
        common_header_ceo.error_filter_parent_linear.visibility = View.GONE

        ceo_yesterday_header.visibility = View.VISIBLE
        ceo_yesterday_v1.visibility = View.VISIBLE
        ceo_yesterday_layout.visibility = View.VISIBLE
        common_header_ceo.filter_parent_linear.visibility = View.VISIBLE
        common_header_ceo.total_sales_common_header.visibility = View.VISIBLE
        common_header_ceo.sales_text_common_header.visibility = View.VISIBLE

        common_calendar.visibility = View.VISIBLE
    }

    fun collapseExpendedListVisibilityForCEOYesterday(){
        if(rcv_sales_yesterday_kpi.visibility == View.VISIBLE){
            rcv_sales_yesterday_kpi.visibility = View.GONE
            aws_text_overview_yesterday_kpi.visibility = View.GONE
            aws_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_food_yesterday_kpi.visibility == View.VISIBLE) {
            rcv_food_yesterday_kpi.visibility = View.GONE
            ideal_vs_food_variance_text_overview_yesterday_kpi.visibility = View.GONE
            food_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_labour_yesterday_kpi.visibility == View.VISIBLE) {
            rcv_labour_yesterday_kpi.visibility = View.GONE
            labour_text_overview_yesterday_kpi.visibility = View.GONE
            labor_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_service_yesterday_kpi.visibility == View.VISIBLE) {
            rcv_service_yesterday_kpi.visibility = View.GONE
            service_text_overview_yesterday_kpi.visibility = View.GONE
            service_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_cash_yesterday_kpi.visibility == View.VISIBLE) {
            rcv_cash_yesterday_kpi.visibility = View.GONE
            cash_text_overview_yesterday_kpi.visibility = View.GONE
            cash_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_oer_yesterday_kpi.visibility == View.VISIBLE) {
            rcv_oer_yesterday_kpi.visibility = View.GONE
            oer_text_overview_yesterday_kpi.visibility = View.GONE
            oer_parent_img_yesterday_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
    }

}