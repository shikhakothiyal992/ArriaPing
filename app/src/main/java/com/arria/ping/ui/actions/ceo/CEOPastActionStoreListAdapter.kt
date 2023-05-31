package com.arria.ping.ui.actions.ceo

import android.annotation.SuppressLint
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
import java.util.*

class CEOPastActionStoreListAdapter(
    private var context: Context,
    private var currentCEOStoreActionListData: List<CEOActionQuery.Store?>,
) :
    RecyclerView.Adapter<CEOPastActionStoreListAdapter.MultipleViewHolder>(), Filterable {
    val gson = Gson()
    var onClickCEOStoreAction: OnItemClickListener? = null
    var currentCEOStoreActionList = listOf<CEOActionQuery.Store?>()

    fun setOnItemCeoStoreClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.onClickCEOStoreAction = mOnItemClickListener
    }
    init {
        currentCEOStoreActionList = currentCEOStoreActionListData
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ceo_past_action_store_list_items, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return currentCEOStoreActionList.size
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val ceoStoreActionViewHolder: MultipleViewHolder = holder
        val ceoStoreActionData = currentCEOStoreActionList[position]
        ceoStoreActionViewHolder.ceoStoreNameWithStoreNumber.text = "Store"+" "+ceoStoreActionData!!.storeNumber+" "+" "+"-"+" "+" "+ ceoStoreActionData.storeName

        if (onClickCEOStoreAction != null) ceoStoreActionViewHolder.ceoActionParentStoreItemList.setOnClickListener {
            onClickCEOStoreAction!!.onItemClick(position)

        }
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    inner class MultipleViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {
        var ceoStoreNameWithStoreNumber: TextView = viewHolder.findViewById(R.id.store_name_with_store_number)
        var ceoActionParentStoreItemList: LinearLayout = viewHolder.findViewById(R.id.action_parent_store_item_list)


    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearchCeoStore = constraint.toString()
                if (charSearchCeoStore.isEmpty()) {
                    currentCEOStoreActionList = currentCEOStoreActionListData
                } else {
                    val resultListCeoStore = mutableListOf<CEOActionQuery.Store?>()
                    for (row in currentCEOStoreActionListData) {
                        if (row!!.storeName!!.trim().toLowerCase(Locale.ROOT)
                                .contains(charSearchCeoStore.toLowerCase(Locale.ROOT)) || row.storeNumber!!.trim().toLowerCase(Locale.ROOT).contains(charSearchCeoStore.toLowerCase(Locale.ROOT))
                        ) {
                            resultListCeoStore.add(row)
                        }
                    }
                    currentCEOStoreActionList = resultListCeoStore
                }
                val filterResultsCeoStore = FilterResults()
                filterResultsCeoStore.values = currentCEOStoreActionList
                return filterResultsCeoStore
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                currentCEOStoreActionList = results?.values as List<CEOActionQuery.Store?>
                notifyDataSetChanged()
            }

        }
    }

}
