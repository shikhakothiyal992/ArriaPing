package com.arria.ping.ui.kpi.do_.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import com.arria.ping.R
import com.arria.ping.kpi.DODefaultTodayQuery
import com.arria.ping.model.StoreDetailPojo
import com.arria.ping.util.Validation
import kotlinx.android.synthetic.main.expandable_list_store.view.*
import kotlinx.android.synthetic.main.expandable_list_supervisor.view.*

class CustomExpandableListAdapterTodayDO internal constructor(
    private val context: Context,
    private val titleListTodayDo: List<String>,
    private var dataListTodayDo: HashMap<String, List<StoreDetailPojo>>,
    private var superVisorDetailsTodayDo: DODefaultTodayQuery.Do_,
    private var actionTodayDo: String,
) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): StoreDetailPojo {
        return this.dataListTodayDo[this.titleListTodayDo[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    infix fun setChild(childData: HashMap<String, List<StoreDetailPojo>>) {
        dataListTodayDo = childData
        notifyDataSetChanged()
    }

    @SuppressLint("InflateParams")
    override fun getChildView(
        listPositionTodayDo: Int,
        expandedListPositionTodayDo: Int,
        isLastChildTodayDo: Boolean,
        convertViewTodayDo: View?,
        parentTodayDo: ViewGroup,
    ): View {
        var itemViewTodayDo = convertViewTodayDo
        val expandedListTextTodayDo = getChild(listPositionTodayDo, expandedListPositionTodayDo)


        if (convertViewTodayDo == null) {
            val layoutInflaterTodayDo =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemViewTodayDo = layoutInflaterTodayDo.inflate(R.layout.expandable_list_store, null)
        }
        itemViewTodayDo!!.store_parent

        val individualStoreListTodayDo = expandedListTextTodayDo
        if (getChildId(listPositionTodayDo,
                expandedListPositionTodayDo) == 0L && getChildrenCount(listPositionTodayDo) > 3
        ) {
            itemViewTodayDo.store_name.text =
                if (superVisorDetailsTodayDo.kpis!!.individualSupervisors[listPositionTodayDo]!!.supervisorName != null) superVisorDetailsTodayDo.kpis!!.individualSupervisors[listPositionTodayDo]!!.supervisorName.plus(
                    "'s overview") else ""
            itemViewTodayDo.store_name.paintFlags =
                itemViewTodayDo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewTodayDo.store_goal.visibility = View.GONE
            itemViewTodayDo.store_variance.visibility = View.GONE
            itemViewTodayDo.store_actual.visibility = View.GONE

        } else {
            itemViewTodayDo.store_name.text = individualStoreListTodayDo.storeNumber
            itemViewTodayDo.store_name.paintFlags =
                itemViewTodayDo.store_name.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            itemViewTodayDo.store_goal.visibility = View.VISIBLE
            itemViewTodayDo.store_variance.visibility = View.VISIBLE
            itemViewTodayDo.store_actual.visibility = View.VISIBLE
        }
        if (actionTodayDo == context.getString(R.string.awus_text)) {
            if (individualStoreListTodayDo.storeGoal != "" && individualStoreListTodayDo.storeGoal != "null") {
                itemViewTodayDo.store_goal.text = context.getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(
                        individualStoreListTodayDo.storeGoal?.toDouble()))
            }
            if (individualStoreListTodayDo.storeVariance != "" && individualStoreListTodayDo.storeVariance != "null") {
                itemViewTodayDo.store_variance.text = context.getString(R.string.dollar_text)
                    .plus(Validation().dollarFormatting(individualStoreListTodayDo.storeVariance?.toDouble()))
            }

            if ((individualStoreListTodayDo.storeVariance != "" && individualStoreListTodayDo.storeVariance != "null") && (individualStoreListTodayDo.status != "" && individualStoreListTodayDo.status != "null")) {
                itemViewTodayDo.store_actual.text = context.getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(individualStoreListTodayDo.storeActual?.toDouble())
                )
                when (individualStoreListTodayDo.status) {
                    context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.red))

                    }
                    context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }else  if (actionTodayDo == context.getString(R.string.labour_text)) {
            if (individualStoreListTodayDo.storeGoal != "" && individualStoreListTodayDo.storeGoal != "null") {
                itemViewTodayDo.store_goal.text =
                    Validation().ignoreZeroAfterDecimal(
                        individualStoreListTodayDo.storeGoal?.toDouble()).plus(context.getString(R.string.percentage_text))
            }
            if (individualStoreListTodayDo.storeVariance != "" && individualStoreListTodayDo.storeVariance != "null") {
                itemViewTodayDo.store_variance.text =
                    Validation().ignoreZeroAfterDecimal(individualStoreListTodayDo.storeVariance?.toDouble()).plus(context.getString(R.string.percentage_text))
            }

            if ((individualStoreListTodayDo.storeVariance != "" && individualStoreListTodayDo.storeVariance != "null") && (individualStoreListTodayDo.status != "" && individualStoreListTodayDo.status != "null")) {
                itemViewTodayDo.store_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualStoreListTodayDo.storeActual?.toDouble()).plus(context.getString(R.string.percentage_text)
                    )
                when (individualStoreListTodayDo.status) {
                    context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.red))

                    }
                    context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }else  if (actionTodayDo == context.getString(R.string.service_text)) {
            if (individualStoreListTodayDo.storeGoal != "" && individualStoreListTodayDo.storeGoal != "null") {
                itemViewTodayDo.store_goal.text =
                    Validation().ignoreZeroAfterDecimal(
                        individualStoreListTodayDo.storeGoal?.toDouble()).plus(context.getString(R.string.percentage_text))
            }
            if (individualStoreListTodayDo.storeVariance != "" && individualStoreListTodayDo.storeVariance != "null") {
                itemViewTodayDo.store_variance.text =
                    Validation().ignoreZeroAfterDecimal(individualStoreListTodayDo.storeVariance?.toDouble()).plus(context.getString(R.string.percentage_text))
            }

            if ((individualStoreListTodayDo.storeVariance != "" && individualStoreListTodayDo.storeVariance != "null") && (individualStoreListTodayDo.status != "" && individualStoreListTodayDo.status != "null")) {
                itemViewTodayDo.store_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualStoreListTodayDo.storeActual?.toDouble()).plus(context.getString(R.string.percentage_text)
                    )
                when (individualStoreListTodayDo.status) {
                    context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.red))

                    }
                    context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }else  if (actionTodayDo == context.getString(R.string.cash_text)) {
            if (individualStoreListTodayDo.storeGoal != "" && individualStoreListTodayDo.storeGoal != "null") {
                itemViewTodayDo.store_goal.text =
                    Validation().ignoreZeroAfterDecimal(
                        individualStoreListTodayDo.storeGoal?.toDouble())
            }
            if (individualStoreListTodayDo.storeVariance != "" && individualStoreListTodayDo.storeVariance != "null") {
                itemViewTodayDo.store_variance.text =
                    Validation().ignoreZeroAfterDecimal(individualStoreListTodayDo.storeVariance?.toDouble())
            }

            if ((individualStoreListTodayDo.storeVariance != "" && individualStoreListTodayDo.storeVariance != "null") && (individualStoreListTodayDo.status != "" && individualStoreListTodayDo.status != "null")) {
                itemViewTodayDo.store_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualStoreListTodayDo.storeActual?.toDouble())
                when (individualStoreListTodayDo.status) {
                    context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.red))

                    }
                    context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }else  if (actionTodayDo == context.getString(R.string.oer_text)) {
            if (individualStoreListTodayDo.storeGoal != "" && individualStoreListTodayDo.storeGoal != "null") {
                itemViewTodayDo.store_goal.text =
                    Validation().ignoreZeroAfterDecimal(
                        individualStoreListTodayDo.storeGoal?.toDouble())
            }
            if (individualStoreListTodayDo.storeVariance != "" && individualStoreListTodayDo.storeVariance != "null") {
                itemViewTodayDo.store_variance.text =
                    Validation().ignoreZeroAfterDecimal(individualStoreListTodayDo.storeVariance?.toDouble())
            }

            if ((individualStoreListTodayDo.storeVariance != "" && individualStoreListTodayDo.storeVariance != "null") && (individualStoreListTodayDo.status != "" && individualStoreListTodayDo.status != "null")) {
                itemViewTodayDo.store_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualStoreListTodayDo.storeActual?.toDouble())
                when (individualStoreListTodayDo.status) {
                    context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.red))

                    }
                    context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayDo.store_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayDo.store_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }



        return itemViewTodayDo
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.dataListTodayDo[this.titleListTodayDo[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return this.titleListTodayDo[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.titleListTodayDo.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    @SuppressLint("InflateParams")
    override fun getGroupView(
        listPositionTodayDoGroup: Int,
        isExpandedTodayDoGroup: Boolean,
        convertViewTodayDoGroup: View?,
        parentTodayDoGroup: ViewGroup,
    ): View {
        var itemViewTodayDoGroup = convertViewTodayDoGroup
        if (convertViewTodayDoGroup == null) {
            val layoutInflater =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemViewTodayDoGroup = layoutInflater.inflate(R.layout.expandable_list_supervisor, null)
        }
        val individualSupervisorListTodayDoGroup = superVisorDetailsTodayDo.kpis!!.individualSupervisors[listPositionTodayDoGroup]
        itemViewTodayDoGroup!!.supervisor_name.text =
            if (individualSupervisorListTodayDoGroup?.supervisorName != null) individualSupervisorListTodayDoGroup.supervisorName else ""


        if (actionTodayDo == context.getString(R.string.awus_text)) {

            itemViewTodayDoGroup.supervisor_goal.text =
                if (individualSupervisorListTodayDoGroup?.today?.sales?.goal?.value?.isNaN() == false) context.getString(
                    R.string.dollar_text
                )
                    .plus(Validation().dollarFormatting(individualSupervisorListTodayDoGroup.today.sales.goal.value)) else ""
            itemViewTodayDoGroup.supervisor_variance.text =
                if (individualSupervisorListTodayDoGroup?.today?.sales?.variance?.value?.isNaN() == false) context.getString(
                    R.string.dollar_text
                )
                    .plus(Validation().dollarFormatting(individualSupervisorListTodayDoGroup.today.sales.variance.value)) else ""
            if (individualSupervisorListTodayDoGroup?.today?.sales?.actual?.value?.isNaN() == false && individualSupervisorListTodayDoGroup.today.sales.status?.toString() != null) {
                itemViewTodayDoGroup.supervisor_actual.text = context.getString(R.string.dollar_text).plus(
                    Validation().dollarFormatting(individualSupervisorListTodayDoGroup.today.sales.actual.value)
                )
                when {
                    individualSupervisorListTodayDoGroup.today.sales.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListTodayDoGroup.today.sales.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }  else if (actionTodayDo == context.getString(R.string.labour_text)) {
            itemViewTodayDoGroup.supervisor_goal.text =
                if (individualSupervisorListTodayDoGroup?.today?.labor?.goal?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayDoGroup.today.labor.goal.percentage).plus(
                    context.getString(
                        R.string.percentage_text
                    )
                ) else ""
            itemViewTodayDoGroup.supervisor_variance.text =
                if (individualSupervisorListTodayDoGroup?.today?.labor?.variance?.percentage?.isNaN() == false) Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayDoGroup.today.labor.variance.percentage) .plus(
                    context.getString(
                        R.string.percentage_text
                    )
                )else ""
            if (individualSupervisorListTodayDoGroup?.today?.labor?.actual?.percentage?.isNaN() == false && individualSupervisorListTodayDoGroup.today.labor.status?.toString() != null) {
                itemViewTodayDoGroup.supervisor_actual.text =
                    individualSupervisorListTodayDoGroup.today.labor.actual.percentage.toString().plus(
                        context.getString(
                            R.string.percentage_text
                        )
                    )
                when {
                    individualSupervisorListTodayDoGroup.today.labor.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListTodayDoGroup.today.labor.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        } else if (actionTodayDo == context.getString(R.string.service_text)) {
            itemViewTodayDoGroup.supervisor_goal.text =
                if (individualSupervisorListTodayDoGroup?.today?.service?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayDoGroup.today.service.goal.value) .plus(
                    context.getString(
                        R.string.percentage_text
                    )
                )else ""
            itemViewTodayDoGroup.supervisor_variance.text =
                if (individualSupervisorListTodayDoGroup?.today?.service?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayDoGroup.today.service.variance.value).plus(
                    context.getString(
                        R.string.percentage_text
                    )
                ) else ""
            if (individualSupervisorListTodayDoGroup?.today?.service?.actual?.value?.isNaN() == false && individualSupervisorListTodayDoGroup.today.service.status?.toString() != null) {
                itemViewTodayDoGroup.supervisor_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayDoGroup.today.service.actual.value).plus(
                        context.getString(
                            R.string.percentage_text
                        )
                    )
                when {
                    individualSupervisorListTodayDoGroup.today.service.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListTodayDoGroup.today.service.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }
        else if (actionTodayDo == context.getString(R.string.cash_text)) {
            if (individualSupervisorListTodayDoGroup?.today?.cash?.actual?.value?.isNaN() == false && individualSupervisorListTodayDoGroup.today.cash.status?.toString() != null) {
                itemViewTodayDoGroup.supervisor_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayDoGroup.today.cash.actual.value)
                when {
                    individualSupervisorListTodayDoGroup.today.cash.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListTodayDoGroup.today.cash.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }
        else if (actionTodayDo == context.getString(R.string.oer_text)) {
            itemViewTodayDoGroup.supervisor_goal.text =
                if (individualSupervisorListTodayDoGroup?.today?.oerStart?.goal?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayDoGroup.today.oerStart.goal.value) else ""
            itemViewTodayDoGroup.supervisor_variance.text =
                if (individualSupervisorListTodayDoGroup?.today?.oerStart?.variance?.value?.isNaN() == false) Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayDoGroup.today.oerStart.variance.value) else ""
            if (individualSupervisorListTodayDoGroup?.today?.oerStart?.actual?.value?.isNaN() == false && individualSupervisorListTodayDoGroup.today.oerStart.status?.toString() != null) {
                itemViewTodayDoGroup.supervisor_actual.text =
                    Validation().ignoreZeroAfterDecimal(individualSupervisorListTodayDoGroup.today.oerStart.actual.value)
                when {
                    individualSupervisorListTodayDoGroup.today.oerStart.status.toString() == context.resources.getString(
                        R.string.out_of_range
                    ) -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.red_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.red))

                    }
                    individualSupervisorListTodayDoGroup.today.oerStart.status.toString() == context.resources.getString(
                        R.string.under_limit
                    ) -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.green_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.green))

                    } else -> {
                        itemViewTodayDoGroup.supervisor_actual.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.black_circle,
                            0
                        )
                        itemViewTodayDoGroup.supervisor_actual.setTextColor(context.getColor(R.color.text_color))

                    }
                }
            }
        }

        return itemViewTodayDoGroup
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
    

}