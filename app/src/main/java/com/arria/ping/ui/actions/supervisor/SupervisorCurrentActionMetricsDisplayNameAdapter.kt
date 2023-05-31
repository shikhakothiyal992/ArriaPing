package com.arria.ping.ui.actions.supervisor

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
import com.arria.ping.kpi.SupervisorActionQuery
import com.arria.ping.ui.actions.DetailPastActionActivity

class SupervisorCurrentActionMetricsDisplayNameAdapter(
    private var context: Context,
    private var currentSupervisorActionMetricsList: List<SupervisorActionQuery.CurrentAction?>,
) :
    RecyclerView.Adapter<MultipleViewHolder>() {

    @RequiresApi(Build.VERSION_CODES.O)
    val supervisorCurrentTypefaceBoldItalic = context.resources.getFont(R.font.sf_ui_text_bolditalic)
    @RequiresApi(Build.VERSION_CODES.O)
    val supervisorCurrentTypefaceRegularItalic = context.resources.getFont(R.font.sf_ui_text_regularitalic)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ceo_action_metrics_display_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return currentSupervisorActionMetricsList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val supervisorCurrentViewHolder : MultipleViewHolder = holder
        val supervisorCurrentData = currentSupervisorActionMetricsList[position]!!

        supervisorCurrentViewHolder.supervisorActionName.text = supervisorCurrentData.actionMetric!!.displayName
        supervisorCurrentViewHolder.supervisorActionStatus.text = supervisorCurrentData.actionStatus

        if(supervisorCurrentData.actionMetric.status.toString() == context.resources!!.getString(R.string.out_of_range) ){
            supervisorCurrentViewHolder.supervisorActionStatus.setTextColor(context.getColor(R.color.red))
            supervisorCurrentViewHolder.supervisorActionStatus.typeface = supervisorCurrentTypefaceBoldItalic
        }else{
            supervisorCurrentViewHolder.supervisorActionStatus.setTextColor(context.getColor(R.color.neutral))
            supervisorCurrentViewHolder.supervisorActionStatus.typeface = supervisorCurrentTypefaceRegularItalic
        }
        supervisorCurrentViewHolder.supervisorActionsParentLinear.setOnClickListener {
            callDetailView(context, position, currentSupervisorActionMetricsList)
        }
        when (supervisorCurrentData.actionRemainingDays) {
            1 -> {
                supervisorCurrentViewHolder.supervisorActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays4.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays5.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays6.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            2 -> {
                supervisorCurrentViewHolder.supervisorActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays4.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays5.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            3 -> {
                supervisorCurrentViewHolder.supervisorActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays4.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            4 -> {
                supervisorCurrentViewHolder.supervisorActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            5 -> {
                supervisorCurrentViewHolder.supervisorActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
                supervisorCurrentViewHolder.supervisorActionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }
            6 -> {
                supervisorCurrentViewHolder.supervisorActionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            }

        }
    }

}


interface OnItemClickListener {
    fun onItemClick(position: Int, storeNumber: String?, action: String)
}


class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
    var supervisorActionStatus: TextView = viewHolder.findViewById(R.id.action_status)
    var supervisorActionName: TextView = viewHolder.findViewById(R.id.action_name)
    var supervisorActionRemainingDays1: View = viewHolder.findViewById(R.id.action_remaining_days_1)
    var supervisorActionRemainingDays2: View = viewHolder.findViewById(R.id.action_remaining_days_2)
    var supervisorActionRemainingDays3: View = viewHolder.findViewById(R.id.action_remaining_days_3)
    var supervisorActionRemainingDays4: View = viewHolder.findViewById(R.id.action_remaining_days_4)
    var supervisorActionRemainingDays5: View = viewHolder.findViewById(R.id.action_remaining_days_5)
    var supervisorActionRemainingDays6: View = viewHolder.findViewById(R.id.action_remaining_days_6)
    var supervisorActionsParentLinear: LinearLayout = viewHolder.findViewById(R.id.actions_parent_linear)

}
private fun callDetailView(
    context: Context,
    position: Int,
    currentActionList: List<SupervisorActionQuery.CurrentAction?>
) {
    val gsonSupervisorCurrentAction = Gson()
    val intentSupervisorCurrentAction = Intent(context, DetailPastActionActivity::class.java)
    intentSupervisorCurrentAction.putExtra("detail_past_action_data_position", position)
    intentSupervisorCurrentAction.putExtra("detail_past_action_data", gsonSupervisorCurrentAction.toJson(currentActionList))
    intentSupervisorCurrentAction.putExtra("isInitialAction", true)

    context.startActivity(intentSupervisorCurrentAction)
}