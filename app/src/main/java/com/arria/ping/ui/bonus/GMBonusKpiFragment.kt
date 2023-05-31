package com.arria.ping.ui.bonus

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Html
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
import com.arria.ping.R.color
import com.arria.ping.apollo.apolloClient
import com.arria.ping.kpi.*
import com.arria.ping.kpi.bonus.GMBonusQuery
import com.arria.ping.kpi.type.FilterType
import com.arria.ping.log.Logger
import com.arria.ping.log.data.QueryData
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.ui.generalview.MainActivity
import com.arria.ping.ui.kpi.*
import com.arria.ping.ui.refreshtoken.model.Status
import com.arria.ping.ui.refreshtoken.viewmodel.RefreshTokenViewModel
import com.arria.ping.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_labour_kpi.*
import kotlinx.android.synthetic.main.bonus_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.ceo_period_range_fragment_kpi.*
import kotlinx.android.synthetic.main.common_calendar.view.*
import kotlinx.android.synthetic.main.common_header_bonus.*
import kotlinx.android.synthetic.main.common_header_bonus.view.*
import kotlinx.android.synthetic.main.common_header_bonus.view.sales_text_common_header
import kotlinx.android.synthetic.main.common_header_bonus.view.store_header
import kotlinx.android.synthetic.main.common_header_bonus.view.store_id
import kotlinx.android.synthetic.main.common_header_bonus.view.total_sales_common_header
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.data_error_layout.view.*
import kotlinx.android.synthetic.main.supervisor_period_range_fragment_kpi.*
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class GMBonusKpiFragment : Fragment() {

    private lateinit var bonusYesterdayGMStoreDetails: GMBonusQuery.GeneralManager
    private val bonusYesterdayGMDateFormatMonthBegin: LocalDate = LocalDate.now()
            .withDayOfMonth(1)

    val refreshTokenViewModelBonusGM by viewModels<RefreshTokenViewModel>()

    @Inject
    lateinit var networkHelper: NetworkHelper
    private var dialogGMBonus: Dialog? = null

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
        return inflater.inflate(R.layout.bonus_yesterday_fragment_kpi, container, false)
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        initBonusGmKpi()
    }

    private fun initBonusGmKpi() {
        Validation().setCustomCalendar(common_calendar_bonus.square_day)
        if (networkHelper.isNetworkConnected()) {
            callMissingDataForBonusGMQuery()
            callBonusGMQuery()
        } else {
            showInternetErrorDialogForBonusGM()
        }

        bonus_swipe_refresh_layout.setOnRefreshListener{
            Logger.info("Pull down to refresh GM Bonus Data", "Bonus KPI")
            callMissingDataForBonusGMQuery()
            callBonusGMQuery()
            bonus_swipe_refresh_layout.isRefreshing = false
        }
    }


    private fun callMissingDataForBonusGMQuery() {

        lifecycleScope.launchWhenResumed {
            val areaCodesGMBonus: ArrayList<String> = ArrayList()
            val stateCodesGMBonus: ArrayList<String> = ArrayList()
            val supervisorCodesGMBonus: ArrayList<String> = ArrayList()
            val storeCodesGMBonus: ArrayList<String> = ArrayList()
            storeCodesGMBonus.add(StorePrefData.StoreIdFromLogin)

            Logger.info(
                    GMBonusQuery.OPERATION_NAME.name(), "Bonus Missing Data", mapQueryFilters(
                    QueryData(
                            areaCodesGMBonus,
                            stateCodesGMBonus,
                            storeCodesGMBonus,
                            supervisorCodesGMBonus,
                            "",
                            "",
                            "MTD",
                            GMBonusQuery.QUERY_DOCUMENT
                    )
            )
            )
            try {
                val responseMissingDataDialogBonusGM =
                        apolloClient(requireContext()).query(
                                MissingDataQuery(
                                        areaCodesGMBonus.toInput(),
                                        stateCodesGMBonus.toInput(),
                                        supervisorCodesGMBonus.toInput(),
                                        storeCodesGMBonus.toInput(),
                                        FilterType.THIS_MONTH.toInput(),
                                        bonusYesterdayGMDateFormatMonthBegin.toString()
                                                .toInput(),
                                        DateFormatterUtil.currentCalendarDateForBonus()
                                                .toInput(),

                                        )
                        )
                                .await()

                if (responseMissingDataDialogBonusGM.data?.missingData != null) {
                    bonus_gm_error_layout.visibility = View.VISIBLE
                    bonus_gm_error_layout.header_data_title.text =
                            responseMissingDataDialogBonusGM.data?.missingData!!.header
                    bonus_gm_error_layout.header_data_description.text =
                            responseMissingDataDialogBonusGM.data?.missingData!!.message
                } else {
                    bonus_gm_error_layout.visibility = View.GONE
                }

            } catch (apolloHttpException: ApolloHttpException) {
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Bonus Missing Data query")
                        }

            } catch (e: ApolloNetworkException) {
                Log.e("GMBonusKPIFragment","${e.message}")
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Bonus Missing Data query")
            }
        }
    }

    fun callBonusGMQuery() {
        val progressDialogBonusGM = CustomProgressDialog(requireActivity())
        progressDialogBonusGM.showProgressDialog()
        lifecycleScope.launchWhenResumed {

            if (networkHelper.isNetworkConnected()) {
                hideErrorScreenVisibleStateForGMBonus()
            }
            val gmBonusStoreListValue = mutableListOf<String>()
            gmBonusStoreListValue.add(StorePrefData.StoreIdFromLogin)

            val kpis = QueryData(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    gmBonusStoreListValue,
                    DateFormatterUtil.currentCalendarDateForBonus(),
                    bonusYesterdayGMDateFormatMonthBegin.toString(),
                    "MTD",
                    GMBonusQuery.QUERY_DOCUMENT
            )

            Logger.info(
                    GMBonusQuery.OPERATION_NAME.name(), "Bonus", mapQueryFilters(kpis)
            )

            try {
                val responseBonusGM =
                        apolloClient(requireContext()).query(
                                GMBonusQuery(
                                        bonusYesterdayGMDateFormatMonthBegin.toString(),
                                        DateFormatterUtil.currentCalendarDateForBonus(),
                                        StorePrefData.StoreIdFromLogin
                                )
                        )
                                .await()

                if (responseBonusGM.data?.generalManager != null) {
                    progressDialogBonusGM.dismissProgressDialog()
                    bonusYesterdayGMStoreDetails = responseBonusGM.data?.generalManager!!

                    if (bonusYesterdayGMStoreDetails.bonus?.store?.period != null) {
                        setGMBonusStoreData(bonusYesterdayGMStoreDetails.bonus?.store?.period)
                    } else {
                        setErrorScreenVisibleStateForBonusGM()
                    }


                }

            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogBonusGM.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Bonus KPI")
                        }
                callRefreshTokenBonusGMApi()
                return@launchWhenResumed
            } catch (e: ApolloNetworkException) {
                progressDialogBonusGM.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    showInternetErrorDialogForBonusGM()
                }
            } catch (e: ApolloException) {
                progressDialogBonusGM.dismissProgressDialog()
                Logger.error(e.message.toString(), "Bonus KPI")
            }

        }
    }

    private fun setGMBonusStoreData(bonusGMKpiDataDetail: GMBonusQuery.Period?) {
        displayHeaderViewForGmBonus(bonusGMKpiDataDetail)
        displaySalesViewForGMBonus(bonusGMKpiDataDetail?.awus)
        displayPotentialViewForGMBonus(bonusGMKpiDataDetail?.potential)
        displayFoodViewForGMBonus(bonusGMKpiDataDetail?.food)
        displayLaborViewForGMBonus(bonusGMKpiDataDetail?.labor)
        displayServiceViewForGMBonus(bonusGMKpiDataDetail?.service)
        displayEADTServiceViewForGMBonus(bonusGMKpiDataDetail?.service?.eADT)
        displayExtremeServiceViewForGMBonus(bonusGMKpiDataDetail?.service?.extremeDelivery)
        displaySinglesServiceViewForGMBonus(bonusGMKpiDataDetail?.service?.singles)
        displayFocusViewForGMBonus(bonusGMKpiDataDetail?.focus)
        displayBonusNarrative(bonusGMKpiDataDetail?.narrative)
        displayBonusViewForGMBonus(bonusGMKpiDataDetail?.bonus)

    }

    fun displayHeaderViewForGmBonus(bonusGMKpiDataDetail: GMBonusQuery.Period?) {
        if (StorePrefData.dayOfLastServiceDate.isNotEmpty()) {
            common_header_bonus.total_sales_common_header.text = DateFormatterUtil.getRemainingDays(
                    StorePrefData.dayOfLastServiceDate
            )
            common_header_bonus.sales_text_common_header.text = getString(R.string.days_to_go)
            common_header_bonus.image_bonus.visibility = View.GONE
        }else{
            val calendarBonusSupervisor = Calendar.getInstance()
            val lastDayBonusSupervisor = calendarBonusSupervisor.getActualMaximum(Calendar.DAY_OF_MONTH)
            val currentDayBonusSupervisor = calendarBonusSupervisor[Calendar.DAY_OF_MONTH]
            val daysLeftBonusSupervisor = lastDayBonusSupervisor - currentDayBonusSupervisor
            common_header_bonus.total_sales_common_header.text = daysLeftBonusSupervisor.toString()
            common_header_bonus.sales_text_common_header.text = getString(R.string.days_to_go)
        }

        view_for_ceo_view.visibility = View.VISIBLE
        common_header_bonus.store_header.text = getString(R.string.mtd_text)
        common_header_bonus.store_id.text = StorePrefData.StoreIdFromLogin

        period_range.text = bonusGMKpiDataDetail?.periodFrom?.let {
            bonusGMKpiDataDetail.periodTo?.let {it1 ->
                EnumMapperUtil.getSelectedDate(
                        it,
                        it1, FilterType.THIS_MONTH
                )
            }
        }

    }

    fun displaySalesViewForGMBonus(awus: GMBonusQuery.Awus?) {
        awus_display_bonus.text = awus?.displayName

        val gmBonusYesterdayAWUSGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                awus?.goal?.amount,
                awus?.goal?.percentage,
                awus?.goal?.value
        )

        val gmBonusYesterdayAWUSVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                awus?.variance?.amount,
                awus?.variance?.percentage,
                awus?.variance?.value
        )

        val gmBonusYesterdayAWUSActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                awus?.actual?.amount,
                awus?.actual?.percentage,
                awus?.actual?.value
        )

        if (gmBonusYesterdayAWUSGoal.isEmpty() && gmBonusYesterdayAWUSVariance.isEmpty() && gmBonusYesterdayAWUSActual
                    .isEmpty()
        ) {

            gm_bonus_awus_error.visibility = View.VISIBLE
            awus_goal_bonus.visibility = View.GONE
            awus_variance_bonus.visibility = View.GONE
            awus_actual_bonus.visibility = View.GONE
            val paramsGMYesterdayAWUSError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMYesterdayAWUSError.weight = 2.0f
            awus_display_bonus.layoutParams = paramsGMYesterdayAWUSError
        } else if (gmBonusYesterdayAWUSGoal.isNotEmpty() && gmBonusYesterdayAWUSVariance.isNotEmpty() && gmBonusYesterdayAWUSActual.isNotEmpty()) {
            awus_goal_bonus.text = gmBonusYesterdayAWUSGoal
            awus_variance_bonus.text = gmBonusYesterdayAWUSVariance
            awus_actual_bonus.text = gmBonusYesterdayAWUSActual

            awus_goal_bonus.visibility = View.VISIBLE
            awus_variance_bonus.visibility = View.VISIBLE
            awus_actual_bonus.visibility = View.VISIBLE
            gm_bonus_awus_error.visibility = View.GONE

        } else {
            gm_bonus_awus_error.visibility = View.GONE
            if (gmBonusYesterdayAWUSGoal.isEmpty()) {

                awus_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                awus_goal_bonus.text = gmBonusYesterdayAWUSGoal
            }
            if (gmBonusYesterdayAWUSVariance.isEmpty()) {

                awus_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                awus_variance_bonus.text = gmBonusYesterdayAWUSVariance
            }
            if (gmBonusYesterdayAWUSActual.isEmpty()) {

                awus_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                awus_actual_bonus.text = gmBonusYesterdayAWUSActual
            }
        }


        if (awus?.status?.toString() != null && gmBonusYesterdayAWUSActual.isNotEmpty()) {
            when {
                awus.status.toString() == resources.getString(R.string.out_of_range) -> {
                    awus_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    awus_actual_bonus.setTextColor(requireContext().getColor(color.red))
                }
                awus.status.toString() == resources.getString(R.string.under_limit) -> {
                    awus_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    awus_actual_bonus.setTextColor(requireContext().getColor(color.green))

                }
                else -> {
                    awus_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    awus_actual_bonus.setTextColor(requireContext().getColor(color.text_color))
                }
            }
        }

    }

    fun displayPotentialViewForGMBonus(potential: GMBonusQuery.Potential?) {
        potential_display_bonus.text = potential?.displayName

        val gmBonusYesterdayPotentialGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                potential?.goal?.amount,
                potential?.goal?.percentage,
                potential?.goal?.value
        )

        val gmBonusYesterdayPotentialVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                potential?.variance?.amount,
                potential?.variance?.percentage,
                potential?.variance?.value
        )

        val gmBonusYesterdayPotentialActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                potential?.actual?.amount,
                potential?.actual?.percentage,
                potential?.actual?.value
        )


        if (gmBonusYesterdayPotentialGoal.isEmpty() && gmBonusYesterdayPotentialVariance.isEmpty() && gmBonusYesterdayPotentialActual.isEmpty()) {

            gm_bonus_pot_error.visibility = View.VISIBLE
            potential_goal_bonus.visibility = View.GONE
            potential_variance_bonus.visibility = View.GONE
            potential_actual_bonus.visibility = View.GONE
            val paramsGMYesterdayPotentialError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMYesterdayPotentialError.weight = 2.0f
            potential_display_bonus.layoutParams = paramsGMYesterdayPotentialError
        } else if (gmBonusYesterdayPotentialGoal.isNotEmpty() && gmBonusYesterdayPotentialVariance.isNotEmpty() && gmBonusYesterdayPotentialActual.isNotEmpty()) {
            potential_goal_bonus.text = gmBonusYesterdayPotentialGoal
            potential_variance_bonus.text = gmBonusYesterdayPotentialVariance
            potential_actual_bonus.text = gmBonusYesterdayPotentialActual
            gm_bonus_pot_error.visibility = View.GONE
        } else {
            gm_bonus_pot_error.visibility = View.GONE
            if (gmBonusYesterdayPotentialGoal.isEmpty()) {

                potential_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                potential_goal_bonus.text = gmBonusYesterdayPotentialGoal
            }
            if (gmBonusYesterdayPotentialVariance.isEmpty()) {

                potential_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                potential_variance_bonus.text = gmBonusYesterdayPotentialVariance
            }
            if (gmBonusYesterdayPotentialActual.isEmpty()) {

                potential_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                potential_actual_bonus.text = gmBonusYesterdayPotentialActual
            }
        }

        if (potential?.status?.toString() != null && gmBonusYesterdayPotentialActual.isNotEmpty()) {
            when {
                potential.status.toString() == resources.getString(R.string.out_of_range) -> {
                    potential_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    potential_actual_bonus.setTextColor(requireContext().getColor(color.red))
                }
                potential.status.toString() == resources.getString(R.string.under_limit) -> {
                    potential_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    potential_actual_bonus.setTextColor(requireContext().getColor(color.green))

                }
                else -> {
                    potential_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    potential_actual_bonus.setTextColor(requireContext().getColor(color.text_color))
                }
            }
        }

    }

    fun displayFoodViewForGMBonus(food: GMBonusQuery.Food?) {
        food_display_bonus.text = food?.displayName

        val gmBonusYesterdayFoodGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.goal?.amount,
                food?.goal?.percentage,
                food?.goal?.value
        )

        val gmBonusYesterdayFoodVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.variance?.amount,
                food?.variance?.percentage,
                food?.variance?.value
        )

        val gmBonusYesterdayFoodActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.actual?.amount,
                food?.actual?.percentage,
                food?.actual?.value
        )


        if (gmBonusYesterdayFoodGoal.isEmpty() && gmBonusYesterdayFoodVariance.isEmpty() && gmBonusYesterdayFoodActual.isEmpty()) {

            gm_bonus_food_error.visibility = View.VISIBLE
            food_goal_bonus.visibility = View.GONE
            food_variance_bonus.visibility = View.GONE
            food_actual_bonus.visibility = View.GONE
            val paramsGMYesterdayFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMYesterdayFoodError.weight = 2.0f
            food_display_bonus.layoutParams = paramsGMYesterdayFoodError

        } else if (gmBonusYesterdayFoodGoal.isNotEmpty() && gmBonusYesterdayFoodVariance.isNotEmpty() && gmBonusYesterdayFoodActual.isNotEmpty()) {
            food_goal_bonus.text = gmBonusYesterdayFoodGoal
            food_variance_bonus.text = gmBonusYesterdayFoodVariance
            food_actual_bonus.text = gmBonusYesterdayFoodActual
            gm_bonus_food_error.visibility = View.GONE
        } else {
            gm_bonus_food_error.visibility = View.GONE
            if (gmBonusYesterdayFoodGoal.isEmpty()) {

                food_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_goal_bonus.text = gmBonusYesterdayFoodGoal
            }
            if (gmBonusYesterdayFoodVariance.isEmpty()) {

                food_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_variance_bonus.text = gmBonusYesterdayFoodVariance
            }
            if (gmBonusYesterdayFoodActual.isEmpty()) {

                food_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_actual_bonus.text = gmBonusYesterdayFoodActual
            }
        }

        if (food?.status?.toString() != null && gmBonusYesterdayFoodActual.isNotEmpty()) {
            when {
                food.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.red_circle, 0)
                    food_actual_bonus.setTextColor(requireContext().getColor(color.red))
                }
                food.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.green_circle, 0)
                    food_actual_bonus.setTextColor(requireContext().getColor(color.green))
                }
                else -> {
                    food_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.black_circle, 0)
                    food_actual_bonus.setTextColor(requireContext().getColor(color.text_color))
                }
            }
        }

    }

    fun displayLaborViewForGMBonus(labor: GMBonusQuery.Labor?) {
        labour_display_bonus.text = labor?.displayName

        val gmBonusYesterdayLaborGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.goal?.amount,
                labor?.goal?.percentage,
                labor?.goal?.value
        )

        val gmBonusYesterdayLaborVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.variance?.amount,
                labor?.variance?.percentage,
                labor?.variance?.value
        )

        val gmBonusYesterdayLaborActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.actual?.amount,
                labor?.actual?.percentage,
                labor?.actual?.value
        )


        if (gmBonusYesterdayLaborGoal.isEmpty() && gmBonusYesterdayLaborVariance.isEmpty() && gmBonusYesterdayLaborActual.isEmpty()) {

            gm_bonus_labour_error.visibility = View.VISIBLE
            labour_goal_bonus.visibility = View.GONE
            labour_variance_bonus.visibility = View.GONE
            labour_actual_bonus.visibility = View.GONE

            val paramsGMYesterdayLaborError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMYesterdayLaborError.weight = 2.0f
            labour_display_bonus.layoutParams = paramsGMYesterdayLaborError

        } else if (gmBonusYesterdayLaborGoal.isNotEmpty() && gmBonusYesterdayLaborVariance.isNotEmpty() && gmBonusYesterdayLaborActual.isNotEmpty()) {
            labour_goal_bonus.text = gmBonusYesterdayLaborGoal
            labour_variance_bonus.text = gmBonusYesterdayLaborVariance
            labour_actual_bonus.text = gmBonusYesterdayLaborActual
            gm_bonus_labour_error.visibility = View.GONE
        } else {
            gm_bonus_labour_error.visibility = View.GONE
            if (gmBonusYesterdayLaborGoal.isEmpty()) {

                labour_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_goal_bonus.text = gmBonusYesterdayLaborGoal
            }
            if (gmBonusYesterdayLaborVariance.isEmpty()) {

                labour_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_variance_bonus.text = gmBonusYesterdayLaborVariance
            }
            if (gmBonusYesterdayLaborActual.isEmpty()) {

                labour_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_actual_bonus.text = gmBonusYesterdayLaborActual
            }
        }

        if (labor?.status != null && gmBonusYesterdayLaborActual.isNotEmpty()) {
            when {
                labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    labour_actual_bonus.setTextColor(requireContext().getColor(color.red))

                }
                labor.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    labour_actual_bonus.setTextColor(requireContext().getColor(color.green))

                }
                else -> {
                    labour_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    labour_actual_bonus.setTextColor(requireContext().getColor(color.text_color))

                }
            }
        }

    }

    fun displayServiceViewForGMBonus(service: GMBonusQuery.Service?) {
        service_display_bonus.text = service?.displayName

        val gmBonusYesterdayServiceGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.goal?.amount,
                service?.goal?.percentage,
                service?.goal?.value
        )

        val gmBonusYesterdayServiceVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.variance?.amount,
                service?.variance?.percentage,
                service?.variance?.value
        )

        val gmBonusYesterdayServiceActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.actual?.amount,
                service?.actual?.percentage,
                service?.actual?.value
        )


        if (gmBonusYesterdayServiceGoal.isEmpty() && gmBonusYesterdayServiceVariance.isEmpty() && gmBonusYesterdayServiceActual.isEmpty()) {

            gm_bonus_service_error.visibility = View.VISIBLE
            service_goal_bonus.visibility = View.GONE
            service_variance_bonus.visibility = View.GONE
            service_actual_bonus.visibility = View.GONE

            val paramsGMYesterdayServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMYesterdayServiceError.weight = 2.0f
            service_display_bonus.layoutParams = paramsGMYesterdayServiceError

        } else if (gmBonusYesterdayServiceGoal.isNotEmpty() && gmBonusYesterdayServiceVariance.isNotEmpty() && gmBonusYesterdayServiceActual.isNotEmpty()) {
            service_goal_bonus.text = gmBonusYesterdayServiceGoal
            service_variance_bonus.text = gmBonusYesterdayServiceVariance
            service_actual_bonus.text = gmBonusYesterdayServiceActual
            gm_bonus_service_error.visibility = View.GONE
        } else {
            gm_bonus_service_error.visibility = View.GONE
            if (gmBonusYesterdayServiceGoal.isEmpty()) {

                service_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_goal_bonus.text = gmBonusYesterdayServiceGoal
            }
            if (gmBonusYesterdayServiceVariance.isEmpty()) {

                service_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_variance_bonus.text = gmBonusYesterdayServiceVariance
            }
            if (gmBonusYesterdayServiceActual.isEmpty()) {

                service_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_actual_bonus.text = gmBonusYesterdayServiceActual
            }
        }

        if (service?.status != null && gmBonusYesterdayServiceActual.isNotEmpty()) {
            when {
                service.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_actual_bonus.setTextColor(requireContext().getColor(color.red))

                }
                service.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_actual_bonus.setTextColor(requireContext().getColor(color.green))

                }
                else -> {
                    service_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_actual_bonus.setTextColor(requireContext().getColor(color.text_color))
                }
            }
        }

    }

    fun displayEADTServiceViewForGMBonus(eADT: GMBonusQuery.EADT?) {
        eadt_display_bonus.text = eADT?.displayName

        val gmBonusYesterdayServiceEATDGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                eADT?.goal?.amount,
                eADT?.goal?.percentage,
                eADT?.goal?.value
        )

        val gmBonusYesterdayServiceEATDVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                eADT?.variance?.amount,
                eADT?.variance?.percentage,
                eADT?.variance?.value
        )

        val gmBonusYesterdayServiceEATDActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                eADT?.actual?.amount,
                eADT?.actual?.percentage,
                eADT?.actual?.value
        )


        if (gmBonusYesterdayServiceEATDGoal.isEmpty() && gmBonusYesterdayServiceEATDVariance.isEmpty() && gmBonusYesterdayServiceEATDActual.isEmpty()) {

            gm_bonus_service_eatd_error.visibility = View.VISIBLE
            service_eadt_goal_bonus.visibility = View.GONE
            service_eadt_variance_bonus.visibility = View.GONE
            service_eadt_actual_bonus.visibility = View.GONE

            val paramsGMYesterdayServiceEADTError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMYesterdayServiceEADTError.weight = 2.0f
            eadt_display_bonus.layoutParams = paramsGMYesterdayServiceEADTError

        } else if (gmBonusYesterdayServiceEATDGoal.isNotEmpty() && gmBonusYesterdayServiceEATDVariance.isNotEmpty() && gmBonusYesterdayServiceEATDActual.isNotEmpty()) {
            service_eadt_goal_bonus.text = gmBonusYesterdayServiceEATDGoal
            service_eadt_variance_bonus.text = gmBonusYesterdayServiceEATDVariance
            service_eadt_actual_bonus.text = gmBonusYesterdayServiceEATDActual
            gm_bonus_service_eatd_error.visibility = View.GONE
        } else {
            gm_bonus_service_eatd_error.visibility = View.GONE
            if (gmBonusYesterdayServiceEATDGoal.isEmpty()) {

                service_eadt_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_goal_bonus.text = gmBonusYesterdayServiceEATDGoal
            }
            if (gmBonusYesterdayServiceEATDVariance.isEmpty()) {

                service_eadt_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_variance_bonus.text = gmBonusYesterdayServiceEATDVariance
            }
            if (gmBonusYesterdayServiceEATDActual.isEmpty()) {

                service_eadt_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_actual_bonus.text = gmBonusYesterdayServiceEATDActual
            }
        }

        if (eADT?.status != null && gmBonusYesterdayServiceEATDActual.isNotEmpty()) {

            when {
                eADT.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_eadt_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_eadt_actual_bonus.setTextColor(requireContext().getColor(color.red))

                }
                eADT.status.toString() == resources.getString(R.string.under_limit) -> {
                    service_eadt_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_eadt_actual_bonus.setTextColor(requireContext().getColor(color.green))
                }
                else -> {
                    service_eadt_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_eadt_actual_bonus.setTextColor(requireContext().getColor(color.text_color))
                }
            }
        }

    }

    fun displayExtremeServiceViewForGMBonus(extremeDelivery: GMBonusQuery.ExtremeDelivery?) {
        extreme_delivery_display_bonus.text = extremeDelivery?.displayName

        val gmBonusYesterdayServiceExtremeGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.goal?.amount,
                extremeDelivery?.goal?.percentage,
                extremeDelivery?.goal?.value
        )

        val gmBonusYesterdayServiceExtremeVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.variance?.amount,
                extremeDelivery?.variance?.percentage,
                extremeDelivery?.variance?.value
        )

        val gmBonusYesterdayServiceExtremeActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.actual?.amount,
                extremeDelivery?.actual?.percentage,
                extremeDelivery?.actual?.value
        )


        if (gmBonusYesterdayServiceExtremeGoal.isEmpty() && gmBonusYesterdayServiceExtremeVariance.isEmpty() && gmBonusYesterdayServiceExtremeActual.isEmpty()) {

            gm_bonus_service_extreme_error.visibility = View.VISIBLE
            service_extreme_goal_bonus.visibility = View.GONE
            service_extreme_variance_bonus.visibility = View.GONE
            service_extreme_actual_bonus.visibility = View.GONE

            val paramsGMYesterdayServiceExtremeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMYesterdayServiceExtremeError.weight = 2.0f
            extreme_delivery_display_bonus.layoutParams = paramsGMYesterdayServiceExtremeError

        } else if (gmBonusYesterdayServiceExtremeGoal.isNotEmpty() && gmBonusYesterdayServiceExtremeVariance.isNotEmpty() && gmBonusYesterdayServiceExtremeActual.isNotEmpty()) {
            service_extreme_goal_bonus.text = gmBonusYesterdayServiceExtremeGoal
            service_extreme_variance_bonus.text = gmBonusYesterdayServiceExtremeVariance
            service_extreme_actual_bonus.text = gmBonusYesterdayServiceExtremeActual
            gm_bonus_service_extreme_error.visibility = View.GONE
        } else {
            gm_bonus_service_extreme_error.visibility = View.GONE
            if (gmBonusYesterdayServiceExtremeGoal.isEmpty()) {

                service_extreme_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_goal_bonus.text = gmBonusYesterdayServiceExtremeGoal
            }
            if (gmBonusYesterdayServiceExtremeVariance.isEmpty()) {

                service_extreme_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_variance_bonus.text = gmBonusYesterdayServiceExtremeVariance
            }
            if (gmBonusYesterdayServiceExtremeActual.isEmpty()) {

                service_extreme_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_actual_bonus.text = gmBonusYesterdayServiceExtremeActual
            }
        }

        if (extremeDelivery?.status != null && gmBonusYesterdayServiceExtremeActual.isNotEmpty()) {

            when {
                extremeDelivery.status.toString() == resources.getString(
                        R.string.out_of_range
                ) -> {
                    service_extreme_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_extreme_actual_bonus.setTextColor(requireContext().getColor(color.red))
                }
                extremeDelivery.status.toString() == resources.getString(
                        R.string.under_limit
                ) -> {
                    service_extreme_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_extreme_actual_bonus.setTextColor(requireContext().getColor(color.green))
                }
                else -> {
                    service_extreme_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_extreme_actual_bonus.setTextColor(requireContext().getColor(color.text_color))
                }
            }
        }

    }

    fun displaySinglesServiceViewForGMBonus(singles: GMBonusQuery.Singles?) {
        single_display_bonus.text = singles?.displayName

        val gmBonusYesterdayServiceSingleGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.goal?.amount,
                singles?.goal?.percentage,
                singles?.goal?.value
        )

        val gmBonusYesterdayServiceSingleVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.variance?.amount,
                singles?.variance?.percentage,
                singles?.variance?.value
        )

        val gmBonusYesterdayServiceSingleActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.actual?.amount,
                singles?.actual?.percentage,
                singles?.actual?.value
        )


        if (gmBonusYesterdayServiceSingleGoal.isEmpty() && gmBonusYesterdayServiceSingleVariance.isEmpty() && gmBonusYesterdayServiceSingleActual.isEmpty()) {

            gm_bonus_service_single_error.visibility = View.VISIBLE
            service_singles_goal_bonus.visibility = View.GONE
            service_singles_variance_bonus.visibility = View.GONE
            service_singles_actual_bonus.visibility = View.GONE

            val paramsGMYesterdayServiceSinglesError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMYesterdayServiceSinglesError.weight = 2.0f
            single_display_bonus.layoutParams = paramsGMYesterdayServiceSinglesError

        } else if (gmBonusYesterdayServiceSingleGoal.isNotEmpty() && gmBonusYesterdayServiceSingleVariance.isNotEmpty() && gmBonusYesterdayServiceSingleActual.isNotEmpty()) {
            service_singles_goal_bonus.text = gmBonusYesterdayServiceSingleGoal
            service_singles_variance_bonus.text = gmBonusYesterdayServiceSingleVariance
            service_singles_actual_bonus.text = gmBonusYesterdayServiceSingleActual
            gm_bonus_service_single_error.visibility = View.GONE
        } else {
            gm_bonus_service_single_error.visibility = View.GONE
            if (gmBonusYesterdayServiceSingleGoal.isEmpty()) {

                service_singles_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_goal_bonus.text = gmBonusYesterdayServiceSingleGoal
            }
            if (gmBonusYesterdayServiceSingleVariance.isEmpty()) {

                service_singles_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_variance_bonus.text = gmBonusYesterdayServiceSingleVariance
            }
            if (gmBonusYesterdayServiceSingleActual.isEmpty()) {

                service_singles_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_actual_bonus.text = gmBonusYesterdayServiceSingleActual
            }
        }

        if (singles?.status != null && gmBonusYesterdayServiceSingleActual.isNotEmpty()) {
            when {
                singles.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_singles_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_singles_actual_bonus.setTextColor(requireContext().getColor(color.red))

                }
                singles.status.toString() == resources.getString(
                        R.string.under_limit
                ) -> {
                    service_singles_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    service_singles_actual_bonus.setTextColor(requireContext().getColor(color.green))
                }
                else -> {
                    service_singles_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    service_singles_actual_bonus.setTextColor(requireContext().getColor(color.text_color))
                }
            }
        }

    }

    fun displayFocusViewForGMBonus(focus: GMBonusQuery.Focus?) {
        focus_display_bonus.text = focus?.displayName

        val gmBonusYesterdayFocusGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                focus?.goal?.amount,
                focus?.goal?.percentage,
                focus?.goal?.value
        )

        val gmBonusYesterdayFocusVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                focus?.variance?.amount,
                focus?.variance?.percentage,
                focus?.variance?.value
        )

        val gmBonusYesterdayFocusActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                focus?.actual?.amount,
                focus?.actual?.percentage,
                focus?.actual?.value
        )


        if (gmBonusYesterdayFocusGoal.isEmpty() && gmBonusYesterdayFocusVariance.isEmpty() && gmBonusYesterdayFocusActual.isEmpty()) {

            gm_bonus_focus_error.visibility = View.VISIBLE
            focus_goal_bonus.visibility = View.GONE
            focus_variance_bonus.visibility = View.GONE
            focus_actual_bonus.visibility = View.GONE
            val paramsGMYesterdayFocusError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMYesterdayFocusError.weight = 2.0f
            focus_display_bonus.layoutParams = paramsGMYesterdayFocusError

        } else if (gmBonusYesterdayFocusGoal.isNotEmpty() && gmBonusYesterdayFocusVariance.isNotEmpty() && gmBonusYesterdayFocusActual.isNotEmpty()) {
            focus_goal_bonus.text = gmBonusYesterdayFocusGoal
            focus_variance_bonus.text = gmBonusYesterdayFocusVariance
            focus_actual_bonus.text = gmBonusYesterdayFocusActual
            gm_bonus_focus_error.visibility = View.GONE
        } else {
            gm_bonus_focus_error.visibility = View.GONE
            if (gmBonusYesterdayFocusGoal.isEmpty()) {

                focus_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                focus_goal_bonus.text = gmBonusYesterdayFocusGoal
            }
            if (gmBonusYesterdayFocusVariance.isEmpty()) {

                focus_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                focus_variance_bonus.text = gmBonusYesterdayFocusVariance
            }
            if (gmBonusYesterdayFocusActual.isEmpty()) {

                focus_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                focus_actual_bonus.text = gmBonusYesterdayFocusActual
            }
        }

        if (focus?.status?.toString() != null && gmBonusYesterdayFocusActual.isNotEmpty()) {
            when {
                focus.status.toString() == resources.getString(R.string.out_of_range) -> {
                    focus_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    focus_actual_bonus.setTextColor(requireContext().getColor(color.red))
                }
                focus.status.toString() == resources.getString(R.string.under_limit) -> {
                    focus_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    focus_actual_bonus.setTextColor(requireContext().getColor(color.green))

                }
                else -> {
                    focus_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    focus_actual_bonus.setTextColor(requireContext().getColor(color.text_color))
                }
            }
        }

    }

    fun displayBonusNarrative(narrative: String?) {
        if (narrative.isNullOrEmpty()) {
            var bonusGMNarrative = narrative.toString()
            bonusGMNarrative = bonusGMNarrative.replace("</p>", "<br><br>")
            bonus_narrative.text = Html.fromHtml(bonusGMNarrative, Html.FROM_HTML_MODE_COMPACT)
        } else {
            narrative_layout.visibility = View.INVISIBLE
        }

    }

    fun displayBonusViewForGMBonus(bonus: GMBonusQuery.Bonus1?) {
        bonus_display_bonus.text = bonus?.displayName

        val gmBonusYesterdayPotentialGM = Validation().checkAmountPercentageValue(
                requireActivity(),
                bonus?.potential?.amount,
                bonus?.potential?.percentage,
                bonus?.potential?.value
        )

        val gmBonusYesterdayMissedGM = Validation().checkAmountPercentageValue(
                requireActivity(),
                bonus?.missed?.amount,
                bonus?.missed?.percentage,
                bonus?.missed?.value
        )

        val gmBonusYesterdayEarnerGm = Validation().checkAmountPercentageValue(
                requireActivity(),
                bonus?.earner?.amount,
                bonus?.earner?.percentage,
                bonus?.earner?.value
        )

        if (gmBonusYesterdayPotentialGM.isEmpty()) {

            bonus_potential_bonus.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0
            )
        } else {
            bonus_potential_bonus.text = gmBonusYesterdayPotentialGM
        }
        if (gmBonusYesterdayMissedGM.isEmpty()) {

            bonus_missed_bonus.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0
            )
        } else {
            bonus_missed_bonus.text = gmBonusYesterdayMissedGM
        }
        if (gmBonusYesterdayEarnerGm.isEmpty()) {

            bonus_earner_bonus.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_error, 0
            )
        } else {
            bonus_earner_bonus.text = gmBonusYesterdayEarnerGm
        }

        if (bonus?.status?.toString() != null && gmBonusYesterdayEarnerGm.isNotEmpty()) {
            when {
                bonus.status.toString() == resources.getString(R.string.out_of_range) -> {
                    bonus_earner_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    bonus_earner_bonus.setTextColor(requireContext().getColor(color.red))
                }
                bonus.status.toString() == resources.getString(R.string.under_limit) -> {
                    bonus_earner_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    bonus_earner_bonus.setTextColor(requireContext().getColor(color.green))

                }
                else -> {
                    bonus_earner_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    bonus_earner_bonus.setTextColor(requireContext().getColor(color.text_color))
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showInternetErrorDialogForBonusGM() {
        dialogGMBonus = DialogUtil.getErrorDialogAccessDialog(
                requireActivity(),
                getString(R.string.network_error_title),
                getString(R.string.network_error_description),
                getString(R.string.retry_button_text),
                {
                    callBonusGMQuery()
                    dialogGMBonus?.dismiss()
                    dialogGMBonus = null

                },
                getString(R.string.cancel),
                {
                    dialogGMBonus?.dismiss()
                    dialogGMBonus = null
                }
        )
        dialogGMBonus?.show()
    }

    fun setErrorScreenVisibleStateForBonusGM() {
        bonus_error_layout.visibility = View.VISIBLE
        common_header_bonus.image_bonus.visibility = View.VISIBLE
        common_header_bonus.image_bonus.setImageResource(R.drawable.ic_data_unavailbale_error)
        common_header_bonus.bonus_error_filter_parent_linear.visibility = View.VISIBLE
        setBonusParentViewsVisibleStateForBonusGM()
        setCalendarViewVisibleStateForBonusGM()
        setHeaderViewsVisibleStateForBonusGM()
    }

    fun setBonusParentViewsVisibleStateForBonusGM() {
        bonus_data_layout.visibility = View.INVISIBLE
        bonus_goal_parent_layout.visibility = View.GONE
        bonus_view.visibility = View.GONE
    }

    fun setCalendarViewVisibleStateForBonusGM() {
        common_calendar_bonus.visibility = View.GONE
    }

    fun setHeaderViewsVisibleStateForBonusGM() {
        common_header_bonus.total_sales_common_header.visibility = View.GONE
        common_header_bonus.sales_text_common_header.visibility = View.GONE
        common_header_bonus.bonus_filter_parent_linear.visibility = View.GONE
    }

    fun hideErrorScreenVisibleStateForGMBonus(){
        bonus_error_layout.visibility = View.GONE
        common_header_bonus.image_bonus.visibility = View.GONE
        common_header_bonus.bonus_error_filter_parent_linear.visibility = View.GONE

        bonus_data_layout.visibility = View.VISIBLE
        bonus_goal_parent_layout.visibility = View.VISIBLE
        bonus_view.visibility = View.VISIBLE
        common_header_bonus.bonus_filter_parent_linear.visibility = View.VISIBLE
        common_header_bonus.total_sales_common_header.visibility = View.VISIBLE
        common_header_bonus.sales_text_common_header.visibility = View.VISIBLE

        common_calendar_bonus.visibility = View.VISIBLE
    }

    private fun callRefreshTokenBonusGMApi() {
        refreshTokenViewModelBonusGM.getRefreshToken()

        refreshTokenViewModelBonusGM.refreshTokenResponseLiveData.observe(requireActivity(), {
            run {
                when (it.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        callBonusGMQuery()
                    }
                    Status.UNSUCCESSFUL -> {
                        CommonUtil.navigateToLogin(mainActivity)
                    }
                    Status.ERROR -> {
                    }
                    Status.OFFLINE -> {
                        showInternetErrorDialogForBonusGM()
                    }
                }
            }
        })
    }
}