package com.arria.ping.ui.actions.ceo

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.arria.ping.R
import com.arria.ping.kpi.CEOActionQuery
import com.arria.ping.ui.actions.DetailPastActionActivity

class CEOCurrentActionMetricsDisplayNameAdapter(
    private var context: Context,
    private var currentCEOActionMetricsList: List<CEOActionQuery.CurrentAction?>,
) :
    RecyclerView.Adapter<MultipleViewHolder>() {

    @RequiresApi(Build.VERSION_CODES.O)
    val ceoCurrentTypefaceBoldItalic = context.resources.getFont(R.font.sf_ui_text_bolditalic)

    @RequiresApi(Build.VERSION_CODES.O)
    val ceoCurrentTypefaceRegularItalic = context.resources.getFont(R.font.sf_ui_text_regularitalic)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ceo_action_metrics_display_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return currentCEOActionMetricsList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val ceoCurrentViewHolder: MultipleViewHolder = holder
        val ceoCurrentData = currentCEOActionMetricsList[position]!!

        ceoCurrentViewHolder.ceoCurrentActionName.text = ceoCurrentData.actionMetric!!.displayName
        ceoCurrentViewHolder.ceoCurrentActionStatus.text = ceoCurrentData.actionStatus

        if(ceoCurrentData.actionMetric.status.toString() == context.resources!!.getString(R.string.out_of_range) ){
            ceoCurrentViewHolder.ceoCurrentActionStatus.setTextColor(context.getColor(R.color.red))
            ceoCurrentViewHolder.ceoCurrentActionStatus.typeface = ceoCurrentTypefaceBoldItalic
        }else{
            ceoCurrentViewHolder.ceoCurrentActionStatus.setTextColor(context.getColor(R.color.neutral))
            ceoCurrentViewHolder.ceoCurrentActionStatus.typeface = ceoCurrentTypefaceRegularItalic
        }
         ceoCurrentViewHolder.ceoCurrentActionsParentLinear.setOnClickListener {
            callDetailView(context,position,currentCEOActionMetricsList)
        }
        when (ceoCurrentData.actionRemainingDays) {
            1 -> {
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays4.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays5.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays6.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            2 -> {
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays4.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays5.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            3 -> {
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays4.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            4 -> {
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            5 -> {
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            6 -> {
                ceoCurrentViewHolder.ceoCurrentActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
        }
    }

}


interface OnItemClickListener {
    fun onItemClick(position: Int)
}


class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
    var ceoCurrentActionStatus: TextView = viewHolder.findViewById(R.id.action_status)
    var ceoCurrentActionName: TextView = viewHolder.findViewById(R.id.action_name)
    var ceoCurrentActionRemainingDays1: View = viewHolder.findViewById(R.id.action_remaining_days_1)
    var ceoCurrentActionRemainingDays2: View = viewHolder.findViewById(R.id.action_remaining_days_2)
    var ceoCurrentActionRemainingDays3: View = viewHolder.findViewById(R.id.action_remaining_days_3)
    var ceoCurrentActionRemainingDays4: View = viewHolder.findViewById(R.id.action_remaining_days_4)
    var ceoCurrentActionRemainingDays5: View = viewHolder.findViewById(R.id.action_remaining_days_5)
    var ceoCurrentActionRemainingDays6: View = viewHolder.findViewById(R.id.action_remaining_days_6)

    var ceoCurrentActionsParentLinear: LinearLayout = viewHolder.findViewById(R.id.actions_parent_linear)

}
private fun callDetailView(
    context: Context,
    position: Int,
    currentActionList: List<CEOActionQuery.CurrentAction?>
) {
    val gsonCeoCurrentAction = Gson()
    val intentCeoCurrentAction = Intent(context, DetailPastActionActivity::class.java)
    intentCeoCurrentAction.putExtra("detail_past_action_data_position", position)
    intentCeoCurrentAction.putExtra("detail_past_action_data", gsonCeoCurrentAction.toJson(currentActionList))
    intentCeoCurrentAction.putExtra("isInitialAction", true)

    context.startActivity(intentCeoCurrentAction)
}
