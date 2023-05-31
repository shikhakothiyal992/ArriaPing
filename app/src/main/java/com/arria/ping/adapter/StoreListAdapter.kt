package com.arria.ping.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.arria.ping.R
import com.arria.ping.model.StoreFilterPojo
import java.util.*

class StoreListAdapter(
    private var context: Context,
    private var storeFilterData: List<StoreFilterPojo>,
    private var action: String
) :
    RecyclerView.Adapter<StoreListAdapter.MultipleViewHolder>(), Filterable {

    var onClickStore: OnItemClickListener? = null
    var isAllChecked = 0
    var storeFilterList = listOf<StoreFilterPojo>()

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener?) {
        this.onClickStore = mOnItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultipleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.store_filter_list_item, parent, false)
        return MultipleViewHolder(view)
    }


    override fun getItemCount(): Int {
        return storeFilterData.size
    }


    override fun onBindViewHolder(holder: MultipleViewHolder, position: Int) {
        val storeViewHolder: MultipleViewHolder = holder
        val storeBean = storeFilterData[position]
        storeFilterList = storeFilterData
        storeViewHolder.mTvNameStore.text = storeBean.storeNumber
        storeViewHolder.itemView.isSelected = storeBean.isSelect

        when (isAllChecked) {
            2 -> {
                storeBean.isSelect = true
                storeViewHolder.mCheckBoxStore.isChecked =  storeBean.isSelect
            }
            1 -> {
                storeBean.isSelect = false
                storeViewHolder.mCheckBoxStore.isChecked =  storeBean.isSelect
            }
            else -> {
                storeViewHolder.mCheckBoxStore.isChecked = storeBean.isSelect
            }
        }

        if (onClickStore != null) storeViewHolder.mCheckBoxStore.setOnClickListener {
            isAllChecked = 0
            storeBean.isSelect = !storeBean.isSelect
            notifyDataSetChanged()
            onClickStore!!.onItemClick(storeBean)
        }
    }


    interface OnItemClickListener {
        fun onItemClick(bean: StoreFilterPojo)
    }


    inner class MultipleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTvNameStore: TextView = itemView.findViewById(R.id.store_filter_id)
        var mCheckBoxStore: CheckBox = itemView.findViewById(R.id.check_box_filter_item)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearchStore = constraint.toString()
                if (charSearchStore.isEmpty()) {
                    storeFilterList = storeFilterData
                } else {
                    val resultListStore = mutableListOf<StoreFilterPojo>()
                    for (row in storeFilterData) {
                        if (row.storeNumber.toLowerCase(Locale.ROOT).contains(charSearchStore.toLowerCase(Locale.ROOT))) {
                            resultListStore.add(row)
                        }
                    }
                    storeFilterList = resultListStore
                }
                val filterResultsStore = FilterResults()
                filterResultsStore.values = storeFilterList
                return filterResultsStore
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                storeFilterData = results?.values as List<StoreFilterPojo>
                notifyDataSetChanged()
            }

        }
    }
}
