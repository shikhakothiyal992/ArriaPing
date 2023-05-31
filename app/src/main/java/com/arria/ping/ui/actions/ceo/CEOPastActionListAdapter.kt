package com.arria.ping.ui.actions.ceo

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
import com.arria.ping.kpi.CEOActionQuery
import java.util.*

class CEOPastActionListAdapter(
    private var context: Context,
    private var ceoPastActionListData: List<CEOActionQuery.PastAction?>,
) :
    RecyclerView.Adapter<CEOPastActionListAdapter.MultipleViewHolder>(), Filterable {
    var onClickCEOPastAction: OnItemClickListener? = null
    var ceoPastActionList = listOf<CEOActionQuery.PastAction?>()

    fun setOnCEOPastActionItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.onClickCEOPastAction = mOnItemClickListener
    }
    init {
        ceoPastActionList = ceoPastActionListData
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gm_past_action_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return ceoPastActionList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val ceoPastActionViewHolder: MultipleViewHolder = holder
        val ceoPastActionData = ceoPastActionList[position]
        ceoPastActionViewHolder.ceoPastActionMainItem.text = ceoPastActionData?.actionMetric!!.displayName
        ceoPastActionViewHolder.ceoPastActionMainItemUsage.text = ceoPastActionData.actionTitle
        ceoPastActionViewHolder.ceoPastActionToPerform.text = ceoPastActionData.actionStatus

        if (onClickCEOPastAction != null) ceoPastActionViewHolder.ceoPastActionParentItem.setOnClickListener {
            onClickCEOPastAction!!.onItemClick(position)

        }
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var ceoPastActionMainItem: TextView = viewHolder.findViewById(R.id.action_main_item)
        var ceoPastActionMainItemUsage: TextView = viewHolder.findViewById(R.id.action_main_item_usage)
        var ceoPastActionToPerform: TextView = viewHolder.findViewById(R.id.action_to_perform)
        var ceoPastActionParentItem: LinearLayout = viewHolder.findViewById(R.id.action_parent_item)

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearchCEOPastAction = constraint.toString()
                if (charSearchCEOPastAction.isEmpty()) {
                    ceoPastActionList = ceoPastActionListData
                } else {
                    val resultListCEOPastAction = mutableListOf<CEOActionQuery.PastAction?>()
                    for (row in ceoPastActionListData) {
                        if (row!!.actionTitle!!.trim().toLowerCase(Locale.ROOT)
                                .contains(charSearchCEOPastAction.toLowerCase(Locale.ROOT)) || row.actionMetric!!.displayName!!.trim().toLowerCase(
                                Locale.ROOT)
                                .contains(charSearchCEOPastAction.toLowerCase(Locale.ROOT)) || row.actionStatus!!.trim().toLowerCase(
                                Locale.ROOT).contains(charSearchCEOPastAction.toLowerCase(Locale.ROOT))
                        ) {
                            resultListCEOPastAction.add(row)
                        }
                    }
                    ceoPastActionList = resultListCEOPastAction
                }
                val filterResultsCEOPastAction = FilterResults()
                filterResultsCEOPastAction.values = ceoPastActionList
                return filterResultsCEOPastAction
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                ceoPastActionList = results?.values as List<CEOActionQuery.PastAction?>
                notifyDataSetChanged()
            }

        }
    }

}
