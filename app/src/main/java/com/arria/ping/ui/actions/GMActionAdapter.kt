package com.arria.ping.ui.actions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.arria.ping.R
import com.arria.ping.kpi.GMActionQuery
import kotlinx.android.synthetic.main.check_in_item.view.*

class GMActionAdapter(
    private var context: Context,
    private var currentActionList: List<GMActionQuery.CurrentAction?>,
) :
    RecyclerView.Adapter<GMActionAdapter.MultipleViewHolder>() {

    var onClick: OnItemClickListener? = null
    @RequiresApi(Build.VERSION_CODES.O)
    val typefaceBoldItalic = context.resources.getFont(R.font.sf_ui_text_bolditalic)
    @RequiresApi(Build.VERSION_CODES.O)
    val typefaceRegularItalic = context.resources.getFont(R.font.sf_ui_text_regularitalic)

    fun setOnItemClickLitener(mOnItemClickLitener: OnItemClickListener?) {
        this.onClick = mOnItemClickLitener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gm_action_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return currentActionList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val viewHolder: MultipleViewHolder = holder
        val data = currentActionList[position]
        viewHolder.actionName.text = data?.actionMetric!!.displayName
        viewHolder.actionStatus.text = data.actionStatus
        if(data.actionMetric.status.toString() == context.resources!!.getString(R.string.out_of_range) ){
            viewHolder.actionStatus.setTextColor(context.getColor(R.color.red))
            viewHolder.actionStatus.typeface = typefaceBoldItalic
        }else{
            viewHolder.actionStatus.setTextColor(context.getColor(R.color.neutral))
            viewHolder.actionStatus.typeface = typefaceRegularItalic
        }
        viewHolder.actionsParentLinear.setOnClickListener {
            callDetailView(context, position, currentActionList)
        }
        if(data.actionRemainingDays == 1){
            viewHolder.actionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays4.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays5.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays6.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            //viewHolder.actionRemainingDays7.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
        }else if(data.actionRemainingDays == 2){
            viewHolder.actionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays4.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays5.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            //viewHolder.actionRemainingDays6.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
        }else if(data.actionRemainingDays == 3){
            viewHolder.actionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays4.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            //viewHolder.actionRemainingDays5.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
        }else if(data.actionRemainingDays == 4){
            viewHolder.actionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            //viewHolder.actionRemainingDays4.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
        }else if(data.actionRemainingDays == 5){
            viewHolder.actionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            viewHolder.actionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            //viewHolder.actionRemainingDays3.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
        }else if(data.actionRemainingDays == 6){
            viewHolder.actionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
            //viewHolder.actionRemainingDays2.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
        }else if(data.actionRemainingDays == 7){
            //viewHolder.actionRemainingDays1.background = ContextCompat.getDrawable(context,R.drawable.green_circle)
        }

    }


    interface OnItemClickListener {
        fun onItemClick(position: Int, storeNumber: String?, action: String)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var actionStatus: TextView = viewHolder.findViewById(R.id.action_status)
        var actionName: TextView = viewHolder.findViewById(R.id.action_name)
        var actionRemainingDays1: View = viewHolder.findViewById(R.id.action_remaining_days_1)
        var actionRemainingDays2: View = viewHolder.findViewById(R.id.action_remaining_days_2)
        var actionRemainingDays3: View = viewHolder.findViewById(R.id.action_remaining_days_3)
        var actionRemainingDays4: View = viewHolder.findViewById(R.id.action_remaining_days_4)
        var actionRemainingDays5: View = viewHolder.findViewById(R.id.action_remaining_days_5)
        var actionRemainingDays6: View = viewHolder.findViewById(R.id.action_remaining_days_6)
        var actionRemainingDays7: View = viewHolder.findViewById(R.id.action_remaining_days_7)

        var actionsParentLinear: LinearLayout = viewHolder.findViewById(R.id.actions_parent_linear)

    }

}
private fun callDetailView(
    context: Context,
    position: Int,
    currentActionList: List<GMActionQuery.CurrentAction?>
) {
    val gson = Gson()
    val intent = Intent(context, DetailPastActionActivity::class.java)
    intent.putExtra("detail_past_action_data_position", position)
    intent.putExtra("detail_past_action_data", gson.toJson(currentActionList[position]))
    intent.putExtra("isInitialAction", true)
    context.startActivity(intent)
}
