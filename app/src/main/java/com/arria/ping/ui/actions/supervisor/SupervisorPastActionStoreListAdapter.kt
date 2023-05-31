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
import com.google.gson.Gson
import com.arria.ping.R
import com.arria.ping.kpi.CEOActionQuery
import com.arria.ping.kpi.SupervisorActionQuery
import java.util.*

class SupervisorPastActionStoreListAdapter(
    private var context: Context,
    private var currentSupervisorStoreActionListData: List<SupervisorActionQuery.Store?>,
) :
    RecyclerView.Adapter<SupervisorPastActionStoreListAdapter.MultipleViewHolder>(), Filterable {
    val gson = Gson()
    var onClickSupervisorStore : OnItemClickListener? = null
    var currentSupervisorStoreActionList = listOf<SupervisorActionQuery.Store?>()

    fun setOnItemClickLitener(mOnItemClickListener: OnItemClickListener?) {
        this.onClickSupervisorStore = mOnItemClickListener
    }
    init {
        currentSupervisorStoreActionList = currentSupervisorStoreActionListData
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ceo_past_action_store_list_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return currentSupervisorStoreActionList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val supervisorStoreViewHolder: MultipleViewHolder = holder
        val supervisorStoreData = currentSupervisorStoreActionList[position]
        supervisorStoreViewHolder.supervisorStoreNameWithStoreNumber.text = "Store"+" "+supervisorStoreData!!.storeNumber+" "+" "+"-"+" "+" "+ supervisorStoreData.storeName

        if (onClickSupervisorStore != null) supervisorStoreViewHolder.supervisorActionParentStoreItemList.setOnClickListener {
            onClickSupervisorStore!!.onItemClick(position)

        }
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var supervisorStoreNameWithStoreNumber: TextView = viewHolder.findViewById(R.id.store_name_with_store_number)
        var supervisorActionParentStoreItemList: LinearLayout = viewHolder.findViewById(R.id.action_parent_store_item_list)


    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearchSupervisorStore = constraint.toString()
                if (charSearchSupervisorStore.isEmpty()) {
                    currentSupervisorStoreActionList = currentSupervisorStoreActionListData
                } else {
                    val resultListSupervisorStore = mutableListOf<SupervisorActionQuery.Store?>()
                    for (row in currentSupervisorStoreActionListData) {
                        if (row!!.storeName!!.trim().toLowerCase(Locale.ROOT)
                                .contains(charSearchSupervisorStore.toLowerCase(Locale.ROOT)) || row.storeNumber!!.trim().toLowerCase(Locale.ROOT).contains(charSearchSupervisorStore.toLowerCase(Locale.ROOT))
                        ) {
                            resultListSupervisorStore.add(row)
                        }
                    }
                    currentSupervisorStoreActionList = resultListSupervisorStore
                }
                val filterResultsSupervisorStore = FilterResults()
                filterResultsSupervisorStore.values = currentSupervisorStoreActionList
                return filterResultsSupervisorStore
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                currentSupervisorStoreActionList = results?.values as List<SupervisorActionQuery.Store?>
                notifyDataSetChanged()
            }

        }
    }

}
