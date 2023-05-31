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
import com.arria.ping.database.DatabaseBuilder
import com.arria.ping.database.DatabaseHelperImpl
import com.arria.ping.util.NetworkHelper
import com.arria.ping.kpi.MissingDataQuery
import com.arria.ping.kpi.bonus.SupervisorBonusQuery
import com.arria.ping.kpi.type.FilterType
import com.arria.ping.ui.generalview.MainActivity
import com.arria.ping.log.Logger
import com.arria.ping.log.data.QueryData
import com.arria.ping.log.mapQueryFilters
import com.arria.ping.ui.kpi.*
import com.arria.ping.ui.refreshtoken.model.Status
import com.arria.ping.ui.refreshtoken.viewmodel.RefreshTokenViewModel
import com.arria.ping.util.CommonUtil
import com.arria.ping.util.CustomProgressDialog
import com.arria.ping.util.EnumMapperUtil
import com.arria.ping.util.Validation
import com.arria.ping.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bonus_yesterday_fragment_kpi.*
import kotlinx.android.synthetic.main.common_calendar.view.*
import kotlinx.android.synthetic.main.common_header_bonus.*
import kotlinx.android.synthetic.main.common_header_bonus.view.*
import kotlinx.android.synthetic.main.common_header_bonus.view.sales_text_common_header
import kotlinx.android.synthetic.main.common_header_bonus.view.store_header
import kotlinx.android.synthetic.main.common_header_bonus.view.store_id
import kotlinx.android.synthetic.main.common_header_bonus.view.total_sales_common_header
import kotlinx.android.synthetic.main.common_header_ceo.view.*
import kotlinx.android.synthetic.main.data_error_layout.view.*
import java.time.LocalDate
import java.util.*
import javax.inject.Inject


@RequiresApi(Build.VERSION_CODES.O)

@AndroidEntryPoint
class SupervisorBonusKpiFragment : Fragment() {

    private lateinit var bonusYesterdaySupervisorStoreDetails: SupervisorBonusQuery.Supervisor
    private val bonusYesterdaySupervisorMonthBegin = LocalDate.now()
            .withDayOfMonth(1)

    private lateinit var bonusYesterdaySupervisorDBHelper: DatabaseHelperImpl

    private val refreshTokenViewModelBonusSupervisor by viewModels<RefreshTokenViewModel>()

    @Inject
    lateinit var networkHelper: NetworkHelper
    private var dialogSupervisorBonus: Dialog? = null

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
        bonusYesterdaySupervisorDBHelper =
                DatabaseHelperImpl(DatabaseBuilder.getInstance(requireActivity()))

        initBonusYesterdaySupervisor()
    }

    private fun initBonusYesterdaySupervisor() {

        Validation().setCustomCalendar(common_calendar_bonus.square_day)

        if (networkHelper.isNetworkConnected()) {
            checkMissingDataApiForBonusSupervisor()
            callBonusSupervisorQuery()
        } else {
            showInternetErrorDialog()
        }

        bonus_swipe_refresh_layout.setOnRefreshListener{
            Logger.info("Pull down to refresh Supervisor Bonus Data", "Bonus KPI")
            checkMissingDataApiForBonusSupervisor()
            callBonusSupervisorQuery()
            bonus_swipe_refresh_layout.isRefreshing = false
        }
    }


    private fun checkMissingDataApiForBonusSupervisor() {


        lifecycleScope.launchWhenResumed {
            val areaCodeBonusSupervisor =
                    bonusYesterdaySupervisorDBHelper.getAllSelectedAreaList(true)
            val stateCodeBonusSupervisor =
                    bonusYesterdaySupervisorDBHelper.getAllSelectedStoreListState(true)
            val storeNumberBonusSupervisor =
                    bonusYesterdaySupervisorDBHelper.getAllSelectedStoreList(true)
            val superVisorCode: ArrayList<String> = ArrayList()

            try {
                val responseMissingDataBonusSupervisor =
                        apolloClient(requireContext()).query(
                                MissingDataQuery(
                                        areaCodeBonusSupervisor.toInput(),
                                        stateCodeBonusSupervisor.toInput(),
                                        superVisorCode.toInput(),
                                        storeNumberBonusSupervisor.toInput(),
                                        FilterType.THIS_MONTH.toInput(),
                                        bonusYesterdaySupervisorMonthBegin.toString()
                                                .toInput(),
                                        DateFormatterUtil.currentCalendarDateForBonus()
                                                .toInput()
                                )
                        )
                                .await()

                if (responseMissingDataBonusSupervisor.data?.missingData != null) {
                    bonus_gm_error_layout.visibility = View.VISIBLE
                    bonus_gm_error_layout.header_data_title.text =
                            responseMissingDataBonusSupervisor.data?.missingData!!.header
                    bonus_gm_error_layout.header_data_description.text =
                            responseMissingDataBonusSupervisor.data?.missingData!!.message
                } else {
                    bonus_gm_error_layout.visibility = View.GONE
                }

            } catch (apolloHttpException: ApolloHttpException) {
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Bonus Missing Data query")
                        }

            } catch (e: ApolloNetworkException) {
                Log.e("SupervisorBonusKPIFragment", "${e.message}")
            } catch (e: ApolloException) {
                Logger.error(e.message.toString(), "Bonus Missing Data query")
            }

        }
    }

    private fun callBonusSupervisorQuery() {

        val progressDialogBonusSupervisor = CustomProgressDialog(requireActivity())
        progressDialogBonusSupervisor.showProgressDialog()

        lifecycleScope.launchWhenResumed {
            if (networkHelper.isNetworkConnected()) {
                hideErrorScreenVisibleStateForSupervisorBonus()
            }
            val areaCodeBonusSupervisor =
                    bonusYesterdaySupervisorDBHelper.getAllSelectedAreaList(true)
            val stateCodeBonusSupervisor =
                    bonusYesterdaySupervisorDBHelper.getAllSelectedStoreListState(true)
            val storeNumberBonusSupervisor =
                    bonusYesterdaySupervisorDBHelper.getAllSelectedStoreList(true)

            Logger.info(
                    SupervisorBonusQuery.OPERATION_NAME.name(),
                    "Bonus",
                    mapQueryFilters(
                            QueryData(
                                    areaCodeBonusSupervisor,
                                    stateCodeBonusSupervisor,
                                    Collections.emptyList(),
                                    storeNumberBonusSupervisor,
                                    DateFormatterUtil.currentCalendarDateForBonus(),
                                    bonusYesterdaySupervisorMonthBegin.toString(),
                                    "MTD",
                                    SupervisorBonusQuery.QUERY_DOCUMENT
                            )
                    )
            )

            try {
                val responseBonusSupervisor =
                        apolloClient(requireContext()).query(
                                SupervisorBonusQuery(
                                        areaCodeBonusSupervisor.toInput(),
                                        stateCodeBonusSupervisor.toInput(),
                                        storeNumberBonusSupervisor.toInput(),
                                        bonusYesterdaySupervisorMonthBegin.toString(),
                                        DateFormatterUtil.currentCalendarDateForBonus()
                                )
                        )
                                .await()

                if (responseBonusSupervisor.data?.supervisor != null) {
                    progressDialogBonusSupervisor.dismissProgressDialog()
                    bonusYesterdaySupervisorStoreDetails = responseBonusSupervisor.data?.supervisor!!

                    if (bonusYesterdaySupervisorStoreDetails.bonus?.stores?.period != null) {
                        setBonusSupervisorStoreData(
                                bonusYesterdaySupervisorStoreDetails.bonus?.stores?.period
                        )
                    } else {
                        setErrorScreenVisibleStateForBonusSupervisor()
                    }
                }
            } catch (apolloHttpException: ApolloHttpException) {
                progressDialogBonusSupervisor.dismissProgressDialog()
                apolloHttpException.code()
                        .let {
                            Logger.error(it.toString(), "Bonus KPI")
                        }
                refreshTokenBonusSupervisor()
                return@launchWhenResumed
            } catch (e: ApolloNetworkException) {
                progressDialogBonusSupervisor.dismissProgressDialog()
                if (!networkHelper.isNetworkConnected()) {
                    showInternetErrorDialog()
                }
            } catch (e: ApolloException) {
                progressDialogBonusSupervisor.dismissProgressDialog()
                Logger.error(e.message.toString(), "Bonus KPI")

            }

        }
    }

    private fun setBonusSupervisorStoreData(supervisorBonusData: SupervisorBonusQuery.Period?) {
        displayHeaderViewForSupervisorBonus(supervisorBonusData)
        displaySalesViewForSupervisorBonus(supervisorBonusData?.awus)
        displayPotentialViewForSupervisorBonus(supervisorBonusData?.potential)
        displayFoodViewForSupervisorBonus(supervisorBonusData?.food)
        displayLaborViewForSupervisorBonus(supervisorBonusData?.labor)
        displayServiceViewForSupervisorBonus(supervisorBonusData?.service)
        displayEADTServiceViewForSupervisorBonus(supervisorBonusData?.service?.eADT)
        displayExtremeServiceViewForSupervisorBonus(supervisorBonusData?.service?.extremeDelivery)
        displaySinglesServiceViewForSupervisorBonus(supervisorBonusData?.service?.singles)
        displayFocusViewForSupervisorBonus(supervisorBonusData?.focus)
        displayBonusNarrativeForSupervisorBonus(supervisorBonusData?.narrative)
        displayBonusViewForSupervisorBonus(supervisorBonusData?.bonus)
    }

    fun displayHeaderViewForSupervisorBonus(supervisorBonusData: SupervisorBonusQuery.Period?) {
        if (StorePrefData.dayOfLastServiceDate.isNotEmpty()) {
            common_header_bonus.total_sales_common_header.text = DateFormatterUtil.getRemainingDays(
                    StorePrefData
                            .dayOfLastServiceDate
            )
            common_header_bonus.sales_text_common_header.text = getString(R.string.days_to_go)
            common_header_bonus.image_bonus.visibility = View.GONE
        }else{
            common_header_bonus.total_sales_common_header.visibility = View.GONE
            common_header_bonus.sales_text_common_header.visibility = View.GONE
            common_header_bonus.image_bonus.visibility = View.VISIBLE
        }
        view_for_ceo_view.visibility = View.VISIBLE
        common_header_bonus.store_header.text = getString(R.string.mtd_text)
        common_header_bonus.store_id.text = getString(R.string.all_store_text)

        period_range.text =
                supervisorBonusData?.periodFrom?.let {
                    supervisorBonusData.periodTo?.let {it1 ->
                        EnumMapperUtil.getSelectedDate(
                                it,
                                it1, FilterType.THIS_MONTH
                        )
                    }
                }

    }

    fun displaySalesViewForSupervisorBonus(awus: SupervisorBonusQuery.Awus?) {
        awus_display_bonus.text = awus?.displayName
        val supervisorBonusYesterdayAWUSGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                awus?.goal?.amount,
                awus?.goal?.percentage,
                awus?.goal?.value
        )

        val supervisorBonusYesterdayAWUSVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                awus?.variance?.amount,
                awus?.variance?.percentage,
                awus?.variance?.value
        )

        val supervisorBonusYesterdayAWUSActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                awus?.actual?.amount,
                awus?.actual?.percentage,
                awus?.actual?.value
        )

        if (supervisorBonusYesterdayAWUSGoal.isEmpty() && supervisorBonusYesterdayAWUSVariance.isEmpty() && supervisorBonusYesterdayAWUSActual.isEmpty()) {

            gm_bonus_awus_error.visibility = View.VISIBLE
            awus_goal_bonus.visibility = View.GONE
            awus_variance_bonus.visibility = View.GONE
            awus_actual_bonus.visibility = View.GONE
            val paramsSupervisorYesterdayAWUSError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsSupervisorYesterdayAWUSError.weight = 2.0f
            awus_display_bonus.layoutParams = paramsSupervisorYesterdayAWUSError
        } else if (supervisorBonusYesterdayAWUSGoal.isNotEmpty() && supervisorBonusYesterdayAWUSVariance.isNotEmpty() && supervisorBonusYesterdayAWUSActual.isNotEmpty()) {
            awus_goal_bonus.text = supervisorBonusYesterdayAWUSGoal
            awus_variance_bonus.text = supervisorBonusYesterdayAWUSVariance
            awus_actual_bonus.text = supervisorBonusYesterdayAWUSActual
            gm_bonus_awus_error.visibility = View.GONE
        } else {
            gm_bonus_awus_error.visibility = View.GONE
            if (supervisorBonusYesterdayAWUSGoal.isEmpty()) {

                awus_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                awus_goal_bonus.text = supervisorBonusYesterdayAWUSGoal
            }
            if (supervisorBonusYesterdayAWUSVariance.isEmpty()) {

                awus_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                awus_variance_bonus.text = supervisorBonusYesterdayAWUSVariance
            }
            if (supervisorBonusYesterdayAWUSActual.isEmpty()) {

                awus_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                awus_actual_bonus.text = supervisorBonusYesterdayAWUSActual
            }
        }


        if (awus?.status?.toString() != null && supervisorBonusYesterdayAWUSActual.isNotEmpty()) {
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

    fun displayPotentialViewForSupervisorBonus(potential: SupervisorBonusQuery.Potential?) {
        potential_display_bonus.text = potential?.displayName
        val supervisorBonusYesterdayPotentialGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                potential?.goal?.amount,
                potential?.goal?.percentage,
                potential?.goal?.value
        )

        val supervisorBonusYesterdayPotentialVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                potential?.variance?.amount,
                potential?.variance?.percentage,
                potential?.variance?.value
        )

        val supervisorBonusYesterdayPotentialActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                potential?.actual?.amount,
                potential?.actual?.percentage,
                potential?.actual?.value
        )


        if (supervisorBonusYesterdayPotentialGoal.isEmpty() && supervisorBonusYesterdayPotentialVariance.isEmpty() && supervisorBonusYesterdayPotentialActual.isEmpty()) {

            gm_bonus_pot_error.visibility = View.VISIBLE
            potential_goal_bonus.visibility = View.GONE
            potential_variance_bonus.visibility = View.GONE
            potential_actual_bonus.visibility = View.GONE
            val paramsGMYesterdayPotentialError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsGMYesterdayPotentialError.weight = 2.0f
            potential_display_bonus.layoutParams = paramsGMYesterdayPotentialError
        } else if (supervisorBonusYesterdayPotentialGoal.isNotEmpty() && supervisorBonusYesterdayPotentialVariance.isNotEmpty() && supervisorBonusYesterdayPotentialActual.isNotEmpty()) {
            potential_goal_bonus.visibility = View.VISIBLE
            potential_variance_bonus.visibility = View.VISIBLE
            potential_actual_bonus.visibility = View.VISIBLE

            potential_goal_bonus.text = supervisorBonusYesterdayPotentialGoal
            potential_variance_bonus.text = supervisorBonusYesterdayPotentialVariance
            potential_actual_bonus.text = supervisorBonusYesterdayPotentialActual
        } else {
            if (supervisorBonusYesterdayPotentialGoal.isEmpty()) {

                potential_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                potential_goal_bonus.text = supervisorBonusYesterdayPotentialGoal
            }
            if (supervisorBonusYesterdayPotentialVariance.isEmpty()) {

                potential_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                potential_variance_bonus.text = supervisorBonusYesterdayPotentialVariance
            }
            if (supervisorBonusYesterdayPotentialActual.isEmpty()) {

                potential_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                potential_actual_bonus.text = supervisorBonusYesterdayPotentialActual
            }
        }


        if (potential?.status?.toString() != null && supervisorBonusYesterdayPotentialActual.isNotEmpty()) {
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

    fun displayFoodViewForSupervisorBonus(food: SupervisorBonusQuery.Food?) {
        food_display_bonus.text = food?.displayName
        val supervisorBonusYesterdayFoodGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.goal?.amount,
                food?.goal?.percentage,
                food?.goal?.value
        )

        val supervisorBonusYesterdayFoodVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.variance?.amount,
                food?.variance?.percentage,
                food?.variance?.value
        )

        val supervisorBonusYesterdayFoodActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                food?.actual?.amount,
                food?.actual?.percentage,
                food?.actual?.value
        )


        if (supervisorBonusYesterdayFoodGoal.isEmpty() && supervisorBonusYesterdayFoodVariance.isEmpty() && supervisorBonusYesterdayFoodActual.isEmpty()) {

            gm_bonus_food_error.visibility = View.VISIBLE
            food_goal_bonus.visibility = View.GONE
            food_variance_bonus.visibility = View.GONE
            food_actual_bonus.visibility = View.GONE

            val paramsSupervisorYesterdayFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsSupervisorYesterdayFoodError.weight = 2.0f
            food_display_bonus.layoutParams = paramsSupervisorYesterdayFoodError

        } else if (supervisorBonusYesterdayFoodGoal.isNotEmpty() && supervisorBonusYesterdayFoodVariance.isNotEmpty() && supervisorBonusYesterdayFoodActual.isNotEmpty()) {
            food_goal_bonus.text = supervisorBonusYesterdayFoodGoal
            food_variance_bonus.text = supervisorBonusYesterdayFoodVariance
            food_actual_bonus.text = supervisorBonusYesterdayFoodActual
            gm_bonus_food_error.visibility = View.GONE
        } else {
            gm_bonus_food_error.visibility = View.GONE
            if (supervisorBonusYesterdayFoodGoal.isEmpty()) {

                food_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_goal_bonus.text = supervisorBonusYesterdayFoodGoal
            }
            if (supervisorBonusYesterdayFoodVariance.isEmpty()) {

                food_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_variance_bonus.text = supervisorBonusYesterdayFoodVariance
            }
            if (supervisorBonusYesterdayFoodActual.isEmpty()) {

                food_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                food_actual_bonus.text = supervisorBonusYesterdayFoodActual
            }
        }


        if (food?.status?.toString() != null && supervisorBonusYesterdayFoodActual.isNotEmpty()) {

            when {
                food.status.toString() == resources.getString(R.string.out_of_range) -> {
                    food_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    food_actual_bonus.setTextColor(requireContext().getColor(color.red))
                }
                food.status.toString() == resources.getString(R.string.under_limit) -> {
                    food_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                    )
                    food_actual_bonus.setTextColor(requireContext().getColor(color.green))

                }
                food.status.toString() == resources.getString(R.string.neutral) -> {
                    food_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                    )
                    food_actual_bonus.setTextColor(requireContext().getColor(color.text_color))

                }
            }
        }

    }

    fun displayLaborViewForSupervisorBonus(labor: SupervisorBonusQuery.Labor?) {
        labour_display_bonus.text = labor?.displayName
        val supervisorBonusYesterdayLaborGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.goal?.amount,
                labor?.goal?.percentage,
                labor?.goal?.value
        )

        val supervisorBonusYesterdayLaborVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.variance?.amount,
                labor?.variance?.percentage,
                labor?.variance?.value
        )

        val supervisorBonusYesterdayLaborActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                labor?.actual?.amount,
                labor?.actual?.percentage,
                labor?.actual?.value
        )


        if (supervisorBonusYesterdayLaborGoal.isEmpty() && supervisorBonusYesterdayLaborVariance.isEmpty() && supervisorBonusYesterdayLaborActual.isEmpty()) {

            gm_bonus_labour_error.visibility = View.VISIBLE
            labour_goal_bonus.visibility = View.GONE
            labour_variance_bonus.visibility = View.GONE
            labour_actual_bonus.visibility = View.GONE
            val paramsSupervisorYesterdayLaborError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsSupervisorYesterdayLaborError.weight = 2.0f
            labour_display_bonus.layoutParams = paramsSupervisorYesterdayLaborError

        } else if (supervisorBonusYesterdayLaborGoal.isNotEmpty() && supervisorBonusYesterdayLaborVariance.isNotEmpty() && supervisorBonusYesterdayLaborActual.isNotEmpty()) {
            labour_goal_bonus.text = supervisorBonusYesterdayLaborGoal
            labour_variance_bonus.text = supervisorBonusYesterdayLaborVariance
            labour_actual_bonus.text = supervisorBonusYesterdayLaborActual
            gm_bonus_labour_error.visibility = View.GONE
        } else {
            gm_bonus_labour_error.visibility = View.GONE
            if (supervisorBonusYesterdayLaborGoal.isEmpty()) {

                labour_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_goal_bonus.text = supervisorBonusYesterdayLaborGoal
            }
            if (supervisorBonusYesterdayLaborVariance.isEmpty()) {

                labour_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_variance_bonus.text = supervisorBonusYesterdayLaborVariance
            }
            if (supervisorBonusYesterdayLaborActual.isEmpty()) {

                labour_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                labour_actual_bonus.text = supervisorBonusYesterdayLaborActual
            }
        }

        if (labor?.status != null && supervisorBonusYesterdayLaborActual.isNotEmpty()) {
            when {
                labor.status.toString() == resources.getString(R.string.out_of_range) -> {
                    labour_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.red_circle, 0
                    )
                    labour_actual_bonus.setTextColor(requireContext().getColor(color.red))

                }
                labor.status.toString() == resources.getString(R.string.under_limit) -> {
                    labour_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.green_circle, 0
                    )
                    labour_actual_bonus.setTextColor(requireContext().getColor(color.green))
                }
                else -> {
                    labour_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.black_circle, 0
                    )
                    labour_actual_bonus.setTextColor(requireContext().getColor(color.text_color))
                }
            }
        }

    }

    fun displayServiceViewForSupervisorBonus(service: SupervisorBonusQuery.Service?) {
        service_display_bonus.text = service?.displayName

        val supervisorBonusYesterdayServiceGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.goal?.amount,
                service?.goal?.percentage,
                service?.goal?.value
        )

        val supervisorBonusYesterdayServiceVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.variance?.amount,
                service?.variance?.percentage,
                service?.variance?.value
        )

        val supervisorBonusYesterdayServiceActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                service?.actual?.amount,
                service?.actual?.percentage,
                service?.actual?.value
        )


        if (supervisorBonusYesterdayServiceGoal.isEmpty() && supervisorBonusYesterdayServiceVariance.isEmpty() && supervisorBonusYesterdayServiceActual.isEmpty()) {

            gm_bonus_service_error.visibility = View.VISIBLE
            service_goal_bonus.visibility = View.GONE
            service_variance_bonus.visibility = View.GONE
            service_actual_bonus.visibility = View.GONE

            val paramsSupervisorYesterdayServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsSupervisorYesterdayServiceError.weight = 2.0f
            service_display_bonus.layoutParams = paramsSupervisorYesterdayServiceError

        } else if (supervisorBonusYesterdayServiceGoal.isNotEmpty() && supervisorBonusYesterdayServiceVariance.isNotEmpty() && supervisorBonusYesterdayServiceActual.isNotEmpty()) {
            service_goal_bonus.text = supervisorBonusYesterdayServiceGoal
            service_variance_bonus.text = supervisorBonusYesterdayServiceVariance
            service_actual_bonus.text = supervisorBonusYesterdayServiceActual
            gm_bonus_service_error.visibility = View.GONE
        } else {
            gm_bonus_service_error.visibility = View.GONE
            if (supervisorBonusYesterdayServiceGoal.isEmpty()) {

                service_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_goal_bonus.text = supervisorBonusYesterdayServiceGoal
            }
            if (supervisorBonusYesterdayServiceVariance.isEmpty()) {

                service_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_variance_bonus.text = supervisorBonusYesterdayServiceVariance
            }
            if (supervisorBonusYesterdayServiceActual.isEmpty()) {

                service_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_actual_bonus.text = supervisorBonusYesterdayServiceActual
            }
        }

        if (service?.status != null && supervisorBonusYesterdayServiceActual.isNotEmpty()) {
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

    fun displayEADTServiceViewForSupervisorBonus(eADT: SupervisorBonusQuery.EADT?) {
        eadt_display_bonus.text = eADT?.displayName
        val supervisorBonusYesterdayServiceEATDGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                eADT?.goal?.amount,
                eADT?.goal?.percentage,
                eADT?.goal?.value
        )

        val supervisorBonusYesterdayServiceEATDVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                eADT?.variance?.amount,
                eADT?.variance?.percentage,
                eADT?.variance?.value
        )

        val supervisorBonusYesterdayServiceEATDActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                eADT?.actual?.amount,
                eADT?.actual?.percentage,
                eADT?.actual?.value
        )


        if (supervisorBonusYesterdayServiceEATDGoal.isEmpty() && supervisorBonusYesterdayServiceEATDVariance.isEmpty() && supervisorBonusYesterdayServiceEATDActual.isEmpty()) {

            gm_bonus_service_eatd_error.visibility = View.VISIBLE
            service_eadt_goal_bonus.visibility = View.GONE
            service_eadt_variance_bonus.visibility = View.GONE
            service_eadt_actual_bonus.visibility = View.GONE

            val paramsSupervisorYesterdayEATDError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsSupervisorYesterdayEATDError.weight = 2.0f
            eadt_display_bonus.layoutParams = paramsSupervisorYesterdayEATDError

        } else if (supervisorBonusYesterdayServiceEATDGoal.isNotEmpty() && supervisorBonusYesterdayServiceEATDVariance.isNotEmpty() && supervisorBonusYesterdayServiceEATDActual.isNotEmpty()) {
            service_eadt_goal_bonus.text = supervisorBonusYesterdayServiceEATDGoal
            service_eadt_variance_bonus.text = supervisorBonusYesterdayServiceEATDVariance
            service_eadt_actual_bonus.text = supervisorBonusYesterdayServiceEATDActual
            gm_bonus_service_eatd_error.visibility = View.GONE
        } else {
            gm_bonus_service_eatd_error.visibility = View.GONE
            if (supervisorBonusYesterdayServiceEATDGoal.isEmpty()) {

                service_eadt_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_goal_bonus.text = supervisorBonusYesterdayServiceEATDGoal
            }
            if (supervisorBonusYesterdayServiceEATDVariance.isEmpty()) {

                service_eadt_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_variance_bonus.text = supervisorBonusYesterdayServiceEATDVariance
            }
            if (supervisorBonusYesterdayServiceEATDActual.isEmpty()) {

                service_eadt_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_eadt_actual_bonus.text = supervisorBonusYesterdayServiceEATDActual
            }
        }

        if (eADT?.status != null && supervisorBonusYesterdayServiceEATDActual.isNotEmpty()) {

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
                            0, 0, R.drawable.green_circle, 0
                    )
                    service_eadt_actual_bonus.setTextColor(requireContext().getColor(color.green))
                }
                else -> {
                    service_eadt_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.black_circle, 0
                    )
                    service_eadt_actual_bonus.setTextColor(requireContext().getColor(color.text_color))
                }
            }
        }

    }

    fun displayExtremeServiceViewForSupervisorBonus(extremeDelivery: SupervisorBonusQuery.ExtremeDelivery?) {
        extreme_delivery_display_bonus.text = extremeDelivery?.displayName

        val supervisorBonusYesterdayServiceExtremeGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.goal?.amount,
                extremeDelivery?.goal?.percentage,
                extremeDelivery?.goal?.value
        )

        val supervisorBonusYesterdayServiceExtremeVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.variance?.amount,
                extremeDelivery?.variance?.percentage,
                extremeDelivery?.variance?.value
        )

        val supervisorBonusYesterdayServiceExtremeActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                extremeDelivery?.actual?.amount,
                extremeDelivery?.actual?.percentage,
                extremeDelivery?.actual?.value
        )


        if (supervisorBonusYesterdayServiceExtremeGoal.isEmpty() && supervisorBonusYesterdayServiceExtremeVariance.isEmpty() && supervisorBonusYesterdayServiceExtremeActual.isEmpty()) {

            gm_bonus_service_extreme_error.visibility = View.VISIBLE
            service_extreme_goal_bonus.visibility = View.GONE
            service_extreme_variance_bonus.visibility = View.GONE
            service_extreme_actual_bonus.visibility = View.GONE

            val paramsSupervisorYesterdayExtremeError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsSupervisorYesterdayExtremeError.weight = 2.0f
            extreme_delivery_display_bonus.layoutParams = paramsSupervisorYesterdayExtremeError

        } else if (supervisorBonusYesterdayServiceExtremeGoal.isNotEmpty() && supervisorBonusYesterdayServiceExtremeVariance.isNotEmpty() && supervisorBonusYesterdayServiceExtremeActual.isNotEmpty()) {
            service_extreme_goal_bonus.text = supervisorBonusYesterdayServiceExtremeGoal
            service_extreme_variance_bonus.text = supervisorBonusYesterdayServiceExtremeVariance
            service_extreme_actual_bonus.text = supervisorBonusYesterdayServiceExtremeActual
            gm_bonus_service_extreme_error.visibility = View.GONE

        } else {
            gm_bonus_service_extreme_error.visibility = View.GONE

            if (supervisorBonusYesterdayServiceExtremeGoal.isEmpty()) {

                service_extreme_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_goal_bonus.text = supervisorBonusYesterdayServiceExtremeGoal
            }
            if (supervisorBonusYesterdayServiceExtremeVariance.isEmpty()) {

                service_extreme_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_variance_bonus.text = supervisorBonusYesterdayServiceExtremeVariance
            }
            if (supervisorBonusYesterdayServiceExtremeActual.isEmpty()) {

                service_extreme_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_extreme_actual_bonus.text = supervisorBonusYesterdayServiceExtremeActual
            }
        }


        if (extremeDelivery?.status != null && supervisorBonusYesterdayServiceExtremeActual.isNotEmpty()) {

            when {
                extremeDelivery.status.toString() == resources.getString(R.string.out_of_range) -> {
                    service_extreme_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                    )
                    service_extreme_actual_bonus.setTextColor(requireContext().getColor(color.red))
                }
                extremeDelivery.status.toString() == resources.getString(R.string.under_limit) -> {
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

    fun displaySinglesServiceViewForSupervisorBonus(singles: SupervisorBonusQuery.Singles?) {
        single_display_bonus.text = singles?.displayName

        val supervisorBonusYesterdayServiceSingleGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.goal?.amount,
                singles?.goal?.percentage,
                singles?.goal?.value
        )

        val supervisorBonusYesterdayServiceSingleVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.variance?.amount,
                singles?.variance?.percentage,
                singles?.variance?.value
        )

        val supervisorBonusYesterdayServiceSingleActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                singles?.actual?.amount,
                singles?.actual?.percentage,
                singles?.actual?.value
        )


        if (supervisorBonusYesterdayServiceSingleGoal.isEmpty() && supervisorBonusYesterdayServiceSingleVariance.isEmpty() && supervisorBonusYesterdayServiceSingleActual.isEmpty()) {

            gm_bonus_service_single_error.visibility = View.VISIBLE
            service_singles_goal_bonus.visibility = View.GONE
            service_singles_variance_bonus.visibility = View.GONE
            service_singles_actual_bonus.visibility = View.GONE
            val paramsSupervisorYesterdaySingleError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsSupervisorYesterdaySingleError.weight = 2.0f
            single_display_bonus.layoutParams = paramsSupervisorYesterdaySingleError

        } else if (supervisorBonusYesterdayServiceSingleGoal.isNotEmpty() && supervisorBonusYesterdayServiceSingleVariance.isNotEmpty() && supervisorBonusYesterdayServiceSingleActual.isNotEmpty()) {
            service_singles_goal_bonus.text = supervisorBonusYesterdayServiceSingleGoal
            service_singles_variance_bonus.text = supervisorBonusYesterdayServiceSingleVariance
            service_singles_actual_bonus.text = supervisorBonusYesterdayServiceSingleActual
            gm_bonus_service_single_error.visibility = View.GONE
        } else {
            gm_bonus_service_single_error.visibility = View.GONE
            if (supervisorBonusYesterdayServiceSingleGoal.isEmpty()) {

                service_singles_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_goal_bonus.text = supervisorBonusYesterdayServiceSingleGoal
            }
            if (supervisorBonusYesterdayServiceSingleVariance.isEmpty()) {

                service_singles_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_variance_bonus.text = supervisorBonusYesterdayServiceSingleVariance
            }
            if (supervisorBonusYesterdayServiceSingleActual.isEmpty()) {

                service_singles_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                service_singles_actual_bonus.text = supervisorBonusYesterdayServiceSingleActual
            }
        }

        if (singles?.status != null && supervisorBonusYesterdayServiceSingleActual.isNotEmpty()) {
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
                singles.status.toString() == resources.getString(R.string.under_limit) -> {
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

    fun displayFocusViewForSupervisorBonus(focus: SupervisorBonusQuery.Focus?) {
        focus_display_bonus.text = focus?.displayName
        val supervisorBonusYesterdayFocusGoal = Validation().checkAmountPercentageValue(
                requireActivity(),
                focus?.goal?.amount,
                focus?.goal?.percentage,
                focus?.goal?.value
        )

        val supervisorBonusYesterdayFocusVariance = Validation().checkAmountPercentageValue(
                requireActivity(),
                focus?.variance?.amount,
                focus?.variance?.percentage,
                focus?.variance?.value
        )

        val supervisorBonusYesterdayFocusActual = Validation().checkAmountPercentageValue(
                requireActivity(),
                focus?.actual?.amount,
                focus?.actual?.percentage,
                focus?.actual?.value
        )


        if (supervisorBonusYesterdayFocusGoal.isEmpty() && supervisorBonusYesterdayFocusVariance.isEmpty() && supervisorBonusYesterdayFocusActual.isEmpty()) {

            gm_bonus_focus_error.visibility = View.VISIBLE
            focus_goal_bonus.visibility = View.GONE
            focus_variance_bonus.visibility = View.GONE
            focus_actual_bonus.visibility = View.GONE
            val paramsSupervisorYesterdayFocusError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsSupervisorYesterdayFocusError.weight = 2.0f
            focus_display_bonus.layoutParams = paramsSupervisorYesterdayFocusError
        } else if (supervisorBonusYesterdayFocusGoal.isNotEmpty() && supervisorBonusYesterdayFocusVariance.isNotEmpty() && supervisorBonusYesterdayFocusActual.isNotEmpty()) {
            focus_goal_bonus.text = supervisorBonusYesterdayFocusGoal
            focus_variance_bonus.text = supervisorBonusYesterdayFocusVariance
            focus_actual_bonus.text = supervisorBonusYesterdayFocusActual

            gm_bonus_focus_error.visibility = View.GONE
        } else {
            gm_bonus_focus_error.visibility = View.GONE
            if (supervisorBonusYesterdayFocusGoal.isEmpty()) {

                focus_goal_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                focus_goal_bonus.text = supervisorBonusYesterdayFocusGoal
            }
            if (supervisorBonusYesterdayFocusVariance.isEmpty()) {

                focus_variance_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                focus_variance_bonus.text = supervisorBonusYesterdayFocusVariance
            }
            if (supervisorBonusYesterdayFocusActual.isEmpty()) {

                focus_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_error, 0
                )
            } else {
                focus_actual_bonus.text = supervisorBonusYesterdayFocusActual
            }
        }


        if (focus?.status?.toString() != null && supervisorBonusYesterdayFocusActual.isNotEmpty()) {
            when {
                focus.status.toString() == resources.getString(R.string.out_of_range) -> {
                    focus_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.red_circle, 0
                    )
                    focus_actual_bonus.setTextColor(requireContext().getColor(color.red))
                }
                focus.status.toString() == resources.getString(R.string.under_limit) -> {
                    focus_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.green_circle, 0
                    )
                    focus_actual_bonus.setTextColor(requireContext().getColor(color.green))
                }
                else -> {
                    focus_actual_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.black_circle, 0
                    )
                    focus_actual_bonus.setTextColor(requireContext().getColor(color.text_color))
                }
            }
        }

    }

    fun displayBonusNarrativeForSupervisorBonus(narrative: String?) {
        if (narrative.isNullOrEmpty()) {
            var bonusSupervisorNarrative = narrative.toString()
            bonusSupervisorNarrative = bonusSupervisorNarrative.replace("</p>", "<br><br>")
            bonus_narrative.text = Html.fromHtml(bonusSupervisorNarrative, Html.FROM_HTML_MODE_COMPACT)
        } else {
            bonus_narrative.visibility = View.INVISIBLE
            narrative_layout.visibility = View.INVISIBLE
        }

    }

    fun displayBonusViewForSupervisorBonus(bonus: SupervisorBonusQuery.Bonus1?) {
                bonus_display_bonus.text = bonus?.displayName

                val supervisorBonusYesterdayPotentialSupervisor = Validation().checkAmountPercentageValue(
                        requireActivity(),
                        bonus?.potential?.amount,
                        bonus?.potential?.percentage,
                        bonus?.potential?.value
                )

                val supervisorBonusYesterdayMissedSupervisor = Validation().checkAmountPercentageValue(
                        requireActivity(),
                        bonus?.missed?.amount,
                        bonus?.missed?.percentage,
                        bonus?.missed?.value
                )

                val supervisorBonusYesterdayEarnerSupervisor = Validation().checkAmountPercentageValue(
                        requireActivity(),
                        bonus?.earner?.amount,
                        bonus?.earner?.percentage,
                        bonus?.earner?.value
                )

                if (supervisorBonusYesterdayPotentialSupervisor.isEmpty()) {

                    bonus_potential_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.ic_error, 0
                    )
                } else {
                    bonus_potential_bonus.text = supervisorBonusYesterdayPotentialSupervisor
                }
                if (supervisorBonusYesterdayMissedSupervisor.isEmpty()) {

                    bonus_missed_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.ic_error, 0
                    )
                } else {
                    bonus_missed_bonus.text = supervisorBonusYesterdayMissedSupervisor
                }
                if (supervisorBonusYesterdayEarnerSupervisor.isEmpty()) {

                    bonus_earner_bonus.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.ic_error, 0
                    )
                } else {
                    bonus_earner_bonus.text = supervisorBonusYesterdayEarnerSupervisor
                }


                if (bonus?.status?.toString() != null && supervisorBonusYesterdayEarnerSupervisor.isNotEmpty()) {
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
    private fun showInternetErrorDialog() {
        dialogSupervisorBonus = DialogUtil.getErrorDialogAccessDialog(
                requireActivity(),
                getString(R.string.network_error_title),
                getString(R.string.network_error_description),
                getString(R.string.retry_button_text),
                {
                    callBonusSupervisorQuery()
                    dialogSupervisorBonus?.dismiss()
                    dialogSupervisorBonus = null

                },
                getString(R.string.cancel),
                {
                    dialogSupervisorBonus?.dismiss()
                    dialogSupervisorBonus = null
                }
        )
        dialogSupervisorBonus?.show()
    }

    fun setErrorScreenVisibleStateForBonusSupervisor() {
        bonus_error_layout.visibility = View.VISIBLE
        common_header_bonus.image_bonus.visibility = View.VISIBLE
        common_header_bonus.image_bonus.setImageResource(R.drawable.ic_data_unavailbale_error)
        common_header_bonus.bonus_error_filter_parent_linear.visibility = View.VISIBLE
        setBonusParentViewsVisibleStateForBonusSupervisor()
        setCalendarViewVisibleStateForBonusSupervisor()
        setHeaderViewsVisibleStateForBonusSupervisor()
    }

    fun setBonusParentViewsVisibleStateForBonusSupervisor() {
        bonus_data_layout.visibility = View.INVISIBLE
        bonus_goal_parent_layout.visibility = View.GONE
        bonus_view.visibility = View.GONE
    }

    fun setCalendarViewVisibleStateForBonusSupervisor() {
        common_calendar_bonus.visibility = View.GONE
    }

    fun setHeaderViewsVisibleStateForBonusSupervisor() {
        common_header_bonus.total_sales_common_header.visibility = View.GONE
        common_header_bonus.sales_text_common_header.visibility = View.GONE
        common_header_bonus.bonus_filter_parent_linear.visibility = View.GONE
    }

    fun hideErrorScreenVisibleStateForSupervisorBonus(){
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

    private fun refreshTokenBonusSupervisor() {
        refreshTokenViewModelBonusSupervisor.getRefreshToken()
        refreshTokenViewModelBonusSupervisor.refreshTokenResponseLiveData.observe(requireActivity(), {
            run {
                when (it.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        callBonusSupervisorQuery()
                    }
                    Status.UNSUCCESSFUL -> {
                        CommonUtil.navigateToLogin(mainActivity)
                    }
                    Status.ERROR -> {
                    }
                    Status.OFFLINE -> {
                        showInternetErrorDialog()
                    }
                }
            }
        })
    }
}