package com.arria.ping.ui.kpi.gm.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import com.arria.ping.R
import com.arria.ping.apollo.apolloClient
import com.arria.ping.util.NetworkHelper
import com.arria.ping.kpi.MissingDataQuery
import com.arria.ping.kpi.gm.StorePeriodRangeKPIQuery
import com.arria.ping.log.Logger
import com.arria.ping.log.data.QueryData
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.ui.filter.FilterActivity
import com.arria.ping.ui.generalview.MainActivity
import com.arria.ping.ui.kpi.overview.*
import com.arria.ping.ui.refreshtoken.model.Status
import com.arria.ping.ui.refreshtoken.viewmodel.RefreshTokenViewModel
import com.arria.ping.util.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_cash.*
import kotlinx.android.synthetic.main.ceo_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.ceo_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.common_calendar.view.*
import kotlinx.android.synthetic.main.common_header_ceo.*
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.data_error_layout.view.*
import kotlinx.android.synthetic.main.do_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.gm_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.supervisor_period_range_fragment_kpi.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class GMPeriodKpiFragment(
    val filterRange: String,
    val periodRange: String?,
    val apiArgument: String,
    val startDateValue: String,
    val endDateValue: String,
) : androidx.fragment.app.Fragment(), View.OnClickListener {

    lateinit var storeDetailsRangeGMPeriodKpi: StorePeriodRangeKPIQuery.GeneralManager
    private var formattedStartGMPeriodKpiDateValue = ""
    private var formattedEndGMPeriodKpiDateValue = ""

    private val refreshTokenViewModelGMPeriodRange by viewModels<RefreshTokenViewModel>()

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
        return inflater.inflate(R.layout.gm_yesterday_fragment_kpi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Validation().setCustomCalendar(common_calendar_gm_yesterday.square_day)

        formattedStartGMPeriodKpiDateValue = StorePrefData.startDateValue
        formattedEndGMPeriodKpiDateValue = StorePrefData.endDateValue

        if(StorePrefData.filterDate.isNotEmpty()){
            setStoreFilterViewForGMPeriod(StorePrefData.StoreIdFromLogin,StorePrefData.filterDate)
        }


        if (networkHelper.isNetworkConnected()) {
            callMissingDataQueryForGMPeriod()
            callGmPeriodRangeLevel1Query()
        } else {
            setInternetErrorScreenVisibleStateForGMPeriod()
        }

        gm_swipe_refresh_layout.setOnRefreshListener{
            Logger.info("Pull down to refresh GM Period Store Data", "Period KPI")
            callMissingDataQueryForGMPeriod()
            callGmPeriodRangeLevel1Query()
            gm_swipe_refresh_layout.isRefreshing = false
        }

        aws_parent_layout_gm_yesterday_kpi.setOnClickListener(this)
        food_parent_layout_gm_yesterday_kpi.setOnClickListener(this)
        labor_parent_layout_gm_yesterday_kpi.setOnClickListener(this)
        service_parent_layout_gm_yesterday_kpi.setOnClickListener(this)
        oer_parent_layout_gm_yesterday_kpi.setOnClickListener(this)
        cash_parent_layout_gm_yesterday_kpi.setOnClickListener(this)

        filter_icon.setOnClickListener(this)
        filter_parent_linear.setOnClickListener(this)
        error_filter_parent_linear.setOnClickListener(this)
    }


    private fun callMissingDataQueryForGMPeriod() {
        val progressDialogMissingDataGMPeriod = CustomProgressDialog(requireActivity())
        progressDialogMissingDataGMPeriod.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodes: ArrayList<String> = ArrayList()
            val stateCodes: ArrayList<String> = ArrayList()
            val superVisorCode: ArrayList<String> = ArrayList()
            val storeCodes: ArrayList<String> = ArrayList()
            storeCodes.add(StorePrefData.StoreIdFromLogin)

            Logger.info(
                    MissingDataQuery.OPERATION_NAME.name(), "Period Range Missing Data",
                    mapQueryFilters(
                            areaCodes,
                            stateCodes,
                            superVisorCode,
                            storeCodes,
                            MissingDataQuery.QUERY_DOCUMENT
                    )
            )

            try {
                val responseMissingDataGM =
                        apolloClient(requireContext()).query(
                                MissingDataQuery(
                                        areaCodes.toInput(),
                                        stateCodes.toInput(),
                                        superVisorCode.toInput(),
                                        storeCodes.toInput(),
                                        EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                                                .toInput(),
                                        formattedEndGMPeriodKpiDateValue.toInput(),
                                        formattedStartGMPeriodKpiDateValue.toInput()
                                )
                        )
                                .await()

                if (responseMissingDataGM.data?.missingData != null) {
                    progressDialogMissingDataGMPeriod.dismissProgressDialog()
                    setMissingDataViewVisibleStateForGMPeriod(
                            responseMissingDataGM.data?.missingData!!.header.toString(),
                            responseMissingDataGM.data?.missingData!!.message.toString()
                    )
                } else {
                    progressDialogMissingDataGMPeriod.dismissProgressDialog()
                    gm_period_range_missing_data_error_layout.visibility = View.GONE

                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogMissingDataGMPeriod.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Missing Data")
                        }

            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogMissingDataGMPeriod.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForGMPeriod()
                }

            } catch (e: ApolloException) {
                progressDialogMissingDataGMPeriod.dismissProgressDialog()
                Logger.error(e.message.toString(), "Period Range Missing Data")
            }

        }
    }

    private fun callGmPeriodRangeLevel1Query() {
        val progressDialogGM = CustomProgressDialog(requireActivity())
        progressDialogGM.showProgressDialog()
        lifecycleScope.launchWhenResumed {

            if (networkHelper.isNetworkConnected()) {
                hideErrorScreenVisibleStateForGMPeriod()
            }

            val gmPeriodStoreListValue = mutableListOf<String>()
            gmPeriodStoreListValue.add(StorePrefData.StoreIdFromLogin)

            Logger.info(
                    StorePeriodRangeKPIQuery.OPERATION_NAME.name(),
                    "Period Range Store KPI",
                    mapQueryFilters(
                            QueryData(
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    gmPeriodStoreListValue,
                                    formattedEndGMPeriodKpiDateValue,
                                    formattedStartGMPeriodKpiDateValue,
                                    StorePrefData.filterType,
                                    StorePeriodRangeKPIQuery.QUERY_DOCUMENT
                            )
                    )
            )

            try {
                val response =
                        apolloClient(requireContext()).query(
                                StorePeriodRangeKPIQuery(
                                        formattedEndGMPeriodKpiDateValue,
                                        formattedStartGMPeriodKpiDateValue,
                                        StorePrefData.StoreIdFromLogin, EnumMapperUtil.getFilterTypeENUM(
                                        StorePrefData.filterType
                                )
                                )
                        )
                                .await()

                if (response.data?.generalManager != null) {
                    progressDialogGM.dismissProgressDialog()
                    storeDetailsRangeGMPeriodKpi = response.data?.generalManager!!

                    if (response.data?.generalManager?.kpis?.store?.period != null) {
                        setGMPeriodRangeLevelStoreData(response.data?.generalManager?.kpis?.store)
                    } else {
                        setErrorScreenVisibleStateForGMPeriod(
                                getString(R.string.error_text_title),
                                getString(R.string.error_text_description)
                        )

                    }

                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogGM.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Period Range Store KPI")
                        }
                refreshTokenGMPeriodKpi()
                return@launchWhenResumed
            } catch (e: ApolloNetworkException) {
                progressDialogGM.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForGMPeriod()
                }
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Period Range Store KPI")
                progressDialogGM.dismissProgressDialog()
                setErrorScreenVisibleStateForGMPeriod(
                        getString(R.string.exception_error_text_title), getString(
                        R.string
                                .exception_error_text_description
                )
                )
            }

        }
    }

    private fun setGMPeriodRangeLevelStoreData(
            storeDetailGMPeriodKpi: StorePeriodRangeKPIQuery.Store?,
    ) {
        val detailGMPeriodKpi = storeDetailGMPeriodKpi!!.period

        val strGMRangeSelectedDate: String? =
                storeDetailGMPeriodKpi.period?.periodFrom?.let {
                    storeDetailGMPeriodKpi.period.periodTo?.let {it1 ->
                        EnumMapperUtil.getSelectedDate(
                                it,
                                it1, EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                        )
                    }
                }
        if(!strGMRangeSelectedDate.isNullOrEmpty()){
            StorePrefData.filterDate = strGMRangeSelectedDate
            setStoreFilterViewForGMPeriod(storeDetailGMPeriodKpi.storeNumber.toString(),StorePrefData.filterDate)
        }



       val gmPeriodSalesValue = Validation().checkAmountPercentageValue(
                requireActivity(),
                detailGMPeriodKpi?.sales?.actual?.amount,
                detailGMPeriodKpi?.sales?.actual?.percentage,
                detailGMPeriodKpi?.sales?.actual?.value
        )

        if (gmPeriodSalesValue.isEmpty()) {
            hideVisibilityStateOfSalesDataForGMPeriod()
        } else {
            showVisibilityStateOfSalesDataForGMPeriod(gmPeriodSalesValue)
        }


        displaySalesViewForGMPeriodRange(detailGMPeriodKpi?.sales)
        displayFoodViewForGMPeriodRange(detailGMPeriodKpi?.food)
        displayLaborViewForGMPeriodRange(detailGMPeriodKpi?.labor)
        displayEADTServiceViewForGMPeriodRange(detailGMPeriodKpi?.service)
        displayExtremeServiceViewForGMPeriodRange(detailGMPeriodKpi?.service?.extremeDelivery)
        displaySinglesServiceViewForGMPeriodRange(detailGMPeriodKpi?.service?.singles)
        displayCashViewForGMPeriodRange(detailGMPeriodKpi?.cash)
        displayOERViewForGMPeriodRange(detailGMPeriodKpi?.oerStart)
    }

    fun hideVisibilityStateOfSalesDataForGMPeriod(){
        common_header_gm_yesterday.total_sales_common_header.visibility = View.GONE
        common_header_gm_yesterday.sales_text_common_header.visibility = View.GONE
        common_header_gm_yesterday.sales_header_error_image.visibility = View.VISIBLE
        common_header_gm_yesterday.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)

    }
    fun showVisibilityStateOfSalesDataForGMPeriod(gmPeriodSalesValue: String) {
        common_header_gm_yesterday.total_sales_common_header.visibility = View.VISIBLE
        common_header_gm_yesterday.total_sales_common_header.text = gmPeriodSalesValue
        common_header_gm_yesterday.sales_text_common_header.visibility = View.VISIBLE
        common_header_gm_yesterday.sales_text_common_header.text = getString(R.string.sales_text)
        common_header_gm_yesterday.sales_header_error_image.visibility = View.GONE
    }


    fun setStoreFilterViewForGMPeriod(storeNumber: String,date : String){
        val periodTextGMRange = "$storeNumber | $date | ${StorePrefData.isSelectedPeriod}"
        common_header_gm_yesterday.store_header.text = periodTextGMRange
    }

    fun displaySalesViewForGMPeriodRange(sales: StorePeriodRangeKPIQuery.Sales?) {
        if (sales?.displayName != null) {
            aws_display_gm_yesterday_kpi.text = sales.displayName
        }

        val salesGMGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.goal?.amount,
                sales?.goal?.percentage,
                sales?.goal?.value
        )
        val salesGMVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.variance?.amount,
                sales?.variance?.percentage,
                sales?.variance?.value
        )
        val salesGMActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.actual?.amount,
                sales?.actual?.percentage,
                sales?.actual?.value
        )

        if (salesGMGoalRange.isEmpty() && salesGMVarianceRange.isEmpty() && salesGMActualRange.isEmpty()) {

            sales_error_gm_period_range_kpi.visibility = View.VISIBLE
            val paramsGMRangeAWUSError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMRangeAWUSError.weight = 2.0f
            aws_display_gm_yesterday_kpi.layoutParams = paramsGMRangeAWUSError

            sales_goal_gm_yesterday_kpi.visibility = View.GONE
            sales_variance_gm_yesterday_kpi.visibility = View.GONE
            sales_actual_gm_yesterday_kpi.visibility = View.GONE
            aws_parent_layout_gm_yesterday_kpi.isClickable = false
        } else if (salesGMGoalRange.isNotEmpty() && salesGMVarianceRange.isNotEmpty() && salesGMActualRange.isNotEmpty()) {

            sales_error_gm_period_range_kpi.visibility = View.GONE
            sales_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            sales_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            sales_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            sales_goal_gm_yesterday_kpi.text = salesGMGoalRange
            sales_variance_gm_yesterday_kpi.text = salesGMVarianceRange
            sales_actual_gm_yesterday_kpi.text = salesGMActualRange
        } else {

            sales_error_gm_period_range_kpi.visibility = View.GONE
            sales_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            sales_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            sales_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            if (salesGMGoalRange.isEmpty()) {
                sales_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_goal_gm_yesterday_kpi.text = salesGMGoalRange
            }

            if (salesGMVarianceRange.isEmpty()) {
                sales_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_variance_gm_yesterday_kpi.text = salesGMVarianceRange
            }

            if (salesGMActualRange.isEmpty()) {
                sales_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_actual_gm_yesterday_kpi.text = salesGMActualRange
            }
        }

        if (sales?.status?.toString() != null && salesGMActualRange.isNotEmpty()) {
            when {
                sales.status.toString() == resources.getString(R.string.out_of_range) -> {
                    sales_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    sales_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                sales.status.toString() == resources.getString(R.string.under_limit) -> {
                    sales_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    sales_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    sales_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    sales_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

    }

    fun displayFoodViewForGMPeriodRange(food: StorePeriodRangeKPIQuery.Food?) {
        if (food?.displayName != null) {
            food_display_gm_yesterday_kpi.text = food.displayName
        }

        val foodGMGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.goal?.amount,
                food?.goal?.percentage,
                food?.goal?.value
        )
        val foodGMVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.variance?.amount,
                food?.variance?.percentage,
                food?.variance?.value
        )
        val foodGMActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.actual?.amount,
                food?.actual?.percentage,
                food?.actual?.value
        )

        if (foodGMGoalRange.isEmpty() && foodGMVarianceRange.isEmpty() && foodGMActualRange.isEmpty()) {

            food_error_gm_period_range_kpi.visibility = View.VISIBLE
            val paramsGMRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMRangeError.weight = 2.0f
            food_display_gm_yesterday_kpi.layoutParams = paramsGMRangeError

            food_goal_gm_yesterday_kpi.visibility = View.GONE
            food_variance_gm_yesterday_kpi.visibility = View.GONE
            food_actual_gm_yesterday_kpi.visibility = View.GONE
            food_parent_layout_gm_yesterday_kpi.isClickable = false
        } else if (foodGMGoalRange.isNotEmpty() && foodGMVarianceRange.isNotEmpty() && foodGMActualRange.isNotEmpty()) {

            food_error_gm_period_range_kpi.visibility = View.GONE
            food_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            food_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            food_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            food_goal_gm_yesterday_kpi.text = foodGMGoalRange
            food_variance_gm_yesterday_kpi.text = foodGMVarianceRange
            food_actual_gm_yesterday_kpi.text = foodGMActualRange
        } else {

            if (foodGMGoalRange.isEmpty()) {
                food_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_goal_gm_yesterday_kpi.text = foodGMGoalRange
            }

            if (foodGMVarianceRange.isEmpty()) {
                food_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_variance_gm_yesterday_kpi.text = foodGMVarianceRange
            }

            if (foodGMActualRange.isEmpty()) {
                food_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_actual_gm_yesterday_kpi.text = foodGMActualRange
            }

        }

        if (food?.status?.toString() != null && foodGMActualRange.isNotEmpty()) {
            when {
                food.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    food_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                food.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    food_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    food_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    food_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }

    }

    fun displayLaborViewForGMPeriodRange(labor: StorePeriodRangeKPIQuery.Labor?) {
        if (labor?.displayName != null) {
            labour_display_gm_yesterday_kpi.text = labor.displayName
        }

        val labourGMGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.goal?.amount,
                labor?.goal?.percentage,
                labor?.goal?.value
        )
        val labourGMVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.variance?.amount,
                labor?.variance?.percentage,
                labor?.variance?.value
        )
        val labourGMActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.actual?.amount,
                labor?.actual?.percentage,
                labor?.actual?.value
        )

        if (labourGMGoalRange.isEmpty() && labourGMVarianceRange.isEmpty() && labourGMActualRange.isEmpty()) {

            labour_error_gm_period_range_kpi.visibility = View.VISIBLE
            val paramsGMLabourRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMLabourRangeError.weight = 2.0f
            labour_display_gm_yesterday_kpi.layoutParams = paramsGMLabourRangeError

            labour_goal_gm_yesterday_kpi.visibility = View.GONE
            labour_variance_gm_yesterday_kpi.visibility = View.GONE
            labour_actual_gm_yesterday_kpi.visibility = View.GONE
            labor_parent_layout_gm_yesterday_kpi.isClickable = false
        } else if (labourGMGoalRange.isNotEmpty() && labourGMVarianceRange.isNotEmpty() && labourGMActualRange.isNotEmpty()) {

            labour_error_gm_period_range_kpi.visibility = View.GONE
            labour_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            labour_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            labour_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            labour_goal_gm_yesterday_kpi.text = labourGMGoalRange
            labour_variance_gm_yesterday_kpi.text = labourGMVarianceRange
            labour_actual_gm_yesterday_kpi.text = labourGMActualRange
        } else {

            if (labourGMGoalRange.isEmpty()) {
                labour_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_goal_gm_yesterday_kpi.text = labourGMGoalRange
            }

            if (labourGMVarianceRange.isEmpty()) {
                labour_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_variance_gm_yesterday_kpi.text = labourGMVarianceRange
            }

            if (labourGMActualRange.isEmpty()) {
                labour_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_actual_gm_yesterday_kpi.text = labourGMActualRange
            }

        }

        if (labor?.status != null && labourGMActualRange.isNotEmpty()) {
            when {
                labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    labour_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                labor.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    labour_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    labour_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    labour_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayEADTServiceViewForGMPeriodRange(service: StorePeriodRangeKPIQuery.Service?) {
        if (service?.displayName != null) {
            service_display_gm_yesterday_kpi.text = service.displayName
        }

        // service breakdown

        if (service?.eADT?.displayName != null) {
            eADT_display_gm_yesterday_kpi.text = service.eADT.displayName
        }

        val serviceGMGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.goal?.amount,
                service?.eADT?.goal?.percentage,
                service?.eADT?.goal?.value
        )
        val serviceGMVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.variance?.amount,
                service?.eADT?.variance?.percentage,
                service?.eADT?.variance?.value
        )
        val serviceGMActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.actual?.amount,
                service?.eADT?.actual?.percentage,
                service?.eADT?.actual?.value
        )

        if (serviceGMGoalRange.isEmpty() && serviceGMVarianceRange.isEmpty() && serviceGMActualRange.isEmpty()) {

            service_error_gm_period_range_kpi.visibility = View.VISIBLE
            val paramsGMServiceEatRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMServiceEatRangeError.weight = 2.0f
            eADT_display_gm_yesterday_kpi.layoutParams = paramsGMServiceEatRangeError

            service_eADT_goal_gm_yesterday_kpi.visibility = View.GONE
            service_eADT_variance_gm_yesterday_kpi.visibility = View.GONE
            service_eADT_actual_gm_yesterday_kpi.visibility = View.GONE
        } else if (serviceGMGoalRange.isNotEmpty() && serviceGMVarianceRange.isNotEmpty() && serviceGMActualRange.isNotEmpty()) {

            service_error_gm_period_range_kpi.visibility = View.GONE
            service_eADT_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            service_eADT_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            service_eADT_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            service_eADT_goal_gm_yesterday_kpi.text = serviceGMGoalRange
            service_eADT_variance_gm_yesterday_kpi.text = serviceGMVarianceRange
            service_eADT_actual_gm_yesterday_kpi.text = serviceGMActualRange
        } else {

            service_error_gm_period_range_kpi.visibility = View.GONE
            service_eADT_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            service_eADT_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            service_eADT_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            if (serviceGMGoalRange.isEmpty()) {
                service_eADT_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eADT_goal_gm_yesterday_kpi.text = serviceGMGoalRange
            }

            if (serviceGMVarianceRange.isEmpty()) {
                service_eADT_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eADT_variance_gm_yesterday_kpi.text = serviceGMVarianceRange
            }

            if (serviceGMActualRange.isEmpty()) {
                service_eADT_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eADT_actual_gm_yesterday_kpi.text = serviceGMActualRange
            }

        }

        if (service?.eADT?.status != null && serviceGMActualRange.isNotEmpty()) {

            when {
                service.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_eADT_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_eADT_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                service.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_eADT_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_eADT_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_eADT_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_eADT_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayExtremeServiceViewForGMPeriodRange(extremeDelivery: StorePeriodRangeKPIQuery.ExtremeDelivery?) {
        if (extremeDelivery?.displayName != null) {
            extreme_delivery_display_gm_yesterday_kpi.text = extremeDelivery.displayName
        }

        val serviceExtremeGMGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.goal?.amount,
                extremeDelivery?.goal?.percentage,
                extremeDelivery?.goal?.value
        )
        val serviceExtremeGMVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.variance?.amount,
                extremeDelivery?.variance?.percentage,
                extremeDelivery?.variance?.value
        )
        val serviceExtremeGMActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.actual?.amount,
                extremeDelivery?.actual?.percentage,
                extremeDelivery?.actual?.value
        )

        if (serviceExtremeGMGoalRange.isEmpty() && serviceExtremeGMVarianceRange.isEmpty() && serviceExtremeGMActualRange.isEmpty()) {

            serviceExtreme_error_gm_period_range_kpi.visibility = View.VISIBLE
            val paramsGMServiceExtremeDeliveryRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMServiceExtremeDeliveryRangeError.weight = 2.0f
            extreme_delivery_display_gm_yesterday_kpi.layoutParams = paramsGMServiceExtremeDeliveryRangeError


            service_extreme_goal_gm_yesterday_kpi.visibility = View.GONE
            service_extreme_variance_gm_yesterday_kpi.visibility = View.GONE
            service_extreme_actual_gm_yesterday_kpi.visibility = View.GONE
        } else if (serviceExtremeGMGoalRange.isNotEmpty() && serviceExtremeGMVarianceRange.isNotEmpty() && serviceExtremeGMActualRange.isNotEmpty()) {

            serviceExtreme_error_gm_period_range_kpi.visibility = View.GONE
            service_extreme_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            service_extreme_goal_gm_yesterday_kpi.text = serviceExtremeGMGoalRange
            service_extreme_variance_gm_yesterday_kpi.text = serviceExtremeGMVarianceRange
            service_extreme_actual_gm_yesterday_kpi.text = serviceExtremeGMActualRange
        } else {

            serviceExtreme_error_gm_period_range_kpi.visibility = View.GONE

            service_extreme_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            if (serviceExtremeGMGoalRange.isEmpty()) {
                service_extreme_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_goal_gm_yesterday_kpi.text = serviceExtremeGMGoalRange
            }

            if (serviceExtremeGMVarianceRange.isEmpty()) {
                service_extreme_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_variance_gm_yesterday_kpi.text = serviceExtremeGMVarianceRange
            }

            if (serviceExtremeGMActualRange.isEmpty()) {
                service_extreme_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_actual_gm_yesterday_kpi.text = serviceExtremeGMActualRange
            }
        }

        if (extremeDelivery?.status != null && serviceExtremeGMActualRange.isNotEmpty()) {

            when {
                extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_extreme_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_extreme_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_extreme_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    service_extreme_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_extreme_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }
    }

    fun displaySinglesServiceViewForGMPeriodRange(singles: StorePeriodRangeKPIQuery.Singles?) {
        if (singles?.displayName != null) {
            single_display_gm_yesterday_kpi.text = singles
                    .displayName
        }

        val serviceSinglesGMGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.goal?.amount,
                singles?.goal?.percentage,
                singles?.goal?.value
        )
        val serviceSinglesGMVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.variance?.amount,
                singles?.variance?.percentage,
                singles?.variance?.value
        )
        val serviceSinglesGMActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.actual?.amount,
                singles?.actual?.percentage,
                singles?.actual?.value
        )

        if (serviceSinglesGMGoalRange.isEmpty() && serviceSinglesGMVarianceRange.isEmpty() && serviceSinglesGMActualRange.isEmpty()) {

            serviceSingles_error_gm_period_range_kpi.visibility = View.VISIBLE
            val paramsGMServiceSingleRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMServiceSingleRangeError.weight = 2.0f
            single_display_gm_yesterday_kpi.layoutParams = paramsGMServiceSingleRangeError

            service_singles_goal_gm_yesterday_kpi.visibility = View.GONE
            service_singles_variance_gm_yesterday_kpi.visibility = View.GONE
            service_singles_actual_gm_yesterday_kpi.visibility = View.GONE
        } else if (serviceSinglesGMGoalRange.isNotEmpty() && serviceSinglesGMVarianceRange.isNotEmpty() && serviceSinglesGMActualRange.isNotEmpty()) {

            serviceSingles_error_gm_period_range_kpi.visibility = View.GONE
            service_singles_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            service_singles_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            service_singles_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            service_singles_goal_gm_yesterday_kpi.text = serviceSinglesGMGoalRange
            service_singles_variance_gm_yesterday_kpi.text = serviceSinglesGMVarianceRange
            service_singles_actual_gm_yesterday_kpi.text = serviceSinglesGMActualRange
        } else {

            serviceSingles_error_gm_period_range_kpi.visibility = View.GONE
            service_singles_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            service_singles_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            service_singles_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            if (serviceSinglesGMGoalRange.isEmpty()) {
                service_singles_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_goal_gm_yesterday_kpi.text = serviceSinglesGMGoalRange
            }

            if (serviceSinglesGMVarianceRange.isEmpty()) {
                service_singles_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_variance_gm_yesterday_kpi.text = serviceSinglesGMVarianceRange
            }

            if (serviceSinglesGMActualRange.isEmpty()) {
                service_singles_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_actual_gm_yesterday_kpi.text = serviceSinglesGMActualRange
            }

        }
        if (singles?.status != null && serviceSinglesGMActualRange.isNotEmpty()) {

            when {
                singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_singles_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_singles_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_singles_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))

                }
                else -> {
                    service_singles_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_singles_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))

                }
            }
        }
    }

    fun displayCashViewForGMPeriodRange(cash: StorePeriodRangeKPIQuery.Cash?) {
        if (cash?.displayName != null) {
            cash_display_gm_yesterday_kpi.text = cash.displayName
        }

        val cashGMGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.goal?.amount,
                cash?.goal?.percentage,
                cash?.goal?.value
        )
        val cashGMVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.variance?.amount,
                cash?.variance?.percentage,
                cash?.variance?.value
        )
        val cashGMActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.actual?.amount,
                cash?.actual?.percentage,
                cash?.actual?.value
        )

        if (cashGMGoalRange.isEmpty() && cashGMVarianceRange.isEmpty() && cashGMActualRange.isEmpty()) {

            cash_error_gm_period_range_kpi.visibility = View.VISIBLE
            val paramsGMCashRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMCashRangeError.weight = 2.0f
            cash_display_gm_yesterday_kpi.layoutParams = paramsGMCashRangeError


            cash_goal_gm_yesterday_kpi.visibility = View.GONE
            cash_variance_gm_yesterday_kpi.visibility = View.GONE
            cash_actual_gm_yesterday_kpi.visibility = View.GONE
            cash_parent_layout_gm_yesterday_kpi.isClickable = false
        } else if (cashGMGoalRange.isNotEmpty() && cashGMVarianceRange.isNotEmpty() && cashGMActualRange.isNotEmpty()) {

            cash_error_gm_period_range_kpi.visibility = View.GONE
            cash_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            cash_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            cash_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            cash_goal_gm_yesterday_kpi.text = cashGMGoalRange
            cash_variance_gm_yesterday_kpi.text = cashGMVarianceRange
            cash_actual_gm_yesterday_kpi.text = cashGMActualRange
        } else {

            cash_error_gm_period_range_kpi.visibility = View.GONE
            cash_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            cash_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            cash_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            if (cashGMGoalRange.isEmpty()) {
                cash_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_goal_gm_yesterday_kpi.text = cashGMGoalRange
            }

            if (cashGMVarianceRange.isEmpty()) {
                cash_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_variance_gm_yesterday_kpi.text = cashGMVarianceRange
            }

            if (cashGMActualRange.isEmpty()) {
                cash_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_actual_gm_yesterday_kpi.text = cashGMActualRange
            }

        }

        if (cash?.status != null && cashGMActualRange.isNotEmpty()) {
            when {
                cash.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    cash_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))
                }
                cash.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    cash_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    cash_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    cash_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }

        }

    }

    fun displayOERViewForGMPeriodRange(oerStart: StorePeriodRangeKPIQuery.OerStart?) {
        if (oerStart?.displayName != null) {
            oer_display_gm_yesterday_kpi.text = oerStart.displayName
        }

        val oerGoalRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.goal?.amount,
                oerStart?.goal?.percentage,
                oerStart?.goal?.value
        )
        val oerVarianceRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.variance?.amount,
                oerStart?.variance?.percentage,
                oerStart?.variance?.value
        )
        val oerActualRange = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.actual?.amount,
                oerStart?.actual?.percentage,
                oerStart?.actual?.value
        )

        if (oerGoalRange.isEmpty() && oerVarianceRange.isEmpty() && oerActualRange.isEmpty()) {

            oer_error_gm_period_range_kpi.visibility = View.VISIBLE
            val paramsGMOERRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMOERRangeError.weight = 2.0f
            oer_display_gm_yesterday_kpi.layoutParams = paramsGMOERRangeError

            oer_goal_gm_yesterday_kpi.visibility = View.GONE
            oer_variance_gm_yesterday_kpi.visibility = View.GONE
            oer_actual_gm_yesterday_kpi.visibility = View.GONE
            oer_parent_layout_gm_yesterday_kpi.isClickable = false
        } else if (oerGoalRange.isNotEmpty() && oerVarianceRange.isNotEmpty() && oerActualRange.isNotEmpty()) {

            oer_error_gm_period_range_kpi.visibility = View.GONE
            oer_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            oer_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            oer_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            oer_goal_gm_yesterday_kpi.text = oerGoalRange
            oer_variance_gm_yesterday_kpi.text = oerVarianceRange
            oer_actual_gm_yesterday_kpi.text = oerActualRange
        } else {

            oer_error_gm_period_range_kpi.visibility = View.GONE
            oer_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            oer_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            oer_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            if (oerGoalRange.isEmpty()) {
                oer_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_goal_gm_yesterday_kpi.text = oerGoalRange
            }

            if (oerVarianceRange.isEmpty()) {
                oer_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_variance_gm_yesterday_kpi.text = oerVarianceRange
            }

            if (oerActualRange.isEmpty()) {
                oer_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_actual_gm_yesterday_kpi.text = oerActualRange
            }

        }
        if (oerStart?.status != null && oerActualRange.isNotEmpty()) {
            when {
                oerStart.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oer_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    oer_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.red))

                }
                oerStart.status.toString() == resources.getString(R.string.under_limit) -> {
                    oer_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    oer_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.green))
                }
                else -> {
                    oer_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    oer_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(R.color.text_color))
                }
            }
        }

    }

    fun showPreviousDay(date: Date): Date? {
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_MONTH, -1)
        return cal.time
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.aws_parent_layout_gm_yesterday_kpi -> {
                openSalesDetailGMPeriodKpi()
            }
            R.id.food_parent_layout_gm_yesterday_kpi -> {
                openFoodDetailGMPeriodKpi()
            }
            R.id.labor_parent_layout_gm_yesterday_kpi -> {
                openLabourDetailGMPeriodKpi()
            }
            R.id.service_parent_layout_gm_yesterday_kpi -> {
                openServiceDetailGMPeriodKpi()
            }
            R.id.oer_parent_layout_gm_yesterday_kpi -> {
                openOERDetailGMPeriodKpi()
            }
            R.id.cash_parent_layout_gm_yesterday_kpi -> {
                openCASHDetailGMPeriodKpi()
            }
            R.id.filter_icon -> {
                openFilterGMPeriodKpi()
            }
            R.id.filter_parent -> {
                openFilterGMPeriodKpi()
            }
            R.id.error_filter_parent_linear -> {
                openFilterGMPeriodKpi()
            }
        }
    }

    private fun openFilterGMPeriodKpi() {
        val intentGMPeriodKpi = Intent(requireContext(), FilterActivity::class.java)
        startActivity(intentGMPeriodKpi)
    }

    private fun openSalesDetailGMPeriodKpi() {
        try {
            val gsonSalesDetailGMPeriodKpi = Gson()
            val intentSalesDetailGMPeriodKpi = Intent(requireContext(), AWUSKpiActivity::class.java)
            intentSalesDetailGMPeriodKpi.putExtra("awus_data", gsonSalesDetailGMPeriodKpi.toJson(storeDetailsRangeGMPeriodKpi))
            intentSalesDetailGMPeriodKpi.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
            startActivity(intentSalesDetailGMPeriodKpi)
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Period Range Sales Details")
        }
    }

    private fun openLabourDetailGMPeriodKpi() {
        try {
            val gsonLabourDetailGMPeriodKpi = Gson()
            val intentLabourDetailGMPeriodKpi = Intent(requireContext(), LabourKpiActivity::class.java)
            intentLabourDetailGMPeriodKpi.putExtra("labour_data", gsonLabourDetailGMPeriodKpi.toJson(storeDetailsRangeGMPeriodKpi))
            intentLabourDetailGMPeriodKpi.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
            startActivity(intentLabourDetailGMPeriodKpi)
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Period Range Labor Details")
        }
    }

    private fun openServiceDetailGMPeriodKpi() {
        try {
            val gsonServiceDetailGMPeriodKpi = Gson()
            val intentServiceDetailGMPeriodKpi = Intent(requireContext(), ServiceKpiActivity::class.java)
            intentServiceDetailGMPeriodKpi.putExtra("service_data", gsonServiceDetailGMPeriodKpi.toJson(storeDetailsRangeGMPeriodKpi))
            intentServiceDetailGMPeriodKpi.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
            startActivity(intentServiceDetailGMPeriodKpi)
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Period Range Service Details")
        }
    }

    private fun openOERDetailGMPeriodKpi() {
        try {
            val gsonOERDetailGMPeriodKpi = Gson()
            val intentOERDetailGMPeriodKpi = Intent(requireContext(), OERStartActivity::class.java)
            intentOERDetailGMPeriodKpi.putExtra("oer_data", gsonOERDetailGMPeriodKpi.toJson(storeDetailsRangeGMPeriodKpi))
            intentOERDetailGMPeriodKpi.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
            startActivity(intentOERDetailGMPeriodKpi)
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Period Range OER Details")
        }
    }

    private fun openFoodDetailGMPeriodKpi() {
        try {
            val gsonFoodDetailGMPeriodKpi = Gson()
            val intentFoodDetailGMPeriodKpi = Intent(requireContext(), FoodKpiActivity::class.java)
            intentFoodDetailGMPeriodKpi.putExtra("food_data", gsonFoodDetailGMPeriodKpi.toJson(storeDetailsRangeGMPeriodKpi))
            intentFoodDetailGMPeriodKpi.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
            startActivity(intentFoodDetailGMPeriodKpi)
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Period Range Food Details")
        }
    }

    private fun openCASHDetailGMPeriodKpi() {
        try {
            val gsonCASHDetailGMPeriodKpi = Gson()
            val intentCASHDetailGMPeriodKpi = Intent(requireContext(), CashKpiActivity::class.java)
            intentCASHDetailGMPeriodKpi.putExtra("cash_data", gsonCASHDetailGMPeriodKpi.toJson(storeDetailsRangeGMPeriodKpi))
            intentCASHDetailGMPeriodKpi.putExtra("api_argument_from_filter", IpConstants.rangeFrom)
            startActivity(intentCASHDetailGMPeriodKpi)
        } catch (e: Exception) {
            Logger.error(e.message.toString(),"Period Range Cash Details")
        }
    }

    private fun refreshTokenGMPeriodKpi() {
        refreshTokenViewModelGMPeriodRange.getRefreshToken()

        refreshTokenViewModelGMPeriodRange.refreshTokenResponseLiveData.observe(requireActivity(), {
            run {
                when (it.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        callGmPeriodRangeLevel1Query()
                    }
                    Status.UNSUCCESSFUL -> {
                        CommonUtil.navigateToLogin(mainActivity)
                    }
                    Status.ERROR -> {
                    }
                    Status.OFFLINE -> {
                        setInternetErrorScreenVisibleStateForGMPeriod()
                    }
                }
            }
        })
    }

    fun setErrorScreenVisibleStateForGMPeriod(
            title: String,
            description: String
    ) {
        gm_yesterday_error_layout.visibility = View.VISIBLE
        gm_yesterday_error_layout.exception_text_title.text = title
        gm_yesterday_error_layout.exception_text_description.text = description
        common_header_gm_yesterday.sales_header_error_image.visibility = View.VISIBLE
        common_header_gm_yesterday.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)
        common_header_gm_yesterday.error_filter_parent_linear.visibility = View.VISIBLE
        gm_period_range_missing_data_error_layout.visibility = View.GONE
        setHeaderViewsVisibleStateForGMPeriod()
        setCalendarViewVisibleStateForGMPeriod()
        hideStoreFilterVisibilityStateForGMPeriod()
    }

    fun setInternetErrorScreenVisibleStateForGMPeriod() {
        gm_yesterday_no_internet_error_layout.visibility = View.VISIBLE
        common_header_gm_yesterday.sales_header_error_image.visibility = View.VISIBLE
        common_header_gm_yesterday.sales_header_error_image.setImageResource(R.drawable.ic_internet_error)
        gm_period_range_missing_data_error_layout.visibility = View.GONE
        common_header_gm_yesterday.error_filter_parent_linear.visibility = View.GONE
        gm_yesterday_error_layout.visibility = View.GONE
        setCalendarViewVisibleStateForGMPeriod()
        setHeaderViewsVisibleStateForGMPeriod()
        showStoreFilterVisibilityStateForGMPeriod()
    }

    fun setHeaderViewsVisibleStateForGMPeriod() {
        goal_parent_yesterday_kpi.visibility = View.GONE
        gm_yesterday_v1.visibility = View.GONE
        gm_yesterday_layout.visibility = View.INVISIBLE
        common_header_gm_yesterday.total_sales_common_header.visibility = View.GONE
        common_header_gm_yesterday.sales_text_common_header.visibility = View.GONE
    }
    fun hideStoreFilterVisibilityStateForGMPeriod(){
        common_header_gm_yesterday.filter_parent_linear.visibility = View.GONE
    }
    fun showStoreFilterVisibilityStateForGMPeriod(){
        common_header_gm_yesterday.filter_parent_linear.visibility = View.VISIBLE
    }

    fun setCalendarViewVisibleStateForGMPeriod() {
        common_calendar_gm_yesterday.visibility = View.GONE
    }

    fun setMissingDataViewVisibleStateForGMPeriod(
            missingDataTitle: String,
            missingDataDescription: String
    ) {
            gm_period_range_missing_data_error_layout.visibility = View.VISIBLE
            gm_period_range_missing_data_error_layout.header_data_title.text = missingDataTitle
            gm_period_range_missing_data_error_layout.header_data_description.text = missingDataDescription

    }

    fun hideErrorScreenVisibleStateForGMPeriod(){
        gm_yesterday_no_internet_error_layout.visibility = View.GONE
        gm_yesterday_error_layout.visibility = View.GONE
        common_header_gm_yesterday.sales_header_error_image.visibility = View.GONE
        common_header_gm_yesterday.error_filter_parent_linear.visibility = View.GONE

        common_header_gm_yesterday.filter_parent_linear.visibility = View.VISIBLE
        common_header_gm_yesterday.total_sales_common_header.visibility = View.VISIBLE
        common_header_gm_yesterday.sales_text_common_header.visibility = View.VISIBLE
        common_calendar_gm_yesterday.visibility = View.VISIBLE

        goal_parent_yesterday_kpi.visibility = View.VISIBLE
        gm_yesterday_v1.visibility = View.VISIBLE
        gm_yesterday_layout.visibility = View.VISIBLE
    }

}