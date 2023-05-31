package com.arria.ping.ui.actions.do_

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
import com.arria.ping.kpi.DOActionQuery
import com.arria.ping.ui.actions.DetailPastActionActivity

class DOCurrentActionMetricsDisplayNameAdapter(
    private var context: Context,
    private var currentDoActionMetricsDisplayList: List<DOActionQuery.CurrentAction?>,
) :
    RecyclerView.Adapter<MultipleViewHolder>() {

    @RequiresApi(Build.VERSION_CODES.O)
    val doCurrentTypefaceBoldItalic = context.resources.getFont(R.font.sf_ui_text_bolditalic)
    @RequiresApi(Build.VERSION_CODES.O)
    val doCurrentTypefaceRegularItalic = context.resources.getFont(R.font.sf_ui_text_regularitalic)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ceo_action_metrics_display_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return currentDoActionMetricsDisplayList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val doCurrentViewHolder: MultipleViewHolder = holder
        val doCurrentData = currentDoActionMetricsDisplayList[position]!!

        doCurrentViewHolder.doCurrentActionName.text = doCurrentData.actionMetric!!.displayName
        doCurrentViewHolder.doCurrentActionStatus.text = doCurrentData.actionStatus

        if(doCurrentData.actionMetric.status.toString() == context.resources!!.getString(R.string.out_of_range) ){
            doCurrentViewHolder.doCurrentActionStatus.setTextColor(context.getColor(R.color.red))
            doCurrentViewHolder.doCurrentActionStatus.typeface = doCurrentTypefaceBoldItalic
        }else{
            doCurrentViewHolder.doCurrentActionStatus.setTextColor(context.getColor(R.color.neutral))
            doCurrentViewHolder.doCurrentActionStatus.typeface = doCurrentTypefaceRegularItalic
        }
        doCurrentViewHolder.doCurrentActionsParentLinear.setOnClickListener {
            callDetailView(context, position, currentDoActionMetricsDisplayList)
        }
        when (doCurrentData.actionRemainingDays) {
            1 -> {
                doCurrentViewHolder.doCurrentActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays4.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays5.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays6.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            2 -> {
                doCurrentViewHolder.doCurrentActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays4.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays5.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            3 -> {
                doCurrentViewHolder.doCurrentActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays4.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            4 -> {
                doCurrentViewHolder.doCurrentActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            5 -> {
                doCurrentViewHolder.doCurrentActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                doCurrentViewHolder.doCurrentActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            6 -> {
                doCurrentViewHolder.doCurrentActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
        }
    }

}


interface OnItemClickListener {
    fun onItemClick(position: Int, storeNumber: String?, action: String)
}


class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
    var doCurrentActionStatus: TextView = viewHolder.findViewById(R.id.action_status)
    var doCurrentActionName: TextView = viewHolder.findViewById(R.id.action_name)
    var doCurrentActionRemainingDays1: View = viewHolder.findViewById(R.id.action_remaining_days_1)
    var doCurrentActionRemainingDays2: View = viewHolder.findViewById(R.id.action_remaining_days_2)
    var doCurrentActionRemainingDays3: View = viewHolder.findViewById(R.id.action_remaining_days_3)
    var doCurrentActionRemainingDays4: View = viewHolder.findViewById(R.id.action_remaining_days_4)
    var doCurrentActionRemainingDays5: View = viewHolder.findViewById(R.id.action_remaining_days_5)
    var doCurrentActionRemainingDays6: View = viewHolder.findViewById(R.id.action_remaining_days_6)
    var doCurrentActionsParentLinear: LinearLayout = viewHolder.findViewById(R.id.actions_parent_linear)

}
private fun callDetailView(
    context: Context,
    position: Int,
    currentActionList: List<DOActionQuery.CurrentAction?>
) {
    val gsonDoCurrentAction = Gson()
    val intentDoCurrentAction = Intent(context, DetailPastActionActivity::class.java)
    intentDoCurrentAction.putExtra("detail_past_action_data_position", position)
    intentDoCurrentAction.putExtra("detail_past_action_data", gsonDoCurrentAction.toJson(currentActionList))
    intentDoCurrentAction.putExtra("isInitialAction", true)

    context.startActivity(intentDoCurrentAction)
}
