package com.arria.ping.ui.kpi.gm.view

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
import com.arria.ping.R.color
import com.arria.ping.apollo.apolloClient
import com.arria.ping.util.NetworkHelper
import com.arria.ping.kpi.MissingDataQuery
import com.arria.ping.kpi.gm.StoreYesterdayKPIQuery
import com.arria.ping.log.Logger
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.ui.filter.FilterActivity
import com.arria.ping.ui.generalview.MainActivity
import com.arria.ping.ui.kpi.overview.*
import com.arria.ping.ui.refreshtoken.model.Status
import com.arria.ping.ui.refreshtoken.viewmodel.RefreshTokenViewModel
import com.arria.ping.util.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bonus_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.ceo_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.common_calendar.view.*
import kotlinx.android.synthetic.main.common_header_ceo.*
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.data_error_layout.view.*
import kotlinx.android.synthetic.main.do_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.gm_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.supervisor_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.supervisor_yesterday_fragment_kpi.*
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class GMYesterdayKpiFragment : Fragment(), View.OnClickListener {

    lateinit var storeDetailsYesterdayKpi: StoreYesterdayKPIQuery.GeneralManager

    @Inject
    lateinit var networkHelper: NetworkHelper

    private val gsonYesterdayKpi = Gson()

    private val refreshTokenViewModelGMYesterday by viewModels<RefreshTokenViewModel>()

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

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        initYesterdayKpi()
    }

    private fun initYesterdayKpi() {

        Validation().setCustomCalendar(common_calendar_gm_yesterday.square_day)

        aws_parent_layout_gm_yesterday_kpi.setOnClickListener(this)
        food_parent_layout_gm_yesterday_kpi.setOnClickListener(this)
        labor_parent_layout_gm_yesterday_kpi.setOnClickListener(this)
        service_parent_layout_gm_yesterday_kpi.setOnClickListener(this)
        oer_parent_layout_gm_yesterday_kpi.setOnClickListener(this)
        cash_parent_layout_gm_yesterday_kpi.setOnClickListener(this)

        filter_icon.setOnClickListener(this)
        filter_parent_linear.setOnClickListener(this)
        error_filter_parent_linear.setOnClickListener(this)

        gm_swipe_refresh_layout.setOnRefreshListener{
            Logger.info("Pull down to refresh GM Yesterday Store Data", "Yesterday KPI")
            callMissingDataQueryForGmYesterday()
            callGmYesterdayLevel1Query()
            gm_swipe_refresh_layout.isRefreshing = false
        }

        if(StorePrefData.filterDate.isNotEmpty()){
            setStoreFilterViewForGMYesterday(StorePrefData.StoreIdFromLogin,StorePrefData.filterDate)
        }


        if (networkHelper.isNetworkConnected()) {
            callMissingDataQueryForGmYesterday()
            callGmYesterdayLevel1Query()
        } else {
            setInternetErrorScreenVisibleStateForGMYesterday()
        }

    }

    private fun callGmYesterdayLevel1Query() {
        val progressDialogGMYesterday = CustomProgressDialog(requireActivity())
        progressDialogGMYesterday.showProgressDialog()
        lifecycleScope.launchWhenResumed {

            if (networkHelper.isNetworkConnected()) {
                hideErrorScreenVisibleStateForGMYesterday()
            }

            val gmYesterdayStoreListValue = mutableListOf<String>()
            gmYesterdayStoreListValue.add(StorePrefData.StoreIdFromLogin)
            Logger.info(
                    StoreYesterdayKPIQuery.OPERATION_NAME.name(),
                    "Yesterday Store KPI",
                    mapQueryFilters(
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            gmYesterdayStoreListValue,
                            StoreYesterdayKPIQuery.QUERY_DOCUMENT
                    )
            )
            try {
                val responseGMYesterdayLevelOne =
                        apolloClient(requireContext()).query(StoreYesterdayKPIQuery(StorePrefData.StoreIdFromLogin))
                                .await()
                if (responseGMYesterdayLevelOne.data?.generalManager != null) {
                    progressDialogGMYesterday.dismissProgressDialog()
                    storeDetailsYesterdayKpi = responseGMYesterdayLevelOne.data?.generalManager!!

                    if (storeDetailsYesterdayKpi.kpis?.store?.yesterday != null) {
                        setGMYesterdayStoreData(
                                storeDetailsYesterdayKpi.kpis?.store?.yesterday,
                                storeDetailsYesterdayKpi.kpis?.store
                        )
                    } else {
                        setErrorScreenVisibleStateForGMYesterday(
                                getString(R.string.error_text_title),
                                getString(R.string.error_text_description)
                        )
                    }

                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogGMYesterday.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Store KPI")
                        }
                refreshTokenKpiYesterday()
                return@launchWhenResumed
            } catch (apolloNetworkException: ApolloNetworkException) {

                progressDialogGMYesterday.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    setInternetErrorScreenVisibleStateForGMYesterday()
                }
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Yesterday Store KPI")
                progressDialogGMYesterday.dismissProgressDialog()
                setErrorScreenVisibleStateForGMYesterday(
                        getString(R.string.exception_error_text_title), getString(
                        R.string
                                .exception_error_text_description
                )
                )

            }

        }
    }

    private fun setGMYesterdayStoreData(
            detailGMYesterdayKpi: StoreYesterdayKPIQuery.Yesterday?,
            storeDetail: StoreYesterdayKPIQuery.Store?,
    ) {

        if(detailGMYesterdayKpi?.periodFrom != null){
            StorePrefData.dayOfLastServiceDate = detailGMYesterdayKpi.periodFrom
            setStoreFilterViewForGMYesterday(storeDetail?.storeNumber.toString(),StorePrefData.dayOfLastServiceDate)
        }


        val strGMYesterdaySelectedDate: String? =
                detailGMYesterdayKpi?.periodFrom?.let {
                    detailGMYesterdayKpi.periodTo?.let {it1 ->
                        EnumMapperUtil.getSelectedDate(
                                it,
                                it1, EnumMapperUtil.getFilterTypeENUM(StorePrefData.filterType)
                        )
                    }
                }
        if(!strGMYesterdaySelectedDate.isNullOrEmpty()){
            StorePrefData.filterDate = strGMYesterdaySelectedDate
            setStoreFilterViewForGMYesterday(storeDetail?.storeNumber.toString(),StorePrefData.filterDate)
        }

        val gmYesterdaySalesValue = Validation().checkAmountPercentageValue(
                requireActivity(),
                detailGMYesterdayKpi?.sales?.actual?.amount,
                detailGMYesterdayKpi?.sales?.actual?.percentage,
                detailGMYesterdayKpi?.sales?.actual?.value
        )
        if (gmYesterdaySalesValue.isEmpty()) {
            hideVisibilityStateOfSalesDataForGMYesterday()
        } else {
            showVisibilityStateOfSalesDataForGMYesterday(gmYesterdaySalesValue)
        }

        displaySalesViewForGmYesterday(detailGMYesterdayKpi?.sales)
        displayFoodViewForGmYesterday(detailGMYesterdayKpi?.food)
        displayLaborViewForGmYesterday(detailGMYesterdayKpi?.labor)
        displayEADTServiceViewForGmYesterday(detailGMYesterdayKpi?.service)
        displayExtremeServiceViewForGmYesterday(detailGMYesterdayKpi?.service?.extremeDelivery)
        displaySinglesServiceViewForGmYesterday(detailGMYesterdayKpi?.service?.singles)
        displayCashViewForGmYesterday(detailGMYesterdayKpi?.cash)
        displayOERViewForGmYesterday(detailGMYesterdayKpi?.oerStart)
    }
 fun setStoreFilterViewForGMYesterday(storeNumber: String,date : String){

     val periodTextGMYesterday = "$storeNumber | $date | ${getString(R.string.yesterday_text)}"

     common_header_gm_yesterday.store_header.text = periodTextGMYesterday

 }
    fun hideVisibilityStateOfSalesDataForGMYesterday(){
        common_header_gm_yesterday.total_sales_common_header.visibility = View.GONE
        common_header_gm_yesterday.sales_text_common_header.visibility = View.GONE
        common_header_gm_yesterday.sales_header_error_image.visibility = View.VISIBLE
        common_header_gm_yesterday.sales_header_error_image.setImageResource(R.drawable.ic_data_unavailbale_error)
    }
    fun showVisibilityStateOfSalesDataForGMYesterday(gmYesterdaySalesValue: String) {
        common_header_gm_yesterday.total_sales_common_header.visibility = View.VISIBLE
        common_header_gm_yesterday.total_sales_common_header.text = gmYesterdaySalesValue
        common_header_gm_yesterday.sales_text_common_header.visibility = View.VISIBLE
        common_header_gm_yesterday.sales_text_common_header.text = getString(R.string.sales_text)
        common_header_gm_yesterday.sales_header_error_image.visibility = View.GONE
    }


    fun displaySalesViewForGmYesterday(sales: StoreYesterdayKPIQuery.Sales?) {
        if (sales?.displayName != null) {
            aws_display_gm_yesterday_kpi.text = sales.displayName
        }
        val salesGMYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.goal?.amount,
                sales?.goal?.percentage,
                sales?.goal?.value
        )
        val salesGMYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.variance?.amount,
                sales?.variance?.percentage,
                sales?.variance?.value
        )
        val salesGMYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                sales?.actual?.amount,
                sales?.actual?.percentage,
                sales?.actual?.value
        )

        if (salesGMYesterdayGoal.isEmpty() && salesGMYesterdayVariance.isEmpty() && salesGMYesterdayActual.isEmpty()) {
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
        } else if (salesGMYesterdayGoal.isNotEmpty() && salesGMYesterdayVariance.isNotEmpty() && salesGMYesterdayActual.isNotEmpty()) {

            sales_error_gm_period_range_kpi.visibility = View.GONE
            sales_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            sales_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            sales_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            sales_goal_gm_yesterday_kpi.text = salesGMYesterdayGoal
            sales_variance_gm_yesterday_kpi.text = salesGMYesterdayVariance
            sales_actual_gm_yesterday_kpi.text = salesGMYesterdayActual
        } else {
            sales_error_gm_period_range_kpi.visibility = View.GONE
            sales_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            sales_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            sales_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            if (salesGMYesterdayGoal.isEmpty()) {
                sales_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_goal_gm_yesterday_kpi.text = salesGMYesterdayGoal
            }

            if (salesGMYesterdayVariance.isEmpty()) {
                sales_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_variance_gm_yesterday_kpi.text = salesGMYesterdayVariance
            }

            if (salesGMYesterdayActual.isEmpty()) {
                sales_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                sales_actual_gm_yesterday_kpi.text = salesGMYesterdayActual
            }
        }

        if (sales?.status?.toString() != null && salesGMYesterdayActual.isNotEmpty()) {
            when {
                sales.status.toString() == resources.getString(R.string.out_of_range) -> {
                    sales_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    sales_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.red))
                }
                sales.status.toString() == resources.getString(R.string.under_limit) -> {
                    sales_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    sales_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.green))

                }
                else -> {
                    sales_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    sales_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.text_color))

                }
            }
        }
    }

    fun displayFoodViewForGmYesterday(food: StoreYesterdayKPIQuery.Food?) {
        if (food?.displayName != null) {
            food_display_gm_yesterday_kpi.text = food.displayName
        }
        val foodGMYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.goal?.amount,
                food?.goal?.percentage,
                food?.goal?.value
        )
        val foodGMYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.variance?.amount,
                food?.variance?.percentage,
                food?.variance?.value
        )
        val foodGMYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.actual?.amount,
                food?.actual?.percentage,
                food?.actual?.value
        )

        if (foodGMYesterdayGoal.isEmpty() && foodGMYesterdayVariance.isEmpty() && foodGMYesterdayActual.isEmpty()) {
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
        } else if (foodGMYesterdayGoal.isNotEmpty() && foodGMYesterdayVariance.isNotEmpty() && foodGMYesterdayActual.isNotEmpty()) {

            food_error_gm_period_range_kpi.visibility = View.GONE
            food_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            food_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            food_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            food_goal_gm_yesterday_kpi.text = foodGMYesterdayGoal
            food_variance_gm_yesterday_kpi.text = foodGMYesterdayVariance
            food_actual_gm_yesterday_kpi.text = foodGMYesterdayActual
        } else {
            if (foodGMYesterdayGoal.isEmpty()) {
                food_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_goal_gm_yesterday_kpi.text = foodGMYesterdayGoal
            }

            if (foodGMYesterdayVariance.isEmpty()) {
                food_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_variance_gm_yesterday_kpi.text = foodGMYesterdayVariance
            }

            if (foodGMYesterdayActual.isEmpty()) {
                food_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_actual_gm_yesterday_kpi.text = foodGMYesterdayActual
            }

        }

        if (food?.status?.toString() != null && foodGMYesterdayActual.isNotEmpty()) {
            when {
                food.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    food_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.red))
                }
                food.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    food_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.green))

                }
                else -> {
                    food_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    food_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.text_color))

                }
            }
        }
    }

    fun displayLaborViewForGmYesterday(labor: StoreYesterdayKPIQuery.Labor?) {
        if (labor?.displayName != null) {
            labour_display_gm_yesterday_kpi.text = labor.displayName
        }

        val labourGMYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.goal?.amount,
                labor?.goal?.percentage,
                labor?.goal?.value
        )
        val labourGMYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.variance?.amount,
                labor?.variance?.percentage,
                labor?.variance?.value
        )
        val labourGMYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.actual?.amount,
                labor?.actual?.percentage,
                labor?.actual?.value
        )

        if (labourGMYesterdayGoal.isEmpty() && labourGMYesterdayVariance.isEmpty() && labourGMYesterdayActual.isEmpty()) {
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
        } else if (labourGMYesterdayGoal.isNotEmpty() && labourGMYesterdayVariance.isNotEmpty() && labourGMYesterdayActual.isNotEmpty()) {

            labour_error_gm_period_range_kpi.visibility = View.GONE
            labour_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            labour_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            labour_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            labour_goal_gm_yesterday_kpi.text = labourGMYesterdayGoal
            labour_variance_gm_yesterday_kpi.text = labourGMYesterdayVariance
            labour_actual_gm_yesterday_kpi.text = labourGMYesterdayActual
        } else {
            if (labourGMYesterdayGoal.isEmpty()) {
                labour_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_goal_gm_yesterday_kpi.text = labourGMYesterdayGoal
            }

            if (labourGMYesterdayVariance.isEmpty()) {
                labour_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_variance_gm_yesterday_kpi.text = labourGMYesterdayVariance
            }

            if (labourGMYesterdayActual.isEmpty()) {
                labour_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_actual_gm_yesterday_kpi.text = labourGMYesterdayActual
            }

        }

        if (labor?.status != null && labourGMYesterdayActual.isNotEmpty()) {
            when {
                labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    labour_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.red))

                }
                labor.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    labour_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.green))

                }
                else -> {
                    labour_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    labour_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.text_color))

                }
            }
        }
    }

    fun displayEADTServiceViewForGmYesterday(service: StoreYesterdayKPIQuery.Service?) {
        if (service?.displayName != null) {
            service_display_gm_yesterday_kpi.text = service.displayName
        }

        if (service?.eADT?.displayName != null) {
            eADT_display_gm_yesterday_kpi.text = service.eADT.displayName
        }

        val serviceEdtGMYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.goal?.amount,
                service?.eADT?.goal?.percentage,
                service?.eADT?.goal?.value
        )
        val serviceEdtGMYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.variance?.amount,
                service?.eADT?.variance?.percentage,
                service?.eADT?.variance?.value
        )
        val serviceEdtGMYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.eADT?.actual?.amount,
                service?.eADT?.actual?.percentage,
                service?.eADT?.actual?.value
        )

        if (serviceEdtGMYesterdayGoal.isEmpty() && serviceEdtGMYesterdayVariance.isEmpty() && serviceEdtGMYesterdayActual.isEmpty()) {
            service_error_gm_period_range_kpi.visibility = View.VISIBLE
            val paramsGMServiceEatRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMServiceEatRangeError.weight = 2.0f
            eADT_display_gm_yesterday_kpi.layoutParams = paramsGMServiceEatRangeError

            service_eADT_goal_gm_yesterday_kpi.visibility = View.GONE
            service_eADT_variance_gm_yesterday_kpi.visibility = View.GONE
            service_eADT_actual_gm_yesterday_kpi.visibility = View.GONE
        } else if (serviceEdtGMYesterdayGoal.isNotEmpty() && serviceEdtGMYesterdayVariance.isNotEmpty() && serviceEdtGMYesterdayActual.isNotEmpty()) {

            service_error_gm_period_range_kpi.visibility = View.GONE
            service_eADT_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            service_eADT_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            service_eADT_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            service_eADT_goal_gm_yesterday_kpi.text = serviceEdtGMYesterdayGoal
            service_eADT_variance_gm_yesterday_kpi.text = serviceEdtGMYesterdayVariance
            service_eADT_actual_gm_yesterday_kpi.text = serviceEdtGMYesterdayActual
        } else {
            service_error_gm_period_range_kpi.visibility = View.GONE
            service_eADT_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            service_eADT_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            service_eADT_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            if (serviceEdtGMYesterdayGoal.isEmpty()) {
                service_eADT_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eADT_goal_gm_yesterday_kpi.text = serviceEdtGMYesterdayGoal
            }

            if (serviceEdtGMYesterdayVariance.isEmpty()) {
                service_eADT_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eADT_variance_gm_yesterday_kpi.text = serviceEdtGMYesterdayVariance
            }

            if (serviceEdtGMYesterdayActual.isEmpty()) {
                service_eADT_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eADT_actual_gm_yesterday_kpi.text = serviceEdtGMYesterdayActual
            }

        }
        if (service?.eADT?.status != null && serviceEdtGMYesterdayActual.isNotEmpty()) {
            when {
                service.eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_eADT_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_eADT_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.red))

                }
                service.eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_eADT_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_eADT_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.green))

                }
                else -> {
                    service_eADT_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_eADT_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.text_color))

                }
            }
        }
    }

    fun displayExtremeServiceViewForGmYesterday(extremeDelivery: StoreYesterdayKPIQuery.ExtremeDelivery?) {
        if (extremeDelivery?.displayName != null) {
            extreme_delivery_display_gm_yesterday_kpi.text = extremeDelivery.displayName
        }

        val serviceExtremeGMYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.goal?.amount,
                extremeDelivery?.goal?.percentage,
                extremeDelivery?.goal?.value
        )
        val serviceExtremeGMYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.variance?.amount,
                extremeDelivery?.variance?.percentage,
                extremeDelivery?.variance?.value
        )
        val serviceExtremeGMYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.actual?.amount,
                extremeDelivery?.actual?.percentage,
                extremeDelivery?.actual?.value
        )

        if (serviceExtremeGMYesterdayGoal.isEmpty() && serviceExtremeGMYesterdayVariance.isEmpty() && serviceExtremeGMYesterdayActual.isEmpty()) {
            serviceExtreme_error_gm_period_range_kpi.visibility = View.VISIBLE
            val paramsGMServiceExtremeDeliveryRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMServiceExtremeDeliveryRangeError.weight = 2.0f
            extreme_delivery_display_gm_yesterday_kpi.layoutParams = paramsGMServiceExtremeDeliveryRangeError


            service_extreme_goal_gm_yesterday_kpi.visibility = View.GONE
            service_extreme_variance_gm_yesterday_kpi.visibility = View.GONE
            service_extreme_actual_gm_yesterday_kpi.visibility = View.GONE
        } else if (serviceExtremeGMYesterdayGoal.isNotEmpty() && serviceExtremeGMYesterdayVariance.isNotEmpty() && serviceExtremeGMYesterdayActual.isNotEmpty()) {

            serviceExtreme_error_gm_period_range_kpi.visibility = View.GONE
            service_extreme_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            service_extreme_goal_gm_yesterday_kpi.text = serviceExtremeGMYesterdayGoal
            service_extreme_variance_gm_yesterday_kpi.text = serviceExtremeGMYesterdayVariance
            service_extreme_actual_gm_yesterday_kpi.text = serviceExtremeGMYesterdayActual
        } else {
            serviceExtreme_error_gm_period_range_kpi.visibility = View.GONE
            service_extreme_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            service_extreme_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            if (serviceExtremeGMYesterdayGoal.isEmpty()) {
                service_extreme_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_goal_gm_yesterday_kpi.text = serviceExtremeGMYesterdayGoal
            }

            if (serviceExtremeGMYesterdayVariance.isEmpty()) {
                service_extreme_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_variance_gm_yesterday_kpi.text = serviceExtremeGMYesterdayVariance
            }

            if (serviceExtremeGMYesterdayActual.isEmpty()) {
                service_extreme_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_actual_gm_yesterday_kpi.text = serviceExtremeGMYesterdayActual
            }
        }

        if (extremeDelivery?.status != null && serviceExtremeGMYesterdayActual.isNotEmpty()) {
            when {
                extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_extreme_actual_gm_yesterday_kpi.setTextColor(
                            requireContext().getColor(
                                    color.red
                            )
                    )
                }
                extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_extreme_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_extreme_actual_gm_yesterday_kpi.setTextColor(
                            requireContext().getColor(
                                    color.green
                            )
                    )
                }
                else -> {
                    service_extreme_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_extreme_actual_gm_yesterday_kpi.setTextColor(
                            requireContext().getColor(
                                    color.text_color
                            )
                    )
                }
            }
        }
    }

    fun displaySinglesServiceViewForGmYesterday(singles: StoreYesterdayKPIQuery.Singles?) {

        if (singles?.displayName != null) {
            single_display_gm_yesterday_kpi.text = singles.displayName
        }
        val serviceSingleGMYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.goal?.amount,
                singles?.goal?.percentage,
                singles?.goal?.value
        )
        val serviceSingleGMYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.variance?.amount,
                singles?.variance?.percentage,
                singles?.variance?.value
        )
        val serviceSingleGMYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.actual?.amount,
                singles?.actual?.percentage,
                singles?.actual?.value
        )

        if (serviceSingleGMYesterdayGoal.isEmpty() && serviceSingleGMYesterdayVariance.isEmpty() && serviceSingleGMYesterdayActual.isEmpty()) {
            serviceSingles_error_gm_period_range_kpi.visibility = View.VISIBLE
            val paramsGMServiceSingleRangeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMServiceSingleRangeError.weight = 2.0f
            single_display_gm_yesterday_kpi.layoutParams = paramsGMServiceSingleRangeError

            service_singles_goal_gm_yesterday_kpi.visibility = View.GONE
            service_singles_variance_gm_yesterday_kpi.visibility = View.GONE
            service_singles_actual_gm_yesterday_kpi.visibility = View.GONE
        } else if (serviceSingleGMYesterdayGoal.isNotEmpty() && serviceSingleGMYesterdayVariance.isNotEmpty() && serviceSingleGMYesterdayActual.isNotEmpty()) {

            serviceSingles_error_gm_period_range_kpi.visibility = View.GONE
            service_singles_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            service_singles_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            service_singles_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            service_singles_goal_gm_yesterday_kpi.text = serviceSingleGMYesterdayGoal
            service_singles_variance_gm_yesterday_kpi.text = serviceSingleGMYesterdayVariance
            service_singles_actual_gm_yesterday_kpi.text = serviceSingleGMYesterdayActual
        } else {
            serviceSingles_error_gm_period_range_kpi.visibility = View.GONE
            service_singles_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            service_singles_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            service_singles_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            if (serviceSingleGMYesterdayGoal.isEmpty()) {
                service_singles_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_goal_gm_yesterday_kpi.text = serviceSingleGMYesterdayGoal
            }

            if (serviceSingleGMYesterdayVariance.isEmpty()) {
                service_singles_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_variance_gm_yesterday_kpi.text = serviceSingleGMYesterdayVariance
            }

            if (serviceSingleGMYesterdayActual.isEmpty()) {
                service_singles_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_actual_gm_yesterday_kpi.text = serviceSingleGMYesterdayActual
            }

        }
        if (singles?.status != null && serviceSingleGMYesterdayActual.isNotEmpty()) {
            when {
                singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_singles_actual_gm_yesterday_kpi.setTextColor(
                            requireContext().getColor(
                                    color.red
                            )
                    )

                }
                singles.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_singles_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_singles_actual_gm_yesterday_kpi.setTextColor(
                            requireContext().getColor(
                                    color.green
                            )
                    )

                }
                else -> {
                    service_singles_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_singles_actual_gm_yesterday_kpi.setTextColor(
                            requireContext().getColor(
                                    color.text_color
                            )
                    )

                }
            }
        }
    }

    fun displayCashViewForGmYesterday(cash: StoreYesterdayKPIQuery.Cash?) {

        if (cash?.displayName != null) {
            cash_display_gm_yesterday_kpi.text = cash.displayName
        } else getString(R.string.cash_text)
        val cashYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.goal?.amount,
                cash?.goal?.percentage,
                cash?.goal?.value
        )
        val cashYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.variance?.amount,
                cash?.variance?.percentage,
                cash?.variance?.value
        )
        val cashYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                cash?.actual?.amount,
                cash?.actual?.percentage,
                cash?.actual?.value
        )

        if (cashYesterdayGoal.isEmpty() && cashYesterdayVariance.isEmpty() && cashYesterdayActual.isEmpty()) {
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
        } else if (cashYesterdayGoal.isNotEmpty() && cashYesterdayVariance.isNotEmpty() && cashYesterdayActual.isNotEmpty()) {

            cash_error_gm_period_range_kpi.visibility = View.GONE
            cash_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            cash_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            cash_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            cash_goal_gm_yesterday_kpi.text = cashYesterdayGoal
            cash_variance_gm_yesterday_kpi.text = cashYesterdayVariance
            cash_actual_gm_yesterday_kpi.text = cashYesterdayActual
        } else {
            cash_error_gm_period_range_kpi.visibility = View.GONE
            cash_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            cash_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            cash_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            if (cashYesterdayGoal.isEmpty()) {
                cash_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_goal_gm_yesterday_kpi.text = cashYesterdayGoal
            }

            if (cashYesterdayVariance.isEmpty()) {
                cash_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_variance_gm_yesterday_kpi.text = cashYesterdayVariance
            }

            if (cashYesterdayActual.isEmpty()) {
                cash_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                cash_actual_gm_yesterday_kpi.text = cashYesterdayActual
            }

        }
        if (cash?.status != null && cashYesterdayActual.isNotEmpty()) {
            when {
                cash.status.toString() == resources.getString(R.string.out_of_range) -> {
                    cash_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    cash_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.red))
                }
                cash.status.toString() == resources.getString(R.string.under_limit) -> {
                    cash_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    cash_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.green))
                }
                else -> {
                    cash_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    cash_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.text_color))
                }
            }

        }
    }

    fun displayOERViewForGmYesterday(oerStart: StoreYesterdayKPIQuery.OerStart?) {
        if (oerStart?.displayName != null) {
            oer_display_gm_yesterday_kpi.text = oerStart.displayName
        }

        val oerGMYesterdayGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.goal?.amount,
                oerStart?.goal?.percentage,
                oerStart?.goal?.value
        )
        val oerGMYesterdayVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.variance?.amount,
                oerStart?.variance?.percentage,
                oerStart?.variance?.value
        )
        val oerGMYesterdayActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                oerStart?.actual?.amount,
                oerStart?.actual?.percentage,
                oerStart?.actual?.value
        )

        if (oerGMYesterdayGoal.isEmpty() && oerGMYesterdayVariance.isEmpty() && oerGMYesterdayActual.isEmpty()) {
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
        } else if (oerGMYesterdayGoal.isNotEmpty() && oerGMYesterdayVariance.isNotEmpty() && oerGMYesterdayActual.isNotEmpty()) {

            oer_error_gm_period_range_kpi.visibility = View.GONE
            oer_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            oer_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            oer_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            oer_goal_gm_yesterday_kpi.text = oerGMYesterdayGoal
            oer_variance_gm_yesterday_kpi.text = oerGMYesterdayVariance
            oer_actual_gm_yesterday_kpi.text = oerGMYesterdayActual
        } else {
            oer_error_gm_period_range_kpi.visibility = View.GONE
            oer_goal_gm_yesterday_kpi.visibility = View.VISIBLE
            oer_variance_gm_yesterday_kpi.visibility = View.VISIBLE
            oer_actual_gm_yesterday_kpi.visibility = View.VISIBLE

            if (oerGMYesterdayGoal.isEmpty()) {
                oer_goal_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_goal_gm_yesterday_kpi.text = oerGMYesterdayGoal
            }

            if (oerGMYesterdayVariance.isEmpty()) {
                oer_variance_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_variance_gm_yesterday_kpi.text = oerGMYesterdayVariance
            }

            if (oerGMYesterdayActual.isEmpty()) {
                oer_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                oer_actual_gm_yesterday_kpi.text = oerGMYesterdayActual
            }

        }
        if (oerStart?.status != null && oerGMYesterdayActual.isNotEmpty()) {
            when {
                oerStart.status.toString() == resources.getString(R.string.out_of_range) -> {
                    oer_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    oer_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.red))

                }
                oerStart.status.toString() == resources.getString(R.string.under_limit) -> {
                    oer_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    oer_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.green))
                }
                else -> {
                    oer_actual_gm_yesterday_kpi.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    oer_actual_gm_yesterday_kpi.setTextColor(requireContext().getColor(color.text_color))
                }
            }
        }
    }


    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.aws_parent_layout_gm_yesterday_kpi -> {
                openSalesDetailKpiYesterday()
            }
            R.id.food_parent_layout_gm_yesterday_kpi -> {
                openFoodDetailKpiYesterday()
            }
            R.id.labor_parent_layout_gm_yesterday_kpi -> {
                openLabourDetailKpiYesterday()
            }
            R.id.service_parent_layout_gm_yesterday_kpi -> {
                openServiceDetailKpiYesterday()
            }
            R.id.oer_parent_layout_gm_yesterday_kpi -> {
                openOERDetailKpiYesterday()
            }
            R.id.filter_icon -> {
                openFilterKpiYesterday()
            }
            R.id.filter_parent -> {
                openFilterKpiYesterday()
            }
            R.id.error_filter_parent_linear -> {
                openFilterKpiYesterday()
            }
            R.id.cash_parent_layout_gm_yesterday_kpi -> {
                if (this::storeDetailsYesterdayKpi.isInitialized) {
                    openCASHDetailKpiYesterday()
                }
            }
        }
    }

    private fun openFilterKpiYesterday() {
        val intentFilterKpiYesterday = Intent(requireContext(), FilterActivity::class.java)
        startActivity(intentFilterKpiYesterday)
    }

    private fun openSalesDetailKpiYesterday() {
        val intentSalesDetailKpiYesterday = Intent(requireContext(), AWUSKpiActivity::class.java)
        intentSalesDetailKpiYesterday.putExtra(
                "awus_data",
                gsonYesterdayKpi.toJson(storeDetailsYesterdayKpi)
        )
        intentSalesDetailKpiYesterday.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentSalesDetailKpiYesterday)
    }

    private fun openLabourDetailKpiYesterday() {
        val intentLabourDetailKpiYesterday = Intent(requireContext(), LabourKpiActivity::class.java)
        intentLabourDetailKpiYesterday.putExtra(
                "labour_data",
                gsonYesterdayKpi.toJson(storeDetailsYesterdayKpi)
        )
        intentLabourDetailKpiYesterday.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentLabourDetailKpiYesterday)
    }

    private fun openServiceDetailKpiYesterday() {
        val intentServiceDetailKpi = Intent(requireContext(), ServiceKpiActivity::class.java)
        intentServiceDetailKpi.putExtra(
                "service_data",
                gsonYesterdayKpi.toJson(storeDetailsYesterdayKpi)
        )
        intentServiceDetailKpi.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentServiceDetailKpi)
    }

    private fun openOERDetailKpiYesterday() {
        val intentOERDetailKpiYesterday = Intent(requireContext(), OERStartActivity::class.java)
        intentOERDetailKpiYesterday.putExtra(
                "oer_data",
                gsonYesterdayKpi.toJson(storeDetailsYesterdayKpi)
        )
        intentOERDetailKpiYesterday.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentOERDetailKpiYesterday)
    }

    private fun openFoodDetailKpiYesterday() {
        val intentFoodDetailKpiYesterday = Intent(requireContext(), FoodKpiActivity::class.java)
        intentFoodDetailKpiYesterday.putExtra(
                "food_data",
                gsonYesterdayKpi.toJson(storeDetailsYesterdayKpi)
        )
        intentFoodDetailKpiYesterday.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentFoodDetailKpiYesterday)
    }

    private fun openCASHDetailKpiYesterday() {
        val gsonCASHDetailKpiYesterday = Gson()
        val intentCASHDetailKpiYesterday = Intent(requireContext(), CashKpiActivity::class.java)
        intentCASHDetailKpiYesterday.putExtra(
                "cash_data",
                gsonCASHDetailKpiYesterday.toJson(storeDetailsYesterdayKpi)
        )
        intentCASHDetailKpiYesterday.putExtra("api_argument_from_filter", IpConstants.Yesterday)
        startActivity(intentCASHDetailKpiYesterday)
    }

    private fun refreshTokenKpiYesterday() {

        refreshTokenViewModelGMYesterday.getRefreshToken()

        refreshTokenViewModelGMYesterday.refreshTokenResponseLiveData.observe(requireActivity(), {
            run {
                when (it.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        callGmYesterdayLevel1Query()
                    }
                    Status.UNSUCCESSFUL -> {
                        CommonUtil.navigateToLogin(mainActivity)
                    }
                    Status.ERROR -> {
                    }
                    Status.OFFLINE -> {
                        setInternetErrorScreenVisibleStateForGMYesterday()
                    }
                }
            }
        })
    }

    private fun callMissingDataQueryForGmYesterday() {
        val progressDialogGMYesterday = CustomProgressDialog(requireActivity())
        progressDialogGMYesterday.showProgressDialog()
        lifecycleScope.launchWhenResumed {
            val areaCodesGMYesterday: ArrayList<String> = ArrayList()
            val stateCodesGMYesterday: ArrayList<String> = ArrayList()
            val superVisorCodeGMYesterday: ArrayList<String> = ArrayList()
            val storeCodesGMYesterday: ArrayList<String> = ArrayList()
            storeCodesGMYesterday.add(StorePrefData.StoreIdFromLogin)

            try {
                val responseMissingDataGMYesterday =
                        apolloClient(requireContext()).query(
                                MissingDataQuery(
                                        areaCodesGMYesterday.toInput(),
                                        stateCodesGMYesterday.toInput(),
                                        superVisorCodeGMYesterday.toInput(),
                                        storeCodesGMYesterday.toInput()
                                )
                        )
                                .await()

                if (responseMissingDataGMYesterday.data?.missingData != null) {
                    progressDialogGMYesterday.dismissProgressDialog()
                    setMissingDataViewVisibleStateForGMYesterday(
                            responseMissingDataGMYesterday.data?.missingData!!.header.toString(),
                            responseMissingDataGMYesterday.data?.missingData!!.message.toString()
                    )
                } else {
                    progressDialogGMYesterday.dismissProgressDialog()
                    gm_period_range_missing_data_error_layout.visibility = View.GONE
                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogGMYesterday.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Yesterday Missing Data")
                        }

            } catch (apolloNetworkException: ApolloNetworkException) {
                progressDialogGMYesterday.dismissProgressDialog()
            } catch (e: ApolloException) {
                progressDialogGMYesterday.dismissProgressDialog()
                Logger.error(e.message.toString(), "Yesterday Missing Data")
            }

        }
    }

    fun setInternetErrorScreenVisibleStateForGMYesterday() {
        gm_yesterday_no_internet_error_layout.visibility = View.VISIBLE
        common_header_gm_yesterday.sales_header_error_image.visibility = View.VISIBLE
        common_header_gm_yesterday.sales_header_error_image.setImageResource(R.drawable.ic_internet_error)
        gm_period_range_missing_data_error_layout.visibility = View.GONE
        common_header_gm_yesterday.error_filter_parent_linear.visibility = View.GONE
        gm_yesterday_error_layout.visibility = View.GONE
        setCalendarViewVisibleStateForGMYesterday()
        setHeaderViewsVisibleStateForGMYesterday()
        showStoreFilterVisibilityStateForGMYesterday()
    }

    fun setErrorScreenVisibleStateForGMYesterday(
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
        setHeaderViewsVisibleStateForGMYesterday()
        setCalendarViewVisibleStateForGMYesterday()
        hideStoreFilterVisibilityStateForGMYesterday()
    }

    fun setHeaderViewsVisibleStateForGMYesterday() {
        goal_parent_yesterday_kpi.visibility = View.GONE
        gm_yesterday_v1.visibility = View.GONE
        gm_yesterday_layout.visibility = View.INVISIBLE
        common_header_gm_yesterday.total_sales_common_header.visibility = View.GONE
        common_header_gm_yesterday.sales_text_common_header.visibility = View.GONE
    }
    fun hideStoreFilterVisibilityStateForGMYesterday(){
        common_header_gm_yesterday.filter_parent_linear.visibility = View.GONE
    }
    fun showStoreFilterVisibilityStateForGMYesterday(){
        common_header_gm_yesterday.filter_parent_linear.visibility = View.VISIBLE
    }

    fun setCalendarViewVisibleStateForGMYesterday() {
        common_calendar_gm_yesterday.visibility = View.GONE
    }

    fun setMissingDataViewVisibleStateForGMYesterday(
            missingDataTitle: String,
            missingDataDescription: String
    ) {
        gm_period_range_missing_data_error_layout.visibility = View.VISIBLE
        gm_period_range_missing_data_error_layout.header_data_title.text = missingDataTitle
        gm_period_range_missing_data_error_layout.header_data_description.text = missingDataDescription
    }

    fun hideErrorScreenVisibleStateForGMYesterday(){
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