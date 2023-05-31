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
import com.google.gson.Gson
import com.arria.ping.R
import com.arria.ping.kpi.CEOActionQuery
import com.arria.ping.kpi.DOActionQuery
import com.arria.ping.kpi.GMActionQuery
import java.util.*

class DOPastActionStoreListAdapter(
    private var context: Context,
    private var currentDoStoreActionListData: List<DOActionQuery.Store?>,
) :
    RecyclerView.Adapter<DOPastActionStoreListAdapter.MultipleViewHolder>(), Filterable {
    val gson = Gson()
    var onClickDoStoreAction : OnItemClickListener? = null
    var currentDoStoreActionList = listOf<DOActionQuery.Store?>()

    fun setOnItemDoStoreClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.onClickDoStoreAction = mOnItemClickListener
    }
    init {
        currentDoStoreActionList = currentDoStoreActionListData
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ceo_past_action_store_list_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return currentDoStoreActionList.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val doStoreActionViewHolder: MultipleViewHolder = holder
        val doStoreActionData = currentDoStoreActionList[position]
        doStoreActionViewHolder.doStoreNameWithStoreNumber.text = "Store"+" "+doStoreActionData!!.storeNumber+" "+" "+"-"+" "+" "+ doStoreActionData.storeName

        if (onClickDoStoreAction != null) doStoreActionViewHolder.doActionParentStoreItemList.setOnClickListener {
            onClickDoStoreAction!!.onItemClick(position)

        }
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var doStoreNameWithStoreNumber: TextView = viewHolder.findViewById(R.id.store_name_with_store_number)
        var doActionParentStoreItemList: LinearLayout = viewHolder.findViewById(R.id.action_parent_store_item_list)


    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearchDoStoreAction = constraint.toString()
                if (charSearchDoStoreAction.isEmpty()) {
                    currentDoStoreActionList = currentDoStoreActionListData
                } else {
                    val resultListDoStoreAction = mutableListOf<DOActionQuery.Store?>()
                    for (row in currentDoStoreActionListData) {
                        if (row!!.storeName!!.trim().toLowerCase(Locale.ROOT)
                                .contains(charSearchDoStoreAction.toLowerCase(Locale.ROOT)) || row.storeNumber!!.trim().toLowerCase(Locale.ROOT).contains(charSearchDoStoreAction.toLowerCase(Locale.ROOT))
                        ) {
                            resultListDoStoreAction.add(row)
                        }
                    }
                    currentDoStoreActionList = resultListDoStoreAction
                }
                val filterResultsDoStoreAction = FilterResults()
                filterResultsDoStoreAction.values = currentDoStoreActionList
                return filterResultsDoStoreAction
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                currentDoStoreActionList = results?.values as List<DOActionQuery.Store?>
                notifyDataSetChanged()
            }

        }
    }

}
