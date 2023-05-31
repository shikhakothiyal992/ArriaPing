package com.arria.ping.ui.kpi.supervisor.view

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.arria.ping.kpi.supervisor.SupervisorPeriodRangeLevelOneQuery
import com.arria.ping.kpi.supervisor.SupervisorPeriodRangeLevelTwoQuery
import com.arria.ping.log.Logger
import com.arria.ping.log.data.QueryData
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.model.StoreDetailPojo
import com.arria.ping.ui.filter.FilterActivity
import com.arria.ping.ui.generalview.MainActivity
import com.arria.ping.ui.kpi.overview.*
import com.arria.ping.ui.kpi.supervisor.adapter.SupervisorYesterdayAndPeriodAdapter
import com.arria.ping.ui.refreshtoken.model.Status
import com.arria.ping.ui.refreshtoken.viewmodel.RefreshTokenViewModel
import com.arria.ping.util.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_filter.*
import kotlinx.android.synthetic.main.activity_store_filter.*
import kotlinx.android.synthetic.main.ceo_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.ceo_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.common_calendar.view.*
import kotlinx.android.synthetic.main.common_header_ceo.*
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.data_error_layout.view.*
import kotlinx.android.synthetic.main.do_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.gm_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.individual_supervisor_item.view.*
import kotlinx.android.synthetic.main.supervisor_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.supervisor_yesterday_fragment_kpi.*
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class SupervisorPeriodRangeKpiFragment : Fragment(), View.OnClickListener {

    private var supervisorPeriodRangeAdapter: SupervisorYesterdayAndPeriodAdapter? = null
    private lateinit var dbHelperSupervisorPeriodRange: DatabaseHelperImpl
    val gson = Gson()
    var superVisorPeriodRangeFormattedStartDateValue = ""
    var superVisorPeriodRangeFormattedEndDateValue = ""

    lateinit var supervisorPeriodRangeLevelOne: SupervisorPeriodRangeLevelOneQuery.Supervisor
    lateinit var supervisorPeriodRangeLevelTwo: SupervisorPeriodRangeLevelTwoQuery.Supervisor


    private val refreshTokenViewModelSupervisorPeriodRange by viewModels<RefreshTokenViewModel>()

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
        return inflater.inflate(R.layout.supervisor_period_range_fragment_kpi, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        dbHelperSupervisorPeriodRange =
                DatabaseHelperImpl(DatabaseBuilder.getInstance(requireActivity()))

        initSupervisorPeriodRange()
        superVisorPeriodRangeFormattedStartDateValue = StorePrefData.startDateValue
        superVisorPeriodRangeFormattedEndDateValue = StorePrefData.endDateValue

        if (StorePrefData.filterDate.isNotEmpty()) {
            setStoreFilterViewForSupervisorPeriod(StorePrefData.filterDate)
        }

        if (networkHelper.isNetworkConnected()) {
            callMissingDataForSupervisorPeriodRange()
            callSupervisorPeriodRangeLevelOneQuery()
        } else {
            setInternetErrorScreenVisibleStateForSupervisorPeriod()
        }

        supervisor_period_swipe_refresh_layout.setOnRefreshListener{
            Logger.info("Pull down to refresh Supervisor Period Store Data", "Period KPI")
            callMissingDataForSupervisorPeriodRange()
            callSupervisorPeriodRangeLevelOneQuery()
            collapseExpendedListVisibilityForSupervisorYesterday()
            supervisor_period_swipe_refresh_layout.isRefreshing = false
        }
    }

    private fun initSupervisorPeriodRange() {
        aws_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
        food_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
        labor_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
        service_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
        oer_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
        cash_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)

        aws_parent_layout_supervisor_period_range.setOnClickListener(this)
        food_parent_layout_supervisor_period_range.setOnClickListener(this)
        service_parent_layout_supervisor_period_range.setOnClickListener(this)
        oer_parent_layout_supervisor_period_range.setOnClickListener(this)
        cash_parent_layout_supervisor_period_range.setOnClickListener(this)
        labour_parent_layout_supervisor_period_range.setOnClickListener(this)

        filter_icon.setOnClickListener(this)
        filter_parent_linear.setOnClickListener(this)
        error_filter_parent_linear.setOnClickListener(this)

        aws_text_overview_supervisor_period_range.setOnClickListener(this)
        food_text_overview_supervisor_period_range.setOnClickListener(this)
        labour_text_overview_supervisor_period_range.setOnClickListener(this)
        service_text_overview_supervisor_period_range.setOnClickListener(this)
        oer_text_overview_supervisor_period_range.setOnClickListener(this)
        cash_text_overview_supervisor_period_range.setOnClickListener(this)

        Validation().setCustomCalendar(common_calendar_supervisor_range.square_day)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun callMissingDataForSupervisorPeriodRange() {
        val progressDialogSupervisorPeriodRangeKpi = CustomProgressDialog(requireActivity())
        progressDialogSupervisorPeriodRangeKpi.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val supervisorNumberSupervisorPeriodRangeKpi = mutableListOf(StorePrefData.email)
            val areaCodeSupervisorPeriodRangeKpi =
                    dbHelperSupervisorPeriodRange.getAllSelectedAreaList(true)
            val stateCodeSupervisorPeriodRangeKpi =
                    dbHelperSupervisorPeriodRange.getAllSelectedStoreListState(true)
            val storeNumberSupervisorPeriodRangeKpi =
                    dbHelperSupervisorPeriodRange.getAllSelectedStoreList(true)

            Logger.info(
                    MissingDataQuery.OPERATION_NAME.name(), "Period Missing Data",
                    mapQueryFilters(
                            areaCodeSupervisorPeriodRangeKpi,
                            stateCodeSupervisorPeriodRangeKpi,
                            supervisorNumberSupervisorPeriodRangeKpi,
                            storeNumberSupervisorPeriodRangeKpi,
                            MissingDataQuery.QUERY_DOCUMENT
                    )
            )
            try {
                val responseMissingDataSupervisorPeriodRangeKpi =
                        apolloClient(requireContext()).query(
                                MissingDataQuery(
                                        areaCodeSupervisorPeriodRangeKpi.toInput(),
                                        stateCodeSupervisorPeriodRangeKpi.toInput(),
                                        supervisorNumberSupervisorPeriodRangeKpi.toInput(),
                                        storeNumberSupervisorPeriodRangeKpi.toInput(),
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                                .toInput(),
                                        superVisorPeriodRangeFormattedStartDateValue.toInput(),
                                        superVisorPeriodRangeFormattedEndDateValue.toInput(),

                                        )
                        )
                                .await()

                if (responseMissingDataSupervisorPeriodRangeKpi.data?.missingData != null) {
                    progressDialogSupervisorPeriodRangeKpi.dismissProgressDialog()
                    setMissingDataViewVisibleStateForSupervisorPeriod(
                            responseMissingDataSupervisorPeriodRangeKpi.data?.missingData!!.header.toString(),
                            responseMissingDataSupervisorPeriodRangeKpi.data?.missingData!!.message.toString()
                    )

                } else {
                    progressDialogSupervisorPeriodRangeKpi.dismissProgressDialog()
                    supervisor_period_range_missing_data_error_layout.visibility = View.GONE
                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogSupervisorPeriodRangeKpi.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Missing Data")
                        }

            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogSupervisorPeriodRangeKpi.dismissProgressDialog()

                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForSupervisorPeriod()
                }
            } catch (e: ApolloException) {
                progressDialogSupervisorPeriodRangeKpi.dismissProgressDialog()
                Logger.error(e.message.toString(), "Period Range Missing Data")
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callSupervisorPeriodRangeLevelOneQuery() {

        val progressDialogSupervisorPeriodRangeKpi = CustomProgressDialog(requireActivity())
        progressDialogSupervisorPeriodRangeKpi.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            if (networkHelper.isNetworkConnected()) {
                hideErrorScreenVisibleStateForSupervisorPeriod()
            }

            val supervisorNumberSupervisorPeriodRangeKpi : List<String> = dbHelperSupervisorPeriodRange.getAllSelectedStoreListSupervisor(true)
            val areaCodeSupervisorPeriodRangeKpi =
                    dbHelperSupervisorPeriodRange.getAllSelectedAreaList(true)
            val stateCodeSupervisorPeriodRangeKpi =
                    dbHelperSupervisorPeriodRange.getAllSelectedStoreListState(true)
            val storeNumberSupervisorPeriodRangeKpi =
                    dbHelperSupervisorPeriodRange.getAllSelectedStoreList(true)

            Logger.info(
                    SupervisorPeriodRangeLevelOneQuery.OPERATION_NAME.name(),
                    "Period Range Level 1",
                    mapQueryFilters(
                            QueryData(
                                    areaCodeSupervisorPeriodRangeKpi,
                                    stateCodeSupervisorPeriodRangeKpi,
                                    supervisorNumberSupervisorPeriodRangeKpi,
                                    storeNumberSupervisorPeriodRangeKpi,
                                    superVisorPeriodRangeFormattedEndDateValue,
                                    superVisorPeriodRangeFormattedStartDateValue,
                                    StorePrefData.filterType,
                                    SupervisorPeriodRangeLevelOneQuery.QUERY_DOCUMENT
                            )
                    )
            )


            val periodText: String?
            if (StorePrefData.isSelectedPeriod.isEmpty()) {
                periodText = DateFormatterUtil.previousDate() + " | " + getString(R.string.period_text)
            } else {
                if (StorePrefData.startDateValue.isEmpty()) {
                    periodText =
                            DateFormatterUtil.previousDate() + " | " + StorePrefData.isSelectedPeriod
                } else {
                    periodText =
                            StorePrefData.isSelectedDate + " | " + StorePrefData.isSelectedPeriod
                }

            }
            Validation().validateFilterKPI(
                    requireActivity(),
                    dbHelperSupervisorPeriodRange,
                    common_header_supervisor_range.store_header!!,
                    periodText
            )
            try {
                val responseSupervisorPeriodRangeKpi =
                        apolloClient(requireContext()).query(
                                SupervisorPeriodRangeLevelOneQuery(
                                        areaCodeSupervisorPeriodRangeKpi.toInput(),
                                        stateCodeSupervisorPeriodRangeKpi.toInput(),
                                        storeNumberSupervisorPeriodRangeKpi.toInput(),
                                        superVisorPeriodRangeFormattedStartDateValue,
                                        superVisorPeriodRangeFormattedEndDateValue,
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                )
                        )
                                .await()

                if (responseSupervisorPeriodRangeKpi.data?.supervisor != null) {
                    progressDialogSupervisorPeriodRangeKpi.dismissProgressDialog()
                    supervisorPeriodRangeLevelOne = responseSupervisorPeriodRangeKpi.data?.supervisor!!

                    if (responseSupervisorPeriodRangeKpi.data?.supervisor?.kpis?.stores?.period != null) {
                        setSupervisorPeriodRangeStoreData(responseSupervisorPeriodRangeKpi.data?.supervisor?.kpis?.stores?.period)
                    } else {
                        setErrorScreenVisibleStateForSupervisorPeriod(
                                getString(R.string.error_text_title),
                                getString(R.string.error_text_description)
                        )
                    }
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogSupervisorPeriodRangeKpi.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Level 1 KPI")
                        }
                refreshSupervisorPeriodRangeKpiToken()
                return@launchWhenResumed
            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogSupervisorPeriodRangeKpi.dismissProgressDialog()

                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForSupervisorPeriod()
                }
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Period Range Level 1")
                progressDialogSupervisorPeriodRangeKpi.dismissProgressDialog()
                setErrorScreenVisibleStateForSupervisorPeriod(
                        getString(R.string.exception_error_text_title), getString(
                        R.string
                                .exception_error_text_description
                )
                )
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setSupervisorPeriodRangeStoreData(
            supervisorPeriod: SupervisorPeriodRangeLevelOneQuery.Period?,
    ) {

        val strSupervisorRangeSelectedDate: String? =
                supervisorPeriod?.periodFrom?.let {
                    supervisorPeriod.periodTo?.let {it1 ->
                        EnumMapperUtil.getSelectedDate(
                                it,
                                it1, EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                        )
                    }
                }
        if (!strSupervisorRangeSelectedDate.isNullOrEmpty()) {
            StorePrefData.filterDate = strSupervisorRangeSelectedDate
            setStoreFilterViewForSupervisorPeriod(StorePrefData.filterDate)
        }

        val supervisorPeriodSalesValue = Validation()
                .checkAmountPercentageValue(
                        requireActivity(),
                        supervisorPeriod?.sales?.actual?.amount,
                        supervisorPeriod?.sales?.actual?.percentage,
                        supervisorPeriod?.sales?.actual?.value,
                )
        if (supervisorPeriodSalesValue.isEmpty()) {
            hideVisibilityStateOfSalesDataForSupervisorPeriod()
        } else {
            showVisibilityStateOfSalesDataForSupervisorPeriod(supervisorPeriodSalesValue)
        }

        displaySalesViewForSupervisorYesterday(supervisorPeriod?.sales)
        displayFoodViewForSupervisorYesterday(supervisorPeriod?.food)
        displayLaborViewForSupervisorYesterday(supervisorPeriod?.labor)
        displayEADTServiceViewForSupervisorYesterday(supervisorPeriod?.service)
        displayExtremeServiceViewForSupervisorYesterday(supervisorPeriod?.service?.extremeDelivery)
        displaySingleServiceViewForSupervisorYesterday(supervisorPeriod?.service?.singles)
        displayCashViewForSupervisorYesterday(supervisorPeriod?.cash)
        displayOERViewForSupervisorYesterday(supervisorPeriod?.oerStart)
    }

    fun setStoreFilterViewForSupervisorPeriod(date: String){
        val periodTextSupervisorRange = "$date | ${StorePrefData.isSelectedPeriod}"
        Validation().validateFilterKPI(
                requireActivity(),
                dbHelperSupervisorPeriodRange,
                common_header_supervisor_range.store_header!!,
                periodTextSupervisorRange
        )

    }

    fun hideVisibilityStateOfSalesDataForSupervisorPeriod(){
        common_header_supervisor_range.total_sales_common_header.visibility = View.GONE
        common_header_supervisor_range.sales_text_common_header.visibility = View.GONE
        common_header_supervisor_range.sales_header_error_image.visibility = View.VISIBLE
        common_header_supervisor_range.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)
    }
    fun showVisibilityStateOfSalesDataForSupervisorPeriod(supervisorPeriodSalesValue: String) {
        common_header_supervisor_range.total_sales_common_header.visibility = View.VISIBLE
        common_header_supervisor_range.total_sales_common_header.text = supervisorPeriodSalesValue
        common_header_supervisor_range.sales_text_common_header.visibility = View.VISIBLE
        common_header_supervisor_range.sales_text_common_header.text = getString(R.string.sales_text)
        common_header_supervisor_range.sales_header_error_image.visibility = View.GONE
    }

    fun displaySalesViewForSupervisorYesterday(sales: SupervisorPeriodRangeLevelOneQuery.Sales?) {
        if (sales?.displayName != null) {
            aws_display_supervisor_period_range.text = sales.displayName
        }
        val salesSupervisorGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.goal?.amount,
                sales?.goal?.percentage,
                sales?.goal?.value
        )
        val salesSupervisorVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.variance?.amount,
                sales?.variance?.percentage,
                sales?.variance?.value
        )
        val salesSupervisorActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.actual?.amount,
                sales?.actual?.percentage,
                sales?.actual?.value
        )

        if (salesSupervisorGoalRange.isEmpty() && salesSupervisorVarianceRange.isEmpty() && salesSupervisorActualRange.isEmpty()) {

            sales_error_supervisor_period_range_kpi.visibility = View.VISIBLE
            val paramSupervisorRangeAWUSError: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            paramSupervisorRangeAWUSError.weight = 2.0f
            aws_display_supervisor_period_range.layoutParams = paramSupervisorRangeAWUSError

            sales_goal_supervisor_period_range.visibility = View.GONE
            sales_variance_supervisor_period_range.visibility = View.GONE
            sales_actual_supervisor_period_range.visibility = View.GONE
            aws_parent_img_supervisor_period_range.visibility = View.GONE
            aws_parent_layout_supervisor_period_range.isClickable = false
        } else if (salesSupervisorGoalRange.isNotEmpty() && salesSupervisorVarianceRange.isNotEmpty() && salesSupervisorActualRange.isNotEmpty()) {

            sales_error_supervisor_period_range_kpi.visibility = View.GONE
            sales_goal_supervisor_period_range.visibility = View.VISIBLE
            sales_variance_supervisor_period_range.visibility = View.VISIBLE
            sales_actual_supervisor_period_range.visibility = View.VISIBLE

            sales_goal_supervisor_period_range.text = salesSupervisorGoalRange
            sales_variance_supervisor_period_range.text = salesSupervisorVarianceRange
            sales_actual_supervisor_period_range.text = salesSupervisorActualRange
        } else {

            sales_error_supervisor_period_range_kpi.visibility = View.GONE
            sales_goal_supervisor_period_range.visibility = View.VISIBLE
            sales_variance_supervisor_period_range.visibility = View.VISIBLE
            sales_actual_supervisor_period_range.visibility = View.VISIBLE

            if (salesSupervisorGoalRange.isEmpty()) {
                sales_goal_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_goal_supervisor_period_range.text = salesSupervisorGoalRange
            }

            if (salesSupervisorVarianceRange.isEmpty()) {
                sales_variance_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_variance_supervisor_period_range.text = salesSupervisorVarianceRange
            }

            if (salesSupervisorActualRange.isEmpty()) {
                sales_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_actual_supervisor_period_range.text = salesSupervisorActualRange
            }
        }


        if (sales?.status?.toString() != null && salesSupervisorActualRange.isNotEmpty()) {
            when {
                sales.status.toString() == resources.getString(R.string.out_of_range) -> {
                    sales_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    sales_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.red))

                }
                sales.status.toString() == resources.getString(R.string.under_limit) -> {
                    sales_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    sales_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    sales_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    sales_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayFoodViewForSupervisorYesterday(food: SupervisorPeriodRangeLevelOneQuery.Food?) {
        if (food?.displayName != null) {
            food_display_supervisor_period_range.text = food.displayName
        }

        val foodSupervisorGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.goal?.amount,
                food?.goal?.percentage,
                food?.goal?.value
        )
        val foodSupervisorVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.variance?.amount,
                food?.variance?.percentage,
                food?.variance?.value
        )
        val foodSupervisorActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.actual?.amount,
                food?.actual?.percentage,
                food?.actual?.value
        )

        if (foodSupervisorGoalRange.isEmpty() && foodSupervisorVarianceRange.isEmpty() && foodSupervisorActualRange.isEmpty()) {

            food_error_supervisor_period_range_kpi.visibility = View.VISIBLE
            val paramSupervisorRangeFoodError: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            paramSupervisorRangeFoodError.weight = 2.0f
            food_display_supervisor_period_range.layoutParams = paramSupervisorRangeFoodError

            food_goal_supervisor_period_range.visibility = View.GONE
            food_variance_supervisor_period_range.visibility = View.GONE
            food_actual_supervisor_period_range.visibility = View.GONE
            food_parent_img_supervisor_period_range.visibility = View.GONE
            food_parent_layout_supervisor_period_range.isClickable = false
        } else if (foodSupervisorGoalRange.isNotEmpty() && foodSupervisorVarianceRange.isNotEmpty() && foodSupervisorActualRange.isNotEmpty()) {

            food_error_supervisor_period_range_kpi.visibility = View.GONE
            food_goal_supervisor_period_range.visibility = View.VISIBLE
            food_variance_supervisor_period_range.visibility = View.VISIBLE
            food_actual_supervisor_period_range.visibility = View.VISIBLE

            food_goal_supervisor_period_range.text = foodSupervisorGoalRange
            food_variance_supervisor_period_range.text = foodSupervisorVarianceRange
            food_actual_supervisor_period_range.text = foodSupervisorActualRange
        } else {

            food_error_supervisor_period_range_kpi.visibility = View.GONE
            food_goal_supervisor_period_range.visibility = View.VISIBLE
            food_variance_supervisor_period_range.visibility = View.VISIBLE
            food_actual_supervisor_period_range.visibility = View.VISIBLE

            if (foodSupervisorGoalRange.isEmpty()) {
                food_goal_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_goal_supervisor_period_range.text = foodSupervisorGoalRange
            }

            if (foodSupervisorVarianceRange.isEmpty()) {
                food_variance_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_variance_supervisor_period_range.text = foodSupervisorVarianceRange
            }

            if (foodSupervisorActualRange.isEmpty()) {
                food_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_actual_supervisor_period_range.text = foodSupervisorActualRange
            }

        }


        if (food?.status?.toString() != null && foodSupervisorActualRange.isNotEmpty()) {
            when {
                food.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    food_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.red))
                }
                food.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    food_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    food_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    food_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayLaborViewForSupervisorYesterday(labor: SupervisorPeriodRangeLevelOneQuery.Labor?) {
        if (labor?.displayName != null) {
            labour_display_supervisor_period_range.text = labor.displayName
        }

        val labourSupervisorGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.goal?.amount,
                labor?.goal?.percentage,
                labor?.goal?.value
        )
        val labourSupervisorVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.variance?.amount,
                labor?.variance?.percentage,
                labor?.variance?.value
        )
        val labourSupervisorActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.actual?.amount,
                labor?.actual?.percentage,
                labor?.actual?.value
        )

        if (labourSupervisorGoalRange.isEmpty() && labourSupervisorVarianceRange.isEmpty() && labourSupervisorActualRange.isEmpty()) {

            labour_error_supervisor_period_range_kpi.visibility = View.VISIBLE
            val paramSupervisorRangeLabourError: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            paramSupervisorRangeLabourError.weight = 2.0f
            labour_display_supervisor_period_range.layoutParams = paramSupervisorRangeLabourError


            labour_goal_supervisor_period_range.visibility = View.GONE
            labour_variance_supervisor_period_range.visibility = View.GONE
            labour_actual_supervisor_period_range.visibility = View.GONE
            labor_parent_img_supervisor_period_range.visibility = View.GONE
            labour_parent_layout_supervisor_period_range.isClickable = false
        } else if (labourSupervisorGoalRange.isNotEmpty() && labourSupervisorVarianceRange.isNotEmpty() && labourSupervisorActualRange.isNotEmpty()) {

            labour_error_supervisor_period_range_kpi.visibility = View.GONE
            labour_goal_supervisor_period_range.visibility = View.VISIBLE
            labour_variance_supervisor_period_range.visibility = View.VISIBLE
            labour_actual_supervisor_period_range.visibility = View.VISIBLE

            labour_goal_supervisor_period_range.text = labourSupervisorGoalRange
            labour_variance_supervisor_period_range.text = labourSupervisorVarianceRange
            labour_actual_supervisor_period_range.text = labourSupervisorActualRange
        } else {

            labour_error_supervisor_period_range_kpi.visibility = View.GONE
            labour_goal_supervisor_period_range.visibility = View.VISIBLE
            labour_variance_supervisor_period_range.visibility = View.VISIBLE
            labour_actual_supervisor_period_range.visibility = View.VISIBLE

            if (labourSupervisorGoalRange.isEmpty()) {
                labour_goal_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_goal_supervisor_period_range.text = labourSupervisorGoalRange
            }

            if (labourSupervisorVarianceRange.isEmpty()) {
                labour_variance_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_variance_supervisor_period_range.text = labourSupervisorVarianceRange
            }

            if (labourSupervisorActualRange.isEmpty()) {
                labour_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_actual_supervisor_period_range.text = labourSupervisorActualRange
            }

        }

        if (labor?.status != null && labourSupervisorActualRange.isNotEmpty()) {
            when {
                labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    labour_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.red))

                }
                labor.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    labour_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    labour_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    labour_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayEADTServiceViewForSupervisorYesterday(service: SupervisorPeriodRangeLevelOneQuery.Service?) {
        if (service?.displayName != null) {
            service_display_supervisor_period_range.text = service.displayName
        }
        if (service?.eADT?.displayName != null) {
            eadt_display_supervisor_period_range.text = service.eADT.displayName
        }
        val serviceSupervisorGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.goal?.amount,
                service?.eADT?.goal?.percentage,
                service?.eADT?.goal?.value
        )
        val serviceSupervisorVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.variance?.amount,
                service?.eADT?.variance?.percentage,
                service?.eADT?.variance?.value
        )
        val serviceSupervisorActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.actual?.amount,
                service?.eADT?.actual?.percentage,
                service?.eADT?.actual?.value
        )

        if (serviceSupervisorGoalRange.isEmpty() && serviceSupervisorVarianceRange.isEmpty() && serviceSupervisorActualRange.isEmpty()) {

            service_error_supervisor_period_range_kpi.visibility = View.VISIBLE
            val paramSupervisorRangeEadtError: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            paramSupervisorRangeEadtError.weight = 2.0f
            eadt_display_supervisor_period_range.layoutParams = paramSupervisorRangeEadtError

            service_eadt_goal_supervisor_period_range.visibility = View.GONE
            service_eadt_variance_supervisor_period_range.visibility = View.GONE
            service_eadt_actual_supervisor_period_range.visibility = View.GONE
        } else if (serviceSupervisorGoalRange.isNotEmpty() && serviceSupervisorVarianceRange.isNotEmpty() && serviceSupervisorActualRange.isNotEmpty()) {

            service_error_supervisor_period_range_kpi.visibility = View.GONE
            service_eadt_goal_supervisor_period_range.visibility = View.VISIBLE
            service_eadt_variance_supervisor_period_range.visibility = View.VISIBLE
            service_eadt_actual_supervisor_period_range.visibility = View.VISIBLE

            service_eadt_goal_supervisor_period_range.text = serviceSupervisorGoalRange
            service_eadt_variance_supervisor_period_range.text = serviceSupervisorVarianceRange
            service_eadt_actual_supervisor_period_range.text = serviceSupervisorActualRange
        } else {

            service_error_supervisor_period_range_kpi.visibility = View.GONE
            service_eadt_goal_supervisor_period_range.visibility = View.VISIBLE
            service_eadt_variance_supervisor_period_range.visibility = View.VISIBLE
            service_eadt_actual_supervisor_period_range.visibility = View.VISIBLE

            if (serviceSupervisorGoalRange.isEmpty()) {
                service_eadt_goal_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_goal_supervisor_period_range.text = serviceSupervisorGoalRange
            }

            if (serviceSupervisorVarianceRange.isEmpty()) {
                service_eadt_variance_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_variance_supervisor_period_range.text =
                        serviceSupervisorVarianceRange
            }

            if (serviceSupervisorActualRange.isEmpty()) {
                service_eadt_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_actual_supervisor_period_range.text = serviceSupervisorActualRange
            }

        }

        if (service?.eADT?.status != null && serviceSupervisorActualRange.isNotEmpty()) {
            when {
                service.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_eadt_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_eadt_actual_supervisor_period_range.setTextColor(
                            requireContext().getColor(
                                    R.color.red
                            )
                    )

                }
                service.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_eadt_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_eadt_actual_supervisor_period_range.setTextColor(
                            requireContext().getColor(
                                    R.color.green
                            )
                    )

                }
                else -> {
                    service_eadt_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_eadt_actual_supervisor_period_range.setTextColor(
                            requireContext().getColor(
                                    R.color.text_color
                            )
                    )

                }
            }
        }
    }

    fun displayExtremeServiceViewForSupervisorYesterday(extremeDelivery: SupervisorPeriodRangeLevelOneQuery.ExtremeDelivery?) {
        if (extremeDelivery?.displayName != null) {
            extreme_delivery_display_supervisor_period_range.text = extremeDelivery.displayName
        }
        val serviceExtremeSupervisorGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.goal?.amount,
                extremeDelivery?.goal?.percentage,
                extremeDelivery?.goal?.value
        )
        val serviceExtremeSupervisorVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.variance?.amount,
                extremeDelivery?.variance?.percentage,
                extremeDelivery?.variance?.value
        )
        val serviceExtremeSupervisorActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.actual?.amount,
                extremeDelivery?.actual?.percentage,
                extremeDelivery?.actual?.value
        )

        if (serviceExtremeSupervisorGoalRange.isEmpty() && serviceExtremeSupervisorVarianceRange.isEmpty() && serviceExtremeSupervisorActualRange.isEmpty()) {

            serviceExtreme_error_supervisor_period_range_kpi.visibility = View.VISIBLE
            val paramSupervisorRangeExtremeError: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            paramSupervisorRangeExtremeError.weight = 2.0f
            extreme_delivery_display_supervisor_period_range.layoutParams =
                    paramSupervisorRangeExtremeError


            service_extreme_goal_supervisor_period_range.visibility = View.GONE
            service_extreme_variance_supervisor_period_range.visibility = View.GONE
            service_extreme_actual_supervisor_period_range.visibility = View.GONE
        } else if (serviceExtremeSupervisorGoalRange.isNotEmpty() && serviceExtremeSupervisorVarianceRange.isNotEmpty() && serviceExtremeSupervisorActualRange.isNotEmpty()) {

            serviceExtreme_error_supervisor_period_range_kpi.visibility = View.GONE
            service_extreme_goal_supervisor_period_range.visibility = View.VISIBLE
            service_extreme_variance_supervisor_period_range.visibility = View.VISIBLE
            service_extreme_actual_supervisor_period_range.visibility = View.VISIBLE

            service_extreme_goal_supervisor_period_range.text = serviceExtremeSupervisorGoalRange
            service_extreme_variance_supervisor_period_range.text =
                    serviceExtremeSupervisorVarianceRange
            service_extreme_actual_supervisor_period_range.text =
                    serviceExtremeSupervisorActualRange
        } else {

            serviceExtreme_error_supervisor_period_range_kpi.visibility = View.GONE
            service_extreme_goal_supervisor_period_range.visibility = View.VISIBLE
            service_extreme_variance_supervisor_period_range.visibility = View.VISIBLE
            service_extreme_actual_supervisor_period_range.visibility = View.VISIBLE

            if (serviceExtremeSupervisorGoalRange.isEmpty()) {
                service_extreme_goal_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_goal_supervisor_period_range.text =
                        serviceExtremeSupervisorGoalRange
            }

            if (serviceExtremeSupervisorVarianceRange.isEmpty()) {
                service_extreme_variance_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_variance_supervisor_period_range.text =
                        serviceExtremeSupervisorVarianceRange
            }

            if (serviceExtremeSupervisorActualRange.isEmpty()) {
                service_extreme_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_actual_supervisor_period_range.text =
                        serviceExtremeSupervisorActualRange
            }
        }

        if (extremeDelivery?.status != null && serviceExtremeSupervisorActualRange.isNotEmpty()) {
            when {
                extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_extreme_actual_supervisor_period_range.setTextColor(
                            requireContext().getColor(
                                    R.color.red
                            )
                    )
                }
                extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_extreme_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_extreme_actual_supervisor_period_range.setTextColor(
                            requireContext().getColor(
                                    R.color.green
                            )
                    )
                }
                else -> {
                    service_extreme_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_extreme_actual_supervisor_period_range.setTextColor(
                            requireContext().getColor(
                                    R.color.text_color
                            )
                    )
                }
            }
        }
    }

    fun displaySingleServiceViewForSupervisorYesterday(singles: SupervisorPeriodRangeLevelOneQuery.Singles?) {
        if (singles?.displayName != null) {
            single_display_supervisor_period_range.text = singles.displayName
        }

        val serviceSinglesSupervisorGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.goal?.amount,
                singles?.goal?.percentage,
                singles?.goal?.value
        )
        val serviceSinglesSupervisorVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.variance?.amount,
                singles?.variance?.percentage,
                singles?.variance?.value
        )
        val serviceSinglesSupervisorActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.actual?.amount,
                singles?.actual?.percentage,
                singles?.actual?.value
        )

        if (serviceSinglesSupervisorGoalRange.isEmpty() && serviceSinglesSupervisorVarianceRange.isEmpty() && serviceSinglesSupervisorActualRange.isEmpty()) {

            serviceSingles_error_supervisor_period_range_kpi.visibility = View.VISIBLE
            val paramSupervisorRangeSingleError: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            paramSupervisorRangeSingleError.weight = 2.0f
            single_display_supervisor_period_range.layoutParams = paramSupervisorRangeSingleError


            service_singles_goal_supervisor_period_range.visibility = View.GONE
            service_singles_variance_supervisor_period_range.visibility = View.GONE
            service_singles_actual_supervisor_period_range.visibility = View.GONE
        } else if (serviceSinglesSupervisorGoalRange.isNotEmpty() && serviceSinglesSupervisorVarianceRange.isNotEmpty() && serviceSinglesSupervisorActualRange.isNotEmpty()) {

            serviceSingles_error_supervisor_period_range_kpi.visibility = View.GONE
            service_singles_goal_supervisor_period_range.visibility = View.VISIBLE
            service_singles_variance_supervisor_period_range.visibility = View.VISIBLE
            service_singles_actual_supervisor_period_range.visibility = View.VISIBLE

            service_singles_goal_supervisor_period_range.text = serviceSinglesSupervisorGoalRange
            service_singles_variance_supervisor_period_range.text =
                    serviceSinglesSupervisorVarianceRange
            service_singles_actual_supervisor_period_range.text =
                    serviceSinglesSupervisorActualRange
        } else {

            serviceSingles_error_supervisor_period_range_kpi.visibility = View.GONE
            service_singles_goal_supervisor_period_range.visibility = View.VISIBLE
            service_singles_variance_supervisor_period_range.visibility = View.VISIBLE
            service_singles_actual_supervisor_period_range.visibility = View.VISIBLE

            if (serviceSinglesSupervisorGoalRange.isEmpty()) {
                service_singles_goal_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_goal_supervisor_period_range.text =
                        serviceSinglesSupervisorGoalRange
            }

            if (serviceSinglesSupervisorVarianceRange.isEmpty()) {
                service_singles_variance_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_variance_supervisor_period_range.text =
                        serviceSinglesSupervisorVarianceRange
            }

            if (serviceSinglesSupervisorActualRange.isEmpty()) {
                service_singles_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_actual_supervisor_period_range.text =
                        serviceSinglesSupervisorActualRange
            }

        }

        if (singles?.status != null && serviceSinglesSupervisorActualRange.isNotEmpty()) {
            when {
                singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_singles_actual_supervisor_period_range.setTextColor(
                            requireContext().getColor(
                                    R.color.red
                            )
                    )

                }
                singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_singles_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_singles_actual_supervisor_period_range.setTextColor(
                            requireContext().getColor(
                                    R.color.green
                            )
                    )

                }
                else -> {
                    service_singles_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_singles_actual_supervisor_period_range.setTextColor(
                            requireContext().getColor(
                                    R.color.text_color
                            )
                    )

                }
            }
        }
    }

    fun displayCashViewForSupervisorYesterday(cash: SupervisorPeriodRangeLevelOneQuery.Cash?) {
        if (cash?.displayName != null) {
            cash_display_supervisor_period_range.text = cash.displayName
        }

        val cashSupervisorGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.goal?.amount,
                cash?.goal?.percentage,
                cash?.goal?.value
        )
        val cashSupervisorVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.variance?.amount,
                cash?.variance?.percentage,
                cash?.variance?.value
        )
        val cashSupervisorActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.actual?.amount,
                cash?.actual?.percentage,
                cash?.actual?.value
        )

        if (cashSupervisorGoalRange.isEmpty() && cashSupervisorVarianceRange.isEmpty() && cashSupervisorActualRange.isEmpty()) {

            cash_error_supervisor_period_range_kpi.visibility = View.VISIBLE
            val paramSupervisorRangeCashError: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            paramSupervisorRangeCashError.weight = 2.0f
            cash_display_supervisor_period_range.layoutParams = paramSupervisorRangeCashError


            cash_goal_supervisor_period_range.visibility = View.GONE
            cash_variance_supervisor_period_range.visibility = View.GONE
            cash_actual_supervisor_period_range.visibility = View.GONE
            cash_parent_img_supervisor_period_range.visibility = View.GONE
            cash_parent_layout_supervisor_period_range.isClickable = false
        } else if (cashSupervisorGoalRange.isNotEmpty() && cashSupervisorVarianceRange.isNotEmpty() && cashSupervisorActualRange.isNotEmpty()) {

            cash_error_supervisor_period_range_kpi.visibility = View.GONE
            cash_goal_supervisor_period_range.visibility = View.VISIBLE
            cash_variance_supervisor_period_range.visibility = View.VISIBLE
            cash_actual_supervisor_period_range.visibility = View.VISIBLE

            cash_goal_supervisor_period_range.text = cashSupervisorGoalRange
            cash_variance_supervisor_period_range.text = cashSupervisorVarianceRange
            cash_actual_supervisor_period_range.text = cashSupervisorActualRange
        } else {

            cash_error_supervisor_period_range_kpi.visibility = View.GONE
            cash_goal_supervisor_period_range.visibility = View.VISIBLE
            cash_variance_supervisor_period_range.visibility = View.VISIBLE
            cash_actual_supervisor_period_range.visibility = View.VISIBLE

            if (cashSupervisorGoalRange.isEmpty()) {
                cash_goal_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_goal_supervisor_period_range.text = cashSupervisorGoalRange
            }

            if (cashSupervisorVarianceRange.isEmpty()) {
                cash_variance_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_variance_supervisor_period_range.text = cashSupervisorVarianceRange
            }

            if (cashSupervisorActualRange.isEmpty()) {
                cash_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_actual_supervisor_period_range.text = cashSupervisorActualRange
            }

        }

        if (cash?.status != null && cashSupervisorActualRange.isNotEmpty()) {
            when {
                cash.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    cash_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.red))
                }
                cash.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    cash_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    cash_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    cash_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }
    }

    fun displayOERViewForSupervisorYesterday(oerStart: SupervisorPeriodRangeLevelOneQuery.OerStart?) {
        if (oerStart?.displayName != null) {
            oer_display_supervisor_period_range.text = oerStart.displayName
        }

        val oerSupervisorGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.goal?.amount,
                oerStart?.goal?.percentage,
                oerStart?.goal?.value
        )
        val oerSupervisorVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.variance?.amount,
                oerStart?.variance?.percentage,
                oerStart?.variance?.value
        )
        val oerSupervisorActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.actual?.amount,
                oerStart?.actual?.percentage,
                oerStart?.actual?.value
        )

        if (oerSupervisorGoalRange.isEmpty() && oerSupervisorVarianceRange.isEmpty() && oerSupervisorActualRange.isEmpty()) {

            oer_error_supervisor_period_range_kpi.visibility = View.VISIBLE
            val paramSupervisorRangeOERError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramSupervisorRangeOERError.weight = 2.0f
            oer_display_supervisor_period_range.layoutParams = paramSupervisorRangeOERError

            oer_goal_supervisor_period_range.visibility = View.GONE
            oer_variance_supervisor_period_range.visibility = View.GONE
            oer_actual_supervisor_period_range.visibility = View.GONE
            oer_parent_img_supervisor_period_range.visibility = View.GONE
            oer_parent_layout_supervisor_period_range.isClickable = false
        } else if (oerSupervisorGoalRange.isNotEmpty() && oerSupervisorVarianceRange.isNotEmpty() && oerSupervisorActualRange.isNotEmpty()) {

            oer_error_supervisor_period_range_kpi.visibility = View.GONE
            oer_goal_supervisor_period_range.visibility = View.VISIBLE
            oer_variance_supervisor_period_range.visibility = View.VISIBLE
            oer_actual_supervisor_period_range.visibility = View.VISIBLE

            oer_goal_supervisor_period_range.text = oerSupervisorGoalRange
            oer_variance_supervisor_period_range.text = oerSupervisorVarianceRange
            oer_actual_supervisor_period_range.text = oerSupervisorActualRange
        } else {

            oer_error_supervisor_period_range_kpi.visibility = View.GONE
            oer_goal_supervisor_period_range.visibility = View.VISIBLE
            oer_variance_supervisor_period_range.visibility = View.VISIBLE
            oer_actual_supervisor_period_range.visibility = View.VISIBLE

            if (oerSupervisorGoalRange.isEmpty()) {
                oer_goal_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_goal_supervisor_period_range.text = oerSupervisorGoalRange
            }

            if (oerSupervisorVarianceRange.isEmpty()) {
                oer_variance_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_variance_supervisor_period_range.text = oerSupervisorVarianceRange
            }

            if (oerSupervisorActualRange.isEmpty()) {
                oer_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_actual_supervisor_period_range.text = oerSupervisorActualRange
            }

        }

        if (oerStart?.status != null && oerSupervisorActualRange.isNotEmpty()) {
            when {
                oerStart.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oer_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    oer_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.red))

                }
                oerStart.status.toString() == resources.getString(R.string.under_limit) -> {
                    oer_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    oer_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    oer_actual_supervisor_period_range.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    oer_actual_supervisor_period_range.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.aws_parent_layout_supervisor_period_range -> {

                rcv_labour_supervisor_period_range.visibility = View.GONE
                rcv_service_supervisor_period_range.visibility = View.GONE
                rcv_oer_supervisor_period_range.visibility = View.GONE
                rcv_cash_supervisor_period_range.visibility = View.GONE
                rcv_food_supervisor_period_range.visibility = View.GONE

                labour_text_overview_supervisor_period_range.visibility = View.GONE
                service_text_overview_supervisor_period_range.visibility = View.GONE
                oer_text_overview_supervisor_period_range.visibility = View.GONE
                cash_text_overview_supervisor_period_range.visibility = View.GONE
                food_text_overview_supervisor_period_range.visibility = View.GONE

                labor_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)

                food_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                rcv_food_supervisor_period_range.visibility = View.GONE
                food_text_overview_supervisor_period_range.visibility = View.GONE

                if (rcv_sales_supervisor_period_range.visibility == View.VISIBLE) {
                    rcv_sales_supervisor_period_range.visibility = View.GONE
                    aws_text_overview_supervisor_period_range.visibility = View.GONE
                    aws_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_sales_supervisor_period_range.visibility = View.VISIBLE
                    aws_text_overview_supervisor_period_range.visibility = View.VISIBLE
                    aws_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_up)
                }
                callSupervisorPeriodLevelTwoQuery(getString(R.string.awus_text), rcv_sales_supervisor_period_range)
            }

            R.id.labour_parent_layout_supervisor_period_range -> {
                rcv_sales_supervisor_period_range.visibility = View.GONE
                rcv_food_supervisor_period_range.visibility = View.GONE
                rcv_service_supervisor_period_range.visibility = View.GONE
                rcv_oer_supervisor_period_range.visibility = View.GONE
                rcv_cash_supervisor_period_range.visibility = View.GONE

                aws_text_overview_supervisor_period_range.visibility = View.GONE
                service_text_overview_supervisor_period_range.visibility = View.GONE
                oer_text_overview_supervisor_period_range.visibility = View.GONE
                cash_text_overview_supervisor_period_range.visibility = View.GONE
                food_text_overview_supervisor_period_range.visibility = View.GONE

                aws_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_labour_supervisor_period_range.visibility == View.VISIBLE) {
                    rcv_labour_supervisor_period_range.visibility = View.GONE
                    labour_text_overview_supervisor_period_range.visibility = View.GONE
                    labor_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_labour_supervisor_period_range.visibility = View.VISIBLE
                    labour_text_overview_supervisor_period_range.visibility = View.VISIBLE
                    labor_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callSupervisorPeriodLevelTwoQuery(getString(R.string.labour_text), rcv_labour_supervisor_period_range)

            }

            R.id.service_parent_layout_supervisor_period_range -> {
                rcv_sales_supervisor_period_range.visibility = View.GONE
                rcv_labour_supervisor_period_range.visibility = View.GONE
                rcv_oer_supervisor_period_range.visibility = View.GONE
                rcv_cash_supervisor_period_range.visibility = View.GONE

                aws_text_overview_supervisor_period_range.visibility = View.GONE
                labour_text_overview_supervisor_period_range.visibility = View.GONE
                oer_text_overview_supervisor_period_range.visibility = View.GONE
                cash_text_overview_supervisor_period_range.visibility = View.GONE


                aws_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)

                food_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                rcv_food_supervisor_period_range.visibility = View.GONE
                food_text_overview_supervisor_period_range.visibility = View.GONE

                if (rcv_service_supervisor_period_range.visibility == View.VISIBLE) {
                    rcv_service_supervisor_period_range.visibility = View.GONE
                    service_text_overview_supervisor_period_range.visibility = View.GONE
                    service_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_service_supervisor_period_range.visibility = View.VISIBLE
                    service_text_overview_supervisor_period_range.visibility = View.VISIBLE
                    service_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callSupervisorPeriodLevelTwoQuery(getString(R.string.service_text), rcv_service_supervisor_period_range)

            }

            R.id.cash_parent_layout_supervisor_period_range -> {
                rcv_sales_supervisor_period_range.visibility = View.GONE
                rcv_labour_supervisor_period_range.visibility = View.GONE
                rcv_service_supervisor_period_range.visibility = View.GONE
                rcv_oer_supervisor_period_range.visibility = View.GONE

                aws_text_overview_supervisor_period_range.visibility = View.GONE
                labour_text_overview_supervisor_period_range.visibility = View.GONE
                service_text_overview_supervisor_period_range.visibility = View.GONE
                oer_text_overview_supervisor_period_range.visibility = View.GONE


                aws_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)

                food_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                rcv_food_supervisor_period_range.visibility = View.GONE
                food_text_overview_supervisor_period_range.visibility = View.GONE

                if (rcv_cash_supervisor_period_range.visibility == View.VISIBLE) {
                    rcv_cash_supervisor_period_range.visibility = View.GONE
                    cash_text_overview_supervisor_period_range.visibility = View.GONE
                    cash_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_cash_supervisor_period_range.visibility = View.VISIBLE
                    cash_text_overview_supervisor_period_range.visibility = View.VISIBLE
                    cash_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callSupervisorPeriodLevelTwoQuery(getString(R.string.cash_text), rcv_cash_supervisor_period_range)

            }

            R.id.oer_parent_layout_supervisor_period_range -> {
                rcv_sales_supervisor_period_range.visibility = View.GONE
                rcv_labour_supervisor_period_range.visibility = View.GONE
                rcv_service_supervisor_period_range.visibility = View.GONE
                rcv_cash_supervisor_period_range.visibility = View.GONE

                aws_text_overview_supervisor_period_range.visibility = View.GONE
                labour_text_overview_supervisor_period_range.visibility = View.GONE
                service_text_overview_supervisor_period_range.visibility = View.GONE
                cash_text_overview_supervisor_period_range.visibility = View.GONE


                aws_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)

                food_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                rcv_food_supervisor_period_range.visibility = View.GONE
                food_text_overview_supervisor_period_range.visibility = View.GONE

                if (rcv_oer_supervisor_period_range.visibility == View.VISIBLE) {
                    rcv_oer_supervisor_period_range.visibility = View.GONE
                    oer_text_overview_supervisor_period_range.visibility = View.GONE
                    oer_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_oer_supervisor_period_range.visibility = View.VISIBLE
                    oer_text_overview_supervisor_period_range.visibility = View.VISIBLE
                    oer_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callSupervisorPeriodLevelTwoQuery(getString(R.string.oer_text), rcv_oer_supervisor_period_range)

            }

            R.id.food_parent_layout_supervisor_period_range -> {
                rcv_sales_supervisor_period_range.visibility = View.GONE
                rcv_labour_supervisor_period_range.visibility = View.GONE
                rcv_service_supervisor_period_range.visibility = View.GONE
                rcv_oer_supervisor_period_range.visibility = View.GONE
                rcv_cash_supervisor_period_range.visibility = View.GONE

                aws_text_overview_supervisor_period_range.visibility = View.GONE
                labour_text_overview_supervisor_period_range.visibility = View.GONE
                service_text_overview_supervisor_period_range.visibility = View.GONE
                oer_text_overview_supervisor_period_range.visibility = View.GONE
                cash_text_overview_supervisor_period_range.visibility = View.GONE

                aws_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_food_supervisor_period_range.visibility == View.VISIBLE) {
                    rcv_food_supervisor_period_range.visibility = View.GONE
                    food_text_overview_supervisor_period_range.visibility = View.GONE
                    food_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_food_supervisor_period_range.visibility = View.VISIBLE
                    food_text_overview_supervisor_period_range.visibility = View.VISIBLE
                    food_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callSupervisorPeriodLevelTwoQuery(
                        getString(R.string.ideal_vs_food_variance_text),
                        rcv_food_supervisor_period_range
                )
            }

            R.id.filter_icon -> {
                openSupervisorPeriodRangeKpiFilter()
            }
            R.id.filter_parent_linear -> {
                openSupervisorPeriodRangeKpiFilter()
            }
            R.id.error_filter_parent_linear -> {
                openSupervisorPeriodRangeKpiFilter()
            }

            R.id.aws_text_overview_supervisor_period_range -> {
                callSupervisorPeriodOverviewQuery(getString(R.string.awus_text), "")
            }
            R.id.food_text_overview_supervisor_period_range -> {
                callSupervisorPeriodOverviewQuery(getString(R.string.food_text), "")
            }
            R.id.labour_text_overview_supervisor_period_range -> {
                callSupervisorPeriodOverviewQuery(getString(R.string.labour_text), "")
            }
            R.id.service_text_overview_supervisor_period_range -> {
                callSupervisorPeriodOverviewQuery(getString(R.string.service_text), "")
            }
            R.id.oer_text_overview_supervisor_period_range -> {
                callSupervisorPeriodOverviewQuery(getString(R.string.oer_text), "")
            }
            R.id.cash_text_overview_supervisor_period_range -> {
                callSupervisorPeriodOverviewQuery(getString(R.string.cash_text), "")
            }
        }
    }

    fun callSupervisorPeriodLevelTwoQuery(
            actionForSupervisorPeriodRangeLevel2: String,
            rcvForSupervisorPeriodRangeLevel2: RecyclerView
    ) {
        val progressDialogSupervisorPeriodRangeLevel2 = CustomProgressDialog(requireActivity())
        lifecycleScope.launchWhenResumed {
            progressDialogSupervisorPeriodRangeLevel2.showProgressDialog()
            val storeCodeSupervisorPeriodRangeLevel2 = dbHelperSupervisorPeriodRange.getAllSelectedStoreList(true)
            val superVisorNumberListSupervisorPeriodRangeLevel2 =  dbHelperSupervisorPeriodRange.getAllSelectedStoreListSupervisor(true)

            Logger.info(
                    SupervisorPeriodRangeLevelTwoQuery.OPERATION_NAME.name(),
                    "Period Range Level 2",
                    mapQueryFilters(
                            QueryData(
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    superVisorNumberListSupervisorPeriodRangeLevel2,
                                    storeCodeSupervisorPeriodRangeLevel2,
                                    superVisorPeriodRangeFormattedEndDateValue,
                                    superVisorPeriodRangeFormattedStartDateValue,
                                    StorePrefData.filterType,
                                    SupervisorPeriodRangeLevelTwoQuery.QUERY_DOCUMENT
                            )
                    )
            )


            try {
                val responseSupervisorPeriodRangeLevel2 =
                        apolloClient(requireContext()).query(
                                SupervisorPeriodRangeLevelTwoQuery(
                                        superVisorNumberListSupervisorPeriodRangeLevel2.toInput(),
                                        storeCodeSupervisorPeriodRangeLevel2.toInput(),
                                        StorePrefData.endDateValue.toInput(),
                                        StorePrefData.startDateValue.toInput(),
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                                .toInput()
                                )
                        )
                                .await()

                if (responseSupervisorPeriodRangeLevel2.data?.supervisor != null) {
                    progressDialogSupervisorPeriodRangeLevel2.dismissProgressDialog()
                    supervisorPeriodRangeLevelTwo = responseSupervisorPeriodRangeLevel2.data?.supervisor!!

                    supervisorPeriodRangeLevelTwo.kpis?.individualStores.let {
                        setSupervisorPeriodRangeKpiOverViewKpiApiExpandableData(
                                actionForSupervisorPeriodRangeLevel2,
                                rcvForSupervisorPeriodRangeLevel2
                        )
                    }
                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogSupervisorPeriodRangeLevel2.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Level 2")
                        }
                refreshSupervisorPeriodRangeKpiToken()
                return@launchWhenResumed
            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogSupervisorPeriodRangeLevel2.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForSupervisorPeriod()
                } else {
                    Logger.error(apolloNetworkException.message.toString(), "Period Level 2")
                }

            } catch (e: ApolloException) {
                progressDialogSupervisorPeriodRangeLevel2.dismissProgressDialog()
                Logger.error(e.message.toString(), "Period Level 2")
                setErrorScreenVisibleStateForSupervisorPeriod(
                        getString(R.string.exception_error_text_title), getString(
                        R.string
                                .exception_error_text_description
                )
                )
            }
        }
    }

    private fun setSupervisorPeriodRangeKpiOverViewKpiApiExpandableData(
            actionSupervisorPeriodRangeExpandable: String,
            rcvSupervisorPeriodRangeExpandable: RecyclerView
    ) {
        checkSupervisorPeriodRangeSize(rcvSupervisorPeriodRangeExpandable,actionSupervisorPeriodRangeExpandable)
        rcvSupervisorPeriodRangeExpandable.layoutManager = LinearLayoutManager(
                activity,
                LinearLayoutManager.VERTICAL,
                false
        )
        val storeDetailSupervisorPeriodRangeExpandable =
                supervisorPeriodRangeLevelTwo.kpis?.individualStores
        val childDataSupervisorPeriodRangeExpandable = mutableListOf<StoreDetailPojo>()
        storeDetailSupervisorPeriodRangeExpandable?.forEachIndexed {_, item ->
            when (actionSupervisorPeriodRangeExpandable) {
                requireActivity().getString(R.string.awus_text) -> {
                    childDataSupervisorPeriodRangeExpandable.add(
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
                    childDataSupervisorPeriodRangeExpandable.add(
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
                    childDataSupervisorPeriodRangeExpandable.add(
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
                    childDataSupervisorPeriodRangeExpandable.add(
                            StoreDetailPojo(
                                    item?.storeNumber.toString(),
                                    null,
                                    null,
                                    null,

                                    item?.period?.service?.status.toString()
                            )
                    )
                }
                requireActivity().getString(R.string.cash_text) -> {
                    childDataSupervisorPeriodRangeExpandable.add(
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
                    childDataSupervisorPeriodRangeExpandable.add(
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
        supervisorPeriodRangeAdapter = SupervisorYesterdayAndPeriodAdapter(
                requireContext(),
                childDataSupervisorPeriodRangeExpandable,
                actionSupervisorPeriodRangeExpandable,
                ""
        )
        rcvSupervisorPeriodRangeExpandable.adapter = supervisorPeriodRangeAdapter

        supervisorPeriodRangeAdapter?.setOnSupervisorYesterdayItemClickListener(object :
                                                                                        SupervisorYesterdayAndPeriodAdapter.OnItemClickListener {
            override fun onItemClick(
                    position: Int,
                    storeNumber: String?,
                    action: String
            ) {
                callSupervisorPeriodOverviewQuery(action, storeNumber!!)
            }

        })


    }


    private fun callSupervisorPeriodOverviewQuery(
            actionSupervisorPeriodRange: String,
            storeNumberSupervisorPeriodRange: String
    ) {
        lifecycleScope.launchWhenResumed {
            val storeNumberListSupervisorPeriodRange = mutableListOf<String>()

            if (storeNumberSupervisorPeriodRange.isNotEmpty() || storeNumberSupervisorPeriodRange.isNotBlank()) {
                storeNumberListSupervisorPeriodRange.add(storeNumberSupervisorPeriodRange)
            } else {
                storeNumberListSupervisorPeriodRange.addAll(
                        dbHelperSupervisorPeriodRange.getAllSelectedStoreList(
                                true
                        )
                )
            }

            if (StorePrefData.isCalendarSelected) {
                superVisorPeriodRangeFormattedStartDateValue = StorePrefData.startDateValue
                superVisorPeriodRangeFormattedEndDateValue = StorePrefData.endDateValue
            } else {
                superVisorPeriodRangeFormattedStartDateValue = StorePrefData.startDateValue
                superVisorPeriodRangeFormattedEndDateValue = StorePrefData.endDateValue
            }

            val progressDialogSupervisorPeriodRange = CustomProgressDialog(requireActivity())
            progressDialogSupervisorPeriodRange.showProgressDialog()

            try {
                val response =
                        apolloClient(requireContext()).query(
                                SupervisorOverviewRangeQuery(
                                        storeNumberListSupervisorPeriodRange.toInput(),
                                        superVisorPeriodRangeFormattedStartDateValue,
                                        superVisorPeriodRangeFormattedEndDateValue,
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                )
                        )
                                .await()

                if (response.data?.supervisor != null) {
                    progressDialogSupervisorPeriodRange.dismissProgressDialog()
                    when (actionSupervisorPeriodRange) {
                        getString(R.string.awus_text) -> {
                            openSupervisorPeriodRangeKpiSalesDetail(response.data?.supervisor!!)
                        }
                        getString(R.string.food_text) -> {
                            openSupervisorPeriodRangeKpiFoodDetail(response.data?.supervisor!!)
                        }
                        getString(R.string.labour_text) -> {
                            openSupervisorPeriodRangeKpiLabourDetail(response.data?.supervisor!!)
                        }
                        getString(R.string.service_text) -> {
                            openSupervisorPeriodRangeKpiServiceDetail(response.data?.supervisor!!)
                        }
                        getString(R.string.oer_text) -> {
                            openSupervisorPeriodRangeKpiOERDetail(response.data?.supervisor!!)
                        }
                        getString(R.string.cash_text) -> {
                            openSupervisorPeriodRangeKpiCASHDetail(response.data?.supervisor!!)
                        }
                    }
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogSupervisorPeriodRange.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Overview KPI")
                        }
                refreshSupervisorPeriodRangeKpiToken()
                return@launchWhenResumed
            } catch (e: ApolloNetworkException) {
                progressDialogSupervisorPeriodRange.dismissProgressDialog()

                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForSupervisorPeriod()
                }
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Period Range Overview KPI")
                progressDialogSupervisorPeriodRange.dismissProgressDialog()
                setErrorScreenVisibleStateForSupervisorPeriod(
                        getString(R.string.exception_error_text_title), getString(
                        R.string
                                .exception_error_text_description
                )
                )
            }

        }
    }

    private fun openSupervisorPeriodRangeKpiFilter() {
        val intentSupervisorPeriodRangeKpiFilter =
                Intent(requireContext(), FilterActivity::class.java)
        startActivity(intentSupervisorPeriodRangeKpiFilter)
    }

    private fun openSupervisorPeriodRangeKpiSalesDetail(supervisorPeriodRangeSalesDetail: SupervisorOverviewRangeQuery.Supervisor) {
        val intentSupervisorPeriodRangeSalesDetail =
                Intent(requireContext(), AWUSKpiActivity::class.java)
        intentSupervisorPeriodRangeSalesDetail.putExtra(
                "awus_data",
                gson.toJson(supervisorPeriodRangeSalesDetail)
        )
        intentSupervisorPeriodRangeSalesDetail.putExtra(
                "api_argument_from_filter",
                IpConstants.rangeFrom
        )
        startActivity(intentSupervisorPeriodRangeSalesDetail)

    }

    private fun openSupervisorPeriodRangeKpiFoodDetail(supervisorPeriodRangeFoodDetail: SupervisorOverviewRangeQuery.Supervisor) {
        val intentSupervisorPeriodRangeFoodDetail =
                Intent(requireContext(), FoodKpiActivity::class.java)
        intentSupervisorPeriodRangeFoodDetail.putExtra(
                "food_data",
                gson.toJson(supervisorPeriodRangeFoodDetail)
        )
        intentSupervisorPeriodRangeFoodDetail.putExtra(
                "api_argument_from_filter",
                IpConstants.rangeFrom
        )
        startActivity(intentSupervisorPeriodRangeFoodDetail)
    }

    private fun openSupervisorPeriodRangeKpiLabourDetail(supervisorPeriodRangeFoodDetail: SupervisorOverviewRangeQuery.Supervisor) {
        val intentSupervisorPeriodRangeFoodDetail =
                Intent(requireContext(), LabourKpiActivity::class.java)
        intentSupervisorPeriodRangeFoodDetail.putExtra(
                "labour_data",
                gson.toJson(supervisorPeriodRangeFoodDetail)
        )
        intentSupervisorPeriodRangeFoodDetail.putExtra(
                "api_argument_from_filter",
                IpConstants.rangeFrom
        )
        startActivity(intentSupervisorPeriodRangeFoodDetail)
    }

    private fun openSupervisorPeriodRangeKpiServiceDetail(supervisorPeriodRangeServiceDetail: SupervisorOverviewRangeQuery.Supervisor) {
        val intentSupervisorPeriodRangeServiceDetail =
                Intent(requireContext(), ServiceKpiActivity::class.java)
        intentSupervisorPeriodRangeServiceDetail.putExtra(
                "service_data",
                gson.toJson(supervisorPeriodRangeServiceDetail)
        )
        intentSupervisorPeriodRangeServiceDetail.putExtra(
                "api_argument_from_filter",
                IpConstants.rangeFrom
        )
        startActivity(intentSupervisorPeriodRangeServiceDetail)
    }

    private fun openSupervisorPeriodRangeKpiOERDetail(supervisorPeriodRangeOerDetail: SupervisorOverviewRangeQuery.Supervisor) {
        val intentSupervisorPeriodRangeOerDetail =
                Intent(requireContext(), OERStartActivity::class.java)
        intentSupervisorPeriodRangeOerDetail.putExtra(
                "oer_data",
                gson.toJson(supervisorPeriodRangeOerDetail)
        )
        intentSupervisorPeriodRangeOerDetail.putExtra(
                "api_argument_from_filter",
                IpConstants.rangeFrom
        )
        startActivity(intentSupervisorPeriodRangeOerDetail)
    }

    private fun openSupervisorPeriodRangeKpiCASHDetail(supervisorPeriodRangeCashDetail: SupervisorOverviewRangeQuery.Supervisor) {
        val intentSupervisorPeriodRangeCashDetail =
                Intent(requireContext(), CashKpiActivity::class.java)
        intentSupervisorPeriodRangeCashDetail.putExtra(
                "cash_data",
                gson.toJson(supervisorPeriodRangeCashDetail)
        )
        intentSupervisorPeriodRangeCashDetail.putExtra(
                "api_argument_from_filter",
                IpConstants.rangeFrom
        )
        startActivity(intentSupervisorPeriodRangeCashDetail)
    }

    private fun refreshSupervisorPeriodRangeKpiToken() {

        refreshTokenViewModelSupervisorPeriodRange.getRefreshToken()

        refreshTokenViewModelSupervisorPeriodRange.refreshTokenResponseLiveData.observe(requireActivity(), {
            run {
                when (it.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        callSupervisorPeriodRangeLevelOneQuery()
                    }
                    Status.UNSUCCESSFUL -> {
                        CommonUtil.navigateToLogin(mainActivity)
                    }
                    Status.ERROR -> {
                    }
                    Status.OFFLINE -> {
                        setInternetErrorScreenVisibleStateForSupervisorPeriod()
                    }
                }
            }
        })
    }

    private fun checkSupervisorPeriodRangeSize(
            rcvSupervisorPeriodRange: RecyclerView,
            actionSupervisorPeriodRange: String
    ) {
        if (supervisorPeriodRangeLevelTwo.kpis?.individualStores != null && supervisorPeriodRangeLevelTwo.kpis?.individualStores!!.isNotEmpty()) {
            rcvSupervisorPeriodRange.visibility = View.VISIBLE

                when (actionSupervisorPeriodRange) {
                    getString(R.string.awus_text) -> {
                        aws_text_overview_supervisor_period_range.visibility = View.VISIBLE
                        aws_parent_img_supervisor_period_range.visibility = View.VISIBLE

                    }
                    getString(R.string.food_text) -> {
                        food_text_overview_supervisor_period_range.visibility = View.VISIBLE
                        food_parent_img_supervisor_period_range.visibility = View.VISIBLE
                    }
                    getString(R.string.labour_text) -> {
                        labour_text_overview_supervisor_period_range.visibility = View.VISIBLE
                        labor_parent_img_supervisor_period_range.visibility = View.VISIBLE

                    }
                    getString(R.string.service_text) -> {
                        service_text_overview_supervisor_period_range.visibility = View.VISIBLE
                        service_parent_img_supervisor_period_range.visibility = View.VISIBLE

                    }
                    getString(R.string.oer_text) -> {
                        oer_text_overview_supervisor_period_range.visibility = View.VISIBLE
                        oer_parent_img_supervisor_period_range.visibility = View.VISIBLE

                    }
                    getString(R.string.cash_text) -> {
                        cash_text_overview_supervisor_period_range.visibility = View.VISIBLE
                        cash_parent_img_supervisor_period_range.visibility = View.VISIBLE
                    }
                }
        }
        else {
            hideSupervisorPeriodOverviewText(actionSupervisorPeriodRange)
            rcvSupervisorPeriodRange.visibility = View.GONE
        }
    }

    private fun hideSupervisorPeriodOverviewText(actionSupervisorPeriodStr: String) {
        when (actionSupervisorPeriodStr) {
            getString(R.string.awus_text) -> {
                aws_text_overview_supervisor_period_range.visibility = View.GONE
                aws_parent_img_supervisor_period_range.visibility = View.GONE

            }
            getString(R.string.food_text) -> {
                food_text_overview_supervisor_period_range.visibility = View.GONE
                food_parent_img_supervisor_period_range.visibility = View.GONE
            }

            getString(R.string.labour_text) -> {
                labour_text_overview_supervisor_period_range.visibility = View.GONE
                    labor_parent_img_supervisor_period_range.visibility = View.GONE

            }
            getString(R.string.service_text) -> {
                service_text_overview_supervisor_period_range.visibility = View.GONE
                service_parent_img_supervisor_period_range.visibility = View.GONE

            }
            getString(R.string.oer_text) -> {
                oer_text_overview_supervisor_period_range.visibility = View.GONE
                oer_parent_img_supervisor_period_range.visibility = View.GONE

            }
            getString(R.string.cash_text) -> {
                cash_text_overview_supervisor_period_range.visibility = View.GONE
                cash_parent_img_supervisor_period_range.visibility = View.GONE
            }
        }
    }

    fun setErrorScreenVisibleStateForSupervisorPeriod(
            title: String,
            description: String
    ) {
        supervisor_range_data_error_layout.visibility = View.VISIBLE
        supervisor_range_data_error_layout.exception_text_title.text = title
        supervisor_range_data_error_layout.exception_text_description.text = description
        common_header_supervisor_range.sales_header_error_image.visibility = View.VISIBLE
        common_header_supervisor_range.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)
        common_header_supervisor_range.error_filter_parent_linear.visibility = View.VISIBLE
        supervisor_period_range_missing_data_error_layout.visibility = View.GONE
        setHeaderViewsVisibleStateForSupervisorPeriod()
        setCalendarViewVisibleStateForSupervisorPeriod()
        hideStoreFilterVisibilityStateForSupervisorPeriod()
    }

    fun setInternetErrorScreenVisibleStateForSupervisorPeriod() {
        supervisor_range_no_internet_error_layout.visibility = View.VISIBLE
        common_header_supervisor_range.sales_header_error_image.visibility = View.VISIBLE
        common_header_supervisor_range.sales_header_error_image.setImageResource(R.drawable.ic_internet_error)
        supervisor_period_range_missing_data_error_layout.visibility = View.GONE
        common_header_supervisor_range.error_filter_parent_linear.visibility = View.GONE
        supervisor_range_data_error_layout.visibility = View.GONE
        setCalendarViewVisibleStateForSupervisorPeriod()
        setHeaderViewsVisibleStateForSupervisorPeriod()
        showStoreFilterVisibilityStateForSupervisorPeriod()
    }

    fun setHeaderViewsVisibleStateForSupervisorPeriod() {
        supervisor_range_header.visibility = View.GONE
        supervisor_range_v1.visibility = View.GONE
        supervisor_range_layout.visibility = View.INVISIBLE
        common_header_supervisor_range.total_sales_common_header.visibility = View.GONE
        common_header_supervisor_range.sales_text_common_header.visibility = View.GONE
    }

    fun hideStoreFilterVisibilityStateForSupervisorPeriod(){
        common_header_supervisor_range.filter_parent_linear.visibility = View.GONE
    }
    fun showStoreFilterVisibilityStateForSupervisorPeriod(){
        common_header_supervisor_range.filter_parent_linear.visibility = View.VISIBLE
    }

    fun setCalendarViewVisibleStateForSupervisorPeriod() {
        common_calendar_supervisor_range.visibility = View.GONE
    }

    fun setMissingDataViewVisibleStateForSupervisorPeriod(
            missingDataTitle: String,
            missingDataDescription: String
    ) {
        supervisor_period_range_missing_data_error_layout.visibility = View.VISIBLE
        supervisor_period_range_missing_data_error_layout.header_data_title.text = missingDataTitle
        supervisor_period_range_missing_data_error_layout.header_data_description.text = missingDataDescription

    }

    fun hideErrorScreenVisibleStateForSupervisorPeriod(){
        supervisor_range_no_internet_error_layout.visibility = View.GONE
        supervisor_range_data_error_layout.visibility = View.GONE
        common_header_supervisor_range.sales_header_error_image.visibility = View.GONE
        common_header_supervisor_range.error_filter_parent_linear.visibility = View.GONE

        supervisor_range_header.visibility = View.VISIBLE
        supervisor_range_v1.visibility = View.VISIBLE
        supervisor_range_layout.visibility = View.VISIBLE

        common_header_supervisor_range.filter_parent_linear.visibility = View.VISIBLE
        common_header_supervisor_range.total_sales_common_header.visibility = View.VISIBLE
        common_header_supervisor_range.sales_text_common_header.visibility = View.VISIBLE

        common_calendar_supervisor_range.visibility = View.VISIBLE
    }

    fun collapseExpendedListVisibilityForSupervisorYesterday(){
        if (rcv_sales_supervisor_period_range.visibility == View.VISIBLE) {
            rcv_sales_supervisor_period_range.visibility = View.GONE
            aws_text_overview_supervisor_period_range.visibility = View.GONE
            aws_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_food_supervisor_period_range.visibility == View.VISIBLE) {
            rcv_food_supervisor_period_range.visibility = View.GONE
            food_text_overview_supervisor_period_range.visibility = View.GONE
            food_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_labour_supervisor_period_range.visibility == View.VISIBLE) {
            rcv_labour_supervisor_period_range.visibility = View.GONE
            labour_text_overview_supervisor_period_range.visibility = View.GONE
            labor_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_service_supervisor_period_range.visibility == View.VISIBLE) {
            rcv_service_supervisor_period_range.visibility = View.GONE
            service_text_overview_supervisor_period_range.visibility = View.GONE
            service_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_cash_supervisor_period_range.visibility == View.VISIBLE) {
            rcv_cash_supervisor_period_range.visibility = View.GONE
            cash_text_overview_supervisor_period_range.visibility = View.GONE
            cash_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_oer_supervisor_period_range.visibility == View.VISIBLE) {
            rcv_oer_supervisor_period_range.visibility = View.GONE
            oer_text_overview_supervisor_period_range.visibility = View.GONE
            oer_parent_img_supervisor_period_range.setImageResource(R.drawable.ic_icon_chevron_down)
        }
    }

}
