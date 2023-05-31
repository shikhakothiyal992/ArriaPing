package com.arria.ping.ui.kpi.ceo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import com.arria.ping.R
import com.arria.ping.kpi.CEODefaultTodayQuery
import com.arria.ping.model.StoreDetailPojo
import com.arria.ping.util.Validation
import kotlinx.android.synthetic.main.expandable_list_store.view.*
import kotlinx.android.synthetic.main.expandable_list_supervisor.view.*

class CustomExpandableListAdapterTodayCEO internal constructor(
    private val context: Context,
    private val titleListTodayCeo: List<String>,
    private var dataListTodayCeo: HashMap<String, List<StoreDetailPojo>>,
    private var superVisorDetailsTodayCeo: CEODefaultTodayQuery.Ceo,
    private var actionTodayCeo: String,
) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): StoreDetailPojo {
        return this.dataListTodayCeo[this.titleListTodayCeo[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    infix fun setChild(childData: HashMap<String, List<StoreDetailPojo>>) {
        dataListTodayCeo = childData
        notifyDataSetChanged()
    }

    @SuppressLint("InflateParams")
    override fun getChildView(
        listPositionTodayCeo: Int,
        expandedListPositionTodayCeo: Int,
        isLastChildTodayCeo: Boolean,
        convertViewTodayCeo: View?,
        parentTodayCeo: ViewGroup,
    ): View {
        var itemViewTodayCeo = convertViewTodayCeo
        val expandedListTextTodayCeo = getChild(listPositionTodayCeo, expandedListPositionTodayCeo)


        if (convertViewTodayCeo == null) {
            val layoutInflaterTodayCeo =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemViewTodayCeo = layoutInflaterTodayCeo.inflate(R.layout.expandable_list_store, null)
        }
        itemViewTodayCeo!!.store_parent

        val individualSupervisorListTodayCeo = expandedListTextTodayCeo
        if (getChildId(listPositionTodayCeo,
                expandedListPositionTodayCeo) == 0L && getChildrenCount(listPositionTodayCeo) > 3
        ) {
            itemViewTodayCeo.store_name.text =
                if (superVisorDetailsTodayCeo.kpis!!.individualSupervisors[listPositionTodayCeo]!!.supervisorName != null) superVisorDetailsTodayCeo.kpis!!.individualSupervisors[listPositionTodayCeo]!!.supervisorName.plus(
                    "'s overview") else ""
            itemViewTodayCeo.store_name.paintFlags =
                itemViewTodayCeo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewTodayCeo.store_goal.visibility = View.GONE
            itemViewTodayCeo.store_variance.visibility = View.GONE
            itemViewTodayCeo.store_actual.visibility = View.GONE

        } else {
            itemViewTodayCeo.store_name.text = individualSupervisorListTodayCeo.storeNumber
            itemViewTodayCeo.store_name.paintFlags =
                itemViewTodayCeo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewTodayCeo.store_goal.visibility = View.VISIBLE
            itemViewTodayCeo.store_variance.visibility = View.VISIBLE
            itemViewTodayCeo.store_actual.visibility = View.VISIBLE
        }
        if (actionTodayCeo == context.getString(R.string.awus_text)) {
            if (individualSupervisorListTodayCeo.storeGoal != "" && individualSupervisorListTodayCeo.storeGoal != "null") {
                itemViewTodayCeo.store_goal.text = context.getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        individualSupervisorListTodayCeo.storeGoal?.toDouble()))
            }
            if (individualSupervisorListTodayCeo.storeVariance != "" && individualSupervisorListTodayCeo.storeVariance != "null") {
                itemViewTodayCeo.store_variance.text = context.getString(R.string.dollar_text)
                    .plus(Validation().dollarFormatting(individualSupervisorListTodayCeo.storeVariance?.toDouble()))
            }

            if ((individualSupervisorListTodayCeo.storeVariance != "" && individualSupervisorListTodayCeo.storeVariance != "null") && (individualSupervisorListTodayCeo.status != "" && individualSupervisorListTodayCeo.status != "null")) {
                itemViewTodayCeo.store_actual.text = context.getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(individualSupervisorListTodayCeo.storeActual?.toDouble())
                )
                when (individualSupervisorListTodayCeo.status) {
                    context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.red))

                    }
                    context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }else  if (actionTodayCeo == context.getString(R.string.labour_text)) {
            if (individualSupervisorListTodayCeo.storeGoal != "" && individualSupervisorListTodayCeo.storeGoal != "null") {
                itemViewTodayCeo.store_goal.text =
                    Validation().ignoreZeroAfterDecimal(
                        individualSupervisorListTodayCeo.storeGoal?.toDouble()).plus(context.getString(R.string.percentage_text))
            }
            if (individualSupervisorListTodayCeo.storeVariance != "" && individualSupervisorListTodayCeo.storeVariance != "null") {
                itemViewTodayCeo.store_variance.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayCeo.storeVariance?.toDouble()).plus(context.getString(R.string.percentage_text))
            }

            if ((individualSupervisorListTodayCeo.storeVariance != "" && individualSupervisorListTodayCeo.storeVariance != "null") && (individualSupervisorListTodayCeo.status != "" && individualSupervisorListTodayCeo.status != "null")) {
                itemViewTodayCeo.store_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayCeo.storeActual?.toDouble()).plus(context.getString(R.string.percentage_text)
                    )
                when (individualSupervisorListTodayCeo.status) {
                    context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.red))

                    }
                    context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }else  if (actionTodayCeo == context.getString(R.string.service_text)) {
            if (individualSupervisorListTodayCeo.storeGoal != "" && individualSupervisorListTodayCeo.storeGoal != "null") {
                itemViewTodayCeo.store_goal.text =
                    Validation().ignoreZeroAfterDecimal(
                        individualSupervisorListTodayCeo.storeGoal?.toDouble()).plus(context.getString(R.string.percentage_text))
            }
            if (individualSupervisorListTodayCeo.storeVariance != "" && individualSupervisorListTodayCeo.storeVariance != "null") {
                itemViewTodayCeo.store_variance.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayCeo.storeVariance?.toDouble()).plus(context.getString(R.string.percentage_text))
            }

            if ((individualSupervisorListTodayCeo.storeVariance != "" && individualSupervisorListTodayCeo.storeVariance != "null") && (individualSupervisorListTodayCeo.status != "" && individualSupervisorListTodayCeo.status != "null")) {
                itemViewTodayCeo.store_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayCeo.storeActual?.toDouble()).plus(context.getString(R.string.percentage_text)
                    )
                when (individualSupervisorListTodayCeo.status) {
                    context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.red))

                    }
                    context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }else  if (actionTodayCeo == context.getString(R.string.cash_text)) {
            if (individualSupervisorListTodayCeo.storeGoal != "" && individualSupervisorListTodayCeo.storeGoal != "null") {
                itemViewTodayCeo.store_goal.text =
                    Validation().ignoreZeroAfterDecimal(
                        individualSupervisorListTodayCeo.storeGoal?.toDouble())
            }
            if (individualSupervisorListTodayCeo.storeVariance != "" && individualSupervisorListTodayCeo.storeVariance != "null") {
                itemViewTodayCeo.store_variance.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayCeo.storeVariance?.toDouble())
            }

            if ((individualSupervisorListTodayCeo.storeVariance != "" && individualSupervisorListTodayCeo.storeVariance != "null") && (individualSupervisorListTodayCeo.status != "" && individualSupervisorListTodayCeo.status != "null")) {
                itemViewTodayCeo.store_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayCeo.storeActual?.toDouble())
                when (individualSupervisorListTodayCeo.status) {
                    context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.red))

                    }
                    context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }else  if (actionTodayCeo == context.getString(R.string.oer_text)) {
            if (individualSupervisorListTodayCeo.storeGoal != "" && individualSupervisorListTodayCeo.storeGoal != "null") {
                itemViewTodayCeo.store_goal.text =
                    Validation().ignoreZeroAfterDecimal(
                        individualSupervisorListTodayCeo.storeGoal?.toDouble())
            }
            if (individualSupervisorListTodayCeo.storeVariance != "" && individualSupervisorListTodayCeo.storeVariance != "null") {
                itemViewTodayCeo.store_variance.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayCeo.storeVariance?.toDouble())
            }

            if ((individualSupervisorListTodayCeo.storeVariance != "" && individualSupervisorListTodayCeo.storeVariance != "null") && (individualSupervisorListTodayCeo.status != "" && individualSupervisorListTodayCeo.status != "null")) {
                itemViewTodayCeo.store_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayCeo.storeActual?.toDouble())
                when (individualSupervisorListTodayCeo.status) {
                    context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.red))

                    }
                    context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayCeo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayCeo.store_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }



        return itemViewTodayCeo
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.dataListTodayCeo[this.titleListTodayCeo[listPosition]]!!.size
    }

    override fun getGroup(listPositionTodayCeo: Int): Any {
        return this.titleListTodayCeo[listPositionTodayCeo]
    }

    override fun getGroupCount(): Int {
        return this.titleListTodayCeo.size
    }

    override fun getGroupId(listPositionTodayCeo: Int): Long {
        return listPositionTodayCeo.toLong()
    }

    @SuppressLint("InflateParams")
    override fun getGroupView(
        listPositionGroupTodayCeo: Int,
        isExpandedGroupTodayCeo: Boolean,
        convertViewGroupTodayCeo: View?,
        parentGroupTodayCeo: ViewGroup,
    ): View {
        var itemViewGroupTodayCeo = convertViewGroupTodayCeo
        if (convertViewGroupTodayCeo == null) {
            val layoutInflaterGroupTodayCeo =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemViewGroupTodayCeo = layoutInflaterGroupTodayCeo.inflate(R.layout.expandable_list_supervisor, null)
        }
        val individualSupervisorListGroupTodayCeo = superVisorDetailsTodayCeo.kpis!!.individualSupervisors[listPositionGroupTodayCeo]
        itemViewGroupTodayCeo!!.supervisor_name.text =
            if (individualSupervisorListGroupTodayCeo?.supervisorName != null) individualSupervisorListGroupTodayCeo.supervisorName else ""


        if (actionTodayCeo == context.getString(R.string.awus_text)) {

            itemViewGroupTodayCeo.supervisor_goal.text =
                if (individualSupervisorListGroupTodayCeo?.today?.sales?.goal?.value?.isNaN() == false) context.getString(
                    R.string.dollar_text
                )
                    .plus(Validation().dollarFormatting(individualSupervisorListGroupTodayCeo.today.sales.goal.value)) else ""
            itemViewGroupTodayCeo.supervisor_variance.text =
                if (individualSupervisorListGroupTodayCeo?.today?.sales?.variance?.value?.isNaN() == false) context.getString(
                    R.string.dollar_text
                )
                    .plus(Validation().dollarFormatting(individualSupervisorListGroupTodayCeo.today.sales.variance.value)) else ""
            if (individualSupervisorListGroupTodayCeo?.today?.sales?.actual?.value?.isNaN() == false && individualSupervisorListGroupTodayCeo.today.sales.status?.toString() != null) {
                itemViewGroupTodayCeo.supervisor_actual.text = context.getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(individualSupervisorListGroupTodayCeo.today.sales.actual.value)
                )
                when {
                    individualSupervisorListGroupTodayCeo.today.sales.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupTodayCeo.today.sales.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    }
                    else -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }  else if (actionTodayCeo == context.getString(R.string.labour_vs_goal_text)) {
            itemViewGroupTodayCeo.supervisor_goal.text =
                if (individualSupervisorListGroupTodayCeo?.today?.labor?.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(individualSupervisorListGroupTodayCeo.today.labor.goal.percentage).plus(
                    context.getString(
                        R.string.percentage_text
                    )
                ) else ""
            itemViewGroupTodayCeo.supervisor_variance.text =
                if (individualSupervisorListGroupTodayCeo?.today?.labor?.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(individualSupervisorListGroupTodayCeo.today.labor.variance.percentage) .plus(
                    context.getString(
                        R.string.percentage_text
                    )
                )else ""
            if (individualSupervisorListGroupTodayCeo?.today?.labor?.actual?.percentage?.isNaN() == false && individualSupervisorListGroupTodayCeo.today.labor.status?.toString() != null) {
                itemViewGroupTodayCeo.supervisor_actual.text =
                    individualSupervisorListGroupTodayCeo.today.labor.actual.percentage.toString().plus(
                        context.getString(
                            R.string.percentage_text
                        )
                    )
                when {
                    individualSupervisorListGroupTodayCeo.today.labor.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupTodayCeo.today.labor.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        } else if (actionTodayCeo == context.getString(R.string.service_text)) {
            itemViewGroupTodayCeo.supervisor_goal.text =
                if (individualSupervisorListGroupTodayCeo?.today?.service?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(individualSupervisorListGroupTodayCeo.today.service.goal.value) .plus(
                    context.getString(
                        R.string.percentage_text
                    )
                )else ""
            itemViewGroupTodayCeo.supervisor_variance.text =
                if (individualSupervisorListGroupTodayCeo?.today?.service?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(individualSupervisorListGroupTodayCeo.today.service.variance.value).plus(
                    context.getString(
                        R.string.percentage_text
                    )
                ) else ""
            if (individualSupervisorListGroupTodayCeo?.today?.service?.actual?.value?.isNaN() == false && individualSupervisorListGroupTodayCeo.today.service.status?.toString() != null) {
                itemViewGroupTodayCeo.supervisor_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListGroupTodayCeo.today.service.actual.value).plus(
                        context.getString(
                            R.string.percentage_text
                        )
                    )
                when {
                    individualSupervisorListGroupTodayCeo.today.service.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupTodayCeo.today.service.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }
        else if (actionTodayCeo == context.getString(R.string.cash_text)) {
            if (individualSupervisorListGroupTodayCeo?.today?.cash?.actual?.value?.isNaN() == false && individualSupervisorListGroupTodayCeo.today.cash.status?.toString() != null) {
                itemViewGroupTodayCeo.supervisor_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListGroupTodayCeo.today.cash.actual.value)
                when {
                    individualSupervisorListGroupTodayCeo.today.cash.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupTodayCeo.today.cash.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }
        else if (actionTodayCeo == context.getString(R.string.oer_text)) {
            itemViewGroupTodayCeo.supervisor_goal.text =
                if (individualSupervisorListGroupTodayCeo?.today?.oerStart?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(individualSupervisorListGroupTodayCeo.today.oerStart.goal.value) else ""
            itemViewGroupTodayCeo.supervisor_variance.text =
                if (individualSupervisorListGroupTodayCeo?.today?.oerStart?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(individualSupervisorListGroupTodayCeo.today.oerStart.variance.value) else ""
            if (individualSupervisorListGroupTodayCeo?.today?.oerStart?.actual?.value?.isNaN() == false && individualSupervisorListGroupTodayCeo.today.oerStart.status?.toString() != null) {
                itemViewGroupTodayCeo.supervisor_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListGroupTodayCeo.today.oerStart.actual.value)
                when {
                    individualSupervisorListGroupTodayCeo.today.oerStart.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListGroupTodayCeo.today.oerStart.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewGroupTodayCeo.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewGroupTodayCeo.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }

        return itemViewGroupTodayCeo
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }


}