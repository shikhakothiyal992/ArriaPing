package com.arria.ping.ui.actions.alerts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.arria.ping.R
import com.arria.ping.kpi.GMAlertsQuery
import com.arria.ping.util.DateFormatterUtil
import com.arria.ping.util.Validation
import java.util.*

class GMPastPastAlertListAdapter(
    private var context: Context,
    private var gmPastAlertsListData: List<GMAlertsQuery.PastAlert?>,
) :
    RecyclerView.Adapter<GMPastPastAlertListAdapter.MultipleViewHolder>(), Filterable {
    val gson = Gson()
    var onClickGMPastAlerts: OnItemClickListener? = null
    var gmPastAlertsList = listOf<GMAlertsQuery.PastAlert?>()

    fun setOnItemClickLitener(mOnItemClickListener: OnItemClickListener?) {
        this.onClickGMPastAlerts = mOnItemClickListener
    }
    init {
        gmPastAlertsList = gmPastAlertsListData
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gm_alerts_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return gmPastAlertsList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val gmPastViewHolder: MultipleViewHolder = holder
        val gmPastData = gmPastAlertsList[position]
        gmPastViewHolder.gmPastAlertTime.text = DateFormatterUtil.formatDateForPastAction(gmPastData!!.alertCreatedOn!!)
        gmPastViewHolder.gmPastAlertTitle.text = gmPastData.alertTitle
        gmPastViewHolder.gmPastAlertNarrative.text = gmPastData.narrative

    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var gmPastAlertTime: TextView = viewHolder.findViewById(R.id.alert_time)
        var gmPastAlertTitle: TextView = viewHolder.findViewById(R.id.alert_title)
        var gmPastAlertNarrative: TextView = viewHolder.findViewById(R.id.alert_narrative)

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearchGMPastAlerts = constraint.toString()
                if (charSearchGMPastAlerts.isEmpty()) {
                    gmPastAlertsList = gmPastAlertsListData
                } else {
                    val resultListGMPastAlerts = mutableListOf<GMAlertsQuery.PastAlert?>()
                    for (row in gmPastAlertsListData) {
                        if (row!!.narrative!!.trim().toLowerCase(Locale.ROOT)
                                .contains(charSearchGMPastAlerts.toLowerCase(Locale.ROOT)) || row.alertTitle!!.trim().toLowerCase(
                                Locale.ROOT)
                                .contains(charSearchGMPastAlerts.toLowerCase(Locale.ROOT)) || row.alertCreatedOn!!.trim().toLowerCase(
                                Locale.ROOT).contains(charSearchGMPastAlerts.toLowerCase(Locale.ROOT))
                        ) {
                            resultListGMPastAlerts.add(row)
                        }
                    }
                    gmPastAlertsList = resultListGMPastAlerts
                }
                val filterResultsGMPastAlerts = FilterResults()
                filterResultsGMPastAlerts.values = gmPastAlertsList
                return filterResultsGMPastAlerts
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                gmPastAlertsList = results?.values as List<GMAlertsQuery.PastAlert?>
                notifyDataSetChanged()
            }

        }
    }

}
