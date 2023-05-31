package com.arria.ping.ui.actions.do_

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
import com.arria.ping.kpi.DOActionQuery
import java.util.*

class DOPastActionListAdapter(
    private var context: Context,
    private var doPastActionListData: List<DOActionQuery.PastAction?>,
) :
    RecyclerView.Adapter<DOPastActionListAdapter.MultipleViewHolder>(), Filterable {
    var onClickDoPastAction: OnItemClickListener? = null
    var doPastActionList = listOf<DOActionQuery.PastAction?>()

    fun setOnDoPastActionItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.onClickDoPastAction = mOnItemClickListener
    }
    init {
        doPastActionList = doPastActionListData
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gm_past_action_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return doPastActionList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val doPastActionViewHolder: MultipleViewHolder = holder
        val doPastActionData = doPastActionList[position]
        doPastActionViewHolder.doPastActionMainItem.text = doPastActionData?.actionMetric!!.displayName
        doPastActionViewHolder.doPastActionMainItemUsage.text = doPastActionData.actionTitle
        doPastActionViewHolder.doPastActionToPerform.text = doPastActionData.actionStatus

        if (onClickDoPastAction != null) doPastActionViewHolder.doPastActionParentItem.setOnClickListener {
            onClickDoPastAction!!.onItemClick(position)

        }
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var doPastActionMainItem: TextView = viewHolder.findViewById(R.id.action_main_item)
        var doPastActionMainItemUsage: TextView = viewHolder.findViewById(R.id.action_main_item_usage)
        var doPastActionToPerform: TextView = viewHolder.findViewById(R.id.action_to_perform)
        var doPastActionParentItem: LinearLayout = viewHolder.findViewById(R.id.action_parent_item)

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearchDoPastAction = constraint.toString()
                if (charSearchDoPastAction.isEmpty()) {
                    doPastActionList = doPastActionListData
                } else {
                    val resultListDoPastAction = mutableListOf<DOActionQuery.PastAction?>()
                    for (row in doPastActionListData) {
                        if (row!!.actionTitle!!.trim().toLowerCase(Locale.ROOT)
                                .contains(charSearchDoPastAction.toLowerCase(Locale.ROOT)) || row.actionMetric!!.displayName!!.trim().toLowerCase(
                                Locale.ROOT)
                                .contains(charSearchDoPastAction.toLowerCase(Locale.ROOT)) || row.actionStatus!!.trim().toLowerCase(
                                Locale.ROOT).contains(charSearchDoPastAction.toLowerCase(Locale.ROOT))
                        ) {
                            resultListDoPastAction.add(row)
                        }
                    }
                    doPastActionList = resultListDoPastAction
                }
                val filterResultsDoPastAction = FilterResults()
                filterResultsDoPastAction.values = doPastActionList
                return filterResultsDoPastAction
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                doPastActionList = results?.values as List<DOActionQuery.PastAction?>
                notifyDataSetChanged()
            }

        }
    }

}
