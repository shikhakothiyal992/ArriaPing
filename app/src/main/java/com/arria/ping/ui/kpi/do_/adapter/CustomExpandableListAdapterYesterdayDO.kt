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
import com.arria.ping.kpi._do.DOYesterdayLevelTwoQuery
import com.arria.ping.model.StoreDetailPojo

import com.arria.ping.util.Validation
import kotlinx.android.synthetic.main.expandable_list_store.view.*
import kotlinx.android.synthetic.main.expandable_list_supervisor.view.*

class CustomExpandableListAdapterYesterdayDO internal constructor(
        private val context: Context,
        private val titleListYesterdayDo: List<String>,
        private var dataListYesterdayDo: HashMap<String, List<StoreDetailPojo>>,
        private var superVisorDetailsYesterdayDo:  DOYesterdayLevelTwoQuery.Do_,
        private var actionYesterdayDo: String,

        ) : BaseExpandableListAdapter() {


    
    override fun getChild(listPosition: Int, expandedListPosition: Int): StoreDetailPojo {
        return this.dataListYesterdayDo[this.titleListYesterdayDo[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    infix fun setChild(childData: HashMap<String, List<StoreDetailPojo>>) {
        dataListYesterdayDo = childData
        notifyDataSetChanged()
    }

    @SuppressLint("InflateParams")
    override fun getChildView(
        listPositionYesterdayDo: Int,
        expandedListPositionYesterdayDo: Int,
        isLastChildYesterdayDo: Boolean,
        convertViewYesterdayDo: View?,
        parentYesterdayDo: ViewGroup,
    ): View {
        var itemViewYesterdayDo = convertViewYesterdayDo
        val expandedListText = getChild(listPositionYesterdayDo, expandedListPositionYesterdayDo)


        if (convertViewYesterdayDo == null) {
            val layoutInflaterYesterdayDo =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemViewYesterdayDo = layoutInflaterYesterdayDo.inflate(R.layout.expandable_list_store, null)
        }
        itemViewYesterdayDo!!.store_parent

        val individualStoreList = expandedListText
        if (getChildId(listPositionYesterdayDo, expandedListPositionYesterdayDo) == 0L
        ) {

            itemViewYesterdayDo.store_name.text =
                if (superVisorDetailsYesterdayDo.kpis!!.individualSupervisors[listPositionYesterdayDo]!!.supervisorName != null) superVisorDetailsYesterdayDo.kpis!!.individualSupervisors[listPositionYesterdayDo]!!.supervisorName.plus(
                    "'s overview") else ""
            itemViewYesterdayDo.store_name.paintFlags =
                itemViewYesterdayDo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewYesterdayDo.store_name.setTypeface(itemViewYesterdayDo.store_name.typeface, Typeface.NORMAL)

            val paramsDOYesterdayNameError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            paramsDOYesterdayNameError.weight = 2.0f
            itemViewYesterdayDo.store_name.layoutParams = paramsDOYesterdayNameError

            itemViewYesterdayDo.store_goal_layout.visibility = View.GONE
            itemViewYesterdayDo.store_variance_layout.visibility = View.GONE
            itemViewYesterdayDo.store_actual_layout.visibility = View.GONE
            itemViewYesterdayDo.store_error_ceo_period_range_kpi.visibility = View.GONE


        } else {
            itemViewYesterdayDo.store_name.text = individualStoreList.storeNumber
            itemViewYesterdayDo.store_name.paintFlags =
                itemViewYesterdayDo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewYesterdayDo.store_name.setTypeface(itemViewYesterdayDo.store_name.typeface, Typeface.NORMAL)
            itemViewYesterdayDo.store_goal.visibility = View.VISIBLE
            itemViewYesterdayDo.store_variance.visibility = View.VISIBLE
            itemViewYesterdayDo.store_actual.visibility = View.VISIBLE
        }
        if (getChildrenCount(listPositionYesterdayDo) < 3){
            itemViewYesterdayDo.store_parent.visibility = View.VISIBLE
            itemViewYesterdayDo.store_name.text = individualStoreList.storeNumber
            itemViewYesterdayDo.store_name.paintFlags =
                itemViewYesterdayDo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewYesterdayDo.store_name.setTypeface(itemViewYesterdayDo.store_name.typeface, Typeface.NORMAL)
            itemViewYesterdayDo.store_goal.visibility = View.VISIBLE
            itemViewYesterdayDo.store_variance.visibility = View.VISIBLE
            itemViewYesterdayDo.store_actual.visibility = View.VISIBLE
        }

        if(!individualStoreList.storeNumber.isNullOrEmpty()){

            val doYesterdayStoreGoal = individualStoreList.storeGoal.toString()
            val doYesterdayStoreVariance = individualStoreList.storeVariance.toString()
            val doYesterdayStoreActual = individualStoreList.storeActual.toString()


            if(doYesterdayStoreGoal.isEmpty() && doYesterdayStoreVariance.isEmpty() && doYesterdayStoreActual.isEmpty()){
                if(actionYesterdayDo == context.getString(R.string.service_text)){
                    itemViewYesterdayDo.store_error_ceo_period_range_kpi.visibility = View.GONE
                }else{
                    itemViewYesterdayDo.store_error_ceo_period_range_kpi.visibility = View.VISIBLE
                }

                itemViewYesterdayDo.store_goal.visibility = View.GONE
                itemViewYesterdayDo.store_variance.visibility = View.GONE
                itemViewYesterdayDo.store_actual.visibility = View.GONE
                val paramsDOYesterdayStoreError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsDOYesterdayStoreError.weight = 2.0f
                itemViewYesterdayDo.store_name.layoutParams = paramsDOYesterdayStoreError

                itemViewYesterdayDo.store_goal_layout.visibility = View.GONE
                itemViewYesterdayDo.store_variance_layout.visibility = View.GONE
                itemViewYesterdayDo.store_actual_layout.visibility = View.GONE

                

            }else if(doYesterdayStoreGoal.isNotEmpty() && doYesterdayStoreVariance.isNotEmpty() && doYesterdayStoreActual.isNotEmpty()){

                itemViewYesterdayDo.store_error_ceo_period_range_kpi.visibility = View.GONE
                itemViewYesterdayDo.store_goal.visibility = View.VISIBLE
                itemViewYesterdayDo.store_variance.visibility = View.VISIBLE
                itemViewYesterdayDo.store_actual.visibility = View.VISIBLE

                itemViewYesterdayDo.store_goal_layout.visibility = View.VISIBLE
                itemViewYesterdayDo.store_variance_layout.visibility = View.VISIBLE
                itemViewYesterdayDo.store_actual_layout.visibility = View.VISIBLE
                val paramsDoYesterdayStores: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsDoYesterdayStores.weight = 0.94f
                itemViewYesterdayDo.store_name.layoutParams = paramsDoYesterdayStores

                itemViewYesterdayDo.store_goal.text= doYesterdayStoreGoal
                itemViewYesterdayDo.store_variance.text= doYesterdayStoreVariance
                itemViewYesterdayDo.store_actual.text= doYesterdayStoreActual

            }else{
                
                val paramsDoYesterdayStore: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsDoYesterdayStore.weight = 0.94f
                itemViewYesterdayDo.store_name.layoutParams = paramsDoYesterdayStore

                itemViewYesterdayDo.store_error_ceo_period_range_kpi.visibility = View.GONE

                if (doYesterdayStoreGoal.isNotEmpty()) {
                    itemViewYesterdayDo.store_goal.text= doYesterdayStoreGoal
                    itemViewYesterdayDo.store_goal.visibility = View.VISIBLE
                    itemViewYesterdayDo.store_goal_error.visibility = View.GONE
                }else{
                    itemViewYesterdayDo.store_goal.visibility = View.INVISIBLE
                    itemViewYesterdayDo.store_goal_error.visibility = View.VISIBLE
                }

                if (doYesterdayStoreVariance.isNotEmpty()) {
                    itemViewYesterdayDo.store_variance.text = doYesterdayStoreVariance
                    itemViewYesterdayDo.store_variance.visibility = View.VISIBLE
                    itemViewYesterdayDo.store_variance_error.visibility = View.GONE

                }else{
                    itemViewYesterdayDo.store_variance.visibility = View.INVISIBLE
                    itemViewYesterdayDo.store_variance_error.visibility = View.VISIBLE
                }
                if (doYesterdayStoreActual.isNotEmpty()) {
                    itemViewYesterdayDo.store_actual.text = doYesterdayStoreActual
                    itemViewYesterdayDo.store_actual.visibility = View.VISIBLE
                    itemViewYesterdayDo.store_actual_error.visibility = View.GONE
                }else{
                    itemViewYesterdayDo.store_actual.visibility = View.INVISIBLE
                    itemViewYesterdayDo.store_actual_error.visibility = View.VISIBLE
                }
            }

        }

        if (!individualStoreList.status.isNullOrEmpty() && !individualStoreList.storeActual.isNullOrEmpty()) {
            when (individualStoreList.status) {
                context.resources.getString(
                    R.string.out_of_range
                ) -> {
                    itemViewYesterdayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.red_circle,
                        0
                    )
                    itemViewYesterdayDo.store_actual.setTextColor(context.getColor(R.color.red))

                }
                context.resources.getString(
                    R.string.under_limit
                ) -> {
                    itemViewYesterdayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.green_circle,
                        0
                    )
                    itemViewYesterdayDo.store_actual.setTextColor(context.getColor(R.color.green))

                } else -> {
                    itemViewYesterdayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.black_circle,
                        0
                    )
                    itemViewYesterdayDo.store_actual.setTextColor(context.getColor(R.color.text_color))

                }
            }
        }

        return itemViewYesterdayDo
    }
    override fun getChildrenCount(listPosition: Int): Int {
        return this.dataListYesterdayDo[this.titleListYesterdayDo[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return this.titleListYesterdayDo[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.titleListYesterdayDo.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    @SuppressLint("InflateParams")
    override fun getGroupView(
        listPositionGroupYesterdayDo: Int,
        isExpandedGroupYesterdayDo: Boolean,
        convertViewGroupYesterdayDo: View?,
        parentGroupYesterdayDo: ViewGroup,
    ): View {
        var itemViewGroupYesterdayDo = convertViewGroupYesterdayDo
        if (convertViewGroupYesterdayDo== null) {
            val layoutInflaterGroupYesterdayDo =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemViewGroupYesterdayDo = layoutInflaterGroupYesterdayDo.inflate(R.layout.expandable_list_supervisor, null)
        }
        val individualSupervisorListGroupYesterdayDo = superVisorDetailsYesterdayDo.kpis!!.individualSupervisors[listPositionGroupYesterdayDo]
        itemViewGroupYesterdayDo!!.supervisor_name.text =
            if (individualSupervisorListGroupYesterdayDo?.supervisorName != null) individualSupervisorListGroupYesterdayDo.supervisorName else ""

        if (actionYesterdayDo == context.getString(R.string.awus_text)) {


            val awusExpandableDOGoal = Validation().checkAmountPercentageValue(context,
                                                                               individualSupervisorListGroupYesterdayDo?.yesterday?.sales?.goal?.amount,
                                                                               individualSupervisorListGroupYesterdayDo?.yesterday?.sales?.goal?.percentage,
                                                                               individualSupervisorListGroupYesterdayDo?.yesterday?.sales?.goal?.value)

            val awusExpandableDOVariance = Validation().checkAmountPercentageValue(context,
                                                                                   individualSupervisorListGroupYesterdayDo?.yesterday?.sales?.variance?.amount,
                                                                                   individualSupervisorListGroupYesterdayDo?.yesterday?.sales?.variance?.percentage,
                                                                                   individualSupervisorListGroupYesterdayDo?.yesterday?.sales?.variance?.value)

            val awusExpandableDOActual = Validation().checkAmountPercentageValue(context,
                                                                                 individualSupervisorListGroupYesterdayDo?.yesterday?.sales?.actual?.amount,
                                                                                 individualSupervisorListGroupYesterdayDo?.yesterday?.sales?.actual?.percentage,
                                                                                 individualSupervisorListGroupYesterdayDo?.yesterday?.sales?.actual?.value)

            if(awusExpandableDOGoal.isEmpty() && awusExpandableDOVariance.isEmpty() && awusExpandableDOActual.isEmpty()){
                
                itemViewGroupYesterdayDo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupYesterdayDo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupYesterdayDo.supervisor_actual_layout.visibility = View.GONE
                val paramsAWUSDOError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsAWUSDOError.weight = 2.0f
                itemViewGroupYesterdayDo.supervisor_name.layoutParams = paramsAWUSDOError

                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(awusExpandableDOGoal.isNotEmpty() && awusExpandableDOVariance.isNotEmpty() && awusExpandableDOActual.isNotEmpty()){

                itemViewGroupYesterdayDo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayDo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayDo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupYesterdayDo.supervisor_goal.text = awusExpandableDOGoal
                itemViewGroupYesterdayDo.supervisor_variance.text = awusExpandableDOVariance
                itemViewGroupYesterdayDo.supervisor_actual.text = awusExpandableDOActual

                val paramsAWUSDo: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsAWUSDo.weight = 2.0f
                itemViewGroupYesterdayDo.supervisor_name.layoutParams = paramsAWUSDo
                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            }else{
                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                
                if(awusExpandableDOGoal.isNotEmpty()){

                    itemViewGroupYesterdayDo.supervisor_goal.text = awusExpandableDOGoal
                    itemViewGroupYesterdayDo.supervisor_goal_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayDo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupYesterdayDo.supervisor_goal_error.visibility = View.VISIBLE
                }

                if(awusExpandableDOVariance.isNotEmpty()){

                    itemViewGroupYesterdayDo.supervisor_variance.text = awusExpandableDOVariance
                    itemViewGroupYesterdayDo.supervisor_variance_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayDo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupYesterdayDo.supervisor_variance_error.visibility = View.VISIBLE
                }

                if(awusExpandableDOActual.isNotEmpty()){

                    itemViewGroupYesterdayDo.supervisor_actual.text = awusExpandableDOActual
                    itemViewGroupYesterdayDo.supervisor_actual_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayDo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupYesterdayDo.supervisor_actual_error.visibility = View.VISIBLE
                }

            }

            if (individualSupervisorListGroupYesterdayDo?.yesterday?.sales?.status?.toString() != null && awusExpandableDOActual.isNotEmpty() ) {

                when {
                    individualSupervisorListGroupYesterdayDo.yesterday.sales.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupYesterdayDo.yesterday.sales.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        } else if (actionYesterdayDo == context.getString(R.string.ideal_vs_food_variance_text)) {

            val foodExpandableDOGoal = Validation().checkAmountPercentageValue(context,
                                                                               individualSupervisorListGroupYesterdayDo?.yesterday?.food?.goal?.amount,
                                                                               individualSupervisorListGroupYesterdayDo?.yesterday?.food?.goal?.percentage,
                                                                               individualSupervisorListGroupYesterdayDo?.yesterday?.food?.goal?.value)

            val foodExpandableDOVariance = Validation().checkAmountPercentageValue(context,
                                                                                   individualSupervisorListGroupYesterdayDo?.yesterday?.food?.variance?.amount,
                                                                                   individualSupervisorListGroupYesterdayDo?.yesterday?.food?.variance?.percentage,
                                                                                   individualSupervisorListGroupYesterdayDo?.yesterday?.food?.variance?.value)

            val foodExpandableDOActual = Validation().checkAmountPercentageValue(context,
                                                                                 individualSupervisorListGroupYesterdayDo?.yesterday?.food?.actual?.amount,
                                                                                 individualSupervisorListGroupYesterdayDo?.yesterday?.food?.actual?.percentage,
                                                                                 individualSupervisorListGroupYesterdayDo?.yesterday?.food?.actual?.value)

            if(foodExpandableDOGoal.isEmpty() && foodExpandableDOVariance.isEmpty() && foodExpandableDOActual.isEmpty()){
                itemViewGroupYesterdayDo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupYesterdayDo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupYesterdayDo.supervisor_actual_layout.visibility = View.GONE

                val paramsFoodDOError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsFoodDOError.weight = 2.0f
                itemViewGroupYesterdayDo.supervisor_name.layoutParams = paramsFoodDOError
                
                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(foodExpandableDOGoal.isNotEmpty() && foodExpandableDOVariance.isNotEmpty() && foodExpandableDOActual.isNotEmpty()){

                val paramsDOFood: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsDOFood.weight = 2.0f
                itemViewGroupYesterdayDo.supervisor_name.layoutParams = paramsDOFood
                itemViewGroupYesterdayDo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayDo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayDo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupYesterdayDo.supervisor_goal.text = foodExpandableDOGoal
                itemViewGroupYesterdayDo.supervisor_variance.text = foodExpandableDOVariance
                itemViewGroupYesterdayDo.supervisor_actual.text = foodExpandableDOActual

                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            }
            else{
                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                
                if(foodExpandableDOGoal.isNotEmpty()){

                    itemViewGroupYesterdayDo.supervisor_goal.text = foodExpandableDOGoal
                    itemViewGroupYesterdayDo.supervisor_goal_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayDo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupYesterdayDo.supervisor_goal_error.visibility = View.VISIBLE
                }

                if(foodExpandableDOVariance.isNotEmpty()){

                    itemViewGroupYesterdayDo.supervisor_variance.text = foodExpandableDOVariance
                    itemViewGroupYesterdayDo.supervisor_variance_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayDo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupYesterdayDo.supervisor_variance_error.visibility = View.VISIBLE
                }

                if(foodExpandableDOActual.isNotEmpty()){

                    itemViewGroupYesterdayDo.supervisor_actual.text = foodExpandableDOActual

                    itemViewGroupYesterdayDo.supervisor_actual_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayDo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupYesterdayDo.supervisor_actual_error.visibility = View.VISIBLE
                }

            }


            if (individualSupervisorListGroupYesterdayDo?.yesterday?.food?.status != null && foodExpandableDOActual.isNotEmpty()) {
                when {
                    individualSupervisorListGroupYesterdayDo.yesterday.food.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupYesterdayDo.yesterday.food.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }

        } else if (actionYesterdayDo == context.getString(R.string.labour_text)) {
            val laborExpandableDOGoal = Validation().checkAmountPercentageValue(context,
                                                                                individualSupervisorListGroupYesterdayDo?.yesterday?.labor?.goal?.amount,
                                                                                individualSupervisorListGroupYesterdayDo?.yesterday?.labor?.goal?.percentage,
                                                                                individualSupervisorListGroupYesterdayDo?.yesterday?.labor?.goal?.value)

            val laborExpandableDOVariance = Validation().checkAmountPercentageValue(context,
                                                                                    individualSupervisorListGroupYesterdayDo?.yesterday?.labor?.variance?.amount,
                                                                                    individualSupervisorListGroupYesterdayDo?.yesterday?.labor?.variance?.percentage,
                                                                                    individualSupervisorListGroupYesterdayDo?.yesterday?.labor?.variance?.value)

            val laborExpandableDOActual = Validation().checkAmountPercentageValue(context,
                                                                                  individualSupervisorListGroupYesterdayDo?.yesterday?.labor?.actual?.amount,
                                                                                  individualSupervisorListGroupYesterdayDo?.yesterday?.labor?.actual?.percentage,
                                                                                  individualSupervisorListGroupYesterdayDo?.yesterday?.labor?.actual?.value)

            if(laborExpandableDOGoal.isEmpty() && laborExpandableDOVariance.isEmpty() && laborExpandableDOActual.isEmpty()){
                itemViewGroupYesterdayDo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupYesterdayDo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupYesterdayDo.supervisor_actual_layout.visibility = View.GONE
                
                val paramsLabourDOError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsLabourDOError.weight = 2.0f
                itemViewGroupYesterdayDo.supervisor_name.layoutParams = paramsLabourDOError

                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(laborExpandableDOGoal.isNotEmpty() && laborExpandableDOVariance.isNotEmpty() && laborExpandableDOActual.isNotEmpty()){

                val paramsDOLabour: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsDOLabour.weight = 2.0f
                itemViewGroupYesterdayDo.supervisor_name.layoutParams = paramsDOLabour
                itemViewGroupYesterdayDo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayDo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayDo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupYesterdayDo.supervisor_goal.text = laborExpandableDOGoal
                itemViewGroupYesterdayDo.supervisor_variance.text = laborExpandableDOVariance
                itemViewGroupYesterdayDo.supervisor_actual.text = laborExpandableDOActual

                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            }
            else{
                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                
                if (laborExpandableDOGoal.isNotEmpty()) {

                    itemViewGroupYesterdayDo.supervisor_goal.text = laborExpandableDOGoal
                    itemViewGroupYesterdayDo.supervisor_goal_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayDo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupYesterdayDo.supervisor_goal_error.visibility = View.VISIBLE
                }
                if (laborExpandableDOVariance.isNotEmpty()) {

                    itemViewGroupYesterdayDo.supervisor_variance.text = laborExpandableDOVariance
                    itemViewGroupYesterdayDo.supervisor_variance_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayDo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupYesterdayDo.supervisor_variance_error.visibility = View.VISIBLE
                }

                if (laborExpandableDOActual.isNotEmpty()) {

                    itemViewGroupYesterdayDo.supervisor_actual.text = laborExpandableDOActual
                    itemViewGroupYesterdayDo.supervisor_actual_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayDo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupYesterdayDo.supervisor_actual_error.visibility = View.VISIBLE
                }

            }

            if (individualSupervisorListGroupYesterdayDo?.yesterday?.labor?.status?.toString() != null && laborExpandableDOActual.isNotEmpty()) {
                when {
                    individualSupervisorListGroupYesterdayDo.yesterday.labor.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupYesterdayDo.yesterday.labor.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        } else if (actionYesterdayDo == context.getString(R.string.service_text)) {

            val serviceExpandableDOGoal = Validation().checkAmountPercentageValue(context,
                                                                                  individualSupervisorListGroupYesterdayDo?.yesterday?.service?.goal?.amount,
                                                                                  individualSupervisorListGroupYesterdayDo?.yesterday?.service?.goal?.percentage,
                                                                                  individualSupervisorListGroupYesterdayDo?.yesterday?.service?.goal?.value)

            val serviceExpandableDOVariance = Validation().checkAmountPercentageValue(context,
                                                                                      individualSupervisorListGroupYesterdayDo?.yesterday?.service?.variance?.amount,
                                                                                      individualSupervisorListGroupYesterdayDo?.yesterday?.service?.variance?.percentage,
                                                                                      individualSupervisorListGroupYesterdayDo?.yesterday?.service?.variance?.value)

            val serviceExpandableDOActual = Validation().checkAmountPercentageValue(context,
                                                                                    individualSupervisorListGroupYesterdayDo?.yesterday?.service?.actual?.amount,
                                                                                    individualSupervisorListGroupYesterdayDo?.yesterday?.service?.actual?.percentage,
                                                                                    individualSupervisorListGroupYesterdayDo?.yesterday?.service?.actual?.value)

            if(individualSupervisorListGroupYesterdayDo?.yesterday == null){
                itemViewGroupYesterdayDo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupYesterdayDo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupYesterdayDo.supervisor_actual_layout.visibility = View.GONE

                val paramsDOServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsDOServiceError.weight = 2.0f
                itemViewGroupYesterdayDo.supervisor_name.layoutParams = paramsDOServiceError

                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }else{
                if(serviceExpandableDOGoal.isEmpty() && serviceExpandableDOVariance.isEmpty() && serviceExpandableDOActual.isEmpty()){
                    itemViewGroupYesterdayDo.supervisor_goal_layout.visibility = View.GONE
                    itemViewGroupYesterdayDo.supervisor_variance_layout.visibility = View.GONE
                    itemViewGroupYesterdayDo.supervisor_actual_layout.visibility = View.GONE

                    val paramsDOServiceError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsDOServiceError.weight = 2.0f
                    itemViewGroupYesterdayDo.supervisor_name.layoutParams = paramsDOServiceError

                    itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                }
                else if(serviceExpandableDOGoal.isNotEmpty() && serviceExpandableDOVariance.isNotEmpty() && serviceExpandableDOActual.isNotEmpty()){

                    val paramsDOService: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    paramsDOService.weight = 2.0f
                    itemViewGroupYesterdayDo.supervisor_name.layoutParams = paramsDOService
                    itemViewGroupYesterdayDo.supervisor_goal_layout.visibility = View.VISIBLE
                    itemViewGroupYesterdayDo.supervisor_variance_layout.visibility = View.VISIBLE
                    itemViewGroupYesterdayDo.supervisor_actual_layout.visibility = View.VISIBLE

                    itemViewGroupYesterdayDo.supervisor_goal.text = serviceExpandableDOGoal
                    itemViewGroupYesterdayDo.supervisor_variance.text = serviceExpandableDOVariance
                    itemViewGroupYesterdayDo.supervisor_actual.text = serviceExpandableDOActual

                    itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                }
                else {
                    itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE

                    if (serviceExpandableDOGoal.isNotEmpty()) {

                        itemViewGroupYesterdayDo.supervisor_goal.text = serviceExpandableDOGoal
                        itemViewGroupYesterdayDo.supervisor_goal_error.visibility = View.GONE
                    }else{
                        itemViewGroupYesterdayDo.supervisor_goal.visibility = View.INVISIBLE
                        itemViewGroupYesterdayDo.supervisor_goal_error.visibility = View.VISIBLE
                    }
                    if (serviceExpandableDOVariance.isNotEmpty()) {

                        itemViewGroupYesterdayDo.supervisor_variance.text = serviceExpandableDOVariance
                        itemViewGroupYesterdayDo.supervisor_variance_error.visibility = View.GONE
                    }else{
                        itemViewGroupYesterdayDo.supervisor_variance.visibility = View.INVISIBLE
                        itemViewGroupYesterdayDo.supervisor_variance_error.visibility = View.VISIBLE
                    }
                    if (serviceExpandableDOActual.isNotEmpty()) {

                        itemViewGroupYesterdayDo.supervisor_actual.text = serviceExpandableDOActual
                        itemViewGroupYesterdayDo.supervisor_actual_error.visibility = View.GONE
                    }else{
                        itemViewGroupYesterdayDo.supervisor_actual.visibility = View.INVISIBLE
                        itemViewGroupYesterdayDo.supervisor_actual_error.visibility = View.VISIBLE
                    }
                }


                if (individualSupervisorListGroupYesterdayDo.yesterday.service?.status?.toString() != null && serviceExpandableDOActual.isNotEmpty()) {

                    when {
                        individualSupervisorListGroupYesterdayDo.yesterday.service.status.toString() == context.resources.getString(
                                R.string.out_of_range
                        ) -> {
                            itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.red_circle,
                                    0
                            )
                            itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                        }
                        individualSupervisorListGroupYesterdayDo.yesterday.service.status.toString() == context.resources.getString(
                                R.string.under_limit
                        ) -> {
                            itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.green_circle,
                                    0
                            )
                            itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                        } else -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.black_circle,
                                0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                    }
                }
            }




        } else if (actionYesterdayDo == context.getString(R.string.cash_text)) {

            val cashExpandableDOActual = Validation().checkAmountPercentageValue(context,
                                                                                 individualSupervisorListGroupYesterdayDo?.yesterday?.cash?.actual?.amount,
                                                                                 individualSupervisorListGroupYesterdayDo?.yesterday?.cash?.actual?.percentage,
                                                                                 individualSupervisorListGroupYesterdayDo?.yesterday?.cash?.actual?.value)

            if(cashExpandableDOActual.isEmpty()){
                
                itemViewGroupYesterdayDo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupYesterdayDo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupYesterdayDo.supervisor_actual_layout.visibility = View.GONE
                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }else{
                itemViewGroupYesterdayDo.supervisor_goal_layout.visibility = View.INVISIBLE
                itemViewGroupYesterdayDo.supervisor_variance_layout.visibility = View.INVISIBLE
                itemViewGroupYesterdayDo.supervisor_actual_layout.visibility = View.VISIBLE

                itemViewGroupYesterdayDo.supervisor_actual.text = cashExpandableDOActual
            }

            if (individualSupervisorListGroupYesterdayDo?.yesterday?.oerStart?.status?.toString() != null && cashExpandableDOActual.isNotEmpty()) {
                when {
                    individualSupervisorListGroupYesterdayDo.yesterday.cash?.status?.toString() != null -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupYesterdayDo.yesterday.cash?.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }

        } else if (actionYesterdayDo == context.getString(R.string.oer_text)) {

            val oerStartExpandableDOGoal = Validation().checkAmountPercentageValue(context,
                                                                                   individualSupervisorListGroupYesterdayDo?.yesterday?.oerStart?.goal?.amount,
                                                                                   individualSupervisorListGroupYesterdayDo?.yesterday?.oerStart?.goal?.percentage,
                                                                                   individualSupervisorListGroupYesterdayDo?.yesterday?.oerStart?.goal?.value)

            val oerStartExpandableDOVariance = Validation().checkAmountPercentageValue(context,
                                                                                       individualSupervisorListGroupYesterdayDo?.yesterday?.oerStart?.variance?.amount,
                                                                                       individualSupervisorListGroupYesterdayDo?.yesterday?.oerStart?.variance?.percentage,
                                                                                       individualSupervisorListGroupYesterdayDo?.yesterday?.oerStart?.variance?.value)

            val oerStartExpandableDoActual = Validation().checkAmountPercentageValue(context,
                                                                                     individualSupervisorListGroupYesterdayDo?.yesterday?.oerStart?.actual?.amount,
                                                                                     individualSupervisorListGroupYesterdayDo?.yesterday?.oerStart?.actual?.percentage,
                                                                                     individualSupervisorListGroupYesterdayDo?.yesterday?.oerStart?.actual?.value)

            if(oerStartExpandableDOGoal.isEmpty() && oerStartExpandableDOVariance.isEmpty() && oerStartExpandableDoActual.isEmpty()){
                itemViewGroupYesterdayDo.supervisor_goal_layout.visibility = View.GONE
                itemViewGroupYesterdayDo.supervisor_variance_layout.visibility = View.GONE
                itemViewGroupYesterdayDo.supervisor_actual_layout.visibility = View.GONE
                
                val paramsOERError: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsOERError.weight = 2.0f
                itemViewGroupYesterdayDo.supervisor_name.layoutParams = paramsOERError

                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.VISIBLE
            }
            else if(oerStartExpandableDOGoal.isNotEmpty() && oerStartExpandableDOVariance.isNotEmpty() && oerStartExpandableDoActual.isNotEmpty()){

                val paramsOer: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsOer.weight = 2.0f
                itemViewGroupYesterdayDo.supervisor_name.layoutParams = paramsOer

                itemViewGroupYesterdayDo.supervisor_goal_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayDo.supervisor_variance_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayDo.supervisor_actual_layout.visibility = View.VISIBLE
                itemViewGroupYesterdayDo.supervisor_goal.text = oerStartExpandableDOGoal
                itemViewGroupYesterdayDo.supervisor_variance.text = oerStartExpandableDOVariance
                itemViewGroupYesterdayDo.supervisor_actual.text = oerStartExpandableDoActual

                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
            }
            else{
                
                itemViewGroupYesterdayDo.supervisor_error_ceo_period_range_kpi.visibility = View.GONE
                if(oerStartExpandableDOGoal.isNotEmpty()){

                    itemViewGroupYesterdayDo.supervisor_goal.text = oerStartExpandableDOGoal
                    itemViewGroupYesterdayDo.supervisor_goal_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayDo.supervisor_goal.visibility = View.INVISIBLE
                    itemViewGroupYesterdayDo.supervisor_goal_error.visibility = View.VISIBLE
                }

                if(oerStartExpandableDOVariance.isNotEmpty()){

                    itemViewGroupYesterdayDo.supervisor_variance.text = oerStartExpandableDOVariance
                    itemViewGroupYesterdayDo.supervisor_variance_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayDo.supervisor_variance.visibility = View.INVISIBLE
                    itemViewGroupYesterdayDo.supervisor_variance_error.visibility = View.VISIBLE
                }

                if(oerStartExpandableDoActual.isNotEmpty()){

                    itemViewGroupYesterdayDo.supervisor_actual.text = oerStartExpandableDoActual
                    itemViewGroupYesterdayDo.supervisor_actual_error.visibility = View.GONE
                }else{
                    itemViewGroupYesterdayDo.supervisor_actual.visibility = View.INVISIBLE
                    itemViewGroupYesterdayDo.supervisor_actual_error.visibility = View.VISIBLE
                }

            }

            if (individualSupervisorListGroupYesterdayDo?.yesterday?.oerStart?.status?.toString() != null && oerStartExpandableDoActual.isNotEmpty()) {
                when {
                    individualSupervisorListGroupYesterdayDo.yesterday.oerStart.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupYesterdayDo.yesterday.oerStart.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupYesterdayDo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupYesterdayDo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }

        return itemViewGroupYesterdayDo
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }


}