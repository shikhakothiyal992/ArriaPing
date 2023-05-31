package com.arria.ping.ui.kpi.ceo.view

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
import com.arria.ping.kpi.ceo.CEOPeriodRangeLevelOneQuery
import com.arria.ping.kpi.ceo.CEOPeriodRangeLevelTwoQuery
import com.arria.ping.log.Logger
import com.arria.ping.log.data.QueryData
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.model.StoreDetailPojo
import com.arria.ping.ui.filter.FilterActivity
import com.arria.ping.ui.generalview.MainActivity
import com.arria.ping.ui.kpi.ceo.adapter.CustomExpandableListAdapterPeriodCEO
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
import kotlinx.android.synthetic.main.common_header.view.*
import kotlinx.android.synthetic.main.common_header_ceo.*
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.common_header_ceo.view.sales_text_common_header
import kotlinx.android.synthetic.main.common_header_ceo.view.total_sales_common_header
import kotlinx.android.synthetic.main.data_error_layout.view.*
import kotlinx.android.synthetic.main.do_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.expandable_list_supervisor.view.*
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class CEOPeriodKpiRangeFragment : androidx.fragment.app.Fragment(), View.OnClickListener {
    lateinit var supervisorLevel3CEOPeriodKpiRange: CEOPeriodLVLThreeQuery.Ceo
    private var lastExpandedPositionCEOPeriodKpiRange = -1
    private val expandableListDetailCEOPeriodKpiRange = HashMap<String, List<StoreDetailPojo>>()
    private var expandableListAdapterCEOPeriodKpiRange: CustomExpandableListAdapterPeriodCEO? = null
    lateinit var dbHelperCEOPeriodKpiRange: DatabaseHelperImpl
    private val gsonCEOPeriodKpiRange = Gson()
    private var formattedCEOPeriodKpiStartDateValue = ""
    private var formattedCEOPeriodKpiEndDateValue = ""

    lateinit var ceoPeriodRangeLevelOne: CEOPeriodRangeLevelOneQuery.Ceo
    lateinit var ceoPeriodRangeLevelTwo: CEOPeriodRangeLevelTwoQuery.Ceo


    private val refreshTokenViewModelCeoPeriodRange by viewModels<RefreshTokenViewModel>()

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
        return inflater.inflate(R.layout.ceo_period_range_fragment_kpi, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        dbHelperCEOPeriodKpiRange = DatabaseHelperImpl(DatabaseBuilder.getInstance(requireActivity()))

        initPeriodCEO()
        formattedCEOPeriodKpiStartDateValue = StorePrefData.startDateValue
        formattedCEOPeriodKpiEndDateValue = StorePrefData.endDateValue

        if (StorePrefData.filterDate.isNotEmpty()) {
            setStoreFilterViewForCEOPeriod(StorePrefData.filterDate)
        }

        if (networkHelper.isNetworkConnected()) {
            callMissingDataQueryForCeoPeriodRangeQuery()
            callCEOPeriodRangeLevelOneQuery()

        } else {
            setInternetErrorScreenVisibleStateForCeoPeriod()
        }

        ceo_period_swipe_refresh_layout.setOnRefreshListener{
            Logger.info("Pull down to refresh CEO Period Store Data", "Period KPI")
            callMissingDataQueryForCeoPeriodRangeQuery()
            callCEOPeriodRangeLevelOneQuery()
            collapseExpendedListVisibilityForCEOPeriod()
            ceo_period_swipe_refresh_layout.isRefreshing = false
        }
    }


    private fun initPeriodCEO() {

        sales_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        food_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        labor_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        service_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        oer_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        cash_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

        sales_parent_layout_ceo_period_range_kpi.setOnClickListener(this)
        food_parent_layout_ceo_period_range_kpi.setOnClickListener(this)
        labor_parent_layout_ceo_period_range_kpi.setOnClickListener(this)
        service_parent_layout_ceo_period_range_kpi.setOnClickListener(this)
        cash_parent_layout_ceo_period_range_kpi.setOnClickListener(this)
        oer_parent_layout_ceo_period_range_kpi.setOnClickListener(this)

        filter_icon.setOnClickListener(this)
        filter_parent_linear.setOnClickListener(this)
        error_filter_parent_linear.setOnClickListener(this)

        sales_text_overview_ceo_period_range_kpi.setOnClickListener(this)
        food_text_overview_ceo_period_range_kpi.setOnClickListener(this)
        labour_text_overview_ceo_period_range_kpi.setOnClickListener(this)
        service_text_overview_ceo_period_range_kpi.setOnClickListener(this)
        cash_text_overview_ceo_period_range_kpi.setOnClickListener(this)
        oer_text_overview_ceo_period_range_kpi.setOnClickListener(this)

        Validation().setCustomCalendar(common_calendar_ceo_range.square_day)
    }

    private fun callMissingDataQueryForCeoPeriodRangeQuery() {
        val progressDialogCEOPeriodKpiRange = CustomProgressDialog(requireActivity())
        progressDialogCEOPeriodKpiRange.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodeCEOPeriodKpiRange = dbHelperCEOPeriodKpiRange.getAllSelectedAreaList(true)
            val stateCodeCEOPeriodKpiRange = dbHelperCEOPeriodKpiRange.getAllSelectedStoreListState(true)
            val supervisorNumberCEOPeriodKpiRange = dbHelperCEOPeriodKpiRange.getAllSelectedStoreListSupervisor(true)
            val storeNumberCEOPeriodKpiRange = dbHelperCEOPeriodKpiRange.getAllSelectedStoreList(true)
            Logger.info(
                    MissingDataQuery.OPERATION_NAME.name(), "Period Range Missing Data",
                    mapQueryFilters(
                            areaCodeCEOPeriodKpiRange,
                            stateCodeCEOPeriodKpiRange,
                            supervisorNumberCEOPeriodKpiRange,
                            storeNumberCEOPeriodKpiRange,
                            MissingDataQuery.QUERY_DOCUMENT
                    )
            )
            try {
                val responseMissingDataCEOPeriodKpiRange =
                        apolloClient(requireContext()).query(
                                MissingDataQuery(
                                        areaCodeCEOPeriodKpiRange.toInput(),
                                        stateCodeCEOPeriodKpiRange.toInput(),
                                        supervisorNumberCEOPeriodKpiRange.toInput(),
                                        storeNumberCEOPeriodKpiRange.toInput(),
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                                .toInput(),
                                        formattedCEOPeriodKpiStartDateValue.toInput(),
                                        formattedCEOPeriodKpiEndDateValue.toInput(),
                                )
                        )
                                .await()

                if (responseMissingDataCEOPeriodKpiRange.data?.missingData != null) {
                    progressDialogCEOPeriodKpiRange.dismissProgressDialog()
                    setMissingDataViewVisibleStateForCeoPeriod(
                            responseMissingDataCEOPeriodKpiRange.data?.missingData!!
                                    .header.toString(),
                            responseMissingDataCEOPeriodKpiRange.data?.missingData!!.message.toString()
                    )
                } else {
                    progressDialogCEOPeriodKpiRange.dismissProgressDialog()
                    ceo_period_range_missing_data_error_layout.visibility = View.GONE
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogCEOPeriodKpiRange.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Missing Data")
                        }

            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogCEOPeriodKpiRange.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForCeoPeriod()
                }
            } catch (e: ApolloException) {
                progressDialogCEOPeriodKpiRange.dismissProgressDialog()
                Logger.error(e.message.toString(), "Period Range Missing Data")
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callCEOPeriodRangeLevelOneQuery() {

        val progressDialogCEOPeriodKpiRange = CustomProgressDialog(requireActivity())
        progressDialogCEOPeriodKpiRange.showProgressDialog()

        lifecycleScope.launchWhenResumed {
            if (networkHelper.isNetworkConnected()) {
                hideErrorScreenVisibleStateForCEOPeriod()
            }
            val areaCodeCEOPeriodKpiRange = dbHelperCEOPeriodKpiRange.getAllSelectedAreaList(true)
            val stateCodeCEOPeriodKpiRange = dbHelperCEOPeriodKpiRange.getAllSelectedStoreListState(true)
            val supervisorNumberCEOPeriodKpiRange = dbHelperCEOPeriodKpiRange.getAllSelectedStoreListSupervisor(true)
            val storeNumberCEOPeriodKpiRange = dbHelperCEOPeriodKpiRange.getAllSelectedStoreList(true)


            Logger.info(
                    CEOPeriodRangeLevelOneQuery.OPERATION_NAME.name(), "Period Range Level 1",
                    mapQueryFilters(
                            QueryData(
                                    areaCodeCEOPeriodKpiRange,
                                    stateCodeCEOPeriodKpiRange,
                                    supervisorNumberCEOPeriodKpiRange,
                                    storeNumberCEOPeriodKpiRange,
                                    formattedCEOPeriodKpiEndDateValue,
                                    formattedCEOPeriodKpiStartDateValue,
                                    StorePrefData.filterType,
                                    CEOPeriodRangeLevelOneQuery.QUERY_DOCUMENT
                            )
                    )
            )
            try {

                val responseCEOPeriodRangeLevelOne =
                        apolloClient(requireContext()).query(
                                CEOPeriodRangeLevelOneQuery(
                                        areaCodeCEOPeriodKpiRange.toInput(),
                                        stateCodeCEOPeriodKpiRange.toInput(),
                                        supervisorNumberCEOPeriodKpiRange.toInput(),
                                        storeNumberCEOPeriodKpiRange.toInput(),
                                        formattedCEOPeriodKpiStartDateValue.toInput(),
                                        formattedCEOPeriodKpiEndDateValue.toInput(),
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                                .toInput()
                                )
                        )
                                .await()

                if (responseCEOPeriodRangeLevelOne.data?.ceo != null) {
                    progressDialogCEOPeriodKpiRange.dismissProgressDialog()
                    ceoPeriodRangeLevelOne = responseCEOPeriodRangeLevelOne.data?.ceo!!

                    if (ceoPeriodRangeLevelOne.kpis?.supervisors?.stores?.period != null) {
                        setCEOPeriodLevelOneStoreValues(ceoPeriodRangeLevelOne.kpis?.supervisors?.stores?.period)
                    } else {
                        setErrorScreenVisibleStateForCeoPeriod(
                                getString(R.string.error_text_title),
                                getString(R.string.error_text_description)
                        )
                    }
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogCEOPeriodKpiRange.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Level 1 KPI")
                        }
                refreshCEOPeriodKpiToken()
                return@launchWhenResumed

            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogCEOPeriodKpiRange.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForCeoPeriod()
                }
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Period Range Level 1 KPI")
                progressDialogCEOPeriodKpiRange.dismissProgressDialog()
                setErrorScreenVisibleStateForCeoPeriod(
                        getString(R.string.exception_error_text_title),
                        getString(R.string.exception_error_text_description)
                )
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setCEOPeriodLevelOneStoreValues(ceoPeriod: CEOPeriodRangeLevelOneQuery.Period?) {

        val strCEORangeSelectedDate: String? =
                ceoPeriod?.periodFrom?.let {
                    ceoPeriod.periodTo?.let {it1 ->
                        EnumMapperUtil.getSelectedDate(
                                it,
                                it1, EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                        )
                    }
                }
        if (strCEORangeSelectedDate != null) {
            StorePrefData.filterDate = strCEORangeSelectedDate
            setStoreFilterViewForCEOPeriod(StorePrefData.filterDate)
        }


        val ceoPeriodSalesValue = Validation().checkAmountPercentageValue(
                requireActivity(),
                ceoPeriod?.sales?.actual?.amount,
                ceoPeriod?.sales?.actual?.percentage,
                ceoPeriod?.sales?.actual?.value,
        )
        if(ceoPeriodSalesValue.isEmpty()) {
            hideVisibilityStateOfSalesDataForCEOPeriod()
        }else{
            showVisibilityStateOfSalesDataForCEOPeriod(ceoPeriodSalesValue)
        }

        displaySalesViewForCEOPeriodRange(ceoPeriod?.sales)
        displayFoodViewForCEOPeriodRange(ceoPeriod?.food)
        displayLaborViewForCEOPeriodRange(ceoPeriod?.labor)
        displayEADTServiceViewForCEOPeriodRange(ceoPeriod?.service)
        displayExtremeServiceViewForCEOPeriodRange(ceoPeriod?.service?.extremeDelivery)
        displaySinglesServiceViewForCEOPeriodRange(ceoPeriod?.service?.singles)
        displayCashViewForCEOPeriodRange(ceoPeriod?.cash)
        displayOERViewForCEOPeriodRange(ceoPeriod?.oerStart)
    }

    fun setStoreFilterViewForCEOPeriod(date: String){
        val periodTextCEORange = "$date | ${StorePrefData.isSelectedPeriod}"
        Validation().validateFilterKPI(
                requireActivity(),
                dbHelperCEOPeriodKpiRange,
                common_header_ceo_range.store_header!!,
                periodTextCEORange
        )

    }

    fun hideVisibilityStateOfSalesDataForCEOPeriod(){
        common_header_ceo_range.sales_header_error_image.visibility = View.VISIBLE
        common_header_ceo_range.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)
        common_header_ceo_range.sales_text_common_header.visibility = View.GONE
        common_header_ceo_range.total_sales_common_header.visibility = View.GONE
    }
    fun showVisibilityStateOfSalesDataForCEOPeriod(ceoPeriodSalesValue: String) {
        common_header_ceo_range.total_sales_common_header.visibility = View.VISIBLE
        common_header_ceo_range.total_sales_common_header.text = ceoPeriodSalesValue
        common_header_ceo_range.sales_text_common_header.visibility = View.VISIBLE
        common_header_ceo_range.sales_text_common_header.text = getString(R.string.sales_text)
        common_header_ceo_range.sales_header_error_image.visibility = View.GONE
    }

    fun displaySalesViewForCEOPeriodRange(sales: CEOPeriodRangeLevelOneQuery.Sales?) {
        if (sales?.displayName != null) {
            sales_display_ceo_period_range_kpi.text = sales.displayName
        }
        val salesCeoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.goal?.amount,
                sales?.goal?.percentage,
                sales?.goal?.value
        )
        val salesCeoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.variance?.amount,
                sales?.variance?.percentage,
                sales?.variance?.value
        )
        val salesCeoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.actual?.amount,
                sales?.actual?.percentage,
                sales?.actual?.value
        )
        if (salesCeoGoalRange.isEmpty() && salesCeoVarianceRange.isEmpty() && salesCeoActualRange.isEmpty()) {

            sales_error_ceo_period_range_kpi.visibility = View.VISIBLE
            val paramCEORangeAWUSError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEORangeAWUSError.weight = 2.0f
            sales_display_ceo_period_range_kpi.layoutParams = paramCEORangeAWUSError

            sales_goal_ceo_period_range_kpi.visibility = View.GONE
            sales_variance_ceo_period_range_kpi.visibility = View.GONE
            sales_actual_ceo_period_range_kpi.visibility = View.GONE
            sales_parent_img_ceo_period_range_kpi.visibility = View.GONE
            sales_parent_layout_ceo_period_range_kpi.isClickable = false
        } else if (salesCeoGoalRange.isNotEmpty() && salesCeoVarianceRange.isNotEmpty() && salesCeoActualRange.isNotEmpty()) {

            sales_error_ceo_period_range_kpi.visibility = View.GONE
            sales_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            sales_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            sales_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            sales_goal_ceo_period_range_kpi.text = salesCeoGoalRange
            sales_variance_ceo_period_range_kpi.text = salesCeoVarianceRange
            sales_actual_ceo_period_range_kpi.text = salesCeoActualRange
        } else {

            sales_error_ceo_period_range_kpi.visibility = View.GONE
            sales_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            sales_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            sales_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            if (salesCeoGoalRange.isEmpty()) {
                sales_goal_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_goal_ceo_period_range_kpi.text = salesCeoGoalRange
            }

            if (salesCeoVarianceRange.isEmpty()) {
                sales_variance_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_variance_ceo_period_range_kpi.text = salesCeoVarianceRange
            }

            if (salesCeoActualRange.isEmpty()) {
                sales_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_actual_ceo_period_range_kpi.text = salesCeoActualRange
            }
        }

        if (sales?.status?.toString() != null && salesCeoActualRange.isNotEmpty()) {
            when {
                sales.status.toString() == resources.getString(R.string.out_of_range) -> {
                    sales_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    sales_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                sales.status.toString() == resources.getString(R.string.under_limit) -> {
                    sales_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    sales_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    sales_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    sales_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayFoodViewForCEOPeriodRange(food: CEOPeriodRangeLevelOneQuery.Food?) {
        if (food?.displayName != null) {
            food_display_ceo_period_range_kpi.text = food.displayName
        }
        val foodCeoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.goal?.amount,
                food?.goal?.percentage,
                food?.goal?.value
        )
        val foodCeoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.variance?.amount,
                food?.variance?.percentage,
                food?.variance?.value
        )
        val foodCeoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.actual?.amount,
                food?.actual?.percentage,
                food?.actual?.value
        )

        if (foodCeoGoalRange.isEmpty() && foodCeoVarianceRange.isEmpty() && foodCeoActualRange.isEmpty()) {

            food_error_ceo_period_range_kpi.visibility = View.VISIBLE
            val paramCEORangeFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEORangeFoodError.weight = 2.0f
            food_display_ceo_period_range_kpi.layoutParams = paramCEORangeFoodError

            food_goal_ceo_period_range_kpi.visibility = View.GONE
            food_variance_ceo_period_range_kpi.visibility = View.GONE
            food_actual_ceo_period_range_kpi.visibility = View.GONE
            food_parent_img_ceo_period_range_kpi.visibility = View.GONE
            food_parent_layout_ceo_period_range_kpi.isClickable = false
        } else if (foodCeoGoalRange.isNotEmpty() && foodCeoVarianceRange.isNotEmpty() && foodCeoActualRange.isNotEmpty()) {

            food_error_ceo_period_range_kpi.visibility = View.GONE

            food_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            food_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            food_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            food_goal_ceo_period_range_kpi.text = foodCeoGoalRange
            food_variance_ceo_period_range_kpi.text = foodCeoVarianceRange
            food_actual_ceo_period_range_kpi.text = foodCeoActualRange
        } else {

            food_error_ceo_period_range_kpi.visibility = View.GONE

            food_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            food_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            food_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            if (foodCeoGoalRange.isEmpty()) {
                food_goal_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_goal_ceo_period_range_kpi.text = foodCeoGoalRange
            }

            if (foodCeoVarianceRange.isEmpty()) {
                food_variance_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_variance_ceo_period_range_kpi.text = foodCeoVarianceRange
            }

            if (foodCeoActualRange.isEmpty()) {
                food_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_actual_ceo_period_range_kpi.text = foodCeoActualRange
            }

        }

        if (food?.status?.toString() != null && foodCeoActualRange.isNotEmpty()) {
            when {
                food.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.red_circle, 0)
                    food_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                food.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    food_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    food_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    food_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayLaborViewForCEOPeriodRange(labor: CEOPeriodRangeLevelOneQuery.Labor?) {

        if (labor?.displayName != null) {
            labour_display_ceo_period_range_kpi.text = labor.displayName
        }

        val labourCeoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.goal?.amount,
                labor?.goal?.percentage,
                labor?.goal?.value
        )
        val labourCeoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.variance?.amount,
                labor?.variance?.percentage,
                labor?.variance?.value
        )
        val labourCeoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.actual?.amount,
                labor?.actual?.percentage,
                labor?.actual?.value
        )

        if (labourCeoGoalRange.isEmpty() && labourCeoVarianceRange.isEmpty() && labourCeoActualRange.isEmpty()) {

            labour_error_ceo_period_range_kpi.visibility = View.VISIBLE
            val paramCEORangeLabourError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEORangeLabourError.weight = 2.0f
            labour_display_ceo_period_range_kpi.layoutParams = paramCEORangeLabourError

            labour_goal_ceo_period_range_kpi.visibility = View.GONE
            labour_variance_ceo_period_range_kpi.visibility = View.GONE
            labour_actual_ceo_period_range_kpi.visibility = View.GONE
            labor_parent_img_ceo_period_range_kpi.visibility = View.GONE
            labor_parent_layout_ceo_period_range_kpi.isClickable = false
        } else if (labourCeoGoalRange.isNotEmpty() && labourCeoVarianceRange.isNotEmpty() && labourCeoActualRange.isNotEmpty()) {

            labour_error_ceo_period_range_kpi.visibility = View.GONE

            labour_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            labour_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            labour_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            labour_goal_ceo_period_range_kpi.text = labourCeoGoalRange
            labour_variance_ceo_period_range_kpi.text = labourCeoVarianceRange
            labour_actual_ceo_period_range_kpi.text = labourCeoActualRange
        } else {

            labour_error_ceo_period_range_kpi.visibility = View.GONE
            labour_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            labour_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            labour_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            if (labourCeoGoalRange.isEmpty()) {
                labour_goal_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_goal_ceo_period_range_kpi.text = labourCeoGoalRange
            }

            if (labourCeoVarianceRange.isEmpty()) {
                labour_variance_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_variance_ceo_period_range_kpi.text = labourCeoVarianceRange
            }

            if (labourCeoActualRange.isEmpty()) {
                labour_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_actual_ceo_period_range_kpi.text = labourCeoActualRange
            }

        }

        if (labor?.status != null && labourCeoActualRange.isNotEmpty()) {
            when {
                labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    labour_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                labor.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    labour_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    labour_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    labour_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayEADTServiceViewForCEOPeriodRange(service: CEOPeriodRangeLevelOneQuery.Service?) {
        if (service?.displayName != null) {
            service_display_ceo_period_range_kpi.text = service.displayName
        }
        if (service?.eADT?.displayName != null) {
            eadt_display_ceo_period_range_kpi.text = service.eADT.displayName
        }

        val serviceCeoEdtCeoRangeGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.goal?.amount,
                service?.eADT?.goal?.percentage,
                service?.eADT?.goal?.value
        )
        val serviceCeoEdtCeoRangeVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.variance?.amount,
                service?.eADT?.variance?.percentage,
                service?.eADT?.variance?.value
        )
        val serviceCeoEdtCeoRangeActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.actual?.amount,
                service?.eADT?.actual?.percentage,
                service?.eADT?.actual?.value
        )

        if (serviceCeoEdtCeoRangeGoal.isEmpty() && serviceCeoEdtCeoRangeVariance.isEmpty() && serviceCeoEdtCeoRangeActual.isEmpty()) {

            service_error_ceo_period_range_kpi.visibility = View.VISIBLE
            val paramCEORangeServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEORangeServiceError.weight = 2.0f
            eadt_display_ceo_period_range_kpi.layoutParams = paramCEORangeServiceError


            service_eadt_goal_ceo_period_range_kpi.visibility = View.GONE
            service_eadt_variance_ceo_period_range_kpi.visibility = View.GONE
            service_eadt_actual_ceo_period_range_kpi.visibility = View.GONE
        } else if (serviceCeoEdtCeoRangeGoal.isNotEmpty() && serviceCeoEdtCeoRangeVariance.isNotEmpty() && serviceCeoEdtCeoRangeActual.isNotEmpty()) {

            service_error_ceo_period_range_kpi.visibility = View.GONE
            service_eadt_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            service_eadt_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            service_eadt_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            service_eadt_goal_ceo_period_range_kpi.text = serviceCeoEdtCeoRangeGoal
            service_eadt_variance_ceo_period_range_kpi.text = serviceCeoEdtCeoRangeVariance
            service_eadt_actual_ceo_period_range_kpi.text = serviceCeoEdtCeoRangeActual
        } else {

            service_error_ceo_period_range_kpi.visibility = View.GONE
            service_eadt_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            service_eadt_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            service_eadt_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            if (serviceCeoEdtCeoRangeGoal.isEmpty()) {
                service_eadt_goal_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_goal_ceo_period_range_kpi.text = serviceCeoEdtCeoRangeGoal
            }

            if (serviceCeoEdtCeoRangeVariance.isEmpty()) {
                service_eadt_variance_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_variance_ceo_period_range_kpi.text = serviceCeoEdtCeoRangeVariance
            }

            if (serviceCeoEdtCeoRangeActual.isEmpty()) {
                service_eadt_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_actual_ceo_period_range_kpi.text = serviceCeoEdtCeoRangeActual
            }

        }

        if (service?.eADT?.status != null && serviceCeoEdtCeoRangeActual.isNotEmpty()) {
            when {
                service.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_eadt_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_eadt_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                service.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_eadt_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_eadt_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_eadt_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_eadt_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayExtremeServiceViewForCEOPeriodRange(extremeDelivery: CEOPeriodRangeLevelOneQuery.ExtremeDelivery?) {
        if (extremeDelivery?.displayName != null) {
            extreme_delivery_display_ceo_period_range_kpi.text = extremeDelivery.displayName
        }

        val serviceCeoExtremeCeoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.goal?.amount,
                extremeDelivery?.goal?.percentage,
                extremeDelivery?.goal?.value
        )
        val serviceCeoExtremeCeoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.variance?.amount,
                extremeDelivery?.variance?.percentage,
                extremeDelivery?.variance?.value
        )
        val serviceCeoExtremeCeoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.actual?.amount,
                extremeDelivery?.actual?.percentage,
                extremeDelivery?.actual?.value
        )

        if (serviceCeoExtremeCeoGoalRange.isEmpty() && serviceCeoExtremeCeoVarianceRange.isEmpty() && serviceCeoExtremeCeoActualRange.isEmpty()) {

            serviceExtreme_error_ceo_period_range_kpi.visibility = View.VISIBLE
            val paramCEORangeServiceExtremeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEORangeServiceExtremeError.weight = 2.0f
            extreme_delivery_display_ceo_period_range_kpi.layoutParams = paramCEORangeServiceExtremeError

            service_extreme_goal_ceo_period_range_kpi.visibility = View.GONE
            service_extreme_variance_ceo_period_range_kpi.visibility = View.GONE
            service_extreme_actual_ceo_period_range_kpi.visibility = View.GONE
        } else if (serviceCeoExtremeCeoGoalRange.isNotEmpty() && serviceCeoExtremeCeoVarianceRange.isNotEmpty() && serviceCeoExtremeCeoActualRange.isNotEmpty()) {

            serviceExtreme_error_ceo_period_range_kpi.visibility = View.GONE
            service_extreme_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            service_extreme_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            service_extreme_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            service_extreme_goal_ceo_period_range_kpi.text = serviceCeoExtremeCeoGoalRange
            service_extreme_variance_ceo_period_range_kpi.text = serviceCeoExtremeCeoVarianceRange
            service_extreme_actual_ceo_period_range_kpi.text = serviceCeoExtremeCeoActualRange
        } else {

            serviceExtreme_error_ceo_period_range_kpi.visibility = View.GONE
            service_extreme_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            service_extreme_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            service_extreme_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            if (serviceCeoExtremeCeoGoalRange.isEmpty()) {
                service_extreme_goal_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_goal_ceo_period_range_kpi.text = serviceCeoExtremeCeoGoalRange
            }

            if (serviceCeoExtremeCeoVarianceRange.isEmpty()) {
                service_extreme_variance_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_variance_ceo_period_range_kpi.text = serviceCeoExtremeCeoVarianceRange
            }

            if (serviceCeoExtremeCeoActualRange.isEmpty()) {
                service_extreme_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_actual_ceo_period_range_kpi.text = serviceCeoExtremeCeoActualRange
            }

        }

        if (extremeDelivery?.status != null && serviceCeoExtremeCeoActualRange.isNotEmpty()) {
            when {
                extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_extreme_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_extreme_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_extreme_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    service_extreme_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_extreme_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }
    }

    fun displaySinglesServiceViewForCEOPeriodRange(singles: CEOPeriodRangeLevelOneQuery.Singles?) {
        if (singles?.displayName != null) {
            single_display_ceo_period_range_kpi.text = singles.displayName
        }

        val serviceCeoSinglesCeoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.goal?.amount,
                singles?.goal?.percentage,
                singles?.goal?.value
        )
        val serviceCeoSinglesCeoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.variance?.amount,
                singles?.variance?.percentage,
                singles?.variance?.value
        )
        val serviceCeoSinglesCeoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.actual?.amount,
                singles?.actual?.percentage,
                singles?.actual?.value
        )

        if (serviceCeoSinglesCeoGoalRange.isEmpty() && serviceCeoSinglesCeoVarianceRange.isEmpty() && serviceCeoSinglesCeoActualRange.isEmpty()) {

            serviceSingles_error_ceo_period_range_kpi.visibility = View.VISIBLE
            val paramCEORangeServiceSingleError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEORangeServiceSingleError.weight = 2.0f
            single_display_ceo_period_range_kpi.layoutParams = paramCEORangeServiceSingleError

            service_singles_goal_ceo_period_range_kpi.visibility = View.GONE
            service_singles_variance_ceo_period_range_kpi.visibility = View.GONE
            service_singles_actual_ceo_period_range_kpi.visibility = View.GONE
        } else if (serviceCeoSinglesCeoGoalRange.isNotEmpty() && serviceCeoSinglesCeoVarianceRange.isNotEmpty() && serviceCeoSinglesCeoActualRange.isNotEmpty()) {

            serviceSingles_error_ceo_period_range_kpi.visibility = View.GONE
            service_singles_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            service_singles_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            service_singles_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            service_singles_goal_ceo_period_range_kpi.text = serviceCeoSinglesCeoGoalRange
            service_singles_variance_ceo_period_range_kpi.text = serviceCeoSinglesCeoVarianceRange
            service_singles_actual_ceo_period_range_kpi.text = serviceCeoSinglesCeoActualRange
        } else {

            serviceSingles_error_ceo_period_range_kpi.visibility = View.GONE
            service_singles_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            service_singles_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            service_singles_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            if (serviceCeoSinglesCeoGoalRange.isEmpty()) {
                service_singles_goal_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_goal_ceo_period_range_kpi.text = serviceCeoSinglesCeoGoalRange
            }

            if (serviceCeoSinglesCeoVarianceRange.isEmpty()) {
                service_singles_variance_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_variance_ceo_period_range_kpi.text = serviceCeoSinglesCeoVarianceRange
            }

            if (serviceCeoSinglesCeoActualRange.isEmpty()) {
                service_singles_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_actual_ceo_period_range_kpi.text = serviceCeoSinglesCeoActualRange
            }

        }


        if (singles?.status != null && serviceCeoSinglesCeoActualRange.isNotEmpty()) {
            when {
                singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_singles_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_singles_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_singles_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_singles_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_singles_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayCashViewForCEOPeriodRange(cash: CEOPeriodRangeLevelOneQuery.Cash?) {

        if (cash?.displayName != null) {
            cash_display_ceo_period_range_kpi.text = cash.displayName
        }
        val cashCeoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.goal?.amount,
                cash?.goal?.percentage,
                cash?.goal?.value
        )
        val cashCeoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.variance?.amount,
                cash?.variance?.percentage,
                cash?.variance?.value
        )
        val cashCeoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.actual?.amount,
                cash?.actual?.percentage,
                cash?.actual?.value
        )

        if (cashCeoGoalRange.isEmpty() && cashCeoVarianceRange.isEmpty() && cashCeoActualRange.isEmpty()) {

            cash_error_ceo_period_range_kpi.visibility = View.VISIBLE
            val paramCEORangeCashError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEORangeCashError.weight = 2.0f
            cash_display_ceo_period_range_kpi.layoutParams = paramCEORangeCashError

            cash_goal_ceo_period_range_kpi.visibility = View.GONE
            cash_variance_ceo_period_range_kpi.visibility = View.GONE
            cash_actual_ceo_period_range_kpi.visibility = View.GONE
            cash_parent_layout_ceo_period_range_kpi.isClickable = false
            cash_parent_img_ceo_period_range_kpi.visibility = View.GONE
        } else if (cashCeoGoalRange.isNotEmpty() && cashCeoVarianceRange.isNotEmpty() && cashCeoActualRange.isNotEmpty()) {

            cash_error_ceo_period_range_kpi.visibility = View.GONE

            cash_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            cash_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            cash_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            cash_goal_ceo_period_range_kpi.text = cashCeoGoalRange
            cash_variance_ceo_period_range_kpi.text = cashCeoVarianceRange
            cash_actual_ceo_period_range_kpi.text = cashCeoActualRange
        } else {

            cash_error_ceo_period_range_kpi.visibility = View.GONE
            cash_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            cash_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            cash_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            if (cashCeoGoalRange.isEmpty()) {
                cash_goal_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_goal_ceo_period_range_kpi.text = cashCeoGoalRange
            }

            if (cashCeoVarianceRange.isEmpty()) {
                cash_variance_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_variance_ceo_period_range_kpi.text = cashCeoVarianceRange
            }

            if (cashCeoActualRange.isEmpty()) {
                cash_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_actual_ceo_period_range_kpi.text = cashCeoActualRange
            }

        }

        if (cash?.status != null && cashCeoActualRange.isNotEmpty()) {
            when {
                cash.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.red_circle, 0)
                    cash_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                cash.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    cash_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    cash_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    cash_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }
    }

    fun displayOERViewForCEOPeriodRange(oerStart: CEOPeriodRangeLevelOneQuery.OerStart?) {
        if (oerStart?.displayName != null) {
            oer_display_ceo_period_range_kpi.text = oerStart.displayName
        }
        val oerCeoGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.goal?.amount,
                oerStart?.goal?.percentage,
                oerStart?.goal?.value
        )
        val oerCeoVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.variance?.amount,
                oerStart?.variance?.percentage,
                oerStart?.variance?.value
        )
        val oerCeoActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.actual?.amount,
                oerStart?.actual?.percentage,
                oerStart?.actual?.value
        )

        if (oerCeoGoalRange.isEmpty() && oerCeoVarianceRange.isEmpty() && oerCeoActualRange.isEmpty()) {

            oer_error_ceo_period_range_kpi.visibility = View.VISIBLE

            val paramCEORangeOERError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramCEORangeOERError.weight = 2.0f
            oer_display_ceo_period_range_kpi.layoutParams = paramCEORangeOERError

            oer_goal_ceo_period_range_kpi.visibility = View.GONE
            oer_variance_ceo_period_range_kpi.visibility = View.GONE
            oer_actual_ceo_period_range_kpi.visibility = View.GONE
            oer_parent_img_ceo_period_range_kpi.visibility = View.GONE
            oer_parent_layout_ceo_period_range_kpi.isClickable = false
        } else if (oerCeoGoalRange.isNotEmpty() && oerCeoVarianceRange.isNotEmpty() && oerCeoActualRange.isNotEmpty()) {

            oer_error_ceo_period_range_kpi.visibility = View.GONE

            oer_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            oer_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            oer_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            oer_goal_ceo_period_range_kpi.text = oerCeoGoalRange
            oer_variance_ceo_period_range_kpi.text = oerCeoVarianceRange
            oer_actual_ceo_period_range_kpi.text = oerCeoActualRange
        } else {

            oer_error_ceo_period_range_kpi.visibility = View.GONE
            oer_goal_ceo_period_range_kpi.visibility = View.VISIBLE
            oer_variance_ceo_period_range_kpi.visibility = View.VISIBLE
            oer_actual_ceo_period_range_kpi.visibility = View.VISIBLE

            if (oerCeoGoalRange.isEmpty()) {
                oer_goal_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_goal_ceo_period_range_kpi.text = oerCeoGoalRange
            }

            if (oerCeoVarianceRange.isEmpty()) {
                oer_variance_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_variance_ceo_period_range_kpi.text = oerCeoVarianceRange
            }

            if (oerCeoActualRange.isEmpty()) {
                oer_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_actual_ceo_period_range_kpi.text = oerCeoActualRange
            }

        }

        if (oerStart?.status != null && oerCeoActualRange.isNotEmpty()) {
            when {
                oerStart.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oer_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    oer_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                oerStart.status.toString() == resources.getString(R.string.under_limit) -> {
                    oer_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    oer_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    oer_actual_ceo_period_range_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    oer_actual_ceo_period_range_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }
    }


    fun callCeoPeriodRangeLevelTwoQuery(
            actionForCEOPeriodRangeLevel2: String,
            rcvForCEOPeriodRangeLevel2: NonScrollExpandableListView
    ) {
        val progressDialogCeoPeriodRangeLevel2 = CustomProgressDialog(requireActivity())
        lifecycleScope.launchWhenResumed {
            progressDialogCeoPeriodRangeLevel2.showProgressDialog()
            val areaCodeCEOPeriodRangeLevel2 = dbHelperCEOPeriodKpiRange.getAllSelectedAreaList(true)
            val stateCodeCEOPeriodRangeLevel2 = dbHelperCEOPeriodKpiRange.getAllSelectedStoreListState(true)
            val superVisorNumberListCEOPeriodRangeLevel2 = dbHelperCEOPeriodKpiRange.getAllSelectedStoreListSupervisor(true)

            Logger.info(
                    CEOPeriodRangeLevelTwoQuery.OPERATION_NAME.name(), "Period Level 2 KPI",
                    mapQueryFilters(
                            areaCodeCEOPeriodRangeLevel2,
                            stateCodeCEOPeriodRangeLevel2,
                            superVisorNumberListCEOPeriodRangeLevel2,
                            Collections.emptyList(),
                            CEOPeriodRangeLevelTwoQuery.QUERY_DOCUMENT
                    )
            )

            try {
                val responseCEOPeriodRangeLevelTwo =
                        apolloClient(requireContext()).query(
                                CEOPeriodRangeLevelTwoQuery(
                                        areaCodeCEOPeriodRangeLevel2.toInput(),
                                        stateCodeCEOPeriodRangeLevel2.toInput(),
                                        superVisorNumberListCEOPeriodRangeLevel2.toInput(),
                                        formattedCEOPeriodKpiStartDateValue.toInput(),
                                        formattedCEOPeriodKpiEndDateValue.toInput(),
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                                .toInput()
                                )
                        )
                                .await()

                if (responseCEOPeriodRangeLevelTwo.data?.ceo != null) {
                    progressDialogCeoPeriodRangeLevel2.dismissProgressDialog()
                    ceoPeriodRangeLevelTwo = responseCEOPeriodRangeLevelTwo.data?.ceo!!

                    ceoPeriodRangeLevelTwo.kpis?.individualSupervisors.let {
                        setCEOPeriodKpiExpandableData(actionForCEOPeriodRangeLevel2, rcvForCEOPeriodRangeLevel2)
                    }
                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogCeoPeriodRangeLevel2.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Level 2")
                        }
                refreshCEOPeriodKpiToken()
                return@launchWhenResumed

            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogCeoPeriodRangeLevel2.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForCeoPeriod()
                }
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Period Range Level 2")
                progressDialogCeoPeriodRangeLevel2.dismissProgressDialog()
                setErrorScreenVisibleStateForCeoPeriod(
                        getString(R.string.exception_error_text_title),
                        getString(R.string.exception_error_text_description)
                )
            }

        }
    }


    private fun setCEOPeriodKpiExpandableData(
            action: String,
            rcv: NonScrollExpandableListView
    ) {
        val childDataCEOPeriodKpi: MutableList<StoreDetailPojo> = ArrayList()

        ceoPeriodRangeLevelTwo.kpis!!.individualSupervisors.forEachIndexed {_, item ->
            expandableListDetailCEOPeriodKpiRange[item!!.supervisorName!!] = childDataCEOPeriodKpi
        }
        val titleListCEOPeriodKpi = ArrayList(expandableListDetailCEOPeriodKpiRange.keys)


        expandableListAdapterCEOPeriodKpiRange = CustomExpandableListAdapterPeriodCEO(
                requireActivity(),
                titleListCEOPeriodKpi as ArrayList<String>,
                expandableListDetailCEOPeriodKpiRange, ceoPeriodRangeLevelTwo, action
        )
        rcv.setAdapter(expandableListAdapterCEOPeriodKpiRange)

        rcv.setOnGroupExpandListener {groupPosition ->
            val ceoRangeExpandableSuperVisorNumberValue =
                    ceoPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.supervisorNumber!!
            val ceoRangeExpandableSupervisorSalesKpiData =
                    ceoPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.period?.sales
            val ceoRangeExpandableSupervisorFoodKpiData =
                    ceoPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.period?.food
            val ceoRangeExpandableSupervisorLaborKpiData =
                    ceoPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.period?.labor
            val ceoRangeExpandableSupervisorOERKpiData =
                    ceoPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.period?.oerStart
            val ceoRangeExpandableSupervisorCashKpiData =
                    ceoPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.period?.cash

            //Sales
            val ceoRangeExpandableSupervisorSalesKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoRangeExpandableSupervisorSalesKpiData?.goal?.amount,
                    ceoRangeExpandableSupervisorSalesKpiData?.goal?.value,
                    ceoRangeExpandableSupervisorSalesKpiData?.goal?.percentage
            )
            val ceoRangeExpandableSupervisorSalesKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoRangeExpandableSupervisorSalesKpiData?.variance?.amount,
                    ceoRangeExpandableSupervisorSalesKpiData?.variance?.value,
                    ceoRangeExpandableSupervisorSalesKpiData?.variance?.percentage
            )
            val ceoRangeExpandableSupervisorSalesKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoRangeExpandableSupervisorSalesKpiData?.actual?.amount,
                    ceoRangeExpandableSupervisorSalesKpiData?.actual?.value,
                    ceoRangeExpandableSupervisorSalesKpiData?.actual?.percentage
            )

            //Food
            val ceoRangeExpandableSupervisorFoodKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoRangeExpandableSupervisorFoodKpiData?.goal?.amount,
                    ceoRangeExpandableSupervisorFoodKpiData?.goal?.value,
                    ceoRangeExpandableSupervisorFoodKpiData?.goal?.percentage
            )
            val ceoRangeExpandableSupervisorFoodKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoRangeExpandableSupervisorFoodKpiData?.variance?.amount,
                    ceoRangeExpandableSupervisorFoodKpiData?.variance?.value,
                    ceoRangeExpandableSupervisorFoodKpiData?.variance?.percentage
            )
            val ceoRangeExpandableSupervisorFoodKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoRangeExpandableSupervisorFoodKpiData?.actual?.amount,
                    ceoRangeExpandableSupervisorFoodKpiData?.actual?.value,
                    ceoRangeExpandableSupervisorFoodKpiData?.actual?.percentage
            )

            //Labor
            val ceoRangeExpandableSupervisorLaborKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoRangeExpandableSupervisorLaborKpiData?.goal?.amount,
                    ceoRangeExpandableSupervisorLaborKpiData?.goal?.value,
                    ceoRangeExpandableSupervisorLaborKpiData?.goal?.percentage
            )
            val ceoRangeExpandableSupervisorLaborKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoRangeExpandableSupervisorLaborKpiData?.variance?.amount,
                    ceoRangeExpandableSupervisorLaborKpiData?.variance?.value,
                    ceoRangeExpandableSupervisorLaborKpiData?.variance?.percentage
            )
            val ceoRangeExpandableSupervisorLaborKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoRangeExpandableSupervisorLaborKpiData?.actual?.amount,
                    ceoRangeExpandableSupervisorLaborKpiData?.actual?.value,
                    ceoRangeExpandableSupervisorLaborKpiData?.actual?.percentage
            )


            //OER
            val ceoRangeExpandableSupervisorOERKpiDataGoal = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoRangeExpandableSupervisorOERKpiData?.goal?.amount,
                    ceoRangeExpandableSupervisorOERKpiData?.goal?.value,
                    ceoRangeExpandableSupervisorOERKpiData?.goal?.percentage
            )
            val ceoRangeExpandableSupervisorOERKpiDataVariance = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoRangeExpandableSupervisorOERKpiData?.variance?.amount,
                    ceoRangeExpandableSupervisorOERKpiData?.variance?.value,
                    ceoRangeExpandableSupervisorOERKpiData?.variance?.percentage
            )
            val ceoRangeExpandableSupervisorOERKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoRangeExpandableSupervisorOERKpiData?.actual?.amount,
                    ceoRangeExpandableSupervisorOERKpiData?.actual?.value,
                    ceoRangeExpandableSupervisorOERKpiData?.actual?.percentage
            )

            //Cash
            val ceoRangeExpandableSupervisorCashKpiDataActual = Validation().checkAmountPercentageValue(
                    requireActivity(),
                    ceoRangeExpandableSupervisorCashKpiData?.actual?.amount,
                    ceoRangeExpandableSupervisorCashKpiData?.actual?.value,
                    ceoRangeExpandableSupervisorCashKpiData?.actual?.percentage
            )

            when {
                rcv_sales_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                    if (ceoRangeExpandableSupervisorSalesKpiDataGoal.isEmpty() && ceoRangeExpandableSupervisorSalesKpiDataVariance.isEmpty() && ceoRangeExpandableSupervisorSalesKpiDataActual.isEmpty()) {
                        rcv.collapseGroup(groupPosition)
                    } else {
                        if (rcv.isGroupExpanded(groupPosition)) {
                            callCEOPeriodKpiStoreAgainstSupervisor(
                                    titleListCEOPeriodKpi[groupPosition],
                                    action,
                                    ceoRangeExpandableSuperVisorNumberValue
                            )
                        } else
                            rcv.collapseGroup(groupPosition)
                    }
                }

                rcv_food_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                    if (ceoRangeExpandableSupervisorFoodKpiDataGoal.isEmpty() && ceoRangeExpandableSupervisorFoodKpiDataVariance.isEmpty() && ceoRangeExpandableSupervisorFoodKpiDataActual.isEmpty()) {
                        rcv.collapseGroup(groupPosition)
                    } else {
                        if (rcv.isGroupExpanded(groupPosition)) {
                            callCEOPeriodKpiStoreAgainstSupervisor(
                                    titleListCEOPeriodKpi[groupPosition],
                                    action,
                                    ceoRangeExpandableSuperVisorNumberValue
                            )
                        } else
                            rcv.collapseGroup(groupPosition)
                    }
                }

                rcv_labour_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                    if (ceoRangeExpandableSupervisorLaborKpiDataGoal.isEmpty() && ceoRangeExpandableSupervisorLaborKpiDataVariance.isEmpty() && ceoRangeExpandableSupervisorLaborKpiDataActual.isEmpty()) {
                        rcv.collapseGroup(groupPosition)
                    } else {
                        if (rcv.isGroupExpanded(groupPosition)) {
                            callCEOPeriodKpiStoreAgainstSupervisor(
                                    titleListCEOPeriodKpi[groupPosition],
                                    action,
                                    ceoRangeExpandableSuperVisorNumberValue
                            )
                        } else
                            rcv.collapseGroup(groupPosition)
                    }
                }

                rcv_service_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                    if (ceoPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.period == null) {
                        rcv.collapseGroup(groupPosition)
                    } else {
                        if (rcv.isGroupExpanded(groupPosition)) {
                            callCEOPeriodKpiStoreAgainstSupervisor(
                                    titleListCEOPeriodKpi[groupPosition],
                                    action,
                                    ceoRangeExpandableSuperVisorNumberValue
                            )
                        } else
                            rcv.collapseGroup(groupPosition)
                    }
                }

                rcv_oer_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                    if (ceoRangeExpandableSupervisorOERKpiDataGoal.isEmpty() && ceoRangeExpandableSupervisorOERKpiDataVariance.isEmpty() && ceoRangeExpandableSupervisorOERKpiDataActual.isEmpty()) {
                        rcv.collapseGroup(groupPosition)
                    } else {
                        if (rcv.isGroupExpanded(groupPosition)) {
                            callCEOPeriodKpiStoreAgainstSupervisor(
                                    titleListCEOPeriodKpi[groupPosition],
                                    action,
                                    ceoRangeExpandableSuperVisorNumberValue
                            )
                        } else
                            rcv.collapseGroup(groupPosition)
                    }

                }

                rcv_cash_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                    if (ceoRangeExpandableSupervisorCashKpiDataActual.isNotEmpty()) {
                        if (rcv.isGroupExpanded(groupPosition)) {
                            callCEOPeriodKpiStoreAgainstSupervisor(
                                    titleListCEOPeriodKpi[groupPosition],
                                    action,
                                    ceoRangeExpandableSuperVisorNumberValue
                            )
                        } else
                            rcv.collapseGroup(groupPosition)
                    } else {
                        rcv.collapseGroup(groupPosition)
                    }

                }
            }


            if (lastExpandedPositionCEOPeriodKpiRange != -1 && groupPosition != lastExpandedPositionCEOPeriodKpiRange) {
                rcv.collapseGroup(lastExpandedPositionCEOPeriodKpiRange)
            }
            lastExpandedPositionCEOPeriodKpiRange = groupPosition
        }

        rcv.setOnChildClickListener {_, _, groupPosition, childPosition, _ ->
            val superVisorNumber = ceoPeriodRangeLevelTwo.kpis!!.individualSupervisors[groupPosition]!!.supervisorNumber!!

            if (childPosition == 0) {

                when {
                    rcv_sales_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                        callCEOPeriodRangeOverViewKpiApi(getString(R.string.awus_text), superVisorNumber, "")
                    }
                    rcv_food_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                        callCEOPeriodRangeOverViewKpiApi(getString(R.string.food_text), superVisorNumber, "")
                    }
                    rcv_labour_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                        callCEOPeriodRangeOverViewKpiApi(getString(R.string.labour_text), superVisorNumber, "")
                    }
                    rcv_service_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                        callCEOPeriodRangeOverViewKpiApi(getString(R.string.service_text), superVisorNumber, "")
                    }
                    rcv_oer_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                        callCEOPeriodRangeOverViewKpiApi(getString(R.string.oer_text), superVisorNumber, "")
                    }
                    rcv_cash_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                        callCEOPeriodRangeOverViewKpiApi(getString(R.string.cash_text), superVisorNumber, "")
                    }
                }

            } else {

                val storeNumber =
                        expandableListDetailCEOPeriodKpiRange[titleListCEOPeriodKpi[groupPosition]]!![(childPosition)].storeNumber!!

                val ceoRangeKpiData =
                        expandableListDetailCEOPeriodKpiRange[titleListCEOPeriodKpi[groupPosition]]!![(childPosition)]
                if (ceoRangeKpiData.storeGoal?.isNotEmpty() == true || ceoRangeKpiData.storeVariance?.isNotEmpty() == true || ceoRangeKpiData.storeActual?.isNotEmpty() == true) {
                    when {
                        rcv_sales_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                            callCEOPeriodRangeOverViewKpiApi(getString(R.string.awus_text), superVisorNumber, storeNumber)
                        }
                        rcv_food_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                            callCEOPeriodRangeOverViewKpiApi(getString(R.string.food_text), superVisorNumber, storeNumber)
                        }
                        rcv_labour_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                            callCEOPeriodRangeOverViewKpiApi(getString(R.string.labour_text), superVisorNumber, storeNumber)
                        }

                        rcv_oer_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                            callCEOPeriodRangeOverViewKpiApi(getString(R.string.oer_text), superVisorNumber, storeNumber)
                        }
                        rcv_cash_ceo_period_range_kpi.visibility == View.VISIBLE -> {
                            callCEOPeriodRangeOverViewKpiApi(getString(R.string.cash_text), superVisorNumber, storeNumber)
                        }
                    }

                }
                if (rcv_service_ceo_period_range_kpi.visibility == View.VISIBLE) {
                    callCEOPeriodRangeOverViewKpiApi(getString(R.string.service_text), superVisorNumber, storeNumber)
                }

            }
            false
        }

    }

    private fun setCEORangeDataForIndividualStore(
            action: String,
            title: String
    ) {
        val storeDetailsCEOPeriodKpi = supervisorLevel3CEOPeriodKpiRange.kpis!!.individualStores
        val childDataCEOPeriodKpi = mutableListOf<StoreDetailPojo>()
        childDataCEOPeriodKpi.add(
                StoreDetailPojo(
                        "",
                        "",
                        "",
                        "",
                        ""
                )
        )

        storeDetailsCEOPeriodKpi.forEachIndexed {_, item ->
            when (action) {
                requireActivity().getString(R.string.awus_text) -> {
                    childDataCEOPeriodKpi.add(
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
                    childDataCEOPeriodKpi.add(
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
                    childDataCEOPeriodKpi.add(
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
                    childDataCEOPeriodKpi.add(
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
                    childDataCEOPeriodKpi.add(
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
                    childDataCEOPeriodKpi.add(
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
        if (childDataCEOPeriodKpi.size == 2) {
            childDataCEOPeriodKpi.removeAt(0)
        }
        if (childDataCEOPeriodKpi.size < 3) {
            Log.e("ceo period", "${childDataCEOPeriodKpi.size}")
        } else if (childDataCEOPeriodKpi.size == 2) {
            childDataCEOPeriodKpi.removeAt(0)
        }
        expandableListDetailCEOPeriodKpiRange[title] = childDataCEOPeriodKpi
        expandableListAdapterCEOPeriodKpiRange!! setChild (expandableListDetailCEOPeriodKpiRange)

    }

    private fun callCEOPeriodKpiStoreAgainstSupervisor(
            title: String,
            action: String,
            ceoRangeSuperVisorNumberValue: String
    ) {

        val formattedStartDateValueCEOPeriod: String
        val formattedEndDateValueCEOPeriod: String

        if (StorePrefData.isCalendarSelected) {
            formattedStartDateValueCEOPeriod = StorePrefData.startDateValue
            formattedEndDateValueCEOPeriod = StorePrefData.endDateValue
        } else {
            formattedStartDateValueCEOPeriod = StorePrefData.startDateValue
            formattedEndDateValueCEOPeriod = StorePrefData.endDateValue
        }


        val progressDialog = CustomProgressDialog(requireActivity())
        lifecycleScope.launchWhenResumed {
            progressDialog.showProgressDialog()
            val areaCode = dbHelperCEOPeriodKpiRange.getAllSelectedAreaList(true)
            val stateCode = dbHelperCEOPeriodKpiRange.getAllSelectedStoreListState(true)
            var superVisorNumberList =
                    dbHelperCEOPeriodKpiRange.getAllSelectedStoreListSupervisor(true)
            val storeListValue = dbHelperCEOPeriodKpiRange.getAllSelectedStoreList(true)
            val ceoRangeSupervisorNumberListTemp = mutableListOf<String>()
            if (ceoRangeSuperVisorNumberValue.isNotEmpty() || ceoRangeSuperVisorNumberValue.isNotBlank()) {
                ceoRangeSupervisorNumberListTemp.add(ceoRangeSuperVisorNumberValue)
            }

            if (dbHelperCEOPeriodKpiRange.getAllSelectedStoreListSupervisor(true)
                        .isEmpty()
            ) {
                superVisorNumberList = ceoRangeSupervisorNumberListTemp
            }

            Logger.info(
                    CEOPeriodLVLThreeQuery.OPERATION_NAME.name(), "Period Range KPI",
                    mapQueryFilters(
                            QueryData(
                                    areaCode,
                                    stateCode,
                                    superVisorNumberList,
                                    storeListValue,
                                    formattedEndDateValueCEOPeriod,
                                    formattedStartDateValueCEOPeriod,
                                    StorePrefData.filterType,
                                    CEOPeriodLVLThreeQuery.QUERY_DOCUMENT
                            )
                    )
            )
            try {
                val response =
                        apolloClient(requireContext()).query(
                                CEOPeriodLVLThreeQuery(
                                        areaCode.toInput(),
                                        stateCode.toInput(),
                                        superVisorNumberList.toInput(),
                                        storeListValue.toInput(),
                                        formattedEndDateValueCEOPeriod.toInput(),
                                        formattedStartDateValueCEOPeriod.toInput(),
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                                .toInput()
                                )
                        )
                                .await()

                if (response.data?.ceo != null) {
                    progressDialog.dismissProgressDialog()
                    supervisorLevel3CEOPeriodKpiRange = response.data?.ceo!!

                    response.data?.ceo?.kpis?.individualStores.let {
                        setCEORangeDataForIndividualStore(
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
                refreshCEOPeriodKpiToken()
                return@launchWhenResumed
            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialog.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForCeoPeriod()
                }

            } catch (exception: ApolloException) {
                progressDialog.dismissProgressDialog()
                Logger.error(exception.message.toString(), "Period Range Level 3 KPI")
                setErrorScreenVisibleStateForCeoPeriod(
                        getString(R.string.exception_error_text_title),
                        getString(R.string.exception_error_text_description)
                )

            }
        }
    }

    private fun callCEOPeriodRangeOverViewKpiApi(
            actionCEORangeOverView: String,
            superVisorNumberCEORangeOverView: String,
            storeNumberCEORangeOverView: String
    ) {
        lifecycleScope.launchWhenResumed {
            val superVisorNumberListCEOPeriodRange = mutableListOf<String>()

            if (superVisorNumberCEORangeOverView.isNotEmpty() || superVisorNumberCEORangeOverView.isNotBlank()) {
                superVisorNumberListCEOPeriodRange.add(superVisorNumberCEORangeOverView)
            }

            val storeNumberListCEOPeriodRange = mutableListOf<String>()

            if (storeNumberCEORangeOverView.isNotEmpty() || storeNumberCEORangeOverView.isNotBlank()) {
                storeNumberListCEOPeriodRange.add(storeNumberCEORangeOverView)
            } else {

                storeNumberListCEOPeriodRange.addAll(dbHelperCEOPeriodKpiRange.getAllSelectedStoreList(true))
            }

            if (StorePrefData.isCalendarSelected) {
                formattedCEOPeriodKpiStartDateValue = StorePrefData.startDateValue
                formattedCEOPeriodKpiEndDateValue = StorePrefData.endDateValue
            } else {
                formattedCEOPeriodKpiStartDateValue = StorePrefData.startDateValue
                formattedCEOPeriodKpiEndDateValue = StorePrefData.endDateValue
            }

            val progressDialogCEOPeriodRange = CustomProgressDialog(requireActivity())
            progressDialogCEOPeriodRange.showProgressDialog()

            try {
                val response =
                        apolloClient(requireContext()).query(
                                CEOOverviewRangeQuery(
                                        superVisorNumberListCEOPeriodRange.toInput(),
                                        storeNumberListCEOPeriodRange.toInput(),
                                        formattedCEOPeriodKpiStartDateValue,
                                        formattedCEOPeriodKpiEndDateValue,
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                )
                        )
                                .await()

                if (response.data?.ceo != null) {
                    progressDialogCEOPeriodRange.dismissProgressDialog()
                    when (actionCEORangeOverView) {
                        getString(R.string.awus_text) -> {
                            openSalesDetailCEOPeriodKpiRange(response.data?.ceo!!)
                        }
                        getString(R.string.food_text) -> {
                            openFoodCEOPeriodKpiDetail(response.data?.ceo!!)
                        }
                        getString(R.string.labour_text) -> {
                            openCEOLabourDetailPeriodKpi(response.data?.ceo!!)
                        }
                        getString(R.string.service_text) -> {
                            openServiceCEOPeriodKpiDetail(response.data?.ceo!!)
                        }
                        getString(R.string.oer_text) -> {
                            openOERCEOPeriodKpiDetail(response.data?.ceo!!)
                        }
                        getString(R.string.cash_text) -> {
                            openCASHCEOPeriodKpiDetail(response.data?.ceo!!)
                        }
                    }
                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogCEOPeriodRange.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Overview KPI")
                        }
                refreshCEOPeriodKpiToken()
                return@launchWhenResumed
            } catch (e: ApolloNetworkException) {
                progressDialogCEOPeriodRange.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForCeoPeriod()
                }
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Period Range Overview KPI")
                progressDialogCEOPeriodRange.dismissProgressDialog()
                setErrorScreenVisibleStateForCeoPeriod(
                        getString(R.string.exception_error_text_title),
                        getString(R.string.exception_error_text_description)
                )
            }

        }
    }


    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.sales_parent_layout_ceo_period_range_kpi -> {

                rcv_food_ceo_period_range_kpi.visibility = View.GONE
                rcv_labour_ceo_period_range_kpi.visibility = View.GONE
                rcv_service_ceo_period_range_kpi.visibility = View.GONE
                rcv_oer_ceo_period_range_kpi.visibility = View.GONE
                rcv_cash_ceo_period_range_kpi.visibility = View.GONE

                food_text_overview_ceo_period_range_kpi.visibility = View.GONE
                labour_text_overview_ceo_period_range_kpi.visibility = View.GONE
                service_text_overview_ceo_period_range_kpi.visibility = View.GONE
                cash_text_overview_ceo_period_range_kpi.visibility = View.GONE
                cash_text_overview_ceo_period_range_kpi.visibility = View.GONE

                food_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_sales_ceo_period_range_kpi.visibility == View.VISIBLE) {
                    rcv_sales_ceo_period_range_kpi.visibility = View.GONE
                    sales_text_overview_ceo_period_range_kpi.visibility = View.GONE
                    sales_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_sales_ceo_period_range_kpi.visibility = View.VISIBLE
                    sales_text_overview_ceo_period_range_kpi.visibility = View.VISIBLE
                    sales_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_up)
                }
                callCeoPeriodRangeLevelTwoQuery(getString(R.string.awus_text), rcv_sales_ceo_period_range_kpi)
            }
            R.id.food_parent_layout_ceo_period_range_kpi -> {
                rcv_sales_ceo_period_range_kpi.visibility = View.GONE
                rcv_labour_ceo_period_range_kpi.visibility = View.GONE
                rcv_service_ceo_period_range_kpi.visibility = View.GONE
                rcv_oer_ceo_period_range_kpi.visibility = View.GONE
                rcv_cash_ceo_period_range_kpi.visibility = View.GONE

                sales_text_overview_ceo_period_range_kpi.visibility = View.GONE
                labour_text_overview_ceo_period_range_kpi.visibility = View.GONE
                service_text_overview_ceo_period_range_kpi.visibility = View.GONE
                oer_text_overview_ceo_period_range_kpi.visibility = View.GONE
                oer_text_overview_ceo_period_range_kpi.visibility = View.GONE

                sales_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_food_ceo_period_range_kpi.visibility == View.VISIBLE) {
                    rcv_food_ceo_period_range_kpi.visibility = View.GONE
                    food_text_overview_ceo_period_range_kpi.visibility = View.GONE
                    food_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_food_ceo_period_range_kpi.visibility = View.VISIBLE
                    food_text_overview_ceo_period_range_kpi.visibility = View.VISIBLE
                    food_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callCeoPeriodRangeLevelTwoQuery(
                        getString(R.string.ideal_vs_food_variance_text),
                        rcv_food_ceo_period_range_kpi
                )
            }
            R.id.labor_parent_layout_ceo_period_range_kpi -> {
                rcv_sales_ceo_period_range_kpi.visibility = View.GONE
                rcv_food_ceo_period_range_kpi.visibility = View.GONE
                rcv_service_ceo_period_range_kpi.visibility = View.GONE
                rcv_oer_ceo_period_range_kpi.visibility = View.GONE
                rcv_cash_ceo_period_range_kpi.visibility = View.GONE

                sales_text_overview_ceo_period_range_kpi.visibility = View.GONE
                food_text_overview_ceo_period_range_kpi.visibility = View.GONE
                service_text_overview_ceo_period_range_kpi.visibility = View.GONE
                oer_text_overview_ceo_period_range_kpi.visibility = View.GONE
                oer_text_overview_ceo_period_range_kpi.visibility = View.GONE

                sales_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_labour_ceo_period_range_kpi.visibility == View.VISIBLE) {
                    rcv_labour_ceo_period_range_kpi.visibility = View.GONE
                    labour_text_overview_ceo_period_range_kpi.visibility = View.GONE
                    labor_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_labour_ceo_period_range_kpi.visibility = View.VISIBLE
                    labour_text_overview_ceo_period_range_kpi.visibility = View.VISIBLE
                    labor_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callCeoPeriodRangeLevelTwoQuery(getString(R.string.labour_text), rcv_labour_ceo_period_range_kpi)

            }
            R.id.service_parent_layout_ceo_period_range_kpi -> {
                rcv_sales_ceo_period_range_kpi.visibility = View.GONE
                rcv_food_ceo_period_range_kpi.visibility = View.GONE
                rcv_labour_ceo_period_range_kpi.visibility = View.GONE
                rcv_oer_ceo_period_range_kpi.visibility = View.GONE
                rcv_cash_ceo_period_range_kpi.visibility = View.GONE

                sales_text_overview_ceo_period_range_kpi.visibility = View.GONE
                food_text_overview_ceo_period_range_kpi.visibility = View.GONE
                labour_text_overview_ceo_period_range_kpi.visibility = View.GONE
                oer_text_overview_ceo_period_range_kpi.visibility = View.GONE
                oer_text_overview_ceo_period_range_kpi.visibility = View.GONE

                sales_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_service_ceo_period_range_kpi.visibility == View.VISIBLE) {
                    rcv_service_ceo_period_range_kpi.visibility = View.GONE
                    service_text_overview_ceo_period_range_kpi.visibility = View.GONE
                    service_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_service_ceo_period_range_kpi.visibility = View.VISIBLE
                    service_text_overview_ceo_period_range_kpi.visibility = View.VISIBLE
                    service_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callCeoPeriodRangeLevelTwoQuery(getString(R.string.service_text), rcv_service_ceo_period_range_kpi)

            }
            R.id.cash_parent_layout_ceo_period_range_kpi -> {
                rcv_sales_ceo_period_range_kpi.visibility = View.GONE
                rcv_food_ceo_period_range_kpi.visibility = View.GONE
                rcv_labour_ceo_period_range_kpi.visibility = View.GONE
                rcv_service_ceo_period_range_kpi.visibility = View.GONE
                rcv_oer_ceo_period_range_kpi.visibility = View.GONE

                sales_text_overview_ceo_period_range_kpi.visibility = View.GONE
                food_text_overview_ceo_period_range_kpi.visibility = View.GONE
                labour_text_overview_ceo_period_range_kpi.visibility = View.GONE
                service_text_overview_ceo_period_range_kpi.visibility = View.GONE
                oer_text_overview_ceo_period_range_kpi.visibility = View.GONE


                sales_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                oer_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_cash_ceo_period_range_kpi.visibility == View.VISIBLE) {
                    rcv_cash_ceo_period_range_kpi.visibility = View.GONE
                    cash_text_overview_ceo_period_range_kpi.visibility = View.GONE
                    cash_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_cash_ceo_period_range_kpi.visibility = View.VISIBLE
                    cash_text_overview_ceo_period_range_kpi.visibility = View.VISIBLE
                    cash_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callCeoPeriodRangeLevelTwoQuery(getString(R.string.cash_text), rcv_cash_ceo_period_range_kpi)

            }
            R.id.oer_parent_layout_ceo_period_range_kpi -> {
                rcv_sales_ceo_period_range_kpi.visibility = View.GONE
                rcv_food_ceo_period_range_kpi.visibility = View.GONE
                rcv_labour_ceo_period_range_kpi.visibility = View.GONE
                rcv_service_ceo_period_range_kpi.visibility = View.GONE
                rcv_cash_ceo_period_range_kpi.visibility = View.GONE

                sales_text_overview_ceo_period_range_kpi.visibility = View.GONE
                food_text_overview_ceo_period_range_kpi.visibility = View.GONE
                labour_text_overview_ceo_period_range_kpi.visibility = View.GONE
                service_text_overview_ceo_period_range_kpi.visibility = View.GONE
                cash_text_overview_ceo_period_range_kpi.visibility = View.GONE

                sales_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                food_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                labor_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                service_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                cash_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)

                if (rcv_oer_ceo_period_range_kpi.visibility == View.VISIBLE) {
                    rcv_oer_ceo_period_range_kpi.visibility = View.GONE
                    oer_text_overview_ceo_period_range_kpi.visibility = View.GONE
                    oer_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
                    return
                } else {
                    rcv_oer_ceo_period_range_kpi.visibility = View.VISIBLE
                    oer_text_overview_ceo_period_range_kpi.visibility = View.VISIBLE
                    oer_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_up)

                }
                callCeoPeriodRangeLevelTwoQuery(getString(R.string.oer_text), rcv_oer_ceo_period_range_kpi)

            }

            R.id.filter_icon -> {
                openFilterCEOPeriodKpiRange()
            }
            R.id.filter_parent_linear -> {
                openFilterCEOPeriodKpiRange()
            }
            R.id.error_filter_parent_linear -> {
                openFilterCEOPeriodKpiRange()
            }

            R.id.sales_text_overview_ceo_period_range_kpi -> {
                callCEOPeriodRangeOverViewKpiApi(getString(R.string.awus_text), "", "")
            }
            R.id.food_text_overview_ceo_period_range_kpi -> {
                callCEOPeriodRangeOverViewKpiApi(getString(R.string.food_text), "", "")
            }
            R.id.labour_text_overview_ceo_period_range_kpi -> {
                callCEOPeriodRangeOverViewKpiApi(getString(R.string.labour_text), "", "")
            }
            R.id.service_text_overview_ceo_period_range_kpi -> {
                callCEOPeriodRangeOverViewKpiApi(getString(R.string.service_text), "", "")
            }
            R.id.oer_text_overview_ceo_period_range_kpi -> {
                callCEOPeriodRangeOverViewKpiApi(getString(R.string.oer_text), "", "")
            }
            R.id.cash_text_overview_ceo_period_range_kpi -> {
                callCEOPeriodRangeOverViewKpiApi(getString(R.string.cash_text), "", "")
            }
        }
    }

    private fun openFilterCEOPeriodKpiRange() {
        val intentFilterCEOPeriodKpiRange = Intent(requireContext(), FilterActivity::class.java)
        startActivity(intentFilterCEOPeriodKpiRange)
    }

    private fun openSalesDetailCEOPeriodKpiRange(salesDetailCEOPeriodKpiRange: CEOOverviewRangeQuery.Ceo) {
        val intentSalesDetailCEOPeriodKpiRange = Intent(requireContext(), AWUSKpiActivity::class.java)
        intentSalesDetailCEOPeriodKpiRange.putExtra("awus_data", gsonCEOPeriodKpiRange.toJson(salesDetailCEOPeriodKpiRange))
        intentSalesDetailCEOPeriodKpiRange.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
        startActivity(intentSalesDetailCEOPeriodKpiRange)

    }

    private fun openCEOLabourDetailPeriodKpi(ceoLabourDetailPeriodKpi: CEOOverviewRangeQuery.Ceo) {
        val intentCeoLabourDetailPeriodKpi = Intent(requireContext(), LabourKpiActivity::class.java)
        intentCeoLabourDetailPeriodKpi.putExtra("labour_data", gsonCEOPeriodKpiRange.toJson(ceoLabourDetailPeriodKpi))
        intentCeoLabourDetailPeriodKpi.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
        startActivity(intentCeoLabourDetailPeriodKpi)
    }

    private fun openServiceCEOPeriodKpiDetail(ceoServicePeriodKpiDetail: CEOOverviewRangeQuery.Ceo) {
        val intentCeoServicePeriodKpiDetail = Intent(requireContext(), ServiceKpiActivity::class.java)
        intentCeoServicePeriodKpiDetail.putExtra("service_data", gsonCEOPeriodKpiRange.toJson(ceoServicePeriodKpiDetail))
        intentCeoServicePeriodKpiDetail.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
        startActivity(intentCeoServicePeriodKpiDetail)
    }

    private fun openOERCEOPeriodKpiDetail(ceoOERPeriodKpiDetail: CEOOverviewRangeQuery.Ceo) {
        val intentCeoOERPeriodKpiDetail = Intent(requireContext(), OERStartActivity::class.java)
        intentCeoOERPeriodKpiDetail.putExtra("oer_data", gsonCEOPeriodKpiRange.toJson(ceoOERPeriodKpiDetail))
        intentCeoOERPeriodKpiDetail.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
        startActivity(intentCeoOERPeriodKpiDetail)
    }

    private fun openFoodCEOPeriodKpiDetail(ceoFoodPeriodKpiDetail: CEOOverviewRangeQuery.Ceo) {
        val intentCeoFoodPeriodKpiDetail = Intent(requireContext(), FoodKpiActivity::class.java)
        intentCeoFoodPeriodKpiDetail.putExtra("food_data", gsonCEOPeriodKpiRange.toJson(ceoFoodPeriodKpiDetail))
        intentCeoFoodPeriodKpiDetail.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
        startActivity(intentCeoFoodPeriodKpiDetail)
    }

    private fun openCASHCEOPeriodKpiDetail(ceoCashPeriodKpiDetail: CEOOverviewRangeQuery.Ceo) {
        val intentCeoCashPeriodKpiDetail = Intent(requireContext(), CashKpiActivity::class.java)
        intentCeoCashPeriodKpiDetail.putExtra("cash_data", gsonCEOPeriodKpiRange.toJson(ceoCashPeriodKpiDetail))
        intentCeoCashPeriodKpiDetail.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
        startActivity(intentCeoCashPeriodKpiDetail)
    }

    private fun refreshCEOPeriodKpiToken() {

        refreshTokenViewModelCeoPeriodRange.getRefreshToken()

        refreshTokenViewModelCeoPeriodRange.refreshTokenResponseLiveData.observe(requireActivity(), {
            run {
                when (it.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        callCEOPeriodRangeLevelOneQuery()
                    }
                    Status.UNSUCCESSFUL -> {
                        CommonUtil.navigateToLogin(mainActivity)
                    }
                    Status.ERROR -> {
                    }
                    Status.OFFLINE -> {
                        setInternetErrorScreenVisibleStateForCeoPeriod()
                    }
                }
            }
        })

    }


    fun setInternetErrorScreenVisibleStateForCeoPeriod() {
        ceo_range_no_internet_error_layout.visibility = View.VISIBLE
        common_header_ceo_range.sales_header_error_image.visibility = View.VISIBLE
        common_header_ceo_range.sales_header_error_image.setImageResource(R.drawable.ic_internet_error)

        setHeaderViewsVisibleStateForCeoPeriod()
        setCalendarViewVisibleStateForCeoPeriod()
        showStoreFilterVisibilityStateForCEOPeriod()
        ceo_period_range_missing_data_error_layout.visibility = View.GONE
        common_header_ceo_range.error_filter_parent_linear.visibility = View.GONE
        ceo_range_error_layout.visibility = View.GONE
    }

    fun setErrorScreenVisibleStateForCeoPeriod(
            title: String,
            description: String
    ) {
        ceo_range_error_layout.visibility = View.VISIBLE
        ceo_range_error_layout.exception_text_title.text = title
        ceo_range_error_layout.exception_text_description.text = description
        common_header_ceo_range.sales_header_error_image.visibility = View.VISIBLE
        common_header_ceo_range.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)
        common_header_ceo_range.error_filter_parent_linear.visibility = View.VISIBLE
        ceo_period_range_missing_data_error_layout.visibility = View.GONE
        setHeaderViewsVisibleStateForCeoPeriod()
        setCalendarViewVisibleStateForCeoPeriod()
        hideStoreFilterVisibilityStateForCEOPeriod()
    }

    fun setHeaderViewsVisibleStateForCeoPeriod() {
        ceo_range_header.visibility = View.GONE
        ceo_range_v1.visibility = View.GONE

        ceo_range_layout.visibility = View.INVISIBLE
        common_header_ceo_range.filter_parent_linear.visibility = View.GONE
        common_header_ceo_range.total_sales_common_header.visibility = View.GONE
        common_header_ceo_range.sales_text_common_header.visibility = View.GONE
    }

    fun hideStoreFilterVisibilityStateForCEOPeriod(){
        common_header_ceo_range.filter_parent_linear.visibility = View.GONE
    }
    fun showStoreFilterVisibilityStateForCEOPeriod(){
        common_header_ceo_range.filter_parent_linear.visibility = View.VISIBLE
    }

    fun setCalendarViewVisibleStateForCeoPeriod() {
        common_calendar_ceo_range.visibility = View.GONE
    }

    fun setMissingDataViewVisibleStateForCeoPeriod(
            missingDataTitle: String,
            missingDataDescription: String
    ) {
        ceo_period_range_missing_data_error_layout.visibility = View.VISIBLE
        ceo_period_range_missing_data_error_layout.header_data_title.text = missingDataTitle
        ceo_period_range_missing_data_error_layout.header_data_description.text = missingDataDescription
    }

    fun hideErrorScreenVisibleStateForCEOPeriod(){
        ceo_range_no_internet_error_layout.visibility = View.GONE
        ceo_range_error_layout.visibility = View.GONE
        common_header_ceo_range.sales_header_error_image.visibility = View.GONE
        common_header_ceo_range.error_filter_parent_linear.visibility = View.GONE

        ceo_range_header.visibility = View.VISIBLE
        ceo_range_v1.visibility = View.VISIBLE
        ceo_range_layout.visibility = View.VISIBLE
        common_header_ceo_range.filter_parent_linear.visibility = View.VISIBLE
        common_header_ceo_range.total_sales_common_header.visibility = View.VISIBLE
        common_header_ceo_range.sales_text_common_header.visibility = View.VISIBLE

        common_calendar_ceo_range.visibility = View.VISIBLE
    }

    fun collapseExpendedListVisibilityForCEOPeriod(){
        if (rcv_sales_ceo_period_range_kpi.visibility == View.VISIBLE) {
            rcv_sales_ceo_period_range_kpi.visibility = View.GONE
            sales_text_overview_ceo_period_range_kpi.visibility = View.GONE
            sales_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_food_ceo_period_range_kpi.visibility == View.VISIBLE) {
            rcv_food_ceo_period_range_kpi.visibility = View.GONE
            food_text_overview_ceo_period_range_kpi.visibility = View.GONE
            food_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_labour_ceo_period_range_kpi.visibility == View.VISIBLE) {
            rcv_labour_ceo_period_range_kpi.visibility = View.GONE
            labour_text_overview_ceo_period_range_kpi.visibility = View.GONE
            labor_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_service_ceo_period_range_kpi.visibility == View.VISIBLE) {
            rcv_service_ceo_period_range_kpi.visibility = View.GONE
            service_text_overview_ceo_period_range_kpi.visibility = View.GONE
            service_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_cash_ceo_period_range_kpi.visibility == View.VISIBLE) {
            rcv_cash_ceo_period_range_kpi.visibility = View.GONE
            cash_text_overview_ceo_period_range_kpi.visibility = View.GONE
            cash_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
        if (rcv_oer_ceo_period_range_kpi.visibility == View.VISIBLE) {
            rcv_oer_ceo_period_range_kpi.visibility = View.GONE
            oer_text_overview_ceo_period_range_kpi.visibility = View.GONE
            oer_parent_img_ceo_period_range_kpi.setImageResource(R.drawable.ic_icon_chevron_down)
        }
    }

}