package com.arria.ping.ui.actions.alerts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import com.arria.ping.kpi.GMAlertsQuery
import com.arria.ping.util.DateFormatterUtil

class GMAlertsAdapter(
    private var context: Context,
    private var gmAlertsList: List<GMAlertsQuery.CurrentAlert?>,
) :
    RecyclerView.Adapter<GMAlertsAdapter.MultipleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gm_alerts_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return gmAlertsList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val gmAlertsViewHolder: MultipleViewHolder = holder
        val gmAlertsData = gmAlertsList[position]
        gmAlertsViewHolder.gmAlertTime.text = DateFormatterUtil.formatDateForPastAction(gmAlertsData!!.alertCreatedOn!!)
        gmAlertsViewHolder.gmAlertTitle.text = gmAlertsData.alertTitle
        gmAlertsViewHolder.gmAlertNarrative.text = gmAlertsData.narrative
    }

    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var gmAlertTime: TextView = viewHolder.findViewById(R.id.alert_time)
        var gmAlertTitle: TextView = viewHolder.findViewById(R.id.alert_title)
        var gmAlertNarrative: TextView = viewHolder.findViewById(R.id.alert_narrative)

    }

}

