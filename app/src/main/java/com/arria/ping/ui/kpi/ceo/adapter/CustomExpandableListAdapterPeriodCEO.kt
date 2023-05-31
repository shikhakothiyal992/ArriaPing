package com.arria.ping.ui.kpi.ceo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.LinearLayout
import com.arria.ping.R
import com.arria.ping.kpi.CEODefaultPeriodRangeQuery
import com.arria.ping.kpi.ceo.CEOPeriodRangeLevelTwoQuery
import com.arria.ping.model.StoreDetailPojo

import com.arria.ping.util.Validation
import kotlinx.android.synthetic.main.expandable_list_store.view.*
import kotlinx.android.synthetic.main.expandable_list_supervisor.view.*


class CustomExpandableListAdapterPeriodCEO internal constructor(
    private val context: Context,
    private val titleListPeriodCeo: List<String>,
    private var dataListPeriodCeo: HashMap<String, List<StoreDetailPojo>>,
    private var superVisorDetailsPeriodCeo: CEOPeriodRangeLevelTwoQuery.Ceo,
    private var actionPeriodCeo: String,

    ) : BaseExpandableListAdapter() {



    override fun getChild(listPosition: Int, expandedListPosition: Int): StoreDetailPojo {
        return this.dataListPeriodCeo[this.titleListPeriodCeo[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    infix fun setChild(childData: HashMap<String, List<StoreDetailPojo>>) {
        dataListPeriodCeo = childData
        notifyDataSetChanged()
    }

    @SuppressLint("InflateParams")
    override fun getChildView(
        listPositionPeriodCeo: Int,
        expandedListPositionPeriodCeo: Int,
        isLastChildPeriodCeo: Boolean,
        convertViewPeriodCeo: View?,
        parentPeriodCeo: ViewGroup,
    ): View {
        var itemViewPeriodCeo = convertViewPeriodCeo
        val expandedListTextPeriodCeo = getChild(listPositionPeriodCeo, expandedListPositionPeriodCeo)


        if (convertViewPeriodCeo == null) {
            val layoutInflaterPeriodCeo =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemViewPeriodCeo = layoutInflaterPeriodCeo.inflate(R.layout.expandable_list_store, null)
        }
        itemViewPeriodCeo!!.store_parent

        val individualStoreList = expandedListTextPeriodCeo
        if (getChildId(listPositionPeriodCeo,
                expandedListPositionPeriodCeo) == 0L
        ) {
            val supervisorOverviewName = superVisorDetailsPeriodCeo.kpis!!.individualSupervisors[listPositionPeriodCeo]!!.supervisorName

           if(supervisorOverviewName.isNullOrEmpty()){
               itemViewPeriodCeo.store_name.text = ""
           }else{
               itemViewPeriodCeo.store_name.text = superVisorDetailsPeriodCeo.kpis!!.individualSupervisors[listPositionPeriodCeo]!!.supervisorName.plus(
                   "'s overview")
           }
            itemViewPeriodCeo.store_name.paintFlags =
                itemViewPeriodCeo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewPeriodCeo.store_name.setTypeface(itemViewPeriodCeo.store_name.typeface, Typeface.NORMAL)


            itemViewPeriodCeo.store_goal_layout.visibility = View.GONE
            itemViewPeriodCeo.store_variance_layout.visibility = View.GONE
            itemViewPeriodCeo.store_actual_layout.visibility = View.GONE
            val paramsCEORangeNameError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsCEORangeNameError.weight = 2.0f
            itemViewPeriodCeo.store_name.layoutParams = paramsCEORangeNameError

            itemViewPeriodCeo.store_error_ceo_period_range_kpi.visibility = View.GONE

        } else {
            itemViewPeriodCeo.store_name.text = individualStoreList.storeNumber
            itemViewPeriodCeo.store_name.paintFlags =
                itemViewPeriodCeo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewPeriodCeo.store_name.setTypeface(itemViewPeriodCeo.store_name.typeface, Typeface.NORMAL)
            itemViewPeriodCeo.store_goal.visibility = View.VISIBLE
            itemViewPeriodCeo.store_variance.visibility = View.VISIBLE
            itemViewPeriodCeo.store_actual.visibility = View.VISIBLE
        }
        if (getChildrenCount(listPositionPeriodCeo) < 3){
            itemViewPeriodCeo.store_parent.visibility = View.VISIBLE
            itemViewPeriodCeo.store_name.text = individualStoreList.storeNumber
            itemViewPeriodCeo.store_name.paintFlags =
                itemViewPeriodCeo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewPeriodCeo.store_name.setTypeface(itemViewPeriodCeo.store_name.typeface, Typeface.NORMAL)
            itemViewPeriodCeo.store_goal.visibility = View.VISIBLE
            itemViewPeriodCeo.store_variance.visibility = View.VISIBLE
            itemViewPeriodCeo.store_actual.visibility = View.VISIBLE
        }


        if (!individualStoreList.storeNumber.isNullOrBlank()) {

            val ceoStoreGoal = individualStoreList.storeGoal.toString()
            val ceoStoreVariance = individualStoreList.storeVariance.toString()
            val ceoStoreActual = individualStoreList.storeActual.toString()

            if (ceoStoreGoal.isEmpty() && ceoStoreVariance.isEmpty() && ceoStoreActual.isEmpty()) {

                if(actionPeriodCeo == context.getString(R.string.service_text)){
                    itemViewPeriodCeo.store_error_ceo_period_range_kpi.visibility = View.GONE
                }else{
                    itemViewPeriodCeo.store_error_ceo_period_range_kpi.visibility = View.VISIBLE
                }

                val paramsCEORangeStoreError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsCEORangeStoreError.weight = 2.0f
                itemViewPeriodCeo.store_name.layoutParams = paramsCEORangeStoreError

                itemViewPeriodCeo.store_goal.visibility = View.GONE
                itemViewPeriodCeo.store_variance.visibility = View.GONE
                itemViewPeriodCeo.store_actual.visibility = View.GONE

                itemViewPeriodCeo.store_goal_layout.visibility = View.GONE
                itemViewPeriodCeo.store_variance_layout.visibility = View.GONE
                itemViewPeriodCeo.store_actual_layout.visibility = View.GONE

            } else if (ceoStoreGoal.isNotEmpty() && ceoStoreVariance.isNotEmpty() && ceoStoreActual.isNotEmpty()) {
                itemViewPeriodCeo.store_error_ceo_period_range_kpi.visibility = View.GONE
                itemViewPeriodCeo.store_goal.visibility = View.VISIBLE
                itemViewPeriodCeo.store_variance.visibility = View.VISIBLE
                itemViewPeriodCeo.store_actual.visibility = View.VISIBLE

                itemViewPeriodCeo.store_goal_error.visibility = View.GONE
                itemViewPeriodCeo.store_variance_error.visibility = View.GONE
                itemViewPeriodCeo.store_actual_error.visibility = View.GONE
                itemViewPeriodCeo.store_goal_layout.visibility = View.VISIBLE
                itemViewPeriodCeo.store_variance_layout.visibility = View.VISIBLE
                itemViewPeriodCeo.store_actual_layout.visibility = View.VISIBLE
                val paramsCEORangeStores: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsCEORangeStores.weight = 0.94f
                itemViewPeriodCeo.store_name.layoutParams = paramsCEORangeStores

                itemViewPeriodCeo.store_goal.text = ceoStoreGoal
                itemViewPeriodCeo.store_variance.text = ceoStoreVariance
                itemViewPeriodCeo.store_actual.text = ceoStoreActual
            } else {
                val paramsCEORangeStore: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsCEORangeStore.weight = 0.94f
                itemViewPeriodCeo.store_name.layoutParams = paramsCEORangeStore

                
                itemViewPeriodCeo.store_error_ceo_period_range_kpi.visibility = View.GONE
                if (ceoStoreGoal.isNotEmpty()) {
                    itemViewPeriodCeo.store_goal.text = ceoStoreGoal
                    itemViewPeriodCeo.store_goal.visibility = View.VISIBLE
                    itemViewPeriodCeo.store_goal_error.visibility = View.GONE
                } else {
                    itemViewPeriodCeo.store_goal.visibility = View.INVISIBLE
                    itemViewPeriodCeo.store_goal_error.visibility = View.VISIBLE
                }
                if (ceoStoreVariance.isNotEmpty()) {
                    itemViewPeriodCeo.store_variance.text = ceoStoreVariance
                    itemViewPeriodCeo.store_variance.visibility = View.VISIBLE
                    itemViewPeriodCeo.store_variance_error.visibility = View.GONE
                } else {
                    itemViewPeriodCeo.store_variance.visibility = View.INVISIBLE
                    itemViewPeriodCeo.store_variance_error.visibility = View.VISIBLE
                }
                if (ceoStoreActual.isNotEmpty()) {
                    itemViewPeriodCeo.store_actual.text = ceoStoreActual
                    itemViewPeriodCeo.store_actual.visibility = View.VISIBLE
                    itemViewPeriodCeo.store_actual_error.visibility = View.GONE
                } else {
                    itemViewPeriodCeo.store_actual.visibility = View.INVISIBLE
                    itemViewPeriodCeo.store_actual_error.visibility = View.VISIBLE
                }
            }
            if (!individualStoreList.status.isNullOrEmpty() && !individualStoreList.storeActual.isNullOrEmpty()) {
                itemViewPeriodCeo.store_actual.text = individualStoreList.storeActual.toString()
                when (individualStoreList.status) {
                    context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewPeriodCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewPeriodCeo.store_actual.setTextColor(context.getColor(R.color.red))

                    }
                    context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewPeriodCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewPeriodCeo.store_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewPeriodCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewPeriodCeo.store_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }

        return itemViewPeriodCeo
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.dataListPeriodCeo[this.titleListPeriodCeo[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return this.titleListPeriodCeo[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.titleListPeriodCeo.size
    }

    override fun getGroupId(listPositionPeriodCeo: Int): Long {
        return listPositionPeriodCeo.toLong()
    }

    @SuppressLint("InflateParams")
    override fun getGroupView(
        listPositionGroupPeriodCeo: Int,
        isExpandedGroupPeriodCeo: Boolean,
        convertViewGroupPeriodCeo: View?,
        parentGroupPeriodCeo: ViewGroup,
    ): View {
        var itemViewGroupPeriodCeo = convertViewGroupPeriodCeo
        if (convertViewGroupPeriodCeo == null) {
            val layoutInflaterGroupPeriodCeo =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemViewGroupPeriodCeo = layoutInflaterGroupPeriodCeo.inflate(R.layout.expandable_list_supervisor, null)
        }
        val individualSupervisorList = superVisorDetailsPeriodCeo.kpis!!.individualSupervisors[listPositionGroupPeriodCeo]
        itemViewGroupPeriodCeo!!.supervisor_name.text =
            if (individualSupervisorList?.supervisorName != null) individualSupervisorList.supervisorName else ""

        if(individualSupervisorList?.period == null){
            itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.INVISIBLE
        }else{
            itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.VISIBLE
            if (isExpandedGroupPeriodCeo) {
                itemViewGroupPeriodCeo.supervisor_expandable_image.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0, R
                                .drawable.expand_list_indicator_up, 0
                )
            }else{
                itemViewGroupPeriodCeo.supervisor_expandable_image.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0, R
                                .drawable.expand_list_indicator_down, 0
                )
            }
        }

        if (actionPeriodCeo == context.getString(R.string.awus_text)) {

            val awusExpandableSupervisorGoal = Validation().checkAmountPercentageValue(context,
                                                                                       individualSupervisorList?.period?.sales?.goal?.amount,
                                                                                       individualSupervisorList?.period?.sales?.goal?.percentage,
                                                                                       individualSupervisorList?.period?.sales?.goal?.value)

            val awusExpandableSupervisorVariance = Validation().checkAmountPercentageValue(context,
                                                                                           individualSupervisorList?.period?.sales?.variance?.amount,
                                                                                           individualSupervisorList?.period?.sales?.variance?.percentage,
                                                                                           individualSupervisorList?.period?.sales?.variance?.value)

            val awusExpandableSupervisorActual = Validation().checkAmountPercentageValue(context,
                                                                                         individualSupervisorList?.period?.sales?.actual?.amount,
                                                                                         individualSupervisorList?.period?.sales?.actual?.percentage,
                                                                                         individualSupervisorList?.period?.sales?.actual?.value)

            if(awusExpandableSupervisorGoal.isEmpty() && awusExpandableSupervisorVariance.isEmpty() && awusExpandableSupervisorActual.isEmpty()){
                itemViewGroupPeriodCeo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_actual_layout.visibility = View.GONE
                val paramsAWUSError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsAWUSError.weight = 2.0f
                itemViewGroupPeriodCeo.supervisor_name.layoutParams = paramsAWUSError
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.INVISIBLE
                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }else if(awusExpandableSupervisorGoal.isNotEmpty() && awusExpandableSupervisorVariance.isNotEmpty() && awusExpandableSupervisorActual.isNotEmpty()){
                itemViewGroupPeriodCeo.supervisor_goal.text = awusExpandableSupervisorGoal
                itemViewGroupPeriodCeo.supervisor_variance.text = awusExpandableSupervisorVariance
                itemViewGroupPeriodCeo.supervisor_actual.text = awusExpandableSupervisorActual
                itemViewGroupPeriodCeo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.VISIBLE
            } else {
                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.VISIBLE
                
                if (awusExpandableSupervisorGoal.isNotEmpty()) {
                    itemViewGroupPeriodCeo.supervisor_goal.text = awusExpandableSupervisorGoal
                    itemViewGroupPeriodCeo.supervisor_goal_error.visibility = View.GONE
                } else {
                    itemViewGroupPeriodCeo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupPeriodCeo.supervisor_goal_error.visibility = View.VISIBLE
                }
                if (awusExpandableSupervisorVariance.isNotEmpty()) {
                    itemViewGroupPeriodCeo.supervisor_variance.text = awusExpandableSupervisorVariance
                    itemViewGroupPeriodCeo.supervisor_variance_error.visibility = View.GONE
                } else {
                    itemViewGroupPeriodCeo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupPeriodCeo.supervisor_variance_error.visibility = View.VISIBLE
                }

                if (awusExpandableSupervisorActual.isNotEmpty()) {
                    itemViewGroupPeriodCeo.supervisor_actual.text = awusExpandableSupervisorActual
                    itemViewGroupPeriodCeo.supervisor_actual_error.visibility = View.GONE
                } else {
                    itemViewGroupPeriodCeo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupPeriodCeo.supervisor_actual_error.visibility = View.VISIBLE
                }
            }

            if (individualSupervisorList?.period?.sales?.status?.toString() != null && awusExpandableSupervisorActual.isNotEmpty()) {
                when {
                    individualSupervisorList.period.sales.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorList.period.sales.status.toString() == context.resources.getString(
                        R.string.under_limit) -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }

        else if (actionPeriodCeo == context.getString(R.string.ideal_vs_food_variance_text)) {

            val foodExpandableSupervisorGoal = Validation().checkAmountPercentageValue(context,
                                                                                       individualSupervisorList?.period?.food?.goal?.amount,
                                                                                       individualSupervisorList?.period?.food?.goal?.percentage,
                                                                                       individualSupervisorList?.period?.food?.goal?.value)

            val foodExpandableSupervisorVariance = Validation().checkAmountPercentageValue(context,
                                                                                           individualSupervisorList?.period?.food?.variance?.amount,
                                                                                           individualSupervisorList?.period?.food?.variance?.percentage,
                                                                                           individualSupervisorList?.period?.food?.variance?.value)

            val foodExpandableSupervisorActual = Validation().checkAmountPercentageValue(context,
                                                                                         individualSupervisorList?.period?.food?.actual?.amount,
                                                                                         individualSupervisorList?.period?.food?.actual?.percentage,
                                                                                         individualSupervisorList?.period?.food?.actual?.value)

            if(foodExpandableSupervisorGoal.isEmpty() && foodExpandableSupervisorVariance.isEmpty() && foodExpandableSupervisorActual.isEmpty()){
                itemViewGroupPeriodCeo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_actual_layout.visibility = View.GONE
                val paramsFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsFoodError.weight = 2.0f
                itemViewGroupPeriodCeo.supervisor_name.layoutParams = paramsFoodError
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.INVISIBLE
                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(foodExpandableSupervisorGoal.isNotEmpty() && foodExpandableSupervisorVariance.isNotEmpty() && foodExpandableSupervisorActual.isNotEmpty()){

                val paramsFood: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsFood.weight = 2.0f
                itemViewGroupPeriodCeo.supervisor_name.layoutParams = paramsFood
                itemViewGroupPeriodCeo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupPeriodCeo.supervisor_goal.text = foodExpandableSupervisorGoal
                itemViewGroupPeriodCeo.supervisor_variance.text = foodExpandableSupervisorVariance
                itemViewGroupPeriodCeo.supervisor_actual.text = foodExpandableSupervisorActual
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            }
            else{
                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.VISIBLE
                
                if(foodExpandableSupervisorGoal.isNotEmpty()){
                    itemViewGroupPeriodCeo.supervisor_goal.text = foodExpandableSupervisorGoal
                    itemViewGroupPeriodCeo.supervisor_goal_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodCeo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupPeriodCeo.supervisor_goal_error.visibility = View.VISIBLE
                }

                if(foodExpandableSupervisorVariance.isNotEmpty()){
                    itemViewGroupPeriodCeo.supervisor_variance.text = foodExpandableSupervisorVariance
                    itemViewGroupPeriodCeo.supervisor_variance_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodCeo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupPeriodCeo.supervisor_variance_error.visibility = View.VISIBLE
                }

                if(foodExpandableSupervisorActual.isNotEmpty()){
                    itemViewGroupPeriodCeo.supervisor_actual.text = foodExpandableSupervisorActual
                    itemViewGroupPeriodCeo.supervisor_actual_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodCeo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupPeriodCeo.supervisor_actual_error.visibility = View.VISIBLE

                }

            }
            if (individualSupervisorList?.period?.food?.status != null && foodExpandableSupervisorActual.isNotEmpty()) {
                when {
                    individualSupervisorList.period.food.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorList.period.food.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }

        else if (actionPeriodCeo == context.getString(R.string.labour_text)) {

            val laborExpandableSupervisorGoal = Validation().checkAmountPercentageValue(context,
                                                                                        individualSupervisorList?.period?.labor?.goal?.amount,
                                                                                        individualSupervisorList?.period?.labor?.goal?.percentage,
                                                                                        individualSupervisorList?.period?.labor?.goal?.value)

            val laborExpandableSupervisorVariance = Validation().checkAmountPercentageValue(context,
                                                                                            individualSupervisorList?.period?.labor?.variance?.amount,
                                                                                            individualSupervisorList?.period?.labor?.variance?.percentage,
                                                                                            individualSupervisorList?.period?.labor?.variance?.value)

            val laborExpandableSupervisorActual = Validation().checkAmountPercentageValue(context,
                                                                                          individualSupervisorList?.period?.labor?.actual?.amount,
                                                                                          individualSupervisorList?.period?.labor?.actual?.percentage,
                                                                                          individualSupervisorList?.period?.labor?.actual?.value)

            if(laborExpandableSupervisorGoal.isEmpty() && laborExpandableSupervisorVariance.isEmpty() && laborExpandableSupervisorActual.isEmpty()){
                itemViewGroupPeriodCeo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_actual_layout.visibility = View.GONE
                val paramsLabourError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsLabourError.weight = 2.0f
                itemViewGroupPeriodCeo.supervisor_name.layoutParams = paramsLabourError
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.INVISIBLE
                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(laborExpandableSupervisorGoal.isNotEmpty() && laborExpandableSupervisorVariance.isNotEmpty() && laborExpandableSupervisorActual.isNotEmpty()){
                val paramsLabour: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsLabour.weight = 2.0f
                itemViewGroupPeriodCeo.supervisor_name.layoutParams = paramsLabour
                itemViewGroupPeriodCeo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupPeriodCeo.supervisor_goal.text = laborExpandableSupervisorGoal
                itemViewGroupPeriodCeo.supervisor_variance.text = laborExpandableSupervisorVariance
                itemViewGroupPeriodCeo.supervisor_actual.text = laborExpandableSupervisorActual
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            }
            else{
                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.VISIBLE
                
                if(laborExpandableSupervisorGoal.isNotEmpty()){
                    itemViewGroupPeriodCeo.supervisor_goal.text = laborExpandableSupervisorGoal
                    itemViewGroupPeriodCeo.supervisor_goal_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodCeo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupPeriodCeo.supervisor_goal_error.visibility = View.GONE
                }
                if(laborExpandableSupervisorVariance.isNotEmpty()){
                    itemViewGroupPeriodCeo.supervisor_variance.text = laborExpandableSupervisorVariance
                    itemViewGroupPeriodCeo.supervisor_variance_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodCeo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupPeriodCeo.supervisor_variance_error.visibility = View.GONE

                }
                if(laborExpandableSupervisorActual.isNotEmpty()){
                    itemViewGroupPeriodCeo.supervisor_actual.text = laborExpandableSupervisorActual
                    itemViewGroupPeriodCeo.supervisor_actual_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodCeo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupPeriodCeo.supervisor_actual_error.visibility = View.GONE

                }
            }

            if (individualSupervisorList?.period?.labor?.status?.toString() != null && laborExpandableSupervisorActual.isNotEmpty()) {
                when {
                    individualSupervisorList.period.labor.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorList.period.labor.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        } else if (actionPeriodCeo == context.getString(R.string.service_text)) {

            val serviceExpandableSupervisorGoal = Validation().checkAmountPercentageValue(
                    context,
                    individualSupervisorList?.period?.service?.goal?.amount,
                    individualSupervisorList?.period?.service?.goal?.percentage,
                    individualSupervisorList?.period?.service?.goal?.value
            )

            val serviceExpandableSupervisorVariance = Validation().checkAmountPercentageValue(
                    context,
                    individualSupervisorList?.period?.service?.variance?.amount,
                    individualSupervisorList?.period?.service?.variance?.percentage,
                    individualSupervisorList?.period?.service?.variance?.value
            )

            val serviceExpandableSupervisorActual = Validation().checkAmountPercentageValue(
                    context,
                    individualSupervisorList?.period?.service?.actual?.amount,
                    individualSupervisorList?.period?.service?.actual?.percentage,
                    individualSupervisorList?.period?.service?.actual?.value
            )

            if (individualSupervisorList?.period == null) {
                itemViewGroupPeriodCeo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_actual_layout.visibility = View.GONE

                val paramsServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsServiceError.weight = 2.0f
                itemViewGroupPeriodCeo.supervisor_name.layoutParams = paramsServiceError

                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            } else {
                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                if (serviceExpandableSupervisorGoal.isEmpty() && serviceExpandableSupervisorVariance.isEmpty() && serviceExpandableSupervisorActual.isEmpty()) {
                    itemViewGroupPeriodCeo.supervisor_goal_layout.visibility = View.GONE
                    itemViewGroupPeriodCeo.supervisor_variance_layout.visibility = View.GONE
                    itemViewGroupPeriodCeo.supervisor_actual_layout.visibility = View.GONE

                    val paramsServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsServiceError.weight = 2.0f
                    itemViewGroupPeriodCeo.supervisor_name.layoutParams = paramsServiceError

                } else if (serviceExpandableSupervisorGoal.isNotEmpty() && serviceExpandableSupervisorVariance.isNotEmpty() && serviceExpandableSupervisorActual.isNotEmpty()) {
                    val paramsService: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsService.weight = 2.0f
                    itemViewGroupPeriodCeo.supervisor_name.layoutParams = paramsService
                    itemViewGroupPeriodCeo.supervisor_goal_layout.visibility = View.VISIBLE
                    itemViewGroupPeriodCeo.supervisor_variance_layout.visibility = View.VISIBLE
                    itemViewGroupPeriodCeo.supervisor_actual_layout.visibility = View.VISIBLE

                    itemViewGroupPeriodCeo.supervisor_goal.text = serviceExpandableSupervisorGoal
                    itemViewGroupPeriodCeo.supervisor_variance.text = serviceExpandableSupervisorVariance
                    itemViewGroupPeriodCeo.supervisor_actual.text = serviceExpandableSupervisorActual

                } else {
                    if (serviceExpandableSupervisorGoal.isNotEmpty()) {
                        itemViewGroupPeriodCeo.supervisor_goal.text = serviceExpandableSupervisorGoal
                        itemViewGroupPeriodCeo.supervisor_goal_error.visibility = View.GONE
                    } else {
                        itemViewGroupPeriodCeo.supervisor_goal.visibility = View.INVISIBLE
                        itemViewGroupPeriodCeo.supervisor_goal_error.visibility = View.VISIBLE
                    }
                    if (serviceExpandableSupervisorVariance.isNotEmpty()) {
                        itemViewGroupPeriodCeo.supervisor_variance.text = serviceExpandableSupervisorVariance
                        itemViewGroupPeriodCeo.supervisor_variance_error.visibility = View.GONE
                    } else {
                        itemViewGroupPeriodCeo.supervisor_variance.visibility = View.INVISIBLE
                        itemViewGroupPeriodCeo.supervisor_variance_error.visibility = View.VISIBLE
                    }
                    if (serviceExpandableSupervisorActual.isNotEmpty()) {
                        itemViewGroupPeriodCeo.supervisor_actual.text = serviceExpandableSupervisorActual
                        itemViewGroupPeriodCeo.supervisor_actual_error.visibility = View.GONE

                    } else {
                        itemViewGroupPeriodCeo.supervisor_actual.visibility = View.INVISIBLE
                        itemViewGroupPeriodCeo.supervisor_actual_error.visibility = View.VISIBLE

                    }
                }

                if (individualSupervisorList.period.service?.status?.toString() != null && serviceExpandableSupervisorActual.isNotEmpty()) {

                    when {
                        individualSupervisorList.period.service.status.toString() == context.resources.getString(
                                R.string.out_of_range
                        ) -> {
                            itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.red_circle,
                                    0
                            )
                            itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                        }
                        individualSupervisorList.period.service.status.toString() == context.resources.getString(
                                R.string.under_limit
                        ) -> {
                            itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.green_circle,
                                    0
                            )
                            itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                        }
                        else -> {
                            itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.black_circle,
                                    0
                            )
                            itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                        }
                    }
                }

            }

        }

        else if (actionPeriodCeo == context.getString(R.string.cash_text)) {

            val cashExpandableSupervisorActual = Validation().checkAmountPercentageValue(context,
                                                                                         individualSupervisorList?.period?.cash?.actual?.amount,
                                                                                         individualSupervisorList?.period?.cash?.actual?.percentage,
                                                                                         individualSupervisorList?.period?.cash?.actual?.value)

            if(cashExpandableSupervisorActual.isNotEmpty()){
                itemViewGroupPeriodCeo.supervisor_goal_layout.visibility = View.INVISIBLE
                itemViewGroupPeriodCeo.supervisor_variance_layout.visibility = View.INVISIBLE
                itemViewGroupPeriodCeo.supervisor_actual_layout.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_actual.text = cashExpandableSupervisorActual
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.VISIBLE
            }else{
                
                itemViewGroupPeriodCeo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_actual_layout.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.INVISIBLE

            }

            if (individualSupervisorList?.period?.cash?.status?.toString() != null && cashExpandableSupervisorActual.isNotEmpty()) {
                when {
                    individualSupervisorList.period.cash.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    )-> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorList.period.cash.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }

        else if (actionPeriodCeo == context.getString(R.string.oer_text)) {

            val oerStartExpandableSupervisorGoal = Validation().checkAmountPercentageValue(context,
                                                                                           individualSupervisorList?.period?.oerStart?.goal?.amount,
                                                                                           individualSupervisorList?.period?.oerStart?.goal?.percentage,
                                                                                           individualSupervisorList?.period?.oerStart?.goal?.value)

            val oerStartExpandableSupervisorVariance = Validation().checkAmountPercentageValue(context,
                                                                                               individualSupervisorList?.period?.oerStart?.variance?.amount,
                                                                                               individualSupervisorList?.period?.oerStart?.variance?.percentage,
                                                                                               individualSupervisorList?.period?.oerStart?.variance?.value)

            val oerStartExpandableSupervisorActual = Validation().checkAmountPercentageValue(context,
                                                                                             individualSupervisorList?.period?.oerStart?.actual?.amount,
                                                                                             individualSupervisorList?.period?.oerStart?.actual?.percentage,
                                                                                             individualSupervisorList?.period?.oerStart?.actual?.value)

            if(oerStartExpandableSupervisorGoal.isEmpty() && oerStartExpandableSupervisorVariance.isEmpty() && oerStartExpandableSupervisorActual.isEmpty()){
                itemViewGroupPeriodCeo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupPeriodCeo.supervisor_actual_layout.visibility = View.GONE
                
                val paramsOERError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsOERError.weight = 2.0f
                itemViewGroupPeriodCeo.supervisor_name.layoutParams = paramsOERError
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.INVISIBLE
                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(oerStartExpandableSupervisorGoal.isNotEmpty() && oerStartExpandableSupervisorVariance.isNotEmpty() && oerStartExpandableSupervisorActual.isNotEmpty()){
                val paramsOer: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsOer.weight = 2.0f
                itemViewGroupPeriodCeo.supervisor_name.layoutParams = paramsOer

                itemViewGroupPeriodCeo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupPeriodCeo.supervisor_goal.text = oerStartExpandableSupervisorGoal
                itemViewGroupPeriodCeo.supervisor_variance.text = oerStartExpandableSupervisorVariance
                itemViewGroupPeriodCeo.supervisor_actual.text = oerStartExpandableSupervisorActual
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            } else {
                itemViewGroupPeriodCeo.supervisor_expandable_image.visibility = View.VISIBLE
                itemViewGroupPeriodCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                if (oerStartExpandableSupervisorGoal.isNotEmpty()) {
                    itemViewGroupPeriodCeo.supervisor_goal.text = oerStartExpandableSupervisorGoal
                    itemViewGroupPeriodCeo.supervisor_goal_error.visibility = View.GONE
                } else {
                    itemViewGroupPeriodCeo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupPeriodCeo.supervisor_goal_error.visibility = View.VISIBLE
                }
                if (oerStartExpandableSupervisorVariance.isNotEmpty()) {
                    itemViewGroupPeriodCeo.supervisor_variance.text = oerStartExpandableSupervisorVariance
                    itemViewGroupPeriodCeo.supervisor_variance_error.visibility = View.GONE
                } else {
                    itemViewGroupPeriodCeo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupPeriodCeo.supervisor_variance_error.visibility = View.VISIBLE
                }
                if (oerStartExpandableSupervisorActual.isNotEmpty()) {
                    itemViewGroupPeriodCeo.supervisor_actual.text = oerStartExpandableSupervisorActual
                    itemViewGroupPeriodCeo.supervisor_actual_error.visibility = View.GONE
                } else {
                    itemViewGroupPeriodCeo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupPeriodCeo.supervisor_actual_error.visibility = View.VISIBLE

                }
            }

            if (individualSupervisorList?.period?.oerStart?.status?.toString() != null && oerStartExpandableSupervisorActual.isNotEmpty()) {
                when {
                    individualSupervisorList.period.oerStart.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorList.period.oerStart.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupPeriodCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupPeriodCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }

        return itemViewGroupPeriodCeo
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }

}