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
import com.arria.ping.kpi.supervisor.SupervisorYesterdayLevelOneQuery
import com.arria.ping.kpi.supervisor.SupervisorYesterdayLevelTwoQuery
import com.arria.ping.log.Logger
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
import kotlinx.android.synthetic.main.bonus_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.ceo_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.ceo_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.common_calendar.view.*
import kotlinx.android.synthetic.main.common_header_ceo.*
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.data_error_layout.view.*
import kotlinx.android.synthetic.main.do_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.supervisor_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.supervisor_yesterday_fragment_kpi.*
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class SupervisorYesterdayKpiFragment(val isFromRange: Boolean) : Fragment(), View.OnClickListener {
    private var supervisorYesterdayAdapter: SupervisorYesterdayAndPeriodAdapter? = null
    private lateinit var dbHelperSupervisorYesterday: DatabaseHelperImpl
    private val gsonSupervisorYesterday = Gson()

    lateinit var supervisorYesterdayLevelOne: SupervisorYesterdayLevelOneQuery.Supervisor
    lateinit var supervisorYesterdayLevelTwo: SupervisorYesterdayLevelTwoQuery.Supervisor

    private val refreshTokenViewModelSupervisorYesterday by viewModels<RefreshTokenViewModel>()

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
        return inflater.inflate(R.layout.supervisor_yesterday_fragment_kpi, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        dbHelperSupervisorYesterday = DatabaseHelperImpl(DatabaseBuilder.getInstance(requireActivity()))

        initSupervisorYesterday()

        if(StorePrefData.filterDate.isNotEmpty()){
            setStoreFilterViewForSupervisorYesterday(StorePrefData.filterDate)
        }

        if (networkHelper.isNetworkConnected()) {
            callMissingDataQueryForSupervisorYesterday()
            callSupervisorYesterdayLevelOneQuery()
        } else {
            setInternetErrorScreenVisibleStateForSupervisorYesterday()
        }

        supervisor_yesterday_swipe_refresh_layout.setOnRefreshListener{
            Logger.info("Pull down to refresh Supervisor Yesterday Store Data", "Yesterday KPI")
            callMissingDataQueryForSupervisorYesterday()
            callSupervisorYesterdayLevelOneQuery()
            collapseExpendedListVisibilityForSupervisorYesterday()
            supervisor_yesterday_swipe_refresh_layout.isRefreshing = false
        }
    }

    private fun initSupervisorYesterday() {
        aws_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
        labor_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
        service_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
        oer_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
        cash_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
        food_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)

        aws_parent_layout_supervisor_yesterday.setOnClickListener(this)
        food_parent_layout_supervisor_yesterday.setOnClickListener(this)
        labour_parent_layout_supervisor_yesterday.setOnClickListener(this)
        service_parent_layout_supervisor_yesterday.setOnClickListener(this)
        oer_parent_layout_supervisor_yesterday.setOnClickListener(this)
        cash_parent_layout_supervisor_yesterday.setOnClickListener(this)

        filter_icon.setOnClickListener(this)
        filter_parent_linear.setOnClickListener(this)
        error_filter_parent_linear.setOnClickListener(this)

        aws_text_overview_supervisor_yesterday.setOnClickListener(this)
        food_text_overview_supervisor_yesterday.setOnClickListener(this)
        labour_text_overview_supervisor_yesterday.setOnClickListener(this)
        service_text_overview_supervisor_yesterday.setOnClickListener(this)
        oer_text_overview_supervisor_yesterday.setOnClickListener(this)
        cash_text_overview_supervisor_yesterday.setOnClickListener(this)

        Validation().setCustomCalendar(common_calendar_supervisor_yesterday.square_day)
    }


    private fun callMissingDataQueryForSupervisorYesterday() {
        val progressDialogSupervisorYesterdayKpiApi = CustomProgressDialog(requireActivity())
        progressDialogSupervisorYesterdayKpiApi.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val supervisorNumberSupervisorYesterdayKpiApi = mutableListOf(StorePrefData.email)
            val storeNumberSupervisorYesterdayKpiApi: List<String> =
                    dbHelperSupervisorYesterday.getAllSelectedStoreList(true)

            Logger.info(
                    MissingDataQuery.OPERATION_NAME.name(), "Yesterday Missing Data",
                    mapQueryFilters(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            supervisorNumberSupervisorYesterdayKpiApi,
                            storeNumberSupervisorYesterdayKpiApi,
                            MissingDataQuery.QUERY_DOCUMENT
                    )
            )


            try {
                val responseMissingDataSupervisorYesterdayKpiApi =
                        apolloClient(requireContext()).query(
                                MissingDataQuery(
                                        supervisorNumberSupervisorYesterdayKpiApi.toInput(),
                                        storeNumberSupervisorYesterdayKpiApi.toInput()
                                )
                        )
                                .await()

                if (responseMissingDataSupervisorYesterdayKpiApi.data?.missingData != null) {
                    progressDialogSupervisorYesterdayKpiApi.dismissProgressDialog()
                    setMissingDataViewVisibleStateForSupervisorYesterday(
                            responseMissingDataSupervisorYesterdayKpiApi
                                    .data?.missingData!!.header.toString(),
                            responseMissingDataSupervisorYesterdayKpiApi.data?.missingData!!.message.toString()
                    )

                } else {
                    progressDialogSupervisorYesterdayKpiApi.dismissProgressDialog()
                    supervisor_yesterday_missing_data_error_layout.visibility = View.GONE
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogSupervisorYesterdayKpiApi.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Overview KPI")
                        }
                refreshSupervisorYesterdayKpiToken()
                return@launchWhenResumed


            } catch (e: ApolloNetworkException) {
                progressDialogSupervisorYesterdayKpiApi.dismissProgressDialog()

                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForSupervisorYesterday()
                }

            } catch (e: ApolloException) {
                progressDialogSupervisorYesterdayKpiApi.dismissProgressDialog()
                Logger.error(e.message.toString(), "Yesterday Missing Data")

            }

        }
    }

    private fun callSupervisorYesterdayLevelOneQuery() {
        val progressDialogSupervisorYesterdayKpiApi = CustomProgressDialog(requireActivity())
        progressDialogSupervisorYesterdayKpiApi.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            if (networkHelper.isNetworkConnected()) {
                hideErrorScreenVisibleStateForSupervisorYesterday()
            }

            val supervisorNumberSupervisorYesterdayKpiApi : List<String> = dbHelperSupervisorYesterday.getAllSelectedStoreListSupervisor(true)
            val storeNumberSupervisorYesterdayKpiApi: List<String> = dbHelperSupervisorYesterday.getAllSelectedStoreList(true)


            val supervisorYesterdayStoreListValue = mutableListOf<String>()
            supervisorYesterdayStoreListValue.add(StorePrefData.StoreIdFromLogin)
            Logger.info(
                    SupervisorYesterdayLevelOneQuery.OPERATION_NAME.name(),
                    "Yesterday Level 1 KPI",
                    mapQueryFilters(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            supervisorNumberSupervisorYesterdayKpiApi,
                            storeNumberSupervisorYesterdayKpiApi,
                            SupervisorYesterdayLevelOneQuery.QUERY_DOCUMENT
                    )
            )
            try {
                val responseSupervisorYesterdayKpiApi =
                        apolloClient(requireContext()).query(
                                SupervisorYesterdayLevelOneQuery(
                                        storeNumberSupervisorYesterdayKpiApi.toInput(),

                                        )
                        )
                                .await()

                if (responseSupervisorYesterdayKpiApi.data?.supervisor != null) {
                    progressDialogSupervisorYesterdayKpiApi.dismissProgressDialog()
                    supervisorYesterdayLevelOne = responseSupervisorYesterdayKpiApi.data?.supervisor!!

                    if (supervisorYesterdayLevelOne.kpis?.stores?.yesterday != null) {
                        setSupervisorYesterdayStoreData(supervisorYesterdayLevelOne.kpis?.stores?.yesterday)
                    } else {
                        setErrorScreenVisibleStateForSupervisorPeriod(
                                getString(R.string.error_text_title),
                                getString(R.string.error_text_description)
                        )
                    }
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogSupervisorYesterdayKpiApi.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Level 1")
                        }
                refreshSupervisorYesterdayKpiToken()
                return@launchWhenResumed
            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogSupervisorYesterdayKpiApi.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForSupervisorYesterday()
                } else {
                    Logger.error(apolloNetworkException.message.toString(), "Yesterday Level 1")
                }

            } catch (e: ApolloException) {
                progressDialogSupervisorYesterdayKpiApi.dismissProgressDialog()
                Logger.error(e.message.toString(), "Yesterday Level 1 KPI")
                setErrorScreenVisibleStateForSupervisorPeriod(
                        getString(R.string.exception_error_text_title), getString(
                        R.string
                                .exception_error_text_description
                )
                )
            }

        }
    }

    private fun setSupervisorYesterdayStoreData(
            supervisorYesterday: SupervisorYesterdayLevelOneQuery.Yesterday?,
    ) {
        if(supervisorYesterday?.periodFrom != null){
            StorePrefData.dayOfLastServiceDate = supervisorYesterday.periodFrom
        }
        val supervisorYesterdaySelectedDate: String? =
                supervisorYesterday?.periodFrom?.let {
                    supervisorYesterday.periodTo?.let {it1 ->
                        EnumMapperUtil.getSelectedDate(
                                it,
                                it1, EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                        )
                    }
                }

        if(!supervisorYesterdaySelectedDate.isNullOrEmpty()){
            StorePrefData.filterDate = supervisorYesterdaySelectedDate
            setStoreFilterViewForSupervisorYesterday(StorePrefData.filterDate)
        }


        val supervisorYesterdaySalesValue = Validation().checkAmountPercentageValue(
                requireActivity(),
                supervisorYesterday?.sales?.actual?.amount,
                supervisorYesterday?.sales?.actual?.percentage,
                supervisorYesterday?.sales?.actual?.value,
        )
        if (supervisorYesterdaySalesValue.isEmpty()) {
            hideVisibilityStateOfSalesDataForSupervisorYesterday()
        } else {
            showVisibilityStateOfSalesDataForSupervisorYesterday(supervisorYesterdaySalesValue)
        }

        displaySalesViewForSupervisorYesterday(supervisorYesterday?.sales)
        displayFoodViewForSupervisorYesterday(supervisorYesterday?.food)
        displayLaborViewForSupervisorYesterday(supervisorYesterday?.labor)
        displayEADTServiceViewForSupervisorYesterday(supervisorYesterday?.service)
        displayExtremeServiceViewForSupervisorYesterday(supervisorYesterday?.service?.extremeDelivery)
        displaySingleServiceViewForSupervisorYesterday(supervisorYesterday?.service?.singles)
        displayCashViewForSupervisorYesterday(supervisorYesterday?.cash)
        displayOERViewForSupervisorYesterday(supervisorYesterday?.oerStart)
    }

    fun setStoreFilterViewForSupervisorYesterday(date: String){
        val periodTextSupervisorYesterday = "$date | ${getString(R.string.yesterday_text)}"
        Validation().validateFilterKPI(
                requireActivity(),
                dbHelperSupervisorYesterday,
                common_header_supervisor_yesterday.store_header!!,
                periodTextSupervisorYesterday
        )
    }

    fun hideVisibilityStateOfSalesDataForSupervisorYesterday(){
        common_header_supervisor_yesterday.total_sales_common_header.visibility = View.GONE
        common_header_supervisor_yesterday.sales_text_common_header.visibility = View.GONE
        common_header_supervisor_yesterday.sales_header_error_image.visibility = View.VISIBLE
        common_header_supervisor_yesterday.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)
    }
    fun showVisibilityStateOfSalesDataForSupervisorYesterday(supervisorYesterdaySalesValue: String) {
        common_header_supervisor_yesterday.total_sales_common_header.visibility = View.VISIBLE
        common_header_supervisor_yesterday.total_sales_common_header.text = supervisorYesterdaySalesValue
        common_header_supervisor_yesterday.sales_text_common_header.visibility = View.VISIBLE
        common_header_supervisor_yesterday.sales_text_common_header.text = getString(R.string.sales_text)
        common_header_supervisor_yesterday.sales_header_error_image.visibility = View.GONE
    }


    fun displaySalesViewForSupervisorYesterday(sales: SupervisorYesterdayLevelOneQuery.Sales?) {
        if (sales?.displayName != null) {
            aws_display_supervisor_yesterday.text = sales.displayName
        }
        val salesSupervisorYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.goal?.amount,
                sales?.goal?.percentage,
                sales?.goal?.value
        )
        val salesSupervisorYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.variance?.amount,
                sales?.variance?.percentage,
                sales?.variance?.value
        )
        val salesSupervisorYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.actual?.amount,
                sales?.actual?.percentage,
                sales?.actual?.value
        )

        if (salesSupervisorYesterdayGoal.isEmpty() && salesSupervisorYesterdayVariance.isEmpty() && salesSupervisorYesterdayActual.isEmpty()) {

            sales_error_supervisor_yesterday_kpi.visibility = View.VISIBLE
            val paramSupervisorYesterdaySalesError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramSupervisorYesterdaySalesError.weight = 2.0f
            aws_display_supervisor_yesterday.layoutParams = paramSupervisorYesterdaySalesError

            sales_goal_supervisor_yesterday.visibility = View.GONE
            sales_variance_supervisor_yesterday.visibility = View.GONE
            sales_actual_supervisor_yesterday.visibility = View.GONE
            aws_parent_img_supervisor_yesterday.visibility = View.GONE
            aws_parent_layout_supervisor_yesterday.isClickable = false
        } else if (salesSupervisorYesterdayGoal.isNotEmpty() && salesSupervisorYesterdayVariance.isNotEmpty() && salesSupervisorYesterdayActual.isNotEmpty()) {
            sales_error_supervisor_yesterday_kpi.visibility = View.GONE

            sales_goal_supervisor_yesterday.visibility = View.VISIBLE
            sales_variance_supervisor_yesterday.visibility = View.VISIBLE
            sales_actual_supervisor_yesterday.visibility = View.VISIBLE

            sales_goal_supervisor_yesterday.text = salesSupervisorYesterdayGoal
            sales_variance_supervisor_yesterday.text = salesSupervisorYesterdayVariance
            sales_actual_supervisor_yesterday.text = salesSupervisorYesterdayActual

        } else {

            sales_error_supervisor_yesterday_kpi.visibility = View.GONE
            if (salesSupervisorYesterdayGoal.isNotEmpty()) {
                sales_goal_supervisor_yesterday.text = salesSupervisorYesterdayGoal
            } else {
                sales_goal_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            }
            if (salesSupervisorYesterdayVariance.isNotEmpty()) {
                sales_variance_supervisor_yesterday.text = salesSupervisorYesterdayVariance
            } else {
                sales_variance_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            }
            if (salesSupervisorYesterdayActual.isNotEmpty()) {
                sales_actual_supervisor_yesterday.text = salesSupervisorYesterdayActual
            } else {
                sales_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            }
        }

        if (sales?.status?.toString() != null && salesSupervisorYesterdayActual.isNotEmpty()) {
            when {
                sales.status.toString() == resources.getString(R.string.out_of_range) -> {
                    sales_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    sales_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.red))

                }
                sales.status.toString() == resources.getString(R.string.under_limit) -> {
                    sales_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    sales_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    sales_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    sales_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayFoodViewForSupervisorYesterday(food: SupervisorYesterdayLevelOneQuery.Food?) {
        if (food?.displayName != null) {
            food_display_supervisor_yesterday.text = food.displayName
        }

        val foodSupervisorYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.goal?.amount,
                food?.goal?.percentage,
                food?.goal?.value
        )
        val foodSupervisorYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.variance?.amount,
                food?.variance?.percentage,
                food?.variance?.value
        )
        val foodSupervisorYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.actual?.amount,
                food?.actual?.percentage,
                food?.actual?.value
        )

        if (foodSupervisorYesterdayGoal.isEmpty() && foodSupervisorYesterdayVariance.isEmpty() && foodSupervisorYesterdayActual.isEmpty()) {

            food_error_supervisor_yesterday_kpi.visibility = View.VISIBLE
            val paramSupervisorYesterdayFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramSupervisorYesterdayFoodError.weight = 2.0f
            food_display_supervisor_yesterday.layoutParams = paramSupervisorYesterdayFoodError

            food_goal_supervisor_yesterday.visibility = View.GONE
            food_variance_supervisor_yesterday.visibility = View.GONE
            food_actual_supervisor_yesterday.visibility = View.GONE
            food_parent_img_supervisor_yesterday.visibility = View.GONE
            food_parent_layout_supervisor_yesterday.isClickable = false

        } else if (foodSupervisorYesterdayGoal.isNotEmpty() && foodSupervisorYesterdayVariance.isNotEmpty() && foodSupervisorYesterdayActual.isNotEmpty()) {
            food_error_supervisor_yesterday_kpi.visibility = View.GONE

            food_goal_supervisor_yesterday.visibility = View.VISIBLE
            food_variance_supervisor_yesterday.visibility = View.VISIBLE
            food_actual_supervisor_yesterday.visibility = View.VISIBLE

            food_goal_supervisor_yesterday.text = foodSupervisorYesterdayGoal
            food_variance_supervisor_yesterday.text = foodSupervisorYesterdayVariance
            food_actual_supervisor_yesterday.text = foodSupervisorYesterdayActual

        } else {

            food_error_supervisor_yesterday_kpi.visibility = View.GONE
            if (foodSupervisorYesterdayGoal.isNotEmpty()) {
                food_goal_supervisor_yesterday.text = foodSupervisorYesterdayGoal
            } else {
                food_goal_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            }
            if (foodSupervisorYesterdayVariance.isNotEmpty()) {
                food_variance_supervisor_yesterday.text = foodSupervisorYesterdayVariance
            } else {
                food_variance_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            }
            if (foodSupervisorYesterdayActual.isNotEmpty()) {
                food_actual_supervisor_yesterday.text = foodSupervisorYesterdayActual
            } else {
                food_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            }
        }

        if (food?.status?.toString() != null && foodSupervisorYesterdayActual.isNotEmpty()) {
            when {
                food.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.red_circle, 0)
                    food_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.red))
                }
                food.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    food_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    food_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    food_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayLaborViewForSupervisorYesterday(labor: SupervisorYesterdayLevelOneQuery.Labor?) {
        if (labor?.displayName != null) {
            labour_display_supervisor_yesterday.text = labor.displayName
        }

        val labourSupervisorYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.goal?.amount,
                labor?.goal?.percentage,
                labor?.goal?.value
        )
        val labourSupervisorYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.variance?.amount,
                labor?.variance?.percentage,
                labor?.variance?.value
        )
        val labourSupervisorYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.actual?.amount,
                labor?.actual?.percentage,
                labor?.actual?.value
        )

        if (labourSupervisorYesterdayGoal.isEmpty() && labourSupervisorYesterdayVariance.isEmpty() && labourSupervisorYesterdayActual.isEmpty()) {

            labour_error_supervisor_yesterday_kpi.visibility = View.VISIBLE
            val paramSupervisorYesterdayFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramSupervisorYesterdayFoodError.weight = 2.0f
            labour_display_supervisor_yesterday.layoutParams = paramSupervisorYesterdayFoodError

            labour_goal_supervisor_yesterday.visibility = View.GONE
            labour_variance_supervisor_yesterday.visibility = View.GONE
            labour_actual_supervisor_yesterday.visibility = View.GONE
            labor_parent_img_supervisor_yesterday.visibility = View.GONE
            labour_parent_layout_supervisor_yesterday.isClickable = false

        } else if (labourSupervisorYesterdayGoal.isNotEmpty() && labourSupervisorYesterdayVariance.isNotEmpty() && labourSupervisorYesterdayActual.isNotEmpty()) {
            labour_error_supervisor_yesterday_kpi.visibility = View.GONE

            labour_goal_supervisor_yesterday.visibility = View.VISIBLE
            labour_variance_supervisor_yesterday.visibility = View.VISIBLE
            labour_actual_supervisor_yesterday.visibility = View.VISIBLE

            labour_goal_supervisor_yesterday.text = labourSupervisorYesterdayGoal
            labour_variance_supervisor_yesterday.text = labourSupervisorYesterdayVariance
            labour_actual_supervisor_yesterday.text = labourSupervisorYesterdayActual

        } else {

            labour_error_supervisor_yesterday_kpi.visibility = View.GONE

            if (labourSupervisorYesterdayGoal.isNotEmpty()) {
                labour_goal_supervisor_yesterday.text = labourSupervisorYesterdayGoal
            } else {
                labour_goal_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            }
            if (labourSupervisorYesterdayVariance.isNotEmpty()) {
                labour_variance_supervisor_yesterday.text = labourSupervisorYesterdayVariance
            } else {
                labour_variance_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            }
            if (labourSupervisorYesterdayActual.isNotEmpty()) {
                labour_actual_supervisor_yesterday.text = labourSupervisorYesterdayActual
            } else {
                labour_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            }
        }
        if (labor?.status != null && labourSupervisorYesterdayActual.isNotEmpty()) {
            when {
                labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    labour_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.red))

                }
                labor.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    labour_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    labour_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    labour_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayEADTServiceViewForSupervisorYesterday(service: SupervisorYesterdayLevelOneQuery.Service?) {
        if (service?.displayName != null) {
            service_display_supervisor_yesterday.text = service.displayName
        }
        if (service?.eADT?.displayName != null) {
            eadt_display_supervisor_yesterday.text = service.eADT.displayName
        }
        val serviceSupervisorYesterdayEadtGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.goal?.amount,
                service?.eADT?.goal?.percentage,
                service?.eADT?.goal?.value
        )
        val serviceSupervisorYesterdayEadtVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.variance?.amount,
                service?.eADT?.variance?.percentage,
                service?.eADT?.variance?.value
        )
        val serviceSupervisorYesterdayEadtActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.actual?.amount,
                service?.eADT?.actual?.percentage,
                service?.eADT?.actual?.value
        )

        if (serviceSupervisorYesterdayEadtGoal.isEmpty() && serviceSupervisorYesterdayEadtVariance.isEmpty() && serviceSupervisorYesterdayEadtActual.isEmpty()) {

            service_error_supervisor_yesterday_kpi.visibility = View.VISIBLE

            val paramSupervisorYesterdayEadtError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramSupervisorYesterdayEadtError.weight = 2.0f
            eadt_display_supervisor_yesterday.layoutParams = paramSupervisorYesterdayEadtError

            service_eadt_goal_supervisor_yesterday.visibility = View.GONE
            service_eadt_variance_supervisor_yesterday.visibility = View.GONE
            service_eadt_actual_supervisor_yesterday.visibility = View.GONE
        } else if (serviceSupervisorYesterdayEadtGoal.isNotEmpty() && serviceSupervisorYesterdayEadtVariance.isNotEmpty() && serviceSupervisorYesterdayEadtActual.isNotEmpty()) {

            service_error_supervisor_yesterday_kpi.visibility = View.GONE

            service_eadt_goal_supervisor_yesterday.visibility = View.VISIBLE
            service_eadt_variance_supervisor_yesterday.visibility = View.VISIBLE
            service_eadt_actual_supervisor_yesterday.visibility = View.VISIBLE

            service_eadt_goal_supervisor_yesterday.text = serviceSupervisorYesterdayEadtGoal
            service_eadt_variance_supervisor_yesterday.text = serviceSupervisorYesterdayEadtVariance
            service_eadt_actual_supervisor_yesterday.text = serviceSupervisorYesterdayEadtActual
        } else {

            service_error_supervisor_yesterday_kpi.visibility = View.GONE
            service_eadt_goal_supervisor_yesterday.visibility = View.VISIBLE
            service_eadt_variance_supervisor_yesterday.visibility = View.VISIBLE
            service_eadt_actual_supervisor_yesterday.visibility = View.VISIBLE

            if (serviceSupervisorYesterdayEadtGoal.isEmpty()) {
                service_eadt_goal_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_goal_supervisor_yesterday.text = serviceSupervisorYesterdayEadtGoal
            }

            if (serviceSupervisorYesterdayEadtVariance.isEmpty()) {
                service_eadt_variance_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_variance_supervisor_yesterday.text = serviceSupervisorYesterdayEadtVariance
            }

            if (serviceSupervisorYesterdayEadtActual.isEmpty()) {
                service_eadt_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_actual_supervisor_yesterday.text = serviceSupervisorYesterdayEadtActual
            }

        }

        if (service?.eADT?.status != null && serviceSupervisorYesterdayEadtActual.isNotEmpty()) {
            when {
                service.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_eadt_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_eadt_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.red))

                }
                service.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_eadt_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_eadt_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_eadt_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_eadt_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayExtremeServiceViewForSupervisorYesterday(
            extremeDelivery: SupervisorYesterdayLevelOneQuery.ExtremeDelivery?

    ) {
        if (extremeDelivery?.displayName != null) {
            extreme_delivery_display_supervisor_yesterday.text = extremeDelivery.displayName
        }
        val serviceExtremeSupervisorYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.goal?.amount,
                extremeDelivery?.goal?.percentage,
                extremeDelivery?.goal?.value
        )
        val serviceExtremeSupervisorYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.variance?.amount,
                extremeDelivery?.variance?.percentage,
                extremeDelivery?.variance?.value
        )
        val serviceExtremeSupervisorYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.actual?.amount,
                extremeDelivery?.actual?.percentage,
                extremeDelivery?.actual?.value
        )

        if (serviceExtremeSupervisorYesterdayGoal.isEmpty() && serviceExtremeSupervisorYesterdayVariance.isEmpty() && serviceExtremeSupervisorYesterdayActual.isEmpty()) {

            serviceExtreme_error_supervisor_yesterday_kpi.visibility = View.VISIBLE
            val paramSupervisorYesterdayExtremeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramSupervisorYesterdayExtremeError.weight = 2.0f
            extreme_delivery_display_supervisor_yesterday.layoutParams = paramSupervisorYesterdayExtremeError

            service_extreme_goal_supervisor_yesterday.visibility = View.GONE
            service_extreme_variance_supervisor_yesterday.visibility = View.GONE
            service_extreme_actual_supervisor_yesterday.visibility = View.GONE
        } else if (serviceExtremeSupervisorYesterdayGoal.isNotEmpty() && serviceExtremeSupervisorYesterdayVariance.isNotEmpty() && serviceExtremeSupervisorYesterdayActual.isNotEmpty()) {

            serviceExtreme_error_supervisor_yesterday_kpi.visibility = View.GONE
            service_extreme_goal_supervisor_yesterday.visibility = View.VISIBLE
            service_extreme_variance_supervisor_yesterday.visibility = View.VISIBLE
            service_extreme_actual_supervisor_yesterday.visibility = View.VISIBLE

            service_extreme_goal_supervisor_yesterday.text = serviceExtremeSupervisorYesterdayGoal
            service_extreme_variance_supervisor_yesterday.text = serviceExtremeSupervisorYesterdayVariance
            service_extreme_actual_supervisor_yesterday.text = serviceExtremeSupervisorYesterdayActual
        } else {

            serviceExtreme_error_supervisor_yesterday_kpi.visibility = View.GONE
            service_extreme_goal_supervisor_yesterday.visibility = View.VISIBLE
            service_extreme_variance_supervisor_yesterday.visibility = View.VISIBLE
            service_extreme_actual_supervisor_yesterday.visibility = View.VISIBLE

            if (serviceExtremeSupervisorYesterdayGoal.isEmpty()) {
                service_extreme_goal_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_goal_supervisor_yesterday.text = serviceExtremeSupervisorYesterdayGoal
            }

            if (serviceExtremeSupervisorYesterdayVariance.isEmpty()) {
                service_extreme_variance_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_variance_supervisor_yesterday.text = serviceExtremeSupervisorYesterdayVariance
            }

            if (serviceExtremeSupervisorYesterdayActual.isEmpty()) {
                service_extreme_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_actual_supervisor_yesterday.text = serviceExtremeSupervisorYesterdayActual
            }
        }


        if (extremeDelivery?.status != null && serviceExtremeSupervisorYesterdayActual.isNotEmpty()) {
            when {
                extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_extreme_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.red))
                }
                extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_extreme_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_extreme_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    service_extreme_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_extreme_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }
    }

    fun displaySingleServiceViewForSupervisorYesterday(singles: SupervisorYesterdayLevelOneQuery.Singles?) {
        if (singles?.displayName != null) {
            single_display_supervisor_yesterday.text = singles.displayName
        }

        val serviceSinglesSupervisorYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.goal?.amount,
                singles?.goal?.percentage,
                singles?.goal?.value
        )
        val serviceSinglesSupervisorYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.variance?.amount,
                singles?.variance?.percentage,
                singles?.variance?.value
        )
        val serviceSinglesSupervisorYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.actual?.amount,
                singles?.actual?.percentage,
                singles?.actual?.value
        )

        if (serviceSinglesSupervisorYesterdayGoal.isEmpty() && serviceSinglesSupervisorYesterdayVariance.isEmpty() && serviceSinglesSupervisorYesterdayActual.isEmpty()) {

            serviceSingles_error_supervisor_yesterday_kpi.visibility = View.VISIBLE
            val paramSupervisorYesterdaySingleError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramSupervisorYesterdaySingleError.weight = 2.0f
            single_display_supervisor_yesterday.layoutParams = paramSupervisorYesterdaySingleError


            service_singles_goal_supervisor_yesterday.visibility = View.GONE
            service_singles_variance_supervisor_yesterday.visibility = View.GONE
            service_singles_actual_supervisor_yesterday.visibility = View.GONE
        } else if (serviceSinglesSupervisorYesterdayGoal.isNotEmpty() && serviceSinglesSupervisorYesterdayVariance.isNotEmpty() && serviceSinglesSupervisorYesterdayActual.isNotEmpty()) {

            serviceSingles_error_supervisor_yesterday_kpi.visibility = View.GONE
            service_singles_goal_supervisor_yesterday.visibility = View.VISIBLE
            service_singles_variance_supervisor_yesterday.visibility = View.VISIBLE
            service_singles_actual_supervisor_yesterday.visibility = View.VISIBLE

            service_singles_goal_supervisor_yesterday.text = serviceSinglesSupervisorYesterdayGoal
            service_singles_variance_supervisor_yesterday.text = serviceSinglesSupervisorYesterdayVariance
            service_singles_actual_supervisor_yesterday.text = serviceSinglesSupervisorYesterdayActual
        } else {

            serviceSingles_error_supervisor_yesterday_kpi.visibility = View.GONE
            service_singles_goal_supervisor_yesterday.visibility = View.VISIBLE
            service_singles_variance_supervisor_yesterday.visibility = View.VISIBLE
            service_singles_actual_supervisor_yesterday.visibility = View.VISIBLE

            if (serviceSinglesSupervisorYesterdayGoal.isEmpty()) {
                service_singles_goal_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_goal_supervisor_yesterday.text = serviceSinglesSupervisorYesterdayGoal
            }

            if (serviceSinglesSupervisorYesterdayVariance.isEmpty()) {
                service_singles_variance_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_variance_supervisor_yesterday.text = serviceSinglesSupervisorYesterdayVariance
            }

            if (serviceSinglesSupervisorYesterdayActual.isEmpty()) {
                service_singles_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_actual_supervisor_yesterday.text = serviceSinglesSupervisorYesterdayActual
            }
        }
        if (singles?.status != null && serviceSinglesSupervisorYesterdayActual.isNotEmpty()) {
            when {
                singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_singles_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.red))

                }
                singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_singles_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_singles_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_singles_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_singles_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayCashViewForSupervisorYesterday(cash: SupervisorYesterdayLevelOneQuery.Cash?) {
        if (cash?.displayName != null) {
            cash_display_supervisor_yesterday.text = cash.displayName
        }

        val cashSupervisorYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.goal?.amount,
                cash?.goal?.percentage,
                cash?.goal?.value
        )
        val cashSupervisorYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.variance?.amount,
                cash?.variance?.percentage,
                cash?.variance?.value
        )
        val cashSupervisorYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.actual?.amount,
                cash?.actual?.percentage,
                cash?.actual?.value
        )

        if (cashSupervisorYesterdayGoal.isEmpty() && cashSupervisorYesterdayVariance.isEmpty() && cashSupervisorYesterdayActual.isEmpty()) {

            cash_error_supervisor_yesterday_kpi.visibility = View.VISIBLE

            val paramSupervisorYesterdayCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramSupervisorYesterdayCashError.weight = 2.0f
            cash_display_supervisor_yesterday.layoutParams = paramSupervisorYesterdayCashError

            cash_goal_supervisor_yesterday.visibility = View.GONE
            cash_variance_supervisor_yesterday.visibility = View.GONE
            cash_actual_supervisor_yesterday.visibility = View.GONE
            cash_parent_img_supervisor_yesterday.visibility = View.GONE
            cash_parent_layout_supervisor_yesterday.isClickable = false
        } else if (cashSupervisorYesterdayGoal.isNotEmpty() && cashSupervisorYesterdayVariance.isNotEmpty() && cashSupervisorYesterdayActual.isNotEmpty()) {

            cash_error_supervisor_yesterday_kpi.visibility = View.GONE

            cash_goal_supervisor_yesterday.visibility = View.VISIBLE
            cash_variance_supervisor_yesterday.visibility = View.VISIBLE
            cash_actual_supervisor_yesterday.visibility = View.VISIBLE

            cash_goal_supervisor_yesterday.text = cashSupervisorYesterdayGoal
            cash_variance_supervisor_yesterday.text = cashSupervisorYesterdayVariance
            cash_actual_supervisor_yesterday.text = cashSupervisorYesterdayActual
        } else {

            cash_error_supervisor_yesterday_kpi.visibility = View.GONE
            cash_goal_supervisor_yesterday.visibility = View.VISIBLE
            cash_variance_supervisor_yesterday.visibility = View.VISIBLE
            cash_actual_supervisor_yesterday.visibility = View.VISIBLE

            if (cashSupervisorYesterdayGoal.isEmpty()) {
                cash_goal_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_goal_supervisor_yesterday.text = cashSupervisorYesterdayGoal
            }

            if (cashSupervisorYesterdayVariance.isEmpty()) {
                cash_variance_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_variance_supervisor_yesterday.text = cashSupervisorYesterdayVariance
            }

            if (cashSupervisorYesterdayActual.isEmpty()) {
                cash_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_actual_supervisor_yesterday.text = cashSupervisorYesterdayActual
            }

        }

        if (cash?.status != null && cashSupervisorYesterdayActual.isNotEmpty()) {
            when {
                cash.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.red_circle, 0)
                    cash_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.red))
                }
                cash.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    cash_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    cash_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    cash_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }
    }

    fun displayOERViewForSupervisorYesterday(oerStart: SupervisorYesterdayLevelOneQuery.OerStart?) {
        if (oerStart?.displayName != null) {
            oer_display_supervisor_yesterday.text = oerStart.displayName
        }

        val oerSupervisorYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.goal?.amount,
                oerStart?.goal?.percentage,
                oerStart?.goal?.value
        )
        val oerSupervisorYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.variance?.amount,
                oerStart?.variance?.percentage,
                oerStart?.variance?.value
        )
        val oerSupervisorYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.actual?.amount,
                oerStart?.actual?.percentage,
                oerStart?.actual?.value
        )

        if (oerSupervisorYesterdayGoal.isEmpty() && oerSupervisorYesterdayVariance.isEmpty() && oerSupervisorYesterdayActual.isEmpty()) {

            oer_error_supervisor_yesterday_kpi.visibility = View.VISIBLE
            val paramSupervisorYesterdayOERError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramSupervisorYesterdayOERError.weight = 2.0f
            oer_display_supervisor_yesterday.layoutParams = paramSupervisorYesterdayOERError

            oer_goal_supervisor_yesterday.visibility = View.GONE
            oer_variance_supervisor_yesterday.visibility = View.GONE
            oer_actual_supervisor_yesterday.visibility = View.GONE
            oer_parent_img_supervisor_yesterday.visibility = View.GONE
            oer_parent_layout_supervisor_yesterday.isClickable = false
        } else if (oerSupervisorYesterdayGoal.isNotEmpty() && oerSupervisorYesterdayVariance.isNotEmpty() && oerSupervisorYesterdayActual.isNotEmpty()) {
            oer_error_supervisor_yesterday_kpi.visibility = View.GONE
            oer_goal_supervisor_yesterday.visibility = View.VISIBLE
            oer_variance_supervisor_yesterday.visibility = View.VISIBLE
            oer_actual_supervisor_yesterday.visibility = View.VISIBLE

            oer_goal_supervisor_yesterday.text = oerSupervisorYesterdayGoal
            oer_variance_supervisor_yesterday.text = oerSupervisorYesterdayVariance
            oer_actual_supervisor_yesterday.text = oerSupervisorYesterdayActual
        } else {

            oer_error_supervisor_yesterday_kpi.visibility = View.GONE
            oer_goal_supervisor_yesterday.visibility = View.VISIBLE
            oer_variance_supervisor_yesterday.visibility = View.VISIBLE
            oer_actual_supervisor_yesterday.visibility = View.VISIBLE

            if (oerSupervisorYesterdayGoal.isEmpty()) {
                oer_goal_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_goal_supervisor_yesterday.text = oerSupervisorYesterdayGoal
            }

            if (oerSupervisorYesterdayVariance.isEmpty()) {
                oer_variance_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_variance_supervisor_yesterday.text = oerSupervisorYesterdayVariance
            }

            if (oerSupervisorYesterdayActual.isEmpty()) {
                oer_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_actual_supervisor_yesterday.text = oerSupervisorYesterdayActual
            }

        }

        if (oerStart?.status != null && oerSupervisorYesterdayActual.isNotEmpty()) {
            when {
                oerStart.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oer_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    oer_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.red))

                }
                oerStart.status.toString() == resources.getString(R.string.under_limit) -> {
                    oer_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    oer_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    oer_actual_supervisor_yesterday.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    oer_actual_supervisor_yesterday.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }

    }


    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.aws_parent_layout_supervisor_yesterday -> {

                rcv_food_supervisor_yesterday.visibility = View.GONE
                rcv_labour_supervisor_yesterday.visibility = View.GONE
                rcv_service_supervisor_yesterday.visibility = View.GONE
                rcv_oer_supervisor_yesterday.visibility = View.GONE
                rcv_cash_supervisor_yesterday.visibility = View.GONE

                food_text_overview_supervisor_yesterday.visibility = View.GONE
                labour_text_overview_supervisor_yesterday.visibility = View.GONE
                service_text_overview_supervisor_yesterday.visibility = View.GONE
                oer_text_overview_supervisor_yesterday.visibility = View.GONE
                cash_text_overview_supervisor_yesterday.visibility = View.GONE

                food_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_sales_supervisor_yesterday.visibility == View.VISIBLE) {
                    rcv_sales_supervisor_yesterday.visibility = View.GONE
                    aws_text_overview_supervisor_yesterday.visibility = View.GONE
                    aws_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_sales_supervisor_yesterday.visibility = View.VISIBLE
                    aws_text_overview_supervisor_yesterday.visibility = View.VISIBLE
                    aws_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_up)
                }
                callSupervisorYesterdayLevelTwoQuery(getString(R.string.awus_text), rcv_sales_supervisor_yesterday)
            }
            R.id.food_parent_layout_supervisor_yesterday -> {
                rcv_sales_supervisor_yesterday.visibility = View.GONE
                rcv_service_supervisor_yesterday.visibility = View.GONE
                rcv_oer_supervisor_yesterday.visibility = View.GONE
                rcv_cash_supervisor_yesterday.visibility = View.GONE
                rcv_labour_supervisor_yesterday.visibility = View.GONE

                aws_text_overview_supervisor_yesterday.visibility = View.GONE
                labour_text_overview_supervisor_yesterday.visibility = View.GONE
                service_text_overview_supervisor_yesterday.visibility = View.GONE
                oer_text_overview_supervisor_yesterday.visibility = View.GONE
                cash_text_overview_supervisor_yesterday.visibility = View.GONE

                aws_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_food_supervisor_yesterday.visibility == View.VISIBLE) {
                    rcv_food_supervisor_yesterday.visibility = View.GONE
                    food_text_overview_supervisor_yesterday.visibility = View.GONE
                    food_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_food_supervisor_yesterday.visibility = View.VISIBLE
                    food_text_overview_supervisor_yesterday.visibility = View.VISIBLE
                    food_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callSupervisorYesterdayLevelTwoQuery(getString(R.string.food_text), rcv_food_supervisor_yesterday)

            }
            R.id.labour_parent_layout_supervisor_yesterday -> {
                rcv_sales_supervisor_yesterday.visibility = View.GONE
                rcv_food_supervisor_yesterday.visibility = View.GONE
                rcv_service_supervisor_yesterday.visibility = View.GONE
                rcv_oer_supervisor_yesterday.visibility = View.GONE
                rcv_cash_supervisor_yesterday.visibility = View.GONE

                aws_text_overview_supervisor_yesterday.visibility = View.GONE
                food_text_overview_supervisor_yesterday.visibility = View.GONE
                service_text_overview_supervisor_yesterday.visibility = View.GONE
                oer_text_overview_supervisor_yesterday.visibility = View.GONE
                cash_text_overview_supervisor_yesterday.visibility = View.GONE


                aws_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_labour_supervisor_yesterday.visibility == View.VISIBLE) {
                    rcv_labour_supervisor_yesterday.visibility = View.GONE
                    labour_text_overview_supervisor_yesterday.visibility = View.GONE
                    labor_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_labour_supervisor_yesterday.visibility = View.VISIBLE
                    labour_text_overview_supervisor_yesterday.visibility = View.VISIBLE
                    labor_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callSupervisorYesterdayLevelTwoQuery(getString(R.string.labour_text), rcv_labour_supervisor_yesterday)

            }
            R.id.service_parent_layout_supervisor_yesterday -> {
                rcv_sales_supervisor_yesterday.visibility = View.GONE
                rcv_food_supervisor_yesterday.visibility = View.GONE
                rcv_labour_supervisor_yesterday.visibility = View.GONE
                rcv_oer_supervisor_yesterday.visibility = View.GONE
                rcv_cash_supervisor_yesterday.visibility = View.GONE

                aws_text_overview_supervisor_yesterday.visibility = View.GONE
                food_text_overview_supervisor_yesterday.visibility = View.GONE
                labour_text_overview_supervisor_yesterday.visibility = View.GONE
                oer_text_overview_supervisor_yesterday.visibility = View.GONE
                cash_text_overview_supervisor_yesterday.visibility = View.GONE


                aws_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_service_supervisor_yesterday.visibility == View.VISIBLE) {
                    rcv_service_supervisor_yesterday.visibility = View.GONE
                    service_text_overview_supervisor_yesterday.visibility = View.GONE
                    service_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_service_supervisor_yesterday.visibility = View.VISIBLE
                    service_text_overview_supervisor_yesterday.visibility = View.VISIBLE
                    service_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callSupervisorYesterdayLevelTwoQuery(getString(R.string.service_text), rcv_service_supervisor_yesterday)

            }
            R.id.cash_parent_layout_supervisor_yesterday -> {
                rcv_sales_supervisor_yesterday.visibility = View.GONE
                rcv_food_supervisor_yesterday.visibility = View.GONE
                rcv_labour_supervisor_yesterday.visibility = View.GONE
                rcv_service_supervisor_yesterday.visibility = View.GONE
                rcv_oer_supervisor_yesterday.visibility = View.GONE

                aws_text_overview_supervisor_yesterday.visibility = View.GONE
                food_text_overview_supervisor_yesterday.visibility = View.GONE
                labour_text_overview_supervisor_yesterday.visibility = View.GONE
                service_text_overview_supervisor_yesterday.visibility = View.GONE
                oer_text_overview_supervisor_yesterday.visibility = View.GONE


                aws_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_cash_supervisor_yesterday.visibility == View.VISIBLE) {
                    rcv_cash_supervisor_yesterday.visibility = View.GONE
                    cash_text_overview_supervisor_yesterday.visibility = View.GONE
                    cash_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_cash_supervisor_yesterday.visibility = View.VISIBLE
                    cash_text_overview_supervisor_yesterday.visibility = View.VISIBLE
                    cash_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callSupervisorYesterdayLevelTwoQuery(getString(R.string.cash_text), rcv_cash_supervisor_yesterday)

            }
            R.id.oer_parent_layout_supervisor_yesterday -> {
                rcv_sales_supervisor_yesterday.visibility = View.GONE
                rcv_food_supervisor_yesterday.visibility = View.GONE
                rcv_labour_supervisor_yesterday.visibility = View.GONE
                rcv_service_supervisor_yesterday.visibility = View.GONE
                rcv_cash_supervisor_yesterday.visibility = View.GONE

                aws_text_overview_supervisor_yesterday.visibility = View.GONE
                food_text_overview_supervisor_yesterday.visibility = View.GONE
                labour_text_overview_supervisor_yesterday.visibility = View.GONE
                service_text_overview_supervisor_yesterday.visibility = View.GONE
                cash_text_overview_supervisor_yesterday.visibility = View.GONE


                aws_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_oer_supervisor_yesterday.visibility == View.VISIBLE) {
                    rcv_oer_supervisor_yesterday.visibility = View.GONE
                    oer_text_overview_supervisor_yesterday.visibility = View.GONE
                    oer_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_oer_supervisor_yesterday.visibility = View.VISIBLE
                    oer_text_overview_supervisor_yesterday.visibility = View.VISIBLE
                    oer_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callSupervisorYesterdayLevelTwoQuery(getString(R.string.oer_text), rcv_oer_supervisor_yesterday)

            }
            R.id.filter_icon -> {
                openSupervisorYesterdayKpiFilter()
            }
            R.id.filter_parent_linear -> {
                openSupervisorYesterdayKpiFilter()
            }
            R.id.error_filter_parent_linear -> {
                openSupervisorYesterdayKpiFilter()
            }
            R.id.aws_text_overview_supervisor_yesterday -> {
                callSupervisorYesterdayOverViewQuery(getString(R.string.awus_text), "")
            }
            R.id.food_text_overview_supervisor_yesterday -> {
                callSupervisorYesterdayOverViewQuery(getString(R.string.food_text), "")
            }
            R.id.labour_text_overview_supervisor_yesterday -> {
                callSupervisorYesterdayOverViewQuery(getString(R.string.labour_text), "")
            }
            R.id.service_text_overview_supervisor_yesterday -> {
                callSupervisorYesterdayOverViewQuery(getString(R.string.service_text), "")
            }
            R.id.oer_text_overview_supervisor_yesterday -> {
                callSupervisorYesterdayOverViewQuery(getString(R.string.oer_text), "")
            }
            R.id.cash_text_overview_supervisor_yesterday -> {
                callSupervisorYesterdayOverViewQuery(getString(R.string.cash_text), "")
            }
        }
    }

    fun callSupervisorYesterdayLevelTwoQuery(
            actionForSupervisorYesterdayLevel2: String,
            rcvForSupervisorYesterdayLevel2: RecyclerView
    ) {
        val progressDialogSupervisorYesterdayLevel2 = CustomProgressDialog(requireActivity())
        lifecycleScope.launchWhenResumed {
            progressDialogSupervisorYesterdayLevel2.showProgressDialog()
            val storeListForSupervisorYesterdayLevel2:List<String>  = dbHelperSupervisorYesterday.getAllSelectedStoreList(true)
            val superVisorNumberListSupervisorYesterdayLevel2 = dbHelperSupervisorYesterday.getAllSelectedStoreListSupervisor(true)
            Logger.info(
                    SupervisorYesterdayLevelTwoQuery.OPERATION_NAME.name(),
                    "Yesterday Level 2 KPI",
                    mapQueryFilters(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            superVisorNumberListSupervisorYesterdayLevel2,
                            storeListForSupervisorYesterdayLevel2,
                            SupervisorYesterdayLevelTwoQuery.QUERY_DOCUMENT
                    )
            )

            try {
                val responseDoYesterdayKpi =
                        apolloClient(requireContext()).query(
                                SupervisorYesterdayLevelTwoQuery(
                                        superVisorNumberListSupervisorYesterdayLevel2.toInput(),
                                        storeListForSupervisorYesterdayLevel2.toInput()

                                )
                        )
                                .await()

                if (responseDoYesterdayKpi.data?.supervisor != null) {
                    progressDialogSupervisorYesterdayLevel2.dismissProgressDialog()
                    supervisorYesterdayLevelTwo = responseDoYesterdayKpi.data?.supervisor!!

                    supervisorYesterdayLevelTwo.kpis?.individualStores.let {
                        setSupervisorYesterdayExpandableData(
                                actionForSupervisorYesterdayLevel2,
                                rcvForSupervisorYesterdayLevel2
                        )
                    }
                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogSupervisorYesterdayLevel2.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Level 2 KPI")
                        }
                refreshSupervisorYesterdayKpiToken()
                return@launchWhenResumed
            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogSupervisorYesterdayLevel2.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForSupervisorYesterday()
                } else {
                    Logger.error(apolloNetworkException.message.toString(), "Yesterday Level 2 KPI")
                }

            } catch (e: ApolloException) {
                progressDialogSupervisorYesterdayLevel2.dismissProgressDialog()
                Logger.error(e.message.toString(), "Yesterday Level 2 KPI")
                setErrorScreenVisibleStateForSupervisorPeriod(
                        getString(R.string.exception_error_text_title), getString(
                        R.string
                                .exception_error_text_description
                )
                )
            }
        }
    }

    private fun setSupervisorYesterdayExpandableData(
            actionSupervisorYesterdayExpandable: String,
            rcvSupervisorYesterdayExpandable: RecyclerView
    ) {
        checkSupervisorSupervisorYesterdayKpiSize(rcvSupervisorYesterdayExpandable, actionSupervisorYesterdayExpandable)
        rcvSupervisorYesterdayExpandable.layoutManager = LinearLayoutManager(
                activity,
                LinearLayoutManager.VERTICAL,
                false
        )
        val storeDetailSupervisorYesterdayExpandable = supervisorYesterdayLevelTwo.kpis?.individualStores
        val childDataSupervisorYesterdayExpandable = mutableListOf<StoreDetailPojo>()
        storeDetailSupervisorYesterdayExpandable?.forEachIndexed {_, item ->
            when (actionSupervisorYesterdayExpandable) {
                requireActivity().getString(R.string.awus_text) -> {
                    childDataSupervisorYesterdayExpandable.add(
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
                    childDataSupervisorYesterdayExpandable.add(
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
                    childDataSupervisorYesterdayExpandable.add(
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
                    childDataSupervisorYesterdayExpandable.add(
                            StoreDetailPojo(
                                    item?.storeNumber.toString(),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.service?.goal?.amount,
                                            item?.yesterday?.service?.goal?.percentage,
                                            item?.yesterday?.service?.goal?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.service?.variance?.amount,
                                            item?.yesterday?.service?.variance?.percentage,
                                            item?.yesterday?.service?.variance?.value
                                    ),
                                    Validation().checkAmountPercentageValue(
                                            requireActivity(),
                                            item?.yesterday?.service?.actual?.amount,
                                            item?.yesterday?.service?.actual?.percentage,
                                            item?.yesterday?.service?.actual?.value
                                    ),
                                    item?.yesterday?.labor?.status.toString()
                            )
                    )
                }
                requireActivity().getString(R.string.cash_text) -> {
                    childDataSupervisorYesterdayExpandable.add(
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
                    childDataSupervisorYesterdayExpandable.add(
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

        supervisorYesterdayAdapter = SupervisorYesterdayAndPeriodAdapter(
                requireContext(),
                childDataSupervisorYesterdayExpandable,
                actionSupervisorYesterdayExpandable,
                ""
        )
        rcvSupervisorYesterdayExpandable.adapter = supervisorYesterdayAdapter

        supervisorYesterdayAdapter?.setOnSupervisorYesterdayItemClickListener(object :
                                                                                      SupervisorYesterdayAndPeriodAdapter.OnItemClickListener {
            override fun onItemClick(
                    position: Int,
                    storeNumber: String?,
                    action: String
            ) {
                callSupervisorYesterdayOverViewQuery(action, storeNumber!!)
            }

        })
    }

    private fun callSupervisorYesterdayOverViewQuery(
            actionSupervisorYesterday: String,
            storeNumberSupervisorYesterday: String
    ) {
        lifecycleScope.launchWhenResumed {
            val storeNumberListSupervisorYesterday = mutableListOf<String>()
            if (storeNumberSupervisorYesterday.isNotEmpty() || storeNumberSupervisorYesterday.isNotBlank()) {
                storeNumberListSupervisorYesterday.add(storeNumberSupervisorYesterday)
            } else {
                storeNumberListSupervisorYesterday.addAll(dbHelperSupervisorYesterday.getAllSelectedStoreList(true))
            }
            val progressDialogSupervisorYesterday = CustomProgressDialog(requireActivity())
            progressDialogSupervisorYesterday.showProgressDialog()

            try {
                val response =
                        apolloClient(requireContext()).query(
                                SupervisorOverviewYesterdayQuery(
                                        storeNumberListSupervisorYesterday.toInput()
                                )
                        )
                                .await()

                if (response.data?.supervisor != null) {
                    progressDialogSupervisorYesterday.dismissProgressDialog()
                    when (actionSupervisorYesterday) {
                        getString(R.string.awus_text) -> {
                            openSupervisorYesterdayKpiSalesDetail(response.data?.supervisor!!)
                        }
                        getString(R.string.food_text) -> {
                            openSupervisorYesterdayKpiFoodDetail(response.data?.supervisor!!)
                        }
                        getString(R.string.labour_text) -> {
                            openSupervisorYesterdayKpiLabourDetail(response.data?.supervisor!!)
                        }
                        getString(R.string.service_text) -> {
                            openSupervisorYesterdayKpiServiceDetail(response.data?.supervisor!!)
                        }
                        getString(R.string.oer_text) -> {
                            openSupervisorYesterdayKpiOERDetail(response.data?.supervisor!!)
                        }
                        getString(R.string.cash_text) -> {
                            openSupervisorYesterdayKpiCASHDetail(response.data?.supervisor!!)
                        }
                    }
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogSupervisorYesterday.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Overview KPI")
                        }
                refreshSupervisorYesterdayKpiToken()
                return@launchWhenResumed
            } catch (e: ApolloNetworkException) {
                progressDialogSupervisorYesterday.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForSupervisorYesterday()
                }
            } catch (e: ApolloException) {
                progressDialogSupervisorYesterday.dismissProgressDialog()
                Logger.error(e.message.toString(), "Yesterday Overview KPI")
                setErrorScreenVisibleStateForSupervisorPeriod(
                        getString(R.string.exception_error_text_title), getString(
                        R.string
                                .exception_error_text_description
                )
                )
            }


        }
    }

    private fun openSupervisorYesterdayKpiFilter() {
        val intentSupervisorYesterdayKpiFilter = Intent(requireContext(), FilterActivity::class.java)
        startActivity(intentSupervisorYesterdayKpiFilter)
    }

    private fun openSupervisorYesterdayKpiSalesDetail(supervisorYesterdaySalesDetails: SupervisorOverviewYesterdayQuery.Supervisor) {
        val intentSupervisorYesterdaySalesDetails = Intent(requireContext(), AWUSKpiActivity::class.java)
        intentSupervisorYesterdaySalesDetails.putExtra(
                "awus_data",
                gsonSupervisorYesterday.toJson(supervisorYesterdaySalesDetails)
        )
        intentSupervisorYesterdaySalesDetails.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentSupervisorYesterdaySalesDetails)

    }

    private fun openSupervisorYesterdayKpiFoodDetail(supervisorYesterdayFoodDetail: SupervisorOverviewYesterdayQuery.Supervisor) {
        val intentSupervisorYesterdayFoodDetail = Intent(requireContext(), FoodKpiActivity::class.java)
        intentSupervisorYesterdayFoodDetail.putExtra(
                "food_data",
                gsonSupervisorYesterday.toJson(supervisorYesterdayFoodDetail)
        )
        intentSupervisorYesterdayFoodDetail.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentSupervisorYesterdayFoodDetail)
    }

    private fun openSupervisorYesterdayKpiLabourDetail(supervisorYesterdayLabourDetail: SupervisorOverviewYesterdayQuery.Supervisor) {
        val intentSupervisorYesterdayLabourDetail = Intent(requireContext(), LabourKpiActivity::class.java)
        intentSupervisorYesterdayLabourDetail.putExtra(
                "labour_data",
                gsonSupervisorYesterday.toJson(supervisorYesterdayLabourDetail)
        )
        intentSupervisorYesterdayLabourDetail.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentSupervisorYesterdayLabourDetail)
    }

    private fun openSupervisorYesterdayKpiServiceDetail(supervisorYesterdayServiceDetails: SupervisorOverviewYesterdayQuery.Supervisor) {
        val intentSupervisorYesterdayServiceDetails = Intent(requireContext(), ServiceKpiActivity::class.java)
        intentSupervisorYesterdayServiceDetails.putExtra(
                "service_data",
                gsonSupervisorYesterday.toJson(supervisorYesterdayServiceDetails)
        )
        intentSupervisorYesterdayServiceDetails.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentSupervisorYesterdayServiceDetails)
    }

    private fun openSupervisorYesterdayKpiOERDetail(supervisorYesterdayOERDetail: SupervisorOverviewYesterdayQuery.Supervisor) {
        val intentSupervisorYesterdayOERDetail = Intent(requireContext(), OERStartActivity::class.java)
        intentSupervisorYesterdayOERDetail.putExtra("oer_data", gsonSupervisorYesterday.toJson(supervisorYesterdayOERDetail))
        intentSupervisorYesterdayOERDetail.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentSupervisorYesterdayOERDetail)
    }

    private fun openSupervisorYesterdayKpiCASHDetail(supervisorYesterdayCashDetail: SupervisorOverviewYesterdayQuery.Supervisor) {
        val intentSupervisorYesterdayCashDetail = Intent(requireContext(), CashKpiActivity::class.java)
        intentSupervisorYesterdayCashDetail.putExtra(
                "cash_data",
                gsonSupervisorYesterday.toJson(supervisorYesterdayCashDetail)
        )
        intentSupervisorYesterdayCashDetail.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentSupervisorYesterdayCashDetail)
    }

    private fun refreshSupervisorYesterdayKpiToken() {
        refreshTokenViewModelSupervisorYesterday.getRefreshToken()
        refreshTokenViewModelSupervisorYesterday.refreshTokenResponseLiveData.observe(requireActivity(), {
            run {
                when (it.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        callSupervisorYesterdayLevelOneQuery()
                    }
                    Status.UNSUCCESSFUL -> {
                        CommonUtil.navigateToLogin(mainActivity)
                    }
                    Status.ERROR -> {
                    }
                    Status.OFFLINE -> {
                        setInternetErrorScreenVisibleStateForSupervisorYesterday()
                    }
                }
            }
        })
    }

    private fun checkSupervisorSupervisorYesterdayKpiSize(
            rcvSupervisorYesterdayKpi: RecyclerView,
            actionSupervisorYesterday: String
    ) {
        if (supervisorYesterdayLevelTwo.kpis?.individualStores != null && supervisorYesterdayLevelTwo.kpis?.individualStores!!.isNotEmpty()) {
            rcvSupervisorYesterdayKpi.visibility = View.VISIBLE
                when (actionSupervisorYesterday) {
                    getString(R.string.awus_text) -> {
                        aws_text_overview_supervisor_yesterday.visibility = View.VISIBLE
                        aws_parent_img_supervisor_yesterday.visibility = View.VISIBLE
                    }
                    getString(R.string.food_text) -> {
                        food_text_overview_supervisor_yesterday.visibility = View.VISIBLE
                        food_parent_img_supervisor_yesterday.visibility = View.VISIBLE
                    }
                    getString(R.string.labour_text) -> {
                        labour_text_overview_supervisor_yesterday.visibility = View.VISIBLE
                        labor_parent_img_supervisor_yesterday.visibility = View.VISIBLE
                    }
                    getString(R.string.service_text) -> {
                        service_text_overview_supervisor_yesterday.visibility = View.VISIBLE
                        service_parent_img_supervisor_yesterday.visibility = View.VISIBLE
                    }
                    getString(R.string.oer_text) -> {
                        oer_text_overview_supervisor_yesterday.visibility = View.VISIBLE
                        oer_parent_img_supervisor_yesterday.visibility = View.VISIBLE
                    }
                    getString(R.string.cash_text) -> {
                        cash_text_overview_supervisor_yesterday.visibility = View.VISIBLE
                        aws_parent_img_supervisor_yesterday.visibility = View.VISIBLE
                        cash_parent_img_supervisor_yesterday.visibility = View.VISIBLE
                    }
                }

        } else {
            hideSupervisorYesterdayOverviewText(actionSupervisorYesterday)
            rcvSupervisorYesterdayKpi.visibility = View.GONE
        }
    }

    private fun hideSupervisorYesterdayOverviewText(actionSupervisorStr: String) {
        when (actionSupervisorStr) {
            getString(R.string.awus_text) -> {
                aws_text_overview_supervisor_yesterday.visibility = View.GONE
                aws_parent_img_supervisor_yesterday.visibility = View.GONE
            }
            getString(R.string.food_text) -> {
                food_text_overview_supervisor_yesterday.visibility = View.GONE
                food_parent_img_supervisor_yesterday.visibility = View.GONE

            }
            getString(R.string.labour_text) -> {
                labour_text_overview_supervisor_yesterday.visibility = View.GONE
                labor_parent_img_supervisor_yesterday.visibility = View.GONE

            }
            getString(R.string.service_text) -> {
                service_text_overview_supervisor_yesterday.visibility = View.GONE
                service_parent_img_supervisor_yesterday.visibility = View.GONE
            }
            getString(R.string.oer_text) -> {
                oer_text_overview_supervisor_yesterday.visibility = View.GONE
                oer_parent_img_supervisor_yesterday.visibility = View.GONE

            }
            getString(R.string.cash_text) -> {
                cash_text_overview_supervisor_yesterday.visibility = View.GONE
                cash_parent_img_supervisor_yesterday.visibility = View.GONE

            }
        }
    }


    fun setErrorScreenVisibleStateForSupervisorPeriod(
            title: String,
            description: String
    ) {
        supervisor_yesterday_data_error_layout.visibility = View.VISIBLE
        supervisor_yesterday_data_error_layout.exception_text_title.text = title
        supervisor_yesterday_data_error_layout.exception_text_description.text = description
        common_header_supervisor_yesterday.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)
        common_header_supervisor_yesterday.sales_header_error_image.visibility = View.VISIBLE
        common_header_supervisor_yesterday.error_filter_parent_linear.visibility = View.VISIBLE
        supervisor_yesterday_missing_data_error_layout.visibility = View.GONE
        setHeaderViewsVisibleStateForSupervisorYesterday()
        setCalendarViewVisibleStateForSupervisorYesterday()
        hideStoreFilterVisibilityStateForSupervisorYesterday()
    }

    fun setInternetErrorScreenVisibleStateForSupervisorYesterday() {
        supervisor_yesterday_no_internet_error_layout.visibility = View.VISIBLE
        common_header_supervisor_yesterday.sales_header_error_image.visibility = View.VISIBLE
        common_header_supervisor_yesterday.sales_header_error_image.setImageResource(R.drawable.ic_internet_error)
        supervisor_yesterday_missing_data_error_layout.visibility = View.GONE
        common_header_supervisor_yesterday.error_filter_parent_linear.visibility = View.GONE
        supervisor_yesterday_data_error_layout.visibility = View.GONE
        setCalendarViewVisibleStateForSupervisorYesterday()
        setHeaderViewsVisibleStateForSupervisorYesterday()
        showStoreFilterVisibilityStateForSupervisorYesterday()
    }

    fun setHeaderViewsVisibleStateForSupervisorYesterday() {
        supervisor_yesterday_header.visibility = View.GONE
        supervisor_yesterday_v1.visibility = View.GONE
        supervisor_yesterday_layout.visibility = View.INVISIBLE
        common_header_supervisor_yesterday.total_sales_common_header.visibility = View.GONE
        common_header_supervisor_yesterday.sales_text_common_header.visibility = View.GONE

    }

    fun hideStoreFilterVisibilityStateForSupervisorYesterday(){
        common_header_supervisor_yesterday.filter_parent_linear.visibility = View.GONE
    }
    fun showStoreFilterVisibilityStateForSupervisorYesterday(){
        common_header_supervisor_yesterday.filter_parent_linear.visibility = View.VISIBLE
    }

    fun setCalendarViewVisibleStateForSupervisorYesterday() {
        common_calendar_supervisor_yesterday.visibility = View.GONE
    }

    fun setMissingDataViewVisibleStateForSupervisorYesterday(
            missingDataTitle: String,
            missingDataDescription: String
    ) {
        supervisor_yesterday_missing_data_error_layout.visibility = View.VISIBLE
        supervisor_yesterday_missing_data_error_layout.header_data_title.text = missingDataTitle
        supervisor_yesterday_missing_data_error_layout.header_data_description.text = missingDataDescription

    }

    fun hideErrorScreenVisibleStateForSupervisorYesterday(){
        supervisor_yesterday_no_internet_error_layout.visibility = View.GONE
        supervisor_yesterday_data_error_layout.visibility = View.GONE
        common_header_supervisor_yesterday.sales_header_error_image.visibility = View.GONE
        common_header_supervisor_yesterday.error_filter_parent_linear.visibility = View.GONE

        supervisor_yesterday_header.visibility = View.VISIBLE
        supervisor_yesterday_v1.visibility = View.VISIBLE
        supervisor_yesterday_layout.visibility = View.VISIBLE

        common_header_supervisor_yesterday.filter_parent_linear.visibility = View.VISIBLE
        common_header_supervisor_yesterday.total_sales_common_header.visibility = View.VISIBLE
        common_header_supervisor_yesterday.sales_text_common_header.visibility = View.VISIBLE

        common_calendar_supervisor_yesterday.visibility = View.VISIBLE
    }

    fun collapseExpendedListVisibilityForSupervisorYesterday(){
        if (rcv_sales_supervisor_yesterday.visibility == View.VISIBLE) {
            rcv_sales_supervisor_yesterday.visibility = View.GONE
            aws_text_overview_supervisor_yesterday.visibility = View.GONE
            aws_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_food_supervisor_yesterday.visibility == View.VISIBLE) {
            rcv_food_supervisor_yesterday.visibility = View.GONE
            food_text_overview_supervisor_yesterday.visibility = View.GONE
            food_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_labour_supervisor_yesterday.visibility == View.VISIBLE) {
            rcv_labour_supervisor_yesterday.visibility = View.GONE
            labour_text_overview_supervisor_yesterday.visibility = View.GONE
            labor_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_service_supervisor_yesterday.visibility == View.VISIBLE) {
            rcv_service_supervisor_yesterday.visibility = View.GONE
            service_text_overview_supervisor_yesterday.visibility = View.GONE
            service_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_cash_supervisor_yesterday.visibility == View.VISIBLE) {
            rcv_cash_supervisor_yesterday.visibility = View.GONE
            cash_text_overview_supervisor_yesterday.visibility = View.GONE
            cash_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_oer_supervisor_yesterday.visibility == View.VISIBLE) {
            rcv_oer_supervisor_yesterday.visibility = View.GONE
            oer_text_overview_supervisor_yesterday.visibility = View.GONE
            oer_parent_img_supervisor_yesterday.setImageResource(R.drawable.ic_icon_chevron_down)
        }
    }


}
