package com.arria.ping.ui.actions.supervisor

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
import com.arria.ping.kpi.SupervisorActionQuery
import java.util.*

class SupervisorPastActionListAdapter(
    private var context: Context,
    private var supervisorPastActionListData: List<SupervisorActionQuery.PastAction?>,
) :
    RecyclerView.Adapter<SupervisorPastActionListAdapter.MultipleViewHolder>(), Filterable {
    var onClickSupervisorPastAction: OnItemClickListener? = null
    var supervisorPastActionList = listOf<SupervisorActionQuery.PastAction?>()

    fun setOnSupervisorPastActionItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.onClickSupervisorPastAction = mOnItemClickListener
    }
    init {
        supervisorPastActionList = supervisorPastActionListData
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gm_past_action_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return supervisorPastActionList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val supervisorPastActionViewHolder: MultipleViewHolder = holder
        val supervisorData = supervisorPastActionList[position]
        supervisorPastActionViewHolder.supervisorPastActionMainItem.text = supervisorData?.actionMetric!!.displayName
        supervisorPastActionViewHolder.supervisorPastActionMainItemUsage.text = supervisorData.actionTitle
        supervisorPastActionViewHolder.supervisorPastActionToPerform.text = supervisorData.actionStatus

        if (onClickSupervisorPastAction != null) supervisorPastActionViewHolder.supervisorPastActionParentItem.setOnClickListener {
            onClickSupervisorPastAction!!.onItemClick(position)

        }
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var supervisorPastActionMainItem: TextView = viewHolder.findViewById(R.id.action_main_item)
        var supervisorPastActionMainItemUsage: TextView = viewHolder.findViewById(R.id.action_main_item_usage)
        var supervisorPastActionToPerform: TextView = viewHolder.findViewById(R.id.action_to_perform)
        var supervisorPastActionParentItem: LinearLayout = viewHolder.findViewById(R.id.action_parent_item)

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearchSupervisorPastAction = constraint.toString()
                if (charSearchSupervisorPastAction.isEmpty()) {
                    supervisorPastActionList = supervisorPastActionListData
                } else {
                    val resultListSupervisorPastAction = mutableListOf<SupervisorActionQuery.PastAction?>()
                    for (row in supervisorPastActionListData) {
                        if (row!!.actionTitle!!.trim().toLowerCase(Locale.ROOT)
                                .contains(charSearchSupervisorPastAction.toLowerCase(Locale.ROOT)) || row.actionMetric!!.displayName!!.trim().toLowerCase(
                                Locale.ROOT)
                                .contains(charSearchSupervisorPastAction.toLowerCase(Locale.ROOT)) || row.actionStatus!!.trim().toLowerCase(
                                Locale.ROOT).contains(charSearchSupervisorPastAction.toLowerCase(Locale.ROOT))
                        ) {
                            resultListSupervisorPastAction.add(row)
                        }
                    }
                    supervisorPastActionList = resultListSupervisorPastAction
                }
                val filterResultsSupervisorPastAction = FilterResults()
                filterResultsSupervisorPastAction.values = supervisorPastActionList
                return filterResultsSupervisorPastAction
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                supervisorPastActionList = results?.values as List<SupervisorActionQuery.PastAction?>
                notifyDataSetChanged()
            }

        }
    }

}
