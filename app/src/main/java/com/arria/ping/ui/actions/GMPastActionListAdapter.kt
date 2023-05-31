package com.arria.ping.ui.actions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import com.arria.ping.kpi.GMActionQuery
import java.util.*

class GMPastActionListAdapter(
    private var context: Context,
    private var gmPastActionListData: List<GMActionQuery.PastAction?>,
) :
    RecyclerView.Adapter<GMPastActionListAdapter.MultipleViewHolder>(), Filterable {
    var onClickGMPastAction: OnItemClickListener? = null
    var gmPastActionList = listOf<GMActionQuery.PastAction?>()

    fun setOnGMPastActionItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.onClickGMPastAction = mOnItemClickListener
    }
    init {
        gmPastActionList = gmPastActionListData
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gm_past_action_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return gmPastActionList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val gmPastActionViewHolder: MultipleViewHolder = holder
        val gmPastActionData = gmPastActionList[position]
        gmPastActionViewHolder.gmActionMainItem.text = gmPastActionData?.actionMetric!!.displayName
        gmPastActionViewHolder.gmActionMainItemUsage.text = gmPastActionData.actionTitle
        gmPastActionViewHolder.gmActionToPerform.text = gmPastActionData.actionStatus
        if (onClickGMPastAction != null) gmPastActionViewHolder.gmActionParentItem.setOnClickListener {
            onClickGMPastAction!!.onItemClick(position)

        }
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var gmActionMainItem: TextView = viewHolder.findViewById(R.id.action_main_item)
        var gmActionMainItemUsage: TextView = viewHolder.findViewById(R.id.action_main_item_usage)
        var gmActionToPerform: TextView = viewHolder.findViewById(R.id.action_to_perform)
        var gmActionParentItem: LinearLayout = viewHolder.findViewById(R.id.action_parent_item)

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearchGMPastAction = constraint.toString()
                if (charSearchGMPastAction.isEmpty()) {
                    gmPastActionList = gmPastActionListData
                } else {
                    val resultListGMPastAction = mutableListOf<GMActionQuery.PastAction?>()
                    for (row in gmPastActionListData) {
                        if (row!!.actionTitle!!.trim().toLowerCase(Locale.ROOT)
                                .contains(charSearchGMPastAction.toLowerCase(Locale.ROOT)) || row.actionMetric!!.displayName!!.trim().toLowerCase(
                                Locale.ROOT)
                                .contains(charSearchGMPastAction.toLowerCase(Locale.ROOT)) || row.actionStatus!!.trim().toLowerCase(
                                Locale.ROOT).contains(charSearchGMPastAction.toLowerCase(Locale.ROOT))
                        ) {
                            resultListGMPastAction.add(row)
                        }
                    }
                    gmPastActionList = resultListGMPastAction
                }
                val filterResultsGMPastAction = FilterResults()
                filterResultsGMPastAction.values = gmPastActionList
                return filterResultsGMPastAction
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                gmPastActionList = results?.values as List<GMActionQuery.PastAction?>
                notifyDataSetChanged()
            }

        }
    }

}
