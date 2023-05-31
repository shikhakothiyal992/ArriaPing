package com.arria.ping.ui.kpi.supervisor.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import com.arria.ping.model.StoreDetailPojo
import com.arria.ping.util.Validation

class SupervisorTodayAdapter(
    private var context: Context,
    private var storeFilterData: List<StoreDetailPojo>,
    private var action: String,
    private var supervisorName: String?,
) :
    RecyclerView.Adapter<SupervisorTodayAdapter.MultipleViewHolder>() {

    var onClick: OnItemClickListener? = null

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.onClick = mOnItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.expandable_list_store, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return storeFilterData.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val viewHolder: MultipleViewHolder = holder
        val bean = storeFilterData[position]

         if (action == context.getString(R.string.awus_text)) {

             if (bean.storeNumber != "" && bean.storeNumber != "null") {
                 viewHolder.storeName.text = bean.storeNumber
                 viewHolder.storeName.paintFlags =
                     viewHolder.storeName.paintFlags or Paint.UNDERLINE_TEXT_FLAG

             }
             if (bean.storeGoal != "" && bean.storeGoal != "null") {
                 viewHolder.storeGoal.text = context.getString(R.string.dollar_text).plus(
                     Validation().dollarFormatting(
                         bean.storeGoal?.toDouble()))
             }

             if (bean.storeVariance != "" && bean.storeVariance != "null") {
                 viewHolder.storeVariance.text = context.getString(R.string.dollar_text)
                     .plus(Validation().dollarFormatting(bean.storeVariance?.toDouble()))
             }

             if ((bean.storeVariance != "" && bean.storeVariance != "null") && (bean.status != "" && bean.status != "null")) {
                 viewHolder.storeActual.text = context.getString(R.string.dollar_text).plus(
                     Validation().dollarFormatting(bean.storeActual?.toDouble())
                 )
                 when (bean.status) {
                     context.resources.getString(
                         R.string.out_of_range
                     ) -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.red_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.red))

                     }
                     context.resources.getString(
                         R.string.under_limit
                     ) -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.green_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.green))

                     } else -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.black_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.text_color))

                     }
                 }
             }
         }else  if (action == context.getString(R.string.labour_vs_goal_text)) {
             if (bean.storeNumber != "" && bean.storeNumber != "null") {
                 viewHolder.storeName.text = bean.storeNumber
                 viewHolder.storeName.paintFlags =
                     viewHolder.storeName.paintFlags or Paint.UNDERLINE_TEXT_FLAG

             }
             if (bean.storeGoal != "" && bean.storeGoal != "null") {
                 viewHolder.storeGoal.text =
                     Validation().ignoreZeroAfterDecimal(
                         bean.storeGoal?.toDouble()).plus(context.getString(R.string.percentage_text))
             }

             if (bean.storeVariance != "" && bean.storeVariance != "null") {
                 viewHolder.storeVariance.text = Validation().dollarFormatting(bean.storeVariance?.toDouble()).plus(context.getString(R.string.percentage_text))
             }

             if ((bean.storeVariance != "" && bean.storeVariance != "null") && (bean.status != "" && bean.status != "null")) {
                 viewHolder.storeActual.text =
                     Validation().dollarFormatting(bean.storeActual?.toDouble()).plus(context.getString(R.string.percentage_text))
                 when (bean.status) {
                     context.resources.getString(
                         R.string.out_of_range
                     ) -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.red_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.red))

                     }
                     context.resources.getString(
                         R.string.under_limit
                     ) -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.green_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.green))

                     } else -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.black_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.text_color))

                     }
                 }
             }
         }else  if (action == context.getString(R.string.service_text)) {
             if (bean.storeNumber != "" && bean.storeNumber != "null") {
                 viewHolder.storeName.text = bean.storeNumber
                 viewHolder.storeName.paintFlags =
                     viewHolder.storeName.paintFlags or Paint.UNDERLINE_TEXT_FLAG

             }
             if (bean.storeGoal != "" && bean.storeGoal != "null") {
                 viewHolder.storeGoal.text =
                     Validation().ignoreZeroAfterDecimal(
                         bean.storeGoal?.toDouble()).plus(context.getString(R.string.percentage_text))
             }

             if (bean.storeVariance != "" && bean.storeVariance != "null") {
                 viewHolder.storeVariance.text = Validation().dollarFormatting(bean.storeVariance?.toDouble()).plus(context.getString(R.string.percentage_text))
             }

             if ((bean.storeVariance != "" && bean.storeVariance != "null") && (bean.status != "" && bean.status != "null")) {
                 viewHolder.storeActual.text =
                     Validation().dollarFormatting(bean.storeActual?.toDouble()).plus(context.getString(R.string.percentage_text))
                 when (bean.status) {
                     context.resources.getString(
                         R.string.out_of_range
                     ) -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.red_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.red))

                     }
                     context.resources.getString(
                         R.string.under_limit
                     ) -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.green_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.green))

                     } else -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.black_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.text_color))

                     }
                 }
             }
         }else  if (action == context.getString(R.string.cash_text)) {
             if (bean.storeNumber != "" && bean.storeNumber != "null") {
                 viewHolder.storeName.text = bean.storeNumber
                 viewHolder.storeName.paintFlags =
                     viewHolder.storeName.paintFlags or Paint.UNDERLINE_TEXT_FLAG

             }
             if (bean.storeGoal != "" && bean.storeGoal != "null") {
                 viewHolder.storeGoal.text =
                     Validation().ignoreZeroAfterDecimal(
                         bean.storeGoal?.toDouble())
             }

             if (bean.storeVariance != "" && bean.storeVariance != "null") {
                 viewHolder.storeVariance.text = Validation().dollarFormatting(bean.storeVariance?.toDouble())
             }

             if ((bean.storeVariance != "" && bean.storeVariance != "null") && (bean.status != "" && bean.status != "null")) {
                 viewHolder.storeActual.text =
                     Validation().dollarFormatting(bean.storeActual?.toDouble())
                 when (bean.status) {
                     context.resources.getString(
                         R.string.out_of_range
                     ) -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.red_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.red))

                     }
                     context.resources.getString(
                         R.string.under_limit
                     ) -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.green_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.green))

                     } else -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.black_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.text_color))

                     }
                 }
             }
         }else if (action == context.getString(R.string.oer_text)) {
             if (bean.storeNumber != "" && bean.storeNumber != "null") {
                 viewHolder.storeName.text = bean.storeNumber
                 viewHolder.storeName.paintFlags =
                     viewHolder.storeName.paintFlags or Paint.UNDERLINE_TEXT_FLAG

             }
             if (bean.storeGoal != "" && bean.storeGoal != "null") {
                 viewHolder.storeGoal.text =
                     Validation().ignoreZeroAfterDecimal(
                         bean.storeGoal?.toDouble())
             }

             if (bean.storeVariance != "" && bean.storeVariance != "null") {
                 viewHolder.storeVariance.text = Validation().dollarFormatting(bean.storeVariance?.toDouble())
             }

             if ((bean.storeVariance != "" && bean.storeVariance != "null") && (bean.status != "" && bean.status != "null")) {
                 viewHolder.storeActual.text =
                     Validation().dollarFormatting(bean.storeActual?.toDouble())
                 when {
                     if (bean.status == context.resources.getString(
                             R.string.out_of_range
                         )
                     ) {
                         true
                     } else {
                         false
                     } -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.red_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.red))

                     }
                     bean.status == context.resources.getString(
                         R.string.under_limit
                     ) -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.green_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.green))

                     } else -> {
                         viewHolder.storeActual.setCompoundDrawablesWithIntrinsicBounds(
                             0,
                             0,
                             R.drawable.black_circle,
                             0
                         )
                         viewHolder.storeActual.setTextColor(context.getColor(R.color.text_color))

                     }
                 }
             }
         }
        if (onClick != null) viewHolder.storeParent.setOnClickListener {
            onClick!!.onItemClick(position, bean.storeNumber, action)
        }
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int, storeNumber: String?, action: String)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var storeName: TextView = viewHolder.findViewById(R.id.store_name)
        var storeGoal: TextView = viewHolder.findViewById(R.id.store_goal)
        var storeVariance: TextView = viewHolder.findViewById(R.id.store_variance)
        var storeActual: TextView = viewHolder.findViewById(R.id.store_actual)
        var storeParent: LinearLayout = viewHolder.findViewById(R.id.store_parent)

    }

}
