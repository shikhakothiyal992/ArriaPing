/*
package com.mp.ping.ui.actions.checkins

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.mp.ping.R
import com.mp.ping.kpi.CEOActionQuery
import com.mp.ping.kpi.DOActionQuery
import com.mp.ping.kpi.GMCheckInsQuery
import com.mp.ping.kpi.SupervisorActionQuery
import com.mp.ping.utills.Validation
import java.util.*

class GMPastCheckInsListAdapter(
    private var context: Context,
    private var currentActionListData: List<GMCheckInsQuery.PastCheckIn?>,
) :
    RecyclerView.Adapter<GMPastCheckInsListAdapter.MultipleViewHolder>(), Filterable {
    val gson = Gson()
    var onClick: OnItemClickListener? = null
    var currentActionList = listOf<GMCheckInsQuery.PastCheckIn?>()

    fun setOnItemClickLitener(mOnItemClickListener: OnItemClickListener?) {
        this.onClick = mOnItemClickListener
    }
    init {
        currentActionList = currentActionListData
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gm_past_check_ins_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return currentActionList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val viewHolder: MultipleViewHolder = holder
        val data = currentActionList[position]
        viewHolder.pastCheckInTime.text = data!!.checkInCreatedOn!!
        viewHolder.pastCheckInTitle.text = data.checkInTitle
        viewHolder.pastCheckInNarrative.text = data.narrative

    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var pastCheckInTime: TextView = viewHolder.findViewById(R.id.past_check_in_time)
        var pastCheckInTitle: TextView = viewHolder.findViewById(R.id.past_check_in_title)
        var pastCheckInNarrative: TextView = viewHolder.findViewById(R.id.past_check_in_narrative)

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    currentActionList = currentActionListData
                } else {
                    val resultList = mutableListOf<GMCheckInsQuery.PastCheckIn?>()
                    for (row in currentActionListData) {
                        if (row!!.narrative!!.trim().toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT)) || row.checkInTitle!!.trim().toLowerCase(
                                Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT)) || row.checkInCreatedOn!!.trim().toLowerCase(
                                Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    currentActionList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = currentActionList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                currentActionList = results?.values as List<GMCheckInsQuery.PastCheckIn?>
                notifyDataSetChanged()
            }

        }
    }

}
*/
