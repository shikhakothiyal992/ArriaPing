package com.arria.ping.ui.kpi.do_.adapter

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
import com.arria.ping.kpi._do.DoPeriodRangeLevelTwoQuery
import com.arria.ping.model.StoreDetailPojo

import com.arria.ping.util.Validation
import kotlinx.android.synthetic.main.expandable_list_store.view.*
import kotlinx.android.synthetic.main.expandable_list_supervisor.view.*

class CustomExpandableListAdapterPeriodDO internal constructor(
    private val context: Context,
    private val titleListPeriodDo: List<String>,
    private var dataListPeriodDo: HashMap<String, List<StoreDetailPojo>>,
    private var superVisorDetailsPeriodDo: DoPeriodRangeLevelTwoQuery.Do_,
    private var actionPeriodDo: String,

    ) : BaseExpandableListAdapter() {



    override fun getChild(listPosition: Int, expandedListPosition: Int): StoreDetailPojo {
        return this.dataListPeriodDo[this.titleListPeriodDo[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    infix fun setChild(childData: HashMap<String, List<StoreDetailPojo>>) {
        dataListPeriodDo = childData
        notifyDataSetChanged()
    }

    @SuppressLint("InflateParams")
    override fun getChildView(
        listPositionPeriodDo: Int,
        expandedListPositionPeriodDo: Int,
        isLastChildPeriodDo: Boolean,
        convertViewPeriodDo: View?,
        parentPeriodDo: ViewGroup,
    ): View {
        var itemViewPeriodDo = convertViewPeriodDo
        val expandedListTextPeriodDo = getChild(listPositionPeriodDo, expandedListPositionPeriodDo)


        if (convertViewPeriodDo == null) {
            val layoutInflaterPeriodDo =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemViewPeriodDo = layoutInflaterPeriodDo.inflate(R.layout.expandable_list_store, null)
        }
        itemViewPeriodDo!!.store_parent

        val individualStoreListPeriodDo = expandedListTextPeriodDo
        if (getChildId(listPositionPeriodDo,
                expandedListPositionPeriodDo) == 0L
        ) {

            itemViewPeriodDo.store_name.text =
                if (superVisorDetailsPeriodDo.kpis!!.individualSupervisors[listPositionPeriodDo]!!.supervisorName != null) superVisorDetailsPeriodDo.kpis!!.individualSupervisors[listPositionPeriodDo]!!.supervisorName.plus(
                    "'s overview") else ""
            itemViewPeriodDo.store_name.paintFlags = itemViewPeriodDo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewPeriodDo.store_name.setTypeface(itemViewPeriodDo.store_name.typeface, Typeface.NORMAL)

            val paramsDOPeriodRangeNameError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsDOPeriodRangeNameError.weight = 2.0f
            itemViewPeriodDo.store_name.layoutParams = paramsDOPeriodRangeNameError

            itemViewPeriodDo.store_goal_layout.visibility = View.GONE
            itemViewPeriodDo.store_variance_layout.visibility = View.GONE
            itemViewPeriodDo.store_actual_layout.visibility = View.GONE
            itemViewPeriodDo.store_error_ceo_period_range_kpi.visibility = View.GONE

        } else {
            itemViewPeriodDo.store_name.text = individualStoreListPeriodDo.storeNumber
            itemViewPeriodDo.store_name.paintFlags = itemViewPeriodDo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewPeriodDo.store_name.setTypeface(itemViewPeriodDo.store_name.typeface, Typeface.NORMAL)
            itemViewPeriodDo.store_goal.visibility = View.VISIBLE
            itemViewPeriodDo.store_variance.visibility = View.VISIBLE
            itemViewPeriodDo.store_actual.visibility = View.VISIBLE

        }
        if (getChildrenCount(listPositionPeriodDo) < 3){
            itemViewPeriodDo.store_parent.visibility = View.VISIBLE
            itemViewPeriodDo.store_name.text = individualStoreListPeriodDo.storeNumber
            itemViewPeriodDo.store_name.paintFlags =
                itemViewPeriodDo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewPeriodDo.store_name.setTypeface(itemViewPeriodDo.store_name.typeface, Typeface.NORMAL)
            itemViewPeriodDo.store_goal.visibility = View.VISIBLE
            itemViewPeriodDo.store_variance.visibility = View.VISIBLE
            itemViewPeriodDo.store_actual.visibility = View.VISIBLE
        }

        if(!individualStoreListPeriodDo.storeNumber.isNullOrEmpty()){

            val doStoreGoal = individualStoreListPeriodDo.storeGoal.toString()
            val doStoreVariance = individualStoreListPeriodDo.storeVariance.toString()
            val doStoreActual = individualStoreListPeriodDo.storeActual.toString()

            if(doStoreGoal.isEmpty() && doStoreVariance.isEmpty() && doStoreActual.isEmpty()){
                if(actionPeriodDo == context.getString(R.string.service_text)){
                    itemViewPeriodDo.store_error_ceo_period_range_kpi.visibility = View.GONE
                }else{
                    itemViewPeriodDo.store_error_ceo_period_range_kpi.visibility = View.VISIBLE
                }
                itemViewPeriodDo.store_goal.visibility = View.GONE
                itemViewPeriodDo.store_variance.visibility = View.GONE
                itemViewPeriodDo.store_actual.visibility = View.GONE

                itemViewPeriodDo.store_goal_layout.visibility = View.GONE
                itemViewPeriodDo.store_variance_layout.visibility = View.GONE
                itemViewPeriodDo.store_actual_layout.visibility = View.GONE
                val paramsDORangeStoreError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsDORangeStoreError.weight = 2.0f
                itemViewPeriodDo.store_name.layoutParams = paramsDORangeStoreError

            }else if(doStoreGoal.isNotEmpty() && doStoreVariance.isNotEmpty() && doStoreActual.isNotEmpty()){

                itemViewPeriodDo.store_error_ceo_period_range_kpi.visibility = View.GONE
                itemViewPeriodDo.store_goal.visibility = View.VISIBLE
                itemViewPeriodDo.store_variance.visibility = View.VISIBLE
                itemViewPeriodDo.store_actual.visibility = View.VISIBLE
                itemViewPeriodDo.store_goal_layout.visibility = View.VISIBLE
                itemViewPeriodDo.store_variance_layout.visibility = View.VISIBLE
                itemViewPeriodDo.store_actual_layout.visibility = View.VISIBLE
                val paramsDoRangeStores: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsDoRangeStores.weight = 0.94f
                itemViewPeriodDo.store_name.layoutParams = paramsDoRangeStores

                itemViewPeriodDo.store_goal.text=doStoreGoal
                itemViewPeriodDo.store_variance.text=doStoreVariance
                itemViewPeriodDo.store_actual.text=doStoreActual

            }else{
                
                val paramsDoRangeStore: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsDoRangeStore.weight = 0.94f
                itemViewPeriodDo.store_name.layoutParams = paramsDoRangeStore

                itemViewPeriodDo.store_error_ceo_period_range_kpi.visibility = View.GONE

                if (doStoreGoal.isNotEmpty()) {
                    itemViewPeriodDo.store_goal.text=doStoreGoal
                    itemViewPeriodDo.store_goal.visibility = View.VISIBLE
                    itemViewPeriodDo.store_goal_error.visibility = View.GONE
                }else{
                    itemViewPeriodDo.store_goal.visibility = View.INVISIBLE
                    itemViewPeriodDo.store_goal_error.visibility = View.VISIBLE
                }

                if (doStoreVariance.isNotEmpty()) {
                    itemViewPeriodDo.store_variance.text = doStoreVariance
                    itemViewPeriodDo.store_variance.visibility = View.VISIBLE
                    itemViewPeriodDo.store_variance_error.visibility = View.GONE

                }else{
                    itemViewPeriodDo.store_variance.visibility = View.INVISIBLE
                    itemViewPeriodDo.store_variance_error.visibility = View.VISIBLE
                }
                if (doStoreActual.isNotEmpty()) {
                    itemViewPeriodDo.store_actual.text = doStoreActual
                    itemViewPeriodDo.store_actual.visibility = View.VISIBLE
                    itemViewPeriodDo.store_actual_error.visibility = View.GONE
                }else{
                    itemViewPeriodDo.store_actual.visibility = View.INVISIBLE
                    itemViewPeriodDo.store_actual_error.visibility = View.VISIBLE
                }
            }

        }
        

        if ((!individualStoreListPeriodDo.status.isNullOrEmpty() && !individualStoreListPeriodDo.storeActual.isNullOrEmpty())) {
            when (individualStoreListPeriodDo.status) {
                context.resources.getString(
                    R.string.out_of_range
                ) -> {
                    itemViewPeriodDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    itemViewPeriodDo.store_actual.setTextColor(context.getColor(R.color.red))

                }
                context.resources.getString(
                    R.string.under_limit
                ) -> {
                    itemViewPeriodDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    itemViewPeriodDo.store_actual.setTextColor(context.getColor(R.color.green))

                } else -> {
                    itemViewPeriodDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    itemViewPeriodDo.store_actual.setTextColor(context.getColor(R.color.text_color))

                }
            }
        }

        return itemViewPeriodDo
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.dataListPeriodDo[this.titleListPeriodDo[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return this.titleListPeriodDo[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.titleListPeriodDo.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    @SuppressLint("InflateParams")
    override fun getGroupView(
        listPositionGroupPeriodDo: Int,
        isExpandedGroupPeriodDo: Boolean,
        convertViewGroupPeriodDo: View?,
        parentGroupPeriodDo: ViewGroup,
    ): View {
        var itemViewGroupPeriodDo = convertViewGroupPeriodDo
        if (convertViewGroupPeriodDo == null) {
            val layoutInflaterGroupPeriodDo =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemViewGroupPeriodDo = layoutInflaterGroupPeriodDo.inflate(R.layout.expandable_list_supervisor, null)
        }
        val individualSupervisorListGroupPeriodDo = superVisorDetailsPeriodDo.kpis!!.individualSupervisors[listPositionGroupPeriodDo]
        itemViewGroupPeriodDo!!.supervisor_name.text =
            if (individualSupervisorListGroupPeriodDo?.supervisorName != null) individualSupervisorListGroupPeriodDo.supervisorName else ""

         if(individualSupervisorListGroupPeriodDo?.period == null){
             itemViewGroupPeriodDo.supervisor_expandable_image.visibility = View.INVISIBLE
        }else{
             itemViewGroupPeriodDo.supervisor_expandable_image.visibility = View.VISIBLE
            if (isExpandedGroupPeriodDo) {
                itemViewGroupPeriodDo.supervisor_expandable_image.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0, R
                                .drawable.expand_list_indicator_up, 0
                )
            }else{
                itemViewGroupPeriodDo.supervisor_expandable_image.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0, R
                                .drawable.expand_list_indicator_down, 0
                )
            }
        }


        if (actionPeriodDo == context.getString(R.string.awus_text)) {

            val awusExpandableDOGoal = Validation().checkAmountPercentageValue(context,
                                                                               individualSupervisorListGroupPeriodDo?.period?.sales?.goal?.amount,
                                                                               individualSupervisorListGroupPeriodDo?.period?.sales?.goal?.percentage,
                                                                               individualSupervisorListGroupPeriodDo?.period?.sales?.goal?.value)

            val awusExpandableDOVariance = Validation().checkAmountPercentageValue(context,
                                                                                   individualSupervisorListGroupPeriodDo?.period?.sales?.variance?.amount,
                                                                                   individualSupervisorListGroupPeriodDo?.period?.sales?.variance?.percentage,
                                                                                   individualSupervisorListGroupPeriodDo?.period?.sales?.variance?.value)

            val awusExpandableDOActual = Validation().checkAmountPercentageValue(context,
                                                                                 individualSupervisorListGroupPeriodDo?.period?.sales?.actual?.amount,
                                                                                 individualSupervisorListGroupPeriodDo?.period?.sales?.actual?.percentage,
                                                                                 individualSupervisorListGroupPeriodDo?.period?.sales?.actual?.value)

            if(awusExpandableDOGoal.isEmpty() && awusExpandableDOVariance.isEmpty() && awusExpandableDOActual.isEmpty()){
                
                itemViewGroupPeriodDo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupPeriodDo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupPeriodDo.supervisor_actual_layout.visibility = View.GONE
                val paramsAWUSDOError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsAWUSDOError.weight = 2.0f
                itemViewGroupPeriodDo.supervisor_name.layoutParams = paramsAWUSDOError

                itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(awusExpandableDOGoal.isNotEmpty() && awusExpandableDOVariance.isNotEmpty() && awusExpandableDOActual.isNotEmpty()){

                itemViewGroupPeriodDo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupPeriodDo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupPeriodDo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupPeriodDo.supervisor_goal.text = awusExpandableDOGoal
                itemViewGroupPeriodDo.supervisor_variance.text = awusExpandableDOVariance
                itemViewGroupPeriodDo.supervisor_actual.text = awusExpandableDOActual

                itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            }else{
                
                itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE

                if(awusExpandableDOGoal.isNotEmpty()){

                    itemViewGroupPeriodDo.supervisor_goal.text = awusExpandableDOGoal
                    itemViewGroupPeriodDo.supervisor_goal_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodDo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupPeriodDo.supervisor_goal_error.visibility = View.VISIBLE
                }

                if(awusExpandableDOVariance.isNotEmpty()){

                    itemViewGroupPeriodDo.supervisor_variance.text = awusExpandableDOVariance
                    itemViewGroupPeriodDo.supervisor_variance_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodDo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupPeriodDo.supervisor_variance_error.visibility = View.VISIBLE
                }

                if(awusExpandableDOActual.isNotEmpty()){

                    itemViewGroupPeriodDo.supervisor_actual.text = awusExpandableDOActual
                    itemViewGroupPeriodDo.supervisor_actual_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodDo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupPeriodDo.supervisor_actual_error.visibility = View.VISIBLE
                }

            }


            if (individualSupervisorListGroupPeriodDo?.period?.sales?.status?.toString() != null && awusExpandableDOActual.isNotEmpty() ) {

                when {
                    individualSupervisorListGroupPeriodDo.period.sales.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupPeriodDo.period.sales.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    }
                    else -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }
        else if (actionPeriodDo == context.getString(R.string.ideal_vs_food_variance_text)) {

            val foodExpandableDOGoal = Validation().checkAmountPercentageValue(context,
                                                                               individualSupervisorListGroupPeriodDo?.period?.food?.goal?.amount,
                                                                               individualSupervisorListGroupPeriodDo?.period?.food?.goal?.percentage,
                                                                               individualSupervisorListGroupPeriodDo?.period?.food?.goal?.value)

            val foodExpandableDOVariance = Validation().checkAmountPercentageValue(context,
                                                                                   individualSupervisorListGroupPeriodDo?.period?.food?.variance?.amount,
                                                                                   individualSupervisorListGroupPeriodDo?.period?.food?.variance?.percentage,
                                                                                   individualSupervisorListGroupPeriodDo?.period?.food?.variance?.value)

            val foodExpandableDOActual = Validation().checkAmountPercentageValue(context,
                                                                                 individualSupervisorListGroupPeriodDo?.period?.food?.actual?.amount,
                                                                                 individualSupervisorListGroupPeriodDo?.period?.food?.actual?.percentage,
                                                                                 individualSupervisorListGroupPeriodDo?.period?.food?.actual?.value)

            if(foodExpandableDOGoal.isEmpty() && foodExpandableDOVariance.isEmpty() && foodExpandableDOActual.isEmpty()){
                itemViewGroupPeriodDo.supervisor_goal_layout.visibility = View.GONE

                itemViewGroupPeriodDo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupPeriodDo.supervisor_actual_layout.visibility = View.GONE

                val paramsFoodDOError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsFoodDOError.weight = 2.0f
                itemViewGroupPeriodDo.supervisor_name.layoutParams = paramsFoodDOError
                
                itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(foodExpandableDOGoal.isNotEmpty() && foodExpandableDOVariance.isNotEmpty() && foodExpandableDOActual.isNotEmpty()){

                val paramsDOFood: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsDOFood.weight = 2.0f
                itemViewGroupPeriodDo.supervisor_name.layoutParams = paramsDOFood
                itemViewGroupPeriodDo.supervisor_goal_layout.visibility = View.VISIBLE

                itemViewGroupPeriodDo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupPeriodDo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupPeriodDo.supervisor_goal.text = foodExpandableDOGoal
                itemViewGroupPeriodDo.supervisor_variance.text = foodExpandableDOVariance
                itemViewGroupPeriodDo.supervisor_actual.text = foodExpandableDOActual

                itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            }
            else{
                
                itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                if(foodExpandableDOGoal.isNotEmpty()){

                    itemViewGroupPeriodDo.supervisor_goal.text = foodExpandableDOGoal
                    itemViewGroupPeriodDo.supervisor_goal_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodDo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupPeriodDo.supervisor_goal_error.visibility = View.VISIBLE
                }

                if(foodExpandableDOVariance.isNotEmpty()){

                    itemViewGroupPeriodDo.supervisor_variance.text = foodExpandableDOVariance
                    itemViewGroupPeriodDo.supervisor_variance_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodDo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupPeriodDo.supervisor_variance_error.visibility = View.VISIBLE
                }

                if(foodExpandableDOActual.isNotEmpty()){

                    itemViewGroupPeriodDo.supervisor_actual.text = foodExpandableDOActual
                    itemViewGroupPeriodDo.supervisor_actual_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodDo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupPeriodDo.supervisor_actual_error.visibility = View.VISIBLE
                }

            }

            if (individualSupervisorListGroupPeriodDo?.period?.food?.status != null && foodExpandableDOActual.isNotEmpty()) {
                when {
                    individualSupervisorListGroupPeriodDo.period.food.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupPeriodDo.period.food.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }
        else if (actionPeriodDo == context.getString(R.string.labour_text)) {

            val laborExpandableDOGoal = Validation().checkAmountPercentageValue(context,
                                                                                individualSupervisorListGroupPeriodDo?.period?.labor?.goal?.amount,
                                                                                individualSupervisorListGroupPeriodDo?.period?.labor?.goal?.percentage,
                                                                                individualSupervisorListGroupPeriodDo?.period?.labor?.goal?.value)

            val laborExpandableDOVariance = Validation().checkAmountPercentageValue(context,
                                                                                    individualSupervisorListGroupPeriodDo?.period?.labor?.variance?.amount,
                                                                                    individualSupervisorListGroupPeriodDo?.period?.labor?.variance?.percentage,
                                                                                    individualSupervisorListGroupPeriodDo?.period?.labor?.variance?.value)

            val laborExpandableDOActual = Validation().checkAmountPercentageValue(context,
                                                                                  individualSupervisorListGroupPeriodDo?.period?.labor?.actual?.amount,
                                                                                  individualSupervisorListGroupPeriodDo?.period?.labor?.actual?.percentage,
                                                                                  individualSupervisorListGroupPeriodDo?.period?.labor?.actual?.value)

            if(laborExpandableDOGoal.isEmpty() && laborExpandableDOVariance.isEmpty() && laborExpandableDOActual.isEmpty()){
                itemViewGroupPeriodDo.supervisor_goal_layout.visibility = View.GONE

                itemViewGroupPeriodDo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupPeriodDo.supervisor_actual_layout.visibility = View.GONE
                
                val paramsLabourDOError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsLabourDOError.weight = 2.0f
                itemViewGroupPeriodDo.supervisor_name.layoutParams = paramsLabourDOError

                itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(laborExpandableDOGoal.isNotEmpty() && laborExpandableDOVariance.isNotEmpty() && laborExpandableDOActual.isNotEmpty()){

                val paramsDOLabour: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsDOLabour.weight = 2.0f
                itemViewGroupPeriodDo.supervisor_name.layoutParams = paramsDOLabour
                itemViewGroupPeriodDo.supervisor_goal_layout.visibility = View.VISIBLE

                itemViewGroupPeriodDo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupPeriodDo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupPeriodDo.supervisor_goal.text = laborExpandableDOGoal
                itemViewGroupPeriodDo.supervisor_variance.text = laborExpandableDOVariance
                itemViewGroupPeriodDo.supervisor_actual.text = laborExpandableDOActual

                itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            }
            else{
                
                itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                if (laborExpandableDOGoal.isNotEmpty()) {

                    itemViewGroupPeriodDo.supervisor_goal.text = laborExpandableDOGoal
                    itemViewGroupPeriodDo.supervisor_goal_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodDo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupPeriodDo.supervisor_goal_error.visibility = View.VISIBLE
                }

                if (laborExpandableDOVariance.isNotEmpty()) {

                    itemViewGroupPeriodDo.supervisor_variance.text = laborExpandableDOVariance
                    itemViewGroupPeriodDo.supervisor_variance_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodDo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupPeriodDo.supervisor_variance_error.visibility = View.VISIBLE
                }

                if (laborExpandableDOActual.isNotEmpty()) {

                    itemViewGroupPeriodDo.supervisor_actual.text = laborExpandableDOActual
                    itemViewGroupPeriodDo.supervisor_actual_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodDo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupPeriodDo.supervisor_actual_error.visibility = View.VISIBLE
                }

            }

            if (individualSupervisorListGroupPeriodDo?.period?.labor?.status?.toString() != null && laborExpandableDOActual.isNotEmpty()) {
                when {
                    individualSupervisorListGroupPeriodDo.period.labor.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupPeriodDo.period.labor.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }
        else if (actionPeriodDo == context.getString(R.string.service_text)) {

            val serviceExpandableDOGoal = Validation().checkAmountPercentageValue(context,
                                                                                  individualSupervisorListGroupPeriodDo?.period?.service?.goal?.amount,
                                                                                  individualSupervisorListGroupPeriodDo?.period?.service?.goal?.percentage,
                                                                                  individualSupervisorListGroupPeriodDo?.period?.service?.goal?.value)

            val serviceExpandableDOVariance = Validation().checkAmountPercentageValue(context,
                                                                                      individualSupervisorListGroupPeriodDo?.period?.service?.variance?.amount,
                                                                                      individualSupervisorListGroupPeriodDo?.period?.service?.variance?.percentage,
                                                                                      individualSupervisorListGroupPeriodDo?.period?.service?.variance?.value)

            val serviceExpandableDOActual = Validation().checkAmountPercentageValue(context,
                                                                                    individualSupervisorListGroupPeriodDo?.period?.service?.actual?.amount,
                                                                                    individualSupervisorListGroupPeriodDo?.period?.service?.actual?.percentage,
                                                                                    individualSupervisorListGroupPeriodDo?.period?.service?.actual?.value)

           if(individualSupervisorListGroupPeriodDo?.period == null){
               itemViewGroupPeriodDo.supervisor_goal_layout.visibility = View.GONE
               itemViewGroupPeriodDo.supervisor_variance_layout.visibility = View.GONE
               itemViewGroupPeriodDo.supervisor_actual_layout.visibility = View.GONE

               val paramsDOServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                       0, LinearLayout.LayoutParams.WRAP_CONTENT
               )
               paramsDOServiceError.weight = 2.0f
               itemViewGroupPeriodDo.supervisor_name.layoutParams = paramsDOServiceError

               itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
           }else{
               if(serviceExpandableDOGoal.isEmpty() && serviceExpandableDOVariance.isEmpty() && serviceExpandableDOActual.isEmpty()){
                   itemViewGroupPeriodDo.supervisor_goal_layout.visibility = View.GONE
                   itemViewGroupPeriodDo.supervisor_variance_layout.visibility = View.GONE
                   itemViewGroupPeriodDo.supervisor_actual_layout.visibility = View.GONE

                   val paramsDOServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                           0, LinearLayout.LayoutParams.WRAP_CONTENT
                   )
                   paramsDOServiceError.weight = 2.0f
                   itemViewGroupPeriodDo.supervisor_name.layoutParams = paramsDOServiceError

                   itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
               }
               else if(serviceExpandableDOGoal.isNotEmpty() && serviceExpandableDOVariance.isNotEmpty() && serviceExpandableDOActual.isNotEmpty()){

                   val paramsDOService: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                           0, LinearLayout.LayoutParams.WRAP_CONTENT
                   )
                   paramsDOService.weight = 2.0f
                   itemViewGroupPeriodDo.supervisor_name.layoutParams = paramsDOService
                   itemViewGroupPeriodDo.supervisor_goal_layout.visibility = View.VISIBLE
                   itemViewGroupPeriodDo.supervisor_variance_layout.visibility = View.VISIBLE
                   itemViewGroupPeriodDo.supervisor_actual_layout.visibility = View.VISIBLE

                   itemViewGroupPeriodDo.supervisor_goal.text = serviceExpandableDOGoal
                   itemViewGroupPeriodDo.supervisor_variance.text = serviceExpandableDOVariance
                   itemViewGroupPeriodDo.supervisor_actual.text = serviceExpandableDOActual

                   itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
               }
               else {

                   itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                   if (serviceExpandableDOGoal.isNotEmpty()) {

                       itemViewGroupPeriodDo.supervisor_goal.text = serviceExpandableDOGoal
                       itemViewGroupPeriodDo.supervisor_goal_error.visibility = View.GONE
                   }else{
                       itemViewGroupPeriodDo.supervisor_goal.visibility = View.INVISIBLE
                       itemViewGroupPeriodDo.supervisor_goal_error.visibility = View.VISIBLE
                   }
                   if (serviceExpandableDOVariance.isNotEmpty()) {

                       itemViewGroupPeriodDo.supervisor_variance.text = serviceExpandableDOVariance
                       itemViewGroupPeriodDo.supervisor_variance_error.visibility = View.GONE
                   }else{
                       itemViewGroupPeriodDo.supervisor_variance.visibility = View.INVISIBLE
                       itemViewGroupPeriodDo.supervisor_variance_error.visibility = View.VISIBLE
                   }
                   if (serviceExpandableDOActual.isNotEmpty()) {

                       itemViewGroupPeriodDo.supervisor_actual.text = serviceExpandableDOActual
                       itemViewGroupPeriodDo.supervisor_actual_error.visibility = View.GONE
                   }else{
                       itemViewGroupPeriodDo.supervisor_actual.visibility = View.INVISIBLE
                       itemViewGroupPeriodDo.supervisor_actual_error.visibility = View.VISIBLE
                   }
               }

               if (individualSupervisorListGroupPeriodDo.period.service?.status?.toString() != null && serviceExpandableDOActual.isNotEmpty()) {

                   when {
                       individualSupervisorListGroupPeriodDo.period.service.status.toString() == context.resources.getString(
                               R.string.out_of_range
                       ) -> {
                           itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                                   0,
                                   0,
                                   R.drawable.red_circle,
                                   0
                           )
                           itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                       }
                       individualSupervisorListGroupPeriodDo.period.service.status.toString() == context.resources.getString(
                               R.string.under_limit
                       ) -> {
                           itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                                   0,
                                   0,
                                   R.drawable.green_circle,
                                   0
                           )
                           itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                       } else -> {
                       itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                               0,
                               0,
                               R.drawable.black_circle,
                               0
                       )
                       itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                   }
                   }
               }

           }

        }
        else if (actionPeriodDo == context.getString(R.string.cash_text)) {

            val cashExpandableDOActual = Validation().checkAmountPercentageValue(context,
                                                                                 individualSupervisorListGroupPeriodDo?.period?.cash?.actual?.amount,
                                                                                 individualSupervisorListGroupPeriodDo?.period?.cash?.actual?.percentage,
                                                                                 individualSupervisorListGroupPeriodDo?.period?.cash?.actual?.value)

            if(cashExpandableDOActual.isEmpty()){
                
                itemViewGroupPeriodDo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupPeriodDo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupPeriodDo.supervisor_actual_layout.visibility = View.GONE
                itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }else{
                itemViewGroupPeriodDo.supervisor_goal_layout.visibility = View.INVISIBLE
                itemViewGroupPeriodDo.supervisor_variance_layout.visibility = View.INVISIBLE
                itemViewGroupPeriodDo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupPeriodDo.supervisor_actual.text = cashExpandableDOActual
            }

            if (individualSupervisorListGroupPeriodDo?.period?.cash?.status?.toString() != null && cashExpandableDOActual.isNotEmpty()) {
                when {
                    true -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupPeriodDo.period.cash.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }

        }
        else if (actionPeriodDo == context.getString(R.string.oer_text)) {

            val oerStartExpandableDOGoal = Validation().checkAmountPercentageValue(context,
                                                                                   individualSupervisorListGroupPeriodDo?.period?.oerStart?.goal?.amount,
                                                                                   individualSupervisorListGroupPeriodDo?.period?.oerStart?.goal?.percentage,
                                                                                   individualSupervisorListGroupPeriodDo?.period?.oerStart?.goal?.value)

            val oerStartExpandableDOVariance = Validation().checkAmountPercentageValue(context,
                                                                                       individualSupervisorListGroupPeriodDo?.period?.oerStart?.variance?.amount,
                                                                                       individualSupervisorListGroupPeriodDo?.period?.oerStart?.variance?.percentage,
                                                                                       individualSupervisorListGroupPeriodDo?.period?.oerStart?.variance?.value)

            val oerStartExpandableDoActual = Validation().checkAmountPercentageValue(context,
                                                                                     individualSupervisorListGroupPeriodDo?.period?.oerStart?.actual?.amount,
                                                                                     individualSupervisorListGroupPeriodDo?.period?.oerStart?.actual?.percentage,
                                                                                     individualSupervisorListGroupPeriodDo?.period?.oerStart?.actual?.value)

            if(oerStartExpandableDOGoal.isEmpty() && oerStartExpandableDOVariance.isEmpty() && oerStartExpandableDoActual.isEmpty()){
                itemViewGroupPeriodDo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupPeriodDo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupPeriodDo.supervisor_actual_layout.visibility = View.GONE
                
                val paramsOERError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsOERError.weight = 2.0f
                itemViewGroupPeriodDo.supervisor_name.layoutParams = paramsOERError

                itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(oerStartExpandableDOGoal.isNotEmpty() && oerStartExpandableDOVariance.isNotEmpty() && oerStartExpandableDoActual.isNotEmpty()){

                val paramsOer: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsOer.weight = 2.0f
                itemViewGroupPeriodDo.supervisor_name.layoutParams = paramsOer

                itemViewGroupPeriodDo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupPeriodDo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupPeriodDo.supervisor_actual_layout.visibility = View.VISIBLE
                itemViewGroupPeriodDo.supervisor_goal.text = oerStartExpandableDOGoal
                itemViewGroupPeriodDo.supervisor_variance.text = oerStartExpandableDOVariance
                itemViewGroupPeriodDo.supervisor_actual.text = oerStartExpandableDoActual

                itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            }
            else{
                
                itemViewGroupPeriodDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                if(oerStartExpandableDOGoal.isNotEmpty()){

                    itemViewGroupPeriodDo.supervisor_goal.text = oerStartExpandableDOGoal
                    itemViewGroupPeriodDo.supervisor_goal_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodDo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupPeriodDo.supervisor_goal_error.visibility = View.VISIBLE
                }

                if(oerStartExpandableDOVariance.isNotEmpty()){

                    itemViewGroupPeriodDo.supervisor_variance.text = oerStartExpandableDOVariance
                    itemViewGroupPeriodDo.supervisor_variance_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodDo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupPeriodDo.supervisor_variance_error.visibility = View.VISIBLE
                }

                if(oerStartExpandableDoActual.isNotEmpty()){

                    itemViewGroupPeriodDo.supervisor_actual.text = oerStartExpandableDoActual
                    itemViewGroupPeriodDo.supervisor_actual_error.visibility = View.GONE
                }else{
                    itemViewGroupPeriodDo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupPeriodDo.supervisor_actual_error.visibility = View.VISIBLE
                }

            }

            if (individualSupervisorListGroupPeriodDo?.period?.oerStart?.status?.toString() != null && oerStartExpandableDoActual.isNotEmpty()) {
                when {
                    individualSupervisorListGroupPeriodDo.period.oerStart.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupPeriodDo.period.oerStart.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupPeriodDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupPeriodDo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }

        return itemViewGroupPeriodDo
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }

}