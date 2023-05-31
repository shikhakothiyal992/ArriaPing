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
import com.arria.ping.kpi.ceo.CEOYesterdayLevelTwoQuery
import com.arria.ping.model.StoreDetailPojo
import com.arria.ping.util.Validation
import kotlinx.android.synthetic.main.expandable_list_store.view.*
import kotlinx.android.synthetic.main.expandable_list_supervisor.view.*


class CustomExpandableListAdapterYesterdayCEO internal constructor(
    private val context: Context,
    private val titleListYesterdayCeo: List<String>,
    private var dataListYesterdayCeo: HashMap<String, List<StoreDetailPojo>>,
    private var superVisorDetailsYesterdayCeo: CEOYesterdayLevelTwoQuery.Ceo,
    private var actionYesterdayCeo: String,

    ) : BaseExpandableListAdapter() {



    override fun getChild(listPosition: Int, expandedListPosition: Int): StoreDetailPojo {
        return this.dataListYesterdayCeo[this.titleListYesterdayCeo[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    infix fun setChild(childData: HashMap<String, List<StoreDetailPojo>>) {
        dataListYesterdayCeo = childData
        notifyDataSetChanged()
    }

    @SuppressLint("InflateParams")
    override fun getChildView(
        listPositionYesterdayCeo: Int,
        expandedListPositionYesterdayCeo: Int,
        isLastChildYesterdayCeo: Boolean,
        convertViewYesterdayCeo: View?,
        parentYesterdayCeo: ViewGroup,
    ): View {
        var itemViewYesterdayCeo = convertViewYesterdayCeo
        val expandedListTextYesterdayCeo = getChild(listPositionYesterdayCeo, expandedListPositionYesterdayCeo)


        if (convertViewYesterdayCeo == null) {
            val layoutInflaterYesterdayCeo =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemViewYesterdayCeo = layoutInflaterYesterdayCeo.inflate(R.layout.expandable_list_store, null)
        }
        itemViewYesterdayCeo!!.store_parent

        val individualStoreListYesterdayCeo = expandedListTextYesterdayCeo

        if (getChildId(listPositionYesterdayCeo, expandedListPositionYesterdayCeo) == 0L) {
            itemViewYesterdayCeo.store_name.text =
                if (superVisorDetailsYesterdayCeo.kpis!!.individualSupervisors[listPositionYesterdayCeo]!!.supervisorName != null) superVisorDetailsYesterdayCeo.kpis!!.individualSupervisors[listPositionYesterdayCeo]!!.supervisorName.plus(
                    "'s overview") else ""
            itemViewYesterdayCeo.store_name.paintFlags =
                itemViewYesterdayCeo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewYesterdayCeo.store_name.setTypeface(itemViewYesterdayCeo.store_name.typeface, Typeface.NORMAL)
            val paramsCEOYesterdayNameError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsCEOYesterdayNameError.weight = 2.0f
            itemViewYesterdayCeo.store_name.layoutParams = paramsCEOYesterdayNameError

            itemViewYesterdayCeo.store_goal_layout.visibility = View.GONE
            itemViewYesterdayCeo.store_variance_layout.visibility = View.GONE
            itemViewYesterdayCeo.store_actual_layout.visibility = View.GONE
            itemViewYesterdayCeo.store_error_ceo_period_range_kpi.visibility = View.GONE
        } else {
            itemViewYesterdayCeo.store_name.text = individualStoreListYesterdayCeo.storeNumber
            itemViewYesterdayCeo.store_name.paintFlags =
                itemViewYesterdayCeo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewYesterdayCeo.store_name.setTypeface(itemViewYesterdayCeo.store_name.typeface, Typeface.NORMAL)
            itemViewYesterdayCeo.store_goal.visibility = View.VISIBLE
            itemViewYesterdayCeo.store_variance.visibility = View.VISIBLE
            itemViewYesterdayCeo.store_actual.visibility = View.VISIBLE
        }
        if (getChildrenCount(listPositionYesterdayCeo) < 3){
            itemViewYesterdayCeo.store_parent.visibility = View.VISIBLE
            itemViewYesterdayCeo.store_name.text = individualStoreListYesterdayCeo.storeNumber
            itemViewYesterdayCeo.store_name.paintFlags =
                itemViewYesterdayCeo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewYesterdayCeo.store_name.setTypeface(itemViewYesterdayCeo.store_name.typeface, Typeface.NORMAL)
            itemViewYesterdayCeo.store_goal.visibility = View.VISIBLE
            itemViewYesterdayCeo.store_variance.visibility = View.VISIBLE
            itemViewYesterdayCeo.store_actual.visibility = View.VISIBLE
        }

        if(!individualStoreListYesterdayCeo.storeNumber.isNullOrBlank()){

            val ceoYesterdayStoreGoal: String = individualStoreListYesterdayCeo.storeGoal.toString()
            val ceoYesterdayStoreVariance: String = individualStoreListYesterdayCeo.storeVariance.toString()
            val ceoYesterdayStoreActual: String = individualStoreListYesterdayCeo.storeActual.toString()

            if(ceoYesterdayStoreGoal.isEmpty() && ceoYesterdayStoreVariance.isEmpty() && ceoYesterdayStoreActual.isEmpty()){


                val paramsCEOYesterdayStoreError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsCEOYesterdayStoreError.weight = 2.0f
                itemViewYesterdayCeo.store_name.layoutParams = paramsCEOYesterdayStoreError

                if(actionYesterdayCeo == context.getString(R.string.service_text)){
                    itemViewYesterdayCeo.store_error_ceo_period_range_kpi.visibility = View.GONE
                }else{
                    itemViewYesterdayCeo.store_error_ceo_period_range_kpi.visibility = View.VISIBLE
                }

                itemViewYesterdayCeo.store_goal.visibility = View.GONE
                itemViewYesterdayCeo.store_variance.visibility = View.GONE
                itemViewYesterdayCeo.store_actual.visibility = View.GONE

                itemViewYesterdayCeo.store_goal_layout.visibility = View.GONE
                itemViewYesterdayCeo.store_variance_layout.visibility = View.GONE
                itemViewYesterdayCeo.store_actual_layout.visibility = View.GONE

            }else if(ceoYesterdayStoreGoal.isNotEmpty() && ceoYesterdayStoreVariance.isNotEmpty() && ceoYesterdayStoreActual.isNotEmpty()){
                itemViewYesterdayCeo.store_error_ceo_period_range_kpi.visibility = View.GONE
                itemViewYesterdayCeo.store_goal.visibility = View.VISIBLE
                itemViewYesterdayCeo.store_variance.visibility = View.VISIBLE
                itemViewYesterdayCeo.store_actual.visibility = View.VISIBLE

                itemViewYesterdayCeo.store_goal_error.visibility = View.GONE
                itemViewYesterdayCeo.store_variance_error.visibility = View.GONE
                itemViewYesterdayCeo.store_actual_error.visibility = View.GONE
                itemViewYesterdayCeo.store_goal_layout.visibility = View.VISIBLE
                itemViewYesterdayCeo.store_variance_layout.visibility = View.VISIBLE
                itemViewYesterdayCeo.store_actual_layout.visibility = View.VISIBLE

                itemViewYesterdayCeo.store_goal.text = ceoYesterdayStoreGoal
                itemViewYesterdayCeo.store_variance.text = ceoYesterdayStoreVariance
                itemViewYesterdayCeo.store_actual.text = ceoYesterdayStoreActual
                val paramsCEOYesterdayStores: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsCEOYesterdayStores.weight = 0.94f
                itemViewYesterdayCeo.store_name.layoutParams = paramsCEOYesterdayStores

            }
            else{
                val paramsCEOYesterdayStore: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsCEOYesterdayStore.weight = 0.94f
                itemViewYesterdayCeo.store_name.layoutParams = paramsCEOYesterdayStore

                itemViewYesterdayCeo.store_error_ceo_period_range_kpi.visibility = View.GONE
                itemViewYesterdayCeo.store_goal_layout.visibility = View.VISIBLE
                itemViewYesterdayCeo.store_variance_layout.visibility = View.VISIBLE
                itemViewYesterdayCeo.store_actual_layout.visibility = View.VISIBLE


                if (ceoYesterdayStoreGoal.isNotEmpty()) {
                    itemViewYesterdayCeo.store_goal.text = ceoYesterdayStoreGoal
                    itemViewYesterdayCeo.store_goal.visibility = View.VISIBLE
                    itemViewYesterdayCeo.store_goal_error.visibility = View.GONE
                } else {
                    itemViewYesterdayCeo.store_goal.visibility = View.INVISIBLE
                    itemViewYesterdayCeo.store_goal_error.visibility = View.VISIBLE
                }
                if (ceoYesterdayStoreVariance.isNotEmpty()) {
                    itemViewYesterdayCeo.store_variance.text = ceoYesterdayStoreVariance
                    itemViewYesterdayCeo.store_variance.visibility = View.VISIBLE
                    itemViewYesterdayCeo.store_variance_error.visibility = View.GONE
                } else {
                    itemViewYesterdayCeo.store_variance.visibility = View.INVISIBLE
                    itemViewYesterdayCeo.store_variance_error.visibility = View.VISIBLE
                }
                if (ceoYesterdayStoreActual.isNotEmpty()) {
                    itemViewYesterdayCeo.store_actual.text = ceoYesterdayStoreActual
                    itemViewYesterdayCeo.store_actual.visibility = View.VISIBLE
                    itemViewYesterdayCeo.store_actual_error.visibility = View.GONE
                }else{
                    itemViewYesterdayCeo.store_actual.visibility = View.INVISIBLE
                    itemViewYesterdayCeo.store_actual_error.visibility = View.VISIBLE
                }
            }
            if (!individualStoreListYesterdayCeo.status.isNullOrEmpty() &&  !individualStoreListYesterdayCeo.storeActual.isNullOrEmpty()) {
                itemViewYesterdayCeo.store_actual.text = individualStoreListYesterdayCeo.storeActual.toString()
                when (individualStoreListYesterdayCeo.status) {
                    context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewYesterdayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewYesterdayCeo.store_actual.setTextColor(context.getColor(R.color.red))

                    }
                    context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewYesterdayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewYesterdayCeo.store_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewYesterdayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewYesterdayCeo.store_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }

        return itemViewYesterdayCeo
    }

    override fun getChildrenCount(listPositionYesterdayCeo: Int): Int {
        return this.dataListYesterdayCeo[this.titleListYesterdayCeo[listPositionYesterdayCeo]]!!.size
    }

    override fun getGroup(listPositionYesterdayCeo: Int): Any {
        return this.titleListYesterdayCeo[listPositionYesterdayCeo]
    }

    override fun getGroupCount(): Int {
        return this.titleListYesterdayCeo.size
    }

    override fun getGroupId(listPositionYesterdayCeo: Int): Long {
        return listPositionYesterdayCeo.toLong()
    }

    @SuppressLint("InflateParams")
    override fun getGroupView(
        listPositionGroupYesterdayCeo: Int,
        isExpandedGroupYesterdayCeo: Boolean,
        convertViewGroupYesterdayCeo: View?,
        parentGroupYesterdayCeo: ViewGroup,
    ): View {
        var itemViewGroupYesterdayCeo = convertViewGroupYesterdayCeo
        if (convertViewGroupYesterdayCeo == null) {
            val layoutInflaterGroupYesterdayCeo =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemViewGroupYesterdayCeo = layoutInflaterGroupYesterdayCeo.inflate(R.layout.expandable_list_supervisor, null)
        }
        val individualSupervisorListGroupYesterdayCeo = superVisorDetailsYesterdayCeo.kpis!!.individualSupervisors[listPositionGroupYesterdayCeo]
        itemViewGroupYesterdayCeo!!.supervisor_name.text =
            if (individualSupervisorListGroupYesterdayCeo?.supervisorName != null) individualSupervisorListGroupYesterdayCeo.supervisorName else ""

        if(individualSupervisorListGroupYesterdayCeo?.yesterday == null){
            itemViewGroupYesterdayCeo.supervisor_expandable_image.visibility = View.INVISIBLE
        }else{
            itemViewGroupYesterdayCeo.supervisor_expandable_image.visibility = View.VISIBLE
            if (isExpandedGroupYesterdayCeo) {
                itemViewGroupYesterdayCeo.supervisor_expandable_image.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0, R
                                .drawable.expand_list_indicator_up, 0
                )
            }else{
                itemViewGroupYesterdayCeo.supervisor_expandable_image.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0, R
                                .drawable.expand_list_indicator_down, 0
                )
            }
        }

        if (actionYesterdayCeo == context.getString(R.string.awus_text)) {

            val awusExpandableSupervisorGoal = Validation().checkAmountPercentageValue(context,
                                                                                       individualSupervisorListGroupYesterdayCeo?.yesterday?.sales?.goal?.amount,
                                                                                       individualSupervisorListGroupYesterdayCeo?.yesterday?.sales?.goal?.percentage,
                                                                                       individualSupervisorListGroupYesterdayCeo?.yesterday?.sales?.goal?.value)

            val awusExpandableSupervisorVariance = Validation().checkAmountPercentageValue(context,
                                                                                           individualSupervisorListGroupYesterdayCeo?.yesterday?.sales?.variance?.amount,
                                                                                           individualSupervisorListGroupYesterdayCeo?.yesterday?.sales?.variance?.percentage,
                                                                                           individualSupervisorListGroupYesterdayCeo?.yesterday?.sales?.variance?.value)

            val awusExpandableSupervisorActual = Validation().checkAmountPercentageValue(context,
                                                                                         individualSupervisorListGroupYesterdayCeo?.yesterday?.sales?.actual?.amount,
                                                                                         individualSupervisorListGroupYesterdayCeo?.yesterday?.sales?.actual?.percentage,
                                                                                         individualSupervisorListGroupYesterdayCeo?.yesterday?.sales?.actual?.value)

            if(awusExpandableSupervisorGoal.isEmpty() && awusExpandableSupervisorVariance.isEmpty() && awusExpandableSupervisorActual.isEmpty()){
                itemViewGroupYesterdayCeo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupYesterdayCeo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupYesterdayCeo.supervisor_actual_layout.visibility = View.GONE
                val paramsAWUSError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsAWUSError.weight = 2.0f
                itemViewGroupYesterdayCeo.supervisor_name.layoutParams = paramsAWUSError

                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }else if(awusExpandableSupervisorGoal.isNotEmpty() && awusExpandableSupervisorVariance.isNotEmpty() && awusExpandableSupervisorActual.isNotEmpty()){
                itemViewGroupYesterdayCeo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayCeo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayCeo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupYesterdayCeo.supervisor_goal.text = awusExpandableSupervisorGoal
                itemViewGroupYesterdayCeo.supervisor_variance.text = awusExpandableSupervisorVariance
                itemViewGroupYesterdayCeo.supervisor_actual.text = awusExpandableSupervisorActual
                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            } else {

                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                if (awusExpandableSupervisorGoal.isNotEmpty()) {
                    itemViewGroupYesterdayCeo.supervisor_goal.text = awusExpandableSupervisorGoal
                    itemViewGroupYesterdayCeo.supervisor_goal_error.visibility = View.GONE
                } else {
                    itemViewGroupYesterdayCeo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupYesterdayCeo.supervisor_goal_error.visibility = View.VISIBLE
                }
                if (awusExpandableSupervisorVariance.isNotEmpty()) {
                    itemViewGroupYesterdayCeo.supervisor_variance.text = awusExpandableSupervisorVariance
                    itemViewGroupYesterdayCeo.supervisor_variance_error.visibility = View.GONE
                } else {
                    itemViewGroupYesterdayCeo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupYesterdayCeo.supervisor_variance_error.visibility = View.VISIBLE
                }

                if (awusExpandableSupervisorActual.isNotEmpty()) {
                    itemViewGroupYesterdayCeo.supervisor_actual.text = awusExpandableSupervisorActual
                    itemViewGroupYesterdayCeo.supervisor_actual_error.visibility = View.GONE
                } else {
                    itemViewGroupYesterdayCeo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupYesterdayCeo.supervisor_actual_error.visibility = View.VISIBLE
                }
            }


            if (individualSupervisorListGroupYesterdayCeo?.yesterday?.sales?.status?.toString() != null && awusExpandableSupervisorActual.isNotEmpty()) {

                when {
                    individualSupervisorListGroupYesterdayCeo.yesterday.sales.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupYesterdayCeo.yesterday.sales.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))
                    }
                }
            }
        } else if (actionYesterdayCeo == context.getString(R.string.ideal_vs_food_variance_text)) {

            val foodExpandableSupervisorGoal = Validation().checkAmountPercentageValue(context,
                                                                                       individualSupervisorListGroupYesterdayCeo?.yesterday?.food?.goal?.amount,
                                                                                       individualSupervisorListGroupYesterdayCeo?.yesterday?.food?.goal?.percentage,
                                                                                       individualSupervisorListGroupYesterdayCeo?.yesterday?.food?.goal?.value)

            val foodExpandableSupervisorVariance = Validation().checkAmountPercentageValue(context,
                                                                                           individualSupervisorListGroupYesterdayCeo?.yesterday?.food?.variance?.amount,
                                                                                           individualSupervisorListGroupYesterdayCeo?.yesterday?.food?.variance?.percentage,
                                                                                           individualSupervisorListGroupYesterdayCeo?.yesterday?.food?.variance?.value)

            val foodExpandableSupervisorActual = Validation().checkAmountPercentageValue(context,
                                                                                         individualSupervisorListGroupYesterdayCeo?.yesterday?.food?.actual?.amount,
                                                                                         individualSupervisorListGroupYesterdayCeo?.yesterday?.food?.actual?.percentage,
                                                                                         individualSupervisorListGroupYesterdayCeo?.yesterday?.food?.actual?.value)

            if(foodExpandableSupervisorGoal.isEmpty() && foodExpandableSupervisorVariance.isEmpty() && foodExpandableSupervisorActual.isEmpty()){
                itemViewGroupYesterdayCeo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupYesterdayCeo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupYesterdayCeo.supervisor_actual_layout.visibility = View.GONE
                val paramsFoodError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsFoodError.weight = 2.0f
                itemViewGroupYesterdayCeo.supervisor_name.layoutParams = paramsFoodError

                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(foodExpandableSupervisorGoal.isNotEmpty() && foodExpandableSupervisorVariance.isNotEmpty() && foodExpandableSupervisorActual.isNotEmpty()){

                val paramsFood: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsFood.weight = 2.0f
                itemViewGroupYesterdayCeo.supervisor_name.layoutParams = paramsFood
                itemViewGroupYesterdayCeo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayCeo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayCeo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupYesterdayCeo.supervisor_goal.text = foodExpandableSupervisorGoal
                itemViewGroupYesterdayCeo.supervisor_variance.text = foodExpandableSupervisorVariance
                itemViewGroupYesterdayCeo.supervisor_actual.text = foodExpandableSupervisorActual

                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            }
            else{

                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                if(foodExpandableSupervisorGoal.isNotEmpty()){
                    itemViewGroupYesterdayCeo.supervisor_goal.text = foodExpandableSupervisorGoal
                    itemViewGroupYesterdayCeo.supervisor_goal_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayCeo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupYesterdayCeo.supervisor_goal_error.visibility = View.VISIBLE
                }

                if(foodExpandableSupervisorVariance.isNotEmpty()){
                    itemViewGroupYesterdayCeo.supervisor_variance.text = foodExpandableSupervisorVariance
                    itemViewGroupYesterdayCeo.supervisor_variance_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayCeo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupYesterdayCeo.supervisor_variance_error.visibility = View.VISIBLE
                }

                if(foodExpandableSupervisorActual.isNotEmpty()){
                    itemViewGroupYesterdayCeo.supervisor_actual.text = foodExpandableSupervisorActual
                    itemViewGroupYesterdayCeo.supervisor_actual_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayCeo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupYesterdayCeo.supervisor_actual_error.visibility = View.VISIBLE

                }

            }


            if (individualSupervisorListGroupYesterdayCeo?.yesterday?.food?.status != null && foodExpandableSupervisorActual.isNotEmpty()) {
                when {
                    individualSupervisorListGroupYesterdayCeo.yesterday.food.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupYesterdayCeo.yesterday.food.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        } else if (actionYesterdayCeo == context.getString(R.string.labour_text)) {

            val laborExpandableSupervisorGoal = Validation().checkAmountPercentageValue(context,
                                                                                        individualSupervisorListGroupYesterdayCeo?.yesterday?.labor?.goal?.amount,
                                                                                        individualSupervisorListGroupYesterdayCeo?.yesterday?.labor?.goal?.percentage,
                                                                                        individualSupervisorListGroupYesterdayCeo?.yesterday?.labor?.goal?.value)

            val laborExpandableSupervisorVariance = Validation().checkAmountPercentageValue(context,
                                                                                            individualSupervisorListGroupYesterdayCeo?.yesterday?.labor?.variance?.amount,
                                                                                            individualSupervisorListGroupYesterdayCeo?.yesterday?.labor?.variance?.percentage,
                                                                                            individualSupervisorListGroupYesterdayCeo?.yesterday?.labor?.variance?.value)

            val laborExpandableSupervisorActual = Validation().checkAmountPercentageValue(context,
                                                                                          individualSupervisorListGroupYesterdayCeo?.yesterday?.labor?.actual?.amount,
                                                                                          individualSupervisorListGroupYesterdayCeo?.yesterday?.labor?.actual?.percentage,
                                                                                          individualSupervisorListGroupYesterdayCeo?.yesterday?.labor?.actual?.value)

            if(laborExpandableSupervisorGoal.isEmpty() && laborExpandableSupervisorVariance.isEmpty() && laborExpandableSupervisorActual.isEmpty()){
                itemViewGroupYesterdayCeo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupYesterdayCeo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupYesterdayCeo.supervisor_actual_layout.visibility = View.GONE
                val paramsLabourError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsLabourError.weight = 2.0f
                itemViewGroupYesterdayCeo.supervisor_name.layoutParams = paramsLabourError

                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(laborExpandableSupervisorGoal.isNotEmpty() && laborExpandableSupervisorVariance.isNotEmpty() && laborExpandableSupervisorActual.isNotEmpty()){
                val paramsLabour: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsLabour.weight = 2.0f
                itemViewGroupYesterdayCeo.supervisor_name.layoutParams = paramsLabour
                itemViewGroupYesterdayCeo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayCeo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayCeo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupYesterdayCeo.supervisor_goal.text = laborExpandableSupervisorGoal
                itemViewGroupYesterdayCeo.supervisor_variance.text = laborExpandableSupervisorVariance
                itemViewGroupYesterdayCeo.supervisor_actual.text = laborExpandableSupervisorActual

                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            }
            else{

                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE

                if(laborExpandableSupervisorGoal.isNotEmpty()){
                    itemViewGroupYesterdayCeo.supervisor_goal.text = laborExpandableSupervisorGoal
                    itemViewGroupYesterdayCeo.supervisor_goal_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayCeo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupYesterdayCeo.supervisor_goal_error.visibility = View.GONE
                }
                if(laborExpandableSupervisorVariance.isNotEmpty()){
                    itemViewGroupYesterdayCeo.supervisor_variance.text = laborExpandableSupervisorVariance
                    itemViewGroupYesterdayCeo.supervisor_variance_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayCeo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupYesterdayCeo.supervisor_variance_error.visibility = View.GONE
                }
                if(laborExpandableSupervisorActual.isNotEmpty()){
                    itemViewGroupYesterdayCeo.supervisor_actual.text = laborExpandableSupervisorActual
                    itemViewGroupYesterdayCeo.supervisor_actual_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayCeo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupYesterdayCeo.supervisor_actual_error.visibility = View.GONE

                }
            }


            if (individualSupervisorListGroupYesterdayCeo?.yesterday?.labor?.status?.toString() != null && laborExpandableSupervisorActual.isNotEmpty()) {
                when {
                    individualSupervisorListGroupYesterdayCeo.yesterday.labor.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupYesterdayCeo.yesterday.labor.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }

        } else if (actionYesterdayCeo == context.getString(R.string.service_text)) {

            val serviceExpandableSupervisorGoal = Validation().checkAmountPercentageValue(context,
                                                                                          individualSupervisorListGroupYesterdayCeo?.yesterday?.service?.goal?.amount,
                                                                                          individualSupervisorListGroupYesterdayCeo?.yesterday?.service?.goal?.percentage,
                                                                                          individualSupervisorListGroupYesterdayCeo?.yesterday?.service?.goal?.value)

            val serviceExpandableSupervisorVariance = Validation().checkAmountPercentageValue(context,
                                                                                              individualSupervisorListGroupYesterdayCeo?.yesterday?.service?.variance?.amount,
                                                                                              individualSupervisorListGroupYesterdayCeo?.yesterday?.service?.variance?.percentage,
                                                                                              individualSupervisorListGroupYesterdayCeo?.yesterday?.service?.variance?.value)

            val serviceExpandableSupervisorActual = Validation().checkAmountPercentageValue(context,
                                                                                            individualSupervisorListGroupYesterdayCeo?.yesterday?.service?.actual?.amount,
                                                                                            individualSupervisorListGroupYesterdayCeo?.yesterday?.service?.actual?.percentage,
                                                                                            individualSupervisorListGroupYesterdayCeo?.yesterday?.service?.actual?.value)

            if(individualSupervisorListGroupYesterdayCeo?.yesterday == null){
                itemViewGroupYesterdayCeo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupYesterdayCeo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupYesterdayCeo.supervisor_actual_layout.visibility = View.GONE

                val paramsServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsServiceError.weight = 2.0f
                itemViewGroupYesterdayCeo.supervisor_name.layoutParams = paramsServiceError

                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }else{
                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                if(serviceExpandableSupervisorGoal.isEmpty() && serviceExpandableSupervisorVariance.isEmpty() && serviceExpandableSupervisorActual.isEmpty()){
                    itemViewGroupYesterdayCeo.supervisor_goal_layout.visibility = View.GONE
                    itemViewGroupYesterdayCeo.supervisor_variance_layout.visibility = View.GONE
                    itemViewGroupYesterdayCeo.supervisor_actual_layout.visibility = View.GONE
                    val paramsLabourError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsLabourError.weight = 2.0f
                    itemViewGroupYesterdayCeo.supervisor_name.layoutParams = paramsLabourError
                }
                else if(serviceExpandableSupervisorGoal.isNotEmpty() && serviceExpandableSupervisorVariance.isNotEmpty() &&
                            serviceExpandableSupervisorActual.isNotEmpty()){
                    val paramsService: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsService.weight = 2.0f
                    itemViewGroupYesterdayCeo.supervisor_name.layoutParams = paramsService
                    itemViewGroupYesterdayCeo.supervisor_goal_layout.visibility = View.VISIBLE
                    itemViewGroupYesterdayCeo.supervisor_variance_layout.visibility = View.VISIBLE
                    itemViewGroupYesterdayCeo.supervisor_actual_layout.visibility = View.VISIBLE

                    itemViewGroupYesterdayCeo.supervisor_goal.text = serviceExpandableSupervisorGoal
                    itemViewGroupYesterdayCeo.supervisor_variance.text = serviceExpandableSupervisorVariance
                    itemViewGroupYesterdayCeo.supervisor_actual.text = serviceExpandableSupervisorActual

                }
                else {
                    if (serviceExpandableSupervisorGoal.isNotEmpty()) {
                        itemViewGroupYesterdayCeo.supervisor_goal.text = serviceExpandableSupervisorGoal
                        itemViewGroupYesterdayCeo.supervisor_goal_error.visibility = View.GONE
                    } else {
                        itemViewGroupYesterdayCeo.supervisor_goal.visibility = View.INVISIBLE
                        itemViewGroupYesterdayCeo.supervisor_goal_error.visibility = View.VISIBLE
                    }
                    if (serviceExpandableSupervisorVariance.isNotEmpty()) {
                        itemViewGroupYesterdayCeo.supervisor_variance.text = serviceExpandableSupervisorVariance
                        itemViewGroupYesterdayCeo.supervisor_variance_error.visibility = View.GONE
                    } else {
                        itemViewGroupYesterdayCeo.supervisor_variance.visibility = View.INVISIBLE
                        itemViewGroupYesterdayCeo.supervisor_variance_error.visibility = View.VISIBLE
                    }
                    if (serviceExpandableSupervisorActual.isNotEmpty()) {
                        itemViewGroupYesterdayCeo.supervisor_actual.text = serviceExpandableSupervisorActual
                        itemViewGroupYesterdayCeo.supervisor_actual_error.visibility = View.GONE

                    } else {
                        itemViewGroupYesterdayCeo.supervisor_actual.visibility = View.INVISIBLE
                        itemViewGroupYesterdayCeo.supervisor_actual_error.visibility = View.VISIBLE
                    }
                }


                if (individualSupervisorListGroupYesterdayCeo?.yesterday?.service?.status?.toString() != null && serviceExpandableSupervisorActual.isNotEmpty()) {

                    when {
                        individualSupervisorListGroupYesterdayCeo.yesterday.service.status.toString() == context.resources.getString(
                                R.string.out_of_range
                        ) -> {
                            itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.red_circle,
                                    0
                            )
                            itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                        }
                        individualSupervisorListGroupYesterdayCeo.yesterday.service.status.toString() == context.resources.getString(
                                R.string.under_limit
                        ) -> {
                            itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.green_circle,
                                    0
                            )
                            itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                        } else -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.black_circle,
                                0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                    }
                }
            }
        } else if (actionYesterdayCeo == context.getString(R.string.cash_text)) {

            val cashExpandableSupervisorActual = Validation().checkAmountPercentageValue(context,
                                                                                         individualSupervisorListGroupYesterdayCeo?.yesterday?.cash?.actual?.amount,
                                                                                         individualSupervisorListGroupYesterdayCeo?.yesterday?.cash?.actual?.percentage,
                                                                                         individualSupervisorListGroupYesterdayCeo?.yesterday?.cash?.actual?.value)

            if(cashExpandableSupervisorActual.isNotEmpty()){
                itemViewGroupYesterdayCeo.supervisor_goal_layout.visibility = View.INVISIBLE
                itemViewGroupYesterdayCeo.supervisor_variance_layout.visibility = View.INVISIBLE
                itemViewGroupYesterdayCeo.supervisor_actual_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayCeo.supervisor_actual.text = cashExpandableSupervisorActual

            }else{
                itemViewGroupYesterdayCeo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupYesterdayCeo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupYesterdayCeo.supervisor_actual_layout.visibility = View.GONE

                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE

            }
            if (individualSupervisorListGroupYesterdayCeo?.yesterday?.cash?.status?.toString() != null && cashExpandableSupervisorActual.isNotEmpty()) {
                when {
                    true -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupYesterdayCeo.yesterday.cash.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }

            }


        } else if (actionYesterdayCeo == context.getString(R.string.oer_text)) {

            val oerStartExpandableSupervisorGoal = Validation().checkAmountPercentageValue(context,
                                                                                           individualSupervisorListGroupYesterdayCeo?.yesterday?.oerStart?.goal?.amount,
                                                                                           individualSupervisorListGroupYesterdayCeo?.yesterday?.oerStart?.goal?.percentage,
                                                                                           individualSupervisorListGroupYesterdayCeo?.yesterday?.oerStart?.goal?.value)

            val oerStartExpandableSupervisorVariance = Validation().checkAmountPercentageValue(context,
                                                                                               individualSupervisorListGroupYesterdayCeo?.yesterday?.oerStart?.variance?.amount,
                                                                                               individualSupervisorListGroupYesterdayCeo?.yesterday?.oerStart?.variance?.percentage,
                                                                                               individualSupervisorListGroupYesterdayCeo?.yesterday?.oerStart?.variance?.value)

            val oerStartExpandableSupervisorActual = Validation().checkAmountPercentageValue(context,
                                                                                             individualSupervisorListGroupYesterdayCeo?.yesterday?.oerStart?.actual?.amount,
                                                                                             individualSupervisorListGroupYesterdayCeo?.yesterday?.oerStart?.actual?.percentage,
                                                                                             individualSupervisorListGroupYesterdayCeo?.yesterday?.oerStart?.actual?.value)

            if(oerStartExpandableSupervisorGoal.isEmpty() && oerStartExpandableSupervisorVariance.isEmpty() && oerStartExpandableSupervisorActual.isEmpty()){
                itemViewGroupYesterdayCeo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupYesterdayCeo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupYesterdayCeo.supervisor_actual_layout.visibility = View.GONE

                val paramsOERError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsOERError.weight = 2.0f
                itemViewGroupYesterdayCeo.supervisor_name.layoutParams = paramsOERError

                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(oerStartExpandableSupervisorGoal.isNotEmpty() && oerStartExpandableSupervisorVariance.isNotEmpty() && oerStartExpandableSupervisorActual.isNotEmpty()){
                val paramsOer: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsOer.weight = 2.0f
                itemViewGroupYesterdayCeo.supervisor_name.layoutParams = paramsOer

                itemViewGroupYesterdayCeo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayCeo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayCeo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupYesterdayCeo.supervisor_goal.text = oerStartExpandableSupervisorGoal
                itemViewGroupYesterdayCeo.supervisor_variance.text = oerStartExpandableSupervisorVariance
                itemViewGroupYesterdayCeo.supervisor_actual.text = oerStartExpandableSupervisorActual

                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            } else {

                itemViewGroupYesterdayCeo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                if (oerStartExpandableSupervisorGoal.isNotEmpty()) {
                    itemViewGroupYesterdayCeo.supervisor_goal.text = oerStartExpandableSupervisorGoal
                    itemViewGroupYesterdayCeo.supervisor_goal_error.visibility = View.GONE
                } else {
                    itemViewGroupYesterdayCeo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupYesterdayCeo.supervisor_goal_error.visibility = View.VISIBLE
                }
                if (oerStartExpandableSupervisorVariance.isNotEmpty()) {
                    itemViewGroupYesterdayCeo.supervisor_variance.text = oerStartExpandableSupervisorVariance
                    itemViewGroupYesterdayCeo.supervisor_variance_error.visibility = View.GONE
                } else {
                    itemViewGroupYesterdayCeo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupYesterdayCeo.supervisor_variance_error.visibility = View.VISIBLE
                }
                if (oerStartExpandableSupervisorActual.isNotEmpty()) {
                    itemViewGroupYesterdayCeo.supervisor_actual.text = oerStartExpandableSupervisorActual
                    itemViewGroupYesterdayCeo.supervisor_actual_error.visibility = View.GONE
                } else {
                    itemViewGroupYesterdayCeo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupYesterdayCeo.supervisor_actual_error.visibility = View.VISIBLE
                }
            }


            if (individualSupervisorListGroupYesterdayCeo?.yesterday?.oerStart?.status?.toString() != null && oerStartExpandableSupervisorActual.isNotEmpty()) {
                when {
                    individualSupervisorListGroupYesterdayCeo.yesterday.oerStart.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupYesterdayCeo.yesterday.oerStart.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupYesterdayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupYesterdayCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }

        return itemViewGroupYesterdayCeo
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }

}